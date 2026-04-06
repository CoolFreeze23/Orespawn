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

    private void makeTree(Level level, int cx, int cy, int cz, boolean roundTree) {
        int trunkHeight = 12 + level.random.nextInt(8);
        int leafRadius = 5 + level.random.nextInt(3);

        for (int y = 1; y <= trunkHeight; y++) {
            level.setBlock(new BlockPos(cx, cy + y, cz), Blocks.OAK_LOG.defaultBlockState(), 3);
        }

        int topY = cy + trunkHeight;
        for (int x = -leafRadius; x <= leafRadius; x++) {
            for (int z = -leafRadius; z <= leafRadius; z++) {
                for (int y = -leafRadius / 2; y <= leafRadius / 2; y++) {
                    double dist;
                    if (roundTree) {
                        dist = Math.sqrt(x * x + y * y + z * z);
                    } else {
                        dist = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    }
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
