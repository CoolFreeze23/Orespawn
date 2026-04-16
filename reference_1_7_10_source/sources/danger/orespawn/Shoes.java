/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class Shoes
extends EntityThrowable {
    public int ShoeId = 0;
    private float my_rotation = 0.0f;

    public Shoes(World par1World) {
        super(par1World);
        this.ShoeId = this.field_70146_Z.nextInt(4) + 2;
        this.field_70180_af.func_75682_a(20, (Object)this.ShoeId);
    }

    public Shoes(World par1World, int par2) {
        super(par1World);
        this.ShoeId = par2;
        this.field_70180_af.func_75682_a(20, (Object)this.ShoeId);
    }

    public Shoes(World par1World, EntityLivingBase par2EntityLiving) {
        super(par1World, par2EntityLiving);
        this.ShoeId = this.field_70146_Z.nextInt(4) + 2;
        this.field_70180_af.func_75682_a(20, (Object)this.ShoeId);
    }

    public Shoes(World par1World, EntityLivingBase par2EntityLiving, int par3) {
        super(par1World, par2EntityLiving);
        this.ShoeId = par3;
        this.field_70180_af.func_75682_a(20, (Object)this.ShoeId);
    }

    public Shoes(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
        this.ShoeId = this.field_70146_Z.nextInt(4) + 2;
        this.field_70180_af.func_75682_a(20, (Object)this.ShoeId);
    }

    public int getShoeId() {
        return this.field_70180_af.func_75679_c(20);
    }

    protected void func_70184_a(MovingObjectPosition par1MovingObjectPosition) {
        if (par1MovingObjectPosition.field_72308_g != null) {
            float var2 = 2.0f;
            if (this.getShoeId() == 6) {
                var2 = 6.0f;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityCreeper) {
                var2 += 4.0f;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Girlfriend) {
                var2 = 1.0f;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof Boyfriend) {
                var2 = 1.0f;
            }
            if (par1MovingObjectPosition.field_72308_g instanceof EntityPlayer) {
                var2 = 0.0f;
            }
            if (OreSpawnMain.valentines_day != 0) {
                var2 = 10.0f;
            }
            par1MovingObjectPosition.field_72308_g.func_70097_a(DamageSource.func_76356_a((Entity)this, (Entity)this.func_85052_h()), var2);
        }
        for (int var3 = 0; var3 < 4; ++var3) {
            this.field_70170_p.func_72869_a("snowballpoof", this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0, 0.0, 0.0);
            this.field_70170_p.func_72869_a("reddust", this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0, 0.0, 0.0);
        }
        if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
        }
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.my_rotation += 20.0f;
        while (this.my_rotation > 360.0f) {
            this.my_rotation -= 360.0f;
        }
        this.field_70125_A = this.field_70127_C = this.my_rotation;
    }
}

