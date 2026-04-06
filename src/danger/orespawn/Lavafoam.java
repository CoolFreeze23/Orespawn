/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Lavafoam
extends Block {
    public Lavafoam(int par1) {
        super(Material.field_151576_e);
        this.func_149711_c(5.0f);
        this.func_149752_b(5.0f);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149675_a(true);
        this.field_149765_K = 1.1f;
    }

    public int tickRate() {
        return 10;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_73012_v.nextInt(20) == 0) {
            this.sparkle(par1World, par2, par3, par4);
        }
    }

    private void sparkle(World par1World, int par2, int par3, int par4) {
        Random var5 = par1World.field_73012_v;
        double var6 = 0.0625;
        for (int var8 = 0; var8 < 6; ++var8) {
            double var9 = (float)par2 + var5.nextFloat();
            double var11 = (float)par3 + var5.nextFloat();
            double var13 = (float)par4 + var5.nextFloat();
            if (var8 == 0 && !par1World.func_147439_a(par2, par3 + 1, par4).func_149662_c()) {
                var11 = (double)(par3 + 1) + var6;
            }
            if (var8 == 1 && !par1World.func_147439_a(par2, par3 - 1, par4).func_149662_c()) {
                var11 = (double)(par3 + 0) - var6;
            }
            if (var8 == 2 && !par1World.func_147439_a(par2, par3, par4 + 1).func_149662_c()) {
                var13 = (double)(par4 + 1) + var6;
            }
            if (var8 == 3 && !par1World.func_147439_a(par2, par3, par4 - 1).func_149662_c()) {
                var13 = (double)(par4 + 0) - var6;
            }
            if (var8 == 4 && !par1World.func_147439_a(par2 + 1, par3, par4).func_149662_c()) {
                var9 = (double)(par2 + 1) + var6;
            }
            if (var8 == 5 && !par1World.func_147439_a(par2 - 1, par3, par4).func_149662_c()) {
                var9 = (double)(par2 + 0) - var6;
            }
            if (!(var9 < (double)par2 || var9 > (double)(par2 + 1) || var11 < 0.0 || var11 > (double)(par3 + 1) || var13 < (double)par4) && !(var13 > (double)(par4 + 1))) continue;
            int which = par1World.field_73012_v.nextInt(10);
            if (which == 1) {
                par1World.func_72869_a("smoke", var9, var11, var13, 0.0, 0.0, 0.0);
            }
            if (which != 2) continue;
            par1World.func_72869_a("reddust", var9, var11, var13, 0.0, 0.0, 0.0);
        }
    }

    public void func_149670_a(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        double pi = 3.14159;
        double pi2 = pi / 2.0;
        double pi4 = pi / 4.0;
        super.func_149670_a(par1World, par2, par3, par4, par5Entity);
        if (par5Entity == null) {
            return;
        }
        if (!(par5Entity instanceof EntityLivingBase)) {
            return;
        }
        double d = Math.atan2(par5Entity.field_70165_t - (double)((float)par2 + 0.5f), par5Entity.field_70161_v - (double)((float)par4 + 0.5f));
        if (d < 0.0) {
            d = pi * 2.0 + d;
        }
        if (d > pi2 - pi4 && d < pi2 + pi4) {
            par5Entity.field_70159_w = 0.45f;
            par5Entity.field_70179_y *= (double)1.35f;
        } else if (d > pi - pi4 && d < pi + pi4) {
            par5Entity.field_70179_y = -0.45f;
            par5Entity.field_70159_w *= (double)1.35f;
        } else if (d > pi + pi2 - pi4 && d < pi + pi2 + pi4) {
            par5Entity.field_70159_w = -0.45f;
            par5Entity.field_70179_y *= (double)1.35f;
        } else {
            par5Entity.field_70179_y = 0.45f;
            par5Entity.field_70159_w *= (double)1.35f;
        }
        d = Math.sqrt(par5Entity.field_70179_y * par5Entity.field_70179_y + par5Entity.field_70159_w * par5Entity.field_70159_w);
        if (d > 1.0) {
            par5Entity.func_70097_a(DamageSource.field_76379_h, (float)d);
        }
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        super.func_149690_a(par1World, par2, par3, par4, par5, par6, par7);
        int j1 = 5 + par1World.field_73012_v.nextInt(5) + par1World.field_73012_v.nextInt(5);
        if (par1World.field_73011_w.field_76574_g == -1) {
            this.func_149657_c(par1World, par2, par3, par4, j1);
        }
    }

    public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
        float f = 0.0125f;
        return AxisAlignedBB.func_72330_a((double)((float)par2 + f), (double)par3, (double)((float)par4 + f), (double)((float)(par2 + 1) - f), (double)(par3 + 1), (double)((float)(par4 + 1) - f));
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

