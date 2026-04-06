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

public class StepUp extends Item {
    private static final int STAIRCASE_LENGTH = 33;
    private static final int TORCH_INTERVAL = 3;

    public StepUp(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().above();
        Direction facing = player.getDirection();

        int forwardX = facing.getStepX();
        int forwardZ = facing.getStepZ();

        for (int stepIndex = 0; stepIndex < STAIRCASE_LENGTH; stepIndex++) {
            BlockPos step = pos.offset(forwardX * stepIndex, stepIndex, forwardZ * stepIndex);
            if (level.getBlockState(step).isAir()) {
                level.setBlock(step, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
            BlockPos aboveStep = step.above();
            if (level.getBlockState(aboveStep).isAir()) {
                level.setBlock(aboveStep, Blocks.AIR.defaultBlockState(), 3);
            }
            BlockPos aboveStep2 = step.above(2);
            if (level.getBlockState(aboveStep2).isAir()) {
                level.setBlock(aboveStep2, Blocks.AIR.defaultBlockState(), 3);
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
