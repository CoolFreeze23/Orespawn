package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.entity.HoverboardEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Phase 10 — Hoverboard item. Right-clicking a block places a
 * {@link HoverboardEntity} on top of it; the item is consumed unless the
 * player is in creative. Mirrors the 1.7.10 ItemElevator placement flow.
 */
public class HoverboardItem extends Item {

    public HoverboardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos pos = ctx.getClickedPos().above();

        if (!level.isClientSide()) {
            HoverboardEntity board = ModEntities.HOVERBOARD.get().create(level);
            if (board == null) return InteractionResult.FAIL;
            board.moveTo(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    player.getYRot(), 0.0f);
            board.setColor(1 + level.getRandom().nextInt(10));
            level.addFreshEntity(board);

            if (!player.getAbilities().instabuild) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Allow placing in midair too — useful for cliff-edge spawns; targets the
        // player's feet block as the surface.
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
