/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAIHurtByTarget
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIMate
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.ai.EntityAITempt
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.GenericTargetSorter;
import danger.orespawn.util.ai.MyEntityAIFollowOwner;
import danger.orespawn.util.ai.MyEntityAIWanderALot;
import danger.orespawn.util.handlers.SoundsHandler;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class GammaMetroid
extends EntityTameable {
    private GenericTargetSorter TargetSorter = null;
    private float moveSpeed = 0.15f;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public GammaMetroid(World worldIn) {
        super(worldIn);
        this.func_70105_a(1.5f, 1.5f);
        this.field_70728_aV = 20;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMate((EntityAnimal)this, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAITempt((EntityCreature)this, (double)1.2f, Items.field_151042_j, false));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 16, 1.0));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false, new Class[0]));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(10.0);
    }

    protected boolean func_70692_ba() {
        if (this.func_70631_g_()) {
            return false;
        }
        if (this.func_70909_n()) {
            return false;
        }
        return !this.func_104002_bU();
    }

    public boolean func_70652_k(Entity entityIn) {
        return entityIn.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 10.0f);
    }

    public boolean func_184645_a(EntityPlayer player, EnumHand hand) {
        ItemStack item = player.field_71071_by.func_70448_g();
        if (super.func_184645_a(player, hand)) {
            return true;
        }
        if (item.func_77973_b() == Items.field_151042_j && (double)player.func_70032_d((Entity)this) < 25.0 && !this.func_70909_n() && !this.field_70170_p.field_72995_K) {
            if (this.field_70146_Z.nextInt(3) == 0) {
                this.func_70903_f(true);
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
            } else {
                this.func_70908_e(false);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
            }
        }
        return false;
    }

    private int mygetMaxHealth() {
        return 100;
    }

    public int func_70658_aO() {
        return super.func_70658_aO();
    }

    public boolean func_175446_cd() {
        return false;
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        if (this.field_70170_p.field_73012_v.nextInt(5) == 1) {
            return SoundsHandler.ENTITY_GAMMAMETROID_LIVING;
        }
        return null;
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return SoundsHandler.ENTITY_DUCK_HURT;
    }

    @Nullable
    protected SoundEvent func_184615_bR() {
        return SoundsHandler.ENTITY_ALOSAURUS_DEATH;
    }

    protected float func_70599_aP() {
        return 1.5f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    @Nullable
    protected Item func_146068_u() {
        return Items.field_151042_j;
    }

    private void dropItemRand(Item item, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), new ItemStack(item, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean wasRecentlyHit, int lootingModifier) {
        int var4;
        int i = 5 + OreSpawnMain.OreSpawnRand.nextInt(10);
        for (var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.field_151074_bl, 1);
        }
        i = 6 + OreSpawnMain.OreSpawnRand.nextInt(10);
        for (var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.field_151042_j, 1);
        }
    }

    protected void func_70619_bc() {
        EntityLivingBase e;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70170_p.func_175659_aa() != EnumDifficulty.PEACEFUL && this.field_70170_p.field_73012_v.nextInt(5) == 0 && (e = this.findSomethingToAttack()) != null) {
            this.func_70625_a((Entity)e, 10.0f, 10.0f);
            if (this.func_70032_d((Entity)e) <= 9.0f) {
                if (this.field_70170_p.field_73012_v.nextInt(4) == 0 || this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                    this.func_70652_k((Entity)e);
                }
            } else {
                this.func_70661_as().func_75497_a((Entity)e, 1.25);
            }
        }
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        if (this.field_70170_p.func_175659_aa() == EnumDifficulty.PEACEFUL) {
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
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof GammaMetroid) {
            return false;
        }
        if (par1EntityLiving instanceof EntityMob) {
            return false;
        }
        if (this.func_70909_n()) {
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
        if (this.func_70631_g_()) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.func_174813_aQ().func_72321_a(10.0, 3.0, 10.0));
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
        return !(this.field_70163_u > 50.0);
    }

    private boolean scanIt(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        Block bid;
        BlockPos pos;
        int j;
        int i;
        int found = 0;
        for (i = -dy; i <= dy; ++i) {
            for (j = -dz; j <= dz; ++j) {
                pos = new BlockPos(x + dx, y + i, z + j);
                bid = this.field_70170_p.func_180495_p(pos).func_177230_c();
                if (bid == Blocks.field_150348_b && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(pos = new BlockPos(x - dx, y + i, z + j)).func_177230_c()) != Blocks.field_150348_b || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                pos = new BlockPos(x + i, y + dy, z + j);
                bid = this.field_70170_p.func_180495_p(pos).func_177230_c();
                if (bid == Blocks.field_150348_b && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(pos = new BlockPos(x + i, y - dy, z + j)).func_177230_c()) != Blocks.field_150348_b || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                pos = new BlockPos(x + i, y + j, z + dz);
                bid = this.field_70170_p.func_180495_p(pos).func_177230_c();
                if (bid == Blocks.field_150348_b && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(pos = new BlockPos(x + i, y + j, z - dz)).func_177230_c()) != Blocks.field_150348_b || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    public void func_70071_h_() {
        if (this.field_70128_L) {
            return;
        }
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
        if ((this.field_70170_p.field_73012_v.nextInt(20) == 0 && this.func_110143_aJ() < (float)this.mygetMaxHealth() || this.field_70170_p.field_73012_v.nextInt(100) == 0) && OreSpawnMain.PlayNicely == 0 && !this.func_70906_o()) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 6; ++i) {
                int j = i;
                if (j > 2) {
                    j = 2;
                }
                if (this.scanIt((int)this.field_70165_t, (int)this.field_70163_u + 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 4) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)this.ty, (double)this.tz, 1.0);
                if (this.closest < 12) {
                    BlockPos pos = new BlockPos(this.tx, this.ty, this.tz);
                    if (this.field_70170_p.func_82736_K().func_82766_b("mobGriefing") && this.func_70613_aW()) {
                        this.field_70170_p.func_175698_g(pos);
                        this.func_70691_i(1.0f);
                        this.func_184185_a(SoundEvents.field_187739_dZ, 0.5f, this.field_70170_p.field_73012_v.nextFloat() * 0.2f + 1.5f);
                    }
                }
            }
        }
    }

    private boolean canPlaceBlock(World p_188518_1_, BlockPos p_188518_2_, Block p_188518_3_, IBlockState p_188518_4_, IBlockState p_188518_5_) {
        if (!p_188518_3_.func_176196_c(p_188518_1_, p_188518_2_)) {
            return false;
        }
        if (p_188518_4_.func_185904_a() != Material.field_151579_a) {
            return false;
        }
        if (p_188518_5_.func_185904_a() == Material.field_151579_a) {
            return false;
        }
        return p_188518_5_.func_185917_h();
    }

    @Nullable
    public EntityAgeable func_90011_a(EntityAgeable ageable) {
        return this.spawnBabyAnimal(ageable);
    }

    private GammaMetroid spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
        GammaMetroid w = new GammaMetroid(this.field_70170_p);
        if (this.func_70909_n()) {
            w.func_70903_f(true);
        }
        return w;
    }

    public void func_146082_f(@Nullable EntityPlayer player) {
    }

    public boolean func_70877_b(ItemStack stack) {
        return stack.func_77973_b() == Items.field_151042_j;
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }
}

