package danger.orespawn.item;

import danger.orespawn.ModEntities;
import danger.orespawn.ModToolTiers;
import danger.orespawn.entity.Fairy;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public class FairySword extends SwordItem {
    public FairySword(Item.Properties properties) {
        super(ModToolTiers.EMERALD, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide) {
            int count = 1 + level.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                Fairy fairy = ModEntities.FAIRY.get().create(level);
                if (fairy != null) {
                    fairy.moveTo(target.getX() + level.random.nextGaussian(),
                            target.getY() + 1.0,
                            target.getZ() + level.random.nextGaussian(),
                            level.random.nextFloat() * 360.0F, 0.0F);
                    fairy.setOwner(attacker);
                    level.addFreshEntity(fairy);
                }
            }
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
