package danger.orespawn.item;

import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class ItemMinersDream extends Item {
    private static final int TUNNEL_LENGTH_BLOCKS = 64;
    private static final int CROSS_AXIS_HALF_WIDTH = 2;
    private static final int VERTICAL_CLEAR_HEIGHT = 5;
    private static final int TORCH_PLACE_INTERVAL = 4;
    private static final int TORCH_SIDE_OFFSET = 2;
    private static final int TORCH_Y_OFFSET = 1;

    public ItemMinersDream(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Config: minersDreamExpensive requires consuming a diamond from inventory to activate
        if (OreSpawnConfig.MINERS_DREAM_EXPENSIVE.get()) {
            int diamondSlot = -1;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (invStack.is(Items.DIAMOND) && !invStack.isEmpty()) {
                    diamondSlot = i;
                    break;
                }
            }
            if (diamondSlot == -1) return InteractionResult.PASS;
            player.getInventory().getItem(diamondSlot).shrink(1);
        }

        BlockPos pos = context.getClickedPos();
        Direction facing = player.getDirection();

        int forwardX = facing.getStepX();
        int forwardZ = facing.getStepZ();

        for (int forwardStep = 0; forwardStep < TUNNEL_LENGTH_BLOCKS; forwardStep++) {
            for (int crossOffset = -CROSS_AXIS_HALF_WIDTH; crossOffset <= CROSS_AXIS_HALF_WIDTH; crossOffset++) {
                for (int heightOffset = 0; heightOffset < VERTICAL_CLEAR_HEIGHT; heightOffset++) {
                    BlockPos target;
                    if (forwardX != 0) {
                        target = new BlockPos(pos.getX() + forwardX * forwardStep, pos.getY() + heightOffset, pos.getZ() + crossOffset);
                    } else {
                        target = new BlockPos(pos.getX() + crossOffset, pos.getY() + heightOffset, pos.getZ() + forwardZ * forwardStep);
                    }
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            if (forwardStep % TORCH_PLACE_INTERVAL == 0) {
                BlockPos torchPos;
                if (forwardX != 0) {
                    torchPos = new BlockPos(pos.getX() + forwardX * forwardStep, pos.getY() + TORCH_Y_OFFSET, pos.getZ() + TORCH_SIDE_OFFSET);
                } else {
                    torchPos = new BlockPos(pos.getX() + TORCH_SIDE_OFFSET, pos.getY() + TORCH_Y_OFFSET, pos.getZ() + forwardZ * forwardStep);
                }
                level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
