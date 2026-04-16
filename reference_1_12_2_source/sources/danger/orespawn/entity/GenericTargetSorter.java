/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.monster.EntityCreeper
 */
package danger.orespawn.entity;

import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;

public class GenericTargetSorter
implements Comparator {
    private Entity theEntity;

    public GenericTargetSorter(Entity par2Entity) {
        this.theEntity = par2Entity;
    }

    public int compareDistanceSq(Entity par1Entity, Entity par2Entity) {
        double weight = 0.0;
        double var3 = this.theEntity.func_70068_e(par1Entity);
        if (par1Entity instanceof EntityCreeper) {
            var3 /= 2.0;
        }
        if ((weight = (double)(par1Entity.field_70131_O * par1Entity.field_70130_N)) > 1.0) {
            var3 /= weight;
        }
        double var5 = this.theEntity.func_70068_e(par2Entity);
        if (par2Entity instanceof EntityCreeper) {
            var5 /= 2.0;
        }
        if ((weight = (double)(par2Entity.field_70131_O * par2Entity.field_70130_N)) > 1.0) {
            var5 /= weight;
        }
        return var3 < var5 ? -1 : (var3 > var5 ? 1 : 0);
    }

    public int compare(Object par1Obj, Object par2Obj) {
        return this.compareDistanceSq((Entity)par1Obj, (Entity)par2Obj);
    }
}

