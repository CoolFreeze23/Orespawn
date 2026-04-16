/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 */
package danger.orespawn.items;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import net.minecraft.item.Item;

public class ItemBasic
extends Item {
    public ItemBasic(String name) {
        this.func_77655_b(name);
        this.setRegistryName(name);
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add(this);
    }
}

