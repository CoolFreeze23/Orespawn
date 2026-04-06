package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSifter extends Item {
    public ItemSifter(Item.Properties properties) {
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

        boolean validBlock = state.is(Blocks.DIRT) || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.SOUL_SAND);

        if (!validBlock) return InteractionResult.FAIL;

        int roll = level.random.nextInt(100);
        ItemStack drop = ItemStack.EMPTY;

        if (roll < 20) {
            drop = new ItemStack(Items.WHEAT_SEEDS, 1);
        } else if (roll < 30) {
            drop = new ItemStack(Items.COAL, 1);
        } else if (roll < 38) {
            drop = new ItemStack(Items.IRON_INGOT, 1);
        } else if (roll < 44) {
            drop = new ItemStack(Items.GOLD_INGOT, 1);
        } else if (roll < 50) {
            drop = new ItemStack(Items.REDSTONE, 1);
        } else if (roll < 55) {
            drop = new ItemStack(Items.EMERALD, 1);
        } else if (roll < 58) {
            drop = new ItemStack(Items.DIAMOND, 1);
        } else if (roll < 60) {
            drop = new ItemStack(Items.LAPIS_LAZULI, 1);
        }

        if (!drop.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, drop);
            level.addFreshEntity(itemEntity);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
        return InteractionResult.SUCCESS;
    }
}
