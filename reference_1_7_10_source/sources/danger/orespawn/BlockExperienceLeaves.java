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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockExperienceLeaves
extends BlockLeaves {
    private IIcon generic_solid = null;

    protected BlockExperienceLeaves(int par1) {
    }

    public void func_149666_a(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(Item.func_150898_a((Block)this), 1, 0));
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        if (!par1World.field_72995_K) {
            // empty if block
        }
    }

    public int func_149745_a(Random par1Random) {
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
                        if ((t %= 24000L) < 14000L || t > 22000L) {
                            return;
                        }
                        if (par1World.field_73012_v.nextInt(65) == 1 && (bid = par1World.func_147439_a(par2, par3 + 1, par4)) == Blocks.field_150350_a) {
                            this.func_149642_a(par1World, par2, par3 + 2, par4, new ItemStack(Items.field_151062_by));
                        }
                        if (par1World.field_73012_v.nextInt(75) == 1 && (bid = par1World.func_147439_a(par2, par3 - 1, par4)) == Blocks.field_150350_a) {
                            EntityExpBottle var2 = new EntityExpBottle(par1World, (double)par2, (double)(par3 - 1), (double)par4);
                            var2.func_70012_b((double)par2, (double)(par3 - 1), (double)par4, 0.0f, 0.0f);
                            var2.func_70186_c((double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 2.0f), (double)-0.1f, (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 2.0f), 0.4f, 5.0f);
                            par1World.func_72838_d((Entity)var2);
                        }
                        return;
                    }
                }
            }
            this.removeLeaves(par1World, par2, par3, par4);
        }
    }

    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        int i;
        Block bid;
        long t = par1World.func_72820_D();
        if ((t %= 24000L) < 13000L || t > 23000L) {
            return;
        }
        int rate = 0;
        if (t < 14000L) {
            rate = (14000 - (int)t) / 2;
        }
        if (t > 22000L) {
            rate = (int)(t - 22000L) / 2;
        }
        if (par1World.field_73012_v.nextInt(200 + rate) == 1 && (bid = par1World.func_147439_a(par2, par3 + 1, par4)) == Blocks.field_150350_a) {
            for (i = 0; i < 10; ++i) {
                par1World.func_72869_a("fireworksSpark", (double)par2, (double)par3 + 1.25, (double)par4, par1World.field_73012_v.nextGaussian(), Math.abs(par1World.field_73012_v.nextGaussian()), par1World.field_73012_v.nextGaussian());
            }
        }
        if (par1World.field_73012_v.nextInt(40 + rate) == 1 && (bid = par1World.func_147439_a(par2, par3 - 1, par4)) == Blocks.field_150350_a) {
            for (i = 0; i < 4; ++i) {
                par1World.func_72869_a("fireworksSpark", (double)par2, (double)par3 - 1.25, (double)par4, (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()), (double)(-Math.abs(par1World.field_73012_v.nextFloat())), (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()));
            }
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

