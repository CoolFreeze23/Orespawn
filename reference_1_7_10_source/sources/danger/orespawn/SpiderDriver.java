/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.GenericTargetSorter;
import danger.orespawn.MyEntityAIWander;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.SpiderRobot;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class SpiderDriver
extends EntitySpider {
    private GenericTargetSorter TargetSorter = new GenericTargetSorter((Entity)this);

    public SpiderDriver(World par1World) {
        super(par1World);
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 0.65f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        return this.field_70154_o == null;
    }

    public boolean func_70650_aV() {
        return true;
    }

    protected Entity func_70782_k() {
        double d0 = 16.0;
        return this.field_70170_p.func_72856_b((Entity)this, d0);
    }

    protected void func_70619_bc() {
        EntityLivingBase e;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.field_70170_p.field_73012_v.nextInt(5) == 0 && this.field_70154_o == null && (e = this.findSpiderRobot()) != null) {
            this.func_70625_a((Entity)e, 10.0f, 10.0f);
            if (this.func_70068_e((Entity)e) < (double)((4.0f + e.field_70130_N / 2.0f) * (4.0f + e.field_70130_N / 2.0f))) {
                this.func_70078_a((Entity)e);
            } else {
                this.func_70661_as().func_75497_a((Entity)e, 0.55);
            }
        }
        if (this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.field_70170_p.field_73012_v.nextInt(4) == 0 && this.field_70154_o != null && (e = this.findSomethingToAttack()) != null) {
            this.func_70625_a((Entity)e, 10.0f, 10.0f);
            if (!(this.func_70068_e((Entity)e) < (double)((11.0f + e.field_70130_N / 2.0f) * (11.0f + e.field_70130_N / 2.0f))) && this.field_70154_o instanceof SpiderRobot) {
                SpiderRobot sp = (SpiderRobot)this.field_70154_o;
                double d1 = e.field_70161_v - this.field_70161_v;
                double d2 = e.field_70165_t - this.field_70165_t;
                double dd = Math.atan2(d1, d2);
                sp.goThisWay(0.35 * Math.cos(dd), 0.35 * Math.sin(dd));
            }
        }
    }

    protected void func_70785_a(Entity par1Entity, float par2) {
        if (this.field_70724_aR <= 0 && par2 < 2.0f && par1Entity.field_70121_D.field_72337_e > this.field_70121_D.field_72338_b && par1Entity.field_70121_D.field_72338_b < this.field_70121_D.field_72337_e) {
            this.field_70724_aR = 16;
            this.func_70652_k(par1Entity);
            if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                ((EntityLivingBase)par1Entity).func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, 60, 0));
            }
        }
    }

    public int func_70658_aO() {
        if (this.field_70154_o != null) {
            return 8;
        }
        return 20;
    }

    private EntityLivingBase findSpiderRobot() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(SpiderRobot.class, this.field_70121_D.func_72314_b(25.0, 15.0, 25.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (var4.field_70153_n != null) continue;
            return var4;
        }
        return null;
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
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
        if (par1EntityLiving instanceof SpiderRobot) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderDriver) {
            return false;
        }
        if (par1EntityLiving instanceof EntitySpider) {
            return false;
        }
        if (par1EntityLiving instanceof EntityCaveSpider) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        return !(this.func_70068_e((Entity)par1EntityLiving) < 36.0);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(35.0, 15.0, 35.0));
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

    public boolean func_70601_bi() {
        SpiderRobot target = null;
        target = (SpiderRobot)this.field_70170_p.func_72857_a(SpiderRobot.class, this.field_70121_D.func_72314_b(24.0, 12.0, 24.0), (Entity)this);
        if (target != null) {
            return true;
        }
        return super.func_70601_bi();
    }
}

