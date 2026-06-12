package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Wild veggie patches — faithful port of {@code OreSpawnWorld.addVeggies}
 * (orig OreSpawnWorld.java:1882-1921).
 *
 * <p>Original: 1-in-15 chunk gate, then 8 attempts. Each attempt scans a random
 * column from Y100 down through air to Y41 for grass and plants ON TOP of it
 * (the grass survives): {@code nextInt(6)} — 0 carrots, 1 potatoes, 2 radish,
 * 3 lettuce, 4 melon stem (only on a further 1-in-10 roll), 5 duplicator-tree
 * sapling (1-in-50 roll; the Duplicator Tree is not ported — Phase D
 * WGEN-044 — so case 5 places nothing, exactly like the original with
 * {@code enableduplicatortree=0}).</p>
 *
 * <p>The original's biome gate (Utopia/Mining/Chaos dimensions, or overworld
 * River/Swampland biomes, orig OreSpawnWorld.java:1887) is expressed by which
 * biome JSONs / biome modifiers reference the placed feature.</p>
 */
public class VeggiePatchFeature extends Feature<NoneFeatureConfiguration> {

    public VeggiePatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkX = context.origin().getX();
        int chunkZ = context.origin().getZ();

        // orig OreSpawnWorld.java:1883 — 1-in-15 per-chunk gate
        if (random.nextInt(15) != 0) return false;

        boolean placedAny = false;
        // orig OreSpawnWorld.java:1888 — 8 attempts
        for (int i = 0; i < 8; i++) {
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:1891 — scan Y100 down while air, stop at Y41
            for (int y = 100; y > 40 && level.getBlockState(new BlockPos(x, y, z)).isAir(); y--) {
                if (!level.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) continue;
                BlockPos plantPos = new BlockPos(x, y, z);
                // orig OreSpawnWorld.java:1893-1917 — crop picker
                int what = random.nextInt(6);
                switch (what) {
                    case 0 -> { level.setBlock(plantPos, Blocks.CARROTS.defaultBlockState(), 2); placedAny = true; }
                    case 1 -> { level.setBlock(plantPos, Blocks.POTATOES.defaultBlockState(), 2); placedAny = true; }
                    case 2 -> { level.setBlock(plantPos, ModBlocks.RADISH_PLANT.get().defaultBlockState(), 2); placedAny = true; }
                    case 3 -> { level.setBlock(plantPos, ModBlocks.LETTUCE_0.get().defaultBlockState(), 2); placedAny = true; }
                    case 4 -> {
                        // orig OreSpawnWorld.java:1911 — melon stem only on a further 1-in-10
                        if (random.nextInt(10) == 0) {
                            level.setBlock(plantPos, Blocks.MELON_STEM.defaultBlockState(), 2);
                            placedAny = true;
                        }
                    }
                    default -> {
                        // orig OreSpawnWorld.java:1915-1916 — duplicator sapling (1/50,
                        // config-gated). Tree not ported (Phase D WGEN-044): roll for
                        // random-stream shape, place nothing.
                        random.nextInt(50);
                    }
                }
                break;
            }
        }
        return placedAny;
    }
}
