package danger.orespawn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.util.OreSpawnEnchantHelper;

/**
 * 1.7.10 UltimateSword class — shared by the Ultimate Sword, Battle Axe,
 * Queen Battle Axe and Chainsaw (orig OreSpawnMain.java:1636,1649-1651).
 *
 * <p>orig UltimateSword.java:44-59 — baked enchants are derived from the
 * {@code UltimateSwordEnchantmentLevel} config (default 5, clamp 1..10; orig
 * OreSpawnMain.java:1518-1525). The Chainsaw gets none, the Battle Axe gets
 * only Looting/Unbreaking, everything else (Ultimate Sword AND Queen Battle
 * Axe) gets the full seven-enchant set.</p>
 */
public class UltimateSword extends SwordItem {

    /** Which branch of orig UltimateSword.java:44-59 this item takes. */
    public enum Variant {
        /** Full set (orig :49-55) — Ultimate Sword, Queen Battle Axe. */
        FULL,
        /** Looting + Unbreaking only (orig :56-59) — Battle Axe. */
        BATTLE_AXE,
        /** No baked enchants (orig :45-47) — Chainsaw. */
        NONE
    }

    private final Variant variant;

    public UltimateSword(Tier tier, Variant variant, Item.Properties properties) {
        super(tier, properties);
        this.variant = variant;
    }

    /**
     * orig UltimateSword.java:40 — {@code setMaxDamage(3000)} class-level
     * override applies to every UltimateSword-class weapon regardless of its
     * tool material (Battle Axe tier 1500, Queen Battle Axe tier 2200,
     * Chainsaw tier 1500 all end up at 3000).
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 3000;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || variant == Variant.NONE) return;
        if (OreSpawnEnchantHelper.hasAnyEnchantments(stack)) return;
        // orig OreSpawnMain.java:1518 — UltimateSwordEnchantmentLevel, default 5
        int magic = OreSpawnConfig.ULTIMATE_SWORD_MAGIC.get();
        if (variant == Variant.FULL) {
            // orig UltimateSword.java:49-55 — Sharp/Smite/Bane = magic,
            // KB/Looting/Unbreaking = 1 + magic/2, Fire Aspect = 1 + magic/3
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, magic);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SMITE, magic);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.BANE_OF_ARTHROPODS, magic);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.KNOCKBACK, 1 + magic / 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.LOOTING, 1 + magic / 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.UNBREAKING, 1 + magic / 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FIRE_ASPECT, 1 + magic / 3);
        } else {
            // orig UltimateSword.java:57-58 — Battle Axe: Looting + Unbreaking
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.LOOTING, 1 + magic / 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.UNBREAKING, 1 + magic / 2);
        }
    }

    /**
     * orig UltimateSword.java:138-147 — when {@code ultimate_sword_pvp} is
     * off, refuses to hit players, Girlfriends, Boyfriends and tamed pets.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity != null && !OreSpawnConfig.ULTIMATE_SWORD_PVP.get()) {
            if (entity instanceof Player || entity instanceof Girlfriend || entity instanceof Boyfriend) {
                return true;
            }
            if (entity instanceof TamableAnimal t && t.isTame()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // orig UltimateSword.java:133-136 — 1 durability per hit
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
