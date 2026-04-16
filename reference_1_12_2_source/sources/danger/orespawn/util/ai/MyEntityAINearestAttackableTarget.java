/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.EntitySelector
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.passive.EntityTameable
 */
package danger.orespawn.util.ai;

import danger.orespawn.util.ai.MyEntityAINearestAttackableTargetSorter;
import danger.orespawn.util.ai.MyEntityAITarget;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class MyEntityAINearestAttackableTarget
extends MyEntityAITarget {
    EntityLiving targetEntity;
    Class targetClass;
    int targetChance;
    private final EntitySelector field_82643_g;
    private MyEntityAINearestAttackableTargetSorter theNearestAttackableTargetSorter;

    public MyEntityAINearestAttackableTarget(EntityLiving par1EntityLiving, Class par2Class, float par3, int par4, boolean par5) {
        this(par1EntityLiving, par2Class, par3, par4, par5, false);
    }

    public MyEntityAINearestAttackableTarget(EntityLiving par1EntityLiving, Class par2Class, float par3, int par4, boolean par5, boolean par6) {
        this(par1EntityLiving, par2Class, par3, par4, par5, par6, null);
    }

    public MyEntityAINearestAttackableTarget(EntityLiving par1, Class par2, float par3, int par4, boolean par5, boolean par6, EntitySelector par7IEntitySelector) {
        super(par1, par3, par5, par6);
        this.targetClass = par2;
        this.targetDistance = par3;
        this.targetChance = par4;
        this.theNearestAttackableTargetSorter = new MyEntityAINearestAttackableTargetSorter(this, (Entity)par1);
        this.field_82643_g = par7IEntitySelector;
        this.func_75248_a(1);
    }

    public boolean func_75250_a() {
        if (this.taskOwner instanceof EntityTameable && !((EntityTameable)this.taskOwner).func_70909_n()) {
            return false;
        }
        if (this.targetChance > 0 && this.taskOwner.func_70681_au().nextInt(100) > this.targetChance) {
            return false;
        }
        List var5 = this.taskOwner.field_70170_p.func_72872_a(this.targetClass, this.taskOwner.func_174813_aQ().func_72321_a((double)this.targetDistance, 4.0, (double)this.targetDistance));
        Collections.sort(var5, this.theNearestAttackableTargetSorter);
        for (Entity var3 : var5) {
            EntityLiving var4 = (EntityLiving)var3;
            if (!this.isSuitableTarget((EntityLivingBase)var4, false)) continue;
            this.targetEntity = var4;
            return true;
        }
        this.targetEntity = null;
        return false;
    }

    @Override
    public void func_75249_e() {
        this.taskOwner.func_70624_b((EntityLivingBase)this.targetEntity);
        super.func_75249_e();
    }
}

