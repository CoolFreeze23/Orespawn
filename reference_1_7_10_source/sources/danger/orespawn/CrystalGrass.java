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
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class CrystalGrass
extends Block {
    @SideOnly(value=Side.CLIENT)
    private IIcon[] field_94364_a;

    protected CrystalGrass(int par1, float f1, float f2) {
        super(Material.field_151577_b);
        this.func_149711_c(f1);
        this.func_149752_b(f2);
        this.func_149675_a(false);
        this.func_149647_a(CreativeTabs.field_78030_b);
    }

    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        if (this.field_94364_a == null) {
            return null;
        }
        return par1 == 1 ? this.field_94364_a[0] : (par1 == 0 ? this.field_94364_a[1] : this.field_94364_a[2]);
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon getBlockTexture(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        if (this.field_94364_a == null) {
            return null;
        }
        if (par5 == 1) {
            return this.field_94364_a[0];
        }
        if (par5 == 0) {
            return this.field_94364_a[1];
        }
        return this.field_94364_a[2];
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return Item.func_150898_a((Block)this);
    }

    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plant) {
        return true;
    }

    public boolean func_149662_c() {
        return OreSpawnMain.current_dimension == OreSpawnMain.DimensionID5;
    }

    public boolean func_149686_d() {
        return OreSpawnMain.current_dimension == OreSpawnMain.DimensionID5;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister par1IIconRegister) {
        this.field_94364_a = new IIcon[3];
        this.field_94364_a[0] = par1IIconRegister.func_94245_a("OreSpawn:crystalgrass_top");
        this.field_94364_a[1] = par1IIconRegister.func_94245_a("OreSpawn:crystalgrass_bottom");
        this.field_94364_a[2] = par1IIconRegister.func_94245_a("OreSpawn:crystalgrass_side");
    }
}

