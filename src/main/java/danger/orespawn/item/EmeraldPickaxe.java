package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class EmeraldPickaxe extends PickaxeItem {
    public EmeraldPickaxe(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // orig EmeraldPickaxe.java:30-35 — keyed on Silk Touch: while it reads 0, Silk Touch 1 (ITEM-076)
        if (!level.isClientSide && OreSpawnEnchantHelper.level(stack, level, Enchantments.SILK_TOUCH) <= 0) {
            OreSpawnEnchantHelper.addIfAbsent(stack, level, Enchantments.SILK_TOUCH, 1);
        }
    }
}
