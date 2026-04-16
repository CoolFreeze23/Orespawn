/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Ghost;
import danger.orespawn.GhostSkelly;
import danger.orespawn.Girlfriend;
import danger.orespawn.InkSack;
import danger.orespawn.Lizard;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.WaterBall;
import danger.orespawn.WaterDragon;
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
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class AttackSquid
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private EntityLivingBase buddy = null;
    private float moveSpeed = 0.25f;
    private int wasshot = 0;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public AttackSquid(World par1World) {
        super(par1World);
        this.func_70105_a(1.0f, 1.25f);
        this.func_70661_as().func_75491_a(false);
        this.field_70728_aV = 15;
        this.field_70174_ab = 3;
        this.field_70178_ae = false;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 16, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.AttackSquid_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void setWasShot() {
        this.wasshot = 250;
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.AttackSquid_stats.health;
    }

    public int func_70658_aO() {
        return OreSpawnMain.AttackSquid_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
    }

    public int getAttackStrength(Entity par1Entity) {
        int var2 = 2;
        return var2;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:squid_hurt";
    }

    protected String func_70673_aS() {
        return "orespawn:squid_death";
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

    protected float func_70599_aP() {
        return 1.0f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return Items.field_151115_aP;
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
        ItemStack is = null;
        int var4 = this.field_70170_p.field_73012_v.nextInt(50);
        switch (var4) {
            case 0: {
                is = this.dropItemRand(Items.field_151074_bl, 1);
                break;
            }
            case 1: {
                is = this.dropItemRand(Items.field_151043_k, 1);
                break;
            }
            case 2: {
                is = this.dropItemRand(Items.field_151150_bK, 1);
                break;
            }
            case 3: {
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
                if (this.field_70170_p.field_73012_v.nextInt(6) != 1) break;
                is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                break;
            }
            case 4: {
                is = this.dropItemRand(Items.field_151011_C, 1);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                }
                if (this.field_70170_p.field_73012_v.nextInt(6) != 1) break;
                is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                break;
            }
            case 5: {
                is = this.dropItemRand(Items.field_151005_D, 1);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                }
                if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                }
                if (this.field_70170_p.field_73012_v.nextInt(6) != 1) break;
                is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                break;
            }
            case 6: {
                is = this.dropItemRand(Items.field_151006_E, 1);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                }
                if (this.field_70170_p.field_73012_v.nextInt(6) != 1) break;
                is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                break;
            }
            case 7: {
                is = this.dropItemRand(Items.field_151013_M, 1);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                }
                if (this.field_70170_p.field_73012_v.nextInt(6) != 1) break;
                is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                break;
            }
            case 8: {
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
                    is.func_77966_a(Enchantment.field_77340_h, 1 + this.field_70170_p.field_73012_v.nextInt(2));
                }
                if (this.field_70170_p.field_73012_v.nextInt(6) != 1) break;
                is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                break;
            }
            case 9: {
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
                if (this.field_70170_p.field_73012_v.nextInt(2) != 1) break;
                is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                break;
            }
            case 10: {
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
                if (this.field_70170_p.field_73012_v.nextInt(2) != 1) break;
                is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                break;
            }
            case 11: {
                is = this.dropItemRand((Item)Items.field_151151_aj, 1);
                if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                    is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) != 1) break;
                is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                break;
            }
            case 12: {
                this.dropItemRand(Items.field_151153_ao, 1);
                break;
            }
            case 13: {
                this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150340_R), 1);
                break;
            }
            case 14: {
                EntityItem var3 = null;
                is = new ItemStack(Items.field_151153_ao, 1, 1);
                var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(3) - (double)OreSpawnMain.OreSpawnRand.nextInt(3), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(3) - (double)OreSpawnMain.OreSpawnRand.nextInt(3), is);
                if (var3 == null) break;
                this.field_70170_p.func_72838_d((Entity)var3);
                break;
            }
            case 15: 
            case 16: 
            case 17: {
                this.dropItemRand(Items.field_151100_aR, 1);
                break;
            }
        }
        int i = 1 + this.field_70170_p.field_73012_v.nextInt(3);
        for (var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.field_151115_aP, 1);
        }
    }

    public void initCreature() {
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70652_k(Entity par1Entity) {
        return super.func_70652_k(par1Entity);
    }

    protected void func_70069_a(float par1) {
        if (this.wasshot != 0) {
            return;
        }
        super.func_70069_a(par1);
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        if (this.field_70128_L) {
            return false;
        }
        Entity e = par1DamageSource.func_76346_g();
        if (e != null && e instanceof AttackSquid) {
            return false;
        }
        if (e != null && e instanceof WaterBall) {
            return false;
        }
        if (e != null && e instanceof WaterDragon) {
            return false;
        }
        if (e != null && e instanceof EntityLiving) {
            if (e instanceof AttackSquid) {
                return false;
            }
            if (e instanceof WaterDragon) {
                return false;
            }
            this.func_70624_b((EntityLivingBase)((EntityLiving)e));
            this.func_70784_b(e);
            this.func_70661_as().func_75497_a((Entity)((EntityLiving)e), 1.2);
            ret = true;
        }
        ret = super.func_70097_a(par1DamageSource, par2);
        if ((this.func_110143_aJ() <= 0.0f || this.field_70128_L) && this.field_70170_p.field_73011_w.field_76574_g != OreSpawnMain.DimensionID5 && !this.field_70170_p.field_72995_K && e != null && e instanceof EntityPlayer && this.field_70170_p.field_73012_v.nextInt(15) == 1 && OreSpawnMain.KrakenEnable != 0 && this.wasshot == 0) {
            int j = 1 + this.field_70170_p.field_73012_v.nextInt(3);
            for (int i = 0; i < j; ++i) {
                EntityCreature newent = (EntityCreature)AttackSquid.spawnCreature((World)this.field_70170_p, (String)"The Kraken", (double)(this.field_70165_t + (double)this.field_70170_p.field_73012_v.nextInt(4) - (double)this.field_70170_p.field_73012_v.nextInt(4)), (double)170.0, (double)(this.field_70161_v + (double)this.field_70170_p.field_73012_v.nextInt(4) - (double)this.field_70170_p.field_73012_v.nextInt(4)));
            }
        }
        return ret;
    }

    private boolean scan_it(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        Block bid;
        int j;
        int i;
        int found = 0;
        for (i = -dy; i <= dy; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_147439_a(x + dx, y + i, z + j);
                if ((bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_147439_a(x - dx, y + i, z + j)) != Blocks.field_150355_j && bid != Blocks.field_150358_i || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_147439_a(x + i, y + dy, z + j);
                if ((bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_147439_a(x + i, y - dy, z + j)) != Blocks.field_150355_j && bid != Blocks.field_150358_i || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                bid = this.field_70170_p.func_147439_a(x + i, y + j, z + dz);
                if ((bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_147439_a(x + i, y + j, z - dz)) != Blocks.field_150355_j && bid != Blocks.field_150358_i || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.wasshot > 0) {
            --this.wasshot;
            if (this.wasshot == 0) {
                this.func_70106_y();
                return;
            }
        }
        if (!this.func_70090_H() && this.field_70170_p.field_73012_v.nextInt(10) == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 12; ++i) {
                int j = i;
                if (j > 5) {
                    j = 5;
                }
                if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 5) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)(this.ty - 1), (double)this.tz, 1.33);
            } else {
                if (this.field_70170_p.field_73012_v.nextInt(25) == 1) {
                    this.func_70691_i(-1.0f);
                }
                if (this.func_110143_aJ() <= 0.0f) {
                    this.func_70106_y();
                    return;
                }
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
            EntityLivingBase e = this.findSomethingToAttack();
            if (e != null) {
                if (this.func_70068_e((Entity)e) < 9.0) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(4) == 0 || this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                        this.func_70652_k((Entity)e);
                    }
                } else {
                    this.func_70661_as().func_75497_a((Entity)e, 1.2);
                    this.watercanon(e);
                }
            } else {
                if (this.buddy != null) {
                    this.func_70661_as().func_75497_a((Entity)this.buddy, 1.0);
                }
                this.setAttacking(0);
            }
        }
    }

    private void watercanon(EntityLivingBase e) {
        double yoff = 1.0;
        double xzoff = 1.2;
        if (this.field_70170_p.field_73012_v.nextInt(5) == 1) {
            if (this.field_70146_Z.nextInt(3) == 1) {
                InkSack var2 = new InkSack(this.field_70170_p, e.field_70165_t - this.field_70165_t, e.field_70163_u + 0.75 - (this.field_70163_u + yoff), e.field_70161_v - this.field_70161_v);
                var2.func_70012_b(this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70759_as)), this.field_70163_u + yoff, this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z)), this.field_70759_as, this.field_70125_A);
                double var3 = e.field_70165_t - this.field_70165_t;
                double var5 = e.field_70163_u + 0.25 - var2.field_70163_u;
                double var7 = e.field_70161_v - this.field_70161_v;
                float var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
                var2.func_70186_c(var3, var5 + (double)var9, var7, 1.4f, 5.0f);
                this.field_70170_p.func_72956_a((Entity)this, "random.bow", 0.75f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
                this.field_70170_p.func_72838_d((Entity)var2);
            } else {
                WaterBall var2 = new WaterBall(this.field_70170_p, e.field_70165_t - this.field_70165_t, e.field_70163_u + 0.75 - (this.field_70163_u + yoff), e.field_70161_v - this.field_70161_v);
                var2.func_70012_b(this.field_70165_t - xzoff * Math.sin(Math.toRadians(this.field_70759_as)), this.field_70163_u + yoff, this.field_70161_v + xzoff * Math.cos(Math.toRadians(this.field_70177_z)), this.field_70759_as, this.field_70125_A);
                double var3 = e.field_70165_t - this.field_70165_t;
                double var5 = e.field_70163_u + 0.25 - var2.field_70163_u;
                double var7 = e.field_70161_v - this.field_70161_v;
                float var9 = MathHelper.func_76133_a((double)(var3 * var3 + var7 * var7)) * 0.2f;
                var2.func_70186_c(var3, var5 + (double)var9, var7, 1.4f, 5.0f);
                this.field_70170_p.func_72956_a((Entity)this, "random.bow", 0.75f, 1.0f / (this.func_70681_au().nextFloat() * 0.4f + 0.8f));
                this.field_70170_p.func_72838_d((Entity)var2);
            }
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
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
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
        if (par1EntityLiving instanceof EntityZombie) {
            return true;
        }
        if (par1EntityLiving instanceof EntityVillager) {
            return true;
        }
        if (par1EntityLiving instanceof EntitySpider) {
            return true;
        }
        if (par1EntityLiving instanceof EntityCaveSpider) {
            return true;
        }
        if (par1EntityLiving instanceof Ghost) {
            return false;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return false;
        }
        if (par1EntityLiving instanceof Lizard) {
            return true;
        }
        if (par1EntityLiving instanceof AttackSquid) {
            if (this.field_70170_p.field_73012_v.nextInt(5) == 1) {
                this.buddy = par1EntityLiving;
            }
            return false;
        }
        return this.wasshot != 0;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(10.0, 4.0, 10.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        EntityLivingBase e = this.func_70638_az();
        if (e != null && e.func_70089_S()) {
            return e;
        }
        this.func_70624_b(null);
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

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("WasShot", this.wasshot);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.wasshot = par1NBTTagCompound.func_74762_e("WasShot");
    }

    public boolean func_70601_bi() {
        super.func_70601_bi();
        if (this.field_70163_u < 50.0) {
            return false;
        }
        return this.field_70170_p.func_72935_r();
    }
}

