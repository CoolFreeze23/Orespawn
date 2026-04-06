package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.entity.AttackSquid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        if (!level.isClientSide) {
            AttackSquid squid = ModEntities.ATTACK_SQUID.get().create(level);
            if (squid != null) {
                squid.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
                squid.setTarget(null);
                level.addFreshEntity(squid);
            }
            player.push(
                    -Math.sin(Math.toRadians(player.getYRot())) * 0.5,
                    0.0,
                    Math.cos(Math.toRadians(player.getYRot())) * 0.5
            );
            level.playSound(null, player.blockPosition(), SoundEvents.SQUID_SQUIRT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
