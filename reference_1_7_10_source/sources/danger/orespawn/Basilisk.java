/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.GenericTargetSorter;
import danger.orespawn.LeafMonster;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
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
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class Basilisk
extends EntityMob {
    private GenericTargetSorter TargetSorter = null;
    private int hurt_timer = 0;
    private float moveSpeed = 0.4f;

    public Basilisk(World par1World) {
        super(par1World);
        this.func_70105_a(1.6f, 3.5f);
        this.field_70728_aV = 150;
        this.field_70174_ab = 2000;
        this.field_70178_ae = true;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMoveThroughVillage((EntityCreature)this, 1.0, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 20, 1.0));
        this.field_70714_bg.func_75776_a(3, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 8.0f));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAIHurtByTarget((EntityCreature)this, false));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.Basilisk_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    public int mygetMaxHealth() {
        return OreSpawnMain.Basilisk_stats.health;
    }

    public int func_70658_aO() {
        return OreSpawnMain.Basilisk_stats.defense;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    protected void func_70664_aZ() {
        this.field_70181_x += 0.25;
        super.func_70664_aZ();
    }

    public void func_70636_d() {
        super.func_70636_d();
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70146_Z.nextInt(200) == 0) {
            this.func_70691_i(1.0f);
        }
    }

    public int getBasiliskHealth() {
        return (int)this.func_110143_aJ();
    }

    protected String func_70639_aQ() {
        if (this.field_70146_Z.nextInt(2) == 0) {
            return "orespawn:basilisk_living";
        }
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:alo_hurt";
    }

    protected String func_70673_aS() {
        return "orespawn:emperorscorpion_death";
    }

    protected float func_70599_aP() {
        return 1.0f;
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
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    protected void func_70628_a(boolean par1, int par2) {
        int var4;
        ItemStack is = null;
        this.dropItemRand(OreSpawnMain.MyBasiliskScale, 1);
        this.dropItemRand(Items.field_151160_bD, 1);
        int i = 12 + this.field_70170_p.field_73012_v.nextInt(6);
        for (var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.field_151166_bC, 1);
        }
        i = 8 + this.field_70170_p.field_73012_v.nextInt(5);
        for (var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.field_151076_bf, 1);
        }
        i = 3 + this.field_70170_p.field_73012_v.nextInt(5);
        block15: for (var4 = 0; var4 < i; ++var4) {
            int var3 = this.field_70170_p.field_73012_v.nextInt(15);
            switch (var3) {
                case 1: {
                    is = this.dropItemRand(Items.field_151166_bC, 1);
                    continue block15;
                }
                case 2: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150475_bE), 1);
                    continue block15;
                }
                case 3: {
                    is = this.dropItemRand(OreSpawnMain.MyEmeraldSword, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77338_j, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block15;
                }
                case 4: {
                    is = this.dropItemRand(OreSpawnMain.MyEmeraldShovel, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block15;
                }
                case 5: {
                    is = this.dropItemRand(OreSpawnMain.MyEmeraldPickaxe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77346_s, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block15;
                }
                case 6: {
                    is = this.dropItemRand(OreSpawnMain.MyEmeraldAxe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block15;
                }
                case 7: {
                    is = this.dropItemRand(OreSpawnMain.MyEmeraldHoe, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                        is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77349_p, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block15;
                }
                case 8: {
                    is = this.dropItemRand((Item)OreSpawnMain.EmeraldHelmet, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(6) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77341_i, 1 + this.field_70170_p.field_73012_v.nextInt(5));
                    continue block15;
                }
                case 9: {
                    is = this.dropItemRand((Item)OreSpawnMain.EmeraldBody, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block15;
                }
                case 10: {
                    is = this.dropItemRand((Item)OreSpawnMain.EmeraldLegs, 1);
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
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block15;
                }
                case 11: {
                    is = this.dropItemRand((Item)OreSpawnMain.EmeraldBoots, 1);
                    if (this.field_70170_p.field_73012_v.nextInt(6) == 1) {
                        is.func_77966_a(Enchantment.field_77330_e, 5 + this.field_70170_p.field_73012_v.nextInt(5));
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(2) != 1) continue block15;
                    is.func_77966_a(Enchantment.field_77347_r, 2 + this.field_70170_p.field_73012_v.nextInt(4));
                    continue block15;
                }
            }
        }
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (super.func_70652_k(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
                int var2 = 8;
                if (this.field_70170_p.field_73013_u == EnumDifficulty.EASY) {
                    var2 = 10;
                }
                if (this.field_70170_p.field_73013_u == EnumDifficulty.NORMAL) {
                    var2 = 12;
                } else if (this.field_70170_p.field_73013_u == EnumDifficulty.HARD) {
                    var2 = 14;
                }
                if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
                    ((EntityLivingBase)par1Entity).func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, var2 * 20, 0));
                }
                double ks = 1.5;
                double inair = 0.15;
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
        if (this.hurt_timer > 0) {
            return false;
        }
        this.hurt_timer = 30;
        return super.func_70097_a(par1DamageSource, par2);
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.field_70170_p.field_73012_v.nextInt(5) == 0) {
            EntityLivingBase e = this.findSomethingToAttack();
            if (e != null) {
                this.func_70625_a((Entity)e, 10.0f, 10.0f);
                if (this.func_70068_e((Entity)e) < (double)((6.0f + e.field_70130_N / 2.0f) * (6.0f + e.field_70130_N / 2.0f))) {
                    this.setAttacking(1);
                    if (this.field_70170_p.field_73012_v.nextInt(3) == 0 || this.field_70170_p.field_73012_v.nextInt(4) == 1) {
                        this.func_70652_k((Entity)e);
                    }
                } else {
                    this.func_70661_as().func_75497_a((Entity)e, 1.25);
                }
                if (e instanceof EntityLivingBase) {
                    e.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 100, 5));
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(75) == 1 && this.func_110143_aJ() < (float)this.mygetMaxHealth()) {
            this.func_70691_i(1.0f);
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
        if (par1EntityLiving instanceof Basilisk) {
            return false;
        }
        if (par1EntityLiving instanceof LeafMonster) {
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
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(24.0, 7.0, 24.0));
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
                    if (s == null || !s.equals("Basilisk")) continue;
                    return true;
                }
            }
        }
        if (!this.func_70814_o()) {
            return false;
        }
        if (this.field_70170_p.func_72935_r()) {
            return false;
        }
        for (k = -1; k < 2; ++k) {
            for (j = -1; j < 2; ++j) {
                for (i = 1; i < 5; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid == Blocks.field_150350_a) continue;
                    return false;
                }
            }
        }
        Basilisk target = null;
        target = (Basilisk)this.field_70170_p.func_72857_a(Basilisk.class, this.field_70121_D.func_72314_b(20.0, 6.0, 20.0), (Entity)this);
        return target == null;
    }
}

