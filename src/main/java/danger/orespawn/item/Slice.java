package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class Slice extends SwordItem {
    public Slice(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 5);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.BANE_OF_ARTHROPODS, 1);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
