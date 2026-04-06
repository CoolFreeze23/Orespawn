/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockRotatedPillar
 *  net.minecraft.block.material.Material
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.IIcon
 *  net.minecraft.world.IBlockAccess
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockCrystalTreeLog
extends BlockRotatedPillar {
    @SideOnly(value=Side.CLIENT)
    private IIcon field_111052_c;
    @SideOnly(value=Side.CLIENT)
    private IIcon tree_top;

    protected BlockCrystalTreeLog(int par1, int par2) {
        super(Material.field_151575_d);
        this.func_149647_a(CreativeTabs.field_78030_b);
    }

    protected ItemStack func_149644_j(int par1) {
        return new ItemStack(Item.func_150898_a((Block)this), 1, 0);
    }

    public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    public boolean isWood(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return false;
    }

    public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return Item.func_150898_a((Block)OreSpawnMain.MyCrystalTreeLog);
    }

    @SideOnly(value=Side.CLIENT)
    protected IIcon func_150163_b(int par1) {
        return this.field_111052_c;
    }

    @SideOnly(value=Side.CLIENT)
    protected IIcon func_150161_d(int par1) {
        return this.tree_top;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_111052_c = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
        this.tree_top = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_top");
    }
}

