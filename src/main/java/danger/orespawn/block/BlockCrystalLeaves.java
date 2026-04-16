package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;

/**
 * Crystal leaves: decorative leaf block with random fruit drops.
 *
 * <p>Extends {@link Block} directly instead of {@code LeavesBlock} because the
 * vanilla leaves class has baked-in decay mechanics that require proximity to a
 * block in the {@code minecraft:logs} tag. Our {@code crystal_tree_log} is not
 * in that tag (it's a custom modded block), so every crystal leaf would decay
 * and fall on chunk load — which is exactly the "leaves breaking everywhere"
 * bug we saw in initial testing.</p>
 *
 * <p>Instead we implement a lightweight random-tick that occasionally drops a
 * Crystal Apple or Crystal Sapling into the air block below, mimicking the
 * 1.7.10 behavior without coupling to vanilla decay.</p>
 */
public class BlockCrystalLeaves extends Block {
    private static final int FRUIT_ATTEMPT_ROLL_BOUND = 20;
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
        if (level.isClientSide()) return;

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (belowState.is(Blocks.AIR) && random.nextInt(FRUIT_ATTEMPT_ROLL_BOUND) == FRUIT_ATTEMPT_SUCCESS_INDEX) {
            dropCrystalProducts(level, below, random);
        }
    }

    private void dropCrystalProducts(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(CRYSTAL_APPLE_ROLL_BOUND) == CRYSTAL_APPLE_SUCCESS_INDEX) {
            Block.popResource(level, pos, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        }
        if (random.nextInt(SAPLING_ROLL_BOUND) == SAPLING_SUCCESS_INDEX) {
            Block.popResource(level, pos, new ItemStack(ModBlocks.CRYSTAL_SAPLING.get()));
        }
    }
}
