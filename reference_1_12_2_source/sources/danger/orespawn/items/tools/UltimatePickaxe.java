/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.ParametersAreNonnullByDefault
 *  net.minecraft.entity.Entity
 *  net.minecraft.init.Enchantments
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemPickaxe
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn.items.tools;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import danger.orespawn.items.tools.OrespawnToolMaterial;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class UltimatePickaxe
extends ItemPickaxe {
    public UltimatePickaxe() {
        super(OrespawnToolMaterial.UltimateTools.Material);
        this.func_77655_b("ultimate_pickaxe");
        this.setRegistryName("ultimate_pickaxe");
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
    }

    @ParametersAreNonnullByDefault
    public void func_77663_a(ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) {
        if (!stack.func_77948_v()) {
            stack.func_77966_a(Enchantments.field_185305_q, 5);
            stack.func_77966_a(Enchantments.field_185308_t, 5);
        }
        super.func_77663_a(stack, world, player, itemSlot, isSelected);
    }
}

