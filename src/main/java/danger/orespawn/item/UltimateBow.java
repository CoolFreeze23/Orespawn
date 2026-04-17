package danger.orespawn.item;

import danger.orespawn.OreSpawnConfig;
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

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            // Config: ultimateBowDamage controls the POWER enchantment level
            int powerLevel = OreSpawnConfig.ULTIMATE_BOW_DAMAGE.get();
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.POWER, powerLevel);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FLAME, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.PUNCH, 2);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.INFINITY, 1);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int charge = this.getUseDuration(stack, entityLiving) - timeLeft;
        float pull = (float) charge / 20.0F;
        pull = (pull * pull + pull * 2.0F) / 3.0F;
        if (pull < 0.1F) return;
        if (pull > 1.0F) pull = 1.0F;

        if (!level.isClientSide) {
            UltimateArrow arrow = new UltimateArrow(level, player, stack);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, pull * 3.0F, 1.0F);
            // 1.7.10 fidelity: full pull is always crit, plus an additional 1/4 random
            // chance to crit on under-pulled shots (matches the original
            // `field_70146_Z.nextInt(4) == 1` roll in UltimateBow.func_77615_a).
            if (pull >= 1.0F || level.random.nextInt(4) == 0) arrow.setCritArrow(true);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + pull * 0.5F);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
