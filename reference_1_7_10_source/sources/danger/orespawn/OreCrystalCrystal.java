/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class OreCrystalCrystal
extends Block {
    public OreCrystalCrystal(int par1, float lv, float f1, float f2) {
        super(Material.field_151576_e);
        this.func_149711_c(f1);
        this.func_149752_b(f2);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149715_a(lv);
        this.func_149675_a(true);
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_73012_v.nextInt(20) == 0) {
            this.sparkle(par1World, par2, par3, par4);
        }
    }

    private void sparkle(World par1World, int par2, int par3, int par4) {
        boolean which = false;
        float dx = 0.5f;
        float dz = 0.5f;
        float dy = 0.5f;
        if (this == OreSpawnMain.TigersEye) {
            par1World.func_72869_a("flame", (double)((float)par2 + dx), (double)par3 + (double)dy, (double)((float)par4 + dz), (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 4.0f), (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 4.0f), (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 4.0f));
        } else {
            par1World.func_72869_a("fireworksSpark", (double)((float)par2 + dx), (double)par3 + (double)dy, (double)((float)par4 + dz), (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 4.0f), (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 4.0f), (double)((par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) / 4.0f));
        }
    }

    public int func_149645_b() {
        return 1;
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return false;
    }

    public void func_149664_b(World par1World, int par2, int par3, int par4, int par5) {
        if (this == OreSpawnMain.CrystalCrystal && !par1World.field_72995_K && par1World.field_73012_v.nextInt(10) == 1) {
            par1World.func_72885_a((Entity)null, (double)((float)par2 + 0.5f), (double)((float)par3 + 0.5f), (double)((float)par4 + 0.5f), 1.0f, true, par1World.func_82736_K().func_82766_b("mobGriefing"));
        }
        super.func_149664_b(par1World, par2, par3, par4, par5);
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        super.func_149690_a(par1World, par2, par3, par4, par5, par6, par7);
        int j1 = 5 + par1World.field_73012_v.nextInt(5) + par1World.field_73012_v.nextInt(10);
        if (par3 < 40) {
            this.func_149657_c(par1World, par2, par3, par4, j1);
        }
    }

    public int func_149745_a(Random par1Random) {
        if (this != OreSpawnMain.TigersEye) {
            return 1;
        }
        return par1Random.nextInt(2);
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

