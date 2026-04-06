package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class ItemNetherLost extends Item {
    public ItemNetherLost(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!isSelected) return;

        // TODO: Enchantments are data-driven in 1.21.1, need registry lookup
        // if (stack.getEnchantmentLevel(Enchantments.SHARPNESS) <= 0) {
        //     stack.enchant(Enchantments.SHARPNESS, 2);
        // }

        if (entity instanceof Player player && level.dimension() == Level.NETHER) {
            BlockPos below = player.blockPosition().below();
            if (level.getBlockState(below).is(Blocks.SOUL_SAND)) {
                level.setBlock(below, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        }
    }
}
