/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Girlfriend;
import danger.orespawn.MyEntityAINearestAttackableTargetSorter;
import danger.orespawn.MyEntityAITarget;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class MyEntityAINearestAttackableTarget
extends MyEntityAITarget {
    EntityLiving targetEntity;
    Class targetClass;
    int targetChance;
    private final IEntitySelector field_82643_g;
    private MyEntityAINearestAttackableTargetSorter theNearestAttackableTargetSorter;

    public MyEntityAINearestAttackableTarget(EntityLiving par1EntityLiving, Class par2Class, float par3, int par4, boolean par5) {
        this(par1EntityLiving, par2Class, par3, par4, par5, false);
    }

    public MyEntityAINearestAttackableTarget(EntityLiving par1EntityLiving, Class par2Class, float par3, int par4, boolean par5, boolean par6) {
        this(par1EntityLiving, par2Class, par3, par4, par5, par6, (IEntitySelector)null);
    }

    public MyEntityAINearestAttackableTarget(EntityLiving par1, Class par2, float par3, int par4, boolean par5, boolean par6, IEntitySelector par7IEntitySelector) {
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
        if (this.taskOwner instanceof Girlfriend && !((Girlfriend)this.taskOwner).func_70909_n()) {
            return false;
        }
        if (this.taskOwner instanceof Girlfriend && ((Girlfriend)this.taskOwner).func_70906_o()) {
            return false;
        }
        if (this.targetChance > 0 && this.taskOwner.func_70681_au().nextInt(100) > this.targetChance) {
            return false;
        }
        List var5 = this.taskOwner.field_70170_p.func_82733_a(this.targetClass, this.taskOwner.field_70121_D.func_72314_b((double)this.targetDistance, 4.0, (double)this.targetDistance), this.field_82643_g);
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

    public void func_75249_e() {
        this.taskOwner.func_70624_b((EntityLivingBase)this.targetEntity);
        super.func_75249_e();
    }
}

