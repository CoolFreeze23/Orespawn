package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Faithful port of the 1.7.10 "scraggly tree" generator shared by the Islands
 * and Chaos chunk providers (orig ChunkProviderOreSpawn4.java:109-196 and
 * ChunkProviderOreSpawn6.java:335-427 — the two copies are byte-identical
 * apart from the per-dimension scan ranges, which the callers own).
 *
 * <p>Shape: a 1-3 block straight oak trunk, then a drunken upward walk of
 * {@code trunk + nextInt(12)} segments that each step ±1 in X/Z and usually +1
 * in Y, sprouting Apple-Leaf clusters (orig uses {@code OreSpawnMain.MyAppleLeaves},
 * ChunkProviderOreSpawn4.java:156) and 1-in-4 random branches along the way.</p>
 *
 * <p>All writes are clamped to the owning chunk (the original wrote through
 * {@code setBlockIDWithMetadataInChunk}, which was likewise chunk-local), so
 * this is safe to call from the parallel worldgen workers.</p>
 */
public final class ScragglyTrees {

    private ScragglyTrees() {
    }

    /**
     * Port of {@code ScragglyTreeWithBranches} (orig ChunkProviderOreSpawn4.java:164-196).
     * (x, y, z) is the first trunk block (one above the grass the caller found).
     */
    public static void scragglyTreeWithBranches(ChunkAccess chunk, RandomSource random, int x, int y, int z) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        // orig ChunkProviderOreSpawn4.java:167-168 — trunk 1-3, walk extends by nextInt(12)
        int trunk = 1 + random.nextInt(3);
        int total = trunk + random.nextInt(12);
        for (int k = 0; k < trunk; k++) {
            BlockState bid = getInChunk(chunk, x, y + k, z);
            // orig :171 — abort if the trunk runs into anything but air/log/apple leaves
            if (k >= 1 && bid != null && !bid.isAir() && !bid.is(Blocks.OAK_LOG) && !isAppleLeaves(bid)) {
                return;
            }
            setInChunk(chunk, x, y + k, z, log);
        }
        y += trunk - 1;
        for (int k = trunk; k < total; k++) {
            // orig :178-180 — drunken walk: ±1 X/Z, +1 Y three times out of four
            int ix = random.nextInt(2) - random.nextInt(2);
            int iz = random.nextInt(2) - random.nextInt(2);
            int iy = random.nextInt(4) > 0 ? 1 : 0;
            x += ix;
            y += iy;
            z += iz;
            BlockState bid = getInChunk(chunk, x, y, z);
            if (bid == null || (!bid.isAir() && !bid.is(Blocks.OAK_LOG) && !isAppleLeaves(bid))) {
                break;
            }
            setInChunk(chunk, x, y, z, log);
            // orig :184-186 — 1-in-4 chance of a side branch with remaining-length budget
            if (random.nextInt(4) == 1) {
                makeScragglyBranch(chunk, random, x, y, z, random.nextInt(1 + total - k),
                        random.nextInt(2) - random.nextInt(2), random.nextInt(2) - random.nextInt(2));
            }
            sprinkleLeaves(chunk, random, x, y, z);
        }
    }

    /**
     * Port of {@code makeScragglyBranch} (orig ChunkProviderOreSpawn4.java:131-162):
     * biased drunken walk of {@code len} log segments with leaf sprinkles.
     */
    private static void makeScragglyBranch(ChunkAccess chunk, RandomSource random,
                                           int x, int y, int z, int len, int biasx, int biasz) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        for (int k = 0; k < len; k++) {
            int ix = clampUnit(random.nextInt(2) - random.nextInt(2) + biasx);
            int iz = clampUnit(random.nextInt(2) - random.nextInt(2) + biasz);
            // orig :149 — branch rises two ticks out of three
            int iy = random.nextInt(3) > 0 ? 1 : 0;
            x += ix;
            y += iy;
            z += iz;
            BlockState bid = getInChunk(chunk, x, y, z);
            if (bid == null || (!bid.isAir() && !bid.is(Blocks.OAK_LOG) && !isAppleLeaves(bid))) {
                return;
            }
            setInChunk(chunk, x, y, z, log);
            sprinkleLeaves(chunk, random, x, y, z);
        }
    }

    /**
     * Leaf halo shared by trunk and branch segments (orig ChunkProviderOreSpawn4.java:153-160):
     * each of the 8 surrounding columns gets an Apple-Leaf block on a coin flip
     * if air, plus a coin-flip cap leaf directly above.
     */
    private static void sprinkleLeaves(ChunkAccess chunk, RandomSource random, int x, int y, int z) {
        BlockState leaves = ModBlocks.APPLE_LEAVES.get().defaultBlockState();
        for (int m = -1; m < 2; m++) {
            for (int n = -1; n < 2; n++) {
                if (random.nextInt(2) != 1) continue;
                BlockState bid = getInChunk(chunk, x + m, y, z + n);
                if (bid != null && bid.isAir()) {
                    setInChunk(chunk, x + m, y, z + n, leaves);
                }
            }
        }
        if (random.nextInt(2) == 1) {
            BlockState bid = getInChunk(chunk, x, y + 1, z);
            if (bid != null && bid.isAir()) {
                setInChunk(chunk, x, y + 1, z, leaves);
            }
        }
    }

    private static boolean isAppleLeaves(BlockState state) {
        return state.is(ModBlocks.APPLE_LEAVES.get());
    }

    private static int clampUnit(int v) {
        return Math.max(-1, Math.min(1, v));
    }

    /** Chunk-local read; returns null when (x,z) is outside the chunk or y out of bounds. */
    private static BlockState getInChunk(ChunkAccess chunk, int x, int y, int z) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        if (x < minX || x >= minX + 16 || z < minZ || z >= minZ + 16) return null;
        if (y < chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) return null;
        return chunk.getBlockState(new BlockPos(x, y, z));
    }

    private static void setInChunk(ChunkAccess chunk, int x, int y, int z, BlockState state) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        if (x < minX || x >= minX + 16 || z < minZ || z >= minZ + 16) return;
        if (y < chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) return;
        chunk.setBlockState(new BlockPos(x, y, z), state, false);
    }
}
