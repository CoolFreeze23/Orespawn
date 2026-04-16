/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.AntRobot;
import danger.orespawn.OreSpawnMain;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class ItemSpiderRobotKit
extends Item {
    public ItemSpiderRobotKit(int i) {
        this.field_77777_bU = 1;
        this.func_77637_a(CreativeTabs.field_78040_i);
        if (i == OreSpawnMain.BaseItemID + 471) {
            this.func_77656_e(OreSpawnMain.SpiderRobot_stats.health);
        } else {
            this.func_77656_e(OreSpawnMain.AntRobot_stats.health);
        }
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        Entity ent;
        if (par3World.field_72995_K) {
            return true;
        }
        String name = null;
        name = "Robot Spider";
        if (par1ItemStack.func_77973_b() == OreSpawnMain.AntRobotKit) {
            name = "Robot Red Ant";
        }
        if ((ent = ItemSpiderRobotKit.spawnCreature((World)par3World, (int)0, (String)name, (double)((double)par4 + 0.5), (double)((double)par5 + 1.01), (double)((double)par6 + 0.5))) != null) {
            EntityLiving e = (EntityLiving)ent;
            e.func_70606_j((float)(this.func_77612_l() - this.getDamage(par1ItemStack)));
            if (ent instanceof EntityLiving && par1ItemStack.func_82837_s()) {
                ((EntityLiving)ent).func_94058_c(par1ItemStack.func_82833_r());
            }
            par3World.func_72956_a((Entity)par2EntityPlayer, "random.explode", 1.0f, par3World.field_73012_v.nextFloat() * 0.2f + 0.9f);
            if (ent instanceof AntRobot) {
                AntRobot a = (AntRobot)ent;
                a.setOwned();
            }
        }
        if (!par2EntityPlayer.field_71075_bZ.field_75098_d) {
            --par1ItemStack.field_77994_a;
        }
        return true;
    }

    public static Entity spawnCreature(World par0World, int par1, String name, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = name == null ? EntityList.func_75616_a((int)par1, (World)par0World) : EntityList.func_75620_a((String)name, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

