package danger.orespawn.item;

import danger.orespawn.entity.LaserBall;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemRayGun extends Item {

    /**
     * Cooldown ticks between shots. 1.7.10 had no rate limit at all, which trivially
     * crashes a modern dedicated server when a player holds right-click — every
     * tick spawns a {@link LaserBall} entity with explosion physics. 10t (~0.5s)
     * still feels rapid-fire but caps spawn pressure to ~2 projectiles/sec.
     */
    private static final int COOLDOWN_TICKS = 10;

    public ItemRayGun(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.5F, 0.5F);
        if (!level.isClientSide) {
            LaserBall projectile = new LaserBall(level, player);
            projectile.setSpecial();
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(3.0));
            projectile.hasImpulse = true;
            level.addFreshEntity(projectile);
            player.push(
                    Math.cos(Math.toRadians(player.getYRot() - 90.0)) * 1.5,
                    0.3,
                    Math.sin(Math.toRadians(player.getYRot() - 90.0)) * 1.5
            );
            player.hurtMarked = true;
        }
        player.swing(hand);
        // Server-authoritative cooldown so the throttle survives latency/desync.
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
