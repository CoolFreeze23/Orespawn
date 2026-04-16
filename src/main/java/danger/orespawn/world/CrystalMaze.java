package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a procedural maze at Y=25 in every Crystal dimension chunk.
 * Ported from the original 1.7.10 CrystalMaze using Prim's randomized algorithm.
 * The maze is a 4x4 grid of cells, each 4 blocks wide, fitting exactly in one 16x16 chunk.
 * Walls are bedrock, with a bedrock floor and ceiling. Random holes allow vertical access.
 */
public class CrystalMaze {

    private static final int WTOP = 1;
    private static final int WRGT = 2;
    private static final int WBOT = 4;
    private static final int WLFT = 8;
    private static final int BORDER_TOP = 16;
    private static final int BORDER_RIGHT = 32;
    private static final int BORDER_BOTTOM = 64;
    private static final int BORDER_LEFT = 128;

    public static void generate(ChunkAccess chunk, RandomSource random, int baseX, int baseY, int baseZ) {
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 3; k++) {
                    setBlock(chunk, baseX + j, baseY + k, baseZ + i, air);
                }
            }
        }

        buildWalls(chunk, random, baseX, baseY, baseZ, 4, 4, 4);
        addFloorCeilingAndHoles(chunk, random, baseX, baseY, baseZ, 4, 4, 4);
    }

    private static void addFloorCeilingAndHoles(ChunkAccess chunk, RandomSource random,
                                                 int xx, int yy, int zz, int xw, int zw, int csz) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        int size = zw * csz;

        for (int i = 0; i < size; i++) {
            for (int h = 0; h < 3; h++) {
                setBlock(chunk, xx, yy + h, zz + i, air);
                setBlock(chunk, xx + i, yy + h, zz, air);
                setBlock(chunk, xx + size - 1, yy + h, zz + i, air);
                setBlock(chunk, xx + i, yy + h, zz + size - 1, air);
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                setBlock(chunk, xx + j, yy - 1, zz + i, bedrock);
                setBlock(chunk, xx + j, yy + 3, zz + i, bedrock);
            }
        }

        for (int k = 0; k < 4; k++) {
            int hx = random.nextInt(size);
            int hz = random.nextInt(size);
            setBlock(chunk, xx + hx, yy + 3, zz + hz, crystalStone);
        }

        int fx = random.nextInt(size);
        int fz = random.nextInt(size);
        setBlock(chunk, xx + fx, yy - 1, zz + fz, crystalStone);
    }

    private static void buildWalls(ChunkAccess chunk, RandomSource random,
                                    int xx, int yy, int zz, int gridw, int gridh, int cellsize) {
        if (cellsize < 3) cellsize = 3;

        int[][] cells = new int[gridw][gridh];
        for (int x = 0; x < gridw; x++)
            for (int y = 0; y < gridh; y++)
                cells[x][y] = WTOP | WRGT | WBOT | WLFT;

        for (int y = 0; y < gridh; y++) {
            cells[0][y] |= BORDER_LEFT;
            cells[gridw - 1][y] |= BORDER_RIGHT;
        }
        for (int x = 0; x < gridw; x++) {
            cells[x][0] |= BORDER_TOP;
            cells[x][gridh - 1] |= BORDER_BOTTOM;
        }

        List<int[]> outlist = new ArrayList<>(gridw * gridh);
        List<int[]> inlist = new ArrayList<>();
        List<int[]> frontlist = new ArrayList<>();

        for (int x = 0; x < gridw; x++)
            for (int y = 0; y < gridh; y++)
                outlist.add(new int[]{x, y});

        int[] current = removeRandom(outlist, random);
        inlist.add(current);
        moveNeighbors(current, cells, outlist, frontlist);

        while (!frontlist.isEmpty()) {
            current = removeRandom(frontlist, random);
            inlist.add(current);
            moveNeighbors(current, cells, outlist, frontlist);
            int dir = findInNeighbor(current, cells, inlist, random);
            if (dir != 0) removeWall(current, dir, cells);
        }

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        for (int x = 0; x < gridw; x++) {
            for (int y = 0; y < gridh; y++) {
                int val = cells[x][y];
                if ((val & WTOP) != 0)
                    drawSide(chunk, x * cellsize, y * cellsize,
                            (x + 1) * cellsize, y * cellsize,
                            xx, yy, zz, cellsize, gridh, gridw, bedrock);
                if ((val & WRGT) != 0)
                    drawSide(chunk, (x + 1) * cellsize - 1, y * cellsize,
                            (x + 1) * cellsize - 1, (y + 1) * cellsize,
                            xx, yy, zz, cellsize, gridh, gridw, bedrock);
                if ((val & WBOT) != 0)
                    drawSide(chunk, x * cellsize, (y + 1) * cellsize - 1,
                            (x + 1) * cellsize, (y + 1) * cellsize - 1,
                            xx, yy, zz, cellsize, gridh, gridw, bedrock);
                if ((val & WLFT) != 0)
                    drawSide(chunk, x * cellsize, y * cellsize,
                            x * cellsize, (y + 1) * cellsize,
                            xx, yy, zz, cellsize, gridh, gridw, bedrock);
            }
        }
    }

    private static void drawSide(ChunkAccess chunk, int fromx, int fromz, int tox, int toz,
                                  int ox, int oy, int oz, int cellsize, int gridh, int gridw,
                                  BlockState block) {
        if (fromx > tox) { int t = fromx; fromx = tox; tox = t; }
        if (fromz > toz) { int t = fromz; fromz = toz; toz = t; }

        if (fromx == tox) {
            for (int j = fromz; j <= toz; j++) {
                if (j >= cellsize * gridh) continue;
                for (int h = 0; h < 3; h++)
                    setBlock(chunk, fromx + ox, oy + h, j + oz, block);
            }
        } else {
            for (int i = fromx; i <= tox; i++) {
                if (i >= cellsize * gridw) continue;
                for (int h = 0; h < 3; h++)
                    setBlock(chunk, i + ox, oy + h, fromz + oz, block);
            }
        }
    }

    private static int findInNeighbor(int[] p, int[][] cells, List<int[]> inlist, RandomSource random) {
        int d = random.nextInt(4);
        for (int k = 0; k < 4; k++) {
            switch (d) {
                case 0:
                    if ((cells[p[0]][p[1]] & BORDER_TOP) == 0 && containsPoint(inlist, p[0], p[1] - 1))
                        return WTOP;
                    break;
                case 1:
                    if ((cells[p[0]][p[1]] & BORDER_RIGHT) == 0 && containsPoint(inlist, p[0] + 1, p[1]))
                        return WRGT;
                    break;
                case 2:
                    if ((cells[p[0]][p[1]] & BORDER_BOTTOM) == 0 && containsPoint(inlist, p[0], p[1] + 1))
                        return WBOT;
                    break;
                case 3:
                    if ((cells[p[0]][p[1]] & BORDER_LEFT) == 0 && containsPoint(inlist, p[0] - 1, p[1]))
                        return WLFT;
                    break;
            }
            d = (d + 1) % 4;
        }
        return 0;
    }

    private static void moveNeighbors(int[] p, int[][] cells, List<int[]> outlist, List<int[]> frontlist) {
        if ((cells[p[0]][p[1]] & BORDER_TOP) == 0)
            movePoint(p[0], p[1] - 1, outlist, frontlist);
        if ((cells[p[0]][p[1]] & BORDER_RIGHT) == 0)
            movePoint(p[0] + 1, p[1], outlist, frontlist);
        if ((cells[p[0]][p[1]] & BORDER_BOTTOM) == 0)
            movePoint(p[0], p[1] + 1, outlist, frontlist);
        if ((cells[p[0]][p[1]] & BORDER_LEFT) == 0)
            movePoint(p[0] - 1, p[1], outlist, frontlist);
    }

    private static void movePoint(int x, int y, List<int[]> from, List<int[]> to) {
        for (int i = 0; i < from.size(); i++) {
            int[] p = from.get(i);
            if (p[0] == x && p[1] == y) {
                from.remove(i);
                to.add(p);
                return;
            }
        }
    }

    private static void removeWall(int[] p, int d, int[][] cells) {
        cells[p[0]][p[1]] ^= d;
        switch (d) {
            case WTOP -> cells[p[0]][p[1] - 1] ^= WBOT;
            case WRGT -> cells[p[0] + 1][p[1]] ^= WLFT;
            case WBOT -> cells[p[0]][p[1] + 1] ^= WTOP;
            case WLFT -> cells[p[0] - 1][p[1]] ^= WRGT;
        }
    }

    private static boolean containsPoint(List<int[]> list, int x, int y) {
        for (int[] p : list) {
            if (p[0] == x && p[1] == y) return true;
        }
        return false;
    }

    private static int[] removeRandom(List<int[]> list, RandomSource random) {
        int i = random.nextInt(list.size());
        return list.remove(i);
    }

    private static void setBlock(ChunkAccess chunk, int x, int y, int z, BlockState state) {
        chunk.setBlockState(new BlockPos(x, y, z), state, false);
    }
}
