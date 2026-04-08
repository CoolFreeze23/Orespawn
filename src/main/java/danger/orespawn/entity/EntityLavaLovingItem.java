package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An ItemEntity that is immune to fire and lava damage.
 * Registered as a custom entity type with fireImmune() so both the
 * entity and its item stack survive indefinitely in lava.
 *
 * Used for drops from fire-immune mobs (Dragon, etc.) to prevent
 * valuable loot from burning up when the mob dies in or near lava.
 */
public class EntityLavaLovingItem extends ItemEntity {

    public EntityLavaLovingItem(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityLavaLovingItem(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.LAVA_LOVING_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setDeltaMovement(level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
