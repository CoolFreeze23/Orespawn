package danger.orespawn.entity.gait;

import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.network.SpiderGaitKeyframePayload;
import danger.orespawn.network.SpiderStepPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 2.0 spider overhaul (S2/S3): the modern gait controller for
 * {@link SpiderRobot}.
 *
 * <p><b>Architecture</b> (design doc D1): the SERVER owns the gait — planted
 * feet are server state, step decisions are server decisions, and (from S4)
 * the leg hitbox parts will be fed from this state. Clients receive compact
 * {@link SpiderStepPayload step events} plus periodic
 * {@link SpiderGaitKeyframePayload keyframes} and REPLAY the deterministic
 * swing locally, converting foot positions to the classic
 * {@link RenderSpiderRobotInfo} angle fields every client tick — the shipped
 * models and renderer are consumed unchanged. The mode itself is the
 * server's construction-time snapshot, carried to clients on the entity's
 * synched data (never the client's own config — see SpiderRobot).</p>
 *
 * <p><b>Gait</b> (S1 reference technique, walk-only per design ruling Q2):
 * a planted foot steps when it drifts outside a speed-widened trigger radius
 * around its rest target, gated by inhibitors — the mirrored pair partner and
 * the same-side neighbors must be planted, and a fresh-landed foot holds a
 * short cooldown. Timers never *initiate* steps (distance does); they only
 * inhibit. The swing target leads the rest target by the body velocity
 * projected over the swing duration (with one fixed-point refinement, since
 * the duration itself depends on the displaced target), so feet land where
 * the rest point will be, not where it was.</p>
 *
 * <p><b>Terrain (S3):</b> footing comes from a 3x3 biased column scan
 * ({@link #scanFooting}) — nine neighbor columns, every walkable surface in
 * a vertical window around body level, scored against a preferred point
 * whose height rises by {@link #CLIMB_HEIGHT_BIAS} when the column directly
 * ahead of the body is blocked (the reference's ledge/wall climb assist). A
 * leg whose scan finds nothing goes STRANDED: it dangles semi-folded below
 * its hip, follows the body, claims no ground contact, and re-steps
 * unconditionally (no inhibitors) the moment any footing appears. Stairs
 * and slopes are emergent from the scan; no special-case code.</p>
 *
 * <p><b>Vertical retrigger × climb assist</b> (owner-flagged interaction):
 * both pull on the same lever — the S2 livelock guard suppresses re-steps
 * that don't change the footing, while the climb assist WANTS re-steps onto
 * higher ground. Resolution: a vertical-only retrigger fires only when the
 * best candidate strictly IMPROVES the |footY − bodyY| mismatch by at least
 * {@link #VERTICAL_RETRIGGER_GAIN} — a ledge column that keeps returning
 * the same off-level footing stays suppressed (no livelock), while a
 * genuinely better (bias-preferred) footing passes the same test by
 * construction, because improving candidates are exactly what the biased
 * scoring surfaces. Drift-triggered steps are never gated on improvement.</p>
 *
 * <p><b>Trample (S3):</b> classic tramples grass client-side when a ridden
 * spider's foot settles ({@code SpiderRobot.updateLegs:639-649}, faithful to
 * 1.7.10's client-only quirk — that path is untouched). Modern mode runs the
 * SAME trigger server-side: on foot touchdown while ridden, mobGriefing
 * permitting, with classic's exact block logic including its int-truncation
 * of the foot coordinates (which differs from flooring at negative coords —
 * quirk mirrored deliberately).</p>
 *
 * <p><b>One tick, one solve:</b> both sides run
 * {@link #solveLegAngles} — pure math on (body pos, yaw, foot pos) — so the
 * client's replayed pose is the server's pose wherever the synced inputs
 * match. Stranded dangles are recomputed from body state on each side
 * independently (deterministic; no per-tick sync). Swing replay clocks the
 * client's game time against the server's step timestamps; the ≤ few-tick
 * clock skew shows as a slightly late swing start (clamped at progress 0)
 * and is corrected by keyframes — accepted for S2+.</p>
 *
 * <p><b>Still deliberately out of scope:</b> body height float and
 * pitch/roll (S3b); hitbox parts (S4); ant rig and ridden steering (S5).
 * The controller never moves or collides the BODY — the server-visible
 * behavior deltas of modern mode are the gait packets and the (gamerule-
 * gated, ridden-only) trample block changes.</p>
 */
public final class ModernSpiderGait {

    // ---- Gait tuning (initial tune, S2/S3; revisit against ride feel in S5) ----
    /** Trigger radius around the rest target while standing, blocks. */
    static final double TRIGGER_RADIUS_MIN = 2.0;
    /** Trigger radius at full speed, blocks (reference: radius lerps with speed). */
    static final double TRIGGER_RADIUS_MAX = 5.0;
    /** Speed treated as "full" for the radius lerp — the rig's movement-speed attribute. */
    static final double FULL_SPEED = 0.35;
    /** Foot travel speed during a swing, blocks/tick (sets step duration). */
    static final double STEP_SPEED = 1.1;
    static final int MIN_STEP_TICKS = 4;
    static final int MAX_STEP_TICKS = 12;
    /** Parabolic swing lift peak, blocks (reference 4t(1-t) profile). */
    static final double LIFT_HEIGHT = 2.0;
    /** Ticks a fresh-landed foot (or its pair partner) refuses to step again. */
    static final int LAND_COOLDOWN_TICKS = 3;
    /** Vertical foot-vs-body mismatch that can force a re-step, blocks. */
    static final double VERTICAL_RETRIGGER = 2.0;
    /** A vertical-only re-step must improve the mismatch at least this much. */
    static final double VERTICAL_RETRIGGER_GAIN = 0.5;
    /** Periodic full-state broadcast interval, ticks (drift snap + late joiners). */
    static final int KEYFRAME_INTERVAL = 40;

    // ---- Terrain scan tuning (S3) ----
    /**
     * Scan window above/below body level, blocks — classic's own probe
     * column ({@code SpiderRobot.findNewFooting:717}: yScan 11 → −14). A
     * shorter window (first S3a cut used 4/−8) cannot see a cliff-wall top
     * beside a landed body, stranding rear legs the classic probe would
     * plant up the wall (independent-review regression, cliff test).
     */
    static final int SCAN_UP = 11;
    static final int SCAN_DOWN = 14;
    /** Preferred-footing height raise when the column ahead is blocked. */
    static final double CLIMB_HEIGHT_BIAS = 1.5;
    /** Candidates are rejected beyond this fraction of full leg reach. */
    static final double REACH_MARGIN = 0.98;
    /** Stranded dangle: horizontal fraction of rest reach, and drop below hip. */
    static final double DANGLE_REACH_FRAC = 0.45;
    static final double DANGLE_DROP = 4.0;
    /** Lookahead ticks when probing footing for a stranded leg. */
    static final int STRANDED_LOOKAHEAD_TICKS = 8;
    /**
     * Reach-contraction fractions of rest reach tried (in order) when the
     * primary target's patch has no footing — the modern analogue of the
     * classic probe's 16→3.5 sweep (SpiderRobot.findNewFooting:711-745).
     * Independent-review BLOCKER: without contraction, narrow bridges and
     * ridges stranded every leg whose fixed-distance rest column was off
     * the terrain, while classic grips near the hip.
     */
    private static final double[] CONTRACTION_FRACTIONS = {0.7, 0.45, 0.25};
    /** Contraction never probes closer than this to the hip, blocks (classic floor 3.5). */
    static final double MIN_CONTRACTED_REACH = 3.5;
    /** Ticks a gate-blocked vertical retrigger waits before rescanning. */
    static final int VERTICAL_RETRY_COOLDOWN = 10;

    private static final int LEGS = SpiderRigProfile.LEG_COUNT;

    // ---- Per-leg state (server-authoritative; client mirrors via payloads) ----
    private final double[] footX = new double[LEGS];
    private final double[] footY = new double[LEGS];
    private final double[] footZ = new double[LEGS];
    private final boolean[] grounded = new boolean[LEGS];
    private final boolean[] swinging = new boolean[LEGS];
    private final boolean[] stranded = new boolean[LEGS];
    private final double[] fromX = new double[LEGS];
    private final double[] fromY = new double[LEGS];
    private final double[] fromZ = new double[LEGS];
    private final double[] toX = new double[LEGS];
    private final double[] toY = new double[LEGS];
    private final double[] toZ = new double[LEGS];
    private final long[] swingStart = new long[LEGS];
    private final int[] swingDuration = new int[LEGS];
    private final long[] lastLand = new long[LEGS];
    /** Per-leg wait-until time for re-evaluating a gate-blocked vertical retrigger. */
    private final long[] verticalRetryAt = new long[LEGS];
    private boolean initialized = false;

    // Scratch buffers for the per-leg solve (single-threaded per side).
    private final double[][] jointScratch = {new double[2], new double[2], new double[2], new double[2]};
    private final double[] angleScratch = new double[5];

    // ---- Test/S4 accessors (server state) ----
    public boolean isGrounded(int leg) {
        return grounded[leg] && !swinging[leg] && !stranded[leg];
    }

    public boolean isSwinging(int leg) {
        return swinging[leg];
    }

    public boolean isStranded(int leg) {
        return stranded[leg];
    }

    public double footX(int leg) {
        return footX[leg];
    }

    public double footY(int leg) {
        return footY[leg];
    }

    public double footZ(int leg) {
        return footZ[leg];
    }

    /** The speed-widened trigger radius (exposed pure for the gait tests). */
    public static double triggerRadius(double bodySpeed) {
        double speedFrac = Math.min(1.0, bodySpeed / FULL_SPEED);
        return Mth.lerp(speedFrac, TRIGGER_RADIUS_MIN, TRIGGER_RADIUS_MAX);
    }

    /**
     * First-tick initialization: plant every foot at its rest target on
     * whatever footing the scan finds; a rest column over a void (spawned on
     * a cliff edge) starts stranded rather than fake-grounded in mid-air.
     * Runs lazily on both sides (the client's copy is overwritten by the
     * first keyframe).
     */
    private void initFeet(SpiderRobot spider) {
        float yaw = spider.getYRot();
        long time = spider.level().getGameTime();
        for (int leg = 0; leg < LEGS; ++leg) {
            double rx = SpiderRigProfile.restFootX(leg, spider.getX(), yaw);
            double rz = SpiderRigProfile.restFootZ(leg, spider.getZ(), yaw);
            double[] found = scanFootingContracted(spider, leg, rx, rz, 0.0, 0.0, 0);
            if (found != null) {
                footX[leg] = found[0];
                footY[leg] = found[1];
                footZ[leg] = found[2];
                grounded[leg] = true;
                stranded[leg] = false;
            } else {
                stranded[leg] = true;
                grounded[leg] = false;
                dangle(spider, leg, yaw);
            }
            swinging[leg] = false;
            lastLand[leg] = time;
        }
        initialized = true;
    }

    /** Places a stranded foot at its semi-folded dangle point (follows the body). */
    private void dangle(SpiderRobot spider, int leg, float yaw) {
        double bearing = SpiderRigProfile.legBearing(leg, yaw);
        double reach = SpiderRigProfile.restReach(leg) * DANGLE_REACH_FRAC;
        footX[leg] = SpiderRigProfile.hipX(leg, spider.getX(), yaw) - reach * Math.sin(bearing);
        footZ[leg] = SpiderRigProfile.hipZ(leg, spider.getZ(), yaw) + reach * Math.cos(bearing);
        footY[leg] = SpiderRigProfile.hipY(leg, spider.getY()) - DANGLE_DROP;
    }

    // ==================== SERVER ====================

    /** One server gait tick; called from {@code SpiderRobot.tick()} (modern mode, server side). */
    public void serverTick(SpiderRobot spider) {
        if (!initialized) {
            initFeet(spider);
        }
        Level level = spider.level();
        long time = level.getGameTime();

        // Land finished swings.
        for (int leg = 0; leg < LEGS; ++leg) {
            if (swinging[leg] && time - swingStart[leg] >= swingDuration[leg]) {
                land(leg, time);
            }
        }
        // Trample sweep (server-side, S3): classic tramples on EVERY tick a
        // ridden leg is settled, not just on touchdown (SpiderRobot.updateLegs
        // :697-711 runs whenever all three axes settle) — mirrored here per
        // independent review so a rider mounting a standing spider, or grass
        // regrowing under a planted foot, tramples exactly as in classic.
        if (spider.getFirstPassenger() != null
                && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for (int leg = 0; leg < LEGS; ++leg) {
                if (grounded[leg] && !swinging[leg] && !stranded[leg]) {
                    trampleAt(spider, leg);
                }
            }
        }

        // Body speed drives the trigger radius (blocks moved this tick).
        double vx = spider.getX() - spider.xo;
        double vz = spider.getZ() - spider.zo;
        double speed = Math.sqrt(vx * vx + vz * vz);
        double radius = triggerRadius(speed);

        float yaw = spider.getYRot();
        for (int leg = 0; leg < LEGS; ++leg) {
            if (swinging[leg]) {
                continue;
            }
            double restX = SpiderRigProfile.restFootX(leg, spider.getX(), yaw);
            double restZ = SpiderRigProfile.restFootZ(leg, spider.getZ(), yaw);

            if (stranded[leg]) {
                // Dangle follows the body; re-step UNCONDITIONALLY (no
                // inhibitors, per the reference) onto any footing that appears
                // — including contracted footing near the hip.
                dangle(spider, leg, yaw);
                double[] found = scanFootingContracted(spider, leg,
                        restX + vx * STRANDED_LOOKAHEAD_TICKS,
                        restZ + vz * STRANDED_LOOKAHEAD_TICKS, vx, vz, STRANDED_LOOKAHEAD_TICKS);
                if (found != null) {
                    beginStep(spider, leg, found[0], found[1], found[2], time);
                }
                continue;
            }

            if (!stepAllowed(leg, time)) {
                continue;
            }
            double dx = footX[leg] - restX;
            double dz = footZ[leg] - restZ;
            boolean drift = dx * dx + dz * dz > radius * radius;
            boolean verticalMismatch = time >= verticalRetryAt[leg]
                    && Math.abs(footY[leg] - spider.getY()) > VERTICAL_RETRIGGER;
            if (!drift && !verticalMismatch) {
                continue;
            }
            // Velocity-projected lookahead with one fixed-point refinement
            // (duration depends on the target which depends on the duration).
            // The refined scan is a fallback, never an override: a valid
            // first candidate is kept when the further-projected patch is
            // empty (review finding — the overwrite spuriously stranded legs
            // at cliff lips).
            double drift2d = Math.sqrt(dx * dx + dz * dz);
            int est = Mth.clamp((int) Math.round(drift2d / STEP_SPEED), MIN_STEP_TICKS, MAX_STEP_TICKS);
            double[] cand = scanFootingContracted(spider, leg, restX + vx * est, restZ + vz * est, vx, vz, est);
            if (cand != null) {
                double stepDist = Math.sqrt((cand[0] - footX[leg]) * (cand[0] - footX[leg])
                        + (cand[1] - footY[leg]) * (cand[1] - footY[leg])
                        + (cand[2] - footZ[leg]) * (cand[2] - footZ[leg]));
                int est2 = Mth.clamp((int) Math.round(stepDist / STEP_SPEED), MIN_STEP_TICKS, MAX_STEP_TICKS);
                if (est2 != est) {
                    double[] refined = scanFooting(spider, leg, restX + vx * est2, restZ + vz * est2,
                            vx, vz, est2);
                    if (refined != null) {
                        cand = refined;
                    }
                }
            }
            if (cand == null) {
                // Neither the projected patch nor the contracted sweep down
                // to ~3.5 blocks from the hip found footing: strand.
                strand(spider, leg, yaw);
                continue;
            }
            if (!drift) {
                // Vertical-only retrigger: must strictly improve the mismatch
                // (livelock guard vs climb assist — see class javadoc). A
                // blocked attempt arms a short cooldown so an idle spider by
                // a ledge doesn't re-scan ~230 blocks per leg every tick.
                double curMismatch = Math.abs(footY[leg] - spider.getY());
                double newMismatch = Math.abs(cand[1] - spider.getY());
                if (curMismatch - newMismatch < VERTICAL_RETRIGGER_GAIN) {
                    verticalRetryAt[leg] = time + VERTICAL_RETRY_COOLDOWN;
                    continue;
                }
            }
            beginStep(spider, leg, cand[0], cand[1], cand[2], time);
        }

        // Keyframes phase-shifted by entity id so multiple spiders don't
        // burst-send on the same global tick.
        if ((time + spider.getId()) % KEYFRAME_INTERVAL == 0) {
            PacketDistributor.sendToPlayersTrackingEntity(spider, buildKeyframe(spider));
        }
    }

    /**
     * Step inhibitors (reference technique: timers inhibit, distance
     * triggers): the mirrored pair partner and both same-side neighbors must
     * be planted, and neither this leg nor its partner may have landed within
     * the cooldown window. Stranded legs bypass this entirely.
     */
    private boolean stepAllowed(int leg, long time) {
        int partner = SpiderRigProfile.pairedWith(leg);
        if (swinging[partner]) {
            return false;
        }
        // Same-side neighbors: even legs are one side, odd the other; the
        // front-to-rear neighbor indices differ by 2 (design doc §1 gait).
        int fore = leg - 2;
        int aft = leg + 2;
        if (fore >= 0 && swinging[fore]) {
            return false;
        }
        if (aft < LEGS && swinging[aft]) {
            return false;
        }
        return time - lastLand[leg] >= LAND_COOLDOWN_TICKS
                && time - lastLand[partner] >= LAND_COOLDOWN_TICKS;
    }

    private void beginStep(SpiderRobot spider, int leg, double tx, double ty, double tz, long time) {
        stranded[leg] = false;
        fromX[leg] = footX[leg];
        fromY[leg] = footY[leg];
        fromZ[leg] = footZ[leg];
        toX[leg] = tx;
        toY[leg] = ty;
        toZ[leg] = tz;
        double dist = Math.sqrt(
                (tx - fromX[leg]) * (tx - fromX[leg])
                        + (ty - fromY[leg]) * (ty - fromY[leg])
                        + (tz - fromZ[leg]) * (tz - fromZ[leg]));
        swingDuration[leg] = Mth.clamp((int) Math.round(dist / STEP_SPEED), MIN_STEP_TICKS, MAX_STEP_TICKS);
        swingStart[leg] = time;
        swinging[leg] = true;
        grounded[leg] = false;
        if (!spider.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(spider, new SpiderStepPayload(
                    spider.getId(), leg, false,
                    fromX[leg], fromY[leg], fromZ[leg],
                    tx, ty, tz,
                    time, swingDuration[leg]));
        }
    }

    /** Server: marks a leg stranded and tells trackers (the dangle itself is computed per side). */
    private void strand(SpiderRobot spider, int leg, float yaw) {
        stranded[leg] = true;
        grounded[leg] = false;
        swinging[leg] = false;
        dangle(spider, leg, yaw);
        if (!spider.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(spider, new SpiderStepPayload(
                    spider.getId(), leg, true,
                    footX[leg], footY[leg], footZ[leg],
                    footX[leg], footY[leg], footZ[leg],
                    spider.level().getGameTime(), MIN_STEP_TICKS));
        }
    }

    private void land(int leg, long time) {
        footX[leg] = toX[leg];
        footY[leg] = toY[leg];
        footZ[leg] = toZ[leg];
        swinging[leg] = false;
        grounded[leg] = true;
        lastLand[leg] = time;
    }

    /**
     * S3 server-side trample — classic's exact trigger cadence and block
     * logic ({@code SpiderRobot.updateLegs:639-649}: EVERY tick a leg is
     * settled while ridden + mobGriefing; SHORT_GRASS at the foot to air,
     * GRASS_BLOCK below to dirt), including classic's int-truncated foot
     * coordinates (differs from flooring at negatives — orig :372 quirk,
     * mirrored deliberately; note modern foot Y is integral-or-shape-exact
     * so the Y half of the quirk is mostly vacuous here, while the XZ half
     * stays live). Classic mode keeps its faithful client-side trample
     * untouched — on a dedicated server classic tramples nothing, faithfully.
     */
    private void trampleAt(SpiderRobot spider, int leg) {
        Level level = spider.level();
        if (level.isClientSide() || spider.getFirstPassenger() == null
                || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        BlockPos footPos = new BlockPos((int) footX[leg], (int) footY[leg], (int) footZ[leg]);
        if (level.getBlockState(footPos).is(Blocks.SHORT_GRASS)) {
            level.setBlock(footPos, Blocks.AIR.defaultBlockState(), 3);
        }
        BlockPos belowFoot = footPos.below();
        if (level.getBlockState(belowFoot).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(belowFoot, Blocks.DIRT.defaultBlockState(), 3);
        }
    }

    /**
     * Scan with classic-style reach contraction: try the (possibly velocity-
     * displaced) primary target's patch first; when it is dry, sweep
     * contracted targets back along the leg's neutral bearing toward the hip
     * ({@link #CONTRACTION_FRACTIONS} of rest reach, floored at
     * {@link #MIN_CONTRACTED_REACH}) — the modern analogue of the classic
     * probe contracting 16→3.5 (SpiderRobot.findNewFooting:711-745). Only
     * when the whole annulus is dry does the caller strand the leg; a
     * narrow bridge or ridge under the body therefore grips near the hips
     * exactly as classic does.
     */
    private double[] scanFootingContracted(SpiderRobot spider, int leg, double tx, double tz,
                                           double vx, double vz, int projTicks) {
        double[] found = scanFooting(spider, leg, tx, tz, vx, vz, projTicks);
        if (found != null) {
            return found;
        }
        float yaw = spider.getYRot();
        double bearing = SpiderRigProfile.legBearing(leg, yaw);
        double hipX = SpiderRigProfile.hipX(leg, spider.getX(), yaw);
        double hipZ = SpiderRigProfile.hipZ(leg, spider.getZ(), yaw);
        for (double frac : CONTRACTION_FRACTIONS) {
            double r = Math.max(MIN_CONTRACTED_REACH, SpiderRigProfile.restReach(leg) * frac);
            double cx = hipX - r * Math.sin(bearing);
            double cz = hipZ + r * Math.cos(bearing);
            found = scanFooting(spider, leg, cx, cz, vx, vz, 0);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * S3 terrain scan: nine columns (±1 around the target) over a vertical
     * window around body level; every walkable surface (a solid block with a
     * non-solid block above, standing height taken from its collision-shape
     * top so slab treads carry feet at +0.5, not a floating +1.0; shapes
     * taller than a full cube — fence posts — are not footing) is a
     * candidate, scored by squared distance to the preferred point — the
     * target XZ at body level, raised by {@link #CLIMB_HEIGHT_BIAS} when
     * the column directly ahead of the BODY at body level is blocked (the
     * ledge/wall climb assist). Candidates beyond {@link #REACH_MARGIN} of
     * full leg reach — measured from the hip PROJECTED {@code projTicks}
     * along the body velocity, matching the target's own displacement
     * (review finding: a current-hip anchor was over-strict by up to
     * v·est) — are ignored. Returns {@code {x, y, z}} of the best footing
     * or {@code null}.
     *
     * <p>Discoverable surfaces span {@code [floor(bodyY)-SCAN_DOWN,
     * floor(bodyY)+SCAN_UP]} in foot-height space. The multi-surface column
     * walk (not just first-from-top) is what retires S2's wall-column
     * pathology: a column inside a wall offers only a high out-of-preference
     * surface, and the neighbor columns outscore it instead of the leg
     * thrashing against it.</p>
     */
    private double[] scanFooting(SpiderRobot spider, int leg, double tx, double tz,
                                 double vx, double vz, int projTicks) {
        Level level = spider.level();
        double bodyY = spider.getY();
        float yaw = spider.getYRot();
        double hipX = SpiderRigProfile.hipX(leg, spider.getX() + vx * projTicks, yaw);
        double hipY = SpiderRigProfile.hipY(leg, bodyY);
        double hipZ = SpiderRigProfile.hipZ(leg, spider.getZ() + vz * projTicks, yaw);
        double maxReachSq = SpiderRigProfile.MAX_REACH * REACH_MARGIN
                * SpiderRigProfile.MAX_REACH * REACH_MARGIN;

        // Climb assist: preferred footing height rises when the body's path
        // is blocked at chest height.
        double preferY = bodyY;
        double speed = Math.sqrt(vx * vx + vz * vz);
        if (speed > 0.05) {
            BlockPos ahead = BlockPos.containing(
                    spider.getX() + vx / speed * 2.5,
                    bodyY + 0.5,
                    spider.getZ() + vz / speed * 2.5);
            if (level.getBlockState(ahead).isSolid()) {
                preferY += CLIMB_HEIGHT_BIAS;
            }
        }

        // Foot-height space [floor-SCAN_DOWN, floor+SCAN_UP] — block rows
        // [bottom, top] with the occupancy seed one above the window.
        int top = Mth.floor(bodyY) + SCAN_UP - 1;
        int bottom = Mth.floor(bodyY) - SCAN_DOWN - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double bestScore = Double.MAX_VALUE;
        double[] best = null;
        for (int ddx = -1; ddx <= 1; ++ddx) {
            for (int ddz = -1; ddz <= 1; ++ddz) {
                double cx = tx + ddx;
                double cz = tz + ddz;
                int bx = Mth.floor(cx);
                int bz = Mth.floor(cz);
                boolean aboveSolid = level.getBlockState(pos.set(bx, top + 1, bz)).isSolid();
                for (int y = top; y >= bottom; --y) {
                    BlockState state = level.getBlockState(pos.set(bx, y, bz));
                    boolean solid = state.isSolid();
                    if (solid && !aboveSolid) {
                        VoxelShape shape = state.getCollisionShape(level, pos);
                        double topOffset = shape.isEmpty() ? 1.0 : shape.max(Direction.Axis.Y);
                        if (topOffset <= 1.01) {
                            double candY = y + topOffset;
                            double rdx = cx - hipX;
                            double rdy = candY - hipY;
                            double rdz = cz - hipZ;
                            if (rdx * rdx + rdy * rdy + rdz * rdz <= maxReachSq) {
                                double sdx = cx - tx;
                                double sdy = candY - preferY;
                                double sdz = cz - tz;
                                double score = sdx * sdx + sdy * sdy + sdz * sdz;
                                if (score < bestScore) {
                                    bestScore = score;
                                    best = new double[]{cx, candY, cz};
                                }
                            }
                        }
                    }
                    aboveSolid = solid;
                }
            }
        }
        return best;
    }

    /**
     * Serializes the full per-leg state for keyframe sync. Self-initializes
     * first: a player can start tracking a fresh spawn before its first
     * server tick, and the all-zero placeholder must never reach the wire.
     */
    public SpiderGaitKeyframePayload buildKeyframe(SpiderRobot spider) {
        if (!initialized) {
            initFeet(spider);
        }
        double[] xs = new double[LEGS];
        double[] ys = new double[LEGS];
        double[] zs = new double[LEGS];
        boolean[] g = new boolean[LEGS];
        boolean[] s = new boolean[LEGS];
        for (int leg = 0; leg < LEGS; ++leg) {
            // A swinging leg keyframes its TARGET: a late-joining client sees
            // the landed pose one swing early rather than a stale origin.
            xs[leg] = swinging[leg] ? toX[leg] : footX[leg];
            ys[leg] = swinging[leg] ? toY[leg] : footY[leg];
            zs[leg] = swinging[leg] ? toZ[leg] : footZ[leg];
            g[leg] = !swinging[leg] && grounded[leg];
            s[leg] = stranded[leg];
        }
        return new SpiderGaitKeyframePayload(spider.getId(), xs, ys, zs, g, s);
    }

    // ==================== CLIENT ====================

    /** Applies a received step event or strand transition (client, main thread). */
    public void applyStep(SpiderStepPayload payload) {
        int leg = payload.leg();
        if (payload.strand()) {
            stranded[leg] = true;
            swinging[leg] = false;
            grounded[leg] = false;
            footX[leg] = payload.toX();
            footY[leg] = payload.toY();
            footZ[leg] = payload.toZ();
            initialized = true;
            return;
        }
        boolean wasStranded = stranded[leg];
        stranded[leg] = false;
        if (wasStranded) {
            // Un-strand pop fix (review): the client has been rendering its
            // OWN dangle (recomputed from interpolation-lagged yaw/pos), so
            // the swing departs from where the foot actually is; the target
            // stays server-authoritative and the landed positions converge.
            fromX[leg] = footX[leg];
            fromY[leg] = footY[leg];
            fromZ[leg] = footZ[leg];
        } else {
            fromX[leg] = payload.fromX();
            fromY[leg] = payload.fromY();
            fromZ[leg] = payload.fromZ();
        }
        toX[leg] = payload.toX();
        toY[leg] = payload.toY();
        toZ[leg] = payload.toZ();
        swingStart[leg] = payload.startTime();
        swingDuration[leg] = payload.duration();
        swinging[leg] = true;
        grounded[leg] = false;
        initialized = true;
    }

    /** Applies a received keyframe (client, main thread): snaps planted feet. */
    public void applyKeyframe(SpiderGaitKeyframePayload payload) {
        for (int leg = 0; leg < LEGS; ++leg) {
            if (payload.stranded()[leg]) {
                stranded[leg] = true;
                swinging[leg] = false;
                grounded[leg] = false;
                // Dangle position is recomputed locally each tick.
                continue;
            }
            stranded[leg] = false;
            if (swinging[leg]) {
                // Mid-swing: retarget the swing to the keyframed foot rather
                // than teleporting it (the landed positions converge anyway).
                toX[leg] = payload.footX()[leg];
                toY[leg] = payload.footY()[leg];
                toZ[leg] = payload.footZ()[leg];
            } else {
                footX[leg] = payload.footX()[leg];
                footY[leg] = payload.footY()[leg];
                footZ[leg] = payload.footZ()[leg];
                grounded[leg] = payload.grounded()[leg];
            }
        }
        initialized = true;
    }

    /**
     * One client gait tick; called from {@code SpiderRobot.tick()} (modern
     * mode, client side) — replays swings, follows stranded dangles, and
     * writes the classic render fields the shipped model consumes.
     */
    public void clientTick(SpiderRobot spider) {
        if (!initialized) {
            initFeet(spider); // placeholder pose until the first keyframe
        }
        long time = spider.level().getGameTime();
        float yaw = spider.getYRot();
        RenderSpiderRobotInfo r = spider.getRenderSpiderRobotInfo();
        ++r.gpcounter; // classic frame counter — keeps the jaw-snap animation alive
        for (int leg = 0; leg < LEGS; ++leg) {
            double fx;
            double fy;
            double fz;
            if (stranded[leg]) {
                dangle(spider, leg, yaw);
                fx = footX[leg];
                fy = footY[leg];
                fz = footZ[leg];
            } else if (swinging[leg]) {
                double progress = (time - swingStart[leg]) / (double) swingDuration[leg];
                if (progress >= 1.0) {
                    land(leg, time);
                    fx = footX[leg];
                    fy = footY[leg];
                    fz = footZ[leg];
                } else {
                    progress = Math.max(0.0, progress);
                    fx = Mth.lerp(progress, fromX[leg], toX[leg]);
                    fz = Mth.lerp(progress, fromZ[leg], toZ[leg]);
                    // Parabolic lift over the endpoint lerp (reference 4t(1-t)).
                    fy = Mth.lerp(progress, fromY[leg], toY[leg])
                            + LIFT_HEIGHT * 4.0 * progress * (1.0 - progress);
                }
            } else {
                fx = footX[leg];
                fy = footY[leg];
                fz = footZ[leg];
            }
            solveLegAngles(spider.getX(), spider.getY(), spider.getZ(), yaw,
                    leg, fx, fy, fz, jointScratch, angleScratch);
            r.ydisplayangle[leg] = (float) angleScratch[0];
            r.uddisplayangle[leg] = (float) angleScratch[1];
            r.p1xangle[leg] = angleScratch[2];
            r.p2xangle[leg] = angleScratch[3];
            r.p3xangle[leg] = angleScratch[4];
            // Classic bookkeeping fields read by the crosshair-era overlays
            // and harmless to mirror: current foot + hip world anchors.
            r.foot_xpos[leg] = (float) fx;
            r.foot_ypos[leg] = (float) fy;
            r.foot_zpos[leg] = (float) fz;
            r.realposx[leg] = (float) SpiderRigProfile.hipX(leg, spider.getX(), yaw);
            r.realposy[leg] = (float) SpiderRigProfile.hipY(leg, spider.getY());
            r.realposz[leg] = (float) SpiderRigProfile.hipZ(leg, spider.getZ(), yaw);
            r.footup[leg] = swinging[leg] || stranded[leg] ? 1 : 0;
        }
    }

    // ==================== THE CONVERSION (design doc D2) ====================

    /**
     * World foot position → the classic model angle fields, exactly.
     *
     * <p><b>Geometry.</b> The model poses a leg as a planar 3-segment chain:
     * every segment shares one yaw ({@code ydisplayangle}) and segment
     * {@code i} extends along model direction
     * {@code (cos(a_i)·sin(yd), −sin(a_i), cos(a_i)·cos(yd))·99px} with
     * {@code a_i = p_i_xangle + uddisplayangle}
     * (ModelSpiderRobot.poseLeg:334-344). So {@code a_i} is simply the
     * segment's elevation above horizontal inside the leg's vertical plane —
     * and any planar chain is exactly representable.</p>
     *
     * <p><b>Model↔world azimuth.</b> Matching the model hip placement
     * ({@code x=−cos(ymid)·legoff·16, z=+sin(ymid)·legoff·16},
     * ModelSpiderRobot.poseLeg:325-327) against the classic world hip
     * ({@code x=−legoff·sin(yawRad'+ymid), z=+legoff·cos(yawRad'+ymid)},
     * SpiderRobot.updateLegs:516-519) gives the render transform's horizontal
     * action: a reflection {@code α_world = yawRad − α_model} (with
     * {@code α = atan2(z, x)}, {@code yawRad = toRadians(wrapDegrees(yRot))});
     * vertical: {@code Δy_world = −Δy_model/16} for VECTORS. Substituting
     * back yields {@code yd = α_world(hip→foot) − yawRad + π/2} — which is
     * algebraically identical to the classic solver's own display formula
     * ({@code ydisplay = ycurrent − yawRad − π/2} with {@code ycurrent} the
     * foot→hip azimuth, SpiderRobot.updateLegs:626-627). The classic code
     * corroborates the mapping; the S2 harness verifies it numerically.</p>
     *
     * <p><b>Two shared, faithful absolute-space caveats</b> (independent
     * review, owner-ratified as faithful — do NOT "fix" in any slice): the
     * mapping is exact for vectors and angles, but the vanilla renderer
     * additionally translates the whole model by a constant (+1.501 blocks
     * vertical at render), and rotates by the interpolated BODY yaw while
     * this conversion — like classic's updateLegs, which uses
     * {@code getYRot()} in the identical places — poses against entity yaw.
     * Both offsets exist identically in classic (1.7.10 included), cancel
     * from every angle and delta this solver computes, and are preserved.</p>
     *
     * <p><b>Angle split.</b> The model only consumes the sums
     * {@code p_i + ud}, so the split is one-parameter redundant; classic
     * always uses {@code p2 = 0} (SpiderRobot.updateLegs:566), so we adopt
     * the same convention: {@code ud = a_2}, {@code p1 = a_1 − a_2},
     * {@code p2 = 0}, {@code p3 = a_3 − a_2} — modern poses stay inside the
     * classic field distribution.</p>
     *
     * @param joints scratch {@code double[4][2]} for the FABRIK solve
     * @param out    {@code [yd, ud, p1, p2, p3]} (radians; yd/ud floats upstream)
     */
    public static void solveLegAngles(double bodyX, double bodyY, double bodyZ, float yawDeg,
                                      int leg, double footWX, double footWY, double footWZ,
                                      double[][] joints, double[] out) {
        double hx = SpiderRigProfile.hipX(leg, bodyX, yawDeg);
        double hy = SpiderRigProfile.hipY(leg, bodyY);
        double hz = SpiderRigProfile.hipZ(leg, bodyZ, yawDeg);
        double dx = footWX - hx;
        double dy = footWY - hy;
        double dz = footWZ - hz;
        double dh = Math.sqrt(dx * dx + dz * dz);
        double yawRad = Math.toRadians(Mth.wrapDegrees((double) yawDeg));

        // Leg plane bearing. A foot directly under the hip has no defined
        // azimuth; hold the leg's neutral bearing (cannot occur in practice —
        // rest reach is 10-16 blocks, dangles hang at ~45% of it).
        double alphaW = dh > 1.0E-6 ? Math.atan2(dz, dx)
                : SpiderRigProfile.legBearing(leg, yawDeg) + Math.PI / 2.0;
        out[0] = wrapPi(alphaW - yawRad + Math.PI / 2.0);

        PlanarFabrik.solve(SpiderRigProfile.SEGMENT_LENGTH, dh, dy, PlanarFabrik.DEFAULT_KNEE_BIAS, joints);
        double a1 = Math.atan2(joints[1][1] - joints[0][1], joints[1][0] - joints[0][0]);
        double a2 = Math.atan2(joints[2][1] - joints[1][1], joints[2][0] - joints[1][0]);
        double a3 = Math.atan2(joints[3][1] - joints[2][1], joints[3][0] - joints[2][0]);
        out[1] = a2;
        out[2] = wrapPi(a1 - a2);
        out[3] = 0.0;
        out[4] = wrapPi(a3 - a2);
    }

    static double wrapPi(double angle) {
        double a = angle % (Math.PI * 2.0);
        if (a > Math.PI) {
            a -= Math.PI * 2.0;
        }
        if (a < -Math.PI) {
            a += Math.PI * 2.0;
        }
        return a;
    }
}
