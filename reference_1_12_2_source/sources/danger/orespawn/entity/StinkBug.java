/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
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
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.AxisAlignedBB
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.util.ai.MyEntityAIWanderALot;
import danger.orespawn.util.handlers.SoundsHandler;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
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
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class StinkBug
extends EntityAnimal {
    private float moveSpeed = 0.15f;

    public StinkBug(World par1World) {
        super(par1World);
        this.func_70105_a(0.55f, 0.55f);
        this.field_70728_aV = 2;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMate((EntityAnimal)this, 1.0));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityPlayer.class, 4.0f, 1.0, (double)1.4f));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0f));
        this.field_70714_bg.func_75776_a(8, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 10, 1.0));
        this.field_70714_bg.func_75776_a(9, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(10, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        super.func_70619_bc();
    }

    public boolean func_175446_cd() {
        return false;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (this.field_70128_L) {
            return false;
        }
        ret = super.func_70097_a(par1DamageSource, par2);
        if (this.func_110143_aJ() <= 0.0f || this.field_70128_L) {
            AxisAlignedBB bb = this.func_174813_aQ().func_72321_a(8.0, 5.0, 8.0);
            List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, bb);
            Iterator var2 = var5.iterator();
            EntityLivingBase var3 = null;
            while (var2.hasNext()) {
                var3 = (EntityLivingBase)var2.next();
                if (var3 == null) continue;
                var3.func_70690_d(new PotionEffect(Objects.requireNonNull(Potion.func_188412_a((int)9)), 300, 0));
            }
        }
        return ret;
    }

    public boolean func_70648_aU() {
        return false;
    }

    public int mygetMaxHealth() {
        return 5;
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        return null;
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return null;
    }

    public void func_70645_a(DamageSource cause) {
        super.func_70645_a(cause);
        switch (this.field_70146_Z.nextInt(9) + 1) {
            case 1: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART1, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 2: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART2, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 3: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART3, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 4: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART4, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 5: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART5, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 6: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART6, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 7: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART7, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 8: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART8, this.func_70599_aP(), this.func_70647_i());
                break;
            }
            case 9: {
                this.func_184185_a(SoundsHandler.ENTITY_STINKBUG_FART9, this.func_70599_aP(), this.func_70647_i());
            }
        }
    }

    protected float func_70599_aP() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return null;
    }

    public boolean func_70601_bi() {
        return !(this.field_70163_u < 50.0);
    }

    protected boolean func_70692_ba() {
        if (this.func_70631_g_()) {
            return false;
        }
        return !this.func_104002_bU();
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return this.spawnBabyAnimal(entityageable);
    }

    public StinkBug spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
        return new StinkBug(this.field_70170_p);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151115_aP;
    }

    public boolean func_70877_b(ItemStack par1ItemStack) {
        return false;
    }
}

