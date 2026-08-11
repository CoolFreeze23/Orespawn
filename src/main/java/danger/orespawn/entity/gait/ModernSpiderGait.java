package danger.orespawn.entity.gait;

import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.network.SpiderGaitKeyframePayload;
import danger.orespawn.network.SpiderStepPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 2.0 spider overhaul (S2): the modern gait controller for {@link SpiderRobot}.
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
 * the rest point will be, not where it was. A vertical-only retrigger fires
 * only when a re-step would actually change the footing — otherwise a ledge
 * or wall column more than {@link #VERTICAL_RETRIGGER} off body level would
 * re-step the same spot forever (independent-review livelock finding).</p>
 *
 * <p><b>One tick, one solve:</b> both sides run
 * {@link #solveLegAngles} — pure math on (body pos, yaw, foot pos) — so the
 * client's replayed pose is the server's pose wherever the synced inputs
 * match. Body position between keyframes is the entity's own vanilla sync,
 * exactly like the classic gait's reliance on synced position. Swing replay
 * clocks the client's game time against the server's step timestamps; the
 * ≤ few-tick clock skew shows as a slightly late swing start (clamped at
 * progress 0) and is corrected by keyframes — accepted for S2.</p>
 *
 * <p><b>What this deliberately does NOT do in S2:</b> no terrain-adaptive
 * 3x3 ground scan, no stranded-leg handling, no body pitch/roll (S3); no
 * hitbox parts (S4); no ant rig or ridden steering (S5); no grass trample
 * (classic's client-side trample stays classic-only until the S3
 * server-side move). The controller never moves or collides the BODY — in
 * S2 the server-visible behavior delta of modern mode is packets only.</p>
 */
public final class ModernSpiderGait {

    // ---- Gait tuning (initial tune, S2; revisit against ride feel in S5) ----
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
    /** A vertical-only re-step must move the footing at least this far. */
    static final double VERTICAL_RETRIGGER_GAIN = 0.5;
    /** Periodic full-state broadcast interval, ticks (drift snap + late joiners). */
    static final int KEYFRAME_INTERVAL = 40;

    private static final int LEGS = SpiderRigProfile.LEG_COUNT;

    // ---- Per-leg state (server-authoritative; client mirrors via payloads) ----
    private final double[] footX = new double[LEGS];
    private final double[] footY = new double[LEGS];
    private final double[] footZ = new double[LEGS];
    private final boolean[] grounded = new boolean[LEGS];
    private final boolean[] swinging = new boolean[LEGS];
    private final double[] fromX = new double[LEGS];
    private final double[] fromY = new double[LEGS];
    private final double[] fromZ = new double[LEGS];
    private final double[] toX = new double[LEGS];
    private final double[] toY = new double[LEGS];
    private final double[] toZ = new double[LEGS];
    private final long[] swingStart = new long[LEGS];
    private final int[] swingDuration = new int[LEGS];
    private final long[] lastLand = new long[LEGS];
    private boolean initialized = false;

    // Scratch buffers for the per-leg solve (single-threaded per side).
    private final double[][] jointScratch = {new double[2], new double[2], new double[2], new double[2]};
    private final double[] angleScratch = new double[5];

    // ---- Test/S4 accessors (server state) ----
    public boolean isGrounded(int leg) {
        return grounded[leg] && !swinging[leg];
    }

    public boolean isSwinging(int leg) {
        return swinging[leg];
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
     * whatever ground the column scan finds. Runs lazily on both sides (the
     * client's copy is immediately overwritten by the first keyframe).
     */
    private void initFeet(SpiderRobot spider) {
        float yaw = spider.getYRot();
        for (int leg = 0; leg < LEGS; ++leg) {
            double rx = SpiderRigProfile.restFootX(leg, spider.getX(), yaw);
            double rz = SpiderRigProfile.restFootZ(leg, spider.getZ(), yaw);
            footX[leg] = rx;
            footZ[leg] = rz;
            footY[leg] = scanGroundY(spider.level(), rx, spider.getY(), rz);
            grounded[leg] = true;
            swinging[leg] = false;
            lastLand[leg] = spider.level().getGameTime();
        }
        initialized = true;
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

        // Body speed drives the trigger radius (blocks moved this tick).
        double vx = spider.getX() - spider.xo;
        double vz = spider.getZ() - spider.zo;
        double speed = Math.sqrt(vx * vx + vz * vz);
        double radius = triggerRadius(speed);

        float yaw = spider.getYRot();
        for (int leg = 0; leg < LEGS; ++leg) {
            if (swinging[leg] || !stepAllowed(leg, time)) {
                continue;
            }
            double restX = SpiderRigProfile.restFootX(leg, spider.getX(), yaw);
            double restZ = SpiderRigProfile.restFootZ(leg, spider.getZ(), yaw);
            double dx = footX[leg] - restX;
            double dz = footZ[leg] - restZ;
            boolean drift = dx * dx + dz * dz > radius * radius;
            boolean verticalMismatch = Math.abs(footY[leg] - spider.getY()) > VERTICAL_RETRIGGER;
            if (!drift && !verticalMismatch) {
                continue;
            }
            // Velocity-projected lookahead with one fixed-point refinement:
            // the swing duration depends on the target which depends on the
            // duration. Estimate from the drift distance, then re-estimate
            // against the displaced target so the foot lands where the rest
            // point WILL be rather than systematically short of it.
            double drift2d = Math.sqrt(dx * dx + dz * dz);
            int est = Mth.clamp((int) Math.round(drift2d / STEP_SPEED), MIN_STEP_TICKS, MAX_STEP_TICKS);
            double tx = restX + vx * est;
            double tz = restZ + vz * est;
            double ty = scanGroundY(level, tx, spider.getY(), tz);
            double stepDist = Math.sqrt((tx - footX[leg]) * (tx - footX[leg])
                    + (ty - footY[leg]) * (ty - footY[leg])
                    + (tz - footZ[leg]) * (tz - footZ[leg]));
            int est2 = Mth.clamp((int) Math.round(stepDist / STEP_SPEED), MIN_STEP_TICKS, MAX_STEP_TICKS);
            if (est2 != est) {
                tx = restX + vx * est2;
                tz = restZ + vz * est2;
                ty = scanGroundY(level, tx, spider.getY(), tz);
            }
            // A vertical-only retrigger must actually improve the footing;
            // otherwise a ledge/wall column that keeps scanning to the same
            // off-level Y would re-step (and broadcast) forever.
            if (!drift && Math.abs(ty - footY[leg]) < VERTICAL_RETRIGGER_GAIN) {
                continue;
            }
            beginStep(spider, leg, tx, ty, tz, time);
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
     * the cooldown window.
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
                    spider.getId(), leg,
                    fromX[leg], fromY[leg], fromZ[leg],
                    tx, ty, tz,
                    time, swingDuration[leg]));
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
     * Single-column ground fit for flat terrain: from just above hip height
     * downward, the first solid block's top is the footing; no ground within
     * the window falls back to the body's own Y (flat-ground assumption).
     * S3 replaces this with the 3x3 biased scan + stranded-leg handling.
     */
    static double scanGroundY(Level level, double x, double bodyY, double z) {
        int top = Mth.floor(bodyY) + 2;
        int bottom = Mth.floor(bodyY) - 6;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= bottom; --y) {
            pos.set(Mth.floor(x), y, Mth.floor(z));
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos).isSolid()) {
                return y + 1.0;
            }
        }
        return bodyY;
    }

    /**
     * Serializes the full per-leg state for keyframe sync. Self-initializes
     * first: a player can start tracking a fresh spawn before its first
     * server tick, and the all-zero placeholder must never reach the wire
     * (independent-review finding — the client would trust it for up to a
     * full keyframe interval).
     */
    public SpiderGaitKeyframePayload buildKeyframe(SpiderRobot spider) {
        if (!initialized) {
            initFeet(spider);
        }
        double[] xs = new double[LEGS];
        double[] ys = new double[LEGS];
        double[] zs = new double[LEGS];
        boolean[] g = new boolean[LEGS];
        for (int leg = 0; leg < LEGS; ++leg) {
            // A swinging leg keyframes its TARGET: a late-joining client sees
            // the landed pose one swing early rather than a stale origin.
            xs[leg] = swinging[leg] ? toX[leg] : footX[leg];
            ys[leg] = swinging[leg] ? toY[leg] : footY[leg];
            zs[leg] = swinging[leg] ? toZ[leg] : footZ[leg];
            g[leg] = !swinging[leg] && grounded[leg];
        }
        return new SpiderGaitKeyframePayload(spider.getId(), xs, ys, zs, g);
    }

    // ==================== CLIENT ====================

    /** Applies a received step event (client, main thread). */
    public void applyStep(SpiderStepPayload payload) {
        int leg = payload.leg();
        fromX[leg] = payload.fromX();
        fromY[leg] = payload.fromY();
        fromZ[leg] = payload.fromZ();
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
     * mode, client side) — replays swings and writes the classic render
     * fields the shipped model consumes.
     */
    public void clientTick(SpiderRobot spider) {
        if (!initialized) {
            initFeet(spider); // placeholder pose until the first keyframe
        }
        long time = spider.level().getGameTime();
        RenderSpiderRobotInfo r = spider.getRenderSpiderRobotInfo();
        ++r.gpcounter; // classic frame counter — keeps the jaw-snap animation alive
        for (int leg = 0; leg < LEGS; ++leg) {
            double fx;
            double fy;
            double fz;
            if (swinging[leg]) {
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
            solveLegAngles(spider.getX(), spider.getY(), spider.getZ(), spider.getYRot(),
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
            r.realposx[leg] = (float) SpiderRigProfile.hipX(leg, spider.getX(), spider.getYRot());
            r.realposy[leg] = (float) SpiderRigProfile.hipY(leg, spider.getY());
            r.realposz[leg] = (float) SpiderRigProfile.hipZ(leg, spider.getZ(), spider.getYRot());
            r.footup[leg] = swinging[leg] ? 1 : 0;
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
     * review): the mapping is exact for vectors and angles, but the vanilla
     * renderer additionally translates the whole model by a constant
     * (+1.501 blocks vertical at render), and rotates by the interpolated
     * BODY yaw while this conversion — like classic's updateLegs, which uses
     * {@code getYRot()} in the identical places — poses against entity yaw.
     * Both offsets exist identically in classic (1.7.10 included), cancel
     * from every angle and delta this solver computes, and are therefore
     * preserved rather than corrected; fixing either would break classic
     * visual parity. Revisit only as a deliberate S3+ decision.</p>
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
        // rest reach is 10-16 blocks).
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
