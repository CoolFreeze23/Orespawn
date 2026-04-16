/*
 * Decompiled with CFR 0.152.
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
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemNetherLost
extends Item {
    public ItemNetherLost(int par1) {
        this.field_77777_bU = 1;
        this.func_77656_e(3000);
        this.func_77637_a(CreativeTabs.field_78031_c);
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par1ItemStack.func_77966_a(Enchantment.field_77338_j, 2);
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77338_j.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            stack.func_77966_a(Enchantment.field_77338_j, 2);
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        Block i;
        Item it;
        ItemStack is;
        EntityLivingBase e = null;
        EntityPlayer p = null;
        this.onUsingTick(stack, (EntityPlayer)null, 0);
        if (par2World == null) {
            return;
        }
        if (par3Entity != null && par3Entity instanceof EntityLivingBase && (e = (EntityLivingBase)par3Entity) instanceof EntityPlayer && (is = (p = (EntityPlayer)e).func_71045_bC()) != null && (it = is.func_77973_b()) != null && it instanceof ItemNetherLost && par2World.field_73011_w.field_76574_g == -1 && (i = par2World.func_147439_a((int)p.field_70165_t, (int)p.field_70163_u - 1, (int)p.field_70161_v)) == Blocks.field_150424_aL) {
            par2World.func_147449_b((int)p.field_70165_t, (int)p.field_70163_u - 1, (int)p.field_70161_v, Blocks.field_150371_ca);
        }
    }

    public int func_77626_a(ItemStack par1ItemStack) {
        return 3000;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

