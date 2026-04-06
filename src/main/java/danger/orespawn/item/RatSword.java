package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.LivingEntity;
import danger.orespawn.ModToolTiers;

public class RatSword extends SwordItem {
    public RatSword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // TODO: Spawn Rat entities on hit when entity type is registered
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
