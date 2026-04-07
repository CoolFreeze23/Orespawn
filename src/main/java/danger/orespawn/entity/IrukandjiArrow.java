package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class IrukandjiArrow extends AbstractArrow {
    private static final double BASE_DAMAGE = 6.0;

    public IrukandjiArrow(EntityType<? extends IrukandjiArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public IrukandjiArrow(Level level, LivingEntity shooter, ItemStack weapon) {
        super(ModEntities.IRUKANDJI_ARROW.get(), shooter, level, new ItemStack(Items.ARROW), weapon);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
