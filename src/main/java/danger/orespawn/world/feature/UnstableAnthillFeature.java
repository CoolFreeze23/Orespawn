package danger.orespawn.world.feature;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Unstable-ant anthills for the Islands dimension — faithful port of
 * {@code OreSpawnWorld.addUnstableAnts} (orig OreSpawnWorld.java:1572-1588):
 * 1-in-30 chunk gate, 3 attempts, each scanning a random column from Y20 down
 * through air to Y3 for grass, which is replaced with the Unstable Ant block.
 * (The low scan range matches the Islands flat plane at Y7,
 * orig ChunkProviderOreSpawn4.java:30-32.)
 */
public class UnstableAnthillFeature extends Feature<NoneFeatureConfiguration> {

    public UnstableAnthillFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkX = context.origin().getX();
        int chunkZ = context.origin().getZ();

        // orig OreSpawnWorld.java:1576 — 1-in-30 per-chunk gate
        if (random.nextInt(30) != 0) return false;

        boolean placedAny = false;
        // orig OreSpawnWorld.java:1579 — 3 attempts
        for (int i = 0; i < 3; i++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:1582 — scan Y20 down while air, stop at Y3
            for (int y = 20; y > 2 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
                BlockPos below = new BlockPos(x, y - 1, z);
                if (!level.getBlockState(below).is(Blocks.GRASS_BLOCK)) continue;
                level.setBlock(below, ModBlocks.UNSTABLE_ANT_BLOCK.get().defaultBlockState(), 2);
                placedAny = true;
                break;
            }
        }
        return placedAny;
    }
}
