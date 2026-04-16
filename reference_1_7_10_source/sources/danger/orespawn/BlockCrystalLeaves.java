/*
 * Decompiled with CFR 0.152.
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

public class BlockCrystalLeaves
extends BlockLeaves {
    protected BlockCrystalLeaves(int par1) {
        this.func_149675_a(true);
        this.func_149647_a(CreativeTabs.field_78031_c);
    }

    public void func_149666_a(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(Item.func_150898_a((Block)this), 1, 0));
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!par1World.field_72995_K) {
            if (par1World.field_73012_v.nextInt(100) == 1) {
                this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MyCrystalApple));
            }
            if (par1World.field_73012_v.nextInt(50) == 1) {
                if (this == OreSpawnMain.MyCrystalLeaves) {
                    this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MyCrystalPlant));
                }
                if (this == OreSpawnMain.MyCrystalLeaves2) {
                    this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MyCrystalPlant2));
                }
                if (this == OreSpawnMain.MyCrystalLeaves3) {
                    this.func_149642_a(par1World, par2, par3, par4, new ItemStack(OreSpawnMain.MyCrystalPlant3));
                }
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
                        if (totaldist > 3 || (bid = par1World.func_147439_a(par2 + var12, par3 + var13, par4 + var14)) == null || !bid.canSustainLeaves((IBlockAccess)par1World, par2 + var12, par3 + var13, par4 + var14)) continue;
                        bid = par1World.func_147439_a(par2, par3 - 1, par4);
                        if (bid == Blocks.field_150350_a && par1World.field_73012_v.nextInt(chance) == 3) {
                            this.func_149690_a(par1World, par2, par3 - 1, par4, 0, 0.0f, 0);
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

    public boolean func_149686_d() {
        return OreSpawnMain.FastGraphicsLeaves != 0;
    }

    public int func_149645_b() {
        if (OreSpawnMain.FastGraphicsLeaves != 0) {
            return super.func_149645_b();
        }
        return 1;
    }

    @SideOnly(value=Side.CLIENT)
    public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        Block i1 = par1IBlockAccess.func_147439_a(par2, par3, par4);
        return OreSpawnMain.FastGraphicsLeaves == 0 || i1 != this;
    }

    @SideOnly(value=Side.CLIENT)
    public int func_149635_D() {
        return 0xDDDDDD;
    }

    @SideOnly(value=Side.CLIENT)
    public int func_149741_i(int par1) {
        return 0xDDDDDD;
    }

    @SideOnly(value=Side.CLIENT)
    public int func_149720_d(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
        return 0xDDDDDD;
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        return this.field_149761_L;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }

    public String[] func_150125_e() {
        return null;
    }
}

