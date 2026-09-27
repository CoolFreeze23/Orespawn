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
 * {@code postProcess}. The structure itself only picks the site, the huge
 * tree roll's (see {@link #findGenerationPoint}).</p>
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

    /**
     * WGEN-079: the royal tree was addHugeTree's 1% branch (orig OreSpawnWorld.java:1830-1880), so it takes the huge
     * roll's site: up to three attempts at chunk + 4 + nextInt(8) (:1842-1843), each taking the grass under air inside
     * Y51-127 (:1844-1846; the dry noise surface stands in for the grass, as for the huge trees), the tree built on the
     * grass ({@code posY - 1}, :1864/:1866). A chunk where no attempt finds grass grows no royal tree.
     */
    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        UtopiaTreeStructure.ColumnProbe probe = UtopiaTreeStructure.probe(context);
        for (int i = 0; i < 3; i++) {
            int x = 4 + chunk.getMinBlockX() + context.random().nextInt(8);
            int z = 4 + chunk.getMinBlockZ() + context.random().nextInt(8);
            int grass = probe.base(x, z, 50, 127);
            if (grass == Integer.MIN_VALUE) continue;
            // the canopy stays below the world ceiling (the window keeps the grass under Y127)
            if (grass + 64 >= context.heightAccessor().getMaxBuildHeight()) return Optional.empty();
            BlockPos origin = new BlockPos(x, grass, z);
            return Optional.of(new GenerationStub(origin, builder ->
                    builder.addPiece(new RoyalTreePiece(origin, queenVariant))));
        }
        return Optional.empty();
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
