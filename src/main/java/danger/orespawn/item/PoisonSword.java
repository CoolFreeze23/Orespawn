package danger.orespawn.item;

import danger.orespawn.ModToolTiers;
import danger.orespawn.util.OreSpawnEnchantHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class PoisonSword extends SwordItem {
    public PoisonSword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 1);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null) {
            int dur = 10 + target.level().random.nextInt(10);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, dur * 20, 0));
            dur = 10 + target.level().random.nextInt(10);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, dur * 20, 0));
            dur = 10 + target.level().random.nextInt(10);
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, dur * 20, 0));
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
