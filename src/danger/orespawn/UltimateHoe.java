/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item$ToolMaterial
 *  net.minecraft.item.ItemHoe
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class UltimateHoe
extends ItemHoe {
    public UltimateHoe(int par1, Item.ToolMaterial par2) {
        super(par2);
        this.field_77777_bU = 1;
        this.func_77656_e(3000);
        this.func_77637_a(CreativeTabs.field_78040_i);
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par1ItemStack.func_77966_a(Enchantment.field_77349_p, 2);
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77349_p.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            stack.func_77966_a(Enchantment.field_77349_p, 2);
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        this.onUsingTick(stack, null, 0);
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        if (!par2EntityPlayer.func_82247_a(par4, par5, par6, par7, par1ItemStack)) {
            return false;
        }
        Block i1 = par3World.func_147439_a(par4, par5, par6);
        boolean air = par3World.func_147437_c(par4, par5 + 1, par6);
        if (par7 != 0 && air && (i1 == Blocks.field_150349_c || i1 == Blocks.field_150346_d)) {
            Block block = Blocks.field_150458_ak;
            par3World.func_72908_a((double)((float)par4 + 0.5f), (double)((float)par5 + 0.5f), (double)((float)par6 + 0.5f), block.field_149762_H.func_150495_a(), (block.field_149762_H.func_150497_c() + 1.0f) / 2.0f, block.field_149762_H.func_150494_d() * 0.8f);
            if (par3World.field_72995_K) {
                return true;
            }
            for (int i = -1; i <= 1; ++i) {
                for (int k = -1; k <= 1; ++k) {
                    for (int j = -1; j <= 1; ++j) {
                        i1 = par3World.func_147439_a(par4 + i, par5 + j, par6 + k);
                        air = par3World.func_147437_c(par4 + i, par5 + j + 1, par6 + k);
                        if (!air || i1 != Blocks.field_150349_c && i1 != Blocks.field_150346_d) continue;
                        par3World.func_147465_d(par4 + i, par5 + j, par6 + k, block, 7, 2);
                    }
                }
            }
            par1ItemStack.func_77972_a(1, (EntityLivingBase)par2EntityPlayer);
            return true;
        }
        return false;
    }

    public String getMaterialName() {
        return "Uranium/Titanium";
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

