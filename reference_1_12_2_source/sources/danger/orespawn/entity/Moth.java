/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.passive.IAnimals
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Moth
extends EntityCreature
implements IAnimals {
    private BlockPos currentFlightTarget = null;
    public int moth_type = OreSpawnMain.OreSpawnRand.nextInt(4);
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Moth(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 0.5f);
    }

    public void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(1.0);
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    public boolean func_175446_cd() {
        return false;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= 0.6;
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
                if ((bid == Blocks.field_150478_aa || bid == ModBlocks.EXTREME_TORCH) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x - dx, y + i, z + j)).func_177230_c()) != Blocks.field_150478_aa && bid != ModBlocks.EXTREME_TORCH || (d = dx * dx + j * j + i * i) >= this.closest) continue;
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
                if ((bid == Blocks.field_150478_aa || bid == ModBlocks.EXTREME_TORCH) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y - dy, z + j)).func_177230_c()) != Blocks.field_150478_aa && bid != ModBlocks.EXTREME_TORCH || (d = dy * dy + j * j + i * i) >= this.closest) continue;
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
                if ((bid == Blocks.field_150478_aa || bid == ModBlocks.EXTREME_TORCH) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if ((bid = this.field_70170_p.func_180495_p(new BlockPos(x + i, y + j, z - dz)).func_177230_c()) != Blocks.field_150478_aa && bid != ModBlocks.EXTREME_TORCH || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    public void func_70619_bc() {
        int keep_trying = 25;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70146_Z.nextInt(100) == 0 || this.currentFlightTarget.func_177954_c((double)((int)this.field_70165_t), (double)((int)this.field_70163_u), (double)((int)this.field_70161_v)) < 4.0) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                this.currentFlightTarget = new BlockPos((int)this.field_70165_t + this.field_70146_Z.nextInt(10) - this.field_70146_Z.nextInt(10), (int)this.field_70163_u + this.field_70146_Z.nextInt(6) - 2, (int)this.field_70161_v + this.field_70146_Z.nextInt(10) - this.field_70146_Z.nextInt(10));
                bid = this.field_70170_p.func_180495_p(new BlockPos(this.currentFlightTarget.func_177958_n(), this.currentFlightTarget.func_177956_o(), this.currentFlightTarget.func_177952_p())).func_177230_c();
                --keep_trying;
            }
        } else if (!this.field_70170_p.func_72935_r() && this.field_70146_Z.nextInt(10) == 0) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 2; i < 15 && !this.scan_it((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, i, i, i); ++i) {
                if (i < 6) continue;
                ++i;
            }
            if (this.closest < 99999) {
                this.currentFlightTarget = new BlockPos(this.tx, this.ty + 1, this.tz);
            }
        }
        double var1 = (double)this.currentFlightTarget.func_177958_n() + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.func_177956_o() + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.func_177952_p() + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.5 - this.field_70159_w) * (double)0.1f;
        this.field_70181_x += (Math.signum(var3) * 0.68 - this.field_70181_x) * (double)0.1f;
        this.field_70179_y += (Math.signum(var5) * 0.5 - this.field_70179_y) * (double)0.1f;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_191988_bg = 0.75f;
        this.field_70177_z += var8;
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
        return !(this.field_70163_u < 50.0);
    }
}

