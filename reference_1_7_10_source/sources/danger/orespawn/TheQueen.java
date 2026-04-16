/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.BetterFireball;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Ghost;
import danger.orespawn.GhostSkelly;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.PurplePower;
import danger.orespawn.QueenHead;
import danger.orespawn.TheKing;
import danger.orespawn.ThunderBolt;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class TheQueen
extends EntityMob {
    private ChunkCoordinates currentFlightTarget = null;
    private GenericTargetSorter TargetSorter = null;
    private EntityLivingBase rt = null;
    private double attdam = 250.0;
    private int hurt_timer = 0;
    private int homex = 0;
    private int homez = 0;
    private int stream_count = 0;
    private int stream_count_l = 0;
    private int ticker = 0;
    private int player_hit_count = 0;
    private int backoff_timer = 0;
    private int guard_mode = 0;
    private volatile int head_found = 0;
    private int wing_sound = 0;
    private int attack_level = 1;
    private EntityLivingBase ev = null;
    private float evh = 0.0f;
    private int mood = 0;
    private int always_mad = 0;

    public TheQueen(World par1World) {
        super(par1World);
        if (OreSpawnMain.PlayNicely == 0) {
            this.func_70105_a(22.0f, 24.0f);
        } else {
            this.func_70105_a(5.5f, 6.0f);
        }
        this.func_70661_as().func_75491_a(false);
        this.field_70728_aV = 25000;
        this.field_70178_ae = true;
        this.field_70174_ab = 5000;
        this.field_70145_X = true;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70155_l = 12.0;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.62f);
        this.attdam = OreSpawnMain.TheQueen_stats.attack;
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(this.attdam);
    }

    protected void func_70088_a() {
        int i = 0;
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)i);
        this.field_70180_af.func_75682_a(21, (Object)OreSpawnMain.PlayNicely);
        this.field_70180_af.func_75682_a(22, (Object)this.mood);
        this.field_70180_af.func_75682_a(23, (Object)this.attack_level);
    }

    public int getPlayNicely() {
        return this.field_70180_af.func_75679_c(21);
    }

    public int getIsHappy() {
        return this.field_70180_af.func_75679_c(22);
    }

    @SideOnly(value=Side.CLIENT)
    public boolean func_70112_a(double par1) {
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3) {
        return true;
    }

    protected boolean func_70692_ba() {
        return false;
    }

    public int getAttacking() {
        return this.field_70180_af.func_75679_c(20);
    }

    public void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)par1);
    }

    public int getPower() {
        return this.field_70180_af.func_75679_c(23);
    }

    public void setPower(int par1) {
        this.field_70180_af.func_75692_b(23, (Object)par1);
    }

    protected float func_70599_aP() {
        return 1.35f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected String func_70639_aQ() {
        return "orespawn:king_living";
    }

    protected String func_70621_aR() {
        return "orespawn:king_hit";
    }

    protected String func_70673_aS() {
        return "orespawn:trex_death";
    }

    public boolean func_70104_M() {
        return false;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.TheQueen_stats.health;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)Blocks.field_150327_N);
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(20) - (double)OreSpawnMain.OreSpawnRand.nextInt(20), this.field_70163_u + 12.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(20) - (double)OreSpawnMain.OreSpawnRand.nextInt(20), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        this.dropItemRand(OreSpawnMain.MyRoyal, 1);
        this.dropItemRand(OreSpawnMain.ThePrinceEgg, 1);
        TheQueen.spawnCreature((World)this.field_70170_p, (String)"The Princess", (double)this.field_70165_t, (double)(this.field_70163_u + 10.0), (double)this.field_70161_v);
        for (int i = 0; i < 56; ++i) {
            this.dropItemRand(OreSpawnMain.MyQueenScale, 1);
            this.dropItemRand(Items.field_151082_bd, 1);
            this.dropItemRand(Items.field_151103_aS, 1);
            this.dropItemRand(Items.field_151078_bh, 1);
        }
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public boolean isHappy() {
        return this.getIsHappy() == 0;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        ++this.wing_sound;
        if (this.wing_sound > 30) {
            if (!this.field_70170_p.field_72995_K) {
                this.field_70170_p.func_72956_a((Entity)this, "orespawn:MothraWings", 1.75f, 0.75f);
            }
            this.wing_sound = 0;
        }
        this.field_70145_X = true;
        this.field_70181_x *= 0.6;
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() * 3 / 4)) {
            this.attdam = OreSpawnMain.TheQueen_stats.attack * 20;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
            this.attdam = OreSpawnMain.TheQueen_stats.attack * 100;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 3)) {
            this.attdam = OreSpawnMain.TheQueen_stats.attack * 500;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 4)) {
            this.attdam = OreSpawnMain.TheQueen_stats.attack * 1000;
        }
        if (this.field_70170_p.field_72995_K && this.getPower() > 800) {
            float f = 7.0f;
            if (this.field_70170_p.field_73012_v.nextInt(4) == 1) {
                for (int i = 0; i < 10; ++i) {
                    this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t - (double)f * Math.sin(Math.toRadians(this.field_70177_z)), this.field_70163_u + 14.0, this.field_70161_v + (double)f * Math.cos(Math.toRadians(this.field_70177_z)), (this.field_70170_p.field_73012_v.nextGaussian() - this.field_70170_p.field_73012_v.nextGaussian()) / 5.0 + this.field_70159_w * 3.0, (this.field_70170_p.field_73012_v.nextGaussian() - this.field_70170_p.field_73012_v.nextGaussian()) / 5.0, (this.field_70170_p.field_73012_v.nextGaussian() - this.field_70170_p.field_73012_v.nextGaussian()) / 5.0 + this.field_70179_y * 3.0);
                }
            }
        }
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean var4;
        if (par1Entity != null && par1Entity instanceof EntityLivingBase && !this.field_70170_p.field_72995_K) {
            EntityLivingBase e = (EntityLivingBase)par1Entity;
            if (!e.field_70128_L) {
                if (this.ev == e) {
                    if (this.evh < e.func_110143_aJ()) {
                        e.func_70606_j(this.evh);
                    }
                } else {
                    this.ev = e;
                }
                if (e.field_70130_N * e.field_70131_O > 30.0f) {
                    e.func_70606_j(e.func_110143_aJ() * 3.0f / 4.0f);
                    e.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), (float)this.attdam);
                }
                this.evh = e.func_110143_aJ();
                if (this.evh <= 0.0f) {
                    this.ev.func_70106_y();
                }
            } else {
                this.ev = null;
                this.evh = 0.0f;
            }
        }
        if (par1Entity != null && par1Entity instanceof EntityDragon) {
            EntityDragon dr = (EntityDragon)par1Entity;
            DamageSource var21 = null;
            var21 = DamageSource.func_94539_a(null);
            var21.func_94540_d();
            if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                dr.func_70965_a(dr.field_70986_h, var21, (float)this.attdam);
            } else {
                dr.func_70965_a(dr.field_70987_i, var21, (float)this.attdam);
            }
        }
        if (var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), (float)this.attdam)) {
            double ks = 2.75;
            double inair = 0.2;
            float f3 = (float)Math.atan2(par1Entity.field_70161_v - this.field_70161_v, par1Entity.field_70165_t - this.field_70165_t);
            inair += (double)(this.field_70170_p.field_73012_v.nextFloat() * 0.25f);
            if (par1Entity.field_70128_L || par1Entity instanceof EntityPlayer) {
                inair *= 1.5;
            }
            par1Entity.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return var4;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 8.75), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    private boolean tooFarFromHome() {
        float d1 = (float)(this.field_70165_t - (double)this.homex);
        float d2 = (float)(this.field_70161_v - (double)this.homez);
        return (d1 = (float)Math.sqrt(d1 * d1 + d2 * d2)) > 120.0f;
    }

    protected void func_70619_bc() {
        Block bid;
        int k;
        int i;
        int j;
        int xdir = 1;
        int zdir = 1;
        int attrand = 5;
        boolean updown = false;
        int which = 0;
        EntityLivingBase e = null;
        EntityLivingBase f = null;
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        double pi = 3.1415926545;
        double var1 = 0.0;
        double var3 = 0.0;
        double var5 = 0.0;
        float var7 = 0.0f;
        float var8 = 0.0f;
        EntityLiving newent = null;
        double xzoff = 8.0;
        double yoff = 14.0;
        List kinglist = null;
        Iterator var2 = null;
        TheKing var4 = null;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.ev != null) {
            if (this.func_70068_e((Entity)this.ev) < 2000.0 && !this.ev.field_70128_L) {
                if (this.evh < this.ev.func_110143_aJ()) {
                    this.ev.func_70606_j(this.evh);
                } else {
                    this.evh = this.ev.func_110143_aJ();
                }
                if (this.evh <= 0.0f) {
                    this.ev.func_70106_y();
                }
            } else {
                this.ev = null;
                this.evh = 0.0f;
            }
        }
        if (this.attack_level > 1000) {
            if (this.mood == 1) {
                j = 15;
                if (this.player_hit_count < 10) {
                    j = 45;
                }
                for (i = 0; i < j; ++i) {
                    Entity ppwr = TheQueen.spawnCreature((World)this.field_70170_p, (String)"PurplePower", (double)(this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z))), (double)(this.field_70163_u + yoff), (double)(this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z))));
                    if (ppwr == null) continue;
                    ppwr.field_70159_w = this.field_70159_w * 3.0;
                    ppwr.field_70179_y = this.field_70179_y * 3.0;
                }
            } else {
                int m;
                if (this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                    block1: for (m = 0; m < 25; ++m) {
                        i = this.field_70170_p.field_73012_v.nextInt(25) - this.field_70170_p.field_73012_v.nextInt(25);
                        k = this.field_70170_p.field_73012_v.nextInt(25) - this.field_70170_p.field_73012_v.nextInt(25);
                        for (j = -20; j < 20; ++j) {
                            bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j, (int)this.field_70161_v + k);
                            if (bid == Blocks.field_150349_c) {
                                if (this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k) != Blocks.field_150350_a) continue block1;
                                which = this.field_70170_p.field_73012_v.nextInt(8);
                                if (which == 0) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, (Block)Blocks.field_150328_O);
                                }
                                if (which == 1) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, (Block)Blocks.field_150327_N);
                                }
                                if (which == 2) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, OreSpawnMain.MyFlowerBlueBlock);
                                }
                                if (which == 3) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, OreSpawnMain.MyFlowerPinkBlock);
                                }
                                if (which == 4) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, OreSpawnMain.CrystalFlowerRedBlock);
                                }
                                if (which == 5) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, OreSpawnMain.CrystalFlowerGreenBlock);
                                }
                                if (which == 6) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, OreSpawnMain.CrystalFlowerBlueBlock);
                                }
                                if (which != 7) continue block1;
                                this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, OreSpawnMain.CrystalFlowerYellowBlock);
                                continue block1;
                            }
                            if (bid == Blocks.field_150346_d && this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k) == Blocks.field_150350_a) {
                                this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j, (int)this.field_70161_v + k, (Block)Blocks.field_150349_c);
                                continue block1;
                            }
                            if (bid == Blocks.field_150348_b && this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k) == Blocks.field_150350_a) {
                                this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, Blocks.field_150346_d);
                                continue block1;
                            }
                            if (bid == Blocks.field_150354_m && this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k) == Blocks.field_150350_a) {
                                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                                    this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k, Blocks.field_150434_aF);
                                    continue block1;
                                }
                                this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j, (int)this.field_70161_v + k, Blocks.field_150346_d);
                                continue block1;
                            }
                            if (bid == Blocks.field_150353_l && this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k) == Blocks.field_150350_a) {
                                this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j, (int)this.field_70161_v + k, Blocks.field_150355_j);
                                continue block1;
                            }
                            if (bid == Blocks.field_150356_k && this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j + 1, (int)this.field_70161_v + k) == Blocks.field_150350_a) {
                                this.field_70170_p.func_147449_b((int)this.field_70165_t + i, (int)this.field_70163_u + j, (int)this.field_70161_v + k, (Block)Blocks.field_150358_i);
                                continue block1;
                            }
                            if (bid == Blocks.field_150350_a && j > 0) continue block1;
                        }
                    }
                }
                for (m = 0; m < 10; ++m) {
                    i = this.field_70170_p.field_73012_v.nextInt(15) - this.field_70170_p.field_73012_v.nextInt(15);
                    k = this.field_70170_p.field_73012_v.nextInt(15) - this.field_70170_p.field_73012_v.nextInt(15);
                    j = this.field_70170_p.field_73012_v.nextInt(20);
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + j, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150350_a) continue;
                    newent = this.field_70170_p.field_73012_v.nextInt(2) == 0 ? (EntityLiving)TheQueen.spawnCreature((World)this.field_70170_p, (String)"Butterfly", (double)(this.field_70165_t + (double)i), (double)(this.field_70163_u + (double)j), (double)(this.field_70161_v + (double)k)) : (EntityLiving)TheQueen.spawnCreature((World)this.field_70170_p, (String)"Bird", (double)(this.field_70165_t + (double)i), (double)(this.field_70163_u + (double)j), (double)(this.field_70161_v + (double)k));
                }
            }
            this.attack_level = 1;
        }
        if (this.attack_level > 1) {
            --this.attack_level;
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.homex == 0 && this.homez == 0 || this.guard_mode == 0) {
            this.homex = (int)this.field_70165_t;
            this.homez = (int)this.field_70161_v;
        }
        if (this.func_110143_aJ() > (float)(this.mygetMaxHealth() - 2) && this.field_70170_p.field_73012_v.nextInt(500) == 1) {
            this.mood = 0;
        }
        if (this.always_mad != 0) {
            this.mood = 1;
        }
        if (this.mood == 0) {
            this.attack_level += 10;
        }
        ++this.ticker;
        if (this.ticker > 30000) {
            this.ticker = 0;
        }
        if (this.ticker % 60 == 0) {
            this.stream_count = 10;
        }
        if (this.ticker % 70 == 0) {
            this.stream_count_l = 6;
        }
        if (this.ticker % 10 == 0) {
            this.field_70180_af.func_75692_b(21, (Object)OreSpawnMain.PlayNicely);
            this.field_70180_af.func_75692_b(22, (Object)this.mood);
            this.setPower(this.attack_level);
        }
        if (this.backoff_timer > 0) {
            --this.backoff_timer;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
            attrand = 3;
        }
        this.field_70145_X = true;
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.tooFarFromHome() || this.field_70170_p.field_73012_v.nextInt(200) == 0 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 9.1f) {
            zdir = this.field_70170_p.field_73012_v.nextInt(120);
            xdir = this.field_70170_p.field_73012_v.nextInt(120);
            if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                zdir = -zdir;
            }
            if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                xdir = -xdir;
            }
            int dist = 0;
            for (i = -5; i <= 5; i += 5) {
                block5: for (j = -5; j <= 5; j += 5) {
                    bid = this.field_70170_p.func_147439_a(this.homex + j, (int)this.field_70163_u, this.homez + i);
                    if (bid != Blocks.field_150350_a) {
                        for (k = 1; k < 20; ++k) {
                            bid = this.field_70170_p.func_147439_a(this.homex + j, (int)this.field_70163_u + k, this.homez + i);
                            ++dist;
                            if (bid == Blocks.field_150350_a) continue block5;
                        }
                        continue;
                    }
                    for (k = 1; k < 20; ++k) {
                        bid = this.field_70170_p.func_147439_a(this.homex + j, (int)this.field_70163_u - k, this.homez + i);
                        --dist;
                        if (bid != Blocks.field_150350_a) continue block5;
                    }
                }
            }
            if ((int)(this.field_70163_u + (double)(dist = dist / 9 + 2)) > 230) {
                dist = 230 - (int)this.field_70163_u;
            }
            this.currentFlightTarget.func_71571_b(this.homex + xdir, (int)(this.field_70163_u + (double)dist), this.homez + zdir);
            if (this.mood == 0 && (kinglist = this.field_70170_p.func_72872_a(TheKing.class, this.field_70121_D.func_72314_b(64.0, 32.0, 64.0))) != null) {
                Collections.sort(kinglist, this.TargetSorter);
                var2 = kinglist.iterator();
                if (var2.hasNext()) {
                    var4 = null;
                    var4 = (TheKing)var2.next();
                    this.guard_mode = 0;
                    zdir = this.field_70170_p.field_73012_v.nextInt(16);
                    xdir = this.field_70170_p.field_73012_v.nextInt(16);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                        zdir = -zdir;
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                        xdir = -xdir;
                    }
                    this.currentFlightTarget.func_71571_b((int)var4.field_70165_t + xdir, (int)(var4.field_70163_u + (double)(this.field_70170_p.field_73012_v.nextInt(8) - this.field_70170_p.field_73012_v.nextInt(8))), (int)var4.field_70161_v + zdir);
                }
            }
        } else if (this.field_70170_p.field_73012_v.nextInt(attrand) == 0) {
            float d1;
            e = this.rt;
            if (OreSpawnMain.PlayNicely != 0 || this.isHappy()) {
                e = null;
            }
            if (e != null && (e instanceof TheQueen || e instanceof QueenHead)) {
                this.rt = null;
                e = null;
            }
            if (e != null) {
                d1 = (float)(e.field_70165_t - (double)this.homex);
                float d2 = (float)(e.field_70161_v - (double)this.homez);
                d1 = (float)Math.sqrt(d1 * d1 + d2 * d2);
                if (e.field_70128_L || this.field_70170_p.field_73012_v.nextInt(450) == 1 || d1 > 128.0f && this.guard_mode == 1) {
                    e = null;
                    this.rt = null;
                }
                if (e != null && !this.MyCanSee(e)) {
                    e = null;
                }
            }
            f = this.findSomethingToAttack();
            if (this.head_found == 0 && this.mood == 1) {
                newent = (EntityLiving)TheQueen.spawnCreature((World)this.field_70170_p, (String)"QueenHead", (double)this.field_70165_t, (double)(this.field_70163_u + 20.0), (double)this.field_70161_v);
            }
            if (e == null) {
                e = f;
            }
            if (e != null) {
                d1 = e.field_70130_N * e.field_70131_O;
                if (this.attack_level < 1000) {
                    this.attack_level += 15;
                    if (this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
                        this.attack_level += 15;
                    }
                    if (d1 > 50.0f) {
                        this.attack_level += 15;
                    }
                    if (d1 > 100.0f) {
                        this.attack_level += 15;
                    }
                    if (d1 > 200.0f) {
                        this.attack_level += 25;
                    }
                }
                this.setAttacking(1);
                if (this.backoff_timer == 0) {
                    int dist = (int)(e.field_70163_u + (double)(e.field_70131_O / 2.0f) + 1.0);
                    if (dist > 230) {
                        dist = 230;
                    }
                    this.currentFlightTarget.func_71571_b((int)e.field_70165_t, dist, (int)e.field_70161_v);
                    if (this.field_70170_p.field_73012_v.nextInt(50) == 1) {
                        this.backoff_timer = 90 + this.field_70170_p.field_73012_v.nextInt(90);
                    }
                } else if (this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 9.1f) {
                    zdir = this.field_70170_p.field_73012_v.nextInt(20) + 30;
                    xdir = this.field_70170_p.field_73012_v.nextInt(20) + 30;
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                        zdir = -zdir;
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                        xdir = -xdir;
                    }
                    int dist = 0;
                    for (i = -5; i <= 5; i += 5) {
                        block9: for (j = -5; j <= 5; j += 5) {
                            bid = this.field_70170_p.func_147439_a((int)e.field_70165_t + j, (int)this.field_70163_u, (int)e.field_70161_v + i);
                            if (bid != Blocks.field_150350_a) {
                                for (k = 1; k < 20; ++k) {
                                    bid = this.field_70170_p.func_147439_a((int)e.field_70165_t + j, (int)this.field_70163_u + k, (int)e.field_70161_v + i);
                                    ++dist;
                                    if (bid == Blocks.field_150350_a) continue block9;
                                }
                                continue;
                            }
                            for (k = 1; k < 20; ++k) {
                                bid = this.field_70170_p.func_147439_a((int)e.field_70165_t + j, (int)this.field_70163_u - k, (int)e.field_70161_v + i);
                                --dist;
                                if (bid != Blocks.field_150350_a) continue block9;
                            }
                        }
                    }
                    if ((int)(this.field_70163_u + (double)(dist = dist / 9 + 2)) > 230) {
                        dist = 230 - (int)this.field_70163_u;
                    }
                    this.currentFlightTarget.func_71571_b((int)e.field_70165_t + xdir, (int)(this.field_70163_u + (double)dist), (int)e.field_70161_v + zdir);
                }
                if (this.func_70068_e((Entity)e) < 900.0) {
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        this.doJumpDamage(this.field_70165_t, this.field_70163_u, this.field_70161_v, 15.0, (double)(OreSpawnMain.TheQueen_stats.attack / 4), 0);
                    }
                    this.func_70652_k((Entity)e);
                }
                double dx = this.field_70165_t + 20.0 * Math.sin(Math.toRadians(this.field_70177_z));
                double dz = this.field_70161_v - 20.0 * Math.cos(Math.toRadians(this.field_70177_z));
                if (this.field_70170_p.field_73012_v.nextInt(3) == 1) {
                    this.doJumpDamage(dx, this.field_70163_u + 10.0, dz, 15.0, (double)(OreSpawnMain.TheQueen_stats.attack / 2), 1);
                }
                if (this.getHorizontalDistanceSqToEntity((Entity)e) > 900.0) {
                    which = this.field_70170_p.field_73012_v.nextInt(2);
                    if (which == 0) {
                        if (this.stream_count > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                            rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            if ((rdd = Math.abs(rdd)) < 0.5) {
                                this.firecanon(e);
                            }
                        }
                    } else if (this.stream_count_l > 0) {
                        this.setAttacking(1);
                        rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                        rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f);
                        rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                        if (rdd > pi) {
                            rdd -= pi * 2.0;
                        }
                        if ((rdd = Math.abs(rdd)) < 0.5) {
                            this.firecanonl(e);
                        }
                    }
                }
            } else {
                this.setAttacking(0);
                this.stream_count = 10;
                this.stream_count_l = 6;
            }
        }
        var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.65 - this.field_70159_w) * 0.35;
        this.field_70181_x += (Math.signum(var3) * 0.69999 - this.field_70181_x) * 0.3;
        this.field_70179_y += (Math.signum(var5) * 0.65 - this.field_70179_y) * 0.35;
        var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.75f;
        this.field_70177_z += var8 / 8.0f;
        if (this.field_70170_p.field_73012_v.nextInt(32) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(5.0f);
            if (this.player_hit_count < 10) {
                this.func_70691_i(50.0f);
            }
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < 2000.0f) {
            this.func_70691_i(2000.0f - this.func_110143_aJ());
        }
    }

    private double getHorizontalDistanceSqToEntity(Entity e) {
        double d1 = e.field_70161_v - this.field_70161_v;
        double d2 = e.field_70165_t - this.field_70165_t;
        return d1 * d1 + d2 * d2;
    }

    private void firecanon(EntityLivingBase e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        BetterFireball bf = null;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        if (this.stream_count > 0) {
            bf = new BetterFireball(this.field_70170_p, (EntityLivingBase)this, e.field_70165_t - cx, e.field_70163_u + (double)(e.field_70131_O / 2.0f) - (this.field_70163_u + yoff), e.field_70161_v - cz);
            bf.func_70012_b(cx, this.field_70163_u + yoff, cz, this.field_70177_z, 0.0f);
            bf.func_70107_b(cx, this.field_70163_u + yoff, cz);
            bf.setReallyBig();
            this.field_70170_p.func_72956_a((Entity)this, "random.fuse", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
            this.field_70170_p.func_72838_d((Entity)bf);
            for (int i = 0; i < 6; ++i) {
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
            --this.stream_count;
        }
    }

    private void firecanonl(EntityLivingBase e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double var3 = 0.0;
        double var5 = 0.0;
        double var7 = 0.0;
        float var9 = 0.0f;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        if (this.stream_count_l > 0) {
            this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
            for (int i = 0; i < 3; ++i) {
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
            --this.stream_count_l;
        }
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

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        float dm = par2;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (dm > 750.0f) {
            dm = 750.0f;
        }
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return false;
        }
        this.mood = 1;
        if (par1DamageSource.func_94541_c()) {
            float s = this.func_110143_aJ();
            if ((s += par2 / 2.0f) > this.func_110138_aP()) {
                s = this.func_110138_aP();
            }
            this.func_70606_j(s);
            return false;
        }
        Entity e = par1DamageSource.func_76346_g();
        if (e != null && e instanceof EntityLivingBase) {
            if (e instanceof PurplePower) {
                return false;
            }
            float s = e.field_70131_O * e.field_70130_N;
            if (e instanceof EntityMob && s < 3.0f) {
                e.func_70106_y();
                return false;
            }
        }
        if (!par1DamageSource.func_76355_l().equals("cactus")) {
            this.hurt_timer = 20;
            ret = super.func_70097_a(par1DamageSource, dm);
            if (e != null && e instanceof EntityPlayer) {
                ++this.player_hit_count;
            }
            if (e != null && e instanceof EntityLivingBase && this.currentFlightTarget != null && !MyUtils.isRoyalty((Entity)e)) {
                this.rt = (EntityLivingBase)e;
                int dist = (int)e.field_70163_u;
                if (dist > 230) {
                    dist = 230;
                }
                this.currentFlightTarget.func_71571_b((int)e.field_70165_t, dist, (int)e.field_70161_v);
            }
        }
        return ret;
    }

    public boolean func_70601_bi() {
        return true;
    }

    public int func_70658_aO() {
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() * 2 / 3)) {
            return OreSpawnMain.TheQueen_stats.defense + 2;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
            return OreSpawnMain.TheQueen_stats.defense + 3;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 3)) {
            return OreSpawnMain.TheQueen_stats.defense + 5;
        }
        return OreSpawnMain.TheQueen_stats.defense;
    }

    public void func_70077_a(EntityLightningBolt par1EntityLightningBolt) {
    }

    public void initCreature() {
    }

    public boolean MyCanSee(EntityLivingBase e) {
        double xzoff = 10.0;
        int nblks = 20;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        float startx = (float)cx;
        float starty = (float)(this.field_70163_u + 14.0);
        float startz = (float)cz;
        float dx = (float)((e.field_70165_t - (double)startx) / 20.0);
        float dy = (float)((e.field_70163_u + (double)(e.field_70131_O / 2.0f) - (double)starty) / 20.0);
        float dz = (float)((e.field_70161_v - (double)startz) / 20.0);
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
            if (bid == Blocks.field_150350_a) continue;
            return false;
        }
        return true;
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
        if (par1EntityLiving instanceof QueenHead) {
            this.head_found = 1;
            return false;
        }
        if (MyUtils.isRoyalty((Entity)par1EntityLiving)) {
            return false;
        }
        float d1 = (float)(par1EntityLiving.field_70165_t - (double)this.homex);
        float d2 = (float)(par1EntityLiving.field_70161_v - (double)this.homez);
        if ((d1 = (float)Math.sqrt(d1 * d1 + d2 * d2)) > 144.0f) {
            return false;
        }
        if (MyUtils.isIgnoreable((EntityLivingBase)par1EntityLiving)) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        if (par1EntityLiving instanceof EntityHorse) {
            return true;
        }
        if (par1EntityLiving instanceof EntityMob) {
            return true;
        }
        if (par1EntityLiving instanceof EntityDragon) {
            return true;
        }
        return MyUtils.isAttackableNonMob((EntityLivingBase)par1EntityLiving);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0 || this.isHappy()) {
            this.head_found = 1;
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(80.0, 60.0, 80.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        EntityLivingBase ret = null;
        this.head_found = 0;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (this.isSuitableTarget(var4, false) && ret == null) {
                ret = var4;
            }
            if (ret == null || this.head_found == 0) continue;
            break;
        }
        return ret;
    }

    public void setGuardMode(int i) {
        this.guard_mode = i;
    }

    public void setBadMood(int i) {
        this.always_mad = i;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("KingHomeX", this.homex);
        par1NBTTagCompound.func_74768_a("KingHomeZ", this.homez);
        par1NBTTagCompound.func_74768_a("GuardMode", this.guard_mode);
        par1NBTTagCompound.func_74768_a("PlayerHits", this.player_hit_count);
        par1NBTTagCompound.func_74768_a("MeanMode", this.always_mad);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.homex = par1NBTTagCompound.func_74762_e("KingHomeX");
        this.homez = par1NBTTagCompound.func_74762_e("KingHomeZ");
        this.guard_mode = par1NBTTagCompound.func_74762_e("GuardMode");
        this.player_hit_count = par1NBTTagCompound.func_74762_e("PlayerHits");
        this.always_mad = par1NBTTagCompound.func_74762_e("MeanMode");
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
        }
        return var8;
    }

    private EntityLivingBase doJumpDamage(double X, double Y, double Z, double dist, double damage, int knock) {
        AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)(X - dist), (double)(Y - 10.0), (double)(Z - dist), (double)(X + dist), (double)(Y + 10.0), (double)(Z + dist));
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, bb);
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (var4 == null || var4 == this || !var4.func_70089_S() || MyUtils.isRoyalty((Entity)var4) || var4 instanceof Ghost || var4 instanceof GhostSkelly) continue;
            DamageSource var21 = null;
            var21 = DamageSource.func_94539_a(null);
            var21.func_94540_d();
            var4.func_70097_a(var21, (float)damage / 2.0f);
            var4.func_70097_a(DamageSource.field_76379_h, (float)damage / 2.0f);
            this.field_70170_p.func_72956_a((Entity)var4, "random.explode", 0.65f, 1.0f + (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.5f);
            if (knock == 0) continue;
            double ks = 2.75;
            double inair = 0.65;
            float f3 = (float)Math.atan2(var4.field_70161_v - this.field_70161_v, var4.field_70165_t - this.field_70165_t);
            var4.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return null;
    }
}

