/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.BetterFireball;
import danger.orespawn.GammaMetroid;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.IceBall;
import danger.orespawn.Kraken;
import danger.orespawn.Leon;
import danger.orespawn.Mothra;
import danger.orespawn.MyEntityAIFollowOwner;
import danger.orespawn.MyEntityAIWander;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.RenderInfo;
import danger.orespawn.Spyro;
import danger.orespawn.ThePrince;
import danger.orespawn.ThePrinceAdult;
import danger.orespawn.ThunderBolt;
import danger.orespawn.WaterDragon;
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
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
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
public class ThePrinceTeen
extends EntityTameable {
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double boatYawHead;
    private int updateit = 1;
    private int playing = 0;
    private GenericTargetSorter TargetSorter = null;
    private RenderInfo renderdata = new RenderInfo();
    private int hurt_timer = 0;
    private int wing_sound = 0;
    private ChunkCoordinates currentFlightTarget = null;
    private boolean target_in_sight = false;
    private int owner_flying = 0;
    private int flyaway = 0;
    private float moveSpeed = 0.32f;
    private float deltasmooth = 0.0f;
    private int which_attack = 0;
    private int fireballticker = 0;
    private int head1ext = 0;
    private int head2ext = 0;
    private int head3ext = 0;
    private int head1dir = 1;
    private int head2dir = 1;
    private int head3dir = 1;
    private int kill_count = 0;
    private int day_count = 0;
    private int is_day = 0;

    public ThePrinceTeen(World par1World) {
        super(par1World);
        this.func_70105_a(3.25f, 4.25f);
        this.func_70661_as().func_75491_a(true);
        this.field_70728_aV = 300;
        this.field_70174_ab = 1000;
        this.field_70178_ae = true;
        this.func_70904_g(false);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIFollowOwner((EntityTameable)this, 1.1f, 12.0f, 2.0f));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAITempt((EntityCreature)this, 1.25, Items.field_151082_bd, false));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 0.75f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 9.0f));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIMoveIndoors((EntityCreature)this));
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityLiving.class, 0, true, false, IMob.field_82192_a));
        }
        this.field_70715_bh.func_75776_a(2, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
        this.field_70153_n = null;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.renderdata = new RenderInfo();
    }

    public ThePrinceTeen(World par1World, double par2, double par4, double par6) {
        this(par1World);
        this.func_70107_b(par2, par4 + (double)this.field_70129_M, par6);
        this.field_70159_w = 0.0;
        this.field_70181_x = 0.0;
        this.field_70179_y = 0.0;
        this.field_70169_q = par2;
        this.field_70167_r = par4;
        this.field_70166_s = par6;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(50.0);
    }

    public boolean shouldRiderSit() {
        return true;
    }

    public int getTrackingRange() {
        return 64;
    }

    public int getUpdateFrequency() {
        return 10;
    }

    public boolean sendsVelocityUpdates() {
        return true;
    }

    public int getHead1Ext() {
        return this.field_70180_af.func_75679_c(22);
    }

    public int getHead2Ext() {
        return this.field_70180_af.func_75679_c(23);
    }

    public int getHead3Ext() {
        return this.field_70180_af.func_75679_c(25);
    }

    public void setHead1Ext(int par1) {
        if (this.field_70170_p != null && this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(22, (Object)par1);
    }

    public void setHead2Ext(int par1) {
        if (this.field_70170_p != null && this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(23, (Object)par1);
    }

    public void setHead3Ext(int par1) {
        if (this.field_70170_p != null && this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(25, (Object)par1);
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    protected boolean func_70041_e_() {
        return true;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
        this.field_70180_af.func_75682_a(21, (Object)0);
        this.field_70180_af.func_75682_a(24, (Object)1);
        this.field_70180_af.func_75682_a(22, (Object)0);
        this.field_70180_af.func_75682_a(23, (Object)0);
        this.field_70180_af.func_75682_a(25, (Object)0);
        this.setActivity(0);
        this.setAttacking(0);
        this.func_70903_f(false);
        this.setThePrinceTeenFire(1);
        this.field_70145_X = false;
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

    public int mygetMaxHealth() {
        return 1500;
    }

    public int getThePrinceTeenHealth() {
        return (int)this.func_110143_aJ();
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
        return 18;
    }

    protected void func_70664_aZ() {
        super.func_70664_aZ();
        this.field_70181_x += 0.25;
    }

    public boolean func_70650_aV() {
        return true;
    }

    public boolean func_70648_aU() {
        return true;
    }

    public String func_70639_aQ() {
        if (this.func_70906_o()) {
            return null;
        }
        if (this.getActivity() == 1 && this.field_70153_n == null) {
            return "orespawn:roar";
        }
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:alo_hurt";
    }

    protected String func_70673_aS() {
        return "orespawn:alo_death";
    }

    protected float func_70599_aP() {
        return 0.6f;
    }

    public float func_70647_i() {
        return 0.75f;
    }

    public boolean func_70104_M() {
        return false;
    }

    public double func_70042_X() {
        return 2.75;
    }

    protected Item func_146068_u() {
        return OreSpawnMain.ThePrinceEgg;
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
        this.dropItemRand(OreSpawnMain.ThePrinceEgg, 1);
    }

    public boolean func_70652_k(Entity par1Entity) {
        double ks = 1.75;
        double inair = 0.1;
        float iskraken = 1.0f;
        if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
            EntityLiving e;
            if (par1Entity instanceof Kraken) {
                iskraken = 2.0f;
            }
            par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), iskraken * 45.0f);
            float f3 = (float)Math.atan2(par1Entity.field_70161_v - this.field_70161_v, par1Entity.field_70165_t - this.field_70165_t);
            if (par1Entity.field_70128_L || par1Entity instanceof EntityPlayer) {
                inair *= 2.0;
            }
            par1Entity.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            if (par1Entity instanceof EntityLiving && (e = (EntityLiving)par1Entity).func_110143_aJ() <= 0.0f) {
                ++this.kill_count;
            }
        }
        return true;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        Entity e = null;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (par1DamageSource.func_76355_l().equals("cactus")) {
            return ret;
        }
        if (par1DamageSource.func_76355_l().equals("inFire")) {
            return ret;
        }
        if (par1DamageSource.func_76355_l().equals("onFire")) {
            return ret;
        }
        if (par1DamageSource.func_76355_l().equals("lava")) {
            return ret;
        }
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return ret;
        }
        this.func_70904_g(false);
        this.setActivity(1);
        e = par1DamageSource.func_76346_g();
        if (e != null && e instanceof BetterFireball) {
            e.func_70106_y();
            return ret;
        }
        if (e != null && e instanceof EntitySmallFireball) {
            e.func_70106_y();
            return ret;
        }
        if (e != null && e instanceof ThePrinceTeen) {
            return false;
        }
        if (e != null && e instanceof Spyro) {
            return false;
        }
        ret = super.func_70097_a(par1DamageSource, par2);
        this.hurt_timer = 20;
        if (e != null && e instanceof EntityLivingBase) {
            if (this.func_70909_n() && e instanceof EntityPlayer) {
                return false;
            }
            this.func_70624_b((EntityLivingBase)e);
            this.func_70784_b(e);
            this.func_70661_as().func_75497_a((Entity)((EntityLivingBase)e), 1.2);
            ret = true;
        }
        return ret;
    }

    public void func_70619_bc() {
        EntityLivingBase e = null;
        super.func_70619_bc();
        if (!this.func_70906_o() && this.getActivity() == 0 && this.field_70153_n == null && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.field_70170_p.field_73012_v.nextInt(10) == 1) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setActivity(1);
            } else {
                this.setAttacking(0);
            }
        }
        if (this.kill_count > 25 && this.day_count > 10) {
            Entity ent = null;
            ThePrinceAdult d = null;
            ent = ThePrinceTeen.spawnCreature((World)this.field_70170_p, (String)"The Young Adult Prince", (double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            if (ent != null) {
                d = (ThePrinceAdult)ent;
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

    public void always_do() {
        EntityPlayer p = null;
        if (this.field_70170_p.field_73012_v.nextInt(250) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(2.0f);
        }
        if (this.field_70170_p.field_73012_v.nextInt(250) == 0) {
            this.func_70624_b(null);
        }
        if (this.func_70906_o()) {
            return;
        }
        this.owner_flying = 0;
        if (this.func_70909_n() && this.func_70902_q() != null && this.field_70153_n == null && !this.func_70906_o()) {
            p = (EntityPlayer)this.func_70902_q();
            if (p.field_71075_bZ.field_75100_b) {
                this.owner_flying = 1;
                this.setActivity(1);
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(50) == 1 && !this.func_70906_o() && !this.target_in_sight && this.field_70153_n == null) {
            if (this.field_70170_p.field_73012_v.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
    }

    public void fly_with_rider() {
        EntityLivingBase e = null;
        if (this.field_70128_L) {
            return;
        }
        if (this.func_70906_o()) {
            return;
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(5) == 1 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setAttacking(1);
                if (this.func_70068_e((Entity)e) < (double)((8.0f + e.field_70130_N / 2.0f) * (8.0f + e.field_70130_N / 2.0f))) {
                    this.func_70652_k((Entity)e);
                } else if (this.func_70068_e((Entity)e) > 100.0 && this.func_70068_e((Entity)e) < 625.0 && !this.func_70090_H() && this.getThePrinceTeenFire() != 0) {
                    this.shoot_something(e.field_70165_t, e.field_70163_u, e.field_70161_v);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    protected void func_70629_bd() {
        if (this.field_70153_n != null) {
            return;
        }
        super.func_70629_bd();
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
        if (par1EntityLiving instanceof Kraken) {
            return true;
        }
        if (par1EntityLiving instanceof Leon) {
            Leon l = (Leon)par1EntityLiving;
            return !l.func_70909_n();
        }
        if (par1EntityLiving instanceof WaterDragon) {
            WaterDragon l = (WaterDragon)par1EntityLiving;
            return !l.func_70909_n();
        }
        if (par1EntityLiving instanceof GammaMetroid) {
            GammaMetroid l = (GammaMetroid)par1EntityLiving;
            return !l.func_70909_n();
        }
        return false;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(25.0, 20.0, 25.0));
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

    public boolean func_145773_az() {
        return false;
    }

    public boolean func_70601_bi() {
        return false;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.75), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70056_a(double par1, double par3, double par5, float par7, float par8, int par9) {
        super.func_70056_a(par1, par3, par5, par7, par8, par9);
        this.boatPosRotationIncrements = par9;
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
        this.boatYawHead = par7;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70016_h(double par1, double par3, double par5) {
        super.func_70016_h(par1, par3, par5);
    }

    public void func_70071_h_() {
        EntityLivingBase e = null;
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
        this.field_70145_X = this.getActivity() != 0;
        if (!this.field_70170_p.field_72995_K) {
            int i;
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
            this.setHead1Ext(this.head1ext);
            this.setHead2Ext(this.head2ext);
            this.setHead3Ext(this.head3ext);
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.getActivity() == 1) {
            ++this.wing_sound;
            if (this.wing_sound > 20) {
                if (!this.field_70170_p.field_72995_K) {
                    this.field_70170_p.func_72956_a((Entity)this, "orespawn:MothraWings", 0.5f, 1.0f);
                }
                this.wing_sound = 0;
            }
        }
        if (this.func_70090_H()) {
            this.field_70181_x += 0.07;
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        if (this.getActivity() == 0 && this.func_70909_n() && this.func_70902_q() != null && !this.func_70906_o() && this.func_70068_e((Entity)(e = this.func_70902_q())) > 400.0) {
            this.setActivity(1);
        }
    }

    private void fly_without_rider() {
        Block bid;
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 10;
        boolean do_new = false;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        boolean has_owner = false;
        EntityLivingBase e = null;
        double speed_factor = 0.5;
        double var1 = 0.0;
        double var3 = 0.0;
        double var5 = 0.0;
        double gh = 1.25;
        double obstruction_factor = 0.0;
        double velocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
        boolean toofar = false;
        if (this.currentFlightTarget == null) {
            do_new = true;
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70153_n != null) {
            return;
        }
        if (this.func_70909_n() && this.func_70902_q() != null) {
            e = this.func_70902_q();
            has_owner = true;
            ox = e.field_70165_t;
            oy = e.field_70163_u;
            oz = e.field_70161_v;
            if (this.func_70068_e((Entity)e) > 400.0) {
                toofar = true;
                this.target_in_sight = false;
                this.setAttacking(0);
                this.func_70904_g(false);
                this.flyaway = 0;
                do_new = true;
            }
        }
        if (this.func_70906_o()) {
            return;
        }
        this.field_70181_x = this.field_70163_u < (double)this.currentFlightTarget.field_71572_b + 2.0 ? (this.field_70181_x *= 0.7) : (this.field_70163_u > (double)this.currentFlightTarget.field_71572_b - 2.0 ? (this.field_70181_x *= 0.5) : (this.field_70181_x *= 0.61));
        if (this.field_70170_p.field_73012_v.nextInt(300) == 1) {
            do_new = true;
        }
        if (this.flyaway > 0) {
            --this.flyaway;
        }
        if (!toofar && this.flyaway == 0 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.field_70170_p.field_73012_v.nextInt(7) == 1) {
            e = this.func_70638_az();
            if (e != null && !e.func_70089_S()) {
                this.func_70624_b(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                if (this.func_70909_n() && this.func_110143_aJ() / (float)this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(1);
                    this.setAttacking(0);
                    this.target_in_sight = false;
                    do_new = false;
                    this.currentFlightTarget.func_71571_b((int)(this.field_70165_t + (this.field_70165_t - e.field_70165_t)), (int)(this.field_70163_u + 1.0), (int)(this.field_70161_v + (this.field_70161_v - e.field_70161_v)));
                } else {
                    this.setActivity(1);
                    this.setAttacking(1);
                    this.target_in_sight = true;
                    this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + 1.0), (int)e.field_70161_v);
                    do_new = false;
                    if (this.func_70068_e((Entity)e) < (double)((8.0f + e.field_70130_N / 2.0f) * (8.0f + e.field_70130_N / 2.0f))) {
                        this.func_70652_k((Entity)e);
                        this.flyaway = 5 + this.field_70170_p.field_73012_v.nextInt(15);
                        do_new = true;
                    } else if (this.func_70068_e((Entity)e) < 400.0 && !this.func_70090_H() && this.getThePrinceTeenFire() != 0 && this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        this.shoot_something(e.field_70165_t, e.field_70163_u, e.field_70161_v);
                    }
                }
            } else {
                this.target_in_sight = false;
                this.flyaway = 0;
                this.setAttacking(0);
            }
        }
        if (this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 2.1f) {
            do_new = true;
        }
        if (do_new && !this.target_in_sight || do_new && this.flyaway != 0) {
            bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                int gox = (int)this.field_70165_t;
                int goy = (int)this.field_70163_u;
                int goz = (int)this.field_70161_v;
                if (has_owner) {
                    gox = (int)ox;
                    goy = (int)oy;
                    goz = (int)oz;
                    if (this.owner_flying == 0) {
                        zdir = this.field_70170_p.field_73012_v.nextInt(14) + 5;
                        xdir = this.field_70170_p.field_73012_v.nextInt(14) + 5;
                    } else {
                        zdir = this.field_70170_p.field_73012_v.nextInt(6);
                        xdir = this.field_70170_p.field_73012_v.nextInt(6);
                    }
                } else {
                    zdir = this.field_70170_p.field_73012_v.nextInt(10) + 16;
                    xdir = this.field_70170_p.field_73012_v.nextInt(10) + 16;
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    zdir = -zdir;
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.func_71571_b(gox + xdir, goy + this.field_70170_p.field_73012_v.nextInt(9 + this.owner_flying * 2) - 4, goz + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                if (bid == Blocks.field_150350_a && !this.canSeeTarget((double)this.currentFlightTarget.field_71574_a, (double)this.currentFlightTarget.field_71572_b, (double)this.currentFlightTarget.field_71573_c)) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
        }
        obstruction_factor = 0.0;
        int dist = 2;
        dist += (int)(velocity * 4.0);
        for (int k = 1; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                double dz;
                double dx = (double)i * Math.cos(Math.toRadians(this.field_70177_z + 90.0f));
                bid = this.field_70170_p.func_147439_a((int)(this.field_70165_t + dx), (int)this.field_70163_u - k, (int)(this.field_70161_v + (dz = (double)i * Math.sin(Math.toRadians(this.field_70177_z + 90.0f)))));
                if (bid == Blocks.field_150350_a) continue;
                obstruction_factor += 0.05;
            }
        }
        this.field_70181_x += obstruction_factor * 0.05;
        this.field_70163_u += obstruction_factor * 0.05;
        speed_factor = 0.5;
        var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
        if (this.owner_flying != 0) {
            speed_factor = 1.75;
            if (this.func_70909_n() && this.func_70902_q() != null && this.func_70068_e((Entity)(e = this.func_70902_q())) > 64.0) {
                speed_factor = 3.5;
            }
        }
        this.field_70159_w += (Math.signum(var1) - this.field_70159_w) * 0.15 * speed_factor;
        this.field_70181_x += (Math.signum(var3) - this.field_70181_x) * 0.21 * speed_factor;
        this.field_70179_y += (Math.signum(var5) - this.field_70179_y) * 0.15 * speed_factor;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = (float)(0.75 * speed_factor);
        this.field_70177_z += var8 / 4.0f;
        this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
    }

    public void func_70636_d() {
        List list = null;
        Entity listEntity = null;
        double d6 = this.field_70146_Z.nextFloat() * 2.0f - 1.0f;
        double d7 = (double)(this.field_70146_Z.nextInt(2) * 2 - 1) * 0.7;
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.95;
        double gh = 1.0;
        double rt = 0.0;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        if (this.getActivity() == 0) {
            super.func_70636_d();
        } else if (this.field_70128_L) {
            super.func_70636_d();
            return;
        }
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_72995_K) {
            if (this.boatPosRotationIncrements > 0 && this.getActivity() != 0) {
                double d4 = this.field_70165_t + (this.boatX - this.field_70165_t) / (double)this.boatPosRotationIncrements;
                double d5 = this.field_70163_u + (this.boatY - this.field_70163_u) / (double)this.boatPosRotationIncrements;
                double d11 = this.field_70161_v + (this.boatZ - this.field_70161_v) / (double)this.boatPosRotationIncrements;
                this.func_70107_b(d4, d5, d11);
                this.field_70125_A = (float)((double)this.field_70125_A + (this.boatPitch - (double)this.field_70125_A) / (double)this.boatPosRotationIncrements);
                double d10 = MathHelper.func_76138_g((double)(this.boatYaw - (double)this.field_70177_z));
                if (this.field_70153_n != null) {
                    d10 = MathHelper.func_76138_g((double)((double)this.field_70153_n.field_70177_z - (double)this.field_70177_z));
                }
                this.field_70177_z = (float)((double)this.field_70177_z + d10 / (double)this.boatPosRotationIncrements);
                this.func_70101_b(this.field_70177_z, this.field_70125_A);
                this.field_70759_as = this.field_70177_z;
                --this.boatPosRotationIncrements;
            }
        } else {
            if (this.getActivity() != 0) {
                if (this.fireballticker > 0) {
                    --this.fireballticker;
                }
                if (this.field_70153_n != null) {
                    double rdv;
                    EntityPlayer pp = (EntityPlayer)this.field_70153_n;
                    if (this.field_70159_w < -2.0) {
                        this.field_70159_w = -2.0;
                    }
                    if (this.field_70159_w > 2.0) {
                        this.field_70159_w = 2.0;
                    }
                    if (this.field_70179_y < -2.0) {
                        this.field_70179_y = -2.0;
                    }
                    if (this.field_70179_y > 2.0) {
                        this.field_70179_y = 2.0;
                    }
                    double velocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
                    gh = 1.25;
                    Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)((float)this.field_70163_u - (float)gh), (int)this.field_70161_v);
                    if (bid != Blocks.field_150350_a) {
                        this.field_70181_x += 0.03;
                        this.field_70163_u += 0.1;
                    } else {
                        this.field_70181_x -= 0.018;
                    }
                    obstruction_factor = 0.0;
                    dist = 3;
                    dist += (int)(velocity * 7.0);
                    for (int k = 1; k < dist; ++k) {
                        for (int i = 1; i < dist * 2; ++i) {
                            double dz;
                            double dx = (double)i * Math.cos(Math.toRadians(this.field_70177_z + 90.0f));
                            bid = this.field_70170_p.func_147439_a((int)(this.field_70165_t + dx), (int)this.field_70163_u - k, (int)(this.field_70161_v + (dz = (double)i * Math.sin(Math.toRadians(this.field_70177_z + 90.0f)))));
                            if (bid == Blocks.field_150350_a) continue;
                            obstruction_factor += 0.05;
                        }
                    }
                    this.field_70181_x += obstruction_factor * 0.07;
                    this.field_70163_u += obstruction_factor * 0.07;
                    if (this.field_70181_x > 2.0) {
                        this.field_70181_x = 2.0;
                    }
                    double d4 = this.field_70153_n.field_70177_z;
                    d4 %= 360.0;
                    while (d4 < 0.0) {
                        d4 += 360.0;
                    }
                    double d5 = this.field_70177_z;
                    d5 %= 360.0;
                    while (d5 < 0.0) {
                        d5 += 360.0;
                    }
                    for (relative_g = (d4 - d5) % 180.0; relative_g < 0.0; relative_g += 180.0) {
                    }
                    if (relative_g > 90.0) {
                        relative_g -= 180.0;
                    }
                    if (velocity > 0.01) {
                        d4 = 1.85 - velocity;
                        if ((d4 = Math.abs(d4)) < 0.01) {
                            d4 = 0.01;
                        }
                        if (d4 > 0.9) {
                            d4 = 0.9;
                        }
                        this.field_70177_z = this.field_70153_n.field_70177_z + (float)(relative_g * d4);
                    } else {
                        this.field_70177_z = this.field_70153_n.field_70177_z;
                    }
                    relative_g = Math.abs(relative_g) * velocity;
                    if (relative_g > 50.0) {
                        relative_g = 0.0;
                    }
                    this.field_70125_A = 2.0f * (float)velocity;
                    this.func_70101_b(this.field_70177_z, this.field_70125_A);
                    double newvelocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
                    double rr = Math.atan2(this.field_70153_n.field_70179_y, this.field_70153_n.field_70159_w);
                    double rhm = Math.atan2(this.field_70179_y, this.field_70159_w);
                    double rhdir = Math.toRadians((this.field_70153_n.field_70177_z + 90.0f) % 360.0f);
                    rt = 0.0;
                    pi = 3.1415926545;
                    deltav = 0.0;
                    float im = pp.field_70701_bs;
                    if (OreSpawnMain.flyup_keystate != 0) {
                        this.field_70181_x += 0.035;
                        this.field_70181_x += velocity * 0.046;
                    }
                    if ((rdv = Math.abs(rhm - rhdir) % (pi * 2.0)) > pi) {
                        rdv -= pi * 2.0;
                    }
                    rdv = Math.abs(rdv);
                    if (Math.abs(newvelocity) < 0.01) {
                        rdv = 0.0;
                    }
                    if (rdv > 1.5) {
                        newvelocity = -newvelocity;
                    }
                    if (Math.abs(im) > 0.001f) {
                        if (im > 0.0f) {
                            deltav = 0.025;
                            if (max_speed > 1.0) {
                                deltav += 0.05;
                            }
                            if (this.deltasmooth < 0.0f) {
                                this.deltasmooth = 0.0f;
                            }
                            this.deltasmooth = (float)((double)this.deltasmooth + deltav / 10.0);
                            if ((double)this.deltasmooth > deltav) {
                                this.deltasmooth = (float)deltav;
                            }
                        } else {
                            max_speed = 0.35;
                            deltav = -0.02;
                            if (this.deltasmooth > 0.0f) {
                                this.deltasmooth = 0.0f;
                            }
                            this.deltasmooth = (float)((double)this.deltasmooth + deltav / 10.0);
                            if ((double)this.deltasmooth < deltav) {
                                this.deltasmooth = (float)deltav;
                            }
                        }
                        if ((newvelocity += (double)this.deltasmooth) >= 0.0) {
                            if (newvelocity > max_speed) {
                                newvelocity = max_speed;
                            }
                            this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                            this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                        } else {
                            if (newvelocity < -max_speed) {
                                newvelocity = -max_speed;
                            }
                            newvelocity = -newvelocity;
                            this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 270.0f)) * newvelocity;
                            this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 270.0f)) * newvelocity;
                        }
                    } else if (newvelocity >= 0.0) {
                        this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                        this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                    } else {
                        this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 270.0f)) * (newvelocity * -1.0);
                        this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 270.0f)) * (newvelocity * -1.0);
                    }
                    if (this.fireballticker == 0 && (pp.field_70702_br < -0.001f || pp.field_70702_br > 0.001f)) {
                        double cz;
                        double cx;
                        double yoff = 1.5;
                        double xzoff = 7.5;
                        ++this.which_attack;
                        if (this.which_attack > 2) {
                            this.which_attack = 0;
                        }
                        if (this.which_attack == 0) {
                            cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z - 10.0f));
                            cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z - 10.0f));
                            BetterFireball bf = new BetterFireball(this.field_70170_p, (EntityLivingBase)this, 0.0, 0.0, 0.0);
                            bf.setNotMe();
                            bf.func_70107_b(cx, this.field_70163_u + (yoff += (double)((float)this.getHead1Ext() * 0.04f)), cz);
                            cx = Math.cos(Math.toRadians(pp.field_70759_as + 90.0f));
                            cz = Math.sin(Math.toRadians(pp.field_70759_as + 90.0f));
                            double cy = -Math.sin(Math.toRadians(pp.field_70125_A));
                            double d3 = MathHelper.func_76133_a((double)(cx * cx + cy * cy + cz * cz));
                            bf.field_70232_b = cx / d3 * 0.1;
                            bf.field_70233_c = cy / d3 * 0.1;
                            bf.field_70230_d = cz / d3 * 0.1;
                            bf.field_70159_w = this.field_70159_w;
                            bf.field_70181_x = this.field_70181_x;
                            bf.field_70179_y = this.field_70179_y;
                            bf.field_70165_t -= this.field_70159_w * 3.0;
                            bf.field_70163_u -= this.field_70181_x * 3.0;
                            bf.field_70161_v -= this.field_70179_y * 3.0;
                            this.field_70170_p.func_72956_a((Entity)this, "random.fuse", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
                            this.field_70170_p.func_72838_d((Entity)bf);
                        }
                        if (this.which_attack == 1) {
                            cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z + 10.0f));
                            cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z + 10.0f));
                            IceBall var2 = new IceBall(this.field_70170_p, cx, this.field_70163_u + (yoff += (double)((float)this.getHead3Ext() * 0.04f)), cz);
                            var2.func_70012_b(cx, this.field_70163_u + yoff, cz, pp.field_70177_z + 90.0f, pp.field_70125_A);
                            var2.setIceMaker(1);
                            double var3 = Math.cos(Math.toRadians(pp.field_70177_z + 90.0f));
                            double var5 = -Math.sin(Math.toRadians(pp.field_70125_A));
                            double var77 = Math.sin(Math.toRadians(pp.field_70177_z + 90.0f));
                            float var9 = MathHelper.func_76133_a((double)(var3 * var3 + var77 * var77)) * 0.2f;
                            var2.func_70186_c(var3, var5 + (double)var9, var77, 1.4f, 5.0f);
                            var2.field_70165_t -= this.field_70159_w * 3.0;
                            var2.field_70163_u -= this.field_70181_x * 3.0;
                            var2.field_70161_v -= this.field_70179_y * 3.0;
                            var2.field_70159_w *= 2.0;
                            var2.field_70181_x *= 2.0;
                            var2.field_70179_y *= 2.0;
                            this.field_70170_p.func_72956_a((Entity)this, "fireworks.launch", 0.75f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
                            this.field_70170_p.func_72838_d((Entity)var2);
                        }
                        if (this.which_attack == 2) {
                            cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
                            cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
                            ThunderBolt lb = new ThunderBolt(this.field_70170_p, (EntityLivingBase)pp);
                            lb.func_70012_b(cx, this.field_70163_u + (yoff += (double)((float)this.getHead2Ext() * 0.04f)), cz, pp.field_70177_z + 90.0f, pp.field_70125_A);
                            lb.field_70159_w *= 3.0;
                            lb.field_70181_x *= 3.0;
                            lb.field_70179_y *= 3.0;
                            this.field_70170_p.func_72956_a((Entity)this, "random.bow", 0.75f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
                            this.field_70170_p.func_72838_d((Entity)lb);
                        }
                        this.fireballticker = 10;
                    }
                    this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
                    this.field_70159_w *= 0.985;
                    this.field_70181_x *= 0.94;
                    this.field_70179_y *= 0.985;
                    if (!this.field_70170_p.field_72995_K && (list = this.field_70170_p.func_72839_b((Entity)this, this.field_70121_D.func_72314_b(3.25, 4.0, 3.25))) != null && !list.isEmpty()) {
                        for (int l = 0; l < list.size(); ++l) {
                            listEntity = (Entity)list.get(l);
                            if (listEntity == this.field_70153_n || listEntity.field_70128_L || !listEntity.func_70104_M()) continue;
                            listEntity.func_70108_f((Entity)this);
                        }
                    }
                    this.fly_with_rider();
                    if (this.field_70153_n != null && this.field_70153_n.field_70128_L) {
                        this.field_70153_n = null;
                    }
                } else {
                    this.fly_without_rider();
                }
            }
            this.always_do();
        }
    }

    public void func_70043_V() {
        if (this.field_70153_n != null) {
            float f = 0.65f;
            this.field_70153_n.func_70107_b(this.field_70165_t - (double)f * Math.sin(Math.toRadians(this.field_70177_z)), this.field_70163_u + this.func_70042_X() + this.field_70153_n.func_70033_W(), this.field_70161_v + (double)f * Math.cos(Math.toRadians(this.field_70177_z)));
        }
    }

    protected void func_70908_e(boolean par1) {
        String s = "heart";
        if (!par1) {
            s = "smoke";
        }
        for (int i = 0; i < 20; ++i) {
            double d0 = this.field_70146_Z.nextGaussian() * 0.08;
            double d1 = this.field_70146_Z.nextGaussian() * 0.08;
            double d2 = this.field_70146_Z.nextGaussian() * 0.08;
            this.field_70170_p.func_72869_a(s, this.field_70165_t + (double)((this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 2.5f), this.field_70163_u + 0.5 + (double)this.field_70146_Z.nextFloat() * 1.5, this.field_70161_v + (double)((this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 2.5f), d0, d1, d2);
        }
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150484_ah) && par1EntityPlayer.func_70068_e((Entity)this) < 25.0) {
            if (!this.field_70170_p.field_72995_K) {
                this.func_70903_f(true);
                this.func_152115_b(par1EntityPlayer.func_110124_au().toString());
                this.func_70908_e(true);
                this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                this.kill_count = 1000;
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
        if (this.func_70909_n()) {
            if (!this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
                return false;
            }
            if (var2 == null && par1EntityPlayer.func_70068_e((Entity)this) < 25.0) {
                if (!this.field_70170_p.field_72995_K) {
                    par1EntityPlayer.func_70078_a((Entity)this);
                    this.setActivity(1);
                    this.func_70904_g(false);
                }
                return true;
            }
            if (var2 != null && var2.func_77973_b() == Items.field_151082_bd && par1EntityPlayer.func_70068_e((Entity)this) < 25.0) {
                if (this.field_70170_p.field_72995_K) {
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
                if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                    this.func_70691_i((float)this.mygetMaxHealth() - this.func_110143_aJ());
                }
                if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                    --var2.field_77994_a;
                    if (var2.field_77994_a <= 0) {
                        par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                    }
                }
                return true;
            }
            if (var2 != null && par1EntityPlayer.func_70068_e((Entity)this) < 25.0 && var2.func_77973_b() instanceof ItemFood) {
                if (!this.field_70170_p.field_72995_K) {
                    ItemFood var3 = (ItemFood)var2.func_77973_b();
                    if ((float)this.mygetMaxHealth() > this.func_110143_aJ()) {
                        this.func_70691_i((float)(var3.func_150905_g(var2) * 10));
                    }
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)7);
                }
                if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                    --var2.field_77994_a;
                    if (var2.field_77994_a <= 0) {
                        par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                    }
                }
                return true;
            }
            if (var2 != null && var2.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150432_aD) && par1EntityPlayer.func_70068_e((Entity)this) < 25.0) {
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                    this.setThePrinceTeenFire(0);
                    String healthMessage = new String();
                    healthMessage = String.format("Fireballs extinguished.", new Object[0]);
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
            if (var2 != null && var2.func_77973_b() == Items.field_151033_d && par1EntityPlayer.func_70068_e((Entity)this) < 25.0) {
                if (!this.field_70170_p.field_72995_K) {
                    this.func_70908_e(true);
                    this.field_70170_p.func_72960_a((Entity)this, (byte)6);
                    this.setThePrinceTeenFire(1);
                    String healthMessage = new String();
                    healthMessage = String.format("Fireballs lit!", new Object[0]);
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
            if (var2 != null && var2.func_77973_b() == Items.field_151045_i && par1EntityPlayer.func_70068_e((Entity)this) < 25.0 && !this.field_70170_p.field_72995_K) {
                Entity ent = null;
                ThePrince d = null;
                ent = ThePrinceTeen.spawnCreature((World)this.field_70170_p, (String)"The Prince", (double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
                if (ent != null) {
                    d = (ThePrince)ent;
                    if (this.func_70909_n()) {
                        d.func_70903_f(true);
                        d.func_152115_b(par1EntityPlayer.func_110124_au().toString());
                        d.set_ok_to_grow();
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
            if (this.func_70909_n() && var2 != null && var2.func_77973_b() == Items.field_151057_cb && par1EntityPlayer.func_70068_e((Entity)this) < 25.0 && this.func_152114_e((EntityLivingBase)par1EntityPlayer)) {
                this.func_94058_c(var2.func_82833_r());
                if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                    --var2.field_77994_a;
                    if (var2.field_77994_a <= 0) {
                        par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                    }
                }
                return true;
            }
            if (var2 != null && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
                if (!this.func_70906_o()) {
                    this.func_70904_g(true);
                    this.setActivity(0);
                } else {
                    this.func_70904_g(false);
                    this.setActivity(0);
                }
                return true;
            }
        }
        return false;
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151082_bd;
    }

    public int getAttacking() {
        return this.field_70180_af.func_75679_c(20);
    }

    public void setAttacking(int par1) {
        if (this.field_70170_p != null && this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(20, (Object)par1);
    }

    public int getActivity() {
        return this.field_70180_af.func_75679_c(21);
    }

    public void setActivity(int par1) {
        if (this.field_70170_p != null && this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(21, (Object)par1);
    }

    public int getThePrinceTeenFire() {
        return this.field_70180_af.func_75679_c(24);
    }

    public void setThePrinceTeenFire(int par1) {
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(24, (Object)par1);
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

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return null;
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        if (this.field_70153_n != null) {
            return false;
        }
        return !this.func_70909_n();
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("ThePrinceTeenAttacking", this.getAttacking());
        par1NBTTagCompound.func_74768_a("ThePrinceTeenActivity", this.getActivity());
        par1NBTTagCompound.func_74768_a("ThePrinceTeenFire", this.getThePrinceTeenFire());
        par1NBTTagCompound.func_74768_a("SpyroKill", this.kill_count);
        par1NBTTagCompound.func_74768_a("SpyroDay", this.day_count);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.setAttacking(par1NBTTagCompound.func_74762_e("ThePrinceTeenAttacking"));
        this.setActivity(par1NBTTagCompound.func_74762_e("ThePrinceTeenActivity"));
        this.setThePrinceTeenFire(par1NBTTagCompound.func_74762_e("ThePrinceTeenFire"));
        this.kill_count = par1NBTTagCompound.func_74762_e("SpyroKill");
        this.day_count = par1NBTTagCompound.func_74762_e("SpyroDay");
    }

    private void shoot_something(double x, double y, double z) {
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        double pi = 3.1415926545;
        int which = this.field_70170_p.field_73012_v.nextInt(3);
        if (which == 0) {
            rr = Math.atan2(z - this.field_70161_v, x - this.field_70165_t);
            rdd = Math.abs(rr - (rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f))) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            if ((rdd = Math.abs(rdd)) < 0.5) {
                this.firecanon(x, y, z);
            }
        } else if (which == 1) {
            rr = Math.atan2(z - this.field_70161_v, x - this.field_70165_t);
            rdd = Math.abs(rr - (rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f))) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            if ((rdd = Math.abs(rdd)) < 0.5) {
                this.firecanonl(x, y, z);
            }
        } else {
            rr = Math.atan2(z - this.field_70161_v, x - this.field_70165_t);
            rdd = Math.abs(rr - (rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f))) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            if ((rdd = Math.abs(rdd)) < 0.5) {
                this.firecanoni(x, y, z);
            }
        }
    }

    private void firecanon(double x, double y, double z) {
        double yoff = 3.5;
        double xzoff = 6.0;
        BetterFireball bf = null;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        float r1 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r2 = 3.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        float r3 = 5.0f * (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat());
        bf = new BetterFireball(this.field_70170_p, (EntityLivingBase)this, x - cx + (double)r1, y + 0.25 - (this.field_70163_u + yoff) + (double)r2, z - cz + (double)r3);
        bf.func_70012_b(cx, this.field_70163_u + yoff, cz, this.field_70177_z, 0.0f);
        bf.func_70107_b(cx, this.field_70163_u + yoff, cz);
        bf.setBig();
        this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
        this.field_70170_p.func_72838_d((Entity)bf);
    }

    private void firecanonl(double x, double y, double z) {
        double yoff = 3.5;
        double xzoff = 6.0;
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
        var3 = x - lb.field_70165_t;
        var5 = y + 0.25 - lb.field_70163_u;
        var7 = z - lb.field_70161_v;
        var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
        lb.func_70186_c(var3, var5 + (double)var9, var7, 1.4f, 4.0f);
        lb.field_70159_w *= 3.0;
        lb.field_70181_x *= 3.0;
        lb.field_70179_y *= 3.0;
        this.field_70170_p.func_72838_d((Entity)lb);
    }

    private void firecanoni(double x, double y, double z) {
        double yoff = 3.5;
        double xzoff = 6.0;
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
        var3 = x - lb.field_70165_t;
        var5 = y + 0.25 - lb.field_70163_u;
        var7 = z - lb.field_70161_v;
        var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
        lb.func_70186_c(var3, var5 + (double)var9, var7, 1.4f, 4.0f);
        lb.field_70159_w *= 3.0;
        lb.field_70181_x *= 3.0;
        lb.field_70179_y *= 3.0;
        this.field_70170_p.func_72838_d((Entity)lb);
    }
}

