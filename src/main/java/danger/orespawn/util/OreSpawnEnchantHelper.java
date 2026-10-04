package danger.orespawn.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class OreSpawnEnchantHelper {

    public static void applyEnchantment(ItemStack stack, Level level, ResourceKey<Enchantment> key, int enchLevel) {
        level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(key))
                .ifPresent(holder -> stack.enchant(holder, enchLevel));
    }

    /**
     * The stack's level of an enchantment, 0 without it: 1.7.10's EnchantmentHelper.getEnchantmentLevel, the check
     * each self-enchanting item made on its own key enchantment before adding its set.
     */
    public static int level(ItemStack stack, Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(key))
                .map(holder -> stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(holder))
                .orElse(0);
    }

    /**
     * Adds an enchantment the stack does not carry yet; one it carries keeps its level. 1.7.10 appended a second entry
     * and read the first, so a level a boss drop's dice put on stands over the item's own set.
     */
    public static void addIfAbsent(ItemStack stack, Level level, ResourceKey<Enchantment> key, int enchLevel) {
        if (level(stack, level, key) <= 0) {
            applyEnchantment(stack, level, key, enchLevel);
        }
    }
}
