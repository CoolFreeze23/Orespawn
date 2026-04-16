/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.EntityThrownRock;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.RockBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemRock
extends Item {
    public ItemRock(int i) {
        this.field_77777_bU = 64;
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (!par3EntityPlayer.field_71075_bZ.field_75098_d) {
            --par1ItemStack.field_77994_a;
        }
        par2World.func_72956_a((Entity)par3EntityPlayer, "random.bow", 0.5f, 0.4f / (field_77697_d.nextFloat() * 0.4f + 0.8f));
        if (!par2World.field_72995_K) {
            if (this == OreSpawnMain.MySmallRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 1));
            }
            if (this == OreSpawnMain.MyRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 2));
            }
            if (this == OreSpawnMain.MyRedRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 3));
            }
            if (this == OreSpawnMain.MyGreenRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 4));
            }
            if (this == OreSpawnMain.MyBlueRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 5));
            }
            if (this == OreSpawnMain.MyPurpleRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 6));
            }
            if (this == OreSpawnMain.MySpikeyRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 7));
            }
            if (this == OreSpawnMain.MyTNTRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 8));
            }
            if (this == OreSpawnMain.MyCrystalRedRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 9));
            }
            if (this == OreSpawnMain.MyCrystalGreenRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 10));
            }
            if (this == OreSpawnMain.MyCrystalBlueRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 11));
            }
            if (this == OreSpawnMain.MyCrystalTNTRock) {
                par2World.func_72838_d((Entity)new EntityThrownRock(par2World, (EntityLivingBase)par3EntityPlayer, 12));
            }
        }
        return par1ItemStack;
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if (!world.field_72995_K) {
            Entity e;
            if (x < 0) {
                ++x;
            }
            if (z < 0) {
                ++z;
            }
            if ((e = this.spawnCreature(world, "Rock", (double)x, (double)y + 1.01, (double)z)) != null) {
                RockBase r = (RockBase)e;
                if (this == OreSpawnMain.MySmallRock) {
                    r.placeRock(1);
                }
                if (this == OreSpawnMain.MyRock) {
                    r.placeRock(2);
                }
                if (this == OreSpawnMain.MyRedRock) {
                    r.placeRock(3);
                }
                if (this == OreSpawnMain.MyGreenRock) {
                    r.placeRock(4);
                }
                if (this == OreSpawnMain.MyBlueRock) {
                    r.placeRock(5);
                }
                if (this == OreSpawnMain.MyPurpleRock) {
                    r.placeRock(6);
                }
                if (this == OreSpawnMain.MySpikeyRock) {
                    r.placeRock(7);
                }
                if (this == OreSpawnMain.MyTNTRock) {
                    r.placeRock(8);
                }
                if (this == OreSpawnMain.MyCrystalRedRock) {
                    r.placeRock(9);
                }
                if (this == OreSpawnMain.MyCrystalGreenRock) {
                    r.placeRock(10);
                }
                if (this == OreSpawnMain.MyCrystalBlueRock) {
                    r.placeRock(11);
                }
                if (this == OreSpawnMain.MyCrystalTNTRock) {
                    r.placeRock(12);
                }
            }
        }
        if (!par2EntityPlayer.field_71075_bZ.field_75098_d) {
            --par1ItemStack.field_77994_a;
        }
        return true;
    }

    private Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            if (par2 > 0.0) {
                par2 += 0.5;
            }
            if (par2 < 0.0) {
                par2 -= 0.5;
            }
            if (par6 > 0.0) {
                par6 += 0.5;
            }
            if (par6 < 0.0) {
                par6 -= 0.5;
            }
            var8.func_70012_b(par2, par4 + 0.01, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
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

