package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * Mining's falls of water and lava down its hillsides, the original's OreSpawnWorld.addLavaAndWater
 * (OreSpawnWorld.java:2454): in one chunk in five, up to six columns tried; down a column of air from Y128 to over Y75,
 * the first grass block with dirt or stone under it whose four sides hold both air and dirt, stone or grass takes a
 * source of water (or lava, one in two) two deep under a third over it, and the fall runs from there; one fall a chunk at
 * most. The top block flows at once in the original; here its fluid tick is set, and it runs once the chunk ticks.
 */
public class LavaAndWaterFeature extends Feature<NoneFeatureConfiguration> {
    public LavaAndWaterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (random.nextInt(5) != 0) return false;
        int x0 = context.origin().getX() & ~15, z0 = context.origin().getZ() & ~15;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        columns:
        for (int i = 0; i < 6; i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            for (int y = 128; y > 75 && level.isEmptyBlock(at.set(x, y, z)); y--) {
                if (!level.getBlockState(at.set(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) continue;
                BlockState under = level.getBlockState(at.set(x, y - 2, z));
                if (!under.is(Blocks.DIRT) && !under.is(Blocks.STONE)) continue columns;
                int air = 0, ground = 0;
                for (int[] side : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    BlockState state = level.getBlockState(at.set(x + side[0], y - 1, z + side[1]));
                    if (state.isAir()) air++;
                    if (state.is(Blocks.DIRT) || state.is(Blocks.STONE) || state.is(Blocks.GRASS_BLOCK)) ground++;
                }
                if (air == 0 || ground == 0) continue columns;
                boolean water = random.nextInt(2) == 0;
                BlockState source = (water ? Blocks.WATER : Blocks.LAVA).defaultBlockState();
                for (int d = 0; d < 3; d++) level.setBlock(at.set(x, y - d, z), source, 3);
                level.scheduleTick(at.set(x, y, z), water ? Fluids.WATER : Fluids.LAVA, 0);
                return true;
            }
        }
        return false;
    }
}
