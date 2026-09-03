package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.entity.gait.ModernSpiderGait;
import danger.orespawn.entity.gait.PlanarFabrik;
import danger.orespawn.entity.gait.SpiderRigProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 2.0 spider overhaul, S2 (batch categories: gait).
 *
 * <p>The render-parity harness runs first by design mandate (design doc §7 /
 * owner directive): it proves the world-joint → model-angle conversion exact
 * against a double-precision reimplementation of the shipped model's forward
 * kinematics before any gait behavior is trusted. Independent-review
 * hardening: the target grid includes the near-full-extension band where
 * FABRIK's convergence is slowest (the original 20-iteration budget failed
 * exactly there), and the emitted angles are round-tripped through
 * {@code float} to match the precision domain the real render path uses.
 * The gait invariant tests then cover the S1 design's invariants 1-3 (no
 * planted-foot slide, feet grounded at rest, cadence bounds + no adjacent
 * simultaneous swings), driven through the entity's own physics so the
 * server gait observes real velocity (a {@code setPos}-driven body reads as
 * stationary — the framework resets {@code xo} before each entity tick).</p>
 *
 * <p>Tests that flip {@code spiderMovement} run in the isolated batch
 * {@code spiderGaitIsolation} (the BOSS-017 idiom): the flag is global and
 * snapshotted at entity construction, so it is restored immediately after
 * the constructions under test — but isolation keeps even that window away
 * from concurrently scheduled default-batch tests. Since the master-override
 * ruling (2026-09-04) the key is inert while {@code [modern] enabled} is
 * off, so every MODERN spawn raises the master with the key and restores
 * both together.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpiderGaitTests {

    /** Hip placement uses no solved angles — pure double algebra. */
    private static final double EPS_HIP = 1.0E-6;
    /**
     * Joint identities after the float round-trip of the five angles (float
     * ulp of an angle in [-π,π] is ~1.2e-7 rad; ~2.3e-6 blocks at full
     * reach — 1e-4 leaves 40× margin while still sub-visual).
     */
    private static final double EPS_JOINT = 1.0E-4;
    /** FABRIK convergence tolerance + float noise + margin. */
    private static final double EPS_TARGET = 0.011;

    // =====================================================================
    // S2 render-parity harness (design doc D2's mandated first proof)
    // =====================================================================

    /**
     * Double-precision forward kinematics of the shipped leg pose — a direct
     * reimplementation of {@code ModelSpiderRobot.poseLeg}: model hip pivot
     * per :325-327, per-segment chain advance per :334-344 (segment direction
     * {@code (cos(a)·sin(yd), −sin(a), cos(a)·cos(yd))·99px} with
     * {@code a_i = p_i + ud}), then the render transform's model→world action
     * for vectors (horizontal azimuth reflection
     * {@code α_world = yawRad − α_model}, vertical negation, ÷16 — derivation
     * and the two shared-with-classic absolute-space caveats, the vanilla
     * +1.501 vertical render translate and entity-vs-body yaw, are documented
     * in {@code ModernSpiderGait.solveLegAngles}; both are constants/shared
     * offsets that cancel from every vector this harness asserts on).
     *
     * <p>The five input angles are round-tripped through {@code float}
     * first, because that is the precision the real path stores/consumes
     * ({@code clientTick} stores float yd/ud; {@code poseLeg} casts the
     * p-angles to float at consumption).</p>
     *
     * @return world positions of hip, knee1, knee2, foot
     */
    private static double[][] modelForwardKinematics(int leg, double bodyX, double bodyY, double bodyZ,
                                                     float yawDeg, double[] angles) {
        double yd = (float) angles[0];
        double ud = (float) angles[1];
        double ymid = SpiderRigProfile.neutralYaw(leg);
        double legoff = SpiderRigProfile.hipRadial(leg);
        double yoff = SpiderRigProfile.hipVertical(leg);

        // Model-space chain in pixels (ModelSpiderRobot.poseLeg:325-327, 334-344).
        double[][] model = new double[4][3];
        model[0][0] = -Math.cos(ymid) * legoff * 16.0;
        model[0][1] = yoff * -16.0;
        model[0][2] = Math.sin(ymid) * legoff * 16.0;
        double[] segAngles = {
                (float) angles[2] + (float) ud,
                (float) angles[3] + (float) ud,
                (float) angles[4] + (float) ud};
        for (int i = 0; i < 3; ++i) {
            double a = segAngles[i];
            model[i + 1][0] = model[i][0] + Math.cos(a) * Math.sin(yd) * 99.0;
            model[i + 1][1] = model[i][1] - Math.sin(a) * 99.0;
            model[i + 1][2] = model[i][2] + Math.cos(a) * Math.cos(yd) * 99.0;
        }

        // Model → world (vector action of the render transform).
        double yawRad = Math.toRadians(Mth.wrapDegrees((double) yawDeg));
        double[][] world = new double[4][3];
        for (int i = 0; i < 4; ++i) {
            double h = Math.hypot(model[i][0], model[i][2]);
            double alphaM = Math.atan2(model[i][2], model[i][0]);
            double alphaW = yawRad - alphaM;
            world[i][0] = bodyX + h / 16.0 * Math.cos(alphaW);
            world[i][1] = bodyY - model[i][1] / 16.0;
            world[i][2] = bodyZ + h / 16.0 * Math.sin(alphaW);
        }
        return world;
    }

    private static void assertClose(GameTestHelper helper, String what,
                                    double ax, double ay, double az,
                                    double bx, double by, double bz, double eps) {
        double d = Math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by) + (az - bz) * (az - bz));
        helper.assertTrue(d <= eps, what + " off by " + d + " (> " + eps + "): ("
                + ax + "," + ay + "," + az + ") vs (" + bx + "," + by + "," + bz + ")");
    }

    /**
     * The mandated conversion proof: for every leg, a spread of body yaws and
     * foot targets — rest pose, offset, near-fold, raised, and the
     * near-full-extension band (17.0 / 18.2 / 18.5 of 18.5625 max reach)
     * where FABRIK converges slowest — the angles emitted by
     * {@code ModernSpiderGait.solveLegAngles} must, when pushed through the
     * model's own forward kinematics and the render transform, put the hip
     * exactly on the rig hip, every knee on the FABRIK joint, and the foot
     * on the requested target (within the FABRIK convergence tolerance;
     * unreachable targets get the straight full-reach chain).
     */
    @GameTest(template = "empty")
    public void s2_render_parity_harness(GameTestHelper helper) {
        double bodyX = 100.5;
        double bodyY = 64.0;
        double bodyZ = -200.25;
        float[] yaws = {0.0f, 37.5f, 90.0f, 180.0f, 271.3f, -45.0f};
        double[][] joints = {new double[2], new double[2], new double[2], new double[2]};
        double[] angles = new double[5];
        int cases = 0;

        for (float yaw : yaws) {
            for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                double hx = SpiderRigProfile.hipX(leg, bodyX, yaw);
                double hy = SpiderRigProfile.hipY(leg, bodyY);
                double hz = SpiderRigProfile.hipZ(leg, bodyZ, yaw);
                double bearing = SpiderRigProfile.legBearing(leg, yaw);
                double outX = -Math.sin(bearing);
                double outZ = Math.cos(bearing);
                double rest = SpiderRigProfile.restReach(leg);

                double[][] targets = {
                        {hx + outX * rest, bodyY, hz + outZ * rest},                    // rest stance
                        {hx + outX * (rest - 2.0) + 1.0, bodyY - 1.0, hz + outZ * (rest - 2.0) - 1.5}, // drifted
                        {hx + outX * 3.0, bodyY - 2.0, hz + outZ * 3.0},                // near-fold
                        {hx + outX * 8.0, bodyY + 3.0, hz + outZ * 8.0},                // raised footing
                        {hx + outX * 17.0, hy, hz + outZ * 17.0},                       // near-extension band
                        {hx + outX * 18.2, hy - 2.0, hz + outZ * 18.2},                 // near-extension, low
                        {hx + outX * 18.5, hy, hz + outZ * 18.5},                       // 99.7% of max reach
                        {hx + outX * 25.0, bodyY - 1.0, hz + outZ * 25.0},              // unreachable
                };
                for (double[] t : targets) {
                    ModernSpiderGait.solveLegAngles(SpiderRigProfile.RIG, bodyX, bodyY, bodyZ, yaw,
                            leg, t[0], t[1], t[2], joints, angles);
                    double[][] fk = modelForwardKinematics(leg, bodyX, bodyY, bodyZ, yaw, angles);

                    // Hip: the render transform must land exactly on the rig hip.
                    assertClose(helper, "leg " + leg + " yaw " + yaw + " hip",
                            fk[0][0], fk[0][1], fk[0][2], hx, hy, hz, EPS_HIP);

                    // Every joint: FK-of-angles == the FABRIK solution mapped to
                    // world. The u-axis fallback mirrors the solver's own
                    // (same 1e-6 epsilon, same neutral-bearing axis).
                    double dx = t[0] - hx;
                    double dz = t[2] - hz;
                    double dh = Math.sqrt(dx * dx + dz * dz);
                    double ux;
                    double uz;
                    if (dh > 1.0E-6) {
                        ux = dx / dh;
                        uz = dz / dh;
                    } else {
                        double fallback = SpiderRigProfile.legBearing(leg, yaw) + Math.PI / 2.0;
                        ux = Math.cos(fallback);
                        uz = Math.sin(fallback);
                    }
                    for (int j = 1; j <= 3; ++j) {
                        assertClose(helper, "leg " + leg + " yaw " + yaw + " joint " + j,
                                fk[j][0], fk[j][1], fk[j][2],
                                hx + ux * joints[j][0], hy + joints[j][1], hz + uz * joints[j][0],
                                EPS_JOINT);
                    }

                    // Foot-on-target for reachable targets; full straight reach otherwise.
                    double dy = t[1] - hy;
                    double targetDist = Math.sqrt(dh * dh + dy * dy);
                    if (targetDist < SpiderRigProfile.MAX_REACH) {
                        assertClose(helper, "leg " + leg + " yaw " + yaw + " foot (dist "
                                        + String.format("%.2f", targetDist) + ")",
                                fk[3][0], fk[3][1], fk[3][2], t[0], t[1], t[2], EPS_TARGET);
                    } else {
                        double reach = Math.sqrt(
                                (fk[3][0] - hx) * (fk[3][0] - hx)
                                        + (fk[3][1] - hy) * (fk[3][1] - hy)
                                        + (fk[3][2] - hz) * (fk[3][2] - hz));
                        helper.assertTrue(Math.abs(reach - SpiderRigProfile.MAX_REACH) < 1.0E-3,
                                "unreachable target must straighten to full reach, got " + reach);
                    }
                    ++cases;
                }
            }
        }
        helper.assertTrue(cases == yaws.length * SpiderRigProfile.LEG_COUNT * 8,
                "harness case count mismatch: " + cases);
        helper.succeed();
    }

    /**
     * FABRIK sanity pinned separately from the conversion: solved chains keep
     * exact segment lengths, the straighten-bias seed folds the knee upward
     * (knee1 above the hip-foot chord), and — the review's convergence-cliff
     * canary — the foot lands within tolerance across the near-extension
     * band that exhausted the original 20-iteration budget. The trigger
     * radius lerp is pinned here too (its runtime path is exercised by the
     * physics-driven walk test).
     */
    @GameTest(template = "empty")
    public void s2_fabrik_segment_and_knee_properties(GameTestHelper helper) {
        double[][] joints = {new double[2], new double[2], new double[2], new double[2]};
        double L = SpiderRigProfile.SEGMENT_LENGTH;
        double[][] targets = {
                {16.0, -0.3}, {10.0, -0.3}, {12.0, 2.0}, {5.0, -3.0}, {3.0, -1.0},
                {17.5, 1.0}, {17.9, 0.0}, {18.2, -1.0}, {18.5, 0.0}, {18.55, 0.0}};
        for (double[] t : targets) {
            PlanarFabrik.solve(L, t[0], t[1], PlanarFabrik.DEFAULT_KNEE_BIAS, joints);
            for (int i = 0; i < 3; ++i) {
                double du = joints[i + 1][0] - joints[i][0];
                double dv = joints[i + 1][1] - joints[i][1];
                double len = Math.sqrt(du * du + dv * dv);
                helper.assertTrue(Math.abs(len - L) < 1.0E-9,
                        "segment " + i + " length " + len + " != " + L + " for target (" + t[0] + "," + t[1] + ")");
            }
            // Convergence: reachable targets end within tolerance (the 18.5x
            // cases needed ~100-275 iterations — the budget must cover them).
            double dist = Math.sqrt(t[0] * t[0] + t[1] * t[1]);
            if (dist < SpiderRigProfile.MAX_REACH) {
                double eu = joints[3][0] - t[0];
                double ev = joints[3][1] - t[1];
                double residual = Math.sqrt(eu * eu + ev * ev);
                helper.assertTrue(residual <= PlanarFabrik.TOLERANCE + 1.0E-9,
                        "FABRIK residual " + residual + " at target (" + t[0] + "," + t[1] + ")");
            }
            // Knee-up: knee1 sits above the straight hip→target chord.
            double chordV = t[1] * (joints[1][0] / Math.max(t[0], 1.0E-9));
            helper.assertTrue(joints[1][1] > chordV - 1.0E-9,
                    "knee folded below the chord for target (" + t[0] + "," + t[1] + ")");
        }
        // Trigger-radius lerp endpoints + midpoint monotonicity (S5b: the
        // radius lives on the rig; the spider's numbers are unchanged).
        helper.assertTrue(SpiderRigProfile.RIG.triggerRadius(0.0) == 2.0, "trigger radius at rest");
        helper.assertTrue(SpiderRigProfile.RIG.triggerRadius(1.0) == 5.0, "trigger radius saturates at full speed");
        double mid = SpiderRigProfile.RIG.triggerRadius(0.175);
        helper.assertTrue(mid > 2.0 && mid < 5.0 && Math.abs(mid - 3.5) < 1.0E-9,
                "trigger radius half-speed midpoint, got " + mid);
        // S5b review: pin the rig-derived tilt spans against an independent
        // recompute from the RAW classic tables (the spans replaced
        // compile-time constants in the LegRig refactor and were otherwise
        // covered only by loose sign bounds).
        double[] legoffT = {1.25, 1.25, 2.0, 2.0, 1.75, 1.75, 3.4, 3.4};
        double[] ymidT = {-0.32, 3.4615927, -1.0, 4.1415925, 0.62831855, 2.5132742, 1.05, 2.0915928};
        double[] restT = {16.0, 16.0, 16.0, 16.0, 10.0, 10.0, 10.0, 10.0};
        double frontZ = 0.0;
        double rearZ = 0.0;
        double evenX = 0.0;
        double oddX = 0.0;
        for (int leg = 0; leg < 8; ++leg) {
            double radius = legoffT[leg] + restT[leg];
            double x = -radius * Math.cos(ymidT[leg]);
            double z = -radius * Math.sin(ymidT[leg]);
            if (leg < 4) {
                frontZ += z / 4.0;
            } else {
                rearZ += z / 4.0;
            }
            if ((leg & 1) == 0) {
                evenX += x / 4.0;
            } else {
                oddX += x / 4.0;
            }
        }
        helper.assertTrue(Math.abs(SpiderRigProfile.RIG.pitchSpan() - Math.abs(frontZ - rearZ)) < 1.0E-9,
                "spider pitchSpan drifted from the table-derived value: " + SpiderRigProfile.RIG.pitchSpan());
        helper.assertTrue(Math.abs(SpiderRigProfile.RIG.rollSpan() - Math.abs(oddX - evenX)) < 1.0E-9,
                "spider rollSpan drifted from the table-derived value: " + SpiderRigProfile.RIG.rollSpan());
        helper.succeed();
    }

    // =====================================================================
    // Gait invariants 1-3 (design doc §4) — isolated batch, modern mode
    // =====================================================================

    /**
     * Invariants over a flat-ground walk: after a settle window every foot is
     * grounded near floor height (inv 2); planted feet never move between
     * observations while the body walks ~19 blocks (inv 1, the no-slide
     * contract); every leg steps at least twice with no leg swinging at the
     * same time as its mirrored partner or same-side neighbors (inv 3).
     *
     * <p>Drive mechanics (independent-review fix): the body is driven by
     * setting its delta movement each test tick, so the entity's own physics
     * moves it during its tick and {@code serverTick} observes a real
     * {@code xo} delta — a {@code setPos}-driven body reads as stationary
     * (the server resets old-pos immediately before each entity tick),
     * silently bypassing the speed-radius lerp and velocity lookahead.
     * Friction is irrelevant because the delta is re-pinned every tick.</p>
     *
     * <p>Structure-shell note: the test box is encased in barrier walls by
     * the framework. Front-leg rest columns sweep past the far wall plane
     * late in the walk; the column scan then tops out on the barrier
     * (footing at body Y+3) — legal raised footings under the S2 flat-ground
     * scan. The vertical-retrigger gain guard keeps that from thrashing
     * (the pre-review code livelocked exactly here), and the step-count
     * upper bound would catch a regression.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s2_gait_invariants_flat_walk(GameTestHelper helper) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        final SpiderRobot spider;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            spider = helper.spawn(ModEntities.SPIDER_ROBOT.get(), new BlockPos(24, 3, 24));
        } finally {
            // Construction snapshot taken — restore immediately.
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        ModernSpiderGait gait = spider.getModernGait();
        helper.assertTrue(gait != null, "spider constructed under MODERN must carry the gait controller");

        final int settleTicks = 30;
        final int walkTicks = 65;      // ~19.5 blocks at 0.30 blocks/tick
        final double driveSpeed = 0.30;
        final double[][] prevFoot = new double[SpiderRigProfile.LEG_COUNT][3];
        final boolean[] prevPlanted = new boolean[SpiderRigProfile.LEG_COUNT];
        final boolean[] prevSwinging = new boolean[SpiderRigProfile.LEG_COUNT];
        final int[] steps = new int[SpiderRigProfile.LEG_COUNT];
        final int[] tick = {0};

        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < settleTicks) {
                    return;
                }
                if (t == settleTicks) {
                    // Invariant 2: at rest on flat ground, everything is planted.
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not grounded after " + settleTicks + " settle ticks (inv 2)");
                        helper.assertTrue(Math.abs(gait.footY(leg) - spider.getY()) < 1.5,
                                "leg " + leg + " foot Y " + gait.footY(leg) + " far from body Y "
                                        + spider.getY() + " on flat ground (inv 2)");
                        prevFoot[leg][0] = gait.footX(leg);
                        prevFoot[leg][1] = gait.footY(leg);
                        prevFoot[leg][2] = gait.footZ(leg);
                        prevPlanted[leg] = true;
                        prevSwinging[leg] = false;
                    }
                    return;
                }

                // Drive the body straight +X through its own physics.
                spider.setDeltaMovement(driveSpeed, spider.getDeltaMovement().y, 0.0);

                for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                    boolean planted = gait.isGrounded(leg);
                    boolean swinging = gait.isSwinging(leg);

                    // Invariant 1: a foot planted across two observations has not moved.
                    if (planted && prevPlanted[leg]) {
                        double d = Math.abs(gait.footX(leg) - prevFoot[leg][0])
                                + Math.abs(gait.footY(leg) - prevFoot[leg][1])
                                + Math.abs(gait.footZ(leg) - prevFoot[leg][2]);
                        helper.assertTrue(d < 1.0E-6,
                                "planted foot " + leg + " slid " + d + " blocks (inv 1)");
                    }
                    if (planted) {
                        prevFoot[leg][0] = gait.footX(leg);
                        prevFoot[leg][1] = gait.footY(leg);
                        prevFoot[leg][2] = gait.footZ(leg);
                    }
                    prevPlanted[leg] = planted;

                    // Invariant 3a (amended S6b, owner-ratified reference
                    // behavior): comfort-invalidated and stranded lifts
                    // BYPASS the inhibitors (canMoveLeg's unconditional
                    // first line in the reference), so co-swing is legal
                    // iff at least one of the two swings was FORCED.
                    // Ordinary drift steps still never co-swing.
                    if (swinging) {
                        int partner = SpiderRigProfile.pairedWith(leg);
                        if (gait.isSwinging(partner)) {
                            helper.assertTrue(gait.isForcedLift(leg) || gait.isForcedLift(partner),
                                    "leg " + leg + " and its pair partner swing simultaneously "
                                            + "with neither lift forced (inv 3)");
                        }
                        if (leg - 2 >= 0 && gait.isSwinging(leg - 2)) {
                            helper.assertTrue(gait.isForcedLift(leg) || gait.isForcedLift(leg - 2),
                                    "legs " + leg + " and " + (leg - 2) + " (same side) swing "
                                            + "simultaneously with neither lift forced (inv 3)");
                        }
                    }
                    if (swinging && !prevSwinging[leg]) {
                        ++steps[leg];
                    }
                    prevSwinging[leg] = swinging;
                }

                if (t == settleTicks + walkTicks) {
                    // Invariant 3b: cadence over ~19 blocks of travel.
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertTrue(steps[leg] >= 2,
                                "leg " + leg + " stepped only " + steps[leg] + " times over the walk (inv 3)");
                        helper.assertTrue(steps[leg] <= 20,
                                "leg " + leg + " stepped " + steps[leg] + " times — thrashing (inv 3)");
                    }
                    helper.assertTrue(spider.getX() - helper.absolutePos(new BlockPos(24, 3, 24)).getX() > 10.0,
                            "drive failed: spider only reached relative x offset "
                                    + (spider.getX() - helper.absolutePos(new BlockPos(24, 3, 24)).getX()));
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard(); // don't leak the walker into later plot reuse
                throw e;
            }
        });
    }

    /**
     * The construction-time snapshot contract (design doc D4) plus the
     * deliberate-duplication guard: a spider constructed under CLASSIC has no
     * modern controller (and a false synced flag) and keeps both through a
     * config flip; the classic solver's live per-leg tables must equal the
     * modern rig mirror ({@code SpiderRigProfile} documents why the values
     * are duplicated rather than shared).
     */
    @GameTest(template = "empty", batch = "spiderGaitIsolation")
    public void s2_movement_mode_construction_snapshot(GameTestHelper helper) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            // Classic with the master off (master-override ruling 2026-09-04).
            OreSpawnConfig.MODERN_ENABLED.set(false);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.CLASSIC);
            SpiderRobot classic = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 2));
            helper.assertTrue(classic.getModernGait() == null,
                    "CLASSIC-constructed spider must not carry a modern gait controller");
            helper.assertFalse(classic.isModernMovement(),
                    "CLASSIC-constructed spider must sync a false modern flag");

            // Mirror guard: classic initLegData values == SpiderRigProfile.
            RenderSpiderRobotInfo r = classic.getRenderSpiderRobotInfo();
            for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                helper.assertTrue(r.ymid[leg] == (float) SpiderRigProfile.neutralYaw(leg),
                        "ymid mirror drift at leg " + leg);
                helper.assertTrue(r.legoff[leg] == (float) SpiderRigProfile.hipRadial(leg),
                        "legoff mirror drift at leg " + leg);
                helper.assertTrue(r.yoff[leg] == (float) SpiderRigProfile.hipVertical(leg),
                        "yoff mirror drift at leg " + leg);
                helper.assertTrue(Math.abs(r.yrange[leg]) == (float) SpiderRigProfile.swingRange(leg),
                        "yrange mirror drift at leg " + leg);
                helper.assertTrue(r.pairedwith[leg] == SpiderRigProfile.pairedWith(leg),
                        "pairedwith mirror drift at leg " + leg);
            }

            // The effective flip: master on AND key MODERN.
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            helper.assertTrue(classic.getModernGait() == null && !classic.isModernMovement(),
                    "config flip must not retrofit a live classic spider (construction snapshot)");
            SpiderRobot modern = helper.spawnWithNoFreeWill(ModEntities.SPIDER_ROBOT.get(), new BlockPos(2, 2, 4));
            helper.assertTrue(modern.getModernGait() != null && modern.isModernMovement(),
                    "MODERN-constructed spider must carry the gait controller + synced flag");
            classic.discard();
            modern.discard();
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        helper.succeed();
    }

    // =====================================================================
    // S3 terrain invariants — cliff recovery (inv 4), stairs, trample
    // =====================================================================

    /**
     * Spawns a modern-mode spider with the config immediately restored --
     * master raised with the key (master-override ruling 2026-09-04).
     */
    private static SpiderRobot spawnModern(GameTestHelper helper, BlockPos pos) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            return helper.spawn(ModEntities.SPIDER_ROBOT.get(), pos);
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
    }

    /**
     * Shared per-tick tracker: no-slide on planted feet, step counting on
     * swing starts, and grounded-census. Returns the planted-leg count.
     */
    private static int trackTick(GameTestHelper helper, ModernSpiderGait gait,
                                 double[][] prevFoot, boolean[] prevPlanted,
                                 boolean[] prevSwinging, int[] steps) {
        int plantedCount = 0;
        for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
            boolean planted = gait.isGrounded(leg);
            boolean swinging = gait.isSwinging(leg);
            if (planted && prevPlanted[leg]) {
                double d = Math.abs(gait.footX(leg) - prevFoot[leg][0])
                        + Math.abs(gait.footY(leg) - prevFoot[leg][1])
                        + Math.abs(gait.footZ(leg) - prevFoot[leg][2]);
                helper.assertTrue(d < 1.0E-6, "planted foot " + leg + " slid " + d + " blocks (inv 1)");
            }
            if (planted) {
                prevFoot[leg][0] = gait.footX(leg);
                prevFoot[leg][1] = gait.footY(leg);
                prevFoot[leg][2] = gait.footZ(leg);
                ++plantedCount;
            }
            prevPlanted[leg] = planted;
            if (swinging && !prevSwinging[leg]) {
                ++steps[leg];
            }
            prevSwinging[leg] = swinging;
        }
        return plantedCount;
    }

    /**
     * Invariant 4 (design doc §4): a spider walked off a cliff edge loses
     * footing, falls (vanilla gravity; fall damage immunity is classic
     * behavior), and re-plants every foot within a bounded settle window
     * with no re-step livelock and the no-slide contract intact throughout.
     * The platform phase also pins the S2/S3 retrigger reconciliation: legs
     * planted ~6 blocks below the platform body hold without thrashing until
     * a genuinely better footing exists.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s3_cliff_recovery(GameTestHelper helper) {
        // Raised platform (surface y=6) with a cliff edge at x=20; the
        // template's real walking surface is relative y=0 (all-air template
        // over the framework support platform), so the platform is filled
        // from y=0 and the drop off its edge is 6 blocks.
        for (int x = 2; x <= 20; ++x) {
            for (int z = 14; z <= 34; ++z) {
                for (int y = 0; y <= 5; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        final SpiderRobot spider = spawnModern(helper, new BlockPos(10, 7, 24));
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }

        final double[][] prevFoot = new double[SpiderRigProfile.LEG_COUNT][3];
        final boolean[] prevPlanted = new boolean[SpiderRigProfile.LEG_COUNT];
        final boolean[] prevSwinging = new boolean[SpiderRigProfile.LEG_COUNT];
        final int[] steps = new int[SpiderRigProfile.LEG_COUNT];
        final int[] phase = {0}; // 0 settle, 1 drive+fall, 2 recover
        final int[] phaseT = {0};
        final boolean[] sawAirborneLeg = {false};
        // Positions are ABSOLUTE world coordinates (test plots sit far below
        // y=0); all thresholds derive from the live settle height.
        final double[] platformY = {0.0};

        helper.onEachTick(() -> {
            try {
                ++phaseT[0];
                if (phase[0] == 0) {
                    if (phaseT[0] == 30) {
                        for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                            helper.assertTrue(gait.isGrounded(leg),
                                    "leg " + leg + " not grounded after settling on the platform");
                            prevFoot[leg][0] = gait.footX(leg);
                            prevFoot[leg][1] = gait.footY(leg);
                            prevFoot[leg][2] = gait.footZ(leg);
                            prevPlanted[leg] = true;
                            prevSwinging[leg] = false;
                        }
                        platformY[0] = spider.getY();
                        phase[0] = 1;
                        phaseT[0] = 0;
                    }
                    return;
                }
                int planted = trackTick(helper, gait, prevFoot, prevPlanted, prevSwinging, steps);
                if (planted < SpiderRigProfile.LEG_COUNT) {
                    sawAirborneLeg[0] = true;
                }
                if (phase[0] == 1) {
                    boolean landedLow = spider.onGround() && spider.getY() < platformY[0] - 3.0;
                    if (!landedLow) {
                        spider.setDeltaMovement(0.30, spider.getDeltaMovement().y, 0.0);
                        helper.assertTrue(phaseT[0] < 150, "spider never went over the cliff");
                    } else {
                        phase[0] = 2;
                        phaseT[0] = 0;
                    }
                    return;
                }
                if (phaseT[0] == 60) {
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not re-planted 60 ticks after the cliff landing (inv 4)");
                        // Cadence bound tight enough to actually catch a
                        // 7-tick-period retrigger livelock (review: <=30 was
                        // unfalsifiable over this window).
                        helper.assertTrue(steps[leg] <= 12,
                                "leg " + leg + " stepped " + steps[leg] + " times — livelock (inv 4)");
                    }
                    helper.assertTrue(sawAirborneLeg[0],
                            "no leg ever left the ground across the 6-block cliff — census broken");
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }

    /**
     * Stairs/slopes are emergent (design doc §1): a half-slab ramp is
     * climbed by the vanilla body (step height covers 0.5 risers) while the
     * legs adapt purely through the 3x3 scan — no special-case code. Exit
     * assertions: the body actually climbed, every foot has footing, the
     * no-slide contract held, and step counts stay bounded (no thrash
     * against the risers). Per-foot height is deliberately NOT asserted:
     * with a 16-block leg reach the outer feet legitimately plant far below
     * body level at the ramp shoulders.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s3_slab_stairs_climb(GameTestHelper helper) {
        // The empty templates are all air; the framework's support platform
        // under the structure is the floor, so the walking surface is
        // relative y=0 and terrain builds start THERE (first-run failure:
        // a ramp based at y=2 floats overhead and the spider walks under it).
        // Ramp: risers of 0.5 every 2 blocks along +X, full template width,
        // then a plateau. surface(k) = k/2 (integer) + 0.5 if k odd.
        for (int k = 1; k <= 10; ++k) {
            int full = k / 2;
            for (int xo = 0; xo <= 1; ++xo) {
                int x = 14 + 2 * k + xo;
                for (int z = 4; z <= 44; ++z) {
                    for (int y = 0; y < full; ++y) {
                        helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                    }
                    if (k % 2 == 1) {
                        helper.setBlock(new BlockPos(x, full, z),
                                net.minecraft.world.level.block.Blocks.SMOOTH_STONE_SLAB);
                    }
                }
            }
        }
        for (int x = 36; x <= 46; ++x) { // plateau at the ramp top (surface y=5)
            for (int z = 4; z <= 44; ++z) {
                for (int y = 0; y <= 4; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        final SpiderRobot spider = spawnModern(helper, new BlockPos(8, 1, 24));
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }

        final double[][] prevFoot = new double[SpiderRigProfile.LEG_COUNT][3];
        final boolean[] prevPlanted = new boolean[SpiderRigProfile.LEG_COUNT];
        final boolean[] prevSwinging = new boolean[SpiderRigProfile.LEG_COUNT];
        final int[] steps = new int[SpiderRigProfile.LEG_COUNT];
        final int[] tick = {0};
        final int settleTicks = 30;
        final int climbTicks = 105;
        // Positions are ABSOLUTE world coordinates; the climb is measured
        // from the live settle height, not a template-relative constant.
        final double[] startY = {0.0};

        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < settleTicks) {
                    return;
                }
                if (t == settleTicks) {
                    // Build sanity: the ramp must actually exist where built.
                    helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.SMOOTH_STONE_SLAB,
                            new BlockPos(16, 0, 24));
                    helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.STONE,
                            new BlockPos(38, 4, 24));
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        prevFoot[leg][0] = gait.footX(leg);
                        prevFoot[leg][1] = gait.footY(leg);
                        prevFoot[leg][2] = gait.footZ(leg);
                        prevPlanted[leg] = gait.isGrounded(leg);
                        prevSwinging[leg] = false;
                    }
                    startY[0] = spider.getY();
                    return;
                }
                if (t <= settleTicks + climbTicks) {
                    spider.setDeltaMovement(0.30, spider.getDeltaMovement().y, 0.0);
                }
                trackTick(helper, gait, prevFoot, prevPlanted, prevSwinging, steps);
                if (t == settleTicks + climbTicks) {
                    BlockPos origin = helper.absolutePos(BlockPos.ZERO);
                    double xOffset = spider.getX() - origin.getX();
                    double zOffset = spider.getZ() - origin.getZ();
                    double startYRel = startY[0] - origin.getY();
                    helper.assertTrue(spider.getY() - startY[0] >= 4.0,
                            "spider only climbed " + (spider.getY() - startY[0])
                                    + " blocks up the ramp (end rel x " + xOffset
                                    + ", rel z " + zOffset + ", settle rel y " + startYRel
                                    + ", onGround " + spider.onGround() + ")");
                }
                // Footing/cadence asserts run after a rest phase — a leg can
                // legitimately be mid-swing on the last driving tick, and the
                // post-stop settle waves (cooldown-serialized re-steps) need
                // headroom: 60 ticks per review margin analysis (30 left only
                // ~3-5 ticks under stacked worst-case timing + spawn-yaw
                // randomization).
                if (t == settleTicks + climbTicks + 60) {
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " without footing at the top of the ramp");
                        helper.assertTrue(steps[leg] <= 15,
                                "leg " + leg + " stepped " + steps[leg] + " times on the ramp — thrashing");
                    }
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }

    /**
     * S3 server-side trample: a RIDDEN modern spider's foot touchdowns
     * convert short grass to air and the grass block below to dirt, on the
     * server, mobGriefing permitting — classic's exact trigger and block
     * logic run at classic's client-only site untouched (that quirk stays;
     * on a dedicated server classic tramples nothing, faithfully).
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s3_modern_trample_server_side(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getGameRules().getBoolean(
                        net.minecraft.world.level.GameRules.RULE_MOBGRIEFING),
                "test assumes mobGriefing=true (vanilla default)");
        // Grass field across the WHOLE walkable floor, based at the real
        // surface (relative y=0 — all-air template over the framework
        // support platform): grass blocks at y=0, short grass at y=1. A
        // raised band would exceed the body's 0.6 step height and stall the
        // walk (first-run failure); on a full field the spider spawns and
        // walks ON the grass tops, so every settled ridden foot is a trample
        // opportunity. Classic's (int)-truncation quirk (mirrored in modern)
        // shifts the checked cell one block toward +x/+z at negative absolute
        // coordinates — absorbed by the field-wide dirt scan below, so do
        // NOT "fix" the scan to check exact foot columns.
        for (int x = 2; x <= 46; ++x) {
            for (int z = 4; z <= 44; ++z) {
                helper.setBlock(new BlockPos(x, 0, z), net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
                helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.SHORT_GRASS);
            }
        }
        final SpiderRobot spider = spawnModern(helper, new BlockPos(8, 3, 24));
        final ModernSpiderGait gait;
        final net.minecraft.world.entity.Entity rider;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
            // S5: the rider is a SpiderDriver, not a mock player — a player
            // rider is now CONTROLLING on modern spiders (Q1), which makes
            // the server skip travel and freezes a packet-less mock-ridden
            // spider; the driver is never controlling, keeps the dm drive
            // alive, and is the classic trample scenario anyway.
            rider = helper.spawnWithNoFreeWill(danger.orespawn.ModEntities.SPIDER_DRIVER.get(),
                    new BlockPos(8, 6, 24));
            // S5 review: the driver's mounted-combat drive lives in
            // customServerAiStep, which removeFreeWill does NOT strip —
            // NoAi makes the rider genuinely inert instead of relying on
            // tick-ordering to shield the test's dm writes.
            ((net.minecraft.world.entity.Mob) rider).setNoAi(true);
            helper.assertTrue(rider.startRiding(spider, true), "driver failed to mount");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }

        final int[] tick = {0};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 20) {
                    return;
                }
                if (t < 100) {
                    spider.setDeltaMovement(0.30, spider.getDeltaMovement().y, 0.0);
                    return;
                }
                int dirt = 0;
                for (int x = 2; x <= 46 && dirt == 0; ++x) {
                    for (int z = 4; z <= 44 && dirt == 0; ++z) {
                        if (helper.getBlockState(new BlockPos(x, 0, z))
                                .is(net.minecraft.world.level.block.Blocks.DIRT)) {
                            ++dirt;
                        }
                    }
                }
                helper.assertTrue(dirt >= 1,
                        "no grass block was trampled to dirt by the ridden modern spider (end x offset "
                                + (spider.getX() - helper.absolutePos(new BlockPos(8, 3, 24)).getX()) + ")");
                rider.stopRiding();
                rider.discard();
                spider.discard();
                helper.succeed();
            } catch (RuntimeException e) {
                rider.discard();
                spider.discard();
                throw e;
            }
        });
    }

    // =====================================================================
    // S3b body dynamics — tilt-compensation harness (FIRST, per directive),
    // flat settle/sag, ramp tilt sign
    // =====================================================================

    /**
     * S3b's mandated first proof (the S2 angle-conversion of this slice),
     * hardened per independent review. Three layers, weakest to strongest:
     * (1) T∘T⁻¹ identity on probe vectors; (2) a JOML replay of the
     * renderer's EXACT op sequence (translate(lift+1.501), Ry(a), Rx(p),
     * Rz(r), Ry(−a), translate(−1.501)) asserted equal to the double-math
     * {@code bodyTransform} — this catches transcription drift
     * (order/handedness/sign/pivot), the blind spot in which the review
     * found the anchor-pivot BLOCKER; (3) the full compensation round trip:
     * inverse-transform the target (with production's reach clamp
     * mirrored), solve, FK, forward-transform, and assert the foot back on
     * its anchor — exact for unclamped cases, bounded graceful shortfall
     * for clamped ones. Every grid cell is accounted for; nothing is
     * silently skipped. What remains render-untestable (per-frame vanilla
     * yaw interpolation) is the classic-shared quirk family documented in
     * solveLegAngles.
     */
    @GameTest(template = "empty")
    public void s3b_tilt_compensation_harness(GameTestHelper helper) {
        double renderOffset = ModernSpiderGait.VANILLA_RENDER_Y_OFFSET;

        // (1) Transform-pair identity on representative vectors.
        double[][] probes = {{17.2, -0.4, 3.1}, {-9.5, 5.9, -12.0}, {0.1, -6.0, 16.4}};
        for (double[] p : probes) {
            double[] v = {p[0], p[1], p[2]};
            ModernSpiderGait.bodyTransform(37.5, 0.3, -0.22, 0.7, v);
            ModernSpiderGait.inverseBodyTransform(37.5, 0.3, -0.22, 0.7, v);
            assertClose(helper, "T^-1(T(v)) identity", v[0], v[1], v[2], p[0], p[1], p[2], 1.0E-9);
        }

        // (2) Renderer-transcription replay: the PoseStack op sequence as a
        // JOML matrix must act on model-chain points (solver vector + the
        // vanilla +1.501 offset) exactly as bodyTransform acts on solver
        // vectors.
        double[][] replayCases = {{0.0, 0.35, 0.0, 0.0}, {37.5, -0.3, 0.2, -0.6}, {-90.0, 0.25, -0.35, 1.0}};
        for (double[] c : replayCases) {
            float a = (float) -Math.toRadians(Mth.wrapDegrees(c[0]));
            org.joml.Matrix4f m = new org.joml.Matrix4f();
            m.translate(0.0f, (float) (c[3] + renderOffset), 0.0f);
            m.rotateY(a);
            m.rotateX((float) c[1]);
            m.rotateZ((float) c[2]);
            m.rotateY(-a);
            m.translate(0.0f, (float) -renderOffset, 0.0f);
            for (double[] p : probes) {
                org.joml.Vector4f v = new org.joml.Vector4f(
                        (float) p[0], (float) (p[1] + renderOffset), (float) p[2], 1.0f);
                m.transform(v);
                double[] expect = {p[0], p[1], p[2]};
                ModernSpiderGait.bodyTransform(c[0], c[1], c[2], c[3], expect);
                assertClose(helper, "renderer-replay vs bodyTransform (yaw " + c[0] + ")",
                        v.x(), v.y(), v.z(),
                        expect[0], expect[1] + renderOffset, expect[2], 1.0E-3);
            }
        }

        // (3) Full compensation round trip.
        double bodyX = 100.5;
        double bodyY = 64.0;
        double bodyZ = -200.25;
        float[] yaws = {0.0f, 37.5f, 217.0f, -90.0f};
        double[] lifts = {-0.8, 0.0, 0.6, 1.0};
        double[] pitches = {-0.3, 0.0, 0.25};
        double[] rolls = {-0.2, 0.0, 0.3};
        double[][] joints = {new double[2], new double[2], new double[2], new double[2]};
        double[] angles = new double[5];
        double[] rel = new double[3];
        double reachCap = SpiderRigProfile.MAX_REACH * 0.98; // production REACH_MARGIN
        int exact = 0;
        int clamped = 0;
        int total = 0;

        for (float yaw : yaws) {
            for (double lift : lifts) {
                for (double pitch : pitches) {
                    for (double roll : rolls) {
                        for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                            double hx = SpiderRigProfile.hipX(leg, bodyX, yaw);
                            double hy = SpiderRigProfile.hipY(leg, bodyY);
                            double hz = SpiderRigProfile.hipZ(leg, bodyZ, yaw);
                            double bearing = SpiderRigProfile.legBearing(leg, yaw);
                            double outX = -Math.sin(bearing);
                            double outZ = Math.cos(bearing);
                            double rest = SpiderRigProfile.restReach(leg);
                            double[][] targets = {
                                    {hx + outX * rest, bodyY, hz + outZ * rest}, // true stance radius
                                    {hx + outX * rest * 0.75, bodyY, hz + outZ * rest * 0.75},
                                    {hx + outX * 5.0, bodyY - 1.5, hz + outZ * 5.0},
                                    {hx + outX * (rest * 0.6), bodyY + 1.0, hz + outZ * (rest * 0.6)},
                            };
                            for (double[] t : targets) {
                                ++total;
                                rel[0] = t[0] - bodyX;
                                rel[1] = t[1] - bodyY;
                                rel[2] = t[2] - bodyZ;
                                ModernSpiderGait.inverseBodyTransform(yaw, pitch, roll, lift, rel);
                                // Production's reach clamp, mirrored exactly.
                                double rdx = (bodyX + rel[0]) - hx;
                                double rdy = (bodyY + rel[1]) - hy;
                                double rdz = (bodyZ + rel[2]) - hz;
                                double dist = Math.sqrt(rdx * rdx + rdy * rdy + rdz * rdz);
                                double shortfall = 0.0;
                                if (dist > reachCap) {
                                    double scale = reachCap / dist;
                                    rel[0] = (hx - bodyX) + rdx * scale;
                                    rel[1] = (hy - bodyY) + rdy * scale;
                                    rel[2] = (hz - bodyZ) + rdz * scale;
                                    shortfall = dist - reachCap;
                                    ++clamped;
                                } else {
                                    ++exact;
                                }
                                ModernSpiderGait.solveLegAngles(SpiderRigProfile.RIG, bodyX, bodyY, bodyZ, yaw,
                                        leg, bodyX + rel[0], bodyY + rel[1], bodyZ + rel[2],
                                        joints, angles);
                                double[][] fk = modelForwardKinematics(leg, bodyX, bodyY, bodyZ, yaw, angles);
                                rel[0] = fk[3][0] - bodyX;
                                rel[1] = fk[3][1] - bodyY;
                                rel[2] = fk[3][2] - bodyZ;
                                ModernSpiderGait.bodyTransform(yaw, pitch, roll, lift, rel);
                                double miss = Math.sqrt(
                                        (bodyX + rel[0] - t[0]) * (bodyX + rel[0] - t[0])
                                                + (bodyY + rel[1] - t[1]) * (bodyY + rel[1] - t[1])
                                                + (bodyZ + rel[2] - t[2]) * (bodyZ + rel[2] - t[2]));
                                helper.assertTrue(miss <= shortfall + 0.012,
                                        "tilted foot miss " + miss + " > shortfall " + shortfall
                                                + " + tol (leg " + leg + " yaw " + yaw + " lift " + lift
                                                + " pitch " + pitch + " roll " + roll + ")");
                            }
                        }
                    }
                }
            }
        }
        helper.assertTrue(exact + clamped == total,
                "tilt harness accounting broke: " + exact + " + " + clamped + " != " + total);
        helper.assertTrue(exact >= total * 7 / 10,
                "tilt harness clamped too much of the grid: exact " + exact + " of " + total);
        helper.succeed();
    }

    /**
     * S3b invariant: on flat ground the body settles at its preferred
     * height (lift ≈ 0) without oscillation; a sudden 8-block teleport up
     * (feet left below, support collapsing) sags the visual body toward
     * MAX_SAG, and after the vanilla fall + re-plant it settles again.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s3b_body_settles_flat_and_sags(GameTestHelper helper) {
        final SpiderRobot spider = spawnModern(helper, new BlockPos(24, 2, 24));
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }
        final int[] tick = {0};
        final double[] lastLift = {0.0};
        final double[] maxDelta = {0.0};
        final double[] minLiftDuringSag = {Double.MAX_VALUE};

        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                double lift = gait.bodyLift();
                if (t > 30 && t <= 40) {
                    // Settle-stability window: small lift, no oscillation.
                    maxDelta[0] = Math.max(maxDelta[0], Math.abs(lift - lastLift[0]));
                }
                lastLift[0] = lift;
                if (t == 40) {
                    helper.assertTrue(Math.abs(lift) < 0.2,
                            "body lift " + lift + " not settled near preferred height on flat ground");
                    helper.assertTrue(maxDelta[0] < 0.05,
                            "body lift oscillating on flat ground (max per-tick delta " + maxDelta[0] + ")");
                    spider.setPos(spider.getX(), spider.getY() + 8.0, spider.getZ());
                    return;
                }
                if (t > 40 && t <= 70) {
                    minLiftDuringSag[0] = Math.min(minLiftDuringSag[0], lift);
                }
                if (t == 120) {
                    helper.assertTrue(minLiftDuringSag[0] < -0.2,
                            "body never sagged after losing support (min lift " + minLiftDuringSag[0] + ")");
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not re-planted after the drop");
                    }
                    helper.assertTrue(Math.abs(gait.bodyLift()) < 0.25,
                            "body lift " + gait.bodyLift() + " did not re-settle after landing");
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }

    /**
     * S3b invariant: climbing the slab ramp nose-first, the body pitches
     * NOSE-UP (negative in the bodyTransform convention: +pitch tips the
     * face down) while roll stays near zero (the ramp is laterally
     * symmetric), with the no-slide contract intact throughout. Yaw is
     * pinned to −90° (facing +X, the drive direction) so the slope loads
     * the pitch axis, not roll.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s3b_ramp_pitch_sign(GameTestHelper helper) {
        for (int k = 1; k <= 10; ++k) { // same geometry as s3_slab_stairs_climb
            int full = k / 2;
            for (int xo = 0; xo <= 1; ++xo) {
                int x = 14 + 2 * k + xo;
                for (int z = 4; z <= 44; ++z) {
                    for (int y = 0; y < full; ++y) {
                        helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                    }
                    if (k % 2 == 1) {
                        helper.setBlock(new BlockPos(x, full, z),
                                net.minecraft.world.level.block.Blocks.SMOOTH_STONE_SLAB);
                    }
                }
            }
        }
        for (int x = 36; x <= 46; ++x) {
            for (int z = 4; z <= 44; ++z) {
                for (int y = 0; y <= 4; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        final SpiderRobot spider = spawnModern(helper, new BlockPos(8, 1, 24));
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }
        final double[][] prevFoot = new double[SpiderRigProfile.LEG_COUNT][3];
        final boolean[] prevPlanted = new boolean[SpiderRigProfile.LEG_COUNT];
        final boolean[] prevSwinging = new boolean[SpiderRigProfile.LEG_COUNT];
        final int[] steps = new int[SpiderRigProfile.LEG_COUNT];
        final int[] tick = {0};
        final double[] minPitch = {Double.MAX_VALUE};
        final double[] maxAbsRoll = {0.0};

        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                spider.setYRot(-90.0f); // face +X so the slope is longitudinal
                if (t < 30) {
                    return;
                }
                if (t == 30) {
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        prevFoot[leg][0] = gait.footX(leg);
                        prevFoot[leg][1] = gait.footY(leg);
                        prevFoot[leg][2] = gait.footZ(leg);
                        prevPlanted[leg] = gait.isGrounded(leg);
                        prevSwinging[leg] = false;
                    }
                    return;
                }
                if (t <= 100) {
                    spider.setDeltaMovement(0.30, spider.getDeltaMovement().y, 0.0);
                }
                trackTick(helper, gait, prevFoot, prevPlanted, prevSwinging, steps);
                if (t > 55 && t <= 100) {
                    // Mid-climb window: the ramp is under the whole rig.
                    minPitch[0] = Math.min(minPitch[0], gait.bodyPitch());
                    maxAbsRoll[0] = Math.max(maxAbsRoll[0], Math.abs(gait.bodyRoll()));
                }
                if (t == 110) {
                    helper.assertTrue(minPitch[0] < -0.05,
                            "body never pitched nose-up on the ramp (min pitch " + minPitch[0] + ")");
                    helper.assertTrue(maxAbsRoll[0] < 0.2,
                            "body rolled " + maxAbsRoll[0] + " on a laterally symmetric ramp");
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }
}
