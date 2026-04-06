/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.monster.EntityCreeper
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.projectile.EntityThrowable
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.world.World
 */
package danger.orespawn;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class SunspotUrchin
extends EntityThrowable {
    private float my_rotation = 0.0f;
    private int my_index = 50;

    public SunspotUrchin(World par1World) {
        super(par1World);
    }

    public SunspotUrchin(World par1World, int par2) {
        super(par1World);
    }

    public SunspotUrchin(World par1World, EntityLivingBase par2EntityLiving) {
        super(par1World, par2EntityLiving);
    }

    public SunspotUrchin(World par1World, EntityLivingBase par2EntityLiving, int par3) {
        super(par1World, par2EntityLiving);
    }

    public SunspotUrchin(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
    }

    public int getUrchinIndex() {
        return this.my_index;
    }

    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (par1MovingObjectPosition.field_72308_g != null) {
            float var2 = 3.0f;
            if (par1MovingObjectPosition.field_72308_g instanceof EntityCreeper) {
                var2 = 6.0f;
            }
            if (!(par1MovingObjectPosition.field_72308_g instanceof EntityPlayer)) {
                par1MovingObjectPosition.field_72308_g.func_70097_a(DamageSource.func_76356_a((Entity)this, (Entity)this.func_85052_h()), var2);
                if (!par1MovingObjectPosition.field_72308_g.func_70045_F()) {
                    par1MovingObjectPosition.field_72308_g.func_70015_d(5);
                }
            }
        } else {
            int i = par1MovingObjectPosition.field_72311_b;
            int j = par1MovingObjectPosition.field_72312_c;
            int k = par1MovingObjectPosition.field_72309_d;
            switch (par1MovingObjectPosition.field_72310_e) {
                case 0: {
                    --j;
                    break;
                }
                case 1: {
                    ++j;
                    break;
                }
                case 2: {
                    --k;
                    break;
                }
                case 3: {
                    ++k;
                    break;
                }
                case 4: {
                    --i;
                    break;
                }
                case 5: {
                    ++i;
                }
            }
            if (this.field_70170_p.func_147437_c(i, j, k)) {
                this.field_70170_p.func_147449_b(i, j, k, (Block)Blocks.field_150480_ab);
            }
        }
        for (int var3 = 0; var3 < 5; ++var3) {
            this.field_70170_p.func_72869_a("smoke", this.field_70165_t, this.field_70163_u, this.field_70161_v, (double)this.field_70170_p.field_73012_v.nextFloat(), (double)this.field_70170_p.field_73012_v.nextFloat(), (double)this.field_70170_p.field_73012_v.nextFloat());
            this.field_70170_p.func_72869_a("reddust", this.field_70165_t, this.field_70163_u, this.field_70161_v, (double)this.field_70170_p.field_73012_v.nextFloat(), (double)this.field_70170_p.field_73012_v.nextFloat(), (double)this.field_70170_p.field_73012_v.nextFloat());
        }
        if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
        }
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.func_70015_d(1);
        this.my_rotation += 30.0f;
        while (this.my_rotation > 360.0f) {
            this.my_rotation -= 360.0f;
        }
        this.field_70125_A = this.field_70127_C = this.my_rotation;
        this.field_70170_p.func_72869_a("smoke", this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0, 0.0, 0.0);
    }
}

