/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.RandomPositionGenerator
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.util.math.Vec3d
 */
package danger.orespawn.util.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.Vec3d;

public class MyEntityAIWander
extends EntityAIBase {
    private EntityCreature entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private float speed;

    public MyEntityAIWander(EntityCreature par1EntityCreature, float par2) {
        this.entity = par1EntityCreature;
        this.speed = par2;
        this.func_75248_a(1);
    }

    public boolean func_75250_a() {
        if (this.entity.func_70681_au().nextInt(90) != 0) {
            return false;
        }
        if (this.entity instanceof EntityTameable && ((EntityTameable)this.entity).func_70906_o()) {
            return false;
        }
        Vec3d var1 = RandomPositionGenerator.func_75463_a((EntityCreature)this.entity, (int)10, (int)7);
        if (var1 == null) {
            return false;
        }
        this.xPosition = var1.field_72450_a;
        this.yPosition = var1.field_72448_b;
        this.zPosition = var1.field_72449_c;
        return true;
    }

    public boolean continueExecuting() {
        EntityTameable gf;
        EntityLivingBase var1;
        if (this.entity != null && this.entity instanceof EntityTameable && (var1 = (gf = (EntityTameable)this.entity).func_70902_q()) != null && (int)gf.field_70161_v == (int)var1.field_70161_v && (int)gf.field_70165_t == (int)var1.field_70165_t && (int)gf.field_70163_u < (int)var1.field_70163_u + 2 && (int)gf.field_70163_u > (int)var1.field_70163_u - 2) {
            return false;
        }
        return !this.entity.func_70661_as().func_75500_f();
    }

    public void func_75249_e() {
        this.entity.func_70661_as().func_75492_a(this.xPosition, this.yPosition, this.zPosition, (double)this.speed);
    }
}

