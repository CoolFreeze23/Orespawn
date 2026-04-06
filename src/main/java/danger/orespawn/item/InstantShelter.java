package danger.orespawn.item;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class InstantShelter extends Item {
    public InstantShelter(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().above();
        int baseX = pos.getX();
        int baseY = pos.getY();
        int baseZ = pos.getZ();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 4; y++) {
                    BlockPos bp = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    boolean isWall = x == -2 || x == 2 || z == -2 || z == 2;
                    boolean isFloor = y == 0;
                    boolean isCeiling = y == 4;

                    if (isFloor || isCeiling) {
                        level.setBlock(bp, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    } else if (isWall) {
                        level.setBlock(bp, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        level.setBlock(new BlockPos(baseX, baseY + 1, baseZ), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        level.setBlock(new BlockPos(baseX + 1, baseY + 1, baseZ), Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(new BlockPos(baseX - 1, baseY + 1, baseZ), Blocks.CHEST.defaultBlockState(), 3);

        BlockPos chestPos = new BlockPos(baseX - 1, baseY + 1, baseZ);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.BREAD, 16));
            chest.setItem(1, new ItemStack(Items.TORCH, 16));
            chest.setItem(2, new ItemStack(Items.COAL, 8));
            chest.setItem(3, new ItemStack(Items.WOODEN_PICKAXE, 1));
            chest.setItem(4, new ItemStack(Items.WOODEN_SWORD, 1));
        }

        level.setBlock(new BlockPos(baseX, baseY + 3, baseZ + 2), Blocks.TORCH.defaultBlockState(), 3);
        level.setBlock(new BlockPos(baseX, baseY + 3, baseZ - 2), Blocks.TORCH.defaultBlockState(), 3);

        level.playSound(null, player.blockPosition(), SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
