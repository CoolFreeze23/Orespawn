/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIAvoidEntity
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIMate
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.ai.EntityAISwimming
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.monster.EntityMob
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.GenericTargetSorter;
import danger.orespawn.util.ai.MyEntityAIWanderALot;
import danger.orespawn.util.handlers.SoundsHandler;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Beaver
extends EntityAnimal {
    private float moveSpeed = 0.15f;
    private GenericTargetSorter TargetSorter = null;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Beaver(World par1World) {
        super(par1World);
        this.func_70105_a(0.6f, 0.8f);
        this.moveSpeed = 0.2f;
        this.field_70728_aV = 5;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAISwimming((EntityLiving)this));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIMate((EntityAnimal)this, 1.0));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityMob.class, 8.0f, 1.0, 1.5));
        this.field_70714_bg.func_75776_a(4, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.5));
        this.field_70714_bg.func_75776_a(5, (EntityAIBase)new EntityAIAvoidEntity((EntityCreature)this, EntityPlayer.class, 8.0f, 1.0, 1.5));
        this.field_70714_bg.func_75776_a(6, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 6.0f));
        this.field_70714_bg.func_75776_a(7, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 10, 1.0));
        this.field_70714_bg.func_75776_a(8, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        super.func_70071_h_();
    }

    private boolean isWood(Block bid) {
        if (bid == Blocks.field_150364_r) {
            return true;
        }
        return bid == Blocks.field_180407_aO || bid == Blocks.field_180390_bo || bid == Blocks.field_150472_an || bid == Blocks.field_180404_aQ || bid == Blocks.field_180392_bq || bid == Blocks.field_180405_aT || bid == Blocks.field_180387_bt || bid == Blocks.field_180403_aR || bid == Blocks.field_180386_br || bid == Blocks.field_180408_aP || bid == Blocks.field_180391_bp || bid == Blocks.field_180406_aS || bid == Blocks.field_180385_bs;
    }

    private boolean scan_it(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        Block bid;
        int j;
        int i;
        int found = 0;
        for (i = -dy; i <= dy; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + dx, y + i, z + j)).func_177230_c();
                if (this.isWood(bid) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if (!this.isWood(bid = this.field_70170_p.func_180495_p(new BlockPos(x - dx, y + i, z + j)).func_177230_c()) || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + dy, z + j)).func_177230_c();
                if (this.isWood(bid) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if (!this.isWood(bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y - dy, z + j)).func_177230_c()) || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z + dz)).func_177230_c();
                if (this.isWood(bid) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if (!this.isWood(bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z - dz)).func_177230_c()) || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    private ItemStack dropItemRand(Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), this.field_70163_u + 4.0 + (double)this.field_70170_p.field_73012_v.nextInt(4), this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(4) - (double)OreSpawnMain.OreSpawnRand.nextInt(4), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    public void breakRecursor(World world, int x, int y, int z, int xf, int yf, int zf, int recursion) {
        int var7 = 1;
        if (recursion > 200) {
            return;
        }
        for (int var9 = -var7; var9 <= var7; ++var9) {
            for (int var10 = -var7; var10 <= var7; ++var10) {
                for (int var11 = -var7; var11 <= var7; ++var11) {
                    Block var12;
                    if (var9 == 0 && var10 == 0 && var11 == 0 || x + var9 == xf && y + var10 == yf && z + var11 == zf || recursion > 0 && x + var9 >= xf - var7 && x + var9 <= xf + var7 && y + var10 >= yf - var7 && y + var10 <= yf + var7 && z + var11 >= zf - var7 && z + var11 <= zf + var7 || !this.isWood(var12 = world.func_180495_p(new BlockPos(x + var9, y + var10, z + var11)).func_177230_c())) continue;
                    world.func_175698_g(new BlockPos(x + var9, y + var10, z + var11));
                    this.dropItemRand(Item.func_150898_a((Block)var12), 1);
                    this.breakRecursor(world, x + var9, y + var10, z + var11, x, y, z, recursion + 1);
                }
            }
        }
    }

    protected void func_70619_bc() {
        Beaver buddy;
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        if ((this.field_70170_p.field_73012_v.nextInt(30) == 0 && this.getBeaverHealth() < this.mygetMaxHealth() || this.field_70170_p.field_73012_v.nextInt(350) == 1) && OreSpawnMain.PlayNicely == 0) {
            int i;
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (i = 1; i < 11; ++i) {
                int j = i;
                if (j > 2) {
                    j = 2;
                }
                if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u + 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 6) continue;
                ++i;
            }
            i = 0;
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)this.ty, (double)this.tz, 1.0);
                if (this.closest < 12) {
                    if (this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                        this.field_70170_p.func_175698_g(new BlockPos(this.tx, this.ty, this.tz));
                        this.breakRecursor(this.field_70170_p, this.tx, this.ty, this.tz, this.tx, this.ty, this.tz, i);
                    }
                    this.func_70691_i(1.0f);
                    this.func_184185_a(SoundsHandler.CHAINSAW, 1.0f, this.field_70170_p.field_73012_v.nextFloat() * 0.2f + 0.9f);
                }
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1 && (buddy = this.findBuddy()) != null) {
            this.func_70661_as().func_75492_a(buddy.field_70165_t, buddy.field_70163_u, buddy.field_70161_v, 0.5);
        }
        super.func_70619_bc();
    }

    private Beaver findBuddy() {
        List var5 = this.field_70170_p.func_72872_a(Beaver.class, this.func_174813_aQ().func_72321_a(16.0, 6.0, 16.0));
        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        Beaver var4 = null;
        if (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (Beaver)var3;
            return var4;
        }
        return null;
    }

    public boolean func_175446_cd() {
        return false;
    }

    public boolean func_70648_aU() {
        return true;
    }

    private int mygetMaxHealth() {
        return 15;
    }

    private int getBeaverHealth() {
        return (int)this.func_110143_aJ();
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        return null;
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return SoundsHandler.ENTITY_SCORPION_HIT;
    }

    public void func_70645_a(DamageSource cause) {
        super.func_70645_a(cause);
        this.func_184185_a(SoundsHandler.ENTITY_CRYO_DEATH, this.func_70599_aP(), this.func_70647_i());
    }

    protected float func_70599_aP() {
        return 0.4f;
    }

    protected Item func_146068_u() {
        return Items.field_151147_al;
    }

    protected float func_70647_i() {
        return this.func_70631_g_() ? (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.1f + 1.5f : (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.1f + 1.0f;
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        if (this.field_70163_u > 100.0) {
            return false;
        }
        Block bid = this.field_70170_p.func_180495_p(new BlockPos((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v)).func_177230_c();
        return bid == Blocks.field_150346_d || bid == Blocks.field_150349_c || bid == Blocks.field_150329_H || bid == Blocks.field_150362_t;
    }

    protected boolean func_70692_ba() {
        return false;
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return this.spawnBabyAnimal(entityageable);
    }

    public Beaver spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
        return new Beaver(this.field_70170_p);
    }

    public boolean isWheat(ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151034_e;
    }

    public boolean func_70877_b(ItemStack par1ItemStack) {
        return false;
    }
}

