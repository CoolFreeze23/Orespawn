/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.Vec3;

public class MyEntityAIWanderALot
extends EntityAIBase {
    private EntityCreature entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private double speed;
    private int xzRange = 10;
    private int busy = 0;

    public MyEntityAIWanderALot(EntityCreature par1EntityCreature, int par1, double par2) {
        this.entity = par1EntityCreature;
        this.xzRange = par1;
        this.speed = par2;
        this.func_75248_a(1);
    }

    public void setBusy(int i) {
        this.busy = i;
    }

    public boolean func_75250_a() {
        if (this.busy != 0) {
            return false;
        }
        if (this.entity.field_70170_p.field_73012_v.nextInt(30) != 0) {
            return false;
        }
        if (this.entity instanceof EntityTameable && ((EntityTameable)this.entity).func_70906_o()) {
            return false;
        }
        Vec3 var1 = RandomPositionGenerator.func_75463_a((EntityCreature)this.entity, (int)this.xzRange, (int)7);
        if (var1 == null) {
            return false;
        }
        this.xPosition = var1.field_72450_a;
        this.yPosition = var1.field_72448_b;
        this.zPosition = var1.field_72449_c;
        return true;
    }

    public boolean func_75253_b() {
        return !this.entity.func_70661_as().func_75500_f();
    }

    public void func_75249_e() {
        this.entity.func_70661_as().func_75492_a(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}

