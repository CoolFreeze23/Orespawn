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
    public SkateBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int charge = this.getUseDuration(stack, entityLiving) - timeLeft;
        float f = (float) charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f < 0.1F) return;
        if (f > 1.75F) f = 1.75F;

        if (!level.isClientSide) {
            Arrow arrow = new Arrow(level, player, stack, null);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 2.0F, 1.0F);
            if (level.random.nextInt(20) == 1) {
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
