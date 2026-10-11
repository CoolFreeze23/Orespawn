package danger.orespawn.block;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The original's fruit, scary and experience leaves' own rule for staying (BlockAppleLeaves, BlockScaryLeaves,
 * BlockExperienceLeaves updateTick; BUG-049): on each random tick a leaf looks for a block that holds leaves within
 * {@code r} each way across and down to {@code r} below (never above), at most 3 steps away counted along the axes; with
 * one it does its own work (fruit, the day and night turns), without one it falls (its drops, then air). The original
 * overrides BlockLeaves' decay entirely: no decay bit, no player-placed exception. So a tree's leaves keep only to its
 * trunk and branches, and a generated crown wider than that wears away once its chunk ticks.
 */
public final class LegacyLeaves {
    private LegacyLeaves() {
    }

    /**
     * Forge 1.7.10's canSustainLeaves: true for BlockLog (vanilla's logs, the logs tag here) and for the OreSpawn logs
     * that override it (BlockSkyTreeLog, BlockCrystalTreeLog, BlockDuplicatorLog).
     */
    public static boolean sustains(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(ModBlocks.SKY_TREE_LOG.get()) || state.is(ModBlocks.CRYSTAL_TREE_LOG.get())
                || state.is(ModBlocks.DUPLICATOR_LOG.get());
    }

    /**
     * The search (orig BlockAppleLeaves.java:64-80): null when the area round the leaf is not loaded (the original does
     * nothing then, {@code checkChunksExist}), else whether a block that holds leaves stands within {@code r} across,
     * {@code r} below to level, and 3 steps.
     */
    public static Boolean logNear(ServerLevel level, BlockPos pos, int r) {
        if (!level.hasChunksAt(pos.offset(-r, -r, -r), pos.offset(r, r, r))) return null;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dy = -r; dy <= 0; ++dy) {
                for (int dz = -r; dz <= r; ++dz) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 3) continue;
                    if (sustains(level.getBlockState(at.setWithOffset(pos, dx, dy, dz)))) return true;
                }
            }
        }
        return false;
    }
}
