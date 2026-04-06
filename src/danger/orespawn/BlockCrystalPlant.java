/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockReed
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockReed;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class BlockCrystalPlant
extends BlockReed {
    protected BlockCrystalPlant(int par1) {
        float var3 = 0.375f;
        this.func_149676_a(0.5f - var3, 0.0f, 0.5f - var3, 0.5f + var3, 1.0f, 0.5f + var3);
        this.func_149675_a(true);
        this.func_149647_a(CreativeTabs.field_78031_c);
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        Block bid = par1World.func_147439_a(par2, par3 - 1, par4);
        if (bid == Blocks.field_150350_a) {
            return false;
        }
        return bid == Blocks.field_150349_c || bid == Blocks.field_150346_d || bid == Blocks.field_150458_ak || bid == OreSpawnMain.CrystalGrass;
    }

    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_73012_v.nextInt(30) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 10; ++j1) {
            par1World.func_72869_a("happyVillager", (double)((float)par2 + par1World.field_73012_v.nextFloat()), (double)par3 + (double)par1World.field_73012_v.nextFloat(), (double)((float)par4 + par1World.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
        }
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_72995_K) {
            return;
        }
        if (par1World.field_73012_v.nextInt(5) != 1) {
            return;
        }
        par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
        if (this == OreSpawnMain.MyCrystalPlant) {
            this.TallCrystalTree(par1World, par2, par3, par4);
        }
        if (this == OreSpawnMain.MyCrystalPlant2) {
            this.ScragglyCrystalTreeWithBranches(par1World, par2, par3, par4);
        }
        if (this == OreSpawnMain.MyCrystalPlant3) {
            this.TallCrystalTreeBlue(par1World, par2, par3, par4);
        }
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        if (this == OreSpawnMain.MyCrystalPlant) {
            return Item.func_150898_a((Block)OreSpawnMain.MyCrystalPlant);
        }
        if (this == OreSpawnMain.MyCrystalPlant2) {
            Item.func_150898_a((Block)OreSpawnMain.MyCrystalPlant2);
        }
        return Item.func_150898_a((Block)OreSpawnMain.MyCrystalPlant3);
    }

    public int func_149745_a(Random par1Random) {
        return 1;
    }

    public int idPicked(World par1World, int par2, int par3, int par4) {
        return 0;
    }

    protected int getSeedItem() {
        return 0;
    }

    protected int getCropItem() {
        return 0;
    }

    public void TallCrystalTree(World world, int x, int y, int z) {
        int n;
        int m;
        Block bid;
        int k;
        int i = 10 + world.field_73012_v.nextInt(12);
        int j = i + world.field_73012_v.nextInt(18);
        for (k = 0; k < i; ++k) {
            bid = world.func_147439_a(x, y + k, z);
            if (k >= 1 && bid != Blocks.field_150350_a && bid != OreSpawnMain.MyCrystalTreeLog && bid != OreSpawnMain.MyCrystalLeaves) {
                return;
            }
            OreSpawnMain.setBlockFast(world, x, y + k, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
        }
        y += i - 1;
        for (k = i; k < j && ((bid = world.func_147439_a(x, ++y, z)) == Blocks.field_150350_a || bid == OreSpawnMain.MyCrystalTreeLog || bid == OreSpawnMain.MyCrystalLeaves); ++k) {
            OreSpawnMain.setBlockFast(world, x, y, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
            if (k % 4 != 0) continue;
            for (m = -1; m < 2; ++m) {
                for (n = -1; n < 2; ++n) {
                    if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x + m, y, z + n)) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves, 0, 2);
                }
            }
        }
        ++y;
        for (m = -1; m < 2; ++m) {
            for (n = -1; n < 2; ++n) {
                if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x + m, y, z + n)) != Blocks.field_150350_a) continue;
                OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalTreeLog, 0, 2);
            }
        }
        for (m = -3; m < 4; ++m) {
            for (n = -3; n < 4; ++n) {
                bid = world.func_147439_a(x + m, y, z + n);
                if (bid != Blocks.field_150350_a) continue;
                OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves, 0, 2);
            }
        }
        ++y;
        for (m = -1; m < 2; ++m) {
            for (n = -1; n < 2; ++n) {
                bid = world.func_147439_a(x + m, y, z + n);
                if (bid != Blocks.field_150350_a) continue;
                OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves, 0, 2);
            }
        }
    }

    public void makeScragglyCrystalBranch(World world, int x, int y, int z, int len, int biasx, int biasz) {
        for (int k = 0; k < len; ++k) {
            int iy;
            Block bid;
            int ix = world.field_73012_v.nextInt(2) - world.field_73012_v.nextInt(2) + biasx;
            int iz = world.field_73012_v.nextInt(2) - world.field_73012_v.nextInt(2) + biasz;
            if (ix > 1) {
                ix = 1;
            }
            if (ix < -1) {
                ix = -1;
            }
            if (iz > 1) {
                iz = 1;
            }
            if (iz < -1) {
                iz = -1;
            }
            if ((bid = world.func_147439_a(x += ix, y += (iy = world.field_73012_v.nextInt(3) > 0 ? 1 : 0), z += iz)) != Blocks.field_150350_a && bid != OreSpawnMain.MyCrystalTreeLog && bid != OreSpawnMain.MyCrystalLeaves2) {
                return;
            }
            OreSpawnMain.setBlockFast(world, x, y, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x + m, y, z + n)) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves2, 0, 2);
                }
            }
            if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x, y + 1, z)) != Blocks.field_150350_a) continue;
            OreSpawnMain.setBlockFast(world, x, y + 1, z, OreSpawnMain.MyCrystalLeaves2, 0, 2);
        }
    }

    public void ScragglyCrystalTreeWithBranches(World world, int x, int y, int z) {
        Block bid;
        int k;
        int i = 1 + world.field_73012_v.nextInt(2);
        int j = i + world.field_73012_v.nextInt(8);
        for (k = 0; k < i; ++k) {
            bid = world.func_147439_a(x, y + k, z);
            if (k >= 1 && bid != Blocks.field_150350_a && bid != OreSpawnMain.MyCrystalTreeLog && bid != OreSpawnMain.MyCrystalLeaves2) {
                return;
            }
            OreSpawnMain.setBlockFast(world, x, y + k, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
        }
        y += i - 1;
        for (k = i; k < j; ++k) {
            int ix = world.field_73012_v.nextInt(2) - world.field_73012_v.nextInt(2);
            int iz = world.field_73012_v.nextInt(2) - world.field_73012_v.nextInt(2);
            int iy = world.field_73012_v.nextInt(4) > 0 ? 1 : 0;
            bid = world.func_147439_a(x += ix, y += iy, z += iz);
            if (bid != Blocks.field_150350_a && bid != OreSpawnMain.MyCrystalTreeLog && bid != OreSpawnMain.MyCrystalLeaves2) break;
            OreSpawnMain.setBlockFast(world, x, y, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
            if (world.field_73012_v.nextInt(4) == 1) {
                this.makeScragglyCrystalBranch(world, x, y, z, world.field_73012_v.nextInt(1 + j - k), world.field_73012_v.nextInt(2) - world.field_73012_v.nextInt(2), world.field_73012_v.nextInt(2) - world.field_73012_v.nextInt(2));
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x + m, y, z + n)) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves2, 0, 2);
                }
            }
            if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x, y + 1, z)) != Blocks.field_150350_a) continue;
            OreSpawnMain.setBlockFast(world, x, y + 1, z, OreSpawnMain.MyCrystalLeaves2, 0, 2);
        }
    }

    public void TallCrystalTreeBlue(World world, int x, int y, int z) {
        int n;
        int m;
        Block bid;
        int k;
        int i = 5 + world.field_73012_v.nextInt(6);
        int j = 2 + i + world.field_73012_v.nextInt(12);
        for (k = 0; k < i; ++k) {
            bid = world.func_147439_a(x, y + k, z);
            if (k >= 1 && bid != Blocks.field_150350_a && bid != OreSpawnMain.MyCrystalTreeLog && bid != OreSpawnMain.MyCrystalLeaves3) {
                return;
            }
            OreSpawnMain.setBlockFast(world, x, y + k, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
        }
        y += i - 1;
        for (k = i; k < j && ((bid = world.func_147439_a(x, ++y, z)) == Blocks.field_150350_a || bid == OreSpawnMain.MyCrystalTreeLog || bid == OreSpawnMain.MyCrystalLeaves3); ++k) {
            OreSpawnMain.setBlockFast(world, x, y, z, OreSpawnMain.MyCrystalTreeLog, 0, 2);
            if (k % 3 != 0) continue;
            for (m = -1; m < 2; ++m) {
                for (n = -1; n < 2; ++n) {
                    if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x + m, y, z + n)) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves3, 0, 2);
                }
            }
        }
        ++y;
        for (m = -1; m < 2; ++m) {
            for (n = -1; n < 2; ++n) {
                if (world.field_73012_v.nextInt(2) != 1 || (bid = world.func_147439_a(x + m, y, z + n)) != Blocks.field_150350_a) continue;
                OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalTreeLog, 0, 2);
            }
        }
        for (m = -3; m < 4; ++m) {
            for (n = -3; n < 4; ++n) {
                bid = world.func_147439_a(x + m, y, z + n);
                if (bid != Blocks.field_150350_a) continue;
                OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves3, 0, 2);
            }
        }
        ++y;
        for (m = -1; m < 2; ++m) {
            for (n = -1; n < 2; ++n) {
                bid = world.func_147439_a(x + m, y, z + n);
                if (bid != Blocks.field_150350_a) continue;
                OreSpawnMain.setBlockFast(world, x + m, y, z + n, OreSpawnMain.MyCrystalLeaves3, 0, 2);
            }
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

