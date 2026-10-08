package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.world.GenerationRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Mining's trees, flowers and grass as 1.7.10's decorator plants them in its extreme hills (the original's Mining is
 * vanilla's Extreme Hills, {@code new BiomeGenHills(3, false)}; BiomeDecorator.genDecorations, BiomeGenHills and
 * BiomeGenBase read from the 1.7.10 server jar): no tree a chunk but one more in one chunk in ten, at the top of its
 * column, two in three a spruce and the rest an oak, one in ten of those a big one; two flower patches, each a dandelion's
 * two in three and else a poppy's, 64 tries round a height drawn below the column's top plus 32; one grass patch, 128
 * tries round the ground under a height drawn below twice the top. A flower or tuft stands where its cell is air, its
 * block below can carry it and it has the sky over it but for leaves (1.7.10: sky or a light of 8).
 */
public class LegacyHillsDecorationFeature extends Feature<NoneFeatureConfiguration> {
    /** The trees 1.7.10's extreme hills grow (WorldGenTaiga2, WorldGenTrees, WorldGenBigTree), as 1.21's own. */
    public enum Tree {
        SPRUCE("spruce"), OAK("oak"), FANCY_OAK("fancy_oak");

        final ResourceKey<ConfiguredFeature<?, ?>> key;

        Tree(String id) {
            this.key = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.withDefaultNamespace(id));
        }
    }

    public LegacyHillsDecorationFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /** BiomeGenHills.func_150567_a: a spruce two in three; else BiomeGenBase's, a big oak one in ten, else an oak. */
    public static Tree tree(RandomSource random) {
        if (random.nextInt(3) > 0) return Tree.SPRUCE;
        return random.nextInt(10) == 0 ? Tree.FANCY_OAK : Tree.OAK;
    }

    /** BiomeGenBase.func_150572_a: a dandelion two in three, else a poppy. */
    public static BlockState flower(RandomSource random) {
        return (random.nextInt(3) > 0 ? Blocks.DANDELION : Blocks.POPPY).defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int x0 = context.origin().getX() & ~15, z0 = context.origin().getZ() & ~15;
        int bottom = GenerationRange.bottom(context.chunkGenerator(), level);
        Registry<ConfiguredFeature<?, ?>> features = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        boolean placed = false;

        // trees: treesPerChunk 0, and one more in one chunk in ten
        int trees = random.nextInt(10) == 0 ? 1 : 0;
        for (int i = 0; i < trees; i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            BlockPos at = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
            ConfiguredFeature<?, ?> tree = features.get(tree(random).key);
            // 1.7.10's tree generators grow only on soil a sapling takes
            if (tree != null && Blocks.OAK_SAPLING.defaultBlockState().canSurvive(level, at)) {
                placed |= tree.place(level, context.chunkGenerator(), random, at);
            }
        }

        // flowers: flowersPerChunk 2, each round nextInt(top + 32)
        for (int i = 0; i < 2; i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int y = random.nextInt(level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 32);
            placed |= patch(level, random, x, y, z, flower(random), 64);
        }

        // grass: grassPerChunk 1, round the ground under nextInt(top * 2)
        int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
        int twice = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) * 2;
        if (twice > 0) {
            int y = random.nextInt(twice);
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, y, z);
            while (at.getY() > bottom) {
                BlockState state = level.getBlockState(at);
                if (!state.isAir() && !state.is(BlockTags.LEAVES)) break;
                at.move(0, -1, 0);
            }
            placed |= patch(level, random, x, at.getY(), z, Blocks.SHORT_GRASS.defaultBlockState(), 128);
        }
        return placed;
    }

    /** WorldGenFlowers and WorldGenTallGrass: {@code tries} cells round x, y, z, 7 out and 3 up or down, triangular. */
    private static boolean patch(WorldGenLevel level, RandomSource random, int x, int y, int z, BlockState plant, int tries) {
        boolean placed = false;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = 0; i < tries; i++) {
            at.set(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4),
                    z + random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(at) && plant.canSurvive(level, at)
                    && at.getY() >= level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at.getX(), at.getZ())) {
                level.setBlock(at, plant, 2);
                placed = true;
            }
        }
        return placed;
    }
}
