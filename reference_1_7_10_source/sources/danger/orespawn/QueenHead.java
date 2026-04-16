/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.TheQueen;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class QueenHead
extends EntityLiving {
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public QueenHead(World par1World) {
        super(par1World);
        this.func_70105_a(19.9f, 10.0f);
        this.field_70145_X = true;
        this.field_70174_ab = 10000;
        this.field_70178_ae = true;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)OreSpawnMain.TheQueen_stats.health);
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)1.33f);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected boolean func_70692_ba() {
        return false;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    protected boolean canTriggerWalQueen() {
        return false;
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    public boolean func_70104_M() {
        return true;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return false;
        }
        Entity e = par1DamageSource.func_76346_g();
        if (e != null && (e instanceof TheQueen || e instanceof QueenHead)) {
            return false;
        }
        e = par1DamageSource.func_76364_f();
        if (e != null && (e instanceof TheQueen || e instanceof QueenHead)) {
            return false;
        }
        List var5 = this.field_70170_p.func_72872_a(TheQueen.class, this.field_70121_D.func_72314_b(48.0, 32.0, 48.0));
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        TheQueen var4 = null;
        if (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (TheQueen)var3;
            ret = var4.func_70097_a(par1DamageSource, par2);
        }
        return ret;
    }

    public boolean func_70067_L() {
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70056_a(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.boatPosRotationIncrements = this.field_70153_n != null ? par9 + 8 : 6;
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
        this.field_70159_w = this.velocityX;
        this.field_70181_x = this.velocityY;
        this.field_70179_y = this.velocityZ;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70016_h(double par1, double par3, double par5) {
        this.velocityX = this.field_70159_w = par1;
        this.velocityY = this.field_70181_x = par3;
        this.velocityZ = this.field_70179_y = par5;
    }

    public void func_70071_h_() {
        if (this.field_70128_L) {
            return;
        }
        this.field_70160_al = true;
        this.func_70015_d(0);
        if (this.field_70170_p.field_72995_K) {
            if (this.boatPosRotationIncrements > 0) {
                double d4 = this.field_70165_t + (this.boatX - this.field_70165_t) / (double)this.boatPosRotationIncrements;
                double d5 = this.field_70163_u + (this.boatY - this.field_70163_u) / (double)this.boatPosRotationIncrements;
                double d11 = this.field_70161_v + (this.boatZ - this.field_70161_v) / (double)this.boatPosRotationIncrements;
                this.func_70107_b(d4, d5, d11);
                this.field_70125_A = (float)((double)this.field_70125_A + (this.boatPitch - (double)this.field_70125_A) / (double)this.boatPosRotationIncrements);
                double d10 = MathHelper.func_76138_g((double)(this.boatYaw - (double)this.field_70177_z));
                if (this.field_70153_n != null) {
                    d10 = MathHelper.func_76138_g((double)((double)this.field_70153_n.field_70177_z - (double)this.field_70177_z));
                }
                this.field_70177_z = (float)((double)this.field_70177_z + d10 / (double)this.boatPosRotationIncrements);
                this.func_70101_b(this.field_70177_z, this.field_70125_A);
                --this.boatPosRotationIncrements;
            }
        } else {
            List var5 = this.field_70170_p.func_72872_a(TheQueen.class, this.field_70121_D.func_72314_b(32.0, 32.0, 32.0));
            Iterator var2 = var5.iterator();
            Entity var3 = null;
            TheQueen var4 = null;
            if (var2.hasNext()) {
                var3 = (Entity)var2.next();
                var4 = (TheQueen)var3;
                this.field_70163_u = var4.field_70163_u + 12.0;
                this.field_70165_t = var4.field_70165_t - 30.0 * Math.sin(Math.toRadians(var4.field_70759_as));
                this.field_70161_v = var4.field_70161_v + 30.0 * Math.cos(Math.toRadians(var4.field_70759_as));
                this.field_70177_z = var4.field_70177_z;
                this.field_70759_as = var4.field_70759_as;
                this.field_70159_w = var4.field_70159_w;
                this.field_70181_x = var4.field_70181_x;
                this.field_70179_y = var4.field_70179_y;
                this.func_70606_j(var4.func_110143_aJ());
            } else {
                this.func_70106_y();
            }
        }
    }
}

