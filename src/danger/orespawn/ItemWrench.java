/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.AntRobot;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.SpiderRobot;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWrench
extends Item {
    public ItemWrench(int i) {
        this.func_77637_a(CreativeTabs.field_78040_i);
        this.func_77656_e(100);
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity != null && entity instanceof SpiderRobot && entity.field_70153_n == null) {
            EntityLiving e = (EntityLiving)entity;
            float h = e.func_110138_aP() - e.func_110143_aJ();
            e.func_70106_y();
            this.dropItem(player.field_70170_p, e, OreSpawnMain.SpiderRobotKit, 1, (int)h);
            for (int var3 = 0; var3 < 8; ++var3) {
                float f1 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                float f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 2.0f;
                float f3 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                player.field_70170_p.func_72869_a("smoke", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, 0.0, 0.0);
                f1 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 2.0f;
                f3 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                player.field_70170_p.func_72869_a("explode", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, 0.0, 0.0);
                f1 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 2.0f;
                f3 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                player.field_70170_p.func_72869_a("reddust", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, 0.0, 0.0);
            }
            player.field_70170_p.func_72956_a((Entity)player, "random.explode", 0.5f, 1.5f);
        } else if (entity != null && entity instanceof AntRobot && entity.field_70153_n == null) {
            AntRobot e = (AntRobot)entity;
            if (e.getOwned() == 0) {
                if (e.func_110143_aJ() / e.func_110138_aP() > 0.5f) {
                    return false;
                }
                e.setOwned();
            }
            float h = e.func_110138_aP() - e.func_110143_aJ();
            e.func_70106_y();
            this.dropItem(player.field_70170_p, e, OreSpawnMain.AntRobotKit, 1, (int)h);
            for (int var3 = 0; var3 < 8; ++var3) {
                float f1 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                float f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 2.0f;
                float f3 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                player.field_70170_p.func_72869_a("smoke", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, 0.0, 0.0);
                f1 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 2.0f;
                f3 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                player.field_70170_p.func_72869_a("explode", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, 0.0, 0.0);
                f1 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 2.0f;
                f3 = player.field_70170_p.field_73012_v.nextFloat() * 3.0f - player.field_70170_p.field_73012_v.nextFloat() * 3.0f;
                player.field_70170_p.func_72869_a("reddust", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, 0.0, 0.0);
            }
            player.field_70170_p.func_72956_a((Entity)player, "random.explode", 0.5f, 1.5f);
        } else {
            return false;
        }
        stack.func_77972_a(2, (EntityLivingBase)player);
        if (stack.field_77994_a <= 0) {
            player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
        }
        return true;
    }

    private void dropItem(World world, EntityLiving e, Item index, int par1, int par2) {
        if (world.field_72995_K) {
            return;
        }
        ItemStack is = new ItemStack(index, par1, 0);
        is.func_77964_b(par2);
        EntityItem var3 = new EntityItem(world, e.field_70165_t, e.field_70163_u + 1.0, e.field_70161_v, is);
        world.func_72838_d((Entity)var3);
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

