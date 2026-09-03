package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BetterFireball;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
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
 * <p>Projectile-tag rulings (owner, 2026-09-04), pinned by the two {@code tags_} tests: the
 * ultimate and irukandji arrows stay outside {@code #minecraft:arrows} because the 1.7.10
 * bows never applied Power to them (the check is quoted on the test), and the
 * ThrowableProjectile family joins {@code #minecraft:impact_projectiles} as vanilla-consistent
 * behaviour with no parity obligation (MOD-030), BerthaHit and EntityCage excepted.</p>
 *
 * <p>No config is flipped, but the class still declares its own batch (TEST-003: new test
 * classes never join the default batch, whose 50-test buckets reshuffle); every ENT-S-098 and
 * tag test is synchronous in one tick, the two ENT-S-102 impact tests wait a fixed 40-tick
 * window ({@code runAfterDelay}), and all discard what they spawned in a finally. Template
 * {@code empty_large} (48x16x48) with the shooter at (24, 8, 24), as HitboxDimsParityTests.</p>
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

    /** A frozen vanilla cow: the shooter constructor reads only LivingEntity position and rotation. */
    private static LivingEntity spawnShooter(GameTestHelper helper) {
        Cow shooter = helper.spawnWithNoFreeWill(EntityType.COW, POS);
        shooter.setNoAi(true);
        shooter.setPersistenceRequired();
        shooter.setYRot(SHOOTER_Y_ROT);
        shooter.setXRot(SHOOTER_X_ROT);
        return shooter;
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
