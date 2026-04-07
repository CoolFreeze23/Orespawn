package danger.orespawn.item;

import danger.orespawn.entity.ThunderBolt;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemThunderStaff extends Item {
    private int repairTicker = 50;

    public ItemThunderStaff(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            ThunderBolt projectile = new ThunderBolt(level, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(3.0));
            projectile.hasImpulse = true;
            level.addFreshEntity(projectile);
            player.push(
                    Math.cos(Math.toRadians(player.getYRot() - 90.0)) * 0.5,
                    0.15,
                    Math.sin(Math.toRadians(player.getYRot() - 90.0)) * 0.5
            );
            player.hurtMarked = true;
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(hand);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && level.isRaining() && level.isThundering()) {
            if (--repairTicker <= 0 && stack.isDamaged()) {
                stack.setDamageValue(stack.getDamageValue() - 1);
                repairTicker = 50;
            }
        }
    }
}
