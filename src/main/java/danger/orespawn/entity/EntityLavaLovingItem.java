package danger.orespawn.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An ItemEntity that is immune to fire and lava damage.
 * Used for drops from lava-dwelling mobs (Lava Eel, etc.) so items
 * don't burn up in lava when killed.
 */
public class EntityLavaLovingItem extends ItemEntity {

    public EntityLavaLovingItem(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityLavaLovingItem(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
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
