package danger.orespawn.item;

import danger.orespawn.ModToolTiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class MantisClaw extends SwordItem {
    public MantisClaw(Item.Properties properties) {
        super(ModToolTiers.AMETHYST, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null && attacker != null && !target.level().isClientSide) {
            target.hurt(target.damageSources().magic(), 1.0f);
            attacker.heal(1.0f);
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
