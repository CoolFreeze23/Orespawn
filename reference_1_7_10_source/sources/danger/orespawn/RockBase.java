/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class RockBase
extends EntityLiving {
    public int rock_type = 0;
    private double dx;
    private double dz;

    public RockBase(World par1World) {
        super(par1World);
        this.func_70105_a(0.25f, 0.15f);
        this.field_70174_ab = 100000;
        this.field_70178_ae = true;
        this.dz = 0.0;
        this.dx = 0.0;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        Entity e = par1DamageSource.func_76346_g();
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return false;
        }
        if (e != null && e instanceof EntityLivingBase) {
            this.func_85030_a("random.pop", 0.75f, 2.25f);
        }
        return super.func_70097_a(par1DamageSource, par2);
    }

    public int getRockType() {
        return this.field_70180_af.func_75679_c(20);
    }

    public void setRockType(int par1) {
        if (this.field_70170_p == null) {
            return;
        }
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70180_af.func_75692_b(20, (Object)par1);
    }

    public void placeRock(int par1) {
        this.rock_type = par1;
        this.setRockType(par1);
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)(1 + this.rock_type / 4));
        this.func_70606_j((float)(1 + this.rock_type / 4));
    }

    public int func_70658_aO() {
        return 0;
    }

    protected void func_70069_a(float par1) {
    }

    public void func_70071_h_() {
        if (this.dx == 0.0 && this.dz == 0.0) {
            this.dx = this.field_70165_t;
            this.dz = this.field_70161_v;
        }
        super.func_70071_h_();
        this.field_70125_A = 0.0f;
        this.field_70759_as = 0.0f;
        this.field_70177_z = 0.0f;
        if (this.field_70170_p.field_72995_K) {
            this.rock_type = this.getRockType();
        }
        if (!this.field_70170_p.field_72995_K && this.rock_type == 0) {
            if (this.field_70170_p.field_73011_w.field_76574_g != OreSpawnMain.DimensionID5) {
                this.rock_type = 1;
                if (this.field_70170_p.field_73012_v.nextInt(10) == 0) {
                    this.rock_type = 2;
                }
                if (this.field_70170_p.field_73012_v.nextInt(20) == 0) {
                    this.rock_type = 3;
                }
                if (this.field_70170_p.field_73012_v.nextInt(30) == 0) {
                    this.rock_type = 4;
                }
                if (this.field_70170_p.field_73012_v.nextInt(40) == 0) {
                    this.rock_type = 5;
                }
                if (this.field_70170_p.field_73012_v.nextInt(50) == 0) {
                    this.rock_type = 6;
                }
                if (this.field_70170_p.field_73012_v.nextInt(100) == 0) {
                    this.rock_type = 7;
                }
                if (this.field_70170_p.field_73012_v.nextInt(200) == 0) {
                    this.rock_type = 8;
                }
                if (this.field_70170_p.field_73012_v.nextInt(500) == 0) {
                    this.rock_type = 9;
                }
                if (this.field_70170_p.field_73012_v.nextInt(500) == 0) {
                    this.rock_type = 10;
                }
                if (this.field_70170_p.field_73012_v.nextInt(500) == 0) {
                    this.rock_type = 11;
                }
                if (this.field_70170_p.field_73012_v.nextInt(1000) == 0) {
                    this.rock_type = 12;
                }
            } else {
                this.rock_type = 9;
                if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
                    this.rock_type = 10;
                }
                if (this.field_70170_p.field_73012_v.nextInt(5) == 0) {
                    this.rock_type = 11;
                }
                if (this.field_70170_p.field_73012_v.nextInt(10) == 0) {
                    this.rock_type = 12;
                }
            }
            this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)(1 + this.rock_type / 4));
            this.func_70606_j((float)(1 + this.rock_type / 4));
        }
        if (!this.field_70170_p.field_72995_K) {
            this.setRockType(this.rock_type);
        }
        if (this.field_70170_p.field_72995_K) {
            if (this.rock_type == 9 && this.field_70170_p.field_73012_v.nextInt(20) == 0) {
                this.field_70170_p.func_72869_a("flame", this.field_70165_t, this.field_70163_u, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f), (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f));
            }
            if (this.rock_type == 10 && this.field_70170_p.field_73012_v.nextInt(20) == 0) {
                this.field_70170_p.func_72869_a("happyVillager", this.field_70165_t, this.field_70163_u + 0.25, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f), (double)(this.field_70170_p.field_73012_v.nextFloat() / 2.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f));
            }
            if (this.rock_type == 11 && this.field_70170_p.field_73012_v.nextInt(20) == 0) {
                this.field_70170_p.func_72869_a("smoke", this.field_70165_t, this.field_70163_u, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f), (double)(this.field_70170_p.field_73012_v.nextFloat() / 10.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f));
            }
            if (this.rock_type == 12 && this.field_70170_p.field_73012_v.nextInt(20) == 0) {
                this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t, this.field_70163_u + 0.25, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f), (double)(this.field_70170_p.field_73012_v.nextFloat() / 5.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 60.0f));
            }
        }
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return null;
    }

    protected String func_70673_aS() {
        return null;
    }

    protected float func_70599_aP() {
        return 0.65f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return null;
    }

    public boolean func_70692_ba() {
        return false;
    }

    public boolean func_70601_bi() {
        return !(this.field_70163_u < 50.0);
    }

    public boolean func_70067_L() {
        return true;
    }

    public boolean func_70104_M() {
        return true;
    }

    public void func_70057_ab() {
        this.field_70738_aO = 0;
        this.field_70737_aN = 0;
        this.field_70739_aP = 0.0f;
    }

    protected void func_70609_aI() {
        this.func_70106_y();
    }

    public void func_70645_a(DamageSource par1DamageSource) {
        this.func_70106_y();
        if (this.rock_type == 1) {
            this.dropItemRand(OreSpawnMain.MySmallRock, 1);
        }
        if (this.rock_type == 2) {
            this.dropItemRand(OreSpawnMain.MyRock, 1);
        }
        if (this.rock_type == 3) {
            this.dropItemRand(OreSpawnMain.MyRedRock, 1);
        }
        if (this.rock_type == 4) {
            this.dropItemRand(OreSpawnMain.MyGreenRock, 1);
        }
        if (this.rock_type == 5) {
            this.dropItemRand(OreSpawnMain.MyBlueRock, 1);
        }
        if (this.rock_type == 6) {
            this.dropItemRand(OreSpawnMain.MyPurpleRock, 1);
        }
        if (this.rock_type == 7) {
            this.dropItemRand(OreSpawnMain.MySpikeyRock, 1);
        }
        if (this.rock_type == 8) {
            this.dropItemRand(OreSpawnMain.MyTNTRock, 1);
        }
        if (this.rock_type == 9) {
            this.dropItemRand(OreSpawnMain.MyCrystalRedRock, 1);
        }
        if (this.rock_type == 10) {
            this.dropItemRand(OreSpawnMain.MyCrystalGreenRock, 1);
        }
        if (this.rock_type == 11) {
            this.dropItemRand(OreSpawnMain.MyCrystalBlueRock, 1);
        }
        if (this.rock_type == 12) {
            this.dropItemRand(OreSpawnMain.MyCrystalTNTRock, 1);
        }
    }

    private ItemStack dropItemRand(Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)((OreSpawnMain.OreSpawnRand.nextFloat() - OreSpawnMain.OreSpawnRand.nextFloat()) / 3.0f), this.field_70163_u + 0.25, this.field_70161_v + (double)((OreSpawnMain.OreSpawnRand.nextFloat() - OreSpawnMain.OreSpawnRand.nextFloat()) / 3.0f), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("ButterflyType", this.rock_type);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.rock_type = par1NBTTagCompound.func_74762_e("ButterflyType");
    }
}

