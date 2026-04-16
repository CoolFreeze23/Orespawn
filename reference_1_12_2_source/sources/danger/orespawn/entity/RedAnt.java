/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIAttackMelee
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAINearestAttackableTarget
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.DimensionType
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.Ant;
import danger.orespawn.init.ModDimensions;
import danger.orespawn.util.Teleport;
import danger.orespawn.util.ai.MyEntityAIWanderALot;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class RedAnt
extends Ant {
    int attack_delay = 20;
    public double moveSpeed = 0.2f;

    public RedAnt(World par1World) {
        super(par1World);
        this.func_70105_a(0.2f, 0.2f);
        this.field_70728_aV = 1;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIPanic((EntityCreature)this, (double)1.4f));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIAttackMelee((EntityCreature)this, 1.0, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 10, 1.0));
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, true));
        }
    }

    @Override
    @Nullable
    public EntityAgeable func_90011_a(EntityAgeable ageable) {
        return null;
    }

    @Override
    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0);
    }

    @Override
    public int mygetMaxHealth() {
        return 2;
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (OreSpawnMain.OreSpawnRand.nextInt(15) != 0) {
            return false;
        }
        if (this.field_70170_p.func_175659_aa() == EnumDifficulty.PEACEFUL) {
            return false;
        }
        return par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 1.0f);
    }

    public boolean func_184645_a(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.func_184586_b(hand);
        if (itemstack.func_190926_b()) {
            if (player.field_71093_bK == DimensionType.OVERWORLD.func_186068_a()) {
                BlockPos playerPos = player.func_180425_c();
                Teleport.teleportToDimension(player, ModDimensions.MINING.func_186068_a(), playerPos.func_177958_n(), playerPos.func_177956_o(), playerPos.func_177952_p());
                player.field_71093_bK = ModDimensions.MINING.func_186068_a();
            } else {
                BlockPos playerPos = player.func_180425_c();
                Teleport.teleportToDimension(player, DimensionType.OVERWORLD.func_186068_a(), playerPos.func_177958_n(), playerPos.func_177956_o(), playerPos.func_177952_p());
                player.field_71093_bK = DimensionType.OVERWORLD.func_186068_a();
            }
        }
        return super.func_184645_a(player, hand);
    }

    @Override
    public void func_70071_h_() {
        super.func_70071_h_();
        if (this.field_70128_L) {
            return;
        }
        if (this.attack_delay > 0) {
            --this.attack_delay;
        }
        if (this.attack_delay > 0) {
            return;
        }
        this.attack_delay = 20;
        if (this.field_70170_p.func_175659_aa() == EnumDifficulty.PEACEFUL) {
            return;
        }
        if (OreSpawnMain.PlayNicely != 0) {
            return;
        }
        EntityPlayer e = this.field_70170_p.func_72890_a((Entity)this, 1.5);
        if (e != null) {
            this.func_70652_k((Entity)e);
        }
    }
}

