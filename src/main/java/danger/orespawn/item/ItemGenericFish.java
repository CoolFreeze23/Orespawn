package danger.orespawn.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemGenericFish extends Item {
    public ItemGenericFish(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        // orig ItemGenericFish.java:24-25 — 1-in-4 chance of Hunger for 20 ticks
        if (!level.isClientSide && level.random.nextInt(4) == 1) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20, 0));
        }
        return result;
    }
}
