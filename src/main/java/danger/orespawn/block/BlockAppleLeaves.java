package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;

public class BlockAppleLeaves extends LeavesBlock {
    /**
     * orig BlockAppleLeaves.java:59 — DimensionID4, named "Dimension-Islands"
     * (orig WorldProviderOreSpawn4.java:23). Night transform and the reduced
     * fruit chance only apply there.
     */
    private static final ResourceKey<Level> ISLANDS =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "islands"));
    // orig BlockAppleLeaves.java:58 — chance = 20 (overworld etc.)
    private static final int FRUIT_ATTEMPT_ROLL_BOUND = 20;
    // orig BlockAppleLeaves.java:59-62 — chance = 100 in Dimension-Islands
    private static final int FRUIT_ATTEMPT_ROLL_BOUND_ISLANDS = 100;
    private static final int FRUIT_ATTEMPT_SUCCESS_INDEX = 3;
    private static final long TICKS_PER_DAY = 24000L;
    private static final long NIGHT_START_TICK = 12000L;
    private static final int APPLE_ROLL_BOUND = 25;
    private static final int APPLE_SUCCESS_INDEX = 1;
    private static final int GOLDEN_APPLE_ROLL_BOUND = 500;
    private static final int GOLDEN_APPLE_SUCCESS_INDEX = 2;
    private static final int ENCHANTED_GOLDEN_APPLE_ROLL_BOUND = 1000;
    private static final int ENCHANTED_GOLDEN_APPLE_SUCCESS_INDEX = 3;
    private static final int MAGIC_APPLE_ROLL_BOUND = 10000;
    private static final int MAGIC_APPLE_SUCCESS_INDEX = 4;

    public BlockAppleLeaves(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        boolean inIslands = level.dimension().equals(ISLANDS);

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        // orig BlockAppleLeaves.java:58-62,70-73 — fruit roll 1/20, 1/100 in Islands
        int fruitRollBound = inIslands ? FRUIT_ATTEMPT_ROLL_BOUND_ISLANDS : FRUIT_ATTEMPT_ROLL_BOUND;
        if (belowState.is(Blocks.AIR) && random.nextInt(fruitRollBound) == FRUIT_ATTEMPT_SUCCESS_INDEX) {
            dropAppleProducts(level, below, random);
        }

        // orig BlockAppleLeaves.java:74-77 — night transform to ScaryLeaves
        // ONLY in Dimension-Islands (DimensionID4)
        long timeOfDayTicks = level.getDayTime() % TICKS_PER_DAY;
        if (timeOfDayTicks > NIGHT_START_TICK && inIslands) {
            level.setBlock(pos, ModBlocks.SCARY_LEAVES.get().defaultBlockState(), 3);
        }
    }

    private void dropAppleProducts(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(APPLE_ROLL_BOUND) == APPLE_SUCCESS_INDEX) {
            Block.popResource(level, pos, new ItemStack(Items.APPLE));
        }
        if (random.nextInt(GOLDEN_APPLE_ROLL_BOUND) == GOLDEN_APPLE_SUCCESS_INDEX) {
            Block.popResource(level, pos, new ItemStack(Items.GOLDEN_APPLE));
        }
        if (random.nextInt(ENCHANTED_GOLDEN_APPLE_ROLL_BOUND) == ENCHANTED_GOLDEN_APPLE_SUCCESS_INDEX) {
            Block.popResource(level, pos, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        }
        if (random.nextInt(MAGIC_APPLE_ROLL_BOUND) == MAGIC_APPLE_SUCCESS_INDEX) {
            Block.popResource(level, pos, new ItemStack(ModItems.MAGIC_APPLE.get()));
        }
    }
}
