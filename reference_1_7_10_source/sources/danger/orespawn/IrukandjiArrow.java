/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Cephadrome;
import danger.orespawn.Dragon;
import danger.orespawn.Elevator;
import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class IrukandjiArrow
extends EntityArrow {
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private int inTile = 0;
    private int inData = 0;
    private boolean inGround = false;
    private int ticksInGround;
    private int ticksInAir = 0;
    private int knockbackStrength;

    public IrukandjiArrow(World par1World) {
        super(par1World);
    }

    public IrukandjiArrow(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
    }

    public IrukandjiArrow(World par1World, EntityLiving par2EntityLiving, EntityLivingBase par3EntityLiving, float par4, float par5) {
        super(par1World, (EntityLivingBase)par2EntityLiving, par3EntityLiving, par4, par5);
    }

    public IrukandjiArrow(World par1World, EntityPlayer par3EntityPlayer, float par3) {
        super(par1World, (EntityLivingBase)par3EntityPlayer, par3);
    }

    protected void func_70088_a() {
        this.field_70180_af.func_75682_a(16, (Object)0);
    }

    public void func_70186_c(double par1, double par3, double par5, float par7, float par8) {
        float var9 = MathHelper.func_76133_a((double)(par1 * par1 + par3 * par3 + par5 * par5));
        par1 /= (double)var9;
        par3 /= (double)var9;
        par5 /= (double)var9;
        this.field_70159_w = par1 *= (double)par7;
        this.field_70181_x = par3 *= (double)par7;
        this.field_70179_y = par5 *= (double)par7;
        float var10 = MathHelper.func_76133_a((double)(par1 * par1 + par5 * par5));
        this.field_70126_B = this.field_70177_z = (float)(Math.atan2(par1, par5) * 180.0 / Math.PI);
        this.field_70127_C = this.field_70125_A = (float)(Math.atan2(par3, var10) * 180.0 / Math.PI);
        this.ticksInGround = 0;
    }

    public void func_70071_h_() {
        Block var16;
        super.func_70030_z();
        if (this.field_70127_C == 0.0f && this.field_70126_B == 0.0f) {
            float var1 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y));
            this.field_70126_B = this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0 / Math.PI);
            this.field_70127_C = this.field_70125_A = (float)(Math.atan2(this.field_70181_x, var1) * 180.0 / Math.PI);
        }
        if ((var16 = this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile)) != Blocks.field_150350_a) {
            var16.func_149719_a((IBlockAccess)this.field_70170_p, this.xTile, this.yTile, this.zTile);
            AxisAlignedBB var2 = var16.func_149668_a(this.field_70170_p, this.xTile, this.yTile, this.zTile);
            if (var2 != null && var2.func_72318_a(Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v))) {
                this.inGround = true;
            }
        }
        if (this.field_70249_b > 0) {
            --this.field_70249_b;
        }
        if (this.inGround) {
            Block var18 = this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile);
            int var19 = this.field_70170_p.func_72805_g(this.xTile, this.yTile, this.zTile);
            if (var18 != Blocks.field_150350_a && var19 == this.inData) {
                ++this.ticksInGround;
                if (this.ticksInGround == 50 && !this.field_70170_p.field_72995_K) {
                    this.func_145779_a(OreSpawnMain.MyIrukandjiArrow, 1);
                    this.func_70106_y();
                }
            } else {
                this.inGround = false;
                this.field_70159_w *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_70181_x *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_70179_y *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        } else {
            float var11;
            int var9;
            ++this.ticksInAir;
            Vec3 var17 = Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            Vec3 var3 = Vec3.func_72443_a((double)(this.field_70165_t + this.field_70159_w), (double)(this.field_70163_u + this.field_70181_x), (double)(this.field_70161_v + this.field_70179_y));
            MovingObjectPosition var4 = this.field_70170_p.func_72901_a(var17, var3, true);
            var17 = Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            var3 = Vec3.func_72443_a((double)(this.field_70165_t + this.field_70159_w), (double)(this.field_70163_u + this.field_70181_x), (double)(this.field_70161_v + this.field_70179_y));
            if (var4 != null) {
                var3 = Vec3.func_72443_a((double)var4.field_72307_f.field_72450_a, (double)var4.field_72307_f.field_72448_b, (double)var4.field_72307_f.field_72449_c);
            }
            Entity var5 = null;
            List var6 = this.field_70170_p.func_72839_b((Entity)this, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(1.0, 1.0, 1.0));
            double var7 = 0.0;
            for (var9 = 0; var9 < var6.size(); ++var9) {
                double var14;
                AxisAlignedBB var12;
                MovingObjectPosition var13;
                Cephadrome c;
                Entity var10 = (Entity)var6.get(var9);
                if (!var10.func_70067_L() || var10 == this.field_70250_c && this.ticksInAir < 5 || var10 instanceof Elevator) continue;
                if (var10 instanceof Cephadrome) {
                    c = (Cephadrome)var10;
                    if (c.field_70153_n != null) continue;
                }
                if (var10 instanceof Dragon) {
                    c = (Dragon)var10;
                    if (c.field_70153_n != null) continue;
                }
                if (var10 instanceof EntityHorse) {
                    c = (EntityHorse)var10;
                    if (c.field_70153_n != null) continue;
                }
                if ((var13 = (var12 = var10.field_70121_D.func_72314_b((double)(var11 = 0.3f), (double)var11, (double)var11)).func_72327_a(var17, var3)) == null || !((var14 = var17.func_72438_d(var13.field_72307_f)) < var7) && var7 != 0.0) continue;
                var5 = var10;
                var7 = var14;
            }
            if (var5 != null) {
                var4 = new MovingObjectPosition(var5);
            }
            if (var4 != null) {
                if (var4.field_72308_g != null) {
                    float var23 = 100.0f;
                    if (OreSpawnMain.ultimate_sword_pvp == 0) {
                        EntityTameable t;
                        if (var4.field_72308_g instanceof EntityPlayer || var4.field_72308_g instanceof Girlfriend || var4.field_72308_g instanceof Boyfriend) {
                            EntityLivingBase e = (EntityLivingBase)var4.field_72308_g;
                            this.func_85030_a("random.bowhit", 1.0f, 1.2f / (this.field_70146_Z.nextFloat() * 0.2f + 0.9f));
                            this.func_70106_y();
                            return;
                        }
                        if (var4.field_72308_g instanceof EntityTameable && (t = (EntityTameable)var4.field_72308_g).func_70909_n()) {
                            this.func_85030_a("random.bowhit", 1.0f, 1.2f / (this.field_70146_Z.nextFloat() * 0.2f + 0.9f));
                            this.func_70106_y();
                            return;
                        }
                    }
                    if (this.func_70241_g()) {
                        var23 += (float)this.field_70146_Z.nextInt((int)var23 / 2 + 2);
                    }
                    DamageSource var21 = null;
                    var21 = this.field_70250_c == null ? DamageSource.func_76353_a((EntityArrow)this, (Entity)this) : DamageSource.func_76353_a((EntityArrow)this, (Entity)this.field_70250_c);
                    if (this.func_70027_ad()) {
                        var4.field_72308_g.func_70015_d(5);
                    }
                    if (var4.field_72308_g.func_70097_a(var21, var23)) {
                        if (var4.field_72308_g instanceof EntityLiving) {
                            float var26;
                            EntityLiving var24 = (EntityLiving)var4.field_72308_g;
                            if (!this.field_70170_p.field_72995_K) {
                                var24.func_85034_r(var24.func_85035_bI() + 1);
                            }
                            if (this.knockbackStrength > 0 && (var26 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y))) > 0.0f) {
                                var4.field_72308_g.func_70024_g(this.field_70159_w * (double)this.knockbackStrength * (double)0.6f / (double)var26, 0.1, this.field_70179_y * (double)this.knockbackStrength * (double)0.6f / (double)var26);
                            }
                            if (this.field_70250_c != null && var4.field_72308_g != this.field_70250_c && var4.field_72308_g instanceof EntityPlayer && this.field_70250_c instanceof EntityPlayerMP) {
                                ((EntityPlayerMP)this.field_70250_c).field_71135_a.func_147359_a((Packet)new S2BPacketChangeGameState(6, 0.0f));
                            }
                        }
                        this.func_85030_a("random.bowhit", 1.0f, 1.2f / (this.field_70146_Z.nextFloat() * 0.2f + 0.9f));
                        this.func_70106_y();
                    } else {
                        this.field_70159_w *= -0.10000000149;
                        this.field_70181_x *= -0.10000000149;
                        this.field_70179_y *= -0.10000000149;
                        this.field_70177_z += 180.0f;
                        this.field_70126_B += 180.0f;
                        this.ticksInAir = 0;
                    }
                } else {
                    this.xTile = var4.field_72311_b;
                    this.yTile = var4.field_72312_c;
                    this.zTile = var4.field_72309_d;
                    this.inData = this.field_70170_p.func_72805_g(this.xTile, this.yTile, this.zTile);
                    this.field_70159_w = (float)(var4.field_72307_f.field_72450_a - this.field_70165_t);
                    this.field_70181_x = (float)(var4.field_72307_f.field_72448_b - this.field_70163_u);
                    this.field_70179_y = (float)(var4.field_72307_f.field_72449_c - this.field_70161_v);
                    float var20 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y));
                    this.field_70165_t -= this.field_70159_w / (double)var20 * 0.05;
                    this.field_70163_u -= this.field_70181_x / (double)var20 * 0.05;
                    this.field_70161_v -= this.field_70179_y / (double)var20 * 0.05;
                    this.func_85030_a("random.bowhit", 1.0f, 1.2f / (this.field_70146_Z.nextFloat() * 0.2f + 0.9f));
                    this.inGround = true;
                    this.field_70249_b = 7;
                    this.func_70243_d(false);
                }
            }
            if (this.func_70241_g()) {
                for (var9 = 0; var9 < 4; ++var9) {
                    this.field_70170_p.func_72869_a("crit", this.field_70165_t + this.field_70159_w * (double)var9 / 4.0, this.field_70163_u + this.field_70181_x * (double)var9 / 4.0, this.field_70161_v + this.field_70179_y * (double)var9 / 4.0, -this.field_70159_w, -this.field_70181_x + 0.2, -this.field_70179_y);
                }
            }
            this.field_70165_t += this.field_70159_w;
            this.field_70163_u += this.field_70181_x;
            this.field_70161_v += this.field_70179_y;
            float var20 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y));
            this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0 / Math.PI);
            this.field_70125_A = (float)(Math.atan2(this.field_70181_x, var20) * 180.0 / Math.PI);
            while (this.field_70125_A - this.field_70127_C < -180.0f) {
                this.field_70127_C -= 360.0f;
            }
            while (this.field_70125_A - this.field_70127_C >= 180.0f) {
                this.field_70127_C += 360.0f;
            }
            while (this.field_70177_z - this.field_70126_B < -180.0f) {
                this.field_70126_B -= 360.0f;
            }
            while (this.field_70177_z - this.field_70126_B >= 180.0f) {
                this.field_70126_B += 360.0f;
            }
            this.field_70125_A = this.field_70127_C + (this.field_70125_A - this.field_70127_C) * 0.2f;
            this.field_70177_z = this.field_70126_B + (this.field_70177_z - this.field_70126_B) * 0.2f;
            float var22 = 0.99f;
            var11 = 0.05f;
            if (this.func_70090_H()) {
                for (int var25 = 0; var25 < 4; ++var25) {
                    float var26 = 0.25f;
                    this.field_70170_p.func_72869_a("bubble", this.field_70165_t - this.field_70159_w * (double)var26, this.field_70163_u - this.field_70181_x * (double)var26, this.field_70161_v - this.field_70179_y * (double)var26, this.field_70159_w, this.field_70181_x, this.field_70179_y);
                }
                var22 = 0.8f;
            }
            this.field_70159_w *= (double)var22;
            this.field_70181_x *= (double)var22;
            this.field_70179_y *= (double)var22;
            this.field_70181_x -= (double)var11;
            this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            this.func_145775_I();
        }
    }

    public void func_70240_a(int par1) {
        this.knockbackStrength = par1;
    }

    public void func_70239_b(double par1) {
    }

    public double func_70242_d() {
        return 100.0;
    }
}

