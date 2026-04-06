package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCrystalLeaves extends LeavesBlock {
    private static final int DEFAULT_FRUIT_ATTEMPT_ROLL_BOUND = 20;
    private static final int FRUIT_ATTEMPT_SUCCESS_INDEX = 3;
    private static final int CRYSTAL_APPLE_ROLL_BOUND = 100;
    private static final int CRYSTAL_APPLE_SUCCESS_INDEX = 1;
    private static final int SAPLING_ROLL_BOUND = 50;
    private static final int SAPLING_SUCCESS_INDEX = 1;

    public BlockCrystalLeaves(BlockBehaviour.Properties properties) {
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

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        int fruitAttemptRollBound = DEFAULT_FRUIT_ATTEMPT_ROLL_BOUND;
        // TODO: Increase chance in crystal dimension

        if (belowState.is(Blocks.AIR) && random.nextInt(fruitAttemptRollBound) == FRUIT_ATTEMPT_SUCCESS_INDEX) {
            dropCrystalProducts(level, below, random);
        }
    }

    private void dropCrystalProducts(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(CRYSTAL_APPLE_ROLL_BOUND) == CRYSTAL_APPLE_SUCCESS_INDEX) {
            // TODO: Drop CrystalApple
            // Block.popResource(level, pos, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        }
        if (random.nextInt(SAPLING_ROLL_BOUND) == SAPLING_SUCCESS_INDEX) {
            // TODO: Drop CrystalPlant sapling based on which variant this leaves block is
            // Block.popResource(level, pos, new ItemStack(ModBlocks.CRYSTAL_PLANT.get()));
        }
    }
}
