/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.CrystalCow;
import danger.orespawn.Flounder;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Irukandji;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.Peacock;
import danger.orespawn.RenderInfo;
import danger.orespawn.Rotator;
import danger.orespawn.Skate;
import danger.orespawn.Vortex;
import danger.orespawn.Whale;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Urchin
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private RenderInfo renderdata = new RenderInfo();
    private float moveSpeed = 0.3f;
    private int was_spawnered = 0;

    public Urchin(World par1World) {
        super(par1World);
        this.func_70105_a(1.35f, 2.1f);
        this.func_70661_as().func_75491_a(true);
        this.field_70728_aV = 20;
        this.field_70174_ab = 1000;
        this.field_70178_ae = true;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.renderdata = new RenderInfo();
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 14, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.Urchin_stats.attack);
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
        return this.was_spawnered == 0;
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
        if (this.func_104002_bU()) {
            return;
        }
        if (this.was_spawnered != 0) {
            return;
        }
        long t = this.field_70170_p.func_72820_D();
        if ((t %= 24000L) < 12000L && this.field_70170_p.field_73012_v.nextInt(400) == 1) {
            this.func_70106_y();
        }
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.Urchin_stats.health;
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

    public int func_70658_aO() {
        return OreSpawnMain.Urchin_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
        if (this.field_70170_p.field_73012_v.nextInt(3) == 1) {
            this.field_70170_p.func_72869_a("flame", this.field_70165_t, this.field_70163_u + 0.75, this.field_70161_v, 0.0, (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), 0.0);
            if (this.func_70090_H() && this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                this.func_70652_k((Entity)this);
                this.field_70170_p.func_72869_a("smoke", this.field_70165_t, this.field_70163_u + 1.75, this.field_70161_v, 0.0, (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), 0.0);
                this.field_70170_p.func_72869_a("largesmoke", this.field_70165_t, this.field_70163_u + 1.75, this.field_70161_v, 0.0, (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), 0.0);
                this.field_70170_p.func_72869_a("smoke", this.field_70165_t, this.field_70163_u + 2.0, this.field_70161_v, 0.0, (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), 0.0);
                this.field_70170_p.func_72869_a("largesmoke", this.field_70165_t, this.field_70163_u + 2.0, this.field_70161_v, 0.0, (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), 0.0);
            }
        }
    }

    protected String func_70639_aQ() {
        return "orespawn:kyuubi_living";
    }

    protected String func_70621_aR() {
        return "orespawn:glasshit";
    }

    protected String func_70673_aS() {
        return "orespawn:glassdead";
    }

    protected float func_70599_aP() {
        return 1.1f;
    }

    protected float func_70647_i() {
        return 1.25f;
    }

    protected Item func_146068_u() {
        int i = this.field_70170_p.field_73012_v.nextInt(3);
        if (i == 1) {
            return OreSpawnMain.MyCrystalPinkIngot;
        }
        if (i == 2) {
            return OreSpawnMain.MyCrystalApple;
        }
        return null;
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70652_k(Entity par1Entity) {
        par1Entity.func_70015_d(5);
        return super.func_70652_k(par1Entity);
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70170_p.field_73012_v.nextInt(8) == 0) {
            EntityLivingBase e = this.findSomethingToAttack();
            if (e != null) {
                if (this.func_70068_e((Entity)e) < 8.0) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(7) == 0 || this.field_70170_p.field_73012_v.nextInt(8) == 1) {
                        this.func_70652_k((Entity)e);
                    }
                } else {
                    this.func_70661_as().func_75497_a((Entity)e, 1.2);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (!par1DamageSource.func_76355_l().equals("cactus")) {
            ret = super.func_70097_a(par1DamageSource, par2);
        }
        return ret;
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
        if (MyUtils.isIgnoreable((EntityLivingBase)par1EntityLiving)) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Vortex) {
            return false;
        }
        if (par1EntityLiving instanceof Rotator) {
            return false;
        }
        if (par1EntityLiving instanceof Peacock) {
            return false;
        }
        if (par1EntityLiving instanceof CrystalCow) {
            return false;
        }
        if (par1EntityLiving instanceof Irukandji) {
            return false;
        }
        if (par1EntityLiving instanceof Skate) {
            return false;
        }
        if (par1EntityLiving instanceof Whale) {
            return false;
        }
        if (par1EntityLiving instanceof Flounder) {
            return false;
        }
        if (par1EntityLiving instanceof Urchin) {
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
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(16.0, 3.0, 16.0));
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

    public final int getAttacking() {
        return this.field_70180_af.func_75683_a(20);
    }

    public final void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    public boolean func_70601_bi() {
        Block bid;
        int j;
        int k;
        int sc = 0;
        for (k = -2; k <= 2; ++k) {
            for (j = -2; j <= 2; ++j) {
                for (int i = 1; i < 4; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Crystal Urchin")) continue;
                    this.was_spawnered = 1;
                    return true;
                }
            }
        }
        for (k = -1; k <= 1; ++k) {
            for (j = -1; j <= 1; ++j) {
                bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + 1, (int)this.field_70161_v + k);
                if (bid != Blocks.field_150350_a) continue;
                ++sc;
            }
        }
        if (sc < 6) {
            return false;
        }
        if (!this.func_70814_o()) {
            return false;
        }
        long t = this.field_70170_p.func_72820_D();
        return (t %= 24000L) >= 13000L;
    }
}

