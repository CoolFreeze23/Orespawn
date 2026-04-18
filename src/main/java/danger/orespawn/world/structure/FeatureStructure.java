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
 *   <li>{@code horizontal_extent} / {@code down_extent} / {@code up_extent} —
 *       half-widths of the bounding-box "permit" handed to
 *       {@link FeatureStructurePiece}. Defaults are {@code 16 / 16 / 80},
 *       which comfortably covers the Phase 13C Royal Trees (±9 horizontal,
 *       up to 60 tall). Smaller surface dungeons can keep the defaults; the
 *       only downside of an over-wide envelope is that vanilla treats it as
 *       a placement reservation, so don't push these much past 32 without
 *       also widening the matching {@code random_spread.spacing}.</li>
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
            com.mojang.serialization.Codec.INT.optionalFieldOf("up_extent", 80).forGetter(s -> s.upExtent)
    ).apply(inst, FeatureStructure::new));

    private final Holder<ConfiguredFeature<?, ?>> feature;
    private final int yOffset;
    private final int horizontalExtent;
    private final int downExtent;
    private final int upExtent;

    public FeatureStructure(StructureSettings settings, Holder<ConfiguredFeature<?, ?>> feature,
                            int yOffset, int horizontalExtent, int downExtent, int upExtent) {
        super(settings);
        this.feature = feature;
        this.yOffset = yOffset;
        this.horizontalExtent = horizontalExtent;
        this.downExtent = downExtent;
        this.upExtent = upExtent;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int y = context.chunkGenerator().getBaseHeight(
                x, z,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        ) + yOffset;
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new Structure.GenerationStub(origin, builder ->
                builder.addPiece(new FeatureStructurePiece(
                        origin, feature, horizontalExtent, downExtent, upExtent))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.FEATURE.get();
    }
}
