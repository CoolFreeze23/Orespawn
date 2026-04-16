/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemAxe
 */
package danger.orespawn.items.tools;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import danger.orespawn.items.tools.OrespawnToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;

public class EmeraldAxe
extends ItemAxe {
    public EmeraldAxe() {
        super(OrespawnToolMaterial.EmeraldTools.Material, OrespawnToolMaterial.EmeraldTools.Damage, -3.0f);
        this.func_77655_b("emerald_axe");
        this.setRegistryName("emerald_axe");
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
    }
}

