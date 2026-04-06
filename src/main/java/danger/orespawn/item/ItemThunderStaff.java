package danger.orespawn.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemThunderStaff extends Item {
    private int ticker = 0;

    public ItemThunderStaff(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // TODO: Spawn ThunderBolt projectile entity when entity type is registered
            player.push(
                    -Math.sin(Math.toRadians(player.getYRot())) * 0.2,
                    0.0,
                    Math.cos(Math.toRadians(player.getYRot())) * 0.2
            );
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && level.isRaining()) {
            ticker++;
            if (ticker > 200 && stack.isDamaged()) {
                stack.setDamageValue(stack.getDamageValue() - 1);
                ticker = 0;
            }
        }
    }
}
