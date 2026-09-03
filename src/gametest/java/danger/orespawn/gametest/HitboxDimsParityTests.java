package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-095 batch 1: the 1.7.10 hitbox dimensions of the plain entities -- the
 * ones whose original {@code setSize} is a single unconditional constructor
 * call with no PlayNicely size branch, no boss bar and no part entities. The
 * port's {@code .sized} registrations drifted from those values; ENT-S-095
 * restores them. Owner ruling (2026-09-03): "every change gets a both-modes
 * dims-pin test", so each entity here is pinned under BOTH flag states, and
 * for a batch-1 entity both states must read the same 1.7.10 box.
 *
 * <p>Every expected value is the original's {@code setSize} float literal
 * (phase_g_reports/ents095_split.md, batch 1), cited on each test and in its
 * failure message; the current {@code ModEntities} value is deliberately not
 * consulted. Two rows do not cite a setSize of their own: {@code jeffery}
 * (1.7.10 Jeffery IS GiantRobot, orig GiantRobot.java:46) and
 * {@code ruby_bird} (RubyBird.java:9-31 inherits Cockateil.java:42).</p>
 *
 * <p>What one test does, synchronously in a single tick (the documented safe
 * contract of TEST-003 / SpawnGateTests): read the flag; set
 * {@code playNicely=false}, spawn, assert {@code getBbWidth/Height} and the
 * live {@link AABB} extents equal the 1.7.10 box within {@value #EPS}; set
 * {@code playNicely=true}, {@code refreshDimensions()} the first instance
 * (live read: no size branch may appear), spawn a fresh instance (ctor
 * snapshot), assert both; discard both and restore the flag in a
 * {@code finally}. Mobs are frozen the way VortexParityTests /
 * KrakenPlayNicelyTests freeze theirs (free will removed, noAi, persistence
 * required); the two projectiles are spawned through the same
 * {@code GameTestHelper#spawn(EntityType, BlockPos)} path, which accepts any
 * entity type (PressurePlateParityTests idiom).</p>
 *
 * <p>Batch (TEST-003): {@code OreSpawnConfig.PLAY_NICELY} is GLOBAL, so this
 * class declares its own batch, {@code hitboxDimsParity}, and never holds the
 * flag across a tick. Template {@code empty_large} is 48x16x48 with the spawn
 * point at (24, 8, 24); giant_robot and jeffery (9.75 tall) top out at 17.75,
 * above the template, which only the framework's cleanup cares about (origin
 * inside) -- the same situation as the 15-tall Kraken pin.</p>
 *
 * <p>The four cows are a separately marked group at the end: their 1.7.10
 * baseline is the vanilla EntityCow constructor size 0.9x1.3 (the OreSpawn
 * cow classes set no size), recalled without vanilla sources in the reference
 * dump -- "vanilla EntityCow baseline 0.9 x 1.3, proven from the Mojang 1.7.10 jar (class wh constructor: ldc 0.9f, ldc 1.3f, setSize)". They are
 * annotated like the rest so the pin exists; if the baseline is refuted the
 * four expected values (or annotations) are what changes.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HitboxDimsParityTests {

    private static final float EPS = 1e-4f;

    /** empty_large is 48x16x48; feet at y=8 (see the class note on the two 9.75-tall robots). */
    private static final BlockPos POS = new BlockPos(24, 8, 24);

    /**
     * Spawns one instance at {@link #POS} and freezes it: Mobs lose their free
     * will, AI and despawn eligibility exactly as {@code spawnWithNoFreeWill}
     * + {@code setNoAi(true)} + {@code setPersistenceRequired()} would give
     * them; non-Mob types (the projectiles) are simply added.
     */
    private static Entity spawnFrozen(GameTestHelper helper, EntityType<?> type) {
        Entity entity = helper.spawn(type, POS);
        if (entity instanceof Mob mob) {
            mob.removeFreeWill();
            mob.setNoAi(true);
            mob.setPersistenceRequired();
        }
        return entity;
    }

    private static void assertDims(GameTestHelper helper, Entity entity, float width, float height,
                                   String id, String origCitation, String mode) {
        AABB box = entity.getBoundingBox();
        helper.assertTrue(Math.abs(entity.getBbWidth() - width) < EPS
                        && Math.abs(entity.getBbHeight() - height) < EPS,
                "orespawn:" + id + " " + mode + " must be " + width + "x" + height + " (" + origCitation
                        + "), got " + entity.getBbWidth() + "x" + entity.getBbHeight() + " (ENT-S-095)");
        helper.assertTrue(Math.abs(box.getXsize() - width) < EPS
                        && Math.abs(box.getYsize() - height) < EPS
                        && Math.abs(box.getZsize() - width) < EPS,
                "orespawn:" + id + " " + mode + " bounding box not " + width + "x" + height + "x" + width
                        + " (" + origCitation + "), got " + box.getXsize() + "x" + box.getYsize() + "x"
                        + box.getZsize() + " (ENT-S-095)");
    }

    /**
     * The both-modes pin, synchronous in one tick: flag off -> spawn -> assert;
     * flag on -> refresh the first (live read) and spawn a second (ctor
     * snapshot) -> assert both; discard both and restore the flag in a finally.
     * A batch-1 entity has no PlayNicely size branch, so all three reads must
     * be the same 1.7.10 box.
     */
    private static void pinBothModes(GameTestHelper helper, EntityType<?> type, float expectedW, float expectedH,
                                     String id, String origCitation) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Entity normal = null;
        Entity nice = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            normal = spawnFrozen(helper, type);
            assertDims(helper, normal, expectedW, expectedH, id, origCitation,
                    "constructed with playNicely=false");

            OreSpawnConfig.PLAY_NICELY.set(true);
            normal.refreshDimensions();
            assertDims(helper, normal, expectedW, expectedH, id, origCitation,
                    "after refreshDimensions() with playNicely=true (live read, no size branch expected)");
            nice = spawnFrozen(helper, type);
            assertDims(helper, nice, expectedW, expectedH, id, origCitation,
                    "constructed with playNicely=true");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (normal != null) normal.discard();
            if (nice != null) nice.discard();
        }
        helper.succeed();
    }

    // ================================================================
    // Monsters
    // ================================================================

    /** orig DungeonBeast.java:48 setSize(1.15f, 1.1f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_dungeon_beast_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.DUNGEON_BEAST.get(), 1.15f, 1.1f, "dungeon_beast",
                "orig DungeonBeast.java:48 setSize(1.15f, 1.1f)");
    }

    /** orig EnderKnight.java:37 setSize(0.6f, 2.9f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_ender_knight_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENDER_KNIGHT.get(), 0.6f, 2.9f, "ender_knight",
                "orig EnderKnight.java:37 setSize(0.6f, 2.9f)");
    }

    /** orig EnderReaper.java:37 setSize(0.7f, 2.9f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_ender_reaper_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENDER_REAPER.get(), 0.7f, 2.9f, "ender_reaper",
                "orig EnderReaper.java:37 setSize(0.7f, 2.9f)");
    }

    /** orig GiantRobot.java:46 setSize(3.0f, 9.75f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_giant_robot_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.GIANT_ROBOT.get(), 3.0f, 9.75f, "giant_robot",
                "orig GiantRobot.java:46 setSize(3.0f, 9.75f)");
    }

    /** orig GiantRobot.java:46 setSize(3.0f, 9.75f) -- 1.7.10 Jeffery IS GiantRobot (ENT-D-042/WGEN-017), same class family as giant_robot. */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_jeffery_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.JEFFERY.get(), 3.0f, 9.75f, "jeffery",
                "orig GiantRobot.java:46 setSize(3.0f, 9.75f) -- 1.7.10 Jeffery IS GiantRobot (ENT-D-042/WGEN-017), same class family as giant_robot");
    }

    /** orig Hammerhead.java:44 setSize(3.0f, 5.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_hammerhead_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.HAMMERHEAD.get(), 3.0f, 5.0f, "hammerhead",
                "orig Hammerhead.java:44 setSize(3.0f, 5.0f)");
    }

    /** orig Irukandji.java:42 setSize(0.25f, 0.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_irukandji_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.IRUKANDJI.get(), 0.25f, 0.25f, "irukandji",
                "orig Irukandji.java:42 setSize(0.25f, 0.25f)");
    }

    /** orig Robot1.java:39 setSize(0.5f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_robot_1_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ROBOT_1.get(), 0.5f, 0.5f, "robot_1",
                "orig Robot1.java:39 setSize(0.5f, 0.5f)");
    }

    /** orig Robot2.java:45 setSize(3.0f, 6.2f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_robot_2_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ROBOT_2.get(), 3.0f, 6.2f, "robot_2",
                "orig Robot2.java:45 setSize(3.0f, 6.2f)");
    }

    /** orig Robot3.java:46 setSize(2.5f, 5.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_robot_3_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ROBOT_3.get(), 2.5f, 5.0f, "robot_3",
                "orig Robot3.java:46 setSize(2.5f, 5.0f)");
    }

    /** orig Robot4.java:49 setSize(2.5f, 4.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_robot_4_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ROBOT_4.get(), 2.5f, 4.0f, "robot_4",
                "orig Robot4.java:49 setSize(2.5f, 4.0f)");
    }

    /** orig Robot5.java:45 setSize(1.0f, 2.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_robot_5_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ROBOT_5.get(), 1.0f, 2.25f, "robot_5",
                "orig Robot5.java:45 setSize(1.0f, 2.25f)");
    }

    /** orig SeaMonster.java:50 setSize(1.25f, 2.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_sea_monster_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.SEA_MONSTER.get(), 1.25f, 2.5f, "sea_monster",
                "orig SeaMonster.java:50 setSize(1.25f, 2.5f)");
    }

    /** orig Skate.java:43 setSize(0.75f, 0.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_skate_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.SKATE.get(), 0.75f, 0.25f, "skate",
                "orig Skate.java:43 setSize(0.75f, 0.25f)");
    }

    /** orig Urchin.java:50 setSize(1.35f, 2.1f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_urchin_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.URCHIN.get(), 1.35f, 2.1f, "urchin",
                "orig Urchin.java:50 setSize(1.35f, 2.1f)");
    }

    /** orig EmperorScorpion.java:59 setSize(3.5f, 3.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_emperor_scorpion_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_EMPEROR_SCORPION.get(), 3.5f, 3.0f, "emperor_scorpion",
                "orig EmperorScorpion.java:59 setSize(3.5f, 3.0f)");
    }

    /** orig HerculesBeetle.java:46 setSize(3.25f, 2.75f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_hercules_beetle_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_HERCULES_BEETLE.get(), 3.25f, 2.75f, "hercules_beetle",
                "orig HerculesBeetle.java:46 setSize(3.25f, 2.75f)");
    }

    /** orig Kyuubi.java:44 setSize(0.5f, 1.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_kyuubi_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_KYUUBI.get(), 0.5f, 1.25f, "kyuubi",
                "orig Kyuubi.java:44 setSize(0.5f, 1.25f)");
    }

    /** orig LeafMonster.java:41 setSize(1.0f, 2.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_leaf_monster_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_LEAF_MONSTER.get(), 1.0f, 2.5f, "leaf_monster",
                "orig LeafMonster.java:41 setSize(1.0f, 2.5f)");
    }

    /** orig LurkingTerror.java:53 setSize(1.75f, 1.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_lurking_terror_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_LURKING_TERROR.get(), 1.75f, 1.25f, "lurking_terror",
                "orig LurkingTerror.java:53 setSize(1.75f, 1.25f)");
    }

    /** orig Mantis.java:57 setSize(2.5f, 3.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_mantis_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_MANTIS.get(), 2.5f, 3.25f, "mantis",
                "orig Mantis.java:57 setSize(2.5f, 3.25f)");
    }

    /** orig Molenoid.java:43 setSize(3.9f, 2.6f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_molenoid_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_MOLENOID.get(), 3.9f, 2.6f, "molenoid",
                "orig Molenoid.java:43 setSize(3.9f, 2.6f)");
    }

    /** orig Rat.java:52 setSize(0.25f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_rat_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_RAT.get(), 0.25f, 0.5f, "rat",
                "orig Rat.java:52 setSize(0.25f, 0.5f)");
    }

    /** orig Rotator.java:56 setSize(1.0f, 2.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_rotator_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_ROTATOR.get(), 1.0f, 2.0f, "rotator",
                "orig Rotator.java:56 setSize(1.0f, 2.0f)");
    }

    /** orig Scorpion.java:50 setSize(0.85f, 0.55f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_scorpion_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_SCORPION.get(), 0.85f, 0.55f, "scorpion",
                "orig Scorpion.java:50 setSize(0.85f, 0.55f)");
    }

    /** orig SpitBug.java:56 setSize(2.0f, 2.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_spit_bug_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_SPIT_BUG.get(), 2.0f, 2.0f, "spit_bug",
                "orig SpitBug.java:56 setSize(2.0f, 2.0f)");
    }

    /** orig TerribleTerror.java:51 setSize(1.0f, 0.75f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_terrible_terror_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_TERRIBLE_TERROR.get(), 1.0f, 0.75f, "terrible_terror",
                "orig TerribleTerror.java:51 setSize(1.0f, 0.75f)");
    }

    /** orig Triffid.java:49 setSize(2.0f, 4.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_triffid_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_TRIFFID.get(), 2.0f, 4.0f, "triffid",
                "orig Triffid.java:49 setSize(2.0f, 4.0f)");
    }

    /** orig TrooperBug.java:58 setSize(3.0f, 3.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_trooper_bug_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_TROOPER_BUG.get(), 3.0f, 3.5f, "trooper_bug",
                "orig TrooperBug.java:58 setSize(3.0f, 3.5f)");
    }

    /** orig WormSmall.java:27 setSize(0.25f, 1.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_worm_small_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_WORM_SMALL.get(), 0.25f, 1.0f, "worm_small",
                "orig WormSmall.java:27 setSize(0.25f, 1.0f)");
    }

    /** orig WormMedium.java:29 setSize(0.5f, 2.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_worm_medium_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_WORM_MEDIUM.get(), 0.5f, 2.0f, "worm_medium",
                "orig WormMedium.java:29 setSize(0.5f, 2.0f)");
    }

    /** orig WormLarge.java:42 setSize(1.55f, 2.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_worm_large_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_WORM_LARGE.get(), 1.55f, 2.5f, "worm_large",
                "orig WormLarge.java:42 setSize(1.55f, 2.5f)");
    }

    // ================================================================
    // Animals (AgeableMob: age 0 at construction -> adult 1.0x scale; the vanilla 0.5x baby halving is on both sides and is not a divergence)
    // ================================================================

    /** orig Baryonyx.java:40 setSize(1.5f, 2.8f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_baryonyx_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.BARYONYX.get(), 1.5f, 2.8f, "baryonyx",
                "orig Baryonyx.java:40 setSize(1.5f, 2.8f)");
    }

    /** orig Cockateil.java:42 setSize(0.5f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_cockateil_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.COCKATEIL.get(), 0.5f, 0.5f, "cockateil",
                "orig Cockateil.java:42 setSize(0.5f, 0.5f)");
    }

    /** orig Coin.java:27 setSize(1.5f, 1.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_coin_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.COIN.get(), 1.5f, 1.5f, "coin",
                "orig Coin.java:27 setSize(1.5f, 1.5f)");
    }

    /** orig EasterBunny.java:35 setSize(0.5f, 0.75f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_easter_bunny_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.EASTER_BUNNY.get(), 0.5f, 0.75f, "easter_bunny",
                "orig EasterBunny.java:35 setSize(0.5f, 0.75f)");
    }

    /** orig Flounder.java:39 setSize(0.55f, 0.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_flounder_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.FLOUNDER.get(), 0.55f, 0.25f, "flounder",
                "orig Flounder.java:39 setSize(0.55f, 0.25f)");
    }

    /** orig GoldFish.java:25 setSize(0.75f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_gold_fish_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.GOLD_FISH.get(), 0.75f, 0.5f, "gold_fish",
                "orig GoldFish.java:25 setSize(0.75f, 0.5f)");
    }

    /** orig EntityAnt.java:39 setSize(0.1f, 0.1f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_ant_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_ANT.get(), 0.1f, 0.1f, "ant",
                "orig EntityAnt.java:39 setSize(0.1f, 0.1f)");
    }

    /** orig CliffRacer.java:26 setSize(0.75f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_cliff_racer_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_CLIFF_RACER.get(), 0.75f, 0.5f, "cliff_racer",
                "orig CliffRacer.java:26 setSize(0.75f, 0.5f)");
    }

    /** orig Cricket.java:24 setSize(0.1f, 0.1f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_cricket_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_CRICKET.get(), 0.1f, 0.1f, "cricket",
                "orig Cricket.java:24 setSize(0.1f, 0.1f)");
    }

    /** orig Dragonfly.java:40 setSize(1.5f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_dragonfly_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_DRAGONFLY.get(), 1.5f, 0.5f, "dragonfly",
                "orig Dragonfly.java:40 setSize(1.5f, 0.5f)");
    }

    /** orig EntityRedAnt.java:33 setSize(0.2f, 0.2f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_red_ant_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_RED_ANT.get(), 0.2f, 0.2f, "red_ant",
                "orig EntityRedAnt.java:33 setSize(0.2f, 0.2f)");
    }

    /** orig EntityRainbowAnt.java:25 setSize(0.1f, 0.1f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_rainbow_ant_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_RAINBOW_ANT.get(), 0.1f, 0.1f, "rainbow_ant",
                "orig EntityRainbowAnt.java:25 setSize(0.1f, 0.1f)");
    }

    /** orig StinkBug.java:43 setSize(0.55f, 0.55f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_stink_bug_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_STINK_BUG.get(), 0.55f, 0.55f, "stink_bug",
                "orig StinkBug.java:43 setSize(0.55f, 0.55f)");
    }

    /** orig Termite.java:46 setSize(0.2f, 0.2f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_termite_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_TERMITE.get(), 0.2f, 0.2f, "termite",
                "orig Termite.java:46 setSize(0.2f, 0.2f)");
    }

    /** orig Tshirt.java:21 setSize(4.0f, 4.0f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_tshirt_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_TSHIRT.get(), 4.0f, 4.0f, "tshirt",
                "orig Tshirt.java:21 setSize(4.0f, 4.0f)");
    }

    /** orig EntityUnstableAnt.java:25 setSize(0.1f, 0.1f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_unstable_ant_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_UNSTABLE_ANT.get(), 0.1f, 0.1f, "unstable_ant",
                "orig EntityUnstableAnt.java:25 setSize(0.1f, 0.1f)");
    }

    /** orig Cockateil.java:42 setSize(0.5f, 0.5f) -- RubyBird.java:9-31 sets no size of its own and inherits Cockateil's. */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_ruby_bird_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.RUBY_BIRD.get(), 0.5f, 0.5f, "ruby_bird",
                "orig Cockateil.java:42 setSize(0.5f, 0.5f) -- RubyBird.java:9-31 sets no size of its own and inherits Cockateil's");
    }

    // ================================================================
    // Tamable animals
    // ================================================================

    /** orig Dragon.java:102 setSize(1.5f, 1.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_dragon_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.DRAGON.get(), 1.5f, 1.25f, "dragon",
                "orig Dragon.java:102 setSize(1.5f, 1.25f)");
    }

    /** orig Lizard.java:57 setSize(1.5f, 1.25f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_lizard_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.LIZARD.get(), 1.5f, 1.25f, "lizard",
                "orig Lizard.java:57 setSize(1.5f, 1.25f)");
    }

    /** orig RubberDucky.java:63 setSize(0.33f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_rubber_ducky_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_RUBBER_DUCKY.get(), 0.33f, 0.5f, "rubber_ducky",
                "orig RubberDucky.java:63 setSize(0.33f, 0.5f)");
    }

    // ================================================================
    // Ambient creatures
    // ================================================================

    /** orig EntityLunaMoth.java:27 setSize(0.5f, 0.5f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_luna_moth_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_LUNA_MOTH.get(), 0.5f, 0.5f, "luna_moth",
                "orig EntityLunaMoth.java:27 setSize(0.5f, 0.5f)");
    }

    /** orig EntityMosquito.java:23 setSize(0.2f, 0.2f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_mosquito_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENTITY_MOSQUITO.get(), 0.2f, 0.2f, "mosquito",
                "orig EntityMosquito.java:23 setSize(0.2f, 0.2f)");
    }

    /** orig Firefly.java:29 setSize(0.4f, 0.8f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_firefly_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.FIREFLY.get(), 0.4f, 0.8f, "firefly",
                "orig Firefly.java:29 setSize(0.4f, 0.8f)");
    }

    // ================================================================
    // Plain Mobs
    // ================================================================

    /** orig PurplePower.java:40 setSize(0.75f, 0.75f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_purple_power_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.PURPLE_POWER.get(), 0.75f, 0.75f, "purple_power",
                "orig PurplePower.java:40 setSize(0.75f, 0.75f)");
    }

    /** orig RockBase.java:26 setSize(0.25f, 0.15f). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_rock_base_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ROCK_BASE.get(), 0.25f, 0.15f, "rock_base",
                "orig RockBase.java:26 setSize(0.25f, 0.15f)");
    }

    // ================================================================
    // Projectiles (outside the ENT-S-095 mob table; spawned through helper.spawn on the EntityType, no Mob freezing)
    // ================================================================

    /** orig BetterFireball.java:48/:57 setSize(1.0f, 1.0f) in both ctors (setSmall :84 0.3125x0.3125 is a separate call, not pinned here). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_better_fireball_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.BETTER_FIREBALL.get(), 1.0f, 1.0f, "better_fireball",
                "orig BetterFireball.java:48/:57 setSize(1.0f, 1.0f) in both ctors (setSmall :84 0.3125x0.3125 is a separate call, not pinned here)");
    }

    /** orig BerthaHit.java:33 setSize(0.33f, 0.33f) in the shooter ctor (the World-only ctor :27 keeps EntityThrowable's 0.25). */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_bertha_hit_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.BERTHA_HIT.get(), 0.33f, 0.33f, "bertha_hit",
                "orig BerthaHit.java:33 setSize(0.33f, 0.33f) in the shooter ctor (the World-only ctor :27 keeps EntityThrowable's 0.25)");
    }

    // ================================================================
    // COWS -- separately marked group: vanilla EntityCow baseline 0.9 x 1.3, proven from the Mojang 1.7.10 jar (class wh constructor: ldc 0.9f, ldc 1.3f, setSize).
    // The 1.7.10 RedCow/GoldCow/CrystalCow/EnchantedCow set no size; 0.9x1.3 is the vanilla
    // 1.7.10 EntityCow ctor size recalled without vanilla sources in the reference dump
    // (ents095_split caveat 3). Another agent may re-point or disable these four once the
    // owner confirms the baseline; the 0.1 height delta is the whole finding.
    // ================================================================

    /** vanilla EntityCow baseline 0.9 x 1.3, proven from the Mojang 1.7.10 jar (class wh constructor: ldc 0.9f, ldc 1.3f, setSize) -- vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- RedCow sets no size of its own. */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_red_cow_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.RED_COW.get(), 0.9f, 1.3f, "red_cow",
                "vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- RedCow sets no size of its own; baseline proven from the Mojang 1.7.10 jar");
    }

    /** vanilla EntityCow baseline 0.9 x 1.3, proven from the Mojang 1.7.10 jar (class wh constructor: ldc 0.9f, ldc 1.3f, setSize) -- vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- GoldCow sets no size of its own. */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_gold_cow_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.GOLD_COW.get(), 0.9f, 1.3f, "gold_cow",
                "vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- GoldCow sets no size of its own; baseline proven from the Mojang 1.7.10 jar");
    }

    /** vanilla EntityCow baseline 0.9 x 1.3, proven from the Mojang 1.7.10 jar (class wh constructor: ldc 0.9f, ldc 1.3f, setSize) -- vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- CrystalCow sets no size of its own. */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_crystal_cow_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.CRYSTAL_COW.get(), 0.9f, 1.3f, "crystal_cow",
                "vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- CrystalCow sets no size of its own; baseline proven from the Mojang 1.7.10 jar");
    }

    /** vanilla EntityCow baseline 0.9 x 1.3, proven from the Mojang 1.7.10 jar (class wh constructor: ldc 0.9f, ldc 1.3f, setSize) -- vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- EnchantedCow sets no size of its own. */
    @GameTest(template = "empty_large", batch = "hitboxDimsParity")
    public void s095_enchanted_apple_cow_dims_both_modes(GameTestHelper helper) {
        pinBothModes(helper, ModEntities.ENCHANTED_APPLE_COW.get(), 0.9f, 1.3f, "enchanted_apple_cow",
                "vanilla 1.7.10 EntityCow ctor setSize(0.9f, 1.3f) -- EnchantedCow sets no size of its own; baseline proven from the Mojang 1.7.10 jar");
    }
}
