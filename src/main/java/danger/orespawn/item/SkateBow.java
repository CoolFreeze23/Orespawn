package danger.orespawn.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SkateBow extends BowItem {
    /** Vanilla-style divisor: charge ticks mapped to normalized pull strength. */
    private static final float TICKS_FOR_FULL_DRAW = 20.0F;
    private static final float MIN_PULL_STRENGTH = 0.1F;
    private static final float MAX_PULL_STRENGTH = 1.75F;
    private static final float ARROW_SPEED_MULTIPLIER = 2.0F;
    private static final int CRIT_CHANCE_DENOMINATOR = 20;

    public SkateBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int charge = this.getUseDuration(stack, entityLiving) - timeLeft;
        float pullStrength = (float) charge / TICKS_FOR_FULL_DRAW;
        pullStrength = (pullStrength * pullStrength + pullStrength * 2.0F) / 3.0F;
        if (pullStrength < MIN_PULL_STRENGTH) return;
        if (pullStrength > MAX_PULL_STRENGTH) pullStrength = MAX_PULL_STRENGTH;

        if (!level.isClientSide) {
            Arrow arrow = new Arrow(level, player, stack, null);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, pullStrength * ARROW_SPEED_MULTIPLIER, 1.0F);
            if (level.random.nextInt(CRIT_CHANCE_DENOMINATOR) == 1) {
                arrow.setCritArrow(true);
            }
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + 0.5F);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
