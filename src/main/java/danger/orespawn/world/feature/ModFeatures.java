package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * DeferredRegister for OreSpawn's custom {@link Feature} types. Each entry
 * here corresponds to a {@code data/orespawn/worldgen/configured_feature/*.json}
 * referencing it via its registry id (e.g. {@code "type": "orespawn:mantis_nest"}).
 *
 * <p>Registered on the mod event bus from {@link OreSpawnMod}.</p>
 */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, MantisNestFeature> MANTIS_NEST =
            FEATURES.register("mantis_nest", () -> new MantisNestFeature(NoneFeatureConfiguration.CODEC));

    // TEST-003 (2026-08-10): chunk-border-safe replacement for the deprecated
    // vanilla lake feature — the classic Village/Mining water+lava lakes
    // crashed worldgen when a corner-hugging lake's ice freeze-check sampled
    // a biome beyond the guaranteed region (see SafeLakeFeature Javadoc).
    public static final DeferredHolder<Feature<?>, SafeLakeFeature> SAFE_LAKE =
            FEATURES.register("safe_lake", () -> new SafeLakeFeature(
                    net.minecraft.world.level.levelgen.feature.LakeFeature.Configuration.CODEC));

    public static final DeferredHolder<Feature<?>, BeehiveFeature> BEEHIVE =
            FEATURES.register("beehive", () -> new BeehiveFeature(NoneFeatureConfiguration.CODEC));

    // Phase D5 (WGEN-005) — the original ~105-type SpawnOres vein pool
    // (orig OreSpawnWorld.java:355-803 / ChunkOreGenerator.java:21-469),
    // replacing the interim Phase C7 dragon/kraken + ancient-dried-egg
    // redesign (PN-010, retired). Config: count_dice 20 (overworld) / 30
    // (dims), passes 3 for the Mining triple pass (ChunkProviderOreSpawn2
    // .java:191-195).
    public static final DeferredHolder<Feature<?>, SpawnOresPoolFeature> SPAWN_ORES_POOL =
            FEATURES.register("spawn_ores_pool",
                    () -> new SpawnOresPoolFeature(SpawnOresPoolFeature.Configuration.CODEC));

    // QA Fix Part 5 — Small Beehive (the "skep" surface variant).
    // Authentic port of GenericDungeon.makeSmallBeeHive (1.7.10 line
    // 1363). The legacy addANest 50/50 branch (OreSpawnWorld.java:1010)
    // calls this in Forest/Jungle biomes alongside the Mantis Hive; the
    // 1.21.1 port previously shipped only the deep makeBeeHive variant,
    // so QA flagged "only one beehive variant generates" — restoring
    // the missing surface skep here.
    public static final DeferredHolder<Feature<?>, SmallBeehiveFeature> SMALL_BEEHIVE =
            FEATURES.register("small_beehive", () -> new SmallBeehiveFeature(NoneFeatureConfiguration.CODEC));

    // Audit Part 4 — King + Queen Challenge Tower promoted from a Feature
    // wrapper to LegacyDungeonStructure (dungeon_type=KING_TOWER /
    // QUEEN_TOWER). The 1.7.10 sources (GenericDungeon.makeEnormousCastle
    // line 191 / makeEnormousCastleQ line 6393) build a 28x28 base + six
    // stacked floors stepping inward (10/10/9/9/8/16 tall), a 4-block
    // foundation skirt, a ~38-block western spire arm + descending stair,
    // and ~90-block total height — far exceeding the WorldGenRegion 24-block
    // write window. The full byte-for-byte port (gauntlet spawners, central
    // decoration rooms, Royal Guardian loot) now lives in
    // LegacyDungeonPiece#generateChallengeTower. The previous
    // ChallengeTowerFeature was a procedural fabrication (stone-brick
    // base, 2 stacked spawners instead of 4 corner stacks) and has been
    // deleted along with its configured_feature / placed_feature JSONs to
    // enforce the audit's "no procedural fabrications" rule.

    // Audit Part 2 — White House promoted from a Feature to a dedicated
    // Structure + StructurePiece (LegacyDungeonStructure with
    // dungeon_type=WHITE_HOUSE). The Feature wrapper sheared the 25x25
    // base + 13-tier roof + flagpole at chunk borders; LegacyDungeonPiece
    // uses the canonical chunkBox.isInside multi-pass stitching pattern.

    // Audit Part 3 — WTF-Alien Dungeon promoted from a Feature wrapper to
    // LegacyDungeonStructure (dungeon_type=ALIEN_WTF). The 1.7.10 source
    // (GenericDungeon.makeAlienWTFDungeon, line 1570) builds a 5x5 lapis
    // surface antenna + a 17-block descending shaft + four cardinal "Part"
    // rooms with widths 9/11/13/15 and difficulty 1/2/3/4 — which exceeds
    // the WorldGenRegion 24-block write window. The full byte-for-byte port
    // now lives in LegacyDungeonPiece#generateAlienWtfDungeon.

    // Audit Part 3 — UFO Crash Site removed: the 1.7.10 source has no
    // makeUfo / makeUFO / addUfo generator (verified via grep over
    // reference_1_7_10_source). The previous UfoCrashSiteFeature was a
    // fully invented procedural structure and has been deleted along with
    // its configured_feature, placed_feature, structure, structure_set,
    // and biome tag JSONs to enforce the audit's "no procedural
    // fabrications" rule.

    // D6 DSB sweep — Crystal Battle Tower Feature removed (dsb_sweep_spec.md
    // flag F4): the Phase 13A CrystalBattleTowerFeature was datapack-dead
    // (zero configured_feature / placed_feature / structure JSONs referenced
    // orespawn:crystal_battle_tower) and unfaithful — a heightmap re-anchor
    // plus solid-footing veto the 1.7.10 original never had, and invented
    // per-floor chest contents matching none of the five original
    // CrystalBattleTower*ContentsList arrays. The faithful transcription of
    // GenericDungeon.makeCrystalBattleTower (1.7.10 line 4831) lives in
    // danger.orespawn.world.CrystalStructures#buildCrystalBattleTower,
    // reached via crystal worldgen (tryPlaceCrystalBattleTower, 1/280) and
    // the Random Dungeon Spawner type-33 adapter (buildCrystalBattleTowerAt).
    // Deleted under the audit's "no procedural fabrications" rule — same
    // treatment as ChallengeTowerFeature / UfoCrashSiteFeature above.

    // WGEN-070 (2026-08-11): the Phase-13A CrystalMazeFeature registration was
    // a dead datapack-orphaned DUPLICATE of the live faithful maze — world/
    // CrystalMaze via OreSpawnChunkGenerator (the WGEN-027 single placement
    // mechanism, orig ChunkProviderOreSpawn5.java:213-214) — and it diverged
    // (stamped outer boundary walls, skipped openCrystalMaze's perimeter
    // carve, different bedrock ordering). Feature class + registration
    // retired; ONE mechanism remains.

    // Audit Part 2 — Robot Lab + Shadow Dungeon promoted from Features to
    // a dedicated Structure + StructurePiece (LegacyDungeonStructure with
    // dungeon_type=ROBOT_LAB / SHADOW). Same reason as White House: the
    // multi-room hangar (10x20 entry + 30x30 main + 35-tall sniper tower)
    // and 19-wide double-pyramid Shadow envelope both exceed the
    // WorldGenRegion 24-block write window. The full byte-for-byte
    // 1.7.10 generators now live in LegacyDungeonPiece#postProcess.

    // Audit Part 1 — Wind Tree (authentic Trees.WindTree port,
    // diagonal oak-log spires with leaf-canopy halos).
    public static final DeferredHolder<Feature<?>, WindTreeFeature> WIND_TREE =
            FEATURES.register("wind_tree", () -> new WindTreeFeature(NoneFeatureConfiguration.CODEC));

    // Audit Part 1 — Sky Tree (authentic Trees.SkyTree port, towering
    // SkyTreeLog trunk with cross-shaped horizontal leaf canopies).
    public static final DeferredHolder<Feature<?>, SkyTreeFeature> SKY_TREE =
            FEATURES.register("sky_tree", () -> new SkyTreeFeature(NoneFeatureConfiguration.CODEC));

    // Audit Part 1 — Round Tree (authentic
    // ItemMagicApple.MakeBigRoundTree port, tiered circular log
    // platforms capped with a diamond block).
    public static final DeferredHolder<Feature<?>, RoundTreeFeature> ROUND_TREE =
            FEATURES.register("round_tree", () -> new RoundTreeFeature(NoneFeatureConfiguration.CODEC));

    // Audit Part 1 — Magic Apple Tree (authentic
    // ItemAppleSeed.makeTree port: tall oak trunk with two cardinal
    // arm tiers and stacked square Apple-Leaf canopy).
    public static final DeferredHolder<Feature<?>, MagicAppleTreeFeature> MAGIC_APPLE_TREE =
            FEATURES.register("magic_apple_tree", () -> new MagicAppleTreeFeature(NoneFeatureConfiguration.CODEC));

    // Phase C7 (WGEN-002) — Ruby ore via the original lava-seek loop
    // (orig OreSpawnWorld.java:879-892); replaces the invented ore_ruby vein.
    public static final DeferredHolder<Feature<?>, RubyLavaSeekFeature> RUBY_LAVA_SEEK =
            FEATURES.register("ruby_lava_seek", () -> new RubyLavaSeekFeature(RubyLavaSeekFeature.Config.CODEC));

    // Phase C7 (WGEN-006/048) — anthill surface blocks
    // (orig OreSpawnWorld.addAnts, OreSpawnWorld.java:1472-1507).
    public static final DeferredHolder<Feature<?>, AnthillFeature> ANTHILL =
            FEATURES.register("anthill", () -> new AnthillFeature(AnthillFeature.Config.CODEC));

    // Phase C7 (WGEN-048) — Islands unstable-ant anthills
    // (orig OreSpawnWorld.addUnstableAnts, OreSpawnWorld.java:1572-1588).
    public static final DeferredHolder<Feature<?>, UnstableAnthillFeature> UNSTABLE_ANTHILL =
            FEATURES.register("unstable_anthill", () -> new UnstableAnthillFeature(NoneFeatureConfiguration.CODEC));

    // Phase C7 (WGEN-030 et al) — wild veggie patches
    // (orig OreSpawnWorld.addVeggies, OreSpawnWorld.java:1882-1921).
    public static final DeferredHolder<Feature<?>, VeggiePatchFeature> VEGGIE_PATCH =
            FEATURES.register("veggie_patch", () -> new VeggiePatchFeature(NoneFeatureConfiguration.CODEC));

    // WGEN-007 — wild strawberry / corn / tomato patches
    // (orig OreSpawnWorld.addStrawberries :961-978, addCorn :1023-1068,
    // addTomatoes :1069-1110); config selects which generator runs.
    public static final DeferredHolder<Feature<?>, WildCropsFeature> WILD_CROPS =
            FEATURES.register("wild_crops", () -> new WildCropsFeature(WildCropsFeature.Config.CODEC));

    // Phase 13C-fix2 — Royal Trees were promoted from a Feature to a
    // dedicated Structure + StructurePiece (see
    // danger.orespawn.world.structure.RoyalTreeStructure /
    // danger.orespawn.world.structure.RoyalTreePiece). The previous
    // Feature wrapper couldn't span chunks because of the WorldGenRegion
    // 24-block write cap, which sheared the trees at chunk borders. The
    // authentic 1.7.10 MakeBigSquareTree + make_branch algorithm lives in
    // RoyalTreePiece#postProcess now, with chunkBox.isInside gating on
    // every setBlock so the multi-chunk tree stitches together cleanly.

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
