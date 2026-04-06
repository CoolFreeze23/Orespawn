package danger.orespawn.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import danger.orespawn.ModToolTiers;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class ExperienceSword extends SwordItem {
    private static final float XP_ON_HIT = 10.0f;
    private static final float XP_PER_PARTICLE_DIVISOR = 2.0f;

    public ExperienceSword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.LOOTING, 3);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float xpBonus = 0;
        Player attackingPlayer = null;
        if (attacker instanceof Player playerAttacker) {
            attackingPlayer = playerAttacker;
        }
        if (target != null) {
            xpBonus = XP_ON_HIT;
        }
        if (xpBonus > 0 && attackingPlayer != null) {
            attackingPlayer.giveExperiencePoints((int) xpBonus);
        }
        if (attackingPlayer != null) {
            float bonusDamage = attackingPlayer.experienceLevel / 2.0f;
            if (bonusDamage > 0 && target != null) {
                target.hurt(target.damageSources().playerAttack(attackingPlayer), bonusDamage);
            }
        }
        if (target != null && target.level() instanceof Level targetWorld) {
            int particleCount = (int) (xpBonus / XP_PER_PARTICLE_DIVISOR);
            for (int particleIndex = 0; particleIndex < particleCount; particleIndex++) {
                targetWorld.addParticle(ParticleTypes.PORTAL,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        targetWorld.random.nextGaussian(), targetWorld.random.nextGaussian(), targetWorld.random.nextGaussian());
            }
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
