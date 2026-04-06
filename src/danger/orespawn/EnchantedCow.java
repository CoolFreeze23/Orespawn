/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.passive.EntityCow
 *  net.minecraft.init.Items
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.RedCow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EnchantedCow
extends RedCow {
    public EnchantedCow(World world) {
        super(world);
    }

    private void dropEnchantedGoldenApple() {
        EntityItem var3 = new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u + 1.0, this.field_70161_v, new ItemStack(Items.field_151153_ao, 1, 1));
        this.field_70170_p.func_72838_d((Entity)var3);
    }

    @Override
    protected void func_70628_a(boolean par1, int par2) {
        int var3 = this.field_70146_Z.nextInt(4) + this.field_70146_Z.nextInt(1 + par2);
        for (int var4 = 0; var4 < var3; ++var4) {
            this.func_145779_a(Items.field_151034_e, 1);
        }
        this.func_145779_a(Items.field_151153_ao, 2);
        this.dropEnchantedGoldenApple();
        super.func_70628_a(par1, par2);
    }

    @Override
    public EntityCow func_90011_a(EntityAgeable entityageable) {
        return this.spawnBabyAnimal(entityageable);
    }

    @Override
    public EnchantedCow spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
        return new EnchantedCow(this.field_70170_p);
    }
}

