/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.ThunderBolt;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemThunderStaff
extends Item {
    private int ticker = 50;

    public ItemThunderStaff(int i) {
        this.field_77777_bU = 1;
        this.func_77656_e(50);
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (par1ItemStack.func_77958_k() - par1ItemStack.func_77960_j() <= 1) {
            return par1ItemStack;
        }
        double xzoff = 1.0;
        double yoff = 1.55;
        ThunderBolt lb = new ThunderBolt(par2World, (EntityLivingBase)par3EntityPlayer);
        lb.func_70012_b(par3EntityPlayer.field_70165_t - xzoff * Math.sin(Math.toRadians(par3EntityPlayer.field_70759_as + 45.0f)), par3EntityPlayer.field_70163_u + yoff, par3EntityPlayer.field_70161_v + xzoff * Math.cos(Math.toRadians(par3EntityPlayer.field_70759_as + 45.0f)), par3EntityPlayer.field_70759_as, par3EntityPlayer.field_70125_A);
        lb.field_70159_w *= 3.0;
        lb.field_70181_x *= 3.0;
        lb.field_70179_y *= 3.0;
        par2World.func_72838_d((Entity)lb);
        par3EntityPlayer.func_71038_i();
        par3EntityPlayer.func_70024_g(Math.cos(Math.toRadians(par3EntityPlayer.field_70759_as - 90.0f)) * 0.5, 0.15, Math.sin(Math.toRadians(par3EntityPlayer.field_70759_as - 90.0f)) * 0.5);
        par1ItemStack.func_77972_a(1, (EntityLivingBase)par3EntityPlayer);
        return par1ItemStack;
    }

    public void func_77663_a(ItemStack par1ItemStack, World world, Entity par3Entity, int par4, boolean par5) {
        if (world.func_72896_J() && world.func_72911_I()) {
            if (this.ticker > 0) {
                --this.ticker;
            }
            if (this.ticker <= 0 && par1ItemStack.func_77960_j() > 0) {
                par1ItemStack.func_77964_b(par1ItemStack.func_77960_j() - 1);
                this.ticker = 50;
            }
        }
    }

    public String getMaterialName() {
        return "Unknown";
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

