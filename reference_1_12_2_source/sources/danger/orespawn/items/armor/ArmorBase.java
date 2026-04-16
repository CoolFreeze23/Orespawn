/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.entity.Entity
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemArmor
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn.items.armor;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import danger.orespawn.items.armor.OrespawnArmorMaterial;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ArmorBase
extends ItemArmor {
    OrespawnArmorMaterial field_77878_bZ;
    boolean enchanted;

    public ArmorBase(String name, OrespawnArmorMaterial material, EntityEquipmentSlot equipmentSlot) {
        super(material.Material, 1, equipmentSlot);
        this.func_77655_b(name);
        this.setRegistryName(name);
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
        this.field_77878_bZ = material;
        this.enchanted = false;
    }

    public void func_77663_a(ItemStack stack, World p_77663_2_, Entity p_77663_3_, int p_77663_4_, boolean p_77663_5_) {
        if (!this.enchanted) {
            this.enchant(stack);
            this.enchanted = true;
        }
        super.func_77663_a(stack, p_77663_2_, p_77663_3_, p_77663_4_, p_77663_5_);
    }

    private void enchant(ItemStack stack) {
        Map enchantments = this.field_77878_bZ.EnchantmentLevels.getMap();
        Iterator iterator = enchantments.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry o;
            Map.Entry e = o = iterator.next();
            stack.func_77966_a((Enchantment)e.getKey(), ((Integer)e.getValue()).intValue());
        }
    }
}

