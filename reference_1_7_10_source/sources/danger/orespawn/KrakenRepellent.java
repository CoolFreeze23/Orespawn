/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.EntityAnt;
import danger.orespawn.Kraken;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class KrakenRepellent
extends BlockTorch {
    public KrakenRepellent(int par1) {
        this.func_149647_a(CreativeTabs.field_78028_d);
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        int var6 = par1World.func_72805_g(par2, par3, par4);
        double var7 = (float)par2 + 0.5f;
        double var9 = (float)par3 + 0.7f;
        double var11 = (float)par4 + 0.5f;
        double var13 = 0.413;
        double var15 = 0.271;
        if (var6 == 1) {
            par1World.func_72869_a("smoke", var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0);
        } else if (var6 == 2) {
            par1World.func_72869_a("smoke", var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0);
        } else if (var6 == 3) {
            par1World.func_72869_a("smoke", var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0);
        } else if (var6 == 4) {
            par1World.func_72869_a("smoke", var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0);
        } else {
            par1World.func_72869_a("smoke", var7, var9 + 0.21, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7, var9 + 0.21, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7, var9 + 0.21, var11, 0.0, 0.0, 0.0);
        }
    }

    public int func_149738_a(World par1World) {
        return 10;
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (!par1World.field_72995_K) {
            this.findSomethingToRepell(par1World, par2, par3, par4);
            par1World.func_147464_a(par2, par3, par4, (Block)this, this.func_149738_a(par1World));
        }
    }

    public void func_149726_b(World world, int x, int y, int z) {
        world.func_147464_a(x, y, z, (Block)this, this.func_149738_a(world));
    }

    public void func_149695_a(World world, int x, int y, int z, Block blockId) {
        world.func_147464_a(x, y, z, (Block)this, this.func_149738_a(world));
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        return super.func_149742_c(par1World, par2, par3, par4);
    }

    private void findSomethingToRepell(World par1World, int par2, int par3, int par4) {
        AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)((double)par2 - 20.0), (double)((double)par3 - 10.0), (double)((double)par4 - 20.0), (double)((double)par2 + 20.0), (double)((double)par3 + 40.0), (double)((double)par4 + 20.0));
        List var5 = par1World.func_72872_a(EntityLivingBase.class, bb);
        Iterator var2 = var5.iterator();
        EntityLivingBase var3 = null;
        while (var2.hasNext()) {
            double d;
            double f;
            double d3;
            double d2;
            double d1;
            var3 = (EntityLivingBase)var2.next();
            if (var3 != null && var3 instanceof Kraken) {
                d1 = var3.field_70165_t - (double)par2;
                d2 = var3.field_70163_u - 15.0 - (double)par3;
                d3 = var3.field_70161_v - (double)par4;
                f = d1 * d1 + d2 * d2 + d3 * d3;
                f = Math.sqrt(f);
                if ((f = 20.0 - f) > 20.0) {
                    f = 20.0;
                }
                if (f < 0.0) {
                    f = 0.0;
                }
                d = (float)Math.atan2(var3.field_70165_t - (double)par2, var3.field_70161_v - (double)par4);
                var3.field_70159_w += (f *= 0.4) * Math.sin(d);
                var3.field_70179_y += f * Math.cos(d);
            }
            if (var3 == null || !(var3 instanceof EntityAnt)) continue;
            d1 = var3.field_70165_t - (double)par2;
            d2 = var3.field_70163_u - (double)par3;
            d3 = var3.field_70161_v - (double)par4;
            f = d1 * d1 + d2 * d2 + d3 * d3;
            f = Math.sqrt(f);
            if ((f = 20.0 - f) > 20.0) {
                f = 20.0;
            }
            if (f < 0.0) {
                f = 0.0;
            }
            d = (float)Math.atan2(var3.field_70165_t - (double)par2, var3.field_70161_v - (double)par4);
            var3.field_70159_w += (f *= 0.4) * Math.sin(d);
            var3.field_70179_y += f * Math.cos(d);
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

