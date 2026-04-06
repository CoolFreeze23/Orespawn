/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Cricket;
import danger.orespawn.EntityAnt;
import danger.orespawn.EntityButterfly;
import danger.orespawn.EntityMosquito;
import danger.orespawn.Firefly;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Girlfriend;
import danger.orespawn.MyEntityAIWander;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.WormSmall;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class Frog
extends EntityAnimal {
    private GenericTargetSorter TargetSorter = null;
    public double moveSpeed = 0.1f;
    private int singing = 0;
    private int jumpcount = 0;

    public Frog(World par1World) {
        super(par1World);
        this.func_70105_a(0.75f, 0.75f);
        this.field_70728_aV = 5;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.func_70661_as().func_75491_a(false);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.4));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWander((EntityCreature)this, 1.0f));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    public boolean func_70648_aU() {
        return true;
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public int getSinging() {
        return this.field_70180_af.func_75683_a(20);
    }

    public void setSinging(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    private void jumpAround() {
        this.field_70181_x += (double)(0.75f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.55f));
        this.field_70163_u += (double)0.35f;
        float f = 0.7f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.75f);
        float d = (float)Math.toRadians(this.field_70177_z);
        this.field_70159_w -= (double)f * Math.sin(d);
        this.field_70179_y += (double)f * Math.cos(d);
        this.field_70160_al = true;
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        super.func_70071_h_();
        if (!this.field_70170_p.field_72995_K) {
            if (this.singing != 0) {
                --this.singing;
                if (this.singing <= 0) {
                    this.setSinging(0);
                }
            }
            if (this.jumpcount > 0) {
                --this.jumpcount;
            }
            if (this.jumpcount == 0 && this.field_70170_p.field_73012_v.nextInt(70) == 1) {
                this.jumpAround();
                this.jumpcount = 50;
            }
        }
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        block2: {
            World world;
            block3: {
                block4: {
                    if (par1EntityPlayer == null || !par1EntityPlayer.func_70093_af() || par1EntityPlayer.field_71071_by.func_70448_g() != null) break block2;
                    world = par1EntityPlayer.field_70170_p;
                    this.func_70106_y();
                    par1EntityPlayer.field_70170_p.func_72956_a((Entity)par1EntityPlayer, "random.explode", 1.0f, world.field_73012_v.nextFloat() * 0.2f + 0.9f);
                    if (world.field_72995_K) break block3;
                    if (world.field_73012_v.nextInt(2) != 0) break block4;
                    Boyfriend ent = null;
                    ent = (Boyfriend)Frog.spawnCreature(world, "Boyfriend", this.field_70165_t, this.field_70163_u + 0.01, this.field_70161_v);
                    if (ent != null) {
                        ent.setPrince(1 + world.field_73012_v.nextInt(2));
                    }
                    break block2;
                }
                Girlfriend ent = null;
                ent = (Girlfriend)Frog.spawnCreature(world, "Girlfriend", this.field_70165_t, this.field_70163_u + 0.01, this.field_70161_v);
                if (ent == null) break block2;
                ent.setPrincess(1 + world.field_73012_v.nextInt(2));
                break block2;
            }
            for (int var3 = 0; var3 < 16; ++var3) {
                world.func_72869_a("smoke", (double)((float)this.field_70165_t + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), (double)((float)this.field_70163_u + world.field_73012_v.nextFloat()), (double)((float)this.field_70161_v + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
                world.func_72869_a("explode", (double)((float)this.field_70165_t + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), (double)((float)this.field_70163_u + world.field_73012_v.nextFloat()), (double)((float)this.field_70161_v + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
                world.func_72869_a("reddust", (double)((float)this.field_70165_t + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), (double)((float)this.field_70163_u + world.field_73012_v.nextFloat()), (double)((float)this.field_70161_v + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
            }
        }
        return false;
    }

    public boolean func_70650_aV() {
        return true;
    }

    public int mygetMaxHealth() {
        return 8;
    }

    protected String func_70639_aQ() {
        if (!this.field_70170_p.field_72995_K) {
            if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                return null;
            }
            this.singing = 35;
            this.setSinging(this.singing);
        }
        return "orespawn:frog";
    }

    protected String func_70621_aR() {
        return "orespawn:scorpion_hit";
    }

    protected String func_70673_aS() {
        return "orespawn:big_splat";
    }

    protected float func_70599_aP() {
        return 0.7f;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    protected void playStepSound(int par1, int par2, int par3, int par4) {
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        for (int i = 0; i < 4; ++i) {
            this.dropItemRand(Items.field_151123_aH, 1);
        }
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 3.0f);
        if (par1Entity.field_70128_L) {
            this.func_70691_i(1.0f);
        }
        return var4;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        ret = super.func_70097_a(par1DamageSource, par2);
        if (!this.field_70170_p.field_72995_K && this.jumpcount <= 0) {
            this.jumpAround();
            this.jumpcount = 25;
        }
        return ret;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.25), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    protected boolean func_70041_e_() {
        return true;
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    private int findBuddies() {
        List var5 = this.field_70170_p.func_72872_a(Frog.class, this.field_70121_D.func_72314_b(20.0, 8.0, 20.0));
        return var5.size();
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5 && this.field_70170_p.field_73012_v.nextInt(20) != 1) {
            return false;
        }
        return this.findBuddies() <= 5;
    }

    protected void func_70619_bc() {
        boolean xdir = true;
        boolean zdir = true;
        int keep_trying = 50;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70146_Z.nextInt(12) == 0 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL) {
            EntityLivingBase e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.func_70661_as().func_75497_a((Entity)e, 1.25);
                if (this.func_70068_e((Entity)e) < 6.0) {
                    this.func_70652_k((Entity)e);
                }
            }
        }
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
        if (par1EntityLiving instanceof EntityAnt) {
            return true;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return true;
        }
        if (par1EntityLiving instanceof Cricket) {
            return true;
        }
        if (par1EntityLiving instanceof EntityMosquito) {
            return true;
        }
        if (par1EntityLiving instanceof Firefly) {
            return true;
        }
        return par1EntityLiving instanceof WormSmall;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(8.0, 3.0, 8.0));
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

