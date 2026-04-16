/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.UltimateFishHook;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class UltimateFishingRod
extends Item {
    public UltimateFishingRod(int par1) {
        this.func_77656_e(3000);
        this.func_77625_d(1);
        this.func_77637_a(CreativeTabs.field_78040_i);
    }

    public boolean func_77662_d() {
        return true;
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par1ItemStack.func_77966_a(Enchantment.field_77347_r, 2);
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77347_r.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            stack.func_77966_a(Enchantment.field_77347_r, 2);
        }
    }

    public boolean func_77629_n_() {
        return true;
    }

    public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (par3EntityPlayer.field_71104_cf != null) {
            int var4 = par3EntityPlayer.field_71104_cf.func_146034_e();
            par1ItemStack.func_77972_a(var4, (EntityLivingBase)par3EntityPlayer);
            par3EntityPlayer.func_71038_i();
        } else {
            par2World.func_72956_a((Entity)par3EntityPlayer, "random.bow", 0.5f, 0.4f / (field_77697_d.nextFloat() * 0.4f + 0.8f));
            if (!par2World.field_72995_K) {
                par2World.func_72838_d((Entity)new UltimateFishHook(par2World, par3EntityPlayer));
            }
            par3EntityPlayer.func_71038_i();
        }
        return par1ItemStack;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

