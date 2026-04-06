package danger.orespawn.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class UltimateBow extends BowItem {
    public UltimateBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) <= 0) {
            stack.enchant(Enchantments.POWER_ARROWS, 5);
            stack.enchant(Enchantments.FLAMING_ARROWS, 3);
            stack.enchant(Enchantments.PUNCH_ARROWS, 2);
            stack.enchant(Enchantments.INFINITY_ARROWS, 1);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        if (!level.isClientSide) {
            Arrow arrow = new Arrow(level, player, stack);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
            arrow.setCritArrow(true);
            arrow.pickup = Arrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }
}
