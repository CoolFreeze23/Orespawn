/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.BerthaHit;
import danger.orespawn.Boyfriend;
import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class Bertha
extends ItemSword {
    public Bertha(int par1, Item.ToolMaterial par2EnumToolMaterial) {
        super(par2EnumToolMaterial);
        this.field_77777_bU = 1;
        this.func_77656_e(9000);
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (this == OreSpawnMain.MyRoyal) {
            par1ItemStack.func_77966_a(Enchantment.field_77347_r, 5);
        } else if (this != OreSpawnMain.MyHammy) {
            par1ItemStack.func_77966_a(Enchantment.field_77337_m, 5);
            par1ItemStack.func_77966_a(Enchantment.field_77336_l, 1);
            par1ItemStack.func_77966_a(Enchantment.field_77334_n, 1);
        }
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77337_m.field_77352_x, (ItemStack)stack);
        if (lvl == 0) {
            lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77347_r.field_77352_x, (ItemStack)stack);
        }
        if (lvl <= 0) {
            if (this == OreSpawnMain.MyRoyal) {
                stack.func_77966_a(Enchantment.field_77347_r, 5);
            } else if (this != OreSpawnMain.MyHammy) {
                stack.func_77966_a(Enchantment.field_77337_m, 5);
                stack.func_77966_a(Enchantment.field_77336_l, 1);
                stack.func_77966_a(Enchantment.field_77334_n, 1);
            }
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        this.onUsingTick(stack, (EntityPlayer)null, 0);
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity != null && OreSpawnMain.big_bertha_pvp == 0) {
            EntityTameable t;
            if (entity instanceof EntityPlayer || entity instanceof Girlfriend || entity instanceof Boyfriend) {
                return true;
            }
            if (entity instanceof EntityTameable && (t = (EntityTameable)entity).func_70909_n()) {
                return true;
            }
        }
        return false;
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (entityLiving != null && entityLiving instanceof EntityPlayer && !entityLiving.field_70170_p.field_72995_K) {
            EntityPlayer p = (EntityPlayer)entityLiving;
            double xzoff = 2.0;
            double yoff = 1.55;
            BerthaHit lb = new BerthaHit(p.field_70170_p, (EntityLivingBase)p);
            lb.func_70012_b(p.field_70165_t - xzoff * Math.sin(Math.toRadians(p.field_70759_as)), p.field_70163_u + yoff, p.field_70161_v + xzoff * Math.cos(Math.toRadians(p.field_70759_as)), p.field_70759_as, p.field_70125_A);
            lb.field_70159_w *= 2.0;
            lb.field_70181_x *= 2.0;
            lb.field_70179_y *= 2.0;
            if (this == OreSpawnMain.MyRoyal) {
                lb.setHitType(2);
            }
            if (this == OreSpawnMain.MyHammy) {
                lb.setHitType(3);
            }
            p.field_70170_p.func_72838_d((Entity)lb);
            stack.func_77972_a(1, (EntityLivingBase)p);
        }
        return false;
    }

    public String getMaterialName() {
        return "Uranium/Titanium";
    }

    public boolean hitEntity(ItemStack par1ItemStack, EntityLiving par2EntityLiving, EntityLiving par3EntityLiving) {
        par1ItemStack.func_77972_a(1, (EntityLivingBase)par3EntityLiving);
        return true;
    }

    public int func_77626_a(ItemStack par1ItemStack) {
        return 9000;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

