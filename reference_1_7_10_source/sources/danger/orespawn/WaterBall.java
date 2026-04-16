/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.AttackSquid;
import danger.orespawn.Dragon;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.WaterDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class WaterBall
extends EntityThrowable {
    private float my_rotation = 0.0f;
    private int my_index = 49;

    public WaterBall(World par1World) {
        super(par1World);
    }

    public WaterBall(World par1World, EntityLivingBase par2EntityLiving) {
        super(par1World, par2EntityLiving);
    }

    public WaterBall(World worldObj, double d, double e, double f) {
        super(worldObj, d, e, f);
    }

    public int getWaterBallIndex() {
        return this.my_index;
    }

    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (par1MovingObjectPosition.field_72308_g != null) {
            Dragon d;
            float var2 = 2.0f;
            if (par1MovingObjectPosition.field_72308_g instanceof EntityCreeper) {
                var2 = 5.0f;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof WaterDragon) {
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof AttackSquid) {
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Dragon && (d = (Dragon)par1MovingObjectPosition.field_72308_g).getDragonType() != 0) {
                return;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityPlayer) {
                d = (EntityPlayer)par1MovingObjectPosition.field_72308_g;
                if (d.field_70154_o != null) {
                    return;
                }
            }
            par1MovingObjectPosition.field_72308_g.func_70097_a(DamageSource.func_76356_a((Entity)this, (Entity)this.func_85052_h()), var2);
            if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
                par1MovingObjectPosition.field_72308_g.func_145779_a(OreSpawnMain.MyWaterBall, 1);
            }
            par1MovingObjectPosition.field_72308_g.func_70066_B();
        }
        for (int var3 = 0; var3 < 8; ++var3) {
            this.field_70170_p.func_72869_a("bubble", this.field_70165_t + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), this.field_70163_u + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), this.field_70161_v + (double)this.field_70146_Z.nextFloat(), 0.0, 0.0, 0.0);
            this.field_70170_p.func_72869_a("splash", this.field_70165_t + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), this.field_70163_u + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), this.field_70161_v + (double)this.field_70146_Z.nextFloat() - (double)this.field_70146_Z.nextFloat(), 0.0, 0.0, 0.0);
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
        this.field_70170_p.func_72869_a("splash", this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0, 0.0, 0.0);
    }
}

