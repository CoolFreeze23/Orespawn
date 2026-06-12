package danger.orespawn.item;

import danger.orespawn.ModBlocks;
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
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Instant Shelter, ported from 1.7.10 InstantShelter.java:28-142. Builds a
 * 7x7 shelter centered on the player (cobblestone floor, plank walls and
 * roof, glass band at head height, 2-high doorway facing the clicked
 * direction) and furnishes it with a furnace, crafting table and a chest
 * stocked with the original survival kit.
 */
public class InstantShelter extends Item {
    // orig InstantShelter.java:35-37 — length 3, width 3, height 3 (half-extents)
    private static final int HALF = 3;
    private static final int WALL_HEIGHT = 3;

    public InstantShelter(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos playerPos = player.blockPosition();
        // orig InstantShelter.java:51-52,76-77 — shelter centered on the player,
        // floor one below the player's feet
        int x = playerPos.getX();
        int y = playerPos.getY() - 1;
        int z = playerPos.getZ();

        // orig InstantShelter.java:47-75 — the doorway faces the clicked block;
        // diagonal or same-column clicks do nothing
        int deltaX = Integer.signum(context.getClickedPos().getX() - playerPos.getX());
        int deltaZ = Integer.signum(context.getClickedPos().getZ() - playerPos.getZ());
        if ((deltaX == 0) == (deltaZ == 0)) {
            return InteractionResult.FAIL;
        }
        // orig InstantShelter.java:54-69 — furnace/chest facing metadata
        // (2=north, 3=south, 4=west, 5=east) chosen per door direction
        Direction stuffFacing;
        if (deltaX < 0) stuffFacing = Direction.SOUTH;      // orig :54-57 — stuffdir 3
        else if (deltaX > 0) stuffFacing = Direction.NORTH; // orig :58-61 — stuffdir 2
        else if (deltaZ < 0) stuffFacing = Direction.EAST;  // orig :62-65 — stuffdir 5
        else stuffFacing = Direction.WEST;                  // orig :66-69 — stuffdir 4

        // orig InstantShelter.java:78 — explosion sound 1.0 / 1.5
        level.playSound(null, playerPos, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 1.0f, 1.5f);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // orig InstantShelter.java:82-108 — cobble floor (k=0), plank roof (k=4),
        // perimeter walls with glass band at k=3 and a 2-high doorway at the
        // clicked-direction wall center, air inside
        for (int i = -HALF; i <= HALF; ++i) {
            for (int j = -HALF; j <= HALF; ++j) {
                for (int k = 0; k <= WALL_HEIGHT + 1; ++k) {
                    BlockPos p = new BlockPos(x + i, y + k, z + j);
                    if (k == WALL_HEIGHT + 1) {
                        level.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    } else if (k == 0) {
                        level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    } else if (i == HALF || j == HALF || i == -HALF || j == -HALF) {
                        if (k == WALL_HEIGHT) {
                            level.setBlock(p, Blocks.GLASS.defaultBlockState(), 3);
                        } else if ((k == 1 || k == 2) && i == deltaX * HALF && j == deltaZ * HALF) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        } else {
                            level.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                        }
                    } else {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // orig InstantShelter.java:109-118 — furnace / crafting table / chest along
        // the back wall row (rotated coordinates: i along facing, j across)
        int row = HALF - 1;
        BlockPos furnacePos = new BlockPos(x + 2 * deltaX + row * deltaZ, y + 1, z + 2 * deltaZ + row * deltaX);
        level.setBlock(furnacePos, Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, stuffFacing), 3);
        BlockPos craftingPos = new BlockPos(x + deltaX + row * deltaZ, y + 1, z + deltaZ + row * deltaX);
        level.setBlock(craftingPos, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        BlockPos chestPos = new BlockPos(x + row * deltaZ, y + 1, z + row * deltaX);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, stuffFacing), 3);

        // orig InstantShelter.java:119-135 — fixed chest kit
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.COMPASS));
            chest.setItem(1, new ItemStack(Items.MAP));
            chest.setItem(2, new ItemStack(Items.PORKCHOP, 8));
            chest.setItem(3, new ItemStack(Blocks.TORCH, 32));
            chest.setItem(4, new ItemStack(Items.COAL, 16));
            chest.setItem(5, new ItemStack(Items.RED_BED));
            chest.setItem(6, new ItemStack(Items.RED_BED));
            chest.setItem(7, new ItemStack(Items.OAK_DOOR));
            chest.setItem(8, new ItemStack(Items.IRON_PICKAXE));
            chest.setItem(9, new ItemStack(Items.IRON_SWORD));
            chest.setItem(10, new ItemStack(Items.IRON_AXE));
            chest.setItem(11, new ItemStack(Items.BUCKET));
            chest.setItem(12, new ItemStack(ModBlocks.ORE_SALT.get(), 4));
            chest.setItem(13, new ItemStack(Blocks.CHEST));
        }

        // orig InstantShelter.java:136-138 — consume one unless creative
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
