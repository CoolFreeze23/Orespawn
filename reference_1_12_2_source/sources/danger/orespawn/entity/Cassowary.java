/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIAvoidEntity
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.util.ai.MyEntityAIWander;
import danger.orespawn.util.handlers.SoundsHandler;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class Cassowary
extends EntityAnimal {
    private float moveSpeed = 0.25f;

    public Cassowary(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 1.2f);
        this.moveSpeed = 0.25f;
        this.field_70728_aV = 5;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityMob.class, 8.0f, 1.0, (double)1.4f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityPlayer.class, 8.0f, 1.0, (double)1.4f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 12.0f));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 1.0f));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    }

    @Nullable
    public EntityAgeable func_90011_a(EntityAgeable ageable) {
        return null;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(8.0);
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    public boolean func_175446_cd() {
        return false;
    }

    public boolean func_70648_aU() {
        return false;
    }

    public int mygetMaxHealth() {
        return 10;
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        return null;
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return SoundsHandler.ENTITY_DUCK_HURT;
    }

    public void func_70645_a(DamageSource cause) {
        super.func_70645_a(cause);
        this.func_184185_a(SoundsHandler.ENTITY_DUCK_HURT, this.func_70599_aP(), this.func_70647_i());
    }

    protected float func_70599_aP() {
        return 0.4f;
    }

    protected Item func_146068_u() {
        return Items.field_151076_bf;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var3 = 0;
        var3 = this.field_70146_Z.nextInt(3);
        var3 += 2;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(Items.field_151076_bf, 1);
        }
    }

    protected void func_70619_bc() {
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        super.func_70619_bc();
    }

    public boolean func_70601_bi() {
        return this.field_70170_p.func_72935_r();
    }

    protected boolean func_70692_ba() {
        if (this.func_70631_g_()) {
            return false;
        }
        return !this.func_104002_bU();
    }

    public Cassowary spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
        return new Cassowary(this.field_70170_p);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151034_e;
    }

    public boolean func_70877_b(ItemStack par1ItemStack) {
        return false;
    }
}

