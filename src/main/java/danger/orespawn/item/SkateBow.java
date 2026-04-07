package danger.orespawn.item;

import danger.orespawn.ModItems;
import danger.orespawn.entity.IrukandjiArrow;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SkateBow extends BowItem {
    public SkateBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;

        boolean hasAmmo = player.getAbilities().instabuild
                || player.getInventory().contains(new ItemStack(ModItems.IRUKANDJI_ARROW.get()));
        if (!hasAmmo) return;

        int charge = this.getUseDuration(stack, entityLiving) - timeLeft;
        float pull = (float) charge / 20.0F;
        pull = (pull * pull + pull * 2.0F) / 3.0F;
        if (pull < 0.1F) return;
        if (pull > 1.75F) pull = 1.75F;

        if (!level.isClientSide) {
            IrukandjiArrow arrow = new IrukandjiArrow(level, player, stack);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, pull * 2.0F, 1.0F);
            if (level.random.nextInt(20) == 0) {
                arrow.setCritArrow(true);
            }
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }

        if (!player.getAbilities().instabuild) {
            ItemStack ammo = findAmmo(player);
            if (!ammo.isEmpty()) ammo.shrink(1);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + 0.5F);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    private static ItemStack findAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ModItems.IRUKANDJI_ARROW.get())) return s;
        }
        return ItemStack.EMPTY;
    }
}
