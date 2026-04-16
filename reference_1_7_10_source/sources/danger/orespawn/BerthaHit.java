/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BerthaHit
extends EntityThrowable {
    private int hit_type = 0;

    public BerthaHit(World par1World) {
        super(par1World);
    }

    public BerthaHit(World par1World, int par2) {
        super(par1World);
    }

    public BerthaHit(World par1World, EntityLivingBase par2EntityLiving) {
        super(par1World, par2EntityLiving);
        this.func_70105_a(0.33f, 0.33f);
        this.func_70012_b(par2EntityLiving.field_70165_t, par2EntityLiving.field_70163_u + (double)par2EntityLiving.func_70047_e(), par2EntityLiving.field_70161_v, par2EntityLiving.field_70177_z, par2EntityLiving.field_70125_A);
        this.field_70165_t -= (double)(MathHelper.func_76134_b((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * 0.16f);
        this.field_70163_u -= 0.1;
        this.field_70161_v -= (double)(MathHelper.func_76126_a((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * 0.16f);
        this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        this.field_70129_M = 0.0f;
        float f = 0.4f;
        this.field_70159_w = -MathHelper.func_76126_a((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(this.field_70125_A / 180.0f * (float)Math.PI)) * f;
        this.field_70179_y = MathHelper.func_76134_b((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(this.field_70125_A / 180.0f * (float)Math.PI)) * f;
        this.field_70181_x = -MathHelper.func_76126_a((float)((this.field_70125_A + this.func_70183_g()) / 180.0f * (float)Math.PI)) * f;
        this.func_70186_c(this.field_70159_w, this.field_70181_x, this.field_70179_y, this.func_70182_d(), 0.1f);
    }

    public BerthaHit(World par1World, EntityLivingBase par2EntityLiving, int par3) {
        super(par1World, par2EntityLiving);
    }

    public BerthaHit(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
    }

    public void setHitType(int i) {
        this.hit_type = i;
    }

    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (this.field_70128_L) {
            return;
        }
        if (par1MovingObjectPosition.field_72308_g != null && this.func_85052_h() != null) {
            float f3;
            double inair;
            EntityTameable t;
            Entity e = par1MovingObjectPosition.field_72308_g;
            if (OreSpawnMain.big_bertha_pvp == 0 && e instanceof EntityPlayer || e instanceof Girlfriend || e instanceof Boyfriend) {
                this.func_70106_y();
                return;
            }
            if (OreSpawnMain.big_bertha_pvp == 0 && e instanceof EntityTameable && (t = (EntityTameable)e).func_70909_n()) {
                this.func_70106_y();
                return;
            }
            if (this.hit_type == 0 && this.func_70068_e((Entity)this.func_85052_h()) < 81.0 && e != this.func_85052_h()) {
                e.func_70097_a(DamageSource.func_76365_a((EntityPlayer)((EntityPlayer)this.func_85052_h())), (float)OreSpawnMain.bertha_stats.damage);
                e.func_70015_d(10);
                double ks = 2.25;
                inair = 0.35;
                f3 = (float)Math.atan2(e.field_70161_v - this.func_85052_h().field_70161_v, e.field_70165_t - this.func_85052_h().field_70165_t);
                if (e.field_70128_L) {
                    inair *= 2.0;
                }
                e.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            if (this.hit_type == 2 && this.func_70068_e((Entity)this.func_85052_h()) < 101.0 && e != this.func_85052_h()) {
                e.func_70097_a(DamageSource.func_76365_a((EntityPlayer)((EntityPlayer)this.func_85052_h())), (float)OreSpawnMain.royal_stats.damage);
                double ks = 1.5;
                inair = 0.25;
                f3 = (float)Math.atan2(e.field_70161_v - this.func_85052_h().field_70161_v, e.field_70165_t - this.func_85052_h().field_70165_t);
                if (e.field_70128_L) {
                    inair *= 2.0;
                }
                e.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            if (this.hit_type == 3 && this.func_70068_e((Entity)this.func_85052_h()) < 64.0 && e != this.func_85052_h()) {
                e.func_70097_a(DamageSource.func_76365_a((EntityPlayer)((EntityPlayer)this.func_85052_h())), (float)OreSpawnMain.hammy_stats.damage);
                double ks = 1.25;
                inair = 0.65;
                f3 = (float)Math.atan2(e.field_70161_v - this.func_85052_h().field_70161_v, e.field_70165_t - this.func_85052_h().field_70165_t);
                if (e.field_70128_L) {
                    inair *= 2.0;
                }
                e.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
                if (!this.field_70170_p.field_72995_K && this.hit_type == 3 && this.func_70068_e((Entity)this.func_85052_h()) < 64.0) {
                    this.field_70170_p.func_72885_a((Entity)null, this.field_70165_t, this.field_70163_u, this.field_70161_v, 1.5f, true, this.field_70170_p.func_82736_K().func_82766_b("mobGriefing"));
                }
            }
        } else if (!this.field_70170_p.field_72995_K && this.hit_type == 3 && this.func_70068_e((Entity)this.func_85052_h()) < 64.0) {
            this.field_70170_p.func_72885_a((Entity)null, this.field_70165_t, this.field_70163_u, this.field_70161_v, 2.1f, true, this.field_70170_p.func_82736_K().func_82766_b("mobGriefing"));
        }
        this.func_70106_y();
    }

    public void func_70071_h_() {
        super.func_70071_h_();
    }
}

