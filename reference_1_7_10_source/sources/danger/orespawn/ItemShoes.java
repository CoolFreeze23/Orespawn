/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.Shoes;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemShoes
extends Item {
    private int my_id = 0;

    public ItemShoes(int i, int j) {
        this.my_id = j;
        this.field_77777_bU = 64;
        this.func_77637_a(CreativeTabs.field_78031_c);
    }

    public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (!par3EntityPlayer.field_71075_bZ.field_75098_d) {
            --par1ItemStack.field_77994_a;
        }
        par2World.func_72956_a((Entity)par3EntityPlayer, "random.bow", 0.5f, 0.4f / (field_77697_d.nextFloat() * 0.4f + 0.8f));
        if (!par2World.field_72995_K) {
            par2World.func_72838_d((Entity)new Shoes(par2World, (EntityLivingBase)par3EntityPlayer, this.my_id));
        }
        return par1ItemStack;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

