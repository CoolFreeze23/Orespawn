package danger.orespawn.item;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.BerthaHit;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.util.OreSpawnEnchantHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 Bertha class — shared by Big Bertha, Slice, the Royal Guardian Sword
 * and the Attitude Adjuster (orig OreSpawnMain.java:1645-1648). All of them
 * fire the BerthaHit shockwave on swing (orig Bertha.java:78-98) and share a
 * 9000-durability class override (orig Bertha.java:31).
 */
public class Bertha extends SwordItem {
    private final int hitType;
    private final ResourceKey<Enchantment>[] enchantKeys;
    private final int[] enchantLevels;

    @SafeVarargs
    public Bertha(Tier tier, Item.Properties properties, int hitType,
                  int[] enchantLevels, ResourceKey<Enchantment>... enchantKeys) {
        super(tier, properties);
        this.hitType = hitType;
        this.enchantKeys = enchantKeys;
        this.enchantLevels = enchantLevels;
    }

    /**
     * orig Bertha.java:31 — {@code setMaxDamage(9000)} class-level override
     * applies to every Bertha-class weapon regardless of tool material
     * (Royal tier 10000 and Hammy tier 2000 both end up at 9000).
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return 9000;
    }

    /**
     * orig Bertha.java:35-43 — func_77622_d (onCrafted) bakes the variant's
     * enchant set the moment the sword is crafted; the inventory-tick path
     * below only exists to re-bake a stack that was later stripped.
     */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            applyBakedEnchants(stack, level);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) {
            return;
        }
        // orig Bertha.java:45-58 — onUsingTick (routed through func_77663_a every
        // inventory tick) probes Knockback (field_77337_m) and falls back to
        // Unbreaking (field_77347_r); only when BOTH read 0 does it re-bake. The
        // two probe keys are the same for every variant — Royal passes via its
        // baked Unbreaking 5, Hammy re-enters the branch each tick and bakes
        // nothing. Probing these two specific enchants (not "any enchant")
        // preserves the orig's re-bake over a stack that carries only, say, a
        // command-given Fire Aspect.
        int lvl = enchantLevel(stack, Enchantments.KNOCKBACK);
        if (lvl == 0) {
            lvl = enchantLevel(stack, Enchantments.UNBREAKING);
        }
        if (lvl <= 0) {
            applyBakedEnchants(stack, level);
        }
    }

    private void applyBakedEnchants(ItemStack stack, Level level) {
        for (int i = 0; i < enchantKeys.length; i++) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, enchantKeys[i], enchantLevels[i]);
        }
    }

    /** Component-map equivalent of 1.7.10 EnchantmentHelper.func_77506_a (orig Bertha.java:46-48). */
    private static int enchantLevel(ItemStack stack, ResourceKey<Enchantment> key) {
        for (var entry : stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet()) {
            if (entry.getKey().is(key)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && !entity.level().isClientSide) {
            double xzOff = 2.0;
            double yOff = 1.55;
            float yaw = (float) Math.toRadians(player.getYRot());

            BerthaHit hit = new BerthaHit(player.level(), player);
            hit.moveTo(
                    player.getX() - xzOff * Math.sin(yaw),
                    player.getY() + yOff,
                    player.getZ() + xzOff * Math.cos(yaw),
                    player.getYRot(), player.getXRot()
            );
            hit.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            hit.setDeltaMovement(hit.getDeltaMovement().scale(2.0));
            hit.setHitType(hitType);
            player.level().addFreshEntity(hit);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // orig Bertha.java:65-76 — the entire skip list (players, Girlfriend,
        // Boyfriend, tamed pets) only applies while big_bertha_pvp == 0.
        if (entity != null && !OreSpawnConfig.BIG_BERTHA_PVP.get()) {
            if (entity instanceof Player || entity instanceof Girlfriend || entity instanceof Boyfriend) {
                return true;
            }
            if (entity instanceof TamableAnimal t && t.isTame()) return true;
        }
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // orig Bertha.java:104-107 — 1 durability per hit
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
