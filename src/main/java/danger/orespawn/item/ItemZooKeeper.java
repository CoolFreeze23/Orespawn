package danger.orespawn.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

public class ItemZooKeeper extends Item {
    public ItemZooKeeper(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.level().isClientSide && entity instanceof LivingEntity living) {
            living.setNoAi(true);
            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        entity.getX(), entity.getY() + 1.0, entity.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            entity.level().playSound(null, entity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.hurtAndBreak(2, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            return true;
        }
        return false;
    }
}
