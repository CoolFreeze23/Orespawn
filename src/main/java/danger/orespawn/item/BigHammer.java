package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.LivingEntity;
import danger.orespawn.ModToolTiers;

public class BigHammer extends SwordItem {
    public BigHammer(Item.Properties properties) {
        super(ModToolTiers.AMETHYST, properties);
    }

    /**
     * orig BigHammer.java:25 — {@code setMaxDamage(9000)} overrides the
     * Amethyst tool material's 2000.
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 9000;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null && !target.level().isClientSide) {
            target.push(0.0, Math.abs(target.level().random.nextFloat() * 2.0f / 3.0f), 0.0);
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
