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

    public static final DeferredHolder<Feature<?>, BeehiveFeature> BEEHIVE =
            FEATURES.register("beehive", () -> new BeehiveFeature(NoneFeatureConfiguration.CODEC));

    // Phase 12 — King + Queen Challenge Tower (procedural multi-floor
    // dungeon, see ChallengeTowerFeature for the per-floor mob ladder).
    public static final DeferredHolder<Feature<?>, ChallengeTowerFeature> CHALLENGE_TOWER =
            FEATURES.register("challenge_tower", () -> new ChallengeTowerFeature(ChallengeTowerFeature.Config.CODEC));

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
    // hallucinations" rule.

    // Phase 13A — Crystal Battle Tower (cylindrical 5-floor tower with the
    // Vortex spawner on the cap floor; see CrystalBattleTowerFeature for
    // the floor-by-floor mob ladder ported from the 1.7.10 source).
    public static final DeferredHolder<Feature<?>, CrystalBattleTowerFeature> CRYSTAL_BATTLE_TOWER =
            FEATURES.register("crystal_battle_tower", () -> new CrystalBattleTowerFeature(NoneFeatureConfiguration.CODEC));

    // Phase 13A — Crystal Dimension Maze. Per-chunk procedural maze, anchored
    // strictly inside the owning chunk's bounding box so it never triggers
    // cross-chunk runaway worldgen.
    public static final DeferredHolder<Feature<?>, CrystalMazeFeature> CRYSTAL_MAZE =
            FEATURES.register("crystal_maze", () -> new CrystalMazeFeature(NoneFeatureConfiguration.CODEC));

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
