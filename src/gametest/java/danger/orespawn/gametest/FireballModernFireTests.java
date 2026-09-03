package danger.orespawn.gametest;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BetterFireball;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
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
import java.util.function.Consumer;

/**
 * MOD-031, owner ruling 2026-09-04, verbatim: "MOD-031: accepted as a modern option, default on;
 * classic stays 1.7.10."
 *
 * <p>The option: the {@code [modern]} key {@code fireRespectsMobGriefing}
 * ({@link OreSpawnConfig#MODERN_FIRE_RESPECTS_MOB_GRIEFING}, default true), effective only through
 * {@link OreSpawnConfig#fireRespectsMobGriefing()} = master AND key (master-override ruling
 * 2026-09-04: new features register under [modern]; the master off forces classic). While effective,
 * {@code BetterFireball.onHitBlock} places orig BetterFireball.java:261-263's face fire only if
 * {@code EventHooks.canEntityGrief(level, owner)} (vanilla SmallFireball.onHitBlock's gate: the
 * mobGriefing gamerule, through EntityMobGriefingEvent for a non-null owner) and {@code onHit} passes
 * that same value as {@code Level.explode}'s fire flag (vanilla LargeFireball.onHit's) where orig :266
 * passed true; the null source and the MOB interaction stay, so block destruction is the gamerule's in
 * both modes. Master or key off: the ENT-S-104 classic paths, fire always, no event posted.</p>
 *
 * <p>One test, three scenarios in SEQUENCE. The gamerule and the config values are global and the tests
 * of one batch run concurrently, so three rule-flipping tests could not share a batch; instead this
 * class's single test owns the batch {@code fireballModernFire} and runs the scenarios back to back --
 * each the ENT-S-102 / ENT-S-104 flight (a big shot into an obsidian wall over a dirt hearth, read
 * IMPACT_WINDOW_TICKS after launch; helpers copied from ProjectileTypeParityTests) with the arena
 * rebuilt between flights -- and restores the master, the key and the rule in a finally that runs on
 * every path (pass, assertion failure, launch failure):</p>
 * <ul>
 *   <li>(a) master on + key on + mobGriefing off: no face fire, explosion fire flag false, KEEP, wall
 *   and hearth intact;</li>
 *   <li>(b) master on + key on + mobGriefing on: face fire present, fire flag true, a DESTROY kind, the
 *   hearth gone -- the classic result, as s104_big_shot_with_mob_griefing_on_... pins it;</li>
 *   <li>(c) master off + key on + mobGriefing off: face fire present and standing, fire flag true, KEEP,
 *   wall and hearth intact -- classic stays 1.7.10 whatever the key says (the s104 mobGriefing-off
 *   result).</li>
 * </ul>
 *
 * <p>The face fire is witnessed at the blast's {@code ExplosionEvent.Start}: Level.explode posts it
 * after onHitBlock has run (Projectile.onHit's block branch, replayed in BetterFireball.onHit) and
 * before the blast, so the cell's state there is onHitBlock's alone -- a DESTROY blast (b) blows the
 * fire away with the hearth a moment later, exactly as the s104 rule-on pin records. The fire flag is
 * {@code Explosion.fire}, read by reflection as the s104 pins do (no getter; the random 1-in-3 fire it
 * seeds is not observable deterministically, and in this arena the only air-over-solid cell in reach
 * is the face cell itself). After the window the cell is read again: air in (a) (nothing placed, no
 * fire flag to seed any), fire in (c) (a KEEP blast removes no block), air in (b) (blown clear, and no
 * re-seed with the hearth gone). Each scenario asserts its rule and config preconditions at launch and
 * at the check; the key's code default (true) is pinned up front. timeoutTicks 400 for three 40-tick
 * flights. Template {@code empty_large} (48x16x48), shooter at (24, 8, 24), as
 * ProjectileTypeParityTests.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FireballModernFireTests {

    /** empty_large is 48x16x48; the shooter stands at (24, 8, 24) like the ProjectileTypeParityTests spawns. */
    private static final BlockPos POS = new BlockPos(24, 8, 24);
    /** The ProjectileTypeParityTests shooter rotation (immaterial to a +z flight; kept for a like-for-like shooter). */
    private static final float SHOOTER_Y_ROT = 37.0f;
    private static final float SHOOTER_X_ROT = -12.0f;
    private static final float DIM_EPS = 1e-4f;
    /** orig BetterFireball.java:74-76 setBig = 2, the power every flight fires at. */
    private static final int BIG_POWER = 2;
    /**
     * The flights aim straight +z: AbstractHurtingProjectile has no gravity, launch() puts the muzzle 2
     * blocks in front (+z) of the shooter at the block centre (24.5, 9.0, 26.5), and the wall stands at
     * WALL_Z, 3.5 blocks ahead of the muzzle.
     */
    private static final Vec3 WALL_AIM = new Vec3(0.0, 0.0, 1.0);
    private static final int WALL_Z = 30;
    /**
     * Ticks to wait before reading an impact (ENT-S-102's window): from 0.1 blocks/tick the vanilla
     * acceleration crosses the face on the ninth tick; 40 is more than four times that, and the discard
     * at MAX_LIFETIME_TICKS (600) is far outside it, so a removed shot with a live owner can only mean an
     * impact.
     */
    private static final int IMPACT_WINDOW_TICKS = 40;
    private static final double IMPACT_RADIUS_FROM_WALL_FACE = 2.0;
    /**
     * The cell orig BetterFireball.java:232-264 sets fire in: the air side of the hit face. The shot flies
     * +z at feet y = 9.0 through the x = 24 column, strikes the wall block (24, 9, WALL_Z) on its north
     * face; relative(NORTH) is (24, 9, WALL_Z - 1).
     */
    private static final BlockPos FIRE_CELL = new BlockPos(POS.getX(), POS.getY() + 1, WALL_Z - 1);
    /**
     * A dirt block under the fire cell (ENT-S-104): the sturdy base that keeps a placed fire alive inside
     * the window whatever doFireTick says, and the block-interaction witness -- the blast sits directly
     * above it, so a DESTROY blast removes it and a KEEP blast leaves it.
     */
    private static final BlockPos HEARTH = FIRE_CELL.below();
    /** Three flights of IMPACT_WINDOW_TICKS plus scheduling slack. */
    private static final int TIMEOUT_TICKS = 400;

    /**
     * One row of the truth table: the master, the key and the rule to set; then what the impact must
     * show -- the effective helper value, whether onHitBlock's face fire is placed, the explosion's fire
     * flag, and whether the block interaction is KEEP (else a DESTROY kind that removes the hearth).
     */
    private record Scenario(String name, boolean master, boolean key, boolean mobGriefing,
                            boolean effective, boolean faceFire, boolean fireFlag, boolean keep) {
    }

    private static final Scenario KEY_ON_RULE_OFF = new Scenario("(a) master on + key on + mobGriefing off",
            true, true, false, true, false, false, true);
    private static final Scenario KEY_ON_RULE_ON = new Scenario("(b) master on + key on + mobGriefing on",
            true, true, true, true, true, true, false);
    private static final Scenario MASTER_OFF_RULE_OFF = new Scenario("(c) master off + key on + mobGriefing off",
            false, true, false, false, true, true, true);

    /** One explosion started inside the structure, with the face cell's block as it stood when the blast started. */
    private record Impact(Explosion explosion, Block faceCellAtStart) {
    }

    // ---------------------------------------------------------------------------------------
    // Helpers copied from ProjectileTypeParityTests (ENT-S-098 / ENT-S-102 / ENT-S-104).
    // ---------------------------------------------------------------------------------------

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
                "ServerLevel#addFreshEntity refused the shot BetterFireball (MOD-031)");
    }

    /**
     * An obsidian wall across the shot's line: relative x 22..26, y 7..11 at z = WALL_Z. Obsidian (blast
     * resistance 1200) stands under the power-2 blast, so the wall is exactly one impact per flight and
     * survives all three.
     */
    private static void buildWall(GameTestHelper helper) {
        for (int x = POS.getX() - 2; x <= POS.getX() + 2; x++) {
            for (int y = POS.getY() - 1; y <= POS.getY() + 3; y++) {
                helper.setBlock(new BlockPos(x, y, WALL_Z), Blocks.OBSIDIAN);
            }
        }
    }

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
     * 1.21.1 Explosion keeps its fire flag in {@code private final boolean fire} with no getter;
     * finalizeExplosion reads it to seed random fire (1-in-3 per affected air cell over a solid-render
     * block), which cannot be observed deterministically, so the flag itself is read -- as the ENT-S-104
     * pins and KrakenHoldReleaseTests (Entity.random) do.
     */
    private static boolean explosionFire(Explosion explosion) {
        try {
            Field fire = Explosion.class.getDeclaredField("fire");
            fire.setAccessible(true);
            return fire.getBoolean(explosion);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Explosion.fire is not reachable by reflection (1.21.1: private final boolean fire; official names at runtime) (MOD-031)", e);
        }
    }

    /**
     * Records the explosions started inside this test's structure while registered -- one
     * {@code ExplosionEvent.Start} per {@code Level.explode} call, posted before the blast runs -- and,
     * with each, the block standing in the face cell at that moment: onHitBlock has already run (it is
     * the block branch of the onHit dispatch that precedes the explode call), the blast has not, so the
     * cell is onHitBlock's placement alone. Filtered to this level and the structure bounds; registered
     * through the Class overload so the bus needs no generic-type resolution; {@code unregister(listener)}
     * drops exactly this consumer.
     */
    private static final class ExplosionCounter {
        private final ServerLevel level;
        private final AABB bounds;
        private final BlockPos faceCell;
        private final List<Impact> seen = new ArrayList<>();
        private final Consumer<ExplosionEvent.Start> listener = this::onStart;
        private boolean registered;

        ExplosionCounter(GameTestHelper helper) {
            this.level = helper.getLevel();
            this.bounds = helper.getBounds();
            this.faceCell = helper.absolutePos(FIRE_CELL);
            NeoForge.EVENT_BUS.addListener(ExplosionEvent.Start.class, this.listener);
            this.registered = true;
        }

        private void onStart(ExplosionEvent.Start event) {
            if (event.getLevel() == this.level && this.bounds.contains(event.getExplosion().center())) {
                this.seen.add(new Impact(event.getExplosion(), this.level.getBlockState(this.faceCell).getBlock()));
            }
        }

        List<Impact> seen() {
            return this.seen;
        }

        void close() {
            if (this.registered) {
                this.registered = false;
                NeoForge.EVENT_BUS.unregister(this.listener);
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // The sequence: set, fly, check, restore.
    // ---------------------------------------------------------------------------------------

    /**
     * Everything the test flips, read once up front and put back exactly once, on every path: the master,
     * the key and the gamerule (the ModernMasterOverrideTests / KrakenPlayNicelyTests flip-and-restore
     * idiom, spread over the flights), plus the one shooter the three flights share.
     */
    private static final class Restore {
        private final ServerLevel level;
        private final GameRules.BooleanValue mobGriefing;
        private final boolean priorRule;
        private final boolean priorMaster;
        private final boolean priorKey;
        private final LivingEntity shooter;
        private boolean done;

        Restore(ServerLevel level, GameRules.BooleanValue mobGriefing, LivingEntity shooter) {
            this.level = level;
            this.mobGriefing = mobGriefing;
            this.priorRule = mobGriefing.get();
            this.priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
            this.priorKey = OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.get();
            this.shooter = shooter;
        }

        void run() {
            if (this.done) return;
            this.done = true;
            OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.set(this.priorKey);
            OreSpawnConfig.MODERN_ENABLED.set(this.priorMaster);
            this.mobGriefing.set(this.priorRule, this.level.getServer());
            this.shooter.discard();
        }
    }

    /**
     * Runs one step of the sequence; if it does not complete normally (an assertion, a refused launch,
     * the reflection guard) the restore runs first -- the finally of every path. The GameTest framework
     * turns the escaping exception into the test's failure and schedules nothing further, so a failed
     * step is the last one that runs.
     */
    private static void guarded(Restore restore, Runnable step) {
        boolean completed = false;
        try {
            step.run();
            completed = true;
        } finally {
            if (!completed) {
                restore.run();
            }
        }
    }

    private static void assertPreconditions(GameTestHelper helper, Scenario scenario, GameRules.BooleanValue mobGriefing, String when) {
        String tag = " [" + scenario.name() + ", " + when + "] (MOD-031)";
        helper.assertTrue(OreSpawnConfig.MODERN_ENABLED.get() == scenario.master(),
                "precondition: modern.enabled must be " + scenario.master() + tag);
        helper.assertTrue(OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.get() == scenario.key(),
                "precondition: modern.fireRespectsMobGriefing must be " + scenario.key() + tag);
        helper.assertTrue(OreSpawnConfig.fireRespectsMobGriefing() == scenario.effective(),
                "precondition: OreSpawnConfig.fireRespectsMobGriefing() (master && key) must read " + scenario.effective() + tag);
        helper.assertTrue(mobGriefing.get() == scenario.mobGriefing(),
                "precondition: mobGriefing must be " + scenario.mobGriefing() + "; another test flipped it" + tag);
    }

    /** The impact checks IMPACT_WINDOW_TICKS after a flight: what the ENT-S-102 / ENT-S-104 pins read, keyed on the scenario's row. */
    private static void assertImpact(GameTestHelper helper, Scenario scenario, LivingEntity shooter, BetterFireball shot,
                                     ExplosionCounter explosions, Vec3 wallFace) {
        String tag = " [" + scenario.name() + "] (MOD-031)";
        helper.assertFalse(shooter.isRemoved(), "the shooter must outlive the window, else the discard check below is vacuous" + tag);
        helper.assertTrue(shot.isRemoved(),
                "the big shot must have struck the wall and been discarded (orig BetterFireball.java:268) inside "
                        + IMPACT_WINDOW_TICKS + " ticks" + tag);
        helper.assertValueEqual(explosions.seen().size(), 1,
                "explosions started inside the structure by one big shot (orig :265-267: one; ENT-S-102)" + tag);
        Impact impact = explosions.seen().get(0);
        Explosion only = impact.explosion();
        helper.assertTrue(Math.abs(only.radius() - BIG_POWER) < DIM_EPS,
                "the explosion must be the port's, at setBig's power " + BIG_POWER + "; got radius " + only.radius() + tag);
        helper.assertTrue(only.getDirectSourceEntity() == null,
                "the explosion must keep orig :266's null source in both modes; got " + only.getDirectSourceEntity() + tag);
        helper.assertTrue(only.center().distanceTo(wallFace) < IMPACT_RADIUS_FROM_WALL_FACE,
                "the explosion must sit at the shot's impact position by the wall face " + wallFace + ", got " + only.center() + tag);

        // The face fire, as onHitBlock left it, witnessed when the blast started.
        if (scenario.faceFire()) {
            helper.assertTrue(impact.faceCellAtStart() == Blocks.FIRE,
                    "fire must stand on the air side of the hit face " + FIRE_CELL + " when the blast starts (orig BetterFireball.java:261-263, "
                            + "BetterFireball.onHitBlock; classic placement); the cell held " + impact.faceCellAtStart() + tag);
        } else {
            helper.assertTrue(impact.faceCellAtStart() == Blocks.AIR,
                    "no fire may be placed on the air side of the hit face " + FIRE_CELL + " with the option effective and mobGriefing off "
                            + "(SmallFireball.onHitBlock's canEntityGrief gate); when the blast started the cell held " + impact.faceCellAtStart() + tag);
        }

        // The explosion's fire flag: orig :266's true in classic, canEntityGrief(level, owner) when effective.
        boolean fire = explosionFire(only);
        helper.assertTrue(fire == scenario.fireFlag(),
                "the explosion's fire flag must be " + scenario.fireFlag() + " (classic: orig :266 passed true unconditionally; effective option: "
                        + "LargeFireball.onHit's canEntityGrief(level, owner) = the gamerule), got " + fire + tag);

        // The block interaction is the gamerule's in both modes (Level.explode: MOB -> canEntityGrief(level, null)).
        if (scenario.keep()) {
            helper.assertTrue(only.getBlockInteraction() == Explosion.BlockInteraction.KEEP && !only.interactsWithBlocks(),
                    "with mobGriefing off the block interaction must be KEEP in both modes, got " + only.getBlockInteraction() + tag);
            assertWallIntact(helper, " -- the obsidian wall must be intact after a KEEP blast" + tag);
            helper.assertBlock(HEARTH, block -> block == Blocks.DIRT,
                    "the dirt hearth under the fire cell must survive a KEEP blast" + tag);
            if (scenario.faceFire()) {
                helper.assertBlock(FIRE_CELL, block -> block == Blocks.FIRE,
                        "fire must remain on the air side of the hit face " + FIRE_CELL + " (a KEEP blast removes no block; classic stays 1.7.10)" + tag);
            } else {
                helper.assertBlock(FIRE_CELL, block -> block == Blocks.AIR,
                        "the face cell " + FIRE_CELL + " must still be air after the window: nothing was placed and a fire = false blast seeds none" + tag);
            }
        } else {
            helper.assertTrue(only.interactsWithBlocks() && only.getBlockInteraction() != Explosion.BlockInteraction.KEEP,
                    "with mobGriefing on the block interaction must be a DESTROY kind (Level.explode: MOB -> the gamerule -> getDestroyType), got "
                            + only.getBlockInteraction() + tag);
            helper.assertBlock(HEARTH, block -> block == Blocks.AIR,
                    "the dirt hearth directly under a power-2 DESTROY blast must be gone (the mobGriefing witness; the classic result)" + tag);
            helper.assertBlock(FIRE_CELL, block -> block == Blocks.AIR,
                    "the face fire placed before the blast is blown away by a DESTROY blast and cannot be re-lit with the hearth gone "
                            + "(the s104 rule-on result)" + tag);
            assertWallIntact(helper, " -- obsidian (resistance 1200) must stand under the power-2 blast" + tag);
        }
    }

    /**
     * One scenario: set the master, the key and the rule, assert them, rebuild the arena (wall, hearth,
     * the face cell cleared), fire a big shot as the boss sites do (setNotMe, setBig) with the counter
     * registered before launch, and IMPACT_WINDOW_TICKS later re-assert the preconditions, run the impact
     * checks, unregister and discard the shot, then hand over to {@code next}. Every step is guarded, so
     * the restore runs on any failure.
     */
    private static void runScenario(GameTestHelper helper, Restore restore, LivingEntity shooter, Scenario scenario, Runnable next) {
        guarded(restore, () -> {
            ServerLevel level = helper.getLevel();
            GameRules.BooleanValue mobGriefing = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
            OreSpawnConfig.MODERN_ENABLED.set(scenario.master());
            OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.set(scenario.key());
            mobGriefing.set(scenario.mobGriefing(), level.getServer());
            assertPreconditions(helper, scenario, mobGriefing, "at launch");

            buildWallAndHearth(helper);
            helper.setBlock(FIRE_CELL, Blocks.AIR);
            helper.assertTrue(level.isEmptyBlock(helper.absolutePos(FIRE_CELL)),
                    "precondition: the fire cell " + FIRE_CELL + " must start as air [" + scenario.name() + "] (MOD-031)");
            helper.assertBlock(HEARTH, block -> block == Blocks.DIRT,
                    "precondition: the dirt hearth must be in place [" + scenario.name() + "] (MOD-031)");
            helper.assertFalse(shooter.isRemoved(), "precondition: the shared shooter must be alive at launch [" + scenario.name() + "] (MOD-031)");

            BetterFireball shot = new BetterFireball(level, shooter, WALL_AIM);
            shot.setNotMe();
            shot.setBig();
            Vec3 wallFace = helper.absoluteVec(new Vec3(POS.getX() + 0.5, POS.getY() + 1.0, WALL_Z));
            ExplosionCounter explosions = new ExplosionCounter(helper);
            try {
                launch(helper, shooter, shot);
            } catch (RuntimeException e) {
                explosions.close();
                shot.discard();
                throw e;
            }
            helper.runAfterDelay(IMPACT_WINDOW_TICKS, () -> guarded(restore, () -> {
                try {
                    assertPreconditions(helper, scenario, mobGriefing, "at the check");
                    assertImpact(helper, scenario, shooter, shot, explosions, wallFace);
                } finally {
                    explosions.close();
                    shot.discard();
                }
                next.run();
            }));
        });
    }

    /**
     * The owner-ruled pin, three scenarios in sequence (see the class comment): (a) key on + master on +
     * mobGriefing off -> no face fire, fire flag false, KEEP; (b) key on + master on + mobGriefing on ->
     * face fire, fire flag true, DESTROY (the classic result); (c) master off + mobGriefing off -> face
     * fire, fire flag true, KEEP (classic stays 1.7.10). The key's code default is pinned first: true,
     * per the ruling "default on". Master, key and rule are restored, and the shared shooter discarded,
     * once, on every path.
     */
    @GameTest(template = "empty_large", timeoutTicks = TIMEOUT_TICKS, batch = "fireballModernFire")
    public void mod031_fire_respects_mob_griefing_three_scenarios_in_sequence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameRules.BooleanValue mobGriefing = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
        LivingEntity shooter = spawnShooter(helper);
        shooter.setNoGravity(true);
        Restore restore = new Restore(level, mobGriefing, shooter);
        guarded(restore, () -> {
            helper.assertTrue(OreSpawnConfig.MODERN_FIRE_RESPECTS_MOB_GRIEFING.getDefault(),
                    "modern.fireRespectsMobGriefing must default to true in code (owner ruling 2026-09-04: "
                            + "\"accepted as a modern option, default on; classic stays 1.7.10\") (MOD-031)");
            runScenario(helper, restore, shooter, KEY_ON_RULE_OFF, () ->
                    runScenario(helper, restore, shooter, KEY_ON_RULE_ON, () ->
                            runScenario(helper, restore, shooter, MASTER_OFF_RULE_OFF, () -> {
                                restore.run();
                                helper.succeed();
                            })));
        });
    }
}
