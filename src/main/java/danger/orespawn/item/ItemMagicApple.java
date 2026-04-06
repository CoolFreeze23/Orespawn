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

public class ItemMagicApple extends Item {
    private static final int TRUNK_HEIGHT_MIN = 12;
    private static final int TRUNK_HEIGHT_EXTRA_RANGE = 8;
    private static final int LEAF_RADIUS_BASE = 5;
    private static final int LEAF_RADIUS_EXTRA_RANGE = 3;

    public ItemMagicApple(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        boolean isGrassOrDirt = level.getBlockState(pos).is(Blocks.GRASS_BLOCK) || level.getBlockState(pos).is(Blocks.DIRT);
        if (!isGrassOrDirt) return InteractionResult.FAIL;

        makeTree(level, pos.getX(), pos.getY(), pos.getZ(), level.random.nextBoolean());
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    private void makeTree(Level level, int baseX, int baseY, int baseZ, boolean roundTree) {
        int trunkHeight = TRUNK_HEIGHT_MIN + level.random.nextInt(TRUNK_HEIGHT_EXTRA_RANGE);
        int leafRadius = LEAF_RADIUS_BASE + level.random.nextInt(LEAF_RADIUS_EXTRA_RANGE);

        for (int trunkY = 1; trunkY <= trunkHeight; trunkY++) {
            level.setBlock(new BlockPos(baseX, baseY + trunkY, baseZ), Blocks.OAK_LOG.defaultBlockState(), 3);
        }

        int topY = baseY + trunkHeight;
        int leafVerticalHalfSpan = leafRadius / 2;
        for (int x = -leafRadius; x <= leafRadius; x++) {
            for (int z = -leafRadius; z <= leafRadius; z++) {
                for (int leafOffsetY = -leafVerticalHalfSpan; leafOffsetY <= leafVerticalHalfSpan; leafOffsetY++) {
                    double dist;
                    if (roundTree) {
                        dist = Math.sqrt(x * x + leafOffsetY * leafOffsetY + z * z);
                    } else {
                        dist = Math.max(Math.abs(x), Math.max(Math.abs(leafOffsetY), Math.abs(z)));
                    }
                    if (dist <= leafRadius) {
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
