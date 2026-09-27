package danger.orespawn.world.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * The share of a one-pick structure set's roll that builds nothing (WGEN-080; see {@link StructurePicks}). The
 * original rolled each chunk for its structures and usually built none: the roll missed, or the cooldown after an
 * earlier build held it back. A set that places a spot for every few chunks carries that share as this structure, at
 * the weight that leaves the set's structures at the original's odds, so the spots it picks for it stay empty. It
 * never generates and stands in no biome.
 */
public class NothingStructure extends Structure {

    public static final MapCodec<NothingStructure> CODEC = simpleCodec(NothingStructure::new);

    public NothingStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return Optional.empty();
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.NOTHING.get();
    }
}
