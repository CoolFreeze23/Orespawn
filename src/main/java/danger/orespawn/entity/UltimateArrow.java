package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class UltimateArrow extends AbstractArrow {

    // orig UltimateArrow.java:157,279 — impact damage = ceil(velocity × UltimateBowDamage),
    // where UltimateBowDamage is the config value (default 10, clamped 2-20,
    // orig OreSpawnMain.java:1519-1530). AbstractArrow applies the same
    // ceil(speed × baseDamage) formula, so base damage = the config value.
    private static double baseDamage() {
        return OreSpawnConfig.ULTIMATE_BOW_DAMAGE.get();
    }

    public UltimateArrow(EntityType<? extends UltimateArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(baseDamage());
    }

    public UltimateArrow(Level level, LivingEntity shooter, ItemStack weapon) {
        super(ModEntities.ULTIMATE_ARROW.get(), shooter, level, new ItemStack(Items.ARROW), weapon);
        this.setBaseDamage(baseDamage());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
