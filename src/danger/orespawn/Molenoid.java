/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAIHurtByTarget
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIMoveThroughVillage
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.tileentity.TileEntityMobSpawner
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.GenericTargetSorter;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
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
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Molenoid
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private float moveSpeed = 0.35f;

    public Molenoid(World par1World) {
        super(par1World);
        this.func_70105_a(3.9f, 2.6f);
        this.func_70661_as().func_75491_a(true);
        this.field_70728_aV = 40;
        this.field_70174_ab = 100;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMoveThroughVillage((EntityCreature)this, 1.0, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 16, 1.0));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.Molenoid_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.Molenoid_stats.health;
    }

    public int func_70658_aO() {
        return OreSpawnMain.Molenoid_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
    }

    protected String func_70639_aQ() {
        if (this.field_70146_Z.nextInt(3) == 0) {
            return "orespawn:molenoid_living";
        }
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:molenoid_hit";
    }

    protected String func_70673_aS() {
        return "orespawn:molenoid_death";
    }

    protected float func_70599_aP() {
        return 1.1f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return Items.field_151082_bd;
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        this.dropItemRand(OreSpawnMain.MolenoidNose, 1);
        this.dropItemRand(Items.field_151160_bD, 1);
        for (var4 = 0; var4 < 10; ++var4) {
            this.dropItemRand(Items.field_151074_bl, 1);
        }
        for (var4 = 0; var4 < 6; ++var4) {
            this.dropItemRand(Items.field_151082_bd, 1);
        }
    }

    public void initCreature() {
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return false;
        }
        return super.func_70097_a(par1DamageSource, par2);
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (super.func_70652_k(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
                double ks = 0.8;
                double inair = 0.1;
                float f3 = (float)Math.atan2(par1Entity.field_70161_v - this.field_70161_v, par1Entity.field_70165_t - this.field_70165_t);
                if (par1Entity.field_70128_L || par1Entity instanceof EntityPlayer) {
                    inair *= 2.0;
                }
                par1Entity.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            return true;
        }
        return false;
    }

    protected void func_70619_bc() {
        double dz;
        double dx;
        EntityLivingBase e = null;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70170_p.field_73012_v.nextInt(4) == 0) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.func_70625_a((Entity)e, 10.0f, 10.0f);
                if (this.func_70068_e((Entity)e) < (double)((6.0f + e.field_70130_N / 2.0f) * (6.0f + e.field_70130_N / 2.0f))) {
                    this.setAttacking(1);
                    if (this.func_70068_e((Entity)e) < 16.0 && (this.field_70170_p.field_73012_v.nextInt(4) == 0 || this.field_70170_p.field_73012_v.nextInt(5) == 1)) {
                        this.func_70652_k((Entity)e);
                    } else if (OreSpawnMain.PlayNicely == 0) {
                        int j = 1 + this.field_70170_p.field_73012_v.nextInt(4);
                        block0: for (int k = 0; k < j; ++k) {
                            dx = e.field_70165_t;
                            dz = e.field_70161_v;
                            dx += (double)(this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 2.0;
                            dz += (double)(this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 2.0;
                            for (int i = 4; i > -3; --i) {
                                if (this.field_70170_p.func_147439_a((int)dx, (int)e.field_70163_u + i + 1, (int)dz) != Blocks.field_150350_a || this.field_70170_p.func_147439_a((int)dx, (int)e.field_70163_u + i, (int)dz) == Blocks.field_150350_a) continue;
                                this.field_70170_p.func_147449_b((int)dx, (int)e.field_70163_u + i + 1, (int)dz, OreSpawnMain.MyMoleDirtBlock);
                                continue block0;
                            }
                        }
                    }
                } else {
                    this.func_70661_as().func_75497_a((Entity)e, 1.25);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
            int odds;
            double spd = 0.0;
            spd = this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y;
            if ((spd = Math.sqrt(spd)) > (double)this.moveSpeed) {
                spd = this.moveSpeed;
            }
            if ((odds = (int)(100.0 * spd / (double)this.moveSpeed)) > 0 && this.field_70170_p.field_73012_v.nextInt(100) < odds && OreSpawnMain.PlayNicely == 0) {
                dx = this.field_70165_t + 6.0 * Math.sin(Math.toRadians(this.field_70759_as));
                dz = this.field_70161_v - 6.0 * Math.cos(Math.toRadians(this.field_70759_as));
                dx += (double)(this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 3.0;
                dz += (double)(this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 3.0;
                for (int i = 4; i > -4; --i) {
                    if (this.field_70170_p.func_147439_a((int)dx, (int)this.field_70163_u + i + 1, (int)dz) != Blocks.field_150350_a || this.field_70170_p.func_147439_a((int)dx, (int)this.field_70163_u + i, (int)dz) == Blocks.field_150350_a) continue;
                    this.field_70170_p.func_147449_b((int)dx, (int)this.field_70163_u + i + 1, (int)dz, OreSpawnMain.MyMoleDirtBlock);
                    break;
                }
            }
        }
        dx = this.field_70165_t - 3.0 * Math.sin(Math.toRadians(this.field_70759_as));
        dz = this.field_70161_v + 3.0 * Math.cos(Math.toRadians(this.field_70759_as));
        dx += (double)(this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 3.0;
        dz += (double)(this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 3.0;
        int dir = 1;
        if (e != null) {
            if ((int)e.field_70163_u > (int)this.field_70163_u) {
                dir = 2;
            }
            if ((int)e.field_70163_u < (int)this.field_70163_u) {
                dir = 0;
            }
        }
        if (OreSpawnMain.PlayNicely == 0) {
            for (int i = dir; i < dir + 3; ++i) {
                Block bid = this.field_70170_p.func_147439_a((int)dx, (int)this.field_70163_u + i, (int)dz);
                if ((bid == Blocks.field_150346_d || bid == Blocks.field_150349_c || bid == Blocks.field_150351_n || bid == Blocks.field_150354_m || bid == Blocks.field_150362_t) && this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                    this.field_70170_p.func_147449_b((int)dx, (int)this.field_70163_u + i, (int)dz, Blocks.field_150350_a);
                }
                if (bid != OreSpawnMain.MyMoleDirtBlock) continue;
                this.field_70170_p.func_147449_b((int)dx, (int)this.field_70163_u + i, (int)dz, Blocks.field_150350_a);
            }
        }
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
        if (!this.MyCanSee(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        if (par1EntityLiving instanceof Molenoid) {
            return false;
        }
        if (par1EntityLiving instanceof EntityMob) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(12.0, 6.0, 12.0));
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
        int i;
        int j;
        int k;
        for (k = -3; k < 3; ++k) {
            for (j = -3; j < 3; ++j) {
                for (i = 0; i < 5; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Molenoid")) continue;
                    return true;
                }
            }
        }
        if (!this.func_70814_o()) {
            return false;
        }
        if (this.field_70163_u < 50.0) {
            return false;
        }
        if (this.field_70170_p.func_72935_r()) {
            return false;
        }
        for (k = -1; k < 1; ++k) {
            for (j = -1; j < 1; ++j) {
                for (i = 1; i < 4; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
        }
        Molenoid target = null;
        target = (Molenoid)this.field_70170_p.func_72857_a(Molenoid.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
        return target == null;
    }

    public boolean MyCanSee(EntityLivingBase e) {
        double xzoff = 2.0;
        int nblks = 10;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        float startx = (float)cx;
        float starty = (float)(this.field_70163_u + 1.0);
        float startz = (float)cz;
        float dx = (float)((e.field_70165_t - (double)startx) / 10.0);
        float dy = (float)((e.field_70163_u + (double)(e.field_70131_O / 2.0f) - (double)starty) / 10.0);
        float dz = (float)((e.field_70161_v - (double)startz) / 10.0);
        if ((double)Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int)((float)nblks * Math.abs(dx));
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if ((double)Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int)((float)nblks * Math.abs(dy));
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if ((double)Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int)((float)nblks * Math.abs(dz));
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            Block bid = this.field_70170_p.func_147439_a((int)(startx += dx), (int)(starty += dy), (int)(startz += dz));
            if (bid == Blocks.field_150350_a || bid == OreSpawnMain.MyMoleDirtBlock || bid == Blocks.field_150346_d || bid == Blocks.field_150349_c || bid == Blocks.field_150329_H || bid == Blocks.field_150354_m || bid == Blocks.field_150351_n) continue;
            return false;
        }
        return true;
    }
}

