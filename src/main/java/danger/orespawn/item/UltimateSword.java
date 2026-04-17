package danger.orespawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import danger.orespawn.ModDataComponents;
import danger.orespawn.ModToolTiers;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.util.OreSpawnEnchantHelper;

import java.util.List;

public class UltimateSword extends SwordItem {
    public UltimateSword(Item.Properties properties) {
        super(ModToolTiers.ULTIMATE, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            // Config: ultimateSwordEnchantmentLevel controls sharpness level
            int sharpnessLevel = OreSpawnConfig.ULTIMATE_SWORD_MAGIC.get();
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, sharpnessLevel);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SMITE, 5);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.BANE_OF_ARTHROPODS, 5);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.KNOCKBACK, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.FIRE_ASPECT, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.LOOTING, 3);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.UNBREAKING, 2);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity != null) {
            // Config: ultimateSwordPvp allows hitting players when true
            if (entity instanceof Player && !OreSpawnConfig.ULTIMATE_SWORD_PVP.get()) {
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
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        if (target.getHealth() <= 0.0f && attacker instanceof Player) {
            int prior = stack.getOrDefault(ModDataComponents.ULTIMATE_SWORD_KILLS.get(), 0);
            stack.set(ModDataComponents.ULTIMATE_SWORD_KILLS.get(), prior + 1);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int kills = stack.getOrDefault(ModDataComponents.ULTIMATE_SWORD_KILLS.get(), 0);
        if (kills > 0) {
            tooltip.add(Component.translatable("tooltip.orespawn.ultimate_sword_kills", kills)
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
