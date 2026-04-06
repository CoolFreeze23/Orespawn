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
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FORTUNE, 1);
        }
    }
}
