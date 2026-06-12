package danger.orespawn.item;

import danger.orespawn.ModItems;
import danger.orespawn.entity.IrukandjiArrow;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * Skate Bow, ported from 1.7.10 SkateBow.java — fires Irukandji arrows with
 * a charge-up draw (cap 1.75), consuming one arrow unless in creative or the
 * bow carries Infinity.
 */
public class SkateBow extends BowItem {
    public SkateBow(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;

        // orig SkateBow.java:39-40 — creative mode or Infinity skips ammo entirely
        boolean freeShot = player.getAbilities().instabuild
                || stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                        .keySet().stream().anyMatch(h -> h.is(Enchantments.INFINITY));
        if (!freeShot && !player.getInventory().contains(new ItemStack(ModItems.IRUKANDJI_ARROW.get()))) {
            return;
        }

        // orig SkateBow.java:42-48 — pull = (f^2 + 2f)/3, min 0.1, cap 1.75
        int charge = this.getUseDuration(stack, entityLiving) - timeLeft;
        float pull = (float) charge / 20.0F;
        pull = (pull * pull + pull * 2.0F) / 3.0F;
        if (pull < 0.1F) return;
        if (pull > 1.75F) pull = 1.75F;

        if (!level.isClientSide) {
            IrukandjiArrow arrow = new IrukandjiArrow(level, player, stack);
            // orig SkateBow.java:49 — the 1.7.10 EntityArrow(world, shooter, f)
            // constructor launches at f * 1.5
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, pull * 1.5F, 1.0F);
            // orig SkateBow.java:50-52 — 1/20 chance to crit
            if (level.random.nextInt(20) == 1) {
                arrow.setCritArrow(true);
            }
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }

        // orig SkateBow.java:61-63 — consume one arrow on non-free shots
        if (!freeShot) {
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
