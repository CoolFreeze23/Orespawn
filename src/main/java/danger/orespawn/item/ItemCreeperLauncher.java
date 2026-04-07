package danger.orespawn.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

public class ItemCreeperLauncher extends Item {
    public ItemCreeperLauncher(Item.Properties properties) {
        super(properties);
    }

    private boolean launchCreeper(Creeper creeper, Player player, ItemStack stack) {
        if (creeper.level().isClientSide) return true;
        if (creeper.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 6; i++) {
                float f1 = serverLevel.random.nextFloat() - serverLevel.random.nextFloat();
                float f2 = 0.25f + serverLevel.random.nextFloat() * 6.0f;
                float f3 = serverLevel.random.nextFloat() - serverLevel.random.nextFloat();
                serverLevel.sendParticles(ParticleTypes.SMOKE, creeper.getX() + f1, creeper.getY() + f2, creeper.getZ() + f3, 1, 0, f2 / 4.0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, creeper.getX() + f1, creeper.getY() + f2, creeper.getZ() + f3, 1, 0, f2 / 4.0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.DUST_PLUME, creeper.getX() + f1, creeper.getY() + f2, creeper.getZ() + f3, 1, 0, f2 / 4.0, 0, 0);
            }
        }
        creeper.level().playSound(null, creeper.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.0F, 1.2F);
        creeper.push(0.0, 4.5, 0.0);
        creeper.hurtMarked = true;
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof Creeper creeper) {
            return launchCreeper(creeper, player, stack);
        }
        return false;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof Creeper creeper) {
            launchCreeper(creeper, player, stack);
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
