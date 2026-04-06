package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSkyTreeLog extends Block {
    public BlockSkyTreeLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            breakRecursor(level, pos, pos, 0);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void breakRecursor(Level level, BlockPos pos, BlockPos from, int recursion) {
        if (recursion > 1000) return;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos target = pos.offset(dx, dy, dz);

                    if (target.equals(from)) continue;
                    if (recursion > 0 && isNear(target, from)) continue;

                    BlockState targetState = level.getBlockState(target);
                    if (targetState.is(this)) {
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), 2);
                        Block.dropResources(targetState, level, target);
                        breakRecursor(level, target, pos, recursion + 1);
                    }
                }
            }
        }
    }

    private boolean isNear(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) <= 1
                && Math.abs(a.getY() - b.getY()) <= 1
                && Math.abs(a.getZ() - b.getZ()) <= 1;
    }
}
