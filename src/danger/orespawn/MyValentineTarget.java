/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.command.IEntitySelector
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 */
package danger.orespawn;

import danger.orespawn.Girlfriend;
import danger.orespawn.MyEntityAITarget;
import danger.orespawn.MyValentineTargetSorter;
import danger.orespawn.OreSpawnMain;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class MyValentineTarget
extends MyEntityAITarget {
    EntityLivingBase targetEntity;
    EntityLivingBase Me;
    Class targetClass;
    int targetChance;
    private final IEntitySelector field_82643_g;
    private MyValentineTargetSorter theNearestAttackableTargetSorter;

    public MyValentineTarget(EntityLiving par1EntityLiving, Class par2Class, float par3, int par4, boolean par5) {
        this(par1EntityLiving, par2Class, par3, par4, par5, false);
        this.Me = par1EntityLiving;
    }

    public MyValentineTarget(EntityLiving par1EntityLiving, Class par2Class, float par3, int par4, boolean par5, boolean par6) {
        this(par1EntityLiving, par2Class, par3, par4, par5, par6, null);
        this.Me = par1EntityLiving;
    }

    public MyValentineTarget(EntityLiving par1, Class par2, float par3, int par4, boolean par5, boolean par6, IEntitySelector par7IEntitySelector) {
        super(par1, par3, par5, par6);
        this.targetClass = par2;
        this.targetDistance = par3;
        this.targetChance = par4;
        this.theNearestAttackableTargetSorter = new MyValentineTargetSorter(this, (Entity)par1);
        this.field_82643_g = par7IEntitySelector;
        this.func_75248_a(1);
        this.Me = par1;
    }

    public boolean func_75250_a() {
        if (OreSpawnMain.valentines_day == 0) {
            return false;
        }
        if (this.Me != null && this.Me instanceof Girlfriend) {
            Girlfriend gf = (Girlfriend)this.Me;
            if (gf.feelingBetter != 0) {
                return false;
            }
        }
        if (this.targetChance > 0 && this.taskOwner.func_70681_au().nextInt(100) > this.targetChance) {
            return false;
        }
        List var5 = this.taskOwner.field_70170_p.func_82733_a(this.targetClass, this.taskOwner.field_70121_D.func_72314_b((double)this.targetDistance, 4.0, (double)this.targetDistance), this.field_82643_g);
        Collections.sort(var5, this.theNearestAttackableTargetSorter);
        for (Entity var3 : var5) {
            EntityLivingBase var4;
            if (!(var3 instanceof EntityLivingBase) || !this.isSuitableTarget(var4 = (EntityLivingBase)var3, false)) continue;
            this.targetEntity = var4;
            return true;
        }
        this.targetEntity = null;
        return false;
    }

    @Override
    public void func_75249_e() {
        this.taskOwner.func_70624_b(this.targetEntity);
        super.func_75249_e();
    }
}

