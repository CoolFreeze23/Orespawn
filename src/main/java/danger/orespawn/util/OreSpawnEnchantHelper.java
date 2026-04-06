package danger.orespawn.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class OreSpawnEnchantHelper {

    public static boolean hasAnyEnchantments(ItemStack stack) {
        return !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public static void applyEnchantment(ItemStack stack, Level level, ResourceKey<Enchantment> key, int enchLevel) {
        level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(key))
                .ifPresent(holder -> stack.enchant(holder, enchLevel));
    }
}
