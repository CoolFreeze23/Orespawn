/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDuctTape
extends Item {
    private Block field_150935_a;

    public ItemDuctTape(int par1, Block par2Block) {
        this.field_150935_a = par2Block;
    }

    public boolean func_77648_a(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_, int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        int i1;
        Block block = p_77648_3_.func_147439_a(p_77648_4_, p_77648_5_, p_77648_6_);
        if (block == Blocks.field_150431_aC && (p_77648_3_.func_72805_g(p_77648_4_, p_77648_5_, p_77648_6_) & 7) < 1) {
            p_77648_7_ = 1;
        } else if (block != Blocks.field_150395_bd && block != Blocks.field_150329_H && block != Blocks.field_150330_I) {
            if (p_77648_7_ == 0) {
                --p_77648_5_;
            }
            if (p_77648_7_ == 1) {
                ++p_77648_5_;
            }
            if (p_77648_7_ == 2) {
                --p_77648_6_;
            }
            if (p_77648_7_ == 3) {
                ++p_77648_6_;
            }
            if (p_77648_7_ == 4) {
                --p_77648_4_;
            }
            if (p_77648_7_ == 5) {
                ++p_77648_4_;
            }
        }
        if (!p_77648_2_.func_82247_a(p_77648_4_, p_77648_5_, p_77648_6_, p_77648_7_, p_77648_1_)) {
            return false;
        }
        if (p_77648_1_.field_77994_a == 0) {
            return false;
        }
        if (p_77648_3_.func_147472_a(this.field_150935_a, p_77648_4_, p_77648_5_, p_77648_6_, false, p_77648_7_, (Entity)null, p_77648_1_) && p_77648_3_.func_147465_d(p_77648_4_, p_77648_5_, p_77648_6_, this.field_150935_a, i1 = this.field_150935_a.func_149660_a(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, p_77648_7_, p_77648_8_, p_77648_9_, p_77648_10_, 0), 3)) {
            if (p_77648_3_.func_147439_a(p_77648_4_, p_77648_5_, p_77648_6_) == this.field_150935_a) {
                this.field_150935_a.func_149689_a(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, (EntityLivingBase)p_77648_2_, p_77648_1_);
                this.field_150935_a.func_149714_e(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, i1);
            }
            p_77648_3_.func_72908_a((double)((float)p_77648_4_ + 0.5f), (double)((float)p_77648_5_ + 0.5f), (double)((float)p_77648_6_ + 0.5f), this.field_150935_a.field_149762_H.func_150496_b(), (this.field_150935_a.field_149762_H.func_150497_c() + 1.0f) / 2.0f, this.field_150935_a.field_149762_H.func_150494_d() * 0.8f);
            --p_77648_1_.field_77994_a;
        }
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

