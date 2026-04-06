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

public class ItemMinersDream extends Item {
    public ItemMinersDream(Item.Properties properties) {
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

        for (int i = 0; i < 64; i++) {
            for (int w = -2; w <= 2; w++) {
                for (int h = 0; h < 5; h++) {
                    BlockPos target;
                    if (dx != 0) {
                        target = new BlockPos(pos.getX() + dx * i, pos.getY() + h, pos.getZ() + w);
                    } else {
                        target = new BlockPos(pos.getX() + w, pos.getY() + h, pos.getZ() + dz * i);
                    }
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            if (i % 4 == 0) {
                BlockPos torchPos;
                if (dx != 0) {
                    torchPos = new BlockPos(pos.getX() + dx * i, pos.getY() + 1, pos.getZ() + 2);
                } else {
                    torchPos = new BlockPos(pos.getX() + 2, pos.getY() + 1, pos.getZ() + dz * i);
                }
                level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
