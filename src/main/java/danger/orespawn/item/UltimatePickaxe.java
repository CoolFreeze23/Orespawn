package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModToolTiers;

public class UltimatePickaxe extends PickaxeItem {
    public UltimatePickaxe(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // TODO: Enchantments are data-driven in 1.21.1, need registry lookup
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity != null) {
            if (entity instanceof Player) {
                return true;
            }
            if (entity instanceof TamableAnimal t && t.isTame()) {
                return true;
            }
        }
        return false;
    }
}
