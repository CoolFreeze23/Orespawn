package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class UltimateShovel extends ShovelItem {
    public UltimateShovel(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.EFFICIENCY, 5);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity != null) {
            if (entity instanceof Player) {
                return true;
            }
            if (entity instanceof TamableAnimal t && t.isTame()) {
                return true;
            }
        }
        return false;
    }
}
