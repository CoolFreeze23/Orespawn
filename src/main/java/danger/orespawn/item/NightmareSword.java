package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;

public class NightmareSword extends SwordItem {
    public NightmareSword(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // TODO: Enchantments are data-driven in 1.21.1, need registry lookup
        // if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.KNOCKBACK) <= 0) {
        //     stack.enchant(Enchantments.SHARPNESS, 1);
        //     stack.enchant(Enchantments.KNOCKBACK, 3);
        //     stack.enchant(Enchantments.SWEEPING_EDGE, 1);
        // }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
