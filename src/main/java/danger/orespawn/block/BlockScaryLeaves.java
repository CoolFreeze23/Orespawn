package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;

public class BlockScaryLeaves extends LeavesBlock {

    private static final long TICKS_PER_DAY = 24000L;
    private static final long DAY_END_TICK = 12000L;
    private static final int FRUIT_DROP_ATTEMPT_ROLL_BOUND = 20;
    private static final int FRUIT_DROP_SUCCESS_INDEX = 3;
    private static final int FRUIT_KIND_ROLL_BOUND = 25;
    private static final int FRUIT_KIND_SUCCESS_INDEX = 1;

    public enum Variant { SCARY, CHERRY, PEACH }

    private final Variant variant;

    public BlockScaryLeaves(BlockBehaviour.Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        long timeOfDayTicks = level.getDayTime() % TICKS_PER_DAY;

        // Scary leaves transform to apple leaves during daytime
        if (variant == Variant.SCARY && timeOfDayTicks < DAY_END_TICK) {
            level.setBlock(pos, ModBlocks.APPLE_LEAVES.get().defaultBlockState(), 3);
        }

        BlockPos below = pos.below();
        if (level.getBlockState(below).is(Blocks.AIR) && random.nextInt(FRUIT_DROP_ATTEMPT_ROLL_BOUND) == FRUIT_DROP_SUCCESS_INDEX) {
            dropFruit(level, below, random);
        }
    }

    private void dropFruit(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(FRUIT_KIND_ROLL_BOUND) != FRUIT_KIND_SUCCESS_INDEX) return;
        switch (variant) {
            case CHERRY:
                Block.popResource(level, pos, new ItemStack(ModItems.CHERRIES.get()));
                break;
            case PEACH:
                Block.popResource(level, pos, new ItemStack(ModItems.PEACH.get()));
                break;
            default:
                break;
        }
    }
}
