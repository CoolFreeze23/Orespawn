/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class BlockRuby
extends Block {
    public BlockRuby(int par1) {
        super(Material.field_151576_e);
        this.func_149711_c(4.0f);
        this.func_149752_b(4.0f);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149715_a(0.4f);
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return true;
    }

    public void func_149670_a(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        if (this == OreSpawnMain.MyBlockMobzillaScaleBlock && par5Entity instanceof EntityLivingBase) {
            ((EntityLivingBase)par5Entity).func_70690_d(new PotionEffect(Potion.field_76420_g.field_76415_H, 200, 0));
        }
    }

    public void func_149724_b(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        if (this == OreSpawnMain.MyBlockMobzillaScaleBlock && par5Entity instanceof EntityLivingBase) {
            ((EntityLivingBase)par5Entity).func_70690_d(new PotionEffect(Potion.field_76420_g.field_76415_H, 200, 0));
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

