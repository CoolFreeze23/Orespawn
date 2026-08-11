package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Acid;
import danger.orespawn.entity.AttackSquid;
import danger.orespawn.entity.Basilisk;
import danger.orespawn.entity.BetterFireball;
import danger.orespawn.entity.Dragon;
import danger.orespawn.entity.EasterBunny;
import danger.orespawn.entity.Elevator;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityCaterKiller;
import danger.orespawn.entity.EntityKyuubi;
import danger.orespawn.entity.EntityRedAnt;
import danger.orespawn.entity.EntitySpitBug;
import danger.orespawn.entity.EntityStinkBug;
import danger.orespawn.entity.EntityTermite;
import danger.orespawn.entity.EntityTriffid;
import danger.orespawn.entity.EntityTrooperBug;
import danger.orespawn.entity.EntityUnstableAnt;
import danger.orespawn.entity.EntityVortex;
import danger.orespawn.entity.EntityWormLarge;
import danger.orespawn.entity.EntityWormSmall;
import danger.orespawn.entity.GiantRobot;
import danger.orespawn.entity.InkSack;
import danger.orespawn.entity.LaserBall;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.Ostrich;
import danger.orespawn.entity.Peacock;
import danger.orespawn.entity.QueenHead;
import danger.orespawn.entity.ThePrince;
import danger.orespawn.entity.TheQueen;
import danger.orespawn.entity.VelocityRaptor;
import danger.orespawn.entity.WaterBall;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Entity-logic GameTests, batch B (items i047-i123 of the entity_logic
 * AUTOMATABLE set, 21 items). Batch K base 500; the only far-region build in
 * this file is {@link #i123_maze_basilisk_persistence} at uniqueK = 520
 * (offset 520*512 = +266240 x, +50000 z from the test origin).
 *
 * <p>Pattern notes:
 * <ul>
 *   <li>Deterministic AI driving uses same-file subclasses that expose the
 *       protected {@code customServerAiStep()} (called {@code ai()} below).
 *       Driven entities are deliberately NOT added to the level, so the level
 *       never double-ticks them and no cleanup race exists; their level-side
 *       effects (spawned projectiles/items, damage to added victims) are
 *       asserted and cleaned within one server tick.</li>
 *   <li>Statistical loops state their false-failure bound in a comment;
 *       every bound is &lt; 1e-9.</li>
 *   <li>Tests that add a real ServerPlayer to the level run in the private
 *       batch {@code "orespawnb_players"} so the player's presence cannot
 *       interact with other batches' despawn logic.</li>
 * </ul>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EntityLogicTestsB {

    // ================================================================
    // helpers
    // ================================================================

    private static void check(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    /**
     * Real in-level ServerPlayer (survival) with chat capture — same
     * construction as the vanilla (deprecated) GameTestHelper
     * .makeMockServerPlayerInLevel(), plus a displayClientMessage override so
     * the Termite gate messages (EntityTermite.java:68,83) are observable.
     */
    private static final class TestServerPlayer extends ServerPlayer {
        final List<String> messages = new ArrayList<>();

        TestServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation info) {
            super(server, level, profile, info);
        }

        @Override
        public void displayClientMessage(Component component, boolean actionBar) {
            this.messages.add(component.getString());
        }
    }

    private static TestServerPlayer addServerPlayer(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "orespawn-test-b"), false);
        TestServerPlayer player = new TestServerPlayer(
                server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private static void removeServerPlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    /** Sum of stack counts over item entities of {@code item} in {@code box}. */
    private static int countItems(ServerLevel level, AABB box, Item item) {
        int total = 0;
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, box, ie -> ie.getItem().is(item))) {
            total += e.getItem().getCount();
        }
        return total;
    }

    private static void discardItems(ServerLevel level, AABB box) {
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, box)) {
            e.discard();
        }
    }

    /** Lay a small stone floor patch (relative coords) so entities have footing. */
    private static void floor(GameTestHelper helper, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    // AI-exposing subclasses (never added to the level unless noted).
    private static final class TestPeacock extends Peacock {
        TestPeacock(Level level) { super(ModEntities.PEACOCK.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestBunny extends EasterBunny {
        TestBunny(Level level) { super(ModEntities.EASTER_BUNNY.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestWormLarge extends EntityWormLarge {
        TestWormLarge(Level level) { super(ModEntities.ENTITY_WORM_LARGE.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestWormSmall extends EntityWormSmall {
        TestWormSmall(Level level) { super(ModEntities.ENTITY_WORM_SMALL.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestBrutalfly extends EntityBrutalfly {
        TestBrutalfly(Level level) { super(ModEntities.ENTITY_BRUTALFLY.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestRobot extends GiantRobot {
        TestRobot(Level level) { super(ModEntities.GIANT_ROBOT.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestPrince extends ThePrince {
        TestPrince(Level level) { super(ModEntities.THE_PRINCE.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestSquid extends AttackSquid {
        TestSquid(Level level) { super(ModEntities.ATTACK_SQUID.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestVortex extends EntityVortex {
        TestVortex(Level level) { super(ModEntities.ENTITY_VORTEX.get(), level); }
        void ai() { this.customServerAiStep(); }
    }

    private static final class TestFireball extends BetterFireball {
        TestFireball(Level level) { super(ModEntities.BETTER_FIREBALL.get(), level); }
        boolean canHit(Entity e) { return this.canHitEntity(e); }
    }

    // ================================================================
    // i047-ent-s-030 — StinkBug death gas
    // ================================================================

    /**
     * Checklist item i047-ent-s-030 (ENT-S-030). Killing a StinkBug applies
     * CONFUSION (nausea) 300t/amp 0 — never HUNGER — to every LivingEntity in
     * the box x±8 / y −5..+10 / z±8 around it.
     * Values: EntityStinkBug.java:82-99, orig StinkBug.java:95-103
     * (field_76431_k = confusion, 300 ticks).
     */
    @GameTest(template = "empty_large")
    public void i047_stinkbug_death_nausea_aoe(GameTestHelper helper) {
        floor(helper, 18, 18, 40, 30);
        EntityStinkBug bug = helper.spawnWithNoFreeWill(ModEntities.ENTITY_STINK_BUG.get(), new BlockPos(24, 1, 24));
        Cow near1 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(27, 1, 24));
        Cow near2 = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(21, 1, 24));
        // "incl. mobs well above it": +8 blocks is inside the y −5..+10 band.
        Cow above = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(24, 9, 24));
        // 12 blocks out on x is outside the ±8 box — negative control.
        Cow far = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(36, 1, 24));

        bug.hurt(helper.getLevel().damageSources().generic(), 99.0f);
        check(bug.getHealth() <= 0.0f || bug.isRemoved(), "StinkBug should die from a 99-damage hit (5 HP)");

        for (Cow cow : List.of(near1, near2, above)) {
            check(cow.hasEffect(MobEffects.CONFUSION), "in-range entity must get CONFUSION (orig StinkBug.java:95-103)");
            check(cow.getEffect(MobEffects.CONFUSION).getDuration() == 300,
                    "CONFUSION duration must be 300t, was " + cow.getEffect(MobEffects.CONFUSION).getDuration());
            check(cow.getEffect(MobEffects.CONFUSION).getAmplifier() == 0, "CONFUSION amplifier must be 0");
            check(!cow.hasEffect(MobEffects.HUNGER), "ENT-S-030: nausea, NOT hunger");
        }
        check(!far.hasEffect(MobEffects.CONFUSION), "entity 12 blocks out (outside x±8) must not be gassed");

        for (Cow cow : List.of(near1, near2, above, far)) cow.discard();
        discardItems(helper.getLevel(), new AABB(bug.blockPosition()).inflate(8));
        helper.succeed();
    }

    // ================================================================
    // i049-ent-s-065 — Velocity Raptor: no mount, sit toggle
    // ================================================================

    /**
     * Checklist item i049-ent-s-065 (ENT-S-065; FIX_LOG.md:93). Right-clicking
     * a tamed Velocity Raptor empty-handed does NOT mount (the 1.7.10 raptor
     * has no mount interaction — orig VelocityRaptor.java:223-294); the
     * shift-click sit toggle still works (VelocityRaptor.java:170-176).
     *
     * <p>Infra note: the sit toggle is OWNERSHIP-gated in both sources (orig
     * VelocityRaptor.java:292 {@code func_152114_e}; port VelocityRaptor.java:172
     * {@code isOwnedBy}), and the owner resolves through the LEVEL's player list
     * ({@code TamableAnimal.getOwner -> level.getPlayerByUUID}). A detached mock
     * player is never found there, so the toggle silently fell through to PASS
     * (the pre-fix red) — the clicker must be a real in-level ServerPlayer.</p>
     */
    @GameTest(template = "empty_large", batch = "orespawnb_players")
    public void i049_velocity_raptor_no_mount_sit_toggle(GameTestHelper helper) {
        floor(helper, 20, 20, 28, 28);
        VelocityRaptor raptor = helper.spawnWithNoFreeWill(ModEntities.VELOCITY_RAPTOR.get(), new BlockPos(24, 1, 24));
        TestServerPlayer player = null;
        try {
            player = addServerPlayer(helper);
            Vec3 at = helper.absoluteVec(new Vec3(26.0, 1.0, 24.0)); // distSq 4 < the 16.0 gate
            player.teleportTo(helper.getLevel(), at.x, at.y, at.z, 0.0f, 0.0f);

            raptor.setTame(true, true);
            raptor.setOwnerUUID(player.getUUID());
            check(!raptor.isOrderedToSit(), "fresh raptor is not sitting");

            // Empty hand, no shift: must not mount, must not toggle sit.
            player.setShiftKeyDown(false);
            raptor.mobInteract(player, InteractionHand.MAIN_HAND);
            check(player.getVehicle() == null, "ENT-S-065: empty-hand click must NOT mount the raptor");
            check(raptor.getPassengers().isEmpty(), "raptor must carry no passenger");
            check(!raptor.isOrderedToSit(), "plain click must not toggle sit");

            // Shift-click: sit toggle on, then off (VelocityRaptor.java:172-176).
            player.setShiftKeyDown(true);
            InteractionResult r1 = raptor.mobInteract(player, InteractionHand.MAIN_HAND);
            check(r1 == InteractionResult.SUCCESS, "shift-click sit toggle should return SUCCESS");
            check(raptor.isOrderedToSit(), "first shift-click must order the raptor to sit");
            raptor.mobInteract(player, InteractionHand.MAIN_HAND);
            check(!raptor.isOrderedToSit(), "second shift-click must stand the raptor back up");
            check(player.getVehicle() == null, "shift-clicks must not mount either");
        } finally {
            removeServerPlayer(helper, player);
        }
        helper.succeed();
    }

    // ================================================================
    // i050-ent-s-069 — Vortex: no skyward launch, drag pull works
    // ================================================================

    /**
     * Checklist item i050-ent-s-069 (ENT-S-069; FIX_LOG.md:94;
     * phase_c_reports/C4_entities_S_Z.md:31). The old port invented a
     * {@code skywardLaunch} (+4.0 up impulse) on Vortex melee; the fix removed
     * it — melee is a plain doHurtTarget (orig Vortex.java:195-197) and the
     * only displacement effect is the drag pull
     * push(cos·(10−d)·0.1, (10−d)·0.05·[×2 for players], sin·(10−d)·0.1)
     * (EntityVortex.java:204-212).
     *
     * <p>(a) Melee on a knockback-resistant Iron Golem leaves its vertical
     * velocity unchanged (vanilla knockback is fully resisted; a +4.0 launch
     * impulse would show regardless of resistance). (b) One driven AI step with
     * the golem exactly 5 blocks east applies the documented pull vector
     * (−0.5, +0.25, 0) toward the Vortex.
     */
    @GameTest(template = "empty_large")
    public void i050_vortex_no_launch_drag_pull(GameTestHelper helper) {
        floor(helper, 16, 16, 34, 34);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(29, 1, 24));
        TestVortex vortex = new TestVortex(helper.getLevel());
        Vec3 vpos = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
        vortex.moveTo(vpos.x, vpos.y, vpos.z, 0.0f, 0.0f);

        // (a) melee: 10 swings, no vertical impulse (ENT-S-069's launch was +4.0 up).
        for (int i = 0; i < 10; i++) {
            golem.setHealth(golem.getMaxHealth());
            golem.invulnerableTime = 0;
            golem.setDeltaMovement(0, 0, 0);
            vortex.doHurtTarget(golem);
            double dy = golem.getDeltaMovement().y;
            check(Math.abs(dy) < 0.05,
                    "Vortex melee must not launch the victim (KB-resistant golem dy should stay ~0, was " + dy + ")");
        }

        // (b) drag pull: golem exactly 5.0 blocks east, same Y.
        Vec3 gpos = new Vec3(vpos.x + 5.0, vpos.y, vpos.z);
        golem.moveTo(gpos.x, gpos.y, gpos.z, 0.0f, 0.0f);
        golem.setHealth(golem.getMaxHealth());
        golem.setDeltaMovement(0, 0, 0);
        vortex.ai(); // pull branch: distSq 25 < 81, windedCooldown 0 (EntityVortex.java:204-212)
        Vec3 v = golem.getDeltaMovement();
        // expected: pullStrength = (10-5)*0.1 = 0.5 toward the vortex (−x);
        // vertical = (10-5)*0.05 = 0.25 (non-player multiplier 1.0).
        check(v.x < -0.45 && v.x > -0.55, "drag pull x should be ~-0.5 toward the Vortex, was " + v.x);
        check(v.y > 0.20 && v.y < 0.30, "drag pull y should be ~+0.25, was " + v.y);
        check(Math.abs(v.z) < 0.05, "drag pull z should be ~0, was " + v.z);

        golem.discard();
        vortex.discard();
        helper.succeed();
    }

    // ================================================================
    // i051-ent-s-085-d4 — WormLarge gear theft
    // ================================================================

    /**
     * Checklist item i051-ent-s-085-d4 (ENT-S-085, D4). WormLarge attack roll
     * is 1-in-10/tick within 3 blocks of the nearest non-creative player; on
     * each attack an independent 1-in-4 steals the helmet (chestplate when
     * bare-headed) and an independent 1-in-4 steals the held item; stolen gear
     * scatters as item entities; nothing is stolen with playNicely=true.
     * Values: EntityWormLarge.java:176-223, orig WormLarge.java:199-238.
     *
     * <p>Statistics: held-item thefts per driven AI step have p = 1/10 · 1/4
     * = 1/40. With n = 3000 samples (mainhand re-equipped each step) the count
     * is Poisson-like, mean 75; the accepted band [25, 160] has false-failure
     * probability &lt; 3e-15 (Chernoff: P(X≤25) ≤ e^{−75}(e·75/25)^25 ≈ 1e-15,
     * P(X≥160) ≤ e^{−75}(e·75/160)^160 ≈ 2e-16) and rejects both
     * steal-every-hit (mean 300) and a wrongly nested 1/16 rate (mean ~19).
     */
    @GameTest(template = "empty_large", batch = "orespawnb_players")
    public void i051_worm_large_gear_theft(GameTestHelper helper) {
        floor(helper, 18, 18, 30, 30);
        boolean playNicelyBefore = OreSpawnConfig.PLAY_NICELY.get();
        TestServerPlayer player = null;
        TestWormLarge worm = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            player = addServerPlayer(helper);
            Vec3 wpos = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
            player.moveTo(wpos.x + 1.5, wpos.y, wpos.z, 0.0f, 0.0f);
            player.setInvulnerable(true); // the 1-in-10 attack lands doHurtTarget (18 ATK) alongside the rolls

            worm = new TestWormLarge(helper.getLevel());
            worm.moveTo(wpos.x, wpos.y, wpos.z, 0.0f, 0.0f);
            // NOT added to the level: aiStep never runs, so the 20+20 worm
            // segment chain (EntityWormLarge.java:136-150) never spawns and no
            // WormMedium can block the theft branch (:171-174).

            // Phase 1 — held-item theft rate (armor slots empty).
            int heldThefts = 0;
            for (int i = 0; i < 3000; i++) {
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
                worm.ai();
                if (player.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) heldThefts++;
            }
            check(heldThefts >= 25 && heldThefts <= 160,
                    "held-item thefts out of statistical band [25,160] for p=1/40 over 3000: " + heldThefts);
            AABB box = new AABB(BlockPos.containing(wpos)).inflate(12, 8, 12);
            check(countItems(helper.getLevel(), box, Items.DIAMOND) >= 1,
                    "stolen held items must scatter as item entities (orig WormLarge.java:231-238)");
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

            // Phase 2 — helmet first, chestplate only when bare-headed.
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            boolean helmetGone = false;
            boolean chestGone = false;
            // P(armor steal per step) = 1/40; P(fewer than 2 in 4000) ≈ 101·e^{-100} < 1e-40.
            for (int i = 0; i < 4000 && !chestGone; i++) {
                worm.ai();
                if (!helmetGone && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                    helmetGone = true;
                    check(!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),
                            "chestplate must survive while the helmet is worn (helmet steals first, orig :210-230)");
                }
                if (helmetGone && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) chestGone = true;
            }
            check(helmetGone, "helmet was never stolen in 4000 driven ticks (p=1/40/tick)");
            check(chestGone, "chestplate was never stolen after the helmet was gone");
            check(countItems(helper.getLevel(), box, Items.IRON_HELMET) >= 1, "helmet must scatter as an item entity");
            check(countItems(helper.getLevel(), box, Items.IRON_CHESTPLATE) >= 1, "chestplate must scatter as an item entity");

            // Phase 3 — playNicely=true: the whole hunt/steal branch is gated off
            // (EntityWormLarge.java:170, orig WormLarge.java:192-198).
            OreSpawnConfig.PLAY_NICELY.set(true);
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
            for (int i = 0; i < 600; i++) worm.ai();
            check(!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "no helmet theft with playNicely=true");
            check(!player.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty(), "no held-item theft with playNicely=true");

            discardItems(helper.getLevel(), box);
            helper.succeed();
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(playNicelyBefore);
            if (worm != null) worm.discard();
            removeServerPlayer(helper, player);
        }
    }

    // ================================================================
    // i052-ent-s-078-d4 — WormSmall boots theft + surfacing pop
    // ================================================================

    /**
     * Checklist item i052-ent-s-078-d4 (ENT-S-078, D4). WormSmall: on each
     * server AI step with the nearest non-creative player within 1.5 blocks,
     * a 1-in-15 attack roll carries an inner 1-in-6 boots steal
     * (EntityWormSmall.java:168-199, orig WormSmall.java:179-195). A worm that
     * surfaces through anything but grass block/dirt/stone pops (discard,
     * EntityWormSmall.java:150-160, orig :108-110), and SHORT_GRASS counts as
     * air for surfacing (isAirOrTallGrass, EntityWormSmall.java:145-148, orig
     * :104-106). The item's "at night" is the natural-spawn context (night-only
     * checkSpawnRules, orig :214-216) — the theft mechanism itself reads no
     * clock, so this test does not touch global time.
     *
     * <p>Statistics: boots thefts per driven step p = 1/15 · 1/6 = 1/90;
     * n = 6000 gives mean 66.7, and the accepted band [20, 133] has
     * false-failure &lt; 2e-10 (Chernoff both tails).
     */
    @GameTest(template = "empty_large", batch = "orespawnb_players")
    public void i052_worm_small_boots_theft_and_pop(GameTestHelper helper) {
        floor(helper, 18, 18, 30, 30);
        boolean playNicelyBefore = OreSpawnConfig.PLAY_NICELY.get();
        TestServerPlayer player = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            player = addServerPlayer(helper);
            Vec3 wpos = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
            player.moveTo(wpos.x + 1.0, wpos.y, wpos.z, 0.0f, 0.0f);
            player.setInvulnerable(true);

            TestWormSmall worm = new TestWormSmall(helper.getLevel());
            worm.moveTo(wpos.x, wpos.y, wpos.z, 0.0f, 0.0f);

            int thefts = 0;
            for (int i = 0; i < 6000; i++) {
                player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                worm.ai();
                if (player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) thefts++;
            }
            check(thefts >= 20 && thefts <= 133,
                    "boots thefts out of statistical band [20,133] for p=1/90 over 6000: " + thefts);
            AABB box = new AABB(BlockPos.containing(wpos)).inflate(12, 8, 12);
            check(countItems(helper.getLevel(), box, Items.IRON_BOOTS) >= 1,
                    "stolen boots must scatter as item entities (orig WormSmall.java:188-195)");
            worm.discard();
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            discardItems(helper.getLevel(), box);

            // ---- surfacing pop (player within 8 selects the upcount branch,
            // which probes the block at y+0.25 — EntityWormSmall.java:105-112).
            // (a) surfacing through SAND -> pop (not grass/dirt/stone).
            helper.setBlock(new BlockPos(26, 1, 26), Blocks.SAND);
            TestWormSmall popWorm = new TestWormSmall(helper.getLevel());
            Vec3 sandAt = helper.absoluteVec(new Vec3(26.5, 1.0, 26.5));
            popWorm.moveTo(sandAt.x, sandAt.y, sandAt.z, 0.0f, 0.0f);
            player.moveTo(sandAt.x + 2.0, sandAt.y, sandAt.z, 0.0f, 0.0f);
            popWorm.aiStep();
            check(popWorm.isRemoved(), "worm surfacing through SAND must pop (orig WormSmall.java:108-110)");

            // (b) surfacing through STONE -> survives (allowed surface block).
            helper.setBlock(new BlockPos(28, 1, 26), Blocks.STONE);
            TestWormSmall stoneWorm = new TestWormSmall(helper.getLevel());
            Vec3 stoneAt = helper.absoluteVec(new Vec3(28.5, 1.0, 26.5));
            stoneWorm.moveTo(stoneAt.x, stoneAt.y, stoneAt.z, 0.0f, 0.0f);
            player.moveTo(stoneAt.x + 2.0, stoneAt.y, stoneAt.z, 0.0f, 0.0f);
            stoneWorm.aiStep();
            check(!stoneWorm.isRemoved(), "worm surfacing through STONE must survive");
            stoneWorm.discard();

            // (c) SHORT_GRASS counts as air: branch skipped entirely, no pop.
            helper.setBlock(new BlockPos(22, 0, 26), Blocks.GRASS_BLOCK);
            helper.setBlock(new BlockPos(22, 1, 26), Blocks.SHORT_GRASS);
            TestWormSmall grassWorm = new TestWormSmall(helper.getLevel());
            Vec3 grassAt = helper.absoluteVec(new Vec3(22.5, 1.0, 26.5));
            grassWorm.moveTo(grassAt.x, grassAt.y, grassAt.z, 0.0f, 0.0f);
            player.moveTo(grassAt.x + 2.0, grassAt.y, grassAt.z, 0.0f, 0.0f);
            grassWorm.aiStep();
            check(!grassWorm.isRemoved(),
                    "SHORT_GRASS counts as air for surfacing — no pop (EntityWormSmall.java:145-148)");
            grassWorm.discard();

            helper.succeed();
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(playNicelyBefore);
            removeServerPlayer(helper, player);
        }
    }

    // ================================================================
    // i053-ent-k-047-048-d4 — Peacock hunts termites; egg laying + spawn gates
    // ================================================================

    /**
     * Checklist item i053-ent-k-047-048-d4 (ENT-K-047/048, D4).
     * <ul>
     *   <li>Hunt: 1-in-10 AI-step roll finds the nearest visible Termite within
     *       10/2/10 and, inside 2 blocks, hits it for a flat 6.0 mob-attack
     *       (Peacock.java:130-140, orig Peacock.java:166-169 func_70652_k).</li>
     *   <li>Egg lay: independent 1-in-5000 AI-step roll drops a single stack of
     *       1-3 Peacock spawn eggs at ±0-1 x/z, y+1 (Peacock.java:141-151, orig
     *       :171-179,197-199 — the orig egg roll is ungated by time/buddies;
     *       verified against reference_1_7_10_source Peacock.func_70619_bc).</li>
     *   <li>The item's "first half of the day / ≤2 other peacocks within 16 /
     *       y 50-100" clauses are the SPAWN gate (Peacock.checkSpawnRules,
     *       Peacock.java:182-190, orig :101-119 func_70601_bi) — asserted
     *       directly with controlled time/position/buddies.</li>
     * </ul>
     * Statistics: egg lays over 120000 driven steps ~ Poisson(24);
     * P(0) = e^{−24} ≈ 3.8e-11 &lt; 1e-9.
     */
    @GameTest(template = "empty_large")
    public void i053_peacock_hunts_termite_and_eggs(GameTestHelper helper) {
        floor(helper, 18, 18, 30, 30);
        boolean playNicelyBefore = OreSpawnConfig.PLAY_NICELY.get();
        ServerLevel level = helper.getLevel();
        long dayTimeBefore = level.getDayTime();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false); // hunt gate, Peacock.java:160-162

            // ---- hunt: flat 6.0 damage on the termite.
            EntityTermite termite = helper.spawnWithNoFreeWill(ModEntities.ENTITY_TERMITE.get(), new BlockPos(25, 1, 24));
            termite.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(30.0);
            termite.setHealth(30.0f);
            TestPeacock peacock = new TestPeacock(level);
            Vec3 ppos = helper.absoluteVec(new Vec3(24.0, 1.0, 24.0));
            Vec3 tpos = helper.absoluteVec(new Vec3(25.5, 1.0, 24.0));
            boolean hit = false;
            // P(hunt attempt per step) = 1/10; P(no hit in 300) = 0.9^300 ≈ 2e-14.
            for (int i = 0; i < 300 && !hit; i++) {
                peacock.moveTo(ppos.x, ppos.y, ppos.z, 0.0f, 0.0f);
                termite.moveTo(tpos.x, tpos.y, tpos.z, 0.0f, 0.0f);
                termite.invulnerableTime = 0;
                peacock.ai();
                if (termite.getHealth() < 30.0f) hit = true;
            }
            check(hit, "peacock never hunted the adjacent termite in 300 driven steps (1/10 roll)");
            check(Math.abs(termite.getHealth() - 24.0f) < 0.001f,
                    "termite hit must be a flat 6.0 (30 -> 24, termite armor 0); health=" + termite.getHealth());
            termite.discard();

            // ---- egg lay: 1-in-5000 per AI step, stack of 1-3 peacock eggs.
            int laysSeen = 0;
            for (int i = 0; i < 120000; i++) {
                peacock.ai();
            }
            AABB eggBox = new AABB(BlockPos.containing(ppos)).inflate(6, 6, 6);
            List<ItemEntity> eggs = level.getEntitiesOfClass(ItemEntity.class, eggBox,
                    ie -> ie.getItem().is(ModItems.PEACOCK_SPAWN_EGG.get()));
            for (ItemEntity egg : eggs) {
                laysSeen++;
                int c = egg.getItem().getCount();
                check(c >= 1 && c <= 3, "each egg lay is a stack of 1-3 (1 + nextInt(3)); saw " + c);
            }
            check(laysSeen >= 1, "no egg stack laid in 120000 driven steps (P(miss) = e^-24)");
            discardItems(level, eggBox);
            peacock.discard();

            // ---- spawn gates (checkSpawnRules, orig func_70601_bi): first half
            // of the day, y 50-100, ≤2 other peacocks within 16/10/16.
            level.setDayTime(1000L);
            Peacock candidate = new Peacock(ModEntities.PEACOCK.get(), level); // not added — orig gate runs pre-add
            Vec3 sky = helper.absoluteVec(new Vec3(24.0, 0.0, 24.0));
            candidate.moveTo(sky.x, 60.0, sky.z, 0.0f, 0.0f);
            check(candidate.checkSpawnRules(level, MobSpawnType.NATURAL),
                    "day 1000 / y60 / 0 buddies must pass the spawn gate");

            candidate.moveTo(sky.x, 45.0, sky.z, 0.0f, 0.0f);
            check(!candidate.checkSpawnRules(level, MobSpawnType.NATURAL), "y<50 must fail the gate");
            candidate.moveTo(sky.x, 120.0, sky.z, 0.0f, 0.0f);
            check(!candidate.checkSpawnRules(level, MobSpawnType.NATURAL), "y>100 must fail the gate");

            candidate.moveTo(sky.x, 60.0, sky.z, 0.0f, 0.0f);
            level.setDayTime(13000L);
            check(!candidate.checkSpawnRules(level, MobSpawnType.NATURAL),
                    "second half of the day (t=13000) must fail the gate");
            level.setDayTime(1000L);

            // 2 other peacocks within 16/10/16 -> still passes; a 3rd -> fails.
            // (The candidate itself is not in the level, matching the orig
            // pre-addition getCanSpawnHere venue, so "buddies" = others only.)
            List<Peacock> buddies = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Peacock buddy = new Peacock(ModEntities.PEACOCK.get(), level);
                buddy.moveTo(sky.x + 2 + i, 60.0, sky.z, 0.0f, 0.0f);
                level.addFreshEntity(buddy);
                buddies.add(buddy);
            }
            check(candidate.checkSpawnRules(level, MobSpawnType.NATURAL),
                    "exactly 2 other peacocks within 16 must still pass (≤2)");
            Peacock third = new Peacock(ModEntities.PEACOCK.get(), level);
            third.moveTo(sky.x + 4.5, 60.0, sky.z, 0.0f, 0.0f);
            level.addFreshEntity(third);
            buddies.add(third);
            check(!candidate.checkSpawnRules(level, MobSpawnType.NATURAL),
                    "3 other peacocks within 16/10/16 must fail the ≤2-buddies gate");
            for (Peacock b : buddies) b.discard();

            helper.succeed();
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(playNicelyBefore);
            level.setDayTime(dayTimeBefore);
        }
    }

    // ================================================================
    // i054-ent-d-010-d4 — Easter Bunny egg table + crystal-apple breeding
    // ================================================================

    /**
     * Checklist item i054-ent-d-010-d4 (ENT-D-010, D4). Direct spawn bypasses
     * the Easter date gate (the gate lives only in checkSpawnRules,
     * EasterBunny.java:246-257). Each server AI step rolls 1-in-600 to lay a
     * single stack of 1-3 random spawn eggs from the 115-slot table (rolls
     * 0-4 and 114 lay nothing) — EasterBunny.java:99-238, orig
     * EasterBunny.java:120-587. Breeding item is the Crystal Apple; there is
     * NO carrot taming (the audit's carrot claim was wrong) —
     * EasterBunny.java:93-97, orig :609-611; the class is a plain Animal with
     * no tame flag at all.
     *
     * <p>Statistics: lays over 15000 driven steps ~ Poisson(25);
     * P(0) = e^{−25} ≈ 1.4e-11. P(every lay hitting one of the 6 empty table
     * slots) is smaller still.
     */
    @GameTest(template = "empty_large", timeoutTicks = 1200)
    public void i054_easter_bunny_eggs_and_breeding(GameTestHelper helper) {
        floor(helper, 14, 14, 34, 34);
        ServerLevel level = helper.getLevel();

        // ---- egg-table loop (driven, entity not added).
        TestBunny layer = new TestBunny(level);
        Vec3 bpos = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
        layer.moveTo(bpos.x, bpos.y, bpos.z, 0.0f, 0.0f);
        for (int i = 0; i < 15000; i++) layer.ai();
        AABB eggBox = new AABB(BlockPos.containing(bpos)).inflate(6, 6, 6);
        List<ItemEntity> eggDrops = level.getEntitiesOfClass(ItemEntity.class, eggBox, ie -> !ie.getItem().isEmpty());
        check(!eggDrops.isEmpty(), "no egg stacks laid over 15000 driven steps (P = e^-25)");
        Set<Item> distinct = new HashSet<>();
        for (ItemEntity drop : eggDrops) {
            ItemStack stack = drop.getItem();
            check(stack.getItem() instanceof SpawnEggItem,
                    "every laid stack must be a spawn egg (115-slot table), saw " + stack);
            check("orespawn".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()),
                    "laid eggs must be orespawn spawn eggs");
            check(stack.getCount() >= 1 && stack.getCount() <= 3,
                    "each lay is a stack of 1-3 (1 + nextInt(3)); saw " + stack.getCount());
            distinct.add(stack.getItem());
        }
        check(distinct.size() >= 3, "the d115 roll should produce ≥3 distinct egg types over ~25 lays");
        discardItems(level, eggBox);
        layer.discard();

        // ---- no carrot interaction: not food, no love, and structurally no tame flag.
        EasterBunny a = helper.spawn(ModEntities.EASTER_BUNNY.get(), new BlockPos(23, 1, 24));
        EasterBunny b = helper.spawn(ModEntities.EASTER_BUNNY.get(), new BlockPos(25, 1, 24));
        check(!a.isFood(new ItemStack(Items.CARROT)), "carrot must not be Easter Bunny food (orig :609-611)");
        check(!net.minecraft.world.entity.TamableAnimal.class.isAssignableFrom(EasterBunny.class),
                "EasterBunny is a plain Animal — no tame flag exists to set");
        Player feeder = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 fpos = helper.absoluteVec(new Vec3(24.0, 1.0, 23.0));
        feeder.moveTo(fpos.x, fpos.y, fpos.z, 0.0f, 0.0f);
        feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
        a.mobInteract(feeder, InteractionHand.MAIN_HAND);
        check(!a.isInLove(), "carrot must not set love mode");

        // ---- breeds with Crystal Apple.
        check(a.isFood(new ItemStack(ModItems.CRYSTAL_APPLE.get())), "Crystal Apple is the breeding item");
        feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CRYSTAL_APPLE.get(), 2));
        a.mobInteract(feeder, InteractionHand.MAIN_HAND);
        b.mobInteract(feeder, InteractionHand.MAIN_HAND);
        check(a.isInLove() && b.isInLove(), "both bunnies must enter love mode from Crystal Apples");

        AABB babyBox = new AABB(BlockPos.containing(bpos)).inflate(64, 16, 64);
        helper.succeedWhen(() -> {
            List<EasterBunny> babies = level.getEntitiesOfClass(EasterBunny.class, babyBox, EasterBunny::isBaby);
            check(!babies.isEmpty(), "waiting for a baby Easter Bunny from Crystal Apple breeding");
            for (EasterBunny bunny : level.getEntitiesOfClass(EasterBunny.class, babyBox)) bunny.discard();
            discardItems(level, babyBox);
        });
    }

    // ================================================================
    // i061-ent-a-031-060-ent-s-061-068 — lava/fire immunity ×4
    // ================================================================

    /**
     * Checklist item i061-ent-a-031-060-ent-s-061-068 (ENT-A-031/060,
     * ENT-S-061/068; FIX_LOG.md:92; C1 rows ENT-A-031/060). Basilisk,
     * Brutalfly, Urchin and Vortex are registered fireImmune
     * (ModEntities.java:52, 212, 160, 281; orig Basilisk.java:52 /
     * Brutalfly.java:58 field_70178_ae = true). Mechanism asserted end to end:
     * vanilla Entity.lavaHurt() no-ops for fireImmune entities and
     * Entity.isInvulnerableTo() blocks every IS_FIRE-tagged source
     * (decompiled 1.21.1 Entity.java:518-525, 2518-2523), so a lava pit deals
     * no fire damage. A behavioral multi-tick lava soak is deliberately not
     * used: the Vortex carries a 1-in-500/tick daytime discard while not
     * fighting (EntityVortex.java:120-126) which would make a soak flaky.
     */
    @GameTest(template = "empty_large")
    public void i061_lava_immunity_basilisk_brutalfly_urchin_vortex(GameTestHelper helper) {
        floor(helper, 18, 18, 30, 30);
        ServerLevel level = helper.getLevel();
        List<EntityType<? extends Mob>> types = List.of(
                ModEntities.BASILISK.get(), ModEntities.ENTITY_BRUTALFLY.get(),
                ModEntities.URCHIN.get(), ModEntities.ENTITY_VORTEX.get());
        for (EntityType<? extends Mob> type : types) {
            String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            check(type.fireImmune(), name + " must be registered fireImmune()");
            Mob mob = type.create(level);
            check(mob != null, "could not create " + name);
            Vec3 at = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
            mob.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
            float before = mob.getHealth();
            check(mob.isInvulnerableTo(level.damageSources().lava()),
                    name + " must be invulnerable to lava (IS_FIRE + fireImmune, Entity.java:2521)");
            check(mob.isInvulnerableTo(level.damageSources().inFire()), name + " must be invulnerable to inFire");
            check(mob.isInvulnerableTo(level.damageSources().onFire()), name + " must be invulnerable to onFire");
            boolean hurt = mob.hurt(level.damageSources().lava(), 4.0f); // the exact lavaHurt() call, Entity.java:521
            check(!hurt, name + ".hurt(lava) must report no damage");
            check(mob.getHealth() == before, name + " health must be unchanged by lava damage");
            mob.discard();
        }
        helper.succeed();
    }

    // ================================================================
    // i062-ent-k-045 — Ostrich cactus immunity
    // ================================================================

    /**
     * Checklist item i062-ent-k-045 (ENT-K-045). Ostrich skips cactus damage
     * entirely but applies everything else via super — while always reporting
     * false to the attacker (preserved orig quirk). Ostrich.java:95-103, orig
     * Ostrich.java:133-138. CactusBlock.entityInside is a plain
     * hurt(damageSources().cactus(), 1.0F), so the direct source invocation is
     * the exact contact path.
     */
    @GameTest(template = "empty_large")
    public void i062_ostrich_cactus_immunity(GameTestHelper helper) {
        floor(helper, 20, 20, 28, 28);
        Ostrich ostrich = helper.spawnWithNoFreeWill(ModEntities.OSTRICH.get(), new BlockPos(24, 1, 24));
        float max = ostrich.getMaxHealth();
        check(max == 25.0f, "Ostrich max health should be 25 (Ostrich.java:90), was " + max);

        boolean cactusRet = ostrich.hurt(helper.getLevel().damageSources().cactus(), 4.0f);
        check(!cactusRet, "cactus hurt reports false");
        check(ostrich.getHealth() == max, "cactus damage must be skipped entirely (orig Ostrich.java:133-138)");

        boolean genericRet = ostrich.hurt(helper.getLevel().damageSources().generic(), 3.0f);
        check(!genericRet, "orig quirk: hurt() always reports false even when damage applies");
        check(Math.abs(ostrich.getHealth() - (max - 3.0f)) < 0.001f,
                "normal damage must still apply (25 -> 22, armor 0); health=" + ostrich.getHealth());
        ostrich.discard();
        helper.succeed();
    }

    // ================================================================
    // i063-ent-k-007-d4 — Kyuubi fire immunity
    // ================================================================

    /**
     * Checklist item i063-ent-k-007-d4 (ENT-K-007, D4). Kyuubi is registered
     * fireImmune (ModEntities.java:232) and so takes no damage standing in
     * lava or its own fire-attack blocks: BaseFireBlock.entityInside's
     * unconditional hurt(inFire) is blocked by Entity.isInvulnerableTo
     * (IS_FIRE + fireImmune — decompiled BaseFireBlock.java:129-139,
     * Entity.java:2518-2523), and Entity.lavaHurt() no-ops (Entity.java:518-525).
     */
    @GameTest(template = "empty_large")
    public void i063_kyuubi_fire_immunity(GameTestHelper helper) {
        floor(helper, 20, 20, 28, 28);
        ServerLevel level = helper.getLevel();
        check(ModEntities.ENTITY_KYUUBI.get().fireImmune(), "Kyuubi must be registered fireImmune()");
        EntityKyuubi kyuubi = helper.spawnWithNoFreeWill(ModEntities.ENTITY_KYUUBI.get(), new BlockPos(24, 1, 24));
        float before = kyuubi.getHealth();
        check(kyuubi.isInvulnerableTo(level.damageSources().inFire()),
                "Kyuubi must be invulnerable to fire blocks (its own fire attack)");
        check(kyuubi.isInvulnerableTo(level.damageSources().onFire()), "Kyuubi must be invulnerable to burning");
        check(kyuubi.isInvulnerableTo(level.damageSources().lava()), "Kyuubi must be invulnerable to lava");
        check(!kyuubi.hurt(level.damageSources().inFire(), 1.0f), "fire-block hurt must be rejected");
        check(!kyuubi.hurt(level.damageSources().lava(), 4.0f), "lava hurt must be rejected");
        check(kyuubi.getHealth() == before, "no self-damage from fire/lava");
        kyuubi.discard();
        helper.succeed();
    }

    // ================================================================
    // i064-ent-s-025-ent-s-047-d4 — SpitBug/Triffid: cactus + fall immunity
    // ================================================================

    /**
     * Checklist item i064-ent-s-025-ent-s-047-d4 (ENT-S-025 / ENT-S-047, D4).
     * SpitBug ignores cactus and fall damage entirely (no hurt, no timer, no
     * retaliation) — EntitySpitBug.java:146-166, orig SpitBug.java:239-256 —
     * while normal damage still lands. The Triffid spawns with its shell
     * closed (DATA_OPEN_CLOSED = 0, EntityTriffid.java:60-65) and a closed
     * shell is fully invulnerable to non-bypass damage, which covers cactus
     * and long falls — EntityTriffid.java:141-185, orig Triffid.java:221-233.
     */
    @GameTest(template = "empty_large")
    public void i064_spitbug_triffid_cactus_fall_immunity(GameTestHelper helper) {
        floor(helper, 18, 18, 30, 30);
        ServerLevel level = helper.getLevel();

        EntitySpitBug spitBug = helper.spawnWithNoFreeWill(ModEntities.ENTITY_SPIT_BUG.get(), new BlockPos(22, 1, 24));
        float sbMax = spitBug.getHealth();
        check(!spitBug.hurt(level.damageSources().cactus(), 4.0f), "SpitBug cactus hurt must be filtered");
        check(spitBug.getHealth() == sbMax, "SpitBug takes no cactus damage (orig :244)");
        spitBug.causeFallDamage(30.0f, 1.0f, level.damageSources().fall());
        check(spitBug.getHealth() == sbMax, "SpitBug takes no fall damage even from a 30-block fall");
        check(spitBug.hurt(level.damageSources().generic(), 2.0f),
                "SpitBug is not blanket-immune: generic damage must land");
        check(spitBug.getHealth() < sbMax, "generic damage must reduce SpitBug health");
        spitBug.discard();

        EntityTriffid triffid = helper.spawnWithNoFreeWill(ModEntities.ENTITY_TRIFFID.get(), new BlockPos(26, 1, 24));
        check(triffid.getOpenClosed() == 0, "Triffid spawns with the shell closed (EntityTriffid.java:64)");
        float trMax = triffid.getHealth();
        check(triffid.isInvulnerableTo(level.damageSources().cactus()),
                "closed Triffid is invulnerable to cactus (EntityTriffid.java:177-185)");
        check(!triffid.hurt(level.damageSources().cactus(), 4.0f), "closed-shell cactus hurt returns false");
        check(triffid.getHealth() == trMax, "no cactus damage on the Triffid");
        triffid.causeFallDamage(30.0f, 1.0f, level.damageSources().fall());
        check(triffid.getHealth() == trMax, "no fall damage on the closed Triffid");
        triffid.discard();
        helper.succeed();
    }

    // ================================================================
    // i065-ent-a-052-ent-a-001-d4 — fireball pass-through, acid vanish
    // ================================================================

    /**
     * Checklist item i065-ent-a-052-ent-a-001-d4 (ENT-A-052 / ENT-A-001, D4).
     * <ul>
     *   <li>BetterFireballs pass through other BetterFireballs, Mothra and
     *       Royalty; with the shooter's notme flag also through players and
     *       Dragons — BetterFireball.canHitEntity (BetterFireball.java:60-78,
     *       orig BetterFireball.java:136-155, 208-217; royalty set:
     *       MyUtils.isRoyalty). canHitEntity is the vanilla sweep filter, so
     *       false = the projectile flies through.</li>
     *   <li>Acid balls vanish harmlessly on the acid bugs (TrooperBug/SpitBug):
     *       discard with zero damage — LaserBall.onHitEntity
     *       (LaserBall.java:103-108, orig LaserBall.java:83-92) — while a
     *       non-spared target in identical geometry does take the hit
     *       (control proves the entity-hit happens before any block hit).</li>
     * </ul>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i065_fireball_passthrough_acid_vanish(GameTestHelper helper) {
        floor(helper, 14, 14, 34, 34);
        ServerLevel level = helper.getLevel();

        // ---- pass-through mechanism (canHitEntity), construction-only targets.
        TestFireball fireball = new TestFireball(level);
        Mothra mothra = new Mothra(ModEntities.MOTHRA.get(), level);
        BetterFireball otherBall = new BetterFireball(ModEntities.BETTER_FIREBALL.get(), level);
        ThePrince royalty = new ThePrince(ModEntities.THE_PRINCE.get(), level);
        Dragon dragon = new Dragon(ModEntities.DRAGON.get(), level);
        Cow cow = EntityType.COW.create(level);
        check(cow != null, "cow control could not be created");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        check(!fireball.canHit(mothra), "BetterFireball must pass through Mothra");
        check(!fireball.canHit(otherBall), "BetterFireball must pass through other BetterFireballs");
        check(!fireball.canHit(royalty), "BetterFireball must pass through Royalty (ThePrince)");
        check(fireball.canHit(cow), "control: a plain mob IS hittable");
        check(fireball.canHit(player), "without notme, players are hittable");
        check(fireball.canHit(dragon), "without notme, Dragons are hittable");
        fireball.setNotMe();
        check(!fireball.canHit(player), "with notme set, players are spared (orig :152,214)");
        check(!fireball.canHit(dragon), "with notme set, Dragons are spared");

        // ---- acid vanish, behavioral: straight drop through the target box.
        EntityTrooperBug trooper = helper.spawnWithNoFreeWill(ModEntities.ENTITY_TROOPER_BUG.get(), new BlockPos(20, 1, 24));
        EntitySpitBug spitBug = helper.spawnWithNoFreeWill(ModEntities.ENTITY_SPIT_BUG.get(), new BlockPos(24, 1, 24));
        Cow control = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(28, 1, 24));
        float trooperHp = trooper.getHealth();
        float spitHp = spitBug.getHealth();
        float controlHp = control.getHealth();

        Acid acidA = new Acid(level, trooper.getX(), trooper.getY() + trooper.getBbHeight() + 2.0, trooper.getZ());
        acidA.setDeltaMovement(0, -0.5, 0);
        level.addFreshEntity(acidA);
        Acid acidB = new Acid(level, spitBug.getX(), spitBug.getY() + spitBug.getBbHeight() + 2.0, spitBug.getZ());
        acidB.setDeltaMovement(0, -0.5, 0);
        level.addFreshEntity(acidB);
        Acid acidC = new Acid(level, control.getX(), control.getY() + control.getBbHeight() + 2.0, control.getZ());
        acidC.setDeltaMovement(0, -0.5, 0);
        level.addFreshEntity(acidC);

        helper.runAfterDelay(10, () -> {
            check(acidA.isRemoved(), "acid on TrooperBug must be discarded on impact");
            check(acidB.isRemoved(), "acid on SpitBug must be discarded on impact");
            check(trooper.getHealth() == trooperHp,
                    "acid must not damage TrooperBug (LaserBall.java:103-108)");
            check(spitBug.getHealth() == spitHp, "acid must not damage SpitBug");
            check(control.getHealth() < controlHp || control.isRemoved() || control.isDeadOrDying(),
                    "control: identical acid drop must damage a non-spared mob (16.0 thrown)");
            trooper.discard();
            spitBug.discard();
            control.discard();
            helper.succeed();
        });
    }

    // ================================================================
    // i070-d2-hoverboard-crash — RECLASSIFIED: HARNESS_LIMIT (MANUAL_ONLY)
    // ================================================================
    //
    // Test method removed in the 2026-08-10 gametest triage. The wall-crash
    // expectation (orig Elevator.java:490-498 — speed > 0.75 into a wall
    // shatters the board into 6-15 sticks + 2 diamonds, no Hoverboard item)
    // CANNOT be verified on the headless GameTestServer:
    //   * The port's documented ANIM-012 architecture (Elevator class
    //     Javadoc) integrates ridden movement on the CONTROLLING CLIENT
    //     (tickRidden, gated by isControlledByLocalInstance()); the server
    //     detects the crash in serverRiddenTick (Elevator.java:358-385) from
    //     in-tick position deltas of the client-synced entity.
    //   * Every server-side travel() path zeroes horizontal motion (riderless
    //     drift, Elevator.java:544-569, restoring orig :485-488), and a
    //     ServerPlayer is never a "local instance", so NO server-side code
    //     path can produce the >0.75 in-tick horizontal position delta the
    //     crash check requires. External movers (pistons, pushes, teleports)
    //     land between entity ticks, where baseTick re-captures xo/zo first.
    //   * The removed test's premise ("with a non-player passenger the server
    //     integrates the board's own deltaMovement") is false for the port,
    //     and matches no documented original behavior either: the original's
    //     ridden branch cast the rider to EntityPlayer (orig Elevator.java
    //     rider physics), so a pig-ridden board was a ClassCastException, not
    //     a crash-drop path.
    // Checklist item i070-d2-hoverboard-crash therefore returns to
    // MANUAL_ONLY (verify in a real client: ride into a wall above 0.75
    // blocks/tick; expect 6-15 sticks + exactly 2 diamonds, rider ejected,
    // no Hoverboard item back).

    // ================================================================
    // i083 — hitbox size table + CaterKiller playNicely halving
    // ================================================================

    /**
     * Checklist item
     * i083-ent-a-002-012-017-023-040-060-072-078-091-106-hitboxes
     * (ENT-A-002/012/017/023/040/060/072/078/091/106). Bounding-box dimensions
     * versus the size table in phase_c_reports/C1_entities_A_C.md
     * ("Carried-forward dimension / fire-immunity remainders"): Alien
     * 1.1×3.25, AntRobot 2.75×1.25, AttackSquid 1.0×1.25, BandP 0.75×1.75,
     * Bee 1.5×2.5, Brutalfly 5.0×2.0, CaterKiller 2.9×4.6 (1.45×2.3 when
     * playNicely — EntityCaterKiller#getDefaultDimensions,
     * EntityCaterKiller.java:67-72, orig CaterKiller.java:54-58), CaveFisher
     * 1.35×0.75, CloudShark 1.0×0.75, CreepingHorror 0.75×0.5. F3+B is only
     * the manual inspection method; EntityType dimensions are the mechanism.
     */
    @GameTest(template = "empty_large")
    public void i083_hitbox_size_table(GameTestHelper helper) {
        record Row(EntityType<?> type, float w, float h, String id) {}
        List<Row> table = List.of(
                new Row(ModEntities.ALIEN.get(), 1.1f, 3.25f, "ENT-A-002 Alien"),
                new Row(ModEntities.ANT_ROBOT.get(), 2.75f, 1.25f, "ENT-A-012 AntRobot"),
                new Row(ModEntities.ATTACK_SQUID.get(), 1.0f, 1.25f, "ENT-A-017 AttackSquid"),
                new Row(ModEntities.BAND_P.get(), 0.75f, 1.75f, "ENT-A-023 BandP"),
                new Row(ModEntities.ENTITY_BEE.get(), 1.5f, 2.5f, "ENT-A-040 Bee"),
                new Row(ModEntities.ENTITY_BRUTALFLY.get(), 5.0f, 2.0f, "ENT-A-060 Brutalfly"),
                new Row(ModEntities.ENTITY_CATER_KILLER.get(), 2.9f, 4.6f, "ENT-A-072 CaterKiller"),
                new Row(ModEntities.CAVE_FISHER.get(), 1.35f, 0.75f, "ENT-A-078 CaveFisher"),
                new Row(ModEntities.CLOUD_SHARK.get(), 1.0f, 0.75f, "ENT-A-091 CloudShark"),
                new Row(ModEntities.CREEPING_HORROR.get(), 0.75f, 0.5f, "ENT-A-106 CreepingHorror"));
        for (Row row : table) {
            check(Math.abs(row.type().getWidth() - row.w()) < 1e-4,
                    row.id() + " width must be " + row.w() + ", was " + row.type().getWidth());
            check(Math.abs(row.type().getHeight() - row.h()) < 1e-4,
                    row.id() + " height must be " + row.h() + ", was " + row.type().getHeight());
        }

        // CaterKiller live halving with playNicely (EntityCaterKiller.java:67-72).
        floor(helper, 18, 18, 30, 30);
        boolean playNicelyBefore = OreSpawnConfig.PLAY_NICELY.get();
        EntityCaterKiller cater = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            cater = helper.spawnWithNoFreeWill(ModEntities.ENTITY_CATER_KILLER.get(), new BlockPos(24, 1, 24));
            cater.refreshDimensions();
            check(Math.abs(cater.getBbWidth() - 2.9f) < 1e-4 && Math.abs(cater.getBbHeight() - 4.6f) < 1e-4,
                    "CaterKiller full size 2.9x4.6 with playNicely=false; was "
                            + cater.getBbWidth() + "x" + cater.getBbHeight());
            OreSpawnConfig.PLAY_NICELY.set(true);
            cater.refreshDimensions();
            check(Math.abs(cater.getBbWidth() - 1.45f) < 1e-4 && Math.abs(cater.getBbHeight() - 2.3f) < 1e-4,
                    "CaterKiller halves to 1.45x2.3 with playNicely=true (orig CaterKiller.java:54-58); was "
                            + cater.getBbWidth() + "x" + cater.getBbHeight());
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(playNicelyBefore);
            if (cater != null) cater.discard();
        }
        helper.succeed();
    }

    // ================================================================
    // i088-ent-a-018-019 — AttackSquid range gate
    // ================================================================

    /**
     * Checklist item i088-ent-a-018-019 (ENT-A-018/019). AttackSquid combat is
     * distance-gated at distSq 9 (3 blocks): inside, the double melee roll
     * (~40%/AI-step) lands doHurtTarget for its 8.0 attack damage and the
     * ranged branch is unreachable; beyond, it spits an InkSack (1-in-3) or
     * WaterBall via watercanon. AttackSquid.java:152-199, orig
     * AttackSquid.java:507 (double melee roll), :523-549 (watercanon);
     * stats orig OreSpawnMain.java:6510 (8 ATK).
     *
     * <p>Statistics: melee P/step = 1/10 · (1 − 3/4 · 4/5) = 0.04 → P(no hit
     * in 600) = 0.96^600 ≈ 2.4e-11. Ranged P/step = 1/10 · 1/5 = 1/50 →
     * P(no shot in 1200) = (49/50)^1200 ≈ 2.9e-11.
     */
    @GameTest(template = "empty_large")
    public void i088_attacksquid_range_gate(GameTestHelper helper) {
        floor(helper, 14, 14, 34, 34);
        ServerLevel level = helper.getLevel();
        TestSquid squid = new TestSquid(level);
        Vec3 spos = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
        squid.moveTo(spos.x, spos.y, spos.z, 0.0f, 0.0f);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(26, 1, 24));
        squid.setTarget(cow);
        AABB projBox = new AABB(BlockPos.containing(spos)).inflate(16, 8, 16);

        // ---- melee inside 3 blocks; ranged branch structurally unreachable.
        Vec3 near = new Vec3(spos.x + 2.0, spos.y, spos.z);
        boolean hit = false;
        for (int i = 0; i < 600 && !hit; i++) {
            squid.setHealth(squid.getMaxHealth()); // cancel any dry-out self-damage rolls (AttackSquid.java:142-144)
            squid.moveTo(spos.x, spos.y, spos.z, 0.0f, 0.0f);
            cow.moveTo(near.x, near.y, near.z, 0.0f, 0.0f);
            cow.invulnerableTime = 0;
            cow.setDeltaMovement(0, 0, 0);
            squid.ai();
            if (cow.getHealth() < cow.getMaxHealth()) hit = true;
        }
        check(hit, "squid never meleed the cow inside 3 blocks over 600 driven steps");
        check(Math.abs(cow.getHealth() - 2.0f) < 0.001f,
                "melee must be the 8.0 attack damage (cow 10 -> 2); health=" + cow.getHealth());
        check(level.getEntitiesOfClass(InkSack.class, projBox).isEmpty()
                        && level.getEntitiesOfClass(WaterBall.class, projBox).isEmpty(),
                "no ink/water projectiles may spawn while the target is inside 3 blocks");

        // ---- ranged beyond 3 blocks: ink/water projectile, no melee.
        cow.setHealth(cow.getMaxHealth());
        Vec3 farPos = new Vec3(spos.x + 6.0, spos.y, spos.z);
        boolean shot = false;
        for (int i = 0; i < 1200 && !shot; i++) {
            squid.setHealth(squid.getMaxHealth());
            squid.moveTo(spos.x, spos.y, spos.z, 0.0f, 0.0f);
            cow.moveTo(farPos.x, farPos.y, farPos.z, 0.0f, 0.0f);
            squid.ai();
            shot = !level.getEntitiesOfClass(InkSack.class, projBox).isEmpty()
                    || !level.getEntitiesOfClass(WaterBall.class, projBox).isEmpty();
        }
        check(shot, "squid never fired an ink/water projectile at a 6-block target over 1200 driven steps");
        check(cow.getHealth() == cow.getMaxHealth(), "no melee damage beyond 3 blocks");
        for (InkSack p : level.getEntitiesOfClass(InkSack.class, projBox)) p.discard();
        for (WaterBall p : level.getEntitiesOfClass(WaterBall.class, projBox)) p.discard();
        cow.discard();
        squid.discard();
        helper.succeed();
    }

    // ================================================================
    // i089-ent-a-062 — Brutalfly fireball type by difficulty, +1 self-heal
    // ================================================================

    /**
     * Checklist item i089-ent-a-062 (ENT-A-062). Brutalfly's attackWithSomething
     * is difficulty-keyed: Easy = vanilla SmallFireball; Hard (and Normal's
     * other 50%) = BetterFireball; every shot self-heals 1 HP when below max.
     * EntityBrutalfly.java:149-232 (barrage odds 1-in-3, 1-in-2 on Hard;
     * player strafe branch :152-161), orig Brutalfly.java:155,168-170,369-406.
     * Difficulty changes are set and restored inside one server tick, so
     * concurrent tests never tick under the altered value.
     *
     * <p>Statistics: P(shot per driven step) = 1/6 · 1/3 = 1/18 on Easy
     * (P(0 in 500) = (17/18)^500 ≈ 4e-13) and 1/6 · 1/2 = 1/12 on Hard
     * (P(0 in 400) ≈ 8e-16). Spawned projectiles are counted and discarded in
     * the same tick, so they never move or explode.
     */
    @GameTest(template = "empty_large", batch = "orespawnb_players")
    public void i089_brutalfly_difficulty_fireballs(GameTestHelper helper) {
        floor(helper, 14, 14, 34, 34);
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        Difficulty before = server.getWorldData().getDifficulty();
        TestServerPlayer player = null;
        TestBrutalfly fly = null;
        try {
            player = addServerPlayer(helper);
            Vec3 fpos = helper.absoluteVec(new Vec3(24.5, 2.0, 24.5));
            player.moveTo(fpos.x + 10.0, fpos.y, fpos.z, 0.0f, 0.0f);
            player.setInvulnerable(true); // projectiles never tick, but belt and braces
            fly = new TestBrutalfly(level);
            fly.moveTo(fpos.x, fpos.y, fpos.z, 0.0f, 0.0f);
            AABB projBox = new AABB(BlockPos.containing(fpos)).inflate(40, 20, 40);

            // ---- EASY: only SmallFireballs.
            // Health 30 of max 110 (orig OreSpawnMain.java:6470) leaves 80 HP of
            // heal headroom; shots ~ Poisson(27.8), P(≥80) ≈ e^-32 — no cap risk.
            server.setDifficulty(Difficulty.EASY, true);
            fly.setHealth(30.0f);
            for (int i = 0; i < 500; i++) {
                fly.moveTo(fpos.x, fpos.y, fpos.z, 0.0f, 0.0f);
                fly.ai();
            }
            List<SmallFireball> smalls = level.getEntitiesOfClass(SmallFireball.class, projBox);
            List<BetterFireball> betters = level.getEntitiesOfClass(BetterFireball.class, projBox);
            check(!smalls.isEmpty(), "Easy difficulty must produce SmallFireballs (orig Brutalfly.java:369-406)");
            check(betters.isEmpty(), "Easy difficulty must never produce BetterFireballs");
            int easyShots = smalls.size();
            check(easyShots < 80, "sanity: shots stayed below the heal headroom");
            check(Math.abs(fly.getHealth() - (30.0f + easyShots)) < 0.001f,
                    "each shot self-heals 1 HP: expected " + (30.0f + easyShots) + ", was " + fly.getHealth());
            for (SmallFireball p : smalls) p.discard();

            // ---- HARD: only BetterFireballs (shots ~ Poisson(33.3), P(≥80) ≈ e^-23).
            server.setDifficulty(Difficulty.HARD, true);
            fly.setHealth(30.0f);
            for (int i = 0; i < 400; i++) {
                fly.moveTo(fpos.x, fpos.y, fpos.z, 0.0f, 0.0f);
                fly.ai();
            }
            smalls = level.getEntitiesOfClass(SmallFireball.class, projBox);
            betters = level.getEntitiesOfClass(BetterFireball.class, projBox);
            check(!betters.isEmpty(), "Hard difficulty must produce BetterFireballs");
            check(smalls.isEmpty(), "Hard difficulty must never produce SmallFireballs");
            int hardShots = betters.size();
            check(hardShots < 80, "sanity: shots stayed below the heal headroom");
            check(Math.abs(fly.getHealth() - (30.0f + hardShots)) < 0.001f,
                    "each Hard shot self-heals 1 HP: expected " + (30.0f + hardShots) + ", was " + fly.getHealth());
            for (BetterFireball p : betters) p.discard();
            helper.succeed();
        } finally {
            server.setDifficulty(before, true);
            if (fly != null) fly.discard();
            removeServerPlayer(helper, player);
        }
    }

    // ================================================================
    // i090-ent-d-044 — GiantRobot laser gate + special lasers >10 blocks
    // ================================================================

    /**
     * Checklist item i090-ent-d-044 (ENT-D-044). GiantRobot only engages once
     * its head has swung to within 0.5 rad of the target bearing
     * (GiantRobot.java:117-138, orig GiantRobot.java:256-263); beyond distSq
     * 100 the LaserBall is setSpecial() with a 25-tick reload, closer shots
     * are non-special with a 10-tick reload (GiantRobot.java:149-182, orig
     * :264-283). The reload ticker decrements once per AI step
     * (GiantRobot.java:106), so "slower special lasers" is asserted as: no
     * follow-up laser possible for 24 aligned steps after a far shot vs. a
     * 9-step lockout after a near shot. isSpecial is read via reflection
     * (private flag, LaserBall.java:30 — no getter exists).
     */
    @GameTest(template = "empty_large")
    public void i090_giantrobot_laser_gate(GameTestHelper helper) throws Exception {
        floor(helper, 6, 6, 42, 42);
        ServerLevel level = helper.getLevel();
        TestRobot robot = new TestRobot(level);
        Vec3 rpos = helper.absoluteVec(new Vec3(20.5, 1.0, 24.5));
        robot.moveTo(rpos.x, rpos.y, rpos.z, 0.0f, 0.0f);
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(35, 1, 24));
        // headroom so the robot's melee (in the same aligned branch) can never
        // kill the target between per-step heals
        golem.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0);
        golem.setHealth(500.0f);
        Vec3 farPos = new Vec3(rpos.x + 15.0, rpos.y, rpos.z); // distSq 225: <256 engage window, >100 special
        golem.moveTo(farPos.x, farPos.y, farPos.z, 0.0f, 0.0f);
        robot.setTarget(golem);
        AABB laserBox = new AABB(BlockPos.containing(rpos)).inflate(30, 20, 30);
        Field specialField = LaserBall.class.getDeclaredField("isSpecial");
        specialField.setAccessible(true);

        double bearing = Math.atan2(golem.getZ() - robot.getZ(), golem.getX() - robot.getX());
        float alignedHead = (float) (Math.toDegrees(bearing) - 90.0);
        float misalignedHead = alignedHead + 180.0f;

        // ---- (a) head facing away: never fires (bearing error ~pi > 0.5 rad).
        for (int i = 0; i < 100; i++) {
            robot.yHeadRot = misalignedHead;
            golem.setHealth(golem.getMaxHealth());
            robot.setTarget(golem);
            robot.ai();
        }
        check(level.getEntitiesOfClass(LaserBall.class, laserBox).isEmpty(),
                "no laser may fire while the head faces away from the target (orig GiantRobot.java:256-263)");

        // ---- (b) aligned + >10 blocks: special laser, 25-tick reload.
        LaserBall firstLaser = null;
        for (int i = 0; i < 300 && firstLaser == null; i++) {
            robot.yHeadRot = alignedHead;
            golem.moveTo(farPos.x, farPos.y, farPos.z, 0.0f, 0.0f);
            golem.setHealth(golem.getMaxHealth());
            robot.setTarget(golem);
            robot.ai();
            List<LaserBall> lasers = level.getEntitiesOfClass(LaserBall.class, laserBox);
            if (!lasers.isEmpty()) firstLaser = lasers.get(0);
        }
        check(firstLaser != null, "aligned head must fire within 300 driven steps (1/5 engage roll)");
        check(specialField.getBoolean(firstLaser),
                "shots from >10 blocks (distSq 225 > 100) must be setSpecial() (orig :264-283)");
        firstLaser.discard();
        // reload 25: the next 24 aligned steps cannot fire (ticker decrements once per step).
        for (int i = 0; i < 24; i++) {
            robot.yHeadRot = alignedHead;
            golem.setHealth(golem.getMaxHealth());
            robot.setTarget(golem);
            robot.ai();
            check(level.getEntitiesOfClass(LaserBall.class, laserBox).isEmpty(),
                    "special-shot reload is 25 ticks — no laser may fire on aligned step " + (i + 1));
        }

        // ---- (c) aligned + <10 blocks: non-special laser, 10-tick reload.
        Vec3 nearPos = new Vec3(rpos.x + 6.0, rpos.y, rpos.z); // distSq 36 < 100
        LaserBall nearLaser = null;
        for (int i = 0; i < 300 && nearLaser == null; i++) {
            robot.yHeadRot = alignedHead;
            golem.moveTo(nearPos.x, nearPos.y, nearPos.z, 0.0f, 0.0f);
            golem.setHealth(golem.getMaxHealth());
            robot.setTarget(golem);
            robot.ai();
            List<LaserBall> lasers = level.getEntitiesOfClass(LaserBall.class, laserBox);
            if (!lasers.isEmpty()) nearLaser = lasers.get(0);
        }
        check(nearLaser != null, "aligned near target must be fired at within 300 driven steps");
        check(!specialField.getBoolean(nearLaser), "shots from inside 10 blocks are NOT special");
        nearLaser.discard();
        // reload 10: only 9 locked-out aligned steps after a near shot.
        for (int i = 0; i < 9; i++) {
            robot.yHeadRot = alignedHead;
            golem.moveTo(nearPos.x, nearPos.y, nearPos.z, 0.0f, 0.0f);
            golem.setHealth(golem.getMaxHealth());
            robot.setTarget(golem);
            robot.ai();
            check(level.getEntitiesOfClass(LaserBall.class, laserBox).isEmpty(),
                    "near-shot reload is 10 ticks — no laser on aligned step " + (i + 1));
        }

        for (LaserBall l : level.getEntitiesOfClass(LaserBall.class, laserBox)) l.discard();
        golem.discard();
        robot.discard();
        helper.succeed();
    }

    // ================================================================
    // i092-boss-024 — Baby Prince hunts the insect prey list
    // ================================================================

    /**
     * Checklist item i092-boss-024 (BOSS-024). The Baby Prince's target filter
     * treats Mothra, Butterflies, Cockateils, Dragonflies and Mosquitoes as
     * PREY (not just Monsters): ThePrince.isSuitableTarget,
     * ThePrince.java:556-566, orig ThePrince.java:746-761. The hunt is a
     * 1-in-7 AI-step roll ending in doHurtTarget's flat 10.0 mob attack within
     * melee range 3 + width/2 (ThePrince.java:341-364, 206-213).
     *
     * <p>Statistics per prey: P(hunt attempt) = 1/7 per driven step →
     * P(no attack in 200 steps) = (6/7)^200 ≈ 4e-14.
     */
    @GameTest(template = "empty_large")
    public void i092_baby_prince_prey_targeting(GameTestHelper helper) {
        floor(helper, 14, 14, 34, 34);
        boolean playNicelyBefore = OreSpawnConfig.PLAY_NICELY.get();
        TestPrince prince = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false); // findSomethingToAttack gate, ThePrince.java:568-570
            prince = new TestPrince(helper.getLevel());
            Vec3 ppos = helper.absoluteVec(new Vec3(24.5, 1.0, 24.5));
            prince.moveTo(ppos.x, ppos.y, ppos.z, 0.0f, 0.0f);

            List<EntityType<? extends Mob>> preyTypes = List.of(
                    ModEntities.ENTITY_BUTTERFLY.get(), ModEntities.COCKATEIL.get(),
                    ModEntities.ENTITY_DRAGONFLY.get(), ModEntities.ENTITY_MOSQUITO.get(),
                    ModEntities.MOTHRA.get());
            for (EntityType<? extends Mob> preyType : preyTypes) {
                String name = BuiltInRegistries.ENTITY_TYPE.getKey(preyType).toString();
                Mob prey = helper.spawnWithNoFreeWill(preyType, new BlockPos(26, 1, 24));
                prey.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(60.0);
                prey.setHealth(60.0f);
                Vec3 preyAt = helper.absoluteVec(new Vec3(26.5, 1.0, 24.5));
                boolean attacked = false;
                for (int i = 0; i < 200 && !attacked; i++) {
                    prince.moveTo(ppos.x, ppos.y, ppos.z, 0.0f, 0.0f);
                    prey.moveTo(preyAt.x, preyAt.y, preyAt.z, 0.0f, 0.0f);
                    prey.invulnerableTime = 0;
                    prince.ai();
                    if (prey.getHealth() < 60.0f || prey.isDeadOrDying() || prey.isRemoved()) attacked = true;
                }
                check(attacked, "Baby Prince must hunt " + name
                        + " unprovoked (prey list, orig ThePrince.java:749-761)");
                prey.discard();
            }
            helper.succeed();
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(playNicelyBefore);
            if (prince != null) prince.discard();
        }
    }

    // ================================================================
    // i101-boss-010 — dormant Queen takes normal damage during wake-up
    // ================================================================

    /**
     * Checklist item i101-boss-010 (BOSS-010; FIX_LOG.md:100). The Queen
     * spawns dormant (blue, IS_AWAKE=false — TheQueen.java:271-273); the first
     * hit starts the 60-tick wake-up (WAKE_UP_DURATION_TICKS,
     * TheQueen.java:131-132, 536-538) but must NOT absorb damage: the 1.7.10
     * Queen had no dormant phase, so the triggering hit and every hit during
     * the window fall through to the normal damage path (TheQueen.java:527-586).
     * Asserted as delta equality: an identical player hit during the window
     * deals exactly the same health delta as the first hit (same armor state —
     * both above 2/3 max HP, playerHitCount &lt; 10). She can also fight during
     * the window (doHurtTarget lands). After the window she is awake
     * (TheQueen.java:683-695).
     */
    @GameTest(template = "empty_large", timeoutTicks = 300)
    public void i101_queen_dormant_wakeup_damage(GameTestHelper helper) {
        floor(helper, 10, 10, 38, 38);
        ServerLevel level = helper.getLevel();
        TheQueen queen = helper.spawn(ModEntities.THE_QUEEN.get(), new BlockPos(24, 1, 24));
        Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 apos = helper.absoluteVec(new Vec3(24.0, 1.0, 20.0));
        attacker.moveTo(apos.x, apos.y, apos.z, 0.0f, 0.0f);
        final float[] delta1 = new float[1];

        helper.startSequence().thenExecute(() -> {
            check(!queen.isAwake(), "Queen must spawn dormant (blue) — TheQueen.java:271-273");
            check(queen.getTransitionTicks() == 0, "no transition before the first hit");
            float before = queen.getHealth();
            boolean landed = queen.hurt(level.damageSources().playerAttack(attacker), 200.0f);
            check(landed, "first hit on the dormant Queen must land");
            delta1[0] = before - queen.getHealth();
            check(delta1[0] > 0.0f, "first hit must deal real damage (BOSS-010)");
            check(queen.getTransitionTicks() > 0, "first hit must start the 60-tick wake-up");
            check(!queen.isAwake(), "she is not awake until the window completes");
        }).thenExecuteAfter(25, () -> {
            // 25 ticks in: window still open (60), her 20-tick i-frames expired.
            check(!queen.isAwake() && queen.getTransitionTicks() > 0, "still waking up 25 ticks in");
            float before = queen.getHealth();
            boolean landed = queen.hurt(level.damageSources().playerAttack(attacker), 200.0f);
            check(landed, "hits during the wake-up window must land");
            float delta2 = before - queen.getHealth();
            check(Math.abs(delta2 - delta1[0]) < 0.001f,
                    "wake-up window damage must equal normal damage: first=" + delta1[0] + " second=" + delta2);
            // she can fight during the window:
            Cow victim = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(25, 1, 22));
            queen.doHurtTarget(victim);
            check(victim.getHealth() < victim.getMaxHealth() || victim.isDeadOrDying() || victim.isRemoved(),
                    "Queen must be able to attack during the wake-up window");
            victim.discard();
        }).thenExecuteAfter(50, () -> {
            // t ≈ 75 > 60: the transition has completed.
            check(queen.isAwake(), "Queen must be awake once the 60-tick window has elapsed (TheQueen.java:683-695)");
            queen.discard();
            AABB around = new AABB(helper.absolutePos(new BlockPos(24, 1, 24))).inflate(64, 32, 64);
            for (QueenHead head : level.getEntitiesOfClass(QueenHead.class, around)) {
                head.discard();
            }
            discardItems(level, around);
        }).thenSucceed();
    }

    // ================================================================
    // i114 — C7 termite gate + ant chain — RECLASSIFIED: HARNESS_LIMIT
    // ================================================================
    //
    // Test method (i114_ant_chain_dimensions) removed in the 2026-08-10
    // gametest triage. Checklist item i114-c7-termite-gate-ant-chain-wgen-049
    // (WGEN-026/049) returns to MANUAL_ONLY: the GameTestServer NEVER
    // instantiates datapack dimensions. GameTestServer.create builds its
    // WorldDimensions from the FLAT world preset baked against a freshly
    // created EMPTY LevelStem registry (decompiled 1.21.1
    // GameTestServer.java:97-103, build/neoform .../steps/patch/outputs.jar),
    // so only minecraft:overworld / the_nether / the_end exist and
    // server.getLevel(EntityAnt.UTOPIA|MINING|ISLANDS|CRYSTAL) returns null
    // (run log: "all four ant-chain dimensions must exist on the test server";
    // the shutdown save lists only the three vanilla dimensions). Every hop
    // assertion needs a live destination ServerLevel, so the chain cannot be
    // exercised here. The termite entry-gate refusals (orig Termite.java:
    // 96-107) are the only sub-checks that do not touch a destination level;
    // they go MANUAL_ONLY with the rest of the item (verify in a real client:
    // Ant→Utopia, RedAnt→Mining, UnstableAnt→Islands, Termite→Crystal with
    // empty-inventory/no-armor gates, 80-tick portal cooldown, same-ant
    // return trips, following-wolf travels / sitting-wolf stays).

    // ================================================================
    // i123 — maze Basilisks are persistent
    // ================================================================

    /**
     * Checklist item i123-walk-130-blocks-from-the-maze-chamber-and-return-
     * the-3-bas (WGEN-037 remainder). The Basilisk Maze spawns 3 persistent
     * Basilisks in its 30×30 chamber — no spawner blocks
     * (BasiliskMazeGenerator.java:26, 432-437: spawnPersistent at castle
     * (x+45+i, y+1.01, z+15) = origin + (48+i, −depth−3, −5), depth 20-29;
     * LegacyDungeonPiece.spawnPersistent sets setPersistenceRequired(),
     * LegacyDungeonPiece.java:646-655). Walking &gt;130 blocks away exercises
     * vanilla Mob.checkDespawn, which early-returns for persistence-required
     * mobs before any distance check (decompiled 1.21.1 Mob.java:703-715), so
     * the test asserts the flag on all three and drives checkDespawn directly
     * with every player &gt;128 blocks away (nearest players sit at the test
     * grid, ~50000 blocks from this build).
     *
     * <p>Far-region build: batch K base 500, uniqueK = 520 → offset
     * (+266240, 0, +50000) from the test origin, within the world border.
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void i123_maze_basilisk_persistence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + 520 * 512, 60, base.getZ() + 50000);
        LegacyDungeonPiece.buildNow(level, origin, LegacyDungeonPiece.DungeonType.BASILISK_MAZE);

        // Basilisk chamber spawn window: x origin+48..50, y origin-32..-23, z origin-5.
        AABB chamber = new AABB(
                origin.getX() + 40, origin.getY() - 40, origin.getZ() - 15,
                origin.getX() + 60, origin.getY() - 15, origin.getZ() + 5);
        List<Basilisk> basilisks = level.getEntitiesOfClass(Basilisk.class, chamber);
        check(basilisks.size() == 3, "the maze chamber must contain exactly 3 Basilisks, found " + basilisks.size());
        for (Basilisk basilisk : basilisks) {
            check(basilisk.isPersistenceRequired(),
                    "maze Basilisks must be persistence-required (LegacyDungeonPiece.java:652)");
            basilisk.checkDespawn(); // no player anywhere near: any that exist sit ~50000 blocks away, far beyond the 128 hard-despawn radius
            check(!basilisk.isRemoved(), "a persistent Basilisk must survive checkDespawn with no player in range");
            check(basilisk.isAlive(), "Basilisk must still be alive after the despawn check");
        }
        for (Basilisk basilisk : basilisks) basilisk.discard();
        helper.succeed();
    }
}
