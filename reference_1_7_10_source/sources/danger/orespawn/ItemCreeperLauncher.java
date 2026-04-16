/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemCreeperLauncher
extends Item {
    public ItemCreeperLauncher(int i) {
        this.func_77637_a(CreativeTabs.field_78028_d);
        this.func_77656_e(1);
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity != null && entity instanceof EntityCreeper) {
            for (int var3 = 0; var3 < 6; ++var3) {
                float f1 = player.field_70170_p.field_73012_v.nextFloat() - player.field_70170_p.field_73012_v.nextFloat();
                float f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 6.0f;
                float f3 = player.field_70170_p.field_73012_v.nextFloat() - player.field_70170_p.field_73012_v.nextFloat();
                player.field_70170_p.func_72869_a("smoke", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, (double)(f2 / 4.0f), 0.0);
                f1 = player.field_70170_p.field_73012_v.nextFloat() - player.field_70170_p.field_73012_v.nextFloat();
                f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 6.0f;
                f3 = player.field_70170_p.field_73012_v.nextFloat() - player.field_70170_p.field_73012_v.nextFloat();
                player.field_70170_p.func_72869_a("explode", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, (double)(f2 / 4.0f), 0.0);
                f1 = player.field_70170_p.field_73012_v.nextFloat() - player.field_70170_p.field_73012_v.nextFloat();
                f2 = 0.25f + player.field_70170_p.field_73012_v.nextFloat() * 6.0f;
                f3 = player.field_70170_p.field_73012_v.nextFloat() - player.field_70170_p.field_73012_v.nextFloat();
                player.field_70170_p.func_72869_a("reddust", (double)((float)entity.field_70165_t + f1), (double)((float)entity.field_70163_u + f2), (double)((float)entity.field_70161_v + f3), 0.0, (double)(f2 / 4.0f), 0.0);
            }
        } else {
            return false;
        }
        player.field_70170_p.func_72956_a((Entity)player, "fireworks.launch", 2.0f, 1.2f);
        EntityLiving e = (EntityLiving)entity;
        e.func_70024_g(0.0, 4.5, 0.0);
        if (!player.field_71075_bZ.field_75098_d) {
            --stack.field_77994_a;
        }
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

