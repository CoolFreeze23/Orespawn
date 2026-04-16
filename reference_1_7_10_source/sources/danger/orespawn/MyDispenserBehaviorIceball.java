/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.IceBall;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.world.World;

final class MyDispenserBehaviorIceball
extends BehaviorProjectileDispense {
    MyDispenserBehaviorIceball() {
    }

    protected IProjectile func_82499_a(World par1World, IPosition par2IPosition) {
        IceBall entityarrow = new IceBall(par1World, par2IPosition.func_82615_a(), par2IPosition.func_82617_b(), par2IPosition.func_82616_c());
        return entityarrow;
    }
}

