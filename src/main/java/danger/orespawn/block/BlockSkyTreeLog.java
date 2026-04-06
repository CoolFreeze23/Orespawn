package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSkyTreeLog extends Block {
    private static final int MAX_BREAK_RECURSION_DEPTH = 1000;

    public BlockSkyTreeLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            breakAdjacentLogs(level, pos, pos, 0);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void breakAdjacentLogs(Level level, BlockPos pos, BlockPos brokenFrom, int searchDepth) {
        if (searchDepth > MAX_BREAK_RECURSION_DEPTH) return;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos neighbor = pos.offset(dx, dy, dz);

                    if (neighbor.equals(brokenFrom)) continue;
                    if (searchDepth > 0 && isAdjacent(neighbor, brokenFrom)) continue;

                    BlockState neighborState = level.getBlockState(neighbor);
                    if (neighborState.is(this)) {
                        level.setBlock(neighbor, Blocks.AIR.defaultBlockState(), 2);
                        Block.dropResources(neighborState, level, neighbor);
                        breakAdjacentLogs(level, neighbor, pos, searchDepth + 1);
                    }
                }
            }
        }
    }

    private boolean isAdjacent(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) <= 1
                && Math.abs(a.getY() - b.getY()) <= 1
                && Math.abs(a.getZ() - b.getZ()) <= 1;
    }
}
