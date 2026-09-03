package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.gait.AntRigProfile;
import danger.orespawn.entity.gait.LegRig;
import danger.orespawn.entity.gait.ModernSpiderGait;
import danger.orespawn.entity.gait.SpiderRigProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * S6b — THE LEG FIX (owner exit criterion, verbatim: "legs never cross
 * the body midline, never stretch beyond the classic stance, and stepping
 * reads as insect movement — legs on each side staying on their side,
 * planting and lifting in a natural rhythm"). The suite-provable halves:
 * the SIDE INVARIANT (a grounded foot's body-local lateral sign always
 * matches its leg's side — guaranteed by comfort radius &lt; min lateral
 * rest offset), the COMFORT INVARIANT (no grounded foot ever outside the
 * comfort radius of the CURRENT rest — the anti-hyper-stretch cap), and
 * structural contralateral impossibility (footing that exists only across
 * the body is never planted on; the leg strands instead).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class S6LegFixTests {

    /** Body-local lateral coordinate of a world point (derivation: inverse of the classic hip/rest rotation; even legs are −X at any yaw). */
    private static double bodyLocalX(Mob robot, double worldX, double worldZ) {
        double yawRad = Math.toRadians(Mth.wrapDegrees((double) robot.getYRot()));
        double dx = worldX - robot.getX();
        double dz = worldZ - robot.getZ();
        return dx * Math.cos(yawRad) + dz * Math.sin(yawRad);
    }

    private static void assertSideAndComfort(GameTestHelper helper, Mob robot,
                                             ModernSpiderGait gait, LegRig rig, String phase) {
        for (int leg = 0; leg < rig.legCount(); ++leg) {
            if (!gait.isGrounded(leg)) {
                continue;
            }
            double lateral = bodyLocalX(robot, gait.footX(leg), gait.footZ(leg));
            boolean evenLeg = (leg & 1) == 0;
            helper.assertTrue(evenLeg ? lateral < -0.1 : lateral > 0.1,
                    phase + ": leg " + leg + " CROSSED the midline (body-local x " + lateral + ")");
            helper.assertTrue(gait.plantWithinComfort(robot, leg),
                    phase + ": leg " + leg + " grounded outside the valid plant region "
                            + "(comfort-or-corridor of the current rest frame)");
            // Independent envelope (review: the predicate is the gait's own
            // oracle) — a mis-sized comfort radius cannot hide behind it.
            double edx = gait.footX(leg) - rig.restFootX(leg, robot.getX(), robot.getYRot());
            double edz = gait.footZ(leg) - rig.restFootZ(leg, robot.getZ(), robot.getYRot());
            helper.assertTrue(edx * edx + edz * edz <= (rig.restReach(leg) + 2.0) * (rig.restReach(leg) + 2.0),
                    phase + ": leg " + leg + " outside the literal drift envelope");
        }
    }

    /**
     * Spider: sustained 3°/tick spin (rest sweep ~0.9 b/t — steppable),
     * then a 180° flick, then settle. The side and comfort invariants hold
     * on EVERY tick; after settling every leg is grounded on its own side.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s6b_spider_spin_flick_no_crossing(GameTestHelper helper) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        final SpiderRobot spider;
        try {
            // Master-override ruling 2026-09-04: MODERN needs the master on.
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            spider = helper.spawn(ModEntities.SPIDER_ROBOT.get(), new BlockPos(24, 2, 24));
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        spider.setYRot(0.0f);
        final ModernSpiderGait gait = spider.getModernGait();
        helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        final int[] tick = {0};
        final int[] streak = new int[8];
        final int[] bestStreak = new int[8];
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                // Assert FIRST, against the yaw the gait actually processed
                // last tick — rotating before asserting would test the gait
                // against a frame it has not seen yet (first-run red).
                if (t >= 45) {
                    assertSideAndComfort(helper, spider, gait, SpiderRigProfile.RIG,
                            t < 240 ? "spin" : "flick/settle");
                }
                if (t >= 40 && t < 160) {
                    spider.setYRot(spider.getYRot() + 3.0f); // sustained turn
                } else if (t >= 170 && t < 230) {
                    // Churn regime (review MAJOR: landings went stale above
                    // ~4.8°/t before mid-swing revalidation): 6°/t must
                    // still produce real plant streaks, not 1-tick touches.
                    spider.setYRot(spider.getYRot() + 6.0f);
                    for (int leg = 0; leg < 8; ++leg) {
                        if (gait.isGrounded(leg)) {
                            streak[leg] = Math.min(99, streak[leg] + 1);
                            bestStreak[leg] = Math.max(bestStreak[leg], streak[leg]);
                        } else {
                            streak[leg] = 0;
                        }
                    }
                } else if (t == 240) {
                    int legsWithStreaks = 0;
                    for (int leg = 0; leg < 8; ++leg) {
                        if (bestStreak[leg] >= 2) {
                            ++legsWithStreaks;
                        }
                    }
                    helper.assertTrue(legsWithStreaks >= 6,
                            "6°/t churn: only " + legsWithStreaks
                                    + " legs achieved 2+-tick plant streaks (perpetual churn)");
                    spider.setYRot(spider.getYRot() + 180.0f); // hard flick
                }
                if (t == 360) {
                    for (int leg = 0; leg < 8; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not settled after spin+flick");
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
     * Ant (the sitting's failing rig): same drill at its scale — sustained
     * 3°/tick spin (rest sweep ~0.52 b/t &lt; ant step speed 0.55), a 180°
     * flick, settle. Side + comfort invariants on every tick.
     */
    @GameTest(template = "empty_large", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s6b_ant_spin_flick_no_crossing(GameTestHelper helper) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        final AntRobot ant;
        try {
            // Master-override ruling 2026-09-04: MODERN needs the master on.
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            ant = helper.spawnWithNoFreeWill(ModEntities.ANT_ROBOT.get(), new BlockPos(24, 2, 24));
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        ant.setOwned(); // inert customServerAiStep (S5b review hardening)
        ant.setYRot(0.0f);
        final ModernSpiderGait gait = ant.getModernGait();
        helper.assertTrue(gait != null, "modern ant must carry the gait controller");
        final int[] tick = {0};
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                // Assert FIRST (see the spider test note).
                if (t >= 45) {
                    assertSideAndComfort(helper, ant, gait, AntRigProfile.RIG,
                            t < 160 ? "spin" : "flick/settle");
                }
                if (t >= 40 && t < 160) {
                    ant.setYRot(ant.getYRot() + 3.0f);
                } else if (t == 160) {
                    ant.setYRot(ant.getYRot() + 180.0f);
                }
                if (t == 300) {
                    for (int leg = 0; leg < 6; ++leg) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "leg " + leg + " not settled after spin+flick");
                    }
                    ant.discard();
                    helper.succeed();
                }
            } catch (RuntimeException e) {
                ant.discard();
                throw e;
            }
        });
    }

    /**
     * Structural contralateral impossibility (reference: his candidate
     * generator cannot emit a cross-body point; ours is pinned here): a
     * spider on the edge of an 18-high half-platform whose off-side drop
     * is BELOW the scan window. The off-side legs face footing ONLY at or
     * beyond the platform's edge — legal solely through their own-side
     * comfort disc or near-hip contraction corridor (the classic bridge
     * grip; the first cut wrongly demanded they STRAND, but a body on
     * solid ground can always contract-grip beside its own hips). The
     * pinned invariant is the exit criterion itself: across settling,
     * stepping and edge-gripping, NO grounded foot ever sits across the
     * body midline, and every leg resolves to grounded-or-stranded (no
     * perpetual swinging). Runs in the tall cage (the 16-high cages'
     * barrier ceiling would sit inside the scan window).
     */
    @GameTest(template = "empty_tall", timeoutTicks = 400, batch = "spiderGaitIsolation")
    public void s6b_contralateral_footing_strands(GameTestHelper helper) {
        for (int x = 24; x <= 46; ++x) {
            for (int z = 4; z <= 44; ++z) {
                for (int y = 0; y <= 17; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.STONE);
                }
            }
        }
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        final SpiderRobot spider;
        try {
            // Master-override ruling 2026-09-04: MODERN needs the master on.
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            spider = helper.spawn(ModEntities.SPIDER_ROBOT.get(), new BlockPos(28, 19, 24));
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
        spider.setYRot(0.0f);
        final ModernSpiderGait gait = spider.getModernGait();
        helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        final int[] tick = {0};
        final boolean[] resolved = new boolean[8];
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t >= 20) {
                    // The exit criterion, structurally: no grounded foot on
                    // the wrong side — EVER, including mid-recovery.
                    for (int leg = 0; leg < 8; ++leg) {
                        if (!gait.isGrounded(leg)) {
                            continue;
                        }
                        double lateral = bodyLocalX(spider, gait.footX(leg), gait.footZ(leg));
                        helper.assertTrue((leg & 1) == 0 ? lateral < -0.1 : lateral > 0.1,
                                "leg " + leg + " planted across the body (lateral " + lateral + ")");
                    }
                }
                if (t >= 100) {
                    for (int leg = 0; leg < 8; ++leg) {
                        if (gait.isGrounded(leg) || gait.isStranded(leg)) {
                            resolved[leg] = true;
                        }
                    }
                }
                if (t == 120) {
                    for (int leg = 0; leg < 8; ++leg) {
                        helper.assertTrue(resolved[leg],
                                "leg " + leg + " never resolved to grounded-or-stranded near the edge");
                    }
                    for (int leg : new int[]{1, 3, 5, 7}) {
                        helper.assertTrue(gait.isGrounded(leg),
                                "platform-side leg " + leg + " failed to stand");
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
}
