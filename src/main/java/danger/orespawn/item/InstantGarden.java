package danger.orespawn.item;

import danger.orespawn.ModBlocks;
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

/**
 * Instant Garden, ported from 1.7.10 InstantGarden.java:26-147. Lays an
 * 18-long by 15-wide plot extending away from the player toward the clicked
 * block (cardinal directions only), clears 10 blocks of headroom, floors it
 * with grass, and plants 11 crop rows separated by 3 cobble-lined water
 * channels: radish, lettuce, carrots, water, potatoes, wheat, tomato, water,
 * corn, strawberry, reeds-on-sand, water, melon stems.
 */
public class InstantGarden extends Item {
    // orig InstantGarden.java:32-34 — height 10, half-width 7, length 18
    private static final int HEIGHT = 10;
    private static final int HALF_WIDTH = 7;
    private static final int LENGTH = 18;

    public InstantGarden(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos clicked = context.getClickedPos();
        int x = clicked.getX();
        int y = player.blockPosition().getY(); // orig :42,49 — plot floor at player feet level
        int z = clicked.getZ();

        // orig InstantGarden.java:44-68 — the garden runs away from the player
        // along whichever axis the clicked block is offset; diagonal or
        // same-column clicks do nothing
        int deltaX = Integer.signum(x - player.blockPosition().getX());
        int deltaZ = Integer.signum(z - player.blockPosition().getZ());
        if ((deltaX == 0) == (deltaZ == 0)) {
            return InteractionResult.FAIL;
        }

        // orig InstantGarden.java:69 — explosion sound 1.0 / 1.5
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 1.0f, 1.5f);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // orig InstantGarden.java:73-81 — clear 18x15x10 and lay the grass floor
        for (int i = 0; i < HEIGHT; ++i) {
            for (int k = 0; k < LENGTH; ++k) {
                for (int j = -HALF_WIDTH; j <= HALF_WIDTH; ++j) {
                    BlockPos p = new BlockPos(x + k * deltaX + j * deltaZ, y + i, z + k * deltaZ + j * deltaX);
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    if (i == 0) {
                        level.setBlock(p.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    }
                }
            }
        }

        // orig InstantGarden.java:82-140 — 15 cross-wise rows (i = j + 7),
        // skipping the first and last length column (k = 1..16)
        for (int k = 1; k < LENGTH - 1; ++k) {
            for (int j = -HALF_WIDTH; j <= HALF_WIDTH; ++j) {
                int row = j + HALF_WIDTH;
                BlockPos surface = new BlockPos(x + k * deltaX + j * deltaZ, y, z + k * deltaZ + j * deltaX);
                switch (row) {
                    case 1 -> plantRow(level, surface, ModBlocks.RADISH_PLANT.get().defaultBlockState());   // orig :85-88
                    case 2 -> plantRow(level, surface, ModBlocks.LETTUCE_0.get().defaultBlockState());      // orig :89-92
                    case 3 -> plantRow(level, surface, Blocks.CARROTS.defaultBlockState());                 // orig :93-96
                    case 4, 8, 12 -> waterChannel(level, surface);                                          // orig :97-100,113-116,130-133
                    case 5 -> plantRow(level, surface, Blocks.POTATOES.defaultBlockState());                // orig :101-104
                    case 6 -> plantRow(level, surface, Blocks.WHEAT.defaultBlockState());                   // orig :105-108
                    case 7 -> plantRow(level, surface, ModBlocks.TOMATO_0.get().defaultBlockState());       // orig :109-112
                    case 9 -> plantRow(level, surface, ModBlocks.CORN_0.get().defaultBlockState());         // orig :117-120
                    case 10 -> plantRow(level, surface, ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState()); // orig :121-124
                    case 11 -> {                                                                            // orig :125-129
                        level.setBlock(surface.below(2), Blocks.COBBLESTONE.defaultBlockState(), 2);
                        level.setBlock(surface.below(), Blocks.SAND.defaultBlockState(), 2);
                        level.setBlock(surface, Blocks.SUGAR_CANE.defaultBlockState(), 2);
                    }
                    case 13 -> plantRow(level, surface, Blocks.MELON_STEM.defaultBlockState());             // orig :134-137
                    default -> { /* rows 0 and 14 stay grass (orig has no case) */ }
                }
            }
        }

        // orig InstantGarden.java:141-143 — consume one unless creative
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /** orig pattern — farmland below, crop on top. */
    private static void plantRow(Level level, BlockPos surface, BlockState crop) {
        level.setBlock(surface.below(), Blocks.FARMLAND.defaultBlockState(), 2);
        level.setBlock(surface, crop, 2);
    }

    /** orig pattern — cobblestone two below, water one below the surface. */
    private static void waterChannel(Level level, BlockPos surface) {
        level.setBlock(surface.below(2), Blocks.COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(surface.below(), Blocks.WATER.defaultBlockState(), 2);
    }
}
