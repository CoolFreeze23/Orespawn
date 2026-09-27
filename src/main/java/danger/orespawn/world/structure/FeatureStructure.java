package danger.orespawn.world.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Thin {@link Structure} wrapper that defers actual block placement to a
 * pre-existing {@link ConfiguredFeature}. This is what makes every OreSpawn
 * hand-built dungeon discoverable through the vanilla {@code /locate
 * structure} command — vanilla only indexes registered {@link Structure}
 * entries, never raw features placed via {@code add_features} biome modifiers.
 *
 * <p><b>Why a wrapper instead of a real structure refactor?</b> Each of our
 * Phase 11–13 dungeons (Robot Lab, Shadow Dungeon, Crystal Battle Tower,
 * Royal Trees, etc.) is already a self-contained {@link
 * net.minecraft.world.level.levelgen.feature.Feature} with its own
 * heightmap-anchored, chunk-bound generator. Re-implementing them as
 * {@code Structure} subclasses would mean porting all the bound-checking +
 * piece serialization logic for zero gameplay benefit. By spawning each
 * structure with a single {@link FeatureStructurePiece} that resolves its
 * configured feature at {@code postProcess} time and calls
 * {@code feature.place(...)}, we get {@code /locate} support for free
 * without touching the existing per-feature placement code.</p>
 *
 * <p>The structure JSON owns:</p>
 * <ul>
 *   <li>{@code biomes} — same biome scope the previous {@code add_features}
 *       biome modifier targeted (so generation footprint is unchanged).</li>
 *   <li>{@code feature} — the {@link ConfiguredFeature} ID to delegate to.</li>
 *   <li>{@code y_offset} — vertical nudge applied on top of the heightmap-
 *       resolved Y. Most of our features re-resolve the heightmap themselves,
 *       so this is normally {@code 0}; the WTF-Alien Dungeon (which buries
 *       itself 12 blocks below grade) and the UFO Crash Site (which floats
 *       a few blocks above grade) use this to hand the feature a friendly
 *       starting Y so {@code /locate}'s reported coordinate matches the
 *       structure's actual centre.</li>
 *   <li>{@code anchor} — where in the chunk the feature is handed its site (WGEN-079): {@code chunk_centre} (the
 *       default), {@code lowest_grass_36} (the lowest grass of a 6×6 column grid above Y40, addBeeHive's scan,
 *       orig OreSpawnWorld.java:2031-2057) or {@code grass_attempts_5} (five columns at chunk + nextInt(16), each
 *       searched from Y128 down to grass under air above Y40, addANest's scan, :999-1021). A scan that finds
 *       nothing refuses the chunk.</li>
 *   <li>{@code overworld_dungeon} — honours {@code DisableOverworldDungeons}, which in 1.7.10 gated the whole
 *       overworld structure pass, addANest included (orig OreSpawnWorld.java:284).</li>
 *   <li>{@code horizontal_extent} / {@code down_extent} / {@code up_extent} —
 *       half-widths of the bounding-box "permit" handed to
 *       {@link FeatureStructurePiece}. Defaults are {@code 16 / 16 / 80},
 *       which comfortably covers the Phase 13C Royal Trees (±9 horizontal,
 *       up to 60 tall). Smaller surface dungeons can keep the defaults; the
 *       only downside of an over-wide envelope is that vanilla treats it as
 *       a placement reservation, so don't push these much past 32.</li>
 * </ul>
 *
 * <p>Placement (spread, salt, exclusion zones) lives in the matching
 * {@code worldgen/structure_set/<id>.json} entry — one structure_set per
 * structure, all using {@link
 * net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement}
 * tuned to approximate the original {@code rarity_filter} probabilities.</p>
 */
public class FeatureStructure extends Structure {

    public static final MapCodec<FeatureStructure> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            settingsCodec(inst),
            ConfiguredFeature.CODEC.fieldOf("feature").forGetter(s -> s.feature),
            com.mojang.serialization.Codec.INT.optionalFieldOf("y_offset", 0).forGetter(s -> s.yOffset),
            com.mojang.serialization.Codec.INT.optionalFieldOf("horizontal_extent", 16).forGetter(s -> s.horizontalExtent),
            com.mojang.serialization.Codec.INT.optionalFieldOf("down_extent", 16).forGetter(s -> s.downExtent),
            com.mojang.serialization.Codec.INT.optionalFieldOf("up_extent", 80).forGetter(s -> s.upExtent),
            com.mojang.serialization.Codec.STRING.optionalFieldOf("anchor", "chunk_centre").forGetter(s -> s.anchor),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("overworld_dungeon", false).forGetter(s -> s.overworldDungeon)
    ).apply(inst, FeatureStructure::new));

    private final Holder<ConfiguredFeature<?, ?>> feature;
    private final int yOffset;
    private final int horizontalExtent;
    private final int downExtent;
    private final int upExtent;
    private final String anchor;
    private final boolean overworldDungeon;

    public FeatureStructure(StructureSettings settings, Holder<ConfiguredFeature<?, ?>> feature,
                            int yOffset, int horizontalExtent, int downExtent, int upExtent,
                            String anchor, boolean overworldDungeon) {
        super(settings);
        this.feature = feature;
        this.yOffset = yOffset;
        this.horizontalExtent = horizontalExtent;
        this.downExtent = downExtent;
        this.upExtent = upExtent;
        this.anchor = anchor;
        this.overworldDungeon = overworldDungeon;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (overworldDungeon && danger.orespawn.OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS.get()) {
            return Optional.empty();
        }
        BlockPos site = switch (anchor) {
            case "lowest_grass_36" -> LegacyDungeonStructure.lowestGrassOrigin(context);
            case "grass_attempts_5" -> grassAttempts(context);
            default -> chunkCentre(context);
        };
        if (site == null) return Optional.empty();
        BlockPos origin = site.above(yOffset);
        return Optional.of(new Structure.GenerationStub(origin, builder ->
                builder.addPiece(new FeatureStructurePiece(
                        origin, feature, horizontalExtent, downExtent, upExtent))));
    }

    /** The site the feature is handed: {@code chunk_centre}, {@code lowest_grass_36} or {@code grass_attempts_5}. */
    public String anchor() {
        return anchor;
    }

    /** Whether {@code DisableOverworldDungeons} turns this structure off. */
    public boolean overworldDungeon() {
        return overworldDungeon;
    }

    private static BlockPos chunkCentre(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        return new BlockPos(x, y, z);
    }

    /**
     * addANest's scan (orig OreSpawnWorld.java:999-1021): five columns at chunk + nextInt(16) (:1006-1007), each
     * searched from Y128 down through air to the first block, which must be grass above Y40 (:1008-1009; the dry
     * noise surface stands in for the grass); the site is the air above it, as the original handed its builders.
     */
    private static BlockPos grassAttempts(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        for (int i = 0; i < 5; i++) {
            int x = chunk.getMinBlockX() + context.random().nextInt(16);
            int z = chunk.getMinBlockZ() + context.random().nextInt(16);
            int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            int floor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            if (surface != floor || surface <= 40 || surface > 128) continue;
            return new BlockPos(x, surface, z);
        }
        return null;
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.FEATURE.get();
    }
}
