package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.entity.BerthaHit;
import danger.orespawn.util.OreSpawnEnchantHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class Slice extends SwordItem {
    public Slice(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !OreSpawnEnchantHelper.hasAnyEnchantments(stack)) {
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.SHARPNESS, 5);
            OreSpawnEnchantHelper.applyEnchantment(stack, level, Enchantments.BANE_OF_ARTHROPODS, 1);
        }
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
            player.level().addFreshEntity(hit);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
