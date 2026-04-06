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

public class StepDown extends Item {
    public StepDown(Item.Properties properties) {
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

        int dx = facing.getStepX();
        int dz = facing.getStepZ();

        for (int i = 0; i < 33; i++) {
            BlockPos step = pos.offset(dx * i, -i, dz * i);
            if (!level.getBlockState(step).isAir()) {
                level.setBlock(step, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
            BlockPos above = step.above();
            level.setBlock(above, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(above.above(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(above.above(2), Blocks.AIR.defaultBlockState(), 3);
            if (i % 3 == 0) {
                BlockPos wallPos = step.above().relative(facing.getClockWise());
                if (!level.getBlockState(wallPos).isAir()) {
                    level.setBlock(step.above(), Blocks.TORCH.defaultBlockState(), 3);
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
