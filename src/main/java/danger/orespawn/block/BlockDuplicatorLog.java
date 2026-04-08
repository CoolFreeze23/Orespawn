package danger.orespawn.block;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDuplicatorLog extends Block {
    public BlockDuplicatorLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Config: disable duplicator tree growth entirely
        if (!OreSpawnConfig.DUPLICATOR_TREE_ENABLE.get()) return;
        if (level.isClientSide()) return;
        if (random.nextInt(5) != 0) return;

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) return;

        int height = 4 + random.nextInt(3);
        BlockState logState = ModBlocks.DUPLICATOR_LOG.get().defaultBlockState();

        for (int y = 1; y <= height; y++) {
            BlockPos logPos = pos.above(y);
            if (level.isEmptyBlock(logPos) || level.getBlockState(logPos).canBeReplaced()) {
                level.setBlock(logPos, logState, 3);
            }
        }

        BlockState leafState = Blocks.OAK_LEAVES.defaultBlockState();
        int leafStart = height - 2;
        for (int dy = leafStart; dy <= height + 1; dy++) {
            int radius = (dy <= height - 1) ? 2 : 1;
            if (dy == height + 1) radius = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0 && dy <= height) continue;
                    BlockPos leafPos = pos.above(dy).offset(dx, 0, dz);
                    if (level.isEmptyBlock(leafPos) || level.getBlockState(leafPos).canBeReplaced()) {
                        level.setBlock(leafPos, leafState, 3);
                    }
                }
            }
        }
    }
}
