package danger.orespawn.item;

import danger.orespawn.ModItems;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.SpiderRobot;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemWrench extends Item {
    public ItemWrench(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.level().isClientSide) {
            ItemStack dropStack = null;
            if (entity instanceof SpiderRobot) {
                dropStack = new ItemStack(ModItems.SPIDER_ROBOT_KIT.get());
            } else if (entity instanceof AntRobot) {
                dropStack = new ItemStack(ModItems.ANT_ROBOT_KIT.get());
            }

            if (dropStack != null) {
                entity.discard();
                entity.level().addFreshEntity(new ItemEntity(
                        entity.level(), entity.getX(), entity.getY(), entity.getZ(), dropStack));
            }

            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        entity.getX(), entity.getY(), entity.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
            entity.level().playSound(null, entity.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
        }
        return false;
    }
}
