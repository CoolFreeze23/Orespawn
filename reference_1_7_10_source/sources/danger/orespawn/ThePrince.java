/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.BetterFireball;
import danger.orespawn.Cockateil;
import danger.orespawn.Dragonfly;
import danger.orespawn.EntityButterfly;
import danger.orespawn.EntityMosquito;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.IceBall;
import danger.orespawn.Mothra;
import danger.orespawn.MyEntityAIFollowOwner;
import danger.orespawn.MyEntityAIWander;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.ThePrinceTeen;
import danger.orespawn.ThunderBolt;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class ThePrince
extends EntityTameable {
    private ChunkCoordinates currentFlightTarget;
    private GenericTargetSorter TargetSorter = null;
    public int activity = 1;
    private int owner_flying = 0;
    private float moveSpeed = 0.3f;
    private int syncit = 0;
    private int head1ext = 0;
    private int head2ext = 0;
    private int head3ext = 0;
    private int head1dir = 1;
    private int head2dir = 1;
    private int head3dir = 1;
    private int ok_to_grow = 0;
    private int kill_count = 0;
    private int fed_count = 0;
    private int day_count = 0;
    private int is_day = 0;

    public ThePrince(World par1World) {
        super(par1World);
        this.func_70105_a(0.75f, 1.25f);
        this.moveSpeed = 0.32f;
        this.field_70174_ab = 1000;
        this.field_70178_ae = true;
        this.func_70661_as().func_75491_a(true);
        this.func_70904_g(false);
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIFollowOwner((EntityTameable)this, 1.15f, 12.0f, 2.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.25, Items.field_151082_bd, false));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 6.0f));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 0.75f));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70728_aV = 50;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(10.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.activity = 1;
        this.field_70180_af.func_75682_a(22, (Object)0);
        this.field_70180_af.func_75682_a(21, (Object)this.activity);
        this.field_70180_af.func_75682_a(20, (Object)1);
        this.func_70904_g(false);
        this.func_70903_f(false);
        this.field_70145_X = false;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("SpyroActivity", this.field_70180_af.func_75679_c(21));
        par1NBTTagCompound.func_74768_a("SpyroFire", this.field_70180_af.func_75679_c(20));
        par1NBTTagCompound.func_74768_a("SpyroGrow", this.ok_to_grow);
        par1NBTTagCompound.func_74768_a("SpyroKill", this.kill_count);
        par1NBTTagCompound.func_74768_a("SpyroFed", this.fed_count);
        par1NBTTagCompound.func_74768_a("SpyroDay", this.day_count);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.activity = par1NBTTagCompound.func_74762_e("SpyroActivity");
        this.field_70180_af.func_75692_b(21, (Object)this.activity);
        this.field_70180_af.func_75692_b(20, (Object)par1NBTTagCompound.func_74762_e("SpyroFire"));
        this.ok_to_grow = par1NBTTagCompound.func_74762_e("SpyroGrow");
        this.kill_count = par1NBTTagCompound.func_74762_e("SpyroKill");
        this.fed_count = par1NBTTagCompound.func_74762_e("SpyroFed");
        this.day_count = par1NBTTagCompound.func_74762_e("SpyroDay");
    }

    public int getActivity() {
        int i;
        this.activity = i = this.field_70180_af.func_75679_c(21);
        return i;
    }

    public void setActivity(int par1) {
        this.activity = par1;
        this.field_70180_af.func_75692_b(21, (Object)0);
        this.field_70180_af.func_75692_b(21, (Object)par1);
    }

    public int getSpyroFire() {
        return this.field_70180_af.func_75679_c(20);
    }

    public void setSpyroFire(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)par1);
    }

    public int getAttacking() {
        return this.field_70180_af.func_75679_c(22);
    }

    public void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(22, (Object)par1);
    }

    public int getHead1Ext() {
        return this.head1ext;
    }

    public int getHead2Ext() {
        return this.head2ext;
    }

    public int getHead3Ext() {
        return this.head3ext;
    }

    public boolean func_70650_aV() {
        return true;
    }

    public boolean func_70648_aU() {
        return true;
    }

    public int mygetMaxHealth() {
        return 500;
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150484_ah) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70903_f(true);
                this.func_152115_b(par1EntityPlayer.func_110124_au().toString());
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                this.ok_to_grow = 1;
                this.kill_count = 1000;
                this.fed_count = 1000;
                this.day_count = 1000;
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && this.func_152114_e((EntityLivingBase)par1EntityPlayer) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && var2.func_77973_b() instanceof ItemFood) {
            if (!this.field_70170_p.field_72995_K) {
                ItemFood var3 = (ItemFood)var2.func_77973_b();
                if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                    this.func_70691_i((float)(var3.func_150905_g(var2) * 10));
                }
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                ++this.fed_count;
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150432_aD) && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                this.setSpyroFire(0);
                String healthMessage = new String();
                healthMessage = String.format("Prince fireballs extinguished.", new Object[0]);
                par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText(healthMessage));
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151033_d && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                this.setSpyroFire(1);
                String healthMessage = new String();
                healthMessage = String.format("Prince fireballs lit!", new Object[0]);
                par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText(healthMessage));
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (var2 != null && var2.func_77973_b() == Items.field_151045_i && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer) && !this.field_70170_p.field_72995_K && this.ok_to_grow != 0) {
            Entity ent = null;
            ThePrinceTeen d = null;
            ent = ThePrince.spawnCreature((World)this.field_70170_p, (String)"The Young Prince", (double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            if (ent != null) {
                d = (ThePrinceTeen)ent;
                if (this.func_70909_n()) {
                    d.func_70903_f(true);
                    d.func_152115_b(par1EntityPlayer.func_110124_au().toString());
                }
                this.func_70106_y();
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151057_cb && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            this.func_94058_c(var2.func_82833_r());
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.func_70909_n() && par1EntityPlayer.func_70068_e((Entity)this) < 16.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
            if (!this.func_70906_o()) {
                this.func_70904_g(true);
                this.setActivity(1);
            } else {
                this.func_70904_g(false);
            }
            return true;
        }
        return super.func_70085_c(par1EntityPlayer);
    }

    public void set_ok_to_grow() {
        this.ok_to_grow = 1;
        this.kill_count = 0;
        this.fed_count = 0;
        this.day_count = 0;
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151082_bd;
    }

    protected boolean func_70692_ba() {
        return false;
    }

    protected String func_70639_aQ() {
        if (this.func_70906_o()) {
            return null;
        }
        if (this.getAttacking() == 0) {
            return null;
        }
        return "orespawn:roar";
    }

    protected String func_70621_aR() {
        return "orespawn:duck_hurt";
    }

    protected String func_70673_aS() {
        return "orespawn:cryo_death";
    }

    protected float func_70599_aP() {
        return 0.6f;
    }

    public int func_70658_aO() {
        return 16;
    }

    protected Item func_146068_u() {
        return Items.field_151082_bd;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var3 = 0;
        var3 = this.field_70170_p.field_73012_v.nextInt(4);
        ++var3;
        for (int var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(Items.field_151082_bd, 1);
        }
    }

    protected float func_70647_i() {
        return (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 0.2f + 1.3f;
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

    public boolean func_70601_bi() {
        return true;
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    public float getAttackStrength(Entity par1Entity) {
        return 10.0f;
    }

    public boolean func_70652_k(Entity par1Entity) {
        EntityLivingBase el;
        float var2 = this.getAttackStrength(par1Entity);
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), var2);
        if (par1Entity instanceof EntityLivingBase && (el = (EntityLivingBase)par1Entity).func_110143_aJ() <= 0.0f) {
            ++this.kill_count;
        }
        return var4;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return false;
        }
        if (!par1DamageSource.func_76355_l().equals("cactus")) {
            ret = super.func_70097_a(par1DamageSource, par2);
            this.func_70904_g(false);
            this.setActivity(2);
        }
        return ret;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.75), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    public void func_70071_h_() {
        int i;
        super.func_70071_h_();
        this.field_70145_X = this.getActivity() == 2;
        if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
            i = this.field_70170_p.field_73012_v.nextInt(3);
            if (i == 0) {
                this.head1dir = 2;
            }
            if (i == 1) {
                this.head1dir = -2;
            }
            if (i == 2) {
                this.head1dir = 0;
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
            i = this.field_70170_p.field_73012_v.nextInt(3);
            if (i == 0) {
                this.head2dir = 2;
            }
            if (i == 1) {
                this.head2dir = -2;
            }
            if (i == 2) {
                this.head2dir = 0;
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
            i = this.field_70170_p.field_73012_v.nextInt(3);
            if (i == 0) {
                this.head3dir = 2;
            }
            if (i == 1) {
                this.head3dir = -2;
            }
            if (i == 2) {
                this.head3dir = 0;
            }
        }
        this.head1ext += this.head1dir;
        if (this.head1ext < 0) {
            this.head1ext = 0;
        }
        if (this.head1ext > 60) {
            this.head1ext = 60;
        }
        this.head2ext += this.head2dir;
        if (this.head2ext < 0) {
            this.head2ext = 0;
        }
        if (this.head2ext > 60) {
            this.head2ext = 60;
        }
        this.head3ext += this.head3dir;
        if (this.head3ext < 0) {
            this.head3ext = 0;
        }
        if (this.head3ext > 60) {
            this.head3ext = 60;
        }
    }

    public void func_70636_d() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70636_d();
        if (this.func_70090_H()) {
            this.field_70181_x += 0.07;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        ++this.syncit;
        if (this.syncit > 20) {
            this.syncit = 0;
            if (this.field_70170_p.field_72995_K) {
                this.getActivity();
            } else {
                int j = this.activity;
                this.setActivity(j);
            }
        }
        if (this.activity == 2) {
            this.field_70181_x *= 0.6;
        }
    }

    protected void func_70619_bc() {
        EntityLivingBase e;
        EntityPlayer p;
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        if (this.activity != 2) {
            super.func_70619_bc();
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(1.0f);
        }
        if (!this.func_70909_n() && (p = this.field_70170_p.func_72890_a((Entity)this, 10.0)) != null) {
            this.func_70903_f(true);
            this.func_152115_b(p.func_110124_au().toString());
            this.func_70908_e(true);
            this.field_70170_p.func_72960_a((Entity)this, (byte)7);
            this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
        }
        if (!this.func_70906_o()) {
            if (this.activity == 0) {
                this.setActivity(1);
            }
            if (this.field_70170_p.field_73012_v.nextInt(100) == 1) {
                if (this.field_70170_p.field_73012_v.nextInt(20) == 1) {
                    this.setActivity(2);
                } else {
                    this.setActivity(1);
                }
            }
            this.owner_flying = 0;
            if (this.func_70909_n() && this.func_70902_q() != null) {
                e = (EntityPlayer)this.func_70902_q();
                if (e.field_71075_bZ.field_75100_b) {
                    this.owner_flying = 1;
                    this.setActivity(2);
                }
            }
            if (this.activity == 1 && this.func_70909_n() && this.func_70902_q() != null && this.func_70068_e((Entity)(e = this.func_70902_q())) > 256.0) {
                this.setActivity(2);
            }
            this.do_movement();
        } else if (this.func_70909_n() && this.func_70902_q() != null && this.func_70068_e((Entity)(e = this.func_70902_q())) > 256.0) {
            this.func_70904_g(false);
            this.setActivity(2);
        }
        if (this.kill_count > 25 && this.fed_count > 10 && this.day_count > 10) {
            Entity ent = null;
            ThePrinceTeen d = null;
            ent = ThePrince.spawnCreature((World)this.field_70170_p, (String)"The Young Prince", (double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            if (ent != null) {
                d = (ThePrinceTeen)ent;
                if (this.func_70909_n()) {
                    d.func_70903_f(true);
                    d.func_152115_b(this.func_152113_b());
                }
                this.func_70106_y();
            }
        }
        if (this.is_day == 0) {
            this.is_day = 1;
            if (!this.field_70170_p.func_72935_r()) {
                this.is_day = -1;
            }
        } else {
            if (this.is_day == -1 && this.field_70170_p.func_72935_r()) {
                ++this.day_count;
            }
            this.is_day = 1;
            if (!this.field_70170_p.func_72935_r()) {
                this.is_day = -1;
            }
        }
    }

    private void do_movement() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 10;
        boolean do_new = false;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        boolean has_owner = false;
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        double pi = 3.1415926545;
        EntityLivingBase e = null;
        if (this.currentFlightTarget == null) {
            do_new = true;
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.activity == 2 && this.field_70170_p.field_73012_v.nextInt(300) == 0) {
            do_new = true;
        }
        if (this.func_70909_n() && this.func_70902_q() != null) {
            e = this.func_70902_q();
            has_owner = true;
            ox = e.field_70165_t;
            oy = e.field_70163_u + 1.0;
            oz = e.field_70161_v;
            if (this.func_70068_e((Entity)e) > 100.0) {
                do_new = true;
            }
            if (this.owner_flying != 0 && this.func_70068_e((Entity)e) > 36.0) {
                do_new = true;
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(7) == 1 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.func_70909_n() && this.func_110143_aJ() / (float)this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(2);
                    this.setAttacking(0);
                    do_new = false;
                    this.currentFlightTarget.func_71571_b((int)(this.field_70165_t + (this.field_70165_t - e.field_70165_t)), (int)(this.field_70163_u + 1.0), (int)(this.field_70161_v + (this.field_70161_v - e.field_70161_v)));
                } else {
                    this.setActivity(2);
                    this.setAttacking(1);
                    this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + 1.0), (int)e.field_70161_v);
                    do_new = false;
                    if (this.func_70068_e((Entity)e) < (double)((3.0f + e.field_70130_N / 2.0f) * (3.0f + e.field_70130_N / 2.0f))) {
                        this.func_70652_k((Entity)e);
                    } else if (this.func_70068_e((Entity)e) > 25.0 && this.func_70068_e((Entity)e) < 144.0 && !this.func_70090_H() && this.getSpyroFire() != 0 && (this.field_70170_p.field_73012_v.nextInt(3) == 0 || this.field_70170_p.field_73012_v.nextInt(4) == 1)) {
                        int which = this.field_70170_p.field_73012_v.nextInt(3);
                        if (which == 0) {
                            rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                            rdd = Math.abs(rr - (rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f))) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            if ((rdd = Math.abs(rdd)) < 0.5) {
                                this.firecanon(e);
                            }
                        } else if (which == 1) {
                            rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                            rdd = Math.abs(rr - (rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f))) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            if ((rdd = Math.abs(rdd)) < 0.5) {
                                this.firecanonl(e);
                            }
                        } else {
                            rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                            rdd = Math.abs(rr - (rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f))) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            if ((rdd = Math.abs(rdd)) < 0.5) {
                                this.firecanoni(e);
                            }
                        }
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.activity == 1) {
            return;
        }
        if (this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 2.1f) {
            do_new = true;
        }
        if (do_new) {
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
                        zdir = this.field_70170_p.field_73012_v.nextInt(8);
                        xdir = this.field_70170_p.field_73012_v.nextInt(8);
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
                this.currentFlightTarget.func_71571_b(gox + xdir, goy + (this.field_70170_p.field_73012_v.nextInt(6 + this.owner_flying * 2) - 2), goz + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                --keep_trying;
            }
        }
        double speed_factor = 1.0;
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
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
        this.field_70701_bs = (float)(0.75 * speed_factor);
        this.field_70177_z += var8 / 3.0f;
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
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (MyUtils.isRoyalty((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityMob) {
            return true;
        }
        if (par1EntityLiving instanceof Mothra) {
            return true;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return true;
        }
        if (par1EntityLiving instanceof Cockateil) {
            return true;
        }
        if (par1EntityLiving instanceof Dragonfly) {
            return true;
        }
        return par1EntityLiving instanceof EntityMosquito;
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
            if (!this.isSuitableTarget(var4, false) || !this.canSeeTarget(var4.field_70165_t, var4.field_70163_u, var4.field_70161_v)) continue;
            return var4;
        }
        return null;
    }

    private void firecanon(EntityLivingBase e) {
        double yoff = 1.0;
        double xzoff = 3.0;
        BetterFireball bf = null;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        float r1 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r2 = 3.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r3 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        bf = new BetterFireball(this.field_70170_p, (EntityLivingBase)this, e.field_70165_t - cx + (double)r1, e.field_70163_u + (double)(e.field_70131_O / 2.0f) - (this.field_70163_u + yoff) + (double)r2, e.field_70161_v - cz + (double)r3);
        bf.func_70012_b(cx, this.field_70163_u + yoff, cz, this.field_70177_z, 0.0f);
        bf.func_70107_b(cx, this.field_70163_u + yoff, cz);
        bf.setBig();
        if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
            bf.setSmall();
        }
        this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
        this.field_70170_p.func_72838_d((Entity)bf);
    }

    private void firecanonl(EntityLivingBase e) {
        double yoff = 1.0;
        double xzoff = 3.0;
        double var3 = 0.0;
        double var5 = 0.0;
        double var7 = 0.0;
        float var9 = 0.0f;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
        float r1 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r2 = 3.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r3 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        ThunderBolt lb = new ThunderBolt(this.field_70170_p, cx, this.field_70163_u + yoff, cz);
        lb.func_70012_b(cx, this.field_70163_u + yoff, cz, 0.0f, 0.0f);
        var3 = e.field_70165_t - lb.field_70165_t;
        var5 = e.field_70163_u + 0.25 - lb.field_70163_u;
        var7 = e.field_70161_v - lb.field_70161_v;
        var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
        lb.func_70186_c(var3, var5 + (double)var9, var7, 1.4f, 4.0f);
        lb.field_70159_w *= 3.0;
        lb.field_70181_x *= 3.0;
        lb.field_70179_y *= 3.0;
        this.field_70170_p.func_72838_d((Entity)lb);
    }

    private void firecanoni(EntityLivingBase e) {
        double yoff = 1.0;
        double xzoff = 3.0;
        double var3 = 0.0;
        double var5 = 0.0;
        double var7 = 0.0;
        float var9 = 0.0f;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
        float r1 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r2 = 3.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r3 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        IceBall lb = new IceBall(this.field_70170_p, cx, this.field_70163_u + yoff, cz);
        lb.setIceMaker(1);
        lb.func_70012_b(cx, this.field_70163_u + yoff, cz, 0.0f, 0.0f);
        var3 = e.field_70165_t - lb.field_70165_t;
        var5 = e.field_70163_u + 0.25 - lb.field_70163_u;
        var7 = e.field_70161_v - lb.field_70161_v;
        var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
        lb.func_70186_c(var3, var5 + (double)var9, var7, 1.4f, 4.0f);
        lb.field_70159_w *= 3.0;
        lb.field_70181_x *= 3.0;
        lb.field_70179_y *= 3.0;
        this.field_70170_p.func_72838_d((Entity)lb);
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }
}

