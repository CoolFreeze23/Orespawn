package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BetterFireball;
import danger.orespawn.entity.IrukandjiArrow;
import danger.orespawn.entity.UltimateArrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * ENT-S-098, owner ruling 2026-09-03: "fix the shot fireball's type, with a gametest
 * that a fired BetterFireball keeps its type across a save/load round trip."
 *
 * <p>Every shooter (17 sites: Mothra, Godzilla, TheKing, TheQueen, Dragon, ThePrince,
 * ThePrinceTeen, ThePrinceAdult, ThePrincess, EntityBrutalfly) builds its shot as
 * {@code new BetterFireball(level, this, accel)}, then sets the muzzle position and
 * the flags (setNotMe / setBig / setReallyBig / setSmall) and addFreshEntity's it.
 * Until the fix that constructor chained to {@code LargeFireball(level, shooter,
 * movement, 1)} = {@code super(EntityType.FIREBALL, ...)}, so a shot was typed
 * {@code minecraft:fireball}: the {@code better_fireball} registration governed only
 * {@code EntityType#create} instances, clients rebuilt shots as plain LargeFireballs,
 * and NBT saved them under the vanilla id. These tests build the shot exactly as a
 * shooter does (the constructor is the single path; the shooter's class does not
 * enter it, so a frozen vanilla cow stands in for the boss) and pin:</p>
 *
 * <ul>
 *   <li>the shot's {@code getType()} is {@code ModEntities.BETTER_FIREBALL} and its
 *   {@code getEncodeId()} (what {@code Entity#save} writes as {@code "id"}) is
 *   {@code orespawn:better_fireball};</li>
 *   <li>the kinematics are the vanilla chain's, checked differentially against a
 *   control {@code new LargeFireball(level, shooter, aim, 1)} built from the same
 *   shooter: shooter-feet position, {@code aim/|aim| * 0.1} delta movement
 *   ({@code AbstractHurtingProjectile.INITAL_ACCELERATION_POWER}, orig
 *   BetterFireball.java:64-67), hasImpulse, the shooter's rotation, the owner;</li>
 *   <li>the round trip: {@code Entity#save} -> a tag whose {@code "id"} resolves through
 *   {@code EntityType#by} to the mod type -> {@code EntityType#loadEntityRecursive}
 *   (the chunk-storage path: {@code by(tag).create(level).load(tag)}) gives a
 *   BetterFireball of the same type, UUID, motion, owner and explosion power, which
 *   re-saves under the same id and re-enters the ServerLevel once the original has
 *   left;</li>
 *   <li>the small path: setSmall() still shrinks a shooter-built shot to 0.3125
 *   (ENT-S-095), and the reloaded copy comes back at the registered 1x1 box. The
 *   small flag is NOT persisted, faithfully: orig BetterFireball.java:272-275 writes
 *   only ExplosionPower and the World constructor (:46-49) re-sizes a reloaded
 *   fireball to 1x1, so only the type and the power are pinned as surviving.</li>
 * </ul>
 *
 * <p>ENT-S-102, owner ruling 2026-09-04 ("fix with a test"): {@code BetterFireball.onHit}
 * used to chain to {@code LargeFireball.onHit}, which explodes at vanilla's private power 1
 * (sourced by the fireball itself) and discards, and then exploded again at the port's own
 * 1 / 2 / 4 when not small, so a big shot detonated twice per impact and a small shot, which
 * orig BetterFireball.java:265-267 never exploded, still got the vanilla blast. The two
 * {@code s102_} impact tests fire a shooter-built shot into an obsidian wall and count the
 * explosions started inside the structure through a temporary {@code ExplosionEvent.Start}
 * listener: a big shot yields exactly one, at the port's power with the orig :266 null
 * source; a small shot yields none; both are discarded (orig :268).</p>
 *
 * <p>ENT-S-103 and ENT-S-104, owner ruling 2026-09-04 ("ENT-S-103 through 107: all parity
 * bugs, fix in classic"; 104 "restores 1.7.10 fire behavior and files a MOD proposal for a
 * config-gated 'fire respects mobGriefing' option", MOD-031). The {@code s103_} test fires two
 * ultimate arrows, the bow's own Punch 2 bake against the same bake with Punch stripped, into
 * frozen cows and reads the 1.7.10 push off their velocity. The three {@code s104_} tests fire
 * into the same obsidian wall as ENT-S-102, now over a dirt hearth: a small shot leaves fire on
 * the air side of the hit face (orig :232-264, with no explosion to disturb it); a big shot with
 * mobGriefing on (asserted; the default) explodes once, fire-flagged, destroying the hearth; a
 * big shot with mobGriefing off (flipped for its own window only, after the batch-mates'
 * windows have closed, restored in a finally) explodes once, fire-flagged, with KEEP block
 * interaction, wall and hearth intact and the face fire still standing.</p>
 *
 * <p>ENT-S-111, owner ruling 2026-09-04 ("ENT-S-108 through 113: all parity, fix in classic"). orig
 * IrukandjiArrow.java:181 gated the Punch push on {@code instanceof EntityLiving} (the 1.7.10 AI-mob
 * base, 1.21.1 {@code Mob}), so a player -- an EntityLivingBase only -- was never pushed; the port
 * pushed any LivingEntity. The {@code s111_} test fires two irukandji arrows from Punch-2 skate bows
 * on the s103 geometry, one into a frozen cow and one into a survival mock player, with
 * {@code ultimateSwordPvp} raised for its window so the arrow hurts the player at all
 * (IrukandjiArrow.onHitEntity no-sells players while it is off), and reads the pushes off their
 * velocities: the cow carries the 1.7.10 push over the vanilla 0.4 hurt knockback, the player the
 * 0.4 alone.</p>
 *
 * <p>Projectile-tag rulings (owner, 2026-09-04), pinned by the two {@code tags_} tests: the
 * ultimate and irukandji arrows stay outside {@code #minecraft:arrows} because the 1.7.10
 * bows never applied Power to them (the check is quoted on the test), and the
 * ThrowableProjectile family joins {@code #minecraft:impact_projectiles} as vanilla-consistent
 * behaviour with no parity obligation (MOD-030), BerthaHit and EntityCage excepted.</p>
 *
 * <p>The one config the class flips is {@code ultimateSwordPvp}, raised by the ENT-S-111 test for
 * its own window and restored on every path; no batch-mate reads it (the only other arrow test
 * shoots cows, which the pvp guard never touches). The class declares its own batch (TEST-003: new
 * test classes never join the default batch, whose 50-test buckets reshuffle); every ENT-S-098 and
 * tag test is synchronous in one tick, the ENT-S-102 and ENT-S-104 impact tests wait a fixed
 * 40-tick window ({@code runAfterDelay}; the mobGriefing-off test first waits 60 ticks for its
 * batch-mates' windows to close, timeoutTicks 200), the ENT-S-103 and ENT-S-111 arrow tests a
 * 10-tick one, and all discard what they spawned in a finally. Template {@code empty_large}
 * (48x16x48) with the shooter at (24, 8, 24), as HitboxDimsParityTests.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
// Own batch (TEST-003): new test classes never join the default batch, whose 50-test buckets reshuffle.
public class ProjectileTypeParityTests {

    private static final String BETTER_FIREBALL_ID = "orespawn:better_fireball";
    private static final String VANILLA_FIREBALL_ID = "minecraft:fireball";
    /** empty_large is 48x16x48; the shooter stands at (24, 8, 24) like the HitboxDimsParityTests spawns. */
    private static final BlockPos POS = new BlockPos(24, 8, 24);
    /** A non-unit, off-axis aim so the normalise-and-scale of the movement is observable. */
    private static final Vec3 AIM = new Vec3(3.0, 1.5, -2.0);
    /** A non-trivial shooter rotation so the setRot copy is observable (setRot stores yRot % 360, xRot % 360). */
    private static final float SHOOTER_Y_ROT = 37.0f;
    private static final float SHOOTER_X_ROT = -12.0f;
    private static final double EPS = 1e-9;
    private static final float DIM_EPS = 1e-4f;
    /** orig BetterFireball.java:42 field_92012_e = 1 default; :78-80 setReallyBig = 4. */
    private static final int DEFAULT_POWER = 1;
    private static final int REALLY_BIG_POWER = 4;

    /** A frozen vanilla cow at a template position: no goals (spawnWithNoFreeWill), NoAI, persistent, the fixed rotation. */
    private static Cow spawnFrozenCow(GameTestHelper helper, BlockPos pos) {
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, pos);
        cow.setNoAi(true);
        cow.setPersistenceRequired();
        cow.setYRot(SHOOTER_Y_ROT);
        cow.setXRot(SHOOTER_X_ROT);
        return cow;
    }

    /** A frozen vanilla cow: the shooter constructor reads only LivingEntity position and rotation. */
    private static LivingEntity spawnShooter(GameTestHelper helper) {
        return spawnFrozenCow(helper, POS);
    }

    /** The tail every shooter site runs after the constructor and the flags: a muzzle setPos, then addFreshEntity. */
    private static void launch(GameTestHelper helper, LivingEntity shooter, BetterFireball shot) {
        shot.setPos(shooter.getX(), shooter.getY() + 1.0, shooter.getZ() + 2.0);
        helper.assertTrue(helper.getLevel().addFreshEntity(shot),
                "ServerLevel#addFreshEntity refused the shot BetterFireball (ENT-S-098)");
    }

    private static void assertVecEqual(GameTestHelper helper, Vec3 actual, Vec3 expected, String what) {
        helper.assertTrue(actual.distanceTo(expected) < EPS,
                what + ": expected " + expected + ", got " + actual + " (ENT-S-098)");
    }

    private static void assertDims(GameTestHelper helper, Entity entity, float width, float height, String what) {
        AABB box = entity.getBoundingBox();
        helper.assertTrue(Math.abs(entity.getBbWidth() - width) < DIM_EPS
                        && Math.abs(entity.getBbHeight() - height) < DIM_EPS,
                what + " must be " + width + "x" + height + ", got " + entity.getBbWidth() + "x"
                        + entity.getBbHeight() + " (ENT-S-098 / ENT-S-095)");
        helper.assertTrue(Math.abs(box.getXsize() - width) < DIM_EPS
                        && Math.abs(box.getYsize() - height) < DIM_EPS
                        && Math.abs(box.getZsize() - width) < DIM_EPS,
                what + " bounding box not " + width + "x" + height + "x" + width + ", got "
                        + box.getXsize() + "x" + box.getYsize() + "x" + box.getZsize() + " (ENT-S-098 / ENT-S-095)");
    }

    private static void assertModTyped(GameTestHelper helper, Entity entity, String what) {
        helper.assertTrue(entity instanceof BetterFireball,
                what + " is not a BetterFireball but " + entity.getClass().getName() + " (ENT-S-098)");
        helper.assertTrue(entity.getType() == ModEntities.BETTER_FIREBALL.get(),
                what + " must carry ModEntities.BETTER_FIREBALL, got " + EntityType.getKey(entity.getType()) + " (ENT-S-098)");
        helper.assertTrue(entity.getType() != EntityType.FIREBALL,
                what + " still carries the vanilla EntityType.FIREBALL (ENT-S-098)");
        helper.assertValueEqual(EntityType.getKey(entity.getType()).toString(), BETTER_FIREBALL_ID,
                what + " type registry key (ENT-S-098)");
        helper.assertValueEqual(entity.getEncodeId(), BETTER_FIREBALL_ID,
                what + " Entity#getEncodeId, the NBT \"id\" Entity#save writes (ENT-S-098)");
    }

    /**
     * A shooter-built shot carries the mod's own EntityType, and its kinematics are the
     * vanilla LargeFireball(level, shooter, movement, 1) chain's it replaced, checked
     * differentially against a control built from the same shooter and aim: position
     * (the shooter's feet, AbstractHurtingProjectile.java:47), delta movement
     * (aim/|aim| * 0.1, :197-200; orig BetterFireball.java:64-67), accelerationPower,
     * hasImpulse, rotation (setRot from the shooter, :49) and owner (:48). The registered
     * 1x1 box (ENT-S-095) now governs the shot, and the ServerLevel accepts and indexes it.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s098_shot_better_fireball_carries_mod_type_with_vanilla_kinematics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, AIM);
        LargeFireball control = new LargeFireball(level, shooter, AIM, 1);
        try {
            assertModTyped(helper, shot, "a shooter-built BetterFireball");
            helper.assertTrue(control.getType() == EntityType.FIREBALL,
                    "control: the vanilla LargeFireball shooter chain must still type minecraft:fireball (ENT-S-098)");
            helper.assertValueEqual(control.getEncodeId(), VANILLA_FIREBALL_ID, "control LargeFireball encode id (ENT-S-098)");

            assertVecEqual(helper, shot.position(), control.position(), "shot position vs the vanilla chain");
            assertVecEqual(helper, shot.position(), shooter.position(),
                    "shot position vs the shooter's feet (AbstractHurtingProjectile.java:47, orig :58-59)");
            assertVecEqual(helper, shot.getDeltaMovement(), control.getDeltaMovement(), "shot delta movement vs the vanilla chain");
            assertVecEqual(helper, shot.getDeltaMovement(),
                    AIM.normalize().scale(AbstractHurtingProjectile.INITAL_ACCELERATION_POWER),
                    "shot delta movement = aim/|aim| * 0.1 (AbstractHurtingProjectile.java:197-200, orig :64-67)");
            helper.assertTrue(shot.accelerationPower == control.accelerationPower
                            && shot.accelerationPower == AbstractHurtingProjectile.INITAL_ACCELERATION_POWER,
                    "shot accelerationPower must be the vanilla 0.1, got " + shot.accelerationPower + " (ENT-S-098)");
            helper.assertTrue(shot.hasImpulse && control.hasImpulse,
                    "shot hasImpulse must be set as assignDirectionalMovement sets it (ENT-S-098)");
            helper.assertTrue(shot.getYRot() == control.getYRot() && shot.getXRot() == control.getXRot(),
                    "shot rotation " + shot.getYRot() + "/" + shot.getXRot() + " differs from the vanilla chain's "
                            + control.getYRot() + "/" + control.getXRot() + " (ENT-S-098)");
            helper.assertTrue(shot.getYRot() == SHOOTER_Y_ROT % 360.0f && shot.getXRot() == SHOOTER_X_ROT % 360.0f,
                    "shot rotation must be the shooter's (setRot, AbstractHurtingProjectile.java:49), got "
                            + shot.getYRot() + "/" + shot.getXRot() + " (ENT-S-098)");
            helper.assertTrue(shot.yRotO == control.yRotO && shot.xRotO == control.xRotO,
                    "shot previous-tick rotation differs from the vanilla chain's (ENT-S-098)");
            helper.assertTrue(shot.getOwner() == shooter && control.getOwner() == shooter,
                    "shot owner must be the shooter (AbstractHurtingProjectile.java:48) (ENT-S-098)");

            assertDims(helper, shot, 1.0f, 1.0f, "a shooter-built BetterFireball before setSmall");

            launch(helper, shooter, shot);
            helper.assertTrue(level.getEntity(shot.getUUID()) == shot,
                    "the ServerLevel must index the added shot under its UUID (ENT-S-098)");
            assertModTyped(helper, level.getEntity(shot.getUUID()), "the shot as the ServerLevel indexes it");
        } finally {
            shot.discard();
            shooter.discard();
        }
        helper.succeed();
    }

    /**
     * The owner-ruled pin: a fired BetterFireball keeps its type across a save/load round
     * trip. Entity#save writes "id" = getEncodeId() (Entity.java:1709-1722), which
     * EntityType#by resolves back to the registered type; EntityType#loadEntityRecursive
     * is the chunk-storage path (by(tag).create(level).load(tag)). The shot is fired with
     * setReallyBig (power 4, orig :78-80; the Godzilla.java:533 / TheKing.java:1044 /
     * TheQueen.java:1276 path) so the persisted ExplosionPower (orig :272-281) is
     * observable, and with setNotMe as the boss sites do. After the original leaves the
     * level (freeing its UUID) the loaded copy re-enters and is indexed under that UUID.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s098_shot_better_fireball_keeps_type_and_power_across_save_load(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, AIM);
        Entity loaded = null;
        try {
            shot.setReallyBig();
            shot.setNotMe();
            launch(helper, shooter, shot);

            CompoundTag saved = new CompoundTag();
            helper.assertTrue(shot.save(saved),
                    "Entity#save of a live shot BetterFireball must succeed (false = removed or a non-serialisable type) (ENT-S-098)");
            helper.assertValueEqual(saved.getString("id"), BETTER_FIREBALL_ID, "NBT \"id\" of a shot BetterFireball (ENT-S-098)");
            helper.assertTrue(EntityType.by(saved).orElse(null) == ModEntities.BETTER_FIREBALL.get(),
                    "EntityType#by must resolve the saved id to ModEntities.BETTER_FIREBALL, got " + EntityType.by(saved) + " (ENT-S-098)");
            helper.assertValueEqual(saved.getInt("ExplosionPower"), REALLY_BIG_POWER,
                    "saved ExplosionPower after setReallyBig (orig BetterFireball.java:274) (ENT-S-098)");
            helper.assertTrue(saved.hasUUID("Owner") && shooter.getUUID().equals(saved.getUUID("Owner")),
                    "saved Owner must be the shooter's UUID (Projectile.java:71-73) (ENT-S-098)");

            loaded = EntityType.loadEntityRecursive(saved, level, e -> e);
            helper.assertTrue(loaded != null,
                    "EntityType#loadEntityRecursive must rebuild the shot from its NBT (null = unknown id or a load failure) (ENT-S-098)");
            assertModTyped(helper, loaded, "the shot reloaded from NBT");
            helper.assertTrue(shot.getUUID().equals(loaded.getUUID()), "the reloaded shot must keep its UUID (ENT-S-098)");
            assertVecEqual(helper, loaded.position(), shot.position(), "reloaded shot position (Pos)");
            assertVecEqual(helper, loaded.getDeltaMovement(), shot.getDeltaMovement(), "reloaded shot delta movement (Motion)");
            BetterFireball reloaded = (BetterFireball) loaded;
            helper.assertTrue(reloaded.accelerationPower == shot.accelerationPower,
                    "reloaded accelerationPower " + reloaded.accelerationPower + " != " + shot.accelerationPower + " (ENT-S-098)");
            helper.assertTrue(reloaded.getOwner() == shooter,
                    "the reloaded shot must resolve its Owner UUID back to the shooter through the ServerLevel (Projectile.java:54-63) (ENT-S-098)");

            CompoundTag resaved = new CompoundTag();
            helper.assertTrue(loaded.save(resaved), "Entity#save of the reloaded shot must succeed (ENT-S-098)");
            helper.assertValueEqual(resaved.getString("id"), BETTER_FIREBALL_ID, "NBT \"id\" of the reloaded shot (ENT-S-098)");
            helper.assertValueEqual(resaved.getInt("ExplosionPower"), REALLY_BIG_POWER,
                    "ExplosionPower of the reloaded shot: the port's power survives the round trip (orig :277-281) (ENT-S-098)");

            shot.discard();
            helper.assertTrue(level.addFreshEntity(loaded),
                    "ServerLevel#addFreshEntity refused the reloaded shot once the original had left (ENT-S-098)");
            helper.assertTrue(level.getEntity(shot.getUUID()) == loaded,
                    "the ServerLevel must index the reloaded shot under the original UUID (ENT-S-098)");
            assertModTyped(helper, level.getEntity(shot.getUUID()), "the reloaded shot as the ServerLevel indexes it");
        } finally {
            shot.discard();
            if (loaded != null) {
                loaded.discard();
            }
            shooter.discard();
        }
        helper.succeed();
    }

    /**
     * The small path through the shooter constructor (Dragon.java:495-496, ThePrince.java:490,
     * ThePrincess.java:622, Godzilla.java:547, TheKing.java:1058, TheQueen.java:1292):
     * setSmall() still shrinks a shooter-built shot to 0.3125x0.3125 (orig :84, ENT-S-095),
     * the shot saves and reloads as the mod type at the default power 1 (orig :42), and the
     * reloaded copy is the registered 1x1. The small flag is not persisted, faithfully:
     * orig BetterFireball.java:272-275 writes only ExplosionPower and the World constructor
     * (:46-49) re-sizes a reloaded fireball to 1x1, so a reloaded small shot is a full-size,
     * full-power fireball in 1.7.10 as well; only the type and the power are pinned as
     * surviving.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s098_small_shot_keeps_type_and_reloads_at_registered_box(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, AIM);
        Entity loaded = null;
        try {
            shot.setNotMe();
            shot.setSmall();
            launch(helper, shooter, shot);
            assertModTyped(helper, shot, "a shooter-built small BetterFireball");
            assertDims(helper, shot, 0.3125f, 0.3125f, "a shooter-built BetterFireball after setSmall (orig :84)");

            CompoundTag saved = new CompoundTag();
            helper.assertTrue(shot.save(saved), "Entity#save of a live small shot must succeed (ENT-S-098)");
            helper.assertValueEqual(saved.getString("id"), BETTER_FIREBALL_ID, "NBT \"id\" of a small shot (ENT-S-098)");
            helper.assertValueEqual(saved.getInt("ExplosionPower"), DEFAULT_POWER,
                    "saved ExplosionPower of a small shot: the default 1 (orig :42), setSmall does not touch it (ENT-S-098)");

            loaded = EntityType.loadEntityRecursive(saved, level, e -> e);
            helper.assertTrue(loaded != null, "EntityType#loadEntityRecursive must rebuild the small shot (ENT-S-098)");
            assertModTyped(helper, loaded, "the small shot reloaded from NBT");
            assertDims(helper, loaded, 1.0f, 1.0f,
                    "a reloaded small shot (the small flag is not persisted, orig :272-275; the registered 1x1 of orig :48 applies)");
        } finally {
            shot.discard();
            if (loaded != null) {
                loaded.discard();
            }
            shooter.discard();
        }
        helper.succeed();
    }

    /**
     * ENT-S-098 refuter: vanilla keys punch deflection, projectile-on-projectile hits and impact
     * block breaking on the ENTITY TYPE through two tags (minecraft:redirectable_projectile and
     * minecraft:impact_projectiles, both listing minecraft:fireball). Retyping the shot to the
     * mod's own type would silently drop all three, which both 1.7.10 (EntityFireball is
     * collidable and punch-redirected) and the pre-fix port had; the two overlay tags under
     * data/minecraft/tags/entity_type restore them and this test pins them on a fired shot.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s098_shot_better_fireball_keeps_vanilla_fireball_tags(GameTestHelper helper) {
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(helper.getLevel(), shooter, AIM);
        try {
            launch(helper, shooter, shot);
            helper.assertTrue(shot.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE),
                    "orespawn:better_fireball must carry minecraft:redirectable_projectile (vanilla lists minecraft:fireball; punch deflection, Player.attack) (ENT-S-098)");
            helper.assertTrue(shot.getType().is(EntityTypeTags.IMPACT_PROJECTILES),
                    "orespawn:better_fireball must carry minecraft:impact_projectiles (vanilla lists minecraft:fireball; Projectile.mayBreak) (ENT-S-098)");
            helper.assertTrue(shot.isPickable(),
                    "a shot BetterFireball must be pickable (Projectile.isPickable is the redirectable tag) so crosshair picks, punches and other projectiles reach it as in 1.7.10 (ENT-S-098)");
            helper.assertTrue(shot.canBeHitByProjectile(),
                    "a shot BetterFireball must be hittable by other projectiles (Entity.canBeHitByProjectile = alive && pickable) (ENT-S-098)");
            helper.succeed();
        } finally {
            shot.discard();
            shooter.discard();
        }
    }

    // ---------------------------------------------------------------------------------------
    // ENT-S-102 (owner ruling 2026-09-04: "fix with a test") -- one explosion per impact.
    // ---------------------------------------------------------------------------------------

    /** orig BetterFireball.java:74-76 setBig = 2, the power the impact test fires at. */
    private static final int BIG_POWER = 2;
    /**
     * The impact tests aim straight +z: AbstractHurtingProjectile has no gravity, launch() puts
     * the muzzle 2 blocks in front (+z) of the shooter at the block centre (24.5, 9.0, 26.5), and
     * the wall stands at WALL_Z, 3.5 blocks ahead of the muzzle.
     */
    private static final Vec3 WALL_AIM = new Vec3(0.0, 0.0, 1.0);
    private static final int WALL_Z = 30;
    /**
     * Ticks to wait before reading the impact. From the constructor's 0.1 blocks/tick the vanilla
     * acceleration (AbstractHurtingProjectile.tick: clip along v, move by v, then
     * v' = (v + 0.1 * v/|v|) * 0.95) travels 0.1, 0.19, 0.28, 0.36, 0.43, 0.51, 0.58, 0.64, 0.71
     * on successive ticks -- 3.08 cumulative after eight, so the ninth tick's clip crosses the
     * face at 3.5 and the blast sits about 0.4 short of it. 40 ticks is more than four times that
     * and well under the default 100-tick timeout; the discard at MAX_LIFETIME_TICKS (600) is far
     * outside the window, so a removed shot with a live owner can only mean an impact.
     */
    private static final int IMPACT_WINDOW_TICKS = 40;
    private static final double IMPACT_RADIUS_FROM_WALL_FACE = 2.0;

    /**
     * Counts the explosions started inside this test's structure while registered: one
     * {@code ExplosionEvent.Start} per {@code Level.explode} call (ServerLevel posts it before
     * the blast runs), filtered to this level and the structure bounds because same-batch tests
     * run concurrently in their own structures. Registered through the Class overload so the bus
     * needs no generic-type resolution; {@code unregister(listener)} drops exactly this consumer.
     */
    private static final class ExplosionCounter {
        private final ServerLevel level;
        private final AABB bounds;
        private final List<Explosion> seen = new ArrayList<>();
        private final Consumer<ExplosionEvent.Start> listener = this::onStart;
        private boolean registered;

        ExplosionCounter(GameTestHelper helper) {
            this.level = helper.getLevel();
            this.bounds = helper.getBounds();
            NeoForge.EVENT_BUS.addListener(ExplosionEvent.Start.class, this.listener);
            this.registered = true;
        }

        private void onStart(ExplosionEvent.Start event) {
            if (event.getLevel() == this.level && this.bounds.contains(event.getExplosion().center())) {
                this.seen.add(event.getExplosion());
            }
        }

        List<Explosion> seen() {
            return this.seen;
        }

        void close() {
            if (this.registered) {
                this.registered = false;
                NeoForge.EVENT_BUS.unregister(this.listener);
            }
        }
    }

    /**
     * An obsidian wall across the shot's line: relative x 22..26, y 7..11 at z = WALL_Z. Obsidian
     * (blast resistance 1200) stands under the power-2 blast, so the wall is exactly one impact
     * and no block breaks or drops muddy the count.
     */
    private static void buildWall(GameTestHelper helper) {
        for (int x = POS.getX() - 2; x <= POS.getX() + 2; x++) {
            for (int y = POS.getY() - 1; y <= POS.getY() + 3; y++) {
                helper.setBlock(new BlockPos(x, y, WALL_Z), Blocks.OBSIDIAN);
            }
        }
    }

    /**
     * Registers the counter, fires the shot as a shooter does (launch), and schedules the impact
     * checks IMPACT_WINDOW_TICKS later; the counter is unregistered and the entities discarded
     * in the check's finally, on the pass and the fail path alike (and right away if launch
     * itself fails). The counter goes in before launch: the shot's first tick comes after this
     * synchronous call returns, so no explosion can slip past it. The shooter is kept aloft
     * (no gravity) because, unlike the one-tick tests above, the window lets it fall; the
     * shot's owner must also stay alive, since AbstractHurtingProjectile.tick discards a shot
     * whose owner is removed.
     */
    private static void fireAtWallThenCheck(GameTestHelper helper, LivingEntity shooter, BetterFireball shot,
                                            Consumer<ExplosionCounter> impactChecks) {
        shooter.setNoGravity(true);
        ExplosionCounter explosions = new ExplosionCounter(helper);
        try {
            launch(helper, shooter, shot);
        } catch (RuntimeException e) {
            explosions.close();
            shot.discard();
            shooter.discard();
            throw e;
        }
        helper.runAfterDelay(IMPACT_WINDOW_TICKS, () -> {
            try {
                impactChecks.accept(explosions);
            } finally {
                explosions.close();
                shot.discard();
                shooter.discard();
            }
            helper.succeed();
        });
    }

    /**
     * The owner-ruled pin, big half: a big shot (setBig, power 2, orig :74-76; setNotMe as the
     * boss sites do) fired into the wall starts exactly ONE explosion inside the structure, and
     * it is the port's -- radius 2 with the orig :266 null source -- not LargeFireball.onHit's
     * vanilla blast (radius 1, sourced by the fireball itself), and the shot is discarded
     * (orig :268). Before the fix this counted two.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s102_big_shot_explodes_exactly_once_at_the_port_power_on_impact(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildWall(helper);
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, WALL_AIM);
        shot.setNotMe();
        shot.setBig();
        Vec3 wallFace = helper.absoluteVec(new Vec3(POS.getX() + 0.5, POS.getY() + 1.0, WALL_Z));
        fireAtWallThenCheck(helper, shooter, shot, explosions -> {
            helper.assertTrue(shot.isRemoved(),
                    "a big shot must be discarded after its wall impact (orig BetterFireball.java:268); still alive "
                            + IMPACT_WINDOW_TICKS + " ticks after launch (ENT-S-102)");
            helper.assertValueEqual(explosions.seen().size(), 1,
                    "explosions started inside the structure by one impact of a big shot (orig :265-267: one, at field_92012_e) (ENT-S-102)");
            Explosion only = explosions.seen().get(0);
            helper.assertTrue(Math.abs(only.radius() - BIG_POWER) < DIM_EPS,
                    "the one explosion must be the port's, at setBig's power " + BIG_POWER + " (orig :74-76); got radius "
                            + only.radius() + " (1 would be LargeFireball.onHit's vanilla blast) (ENT-S-102)");
            helper.assertTrue(only.getDirectSourceEntity() == null,
                    "the one explosion must carry orig :266's null source (LargeFireball.onHit's blast is sourced by the fireball itself); got "
                            + only.getDirectSourceEntity() + " (ENT-S-102)");
            helper.assertTrue(only.center().distanceTo(wallFace) < IMPACT_RADIUS_FROM_WALL_FACE,
                    "the explosion must sit at the shot's impact position by the wall face " + wallFace + ", got "
                            + only.center() + " (ENT-S-102)");
        });
    }

    /**
     * The owner-ruled pin, small half: a small shot (setSmall, the Dragon / Prince / Princess /
     * Godzilla / King / Queen path) fired into the wall starts NO explosion -- orig :265-267
     * explodes only when not small; before the fix the vanilla power-1 blast still went off --
     * and is discarded on impact all the same (orig :268).
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s102_small_shot_never_explodes_and_is_discarded_on_impact(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildWall(helper);
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, WALL_AIM);
        shot.setNotMe();
        shot.setSmall();
        fireAtWallThenCheck(helper, shooter, shot, explosions -> {
            helper.assertFalse(shooter.isRemoved(), "the shooter must outlive the 40-tick window, else the discard check below is vacuous");
            helper.assertTrue(shot.isRemoved(),
                    "a small shot must be discarded after its wall impact (orig BetterFireball.java:268); still alive "
                            + IMPACT_WINDOW_TICKS + " ticks after launch (ENT-S-102)");
            helper.assertValueEqual(explosions.seen().size(), 0,
                    "explosions started inside the structure by the impact of a small shot (orig :265-267: none when small; "
                            + "before the fix LargeFireball.onHit's power-1 blast still fired) (ENT-S-102)");
        });
    }

    // ---------------------------------------------------------------------------------------
    // ENT-S-104 (owner ruling 2026-09-04: parity bug, fix in classic) -- fire beside a block hit,
    // explosion fire independent of mobGriefing.
    // ---------------------------------------------------------------------------------------

    /**
     * The cell orig BetterFireball.java:232-264 sets fire in: the air side of the hit face. The
     * shot flies +z at feet y = 9.0 through the x = 24 column, so the clip (from the entity
     * position, ProjectileUtil.getHitResultOnMoveVector) traverses the y = 9 row and strikes the
     * wall block (24, 9, WALL_Z) on its north face; relative(NORTH) is (24, 9, WALL_Z - 1).
     */
    private static final BlockPos FIRE_CELL = new BlockPos(POS.getX(), POS.getY() + 1, WALL_Z - 1);
    /**
     * A dirt block under the fire cell. Fire survives only over a sturdy face or beside a
     * flammable neighbour (FireBlock.canSurvive; its scheduled tick, 30-39 ticks after placement,
     * removes it otherwise -- inside the 40-tick window), and the wall is obsidian over template
     * air, so the hearth is what keeps the pin independent of doFireTick (GameTestServer turns
     * it off; a client-side /test run leaves it on: over a sturdy base with no flammable
     * neighbour fire is removed only once its age passes 3, four ticks at 30+ apart). Dirt, not
     * obsidian, so it doubles as the block-interaction witness: the blast sits directly above it
     * (the shot stops about 0.4 short of the face at feet y = 9.0, the hearth's top face), so a
     * DESTROY blast removes it and a KEEP blast leaves it.
     */
    private static final BlockPos HEARTH = FIRE_CELL.below();
    /**
     * Ticks the mobGriefing-off test waits before it flips the rule and launches: the batch-mates
     * (both s102 tests, the other two s104 tests) read their impacts IMPACT_WINDOW_TICKS after a
     * common start, so by then their windows have closed and the rule they saw was the default.
     */
    private static final int MOB_GRIEFING_OFF_DELAY_TICKS = IMPACT_WINDOW_TICKS + 20;

    private static void buildWallAndHearth(GameTestHelper helper) {
        buildWall(helper);
        helper.setBlock(HEARTH, Blocks.DIRT);
    }

    private static void assertWallIntact(GameTestHelper helper, String why) {
        for (int x = POS.getX() - 2; x <= POS.getX() + 2; x++) {
            for (int y = POS.getY() - 1; y <= POS.getY() + 3; y++) {
                BlockPos pos = new BlockPos(x, y, WALL_Z);
                helper.assertTrue(helper.getBlockState(pos).is(Blocks.OBSIDIAN),
                        "wall block " + pos + " is " + helper.getBlockState(pos) + why);
            }
        }
    }

    /**
     * 1.21.1 Explosion keeps its fire flag in {@code private final boolean fire} with no getter
     * (bytecode: radius(), center(), getBlockInteraction(), interactsWithBlocks(), getToBlow()
     * ... are the public surface); finalizeExplosion reads it to seed random fire (1-in-3 per
     * affected air cell over a solid-render block), which cannot be observed deterministically,
     * so the flag itself is read -- as KrakenHoldReleaseTests reads Entity.random.
     */
    private static boolean explosionFire(Explosion explosion) {
        try {
            Field fire = Explosion.class.getDeclaredField("fire");
            fire.setAccessible(true);
            return fire.getBoolean(explosion);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Explosion.fire is not reachable by reflection (1.21.1: private final boolean fire; official names at runtime) (ENT-S-104)", e);
        }
    }

    /**
     * orig BetterFireball.java:232-264: a block hit sets fire on the air side of the hit face, for
     * every shot (the small gate at :265 covers only the explosion) and under no mobGriefing
     * condition. A small shot pins the placement in isolation: no explosion (orig :265-267,
     * ENT-S-102) can blow the fire away or seed random fire of its own, and the rule's value
     * during the window is immaterial, orig having no such gate. Before the fix the port had no
     * onHitBlock override and placed nothing.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s104_small_shot_leaves_fire_on_the_air_side_of_the_hit_face(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildWallAndHearth(helper);
        helper.assertTrue(level.isEmptyBlock(helper.absolutePos(FIRE_CELL)),
                "precondition: the fire cell " + FIRE_CELL + " must start as air");
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, WALL_AIM);
        shot.setNotMe();
        shot.setSmall();
        fireAtWallThenCheck(helper, shooter, shot, explosions -> {
            helper.assertFalse(shooter.isRemoved(), "the shooter must outlive the window, else the discard check below is vacuous");
            helper.assertTrue(shot.isRemoved(),
                    "the small shot must have struck the wall and been discarded (orig BetterFireball.java:268) inside "
                            + IMPACT_WINDOW_TICKS + " ticks (ENT-S-104)");
            helper.assertValueEqual(explosions.seen().size(), 0,
                    "explosions from a small shot (none, orig :265-267; ENT-S-102)");
            helper.assertBlock(FIRE_CELL, block -> block == Blocks.FIRE,
                    "no fire on the air side of the hit face " + FIRE_CELL + " (orig BetterFireball.java:236-263: the neighbour on the hit side, "
                            + "if air, becomes Blocks.fire; port BetterFireball.onHitBlock) (ENT-S-104)");
            helper.assertBlock(HEARTH, block -> block == Blocks.DIRT,
                    "the dirt hearth under the fire cell must be untouched by a small shot (ENT-S-104)");
            assertWallIntact(helper, " -- the obsidian wall must be untouched by a small shot (ENT-S-104)");
        });
    }

    /**
     * orig BetterFireball.java:266 exploded with fire = true ALWAYS and block destruction =
     * mobGriefing (1.7.10 Explosion isFlaming / isSmoking); the port fed mobGriefing into 1.21.1's
     * single fire slot. With the rule on (asserted at launch and at the check; the default, which
     * the batch-mate that turns it off does not touch until this window has closed) a big shot
     * behaves as before ENT-S-104: exactly one explosion, the port's radius 2 with the null
     * source, the shot discarded -- and now flagged fire = true, with the block interaction the
     * rule grants (1.21.1 Level.explode maps MOB through EventHooks.canEntityGrief, the gamerule
     * for a null source: DESTROY / DESTROY_WITH_DECAY, never KEEP). The dirt hearth directly under
     * the blast is the destruction witness; the fire orig :262 placed on the face a moment
     * earlier goes with it, as 1.7.10's doExplosionB (isSmoking) removed it too, and no fire
     * returns to that cell because the block below it is gone.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s104_big_shot_with_mob_griefing_on_explodes_once_with_fire_as_before(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameRules.BooleanValue mobGriefing = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
        helper.assertTrue(mobGriefing.get(),
                "test assumes mobGriefing=true (vanilla default) at launch; the batch-mate that turns it off waits "
                        + MOB_GRIEFING_OFF_DELAY_TICKS + " ticks first (ENT-S-104)");
        buildWallAndHearth(helper);
        LivingEntity shooter = spawnShooter(helper);
        BetterFireball shot = new BetterFireball(level, shooter, WALL_AIM);
        shot.setNotMe();
        shot.setBig();
        fireAtWallThenCheck(helper, shooter, shot, explosions -> {
            helper.assertTrue(mobGriefing.get(),
                    "test assumes mobGriefing=true through its window; another test flipped it (ENT-S-104)");
            helper.assertTrue(shot.isRemoved(),
                    "the big shot must have struck the wall and been discarded (orig BetterFireball.java:268) inside "
                            + IMPACT_WINDOW_TICKS + " ticks (ENT-S-104)");
            helper.assertValueEqual(explosions.seen().size(), 1,
                    "explosions from one big shot with mobGriefing on (orig :265-267: one) (ENT-S-104)");
            Explosion only = explosions.seen().get(0);
            helper.assertTrue(Math.abs(only.radius() - BIG_POWER) < DIM_EPS,
                    "the explosion must be the port's, at setBig's power " + BIG_POWER + "; got radius " + only.radius() + " (ENT-S-104)");
            helper.assertTrue(only.getDirectSourceEntity() == null,
                    "the explosion must carry orig :266's null source; got " + only.getDirectSourceEntity() + " (ENT-S-104)");
            helper.assertTrue(explosionFire(only),
                    "the explosion must carry fire = true (orig :266 passed it unconditionally) (ENT-S-104)");
            helper.assertTrue(only.interactsWithBlocks() && only.getBlockInteraction() != Explosion.BlockInteraction.KEEP,
                    "with mobGriefing on the block interaction must be a DESTROY kind (Level.explode: MOB -> canEntityGrief(level, null) "
                            + "= the gamerule -> getDestroyType), got " + only.getBlockInteraction() + " (ENT-S-104)");
            helper.assertBlock(HEARTH, block -> block == Blocks.AIR,
                    "the dirt hearth directly under a power-2 DESTROY blast must be gone (the mobGriefing witness) (ENT-S-104)");
            helper.assertBlock(FIRE_CELL, block -> block == Blocks.AIR,
                    "the face fire placed before the blast (orig :262 then :266) is blown away by a DESTROY blast, and the cell "
                            + "cannot be re-lit with the hearth gone (ENT-S-104)");
            assertWallIntact(helper, " -- obsidian (resistance 1200) must stand under the power-2 blast (ENT-S-104)");
        });
    }

    /**
     * The mobGriefing-off half of orig BetterFireball.java:266 (fire = true ALWAYS, destruction =
     * mobGriefing): before the fix the port passed the rule as 1.21.1's fire flag, so with the
     * rule off an OreSpawn fireball set no explosion fire at all. The rule is turned off for this
     * test's window only -- after MOB_GRIEFING_OFF_DELAY_TICKS, once the batch-mates' windows
     * have closed, and restored in the check's finally (the KrakenPlayNicelyTests flip-and-restore
     * idiom; the shooter is spawned up front and waits, aloft and NoAI). Then: exactly one
     * explosion, flagged fire = true, block interaction KEEP (Level.explode: MOB ->
     * canEntityGrief(level, null) = the gamerule -> KEEP), the obsidian wall and the dirt hearth
     * untouched, and the fire orig :232-264 placed on the air side of the hit face still standing
     * (a KEEP blast removes no block). The explosion's own fire is seeded at random (1-in-3 per
     * affected air cell over a solid block, Explosion.finalizeExplosion) and is not asserted; the
     * flag is. Since MOD-031 (accepted 2026-09-04, default on) makes fireballs respect the rule,
     * the modern key is forced off for the window as well, so this stays the CLASSIC pin
     * (FireballModernFireTests pins the modern option).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "projectileTypeParity")
    public void s104_big_shot_with_mob_griefing_off_still_carries_fire_and_leaves_the_wall_intact(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        buildWallAndHearth(helper);
        helper.assertTrue(level.isEmptyBlock(helper.absolutePos(FIRE_CELL)),
                "precondition: the fire cell " + FIRE_CELL + " must start as air");
        LivingEntity shooter = spawnShooter(helper);
        shooter.setNoGravity(true);
        GameRules.BooleanValue mobGriefing = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
        helper.runAfterDelay(MOB_GRIEFING_OFF_DELAY_TICKS, () -> {
            boolean previous = mobGriefing.get();
            mobGriefing.set(false, level.getServer());
            // MOD-031 (accepted 2026-09-04, default on) makes fireballs respect mobGriefing on a default
            // config; this pin is the CLASSIC fire-always behaviour, so the key is forced off around the
            // window and restored with the rule (FireballModernFireTests pins the modern option).
            final boolean priorKey = OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.get();
            OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.set(false);
            try {
                BetterFireball shot = new BetterFireball(level, shooter, WALL_AIM);
                shot.setNotMe();
                shot.setBig();
                fireAtWallThenCheck(helper, shooter, shot, explosions -> {
                    try {
                        helper.assertFalse(mobGriefing.get(),
                                "precondition: mobGriefing must still be off at the check; another test flipped it (ENT-S-104)");
                        helper.assertTrue(shot.isRemoved(),
                                "the big shot must have struck the wall and been discarded (orig BetterFireball.java:268) inside "
                                        + IMPACT_WINDOW_TICKS + " ticks (ENT-S-104)");
                        helper.assertValueEqual(explosions.seen().size(), 1,
                                "explosions from one big shot with mobGriefing off (orig :265-267: still one) (ENT-S-104)");
                        Explosion only = explosions.seen().get(0);
                        helper.assertTrue(Math.abs(only.radius() - BIG_POWER) < DIM_EPS,
                                "the explosion must be the port's, at setBig's power " + BIG_POWER + "; got radius " + only.radius() + " (ENT-S-104)");
                        helper.assertTrue(only.getDirectSourceEntity() == null,
                                "the explosion must carry orig :266's null source; got " + only.getDirectSourceEntity() + " (ENT-S-104)");
                        helper.assertTrue(explosionFire(only),
                                "the explosion must carry fire = true with mobGriefing off (orig :266 passed true unconditionally; "
                                        + "the port used to pass the gamerule) (ENT-S-104)");
                        helper.assertTrue(only.getBlockInteraction() == Explosion.BlockInteraction.KEEP && !only.interactsWithBlocks(),
                                "with mobGriefing off the block interaction must be KEEP (Level.explode: MOB -> canEntityGrief(level, null) "
                                        + "= the gamerule), got " + only.getBlockInteraction() + " (ENT-S-104)");
                        assertWallIntact(helper, " -- the obsidian wall must be intact (ENT-S-104)");
                        helper.assertBlock(HEARTH, block -> block == Blocks.DIRT,
                                "the dirt hearth under the fire cell must survive a KEEP blast (ENT-S-104)");
                        helper.assertBlock(FIRE_CELL, block -> block == Blocks.FIRE,
                                "fire must remain on the air side of the hit face " + FIRE_CELL
                                        + " (orig BetterFireball.java:261-263; a KEEP blast removes no block) (ENT-S-104)");
                    } finally {
                        mobGriefing.set(previous, level.getServer());
                        OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.set(priorKey);
                    }
                });
            } catch (RuntimeException e) {
                mobGriefing.set(previous, level.getServer());
                OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.set(priorKey);
                shooter.discard();
                throw e;
            }
        });
    }

    // ---------------------------------------------------------------------------------------
    // ENT-S-103 (owner ruling 2026-09-04: parity bug, fix in classic) -- UltimateArrow Punch.
    // ---------------------------------------------------------------------------------------

    /** orig UltimateBow.java:30-33 / item/UltimateBow.java:31: the bow bakes Punch 2 onto itself. */
    private static final int BOW_PUNCH = 2;
    /** orig UltimateArrow.java:190: 0.6 per Punch level along the flat flight line, and the 0.1 lift. */
    private static final double PUNCH_PER_LEVEL = 0.6;
    private static final double PUNCH_LIFT = 0.1;
    /** orig UltimateBow.java:48 / item/UltimateBow.java:48: the fixed launch velocity. */
    private static final float BOW_VELOCITY = 3.0f;
    /**
     * LivingEntity.hurt -> knockback(0.4F, ...) (1.21.1 LivingEntity.java:1225): the vanilla hurt
     * knockback every successful arrow hit applies, Punch or not; for a Projectile source its
     * direction is the projectile's own flat delta movement
     * (Projectile.calculateHorizontalHurtKnockbackDirection, :304-308), i.e. the flight line.
     * Float-widened, as the code passes it.
     */
    private static final double VANILLA_HURT_KNOCKBACK = 0.4F;
    /** Two lanes 8 blocks apart on x; per lane the shooter stands at z 22 and the target at z 27, 5 blocks on. */
    private static final int CONTROL_LANE_X = 20;
    private static final int PUNCH_LANE_X = 28;
    private static final int LANE_SHOOTER_Z = 22;
    private static final int LANE_TARGET_Z = 27;
    /** Enough health to take the ceil(3.0 x 10) = 30 hit and stay a live, readable target. */
    private static final double TARGET_HEALTH = 10000.0;
    /**
     * Both hits land on the arrows' second tick: the first clip runs z 22.5 -> 25.5, short of the
     * target's inflated box (27.05 - 0.3 = 26.75), the second 25.5 -> 28.47 crosses it. 10 is ample
     * and the discard on hit is what the snapshot keys on.
     */
    private static final int ARROW_WINDOW_TICKS = 10;
    /** On the lane difference: the inputs are exact, so only ulps separate expected from computed. */
    private static final double PUSH_DIFF_EPS = 1e-9;
    /** On the absolute values, which add the float 0.4F's widening. */
    private static final double PUSH_ABS_EPS = 1e-6;

    private static void setMaxHealth(LivingEntity entity, double hp) {
        Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(hp);
        entity.setHealth((float) hp);
    }

    /**
     * Builds the arrow exactly as item/UltimateBow.java:46 does ({@code new UltimateArrow(level,
     * shooter, bow)}: the bow stack the arrow keeps a copy of) and launches it flat along +z at the
     * bow's 3.0 with zero inaccuracy, from the shooter's exact x/z at the target's mid-height (the
     * constructor's start is the shooter's eye height minus 0.1; the target's box is what the flat
     * line must cross). Crit off: a random damage bonus only (orig :174-176).
     */
    private static UltimateArrow shootArrow(GameTestHelper helper, LivingEntity shooter, ItemStack bow, LivingEntity target) {
        UltimateArrow arrow = new UltimateArrow(helper.getLevel(), shooter, bow);
        arrow.setPos(shooter.getX(), target.getY() + target.getBbHeight() / 2.0, shooter.getZ());
        arrow.shoot(0.0, 0.0, 1.0, BOW_VELOCITY, 0.0f);
        arrow.setCritArrow(false);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        helper.assertTrue(helper.getLevel().addFreshEntity(arrow), "ServerLevel#addFreshEntity refused the ultimate arrow (ENT-S-103)");
        return arrow;
    }

    /**
     * orig UltimateBow.java:52-54 seeded the arrow with the bow's Punch level and orig
     * UltimateArrow.java:189-191 spent it: addVelocity(0.6 x level along the flat flight line,
     * +0.1 lift) after a successful hurt on an EntityLiving target. Vanilla 1.21.1 keys Punch on
     * #minecraft:arrows (data/minecraft/enchantment/punch.json), which the ultimate arrow is
     * ruled out of (tags_ultimate_and_irukandji_arrows_stay_outside_minecraft_arrows), so before
     * the fix the bow's own Punch 2 never landed; UltimateArrow.doKnockback now applies it the
     * IrukandjiArrow way.
     *
     * <p>Two lanes, one arrow each, built as item/UltimateBow.java:46 builds them: the punch
     * lane's bow is the bow's own bake (item/UltimateBow.java:26-33 through inventoryTick: Power 5
     * / Flame 3 / Punch 2 / Infinity 1, the levels EntityLogicTestsA pins), the control lane's is
     * that same bake with Punch stripped to 0, so the lanes differ in nothing but Punch. The
     * arrows fly with {@code shoot(0, 0, 1, 3.0, 0)} -- the bow's fixed 3.0 (orig :48) with
     * inaccuracy 0 instead of releaseUsing's 1.0, whose triangle jitter would randomise the flat
     * direction; releaseUsing itself is pinned by EntityLogicTestsA#ultimate_bow_instant_power5_crit_damage.
     * The targets are frozen cows (NoAI, 10000 max health, spawned before the arrows): a NoAI
     * mob runs no travel physics (LivingEntity.travel :2161-2162 is gated on
     * isControlledByLocalInstance = isEffectiveAi, false for NoAI), so the push never moves it
     * and its delta movement only decays 0.98 per own tick (aiStep :2673-2674) -- the 1.7.10
     * addVelocity is observable as velocity, exactly what orig :190 wrote. The snapshot is taken
     * by an onEachTick task on the first tick each arrow reports removed (the discard at the end
     * of AbstractArrow.onHitEntity): test tasks run after the level tick
     * (MinecraftServer.tickChildren: ServerLevel.tick, then GameTestTicker.tick), and within the
     * level tick the target ticks before the arrow (EntityTickList is an
     * Int2ObjectLinkedOpenHashMap, insertion order; the targets are added first), so the value
     * read is the undecayed post-hit velocity: the vanilla 0.4 hurt knockback along the flight
     * line for both lanes, plus 0.6 x 2 along it and the 0.1 lift for the punch lane (a NoAI cow
     * never runs move(), so onGround stays false and the hurt knockback leaves y alone).
     *
     * <p>Assertions: sign -- both targets carry +z (the flight line) velocity; magnitude -- the
     * lane difference is (0, +0.1, +1.2) within 1e-9, the control's horizontal velocity is the
     * vanilla 0.4 alone with no Punch share (under even a level-1 push of 0.6), and the absolute
     * values 0.4 and 1.6 hold within 1e-6. Tolerances: every input is exact (a unit +z aim with
     * zero inaccuracy, 0.6 x 2, the float 0.4), so expected and computed differ by a handful of
     * ulps (~1e-15); 1e-9 on the difference and 1e-6 on the absolutes (which add the 0.4F
     * widening) are a million times that and still four orders under the smallest semantic
     * deviation the pin guards -- a knockback-resistance factor, 0.5 for 0.6, level 1 for 2, or
     * a one-tick 0.98 decay (0.024 short), which is also what a wrong tick-order assumption would
     * show. The ARROW_HIT_PLAYER ding and the pvp no-sell are not exercised.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s103_ultimate_arrow_punch_pushes_the_target_along_the_flight_line_by_the_1_7_10_amount(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Holder<Enchantment> punch = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.PUNCH);
        List<Entity> spawned = new ArrayList<>();
        UltimateArrow controlArrow;
        UltimateArrow punchArrow;
        Cow controlTarget;
        Cow punchTarget;
        try {
            Cow controlShooter = spawnFrozenCow(helper, new BlockPos(CONTROL_LANE_X, POS.getY(), LANE_SHOOTER_Z));
            spawned.add(controlShooter);
            Cow punchShooter = spawnFrozenCow(helper, new BlockPos(PUNCH_LANE_X, POS.getY(), LANE_SHOOTER_Z));
            spawned.add(punchShooter);
            controlTarget = spawnFrozenCow(helper, new BlockPos(CONTROL_LANE_X, POS.getY(), LANE_TARGET_Z));
            spawned.add(controlTarget);
            punchTarget = spawnFrozenCow(helper, new BlockPos(PUNCH_LANE_X, POS.getY(), LANE_TARGET_Z));
            spawned.add(punchTarget);
            setMaxHealth(controlTarget, TARGET_HEALTH);
            setMaxHealth(punchTarget, TARGET_HEALTH);
            helper.assertTrue(controlTarget.getDeltaMovement().equals(Vec3.ZERO) && punchTarget.getDeltaMovement().equals(Vec3.ZERO),
                    "precondition: both targets start at rest");
            helper.assertTrue(controlShooter.getX() == controlTarget.getX() && punchShooter.getX() == punchTarget.getX(),
                    "precondition: each lane's shooter and target share the exact x, so the flat flight line is +z");

            // The bow's own bake (item/UltimateBow.java:26-33) and the same bake with Punch stripped.
            ItemStack punchBow = new ItemStack(ModItems.ULTIMATE_BOW.get());
            punchBow.getItem().inventoryTick(punchBow, level, punchShooter, 0, true);
            ItemStack controlBow = punchBow.copy();
            EnchantmentHelper.updateEnchantments(controlBow, mutable -> mutable.set(punch, 0));
            helper.assertValueEqual(EnchantmentHelper.getItemEnchantmentLevel(punch, punchBow), BOW_PUNCH,
                    "precondition: the bow's self-baked Punch level (item/UltimateBow.java:31)");
            helper.assertValueEqual(EnchantmentHelper.getItemEnchantmentLevel(punch, controlBow), 0,
                    "precondition: the control bow's Punch level after stripping");

            controlArrow = shootArrow(helper, controlShooter, controlBow, controlTarget);
            spawned.add(controlArrow);
            punchArrow = shootArrow(helper, punchShooter, punchBow, punchTarget);
            spawned.add(punchArrow);
            helper.assertTrue(punchArrow.getWeaponItem() != null
                            && EnchantmentHelper.getItemEnchantmentLevel(punch, punchArrow.getWeaponItem()) == BOW_PUNCH,
                    "precondition: the punch arrow's weapon copy (AbstractArrow.getWeaponItem) must carry Punch " + BOW_PUNCH + " (ENT-S-103)");
            helper.assertTrue(controlArrow.getWeaponItem() != null
                            && EnchantmentHelper.getItemEnchantmentLevel(punch, controlArrow.getWeaponItem()) == 0,
                    "precondition: the control arrow's weapon copy must carry no Punch (ENT-S-103)");
            helper.assertTrue(controlArrow.getDeltaMovement().equals(new Vec3(0.0, 0.0, BOW_VELOCITY))
                            && punchArrow.getDeltaMovement().equals(new Vec3(0.0, 0.0, BOW_VELOCITY)),
                    "precondition: zero-inaccuracy shoot(0, 0, 1, 3.0) must give exactly (0, 0, 3.0), got "
                            + controlArrow.getDeltaMovement() + " / " + punchArrow.getDeltaMovement());
        } catch (RuntimeException e) {
            spawned.forEach(Entity::discard);
            throw e;
        }

        Vec3[] controlPush = new Vec3[1];
        Vec3[] punchPush = new Vec3[1];
        helper.onEachTick(() -> {
            if (controlPush[0] == null && controlArrow.isRemoved()) {
                controlPush[0] = controlTarget.getDeltaMovement();
            }
            if (punchPush[0] == null && punchArrow.isRemoved()) {
                punchPush[0] = punchTarget.getDeltaMovement();
            }
        });
        helper.runAfterDelay(ARROW_WINDOW_TICKS, () -> {
            try {
                helper.assertTrue(controlPush[0] != null && punchPush[0] != null,
                        "both arrows must have hit and been discarded inside " + ARROW_WINDOW_TICKS + " ticks (control removed: "
                                + controlArrow.isRemoved() + ", punch removed: " + punchArrow.isRemoved() + ") (ENT-S-103)");
                helper.assertFalse(controlTarget.isRemoved() || punchTarget.isRemoved(),
                        "the targets must survive the hit (10000 max health) so their velocity is readable (ENT-S-103)");
                Vec3 control = controlPush[0];
                Vec3 pushed = punchPush[0];
                Vec3 diff = pushed.subtract(control);
                double expectedPush = BOW_PUNCH * PUNCH_PER_LEVEL;

                helper.assertTrue(pushed.z > 0.0 && control.z > 0.0,
                        "both targets must be moving along the flight line (+z): control " + control + ", punch " + pushed + " (ENT-S-103)");
                helper.assertTrue(Math.abs(diff.z - expectedPush) < PUSH_DIFF_EPS,
                        "Punch " + BOW_PUNCH + " must add exactly 0.6 x " + BOW_PUNCH + " = " + expectedPush
                                + " along the flat flight line (orig UltimateArrow.java:190), got " + diff.z
                                + " (control " + control + ", punch " + pushed + ") (ENT-S-103)");
                helper.assertTrue(Math.abs(diff.x) < PUSH_DIFF_EPS,
                        "the push is along the flight line only: no x share, got " + diff.x + " (ENT-S-103)");
                helper.assertTrue(Math.abs(diff.y - PUNCH_LIFT) < PUSH_DIFF_EPS,
                        "the push carries orig :190's fixed 0.1 lift, got " + diff.y + " (ENT-S-103)");
                helper.assertTrue(Math.abs(control.z - VANILLA_HURT_KNOCKBACK) < PUSH_ABS_EPS && Math.abs(control.x) < PUSH_ABS_EPS,
                        "a Punch-0 arrow must not push: the control's horizontal velocity must be the vanilla hurt knockback "
                                + VANILLA_HURT_KNOCKBACK + " alone (LivingEntity.hurt :1225), got " + control + " (ENT-S-103)");
                helper.assertTrue(control.horizontalDistance() < PUNCH_PER_LEVEL,
                        "a Punch-0 arrow must not push: the control's horizontal speed " + control.horizontalDistance()
                                + " is not under even a level-1 push of " + PUNCH_PER_LEVEL + " (ENT-S-103)");
                helper.assertTrue(Math.abs(pushed.z - (VANILLA_HURT_KNOCKBACK + expectedPush)) < PUSH_ABS_EPS,
                        "the punch target's velocity must be the raw post-hit 0.4 + " + expectedPush + " along +z, got " + pushed.z
                                + " (a one-tick 0.98 decay would mean the target ticked after the arrow, breaking the snapshot) (ENT-S-103)");
            } finally {
                spawned.forEach(Entity::discard);
            }
            helper.succeed();
        });
    }

    // ---------------------------------------------------------------------------------------
    // ENT-S-111 (owner ruling 2026-09-04: parity, fix in classic) -- the IrukandjiArrow Punch push
    // reaches an EntityLiving (1.21.1 Mob) victim only, never a player.
    // ---------------------------------------------------------------------------------------

    /** The Punch level written onto the skate bow (orig SkateBow.java:53-55 seeds the arrow's knockbackStrength from the bow's Punch). */
    private static final int SKATE_BOW_PUNCH = 2;
    /** The cow lane on the s103 control lane's x, the player lane on its punch lane's x; shooter and target z as there. */
    private static final int COW_LANE_X = CONTROL_LANE_X;
    private static final int PLAYER_LANE_X = PUNCH_LANE_X;

    /**
     * A survival mock player standing on a lane target spot: game mode set explicitly (the
     * game-test server defaults mock players to CREATIVE, GameTestServer.java:85 -- the
     * CreativeMappingParityTests idiom), 10000 max health so the flat 100 (orig :157) leaves it a
     * live, readable target, teleported to the block's bottom centre so it shares the shooter's
     * exact x, and its spawn invulnerability cleared ({@link #clearSpawnInvulnerability}).
     * Deprecated mock-player factory tolerated the way CreativeMappingParityTests does.
     */
    @SuppressWarnings({"removal", "deprecation"})
    private static ServerPlayer survivalPlayerAt(GameTestHelper helper, BlockPos pos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        setMaxHealth(player, TARGET_HEALTH);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(pos));
        player.teleportTo(helper.getLevel(), at.x, at.y, at.z, 0.0f, 0.0f);
        clearSpawnInvulnerability(player);
        return player;
    }

    /**
     * A fresh ServerPlayer refuses every hurt that does not bypass invulnerability for its first
     * 60 ticks (ServerPlayer.hurt reads {@code spawnInvulnerableTime}, initialised to 60 and
     * counted down in ServerPlayer.tick); it is written to 0 by name, the way the ENT-S-104 tests
     * read Explosion.fire, so the arrow can hurt the player on its second tick.
     */
    private static void clearSpawnInvulnerability(ServerPlayer player) {
        try {
            Field field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            field.setAccessible(true);
            field.setInt(player, 0);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "ServerPlayer.spawnInvulnerableTime is not reachable by reflection (1.21.1: private int; official names at runtime) (ENT-S-111)", e);
        }
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    /**
     * Builds the arrow as item/SkateBow.java:49 does ({@code new IrukandjiArrow(level, shooter,
     * bow)}: the bow stack the arrow keeps a copy of) and launches it flat along +z at 3.0 with zero
     * inaccuracy, from the shooter's exact x/z at the target's mid-height -- the s103 launch. The
     * skate bow's own pull-scaled speed (1.5 x pull, orig SkateBow.java:49) is not what is under
     * test: the push normalises the flat flight line (orig :187-188), so it is speed-independent.
     * Crit off (orig :172-173, a random damage bonus only).
     */
    private static IrukandjiArrow shootIrukandjiArrow(GameTestHelper helper, LivingEntity shooter, ItemStack bow, LivingEntity target) {
        IrukandjiArrow arrow = new IrukandjiArrow(helper.getLevel(), shooter, bow);
        arrow.setPos(shooter.getX(), target.getY() + target.getBbHeight() / 2.0, shooter.getZ());
        arrow.shoot(0.0, 0.0, 1.0, BOW_VELOCITY, 0.0f);
        arrow.setCritArrow(false);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        helper.assertTrue(helper.getLevel().addFreshEntity(arrow), "ServerLevel#addFreshEntity refused the irukandji arrow (ENT-S-111)");
        return arrow;
    }

    /**
     * orig IrukandjiArrow.java:180-193: after a successful hurt, the arrow count, the Punch push of
     * :187-188 (0.6 x level along the flat flight line, +0.1 lift) and the ding all sit inside
     * {@code if (var4.field_72308_g instanceof EntityLiving)} (:181), the 1.7.10 AI-mob base that
     * EntityPlayer (an EntityLivingBase) never was -- so a player took the vanilla hurt knockback
     * and no push. The port's push applied to any LivingEntity; it now carries the Mob gate of
     * UltimateArrow.doKnockback (ENT-S-103).
     *
     * <p>Two lanes on the s103 geometry, one arrow each from a skate bow with Punch
     * {@value #SKATE_BOW_PUNCH} written onto it (orig SkateBow.java:53-55 seeds the arrow from the
     * bow's Punch, which the port's arrow reads off its weapon copy): the cow lane's target is a
     * frozen cow (a Mob), the player lane's a survival mock player, both at 10000 health, both hit
     * on the arrows' second tick and snapshotted by an onEachTick task the tick their arrow is
     * discarded, exactly as the s103 test does (the cow's velocity would decay 0.98 per own tick;
     * the mock player's movement tick -- Player.tick through ServerPlayer.doTick, driven by a
     * packet listener the mock has none of -- never runs, so its velocity is the post-hit value for
     * as long as it stands). {@code ultimateSwordPvp} is raised for the window: with it off
     * IrukandjiArrow.onHitEntity no-sells a player before the hurt (orig :158-165) and the pin
     * would be vacuous. It is restored in the check's finally and on the setup's failure path; no
     * batch-mate reads it (the s103 arrows hit cows, which the guard never touches).</p>
     *
     * <p>Assertions: the player was hurt (health below 10000 at the hit) -- the precondition that
     * makes the rest meaningful; the player's velocity is the vanilla 0.4 hurt knockback along the
     * flight line alone (LivingEntity.hurt :1225, direction from the projectile's flat delta
     * movement), no Punch share on any axis (y stays 0: the mock player is not flagged on the
     * ground, asserted, and off the ground the hurt knockback leaves y alone), under even a
     * level-1 push; the cow's velocity is 0.4 + 0.6 x {@value #SKATE_BOW_PUNCH} along the line with
     * the 0.1 lift; and the lane difference is exactly (0, 0.1, 1.2). Tolerances are the s103 ones
     * for the s103 reasons: exact inputs, 1e-9 on the difference, 1e-6 on the absolutes (which add
     * the float 0.4F's widening).</p>
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void s111_irukandji_arrow_punch_pushes_a_mob_but_never_a_player(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(level.getDifficulty() != Difficulty.PEACEFUL,
                "precondition: on Peaceful Player.hurt zeroes an arrow's difficulty-scaled damage and refuses the hit;"
                        + " the game-test level runs at NORMAL (ENT-S-111 test setup)");
        Holder<Enchantment> punch = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.PUNCH);
        final boolean priorPvp = OreSpawnConfig.ULTIMATE_SWORD_PVP.get();
        List<Entity> spawned = new ArrayList<>();
        ServerPlayer playerTarget = null;
        Cow cowTarget;
        IrukandjiArrow cowArrow;
        IrukandjiArrow playerArrow;
        try {
            Cow cowShooter = spawnFrozenCow(helper, new BlockPos(COW_LANE_X, POS.getY(), LANE_SHOOTER_Z));
            spawned.add(cowShooter);
            Cow playerShooter = spawnFrozenCow(helper, new BlockPos(PLAYER_LANE_X, POS.getY(), LANE_SHOOTER_Z));
            spawned.add(playerShooter);
            cowTarget = spawnFrozenCow(helper, new BlockPos(COW_LANE_X, POS.getY(), LANE_TARGET_Z));
            spawned.add(cowTarget);
            setMaxHealth(cowTarget, TARGET_HEALTH);
            playerTarget = survivalPlayerAt(helper, new BlockPos(PLAYER_LANE_X, POS.getY(), LANE_TARGET_Z));
            LivingEntity playerAsLiving = playerTarget;
            helper.assertTrue(cowTarget instanceof Mob && !(playerAsLiving instanceof Mob),
                    "precondition: the cow is a Mob (orig :181's EntityLiving) and the player is not (an EntityLivingBase only"
                            + " in 1.7.10) (ENT-S-111 test setup)");
            helper.assertTrue(!playerTarget.getAbilities().instabuild && !playerTarget.getAbilities().invulnerable,
                    "precondition: the mock player must be survival (instabuild and invulnerable clear), else Player.hurt"
                            + " refuses the hit (ENT-S-111 test setup)");
            helper.assertFalse(playerTarget.onGround(),
                    "precondition: nothing has moved the mock player, so it is not flagged on the ground and the hurt"
                            + " knockback leaves its y velocity alone (ENT-S-111 test setup)");
            helper.assertTrue(cowTarget.getDeltaMovement().equals(Vec3.ZERO) && playerTarget.getDeltaMovement().equals(Vec3.ZERO),
                    "precondition: both targets start at rest");
            helper.assertTrue(cowShooter.getX() == cowTarget.getX() && playerShooter.getX() == playerTarget.getX(),
                    "precondition: each lane's shooter and target share the exact x, so the flat flight line is +z");

            ItemStack bow = new ItemStack(ModItems.SKATE_BOW.get());
            EnchantmentHelper.updateEnchantments(bow, mutable -> mutable.set(punch, SKATE_BOW_PUNCH));
            helper.assertValueEqual(EnchantmentHelper.getItemEnchantmentLevel(punch, bow), SKATE_BOW_PUNCH,
                    "precondition: the skate bow's Punch level after writing it (ENT-S-111 test setup)");

            OreSpawnConfig.ULTIMATE_SWORD_PVP.set(true);
            cowArrow = shootIrukandjiArrow(helper, cowShooter, bow, cowTarget);
            spawned.add(cowArrow);
            playerArrow = shootIrukandjiArrow(helper, playerShooter, bow, playerTarget);
            spawned.add(playerArrow);
            helper.assertTrue(cowArrow.getWeaponItem() != null
                            && EnchantmentHelper.getItemEnchantmentLevel(punch, cowArrow.getWeaponItem()) == SKATE_BOW_PUNCH
                            && playerArrow.getWeaponItem() != null
                            && EnchantmentHelper.getItemEnchantmentLevel(punch, playerArrow.getWeaponItem()) == SKATE_BOW_PUNCH,
                    "precondition: both arrows' weapon copies (AbstractArrow.getWeaponItem) must carry Punch " + SKATE_BOW_PUNCH + " (ENT-S-111)");
            helper.assertTrue(cowArrow.getDeltaMovement().equals(new Vec3(0.0, 0.0, BOW_VELOCITY))
                            && playerArrow.getDeltaMovement().equals(new Vec3(0.0, 0.0, BOW_VELOCITY)),
                    "precondition: zero-inaccuracy shoot(0, 0, 1, 3.0) must give exactly (0, 0, 3.0), got "
                            + cowArrow.getDeltaMovement() + " / " + playerArrow.getDeltaMovement());
        } catch (RuntimeException e) {
            OreSpawnConfig.ULTIMATE_SWORD_PVP.set(priorPvp);
            spawned.forEach(Entity::discard);
            removePlayer(helper, playerTarget);
            throw e;
        }
        final ServerPlayer player = playerTarget;

        Vec3[] cowPush = new Vec3[1];
        Vec3[] playerPush = new Vec3[1];
        float[] playerHealthAtHit = new float[1];
        helper.onEachTick(() -> {
            if (cowPush[0] == null && cowArrow.isRemoved()) {
                cowPush[0] = cowTarget.getDeltaMovement();
            }
            if (playerPush[0] == null && playerArrow.isRemoved()) {
                playerPush[0] = player.getDeltaMovement();
                playerHealthAtHit[0] = player.getHealth();
            }
        });
        helper.runAfterDelay(ARROW_WINDOW_TICKS, () -> {
            try {
                helper.assertTrue(cowPush[0] != null && playerPush[0] != null,
                        "both arrows must have hit and been discarded inside " + ARROW_WINDOW_TICKS + " ticks (cow arrow removed: "
                                + cowArrow.isRemoved() + ", player arrow removed: " + playerArrow.isRemoved() + ") (ENT-S-111)");
                helper.assertFalse(cowTarget.isRemoved() || player.isRemoved(),
                        "the targets must survive the hit (10000 max health) so their velocity is readable (ENT-S-111)");
                helper.assertTrue(playerHealthAtHit[0] < (float) TARGET_HEALTH,
                        "the survival player must have been hurt by the arrow with ultimateSwordPvp on -- orig :180 gates everything"
                                + " on a successful hurt, and with the flag off onHitEntity no-sells players (orig :158-165) -- health at"
                                + " the hit " + playerHealthAtHit[0] + " of " + TARGET_HEALTH + " (ENT-S-111)");
                Vec3 pushed = cowPush[0];
                Vec3 unpushed = playerPush[0];
                double expectedPush = SKATE_BOW_PUNCH * PUNCH_PER_LEVEL;

                helper.assertTrue(unpushed.z > 0.0 && pushed.z > 0.0,
                        "both targets must be moving along the flight line (+z): player " + unpushed + ", cow " + pushed + " (ENT-S-111)");
                helper.assertTrue(Math.abs(unpushed.z - VANILLA_HURT_KNOCKBACK) < PUSH_ABS_EPS && Math.abs(unpushed.x) < PUSH_ABS_EPS,
                        "a player is never pushed (orig IrukandjiArrow.java:181 EntityLiving gate): the player's horizontal velocity"
                                + " must be the vanilla hurt knockback " + VANILLA_HURT_KNOCKBACK + " alone (LivingEntity.hurt :1225), got "
                                + unpushed + " (ENT-S-111)");
                helper.assertTrue(Math.abs(unpushed.y) < PUSH_ABS_EPS,
                        "a player is never pushed: no 0.1 lift (orig :188) on the player, got y " + unpushed.y + " (ENT-S-111)");
                helper.assertTrue(unpushed.horizontalDistance() < PUNCH_PER_LEVEL,
                        "a player is never pushed: the player's horizontal speed " + unpushed.horizontalDistance()
                                + " is not under even a level-1 push of " + PUNCH_PER_LEVEL + " (ENT-S-111)");
                helper.assertTrue(Math.abs(pushed.z - (VANILLA_HURT_KNOCKBACK + expectedPush)) < PUSH_ABS_EPS && Math.abs(pushed.x) < PUSH_ABS_EPS,
                        "a Mob is pushed (orig :181-188): the cow's velocity must be the raw post-hit 0.4 + " + expectedPush
                                + " along +z, got " + pushed + " (ENT-S-111)");
                helper.assertTrue(Math.abs(pushed.y - PUNCH_LIFT) < PUSH_ABS_EPS,
                        "the cow's push carries orig :188's fixed 0.1 lift, got y " + pushed.y + " (ENT-S-111)");
                Vec3 diff = pushed.subtract(unpushed);
                helper.assertTrue(Math.abs(diff.x) < PUSH_DIFF_EPS && Math.abs(diff.y - PUNCH_LIFT) < PUSH_DIFF_EPS
                                && Math.abs(diff.z - expectedPush) < PUSH_DIFF_EPS,
                        "the lanes must differ by exactly the 1.7.10 push (0, " + PUNCH_LIFT + ", " + expectedPush + "), got " + diff
                                + " (ENT-S-111)");
            } finally {
                OreSpawnConfig.ULTIMATE_SWORD_PVP.set(priorPvp);
                spawned.forEach(Entity::discard);
                removePlayer(helper, player);
            }
            helper.succeed();
        });
    }

    // ---------------------------------------------------------------------------------------
    // Projectile-tag rulings (owner, 2026-09-04).
    // ---------------------------------------------------------------------------------------

    /**
     * Ruling: "arrows join #minecraft:arrows only if the 1.7.10 bow code applied Power and Punch
     * to them -- check and rule from that". The check, orig UltimateBow.java:46-64 and
     * SkateBow.java:36-68: both bows read Punch (field_77344_u) into {@code func_70240_a} =
     * setKnockbackStrength (UltimateBow :52-54, SkateBow :53-55) and Flame (field_77343_v) into
     * {@code func_70015_d(100)} = setFire (:55-57 / :56-58), and NEITHER reads Power
     * (field_77345_t): vanilla ItemBow's {@code setDamage(getDamage() + power * 0.5 + 0.5)} block
     * is absent, and the arrows' own {@code func_70239_b} (setDamage) is an empty override besides
     * (UltimateArrow.java:275-276, IrukandjiArrow.java:269-270) -- their damage is
     * {@code ceil(speed * UltimateBowDamage)} (UltimateArrow :157) and a flat 100
     * (IrukandjiArrow :157). Power was never applied, so the arrows stay OUT of
     * {@code #minecraft:arrows}, on which 1.21.1 keys Power's +0.5/level damage and Punch's
     * knockback (data/minecraft/enchantment/power.json and punch.json: {@code direct_attacker}
     * in #minecraft:arrows) and the adventure/shoot_arrow advancement ({@code direct_entity} in
     * #minecraft:arrows): the arrows keep their own damage, the port's UltimateBow keeps its
     * self-applied Power 5 / Flame 3 / Punch 2 / Infinity 1 (Flame's ignite fires unconditionally
     * on projectile_spawned, the 1.7.10 setFire(100)), and an ultimate or irukandji arrow never
     * grants shoot_arrow. The vanilla arrow is the control that the tag itself is loaded.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void tags_ultimate_and_irukandji_arrows_stay_outside_minecraft_arrows(GameTestHelper helper) {
        helper.assertTrue(EntityType.ARROW.is(EntityTypeTags.ARROWS),
                "control: minecraft:arrow must carry #minecraft:arrows (the vanilla tag is loaded)");
        helper.assertFalse(ModEntities.ULTIMATE_ARROW.get().is(EntityTypeTags.ARROWS),
                "orespawn:ultimate_arrow must NOT carry #minecraft:arrows: orig UltimateBow.java:46-64 applied Punch (:52-54) and Flame (:55-57) but never Power (owner ruling 2026-09-04)");
        helper.assertFalse(ModEntities.IRUKANDJI_ARROW.get().is(EntityTypeTags.ARROWS),
                "orespawn:irukandji_arrow must NOT carry #minecraft:arrows: orig SkateBow.java:36-68 applied Punch (:53-55) and Flame (:56-58) but never Power (owner ruling 2026-09-04)");
        helper.succeed();
    }

    /**
     * Ruling: "Throwables join impact_projectiles as vanilla-consistent behavior with no parity
     * obligation; record as a MOD note" (MOD-030). {@code minecraft:impact_projectiles} feeds only
     * {@code Projectile.mayBreak} (type in the tag AND the projectilesCanBreakBlocks gamerule),
     * which DecoratedPotBlock, ChorusFlowerBlock and PointedDripstoneBlock consult in
     * onProjectileHit -- blocks 1.7.10 did not have. The overlay
     * data/minecraft/tags/entity_type/impact_projectiles.json now lists the ThrowableProjectile
     * family that flies and lands like a snowball or egg: LaserBall and its Acid / IceBall /
     * DeadIrukandji subclasses, WaterBall, ThunderBolt, SunspotUrchin, InkSack, Shoes and
     * EntityThrownRock. BerthaHit (the invisible swing proxy) and EntityCage (the capture bobber)
     * stay out. The snowball and the fishing bobber are the vanilla controls.
     */
    @GameTest(template = "empty_large", batch = "projectileTypeParity")
    public void tags_throwables_join_impact_projectiles_bertha_hit_and_cage_stay_out(GameTestHelper helper) {
        helper.assertTrue(EntityType.SNOWBALL.is(EntityTypeTags.IMPACT_PROJECTILES),
                "control: minecraft:snowball must carry #minecraft:impact_projectiles (the vanilla tag is loaded)");
        helper.assertFalse(EntityType.FISHING_BOBBER.is(EntityTypeTags.IMPACT_PROJECTILES),
                "control: minecraft:fishing_bobber must not carry #minecraft:impact_projectiles");
        List<EntityType<?>> throwables = List.of(
                ModEntities.LASER_BALL.get(), ModEntities.ACID.get(), ModEntities.ICE_BALL.get(),
                ModEntities.DEAD_IRUKANDJI.get(), ModEntities.WATER_BALL.get(), ModEntities.THUNDER_BOLT.get(),
                ModEntities.SUNSPOT_URCHIN.get(), ModEntities.INK_SACK.get(), ModEntities.SHOES.get(),
                ModEntities.ENTITY_THROWN_ROCK.get());
        for (EntityType<?> type : throwables) {
            helper.assertTrue(type.is(EntityTypeTags.IMPACT_PROJECTILES),
                    EntityType.getKey(type) + " must carry #minecraft:impact_projectiles (MOD-030 overlay, owner ruling 2026-09-04)");
        }
        helper.assertFalse(ModEntities.BERTHA_HIT.get().is(EntityTypeTags.IMPACT_PROJECTILES),
                "orespawn:bertha_hit (the invisible swing proxy) must stay outside #minecraft:impact_projectiles (MOD-030)");
        helper.assertFalse(ModEntities.ENTITY_CAGE.get().is(EntityTypeTags.IMPACT_PROJECTILES),
                "orespawn:cage (the capture bobber) must stay outside #minecraft:impact_projectiles (MOD-030)");
        helper.succeed();
    }
}
