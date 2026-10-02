package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Urchin;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-178, a persistent Urchin by day, and ENT-S-179, a tamed Girlfriend offered body armour.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GirlfriendAndUrchinTests {

    /**
     * ENT-S-178: an Urchin that must persist is never discarded by day (orig Urchin.java:97-99 returns first). By day an
     * Urchin discards itself one tick in 400; twenty persistent ones by day for 200 ticks all stay (without the check all
     * twenty would stay in only (399/400)^4000, about 4.5e-5, of runs). A batch of its own: the test sets the level's
     * time of day, and puts it back on every exit.
     */
    @GameTest(template = "empty_large", timeoutTicks = 260, batch = "urchinDaylight")
    public static void ent178_a_persistent_urchin_stays_through_the_day(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long dayTime = level.getDayTime();
        long morning = dayTime - Math.floorMod(dayTime, 24000L) + 1000L;
        level.setDayTime(morning);
        List<Urchin> urchins = new ArrayList<>();
        EntityLogicTestsA.onTestExit(helper, () -> {
            urchins.forEach(Entity::discard);
            level.setDayTime(dayTime + (level.getDayTime() - morning));
        });
        for (int i = 0; i < 20; i++) {
            Urchin urchin = helper.spawnWithNoFreeWill(ModEntities.URCHIN.get(), new BlockPos(6 + (i % 10) * 4, 0, i < 10 ? 16 : 32));
            urchin.setNoAi(true);
            urchin.setPersistenceRequired();
            urchins.add(urchin);
        }
        helper.runAfterDelay(200, () -> {
            helper.assertTrue(Math.floorMod(level.getDayTime(), 24000L) < 12000L,
                    "the time of day left daytime: " + Math.floorMod(level.getDayTime(), 24000L));
            long gone = urchins.stream().filter(Entity::isRemoved).count();
            helper.assertTrue(gone == 0, gone + " of 20 persistent Urchins discarded themselves by day");
            helper.succeed();
        });
    }

    /**
     * ENT-S-179: a tamed Girlfriend refuses an item whose slot is the body's: her owner offering horse armour or wolf
     * armour (vanilla's and the mod's) keeps it, her body slot stays empty, and the click ends there. The click is played
     * in the game's own order (each hand in turn, the creature first, then the held item, until one takes the click), so
     * a refusal that passed the click on would reach the empty off hand and her empty-hand branch, which hands her sword
     * to the owner. A Crystal Pink helmet still goes on.
     */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ent179_a_tamed_girlfriend_refuses_body_armour(GameTestHelper helper) {
        ServerPlayer owner = survivalPlayer(helper);
        Girlfriend girlfriend = helper.spawnWithNoFreeWill(ModEntities.GIRLFRIEND.get(), new BlockPos(1, 2, 1));
        EntityLogicTestsA.onTestExit(helper, girlfriend::discard);
        girlfriend.tame(owner);
        girlfriend.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        boolean sitting = girlfriend.isOrderedToSit();
        Vec3 beside = helper.absoluteVec(new Vec3(2.5, 2.0, 1.5));
        owner.teleportTo(helper.getLevel(), beside.x, beside.y, beside.z, 0.0f, 0.0f);
        helper.assertTrue(girlfriend.isTame() && girlfriend.isOwnedBy(owner) && girlfriend.distanceToSqr(owner) < 16.0,
                "the Girlfriend is not tamed by the player within reach, so the check would prove nothing");
        for (Item body : List.of(Items.IRON_HORSE_ARMOR, Items.WOLF_ARMOR, ModItems.RUBY_HORSE_ARMOR.get(),
                ModItems.RUBY_WOLF_ARMOR.get())) {
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(body));
            click(owner, girlfriend);
            helper.assertTrue(girlfriend.getItemBySlot(EquipmentSlot.BODY).isEmpty(),
                    "offering " + body + ": her body slot took " + girlfriend.getItemBySlot(EquipmentSlot.BODY));
            helper.assertTrue(owner.getMainHandItem().is(body) && owner.getMainHandItem().getCount() == 1,
                    "offering " + body + ": the owner's hand no longer holds it: " + owner.getMainHandItem());
            helper.assertTrue(girlfriend.getMainHandItem().is(Items.IRON_SWORD) && owner.getOffhandItem().isEmpty(),
                    "offering " + body + ": the click went on to the off hand: her hand " + girlfriend.getMainHandItem()
                            + ", the owner's off hand " + owner.getOffhandItem());
            helper.assertTrue(girlfriend.isOrderedToSit() == sitting, "offering " + body + " sat her down or stood her up");
        }
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.PINK_HELMET.get()));
        click(owner, girlfriend);
        helper.assertTrue(girlfriend.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PINK_HELMET.get())
                        && owner.getMainHandItem().isEmpty(),
                "the Crystal Pink helmet did not go on: head " + girlfriend.getItemBySlot(EquipmentSlot.HEAD)
                        + ", the owner's hand " + owner.getMainHandItem());
        helper.succeed();
    }

    /**
     * A use click on a creature as the game plays it (Minecraft.startUseItem, each step as the server runs it): each
     * hand in turn, the creature first, then the held item's own use, until one of them takes the click.
     */
    private static void click(ServerPlayer player, Entity target) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.interactOn(target, hand).consumesAction()) {
                return;
            }
            ItemStack held = player.getItemInHand(hand);
            if (!held.isEmpty() && player.gameMode.useItem(player, player.level(), held, hand).consumesAction()) {
                return;
            }
        }
    }

    /** A survival player on the server's player list (a tamed mob finds its owner among the level's players), taken off it when the test ends. */
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "test-girlfriend-owner"), false);
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        EntityLogicTestsA.onTestExit(helper, () -> server.getPlayerList().remove(player));
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }
}
