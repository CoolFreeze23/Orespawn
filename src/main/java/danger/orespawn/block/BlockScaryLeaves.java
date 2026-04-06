package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;

public class BlockScaryLeaves extends LeavesBlock {

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

        long t = level.getDayTime() % 24000L;

        // Scary leaves transform to apple leaves during daytime
        if (variant == Variant.SCARY && t < 12000L) {
            // TODO: Replace with AppleLeaves
            // level.setBlock(pos, ModBlocks.APPLE_LEAVES.get().defaultBlockState(), 3);
        }

        BlockPos below = pos.below();
        if (level.getBlockState(below).is(Blocks.AIR) && random.nextInt(20) == 3) {
            dropFruit(level, below, random);
        }
    }

    private void dropFruit(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(25) != 1) return;
        switch (variant) {
            case CHERRY:
                // TODO: Block.popResource(level, pos, new ItemStack(ModItems.CHERRY.get()));
                break;
            case PEACH:
                // TODO: Block.popResource(level, pos, new ItemStack(ModItems.PEACH.get()));
                break;
            default:
                break;
        }
    }
}
