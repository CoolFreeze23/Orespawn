/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.AttackSquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemSquidZooka
extends Item {
    public ItemSquidZooka(int i) {
        this.field_77777_bU = 1;
        this.func_77656_e(100);
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (par1ItemStack.func_77958_k() - par1ItemStack.func_77960_j() <= 1) {
            return par1ItemStack;
        }
        par2World.func_72956_a((Entity)par3EntityPlayer, "random.explode", 0.5f, 0.5f);
        if (!par2World.field_72995_K) {
            double xzoff = 2.5;
            double yoff = 1.65;
            Entity e = ItemSquidZooka.spawnCreature(par2World, "Attack Squid", par3EntityPlayer.field_70165_t - xzoff * Math.sin(Math.toRadians(par3EntityPlayer.field_70759_as + 15.0f)), par3EntityPlayer.field_70163_u + yoff, par3EntityPlayer.field_70161_v + xzoff * Math.cos(Math.toRadians(par3EntityPlayer.field_70759_as + 15.0f)));
            if (e instanceof AttackSquid) {
                AttackSquid a = (AttackSquid)e;
                a.setWasShot();
            }
            float f = 3.6f;
            e.field_70159_w = -MathHelper.func_76126_a((float)(par3EntityPlayer.field_70177_z / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(par3EntityPlayer.field_70125_A / 180.0f * (float)Math.PI)) * f;
            e.field_70179_y = MathHelper.func_76134_b((float)(par3EntityPlayer.field_70177_z / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(par3EntityPlayer.field_70125_A / 180.0f * (float)Math.PI)) * f;
            e.field_70181_x = -MathHelper.func_76126_a((float)(par3EntityPlayer.field_70125_A / 180.0f * (float)Math.PI)) * f;
            e.field_70159_w += (double)(par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) * 0.05;
            e.field_70181_x += (double)(par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) * 0.05;
            e.field_70179_y += (double)(par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) * 0.05;
        }
        par3EntityPlayer.func_71038_i();
        par3EntityPlayer.func_70024_g(Math.cos(Math.toRadians(par3EntityPlayer.field_70759_as - 90.0f)) * 0.45, 0.1, Math.sin(Math.toRadians(par3EntityPlayer.field_70759_as - 90.0f)) * 0.45);
        par1ItemStack.func_77972_a(1, (EntityLivingBase)par3EntityPlayer);
        return par1ItemStack;
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
        }
        return var8;
    }

    public String getMaterialName() {
        return "Unknown";
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

