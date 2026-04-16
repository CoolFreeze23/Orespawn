/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item$ToolMaterial
 *  net.minecraftforge.common.util.EnumHelper
 */
package danger.orespawn.items.tools;

import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;

public class OrespawnToolMaterial {
    public final String Name;
    public final int HarvestLevel;
    public final int Durability;
    public final float Efficiency;
    public final float Damage;
    public final int Enchantability;
    public final Item.ToolMaterial Material;
    public static OrespawnToolMaterial UltimateTools = new OrespawnToolMaterial("ultimate", 10, 3000, 15.0f, 36.0f, 100);
    public static OrespawnToolMaterial EmeraldTools = new OrespawnToolMaterial("emerald", 2, 1000, 6.5f, 3.0f, 12);
    public static OrespawnToolMaterial AmethystTools = new OrespawnToolMaterial("emerald", 2, 1000, 6.5f, 3.0f, 12);

    public OrespawnToolMaterial(String name, int harvestLevel, int durability, float efficiency, float damage, int enchantability) {
        this.Name = name;
        this.HarvestLevel = harvestLevel;
        this.Durability = durability;
        this.Efficiency = efficiency;
        this.Damage = damage;
        this.Enchantability = enchantability;
        this.Material = EnumHelper.addToolMaterial((String)this.Name, (int)this.HarvestLevel, (int)this.Durability, (float)this.Efficiency, (float)this.Damage, (int)this.Enchantability);
    }
}

