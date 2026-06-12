package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.ModToolTiers;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class NightmareSword extends SwordItem {
    public NightmareSword(Item.Properties properties) {
        super(ModToolTiers.NIGHTMARE, properties);
    }

    /**
     * orig NightmareSword.java:26 — {@code setMaxDamage(1200)} overrides the
     * Nightmare tool material's 1800.
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 1200;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            // orig NightmareSword.java:31-33 — Sharpness 1, Knockback 3, Fire Aspect 1
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 1);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.KNOCKBACK, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FIRE_ASPECT, 1);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
