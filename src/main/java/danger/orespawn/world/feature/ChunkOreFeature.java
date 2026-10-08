package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.LegacyChunkVein;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/**
 * One vein of OreSpawn's own ore pass in Utopia, the Village and Mining ({@code orespawn:chunk_ore}): the original's
 * ChunkOreGenerator vein ({@link LegacyChunkVein}), centred 8 blocks in from the origin and kept to its chunk, of the
 * first target's block over that target, the configuration's size in the original's sense (size + 1 spheres).
 */
public class ChunkOreFeature extends Feature<OreConfiguration> {
    public ChunkOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        OreConfiguration config = context.config();
        OreConfiguration.TargetBlockState target = config.targetStates.get(0);
        WorldGenLevel level = context.level();
        return LegacyChunkVein.place(level, context.random(), context.origin(), config.size, target.state, target.target,
                GenerationRange.bottom(context.chunkGenerator(), level), GenerationRange.top(context.chunkGenerator(), level)) > 0;
    }
}
