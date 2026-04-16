/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIAvoidEntity
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIMoveIndoors
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.ai.EntityAITempt
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.projectile.EntitySmallFireball
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.datasync.DataParameter
 *  net.minecraft.network.datasync.DataSerializer
 *  net.minecraft.network.datasync.DataSerializers
 *  net.minecraft.network.datasync.EntityDataManager
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.SoundCategory
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.text.ITextComponent
 *  net.minecraft.util.text.TextComponentString
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.GenericTargetSorter;
import danger.orespawn.util.ai.MyEntityAIFollowOwner;
import danger.orespawn.util.ai.MyEntityAIWander;
import danger.orespawn.util.handlers.SoundsHandler;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class Spyro
extends EntityTameable {
    protected static final DataParameter<Byte> ACTIVITY = EntityDataManager.func_187226_a(Spyro.class, (DataSerializer)DataSerializers.field_187191_a);
    protected static final DataParameter<Byte> FIRE = EntityDataManager.func_187226_a(Spyro.class, (DataSerializer)DataSerializers.field_187191_a);
    private BlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter = null;
    public int activity = 1;
    private int owner_flying = 0;
    private boolean target_in_sight = false;
    private float moveSpeed = 0.3f;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Spyro(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 0.5f);
        this.moveSpeed = 0.3f;
        this.field_70178_ae = true;
        this.func_70904_g(false);
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityMob.class, 8.0f, (double)0.3f, (double)0.4f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new MyEntityAIFollowOwner(this, 1.15f, 12.0f, 2.0f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.25, Items.field_151082_bd, false));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0f));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 0.75f));
        this.field_70714_bg.func_75776_a(8, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(9, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70728_aV = 35;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(5.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.activity = 1;
        this.field_70180_af.func_187214_a(ACTIVITY, (Object)((byte)this.activity));
        this.field_70180_af.func_187214_a(FIRE, (Object)1);
        this.func_70904_g(false);
        this.func_70903_f(false);
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("SpyroActivity", (int)((Byte)this.field_70180_af.func_187225_a(ACTIVITY)).byteValue());
        par1NBTTagCompound.func_74768_a("SpyroFire", (int)((Byte)this.field_70180_af.func_187225_a(FIRE)).byteValue());
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.activity = par1NBTTagCompound.func_74762_e("SpyroActivity");
        this.field_70180_af.func_187227_b(ACTIVITY, (Object)((byte)this.activity));
        this.field_70180_af.func_187227_b(FIRE, (Object)((byte)par1NBTTagCompound.func_74762_e("SpyroFire")));
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
                if ((bid == Blocks.field_150353_l || bid == Blocks.field_150356_k) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x - dx, y + i, z + j)).func_177230_c()) != Blocks.field_150353_l && bid != Blocks.field_150356_k || (d = dx * dx + j * j + i * i) >= this.closest) continue;
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
                if ((bid == Blocks.field_150353_l || bid == Blocks.field_150356_k) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y - dy, z + j)).func_177230_c()) != Blocks.field_150353_l && bid != Blocks.field_150356_k || (d = dy * dy + j * j + i * i) >= this.closest) continue;
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
                if ((bid == Blocks.field_150353_l || bid == Blocks.field_150356_k) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z - dz)).func_177230_c()) != Blocks.field_150353_l && bid != Blocks.field_150356_k || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    public int getActivity() {
        byte i = (Byte)this.field_70180_af.func_187225_a(ACTIVITY);
        return i;
    }

    public void setActivity(int par1) {
        this.activity = par1;
        this.field_70180_af.func_187227_b(ACTIVITY, (Object)((byte)this.activity));
    }

    public int getSpyroFire() {
        return ((Byte)this.field_70180_af.func_187225_a(FIRE)).byteValue();
    }

    public void setSpyroFire(int par1) {
        this.field_70180_af.func_187227_b(FIRE, (Object)((byte)par1));
    }

    public boolean func_175446_cd() {
        return false;
    }

    public boolean func_70648_aU() {
        return true;
    }

    public int mygetMaxHealth() {
        return 200;
    }

    public int getSpyroHealth() {
        return (int)this.func_110143_aJ();
    }

    public boolean func_184645_a(EntityPlayer player, EnumHand hand) {
        ItemStack var2 = player.field_71071_by.func_70448_g();
        if (var2 != null && var2.func_77973_b() == Items.field_151082_bd && player.func_70068_e((Entity)this) < 16.0) {
            if (!this.func_70909_n() && !this.field_70170_p.field_72995_K) {
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    this.func_193101_c(player);
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                    this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                } else {
                    this.func_70908_e(false);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                }
            }
            if (!player.field_71075_bZ.field_75098_d) {
                var2.func_190920_e(var2.func_190916_E() - 1);
                if (var2.func_190916_E() <= 0) {
                    player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150330_I) && player.func_70068_e((Entity)this) < 16.0) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70903_f(false);
                this.func_70606_j(this.mygetMaxHealth());
                this.func_184754_b(null);
                this.func_70908_e(false);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
            }
            if (!player.field_71075_bZ.field_75098_d) {
                var2.func_190920_e(var2.func_190916_E() - 1);
                if (var2.func_190916_E() <= 0) {
                    player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150432_aD) && player.func_70068_e((Entity)this) < 16.0) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                this.setSpyroFire(0);
                player.func_145747_a((ITextComponent)new TextComponentString("Baby Spyro fireballs extinguished."));
            }
            if (!player.field_71075_bZ.field_75098_d) {
                var2.func_190920_e(var2.func_190916_E() - 1);
                if (var2.func_190916_E() <= 0) {
                    player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151033_d && player.func_70068_e((Entity)this) < 16.0) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                this.setSpyroFire(1);
                player.func_145747_a((ITextComponent)new TextComponentString("Baby Spyro fireballs lit!"));
            }
            if (!player.field_71075_bZ.field_75098_d) {
                var2.func_190920_e(var2.func_190916_E() - 1);
                if (var2.func_190916_E() <= 0) {
                    player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151057_cb && player.func_70068_e((Entity)this) < 16.0) {
            this.func_96094_a(var2.func_82833_r());
            if (!player.field_71075_bZ.field_75098_d) {
                var2.func_190920_e(var2.func_190916_E() - 1);
                if (var2.func_190916_E() <= 0) {
                    player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && player.func_70068_e((Entity)this) < 16.0) {
            this.func_70904_g(!this.func_70906_o());
            return true;
        }
        return super.func_184645_a(player, hand);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151082_bd;
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        return !this.func_70909_n();
    }

    public int func_70658_aO() {
        return 5;
    }

    protected String getLivingSound() {
        if (this.func_70906_o()) {
            return null;
        }
        if (this.getActivity() != 2) {
            return null;
        }
        return "orespawn:roar";
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return SoundsHandler.ENTITY_DUCK_HURT;
    }

    protected float func_70599_aP() {
        return 0.4f;
    }

    protected Item func_146068_u() {
        return Items.field_151082_bd;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var3 = 0;
        if (this.func_70909_n()) {
            var3 = this.field_70170_p.field_73012_v.nextInt(4);
            ++var3;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.func_145779_a(Items.field_151082_bd, 1);
            }
        }
    }

    protected float func_70647_i() {
        return this.func_70631_g_() ? (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 0.1f + 1.5f : (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 0.1f + 1.0f;
    }

    protected boolean func_70041_e_() {
        return true;
    }

    protected void fall(float par1) {
    }

    protected void updateFallState(double par1, boolean par3) {
    }

    public boolean func_145773_az() {
        return false;
    }

    public boolean func_70601_bi() {
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        return !(this.field_70163_u < 50.0);
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    public float getAttackStrength(Entity par1Entity) {
        return 4.0f;
    }

    public boolean func_70652_k(Entity par1Entity) {
        float var2 = this.getAttackStrength(par1Entity);
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), var2);
        return var4;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (!par1DamageSource.func_76355_l().equals("cactus")) {
            ret = super.func_70097_a(par1DamageSource, par2);
        }
        return ret;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(new Vec3d(this.field_70165_t, this.field_70163_u + 0.75, this.field_70161_v), new Vec3d(pX, pY, pZ), false) == null;
    }

    public void func_70071_h_() {
        EntityLivingBase e;
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
        if (this.func_70090_H()) {
            this.field_70181_x += 0.07;
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70170_p.field_73012_v.nextInt(100000) == 1 && !this.func_104002_bU()) {
            Entity ent = null;
            Spyro d = null;
            ent = Spyro.spawnCreature(this.field_70170_p, "Spyro", this.field_70165_t, this.field_70163_u, this.field_70161_v);
            if (ent != null) {
                d = (Spyro)ent;
                if (this.func_70909_n()) {
                    d.func_70903_f(true);
                }
                this.func_70106_y();
                return;
            }
        }
        if (this.activity == 2) {
            this.field_70181_x = this.field_70163_u < (double)this.currentFlightTarget.func_177956_o() + 2.0 ? (this.field_70181_x *= 0.7) : (this.field_70163_u > (double)this.currentFlightTarget.func_177956_o() - 2.0 ? (this.field_70181_x *= 0.5) : (this.field_70181_x *= 0.61));
        }
        if (this.activity == 1 && this.func_70909_n() && this.func_70902_q() != null && this.func_70068_e((Entity)(e = this.func_70902_q())) > 256.0) {
            this.setActivity(2);
        }
        this.do_movement();
    }

    public void func_70636_d() {
        super.func_70636_d();
    }

    public void func_70030_z() {
        boolean fly = false;
        if (this.activity == 1) {
            super.func_70030_z();
        }
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        if (!this.func_70906_o()) {
            if (this.activity == 0) {
                this.activity = 1;
            }
            if (this.field_70170_p.field_73012_v.nextInt(20) == 0) {
                this.closest = 99999;
                this.tz = 0;
                this.ty = 0;
                this.tx = 0;
                for (int i = 1; i < 11; ++i) {
                    int j = i;
                    if (j > 4) {
                        j = 4;
                    }
                    if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v, i, j, i)) break;
                    if (i < 6) continue;
                    ++i;
                }
                if (this.closest < 99999) {
                    this.setActivity(1);
                    this.func_70661_as().func_75492_a((double)this.tx, (double)(this.ty - 1), (double)this.tz, 1.0);
                    if (this.func_70072_I()) {
                        this.func_70691_i(1.0f);
                        this.func_184185_a(SoundEvents.field_187547_bF, 1.0f, this.field_70170_p.field_73012_v.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }
            if (this.field_70170_p.field_73012_v.nextInt(100) == 1 && !this.target_in_sight) {
                this.activity = 1;
                if (this.field_70170_p.field_73012_v.nextInt(8) == 1) {
                    this.activity = 2;
                }
                this.setActivity(this.activity);
            }
            this.owner_flying = 0;
            if (this.func_70909_n() && this.func_70902_q() != null) {
                EntityPlayer e = (EntityPlayer)this.func_70902_q();
                if (e.field_71075_bZ.field_75100_b) {
                    fly = true;
                }
                if (fly) {
                    this.owner_flying = 1;
                    this.setActivity(2);
                }
            }
        }
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70170_p.field_73012_v.nextInt(100) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(1.0f);
        }
    }

    private void do_movement() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        boolean do_new = false;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        boolean has_owner = false;
        EntityLivingBase e = null;
        if (this.currentFlightTarget == null) {
            do_new = true;
            this.currentFlightTarget = new BlockPos((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.func_70906_o()) {
            return;
        }
        if (this.activity == 1) {
            return;
        }
        if (this.getActivity() == 2 && this.field_70170_p.field_73012_v.nextInt(300) == 0) {
            do_new = true;
        }
        if (this.func_70909_n() && this.func_70902_q() != null) {
            e = this.func_70902_q();
            has_owner = true;
            ox = e.field_70165_t;
            oy = e.field_70163_u;
            oz = e.field_70161_v;
            if (this.func_70068_e((Entity)e) > 100.0) {
                do_new = true;
            }
            if (this.owner_flying != 0 && this.func_70068_e((Entity)e) > 36.0) {
                do_new = true;
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(6) == 1 && this.field_70170_p.func_175659_aa() != EnumDifficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.func_70909_n() && this.func_110143_aJ() / (float)this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(2);
                    this.target_in_sight = false;
                    do_new = false;
                    this.currentFlightTarget = new BlockPos((int)(this.field_70165_t + (this.field_70165_t - e.field_70165_t)), (int)(this.field_70163_u + 1.0), (int)(this.field_70161_v + (this.field_70161_v - e.field_70161_v)));
                } else {
                    this.setActivity(2);
                    this.target_in_sight = true;
                    this.currentFlightTarget = new BlockPos((int)e.field_70165_t, (int)(e.field_70163_u + 1.0), (int)e.field_70161_v);
                    this.func_70661_as().func_75497_a((Entity)e, 1.25);
                    do_new = false;
                    if (this.func_70068_e((Entity)e) < (double)((3.0f + e.field_70130_N / 2.0f) * (3.0f + e.field_70130_N / 2.0f))) {
                        this.func_70652_k((Entity)e);
                    } else if (this.func_70068_e((Entity)e) < 64.0 && !this.func_70090_H() && (this.getSpyroFire() == 1 && this.field_70170_p.field_73012_v.nextInt(10) == 0 || this.field_70170_p.field_73012_v.nextInt(15) == 1)) {
                        EntitySmallFireball var2 = new EntitySmallFireball(this.field_70170_p, (EntityLivingBase)this, e.field_70165_t - this.field_70165_t, e.field_70163_u + 0.25 - (this.field_70163_u + 1.25), e.field_70161_v - this.field_70161_v);
                        var2.func_70012_b(this.field_70165_t, this.field_70163_u + 1.25, this.field_70161_v, this.field_70177_z, this.field_70125_A);
                        this.field_70170_p.func_184133_a(null, this.func_180425_c(), SoundEvents.field_187737_v, SoundCategory.NEUTRAL, 0.75f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
                        this.field_70170_p.func_72838_d((Entity)var2);
                    }
                }
            } else {
                this.target_in_sight = false;
            }
        }
        if (this.currentFlightTarget.func_177954_c((double)((int)this.field_70165_t), (double)((int)this.field_70163_u), (double)((int)this.field_70161_v)) < (double)2.1f && this.getActivity() != 3) {
            do_new = true;
        }
        if (do_new && !this.target_in_sight) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                int gox = (int)this.field_70165_t;
                int goy = (int)this.field_70163_u;
                int goz = (int)this.field_70161_v;
                if (has_owner) {
                    gox = (int)ox;
                    goy = (int)oy;
                    goz = (int)oz;
                    if (this.owner_flying == 0) {
                        zdir = this.field_70170_p.field_73012_v.nextInt(4) + 6;
                        xdir = this.field_70170_p.field_73012_v.nextInt(4) + 6;
                    } else {
                        zdir = this.field_70170_p.field_73012_v.nextInt(6);
                        xdir = this.field_70170_p.field_73012_v.nextInt(6);
                    }
                } else {
                    zdir = this.field_70170_p.field_73012_v.nextInt(5) + 6;
                    xdir = this.field_70170_p.field_73012_v.nextInt(5) + 6;
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget = new BlockPos(gox + xdir, goy + this.field_70170_p.field_73012_v.nextInt(9 + this.owner_flying * 2) - 4, goz + zdir);
                bid = this.field_70170_p.func_180495_p(new BlockPos(this.currentFlightTarget.func_177958_n(), this.currentFlightTarget.func_177956_o(), this.currentFlightTarget.func_177952_p())).func_177230_c();
                if (bid == Blocks.field_150350_a && !this.canSeeTarget(this.currentFlightTarget.func_177958_n(), this.currentFlightTarget.func_177956_o(), this.currentFlightTarget.func_177952_p())) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
        }
        double speed_factor = 0.5;
        double var1 = (double)this.currentFlightTarget.func_177958_n() + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.func_177956_o() + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.func_177952_p() + 0.5 - this.field_70161_v;
        if (this.owner_flying != 0) {
            speed_factor = 1.75;
            if (this.func_70909_n() && this.func_70902_q() != null && this.func_70068_e((Entity)(e = this.func_70902_q())) > 49.0) {
                speed_factor = 3.5;
            }
        }
        this.field_70159_w += (Math.signum(var1) * 0.5 - this.field_70159_w) * 0.15 * speed_factor;
        this.field_70181_x += (Math.signum(var3) * 0.7 - this.field_70181_x) * 0.21 * speed_factor;
        this.field_70179_y += (Math.signum(var5) * 0.5 - this.field_70179_y) * 0.15 * speed_factor;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_191988_bg = (float)(0.75 * speed_factor);
        this.field_70177_z += var8 / 3.0f;
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
        if (par1EntityLiving instanceof Spyro) {
            return false;
        }
        return par1EntityLiving instanceof EntityMob;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.func_174813_aQ().func_72321_a(12.0, 6.0, 12.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (!this.isSuitableTarget(var4, false) || !this.canSeeTarget(var4.field_70165_t, var4.field_70163_u, var4.field_70161_v)) continue;
            return var4;
        }
        return null;
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_188429_b((ResourceLocation)new ResourceLocation(par1), (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }
}

