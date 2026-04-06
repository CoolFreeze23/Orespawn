/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.projectile.EntityFireball
 *  net.minecraft.init.Blocks
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.Dragon;
import danger.orespawn.Godzilla;
import danger.orespawn.GodzillaHead;
import danger.orespawn.Kraken;
import danger.orespawn.Mothra;
import danger.orespawn.MyUtils;
import danger.orespawn.PitchBlack;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BetterFireball
extends EntityFireball {
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private int inTile = 0;
    private boolean inGround = false;
    public EntityLivingBase field_70235_a;
    private int ticksAlive;
    private int ticksInAir = 0;
    public double field_70232_b;
    public double field_70233_c;
    public double field_70230_d;
    public int field_92012_e = 1;
    private int notme = 0;
    private boolean small = false;

    public BetterFireball(World par1World) {
        super(par1World);
        this.func_70105_a(1.0f, 1.0f);
    }

    protected void func_70088_a() {
    }

    public BetterFireball(World par1World, EntityLivingBase par2EntityLiving, double par3, double par5, double par7) {
        super(par1World);
        this.field_70235_a = par2EntityLiving;
        this.func_70105_a(1.0f, 1.0f);
        this.func_70012_b(par2EntityLiving.field_70165_t, par2EntityLiving.field_70163_u, par2EntityLiving.field_70161_v, par2EntityLiving.field_70177_z, par2EntityLiving.field_70125_A);
        this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        this.field_70129_M = 0.0f;
        this.field_70179_y = 0.0;
        this.field_70181_x = 0.0;
        this.field_70159_w = 0.0;
        double var9 = MathHelper.func_76133_a((double)(par3 * par3 + par5 * par5 + par7 * par7));
        this.field_70232_b = par3 / var9 * 0.1;
        this.field_70233_c = par5 / var9 * 0.1;
        this.field_70230_d = par7 / var9 * 0.1;
    }

    public void setNotMe() {
        this.notme = 1;
    }

    public void setBig() {
        this.field_92012_e = 2;
    }

    public void setReallyBig() {
        this.field_92012_e = 4;
    }

    public void setSmall() {
        this.small = true;
        this.func_70105_a(0.3125f, 0.3125f);
    }

    public void func_70071_h_() {
        Vec3 var15 = null;
        Vec3 var2 = null;
        MovingObjectPosition var3 = null;
        Entity var4 = null;
        List var5 = null;
        double var6 = 0.0;
        Entity var9 = null;
        float var10 = 0.3f;
        double var13 = 0.0;
        float var16 = 0.0f;
        float var17 = 0.0f;
        float var18 = 0.0f;
        if (this.ticksAlive >= 600 || this.ticksInAir >= 600) {
            this.func_70106_y();
            return;
        }
        if (!this.field_70170_p.field_72995_K && (this.field_70235_a != null && this.field_70235_a.field_70128_L || !this.field_70170_p.func_72899_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v))) {
            this.func_70106_y();
        } else {
            super.func_70071_h_();
            this.func_70015_d(1);
            if (this.inGround) {
                Block var1 = this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile);
                if (var1 != Blocks.field_150350_a) {
                    ++this.ticksAlive;
                }
                this.inGround = false;
                this.field_70159_w *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_70181_x *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_70179_y *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
            } else {
                ++this.ticksInAir;
            }
            var15 = Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            var2 = Vec3.func_72443_a((double)(this.field_70165_t + this.field_70159_w), (double)(this.field_70163_u + this.field_70181_x), (double)(this.field_70161_v + this.field_70179_y));
            var3 = this.field_70170_p.func_72901_a(var15, var2, false);
            var15 = Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            var2 = Vec3.func_72443_a((double)(this.field_70165_t + this.field_70159_w), (double)(this.field_70163_u + this.field_70181_x), (double)(this.field_70161_v + this.field_70179_y));
            if (var3 != null) {
                var2 = Vec3.func_72443_a((double)var3.field_72307_f.field_72450_a, (double)var3.field_72307_f.field_72448_b, (double)var3.field_72307_f.field_72449_c);
            }
            var4 = null;
            var5 = this.field_70170_p.func_72839_b((Entity)this, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(1.0, 1.0, 1.0));
            var6 = 0.0;
            for (int var8 = 0; var8 < var5.size(); ++var8) {
                AxisAlignedBB var11;
                MovingObjectPosition var12;
                var9 = (Entity)var5.get(var8);
                if (this.field_70235_a == var9) {
                    var3 = null;
                    break;
                }
                if (var9 instanceof BetterFireball) {
                    var3 = null;
                    break;
                }
                if (var9 instanceof GodzillaHead) {
                    var3 = null;
                    break;
                }
                if (MyUtils.isRoyalty(var9)) {
                    var3 = null;
                    break;
                }
                if (this.notme != 0 && (var9 instanceof EntityPlayer || var9 instanceof Dragon || var9 instanceof Mothra)) {
                    var3 = null;
                    break;
                }
                if (!var9.func_70067_L() || var9.func_70028_i((Entity)this.field_70235_a) && this.ticksInAir < 25 || (var12 = (var11 = var9.field_70121_D.func_72314_b((double)var10, (double)var10, (double)var10)).func_72327_a(var15, var2)) == null || !((var13 = var15.func_72438_d(var12.field_72307_f)) < var6) && var6 != 0.0) continue;
                var4 = var9;
                var6 = var13;
            }
            if (var4 != null) {
                var3 = new MovingObjectPosition(var4);
            }
            if (var3 != null) {
                this.func_70227_a(var3);
            }
            this.field_70165_t += this.field_70159_w;
            this.field_70163_u += this.field_70181_x;
            this.field_70161_v += this.field_70179_y;
            var16 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y));
            this.field_70177_z = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) + 90.0f;
            this.field_70125_A = (float)(Math.atan2(var16, this.field_70181_x) * 180.0 / Math.PI) - 90.0f;
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
            var17 = this.func_82341_c();
            if (this.func_70090_H()) {
                for (int var19 = 0; var19 < 4; ++var19) {
                    var18 = 0.25f;
                    this.field_70170_p.func_72869_a("bubble", this.field_70165_t - this.field_70159_w * (double)var18, this.field_70163_u - this.field_70181_x * (double)var18, this.field_70161_v - this.field_70179_y * (double)var18, this.field_70159_w, this.field_70181_x, this.field_70179_y);
                }
                var17 = 0.8f;
            }
            this.field_70159_w += this.field_70232_b;
            this.field_70181_x += this.field_70233_c;
            this.field_70179_y += this.field_70230_d;
            this.field_70159_w *= (double)var17;
            this.field_70181_x *= (double)var17;
            this.field_70179_y *= (double)var17;
            this.field_70170_p.func_72869_a("smoke", this.field_70165_t, this.field_70163_u + 0.5, this.field_70161_v, 0.0, 0.0, 0.0);
            this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        }
    }

    protected void func_70227_a(MovingObjectPosition par1MovingObjectPosition) {
        if (!this.field_70170_p.field_72995_K) {
            if (par1MovingObjectPosition.field_72308_g != null) {
                if (par1MovingObjectPosition.field_72308_g instanceof BetterFireball) {
                    return;
                }
                if (par1MovingObjectPosition.field_72308_g instanceof Mothra) {
                    return;
                }
                if (this.notme != 0 && (par1MovingObjectPosition.field_72308_g instanceof Dragon || par1MovingObjectPosition.field_72308_g instanceof EntityPlayer)) {
                    this.func_70106_y();
                    return;
                }
                Entity e = par1MovingObjectPosition.field_72308_g;
                if (e instanceof EntityLiving) {
                    EntityLiving el = (EntityLiving)e;
                    if (!(!(el.field_70130_N * el.field_70131_O > 30.0f) || MyUtils.isRoyalty((Entity)el) || el instanceof Godzilla || el instanceof GodzillaHead || el instanceof PitchBlack || el instanceof Kraken)) {
                        el.func_70606_j(el.func_110143_aJ() / 2.0f);
                    }
                }
                if (!this.small) {
                    par1MovingObjectPosition.field_72308_g.func_70097_a(DamageSource.func_76362_a((EntityFireball)this, (Entity)this.field_70235_a), 10.0f);
                    par1MovingObjectPosition.field_72308_g.func_70015_d(5);
                } else {
                    par1MovingObjectPosition.field_72308_g.func_70097_a(DamageSource.func_76362_a((EntityFireball)this, (Entity)this.field_70235_a), 5.0f);
                    par1MovingObjectPosition.field_72308_g.func_70015_d(5);
                }
            } else {
                int i = par1MovingObjectPosition.field_72311_b;
                int j = par1MovingObjectPosition.field_72312_c;
                int k = par1MovingObjectPosition.field_72309_d;
                switch (par1MovingObjectPosition.field_72310_e) {
                    case 0: {
                        --j;
                        break;
                    }
                    case 1: {
                        ++j;
                        break;
                    }
                    case 2: {
                        --k;
                        break;
                    }
                    case 3: {
                        ++k;
                        break;
                    }
                    case 4: {
                        --i;
                        break;
                    }
                    case 5: {
                        ++i;
                    }
                }
                if (this.field_70170_p.func_147437_c(i, j, k)) {
                    this.field_70170_p.func_147449_b(i, j, k, (Block)Blocks.field_150480_ab);
                }
            }
            if (!this.small) {
                this.field_70170_p.func_72885_a((Entity)null, this.field_70165_t, this.field_70163_u, this.field_70161_v, (float)this.field_92012_e, true, this.field_70170_p.func_82736_K().func_82766_b("mobGriefing"));
            }
            this.func_70106_y();
        }
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("ExplosionPower", this.field_92012_e);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        if (par1NBTTagCompound.func_74764_b("ExplosionPower")) {
            this.field_92012_e = par1NBTTagCompound.func_74762_e("ExplosionPower");
        }
    }
}

