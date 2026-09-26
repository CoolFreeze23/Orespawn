package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

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

    /** The structure set that places the King and Queen trees. */
    public static final ResourceKey<StructureSet> ROYAL_TREES = ResourceKey.create(Registries.STRUCTURE_SET,
            ResourceLocation.fromNamespaceAndPath("orespawn", "royal_trees"));

    /**
     * WGEN-075: whether a royal tree starts in {@code context}'s chunk, answered the way the structure pass answers
     * it: the royal_trees set's {@code random_spread} placement picks this chunk (its one potential chunk per spacing
     * cell; the set keeps the default frequency and no exclusion zone), and the King or the Queen accepts it there
     * (the set tries the other one when its weighted pick refuses). Each is asked with its own biomes, as
     * {@code ChunkGenerator.tryGenerateStructure} builds the context.
     */
    public static boolean startsIn(Structure.GenerationContext context) {
        Registry<StructureSet> sets = context.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        StructureSet set = sets.get(ROYAL_TREES);
        if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) return false;
        ChunkPos chunk = context.chunkPos();
        if (!placementPicks(placement, context.seed(), chunk)) return false;
        for (StructureSet.StructureSelectionEntry entry : set.structures()) {
            Structure royal = entry.structure().value();
            Structure.GenerationContext asRoyal = new Structure.GenerationContext(context.registryAccess(),
                    context.chunkGenerator(), context.biomeSource(), context.randomState(),
                    context.structureTemplateManager(), context.seed(), chunk, context.heightAccessor(),
                    royal.biomes()::contains);
            if (royal.findValidGenerationPoint(asRoyal).isPresent()) return true;
        }
        return false;
    }

    /** Whether the set's placement puts its start for this spacing cell in {@code chunk}. */
    public static boolean placementPicks(RandomSpreadStructurePlacement placement, long seed, ChunkPos chunk) {
        ChunkPos picked = placement.getPotentialStructureChunk(seed, chunk.x, chunk.z);
        return picked.x == chunk.x && picked.z == chunk.z;
    }
}
