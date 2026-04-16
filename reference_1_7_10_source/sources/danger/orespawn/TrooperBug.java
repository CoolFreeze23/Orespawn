/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.EnderKnight;
import danger.orespawn.EnderReaper;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Hydrolisc;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.RenderInfo;
import danger.orespawn.SpitBug;
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
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class TrooperBug
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private RenderInfo renderdata = new RenderInfo();
    private int force_sync = 50;
    private int hurt_timer = 0;
    private float moveSpeed = 0.4f;

    public TrooperBug(World par1World) {
        super(par1World);
        this.func_70105_a(3.0f, 3.5f);
        this.func_70661_as().func_75491_a(true);
        this.field_70728_aV = 150;
        this.field_70174_ab = 100;
        this.field_70178_ae = false;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.renderdata = new RenderInfo();
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMoveThroughVillage((EntityCreature)this, (double)0.9f, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 14, 1.0));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 10.0f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
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
        this.force_sync = 50;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.TrooperBug_stats.attack);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
        if (this.field_70160_al) {
            this.func_70661_as().func_75484_a(null, 0.0);
        }
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.TrooperBug_stats.health;
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
        return OreSpawnMain.TrooperBug_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
    }

    protected void func_70664_aZ() {
        this.field_70181_x += (double)1.15f;
        this.field_70163_u += 1.5;
        float f = 0.2f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.45f);
        this.field_70159_w -= (double)f * Math.sin(Math.toRadians(this.field_70759_as));
        this.field_70179_y += (double)f * Math.cos(Math.toRadians(this.field_70759_as));
        this.field_70160_al = true;
    }

    protected void jumpAtEntity(EntityLivingBase e) {
        this.field_70181_x += 1.25;
        this.field_70163_u += 1.25;
        float f = 0.3f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.25f);
        float d = (float)Math.atan2(e.field_70165_t - this.field_70165_t, e.field_70161_v - this.field_70161_v);
        this.field_70159_w += (double)f * Math.sin(d);
        this.field_70179_y += (double)f * Math.cos(d);
        this.field_70160_al = true;
    }

    public int getTrooperBugHealth() {
        return (int)this.func_110143_aJ();
    }

    protected String func_70639_aQ() {
        if (this.field_70146_Z.nextInt(4) == 0) {
            return "orespawn:clatter";
        }
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:crunch";
    }

    protected String func_70673_aS() {
        return "orespawn:emperorscorpion_death";
    }

    protected float func_70599_aP() {
        return 1.5f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return Items.field_151082_bd;
    }

    private ItemStack dropItemRand(Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(5) - (double)OreSpawnMain.OreSpawnRand.nextInt(5), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(5) - (double)OreSpawnMain.OreSpawnRand.nextInt(5), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        this.dropItemRand(OreSpawnMain.MyJumpyBugScale, 1);
        this.dropItemRand(Items.field_151160_bD, 1);
        int i = 2 + this.field_70170_p.field_73012_v.nextInt(5);
        for (var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(OreSpawnMain.MyAmethyst, 1);
        }
        i = 1 + this.field_70170_p.field_73012_v.nextInt(5);
        block16: for (var4 = 0; var4 < i; ++var4) {
            int var3 = this.field_70170_p.field_73012_v.nextInt(14);
            switch (var3) {
                case 0: {
                    continue block16;
                }
                case 1: {
                    continue block16;
                }
                case 2: {
                    ItemStack is = this.dropItemRand(Item.func_150898_a((Block)OreSpawnMain.MyBlockAmethystBlock), 1);
                    continue block16;
                }
                case 3: {
                    ItemStack is = this.dropItemRand(OreSpawnMain.MyAmethystSword, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block16;
                }
                case 4: {
                    ItemStack is = this.dropItemRand(OreSpawnMain.MyAmethystShovel, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block16;
                }
                case 5: {
                    ItemStack is = this.dropItemRand(OreSpawnMain.MyAmethystPickaxe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block16;
                }
                case 6: {
                    ItemStack is = this.dropItemRand(OreSpawnMain.MyAmethystAxe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block16;
                }
                case 7: {
                    ItemStack is = this.dropItemRand(OreSpawnMain.MyAmethystHoe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block16;
                }
                case 8: {
                    ItemStack is = this.dropItemRand((Item)OreSpawnMain.AmethystHelmet, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block16;
                }
                case 9: {
                    ItemStack is = this.dropItemRand((Item)OreSpawnMain.AmethystBody, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block16;
                }
                case 10: {
                    ItemStack is = this.dropItemRand((Item)OreSpawnMain.AmethystLegs, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block16;
                }
                case 11: {
                    ItemStack is = this.dropItemRand((Item)OreSpawnMain.AmethystBoots, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block16;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block16;
                }
                case 12: {
                    continue block16;
                }
            }
        }
    }

    public void initCreature() {
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70652_k(Entity par1Entity) {
        double ks = 1.8;
        double inair = 0.2;
        int var2 = 6;
        if (super.func_70652_k(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
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
        boolean ret = false;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (!par1DamageSource.func_76355_l().equals("cactus") && !par1DamageSource.func_76355_l().equals("fall")) {
            ret = super.func_70097_a(par1DamageSource, par2);
            this.hurt_timer = 20;
            Entity e = par1DamageSource.func_76346_g();
            if (e != null && e instanceof EntityLiving) {
                this.func_70624_b((EntityLivingBase)((EntityLiving)e));
                this.func_70784_b(e);
                this.func_70661_as().func_75497_a((Entity)((EntityLiving)e), 1.2);
                ret = true;
            }
        }
        return ret;
    }

    protected void func_70619_bc() {
        EntityLivingBase e = null;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.field_70170_p.field_73012_v.nextInt(5) == 0) {
            e = this.func_70638_az();
            if (e != null && !e.func_70089_S()) {
                this.func_70624_b(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.func_70625_a((Entity)e, 10.0f, 10.0f);
                if (this.field_70170_p.field_73012_v.nextInt(10) == 1 && !this.field_70160_al) {
                    this.jumpAtEntity(e);
                } else if (this.func_70068_e((Entity)e) < (double)((5.0f + e.field_70130_N / 2.0f) * (5.0f + e.field_70130_N / 2.0f))) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 0 || this.field_70170_p.field_73012_v.nextInt(7) == 1) {
                        this.func_70652_k((Entity)e);
                        if (!this.field_70170_p.field_72995_K) {
                            if (this.field_70170_p.field_73012_v.nextInt(3) == 1) {
                                this.field_70170_p.func_72956_a((Entity)e, "orespawn:scorpion_attack", 1.4f, 1.0f);
                            } else {
                                this.field_70170_p.func_72956_a((Entity)e, "orespawn:clatter", 1.0f, 1.0f);
                            }
                        }
                    }
                } else if (!this.field_70160_al) {
                    this.func_70661_as().func_75497_a((Entity)e, 1.2);
                }
                if (this.field_70170_p.field_73012_v.nextInt(30) == 1) {
                    EntityCreature entityCreature = (EntityCreature)TrooperBug.spawnCreature((World)this.field_70170_p, (String)"Spit Bug", (double)((this.field_70165_t + e.field_70165_t) / 2.0 + (double)this.field_70170_p.field_73012_v.nextInt(5) - (double)this.field_70170_p.field_73012_v.nextInt(5)), (double)((this.field_70163_u + e.field_70163_u) / 2.0 + 1.01), (double)((this.field_70161_v + e.field_70161_v) / 2.0 + (double)this.field_70170_p.field_73012_v.nextInt(5) - (double)this.field_70170_p.field_73012_v.nextInt(5)));
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(150) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(1.0f);
        }
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
        if (par1EntityLiving instanceof Hydrolisc) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (par1EntityLiving instanceof EnderKnight) {
            return false;
        }
        if (par1EntityLiving instanceof EntityEnderman) {
            return false;
        }
        if (par1EntityLiving instanceof EntityCreeper) {
            return false;
        }
        if (par1EntityLiving instanceof TrooperBug) {
            return false;
        }
        if (par1EntityLiving instanceof SpitBug) {
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
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(12.0, 7.0, 12.0));
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

    public final int getAttacking() {
        return this.field_70180_af.func_75683_a(20);
    }

    public final void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    public boolean func_70601_bi() {
        Block bid;
        int i;
        int j;
        int k;
        for (k = -3; k < 3; ++k) {
            for (j = -3; j < 3; ++j) {
                for (i = 0; i < 5; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Jumpy Bug")) continue;
                    return true;
                }
            }
        }
        if (!this.func_70814_o()) {
            return false;
        }
        if (this.field_70170_p.func_72935_r() && this.field_70170_p.field_73012_v.nextInt(20) > 1) {
            return false;
        }
        for (k = -2; k < 2; ++k) {
            for (j = -2; j < 2; ++j) {
                for (i = 1; i < 5; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
        }
        return true;
    }
}

