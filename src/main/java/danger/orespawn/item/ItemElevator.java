package danger.orespawn.item;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Hoverboard placement item (orig ItemElevator.java — display name
 * "Hoverboard", orig OreSpawnMain.java:5174). Right-clicking a block spawns
 * the board 1.2 above it at a random yaw and consumes the item unless the
 * player is in creative (orig ItemElevator.java:25-36).
 */
public class ItemElevator extends Item {
    public ItemElevator(Item.Properties properties) {
        // orig ItemElevator.java:21 — maxStackSize = 1.
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // orig ItemElevator.java:29-31 — spawn at block center, +1.2 up, random yaw.
        BlockPos pos = context.getClickedPos();
        Entity elevator = ModEntities.ELEVATOR.get().create(level);
        if (elevator != null) {
            elevator.moveTo(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    level.getRandom().nextFloat() * 360.0f, 0.0f);
            level.addFreshEntity(elevator);
        }
        // orig ItemElevator.java:32-34 — creative players keep the item.
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
