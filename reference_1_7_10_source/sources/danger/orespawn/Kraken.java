/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.AttackSquid;
import danger.orespawn.Cephadrome;
import danger.orespawn.Chipmunk;
import danger.orespawn.Dragon;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Leon;
import danger.orespawn.Mothra;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.RenderInfo;
import danger.orespawn.Spyro;
import danger.orespawn.StinkBug;
import danger.orespawn.ThePrinceAdult;
import danger.orespawn.ThePrinceTeen;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

/*
 * Exception performing whole class analysis ignored.
 */
public class Kraken
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private RenderInfo renderdata = new RenderInfo();
    private ChunkCoordinates currentFlightTarget = null;
    private EntityLivingBase caught = null;
    private int newtarget = 0;
    private int release = 0;
    private int weather_set = 10;
    private int long_enough = 3600;
    private int call_reinforcements = 0;
    private boolean hit_by_player = false;
    private int straight_down = 1;
    private int hurt_timer = 0;

    public Kraken(World par1World) {
        super(par1World);
        if (OreSpawnMain.PlayNicely == 0) {
            this.func_70105_a(4.0f, 15.0f);
        } else {
            this.func_70105_a(1.3333334f, 5.0f);
        }
        this.func_70661_as().func_75491_a(false);
        this.field_70728_aV = 500;
        this.field_70174_ab = 120;
        this.field_70178_ae = true;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.renderdata = new RenderInfo();
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.37f);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.Kraken_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
        this.field_70180_af.func_75682_a(21, (Object)OreSpawnMain.PlayNicely);
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

    public int getPlayNicely() {
        return this.field_70180_af.func_75679_c(21);
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.Kraken_stats.health;
    }

    public int getKrakenHealth() {
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
        return OreSpawnMain.Kraken_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
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

    public void func_70071_h_() {
        super.func_70071_h_();
        if (this.field_70128_L) {
            return;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)(this.field_70163_u - 10.0), (int)this.field_70161_v);
        } else {
            this.field_70181_x = this.field_70163_u < (double)this.currentFlightTarget.field_71572_b ? (this.field_70181_x *= 0.72) : (this.field_70181_x *= 0.5);
        }
        if (this.weather_set > 0 && OreSpawnMain.PlayNicely == 0) {
            --this.weather_set;
            if (this.weather_set == 0 && !this.field_70170_p.field_72995_K) {
                WorldInfo worldinfo = this.field_70170_p.func_72912_H();
                if (!this.field_70170_p.func_72896_J()) {
                    worldinfo.func_76080_g(300);
                    worldinfo.func_76090_f(300);
                    worldinfo.func_76084_b(true);
                    worldinfo.func_76069_a(true);
                } else {
                    worldinfo.func_76080_g(300);
                    worldinfo.func_76090_f(300);
                }
                this.weather_set = 100;
            }
        }
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("LongEnough", this.long_enough);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.long_enough = par1NBTTagCompound.func_74762_e("LongEnough");
    }

    protected String func_70639_aQ() {
        if (this.field_70146_Z.nextInt(5) == 0) {
            return "orespawn:kraken_living";
        }
        return null;
    }

    protected String func_70621_aR() {
        return null;
    }

    protected String func_70673_aS() {
        return "orespawn:alo_death";
    }

    protected float func_70599_aP() {
        return 2.0f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return Items.field_151128_bU;
    }

    private ItemStack dropItemRand(Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(8) - (double)OreSpawnMain.OreSpawnRand.nextInt(8), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(8) - (double)OreSpawnMain.OreSpawnRand.nextInt(8), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        ItemStack is = null;
        this.dropItemRand(OreSpawnMain.MyKrakenTooth, 1);
        this.dropItemRand(Items.field_151160_bD, 1);
        int var5 = 120 + this.field_70170_p.field_73012_v.nextInt(160);
        for (var4 = 0; var4 < var5; ++var4) {
            this.dropItemRand(Items.field_151100_aR, 1);
        }
        int i = 5 + this.field_70170_p.field_73012_v.nextInt(10);
        block56: for (var4 = 0; var4 < i; ++var4) {
            int var3 = this.field_70170_p.field_73012_v.nextInt(53);
            switch (var3) {
                case 0: {
                    is = this.dropItemRand(OreSpawnMain.MyUltimateSword, 1);
                    continue block56;
                }
                case 1: {
                    is = this.dropItemRand(Items.field_151045_i, 1);
                    continue block56;
                }
                case 2: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150484_ah), 1);
                    continue block56;
                }
                case 3: {
                    is = this.dropItemRand(Items.field_151048_u, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77336_l, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77337_m, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77335_o, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77334_n, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 4: {
                    is = this.dropItemRand(Items.field_151047_v, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 5: {
                    is = this.dropItemRand(Items.field_151046_w, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 6: {
                    is = this.dropItemRand(Items.field_151056_x, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 7: {
                    is = this.dropItemRand(Items.field_151012_L, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 8: {
                    is = this.dropItemRand((Item)Items.field_151161_ac, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77340_h, 1 + this.field_70170_p.field_73012_v.nextInt(2));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 9: {
                    is = this.dropItemRand((Item)Items.field_151163_ad, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 10: {
                    is = this.dropItemRand((Item)Items.field_151173_ae, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 11: {
                    is = this.dropItemRand((Item)Items.field_151175_af, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 12: {
                    is = this.dropItemRand(OreSpawnMain.MyUltimateBow, 1);
                    continue block56;
                }
                case 13: {
                    is = this.dropItemRand(OreSpawnMain.MyUltimateAxe, 1);
                    continue block56;
                }
                case 14: {
                    is = this.dropItemRand(Items.field_151042_j, 1);
                    continue block56;
                }
                case 15: {
                    is = this.dropItemRand(OreSpawnMain.MyUltimatePickaxe, 1);
                    continue block56;
                }
                case 16: {
                    is = this.dropItemRand(Items.field_151040_l, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77336_l, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77337_m, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77335_o, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77334_n, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 17: {
                    is = this.dropItemRand(Items.field_151037_a, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 18: {
                    is = this.dropItemRand(Items.field_151035_b, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 19: {
                    is = this.dropItemRand(Items.field_151036_c, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 20: {
                    is = this.dropItemRand(Items.field_151019_K, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 21: {
                    is = this.dropItemRand((Item)Items.field_151028_Y, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77340_h, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 22: {
                    is = this.dropItemRand((Item)Items.field_151030_Z, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 23: {
                    is = this.dropItemRand((Item)Items.field_151165_aa, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 24: {
                    is = this.dropItemRand((Item)Items.field_151167_ab, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 25: {
                    is = this.dropItemRand(OreSpawnMain.MyUltimateShovel, 1);
                    continue block56;
                }
                case 26: {
                    this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150339_S), 1);
                    continue block56;
                }
                case 27: {
                    is = this.dropItemRand(Items.field_151074_bl, 1);
                    continue block56;
                }
                case 28: {
                    is = this.dropItemRand(Items.field_151043_k, 1);
                    continue block56;
                }
                case 29: {
                    is = this.dropItemRand(Items.field_151150_bK, 1);
                    continue block56;
                }
                case 30: {
                    is = this.dropItemRand(Items.field_151010_B, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77336_l, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77337_m, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77335_o, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77334_n, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 31: {
                    is = this.dropItemRand(Items.field_151011_C, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 32: {
                    is = this.dropItemRand(Items.field_151005_D, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 33: {
                    is = this.dropItemRand(Items.field_151006_E, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 34: {
                    is = this.dropItemRand(Items.field_151013_M, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 35: {
                    is = this.dropItemRand((Item)Items.field_151169_ag, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77340_h, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 36: {
                    is = this.dropItemRand((Item)Items.field_151171_ah, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 37: {
                    is = this.dropItemRand((Item)Items.field_151149_ai, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 38: {
                    is = this.dropItemRand((Item)Items.field_151151_aj, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 39: {
                    this.dropItemRand(Items.field_151153_ao, 1);
                    continue block56;
                }
                case 40: {
                    this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150340_R), 1);
                    continue block56;
                }
                case 41: {
                    EntityItem var33 = null;
                    is = new ItemStack(Items.field_151153_ao, 1, 1);
                    var33 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(3) - (double)OreSpawnMain.OreSpawnRand.nextInt(3), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(3) - (double)OreSpawnMain.OreSpawnRand.nextInt(3), is);
                    if (var33 == null) continue block56;
                    this.field_70170_p.func_72838_d((Entity)var33);
                    continue block56;
                }
                case 42: {
                    is = this.dropItemRand(OreSpawnMain.MyExperienceSword, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77336_l, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77337_m, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77335_o, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77334_n, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 43: {
                    is = this.dropItemRand((Item)OreSpawnMain.ExperienceHelmet, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77340_h, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 44: {
                    is = this.dropItemRand((Item)OreSpawnMain.ExperienceBody, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 45: {
                    is = this.dropItemRand((Item)OreSpawnMain.ExperienceLegs, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77332_c, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77327_f, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77329_d, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77328_g, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 46: {
                    is = this.dropItemRand((Item)OreSpawnMain.ExperienceBoots, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block56;
                }
                case 47: {
                    is = this.dropItemRand(OreSpawnMain.MyAmethystSword, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77336_l, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77337_m, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77335_o, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77334_n, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 48: {
                    is = this.dropItemRand(OreSpawnMain.MyAmethystShovel, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 49: {
                    is = this.dropItemRand(OreSpawnMain.MyAmethystPickaxe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 50: {
                    is = this.dropItemRand(OreSpawnMain.MyAmethystAxe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 51: {
                    is = this.dropItemRand(OreSpawnMain.MyAmethystHoe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block56;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block56;
                }
                case 52: {
                    is = this.dropItemRand(Item.func_150898_a((Block)OreSpawnMain.MyBlockAmethystBlock), 1);
                    continue block56;
                }
            }
        }
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        if (this.long_enough <= 0) {
            return true;
        }
        if (this.field_70163_u > 150.0 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 2)) {
            return true;
        }
        if (this.field_70163_u > 180.0 && this.long_enough <= 0) {
            this.func_70106_y();
            return true;
        }
        return false;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.75), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    protected void func_70619_bc() {
        int i;
        Block bid;
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.long_enough > 0) {
            --this.long_enough;
        }
        this.field_70180_af.func_75692_b(21, (Object)OreSpawnMain.PlayNicely);
        if (this.field_70170_p.field_73012_v.nextInt(400) == 1 && OreSpawnMain.PlayNicely == 0) {
            this.field_70170_p.func_72942_c((Entity)new EntityLightningBolt(this.field_70170_p, this.field_70165_t, this.field_70163_u - 16.0, this.field_70161_v));
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.newtarget != 0 || this.field_70146_Z.nextInt(250) == 1 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 9.1f) {
            int ground_dist;
            this.newtarget = 0;
            for (ground_dist = 0; ground_dist < 31; ++ground_dist) {
                bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u - ground_dist, (int)this.field_70161_v);
                if (bid == Blocks.field_150350_a) continue;
                this.straight_down = 0;
                break;
            }
            ground_dist = 20 - ground_dist;
            bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                zdir = this.field_70170_p.field_73012_v.nextInt(6) + 12;
                xdir = this.field_70170_p.field_73012_v.nextInt(6) + 12;
                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                if (this.straight_down != 0) {
                    xdir = 0;
                    zdir = 0;
                }
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t + xdir, (int)this.field_70163_u + ground_dist + this.field_70146_Z.nextInt(9) - 6, (int)this.field_70161_v + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                if (bid == Blocks.field_150350_a && !this.canSeeTarget((double)this.currentFlightTarget.field_71574_a, (double)this.currentFlightTarget.field_71572_b, (double)this.currentFlightTarget.field_71573_c)) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
            if (this.long_enough <= 0 || this.field_70163_u < 200.0 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 4)) {
                this.currentFlightTarget.func_71571_b(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b + 30, this.currentFlightTarget.field_71573_c);
                if (this.hit_by_player && this.call_reinforcements == 0 && this.func_110143_aJ() < (float)(this.mygetMaxHealth() / 8) && this.field_70163_u > 130.0) {
                    this.call_reinforcements = 1;
                    for (i = 0; i < 10; ++i) {
                        EntityCreature newent = (EntityCreature)Kraken.spawnCreature((World)this.field_70170_p, (String)"The Kraken", (double)(this.field_70165_t + (double)this.field_70170_p.field_73012_v.nextInt(10) - (double)this.field_70170_p.field_73012_v.nextInt(10)), (double)170.0, (double)(this.field_70161_v + (double)this.field_70170_p.field_73012_v.nextInt(10) - (double)this.field_70170_p.field_73012_v.nextInt(10)));
                    }
                }
            }
        } else if (this.caught == null && this.field_70170_p.field_73012_v.nextInt(8) == 1 && OreSpawnMain.PlayNicely == 0) {
            EntityPlayer target = null;
            target = (EntityPlayer)this.field_70170_p.func_72857_a(EntityPlayer.class, this.field_70121_D.func_72314_b(25.0, 40.0, 25.0), (Entity)this);
            if (target != null) {
                if (!target.field_71075_bZ.field_75098_d) {
                    if (this.func_70635_at().func_75522_a((Entity)target)) {
                        this.currentFlightTarget.func_71571_b((int)target.field_70165_t, (int)target.field_70163_u + 15, (int)target.field_70161_v);
                        this.attackWithSomething((EntityLivingBase)target);
                    }
                } else {
                    target = null;
                }
            }
            if (target == null && this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                EntityLivingBase e = null;
                e = this.findSomethingToAttack();
                if (e != null) {
                    this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)e.field_70163_u + 15, (int)e.field_70161_v);
                    this.attackWithSomething(e);
                }
            }
        }
        if (this.caught != null) {
            if (!this.caught.field_70128_L) {
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t, 200, (int)this.field_70161_v);
                if (this.field_70163_u > 190.0) {
                    this.release = 1;
                }
                this.caught.field_70159_w = this.field_70159_w;
                this.caught.field_70179_y = this.field_70179_y;
                this.caught.field_70181_x = this.field_70181_x;
                this.caught.field_70165_t = this.field_70165_t;
                if (this.field_70163_u - this.caught.field_70163_u > 16.0) {
                    this.caught.field_70181_x += 0.25;
                }
                this.caught.field_70163_u = this.field_70163_u - 15.0;
                this.caught.field_70161_v = this.field_70161_v;
                this.caught.field_70177_z = this.field_70177_z;
                if (this.field_70170_p.field_73012_v.nextInt(50) == 1) {
                    this.func_70652_k((Entity)this.caught);
                }
                if (this.release != 0 || this.field_70170_p.field_73012_v.nextInt(250) == 1) {
                    this.caught = null;
                    this.newtarget = 1;
                    this.release = 0;
                    this.setAttacking(0);
                }
            } else {
                this.caught = null;
                this.newtarget = 1;
                this.release = 0;
                this.setAttacking(0);
            }
        }
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.3 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.3 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.45 - this.field_70159_w) * 0.15;
        this.field_70181_x += (Math.signum(var3) * 0.70999 - this.field_70181_x) * 0.202;
        this.field_70179_y += (Math.signum(var5) * 0.45 - this.field_70179_y) * 0.15;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.4f;
        if (Math.abs(this.field_70159_w) + Math.abs(this.field_70179_y) < 0.15) {
            var8 = 0.0f;
        }
        this.field_70177_z += var8 / 5.0f;
        double obstruction_factor = 0.0;
        double dx = 0.0;
        double dz = 0.0;
        int dist = 10;
        for (int k = -20; k < 18; k += 2) {
            for (i = 1; i < dist; i += 2) {
                dx = (double)i * Math.cos(Math.toRadians(this.field_70177_z + 90.0f));
                bid = this.field_70170_p.func_147439_a((int)(this.field_70165_t + dx), (int)this.field_70163_u + k, (int)(this.field_70161_v + (dz = (double)i * Math.sin(Math.toRadians(this.field_70177_z + 90.0f)))));
                if (bid == Blocks.field_150350_a) continue;
                obstruction_factor += 0.1;
            }
        }
        this.field_70181_x += obstruction_factor * 0.08;
        this.field_70163_u += obstruction_factor * 0.08;
        if (this.field_70163_u > 256.0 && !this.func_104002_bU()) {
            this.func_70106_y();
        }
    }

    private void attackWithSomething(EntityLivingBase par1) {
        if (this.caught != null) {
            return;
        }
        double dist = (this.field_70165_t - par1.field_70165_t) * (this.field_70165_t - par1.field_70165_t);
        dist += (this.field_70161_v - par1.field_70161_v) * (this.field_70161_v - par1.field_70161_v);
        if ((dist += (this.field_70163_u - par1.field_70163_u - 15.0) * (this.field_70163_u - par1.field_70163_u - 15.0)) < 30.0) {
            this.caught = par1;
            this.release = 0;
            this.setAttacking(1);
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
        if (MyUtils.isIgnoreable((EntityLivingBase)par1EntityLiving)) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            if (p.field_71075_bZ.field_75098_d) {
                return false;
            }
            return !p.field_71075_bZ.field_75100_b;
        }
        if (!par1EntityLiving.field_70122_E && !par1EntityLiving.func_70090_H()) {
            return false;
        }
        if (par1EntityLiving instanceof EntitySquid) {
            return false;
        }
        if (par1EntityLiving instanceof AttackSquid) {
            return false;
        }
        if (par1EntityLiving instanceof Kraken) {
            return false;
        }
        if (par1EntityLiving instanceof Spyro) {
            return false;
        }
        if (par1EntityLiving instanceof Dragon) {
            Dragon c = (Dragon)par1EntityLiving;
            return c.field_70153_n == null;
        }
        if (par1EntityLiving instanceof Cephadrome) {
            Cephadrome c = (Cephadrome)par1EntityLiving;
            return c.field_70153_n == null;
        }
        if (par1EntityLiving instanceof Leon) {
            Leon c = (Leon)par1EntityLiving;
            return c.field_70153_n == null;
        }
        if (par1EntityLiving instanceof ThePrinceTeen) {
            ThePrinceTeen c = (ThePrinceTeen)par1EntityLiving;
            return c.field_70153_n == null;
        }
        if (par1EntityLiving instanceof ThePrinceAdult) {
            ThePrinceAdult c = (ThePrinceAdult)par1EntityLiving;
            return c.field_70153_n == null;
        }
        if (par1EntityLiving instanceof EntityChicken) {
            return false;
        }
        if (par1EntityLiving instanceof Chipmunk) {
            return false;
        }
        if (par1EntityLiving instanceof StinkBug) {
            return false;
        }
        return !(par1EntityLiving instanceof Mothra);
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(20.0, 40.0, 20.0));
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

    public void func_70077_a(EntityLightningBolt par1EntityLightningBolt) {
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        Entity e = par1DamageSource.func_76346_g();
        boolean ret = false;
        if (this.currentFlightTarget != null && e != null && e instanceof EntityPlayer && this.func_110143_aJ() > (float)(this.mygetMaxHealth() / 4)) {
            this.hit_by_player = true;
            this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)e.field_70163_u + 15, (int)e.field_70161_v);
        }
        if (this.hurt_timer > 0) {
            return false;
        }
        this.hurt_timer = 30;
        ret = super.func_70097_a(par1DamageSource, par2);
        if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
            this.release = 1;
        }
        return ret;
    }

    public final int getAttacking() {
        return this.field_70180_af.func_75683_a(20);
    }

    public final void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        for (int k = -1; k < 2; ++k) {
            for (int j = -1; j < 1; ++j) {
                for (int i = 1; i < 6; ++i) {
                    Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
                    return false;
                }
            }
        }
        return true;
    }
}

