/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockLeaves
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.IIcon
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockScaryLeaves
extends BlockLeaves {
    private IIcon scary_solid = null;
    private IIcon generic_solid = null;

    protected BlockScaryLeaves(int par1) {
    }

    public void func_149666_a(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(Item.func_150898_a((Block)this), 1, 0));
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!par1World.field_72995_K && par1World.field_73012_v.nextInt(25) == 1) {
            if (this == OreSpawnMain.MyCherryLeaves) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MyCherry));
            }
            if (this == OreSpawnMain.MyPeachLeaves) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MyPeach));
            }
        }
    }

    public int func_149745_a(Random par1Random) {
        if (this == OreSpawnMain.MyCherryLeaves) {
            return par1Random.nextInt(4);
        }
        if (this == OreSpawnMain.MyPeachLeaves) {
            return par1Random.nextInt(1);
        }
        return 0;
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        int var7 = 2;
        int totaldist = 0;
        if (!par1World.field_72995_K && par1World.func_72904_c(par2 - var7, par3 - var7, par4 - var7, par2 + var7, par3 + var7, par4 + var7)) {
            for (int var12 = -var7; var12 <= var7; ++var12) {
                for (int var13 = -var7; var13 <= 0; ++var13) {
                    for (int var14 = -var7; var14 <= var7; ++var14) {
                        Block bid;
                        totaldist = Math.abs(var12) + Math.abs(var13) + Math.abs(var14);
                        if (totaldist > 3 || (bid = par1World.func_147439_a(par2 + var12, par3 + var13, par4 + var14)) == null || !bid.canSustainLeaves((IBlockAccess)par1World, par2 + var12, par3 + var13, par4 + var14)) continue;
                        long t = par1World.func_72820_D();
                        if (this == OreSpawnMain.MyScaryLeaves && (t %= 24000L) < 12000L) {
                            OreSpawnMain.setBlockFast(par1World, par2, par3, par4, OreSpawnMain.MyAppleLeaves, 0, 3);
                        }
                        if ((bid = par1World.func_147439_a(par2, par3 - 1, par4)) == Blocks.field_150350_a && par1World.field_73012_v.nextInt(20) == 3) {
                            this.func_149690_a(par1World, par2, par3 - 1, par4, 0, 0.0f, 0);
                        }
                        return;
                    }
                }
            }
            this.removeLeaves(par1World, par2, par3, par4);
        }
    }

    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
    }

    private void removeLeaves(World par1World, int par2, int par3, int par4) {
        this.func_149697_b(par1World, par2, par3, par4, 0, 0);
        par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
    }

    public boolean func_149662_c() {
        return OreSpawnMain.FastGraphicsLeaves != 0;
    }

    @SideOnly(value=Side.CLIENT)
    public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        Block i1 = par1IBlockAccess.func_147439_a(par2, par3, par4);
        return OreSpawnMain.FastGraphicsLeaves == 0 || i1 != this;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
        this.generic_solid = iconRegister.func_94245_a("OreSpawn:generic_solid");
        this.scary_solid = iconRegister.func_94245_a("OreSpawn:scary_solid");
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        if (OreSpawnMain.FastGraphicsLeaves != 0) {
            if (this == OreSpawnMain.MyCherryLeaves) {
                return this.generic_solid;
            }
            if (this == OreSpawnMain.MyPeachLeaves) {
                return this.generic_solid;
            }
            return this.scary_solid;
        }
        return this.field_149761_L;
    }

    public String[] func_150125_e() {
        return null;
    }
}

