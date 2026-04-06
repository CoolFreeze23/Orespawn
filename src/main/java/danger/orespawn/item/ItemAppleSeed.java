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
    private static final int TRUNK_HEIGHT_MIN = 5;
    private static final int TRUNK_HEIGHT_EXTRA_RANGE = 3;
    private static final int LEAF_RADIUS = 3;

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

    private void makeTree(Level level, int baseX, int baseY, int baseZ) {
        int trunkHeight = TRUNK_HEIGHT_MIN + level.random.nextInt(TRUNK_HEIGHT_EXTRA_RANGE);
        for (int trunkY = 1; trunkY <= trunkHeight; trunkY++) {
            level.setBlock(new BlockPos(baseX, baseY + trunkY, baseZ), Blocks.OAK_LOG.defaultBlockState(), 3);
        }
        int topY = baseY + trunkHeight;
        for (int x = -LEAF_RADIUS; x <= LEAF_RADIUS; x++) {
            for (int z = -LEAF_RADIUS; z <= LEAF_RADIUS; z++) {
                for (int leafOffsetY = 0; leafOffsetY <= LEAF_RADIUS; leafOffsetY++) {
                    double dist = Math.sqrt(x * x + leafOffsetY * leafOffsetY + z * z);
                    if (dist <= LEAF_RADIUS) {
                        BlockPos leafPos = new BlockPos(baseX + x, topY + leafOffsetY, baseZ + z);
                        if (level.getBlockState(leafPos).isAir()) {
                            level.setBlock(leafPos, Blocks.OAK_LEAVES.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
