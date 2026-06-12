package danger.orespawn.item;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * StepAccross, ported from 1.7.10 StepAccross.java:26-99. Identical to
 * StepUp but the cobble path runs level (bridge); Extreme Torch every 8
 * steps, stops at the first non-air block.
 */
public class StepAccross extends Item {

    public StepAccross(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // orig StepAccross.java:31-33 — start one above the clicked block
        int x = context.getClickedPos().getX();
        int y = context.getClickedPos().getY() + 1;
        int z = context.getClickedPos().getZ();

        // orig StepAccross.java:34-81 — same 8-way head-yaw octant as StepUp
        int[] delta = StepUp.headingDeltas(player);
        if (delta == null) return InteractionResult.FAIL;

        if (level.isClientSide) return InteractionResult.SUCCESS;
        // orig StepAccross.java:82-88 — explosion fx, particles at path level
        StepUp.playUseEffects(level, player, x, y, z, 0.0f);

        // orig StepAccross.java:91-95 — flat path one below start until
        // obstructed; Extreme Torch on steps 1, 9, 17, 25 when air above
        for (int k = 1; k < StepUp.LENGTH; ++k) {
            BlockPos step = new BlockPos(x + k * delta[0], y - 1, z + k * delta[1]);
            if (!level.getBlockState(step).isAir()) break;
            level.setBlock(step, Blocks.COBBLESTONE.defaultBlockState(), 2);
            if ((k - 1) % 8 == 0 && level.getBlockState(step.above()).isAir()) {
                level.setBlock(step.above(), ModBlocks.EXTREME_TORCH.get().defaultBlockState(), 2);
            }
        }

        // orig StepAccross.java:96-98 — consume one unless creative
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
