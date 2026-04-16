/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.util.handlers.SoundsHandler;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WormDoom
extends EntityLiving {
    public double[] lposx = new double[100];
    public double[] lposy = new double[100];
    public double[] lposz = new double[100];
    public double[] rotpitch = new double[100];
    public double[] rotyaw = new double[100];
    public long lasttime = 0L;
    public int backoff = 0;
    public int inarow = 0;
    public float cycle;
    public float target_direction = 0.0f;
    public float local_rotationYaw = 0.0f;
    public float local_rotation_yaw_motion = 0.0f;
    Block bid;
    float updown = 0.0f;
    float local_motionX;
    float local_motionY;
    float local_motionZ;
    public float local_posX;
    public float local_posY;
    public float local_posZ;

    public WormDoom(World worldIn) {
        super(worldIn);
        WormDoom.func_184227_b((double)12.0);
        this.field_70158_ak = true;
        this.field_70177_z = 1.0f;
        this.local_rotationYaw = 1.0f;
        this.field_70145_X = true;
        double mx = (float)Math.sin(Math.toRadians(this.local_rotationYaw)) * 0.75f;
        double mz = (float)Math.cos(Math.toRadians(this.local_rotationYaw)) * 0.75f;
        mx /= 2.0;
        mz /= 2.0;
        for (int i = 0; i < 100; ++i) {
            this.lposx[i] = this.field_70165_t - mx * (double)i * 1.5;
            this.lposy[i] = this.field_70163_u + (double)((float)Math.sin(Math.toRadians(i * 10 + 180)) * 4.0f);
            this.lposz[i] = this.field_70161_v - mz * (double)i * 1.5;
            this.rotpitch[i] = 0.0;
            this.rotyaw[i] = this.local_rotationYaw;
        }
        this.cycle = worldIn.field_73012_v.nextFloat() * 360.0f;
        this.target_direction = this.local_rotationYaw;
        this.local_motionX = (float)mx;
        this.local_motionY = 0.0f;
        this.local_motionZ = (float)mz;
        this.local_posX = (float)this.field_70165_t;
        this.local_posY = (float)this.field_70163_u;
        this.local_posZ = (float)this.field_70161_v;
    }

    public boolean func_70112_a(double distance) {
        return true;
    }

    public boolean func_145770_h(double x, double y, double z) {
        return true;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        if (this.field_70170_p.field_72995_K) {
            for (int i = 99; i > 0; --i) {
                this.lposx[i] = this.lposx[i - 1];
                this.lposy[i] = this.lposy[i - 1];
                this.lposz[i] = this.lposz[i - 1];
                this.rotpitch[i] = this.rotpitch[i - 1];
                this.rotyaw[i] = this.rotyaw[i - 1];
            }
            this.lposx[0] = this.field_70165_t;
            this.lposy[0] = this.field_70163_u;
            this.lposz[0] = this.field_70161_v;
            this.rotpitch[0] = this.field_70125_A;
            this.rotyaw[0] = this.field_70177_z;
            this.field_70179_y = 0.0;
            this.field_70181_x = 0.0;
            this.field_70159_w = 0.0;
        } else {
            float myspeed = 0.3f;
            if (this.func_70631_g_()) {
                myspeed = 0.2f;
            }
            this.local_motionX = (float)Math.sin(Math.toRadians(this.local_rotationYaw)) * myspeed;
            this.local_motionZ = (float)Math.cos(Math.toRadians(this.local_rotationYaw)) * myspeed;
            int k = 0;
            int ht = 6;
            float frq = 10.0f;
            if (this.func_70631_g_()) {
                ht = 3;
                frq = 20.0f;
            }
            for (k = ht; k >= -ht; --k) {
                this.bid = this.field_70170_p.func_180495_p(new BlockPos((int)this.field_70165_t, (int)this.field_70163_u + k, (int)this.field_70161_v)).func_177230_c();
                if (this.bid != Blocks.field_150350_a) break;
            }
            this.updown = (float)Math.sin(Math.toRadians(this.cycle * frq)) * (float)ht;
            this.updown -= (float)k;
            this.updown = -this.updown;
            this.local_motionY *= 0.98f;
            this.local_motionY += 0.008f * this.updown;
            this.cycle = (this.cycle + 0.5f) % 360.0f;
            float dx = (float)Math.sqrt(this.local_motionX * this.local_motionX + this.local_motionZ * this.local_motionZ);
            float dz = (float)Math.atan2(-this.local_motionY, dx);
            this.field_70125_A = (float)Math.toDegrees(dz);
            float cdir = (float)Math.toRadians(this.local_rotationYaw);
            if (this.field_70170_p.field_73012_v.nextInt(100) == 1) {
                this.target_direction = 360.0f * this.field_70170_p.field_73012_v.nextFloat();
            }
            float tdir = (float)Math.toRadians(this.target_direction);
            float ddiff = tdir - cdir;
            while ((double)ddiff > Math.PI) {
                ddiff = (float)((double)ddiff - Math.PI * 2);
            }
            while ((double)ddiff < -Math.PI) {
                ddiff = (float)((double)ddiff + Math.PI * 2);
            }
            this.local_rotation_yaw_motion *= 0.95f;
            this.local_rotation_yaw_motion = (float)((double)this.local_rotation_yaw_motion + (double)(ddiff * 180.0f) / Math.PI / 20.0);
            this.local_rotationYaw += this.local_rotation_yaw_motion;
            this.field_70177_z = -this.local_rotationYaw;
            this.field_70159_w = this.local_motionX;
            this.field_70181_x = this.local_motionY;
            this.field_70179_y = -this.local_motionZ;
            System.out.println(this.field_70181_x);
        }
    }

    public boolean func_189652_ae() {
        return true;
    }

    public void func_180430_e(float distance, float damageMultiplier) {
    }

    protected void func_184231_a(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(3000.0);
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
    }

    @Nullable
    protected SoundEvent func_184639_G() {
        int which = this.field_70170_p.field_73012_v.nextInt(4);
        if (which == 0) {
            return SoundsHandler.ENTITY_CATERKILLER_LIVING1;
        }
        if (which == 1) {
            return SoundsHandler.ENTITY_CATERKILLER_LIVING2;
        }
        if (which == 2) {
            return SoundsHandler.ENTITY_CATERKILLER_LIVING3;
        }
        return SoundsHandler.ENTITY_CATERKILLER_LIVING4;
    }

    @Nullable
    protected SoundEvent func_184601_bQ(DamageSource damageSourceIn) {
        return SoundsHandler.ENTITY_ALOSAURUS_HURT;
    }

    @Nullable
    protected SoundEvent func_184615_bR() {
        return SoundsHandler.ENTITY_ALOSAURUS_DEATH;
    }
}

