/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPizza
extends Item {
    private Block spawnID;

    public ItemPizza(int par1, Block par2Block) {
        this.spawnID = par2Block;
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        int j1;
        Block i1 = par3World.func_147439_a(par4, par5, par6);
        if (i1 == Blocks.field_150433_aE && (par3World.func_72805_g(par4, par5, par6) & 7) < 1) {
            par7 = 1;
        } else if (i1 != Blocks.field_150395_bd && i1 != Blocks.field_150329_H && i1 != Blocks.field_150330_I) {
            if (par7 == 0) {
                --par5;
            }
            if (par7 == 1) {
                ++par5;
            }
            if (par7 == 2) {
                --par6;
            }
            if (par7 == 3) {
                ++par6;
            }
            if (par7 == 4) {
                --par4;
            }
            if (par7 == 5) {
                ++par4;
            }
        }
        if (!par2EntityPlayer.func_82247_a(par4, par5, par6, par7, par1ItemStack)) {
            return false;
        }
        if (par1ItemStack.field_77994_a == 0) {
            return false;
        }
        if (par3World.func_147472_a(this.spawnID, par4, par5, par6, false, par7, (Entity)null, par1ItemStack) && par3World.func_147465_d(par4, par5, par6, this.spawnID, j1 = this.spawnID.func_149660_a(par3World, par4, par5, par6, par7, par8, par9, par10, 0), 3)) {
            if (par3World.func_147439_a(par4, par5, par6) == this.spawnID) {
                this.spawnID.func_149689_a(par3World, par4, par5, par6, (EntityLivingBase)par2EntityPlayer, par1ItemStack);
                this.spawnID.func_149714_e(par3World, par4, par5, par6, j1);
            }
            par3World.func_72908_a((double)((float)par4 + 0.5f), (double)((float)par5 + 0.5f), (double)((float)par6 + 0.5f), this.spawnID.field_149762_H.func_150498_e(), (this.spawnID.field_149762_H.func_150497_c() + 1.0f) / 2.0f, this.spawnID.field_149762_H.func_150494_d() * 0.8f);
            --par1ItemStack.field_77994_a;
        }
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

