/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Girlfriend;
import danger.orespawn.Lizard;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.RubberDucky;
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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Crab
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private int hurt_timer = 0;
    private float moveSpeed = 0.55f;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Crab(World par1World) {
        super(par1World);
        this.func_70105_a(1.25f, 2.5f);
        this.func_70661_as().func_75491_a(false);
        this.field_70728_aV = 150;
        this.field_70174_ab = 30;
        this.field_70178_ae = false;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 16, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 10.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 8.0f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)(this.moveSpeed * this.getCrabScale()));
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)((float)OreSpawnMain.Crab_stats.attack * this.getCrabScale()));
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
        this.field_70180_af.func_75682_a(21, (Object)0);
        float t = 0.25f;
        if (this.field_70170_p != null) {
            if (this.field_70170_p.field_73012_v.nextInt(4) == 1) {
                t = 0.5f;
            }
            if (this.field_70170_p.field_73012_v.nextInt(8) == 2) {
                t = 1.0f;
            }
        } else {
            if (OreSpawnMain.OreSpawnRand.nextInt(4) == 1) {
                t = 0.5f;
            }
            if (OreSpawnMain.OreSpawnRand.nextInt(8) == 2) {
                t = 1.0f;
            }
        }
        this.setCrabScale(t);
        this.field_70728_aV = (int)(400.0f * t);
        this.field_70174_ab = (int)(10.0f * t);
        this.func_70105_a(3.75f * this.getCrabScale(), 3.5f * this.getCrabScale());
    }

    public float getCrabScale() {
        int i = this.field_70180_af.func_75679_c(21);
        float f = i;
        return f / 100.0f;
    }

    public void setCrabScale(float par1) {
        float f = par1 * 100.0f;
        int i = (int)f;
        this.field_70180_af.func_75692_b(21, (Object)i);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.setCrabScale(par1NBTTagCompound.func_74760_g("Fscale"));
        this.func_70105_a(3.75f * this.getCrabScale(), 3.5f * this.getCrabScale());
        this.field_70728_aV = (int)(400.0f * this.getCrabScale());
        this.field_70174_ab = (int)(10.0f * this.getCrabScale());
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74776_a("Fscale", this.getCrabScale());
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void func_70071_h_() {
        this.moveSpeed = this.func_70090_H() ? 0.95f : 0.55f;
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)(this.moveSpeed * this.getCrabScale()));
        super.func_70071_h_();
        this.func_70105_a(2.5f * this.getCrabScale(), 3.5f * this.getCrabScale());
    }

    public int mygetMaxHealth() {
        return (int)((float)OreSpawnMain.PitchBlack_stats.health * this.getCrabScale());
    }

    public int func_70658_aO() {
        return OreSpawnMain.Crab_stats.defense + (int)(2.0f * this.getCrabScale());
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
    }

    public int getCrabHealth() {
        return (int)this.func_110143_aJ();
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:leaves_hit";
    }

    protected String func_70673_aS() {
        return null;
    }

    protected float func_70599_aP() {
        return 0.75f;
    }

    protected float func_70647_i() {
        return 2.0f - 0.3f * (1.0f / this.getCrabScale());
    }

    protected Item func_146068_u() {
        return Items.field_151115_aP;
    }

    private ItemStack dropItemRand(Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    protected void func_70628_a(boolean par1, int par2) {
        Object is = null;
        int var5 = 4 + this.field_70170_p.field_73012_v.nextInt(8);
        if ((var5 = (int)((float)var5 * this.getCrabScale())) < 1) {
            var5 = 1;
        }
        for (int var4 = 0; var4 < var5; ++var4) {
            this.dropItemRand(OreSpawnMain.MyRawCrabMeat, 1);
        }
    }

    public void initCreature() {
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), (float)OreSpawnMain.Crab_stats.attack * this.getCrabScale());
        if (var4 && par1Entity != null && par1Entity instanceof EntityLivingBase) {
            double ks = 1.15 * (double)this.getCrabScale();
            double inair = 0.48 * (double)this.getCrabScale();
            float f3 = (float)Math.atan2(par1Entity.field_70161_v - this.field_70161_v, par1Entity.field_70165_t - this.field_70165_t);
            if (par1Entity.field_70128_L || par1Entity instanceof EntityPlayer) {
                inair *= 2.0;
            }
            par1Entity.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return var4;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (par1DamageSource.func_76355_l().equals("cactus")) {
            return false;
        }
        Entity e = par1DamageSource.func_76346_g();
        if (this.hurt_timer <= 0) {
            ret = super.func_70097_a(par1DamageSource, par2);
            this.hurt_timer = 8;
        }
        if (e != null && e instanceof EntityLiving) {
            if (e instanceof Crab) {
                return false;
            }
            this.func_70624_b((EntityLivingBase)((EntityLiving)e));
            this.func_70784_b(e);
            this.func_70661_as().func_75497_a((Entity)((EntityLiving)e), 1.2);
        }
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
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (!this.func_70090_H() && this.field_70170_p.field_73012_v.nextInt(25) == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 12; ++i) {
                int j = i;
                if (j > 10) {
                    j = 10;
                }
                if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 5) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)(this.ty - 1), (double)this.tz, 1.33);
            } else {
                if (this.field_70170_p.field_73012_v.nextInt(100) == 1) {
                    this.func_70691_i(-1.0f * this.getCrabScale());
                }
                if (this.func_110143_aJ() <= 0.0f) {
                    this.func_70106_y();
                    return;
                }
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(5) == 1) {
            EntityLivingBase e = null;
            if (this.field_70170_p.field_73012_v.nextInt(100) == 1) {
                this.func_70624_b(null);
            }
            if ((e = this.func_70638_az()) != null && !e.func_70089_S()) {
                this.func_70624_b(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.func_70625_a((Entity)e, 10.0f, 10.0f);
                if (this.func_70068_e((Entity)e) < (double)((6.0f + e.field_70130_N / 2.0f) * (6.0f + e.field_70130_N / 2.0f) * this.getCrabScale())) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(4) == 0 || this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                        this.func_70652_k((Entity)e);
                        if (!this.field_70170_p.field_72995_K) {
                            if (this.field_70170_p.field_73012_v.nextInt(3) == 1) {
                                this.field_70170_p.func_72956_a((Entity)e, "orespawn:scorpion_attack", 0.75f, 1.5f);
                            } else {
                                this.field_70170_p.func_72956_a((Entity)e, "orespawn:scorpion_living", 0.75f, 1.5f);
                            }
                        }
                    }
                } else {
                    this.func_70661_as().func_75497_a((Entity)e, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(120) == 1 && this.func_70090_H() && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_85030_a("splash", 1.5f, this.field_70170_p.field_73012_v.nextFloat() * 0.2f + 0.9f);
            this.func_70691_i(4.0f * this.getCrabScale());
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
        if (par1EntityLiving instanceof Crab) {
            return false;
        }
        if (par1EntityLiving instanceof EntityMob) {
            return true;
        }
        if (par1EntityLiving instanceof Lizard) {
            return true;
        }
        if (par1EntityLiving instanceof RubberDucky) {
            return true;
        }
        if (par1EntityLiving instanceof EntityVillager) {
            return true;
        }
        if (par1EntityLiving instanceof Girlfriend) {
            return true;
        }
        if (par1EntityLiving instanceof Boyfriend) {
            return true;
        }
        return MyUtils.isAttackableNonMob((EntityLivingBase)par1EntityLiving);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(16.0, 6.0, 16.0));
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
        List var5 = this.field_70170_p.func_72872_a(Crab.class, this.field_70121_D.func_72314_b(24.0, 8.0, 24.0));
        return var5.size();
    }

    public boolean func_70601_bi() {
        for (int k = -3; k < 3; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 0; i < 5; ++i) {
                    Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Crab")) continue;
                    this.setCrabScale(0.35f);
                    return true;
                }
            }
        }
        if (this.field_70163_u < 50.0) {
            return false;
        }
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5) {
            if (this.field_70170_p.field_73012_v.nextInt(40) != 1) {
                return false;
            }
            if (this.findBuddies() > 3) {
                return false;
            }
        }
        return true;
    }

    public boolean func_70648_aU() {
        return true;
    }
}

