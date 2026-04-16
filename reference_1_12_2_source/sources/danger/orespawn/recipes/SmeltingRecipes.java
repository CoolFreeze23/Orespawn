/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraftforge.fml.common.registry.GameRegistry
 */
package danger.orespawn.recipes;

import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SmeltingRecipes {
    public static void init() {
        GameRegistry.addSmelting((ItemStack)new ItemStack(ModBlocks.URANIUM_ORE), (ItemStack)new ItemStack(ModItems.URANIUM_NUGGET), (float)0.4f);
        GameRegistry.addSmelting((ItemStack)new ItemStack(ModBlocks.TITANIUM_ORE), (ItemStack)new ItemStack(ModItems.TITANIUM_NUGGET), (float)0.4f);
    }
}

