package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import danger.orespawn.ModToolTiers;

public class ExperienceSword extends SwordItem {
    public ExperienceSword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // TODO: Enchantments are data-driven in 1.21.1, need registry lookup
        // if (!level.isClientSide && stack.getEnchantmentLevel(Enchantments.SHARPNESS) <= 0) {
        //     stack.enchant(Enchantments.SHARPNESS, 2);
        //     stack.enchant(Enchantments.LOOTING, 3);
        // }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float xpBonus = 0;
        Player player = null;
        if (attacker instanceof Player p) {
            player = p;
        }
        if (target != null) {
            xpBonus = 10.0f;
        }
        if (xpBonus > 0 && player != null) {
            player.giveExperiencePoints((int) xpBonus);
        }
        if (player != null) {
            float bonusDamage = player.experienceLevel / 2.0f;
            if (bonusDamage > 0 && target != null) {
                target.hurt(target.damageSources().playerAttack(player), bonusDamage);
            }
        }
        if (target != null && target.level() instanceof Level clientLevel) {
            for (int j = 0; j < (int)(xpBonus / 2.0f); j++) {
                clientLevel.addParticle(ParticleTypes.PORTAL,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        clientLevel.random.nextGaussian(), clientLevel.random.nextGaussian(), clientLevel.random.nextGaussian());
            }
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
