/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.GenericTargetSorter;
import danger.orespawn.MyEntityAIWander;
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
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Irukandji
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private EntityLivingBase buddy = null;
    private float moveSpeed = 0.15f;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Irukandji(World par1World) {
        super(par1World);
        this.func_70105_a(0.25f, 0.25f);
        this.func_70661_as().func_75491_a(false);
        this.field_70728_aV = 50;
        this.field_70174_ab = 1;
        this.field_70178_ae = false;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 1.0f));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.Irukandji_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public boolean func_70648_aU() {
        return true;
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.Irukandji_stats.health;
    }

    public int func_70658_aO() {
        return OreSpawnMain.Irukandji_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
    }

    public int getAttackStrength(Entity par1Entity) {
        int var2 = 2;
        return var2;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:little_splt";
    }

    protected String func_70673_aS() {
        return "orespawn:ratdead";
    }

    protected float func_70599_aP() {
        return 0.25f;
    }

    protected float func_70647_i() {
        return 2.0f;
    }

    protected Item func_146068_u() {
        return OreSpawnMain.MyIrukandji;
    }

    public void initCreature() {
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        if (par1EntityPlayer != null && par1EntityPlayer.func_71045_bC() == null) {
            par1EntityPlayer.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 200.0f);
        }
        return false;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        EntityPlayer p;
        boolean ret = false;
        if (this.field_70128_L) {
            return false;
        }
        Entity e = par1DamageSource.func_76346_g();
        if (e != null && e instanceof EntityPlayer && (p = (EntityPlayer)e).func_71045_bC() == null) {
            p.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 200.0f);
            return false;
        }
        if (e != null && e instanceof EntityLiving) {
            if (e instanceof Irukandji) {
                return false;
            }
            this.func_70624_b((EntityLivingBase)((EntityLiving)e));
            this.func_70784_b(e);
            this.func_70661_as().func_75497_a((Entity)((EntityLiving)e), 1.2);
            ret = true;
        }
        ret = super.func_70097_a(par1DamageSource, par2);
        return ret;
    }

    private boolean scan_it(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        Block bid;
        int j;
        int i;
        int found = 0;
        for (i = -dy; i <= dy; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_147439_a(x + dx, y + i, z + j);
                if ((bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_147439_a(x - dx, y + i, z + j)) != Blocks.field_150355_j && bid != Blocks.field_150358_i || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_147439_a(x + i, y + dy, z + j);
                if ((bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_147439_a(x + i, y - dy, z + j)) != Blocks.field_150355_j && bid != Blocks.field_150358_i || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                bid = this.field_70170_p.func_147439_a(x + i, y + j, z + dz);
                if ((bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_147439_a(x + i, y + j, z - dz)) != Blocks.field_150355_j && bid != Blocks.field_150358_i || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (!this.func_70090_H() && this.field_70170_p.field_73012_v.nextInt(10) == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 12; ++i) {
                int j = i;
                if (j > 5) {
                    j = 5;
                }
                if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 5) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)(this.ty - 1), (double)this.tz, 1.33);
            } else {
                if (this.field_70170_p.field_73012_v.nextInt(25) == 1) {
                    this.func_70691_i(-1.0f);
                }
                if (this.func_110143_aJ() <= 0.0f) {
                    this.func_70106_y();
                    return;
                }
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(8) == 1) {
            EntityLivingBase e = this.findSomethingToAttack();
            if (e != null) {
                if (this.func_70068_e((Entity)e) < 3.0) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(4) == 0 || this.field_70170_p.field_73012_v.nextInt(5) == 1) {
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
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        return false;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(6.0, 4.0, 6.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        EntityLivingBase e = this.func_70638_az();
        if (e != null && e.func_70089_S()) {
            return e;
        }
        this.func_70624_b(null);
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

    private int findBuddies() {
        List var5 = this.field_70170_p.func_72872_a(Irukandji.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0));
        return var5.size();
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70170_p.field_73012_v.nextInt(60) != 1) {
            return false;
        }
        return this.findBuddies() <= 2;
    }
}

