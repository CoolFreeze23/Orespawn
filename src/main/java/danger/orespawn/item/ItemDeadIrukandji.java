package danger.orespawn.item;

import danger.orespawn.entity.DeadIrukandji;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Dead Irukandji, ported from 1.7.10 ItemIrukandji.java:25-34. Right-click
 * throws a DeadIrukandji projectile (100 damage on entity hit via the
 * LaserBall irukandji branch) with a loud bow sound.
 */
public class ItemDeadIrukandji extends Item {
    public ItemDeadIrukandji(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // orig ItemIrukandji.java:26-28 — consume one unless creative
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        // orig ItemIrukandji.java:29 — random.bow at 3.0 / 1.0
        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 3.0F, 1.0F);
        if (!level.isClientSide) {
            // orig ItemIrukandji.java:31 — EntityThrowable default heading (1.5 speed)
            DeadIrukandji projectile = new DeadIrukandji(level, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
