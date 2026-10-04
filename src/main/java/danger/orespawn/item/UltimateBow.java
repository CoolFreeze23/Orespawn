package danger.orespawn.item;

import danger.orespawn.entity.UltimateArrow;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.util.OreSpawnEnchantHelper;

public class UltimateBow extends BowItem {
    public UltimateBow(Item.Properties properties) {
        super(properties);
    }

    /**
     * orig UltimateBow.java:83-85 — getItemEnchantability 50 (a vanilla bow has 1). It takes the durability set
     * (Unbreaking, Mending, Curse of Vanishing) at the anvil, and the table offers it Unbreaking while it carries no
     * enchantment, which its own Power, Flame, Punch and Infinity end on its first tick in an inventory; no bow
     * enchantments from the table or the anvil, which 1.7.10's plain-Item bows never took there.
     */
    @Override
    public int getEnchantmentValue() {
        return 50;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            // orig UltimateBow.java:30-33 — fixed self-enchants Power 5 / Flame 3 /
            // Punch 2 / Infinity 1. The ultimateBowDamage config scales the ARROW's
            // base damage instead (orig UltimateArrow.java:157), not the Power level.
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.POWER, 5);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FLAME, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.PUNCH, 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.INFINITY, 1);
        }
    }

    /**
     * orig UltimateBow.java:46-64 — the arrow always launches at velocity 3.0
     * the moment the use is released; there is no charge-up scaling and no
     * minimum draw. Crit is a flat 1-in-4 roll (orig :49-51).
     */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;

        if (!level.isClientSide) {
            UltimateArrow arrow = new UltimateArrow(level, player, stack);
            // orig UltimateBow.java:48 — fixed velocity 3.0
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
            // orig UltimateBow.java:49-51 — 1/4 chance to crit
            if (level.random.nextInt(4) == 1) arrow.setCritArrow(true);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }
        // orig UltimateBow.java:59 — bow sound with the vanilla random pitch + 0.5
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + 0.5F);
        // orig UltimateBow.java:58 — 1 durability per shot
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
