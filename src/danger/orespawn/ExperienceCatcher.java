/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.item.EntityXPOrb
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class ExperienceCatcher
extends Item {
    public ExperienceCatcher(int i) {
        this.field_77777_bU = 16;
        this.func_77637_a(CreativeTabs.field_78040_i);
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        par2EntityPlayer.func_71038_i();
        System.out.printf("x, y,z, 7,8,9,10 == %d, %d, %d - %d, %f, %f, %f\n", x, y, z, par7, Float.valueOf(par8), Float.valueOf(par9), Float.valueOf(par10));
        if (!par2EntityPlayer.field_70170_p.field_72995_K) {
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)((double)x - 0.5 + (double)par8), (double)y, (double)((double)z - 0.5 + (double)par10), (double)((double)x + 0.5 + (double)par8), (double)((double)y + 2.0), (double)((double)z + 0.5 + (double)par10));
            List var5 = world.func_72872_a(EntityXPOrb.class, bb);
            for (Entity var3 : var5) {
                EntityXPOrb ex;
                if (!(var3 instanceof EntityXPOrb) || (ex = (EntityXPOrb)var3).func_70526_d() < 3 || world.field_73012_v.nextInt(5) == 1) continue;
                var3.func_70106_y();
                EntityItem var4 = null;
                ItemStack is = new ItemStack(Items.field_151062_by, 1, 0);
                var4 = new EntityItem(par2EntityPlayer.field_70170_p, (double)(par8 + (float)x), (double)y + 1.0, (double)(par10 + (float)z), is);
                if (var4 != null) {
                    par2EntityPlayer.field_70170_p.func_72838_d((Entity)var4);
                }
                if ((var4 = new EntityItem(par2EntityPlayer.field_70170_p, (double)(par8 + (float)x), (double)y + 1.0, (double)(par10 + (float)z), is = new ItemStack(Items.field_151007_F, 1, 0))) != null) {
                    par2EntityPlayer.field_70170_p.func_72838_d((Entity)var4);
                }
                if ((var4 = new EntityItem(par2EntityPlayer.field_70170_p, (double)(par8 + (float)x), (double)y + 1.0, (double)(par10 + (float)z), is = new ItemStack(Items.field_151055_y, 1, 0))) != null) {
                    par2EntityPlayer.field_70170_p.func_72838_d((Entity)var4);
                }
                if (!par2EntityPlayer.field_71075_bZ.field_75098_d) {
                    --par1ItemStack.field_77994_a;
                }
                return true;
            }
            EntityItem var4 = null;
            ItemStack is = new ItemStack(OreSpawnMain.MyExperienceCatcher, 1, 0);
            var4 = new EntityItem(par2EntityPlayer.field_70170_p, (double)(par8 + (float)x), (double)y + 1.0, (double)(par10 + (float)z), is);
            if (var4 != null) {
                par2EntityPlayer.field_70170_p.func_72838_d((Entity)var4);
            }
            --par1ItemStack.field_77994_a;
        }
        return true;
    }

    public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par3EntityPlayer.func_71038_i();
        return par1ItemStack;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

