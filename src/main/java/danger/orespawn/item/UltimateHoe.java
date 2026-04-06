package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModToolTiers;

public class UltimateHoe extends HoeItem {
    public UltimateHoe(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.EFFICIENCY) <= 0) {
            stack.enchant(Enchantments.EFFICIENCY, 2);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            for (int i = -1; i <= 1; i++) {
                for (int k = -1; k <= 1; k++) {
                    for (int j = -1; j <= 1; j++) {
                        BlockPos target = pos.offset(i, j, k);
                        BlockState targetState = level.getBlockState(target);
                        boolean airAbove = level.isEmptyBlock(target.above());
                        if (airAbove && (targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.DIRT))) {
                            level.setBlock(target, Blocks.FARMLAND.defaultBlockState(), 2);
                        }
                    }
                }
            }
            context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
