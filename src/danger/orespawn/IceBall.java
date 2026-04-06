/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.MovingObjectPosition
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.LaserBall;
import danger.orespawn.MyUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class IceBall
extends LaserBall {
    private int my_index = 84;
    private int icemaker = 0;

    public IceBall(World par1World) {
        super(par1World);
        super.setIceBall();
    }

    public IceBall(World par1World, int par2) {
        super(par1World);
        super.setIceBall();
    }

    public IceBall(World par1World, EntityLivingBase par2EntityLiving) {
        super(par1World, par2EntityLiving);
        super.setIceBall();
    }

    public IceBall(World par1World, EntityLivingBase par2EntityLiving, int par3) {
        super(par1World, par2EntityLiving);
        super.setIceBall();
    }

    public IceBall(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
        super.setIceBall();
    }

    public int getIceBallIndex() {
        return this.my_index;
    }

    public void setIceMaker(int i) {
        this.icemaker = i;
    }

    @Override
    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (par1MovingObjectPosition.field_72308_g != null && MyUtils.isRoyalty(par1MovingObjectPosition.field_72308_g)) {
            this.func_70106_y();
            return;
        }
        super.func_70184_a(par1MovingObjectPosition);
        if (this.icemaker != 0) {
            for (int i = 0; i < 5; ++i) {
                int x = this.field_70170_p.field_73012_v.nextInt(4);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    x = -x;
                }
                int y = this.field_70170_p.field_73012_v.nextInt(4);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    y = -y;
                }
                int z = this.field_70170_p.field_73012_v.nextInt(4);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    z = -z;
                }
                x = (int)((double)x + par1MovingObjectPosition.field_72307_f.field_72450_a);
                y = (int)((double)y + par1MovingObjectPosition.field_72307_f.field_72448_b);
                z = (int)((double)z + par1MovingObjectPosition.field_72307_f.field_72449_c);
                this.field_70170_p.func_147449_b(x, y, z, Blocks.field_150432_aD);
            }
        }
        this.func_70106_y();
    }
}

