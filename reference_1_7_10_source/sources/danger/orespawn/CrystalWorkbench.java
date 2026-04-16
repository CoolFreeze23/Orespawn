/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class CrystalWorkbench
extends BlockWorkbench {
    @SideOnly(value=Side.CLIENT)
    private IIcon workbenchIconTop;
    @SideOnly(value=Side.CLIENT)
    private IIcon workbenchIconFront;

    protected CrystalWorkbench(int par1, float f1, float f2) {
        this.func_149647_a(CreativeTabs.field_78031_c);
        this.func_149711_c(f1);
        this.func_149752_b(f2);
    }

    public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        if (par1World.field_72995_K) {
            return true;
        }
        par5EntityPlayer.openGui((Object)OreSpawnMain.instance, 1, par1World, par2, par3, par4);
        return true;
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return false;
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        return par1 == 1 ? this.workbenchIconTop : (par1 == 0 ? this.field_149761_L : (par1 != 2 && par1 != 4 ? this.field_149761_L : this.workbenchIconFront));
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister par1IIconRegister) {
        this.field_149761_L = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_side");
        this.workbenchIconTop = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_top");
        this.workbenchIconFront = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_bottom");
    }
}

