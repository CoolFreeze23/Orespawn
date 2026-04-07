package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class UltimateArrow extends AbstractArrow {
    private static final double BASE_DAMAGE = 12.0;

    public UltimateArrow(EntityType<? extends UltimateArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public UltimateArrow(Level level, LivingEntity shooter, ItemStack weapon) {
        super(ModEntities.ULTIMATE_ARROW.get(), shooter, level, new ItemStack(Items.ARROW), weapon);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
