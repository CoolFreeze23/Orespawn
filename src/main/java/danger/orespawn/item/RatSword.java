package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.ModToolTiers;
import danger.orespawn.entity.EntityRat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public class RatSword extends SwordItem {
    public RatSword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide) {
            int count = 1 + level.random.nextInt(6);
            for (int i = 0; i < count; i++) {
                EntityRat rat = ModEntities.ENTITY_RAT.get().create(level);
                if (rat != null) {
                    rat.moveTo(target.getX() + level.random.nextGaussian(),
                            target.getY(),
                            target.getZ() + level.random.nextGaussian(),
                            level.random.nextFloat() * 360.0F, 0.0F);
                    rat.setOwner(attacker);
                    level.addFreshEntity(rat);
                }
            }
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
