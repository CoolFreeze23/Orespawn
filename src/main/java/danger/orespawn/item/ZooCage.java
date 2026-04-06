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

public class ZooCage extends Item {
    private final int cageSize;

    public ZooCage(Item.Properties properties, int cageSize) {
        super(properties);
        this.cageSize = cageSize;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();
        int halfExtent = cageSize;
        int ceilingHeight = cageSize;

        for (int x = -halfExtent; x <= halfExtent; x++) {
            for (int z = -halfExtent; z <= halfExtent; z++) {
                for (int y = 0; y <= ceilingHeight; y++) {
                    boolean isEdge = x == -halfExtent || x == halfExtent || z == -halfExtent || z == halfExtent;
                    boolean isFloor = y == 0;
                    boolean isCeiling = y == ceilingHeight;

                    if (isFloor || isCeiling) {
                        level.setBlock(new BlockPos(originX + x, originY + y, originZ + z), Blocks.GLASS.defaultBlockState(), 3);
                    } else if (isEdge) {
                        level.setBlock(new BlockPos(originX + x, originY + y, originZ + z), Blocks.IRON_BARS.defaultBlockState(), 3);
                    }
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    public int getCageSize() {
        return cageSize;
    }
}
