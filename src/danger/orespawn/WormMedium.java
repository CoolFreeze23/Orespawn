/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.WormSmall;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class WormMedium
extends EntityMob {
    public int upcount = 0;
    public int downcount = 0;

    public WormMedium(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 2.0f);
        this.func_70661_as().func_75491_a(true);
        this.field_70728_aV = 0;
        this.field_70145_X = true;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.WormMedium_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    protected boolean func_70692_ba() {
        return false;
    }

    protected float func_70599_aP() {
        return 0.5f;
    }

    protected float func_70647_i() {
        return 1.5f;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:little_splat";
    }

    protected String func_70673_aS() {
        return "orespawn:big_splat";
    }

    public boolean func_70104_M() {
        return true;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    protected void func_85033_bc() {
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.WormMedium_stats.health;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        Block bid = Blocks.field_150350_a;
        EntityPlayer target = null;
        WormSmall worms = null;
        super.func_70636_d();
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        worms = (WormSmall)this.field_70170_p.func_72857_a(WormSmall.class, this.field_70121_D.func_72314_b(8.0, 8.0, 8.0), (Entity)this);
        if (worms == null) {
            target = (EntityPlayer)this.field_70170_p.func_72857_a(EntityPlayer.class, this.field_70121_D.func_72314_b(8.0, 8.0, 8.0), (Entity)this);
        }
        if (worms == null && target != null || OreSpawnMain.PlayNicely != 0) {
            if (this.upcount > 0) {
                --this.upcount;
                if (this.upcount == 0) {
                    this.downcount = 100 + this.field_70170_p.field_73012_v.nextInt(150);
                }
                if (target != null) {
                    this.pointAtEntity((EntityLivingBase)target);
                }
                if ((bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)(this.field_70163_u + 0.25), (int)this.field_70161_v)) == Blocks.field_150329_H) {
                    bid = Blocks.field_150350_a;
                }
                if (bid != Blocks.field_150350_a) {
                    if (bid != Blocks.field_150349_c && bid != Blocks.field_150346_d && bid != Blocks.field_150348_b) {
                        this.func_70106_y();
                    }
                    this.field_70181_x += (double)0.2f;
                    this.field_70163_u += (double)0.1f;
                }
            } else {
                if (this.downcount > 0) {
                    --this.downcount;
                } else {
                    this.upcount = 25 + this.field_70170_p.field_73012_v.nextInt(75);
                }
                bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u + 3, (int)this.field_70161_v);
                if (bid == Blocks.field_150329_H) {
                    bid = Blocks.field_150350_a;
                }
                if (bid != Blocks.field_150350_a) {
                    if (bid != Blocks.field_150349_c && bid != Blocks.field_150346_d && bid != Blocks.field_150348_b) {
                        this.func_70106_y();
                    }
                    this.field_70181_x += (double)0.1f;
                    this.field_70163_u += (double)0.05f;
                }
            }
        } else {
            this.upcount = this.field_70170_p.field_73012_v.nextInt(50);
            this.downcount = 0;
            bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u + 3, (int)this.field_70161_v);
            if (bid == Blocks.field_150329_H) {
                bid = Blocks.field_150350_a;
            }
            if (bid != Blocks.field_150350_a) {
                if (bid != Blocks.field_150349_c && bid != Blocks.field_150346_d && bid != Blocks.field_150348_b) {
                    this.func_70106_y();
                }
                this.field_70181_x += (double)0.1f;
                this.field_70163_u += (double)0.05f;
            }
        }
        this.field_70181_x -= 0.01;
        this.field_70159_w = 0.0;
        this.field_70179_y = 0.0;
        this.field_70701_bs = 0.0f;
    }

    public void func_70071_h_() {
        if (this.func_104002_bU()) {
            this.field_70145_X = false;
        }
        super.func_70071_h_();
        this.field_70181_x *= 0.65;
    }

    public void pointAtEntity(EntityLivingBase e) {
        float f2;
        double d1 = e.field_70165_t - this.field_70165_t;
        double d2 = e.field_70161_v - this.field_70161_v;
        float d = (float)Math.atan2(d2, d1);
        this.field_70177_z = this.field_70759_as = (f2 = (float)((double)d * 180.0 / Math.PI) - 90.0f);
    }

    public int func_70658_aO() {
        return OreSpawnMain.WormMedium_stats.defense;
    }

    protected void func_70619_bc() {
        int bid = 0;
        EntityPlayer target = null;
        WormSmall worms = null;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (OreSpawnMain.PlayNicely != 0) {
            return;
        }
        worms = (WormSmall)this.field_70170_p.func_72857_a(WormSmall.class, this.field_70121_D.func_72314_b(8.0, 8.0, 8.0), (Entity)this);
        if (worms != null) {
            return;
        }
        target = (EntityPlayer)this.field_70170_p.func_72857_a(EntityPlayer.class, this.field_70121_D.func_72314_b(2.25, 8.0, 2.25), (Entity)this);
        if (target != null && target.field_71075_bZ.field_75098_d) {
            target = null;
        }
        if (target != null) {
            this.pointAtEntity((EntityLivingBase)target);
            if (this.upcount > 0 && this.field_70170_p.field_73012_v.nextInt(15) == 1 && !target.field_71075_bZ.field_75098_d) {
                super.func_70652_k((Entity)target);
                if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                    ItemStack boots = target.func_71124_b(1);
                    if (boots != null) {
                        target.func_70062_b(1, null);
                        bid = boots.func_77958_k() - boots.func_77960_j();
                        bid = bid > 15 ? (bid /= 15) : 1;
                        boots.func_77972_a(bid, (EntityLivingBase)this);
                        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(5) - (double)OreSpawnMain.OreSpawnRand.nextInt(5), this.field_70163_u + 3.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(5) - (double)OreSpawnMain.OreSpawnRand.nextInt(5), boots);
                        this.field_70170_p.func_72838_d((Entity)var3);
                    } else {
                        boots = target.func_71124_b(2);
                        if (boots != null) {
                            target.func_70062_b(2, null);
                            bid = boots.func_77958_k() - boots.func_77960_j();
                            bid = bid > 15 ? (bid /= 15) : 1;
                            boots.func_77972_a(bid, (EntityLivingBase)this);
                            EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(5) - (double)OreSpawnMain.OreSpawnRand.nextInt(5), this.field_70163_u + 3.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(5) - (double)OreSpawnMain.OreSpawnRand.nextInt(5), boots);
                            this.field_70170_p.func_72838_d((Entity)var3);
                        }
                    }
                }
            }
        }
    }

    protected boolean func_70041_e_() {
        return false;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_145773_az() {
        return true;
    }

    public boolean func_70601_bi() {
        return !this.field_70170_p.func_72935_r();
    }

    public void initCreature() {
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return ret;
        }
        ret = super.func_70097_a(par1DamageSource, par2);
        return ret;
    }

    protected Item func_146068_u() {
        return Items.field_151078_bh;
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(3) - (double)OreSpawnMain.OreSpawnRand.nextInt(3), this.field_70163_u + 2.5 + (double)this.field_70170_p.field_73012_v.nextInt(3), this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(3) - (double)OreSpawnMain.OreSpawnRand.nextInt(3), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        for (var4 = 0; var4 < 2; ++var4) {
            this.dropItemRand(Items.field_151078_bh, 1);
        }
        for (var4 = 0; var4 < 2; ++var4) {
            this.dropItemRand(Items.field_151116_aA, 1);
        }
    }
}

