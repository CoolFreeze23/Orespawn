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

/**
 * Experience Sword, ported from 1.7.10 ExperienceSword.java. Grants XP on
 * hit, deals bonus damage scaling with the wielder's level, and generates XP
 * over time while the holder wears Experience armor.
 */
public class ExperienceSword extends SwordItem {
    // orig ExperienceSword.java:122 — +10 XP per hit on a living target
    private static final float XP_ON_HIT = 10.0f;
    private static final float XP_PER_PARTICLE_DIVISOR = 2.0f;

    public ExperienceSword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    /**
     * orig ExperienceSword.java:35 — {@code setMaxDamage(1400)} overrides the
     * Emerald tool material's 1300.
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 1400;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            // orig ExperienceSword.java:40-41 — Sharpness 2 + Unbreaking 3
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.UNBREAKING, 3);
        }
        tickArmorExperience(level, entity);
    }

    /**
     * orig ExperienceSword.java:63-103 — while the sword ticks in a player's
     * inventory, each worn Experience armor piece occasionally generates XP:
     * a 1-in-60 outer roll per tick, then per piece helmet 1-in-10, chest
     * 1-in-20, leggings 1-in-30, boots 1-in-40 for +1 XP, with portal
     * particles at slot-dependent heights.
     */
    private void tickArmorExperience(Level level, Entity entity) {
        if (!(entity instanceof Player player)) return;
        if (level.random.nextInt(60) != 1) return;

        grantArmorXp(level, player, EquipmentSlot.HEAD, 10, 1.5);   // orig :73-79
        grantArmorXp(level, player, EquipmentSlot.CHEST, 20, 1.25); // orig :80-86
        grantArmorXp(level, player, EquipmentSlot.LEGS, 30, 0.75);  // orig :87-93
        grantArmorXp(level, player, EquipmentSlot.FEET, 40, 0.25);  // orig :94-100
    }

    private void grantArmorXp(Level level, Player player, EquipmentSlot slot, int oneIn, double particleYOffset) {
        ItemStack worn = player.getItemBySlot(slot);
        if (!(worn.getItem() instanceof ItemOreSpawnArmor armor)
                || !"experience".equals(armor.getArmorMaterialName())) {
            return;
        }
        if (!level.isClientSide && level.random.nextInt(oneIn) == 1) {
            player.giveExperiencePoints(1);
        }
        level.addParticle(ParticleTypes.PORTAL,
                player.getX(), player.getY() + particleYOffset, player.getZ(),
                level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextGaussian());
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
            // orig ExperienceSword.java:121-125 — +10 XP per hit
            attackingPlayer.giveExperiencePoints((int) xpBonus);
        }
        if (attackingPlayer != null) {
            // orig ExperienceSword.java:127-128 — bonus damage = playerLevel / 2
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
