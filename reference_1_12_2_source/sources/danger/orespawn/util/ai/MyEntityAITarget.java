/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.monster.EntityCreeper
 *  net.minecraft.entity.monster.EntityEnderman
 *  net.minecraft.entity.monster.EntityGhast
 *  net.minecraft.entity.monster.EntityPigZombie
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.pathfinding.Path
 *  net.minecraft.pathfinding.PathPoint
 *  net.minecraft.util.math.MathHelper
 */
package danger.orespawn.util.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.MathHelper;

public abstract class MyEntityAITarget
extends EntityAIBase {
    protected EntityLiving taskOwner;
    protected float targetDistance;
    protected boolean shouldCheckSight;
    private boolean field_75303_a;
    private int field_75301_b = 0;
    private int field_75302_c = 0;
    private int field_75298_g = 0;

    public MyEntityAITarget(EntityLiving par1EntityLiving, float par2, boolean par3) {
        this(par1EntityLiving, par2, par3, false);
    }

    public MyEntityAITarget(EntityLiving par1EntityLiving, float par2, boolean par3, boolean par4) {
        this.taskOwner = par1EntityLiving;
        this.targetDistance = par2;
        this.shouldCheckSight = par3;
        this.field_75303_a = par4;
    }

    public boolean continueExecuting() {
        EntityLivingBase var1 = this.taskOwner.func_70638_az();
        if (var1 == null) {
            return false;
        }
        if (!var1.func_70089_S()) {
            this.taskOwner.func_70624_b(null);
            return false;
        }
        if (this.taskOwner.func_70068_e((Entity)var1) > (double)(this.targetDistance * this.targetDistance)) {
            return false;
        }
        if (this.taskOwner instanceof EntityTameable && ((EntityTameable)this.taskOwner).func_70909_n() && var1 instanceof EntityTameable && ((EntityTameable)var1).func_70909_n()) {
            return false;
        }
        if (this.shouldCheckSight) {
            if (this.taskOwner.func_70635_at().func_75522_a((Entity)var1)) {
                this.field_75298_g = 0;
            } else if (++this.field_75298_g > 60) {
                return false;
            }
        }
        return true;
    }

    public void func_75249_e() {
        this.field_75301_b = 0;
        this.field_75302_c = 0;
        this.field_75298_g = 0;
    }

    public void func_75251_c() {
        this.taskOwner.func_70624_b((EntityLivingBase)((EntityLiving)null));
    }

    protected boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this.taskOwner) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (this.taskOwner instanceof EntityTameable && ((EntityTameable)this.taskOwner).func_70909_n()) {
            if (par1EntityLiving instanceof EntityTameable && ((EntityTameable)par1EntityLiving).func_70909_n()) {
                return false;
            }
            if (par1EntityLiving == ((EntityTameable)this.taskOwner).func_70902_q()) {
                return false;
            }
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPigZombie) {
            return false;
        }
        if (par1EntityLiving instanceof EntityEnderman) {
            return false;
        }
        if (this.shouldCheckSight && !this.taskOwner.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityCreeper) {
            return true;
        }
        if (par1EntityLiving instanceof EntityGhast) {
            return true;
        }
        if (this.field_75303_a) {
            if (--this.field_75302_c <= 0) {
                this.field_75301_b = 0;
            }
            if (this.field_75301_b == 0) {
                int n = this.field_75301_b = this.func_75295_a(par1EntityLiving) ? 1 : 2;
            }
            if (this.field_75301_b == 2) {
                return false;
            }
        }
        return true;
    }

    private boolean func_75295_a(EntityLivingBase par1EntityLiving) {
        int var5;
        this.field_75302_c = 10 + this.taskOwner.func_70681_au().nextInt(5);
        Path var2 = this.taskOwner.func_70661_as().func_75494_a((Entity)par1EntityLiving);
        if (var2 == null) {
            return false;
        }
        PathPoint var3 = var2.func_75870_c();
        if (var3 == null) {
            return false;
        }
        int var4 = var3.field_75839_a - MathHelper.func_76128_c((double)par1EntityLiving.field_70165_t);
        return (double)(var4 * var4 + (var5 = var3.field_75838_c - MathHelper.func_76128_c((double)par1EntityLiving.field_70161_v)) * var5) <= 2.25;
    }
}

