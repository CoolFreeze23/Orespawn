/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.dispenser.BehaviorProjectileDispense
 *  net.minecraft.dispenser.IPosition
 *  net.minecraft.entity.IProjectile
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.IrukandjiArrow;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.world.World;

final class MyDispenserBehaviorArrow
extends BehaviorProjectileDispense {
    MyDispenserBehaviorArrow() {
    }

    protected IProjectile func_82499_a(World par1World, IPosition par2IPosition) {
        IrukandjiArrow entityarrow = new IrukandjiArrow(par1World, par2IPosition.func_82615_a(), par2IPosition.func_82617_b(), par2IPosition.func_82616_c());
        entityarrow.field_70251_a = 1;
        return entityarrow;
    }
}

