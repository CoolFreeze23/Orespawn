/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
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
 *  net.minecraft.init.MobEffects
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.datasync.DataParameter
 *  net.minecraft.network.datasync.DataSerializer
 *  net.minecraft.network.datasync.DataSerializers
 *  net.minecraft.network.datasync.EntityDataManager
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.EnumParticleTypes
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.GenericTargetSorter;
import danger.orespawn.entity.RenderInfo;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.util.ai.MyEntityAIWanderALot;
import danger.orespawn.util.handlers.SoundsHandler;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class Alien
extends EntityMob {
    protected static final DataParameter<Byte> ATTACKING = EntityDataManager.func_187226_a(Alosaurus.class, (DataSerializer)DataSerializers.field_187191_a);
    private GenericTargetSorter TargetSorter;
    private RenderInfo renderdata;
    private int hurt_timer = 0;
    private double moveSpeed = 0.65;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Alien(World par1World) {
        super(par1World);
        this.func_70105_a(1.1f, 3.25f);
        this.field_70728_aV = 100;
        this.field_70178_ae = false;
        this.field_70747_aH = 0.6f;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.renderdata = new RenderInfo();
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMoveThroughVillage((EntityCreature)this, 1.0, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 10, 1.0));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(12.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_187214_a(ATTACKING, (Object)0);
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
        return !this.func_104002_bU();
    }

    public int mygetMaxHealth() {
        return 100;
    }

    protected void func_70664_aZ() {
        super.func_70664_aZ();
        this.field_70181_x += 0.25;
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
        return 8;
    }

    public boolean func_175446_cd() {
        return false;
    }

    public void func_70636_d() {
        super.func_70636_d();
        if (this.field_70170_p.field_72995_K) {
            float f = 1.7f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.75f);
            if (this.field_70170_p.field_73012_v.nextInt(20) == 1) {
                this.field_70170_p.func_175688_a(EnumParticleTypes.DRIP_LAVA, this.field_70165_t - (double)f * Math.sin(Math.toRadians(this.field_70759_as)), this.field_70163_u + 1.6, this.field_70161_v + (double)f * Math.cos(Math.toRadians(this.field_70759_as)), 0.0, 0.0, 0.0, new int[0]);
            }
        }
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        super.func_70071_h_();
    }

    public int getAlienHealth() {
        return (int)this.func_110143_aJ();
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        if (this.field_70170_p.field_73012_v.nextInt(4) == 0) {
            return SoundsHandler.ENTITY_ALIEN_LIVING;
        }
        return null;
    }

    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return SoundsHandler.ENTITY_ALIEN_HURT;
    }

    protected float func_70599_aP() {
        return 1.0f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return Items.field_151070_bp;
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        int var5 = 5 + this.field_70170_p.field_73012_v.nextInt(6);
        for (var4 = 0; var4 < var5; ++var4) {
            this.dropItemRand(Items.field_151070_bp, 1);
        }
        var5 = 5 + this.field_70170_p.field_73012_v.nextInt(6);
        for (var4 = 0; var4 < var5; ++var4) {
            this.dropItemRand(Items.field_151145_ak, 1);
        }
        this.dropItemRand((Item)Items.field_151148_bJ, 1);
        this.dropItemRand(Items.field_151113_aN, 1);
        this.dropItemRand(Items.field_151111_aL, 1);
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (super.func_70652_k(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
                int var2 = 6;
                if (this.field_70170_p.func_175659_aa() == EnumDifficulty.EASY) {
                    var2 = 8;
                    if (this.field_70170_p.func_175659_aa() == EnumDifficulty.NORMAL) {
                        var2 = 10;
                    } else if (this.field_70170_p.func_175659_aa() == EnumDifficulty.HARD) {
                        var2 = 12;
                    }
                }
                if (par1Entity instanceof EntityLivingBase && this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                    ((EntityLivingBase)par1Entity).func_70690_d(new PotionEffect(MobEffects.field_76436_u, var2 * 5, 0));
                }
                double ks = 1.1;
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

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        Entity e;
        boolean ret = false;
        if (par1DamageSource.func_76355_l().equals("cactus")) {
            return false;
        }
        if (this.hurt_timer <= 0) {
            ret = super.func_70097_a(par1DamageSource, par2);
        }
        if ((e = par1DamageSource.func_76346_g()) != null && e instanceof EntityLiving) {
            this.func_70624_b((EntityLivingBase)((EntityLiving)e));
            this.func_70661_as().func_75497_a((Entity)((EntityLiving)e), 1.2);
            ret = true;
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
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + dx, y + i, z + j)).func_177230_c();
                if ((bid == Blocks.field_150478_aa || bid == ModBlocks.EXTREME_TORCH) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x - dx, y + i, z + j)).func_177230_c()) != Blocks.field_150478_aa && bid != ModBlocks.EXTREME_TORCH || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + dy, z + j)).func_177230_c();
                if ((bid == Blocks.field_150478_aa || bid == ModBlocks.EXTREME_TORCH) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y - dy, z + j)).func_177230_c()) != Blocks.field_150478_aa && bid != ModBlocks.EXTREME_TORCH || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z + dz)).func_177230_c();
                if (bid == Blocks.field_150478_aa && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z - dz)).func_177230_c()) != Blocks.field_150478_aa || (d = dz * dz + j * j + i * i) >= this.closest) continue;
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
        if (this.field_70170_p.field_73012_v.nextInt(8) == 0) {
            EntityLivingBase e = this.findSomethingToAttack();
            if (e != null) {
                this.func_70625_a((Entity)e, 10.0f, 10.0f);
                if (this.func_70068_e((Entity)e) < 16.0) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(4) == 0 || this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                        this.func_70652_k((Entity)e);
                    }
                }
                this.func_70661_as().func_75497_a((Entity)e, 1.2);
            } else {
                this.setAttacking(0);
            }
        } else if (this.field_70146_Z.nextInt(30) == 0 && OreSpawnMain.PlayNicely == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 2; i < 15 && !this.scan_it((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, i, i, i); ++i) {
                if (i < 10) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)this.ty, (double)this.tz, 1.0);
                if (this.closest < 27 && this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                    this.field_70170_p.func_175656_a(new BlockPos(this.tx, this.ty, this.tz), Blocks.field_150350_a.func_176223_P());
                }
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(40) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(1.0f);
        }
    }

    private boolean isSuitableTarget(EntityLivingBase var4, boolean par2) {
        if (var4 == null) {
            return false;
        }
        if (var4 == this) {
            return false;
        }
        if (!var4.func_70089_S()) {
            return false;
        }
        if (var4 instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)var4;
            return !p.field_71075_bZ.field_75098_d;
        }
        return false;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.func_174813_aQ().func_72321_a(12.0, 4.0, 12.0));
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
        return ((Byte)this.field_70180_af.func_187225_a(ATTACKING)).byteValue();
    }

    public final void setAttacking(int par1) {
        this.field_70180_af.func_187227_b(ATTACKING, (Object)((byte)par1));
    }

    public boolean func_70601_bi() {
        if (!this.func_70814_o()) {
            return false;
        }
        if (this.field_70163_u > 50.0) {
            return false;
        }
        for (int k = -1; k < 2; ++k) {
            for (int j = -1; j < 2; ++j) {
                for (int i = 1; i < 4; ++i) {
                    Block bid = this.field_70170_p.func_180495_p(new BlockPos((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k)).func_177230_c();
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
        }
        return true;
    }
}

