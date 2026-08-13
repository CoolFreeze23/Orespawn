package danger.orespawn.entity.gait;

import danger.orespawn.entity.IModernLeggedRobot;
import net.minecraft.world.entity.Mob;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.network.SpiderGaitKeyframePayload;
import danger.orespawn.network.SpiderStepPayload;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
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
 * 2.0 spider overhaul (S2/S3, rig-abstracted in S5b): the modern gait
 * controller for the legged robots ({@code SpiderRobot},
 * {@code AntRobot} — one instance per robot per side, parameterized by
 * its {@link LegRig}).
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

    /**
     * S5b: the rig this controller drives — tables, classic probe geometry
     * and rig-scaled tuning all come from here ({@code SpiderRigProfile.RIG}
     * or {@code AntRigProfile.RIG}). One controller instance per robot per
     * side, created with that robot's rig; per-leg arrays size to
     * {@link LegRig#legCount()}.
     */
    private final LegRig rig;
    private final int legCount;

    public ModernSpiderGait(LegRig rig) {
        this.rig = rig;
        this.legCount = rig.legCount();
        this.footX = new double[legCount];
        this.footY = new double[legCount];
        this.footZ = new double[legCount];
        this.grounded = new boolean[legCount];
        this.swinging = new boolean[legCount];
        this.stranded = new boolean[legCount];
        this.fromX = new double[legCount];
        this.fromY = new double[legCount];
        this.fromZ = new double[legCount];
        this.toX = new double[legCount];
        this.toY = new double[legCount];
        this.toZ = new double[legCount];
        this.swingStart = new long[legCount];
        this.swingDuration = new int[legCount];
        this.lastLand = new long[legCount];
        this.verticalRetryAt = new long[legCount];
        this.forcedLift = new boolean[legCount];
        this.sagFloorEff = rig.maxSag();
        this.liftCeilEff = rig.maxLift();
    }

    /** The rig's leg count (payload handlers validate against it exactly). */
    public int legCount() {
        return legCount;
    }

    // ---- Gait tuning (shared across rigs; rig-scaled values live in LegRig) ----
    static final int MIN_STEP_TICKS = 4;
    static final int MAX_STEP_TICKS = 12;
    /** Ticks a fresh-landed foot (or its pair partner) refuses to step again. */
    static final int LAND_COOLDOWN_TICKS = 3;
    /** A vertical-only re-step must improve the mismatch at least this much. */
    static final double VERTICAL_RETRIGGER_GAIN = 0.5;
    /** Periodic full-state broadcast interval, ticks (drift snap + late joiners). */
    static final int KEYFRAME_INTERVAL = 40;

    // ---- Terrain scan tuning (S3; the scan WINDOW is per-rig classic
    // probe geometry and lives on LegRig — see its scanUp/scanDown docs) ----
    /** Preferred-footing height raise when the column ahead is blocked. */
    static final double CLIMB_HEIGHT_BIAS = 1.5;
    /** Stranded dangle: horizontal fraction of rest reach, and drop below hip. */
    static final double DANGLE_REACH_FRAC = 0.45;
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
    /** Ticks a gate-blocked vertical retrigger waits before rescanning. */
    static final int VERTICAL_RETRY_COOLDOWN = 10;


    // ---- Per-leg state (server-authoritative; client mirrors via payloads) ----
    private final double[] footX;
    private final double[] footY;
    private final double[] footZ;
    private final boolean[] grounded;
    private final boolean[] swinging;
    private final boolean[] stranded;
    private final double[] fromX;
    private final double[] fromY;
    private final double[] fromZ;
    private final double[] toX;
    private final double[] toY;
    private final double[] toZ;
    private final long[] swingStart;
    private final int[] swingDuration;
    private final long[] lastLand;
    /** Per-leg wait-until time for re-evaluating a gate-blocked vertical retrigger. */
    private final long[] verticalRetryAt;
    /**
     * S6b: true while a leg's current/latest swing was FORCED — begun by
     * comfort invalidation or the stranded re-step, the two owner-ratified
     * inhibitor-bypass classes (reference: canMoveLeg's unconditional
     * first line). Server-side observability for the amended S2 rhythm
     * invariant: ordinary drift steps still never co-swing with a partner.
     */
    private final boolean[] forcedLift;
    private boolean initialized = false;

    // ---- S3b body dynamics (VISUAL layer — never touches entity physics) ----
    /** Reference gravity pulling the visual body down, blocks/tick². */
    static final double BODY_GRAVITY = -0.08;
    /** PD spring toward the preferred height. */
    static final double LIFT_STIFFNESS = 0.15;
    static final double LIFT_DAMPING = 0.5;
    /**
     * Max upward leg force at full support, blocks/tick² (reference 0.32),
     * scaled by the grounded-leg fraction — stranded legs sag the body.
     */
    static final double LIFT_FORCE_CAP = 0.32;
    /** Low-pass factor for pitch/roll convergence (reference 0.3). */
    static final double TILT_SMOOTH = 0.3;
    /** Tilt clamp, radians (~20°). */
    static final double MAX_TILT = 0.35;
    /**
     * Per-tick rate limits on the visual dynamics (review: the renderer uses
     * raw tick values so planted-foot compensation is exact every frame; the
     * body pose therefore steps once per tick, and these keep each step
     * sub-visual — ≤0.02 rad tilt ≈ 0.06 blocks at body scale).
     */
    static final double TILT_RATE_LIMIT = 0.02;
    static final double LIFT_RATE_LIMIT = 0.15;
    // The S5 rest-heading dead-band/rate constants are per-rig (LegRig
    // restYawDeadbandDeg/restYawRateDeg — full anti-dance rationale there).
    /**
     * The vanilla render chain draws the model this far ABOVE the entity
     * anchor (LivingEntityRenderer's translate(0,−1.501,0) after the
     * (−1,−1,1) flip). The tilt must be conjugated about THAT pivot — a
     * rotation about the bare anchor displaces every planted foot by
     * (R−I)·(0,1.501,0), up to ~0.52 blocks at the tilt clamp
     * (independent-review BLOCKER).
     */
    public static final float VANILLA_RENDER_Y_OFFSET = 1.501f;
    // The tilt-target centroid spans are rig-derived (LegRig pitchSpan/
    // rollSpan, computed in its ctor from the rest stance at yaw 0).

    /**
     * Visual body offset state, one set per side (server's copy will feed
     * the S4 parts; the client's copy drives the renderer). The renderer
     * consumes the RAW tick values — no per-frame interpolation — so the
     * foot compensation baked into this tick's leg angles cancels the
     * render transform exactly on every frame (review: lerping prev→cur
     * against tick-solved angles produced a per-tick sawtooth foot slide of
     * up to ~1.7 blocks on far legs); the rate limits above keep the
     * resulting once-per-tick body stepping sub-visual instead.
     */
    private double bodyLift;
    private double bodyLiftVel;
    private double bodyPitch;
    private double bodyRoll;

    // Scratch buffers for the per-leg solve (single-threaded per side).
    private final double[][] jointScratch = {new double[2], new double[2], new double[2], new double[2]};
    private final double[] angleScratch = new double[5];
    private final double[] footScratch = new double[3];
    private final double[] compScratch = new double[3];

    // ---- S4: MHLib leg-part feed ----
    /** Resolved once per side after parts exist ({@code null} slots = absent). */
    private MHLibPartEntity<?>[] legParts;

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

    /** S6b: whether the leg's current/latest swing was a FORCED lift (comfort/stranded bypass; server observability). */
    public boolean isForcedLift(int leg) {
        return forcedLift[leg];
    }

    /**
     * S6b (test observability): whether the leg's CURRENT foot lies in the
     * valid plant region of the current rest frame — the exact
     * comfort-or-corridor predicate the gait invalidates against.
     */
    public boolean plantWithinComfort(Mob robot, int leg) {
        float yaw = robot.getYRot();
        return candidateComfortable(
                footX[leg], footZ[leg],
                rig.restFootX(leg, robot.getX(), yaw), rig.restFootZ(leg, robot.getZ(), yaw),
                rig.hipX(leg, robot.getX(), yaw), rig.hipZ(leg, robot.getZ(), yaw));
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
    public double triggerRadius(double bodySpeed) {
        return rig.triggerRadius(bodySpeed);
    }

    // ---- S3b body-dynamics accessors ----
    public double bodyLift() {
        return bodyLift;
    }

    public double bodyPitch() {
        return bodyPitch;
    }

    public double bodyRoll() {
        return bodyRoll;
    }

    /**
     * Body lift for rendering — deliberately NOT partial-tick interpolated:
     * the leg angles were compensated against exactly these tick values, so
     * only these values cancel the render transform and lock planted feet
     * (see the state javadoc; the rate limits keep the stepping smooth).
     */
    public float renderLift() {
        return (float) bodyLift;
    }

    /** Body pitch for rendering, radians (+ tips the face down); un-interpolated by design. */
    public float renderPitch() {
        return (float) bodyPitch;
    }

    /** Body roll for rendering, radians (+ raises the odd-leg, +X-at-yaw-0 side); un-interpolated by design. */
    public float renderRoll() {
        return (float) bodyRoll;
    }

    // ==================== S3b BODY TRANSFORM (canonical pair) ====================

    /**
     * The visual body transform, defined ONCE — the renderer transcribes
     * this exact formula with PoseStack ops and the client solve inverts it,
     * so planted feet stay planted while the body tilts around them.
     *
     * <p>{@code T(v) = Ry(a)·Rx(pitch)·Rz(roll)·Ry(−a)·v + (0, lift, 0)}
     * with {@code v} relative to the entity anchor and
     * {@code a = −toRadians(wrapDegrees(yaw))}: the conjugation aligns the
     * pitch axis with the body's lateral axis and the roll axis with its
     * facing axis at any yaw (verified: yaw 0 → pitch about world X̂; yaw 90°
     * → pitch about world Ẑ). All rotations are standard right-handed about
     * the positive axes, matching JOML's {@code Axis.*.rotation}. Sign
     * conventions: {@code +pitch} tips the FACE toward the ground,
     * {@code +roll} raises the odd-leg (right at yaw 0… body-relative) side
     * — the dynamics targets are built against these signs.</p>
     */
    public static void bodyTransform(double yawDeg, double pitch, double roll, double lift, double[] v) {
        double a = -Math.toRadians(Mth.wrapDegrees(yawDeg));
        rotateY(v, -a);
        rotateZ(v, roll);
        rotateX(v, pitch);
        rotateY(v, a);
        v[1] += lift;
    }

    /** Exact inverse of {@link #bodyTransform}. */
    public static void inverseBodyTransform(double yawDeg, double pitch, double roll, double lift, double[] v) {
        double a = -Math.toRadians(Mth.wrapDegrees(yawDeg));
        v[1] -= lift;
        rotateY(v, -a);
        rotateX(v, -pitch);
        rotateZ(v, -roll);
        rotateY(v, a);
    }

    private static void rotateX(double[] v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double y = v[1] * cos - v[2] * sin;
        double z = v[1] * sin + v[2] * cos;
        v[1] = y;
        v[2] = z;
    }

    private static void rotateY(double[] v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v[0] * cos + v[2] * sin;
        double z = -v[0] * sin + v[2] * cos;
        v[0] = x;
        v[2] = z;
    }

    private static void rotateZ(double[] v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v[0] * cos - v[1] * sin;
        double y = v[0] * sin + v[1] * cos;
        v[0] = x;
        v[1] = y;
    }

    // ---- S6b (THE LEG FIX) rotation state, both sides ----
    /** Per-tick yaw delta above this arms the rotation latch (deg/tick). */
    static final float ROTATING_YAW_THRESHOLD_DEG = 0.5f;
    /**
     * Ticks the widened trigger radius persists after rotation stops —
     * the anti-dance duty the S5a dead-band chase used to carry
     * (reference: lerpedGait forces the full moving radius while
     * isRotatingYaw; the latch bridges vanilla's tick-to-tick yaw jitter).
     */
    static final int ROTATION_LATCH_TICKS = 10;
    /**
     * Half-width of the classic contraction corridor for candidate
     * validity, blocks — covers the contracted probe aims on the
     * hip-to-rest ray plus their +-1 scan columns (max perpendicular ~1.41).
     */
    static final double CORRIDOR_HALF_WIDTH = 1.5;
    /** Last tick's body yaw (deg) — yaw-rate input for latch/advection. */
    private float lastYawDeg;
    /**
     * S6b review: lastYawDeg needs an explicit seed — a CLIENT gait can be
     * initialized by a payload (applyStep/applyKeyframe) before its first
     * clientTick, and an unseeded delta from the 0.0 default would rotate
     * in-flight swing origins by up to 180 degrees in one tick.
     */
    private boolean yawSeeded;
    /** Max per-tick yaw rotation applied as scan-target lead, radians (~10 deg — snaps self-correct via mid-swing revalidation instead). */
    static final double MAX_LEAD_RAD = 0.1745;
    /** Remaining ticks of the post-rotation trigger-radius latch (server). */
    private int rotatingYawLatch;
    /**
     * S5: effective lift/sag bounds — converge toward the ridden (±0.15) or
     * free (MAX_LIFT/MAX_SAG) targets at LIFT_RATE_LIMIT per tick, so the
     * mount/dismount transitions never snap the body.
     */
    private double sagFloorEff;
    private double liftCeilEff;

    /**
     * S3b body dynamics tick (both sides, deterministic from foot state —
     * VISUAL ONLY: gait triggers, scans and the entity's physics all keep
     * using the real body position). Height: a PD spring toward the average
     * planted-foot height with gravity always pulling and the legs pushing
     * up only, capped by {@link #LIFT_FORCE_CAP} × grounded fraction — a
     * spider with stranded legs (mid-fall, over a void) sags toward
     * {@link #MAX_SAG} and recovers as feet re-plant, per the reference's
     * emergent-sag behavior. Pitch/roll: low-passed toward the tilt of the
     * planted front/rear and left/right foot-group centroids; a side with
     * no planted feet holds its previous target.
     */
    private void updateBodyDynamics(Mob robot) {
        int planted = 0;
        double sumY = 0.0;
        double frontSum = 0.0;
        int frontCount = 0;
        double rearSum = 0.0;
        int rearCount = 0;
        double evenSum = 0.0;
        int evenCount = 0;
        double oddSum = 0.0;
        int oddCount = 0;
        for (int leg = 0; leg < legCount; ++leg) {
            if (stranded[leg]) {
                continue;
            }
            // Tilt centroids stay CONTINUOUS across step transitions
            // (review: dropping a swinging far leg from a 4-member average
            // hopped the roll target ~5° every stride — a cadence-locked
            // sway): a swinging leg contributes its swing DESTINATION.
            double y = swinging[leg] ? toY[leg] : footY[leg];
            if (!swinging[leg] && grounded[leg]) {
                ++planted;
                sumY += footY[leg];
            }
            // S5b: pitch-group membership is rig data (the spider's is the
            // exact leg<4 split this code shipped with; the ant's mid pair
            // sits out of the pitch centroids).
            int pitchGroup = rig.pitchGroup(leg);
            if (pitchGroup > 0) {
                frontSum += y;
                ++frontCount;
            } else if (pitchGroup < 0) {
                rearSum += y;
                ++rearCount;
            }
            if ((leg & 1) == 0) {
                evenSum += y;
                ++evenCount;
            } else {
                oddSum += y;
                ++oddCount;
            }
        }

        double supportFraction = planted / (double) legCount;
        // S5 seat resolution (the S3b handoff, resolved per the ride design):
        // while ridden the visual body goes NEAR-RIGID — lift AND sag both
        // clamped to ±0.15 — because the passenger renders from real entity
        // state and any larger body offset visibly detaches the seat.
        // The bounds TIGHTEN at LIFT_RATE_LIMIT rather than snapping
        // (independent review: a rider mounting a climbing/sagged spider at
        // |lift| up to 1.0 would otherwise teleport the body up to 0.85
        // blocks in one tick, 5.7x the rate limit the dynamics promise).
        boolean ridden = robot.getFirstPassenger() != null;
        double sagTarget = ridden ? -0.15 : rig.maxSag();
        double liftTarget = ridden ? 0.15 : rig.maxLift();
        sagFloorEff += Mth.clamp(sagTarget - sagFloorEff, -LIFT_RATE_LIMIT, LIFT_RATE_LIMIT);
        liftCeilEff += Mth.clamp(liftTarget - liftCeilEff, -LIFT_RATE_LIMIT, LIFT_RATE_LIMIT);
        double sagFloor = sagFloorEff;
        double liftCeil = liftCeilEff;
        double targetLift = planted > 0
                ? Mth.clamp(sumY / planted - robot.getY(), sagFloor, liftCeil)
                : sagFloor;
        // PD acceleration the spring wants; the legs may only PUSH UP
        // (normal force), capped by available support — gravity always acts.
        double wanted = (targetLift - bodyLift) * LIFT_STIFFNESS - bodyLiftVel * LIFT_DAMPING;
        double legPush = Mth.clamp(wanted - BODY_GRAVITY, 0.0, LIFT_FORCE_CAP * supportFraction);
        bodyLiftVel += BODY_GRAVITY + legPush;
        bodyLiftVel = Mth.clamp(bodyLiftVel, -LIFT_RATE_LIMIT, LIFT_RATE_LIMIT);
        bodyLift += bodyLiftVel;
        if (bodyLift > liftCeil) {
            bodyLift = liftCeil;
            bodyLiftVel = Math.min(bodyLiftVel, 0.0);
        }
        if (bodyLift < sagFloor) {
            bodyLift = sagFloor;
            bodyLiftVel = Math.max(bodyLiftVel, 0.0);
        }

        // Tilt targets from corner-group centroids. Signs per bodyTransform:
        // +pitch = face down, so climbing terrain (front feet higher) yields
        // a NEGATIVE target (nose up); +roll raises the odd (+X at yaw 0)
        // side. A group with no usable legs (all stranded) DECAYS toward
        // level rather than freezing mid-tilt (review: a knocked-off spider
        // fell with a stale 0.35 rad tilt locked in). Rate-limited per tick
        // so the un-interpolated render stepping stays sub-visual.
        double pitchStep;
        if (frontCount > 0 && rearCount > 0) {
            double pitchTarget = Mth.clamp(
                    Math.atan2(rearSum / rearCount - frontSum / frontCount, rig.pitchSpan()),
                    -MAX_TILT, MAX_TILT);
            pitchStep = (pitchTarget - bodyPitch) * TILT_SMOOTH;
        } else {
            pitchStep = -bodyPitch * TILT_SMOOTH;
        }
        bodyPitch += Mth.clamp(pitchStep, -TILT_RATE_LIMIT, TILT_RATE_LIMIT);

        double rollStep;
        if (evenCount > 0 && oddCount > 0) {
            double rollTarget = Mth.clamp(
                    Math.atan2(oddSum / oddCount - evenSum / evenCount, rig.rollSpan()),
                    -MAX_TILT, MAX_TILT);
            rollStep = (rollTarget - bodyRoll) * TILT_SMOOTH;
        } else {
            rollStep = -bodyRoll * TILT_SMOOTH;
        }
        bodyRoll += Mth.clamp(rollStep, -TILT_RATE_LIMIT, TILT_RATE_LIMIT);
    }

    /**
     * First-tick initialization: plant every foot at its rest target on
     * whatever footing the scan finds; a rest column over a void (spawned on
     * a cliff edge) starts stranded rather than fake-grounded in mid-air.
     * Runs lazily on both sides (the client's copy is overwritten by the
     * first keyframe).
     */
    private void initFeet(Mob robot) {
        float yaw = robot.getYRot();
        long time = robot.level().getGameTime();
        for (int leg = 0; leg < legCount; ++leg) {
            double rx = rig.restFootX(leg, robot.getX(), yaw);
            double rz = rig.restFootZ(leg, robot.getZ(), yaw);
            double[] found = scanFootingContracted(robot, leg, rx, rz, 0.0, 0.0, 0);
            if (found != null) {
                footX[leg] = found[0];
                footY[leg] = found[1];
                footZ[leg] = found[2];
                grounded[leg] = true;
                stranded[leg] = false;
            } else {
                stranded[leg] = true;
                grounded[leg] = false;
                dangle(robot, leg, yaw);
            }
            swinging[leg] = false;
            lastLand[leg] = time;
        }
        // S6b: seed the yaw-rate tracker at the spawn heading so the first
        // tick reads a zero delta, not a chase from 0 degrees.
        lastYawDeg = robot.getYRot();
        yawSeeded = true;
        initialized = true;
    }

    /** Places a stranded foot at its semi-folded dangle point (follows the body). */
    private void dangle(Mob robot, int leg, float yaw) {
        double bearing = rig.legBearing(leg, yaw);
        double reach = rig.restReach(leg) * DANGLE_REACH_FRAC;
        footX[leg] = rig.hipX(leg, robot.getX(), yaw) - reach * Math.sin(bearing);
        footZ[leg] = rig.hipZ(leg, robot.getZ(), yaw) + reach * Math.cos(bearing);
        footY[leg] = rig.hipY(leg, robot.getY()) - rig.dangleDrop();
    }

    // ==================== SERVER ====================

    /** One server gait tick; called from the robot's {@code tick()} (modern mode, server side). */
    public void serverTick(Mob robot) {
        if (!initialized) {
            initFeet(robot);
        }
        Level level = robot.level();
        long time = level.getGameTime();

        // Land finished swings.
        for (int leg = 0; leg < legCount; ++leg) {
            if (swinging[leg] && time - swingStart[leg] >= swingDuration[leg]) {
                land(leg, time);
            }
        }
        // Trample sweep (server-side, S3): classic tramples on EVERY tick a
        // ridden leg is settled, not just on touchdown (SpiderRobot.updateLegs
        // :697-711 runs whenever all three axes settle) — mirrored here per
        // independent review so a rider mounting a standing spider, or grass
        // regrowing under a planted foot, tramples exactly as in classic.
        if (rig.tramples() && robot.getFirstPassenger() != null
                && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for (int leg = 0; leg < legCount; ++leg) {
                if (grounded[leg] && !swinging[leg] && !stranded[leg]) {
                    trampleAt(robot, leg);
                }
            }
        }

        // Body speed drives the trigger radius (blocks moved this tick).
        double vx = robot.getX() - robot.xo;
        double vz = robot.getZ() - robot.zo;
        double speed = Math.sqrt(vx * vx + vz * vz);
        double radius = triggerRadius(speed);

        // S6b (THE LEG FIX; reference Leg.updateMemo): the rest frame is a
        // pure function of CURRENT body yaw — zero lag, recomputed every
        // tick. The S5a dead-band CHASE this replaces lagged the frame by
        // design, which left planted feet sitting contralateral in the new
        // body frame during every fast turn (the sitting-confirmed
        // midline-crossing mechanism). The anti-dance duty moves to where
        // the reference keeps it: while the body yaw-rotates — with a
        // short latch over vanilla's tick-to-tick jitter — the trigger
        // radius is FORCED to the full moving value (SpiderBody.lerpedGait
        // :66-73), so look-jitter sweeps rests inside a wide trigger band
        // and moves nothing, while genuine turns re-plant promptly against
        // yaw-fresh rests.
        float yawTrue = robot.getYRot();
        float yaw = yawTrue;
        float yawDeltaDeg = Mth.degreesDifference(lastYawDeg, yawTrue);
        lastYawDeg = yawTrue;
        double yawStepRad = Math.toRadians(yawDeltaDeg);
        if (Math.abs(yawDeltaDeg) > ROTATING_YAW_THRESHOLD_DEG) {
            rotatingYawLatch = ROTATION_LATCH_TICKS;
        } else if (rotatingYawLatch > 0) {
            --rotatingYawLatch;
        }
        if (rotatingYawLatch > 0) {
            radius = rig.triggerRadiusMax();
        }
        for (int leg = 0; leg < legCount; ++leg) {
            if (swinging[leg]) {
                // S6b (reference applyBodyMotion, Leg.kt:189-192): the
                // swing ORIGIN inherits body translation and yaw rotation
                // each tick, so in-flight feet track a moving, turning
                // body.
                advectSwingOrigin(robot, leg, vx, vz, yawStepRad);
                // S6b review MAJOR: the in-flight TARGET is revalidated
                // against the CURRENT frame every tick (reference:
                // locateGroundTarget runs per tick and softResetStep
                // retargets mid-flight, Leg.kt:137-155/238-241). Frozen
                // landings went stale above ~4.5 deg/tick of sustained
                // rotation and re-invalidated the moment they landed — a
                // perpetual forced-lift churn the reference cannot enter.
                double restXNow = rig.restFootX(leg, robot.getX(), yaw);
                double restZNow = rig.restFootZ(leg, robot.getZ(), yaw);
                if (!candidateComfortable(toX[leg], toZ[leg], restXNow, restZNow,
                        rig.hipX(leg, robot.getX(), yaw), rig.hipZ(leg, robot.getZ(), yaw))) {
                    interpolatedFootPos(leg, time, footScratch);
                    footX[leg] = footScratch[0];
                    footY[leg] = footScratch[1];
                    footZ[leg] = footScratch[2];
                    double[] retarget = scanFootingContracted(robot, leg,
                            restXNow + vx * MIN_STEP_TICKS, restZNow + vz * MIN_STEP_TICKS,
                            vx, vz, MIN_STEP_TICKS);
                    if (retarget != null) {
                        beginStep(robot, leg, retarget[0], retarget[1], retarget[2], time, true);
                    } else {
                        strand(robot, leg, yawTrue);
                    }
                }
                continue;
            }
            double restX = rig.restFootX(leg, robot.getX(), yaw);
            double restZ = rig.restFootZ(leg, robot.getZ(), yaw);

            if (stranded[leg]) {
                // Dangle follows the body; re-step UNCONDITIONALLY (no
                // inhibitors, per the reference) onto any footing that appears
                // — including contracted footing near the hip. TRUE yaw: the
                // client computes the same dangle from its own getYRot.
                dangle(robot, leg, yawTrue);
                double[] found = scanFootingContracted(robot, leg,
                        restX + vx * STRANDED_LOOKAHEAD_TICKS,
                        restZ + vz * STRANDED_LOOKAHEAD_TICKS, vx, vz, STRANDED_LOOKAHEAD_TICKS);
                if (found != null) {
                    beginStep(robot, leg, found[0], found[1], found[2], time, true);
                }
                continue;
            }

            double dx = footX[leg] - restX;
            double dz = footZ[leg] - restZ;
            // S6b comfort invalidation (reference: updateMovement's target
            // swap, Leg.kt:147-149, + canMoveLeg's unconditional first
            // line, GaitType.kt:22): a planted foot outside the VALID
            // plant region of the CURRENT rest frame is INVALID — it lifts
            // NOW, bypassing the pair/neighbor inhibitors exactly as
            // stranded legs always have. The region is the SAME
            // comfort-or-corridor set candidates are drawn from (first-run
            // red: comfort-only invalidation made a legal classic
            // corridor edge-grip oscillate plant->invalidate forever);
            // corridor plants are near-hip FOLDED grips — ipsilateral and
            // short — so neither crossing nor stretching re-enters.
            boolean uncomfortable = !candidateComfortable(
                    footX[leg], footZ[leg], restX, restZ,
                    rig.hipX(leg, robot.getX(), yaw), rig.hipZ(leg, robot.getZ(), yaw));
            if (!uncomfortable) {
                // S6b review MAJOR: a plant must KEEP satisfying the same
                // 3D reach guard its candidate passed at scan time — the
                // body walking or hover-bobbing away from a legally planted
                // foot otherwise parks a PERMANENT render-clamp slide (ant
                // front pair: settled demand up to ~10.0 vs the 9.14 cap,
                // ~0.9 blocks of standing clamp slide — not sub-visual).
                double reachDx = footX[leg] - rig.hipX(leg, robot.getX(), yaw);
                double reachDy = footY[leg] - rig.hipY(leg, robot.getY());
                double reachDz = footZ[leg] - rig.hipZ(leg, robot.getZ(), yaw);
                double reachCap = rig.maxReach() * rig.reachMargin();
                if (reachDx * reachDx + reachDy * reachDy + reachDz * reachDz > reachCap * reachCap) {
                    uncomfortable = true;
                }
            }
            if (!uncomfortable && !stepAllowed(leg, time)) {
                continue;
            }
            boolean drift = uncomfortable || dx * dx + dz * dz > radius * radius;
            boolean verticalMismatch = time >= verticalRetryAt[leg]
                    && Math.abs(footY[leg] - robot.getY()) > rig.verticalRetrigger();
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
            int est = Mth.clamp((int) Math.round(drift2d / rig.stepSpeed()), MIN_STEP_TICKS, MAX_STEP_TICKS);
            // S6b (reference lookAheadPosition, Leg.kt:253): the scan
            // target leads the turn by ONE tick's yaw rate, rotated about
            // the body — footfalls land where the rest ring is going.
            double targetX = restX + vx * est;
            double targetZ = restZ + vz * est;
            // Lead CLAMPED to a sustained-rotation bound (review: the raw
            // per-tick delta over-rotates the scan on snap turns — a 180
            // flick aimed the scan at the PRE-flick rests; the reference's
            // lead uses a physics-bounded angular velocity, Leg.kt:253).
            double leadRad = Mth.clamp(yawStepRad, -MAX_LEAD_RAD, MAX_LEAD_RAD);
            if (leadRad != 0.0) {
                double relX = targetX - robot.getX();
                double relZ = targetZ - robot.getZ();
                double cosR = Math.cos(leadRad);
                double sinR = Math.sin(leadRad);
                targetX = robot.getX() + relX * cosR - relZ * sinR;
                targetZ = robot.getZ() + relX * sinR + relZ * cosR;
            }
            double[] cand = scanFootingContracted(robot, leg, targetX, targetZ, vx, vz, est);
            if (cand != null) {
                double stepDist = Math.sqrt((cand[0] - footX[leg]) * (cand[0] - footX[leg])
                        + (cand[1] - footY[leg]) * (cand[1] - footY[leg])
                        + (cand[2] - footZ[leg]) * (cand[2] - footZ[leg]));
                int est2 = Mth.clamp((int) Math.round(stepDist / rig.stepSpeed()), MIN_STEP_TICKS, MAX_STEP_TICKS);
                if (est2 != est) {
                    double[] refined = scanFooting(robot, leg, restX + vx * est2, restZ + vz * est2,
                            vx, vz, est2);
                    if (refined != null) {
                        cand = refined;
                    }
                }
            }
            if (cand == null) {
                // Neither the projected patch nor the contracted sweep down
                // to ~3.5 blocks from the hip found footing: strand (TRUE
                // yaw — the strand dangle must match the client's).
                strand(robot, leg, yawTrue);
                continue;
            }
            if (!drift) {
                // Vertical-only retrigger: must strictly improve the mismatch
                // (livelock guard vs climb assist — see class javadoc). A
                // blocked attempt arms a short cooldown so an idle spider by
                // a ledge doesn't re-scan ~230 blocks per leg every tick.
                double curMismatch = Math.abs(footY[leg] - robot.getY());
                double newMismatch = Math.abs(cand[1] - robot.getY());
                if (curMismatch - newMismatch < VERTICAL_RETRIGGER_GAIN) {
                    verticalRetryAt[leg] = time + VERTICAL_RETRY_COOLDOWN;
                    continue;
                }
            }
            beginStep(robot, leg, cand[0], cand[1], cand[2], time, uncomfortable);
        }

        // S3b: visual body dynamics from the post-land foot state.
        updateBodyDynamics(robot);

        // S4: feed the MHLib leg parts from this tick's solve — server truth
        // for damage. MHLib's own alignSubParts static alignment ran earlier
        // this tick (aiStep TAIL, inside super.tick()) and is deliberately
        // overwritten here.
        feedParts(robot, time);

        // Keyframes phase-shifted by entity id so multiple spiders don't
        // burst-send on the same global tick.
        if ((time + robot.getId()) % KEYFRAME_INTERVAL == 0) {
            PacketDistributor.sendToPlayersTrackingEntity(robot, buildKeyframe(robot));
        }
    }

    /**
     * Step inhibitors (reference technique: timers inhibit, distance
     * triggers): the mirrored pair partner and both same-side neighbors must
     * be planted, and neither this leg nor its partner may have landed within
     * the cooldown window. Stranded legs bypass this entirely.
     */
    private boolean stepAllowed(int leg, long time) {
        int partner = rig.pairedWith(leg);
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
        if (aft < legCount && swinging[aft]) {
            return false;
        }
        return time - lastLand[leg] >= LAND_COOLDOWN_TICKS
                && time - lastLand[partner] >= LAND_COOLDOWN_TICKS;
    }

    private void beginStep(Mob robot, int leg, double tx, double ty, double tz, long time, boolean forced) {
        forcedLift[leg] = forced;
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
        swingDuration[leg] = Mth.clamp((int) Math.round(dist / rig.stepSpeed()), MIN_STEP_TICKS, MAX_STEP_TICKS);
        swingStart[leg] = time;
        swinging[leg] = true;
        grounded[leg] = false;
        if (!robot.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(robot, new SpiderStepPayload(
                    robot.getId(), leg, false,
                    fromX[leg], fromY[leg], fromZ[leg],
                    tx, ty, tz,
                    time, swingDuration[leg]));
        }
    }

    /** Server: marks a leg stranded and tells trackers (the dangle itself is computed per side). */
    private void strand(Mob robot, int leg, float yaw) {
        stranded[leg] = true;
        grounded[leg] = false;
        swinging[leg] = false;
        dangle(robot, leg, yaw);
        if (!robot.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(robot, new SpiderStepPayload(
                    robot.getId(), leg, true,
                    footX[leg], footY[leg], footZ[leg],
                    footX[leg], footY[leg], footZ[leg],
                    robot.level().getGameTime(), MIN_STEP_TICKS));
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
    private void trampleAt(Mob robot, int leg) {
        Level level = robot.level();
        if (!rig.tramples() || level.isClientSide() || robot.getFirstPassenger() == null
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
    private double[] scanFootingContracted(Mob robot, int leg, double tx, double tz,
                                           double vx, double vz, int projTicks) {
        double[] found = scanFooting(robot, leg, tx, tz, vx, vz, projTicks);
        if (found != null) {
            return found;
        }
        float yaw = robot.getYRot();
        double bearing = rig.legBearing(leg, yaw);
        double hipX = rig.hipX(leg, robot.getX(), yaw);
        double hipZ = rig.hipZ(leg, robot.getZ(), yaw);
        for (double frac : CONTRACTION_FRACTIONS) {
            double r = Math.max(rig.minContractedReach(), rig.restReach(leg) * frac);
            double cx = hipX - r * Math.sin(bearing);
            double cz = hipZ + r * Math.cos(bearing);
            found = scanFooting(robot, leg, cx, cz, vx, vz, 0);
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
    private double[] scanFooting(Mob robot, int leg, double tx, double tz,
                                 double vx, double vz, int projTicks) {
        Level level = robot.level();
        double bodyY = robot.getY();
        float yaw = robot.getYRot();
        double hipX = rig.hipX(leg, robot.getX() + vx * projTicks, yaw);
        double hipY = rig.hipY(leg, bodyY);
        double hipZ = rig.hipZ(leg, robot.getZ() + vz * projTicks, yaw);
        // S6b: candidate validity is anchored on the PROJECTED rest (the
        // rest the foot will serve when it lands) and the projected
        // hip-to-rest ray (the classic contraction corridor).
        double projRestX = rig.restFootX(leg, robot.getX() + vx * projTicks, yaw);
        double projRestZ = rig.restFootZ(leg, robot.getZ() + vz * projTicks, yaw);
        double maxReachSq = rig.maxReach() * rig.reachMargin()
                * rig.maxReach() * rig.reachMargin();

        // Climb assist: preferred footing height rises when the body's path
        // is blocked at chest height.
        double preferY = bodyY;
        double speed = Math.sqrt(vx * vx + vz * vz);
        if (speed > 0.05) {
            BlockPos ahead = BlockPos.containing(
                    robot.getX() + vx / speed * 2.5,
                    bodyY + 0.5,
                    robot.getZ() + vz / speed * 2.5);
            if (level.getBlockState(ahead).isSolid()) {
                preferY += CLIMB_HEIGHT_BIAS;
            }
        }

        // Foot-height space [floor-SCAN_DOWN, floor+SCAN_UP] — block rows
        // [bottom, top] with the occupancy seed one above the window.
        int top = Mth.floor(bodyY) + rig.scanUp() - 1;
        int bottom = Mth.floor(bodyY) - rig.scanDown() - 1;
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
                            if (rdx * rdx + rdy * rdy + rdz * rdz <= maxReachSq
                                    && candidateComfortable(cx, cz, projRestX, projRestZ, hipX, hipZ)) {
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

    /** S6b: per-tick body-motion inheritance for a swing origin (reference applyBodyMotion). */
    private void advectSwingOrigin(Mob robot, int leg, double vx, double vz, double yawStepRad) {
        fromX[leg] += vx;
        fromZ[leg] += vz;
        if (yawStepRad != 0.0) {
            double relX = fromX[leg] - robot.getX();
            double relZ = fromZ[leg] - robot.getZ();
            double cosR = Math.cos(yawStepRad);
            double sinR = Math.sin(yawStepRad);
            fromX[leg] = robot.getX() + relX * cosR - relZ * sinR;
            fromZ[leg] = robot.getZ() + relX * sinR + relZ * cosR;
        }
    }

    /**
     * S6b candidate validity (reference: locateGroundTarget's comfort
     * rejection, Leg.kt:314-316 — his generator is structurally unable to
     * emit a contralateral point; ours gets the same guarantee here): a
     * candidate must sit inside the comfort radius of the projected rest,
     * OR inside the classic contraction corridor — within
     * {@link #CORRIDOR_HALF_WIDTH} of the projected hip-to-rest ray, no
     * closer to the hip than the rig's classic sweep floor (so the
     * corridor's near end stays ipsilateral even on the ant's
     * 0.75-block hip radius).
     */
    private boolean candidateComfortable(double cx, double cz, double restX, double restZ,
                                         double hipX, double hipZ) {
        double dx = cx - restX;
        double dz = cz - restZ;
        double comfort = rig.comfortRadius();
        if (dx * dx + dz * dz <= comfort * comfort) {
            return true;
        }
        double sx = restX - hipX;
        double sz = restZ - hipZ;
        double lenSq = sx * sx + sz * sz;
        if (lenSq < 1.0E-9) {
            return false;
        }
        double len = Math.sqrt(lenSq);
        double tMin = Math.min(1.0, rig.minContractedReach() / len);
        double t = Mth.clamp(((cx - hipX) * sx + (cz - hipZ) * sz) / lenSq, tMin, 1.0);
        double px = hipX + sx * t;
        double pz = hipZ + sz * t;
        double ddx = cx - px;
        double ddz = cz - pz;
        return ddx * ddx + ddz * ddz <= CORRIDOR_HALF_WIDTH * CORRIDOR_HALF_WIDTH;
    }

    /**
     * Serializes the full per-leg state for keyframe sync. Self-initializes
     * first: a player can start tracking a fresh spawn before its first
     * server tick, and the all-zero placeholder must never reach the wire.
     */
    public SpiderGaitKeyframePayload buildKeyframe(Mob robot) {
        if (!initialized) {
            initFeet(robot);
        }
        double[] xs = new double[legCount];
        double[] ys = new double[legCount];
        double[] zs = new double[legCount];
        boolean[] g = new boolean[legCount];
        boolean[] s = new boolean[legCount];
        for (int leg = 0; leg < legCount; ++leg) {
            // A swinging leg keyframes its TARGET: a late-joining client sees
            // the landed pose one swing early rather than a stale origin.
            xs[leg] = swinging[leg] ? toX[leg] : footX[leg];
            ys[leg] = swinging[leg] ? toY[leg] : footY[leg];
            zs[leg] = swinging[leg] ? toZ[leg] : footZ[leg];
            g[leg] = !swinging[leg] && grounded[leg];
            s[leg] = stranded[leg];
        }
        return new SpiderGaitKeyframePayload(robot.getId(), xs, ys, zs, g, s);
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
        for (int leg = 0; leg < legCount; ++leg) {
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
     * One client gait tick; called from the robot's {@code tick()} (modern
     * mode, client side) — replays swings, follows stranded dangles, and
     * writes the classic render fields the shipped model consumes.
     */
    public void clientTick(Mob robot) {
        if (!initialized) {
            initFeet(robot); // placeholder pose until the first keyframe
        }
        long time = robot.level().getGameTime();
        float yaw = robot.getYRot();
        // S6b: the client replays the same swing-origin advection from its
        // own view of body motion (deterministic per side; landings are the
        // synced fixed targets, so the sides converge exactly at land).
        double clientVx = robot.getX() - robot.xo;
        double clientVz = robot.getZ() - robot.zo;
        if (!yawSeeded) {
            // Payload-initialized client gait: seed now, no spurious delta.
            lastYawDeg = yaw;
            yawSeeded = true;
        }
        double clientYawStepRad = Math.toRadians(Mth.degreesDifference(lastYawDeg, yaw));
        lastYawDeg = yaw;
        RenderSpiderRobotInfo r = ((IModernLeggedRobot) robot).getRenderSpiderRobotInfo();
        ++r.gpcounter; // classic frame counter — keeps the jaw-snap animation alive
        // S3b: dynamics from last tick's replayed feet (in-loop lands below
        // reach the dynamics next tick — a deliberate 1-tick lag, matching
        // the server's post-land ordering closely enough to converge).
        updateBodyDynamics(robot);
        for (int leg = 0; leg < legCount; ++leg) {
            double fx;
            double fy;
            double fz;
            if (stranded[leg]) {
                dangle(robot, leg, yaw);
                fx = footX[leg];
                fy = footY[leg];
                fz = footZ[leg];
            } else if (swinging[leg]) {
                advectSwingOrigin(robot, leg, clientVx, clientVz, clientYawStepRad);
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
                            + rig.liftHeight() * 4.0 * progress * (1.0 - progress);
                }
            } else {
                fx = footX[leg];
                fy = footY[leg];
                fz = footZ[leg];
            }
            // S3b foot compensation: the renderer applies bodyTransform
            // (pivot-conjugated about the vanilla +1.501 model offset) to
            // the whole model, so the solve targets the INVERSE-transformed
            // foot — the tilted render then lands the foot back on its true
            // world anchor and planted feet stay motionless under tilt.
            compensateFoot(robot, leg, fx, fy, fz, compScratch);
            solveLegAngles(rig, robot.getX(), robot.getY(), robot.getZ(), yaw,
                    leg, compScratch[0], compScratch[1], compScratch[2],
                    jointScratch, angleScratch);
            // S4: mirror the leg part locally (client picking) from the
            // same solve that just filled the joint scratch.
            positionLegPart(robot, leg, compScratch[0], compScratch[2]);
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
            r.realposx[leg] = (float) rig.hipX(leg, robot.getX(), yaw);
            r.realposy[leg] = (float) rig.hipY(leg, robot.getY());
            r.realposz[leg] = (float) rig.hipZ(leg, robot.getZ(), yaw);
            r.footup[leg] = swinging[leg] || stranded[leg] ? 1 : 0;
        }
    }

    // ==================== S4: MHLib LEG-PART FEED ====================

    /**
     * The compensated solve target for a foot's world anchor: inverse body
     * transform, then the reach guard pulling over-reach back along the hip
     * ray (S3b review — the shortfall stays in the graceful straight-stretch
     * family). Output is WORLD coordinates ready for {@code solveLegAngles}.
     */
    private void compensateFoot(Mob robot, int leg,
                                double fx, double fy, double fz, double[] out) {
        float yaw = robot.getYRot();
        out[0] = fx - robot.getX();
        out[1] = fy - robot.getY();
        out[2] = fz - robot.getZ();
        inverseBodyTransform(yaw, bodyPitch, bodyRoll, bodyLift, out);
        double chx = rig.hipX(leg, robot.getX(), yaw) - robot.getX();
        double chy = rig.hipY(leg, robot.getY()) - robot.getY();
        double chz = rig.hipZ(leg, robot.getZ(), yaw) - robot.getZ();
        double rx = out[0] - chx;
        double ry = out[1] - chy;
        double rz = out[2] - chz;
        double reachDist = Math.sqrt(rx * rx + ry * ry + rz * rz);
        double reachCap = rig.maxReach() * rig.reachMargin();
        if (reachDist > reachCap) {
            double scale = reachCap / reachDist;
            out[0] = chx + rx * scale;
            out[1] = chy + ry * scale;
            out[2] = chz + rz * scale;
        }
        out[0] += robot.getX();
        out[1] += robot.getY();
        out[2] += robot.getZ();
    }

    /**
     * Public view of the live foot trajectory (planted anchor, swing
     * interpolation, or dangle) — the SERVER-true position the S4 part feed
     * follows. Exposed for the invariant tests' restated-tolerance contract
     * (design ruling: swinging parts assert against the SERVER trajectory,
     * never the latency-lagged rendered leg).
     */
    public void currentFootPos(int leg, long time, double[] out) {
        interpolatedFootPos(leg, time, out);
    }

    /** Current foot position incl. swing interpolation (shared server/client). */
    private void interpolatedFootPos(int leg, long time, double[] out) {
        if (swinging[leg]) {
            double progress = Mth.clamp(
                    (time - swingStart[leg]) / (double) swingDuration[leg], 0.0, 1.0);
            out[0] = Mth.lerp(progress, fromX[leg], toX[leg]);
            out[2] = Mth.lerp(progress, fromZ[leg], toZ[leg]);
            out[1] = Mth.lerp(progress, fromY[leg], toY[leg])
                    + rig.liftHeight() * 4.0 * progress * (1.0 - progress);
        } else {
            out[0] = footX[leg];
            out[1] = footY[leg];
            out[2] = footZ[leg];
        }
    }

    /** Resolves the profile's named leg parts once per side (design D3: leg0..leg7). */
    private void resolveLegParts(Mob robot) {
        if (legParts != null) {
            return;
        }
        if (robot.getParts() == null || robot.getParts().length == 0) {
            return;
        }
        Object self = robot;
        if (!(self instanceof IMultipartEntity<?> multipart)) {
            return;
        }
        MHLibPartEntity<?>[] resolved = new MHLibPartEntity<?>[legCount];
        for (int leg = 0; leg < legCount; ++leg) {
            resolved[leg] = multipart.getPartByName("leg" + leg).orElse(null);
        }
        legParts = resolved;
    }

    /**
     * S4 server feed (design D3): every tick, each leg's hitbox part is
     * placed on the lower-segment (knee2→foot) midpoint of the SAME
     * compensated solve the client renders from — server truth for damage,
     * no client authority. Runs after {@code updateBodyDynamics} so parts
     * carry this tick's tilt.
     */
    private void feedParts(Mob robot, long time) {
        resolveLegParts(robot);
        if (legParts == null) {
            return;
        }
        float yaw = robot.getYRot();
        for (int leg = 0; leg < legCount; ++leg) {
            if (legParts[leg] == null) {
                continue;
            }
            interpolatedFootPos(leg, time, footScratch);
            compensateFoot(robot, leg, footScratch[0], footScratch[1], footScratch[2], compScratch);
            solveLegAngles(rig, robot.getX(), robot.getY(), robot.getZ(), yaw,
                    leg, compScratch[0], compScratch[1], compScratch[2],
                    jointScratch, angleScratch);
            positionLegPart(robot, leg, compScratch[0], compScratch[2]);
        }
    }

    /**
     * Places leg {@code leg}'s part from the joint scratch the caller just
     * filled: solver-frame knee2/foot joints → untilted body frame along the
     * compensated target's bearing → {@link #bodyTransform} → world;
     * {@code setPos} anchors the 0.6-cube's bottom center.
     */
    private void positionLegPart(Mob robot, int leg, double compWorldX, double compWorldZ) {
        resolveLegParts(robot);
        if (legParts == null || legParts[leg] == null) {
            return;
        }
        float yaw = robot.getYRot();
        double hipRelX = rig.hipX(leg, robot.getX(), yaw) - robot.getX();
        double hipRelY = rig.hipY(leg, robot.getY()) - robot.getY();
        double hipRelZ = rig.hipZ(leg, robot.getZ(), yaw) - robot.getZ();
        double dx = (compWorldX - robot.getX()) - hipRelX;
        double dz = (compWorldZ - robot.getZ()) - hipRelZ;
        double dh = Math.sqrt(dx * dx + dz * dz);
        // Degenerate-bearing fallback MIRRORS solveLegAngles exactly (same
        // 1e-6 threshold, same neutral-bearing axis) — review: a 1e-9/world-
        // +X fallback here could place a part blocks away from the rendered
        // leg in the (unreachable in practice) foot-over-hip case.
        final double ux;
        final double uz;
        if (dh > 1.0E-6) {
            ux = dx / dh;
            uz = dz / dh;
        } else {
            double alphaW = rig.legBearing(leg, yaw) + Math.PI / 2.0;
            ux = Math.cos(alphaW);
            uz = Math.sin(alphaW);
        }
        double midU = (jointScratch[2][0] + jointScratch[3][0]) * 0.5;
        double midV = (jointScratch[2][1] + jointScratch[3][1]) * 0.5;
        double[] world = {hipRelX + ux * midU, hipRelY + midV, hipRelZ + uz * midU};
        bodyTransform(yaw, bodyPitch, bodyRoll, bodyLift, world);
        legParts[leg].setPos(
                robot.getX() + world[0],
                robot.getY() + world[1] - rig.partHalfHeight(),
                robot.getZ() + world[2]);
    }

    /**
     * S6a (sitting OBS-2 pin): the part-anchor position (box BOTTOM center)
     * the feed would set THIS tick for {@code leg}, recomputed from live
     * gait state through the same interpolate→compensate→solve→transform
     * sequence {@link #feedParts} runs. The suite pins the live part
     * entities to this within epsilon so box-vs-solver drift can never go
     * silent (the boxes' one-cube-per-leg offset from the VISUAL leg is
     * the accepted Q3 design cost; drift from the SOLVER is a defect).
     */
    public void currentLegPartAnchor(Mob robot, int leg, long time, double[] out) {
        interpolatedFootPos(leg, time, footScratch);
        compensateFoot(robot, leg, footScratch[0], footScratch[1], footScratch[2], compScratch);
        float yaw = robot.getYRot();
        solveLegAngles(rig, robot.getX(), robot.getY(), robot.getZ(), yaw,
                leg, compScratch[0], compScratch[1], compScratch[2],
                jointScratch, angleScratch);
        double hipRelX = rig.hipX(leg, robot.getX(), yaw) - robot.getX();
        double hipRelY = rig.hipY(leg, robot.getY()) - robot.getY();
        double hipRelZ = rig.hipZ(leg, robot.getZ(), yaw) - robot.getZ();
        double dx = (compScratch[0] - robot.getX()) - hipRelX;
        double dz = (compScratch[2] - robot.getZ()) - hipRelZ;
        double dh = Math.sqrt(dx * dx + dz * dz);
        final double ux;
        final double uz;
        if (dh > 1.0E-6) {
            ux = dx / dh;
            uz = dz / dh;
        } else {
            double alphaW = rig.legBearing(leg, yaw) + Math.PI / 2.0;
            ux = Math.cos(alphaW);
            uz = Math.sin(alphaW);
        }
        double midU = (jointScratch[2][0] + jointScratch[3][0]) * 0.5;
        double midV = (jointScratch[2][1] + jointScratch[3][1]) * 0.5;
        double[] world = {hipRelX + ux * midU, hipRelY + midV, hipRelZ + uz * midU};
        bodyTransform(yaw, bodyPitch, bodyRoll, bodyLift, world);
        out[0] = robot.getX() + world[0];
        out[1] = robot.getY() + world[1] - rig.partHalfHeight();
        out[2] = robot.getZ() + world[2];
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
     * <p>S5b: the mapping is rig-parameterized — the ant's
     * {@code ModelAntRobot} hip placement and chain advance are structurally
     * identical (49px segments in place of 99px), so the SAME conversion
     * serves both rigs; the render-parity harness runs both grids.</p>
     *
     * @param rig    the leg rig supplying hip geometry and segment length
     * @param joints scratch {@code double[4][2]} for the FABRIK solve
     * @param out    {@code [yd, ud, p1, p2, p3]} (radians; yd/ud floats upstream)
     */
    public static void solveLegAngles(LegRig rig, double bodyX, double bodyY, double bodyZ, float yawDeg,
                                      int leg, double footWX, double footWY, double footWZ,
                                      double[][] joints, double[] out) {
        double hx = rig.hipX(leg, bodyX, yawDeg);
        double hy = rig.hipY(leg, bodyY);
        double hz = rig.hipZ(leg, bodyZ, yawDeg);
        double dx = footWX - hx;
        double dy = footWY - hy;
        double dz = footWZ - hz;
        double dh = Math.sqrt(dx * dx + dz * dz);
        double yawRad = Math.toRadians(Mth.wrapDegrees((double) yawDeg));

        // Leg plane bearing. A foot directly under the hip has no defined
        // azimuth; hold the leg's neutral bearing (cannot occur in practice —
        // rest reach is 10-16 blocks, dangles hang at ~45% of it).
        double alphaW = dh > 1.0E-6 ? Math.atan2(dz, dx)
                : rig.legBearing(leg, yawDeg) + Math.PI / 2.0;
        out[0] = wrapPi(alphaW - yawRad + Math.PI / 2.0);

        PlanarFabrik.solve(rig.segmentLength(), dh, dy, PlanarFabrik.DEFAULT_KNEE_BIAS, joints);
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
