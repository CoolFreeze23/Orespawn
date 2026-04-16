/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemSword
 */
package danger.orespawn.items.tools;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import danger.orespawn.items.tools.OrespawnToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

public class GenericSword
extends ItemSword {
    public GenericSword(String name, OrespawnToolMaterial material) {
        super(material.Material);
        this.func_77655_b(name);
        this.setRegistryName(name);
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
    }
}

