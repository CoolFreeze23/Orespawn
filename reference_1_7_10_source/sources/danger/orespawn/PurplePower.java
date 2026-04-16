/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.GenericTargetSorter;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class PurplePower
extends EntityLiving {
    private ChunkCoordinates currentFlightTarget = null;
    private GenericTargetSorter TargetSorter = null;
    private int purple_type = 0;

    public PurplePower(World par1World) {
        super(par1World);
        this.func_70105_a(0.75f, 0.75f);
        this.field_70728_aV = 35;
        this.field_70178_ae = true;
        this.field_70174_ab = 25;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70145_X = true;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.25);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(500.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    public void setPurpleType(int par1) {
        if (this.field_70170_p == null) {
            return;
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        this.purple_type = par1;
        this.field_70180_af.func_75692_b(20, (Object)par1);
    }

    public int getPurpleType() {
        return this.field_70180_af.func_75679_c(20);
    }

    protected boolean func_70692_ba() {
        return false;
    }

    protected float func_70599_aP() {
        return 0.75f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return null;
    }

    protected String func_70673_aS() {
        return null;
    }

    public boolean func_70104_M() {
        return false;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    public int mygetMaxHealth() {
        return 1000;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70071_h_() {
        Object e = null;
        super.func_70071_h_();
        this.field_70181_x *= 0.6;
        if (this.getPurpleType() == 0) {
            if (this.field_70170_p.field_72995_K && this.field_70170_p.field_73012_v.nextInt(4) == 1) {
                this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t, this.field_70163_u + 1.25, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 2.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 2.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 2.0f));
            }
        } else if (this.field_70170_p.field_72995_K && this.field_70170_p.field_73012_v.nextInt(6) == 1) {
            this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t, this.field_70163_u + (double)0.65f, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 5.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 5.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 5.0f));
        }
        if (this.field_70170_p.field_72995_K) {
            this.purple_type = this.getPurpleType();
        } else {
            this.setPurpleType(this.purple_type);
        }
        if (!this.field_70170_p.field_72995_K && this.field_70170_p.field_73012_v.nextInt(2500) == 1) {
            if (this.getPurpleType() == 10) {
                this.field_70170_p.func_72885_a((Entity)null, this.field_70165_t, this.field_70163_u + 0.25, this.field_70161_v, 9.1f, true, this.field_70170_p.func_82736_K().func_82766_b("mobGriefing"));
            }
            this.func_70106_y();
        }
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.55), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    protected void func_70619_bc() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        EntityLivingBase e = null;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70146_Z.nextInt(300) == 0 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 2.1f) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                zdir = this.field_70146_Z.nextInt(10) + 8;
                xdir = this.field_70146_Z.nextInt(10) + 8;
                if (this.field_70146_Z.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.field_70146_Z.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t + xdir, (int)this.field_70163_u + this.field_70146_Z.nextInt(20) - 10, (int)this.field_70161_v + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                if (bid == Blocks.field_150350_a && !this.canSeeTarget((double)this.currentFlightTarget.field_71574_a, (double)this.currentFlightTarget.field_71572_b, (double)this.currentFlightTarget.field_71573_c)) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
        } else if (this.field_70146_Z.nextInt(7) == 2 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && (e = this.findSomethingToAttack()) != null) {
            this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + (double)(e.field_70131_O / 2.0f)), (int)e.field_70161_v);
            if (this.func_70068_e((Entity)e) < (double)((4.0f + e.field_70130_N / 2.0f) * (4.0f + e.field_70130_N / 2.0f))) {
                this.func_70652_k((Entity)e);
                this.func_70106_y();
            }
        }
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            this.func_70106_y();
        }
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.4 - this.field_70159_w) * 0.2;
        this.field_70181_x += (Math.signum(var3) * (double)0.7f - this.field_70181_x) * 0.20000000149011612;
        this.field_70179_y += (Math.signum(var5) * 0.4 - this.field_70179_y) * 0.2;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.75f;
        this.field_70177_z += var8 / 4.0f;
    }

    protected boolean func_70041_e_() {
        return false;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_145773_az() {
        return true;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        Entity e = par1DamageSource.func_76346_g();
        float dm = par2;
        if (e != null && e instanceof EntityArrow) {
            return false;
        }
        if (dm > 10.0f) {
            dm = 10.0f;
        }
        ret = super.func_70097_a(par1DamageSource, dm);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + (double)(e.field_70131_O / 2.0f)), (int)e.field_70161_v);
        }
        return ret;
    }

    public boolean func_70601_bi() {
        return true;
    }

    public int func_70658_aO() {
        return 25;
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        EntityTameable e;
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return false;
        }
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (MyUtils.isIgnoreable((EntityLivingBase)par1EntityLiving)) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            if (p.field_71075_bZ.field_75098_d) {
                return false;
            }
            return this.getPurpleType() <= 0 || this.getPurpleType() == 10;
        }
        if (this.getPurpleType() != 0 && this.getPurpleType() != 10 && par1EntityLiving instanceof EntityTameable && (e = (EntityTameable)par1EntityLiving).func_70909_n()) {
            return false;
        }
        return !MyUtils.isRoyalty((Entity)par1EntityLiving);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(32.0, 24.0, 32.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (!this.isSuitableTarget(var4, false)) continue;
            return var4;
        }
        return null;
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean var4 = false;
        if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
            EntityLivingBase e = (EntityLivingBase)par1Entity;
            if (this.getPurpleType() == 0 || this.getPurpleType() == 10) {
                e.func_70606_j(e.func_110143_aJ() / 4.0f - 1.0f);
                var4 = e.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), e.func_110138_aP() / 8.0f);
                if (this.getPurpleType() == 10) {
                    this.field_70170_p.func_72885_a((Entity)null, e.field_70165_t, e.field_70163_u - 0.25, e.field_70161_v, 9.1f, true, this.field_70170_p.func_82736_K().func_82766_b("mobGriefing"));
                }
            } else {
                e.func_70606_j(e.func_110143_aJ() * 15.0f / 16.0f);
                var4 = e.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 5.0f);
                if (this.getPurpleType() == 1) {
                    e.func_70015_d(10);
                }
                if (this.getPurpleType() == 2) {
                    e.func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, 50, 0));
                }
                if (this.getPurpleType() == 3) {
                    e.func_70690_d(new PotionEffect(Potion.field_76437_t.field_76415_H, 50, 0));
                }
            }
        }
        return var4;
    }

    protected Item func_146068_u() {
        return null;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("PurpleType", this.purple_type);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.purple_type = par1NBTTagCompound.func_74762_e("PurpleType");
    }
}

