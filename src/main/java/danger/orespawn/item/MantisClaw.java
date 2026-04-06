package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.LivingEntity;
import danger.orespawn.ModToolTiers;

public class MantisClaw extends SwordItem {
    public MantisClaw(Item.Properties properties) {
        super(ModToolTiers.AMETHYST, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null && attacker != null && !target.level().isClientSide) {
            target.heal(-1.0f);
            attacker.heal(1.0f);
        }
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        return true;
    }
}
