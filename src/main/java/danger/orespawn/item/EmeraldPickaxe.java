package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;

public class EmeraldPickaxe extends PickaxeItem {
    public EmeraldPickaxe(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // TODO: Enchantments are data-driven in 1.21.1, need registry lookup
        // if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.FORTUNE) <= 0) {
        //     stack.enchant(Enchantments.FORTUNE, 1);
        // }
    }
}
