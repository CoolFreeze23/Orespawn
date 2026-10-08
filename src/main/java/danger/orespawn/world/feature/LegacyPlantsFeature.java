package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.LegacyLight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.common.util.TriState;

import java.util.Optional;
import java.util.function.Predicate;

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
 * <p>The rules are the original's: a flower or a tuft stands in air on soil that carries it, whatever the light
 * (BlockBush), a mushroom in air on mycelium or podzol, or on a block that carries it with a light under 13
 * (BlockMushroom), a reed in air on soil with water beside the soil, two to four high, a pumpkin in air on grass. The
 * light is the one 1.7.10 had while decorating ({@link LegacyLight}), not the game's, which a generating chunk has
 * not yet: its columns' sky light, which puts the original's mushrooms in caves, in rooms and under shade. The heights
 * are drawn from 1.7.10's height map ({@link LegacyLight#height}), the first cell over the highest block that dims the
 * light.</p>
 *
 * <p>{@code unseen}, a block tag, names the blocks that height map and that light never held: in Utopia and the Village
 * every log and leaf, their trees being OreSpawnWorld's, grown after the decorator and written straight into the chunk
 * with no height map or light update (setBlockFast). A grass patch sinks through them as through air and leaves.</p>
 */
public class LegacyPlantsFeature extends Feature<LegacyPlantsFeature.Config> {
    public record Config(int flowersPerChunk, int grassPerChunk, int mushroomsPerChunk, int reedsPerChunk,
                         Optional<TagKey<Block>> unseen) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("flowers_per_chunk", 2).forGetter(Config::flowersPerChunk),
                Codec.INT.optionalFieldOf("grass_per_chunk", 1).forGetter(Config::grassPerChunk),
                Codec.INT.optionalFieldOf("mushrooms_per_chunk", 0).forGetter(Config::mushroomsPerChunk),
                Codec.INT.optionalFieldOf("reeds_per_chunk", 0).forGetter(Config::reedsPerChunk),
                TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("unseen").forGetter(Config::unseen)
        ).apply(i, Config::new));

        /** The blocks the decorator's height map and light never held: the {@code unseen} tag's, or none. */
        public Predicate<BlockState> unseenBlocks() {
            return unseen.<Predicate<BlockState>>map(tag -> state -> state.is(tag)).orElse(state -> false);
        }
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
        Predicate<BlockState> unseen = config.unseenBlocks();
        boolean placed = false;

        for (int i = 0; i < config.flowersPerChunk(); i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int y = random.nextInt(Math.max(LegacyLight.height(level, x, z, unseen) + 32, 1));
            placed |= patch(level, random, x, y, z, LegacyHillsDecorationFeature.flower(random), 64, unseen);
        }
        for (int i = 0; i < config.grassPerChunk(); i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int twice = LegacyLight.height(level, x, z, unseen) * 2;
            if (twice <= 0) continue;
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, random.nextInt(twice), z);
            while (at.getY() > bottom) {
                BlockState state = level.getBlockState(at);
                if (!state.isAir() && !LegacyLight.isLeaves(state) && !unseen.test(state)) break;
                at.move(Direction.DOWN);
            }
            placed |= patch(level, random, x, at.getY(), z, Blocks.SHORT_GRASS.defaultBlockState(), 128, unseen);
        }
        for (int i = 0; i < config.mushroomsPerChunk(); i++) {
            if (random.nextInt(4) == 0) {
                int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
                placed |= patch(level, random, x, LegacyLight.height(level, x, z, unseen), z,
                        Blocks.BROWN_MUSHROOM.defaultBlockState(), 64, unseen);
            }
            if (random.nextInt(8) == 0) {
                placed |= mushrooms(level, random, x0, z0, Blocks.RED_MUSHROOM.defaultBlockState(), unseen);
            }
        }
        if (random.nextInt(4) == 0) {
            placed |= mushrooms(level, random, x0, z0, Blocks.BROWN_MUSHROOM.defaultBlockState(), unseen);
        }
        if (random.nextInt(8) == 0) {
            placed |= mushrooms(level, random, x0, z0, Blocks.RED_MUSHROOM.defaultBlockState(), unseen);
        }
        for (int i = 0; i < Math.max(config.reedsPerChunk(), 0) + 10; i++) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int twice = LegacyLight.height(level, x, z, unseen) * 2;
            if (twice > 0) placed |= reeds(level, random, x, random.nextInt(twice), z);
        }
        if (random.nextInt(32) == 0) {
            int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
            int twice = LegacyLight.height(level, x, z, unseen) * 2;
            if (twice > 0) placed |= pumpkins(level, random, x, random.nextInt(twice), z);
        }
        return placed;
    }

    private static boolean mushrooms(WorldGenLevel level, RandomSource random, int x0, int z0, BlockState mushroom,
                                     Predicate<BlockState> unseen) {
        int x = x0 + random.nextInt(16), z = z0 + random.nextInt(16);
        int twice = LegacyLight.height(level, x, z, unseen) * 2;
        return twice > 0 && patch(level, random, x, random.nextInt(twice), z, mushroom, 64, unseen);
    }

    /** WorldGenFlowers and WorldGenTallGrass: {@code tries} cells round x, y, z, 7 out and 3 up or down, triangular. */
    private static boolean patch(WorldGenLevel level, RandomSource random, int x, int y, int z, BlockState plant, int tries,
                                 Predicate<BlockState> unseen) {
        boolean placed = false;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = 0; i < tries; i++) {
            at.set(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4),
                    z + random.nextInt(8) - random.nextInt(8));
            if (level.isEmptyBlock(at) && stays(level, at, plant, unseen)) {
                level.setBlock(at, plant, 2);
                placed = true;
            }
        }
        return placed;
    }

    /**
     * Whether the plant may stand at {@code at} by 1.7.10's rules: a flower or a tuft on a block that carries it, whatever
     * the light (BlockBush.canBlockStay); a mushroom on mycelium or podzol, or with a light under 13 ({@link LegacyLight})
     * on a block that carries it (BlockMushroom.canBlockStay): a solid block, and leaves, which 1.7.10 took for full
     * blocks. Not 1.21's mushroom rule, which reads the game's light, and a generating chunk and its neighbours have
     * theirs or not by the order they generate in.
     */
    public static boolean stays(WorldGenLevel level, BlockPos at, BlockState plant) {
        return stays(level, at, plant, state -> false);
    }

    /** {@link #stays(WorldGenLevel, BlockPos, BlockState)} with the light blind to the blocks {@code unseen}. */
    public static boolean stays(WorldGenLevel level, BlockPos at, BlockState plant, Predicate<BlockState> unseen) {
        if (!(plant.getBlock() instanceof MushroomBlock)) return plant.canSurvive(level, at);
        BlockPos below = at.below();
        BlockState soil = level.getBlockState(below);
        if (soil.is(BlockTags.MUSHROOM_GROW_BLOCK)) return true;
        TriState sustains = soil.canSustainPlant(level, below, Direction.UP, plant);
        boolean carries = sustains.isDefault()
                ? soil.isSolidRender(level, below) || LegacyLight.isLeaves(soil) : sustains.isTrue();
        return carries && LegacyLight.sky(level, at.getX(), at.getY(), at.getZ(), unseen) < 13;
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
