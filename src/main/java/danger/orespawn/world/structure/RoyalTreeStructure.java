package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Dedicated {@link Structure} for the Phase 13C Royal Trees. Replaces the
 * generic {@link FeatureStructure} wiring for these two structures
 * specifically because the wrapper's "delegate to a Feature" model was
 * limited to single-chunk writes by the {@code WorldGenRegion} 24-block
 * cap, causing the trees to clip at chunk borders.
 *
 * <p>This class instantiates a single {@link RoyalTreePiece} into the
 * structure pieces builder. The piece carries the massive blueprint
 * bounding box and runs the chunk-by-chunk write algorithm in its
 * {@code postProcess}. The structure itself is otherwise minimal —
 * heightmap-anchored XZ at chunk centre, with a Y-bound rejection so we
 * never spawn a 60-tall tree near the world ceiling.</p>
 *
 * <p>The single {@code queen_variant} codec field switches the entire
 * palette dispatch (gold/emerald/diamond → obsidian/ruby/amethyst, plus
 * the spawner block at the apex).</p>
 *
 * <p>JSON usage: {@code "type": "orespawn:royal_tree", "queen_variant": true}.</p>
 */
public class RoyalTreeStructure extends Structure {

    public static final MapCodec<RoyalTreeStructure> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            settingsCodec(inst),
            Codec.BOOL.optionalFieldOf("queen_variant", false).forGetter(s -> s.queenVariant)
    ).apply(inst, RoyalTreeStructure::new));

    private final boolean queenVariant;

    public RoyalTreeStructure(StructureSettings settings, boolean queenVariant) {
        super(settings);
        this.queenVariant = queenVariant;
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
                context.randomState());

        // Y-bound guard: reject any spawn that would put the apex within 64
        // blocks of the world ceiling. Mirrors the legacy worldgen safeguard
        // that prevented canopy clipping in 1.7.10.
        if (y <= context.heightAccessor().getMinBuildHeight() + 4) return Optional.empty();
        if (y + 64 >= context.heightAccessor().getMaxBuildHeight()) return Optional.empty();

        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(origin, builder ->
                builder.addPiece(new RoyalTreePiece(origin, queenVariant))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.ROYAL_TREE.get();
    }
}
