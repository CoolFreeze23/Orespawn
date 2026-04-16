/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.init.Enchantments
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.ItemArmor$ArmorMaterial
 *  net.minecraft.util.SoundEvent
 *  net.minecraftforge.common.util.EnumHelper
 */
package danger.orespawn.items.armor;

import danger.orespawn.util.ItemEnchantments;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;

public class OrespawnArmorMaterial {
    public final String Name;
    public final int Durability;
    public final int[] Protections;
    public final int Enchantability;
    public final float Toughness;
    public final ItemEnchantments EnchantmentLevels;
    public final ItemArmor.ArmorMaterial Material;
    public static final OrespawnArmorMaterial UltimateArmor = new OrespawnArmorMaterial("ultimate", 200, new int[]{6, 12, 10, 6}, 100, 3.0f, new ItemEnchantments().addEnchantment(Enchantments.field_185298_f, 2).addEnchantment(Enchantments.field_185299_g, 2).addEnchantment(Enchantments.field_180310_c, 5).addEnchantment(Enchantments.field_77329_d, 5).addEnchantment(Enchantments.field_180308_g, 5).addEnchantment(Enchantments.field_185307_s, 5).addEnchantment(Enchantments.field_180309_e, 3));
    public static final OrespawnArmorMaterial EmeraldArmor = new OrespawnArmorMaterial("emerald", 100, new int[]{3, 8, 6, 3}, 12, 3.0f, new ItemEnchantments());
    public static final OrespawnArmorMaterial MothArmor = new OrespawnArmorMaterial("moth", 100, new int[]{2, 7, 5, 2}, 12, 3.0f, new ItemEnchantments());
    public static final OrespawnArmorMaterial AmethystArmor = new OrespawnArmorMaterial("amethyst", 100, new int[]{4, 8, 7, 3}, 12, 3.0f, new ItemEnchantments());

    public OrespawnArmorMaterial(String name, int durability, int[] protections, int enchantability, float toughness, ItemEnchantments enchantments) {
        this.Name = name;
        this.Durability = durability;
        this.Protections = protections;
        this.Enchantability = enchantability;
        this.Toughness = toughness;
        this.EnchantmentLevels = enchantments;
        this.Material = EnumHelper.addArmorMaterial((String)this.Name, (String)("orespawn:" + this.Name), (int)this.Durability, (int[])this.Protections, (int)this.Enchantability, (SoundEvent)SoundEvents.field_187719_p, (float)this.Toughness);
    }
}

