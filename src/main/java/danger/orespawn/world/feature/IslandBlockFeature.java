package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * The Islands dimension's Island blocks, orig {@code OreSpawnWorld.addIslands} (OreSpawnWorld.java:1779-1790), run
 * after the unstable anthills (:182-183) as the original ran it: a place chunk + 2 + nextInt(12) each way, then one
 * chunk in 10 + LessLag x 2, the first air from Y20 down with grass below it, the block set there. The block makes the
 * flying island as it ticks ({@link danger.orespawn.block.IslandBlock}). WGEN-118.
 */
public class IslandBlockFeature extends Feature<NoneFeatureConfiguration> {

    public IslandBlockFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int posX = 2 + context.origin().getX() + random.nextInt(12);                // :1780
        int posZ = 2 + context.origin().getZ() + random.nextInt(12);                // :1781
        if (random.nextInt(10 + OreSpawnConfig.LESS_LAG.get() * 2) != 1) return false; // :1782
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int posY = 20; posY > 2 && level.getBlockState(cursor.set(posX, posY, posZ)).isAir(); posY--) { // :1785
            if (!level.getBlockState(cursor.set(posX, posY - 1, posZ)).is(Blocks.GRASS_BLOCK)) continue;    // :1786
            level.setBlock(cursor.set(posX, posY, posZ), ModBlocks.ISLAND.get().defaultBlockState(), 2);    // :1787
            return true;
        }
        return false;
    }
}
