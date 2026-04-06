package danger.orespawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class ItemNetherLost extends Item {
    public ItemNetherLost(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!isSelected) return;

        if (!OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 2);
        }

        if (entity instanceof Player player && level.dimension() == Level.NETHER) {
            BlockPos below = player.blockPosition().below();
            if (level.getBlockState(below).is(Blocks.SOUL_SAND)) {
                level.setBlock(below, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        }
    }
}
