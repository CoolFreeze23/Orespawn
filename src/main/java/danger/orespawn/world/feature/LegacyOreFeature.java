package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/**
 * The OreSpawn dimensions' 1.7.10 ores (WGEN-117): 1.21's ore vein, its shape and its draws as OreFeature's, placed
 * always, as the original's WorldGenMinable placed every vein. OreFeature first reads the ocean floor's height over the
 * vein's box and draws the vein's shape only where a column reaches its bottom; those heights depend on what was
 * decorated before (a neighbour's tree, a heightmap kept only before features or rebuilt after a chunk is read back), and
 * the placement draws its next places from the same random, so one vein's check going the other way moved every later
 * vein of the feature in the chunk. Here the shape is drawn every time: a vein in the air places nothing, and the places
 * after it are the same whatever the ground.
 */
public class LegacyOreFeature extends OreFeature {

    public LegacyOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        OreConfiguration config = context.config();
        float angle = random.nextFloat() * (float) Math.PI;
        float spread = (float) config.size / 8.0F;
        int radius = Mth.ceil(((float) config.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double x0 = (double) origin.getX() + Math.sin(angle) * (double) spread;
        double x1 = (double) origin.getX() - Math.sin(angle) * (double) spread;
        double z0 = (double) origin.getZ() + Math.cos(angle) * (double) spread;
        double z1 = (double) origin.getZ() - Math.cos(angle) * (double) spread;
        double y0 = origin.getY() + random.nextInt(3) - 2;
        double y1 = origin.getY() + random.nextInt(3) - 2;
        int minX = origin.getX() - Mth.ceil(spread) - radius;
        int minY = origin.getY() - 2 - radius;
        int minZ = origin.getZ() - Mth.ceil(spread) - radius;
        int width = 2 * (Mth.ceil(spread) + radius);
        int height = 2 * (2 + radius);
        return this.doPlace(level, random, config, x0, x1, z0, z1, y0, y1, minX, minY, minZ, width, height);
    }
}
