package danger.orespawn.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class UltimateBow extends BowItem {
    public UltimateBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.POWER, 5);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FLAME, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.PUNCH, 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.INFINITY, 1);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        if (!level.isClientSide) {
            Arrow arrow = new Arrow(level, player, stack, null);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
            arrow.setCritArrow(true);
            arrow.pickup = Arrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
