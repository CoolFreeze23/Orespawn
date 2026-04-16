/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Coin
extends EntityAnimal {
    private float moveSpeed = 0.0f;

    public Coin(World par1World) {
        super(par1World);
        this.func_70105_a(1.5f, 1.5f);
        this.field_70728_aV = 10;
        this.field_70174_ab = 100;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void func_70071_h_() {
        super.func_70071_h_();
    }

    public int mygetMaxHealth() {
        return 1;
    }

    public int func_70658_aO() {
        return 0;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70636_d() {
        super.func_70636_d();
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
        return 1.0f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected Item func_146068_u() {
        return null;
    }

    private void dropItemRand(Item index, int par1) {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), new ItemStack(index, par1, 0));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    protected void func_70628_a(boolean par1, int par2) {
        int i = this.field_70170_p.field_73012_v.nextInt(10);
        Item j = OreSpawnMain.MyEmeraldSword;
        if (i == 0) {
            j = Items.field_151045_i;
        }
        if (i == 1) {
            j = OreSpawnMain.UraniumNugget;
        }
        if (i == 2) {
            j = OreSpawnMain.TitaniumNugget;
        }
        if (i == 3) {
            j = Items.field_151166_bC;
        }
        if (i == 4) {
            j = OreSpawnMain.MyEmeraldAxe;
        }
        if (i == 5) {
            j = OreSpawnMain.MyEmeraldShovel;
        }
        if (i == 6) {
            j = OreSpawnMain.MyEmeraldPickaxe;
        }
        if (i == 7) {
            j = OreSpawnMain.MyEmeraldHoe;
        }
        if (i == 8) {
            j = OreSpawnMain.CoinEgg;
        }
        this.dropItemRand(j, 1);
    }

    public void initCreature() {
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        return false;
    }

    public boolean func_70601_bi() {
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70163_u < 50.0) {
            return false;
        }
        Coin target = null;
        target = (Coin)this.field_70170_p.func_72857_a(Coin.class, this.field_70121_D.func_72314_b(20.0, 8.0, 20.0), (Entity)this);
        return target == null;
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return null;
    }
}

