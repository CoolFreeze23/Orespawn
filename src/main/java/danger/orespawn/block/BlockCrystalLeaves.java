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

public class BlockCrystalLeaves extends LeavesBlock {
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

        int chance = 20;
        // TODO: Increase chance in crystal dimension

        if (belowState.is(Blocks.AIR) && random.nextInt(chance) == 3) {
            dropCrystalProducts(level, below, random);
        }
    }

    private void dropCrystalProducts(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 1) {
            // TODO: Drop CrystalApple
            // Block.popResource(level, pos, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        }
        if (random.nextInt(50) == 1) {
            // TODO: Drop CrystalPlant sapling based on which variant this leaves block is
            // Block.popResource(level, pos, new ItemStack(ModBlocks.CRYSTAL_PLANT.get()));
        }
    }
}
