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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPizza
extends Block {
    @SideOnly(value=Side.CLIENT)
    private IIcon pizzaTopIcon;
    @SideOnly(value=Side.CLIENT)
    private IIcon pizzaBottomIcon;
    @SideOnly(value=Side.CLIENT)
    private IIcon field_94382_c;

    protected BlockPizza(int par1) {
        super(Material.field_151568_F);
        this.func_149675_a(true);
    }

    public void func_149719_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
        int l = par1IBlockAccess.func_72805_g(par2, par3, par4);
        float f = 0.0625f;
        float f1 = (float)(1 + l * 2) / 16.0f;
        float f2 = 0.25f;
        this.func_149676_a(f1, 0.0f, f, 1.0f - f, f2, 1.0f - f);
    }

    public void func_149683_g() {
        float f = 0.0625f;
        float f1 = 0.25f;
        this.func_149676_a(f, 0.0f, f, 1.0f - f, f1, 1.0f - f);
    }

    public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
        int l = par1World.func_72805_g(par2, par3, par4);
        float f = 0.0625f;
        float f1 = (float)(1 + l * 2) / 16.0f;
        float f2 = 0.25f;
        return AxisAlignedBB.func_72330_a((double)((float)par2 + f1), (double)par3, (double)((float)par4 + f), (double)((float)(par2 + 1) - f), (double)((float)par3 + f2 - f), (double)((float)(par4 + 1) - f));
    }

    public boolean func_149686_d() {
        return false;
    }

    @SideOnly(value=Side.CLIENT)
    public AxisAlignedBB func_149633_g(World par1World, int par2, int par3, int par4) {
        int l = par1World.func_72805_g(par2, par3, par4);
        float f = 0.0625f;
        float f1 = (float)(1 + l * 2) / 16.0f;
        float f2 = 0.25f;
        return AxisAlignedBB.func_72330_a((double)((float)par2 + f1), (double)par3, (double)((float)par4 + f), (double)((float)(par2 + 1) - f), (double)((float)par3 + f2), (double)((float)(par4 + 1) - f));
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        return par1 == 1 ? this.pizzaTopIcon : (par1 == 0 ? this.pizzaBottomIcon : (par2 > 0 && par1 == 4 ? this.field_94382_c : this.field_149761_L));
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister par1IIconRegister) {
        this.field_149761_L = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_side");
        this.field_94382_c = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_inner");
        this.pizzaTopIcon = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_top");
        this.pizzaBottomIcon = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_bottom");
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        this.eatPizzaSlice(par1World, par2, par3, par4, par5EntityPlayer);
        return true;
    }

    public void func_149699_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
        this.eatPizzaSlice(par1World, par2, par3, par4, par5EntityPlayer);
    }

    private void eatPizzaSlice(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
        if (par5EntityPlayer.func_71043_e(false)) {
            par5EntityPlayer.func_71024_bL().func_75122_a(4, 0.2f);
            int l = par1World.func_72805_g(par2, par3, par4) + 1;
            if (l >= 6) {
                par1World.func_147468_f(par2, par3, par4);
            } else {
                par1World.func_72921_c(par2, par3, par4, l, 2);
            }
        }
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        return !super.func_149742_c(par1World, par2, par3, par4) ? false : this.func_149718_j(par1World, par2, par3, par4);
    }

    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
        if (!this.func_149718_j(par1World, par2, par3, par4)) {
            par1World.func_147468_f(par2, par3, par4);
        }
    }

    public boolean func_149718_j(World par1World, int par2, int par3, int par4) {
        return par1World.func_147439_a(par2, par3 - 1, par4).func_149721_r();
    }

    public int func_149745_a(Random par1Random) {
        return 0;
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return OreSpawnMain.MyPizzaItem;
    }
}

