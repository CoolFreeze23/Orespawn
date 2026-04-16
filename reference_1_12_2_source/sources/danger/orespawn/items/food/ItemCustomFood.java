/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemFood
 */
package danger.orespawn.items.food;

import danger.orespawn.init.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;

public class ItemCustomFood
extends ItemFood {
    public ItemCustomFood(String name, int amount, boolean isWolfFood) {
        super(amount, isWolfFood);
        this.func_77655_b(name);
        this.setRegistryName(name);
        ModItems.ITEMS.add((Item)this);
    }
}

