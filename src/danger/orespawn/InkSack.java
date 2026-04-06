/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.monster.EntityCreeper
 *  net.minecraft.entity.projectile.EntityThrowable
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.AttackSquid;
import danger.orespawn.WaterDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class InkSack
extends EntityThrowable {
    private float my_rotation = 0.0f;
    private int my_index = 65;

    public InkSack(World par1World) {
        super(par1World);
    }

    public InkSack(World par1World, int par2) {
        super(par1World);
    }

    public InkSack(World par1World, EntityLiving par2EntityLiving) {
        super(par1World, (EntityLivingBase)par2EntityLiving);
    }

    public InkSack(World par1World, EntityLiving par2EntityLiving, int par3) {
        super(par1World, (EntityLivingBase)par2EntityLiving);
    }

    public InkSack(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
    }

    public int getInkSackIndex() {
        return this.my_index;
    }

    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (par1MovingObjectPosition.field_72308_g != null) {
            float var2 = 1.0f;
            if (par1MovingObjectPosition.field_72308_g instanceof EntityCreeper) {
                var2 = 4.0f;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof WaterDragon) {
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof AttackSquid) {
                return;
            }
            par1MovingObjectPosition.field_72308_g.func_70097_a(DamageSource.func_76356_a((Entity)this, (Entity)this.func_85052_h()), var2);
            if (par1MovingObjectPosition.field_72308_g instanceof EntityLivingBase && this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                ((EntityLivingBase)par1MovingObjectPosition.field_72308_g).func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, 100 + 50 * this.field_70170_p.field_73012_v.nextInt(8), 0));
            }
        }
        for (int var3 = 0; var3 < 4; ++var3) {
            this.field_70170_p.func_72869_a("smoke", this.field_70165_t + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), this.field_70163_u + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), this.field_70161_v + (double)this.field_70146_Z.nextFloat(), 0.0, 0.0, 0.0);
        }
        this.func_85030_a("random.splash", 0.5f, 1.0f + (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.5f);
        if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
        }
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.my_rotation += 30.0f;
        while (this.my_rotation > 360.0f) {
            this.my_rotation -= 360.0f;
        }
        this.field_70125_A = this.field_70127_C = this.my_rotation;
    }
}

