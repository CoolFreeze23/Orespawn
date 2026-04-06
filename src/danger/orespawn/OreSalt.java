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
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.EntityAnt;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class OreSalt
extends Block {
    public OreSalt(int par1) {
        super(Material.field_151576_e);
        this.func_149711_c(5.0f);
        this.func_149752_b(2.0f);
        this.func_149647_a(CreativeTabs.field_78030_b);
    }

    public void func_149670_a(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        if (par5Entity instanceof EntityAnt) {
            par5Entity.func_70097_a(DamageSource.field_76367_g, 5.0f);
        }
    }

    public void func_149724_b(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        if (par5Entity instanceof EntityAnt) {
            par5Entity.func_70097_a(DamageSource.field_76367_g, 5.0f);
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

