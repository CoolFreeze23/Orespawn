/*
 * Decompiled with CFR 0.152.
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class OreTitanium
extends Block {
    private boolean glowing = false;
    private int glowcount = 0;

    public OreTitanium(int par1) {
        super(Material.field_151576_e);
        this.func_149711_c(15.0f);
        this.func_149752_b(5.0f);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149675_a(true);
        this.glowing = false;
    }

    public int tickRate() {
        return 30;
    }

    public void func_149699_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
        this.glow(par1World, par2, par3, par4);
        super.func_149699_a(par1World, par2, par3, par4, par5EntityPlayer);
    }

    public void func_149724_b(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        this.glow(par1World, par2, par3, par4);
        super.func_149724_b(par1World, par2, par3, par4, par5Entity);
    }

    public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        this.glow(par1World, par2, par3, par4);
        return super.func_149727_a(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
    }

    private void glow(World par1World, int par2, int par3, int par4) {
        this.glowing = true;
        this.glowcount = 5;
        this.sparkle(par1World, par2, par3, par4);
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (this.glowing) {
            this.sparkle(par1World, par2, par3, par4);
            if (this.glowcount > 0) {
                --this.glowcount;
            } else {
                this.glowing = false;
            }
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
            par1World.func_72869_a("reddust", var9, var11, var13, 0.0, 0.0, 0.0);
        }
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        super.func_149690_a(par1World, par2, par3, par4, par5, par6, par7);
        int j1 = 5 + par1World.field_73012_v.nextInt(5) + par1World.field_73012_v.nextInt(10);
        if (par3 < 40) {
            this.func_149657_c(par1World, par2, par3, par4, j1);
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

