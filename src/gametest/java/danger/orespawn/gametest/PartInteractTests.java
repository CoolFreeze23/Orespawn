package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.TheQueen;
import danger.orespawn.entity.gait.ModernSpiderGait;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * S6a — sitting finding F-1 (owner-ratified deliberate delta): part
 * entities forward interactions to the parent, so legs are clickable
 * mount surfaces. Player-path law: the tests drive the same
 * {@code Entity.interact} entry the vanilla click packet dispatches to
 * (packet → getEntityOrPart → interact), through a real leg part.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PartInteractTests {

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
     * The F-1 fix, player-path: an empty-hand click ON A LEG PART mounts
     * the spider (part → parent interact chain → mobInteract →
     * startRiding). Classic body-click mounting is asserted alongside as
     * the parity control.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s6_mount_through_leg_part(GameTestHelper helper) {
        SpiderRobot modern = spawnMode(helper, new BlockPos(12, 2, 12), OreSpawnConfig.SpiderMovement.MODERN);
        SpiderRobot classic = spawnMode(helper, new BlockPos(36, 2, 36), OreSpawnConfig.SpiderMovement.CLASSIC);
        try {
            helper.assertTrue(modern.getParts() != null && modern.getParts().length == 8,
                    "part census precondition failed");
            Player rider = helper.makeMockPlayer(GameType.SURVIVAL);
            // Within mobInteract's classic 4-block mount range of the CENTER.
            rider.setPos(modern.getX() + 1.5, modern.getY(), modern.getZ());
            PartEntity<?> leg = (PartEntity<?>) modern.getParts()[3];
            InteractionResult result = leg.interact(rider, InteractionHand.MAIN_HAND);
            helper.assertTrue(result.consumesAction(),
                    "leg-part click was not consumed (got " + result + ")");
            helper.assertTrue(rider.getVehicle() == modern,
                    "clicking a leg part must mount the spider (F-1 forwarding)");
            rider.stopRiding();

            // Parity control: the classic body click still mounts (no parts
            // exist there; the fix must not touch the classic path).
            Player classicRider = helper.makeMockPlayer(GameType.SURVIVAL);
            classicRider.setPos(classic.getX() + 1.5, classic.getY(), classic.getZ());
            classic.interact(classicRider, InteractionHand.MAIN_HAND);
            helper.assertTrue(classicRider.getVehicle() == classic,
                    "classic body-click mounting regressed");
            classicRider.stopRiding();
        } finally {
            modern.discard();
            classic.discard();
        }
        helper.succeed();
    }

    /**
     * Neutrality pin: a Queen PART click routes to exactly what her BODY
     * click always did (she has no mobInteract — the vanilla default),
     * and never mounts her.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s6_queen_part_interact_neutrality(GameTestHelper helper) {
        TheQueen queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(24, 2, 24));
        try {
            helper.assertTrue(queen.getParts() != null && queen.getParts().length > 0,
                    "queen part census precondition failed");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setPos(queen.getX() + 1.0, queen.getY(), queen.getZ());
            InteractionResult viaBody = queen.interact(player, InteractionHand.MAIN_HAND);
            InteractionResult viaPart = ((PartEntity<?>) queen.getParts()[0])
                    .interact(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(viaPart == viaBody,
                    "queen part interact diverged from body interact: " + viaPart + " vs " + viaBody);
            helper.assertTrue(player.getVehicle() == null, "queen must never be mountable");
        } finally {
            queen.discard();
        }
        helper.succeed();
    }

    /**
     * S6a — sitting OBS-2 pin: every live leg part sits EXACTLY on the
     * solver's lower-segment chord anchor (recomputed from live state),
     * so box-vs-solver drift can never go silent. The box's offset from
     * the VISUAL leg is the accepted Q3 one-box design cost; this pins
     * the part to the SOLVER, the authority the box is defined against.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "spiderGaitIsolation")
    public void s6_part_anchor_chord_pin(GameTestHelper helper) {
        final SpiderRobot spider = spawnMode(helper, new BlockPos(24, 2, 24), OreSpawnConfig.SpiderMovement.MODERN);
        final ModernSpiderGait gait = spider.getModernGait();
        helper.assertTrue(gait != null, "modern spider must carry the gait controller");
        final int[] tick = {0};
        final double[] anchor = new double[3];
        helper.onEachTick(() -> {
            try {
                int t = ++tick[0];
                if (t < 30) {
                    return; // settle
                }
                long time = helper.getLevel().getGameTime();
                for (int leg = 0; leg < 8; ++leg) {
                    gait.currentLegPartAnchor(spider, leg, time, anchor);
                    PartEntity<?> part = (PartEntity<?>) spider.getParts()[leg];
                    double d = Math.sqrt(
                            (part.getX() - anchor[0]) * (part.getX() - anchor[0])
                                    + (part.getY() - anchor[1]) * (part.getY() - anchor[1])
                                    + (part.getZ() - anchor[2]) * (part.getZ() - anchor[2]));
                    helper.assertTrue(d <= 1.0E-6,
                            "leg " + leg + " part drifted " + d + " off the solver chord anchor");
                }
                spider.discard();
                helper.succeed();
            } catch (RuntimeException e) {
                spider.discard();
                throw e;
            }
        });
    }
}
