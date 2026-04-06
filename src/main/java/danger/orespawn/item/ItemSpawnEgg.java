package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class ItemSpawnEgg extends Item {
    private final Supplier<EntityType<?>> entityTypeSupplier;

    public ItemSpawnEgg(Item.Properties properties, Supplier<EntityType<?>> entityTypeSupplier) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().above();
        Entity entity = this.entityTypeSupplier.get().create(level);
        if (entity != null) {
            entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(entity);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
