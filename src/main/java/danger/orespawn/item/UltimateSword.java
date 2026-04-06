package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;

public class UltimateSword extends SwordItem {
    public UltimateSword(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.KNOCKBACK) <= 0) {
            stack.enchant(Enchantments.SHARPNESS, 5);
            stack.enchant(Enchantments.SMITE, 5);
            stack.enchant(Enchantments.BANE_OF_ARTHROPODS, 5);
            stack.enchant(Enchantments.KNOCKBACK, 3);
            stack.enchant(Enchantments.FIRE_ASPECT, 3);
            stack.enchant(Enchantments.LOOTING, 3);
            stack.enchant(Enchantments.SWEEPING_EDGE, 2);
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

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        return true;
    }
}
