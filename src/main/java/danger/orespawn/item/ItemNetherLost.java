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

/**
 * Lost-in-the-Nether compass, ported from 1.7.10 ItemNetherLost.java:29-57.
 * Bakes Sharpness 2 onto itself and, while held in the Nether, turns the
 * netherrack block under the player into quartz block, leaving a breadcrumb
 * trail home.
 */
public class ItemNetherLost extends Item {
    public ItemNetherLost(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;

        // orig ItemNetherLost.java:30,36 — Sharpness (field_77338_j) 2 baked on
        if (!OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 2);
        }

        // orig ItemNetherLost.java:50-52 — held item, Nether only (dimension -1),
        // netherrack below the player becomes quartz block
        if (isSelected && entity instanceof Player player && level.dimension() == Level.NETHER) {
            BlockPos below = player.blockPosition().below();
            if (level.getBlockState(below).is(Blocks.NETHERRACK)) {
                level.setBlock(below, Blocks.QUARTZ_BLOCK.defaultBlockState(), 3);
            }
        }
    }
}
