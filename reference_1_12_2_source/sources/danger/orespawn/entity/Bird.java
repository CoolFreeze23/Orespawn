/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.passive.IAnimals
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.util.handlers.SoundsHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Bird
extends EntityCreature
implements IAnimals {
    private BlockPos spawnPosition;
    public int birdType;

    public Bird(World worldIn) {
        super(worldIn);
        this.func_70105_a(0.4f, 0.4f);
        this.birdType = this.field_70146_Z.nextInt(6);
    }

    public void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(2.0);
    }

    public void func_70619_bc() {
        super.func_70619_bc();
        if (!(this.spawnPosition == null || this.field_70170_p.func_175623_d(this.spawnPosition) && this.spawnPosition.func_177956_o() >= 1)) {
            this.spawnPosition = null;
        }
        if (this.spawnPosition == null || this.field_70146_Z.nextInt(30) == 0 || this.spawnPosition.func_177954_c((double)((int)this.field_70165_t), (double)((int)this.field_70163_u), (double)((int)this.field_70161_v)) < 4.0) {
            this.spawnPosition = new BlockPos((int)this.field_70165_t + this.field_70146_Z.nextInt(7) - this.field_70146_Z.nextInt(7), (int)this.field_70163_u + this.field_70146_Z.nextInt(6) - 2, (int)this.field_70161_v + this.field_70146_Z.nextInt(7) - this.field_70146_Z.nextInt(7));
        }
        double d0 = (double)this.spawnPosition.func_177958_n() + 0.5 - this.field_70165_t;
        double d1 = (double)this.spawnPosition.func_177956_o() + 0.1 - this.field_70163_u;
        double d2 = (double)this.spawnPosition.func_177952_p() + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(d0) * 0.5 - this.field_70159_w) * (double)0.1f;
        this.field_70181_x += (Math.signum(d1) * (double)0.7f - this.field_70181_x) * (double)0.1f;
        this.field_70179_y += (Math.signum(d2) * 0.5 - this.field_70179_y) * (double)0.1f;
        float f = (float)(MathHelper.func_181159_b((double)this.field_70179_y, (double)this.field_70159_w) * 57.29577951308232) - 90.0f;
        float f1 = MathHelper.func_76142_g((float)(f - this.field_70177_z));
        this.field_191988_bg = 0.5f;
        this.field_70177_z += f1;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= (double)0.6f;
        if (this.field_70170_p.func_175657_ab() > 7) {
            this.func_70106_y();
        }
    }

    public void func_180430_e(float distance, float damageMultiplier) {
    }

    protected void func_184231_a(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    protected float func_70599_aP() {
        return 0.55f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected SoundEvent func_184639_G() {
        if (this.field_70146_Z.nextInt(4) == 0) {
            switch (this.field_70146_Z.nextInt(23) + 1) {
                case 1: {
                    return SoundsHandler.ENTITY_BIRD_BIRD1;
                }
                case 2: {
                    return SoundsHandler.ENTITY_BIRD_BIRD2;
                }
                case 3: {
                    return SoundsHandler.ENTITY_BIRD_BIRD3;
                }
                case 4: {
                    return SoundsHandler.ENTITY_BIRD_BIRD4;
                }
                case 5: {
                    return SoundsHandler.ENTITY_BIRD_BIRD5;
                }
                case 6: {
                    return SoundsHandler.ENTITY_BIRD_BIRD6;
                }
                case 7: {
                    return SoundsHandler.ENTITY_BIRD_BIRD7;
                }
                case 8: {
                    return SoundsHandler.ENTITY_BIRD_BIRD8;
                }
                case 9: {
                    return SoundsHandler.ENTITY_BIRD_BIRD9;
                }
                case 10: {
                    return SoundsHandler.ENTITY_BIRD_BIRD10;
                }
                case 11: {
                    return SoundsHandler.ENTITY_BIRD_BIRD11;
                }
                case 12: {
                    return SoundsHandler.ENTITY_BIRD_BIRD12;
                }
                case 13: {
                    return SoundsHandler.ENTITY_BIRD_BIRD13;
                }
                case 14: {
                    return SoundsHandler.ENTITY_BIRD_BIRD14;
                }
                case 15: {
                    return SoundsHandler.ENTITY_BIRD_BIRD15;
                }
                case 16: {
                    return SoundsHandler.ENTITY_BIRD_BIRD16;
                }
                case 17: {
                    return SoundsHandler.ENTITY_BIRD_BIRD17;
                }
                case 18: {
                    return SoundsHandler.ENTITY_BIRD_BIRD18;
                }
                case 19: {
                    return SoundsHandler.ENTITY_BIRD_BIRD19;
                }
                case 20: {
                    return SoundsHandler.ENTITY_BIRD_BIRD20;
                }
                case 21: {
                    return SoundsHandler.ENTITY_BIRD_BIRD21;
                }
                case 22: {
                    return SoundsHandler.ENTITY_BIRD_BIRD22;
                }
            }
            return SoundsHandler.ENTITY_BIRD_BIRD23;
        }
        return null;
    }
}

