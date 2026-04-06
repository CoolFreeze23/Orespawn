package danger.orespawn.item;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemElevator extends Item {
    public ItemElevator(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().above();
        Entity elevator = ModEntities.ELEVATOR.get().create(level);
        if (elevator != null) {
            elevator.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0.0F);
            level.addFreshEntity(elevator);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
