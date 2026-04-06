/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockDispenser
 *  net.minecraft.dispenser.BehaviorDefaultDispenseItem
 *  net.minecraft.dispenser.IBlockSource
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.EnumFacing
 */
package danger.orespawn;

import danger.orespawn.ItemSpawnEgg;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

final class DispenserBehaviorOreSpawnEgg
extends BehaviorDefaultDispenseItem {
    DispenserBehaviorOreSpawnEgg() {
    }

    public ItemStack func_82487_b(IBlockSource par1IBlockSource, ItemStack par2ItemStack) {
        EnumFacing enumfacing = BlockDispenser.func_149937_b((int)par1IBlockSource.func_82620_h());
        double d0 = par1IBlockSource.func_82615_a() + (double)enumfacing.func_82601_c() * 2.0;
        double d1 = (float)par1IBlockSource.func_82622_e() + 0.2f;
        double d2 = par1IBlockSource.func_82616_c() + (double)enumfacing.func_82599_e() * 2.0;
        Item it = par2ItemStack.func_77973_b();
        if (it instanceof ItemSpawnEgg) {
            ItemSpawnEgg ise = (ItemSpawnEgg)it;
            Entity entity = ItemSpawnEgg.spawn_something(ise.my_id, par1IBlockSource.func_82618_k(), (int)d0, (int)d1, (int)d2);
            if (entity instanceof EntityLivingBase && par2ItemStack.func_82837_s()) {
                ((EntityLiving)entity).func_94058_c(par2ItemStack.func_82833_r());
            }
        }
        par2ItemStack.func_77979_a(1);
        return par2ItemStack;
    }
}

