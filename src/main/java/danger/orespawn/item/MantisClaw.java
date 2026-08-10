package danger.orespawn.item;

import danger.orespawn.ModToolTiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class MantisClaw extends SwordItem {
    public MantisClaw(Item.Properties properties) {
        // ENT-K-032: orig OreSpawnMain.java:1661 constructs MantisClaw on
        // toolEMERALD ("REALEMERALD", stats :1512 — dmg 6, ench 75, emerald
        // repair). The class's getMaterialName() returning "AMETHYST" is
        // cosmetic-only in 1.7.10; the tier here drives enchantability and
        // repair ingredient. Attack attributes come from the registration
        // (EMERALD, modern +3 base per the ENT-A-045 convention).
        super(ModToolTiers.EMERALD, properties);
    }

    /**
     * orig MantisClaw.java:25 — {@code setMaxDamage(1000)} overrides the
     * Emerald tool material's 1300.
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 1000;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null && attacker != null && !target.level().isClientSide) {
            // orig MantisClaw.java:36-37 — silent health drain via heal(-1), no damage event/invuln frames
            // (setHealth used because NeoForge's heal() rejects negative amounts)
            if (target.getHealth() > 0.0f) {
                target.setHealth(target.getHealth() - 1.0f);
            }
            attacker.heal(1.0f);
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
