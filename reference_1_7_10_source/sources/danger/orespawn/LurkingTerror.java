/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Bee;
import danger.orespawn.CloudShark;
import danger.orespawn.CreepingHorror;
import danger.orespawn.Dragon;
import danger.orespawn.EnderReaper;
import danger.orespawn.EntityButterfly;
import danger.orespawn.Firefly;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Island;
import danger.orespawn.IslandToo;
import danger.orespawn.LeafMonster;
import danger.orespawn.Mantis;
import danger.orespawn.Mothra;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.PitchBlack;
import danger.orespawn.RenderInfo;
import danger.orespawn.RockBase;
import danger.orespawn.Rotator;
import danger.orespawn.TerribleTerror;
import danger.orespawn.Triffid;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class LurkingTerror
extends EntityMob {
    private ChunkCoordinates currentFlightTarget = null;
    private GenericTargetSorter TargetSorter = null;
    private RenderInfo renderdata = new RenderInfo();

    public LurkingTerror(World par1World) {
        super(par1World);
        this.func_70105_a(1.75f, 1.25f);
        this.func_70661_as().func_75491_a(false);
        this.field_70728_aV = 20;
        this.field_70178_ae = false;
        this.field_70174_ab = 5;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.25);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.LurkingTerror_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
        if (this.renderdata == null) {
            this.renderdata = new RenderInfo();
        }
        this.renderdata.rf1 = 0.0f;
        this.renderdata.rf2 = 0.0f;
        this.renderdata.rf3 = 0.0f;
        this.renderdata.rf4 = 0.0f;
        this.renderdata.ri1 = 0;
        this.renderdata.ri2 = 0;
        this.renderdata.ri3 = 0;
        this.renderdata.ri4 = 0;
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        return this.getAttacking() == 0;
    }

    public int getAttacking() {
        return this.field_70180_af.func_75683_a(20);
    }

    public void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    public void setRenderInfo(RenderInfo r) {
        this.renderdata.rf1 = r.rf1;
        this.renderdata.rf2 = r.rf2;
        this.renderdata.rf3 = r.rf3;
        this.renderdata.rf4 = r.rf4;
        this.renderdata.ri1 = r.ri1;
        this.renderdata.ri2 = r.ri2;
        this.renderdata.ri3 = r.ri3;
        this.renderdata.ri4 = r.ri4;
    }

    protected float func_70599_aP() {
        return 0.55f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected String func_70639_aQ() {
        return "orespawn:lurkinghorror_living";
    }

    protected String func_70621_aR() {
        return "orespawn:lurkinghorror_hit";
    }

    protected String func_70673_aS() {
        return "orespawn:lurkinghorror_dead";
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.LurkingTerror_stats.health;
    }

    public int func_70658_aO() {
        return OreSpawnMain.LurkingTerror_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= 0.6;
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 5.0f);
        return var4;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.75), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    protected void func_70619_bc() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70146_Z.nextInt(120) == 0 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 2.1f) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                zdir = this.field_70146_Z.nextInt(10) + 2;
                xdir = this.field_70146_Z.nextInt(10) + 2;
                if (this.field_70146_Z.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.field_70146_Z.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t + xdir, (int)this.field_70163_u + this.field_70146_Z.nextInt(5) - 2, (int)this.field_70161_v + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                if (bid == Blocks.field_150350_a && !this.canSeeTarget((double)this.currentFlightTarget.field_71574_a, (double)this.currentFlightTarget.field_71572_b, (double)this.currentFlightTarget.field_71573_c)) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
        } else if (this.field_70146_Z.nextInt(9) == 0) {
            EntityLivingBase e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setAttacking(1);
                this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + 1.0), (int)e.field_70161_v);
                if (this.func_70068_e((Entity)e) < 6.0) {
                    this.func_70652_k((Entity)e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.4 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.4 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.4 - this.field_70159_w) * 0.30000000149011613;
        this.field_70181_x += (Math.signum(var3) * (double)0.7f - this.field_70181_x) * 0.20000000149011612;
        this.field_70179_y += (Math.signum(var5) * 0.4 - this.field_70179_y) * 0.30000000149011613;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.75f;
        this.field_70177_z += var8 / 4.0f;
    }

    protected boolean func_70041_e_() {
        return true;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_145773_az() {
        return false;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = super.func_70097_a(par1DamageSource, par2);
        Entity e = par1DamageSource.func_76346_g();
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)e.field_70163_u, (int)e.field_70161_v);
        }
        return ret;
    }

    public boolean func_70601_bi() {
        LurkingTerror target = null;
        for (int k = -2; k < 2; ++k) {
            for (int j = -2; j < 2; ++j) {
                for (int i = 0; i < 5; ++i) {
                    Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Lurking Terror")) continue;
                    return true;
                }
            }
        }
        if (!this.func_70814_o()) {
            return false;
        }
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70170_p.field_73012_v.nextInt(2) != 1) {
            return false;
        }
        if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 && this.field_70170_p.field_73012_v.nextInt(6) != 0) {
            return false;
        }
        target = (LurkingTerror)this.field_70170_p.func_72857_a(LurkingTerror.class, this.field_70121_D.func_72314_b(32.0, 16.0, 32.0), (Entity)this);
        if (target != null) {
            return false;
        }
        return !(this.field_70163_u < 10.0);
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof LurkingTerror) {
            return false;
        }
        if (par1EntityLiving instanceof RockBase) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (par1EntityLiving instanceof LeafMonster) {
            return false;
        }
        if (par1EntityLiving instanceof TerribleTerror) {
            return false;
        }
        if (par1EntityLiving instanceof Mothra) {
            return false;
        }
        if (par1EntityLiving instanceof CloudShark) {
            return false;
        }
        if (par1EntityLiving instanceof Rotator) {
            return false;
        }
        if (par1EntityLiving instanceof Bee) {
            return false;
        }
        if (par1EntityLiving instanceof Mantis) {
            return false;
        }
        if (par1EntityLiving instanceof CreepingHorror) {
            return false;
        }
        if (par1EntityLiving instanceof Triffid) {
            return false;
        }
        if (par1EntityLiving instanceof PitchBlack) {
            return false;
        }
        if (par1EntityLiving instanceof Dragon) {
            return false;
        }
        if (par1EntityLiving instanceof Island) {
            return false;
        }
        if (par1EntityLiving instanceof IslandToo) {
            return false;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return false;
        }
        if (par1EntityLiving instanceof Firefly) {
            return false;
        }
        if (par1EntityLiving instanceof Triffid) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            if (p.field_71075_bZ.field_75098_d) {
                return false;
            }
        }
        return true;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(12.0, 8.0, 12.0));
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

    protected Item func_146068_u() {
        int i = this.field_70170_p.field_73012_v.nextInt(3);
        if (i == 0) {
            return Items.field_151082_bd;
        }
        if (i == 1) {
            return Items.field_151145_ak;
        }
        return Items.field_151008_G;
    }
}

