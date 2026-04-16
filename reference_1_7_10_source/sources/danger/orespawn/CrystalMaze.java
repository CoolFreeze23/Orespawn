/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import java.awt.Point;
import java.util.Vector;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class CrystalMaze {
    public static final int WTOP = 1;
    public static final int WRGT = 2;
    public static final int WBOT = 4;
    public static final int WLFT = 8;

    public void buildCrystalMaze(World world, int x, int y, int z, Chunk chunk) {
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 3; ++k) {
                    OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(x + j), (int)(y + k), (int)(z + i), (Block)Blocks.field_150350_a, (int)0);
                }
            }
        }
        this.makeMaze(world, x, y, z, 4, 4, 4, 1, chunk);
        this.openCrystalMaze(world, x, y, z, 4, 4, 4, chunk);
    }

    private void openCrystalMaze(World world, int xx, int yy, int zz, int xw, int zw, int csz, Chunk chunk) {
        int j;
        int i;
        for (i = 0; i < zw * csz; ++i) {
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)xx, (int)yy, (int)(zz + i), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)xx, (int)(yy + 1), (int)(zz + i), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)xx, (int)(yy + 2), (int)(zz + i), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + i), (int)yy, (int)zz, (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + i), (int)(yy + 1), (int)zz, (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + i), (int)(yy + 2), (int)zz, (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + zw * csz - 1), (int)yy, (int)(zz + i), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + zw * csz - 1), (int)(yy + 1), (int)(zz + i), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + zw * csz - 1), (int)(yy + 2), (int)(zz + i), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + i), (int)yy, (int)(zz + zw * csz - 1), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + i), (int)(yy + 1), (int)(zz + zw * csz - 1), (Block)Blocks.field_150350_a, (int)0);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + i), (int)(yy + 2), (int)(zz + zw * csz - 1), (Block)Blocks.field_150350_a, (int)0);
        }
        for (i = 0; i < zw * csz; ++i) {
            for (j = 0; j < zw * csz; ++j) {
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + j), (int)(yy - 1), (int)(zz + i), (Block)Blocks.field_150357_h, (int)0);
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + j), (int)(yy + 3), (int)(zz + i), (Block)Blocks.field_150357_h, (int)0);
            }
        }
        for (int k = 0; k < 4; ++k) {
            i = world.field_73012_v.nextInt(zw * csz);
            j = world.field_73012_v.nextInt(zw * csz);
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + j), (int)(yy + 3), (int)(zz + i), (Block)OreSpawnMain.CrystalStone, (int)0);
        }
        i = world.field_73012_v.nextInt(zw * csz);
        j = world.field_73012_v.nextInt(zw * csz);
        OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(xx + j), (int)(yy - 1), (int)(zz + i), (Block)OreSpawnMain.CrystalStone, (int)0);
    }

    private void makeMaze(World world, int xx, int yy, int zz, int xw, int zw, int csz, int b, Chunk chunk) {
        int y;
        int x;
        int gridw = xw;
        int gridh = zw;
        int cellsize = csz;
        if (cellsize < 3) {
            cellsize = 3;
        }
        int[][] cells = new int[gridw][gridh];
        int full = 15;
        for (x = 0; x < gridw; ++x) {
            for (y = 0; y < gridh; ++y) {
                cells[x][y] = full;
            }
        }
        int left = 128;
        int right = 32;
        y = 0;
        while (y < gridh) {
            int[] nArray = cells[0];
            int n = y;
            nArray[n] = nArray[n] | left;
            int[] nArray2 = cells[gridw - 1];
            int n2 = y++;
            nArray2[n2] = nArray2[n2] | right;
        }
        int top = 16;
        int bottom = 64;
        for (x = 0; x < gridw; ++x) {
            int[] nArray = cells[x];
            nArray[0] = nArray[0] | top;
            int[] nArray3 = cells[x];
            int n = gridh - 1;
            nArray3[n] = nArray3[n] | bottom;
        }
        Vector<Point> outlist = new Vector<Point>(gridw * gridh);
        Vector<Point> inlist = new Vector<Point>(10, 10);
        Vector frontlist = new Vector(10, 10);
        for (x = 0; x < gridw; ++x) {
            for (y = 0; y < gridh; ++y) {
                outlist.addElement(new Point(x, y));
            }
        }
        Point current_cell = (Point)this.rndElement(outlist);
        inlist.addElement(current_cell);
        this.moveNbrs(current_cell, cells, outlist, frontlist);
        while (!frontlist.isEmpty()) {
            current_cell = (Point)this.rndElement(frontlist);
            inlist.addElement(current_cell);
            this.moveNbrs(current_cell, cells, outlist, frontlist);
            int dir = this.findInNbr(current_cell, cells, inlist);
            this.removeWall(current_cell, dir, cells);
        }
        current_cell = null;
        for (x = 0; x < gridw; ++x) {
            for (y = 0; y < gridh; ++y) {
                int val = cells[x][y];
                if ((val & 1) != 0) {
                    this.drawSide(world, x * cellsize, y * cellsize, (x + 1) * cellsize, y * cellsize, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
                if ((val & 2) != 0) {
                    this.drawSide(world, (x + 1) * cellsize - 1, y * cellsize, (x + 1) * cellsize - 1, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
                if ((val & 4) != 0) {
                    this.drawSide(world, x * cellsize, (y + 1) * cellsize - 1, (x + 1) * cellsize, (y + 1) * cellsize - 1, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
                if ((val & 8) == 0) continue;
                this.drawSide(world, x * cellsize, y * cellsize, x * cellsize, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
            }
        }
    }

    private void drawSide(World world, int fromx, int fromz, int tox, int toz, int x, int y, int z, int cellsize, int gridh, int gridw, int bb, Chunk chunk) {
        int i;
        Block blk = Blocks.field_150343_Z;
        if (bb != 0) {
            blk = Blocks.field_150357_h;
        }
        if (fromx > tox) {
            i = fromx;
            fromx = tox;
            tox = i;
        }
        if (fromz > toz) {
            i = fromz;
            fromz = toz;
            toz = i;
        }
        if (fromx == tox) {
            i = fromx;
            for (int j = fromz; j <= toz; ++j) {
                if (j >= cellsize * gridh) continue;
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(i + x), (int)y, (int)(j + z), (Block)blk, (int)0);
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(i + x), (int)(y + 1), (int)(j + z), (Block)blk, (int)0);
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(i + x), (int)(y + 2), (int)(j + z), (Block)blk, (int)0);
            }
        } else {
            int j = fromz;
            for (i = fromx; i <= tox; ++i) {
                if (i >= cellsize * gridw) continue;
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(i + x), (int)y, (int)(j + z), (Block)blk, (int)0);
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(i + x), (int)(y + 1), (int)(j + z), (Block)blk, (int)0);
                OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(i + x), (int)(y + 2), (int)(j + z), (Block)blk, (int)0);
            }
        }
    }

    private int findInNbr(Point p, int[][] cells, Vector inlist) {
        int d = this.rnd(4) - 1;
        for (int k = 0; k < 4; ++k) {
            switch (d) {
                case 0: {
                    if ((cells[p.x][p.y] & 0x10) != 0 || inlist.indexOf(new Point(p.x, p.y - 1)) < 0) break;
                    return 1;
                }
                case 1: {
                    if ((cells[p.x][p.y] & 0x20) != 0 || inlist.indexOf(new Point(p.x + 1, p.y)) < 0) break;
                    return 2;
                }
                case 2: {
                    if ((cells[p.x][p.y] & 0x40) != 0 || inlist.indexOf(new Point(p.x, p.y + 1)) < 0) break;
                    return 4;
                }
                case 3: {
                    if ((cells[p.x][p.y] & 0x80) != 0 || inlist.indexOf(new Point(p.x - 1, p.y)) < 0) break;
                    return 8;
                }
            }
            d = (d + 1) % 4;
        }
        return 0;
    }

    private void moveNbrs(Point p, int[][] cells, Vector outlist, Vector frontlist) {
        Point s;
        if ((cells[p.x][p.y] & 0x10) == 0) {
            s = new Point(p.x, p.y - 1);
            this.movePoint(s, outlist, frontlist);
        }
        if ((cells[p.x][p.y] & 0x20) == 0) {
            s = new Point(p.x + 1, p.y);
            this.movePoint(s, outlist, frontlist);
        }
        if ((cells[p.x][p.y] & 0x40) == 0) {
            s = new Point(p.x, p.y + 1);
            this.movePoint(s, outlist, frontlist);
        }
        if ((cells[p.x][p.y] & 0x80) == 0) {
            s = new Point(p.x - 1, p.y);
            this.movePoint(s, outlist, frontlist);
        }
    }

    private void movePoint(Point p, Vector v, Vector w) {
        int i = v.indexOf(p);
        if (i >= 0) {
            v.removeElementAt(i);
            w.addElement(p);
        }
    }

    private void removeWall(Point p, int d, int[][] cells) {
        int[] nArray = cells[p.x];
        int n = p.y;
        nArray[n] = nArray[n] ^ d;
        switch (d) {
            case 1: {
                int[] nArray2 = cells[p.x];
                int n2 = p.y - 1;
                nArray2[n2] = nArray2[n2] ^ 4;
                break;
            }
            case 2: {
                int[] nArray3 = cells[p.x + 1];
                int n3 = p.y;
                nArray3[n3] = nArray3[n3] ^ 8;
                break;
            }
            case 4: {
                int[] nArray4 = cells[p.x];
                int n4 = p.y + 1;
                nArray4[n4] = nArray4[n4] ^ 1;
                break;
            }
            case 8: {
                int[] nArray5 = cells[p.x - 1];
                int n5 = p.y;
                nArray5[n5] = nArray5[n5] ^ 2;
            }
        }
    }

    private int rnd(int n) {
        return (int)(Math.random() * (double)n + 1.0);
    }

    private Object rndElement(Vector v) {
        int i = this.rnd(v.size()) - 1;
        Object s = v.elementAt(i);
        v.removeElementAt(i);
        return s;
    }

    private void clearArea(World world, int x, int y, int z, Chunk chunk) {
        int k;
        int j;
        int i;
        for (i = 0; i < 60; ++i) {
            int hi = 5;
            if (i >= 30) {
                hi = 7;
            }
            for (j = 0; j < hi; ++j) {
                for (k = 0; k < 30; ++k) {
                    OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(x + i), (int)(y + j), (int)(z + k), (Block)Blocks.field_150350_a, (int)0);
                }
            }
        }
        for (i = 0; i < 5; ++i) {
            for (j = 0; j < 6; ++j) {
                for (k = 0; k < 30; ++k) {
                    OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(x - i), (int)(y + j), (int)(z + k), (Block)Blocks.field_150350_a, (int)0);
                }
            }
        }
    }
}

