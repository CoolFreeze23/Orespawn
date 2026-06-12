package danger.orespawn.block;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Duplicator Log, ported from 1.7.10 BlockDuplicatorLog + Trees.DuplicatorTree.
 *
 * <p>orig BlockDuplicatorLog.java:32-39 — every random tick (gated by the
 * {@code enableduplicatortree} config) calls
 * {@code OreSpawnTrees.DuplicatorTree} (orig Trees.java:121-182), which builds
 * the tree incrementally, ONE block per tick:</p>
 * <ol>
 *   <li>requires grass/dirt/farmland directly below the log (orig :125-139;
 *   the decompiled y-2/y-3 probes are dead code — that branch always
 *   returns);</li>
 *   <li>extends the trunk to 3 duplicator logs (orig :140-154);</li>
 *   <li>caps it with an apple-leaves block (orig :155-159) and a 3x3
 *   apple-leaves ring around the top log (orig :160-166);</li>
 *   <li>once complete, each tick picks a random non-air, non-log block in the
 *   5x5 area at trunk-base level (20 tries) and copies it to a random air
 *   spot in the same area (20 tries) — the actual block duplication
 *   (orig :167-181).</li>
 * </ol>
 */
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
        if (level.isClientSide()) return;
        // orig BlockDuplicatorLog.java:36 — enableduplicatortree config gate
        if (!OreSpawnConfig.DUPLICATOR_TREE_ENABLE.get()) return;
        duplicatorTree(level, pos, random);
    }

    /**
     * orig Trees.java:121-182 — incremental tree build plus the 5x5 block
     * duplication step. {@code pos} is the position of the calling log.
     */
    private void duplicatorTree(ServerLevel level, BlockPos pos, RandomSource random) {
        Block dt = ModBlocks.DUPLICATOR_LOG.get();

        // orig Trees.java:125-139 — only grass/dirt/farmland directly below counts
        BlockState soil = level.getBlockState(pos.below());
        if (!soil.is(Blocks.GRASS_BLOCK) && !soil.is(Blocks.DIRT) && !soil.is(Blocks.FARMLAND)) {
            return;
        }
        BlockPos base = pos.below(); // orig "realy" — soil position

        // orig Trees.java:140-154 — grow trunk to 3 logs, one per tick
        for (int dy = 1; dy <= 3; ++dy) {
            BlockPos trunk = base.above(dy);
            if (!level.getBlockState(trunk).is(dt)) {
                level.setBlock(trunk, dt.defaultBlockState(), 2);
                return;
            }
        }

        // orig Trees.java:155-159 — apple-leaves cap above the trunk
        BlockState leaves = ModBlocks.APPLE_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        BlockPos cap = base.above(4);
        if (!level.getBlockState(cap).is(ModBlocks.APPLE_LEAVES.get())) {
            level.setBlock(cap, leaves, 2);
            return;
        }

        // orig Trees.java:160-166 — 3x3 leaf ring around the top log
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0) continue;
                BlockPos ring = base.above(3).offset(i, 0, j);
                if (!level.getBlockState(ring).is(ModBlocks.APPLE_LEAVES.get())) {
                    level.setBlock(ring, leaves, 2);
                    return;
                }
            }
        }

        // orig Trees.java:167-181 — duplicate a random nearby block: 20 tries to
        // find a non-air, non-log source in the 5x5 at trunk-base level, then 20
        // tries to find an air destination in the same area
        for (int tries = 0; tries < 20; ++tries) {
            int i = random.nextInt(5) - 2;
            int j = random.nextInt(5) - 2;
            BlockPos srcPos = base.above(1).offset(i, 0, j);
            BlockState src = level.getBlockState(srcPos);
            if (src.isAir() || src.is(dt)) continue;
            for (int k = 0; k < 20; ++k) {
                int di = random.nextInt(5) - 2;
                int dj = random.nextInt(5) - 2;
                BlockPos dstPos = base.above(1).offset(di, 0, dj);
                if (!level.getBlockState(dstPos).isAir()) continue;
                level.setBlock(dstPos, src, 2);
                return;
            }
            return;
        }
    }
}
