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
import net.minecraft.world.World;

public class BlockTitanium
extends Block {
    public BlockTitanium(int par1) {
        super(Material.field_151576_e);
        this.func_149711_c(5.0f);
        this.func_149752_b(5.0f);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149715_a(0.5f);
    }

    public int tickRate() {
        return 100;
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
            int which = par1World.field_73012_v.nextInt(3);
            if (which == 0) {
                par1World.func_72869_a("flame", var9, var11, var13, 0.0, 0.0, 0.0);
            }
            if (which == 1) {
                par1World.func_72869_a("smoke", var9, var11, var13, 0.0, 0.0, 0.0);
            }
            if (which != 2) continue;
            par1World.func_72869_a("reddust", var9, var11, var13, 0.0, 0.0, 0.0);
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

