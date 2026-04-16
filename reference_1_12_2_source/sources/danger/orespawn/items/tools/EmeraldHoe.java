/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemHoe
 */
package danger.orespawn.items.tools;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import danger.orespawn.items.tools.OrespawnToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;

public class EmeraldHoe
extends ItemHoe {
    public EmeraldHoe() {
        super(OrespawnToolMaterial.EmeraldTools.Material);
        this.func_77655_b("emerald_hoe");
        this.setRegistryName("emerald_hoe");
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
    }
}

