package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.LegacyLight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;

/**
 * The plants 1.7.10's BiomeDecorator.genDecorations sets in the original's dimensions (read from the 1.7.10 server jar),
 * after the counts each dimension's decorator carries: {@code flowers_per_chunk} patches of 64 tries round a height drawn
 * below the column's top plus 32, a dandelion two in three and else a poppy (BiomeGenBase.func_150572_a);
 * {@code grass_per_chunk} patches of 128 tries round the ground under a height drawn below twice the top
 * (WorldGenTallGrass); {@code mushrooms_per_chunk} rounds of a brown patch one in four at the top and a red one one in
 * eight; {@code reeds_per_chunk} reed tries; and, whatever the counts, a brown mushroom patch one chunk in four and a red
 * one in eight, ten reed tries and a pumpkin patch one chunk in 32, each round a height drawn below twice the top. A count
 * of -999 (the original's "none") or 0 sets nothing.
 *
 * <p>The rules are the original's: a flower or a tuft stands in air on soil that carries it with a light of 8 or more,
 * a mushroom in air on solid ground (or mycelium or podzol) with a light under 13, a reed in air on soil with water beside
 * the soil, two to four high, a pumpkin in air on grass. The light is the one 1.7.10 had while decorating
 * ({@link LegacyLight}): its columns' sky light, which puts the original's mushrooms in caves, in rooms and under deep
 * shade and keeps its flowers out of them.</p>
 */
public class LegacyPlantsFeature extends Feature<LegacyPlantsFeature.Config> {
    public record Config(int flowersPerChunk, int grassPerChunk, int mushroomsPerChunk, int reedsPerChunk)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("flowers_per_chunk", 2).forGetter(Config::flowersPerChunk),
                Codec.INT.optionalFieldOf("grass_per_chunk", 1).forGetter(Config::grassPerChunk),
                Codec.INT.optionalFieldOf("mushrooms_per_chunk", 0).forGetter(Config::mushroomsPerChunk),
                Codec.INT.optionalFieldOf("reeds_per_chunk", 0).forGetter(Config::reedsPerChunk)
        ).apply(i, Config::new));
    }

    public LegacyPlantsFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        // the decorator draws its places at chunk_X + nextInt(16) + 8: eight blocks into the chunks east and south, so a
        // chunk's plants are half its own and half its neighbours'
        int x0 = (context.origin().getX() & ~15) + 8, z0 = (context.origin().getZ() & ~15) + 8;
        int bottom = GenerationRange.bottom(context.chunkGenerator(), level);
        boolean placed = false;

        for (int i = 0; i < config.flowersPerChunk(); i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int y = random.nextInt(Math.max(top(level, x, z) + 32, 1));
            placed |= patch(level, random, x, y, z, LegacyHillsDecorationFeature.flower(random), 64, Rule.BUSH);
        }
        for (int i = 0; i < config.grassPerChunk(); i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int twice = top(level, x, z) * 2;
            if (twice <= 0) continue;
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, random.nextInt(twice), z);
            while (at.getY() > bottom) {
                BlockState state = level.getBlockState(at);
                if (!state.isAir() && !state.is(BlockTags.LEAVES)) break;
                at.move(Direction.DOWN);
            }
            placed |= patch(level, random, x, at.getY(), z, Blocks.SHORT_GRASS.defaultBlockState(), 128, Rule.BUSH);
        }
        for (int i = 0; i < config.mushroomsPerChunk(); i++) {
            if (random.nextInt(4) == 0) {
                int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
                placed |= patch(level, random, x, top(level, x, z), z, Blocks.BROWN_MUSHROOM.defaultBlockState(), 64, Rule.MUSHROOM);
            }
            if (random.nextInt(8) == 0) {
                placed |= mushrooms(level, random, x0, z0, Blocks.RED_MUSHROOM.defaultBlockState());
            }
        }
        if (random.nextInt(4) == 0) {
            placed |= mushrooms(level, random, x0, z0, Blocks.BROWN_MUSHROOM.defaultBlockState());
        }
        if (random.nextInt(8) == 0) {
            placed |= mushrooms(level, random, x0, z0, Blocks.RED_MUSHROOM.defaultBlockState());
        }
        for (int i = 0; i < Math.max(config.reedsPerChunk(), 0) + 10; i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int twice = top(level, x, z) * 2;
            if (twice > 0) placed |= reeds(level, random, x, random.nextInt(twice), z);
        }
        if (random.nextInt(32) == 0) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int twice = top(level, x, z) * 2;
            if (twice > 0) placed |= pumpkins(level, random, x, random.nextInt(twice), z);
        }
        return placed;
    }

    /** The column's top as 1.7.10's height map has it: over the highest block that stops light, leaves and water too. */
    private static int top(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    private static boolean mushrooms(WorldGenLevel level, RandomSource random, int x0, int z0, BlockState mushroom) {
        int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
        int twice = top(level, x, z) * 2;
        return twice > 0 && patch(level, random, x, random.nextInt(twice), z, mushroom, 64, Rule.MUSHROOM);
    }

    private enum Rule { BUSH, MUSHROOM }

    /** WorldGenFlowers and WorldGenTallGrass: {@code tries} cells round x, y, z, 7 out and 3 up or down, triangular. */
    private static boolean patch(WorldGenLevel level, RandomSource random, int x, int y, int z, BlockState plant, int tries,
                                 Rule rule) {
        boolean placed = false;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = 0; i < tries; i++) {
            at.set(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4),
                    z + random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(at) && plant.canSurvive(level, at) && lit(level, at, rule)) {
                level.setBlock(at, plant, 2);
                placed = true;
            }
        }
        return placed;
    }

    /** BlockBush.canBlockStay's light of 8 (or the sky), BlockMushroom's under 13 unless on mycelium or podzol. */
    private static boolean lit(WorldGenLevel level, BlockPos at, Rule rule) {
        int light = LegacyLight.sky(level, at.getX(), at.getY(), at.getZ());
        if (rule == Rule.BUSH) return light >= 8;
        return light < 13 || level.getBlockState(at.below()).is(BlockTags.MUSHROOM_GROW_BLOCK);
    }

    /** WorldGenReed: 20 tries at the height given, 3 out, triangular, in air with water beside the block under. */
    private static boolean reeds(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        boolean placed = false;
        BlockState reed = Blocks.SUGAR_CANE.defaultBlockState();
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 20; i++) {
            at.set(x + random.nextInt(4) - random.nextInt(4), y, z + random.nextInt(4) - random.nextInt(4));
            if (!level.isEmptyBlock(at) || !waterBeside(level, at.below())) continue;
            int height = 2 + random.nextInt(random.nextInt(3) + 1);
            for (int k = 0; k < height; k++) {
                BlockPos cell = at.above(k);
                if (level.isEmptyBlock(cell) && reed.canSurvive(level, cell)) {
                    level.setBlock(cell, reed, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean waterBeside(WorldGenLevel level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            FluidState fluid = level.getFluidState(pos.relative(side));
            if (fluid.is(FluidTags.WATER)) return true;
        }
        return false;
    }

    /** WorldGenPumpkin: 64 tries, 7 out and 3 up or down, triangular, in air on grass. */
    private static boolean pumpkins(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        boolean placed = false;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 64; i++) {
            at.set(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4),
                    z + random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(at) && level.getBlockState(at.below()).is(Blocks.GRASS_BLOCK)) {
                random.nextInt(4); // the original's facing, which a 1.21 pumpkin has no use for
                level.setBlock(at, Blocks.PUMPKIN.defaultBlockState(), 2);
                placed = true;
            }
        }
        return placed;
    }
}
