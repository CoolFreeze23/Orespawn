package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.TheQueen;
import danger.orespawn.entity.gait.ModernSpiderGait;
import danger.orespawn.entity.gait.SpiderRigProfile;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 2.0 S5 — the Q1 ridden path (server-testable wiring; player-driven travel
 * is client-controlled and provably untestable in gametests — the steering
 * FEEL is the owner's in-game session per the recorded exit evidence) plus
 * the ratified part-stream gate predicate and the S3b→S5 seat resolution.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RideTests {

    private static SpiderRobot spawnMode(GameTestHelper helper, BlockPos pos,
                                         OreSpawnConfig.SpiderMovement mode) {
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.SPIDER_MOVEMENT.set(mode);
            return helper.spawn(ModEntities.SPIDER_ROBOT.get(), pos);
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
        }
    }

    /**
     * The Q1 control truth table: a mounted PLAYER controls a MODERN spider,
     * never a CLASSIC one (the faithful 1.0 no-steer gap), and the
     * SpiderDriver controls neither — its classic velocity-set shoving
     * still moves the body.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5_ride_control_truth_table(GameTestHelper helper) {
        SpiderRobot modern = spawnMode(helper, new BlockPos(10, 2, 10), OreSpawnConfig.SpiderMovement.MODERN);
        SpiderRobot classic = spawnMode(helper, new BlockPos(10, 2, 30), OreSpawnConfig.SpiderMovement.CLASSIC);
        final Entity driverSpider = spawnMode(helper, new BlockPos(30, 2, 30), OreSpawnConfig.SpiderMovement.MODERN);
        try {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(player.startRiding(modern, true), "player failed to mount modern spider");
            helper.assertTrue(modern.getControllingPassenger() == player,
                    "a mounted player must CONTROL a modern spider (Q1)");
            player.stopRiding();
            helper.assertTrue(modern.getControllingPassenger() == null,
                    "control must clear on dismount");

            Player classicRider = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(classicRider.startRiding(classic, true), "player failed to mount classic spider");
            helper.assertTrue(classic.getControllingPassenger() == null,
                    "a CLASSIC spider must stay faithfully unsteerable (Q1)");
            classicRider.stopRiding();

            Mob driver = helper.spawnWithNoFreeWill(ModEntities.SPIDER_DRIVER.get(), new BlockPos(30, 5, 30));
            // S5 review: NoAi — the driver's combat drive lives in
            // customServerAiStep, which removeFreeWill does not strip.
            driver.setNoAi(true);
            helper.assertTrue(driver.startRiding(driverSpider, true), "driver failed to mount");
            helper.assertTrue(((SpiderRobot) driverSpider).getControllingPassenger() == null,
                    "the SpiderDriver must NEVER be controlling");
            final double startX = driverSpider.getX();
            final int[] tick = {0};
            helper.onEachTick(() -> {
                try {
                    int t = ++tick[0];
                    if (t <= 20) {
                        driverSpider.setDeltaMovement(0.25, driverSpider.getDeltaMovement().y, 0.0);
                        return;
                    }
                    helper.assertTrue(driverSpider.getX() - startX > 2.0,
                            "driver-ridden spider stopped moving under velocity-set shoving "
                                    + "(moved " + (driverSpider.getX() - startX) + ")");
                    driver.discard();
                    driverSpider.discard();
                    helper.succeed();
                } catch (RuntimeException e) {
                    driver.discard();
                    driverSpider.discard();
                    throw e;
                }
            });
        } finally {
            modern.discard();
            classic.discard();
        }
    }

    /**
     * S5 seat resolution (the S3b handoff): while ridden the visual body is
     * near-rigid — |lift| ≤ 0.15 — so the real-position-rendered rider
     * never detaches from the seat. Discriminating terrain (independent
     * review — on flat ground unridden lift is also ~0, so a flat drive
     * could never fail): a 2-high stone shelf ahead of the spider on the
     * FACING (+Z) axis — legs 2/3 rest at z-offset ≈ +15.15 (x ±9.73) at
     * yaw 0, so BOTH their full 3x3 scan windows sit on the shelf (z≈39,
     * five blocks inside its z=34 edge; robust against scan-scoring or
     * init-timing changes, per the verify pass — the first cut put the
     * shelf on +X off an axis-swapped offset table and its margin rode on
     * an init-height scan quirk). Two feet at +2 pull the unridden lift
     * target to +0.5, so the clamp is what holds the body down — proven by
     * the dismount control phase, where the body must RISE past the ridden
     * ceiling once the rider leaves.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5_ridden_seat_clamp(GameTestHelper helper) {
        for (int z = 34; z <= 46; ++z) {
            for (int x = 4; x <= 44; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        final SpiderRobot spider = spawnMode(helper, new BlockPos(24, 2, 24), OreSpawnConfig.SpiderMovement.MODERN);
        spider.setYRot(0.0f);
        final ModernSpiderGait gait;
        final Mob driver;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
            driver = helper.spawnWithNoFreeWill(ModEntities.SPIDER_DRIVER.get(), new BlockPos(24, 5, 24));
            // S5 review: NoAi — the combat drive lives in customServerAiStep,
            // which removeFreeWill does not strip.
            driver.setNoAi(true);
            helper.assertTrue(driver.startRiding(spider, true), "driver failed to mount");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }
        final int[] tick = {0};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 10) {
                    return;
                }
                if (t <= 100) {
                    helper.assertTrue(Math.abs(gait.bodyLift()) <= 0.15 + 1.0E-6,
                            "ridden body lift " + gait.bodyLift() + " escaped the ±0.15 seat clamp (S5)");
                    if (t == 100) {
                        driver.stopRiding();
                    }
                    return;
                }
                if (t == 180) {
                    // Control phase: with the rider gone the shelf must
                    // genuinely lift the body past the ridden ceiling —
                    // otherwise this test never discriminated the clamp.
                    helper.assertTrue(gait.bodyLift() > 0.3,
                            "dismounted body lift " + gait.bodyLift()
                                    + " never rose above the ridden ceiling — non-discriminating terrain");
                    driver.discard();
                    spider.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                driver.discard();
                spider.discard();
                throw e;
            }
        });
    }

    /**
     * S7a seat geometry (sitting FAIL-3): the spider's ORIGINAL seat was
     * never ported (orig :523-536 — a classic parity bug, restored) and
     * the modern seat composes through the S3b body transform. Driver
     * seats are bob-free on the spider (flat 2.0) — near-exact asserts;
     * fore-aft carries the ±0.05 bob. The modern assert inverse-transforms
     * the rider through the PUBLIC bodyTransform pair and checks the
     * seat-frame coordinates, with tolerance for one tick of rate-limited
     * dynamics drift between positioning and sampling.
     */
    @GameTest(template = "empty_large", timeoutTicks = 300, batch = "spiderGaitIsolation")
    public void s7_seat_geometry(GameTestHelper helper) {
        // Reviewer finding: on flat ground the dynamics converge to ~zero and
        // the inverse-transform assert degenerates to identity — seed a
        // 2-high shelf under the modern mounts' forward legs so pitch/lift
        // are NONZERO and a dropped/mis-signed composition cannot pass.
        for (int x = 20; x <= 44; ++x) {
            for (int z = 18; z <= 28; ++z) {
                for (int y = 0; y <= 1; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        final SpiderRobot classic = spawnMode(helper, new BlockPos(10, 2, 30), OreSpawnConfig.SpiderMovement.CLASSIC);
        final SpiderRobot modern = spawnMode(helper, new BlockPos(30, 2, 10), OreSpawnConfig.SpiderMovement.MODERN);
        final danger.orespawn.entity.AntRobot antClassic;
        final danger.orespawn.entity.AntRobot antModern;
        final Mob[] riders = new Mob[4];
        {
            OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
            try {
                OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.CLASSIC);
                antClassic = helper.spawn(ModEntities.ANT_ROBOT.get(), new BlockPos(10, 2, 10));
                OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
                antModern = helper.spawn(ModEntities.ANT_ROBOT.get(), new BlockPos(30, 2, 30));
            } finally {
                OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            }
        }
        try {
            Mob[] mounts = {classic, modern, antClassic, antModern};
            BlockPos[] at = {new BlockPos(10, 5, 30), new BlockPos(30, 5, 10),
                    new BlockPos(10, 5, 10), new BlockPos(30, 5, 30)};
            for (int i = 0; i < 4; ++i) {
                riders[i] = helper.spawnWithNoFreeWill(ModEntities.SPIDER_DRIVER.get(), at[i]);
                riders[i].setNoAi(true);
                helper.assertTrue(riders[i].startRiding(mounts[i], true), "mount " + i + " failed");
            }
        } catch (RuntimeException e) {
            classic.discard();
            modern.discard();
            antClassic.discard();
            antModern.discard();
            throw e;
        }
        final int[] tick = {0};
        final Player[] playerRider = new Player[1];
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 20) {
                    return;
                }
                // Classic spider: EXACT orig math — driver flat 2.0 up,
                // ~3.0 behind (t<30); then the PLAYER branch (reviewer
                // finding: the −0.5 TF-029 arm was untested): 2.125±bob.
                if (t < 30) {
                    double cy = riders[0].getY() - classic.getY();
                    helper.assertTrue(Math.abs(cy - 2.0) < 1.0E-6,
                            "classic spider driver seat height " + cy + " != orig 2.0");
                    double chor = Math.hypot(riders[0].getX() - classic.getX(), riders[0].getZ() - classic.getZ());
                    helper.assertTrue(Math.abs(chor - 3.0) <= 0.051,
                            "classic spider driver offset " + chor + " != orig 3.0±0.05");
                } else if (t == 30) {
                    riders[0].stopRiding();
                    playerRider[0] = helper.makeMockPlayer(GameType.SURVIVAL);
                    helper.assertTrue(playerRider[0].startRiding(classic, true), "player mount failed");
                } else if (t >= 45) {
                    double py = playerRider[0].getY() - classic.getY();
                    helper.assertTrue(py >= 2.10 && py <= 2.15,
                            "classic PLAYER seat height " + py + " != orig 2.125±bob (TF-029)");
                }
                // Modern spider: seat-frame coords via inverse transform.
                ModernSpiderGait sg = modern.getModernGait();
                double[] v = {riders[1].getX() - modern.getX(), riders[1].getY() - modern.getY(),
                        riders[1].getZ() - modern.getZ()};
                ModernSpiderGait.inverseBodyTransform(modern.getYRot(),
                        sg.bodyPitch(), sg.bodyRoll(), sg.bodyLift(), v);
                helper.assertTrue(Math.abs(v[1] - 2.0) <= 0.25,
                        "modern spider seat-frame height " + v[1] + " != 2.0 (composed)");
                helper.assertTrue(Math.abs(Math.hypot(v[0], v[2]) - 3.0) <= 0.2,
                        "modern spider seat-frame offset " + Math.hypot(v[0], v[2]));
                // Classic ant: unchanged 1.0 seat (driver 0.55±0.02, 1.25±0.05 behind).
                double ay = riders[2].getY() - antClassic.getY();
                helper.assertTrue(ay >= 0.52 && ay <= 0.58,
                        "classic ant driver seat height " + ay + " != orig 0.55±0.02");
                // Modern ant: raised +0.9 and composed.
                ModernSpiderGait ag = antModern.getModernGait();
                double[] av = {riders[3].getX() - antModern.getX(), riders[3].getY() - antModern.getY(),
                        riders[3].getZ() - antModern.getZ()};
                ModernSpiderGait.inverseBodyTransform(antModern.getYRot(),
                        ag.bodyPitch(), ag.bodyRoll(), ag.bodyLift(), av);
                helper.assertTrue(av[1] >= 1.42 - 0.25 && av[1] <= 1.48 + 0.25,
                        "modern ant seat-frame height " + av[1] + " != 1.45±bob (raised+composed)");
                if (t == 70) {
                    for (Mob r : riders) {
                        r.discard();
                    }
                    if (playerRider[0] != null) {
                        playerRider[0].stopRiding();
                    }
                    classic.discard();
                    modern.discard();
                    antClassic.discard();
                    antModern.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                classic.discard();
                modern.discard();
                antClassic.discard();
                antModern.discard();
                throw e;
            }
        });
    }

    /**
     * S5 pin, mechanism replaced in S6b: look-jitter must not dance the
     * legs. The rotation-latched trigger radius (which replaced the S5a
     * dead-band chase) absorbs yaw wobble — ZERO steps — while a genuine
     * 90° turn re-plants every leg onto the yaw-fresh rests immediately
     * (zero-lag rest frame + comfort invalidation). Server-observable: yaw is the gait's
     * input regardless of whether a rider's vehicle packets or a test
     * produced it, so this sits inside the recorded honest testing limit.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5_yaw_jitter_no_dance(GameTestHelper helper) {
        final SpiderRobot spider = spawnMode(helper, new BlockPos(24, 2, 24), OreSpawnConfig.SpiderMovement.MODERN);
        spider.setYRot(0.0f);
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }
        final int[] tick = {0};
        final int[] jitterSteps = {0};
        final boolean[] wasSwinging = new boolean[SpiderRigProfile.LEG_COUNT];
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 40) {
                    return; // settle from spawn
                }
                if (t <= 140) {
                    // Phase A: ±10° look-jitter (20° swings — rest
                    // displacement 3.14 sits in the LATCH-ONLY band
                    // (stationary 2.0 < 3.14 < forced 5.0), so this pin
                    // DISCRIMINATES the rotation latch; review: the old ±6°
                    // passed latch-deleted).
                    spider.setYRot(((t / 5) % 2 == 0) ? 10.0f : -10.0f);
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        boolean s = gait.isSwinging(leg);
                        if (s && !wasSwinging[leg]) {
                            ++jitterSteps[0];
                        }
                        wasSwinging[leg] = s;
                    }
                    if (t == 140) {
                        helper.assertTrue(jitterSteps[0] == 0,
                                "look-jitter danced the legs: " + jitterSteps[0] + " steps triggered");
                        spider.setYRot(90.0f); // Phase B: a genuine turn
                    }
                    return;
                }
                if (t == 300) {
                    // S6b: the rest frame is ZERO-LAG now — feet must have
                    // re-planted around the yaw-90 rests themselves (the
                    // S5a heading-chase assertions died with the chase).
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertFalse(gait.isSwinging(leg),
                                "leg " + leg + " still stepping long after the turn");
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not grounded after the turn");
                        double dx = gait.footX(leg)
                                - SpiderRigProfile.restFootX(leg, spider.getX(), 90.0f);
                        double dz = gait.footZ(leg)
                                - SpiderRigProfile.restFootZ(leg, spider.getZ(), 90.0f);
                        helper.assertTrue(dx * dx + dz * dz <= 2.5 * 2.5,
                                "leg " + leg + " never re-planted to the turned heading");
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
     * Mount/dismount mid-swing (reviewer target): mounting during active
     * swings and dismounting again leaves no leg stuck airborne — every
     * swing completes and the gait settles fully grounded.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5_mount_dismount_mid_swing(GameTestHelper helper) {
        final SpiderRobot spider = spawnMode(helper, new BlockPos(12, 2, 24), OreSpawnConfig.SpiderMovement.MODERN);
        final ModernSpiderGait gait;
        try {
            gait = spider.getModernGait();
            helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        } catch (RuntimeException e) {
            spider.discard();
            throw e;
        }
        final Player rider = helper.makeMockPlayer(GameType.SURVIVAL);
        final int[] tick = {0};
        final boolean[] sawSwingAtMount = {false};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t <= 55) {
                    // Drive so swings are active when the mount happens; the
                    // census latches over a WINDOW (t=50..55) rather than one
                    // tick — the pair/neighbor inhibitors admit occasional
                    // all-planted ticks, and a single-tick sample would flake
                    // on tuning changes (independent review).
                    if (t >= 25) {
                        spider.setDeltaMovement(0.30, spider.getDeltaMovement().y, 0.0);
                    }
                    if (t >= 50) {
                        for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                            if (gait.isSwinging(leg)) {
                                sawSwingAtMount[0] = true;
                            }
                        }
                    }
                    if (t == 55) {
                        helper.assertTrue(rider.startRiding(spider, true), "mid-swing mount failed");
                    }
                    return;
                }
                if (t == 70) {
                    rider.stopRiding();
                    return;
                }
                if (t == 120) {
                    helper.assertTrue(sawSwingAtMount[0],
                            "census broken: no swing was active at the mount tick");
                    for (int leg = 0; leg < SpiderRigProfile.LEG_COUNT; ++leg) {
                        helper.assertFalse(gait.isSwinging(leg),
                                "leg " + leg + " stuck mid-swing after mount/dismount");
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not settled after mount/dismount");
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
     * The RATIFIED part-stream gate predicate, both arms (the mixin call
     * site consumes exactly this; packet-level neutrality per the S4
     * election-test precedent — no clients exist in gametests, so the
     * decision function is the server-side observable): locally-mirrored
     * boneless profiles (modern spider) stop streaming; TheQueen streams
     * unchanged.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s5_part_stream_gate_predicate(GameTestHelper helper) {
        SpiderRobot spider = spawnMode(helper, new BlockPos(10, 2, 10), OreSpawnConfig.SpiderMovement.MODERN);
        TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(34, 2, 34));
        try {
            Object spiderObj = spider;
            Object queenObj = queen;
            helper.assertTrue(spiderObj instanceof IMultipartEntity<?> sp && !sp.mhlibShouldStreamParts(),
                    "modern spider must NOT stream parts (locally mirrored — ratified gate)");
            helper.assertTrue(queenObj instanceof IMultipartEntity<?> qp && qp.mhlibShouldStreamParts(),
                    "TheQueen must STILL stream parts (sync-to-model — gate not Queen-neutral)");
        } finally {
            spider.discard();
            queen.discard();
        }
        helper.succeed();
    }
}
