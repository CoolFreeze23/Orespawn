package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class InstantGarden extends Item {
    public InstantGarden(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        int startX = pos.getX();
        int startZ = pos.getZ();
        int y = pos.getY();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos ground = new BlockPos(startX + x, y, startZ + z);
                BlockPos above = ground.above();
                level.setBlock(ground, Blocks.FARMLAND.defaultBlockState(), 3);
                if (level.getBlockState(above).isAir()) {
                    if ((x + z) % 3 == 0) {
                        level.setBlock(above, Blocks.WHEAT.defaultBlockState(), 3);
                    } else if ((x + z) % 3 == 1) {
                        level.setBlock(above, Blocks.CARROTS.defaultBlockState(), 3);
                    } else {
                        level.setBlock(above, Blocks.POTATOES.defaultBlockState(), 3);
                    }
                }
            }
        }

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == -radius || x == radius || z == -radius || z == radius) {
                    BlockPos fencePos = new BlockPos(startX + x, y + 1, startZ + z);
                    level.setBlock(fencePos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
