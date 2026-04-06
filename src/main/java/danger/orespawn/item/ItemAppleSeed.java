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
import net.minecraft.world.level.block.state.BlockState;

public class ItemAppleSeed extends Item {
    public ItemAppleSeed(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean validBlock = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
        if (!validBlock) return InteractionResult.FAIL;

        makeTree(level, pos.getX(), pos.getY(), pos.getZ());
        level.playSound(null, player.blockPosition(), SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    private void makeTree(Level level, int cx, int cy, int cz) {
        int trunkHeight = 5 + level.random.nextInt(3);
        for (int y = 1; y <= trunkHeight; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz), Blocks.OAK_LOG.defaultBlockState(), 3);
        }
        int topY = cy + trunkHeight;
        int leafRadius = 3;
        for (int x = -leafRadius; x <= leafRadius; x++) {
            for (int z = -leafRadius; z <= leafRadius; z++) {
                for (int y = 0; y <= leafRadius; y++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist <= leafRadius) {
                        BlockPos lp = new BlockPos(cx + x, topY + y, cz + z);
                        if (level.getBlockState(lp).isAir()) {
                            level.setBlock(lp, Blocks.OAK_LEAVES.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
