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
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class OreGenericEgg
extends Block {
    public OreGenericEgg(int oldid) {
        super(Material.field_151578_c);
        this.func_149711_c(0.5f);
        this.func_149752_b(1.0f);
        this.func_149672_a(Block.field_149767_g);
        this.func_149647_a(CreativeTabs.field_78030_b);
    }

    public void func_149690_a(World par1World, int par2, int par3, int par4, int par5, float par6, int par7) {
        super.func_149690_a(par1World, par2, par3, par4, par5, par6, par7);
        int j1 = 5 + par1World.field_73012_v.nextInt(3) + par1World.field_73012_v.nextInt(3);
        if (par1World.field_73012_v.nextInt(2) == 1) {
            this.func_149657_c(par1World, par2, par3, par4, j1);
        }
    }

    public boolean func_149662_c() {
        return true;
    }

    public boolean func_149686_d() {
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

