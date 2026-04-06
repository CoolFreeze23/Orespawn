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
 *  net.minecraft.init.Items
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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAppleLeaves
extends BlockLeaves {
    private IIcon generic_solid = null;

    protected BlockAppleLeaves(int par1) {
    }

    public void func_149666_a(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(Item.func_150898_a((Block)this), 1, 0));
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!par1World.field_72995_K) {
            if (par1World.field_73012_v.nextInt(25) == 1) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(Items.field_151034_e));
            }
            if (par1World.field_73012_v.nextInt(500) == 2) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(Items.field_151153_ao, 1, 0));
            }
            if (par1World.field_73012_v.nextInt(1000) == 3) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(Items.field_151153_ao, 1, 1));
            }
            if (par1World.field_73012_v.nextInt(10000) == 4) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MagicApple));
            }
        }
    }

    public int func_149745_a(Random par1Random) {
        return 1;
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        int var7 = 2;
        int totaldist = 0;
        int chance = 20;
        if (par1World.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
            chance = 100;
            var7 = 1;
        }
        if (!par1World.field_72995_K && par1World.func_72904_c(par2 - var7, par3 - var7, par4 - var7, par2 + var7, par3 + var7, par4 + var7)) {
            for (int var12 = -var7; var12 <= var7; ++var12) {
                for (int var13 = -var7; var13 <= 0; ++var13) {
                    for (int var14 = -var7; var14 <= var7; ++var14) {
                        Block bid;
                        totaldist = Math.abs(var12) + Math.abs(var13) + Math.abs(var14);
                        if (totaldist > 3 || (bid = par1World.func_147439_a(par2 + var12, par3 + var13, par4 + var14)) == Blocks.field_150350_a || !bid.canSustainLeaves((IBlockAccess)par1World, par2 + var12, par3 + var13, par4 + var14)) continue;
                        bid = par1World.func_147439_a(par2, par3 - 1, par4);
                        if (bid == Blocks.field_150350_a && par1World.field_73012_v.nextInt(chance) == 3) {
                            this.func_149690_a(par1World, par2, par3 - 1, par4, 0, 0.0f, 0);
                        }
                        long t = par1World.func_72820_D();
                        if ((t %= 24000L) > 12000L && par1World.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
                            OreSpawnMain.setBlockFast(par1World, par2, par3, par4, OreSpawnMain.MyScaryLeaves, 0, 3);
                        }
                        return;
                    }
                }
            }
            this.removeLeaves(par1World, par2, par3, par4);
        }
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
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        if (OreSpawnMain.FastGraphicsLeaves != 0) {
            return this.generic_solid;
        }
        return this.field_149761_L;
    }

    public String[] func_150125_e() {
        return null;
    }
}

