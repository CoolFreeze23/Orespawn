/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class UltimateFishHook
extends EntityFishHook {
    private static final List field_146039_d = Arrays.asList(new WeightedRandomFishable(new ItemStack((Item)Items.field_151021_T), 10).func_150709_a(0.9f), new WeightedRandomFishable(new ItemStack(Items.field_151116_aA), 10), new WeightedRandomFishable(new ItemStack(Items.field_151103_aS), 10), new WeightedRandomFishable(new ItemStack((Item)Items.field_151068_bn), 10), new WeightedRandomFishable(new ItemStack(Items.field_151007_F), 5), new WeightedRandomFishable(new ItemStack((Item)Items.field_151112_aM), 2).func_150709_a(0.9f), new WeightedRandomFishable(new ItemStack(Items.field_151054_z), 10), new WeightedRandomFishable(new ItemStack(Items.field_151055_y), 5), new WeightedRandomFishable(new ItemStack(Items.field_151100_aR, 10, 0), 1), new WeightedRandomFishable(new ItemStack((Block)Blocks.field_150479_bC), 10), new WeightedRandomFishable(new ItemStack(Items.field_151078_bh), 10));
    private static final List field_146041_e = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.field_150392_bi), 1), new WeightedRandomFishable(new ItemStack(Items.field_151057_cb), 1), new WeightedRandomFishable(new ItemStack(Items.field_151141_av), 1), new WeightedRandomFishable(new ItemStack((Item)Items.field_151031_f), 1).func_150709_a(0.25f).func_150707_a(), new WeightedRandomFishable(new ItemStack((Item)Items.field_151112_aM), 1).func_150709_a(0.25f).func_150707_a(), new WeightedRandomFishable(new ItemStack(Items.field_151122_aG), 1).func_150707_a());
    private static final List field_146036_f = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.field_151115_aP, 1, ItemFishFood.FishType.COD.func_150976_a()), 60), new WeightedRandomFishable(new ItemStack(Items.field_151115_aP, 1, ItemFishFood.FishType.SALMON.func_150976_a()), 25), new WeightedRandomFishable(new ItemStack(Items.field_151115_aP, 1, ItemFishFood.FishType.CLOWNFISH.func_150976_a()), 2), new WeightedRandomFishable(new ItemStack(Items.field_151115_aP, 1, ItemFishFood.FishType.PUFFERFISH.func_150976_a()), 13));
    private static final List orespawn_lava_fish = Arrays.asList(new WeightedRandomFishable(new ItemStack(OreSpawnMain.MySunspotUrchin), 25), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyLavaEel), 10), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MySunFish), 15), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MySparkFish), 10), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyFireFish), 15));
    private static final List orespawn_fish = Arrays.asList(new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyBlueFish), 25), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyPinkFish), 10), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyRockFish), 15), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyWoodFish), 10), new WeightedRandomFishable(new ItemStack(OreSpawnMain.MyGreyFish), 15));
    private int field_146037_g = -1;
    private int field_146048_h = -1;
    private int field_146050_i = -1;
    private Block field_146046_j;
    private boolean field_146051_au;
    private int field_146049_av;
    private int field_146047_aw;
    private int fish_on_hook;
    private int fish_wait_time;
    private int ticks_catchable;
    private float fish_direction;
    public Entity field_146043_c;
    private int field_146055_aB;
    private double field_146056_aC;
    private double field_146057_aD;
    private double field_146058_aE;
    private double field_146059_aF;
    private double field_146060_aG;
    @SideOnly(value=Side.CLIENT)
    private double field_146061_aH;
    @SideOnly(value=Side.CLIENT)
    private double field_146052_aI;
    @SideOnly(value=Side.CLIENT)
    private double field_146053_aJ;
    private int fishing_in_lava = 0;

    public UltimateFishHook(World par1World) {
        super(par1World);
        this.func_70105_a(0.25f, 0.25f);
        this.field_70158_ak = true;
        this.field_70174_ab = 3000;
        this.field_70178_ae = true;
    }

    @SideOnly(value=Side.CLIENT)
    public UltimateFishHook(World par1World, double par2, double par4, double par6, EntityPlayer par8EntityPlayer) {
        this(par1World);
        this.func_70107_b(par2, par4, par6);
        this.field_70158_ak = true;
        this.field_146042_b = par8EntityPlayer;
        par8EntityPlayer.field_71104_cf = this;
        this.field_70174_ab = 3000;
        this.field_70178_ae = true;
    }

    public UltimateFishHook(World par1World, EntityPlayer par2EntityPlayer) {
        super(par1World);
        this.field_70158_ak = true;
        this.field_146042_b = par2EntityPlayer;
        this.field_146042_b.field_71104_cf = this;
        this.func_70105_a(0.25f, 0.25f);
        this.func_70012_b(par2EntityPlayer.field_70165_t, par2EntityPlayer.field_70163_u + 1.62 - (double)par2EntityPlayer.field_70129_M, par2EntityPlayer.field_70161_v, par2EntityPlayer.field_70177_z, par2EntityPlayer.field_70125_A);
        this.field_70165_t -= (double)(MathHelper.func_76134_b((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * 0.16f);
        this.field_70163_u -= (double)0.1f;
        this.field_70161_v -= (double)(MathHelper.func_76126_a((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * 0.16f);
        this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        this.field_70129_M = 0.0f;
        float f = 0.4f;
        this.field_70159_w = -MathHelper.func_76126_a((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(this.field_70125_A / 180.0f * (float)Math.PI)) * f;
        this.field_70179_y = MathHelper.func_76134_b((float)(this.field_70177_z / 180.0f * (float)Math.PI)) * MathHelper.func_76134_b((float)(this.field_70125_A / 180.0f * (float)Math.PI)) * f;
        this.field_70181_x = -MathHelper.func_76126_a((float)(this.field_70125_A / 180.0f * (float)Math.PI)) * f;
        this.func_146035_c(this.field_70159_w, this.field_70181_x, this.field_70179_y, 1.5f, 1.0f);
        this.field_70174_ab = 3000;
        this.field_70178_ae = true;
    }

    protected void func_70088_a() {
        this.field_70174_ab = 3000;
        this.field_70178_ae = true;
    }

    public void func_146035_c(double p_146035_1_, double p_146035_3_, double p_146035_5_, float p_146035_7_, float p_146035_8_) {
        float f2 = MathHelper.func_76133_a((double)(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_));
        p_146035_1_ /= (double)f2;
        p_146035_3_ /= (double)f2;
        p_146035_5_ /= (double)f2;
        p_146035_1_ += this.field_70146_Z.nextGaussian() * (double)0.0075f * (double)p_146035_8_;
        p_146035_3_ += this.field_70146_Z.nextGaussian() * (double)0.0075f * (double)p_146035_8_;
        p_146035_5_ += this.field_70146_Z.nextGaussian() * (double)0.0075f * (double)p_146035_8_;
        this.field_70159_w = p_146035_1_ *= (double)p_146035_7_;
        this.field_70181_x = p_146035_3_ *= (double)p_146035_7_;
        this.field_70179_y = p_146035_5_ *= (double)p_146035_7_;
        float f3 = MathHelper.func_76133_a((double)(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_));
        this.field_70126_B = this.field_70177_z = (float)(Math.atan2(p_146035_1_, p_146035_5_) * 180.0 / Math.PI);
        this.field_70127_C = this.field_70125_A = (float)(Math.atan2(p_146035_3_, f3) * 180.0 / Math.PI);
        this.field_146049_av = 0;
    }

    @SideOnly(value=Side.CLIENT)
    public boolean func_70112_a(double par1) {
        double d1 = this.field_70121_D.func_72320_b() * 4.0;
        return par1 < (d1 *= 64.0) * d1;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70056_a(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.field_146056_aC = par1;
        this.field_146057_aD = par3;
        this.field_146058_aE = par5;
        this.field_146059_aF = par7;
        this.field_146060_aG = par8;
        this.field_146055_aB = par9;
        this.field_70159_w = this.field_146061_aH;
        this.field_70181_x = this.field_146052_aI;
        this.field_70179_y = this.field_146053_aJ;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70016_h(double par1, double par3, double par5) {
        this.field_146061_aH = this.field_70159_w = par1;
        this.field_146052_aI = this.field_70181_x = par3;
        this.field_146053_aJ = this.field_70179_y = par5;
    }

    public void func_70071_h_() {
        if (this.field_146055_aB > 0) {
            double d7 = this.field_70165_t + (this.field_146056_aC - this.field_70165_t) / (double)this.field_146055_aB;
            double d8 = this.field_70163_u + (this.field_146057_aD - this.field_70163_u) / (double)this.field_146055_aB;
            double d9 = this.field_70161_v + (this.field_146058_aE - this.field_70161_v) / (double)this.field_146055_aB;
            double d1 = MathHelper.func_76138_g((double)(this.field_146059_aF - (double)this.field_70177_z));
            this.field_70177_z = (float)((double)this.field_70177_z + d1 / (double)this.field_146055_aB);
            this.field_70125_A = (float)((double)this.field_70125_A + (this.field_146060_aG - (double)this.field_70125_A) / (double)this.field_146055_aB);
            --this.field_146055_aB;
            this.func_70107_b(d7, d8, d9);
            this.func_70101_b(this.field_70177_z, this.field_70125_A);
        } else {
            double d2;
            if (!this.field_70170_p.field_72995_K) {
                ItemStack itemstack = this.field_146042_b.func_71045_bC();
                if (this.field_146042_b.field_70128_L || !this.field_146042_b.func_70089_S() || itemstack == null || itemstack.func_77973_b() != OreSpawnMain.MyUltimateFishingRod || this.func_70068_e((Entity)this.field_146042_b) > 1024.0) {
                    this.func_70106_y();
                    this.field_146042_b.field_71104_cf = null;
                    return;
                }
                if (this.field_146043_c != null) {
                    if (!this.field_146043_c.field_70128_L) {
                        this.field_70165_t = this.field_146043_c.field_70165_t;
                        this.field_70163_u = this.field_146043_c.field_70121_D.field_72338_b + (double)this.field_146043_c.field_70131_O * 0.8;
                        this.field_70161_v = this.field_146043_c.field_70161_v;
                        return;
                    }
                    this.field_146043_c = null;
                }
            }
            if (this.field_146044_a > 0) {
                --this.field_146044_a;
            }
            if (this.field_146051_au) {
                if (this.field_70170_p.func_147439_a(this.field_146037_g, this.field_146048_h, this.field_146050_i) == this.field_146046_j) {
                    ++this.field_146049_av;
                    if (this.field_146049_av == 1200) {
                        this.func_70106_y();
                    }
                    return;
                }
                this.field_146051_au = false;
                this.field_70159_w *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_70181_x *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_70179_y *= (double)(this.field_70146_Z.nextFloat() * 0.2f);
                this.field_146049_av = 0;
                this.field_146047_aw = 0;
            } else {
                ++this.field_146047_aw;
            }
            Vec3 vec31 = Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            Vec3 vec3 = Vec3.func_72443_a((double)(this.field_70165_t + this.field_70159_w), (double)(this.field_70163_u + this.field_70181_x), (double)(this.field_70161_v + this.field_70179_y));
            MovingObjectPosition movingobjectposition = this.field_70170_p.func_72933_a(vec31, vec3);
            vec31 = Vec3.func_72443_a((double)this.field_70165_t, (double)this.field_70163_u, (double)this.field_70161_v);
            vec3 = Vec3.func_72443_a((double)(this.field_70165_t + this.field_70159_w), (double)(this.field_70163_u + this.field_70181_x), (double)(this.field_70161_v + this.field_70179_y));
            if (movingobjectposition != null) {
                vec3 = Vec3.func_72443_a((double)movingobjectposition.field_72307_f.field_72450_a, (double)movingobjectposition.field_72307_f.field_72448_b, (double)movingobjectposition.field_72307_f.field_72449_c);
            }
            Entity entity = null;
            List list = this.field_70170_p.func_72839_b((Entity)this, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(1.0, 1.0, 1.0));
            double d0 = 0.0;
            for (int i = 0; i < list.size(); ++i) {
                float f;
                AxisAlignedBB axisalignedbb;
                MovingObjectPosition movingobjectposition1;
                Entity entity1 = (Entity)list.get(i);
                if (!entity1.func_70067_L() || entity1 == this.field_146042_b && this.field_146047_aw < 5 || (movingobjectposition1 = (axisalignedbb = entity1.field_70121_D.func_72314_b((double)(f = 0.3f), (double)f, (double)f)).func_72327_a(vec31, vec3)) == null || !((d2 = vec31.func_72438_d(movingobjectposition1.field_72307_f)) < d0) && d0 != 0.0) continue;
                entity = entity1;
                d0 = d2;
            }
            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
            if (movingobjectposition != null) {
                if (movingobjectposition.field_72308_g != null) {
                    if (movingobjectposition.field_72308_g.func_70097_a(DamageSource.func_76356_a((Entity)this, (Entity)this.field_146042_b), 0.0f)) {
                        this.field_146043_c = movingobjectposition.field_72308_g;
                    }
                } else {
                    this.field_146051_au = true;
                }
            }
            if (!this.field_146051_au) {
                this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
                float f5 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y));
                this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0 / Math.PI);
                this.field_70125_A = (float)(Math.atan2(this.field_70181_x, f5) * 180.0 / Math.PI);
                while (this.field_70125_A - this.field_70127_C < -180.0f) {
                    this.field_70127_C -= 360.0f;
                }
                while (this.field_70125_A - this.field_70127_C >= 180.0f) {
                    this.field_70127_C += 360.0f;
                }
                while (this.field_70177_z - this.field_70126_B < -180.0f) {
                    this.field_70126_B -= 360.0f;
                }
                while (this.field_70177_z - this.field_70126_B >= 180.0f) {
                    this.field_70126_B += 360.0f;
                }
                this.field_70125_A = this.field_70127_C + (this.field_70125_A - this.field_70127_C) * 0.2f;
                this.field_70177_z = this.field_70126_B + (this.field_70177_z - this.field_70126_B) * 0.2f;
                float f6 = 0.92f;
                if (this.field_70122_E || this.field_70123_F) {
                    f6 = 0.5f;
                }
                int b0 = 5;
                double d10 = 0.0;
                for (int j = 0; j < b0; ++j) {
                    double d3 = this.field_70121_D.field_72338_b + (this.field_70121_D.field_72337_e - this.field_70121_D.field_72338_b) * (double)(j + 0) / (double)b0 - 0.125 + 0.125;
                    double d4 = this.field_70121_D.field_72338_b + (this.field_70121_D.field_72337_e - this.field_70121_D.field_72338_b) * (double)(j + 1) / (double)b0 - 0.125 + 0.125;
                    AxisAlignedBB axisalignedbb1 = AxisAlignedBB.func_72330_a((double)this.field_70121_D.field_72340_a, (double)d3, (double)this.field_70121_D.field_72339_c, (double)this.field_70121_D.field_72336_d, (double)d4, (double)this.field_70121_D.field_72334_f);
                    if (this.field_70170_p.func_72830_b(axisalignedbb1, Material.field_151586_h)) {
                        d10 += 1.0 / (double)b0;
                    }
                    if (!this.field_70170_p.func_72830_b(axisalignedbb1, Material.field_151587_i)) continue;
                    d10 += 1.0 / (double)b0;
                }
                if (!this.field_70170_p.field_72995_K && d10 > 0.0) {
                    WorldServer worldserver = (WorldServer)this.field_70170_p;
                    int k = 1;
                    if (this.field_70146_Z.nextFloat() < 0.25f && this.field_70170_p.func_72951_B(MathHelper.func_76128_c((double)this.field_70165_t), MathHelper.func_76128_c((double)this.field_70163_u) + 1, MathHelper.func_76128_c((double)this.field_70161_v))) {
                        k = 2;
                    }
                    if (this.field_70146_Z.nextFloat() < 0.5f && !this.field_70170_p.func_72937_j(MathHelper.func_76128_c((double)this.field_70165_t), MathHelper.func_76128_c((double)this.field_70163_u) + 1, MathHelper.func_76128_c((double)this.field_70161_v))) {
                        --k;
                    }
                    if (this.fish_on_hook > 0) {
                        --this.fish_on_hook;
                        if (this.fish_on_hook <= 0) {
                            this.fish_wait_time = 0;
                            this.ticks_catchable = 0;
                        }
                    } else if (this.ticks_catchable > 0) {
                        this.ticks_catchable -= k;
                        if (this.ticks_catchable <= 0) {
                            this.field_70181_x -= (double)0.2f;
                            this.func_85030_a("random.splash", 0.25f, 1.0f + (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.4f);
                            float f1 = MathHelper.func_76128_c((double)this.field_70121_D.field_72338_b);
                            worldserver.func_147487_a("bubble", this.field_70165_t, (double)(f1 + 1.0f), this.field_70161_v, (int)(1.0f + this.field_70130_N * 20.0f), (double)this.field_70130_N, 0.0, (double)this.field_70130_N, (double)0.2f);
                            worldserver.func_147487_a("wake", this.field_70165_t, (double)(f1 + 1.0f), this.field_70161_v, (int)(1.0f + this.field_70130_N * 20.0f), (double)this.field_70130_N, 0.0, (double)this.field_70130_N, (double)0.2f);
                            this.fish_on_hook = MathHelper.func_76136_a((Random)this.field_70146_Z, (int)10, (int)30);
                        } else {
                            this.fish_direction = (float)((double)this.fish_direction + this.field_70146_Z.nextGaussian() * 4.0);
                            float f1 = this.fish_direction * ((float)Math.PI / 180);
                            float f7 = MathHelper.func_76126_a((float)f1);
                            float f2 = MathHelper.func_76134_b((float)f1);
                            double d11 = this.field_70165_t + (double)(f7 * (float)this.ticks_catchable * 0.1f);
                            double d5 = (float)MathHelper.func_76128_c((double)this.field_70121_D.field_72338_b) + 1.0f;
                            double d6 = this.field_70161_v + (double)(f2 * (float)this.ticks_catchable * 0.1f);
                            if (this.field_70146_Z.nextFloat() < 0.15f) {
                                worldserver.func_147487_a("bubble", d11, d5 - (double)0.1f, d6, 1, (double)f7, 0.1, (double)f2, 0.0);
                            }
                            float f3 = f7 * 0.04f;
                            float f4 = f2 * 0.04f;
                            worldserver.func_147487_a("wake", d11, d5, d6, 0, (double)f4, 0.01, (double)(-f3), 1.0);
                            worldserver.func_147487_a("wake", d11, d5, d6, 0, (double)(-f4), 0.01, (double)f3, 1.0);
                        }
                    } else if (this.fish_wait_time > 0) {
                        this.fish_wait_time -= k;
                        float f1 = 0.15f;
                        if (this.fish_wait_time < 20) {
                            f1 = (float)((double)f1 + (double)(20 - this.fish_wait_time) * 0.05);
                        } else if (this.fish_wait_time < 40) {
                            f1 = (float)((double)f1 + (double)(40 - this.fish_wait_time) * 0.02);
                        } else if (this.fish_wait_time < 60) {
                            f1 = (float)((double)f1 + (double)(60 - this.fish_wait_time) * 0.01);
                        }
                        if (this.field_70146_Z.nextFloat() < f1) {
                            float f7 = MathHelper.func_151240_a((Random)this.field_70146_Z, (float)0.0f, (float)360.0f) * ((float)Math.PI / 180);
                            float f2 = MathHelper.func_151240_a((Random)this.field_70146_Z, (float)25.0f, (float)60.0f);
                            double d11 = this.field_70165_t + (double)(MathHelper.func_76126_a((float)f7) * f2 * 0.1f);
                            double d5 = (float)MathHelper.func_76128_c((double)this.field_70121_D.field_72338_b) + 1.0f;
                            double d6 = this.field_70161_v + (double)(MathHelper.func_76134_b((float)f7) * f2 * 0.1f);
                            worldserver.func_147487_a("splash", d11, d5, d6, 2 + this.field_70146_Z.nextInt(2), (double)0.1f, 0.0, (double)0.1f, 0.0);
                        }
                        if (this.fish_wait_time <= 0) {
                            this.fish_direction = MathHelper.func_151240_a((Random)this.field_70146_Z, (float)0.0f, (float)360.0f);
                            this.ticks_catchable = MathHelper.func_76136_a((Random)this.field_70146_Z, (int)100, (int)200);
                        }
                    } else {
                        this.fish_wait_time = MathHelper.func_76136_a((Random)this.field_70146_Z, (int)50, (int)300);
                        this.fish_wait_time -= EnchantmentHelper.func_151387_h((EntityLivingBase)this.field_146042_b) * 20 * 5;
                    }
                    if (this.fish_on_hook > 0) {
                        this.field_70181_x -= (double)(this.field_70146_Z.nextFloat() * this.field_70146_Z.nextFloat() * this.field_70146_Z.nextFloat()) * 0.2;
                    }
                }
                d2 = d10 * 2.0 - 1.0;
                this.field_70181_x += (double)0.04f * d2;
                if (d10 > 0.0) {
                    f6 = (float)((double)f6 * 0.9);
                    this.field_70181_x *= 0.8;
                }
                this.field_70159_w *= (double)f6;
                this.field_70181_x *= (double)f6;
                this.field_70179_y *= (double)f6;
                this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            }
        }
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        par1NBTTagCompound.func_74777_a("xTile", (short)this.field_146037_g);
        par1NBTTagCompound.func_74777_a("yTile", (short)this.field_146048_h);
        par1NBTTagCompound.func_74777_a("zTile", (short)this.field_146050_i);
        par1NBTTagCompound.func_74774_a("inTile", (byte)Block.func_149682_b((Block)this.field_146046_j));
        par1NBTTagCompound.func_74774_a("shake", (byte)this.field_146044_a);
        par1NBTTagCompound.func_74774_a("inGround", (byte)(this.field_146051_au ? 1 : 0));
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        this.field_146037_g = par1NBTTagCompound.func_74765_d("xTile");
        this.field_146048_h = par1NBTTagCompound.func_74765_d("yTile");
        this.field_146050_i = par1NBTTagCompound.func_74765_d("zTile");
        this.field_146046_j = Block.func_149729_e((int)(par1NBTTagCompound.func_74771_c("inTile") & 0xFF));
        this.field_146044_a = par1NBTTagCompound.func_74771_c("shake") & 0xFF;
        this.field_146051_au = par1NBTTagCompound.func_74771_c("inGround") == 1;
    }

    @SideOnly(value=Side.CLIENT)
    public float func_70053_R() {
        return 0.0f;
    }

    public int func_146034_e() {
        if (this.field_70170_p.field_72995_K) {
            return 0;
        }
        int b0 = 0;
        if (this.field_146043_c != null) {
            double d0 = this.field_146042_b.field_70165_t - this.field_70165_t;
            double d2 = this.field_146042_b.field_70163_u - this.field_70163_u;
            double d4 = this.field_146042_b.field_70161_v - this.field_70161_v;
            double d6 = MathHelper.func_76133_a((double)(d0 * d0 + d2 * d2 + d4 * d4));
            double d8 = 0.1;
            this.field_146043_c.field_70159_w += d0 * d8;
            this.field_146043_c.field_70181_x += d2 * d8 + (double)MathHelper.func_76133_a((double)d6) * 0.08;
            this.field_146043_c.field_70179_y += d4 * d8;
            b0 = 3;
        } else if (this.fish_on_hook > 0) {
            EntityItem entityitem = new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u + 1.25, this.field_70161_v, this.func_146033_f());
            double d1 = this.field_146042_b.field_70165_t - this.field_70165_t;
            double d3 = this.field_146042_b.field_70163_u - this.field_70163_u;
            double d5 = this.field_146042_b.field_70161_v - this.field_70161_v;
            double d7 = MathHelper.func_76133_a((double)(d1 * d1 + d3 * d3 + d5 * d5));
            double d9 = 0.1;
            entityitem.field_70159_w = d1 * d9;
            entityitem.field_70181_x = d3 * d9 + (double)MathHelper.func_76133_a((double)d7) * 0.08;
            entityitem.field_70179_y = d5 * d9;
            entityitem.field_70174_ab = 3000;
            this.field_70170_p.func_72838_d((Entity)entityitem);
            this.field_146042_b.field_70170_p.func_72838_d((Entity)new EntityXPOrb(this.field_146042_b.field_70170_p, this.field_146042_b.field_70165_t, this.field_146042_b.field_70163_u + 0.5, this.field_146042_b.field_70161_v + 0.5, this.field_70146_Z.nextInt(6) + 1));
            b0 = 1;
        }
        if (this.field_146051_au) {
            b0 = 2;
        }
        this.func_70106_y();
        this.field_146042_b.field_71104_cf = null;
        return b0;
    }

    private ItemStack func_146033_f() {
        float f = this.field_70170_p.field_73012_v.nextFloat();
        int i = EnchantmentHelper.func_151386_g((EntityLivingBase)this.field_146042_b);
        int j = EnchantmentHelper.func_151387_h((EntityLivingBase)this.field_146042_b);
        float f1 = 0.1f - (float)i * 0.025f - (float)j * 0.01f;
        float f2 = 0.05f + (float)i * 0.01f - (float)j * 0.01f;
        f1 = MathHelper.func_76131_a((float)f1, (float)0.0f, (float)1.0f);
        f2 = MathHelper.func_76131_a((float)f2, (float)0.0f, (float)1.0f);
        Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        if (this.func_70058_J() || bid == Blocks.field_150353_l || bid == Blocks.field_150356_k) {
            this.field_146042_b.func_71064_a(StatList.field_75933_B, 1);
            return ((WeightedRandomFishable)WeightedRandom.func_76271_a((Random)this.field_70146_Z, (Collection)orespawn_lava_fish)).func_150708_a(this.field_70146_Z);
        }
        if (f < f1) {
            this.field_146042_b.func_71064_a(StatList.field_151183_A, 1);
            return ((WeightedRandomFishable)WeightedRandom.func_76271_a((Random)this.field_70146_Z, (Collection)field_146039_d)).func_150708_a(this.field_70146_Z);
        }
        if ((f -= f1) < f2) {
            this.field_146042_b.func_71064_a(StatList.field_151184_B, 1);
            return ((WeightedRandomFishable)WeightedRandom.func_76271_a((Random)this.field_70146_Z, (Collection)field_146041_e)).func_150708_a(this.field_70146_Z);
        }
        float f3 = this.field_70170_p.field_73012_v.nextFloat();
        this.field_146042_b.func_71064_a(StatList.field_75933_B, 1);
        if (f3 < 0.5f) {
            return ((WeightedRandomFishable)WeightedRandom.func_76271_a((Random)this.field_70146_Z, (Collection)field_146036_f)).func_150708_a(this.field_70146_Z);
        }
        return ((WeightedRandomFishable)WeightedRandom.func_76271_a((Random)this.field_70146_Z, (Collection)orespawn_fish)).func_150708_a(this.field_70146_Z);
    }

    public void func_70106_y() {
        super.func_70106_y();
        if (this.field_146042_b != null) {
            this.field_146042_b.field_71104_cf = null;
        }
    }
}

