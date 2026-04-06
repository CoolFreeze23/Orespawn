package danger.orespawn.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

public class ItemCreeperLauncher extends Item {
    public ItemCreeperLauncher(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof Creeper creeper && !entity.level().isClientSide) {
            creeper.push(0.0, 2.0, 0.0);
            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        creeper.getX(), creeper.getY(), creeper.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            entity.level().playSound(null, creeper.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.shrink(1);
            return true;
        }
        return false;
    }
}
