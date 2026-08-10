package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRat;
import danger.orespawn.entity.Godzilla;
import danger.orespawn.entity.ThePrince;
import danger.orespawn.entity.ThePrinceTeen;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Crash-regression tests (batch categories: crash).
 *
 * <p>Findings covered: BUG-003 (EntityRat {@code UUID.fromString("")}
 * ticking-entity crash, AUDIT_FINDINGS.md:5130-5136 / FIX_LOG.md "BUG-003 —
 * FIXED"), BUG-004 (Prince growth chain {@code tame(null)} NPE with the owner
 * offline, AUDIT_FINDINGS.md:5138-5142 / FIX_LOG.md "BUG-004 — FIXED"), and
 * BUG-006 (Godzilla jump-landing damage bypassing Creative/Spectator
 * invulnerability, AUDIT_FINDINGS.md:5154-5158 / FIX_LOG.md "BUG-006 —
 * FIXED").</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CrashReproTests {

    /** Stone floor + 3-high perimeter walls so wandering test mobs stay inside their own structure. */
    private static void buildPen(GameTestHelper helper, int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                boolean wall = x == minX || x == maxX || z == minZ || z == maxZ;
                for (int y = 2; y <= 4 && wall; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
    }

    /**
     * Checklist item i001-bug-003 (BUG-003): summoned/spawner rats must tick
     * their AI without crashing the server and must despawn normally when the
     * nearest player is beyond 128 blocks.
     *
     * <p>Crash half: the original defect was {@code UUID.fromString("")} in
     * {@code customServerAiStep} when the rat's NBT carried no owner
     * (orig-port EntityRat.java:139 pre-fix; fixed port EntityRat.java:41-47,
     * 122-139). Two rats cover both historic inputs: a plain summoned rat (no
     * {@code MyOwner} tag) and a rat whose save data carries the legacy
     * string-form {@code MyOwner: ""} exactly as 1.7.10-era saves and
     * spawner-deserialized rats did. 220 ticks of live AI with no crash and
     * both rats alive is the regression assert.</p>
     *
     * <p>Despawn half: {@code Mob.checkDespawn} (verified against the
     * decompiled 1.21.1 source: discard when the nearest player is beyond the
     * MONSTER-category 128-block despawn distance and
     * {@code removeWhenFarAway} agrees — EntityRat.java:88-91 returns true for
     * ownerless rats; the original bug additionally made such rats never
     * despawn). A real {@code ServerPlayer} (the vanilla mock-server-player
     * test hook) is placed 300 blocks out, {@code checkDespawn()} is invoked
     * directly, and the player is removed again — all synchronously inside one
     * server-thread slice, so no concurrently scheduled test can ever observe
     * the temporary player.</p>
     */
    @SuppressWarnings({"deprecation", "removal"})
    @GameTest(template = "empty_large", timeoutTicks = 400)
    public void bug003_rat_ai_ticks_and_despawns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildPen(helper, 20, 20, 28, 28);
        Vec3 center = helper.absoluteVec(new Vec3(24.5, 2.0, 24.5));

        EntityRat plainRat = ModEntities.ENTITY_RAT.get().create(level);
        helper.assertTrue(plainRat != null, "rat create failed");
        plainRat.moveTo(center.x, center.y, center.z, 0.0f, 0.0f);

        EntityRat legacyRat = ModEntities.ENTITY_RAT.get().create(level);
        helper.assertTrue(legacyRat != null, "rat create failed");
        CompoundTag legacyTag = new CompoundTag();
        legacyRat.saveWithoutId(legacyTag);
        legacyTag.putString("MyOwner", ""); // the exact legacy shape that crashed (BUG-003)
        legacyRat.load(legacyTag);
        legacyRat.moveTo(center.x, center.y, center.z + 1.0, 0.0f, 0.0f);

        level.addFreshEntity(plainRat);
        level.addFreshEntity(legacyRat);

        helper.runAfterDelay(220, () -> {
            helper.assertTrue(plainRat.isAlive() && !plainRat.isRemoved(),
                    "plain summoned rat vanished during AI ticking (BUG-003)");
            helper.assertTrue(legacyRat.isAlive() && !legacyRat.isRemoved(),
                    "legacy empty-owner rat vanished during AI ticking (BUG-003)");

            ServerPlayer farPlayer = helper.makeMockServerPlayerInLevel();
            try {
                farPlayer.setPos(center.x + 300.0, center.y, center.z); // > 128-block MONSTER despawn distance
                plainRat.checkDespawn();
                legacyRat.checkDespawn();
                helper.assertTrue(plainRat.isRemoved(),
                        "unowned rat did not despawn with the nearest player 300 blocks away (BUG-003)");
                helper.assertTrue(legacyRat.isRemoved(),
                        "legacy empty-owner rat did not despawn (the original bug made these rats immortal, BUG-003)");
            } finally {
                level.getServer().getPlayerList().remove(farPlayer);
            }
            helper.succeed();
        });
    }

    /**
     * Checklist item i097-bug-006 (BUG-006): Godzilla's jump-landing shockwave
     * hurts Survival players but NOT Creative or Spectator players.
     *
     * <p>Documented mechanism: the pre-fix port dealt the landing damage with
     * {@code genericKill} (the /kill source, tagged BYPASSES_INVULNERABILITY),
     * killing Creative/Spectator players; the fix routes it as half
     * unattributed-explosion, half fall damage exactly like orig
     * Godzilla.java:509-512 (port Godzilla.java:399-421, FIX_LOG.md BUG-006).
     * The landing branch itself only feeds positions/damage into the private
     * {@code doJumpDamage(x, y, z, dist, damage, knock)}
     * (Godzilla.java:573-583 calls it with the 25-block/37.5-damage outer
     * ring, :581), which is invoked here directly via reflection so no live
     * Godzilla AI (block crushing, targeting) runs inside the shared grid.</p>
     *
     * <p>The three mock players get their abilities from
     * {@code GameType.updatePlayerAbilities} — the exact flag set a real
     * gamemode switch applies ({@code abilities.invulnerable} is what
     * {@code Player.hurt} consults, verified against the decompiled 1.21.1
     * source). Everything runs synchronously and every entity is discarded
     * before the method returns, so nothing ever ticks.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 100)
    public void bug006_godzilla_shockwave_respects_gamemode_invulnerability(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 center = helper.absoluteVec(new Vec3(24.5, 2.0, 24.5));

        Godzilla godzilla = ModEntities.GODZILLA.get().create(level);
        helper.assertTrue(godzilla != null, "Godzilla create failed");
        godzilla.moveTo(center.x, center.y, center.z, 0.0f, 0.0f);

        Player survival = helper.makeMockPlayer(GameType.SURVIVAL);
        survival.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        survival.setHealth(1000.0f);
        survival.setPos(center.x + 20.0, center.y, center.z);

        Player creative = helper.makeMockPlayer(GameType.CREATIVE);
        GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
        creative.setPos(center.x - 20.0, center.y, center.z);

        Player spectator = helper.makeMockPlayer(GameType.SPECTATOR);
        GameType.SPECTATOR.updatePlayerAbilities(spectator.getAbilities());
        spectator.setPos(center.x, center.y, center.z + 20.0);

        level.addFreshEntity(survival);
        level.addFreshEntity(creative);
        level.addFreshEntity(spectator);

        float survivalBefore = survival.getHealth();
        float creativeBefore = creative.getHealth();
        float spectatorBefore = spectator.getHealth();
        try {
            Method doJumpDamage = Godzilla.class.getDeclaredMethod("doJumpDamage",
                    double.class, double.class, double.class, double.class, double.class, int.class);
            doJumpDamage.setAccessible(true);
            // The outer landing ring: dist 25, damage 37.5, no knockback (Godzilla.java:581).
            doJumpDamage.invoke(godzilla, center.x, center.y, center.z, 25.0, 37.5, 0);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("reflection into Godzilla.doJumpDamage failed", e);
        }

        double survivalDelta = survivalBefore - survival.getHealth();
        double creativeDelta = creativeBefore - creative.getHealth();
        double spectatorDelta = spectatorBefore - spectator.getHealth();

        godzilla.discard();
        survival.discard();
        creative.discard();
        spectator.discard();

        helper.assertTrue(survivalDelta > 0.0 && survivalDelta <= 100.0,
                "Survival player not hit by the landing shockwave (delta=" + survivalDelta + ", expected ~18.75)");
        helper.assertValueEqual(creativeDelta, 0.0,
                "BUG-006: Creative player damaged by the landing shockwave");
        helper.assertValueEqual(spectatorDelta, 0.0,
                "BUG-006: Spectator player damaged by the landing shockwave");
        helper.succeed();
    }

    /** Diamond-block interact shared by both BUG-004 tests. */
    private static Player tamePrinceWithDiamondBlock(GameTestHelper helper, ThePrince prince) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 ownerPos = helper.absoluteVec(new Vec3(26.5, 2.0, 24.5));
        owner.setPos(ownerPos.x, ownerPos.y, ownerPos.z);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.DIAMOND_BLOCK));
        InteractionResult result = prince.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.consumesAction(), "diamond-block interact was not consumed");
        helper.assertTrue(prince.isTame(), "prince not tamed by the diamond block");
        helper.assertTrue(owner.getUUID().equals(prince.getOwnerUUID()), "prince owner UUID not set on tame");
        return owner;
    }

    /**
     * Checklist item i002-bug-004, DOCUMENTED-flow half. EXPECTED TO FAIL.
     *
     * <p>The documented flow (items.json i002 / FIX_LOG.md "Pending manual
     * tests" BUG-004: "Tame a Prince (diamond block), give diamond to grow
     * baby→teen→adult") implies the diamond-block tame leaves a BABY prince
     * that waits for a separate diamond before growing. The user manually
     * observed the port transforming INSTANTLY on the diamond-block tame.
     * This test asserts the documented two-step behavior and is expected to
     * fail, pinning the observation.</p>
     *
     * <p>Why it fails (source conflict, recorded for triage): the diamond-block
     * tame sets kill/fed/day counters to 1000 (port ThePrince.java:583-595),
     * and the counter-driven natural-growth check
     * ({@code killCount > 25 && fedCount > 10 && dayCount > 10}, port
     * ThePrince.java:286-291) transforms on the very next AI tick. The 1.7.10
     * original does the SAME (orig ThePrince.java:195-206 sets the counters to
     * 1000; orig :556-568 transforms on the counters alone) — i.e. the
     * documented two-step expectation contradicts both the port and the
     * original source. The failure is the deliverable; resolution (fix docs vs
     * fix behavior) belongs to the maintainer.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void bug004_documented_diamond_block_tame_keeps_baby(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ThePrince prince = helper.spawn(ModEntities.THE_PRINCE.get(), new BlockPos(24, 2, 24));
        tamePrinceWithDiamondBlock(helper, prince);
        Vec3 center = helper.absoluteVec(new Vec3(24.5, 2.0, 24.5));

        helper.runAfterDelay(20, () -> {
            boolean stillBaby = !prince.isRemoved();
            // Clean up any teen the (undocumented) instant transform produced
            // BEFORE failing, so the shared grid is not polluted.
            AABB sweep = new AABB(center, center).inflate(20.0, 20.0, 20.0);
            List<ThePrinceTeen> teens = level.getEntitiesOfClass(ThePrinceTeen.class, sweep);
            teens.forEach(Entity::discard);
            prince.discard();
            helper.assertTrue(stillBaby,
                    "EXPECTED FAIL (documented BUG-004 flow): the prince transformed instantly on the "
                            + "diamond-block tame instead of staying a baby until fed a diamond "
                            + "(counters set to 1000 by ThePrince.java:583-595 trip the growth check at :286-291; "
                            + "orig ThePrince.java:195-206/:556 behaves identically — docs and source disagree)");
            helper.succeed();
        });
    }

    /**
     * Checklist item i002-bug-004, crash/persistence half (the automatable
     * core of the finding): the growth transformation with the owner OFFLINE
     * must complete without an NPE and carry ownership over by UUID.
     *
     * <p>Documented mechanism: pre-fix, {@code transformToTeen} called
     * {@code tame(getPlayerByUUID(owner))} and NPE'd the server when the owner
     * was offline (AUDIT_FINDINGS BUG-004, orig-port ThePrince.java:241). The
     * fix (FIX_LOG.md BUG-004; port ThePrince.java:536-554) null-checks and
     * falls back to {@code setOwnerUUID(...) + setTame(true, true)}. A
     * gametest mock player is never in {@code ServerLevel.players()}, so
     * {@code getPlayerByUUID} returns null — the owner is genuinely "offline"
     * the moment the counters trip, which is exactly the crash window. The
     * server surviving the transform plus the teen keeping tame+owner is the
     * regression assert.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void bug004_growth_transform_owner_offline_keeps_ownership(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ThePrince prince = helper.spawn(ModEntities.THE_PRINCE.get(), new BlockPos(24, 2, 24));
        Player owner = tamePrinceWithDiamondBlock(helper, prince);
        UUID ownerId = owner.getUUID();
        Vec3 center = helper.absoluteVec(new Vec3(24.5, 2.0, 24.5));
        AABB searchBox = new AABB(center, center).inflate(40.0, 60.0, 40.0);

        boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) {
                return;
            }
            List<ThePrinceTeen> teens = level.getEntitiesOfClass(ThePrinceTeen.class, searchBox);
            if (teens.isEmpty()) {
                return;
            }
            done[0] = true;
            ThePrinceTeen teen = teens.get(0);
            boolean tame = teen.isTame();
            UUID teenOwner = teen.getOwnerUUID();
            teens.forEach(Entity::discard);
            prince.discard();
            helper.assertTrue(tame, "teen lost its tame flag in the owner-offline transform (BUG-004)");
            helper.assertTrue(ownerId.equals(teenOwner),
                    "teen lost the owner UUID in the owner-offline transform (BUG-004): " + teenOwner);
            helper.succeed();
        });
    }
}
