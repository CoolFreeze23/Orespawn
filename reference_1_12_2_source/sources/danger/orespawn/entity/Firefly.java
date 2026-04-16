/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.passive.EntityAmbientCreature
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.init.ModBlocks;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Firefly
extends EntityAmbientCreature {
    int my_blink = 20 + this.field_70146_Z.nextInt(20);
    int blinker = 0;
    int myspace = 0;
    private BlockPos currentFlightTarget = null;

    public Firefly(World par1World) {
        super(par1World);
        this.func_70105_a(0.4f, 0.8f);
    }

    public boolean func_70112_a(double distance) {
        double d0 = this.func_174813_aQ().func_72320_b();
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }
        return distance < (d0 = d0 * 64.0 * 3.0) * d0;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
    }

    public float getBlink() {
        if (this.blinker < this.my_blink / 2) {
            return 240.0f;
        }
        return 0.0f;
    }

    protected float func_70599_aP() {
        return 0.0f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        return null;
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return null;
    }

    public boolean func_70104_M() {
        return true;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    protected void func_85033_bc() {
    }

    public int mygetMaxHealth() {
        return 1;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)ModBlocks.EXTREME_TORCH);
    }

    public boolean func_175446_cd() {
        return false;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= 0.600000023841;
        ++this.blinker;
        if (this.blinker > this.my_blink) {
            this.blinker = 0;
        }
        if (this.func_104002_bU()) {
            return;
        }
        long t = this.field_70170_p.func_72820_D();
        if ((t %= 24000L) > 11000L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(500) == 1) {
            this.func_70106_y();
        }
    }

    protected void func_70619_bc() {
        int keep_trying = 25;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70146_Z.nextInt(40) == 0 || this.currentFlightTarget.func_177954_c((double)((int)this.field_70165_t), (double)((int)this.field_70163_u), (double)((int)this.field_70161_v)) < 2.0) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                this.currentFlightTarget = new BlockPos((int)this.field_70165_t + this.field_70146_Z.nextInt(4) - this.field_70146_Z.nextInt(4), (int)this.field_70163_u + this.field_70146_Z.nextInt(4) - 2, (int)this.field_70161_v + this.field_70146_Z.nextInt(4) - this.field_70146_Z.nextInt(4));
                bid = this.field_70170_p.func_180495_p(new BlockPos(this.currentFlightTarget.func_177958_n(), this.currentFlightTarget.func_177956_o(), this.currentFlightTarget.func_177952_p())).func_177230_c();
                --keep_trying;
            }
        }
        double var1 = (double)this.currentFlightTarget.func_177958_n() + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.func_177956_o() + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.func_177952_p() + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.2 - this.field_70159_w) * 0.1;
        this.field_70181_x += (Math.signum(var3) * (double)0.7f - this.field_70181_x) * 0.1;
        this.field_70179_y += (Math.signum(var5) * 0.2 - this.field_70179_y) * 0.1;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_191988_bg = 0.2f;
        this.field_70177_z += var8 / 4.0f;
    }

    protected boolean func_70041_e_() {
        return false;
    }

    public void func_180430_e(float distance, float damageMultiplier) {
    }

    protected void func_184231_a(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public boolean func_145773_az() {
        return true;
    }

    public boolean func_70601_bi() {
        Block bid = this.field_70170_p.func_180495_p(new BlockPos((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v)).func_177230_c();
        if (bid != Blocks.field_150350_a) {
            return false;
        }
        if (this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.findBuddies() > 10) {
            return false;
        }
        return !(this.field_70163_u < 50.0);
    }

    private int findBuddies() {
        List var5 = this.field_70170_p.func_72872_a(Firefly.class, this.func_174813_aQ().func_72321_a(20.0, 8.0, 20.0));
        return var5.size();
    }

    protected boolean func_70692_ba() {
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        return !this.func_104002_bU();
    }
}

