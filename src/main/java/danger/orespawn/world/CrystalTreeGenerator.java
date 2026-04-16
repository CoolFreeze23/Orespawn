package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Generates crystal trees in the Crystal dimension.
 * Ported from ChunkProviderOreSpawn5.addCrystalTrees, TallCrystalTree,
 * and ScragglyCrystalTreeWithBranches.
 *
 * Two types: Tall Crystal Trees (straight trunk, leaf rings, 7x7 canopy)
 * and Scraggly Crystal Trees (crooked trunk with random branches).
 */
public class CrystalTreeGenerator {

    public static void generate(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(5) != 0) return;

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int what = random.nextInt(5);
        int howmany = random.nextInt(8);
        if (what != 0) howmany *= 2;

        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();

        for (int i = 0; i < howmany; i++) {
            int posX = 4 + minX + random.nextInt(8);
            int posZ = 4 + minZ + random.nextInt(8);
            for (int posY = 128; posY > 40; posY--) {
                BlockPos pos = new BlockPos(posX, posY, posZ);
                BlockPos below = new BlockPos(posX, posY - 1, posZ);
                if (!chunk.getBlockState(pos).isAir() || !chunk.getBlockState(below).equals(crystalGrass))
                    continue;

                if (what == 0) {
                    placeTallTree(chunk, random, posX, posY, posZ);
                } else {
                    placeScragglyTree(chunk, random, posX, posY, posZ);
                }
                break;
            }
        }
    }

    /**
     * Tall crystal tree: 10-22 block straight trunk, then continues upward with
     * leaf rings every 4th block, topped with a 7x7 canopy and a 3x3 cap.
     */
    private static void placeTallTree(ChunkAccess chunk, RandomSource random, int x, int y, int z) {
        BlockState log = ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
        BlockState leaves = ModBlocks.CRYSTAL_LEAVES.get().defaultBlockState();

        int trunkHeight = 10 + random.nextInt(12);
        int totalHeight = trunkHeight + random.nextInt(18);

        for (int k = 0; k < trunkHeight; k++) {
            BlockPos pos = new BlockPos(x, y + k, z);
            if (k >= 1 && !canReplace(chunk, pos)) return;
            safeSet(chunk, x, y + k, z, log);
        }

        int cy = y + trunkHeight - 1;
        for (int k = trunkHeight; k < totalHeight; k++) {
            cy++;
            BlockPos pos = new BlockPos(x, cy, z);
            if (!canReplace(chunk, pos)) break;
            safeSet(chunk, x, cy, z, log);

            if (k % 4 == 0) {
                for (int m = -1; m < 2; m++) {
                    for (int n = -1; n < 2; n++) {
                        if (random.nextInt(2) != 1) continue;
                        BlockPos lp = new BlockPos(x + m, cy, z + n);
                        if (isInChunk(chunk, x + m, z + n) && chunk.getBlockState(lp).isAir()) {
                            safeSet(chunk, x + m, cy, z + n, leaves);
                        }
                    }
                }
            }
        }

        cy++;
        for (int m = -1; m < 2; m++) {
            for (int n = -1; n < 2; n++) {
                if (random.nextInt(2) != 1) continue;
                if (isInChunk(chunk, x + m, z + n) && chunk.getBlockState(new BlockPos(x + m, cy, z + n)).isAir()) {
                    safeSet(chunk, x + m, cy, z + n, log);
                }
            }
        }
        for (int m = -3; m < 4; m++) {
            for (int n = -3; n < 4; n++) {
                if (!isInChunk(chunk, x + m, z + n)) continue;
                if (chunk.getBlockState(new BlockPos(x + m, cy, z + n)).isAir()) {
                    safeSet(chunk, x + m, cy, z + n, leaves);
                }
            }
        }

        cy++;
        for (int m = -1; m < 2; m++) {
            for (int n = -1; n < 2; n++) {
                if (!isInChunk(chunk, x + m, z + n)) continue;
                if (chunk.getBlockState(new BlockPos(x + m, cy, z + n)).isAir()) {
                    safeSet(chunk, x + m, cy, z + n, leaves);
                }
            }
        }
    }

    /**
     * Scraggly crystal tree: short straight base, then a crooked trunk that
     * wanders randomly with branches splitting off. Uses CrystalLeaves2.
     */
    private static void placeScragglyTree(ChunkAccess chunk, RandomSource random, int x, int y, int z) {
        BlockState log = ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
        BlockState leaves = ModBlocks.CRYSTAL_LEAVES_2.get().defaultBlockState();

        int baseHeight = 1 + random.nextInt(2);
        int totalHeight = baseHeight + random.nextInt(8);

        for (int k = 0; k < baseHeight; k++) {
            BlockPos pos = new BlockPos(x, y + k, z);
            if (k >= 1 && !canReplace(chunk, pos)) return;
            safeSet(chunk, x, y + k, z, log);
        }

        y += baseHeight - 1;
        int cx = x, cy = y, cz = z;

        for (int k = baseHeight; k < totalHeight; k++) {
            int ix = random.nextInt(2) - random.nextInt(2);
            int iz = random.nextInt(2) - random.nextInt(2);
            int iy = random.nextInt(4) > 0 ? 1 : 0;
            cx += ix;
            cy += iy;
            cz += iz;

            if (!isInChunk(chunk, cx, cz)) break;
            BlockPos pos = new BlockPos(cx, cy, cz);
            if (!canReplace(chunk, pos)) break;

            safeSet(chunk, cx, cy, cz, log);

            if (random.nextInt(4) == 1) {
                placeBranch(chunk, random, cx, cy, cz,
                        random.nextInt(1 + totalHeight - k),
                        random.nextInt(2) - random.nextInt(2),
                        random.nextInt(2) - random.nextInt(2),
                        log, leaves);
            }

            for (int m = -1; m < 2; m++) {
                for (int n = -1; n < 2; n++) {
                    if (random.nextInt(2) != 1) continue;
                    if (!isInChunk(chunk, cx + m, cz + n)) continue;
                    if (chunk.getBlockState(new BlockPos(cx + m, cy, cz + n)).isAir()) {
                        safeSet(chunk, cx + m, cy, cz + n, leaves);
                    }
                }
            }

            if (random.nextInt(2) == 1 && isInChunk(chunk, cx, cz)
                    && chunk.getBlockState(new BlockPos(cx, cy + 1, cz)).isAir()) {
                safeSet(chunk, cx, cy + 1, cz, leaves);
            }
        }
    }

    private static void placeBranch(ChunkAccess chunk, RandomSource random,
                                     int x, int y, int z, int len, int biasX, int biasZ,
                                     BlockState log, BlockState leaves) {
        int cx = x, cy = y, cz = z;
        for (int k = 0; k < len; k++) {
            int ix = Math.max(-1, Math.min(1, random.nextInt(2) - random.nextInt(2) + biasX));
            int iz = Math.max(-1, Math.min(1, random.nextInt(2) - random.nextInt(2) + biasZ));
            int iy = random.nextInt(3) > 0 ? 1 : 0;
            cx += ix;
            cy += iy;
            cz += iz;

            if (!isInChunk(chunk, cx, cz)) return;
            BlockPos pos = new BlockPos(cx, cy, cz);
            if (!canReplace(chunk, pos)) return;

            safeSet(chunk, cx, cy, cz, log);

            for (int m = -1; m < 2; m++) {
                for (int n = -1; n < 2; n++) {
                    if (random.nextInt(2) != 1) continue;
                    if (!isInChunk(chunk, cx + m, cz + n)) continue;
                    if (chunk.getBlockState(new BlockPos(cx + m, cy, cz + n)).isAir()) {
                        safeSet(chunk, cx + m, cy, cz + n, leaves);
                    }
                }
            }

            if (random.nextInt(2) == 1 && isInChunk(chunk, cx, cz)
                    && chunk.getBlockState(new BlockPos(cx, cy + 1, cz)).isAir()) {
                safeSet(chunk, cx, cy + 1, cz, leaves);
            }
        }
    }

    private static boolean canReplace(ChunkAccess chunk, BlockPos pos) {
        BlockState state = chunk.getBlockState(pos);
        return state.isAir()
                || state.equals(ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState())
                || state.equals(ModBlocks.CRYSTAL_LEAVES.get().defaultBlockState())
                || state.equals(ModBlocks.CRYSTAL_LEAVES_2.get().defaultBlockState());
    }

    private static boolean isInChunk(ChunkAccess chunk, int worldX, int worldZ) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        return worldX >= minX && worldX < minX + 16 && worldZ >= minZ && worldZ < minZ + 16;
    }

    private static void safeSet(ChunkAccess chunk, int x, int y, int z, BlockState state) {
        if (isInChunk(chunk, x, z) && y >= chunk.getMinBuildHeight() && y < chunk.getMaxBuildHeight()) {
            chunk.setBlockState(new BlockPos(x, y, z), state, false);
        }
    }
}
