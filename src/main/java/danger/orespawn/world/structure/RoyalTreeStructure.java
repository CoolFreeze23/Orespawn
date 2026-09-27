package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
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
 * {@code postProcess}. The structure itself only reads the chunk's huge tree
 * roll (see {@link #findGenerationPoint}).</p>
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
     * WGEN-080: the royal tree is addHugeTree's own royal branch (orig OreSpawnWorld.java:1830-1880), read from the
     * chunk's huge roll ({@link UtopiaTreeStructure#hugeRoll}) on the random the structure pass gives every structure
     * of the chunk: the one-in-fifty gate, up to three attempts at chunk + 4 + nextInt(8) (:1842-1843) for the grass
     * under air inside Y51-127 (:1844-1846; the dry noise surface stands in for the grass, as for the huge trees), the
     * type roll's 0 (:1855, :1860), then the King on a 0 and the Queen otherwise (:1863). The tree is built on the grass
     * ({@code posY - 1}, :1864/:1866). The royal_trees set asks every chunk (spacing 1) and tries both trees; the one
     * the roll did not pick refuses, so a chunk grows the royal tree its roll picked, one chunk in 5,000 where the grass
     * holds, as in the original.
     */
    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        UtopiaTreeStructure.HugeRoll roll = UtopiaTreeStructure.hugeRoll(context.random(), context.chunkPos(),
                UtopiaTreeStructure.probe(context));
        if (roll.royal() == null || roll.queen() != queenVariant) return Optional.empty();
        BlockPos origin = roll.royal();
        // the canopy stays below the world ceiling (the window keeps the grass under Y127)
        if (origin.getY() + 64 >= context.heightAccessor().getMaxBuildHeight()) return Optional.empty();
        return Optional.of(new GenerationStub(origin, builder ->
                builder.addPiece(new RoyalTreePiece(origin, queenVariant))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.ROYAL_TREE.get();
    }
}
