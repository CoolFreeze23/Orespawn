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
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class MoleDirtBlock
extends Block {
    public MoleDirtBlock(int i) {
        super(Material.field_151578_c);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149675_a(true);
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_72995_K) {
            return;
        }
        par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
    }

    public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
        float f = 0.125f;
        return AxisAlignedBB.func_72330_a((double)par2, (double)par3, (double)par4, (double)(par2 + 1), (double)((float)(par3 + 1) - f), (double)(par4 + 1));
    }

    public void func_149670_a(World par1World, int par2, int par3, int par4, Entity par5Entity) {
        if (par5Entity != null) {
            par5Entity.field_70159_w *= 0.3;
            par5Entity.field_70179_y *= 0.3;
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

