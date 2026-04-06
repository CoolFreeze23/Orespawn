package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StepAccross extends Item {
    private static final int BRIDGE_LENGTH = 33;
    private static final int TORCH_INTERVAL = 4;

    public StepAccross(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        Direction facing = player.getDirection();

        int forwardX = facing.getStepX();
        int forwardZ = facing.getStepZ();

        for (int stepIndex = 0; stepIndex < BRIDGE_LENGTH; stepIndex++) {
            BlockPos step = pos.offset(forwardX * stepIndex, 0, forwardZ * stepIndex);
            if (level.getBlockState(step).isAir() || !level.getBlockState(step).isSolidRender(level, step)) {
                level.setBlock(step, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
            if (stepIndex % TORCH_INTERVAL == 0) {
                BlockPos torchPos = step.above();
                if (level.getBlockState(torchPos).isAir()) {
                    level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
