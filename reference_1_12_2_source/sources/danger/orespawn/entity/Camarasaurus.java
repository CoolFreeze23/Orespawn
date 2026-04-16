/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIAvoidEntity
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIMate
 *  net.minecraft.entity.ai.EntityAIMoveIndoors
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.ai.EntityAITempt
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.util.ai.MyEntityAIFollowOwner;
import danger.orespawn.util.ai.MyEntityAIWander;
import danger.orespawn.util.handlers.SoundsHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Camarasaurus
extends EntityTameable {
    private float moveSpeed = 0.2f;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Camarasaurus(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 1.2f);
        this.moveSpeed = 0.2f;
        this.field_70728_aV = 5;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMate((EntityAnimal)this, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityMob.class, 8.0f, 1.0, (double)1.4f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAITempt((EntityCreature)this, (double)1.2f, Items.field_151034_e, false));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0f));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 1.0f));
        this.field_70714_bg.func_75776_a(8, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(9, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.func_70904_g(false);
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        return this.field_70170_p.func_72935_r();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.updateAITick();
        super.func_70071_h_();
    }

    protected void fall(float par1) {
        float i = MathHelper.func_76123_f((float)(par1 - 3.0f));
        if (i > 0.0f) {
            if (i > 3.0f) {
                this.func_184185_a(SoundEvents.field_187655_bw, 1.0f, 1.0f);
            } else {
                this.func_184185_a(SoundEvents.field_187545_bE, 1.0f, 1.0f);
            }
            if (i > 2.0f) {
                i = 2.0f;
            }
            this.func_70097_a(DamageSource.field_76379_h, i);
        }
    }

    private boolean scan_it(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        Block bid;
        int j;
        int i;
        int found = 0;
        for (i = -dy; i <= dy; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + dx, y + i, z + j)).func_177230_c();
                if ((bid == Blocks.field_150362_t || bid == Blocks.field_150395_bd || bid == Blocks.field_150329_H || bid == Blocks.field_150434_aF || bid == Blocks.field_150398_cm) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x - dx, y + i, z + j)).func_177230_c()) != Blocks.field_150362_t && bid != Blocks.field_150395_bd && bid != Blocks.field_150329_H && bid != Blocks.field_150434_aF && bid != Blocks.field_150398_cm || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + dy, z + j)).func_177230_c();
                if ((bid == Blocks.field_150362_t || bid == Blocks.field_150395_bd || bid == Blocks.field_150329_H || bid == Blocks.field_150434_aF || bid == Blocks.field_150398_cm) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y - dy, z + j)).func_177230_c()) != Blocks.field_150362_t && bid != Blocks.field_150395_bd && bid != Blocks.field_150329_H && bid != Blocks.field_150434_aF && bid != Blocks.field_150398_cm || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z + dz)).func_177230_c();
                if ((bid == Blocks.field_150362_t || bid == Blocks.field_150395_bd || bid == Blocks.field_150329_H || bid == Blocks.field_150434_aF || bid == Blocks.field_150398_cm) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z - dz)).func_177230_c()) != Blocks.field_150362_t && bid != Blocks.field_150395_bd && bid != Blocks.field_150329_H && bid != Blocks.field_150434_aF && bid != Blocks.field_150398_cm || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    protected void updateAITick() {
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        super.func_70619_bc();
        if (!this.func_70906_o() && (this.field_70170_p.field_73012_v.nextInt(20) == 0 && this.getCamarasaurusHealth() < this.mygetMaxHealth() || this.field_70170_p.field_73012_v.nextInt(250) == 0) && OreSpawnMain.PlayNicely == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 11; ++i) {
                int j = i;
                if (j > 2) {
                    j = 2;
                }
                if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u + 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 6) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)this.ty, (double)this.tz, 1.0);
                if (this.closest < 12) {
                    if (this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                        this.field_70170_p.func_175656_a(new BlockPos(this.tx, this.ty, this.tz), Blocks.field_150350_a.func_176223_P());
                    }
                    this.func_70691_i(1.0f);
                    this.func_184185_a(SoundEvents.field_187739_dZ, 1.0f, this.field_70170_p.field_73012_v.nextFloat() * 0.2f + 0.9f);
                }
            }
        }
    }

    public boolean isAIEnabled() {
        return true;
    }

    public boolean func_70648_aU() {
        return false;
    }

    public int mygetMaxHealth() {
        return 20;
    }

    public int getCamarasaurusHealth() {
        return (int)this.func_110143_aJ();
    }

    public boolean func_184645_a(EntityPlayer par1EntityPlayer, EnumHand hand) {
        ItemStack var2 = par1EntityPlayer.func_184586_b(hand);
        if (var2 != null && var2.func_190916_E() <= 0) {
            var2 = null;
        }
        if (super.func_184645_a(par1EntityPlayer, hand)) {
            return true;
        }
        if (var2 != null && var2.func_77973_b() == Items.field_151034_e && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            if (!this.func_70909_n()) {
                if (!this.field_70170_p.field_72995_K) {
                    if (this.field_70146_Z.nextInt(2) == 0) {
                        this.func_70903_f(true);
                        this.func_193101_c(par1EntityPlayer);
                        this.func_70908_e(true);
                        this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                        this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                    } else {
                        this.func_70908_e(false);
                        this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                        this.func_70904_g(true);
                    }
                }
            } else if (this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
                if (this.field_70170_p.field_72995_K) {
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
                if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                    this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                    this.func_70908_e(true);
                }
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                var2.func_190918_g(1);
                if (var2.func_190916_E() <= 0) {
                    // empty if block
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151057_cb && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            this.func_96094_a(var2.func_82833_r());
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                var2.func_190918_g(1);
                if (var2.func_190916_E() <= 0) {
                    // empty if block
                }
            }
            return true;
        }
        if (this.func_70909_n() && this.func_152114_e((EntityLivingBase)par1EntityPlayer) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            if (!this.func_70906_o()) {
                this.func_70904_g(true);
            } else {
                this.func_70904_g(false);
            }
            return true;
        }
        return false;
    }

    protected SoundEvent func_184639_G() {
        if (this.func_70906_o()) {
            return null;
        }
        return null;
    }

    protected SoundEvent func_184601_bQ(DamageSource source) {
        return SoundsHandler.ENTITY_CRYO_HURT;
    }

    protected SoundEvent func_184615_bR() {
        return SoundsHandler.ENTITY_CRYO_DEATH;
    }

    protected float func_70599_aP() {
        return 0.4f;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)Blocks.field_150328_O);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var3 = 0;
        if (this.func_70909_n()) {
            var3 = this.field_70146_Z.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.func_145779_a(Item.func_150898_a((Block)Blocks.field_150328_O), 1);
            }
        }
    }

    protected float func_70647_i() {
        return this.func_70631_g_() ? (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.1f + 1.5f : (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.1f + 1.0f;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        ret = super.func_70097_a(par1DamageSource, par2);
        return ret;
    }

    protected boolean func_70692_ba() {
        if (this.func_70631_g_()) {
            return false;
        }
        if (this.func_104002_bU()) {
            return false;
        }
        return !this.func_70909_n();
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return new Camarasaurus(this.field_70170_p);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151034_e;
    }

    public boolean func_70877_b(ItemStack par1ItemStack) {
        return par1ItemStack.func_77973_b() == Items.field_151153_ao;
    }

    public boolean func_70878_b(EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        }
        if (!this.func_70909_n()) {
            return false;
        }
        if (!(otherAnimal instanceof Camarasaurus)) {
            return false;
        }
        Camarasaurus camarasaurus = (Camarasaurus)otherAnimal;
        if (!camarasaurus.func_70909_n()) {
            return false;
        }
        if (camarasaurus.func_70906_o()) {
            return false;
        }
        if (this.func_70906_o()) {
            return false;
        }
        return this.func_70880_s() && camarasaurus.func_70880_s();
    }
}

