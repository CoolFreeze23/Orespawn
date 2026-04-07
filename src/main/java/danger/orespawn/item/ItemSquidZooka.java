package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.entity.AttackSquid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemSquidZooka extends Item {
    public ItemSquidZooka(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5F, 0.5F);
        if (!level.isClientSide) {
            double xzOff = 2.5;
            double yOff = 1.65;
            float yawRad = (float) Math.toRadians(player.getYRot() + 15.0f);

            double spawnX = player.getX() - xzOff * Mth.sin(yawRad);
            double spawnY = player.getY() + yOff;
            double spawnZ = player.getZ() + xzOff * Mth.cos(yawRad);

            AttackSquid squid = ModEntities.ATTACK_SQUID.get().create(level);
            if (squid != null) {
                float speed = 3.6F;
                float yawPlayer = (float) Math.toRadians(player.getYRot());
                float pitchPlayer = (float) Math.toRadians(player.getXRot());

                squid.moveTo(spawnX, spawnY, spawnZ, level.random.nextFloat() * 360.0F, 0.0F);
                squid.setWasShot();
                squid.setNoGravity(true);
                squid.setDeltaMovement(
                        -Mth.sin(yawPlayer) * Mth.cos(pitchPlayer) * speed + (level.random.nextFloat() - level.random.nextFloat()) * 0.05,
                        -Mth.sin(pitchPlayer) * speed + (level.random.nextFloat() - level.random.nextFloat()) * 0.05,
                        Mth.cos(yawPlayer) * Mth.cos(pitchPlayer) * speed + (level.random.nextFloat() - level.random.nextFloat()) * 0.05
                );
                squid.hasImpulse = true;
                level.addFreshEntity(squid);
            }

            player.push(
                    Math.cos(Math.toRadians(player.getYRot() - 90.0)) * 0.45,
                    0.1,
                    Math.sin(Math.toRadians(player.getYRot() - 90.0)) * 0.45
            );
            player.hurtMarked = true;
        }
        player.swing(hand);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
