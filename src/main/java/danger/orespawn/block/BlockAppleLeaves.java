package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;

public class BlockAppleLeaves extends LeavesBlock {
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

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (belowState.is(Blocks.AIR) && random.nextInt(20) == 3) {
            dropAppleProducts(level, below, random);
        }

        // Night-time transformation in Danger Dimension
        long t = level.getDayTime() % 24000L;
        if (t > 12000L) {
            // TODO: Check if in DimensionID4, then replace with ScaryLeaves
            // level.setBlock(pos, ModBlocks.SCARY_LEAVES.get().defaultBlockState(), 3);
        }
    }

    private void dropAppleProducts(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(25) == 1) {
            Block.popResource(level, pos, new ItemStack(Items.APPLE));
        }
        if (random.nextInt(500) == 2) {
            Block.popResource(level, pos, new ItemStack(Items.GOLDEN_APPLE));
        }
        if (random.nextInt(1000) == 3) {
            Block.popResource(level, pos, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        }
        if (random.nextInt(10000) == 4) {
            // TODO: Drop MagicApple from ModItems
            // Block.popResource(level, pos, new ItemStack(ModItems.MAGIC_APPLE.get()));
        }
    }
}
