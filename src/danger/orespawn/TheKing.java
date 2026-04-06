/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAIHurtByTarget
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.boss.EntityDragon
 *  net.minecraft.entity.effect.EntityLightningBolt
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.passive.EntityHorse
 *  net.minecraft.entity.passive.EntityVillager
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.ChunkCoordinates
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.IChatComponent
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.BetterFireball;
import danger.orespawn.Boyfriend;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Ghost;
import danger.orespawn.GhostSkelly;
import danger.orespawn.Girlfriend;
import danger.orespawn.Godzilla;
import danger.orespawn.GodzillaHead;
import danger.orespawn.IceBall;
import danger.orespawn.KingHead;
import danger.orespawn.Kraken;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.PitchBlack;
import danger.orespawn.PurplePower;
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
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TheKing
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
    private int stream_count_i = 0;
    private int ticker = 0;
    private int player_hit_count = 0;
    private int backoff_timer = 0;
    private int guard_mode = 0;
    private volatile int head_found = 0;
    private int wing_sound = 0;
    private int large_unknown_detected = 0;
    private int isEnd = 0;
    private int endCounter = 0;

    public TheKing(World par1World) {
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
        this.attdam = OreSpawnMain.TheKing_stats.attack;
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(this.attdam);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
        this.field_70180_af.func_75682_a(21, (Object)OreSpawnMain.PlayNicely);
        this.field_70180_af.func_75682_a(22, (Object)this.isEnd);
    }

    public int getPlayNicely() {
        return this.field_70180_af.func_75679_c(21);
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

    public final int getAttacking() {
        return this.field_70180_af.func_75683_a(20);
    }

    public final void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
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
        return OreSpawnMain.TheKing_stats.health;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)Blocks.field_150327_N);
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(20) - (double)OreSpawnMain.OreSpawnRand.nextInt(20), this.field_70163_u + 12.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(20) - (double)OreSpawnMain.OreSpawnRand.nextInt(20), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int k;
        Item it = null;
        Block bl = null;
        TheKing.spawnCreature(this.field_70170_p, "The Prince", this.field_70165_t, this.field_70163_u + 10.0, this.field_70161_v);
        this.dropItemRand((Item)OreSpawnMain.RoyalBody, 1);
        this.dropItemRand((Item)OreSpawnMain.RoyalHelmet, 1);
        this.dropItemRand((Item)OreSpawnMain.RoyalLegs, 1);
        this.dropItemRand((Item)OreSpawnMain.RoyalBoots, 1);
        this.dropItemRand(OreSpawnMain.MyRoyal, 1);
        Iterator ilist = Item.field_150901_e.iterator();
        int icount = 0;
        while (ilist.hasNext()) {
            it = (Item)ilist.next();
            if (it == null) continue;
            ++icount;
        }
        int j = 0;
        while (j < 150) {
            ilist = Item.field_150901_e.iterator();
            for (k = 1 + this.field_70170_p.field_73012_v.nextInt(icount); k > 0 && ilist.hasNext(); --k) {
                it = (Item)ilist.next();
            }
            if (it == null) continue;
            ++j;
            this.dropItemRand(it, 1);
        }
        Iterator blist = Block.field_149771_c.iterator();
        int bcount = 0;
        while (blist.hasNext()) {
            bl = (Block)blist.next();
            if (bl == null) continue;
            ++bcount;
        }
        j = 0;
        while (j < 150) {
            blist = Block.field_149771_c.iterator();
            for (k = 1 + this.field_70170_p.field_73012_v.nextInt(bcount); k > 0 && blist.hasNext(); --k) {
                bl = (Block)blist.next();
            }
            if (bl == null) continue;
            ++j;
            this.dropItemRand(Item.func_150898_a((Block)bl), 1);
        }
    }

    protected boolean func_70650_aV() {
        return true;
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
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() * 2 / 3)) {
            this.attdam = OreSpawnMain.TheKing_stats.attack * 2;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
            this.attdam = OreSpawnMain.TheKing_stats.attack * 4;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 4)) {
            this.attdam = OreSpawnMain.TheKing_stats.attack * 8;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 8)) {
            this.attdam = OreSpawnMain.TheKing_stats.attack * 16;
        }
        if (this.field_70170_p.field_72995_K) {
            float f = 7.0f;
            this.isEnd = this.field_70180_af.func_75679_c(22);
            if (this.isEnd != 0 && this.field_70170_p.field_73012_v.nextInt(3) == 1) {
                for (int i = 0; i < 10; ++i) {
                    this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t - (double)f * Math.sin(Math.toRadians(this.field_70177_z)), this.field_70163_u + 14.0, this.field_70161_v + (double)f * Math.cos(Math.toRadians(this.field_70177_z)), (this.field_70170_p.field_73012_v.nextGaussian() - this.field_70170_p.field_73012_v.nextGaussian()) / 4.0 + this.field_70159_w * 6.0, (this.field_70170_p.field_73012_v.nextGaussian() - this.field_70170_p.field_73012_v.nextGaussian()) / 4.0, (this.field_70170_p.field_73012_v.nextGaussian() - this.field_70170_p.field_73012_v.nextGaussian()) / 4.0 + this.field_70179_y * 6.0);
                }
            }
        }
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean var4;
        float s;
        if (!(par1Entity == null || !(par1Entity instanceof EntityLivingBase) || !((s = par1Entity.field_70131_O * par1Entity.field_70130_N) > 30.0f) || MyUtils.isRoyalty(par1Entity) || par1Entity instanceof Godzilla || par1Entity instanceof GodzillaHead || par1Entity instanceof PitchBlack || par1Entity instanceof Kraken)) {
            EntityLivingBase e = (EntityLivingBase)par1Entity;
            e.func_70606_j(e.func_110143_aJ() / 2.0f);
            e.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), (float)this.attdam * 10.0f);
            this.large_unknown_detected = 1;
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
            double ks = 3.3;
            double inair = 0.25;
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

    private void msgToPlayers(String s) {
        List var5 = this.field_70170_p.func_72872_a(EntityPlayer.class, this.field_70121_D.func_72314_b(80.0, 64.0, 80.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityPlayer var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityPlayer)var3;
            var4.func_146105_b((IChatComponent)new ChatComponentText(s));
        }
    }

    private EntityPlayer findNearestPlayer() {
        List var5 = this.field_70170_p.func_72872_a(EntityPlayer.class, this.field_70121_D.func_72314_b(80.0, 64.0, 80.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityPlayer var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            if (var3 instanceof EntityPlayer) {
                var4 = (EntityPlayer)var3;
            }
            if (var4 == null) continue;
            break;
        }
        return var4;
    }

    protected void func_70619_bc() {
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
        EntityPlayer p = null;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        this.field_70180_af.func_75692_b(22, (Object)this.isEnd);
        this.field_70180_af.func_75692_b(21, (Object)OreSpawnMain.PlayNicely);
        if (this.isEnd == 1) {
            ++this.endCounter;
            this.field_70145_X = true;
            this.field_70159_w = 0.0;
            this.field_70181_x = 0.0;
            this.field_70179_y = 0.0;
            this.hurt_timer = 10;
            if (this.field_70128_L) {
                return;
            }
            p = this.findNearestPlayer();
            if (p != null) {
                float f2;
                this.func_70625_a((Entity)p, 10.0f, 10.0f);
                p.field_70159_w = 0.0;
                p.field_70181_x = 0.0;
                p.field_70179_y = 0.0;
                double dd0 = this.field_70165_t - p.field_70165_t;
                double dd1 = this.field_70161_v - p.field_70161_v;
                p.field_70177_z = f2 = (float)(Math.atan2(dd1, dd0) * 180.0 / Math.PI) - 90.0f;
                p.func_70606_j(1.0f);
            }
            if (this.endCounter == 10) {
                this.msgToPlayers("The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
                return;
            }
            if (this.endCounter == 80) {
                this.msgToPlayers("The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
                return;
            }
            if (this.endCounter == 160) {
                this.msgToPlayers("The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
                return;
            }
            if (this.endCounter == 240) {
                this.msgToPlayers("The King: I used you to learn your ways, and I have reached my conclusion on your species.");
                return;
            }
            if (this.endCounter == 300) {
                this.msgToPlayers("The King: You have 10 seconds to run...");
                return;
            }
            if (this.endCounter == 320) {
                this.msgToPlayers("9.");
                return;
            }
            if (this.endCounter == 340) {
                this.msgToPlayers("8.");
                return;
            }
            if (this.endCounter == 360) {
                this.msgToPlayers("7.");
                return;
            }
            if (this.endCounter == 380) {
                this.msgToPlayers("6.");
                return;
            }
            if (this.endCounter == 400) {
                this.msgToPlayers("5.");
                return;
            }
            if (this.endCounter == 420) {
                this.msgToPlayers("4.");
                return;
            }
            if (this.endCounter == 440) {
                this.msgToPlayers("3.");
                return;
            }
            if (this.endCounter == 460) {
                this.msgToPlayers("2.");
                return;
            }
            if (this.endCounter == 480) {
                this.msgToPlayers("1.");
                return;
            }
            if (this.endCounter == 500) {
                this.msgToPlayers("The King: Prepare to die!");
                this.isEnd = 2;
                return;
            }
            return;
        }
        if (this.isEnd == 2) {
            this.hurt_timer = 10;
            this.player_hit_count = 0;
            this.stream_count = 10;
            this.stream_count_l = 10;
            this.stream_count_i = 10;
            attrand = 3;
            this.guard_mode = 0;
            this.large_unknown_detected = 1;
            if (this.backoff_timer > 0) {
                --this.backoff_timer;
            }
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.homex == 0 && this.homez == 0 || this.guard_mode == 0) {
            this.homex = (int)this.field_70165_t;
            this.homez = (int)this.field_70161_v;
        }
        ++this.ticker;
        if (this.ticker > 30000) {
            this.ticker = 0;
        }
        if (this.ticker % 80 == 0) {
            this.stream_count = 10;
        }
        if (this.ticker % 90 == 0) {
            this.stream_count_l = 5;
        }
        if (this.ticker % 70 == 0) {
            this.stream_count_i = 8;
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
            for (int i = -5; i <= 5; i += 5) {
                block1: for (int j = -5; j <= 5; j += 5) {
                    int k;
                    Block bid = this.field_70170_p.func_147439_a(this.homex + j, (int)this.field_70163_u, this.homez + i);
                    if (bid != Blocks.field_150350_a) {
                        for (k = 1; k < 20; ++k) {
                            bid = this.field_70170_p.func_147439_a(this.homex + j, (int)this.field_70163_u + k, this.homez + i);
                            ++dist;
                            if (bid == Blocks.field_150350_a) continue block1;
                        }
                        continue;
                    }
                    for (k = 1; k < 20; ++k) {
                        bid = this.field_70170_p.func_147439_a(this.homex + j, (int)this.field_70163_u - k, this.homez + i);
                        --dist;
                        if (bid != Blocks.field_150350_a) continue block1;
                    }
                }
            }
            if ((int)(this.field_70163_u + (double)(dist = dist / 9 + 2)) > 230) {
                dist = 230 - (int)this.field_70163_u;
            }
            this.currentFlightTarget.func_71571_b(this.homex + xdir, (int)(this.field_70163_u + (double)dist), this.homez + zdir);
        } else if (this.field_70170_p.field_73012_v.nextInt(attrand) == 0) {
            e = this.rt;
            if (OreSpawnMain.PlayNicely != 0) {
                e = null;
            }
            if (e != null && (e instanceof TheKing || e instanceof KingHead)) {
                this.rt = null;
                e = null;
            }
            if (e != null) {
                float d1 = (float)(e.field_70165_t - (double)this.homex);
                float d2 = (float)(e.field_70161_v - (double)this.homez);
                d1 = (float)Math.sqrt(d1 * d1 + d2 * d2);
                if (e.field_70128_L || this.field_70170_p.field_73012_v.nextInt(250) == 1 || d1 > 128.0f && this.guard_mode == 1) {
                    e = null;
                    this.rt = null;
                }
                if (e != null && !this.MyCanSee(e)) {
                    e = null;
                }
            }
            f = this.findSomethingToAttack();
            if (this.head_found == 0) {
                EntityLiving entityLiving = (EntityLiving)TheKing.spawnCreature(this.field_70170_p, "KingHead", this.field_70165_t, this.field_70163_u + 20.0, this.field_70161_v);
            }
            if (e == null) {
                e = f;
            }
            if (e != null) {
                this.setAttacking(1);
                if (this.backoff_timer == 0) {
                    int dist = (int)(e.field_70163_u + (double)(e.field_70131_O / 2.0f) + 1.0);
                    if (dist > 230) {
                        dist = 230;
                    }
                    this.currentFlightTarget.func_71571_b((int)e.field_70165_t, dist, (int)e.field_70161_v);
                    if (this.field_70170_p.field_73012_v.nextInt(70) == 1) {
                        this.backoff_timer = 80 + this.field_70170_p.field_73012_v.nextInt(80);
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
                    for (int i = -5; i <= 5; i += 5) {
                        block5: for (int j = -5; j <= 5; j += 5) {
                            int k;
                            Block bid = this.field_70170_p.func_147439_a((int)e.field_70165_t + j, (int)this.field_70163_u, (int)e.field_70161_v + i);
                            if (bid != Blocks.field_150350_a) {
                                for (k = 1; k < 20; ++k) {
                                    bid = this.field_70170_p.func_147439_a((int)e.field_70165_t + j, (int)this.field_70163_u + k, (int)e.field_70161_v + i);
                                    ++dist;
                                    if (bid == Blocks.field_150350_a) continue block5;
                                }
                                continue;
                            }
                            for (k = 1; k < 20; ++k) {
                                bid = this.field_70170_p.func_147439_a((int)e.field_70165_t + j, (int)this.field_70163_u - k, (int)e.field_70161_v + i);
                                --dist;
                                if (bid != Blocks.field_150350_a) continue block5;
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
                        this.doJumpDamage(this.field_70165_t, this.field_70163_u, this.field_70161_v, 15.0, OreSpawnMain.TheKing_stats.attack / 4, 0);
                    }
                    this.func_70652_k((Entity)e);
                }
                double dx = this.field_70165_t + 20.0 * Math.sin(Math.toRadians(this.field_70759_as));
                double dz = this.field_70161_v - 20.0 * Math.cos(Math.toRadians(this.field_70759_as));
                if (this.field_70170_p.field_73012_v.nextInt(3) == 1) {
                    this.doJumpDamage(dx, this.field_70163_u + 10.0, dz, 15.0, OreSpawnMain.TheKing_stats.attack / 2, 1);
                }
                if (this.getHorizontalDistanceSqToEntity((Entity)e) > 900.0) {
                    which = this.field_70170_p.field_73012_v.nextInt(3);
                    if (which == 0) {
                        if (this.stream_count > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                            rhdir = Math.toRadians((this.field_70759_as + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            if ((rdd = Math.abs(rdd)) < 0.5) {
                                this.firecanon(e);
                            }
                        }
                    } else if (which == 1) {
                        if (this.stream_count_l > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                            rhdir = Math.toRadians((this.field_70759_as + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            if ((rdd = Math.abs(rdd)) < 0.5) {
                                this.firecanonl(e);
                            }
                        }
                    } else if (this.stream_count_i > 0) {
                        this.setAttacking(1);
                        rr = Math.atan2(e.field_70161_v - this.field_70161_v, e.field_70165_t - this.field_70165_t);
                        rhdir = Math.toRadians((this.field_70759_as + 90.0f) % 360.0f);
                        rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                        if (rdd > pi) {
                            rdd -= pi * 2.0;
                        }
                        if ((rdd = Math.abs(rdd)) < 0.5) {
                            this.firecanoni(e);
                        }
                    }
                }
            } else {
                this.setAttacking(0);
                this.stream_count = 10;
                this.stream_count_l = 5;
                this.stream_count_i = 8;
            }
        }
        if (this.getAttacking() != 0 && this.isEnd == 2) {
            double xzoff = 10.0;
            double yoff = 14.0;
            Entity ppwr = TheKing.spawnCreature(this.field_70170_p, "PurplePower", this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z)), this.field_70163_u + yoff, this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z)));
            if (ppwr != null) {
                PurplePower pwr = (PurplePower)ppwr;
                pwr.field_70159_w = this.field_70159_w * 3.0;
                pwr.field_70179_y = this.field_70179_y * 3.0;
                pwr.setPurpleType(10);
            }
        }
        var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.7 - this.field_70159_w) * 0.35;
        this.field_70181_x += (Math.signum(var3) * 0.69999 - this.field_70181_x) * 0.3;
        this.field_70179_y += (Math.signum(var5) * 0.7 - this.field_70179_y) * 0.35;
        var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 1.0f;
        this.field_70177_z += var8 / 8.0f;
        if (this.field_70170_p.field_73012_v.nextInt(30) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(5.0f);
            if (this.large_unknown_detected != 0) {
                this.func_70691_i(200.0f);
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
            --this.stream_count_l;
        }
    }

    private void firecanoni(EntityLivingBase e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double var3 = 0.0;
        double var5 = 0.0;
        double var7 = 0.0;
        float var9 = 0.0f;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        if (this.stream_count_i > 0) {
            this.field_70170_p.func_72956_a((Entity)this, "random.bow", 1.0f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
            for (int i = 0; i < 5; ++i) {
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
            --this.stream_count_i;
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
        Entity e = par1DamageSource.func_76346_g();
        if (e != null && e instanceof EntityLivingBase) {
            EntityLivingBase enl = (EntityLivingBase)e;
            float s = enl.field_70131_O * enl.field_70130_N;
            if (!(!(s > 30.0f) || MyUtils.isRoyalty((Entity)enl) || enl instanceof Godzilla || enl instanceof GodzillaHead || enl instanceof PitchBlack || enl instanceof Kraken)) {
                dm /= 10.0f;
                this.hurt_timer = 50;
                this.large_unknown_detected = 1;
            }
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
            if (e != null && e instanceof EntityLivingBase && this.currentFlightTarget != null && !MyUtils.isRoyalty(e)) {
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
        if (this.large_unknown_detected != 0) {
            return 25;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() * 2 / 3)) {
            return OreSpawnMain.TheKing_stats.defense + 1;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
            return OreSpawnMain.TheKing_stats.defense + 2;
        }
        if (this.player_hit_count < 10 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 4)) {
            return OreSpawnMain.TheKing_stats.defense + 3;
        }
        return OreSpawnMain.TheKing_stats.defense;
    }

    public void func_70077_a(EntityLightningBolt par1EntityLightningBolt) {
    }

    public void initCreature() {
    }

    public boolean MyCanSee(EntityLivingBase e) {
        double xzoff = 22.0;
        int nblks = 20;
        double cx = this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70177_z));
        double cz = this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z));
        float startx = (float)cx;
        float starty = (float)(this.field_70163_u + (double)(this.field_70131_O * 7.0f / 8.0f));
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
            if (bid == Blocks.field_150358_i || bid == Blocks.field_150355_j || bid == Blocks.field_150362_t || bid == Blocks.field_150395_bd || bid == Blocks.field_150350_a) continue;
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
        if (par1EntityLiving instanceof KingHead) {
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (this.isEnd == 2) {
            if (par1EntityLiving instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer)par1EntityLiving;
                return !p.field_71075_bZ.field_75098_d;
            }
            if (par1EntityLiving instanceof Girlfriend) {
                return true;
            }
            if (par1EntityLiving instanceof Boyfriend) {
                return true;
            }
            if (par1EntityLiving instanceof EntityVillager) {
                return true;
            }
        }
        if (!this.MyCanSee(par1EntityLiving)) {
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
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            this.head_found = 1;
            return null;
        }
        if (this.isEnd == 2) {
            List var5p = this.field_70170_p.func_72872_a(EntityPlayer.class, this.field_70121_D.func_72314_b(80.0, 64.0, 80.0));
            Collections.sort(var5p, this.TargetSorter);
            Iterator var2p = var5p.iterator();
            Entity var3p = null;
            EntityLivingBase var4p = null;
            Object retp = null;
            this.head_found = 1;
            while (var2p.hasNext()) {
                var3p = (Entity)var2p.next();
                var4p = (EntityLivingBase)var3p;
                if (!this.isSuitableTarget(var4p, false)) continue;
                return var4p;
            }
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(80.0, 64.0, 80.0));
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

    public void setFree() {
        this.isEnd = 1;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("KingHomeX", this.homex);
        par1NBTTagCompound.func_74768_a("KingHomeZ", this.homez);
        par1NBTTagCompound.func_74768_a("GuardMode", this.guard_mode);
        par1NBTTagCompound.func_74768_a("PlayerHits", this.player_hit_count);
        par1NBTTagCompound.func_74768_a("IsEnd", this.isEnd);
        par1NBTTagCompound.func_74768_a("EndCounter", this.endCounter);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.homex = par1NBTTagCompound.func_74762_e("KingHomeX");
        this.homez = par1NBTTagCompound.func_74762_e("KingHomeZ");
        this.guard_mode = par1NBTTagCompound.func_74762_e("GuardMode");
        this.player_hit_count = par1NBTTagCompound.func_74762_e("PlayerHits");
        this.isEnd = par1NBTTagCompound.func_74762_e("IsEnd");
        this.endCounter = par1NBTTagCompound.func_74762_e("EndCounter");
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

