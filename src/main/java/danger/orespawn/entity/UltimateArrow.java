package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Port of the original UltimateArrow (orig UltimateArrow.java). The original
 * was a near-verbatim copy of the 1.7.10 EntityArrow.onUpdate with three
 * deltas, all ported below; everything else (ignite-on-fire, punch knockback,
 * crit trail particles, bow-hit sound, ground sticking) is stock arrow
 * behavior that the 1.21.1 {@link AbstractArrow} base class already provides.
 * Damage = ceil(velocity x UltimateBowDamage) via {@code setBaseDamage}
 * (orig :157,279; config default 10, clamped 2-20, orig OreSpawnMain.java:1519-1530).
 */
public class UltimateArrow extends AbstractArrow {

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

    /**
     * orig UltimateArrow.java:134-146 — the arrow flies straight through
     * Elevators and through any ridden Cephadrome/Dragon/horse, so a shot
     * can never hit the shooter's (or anyone's) mount.
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof Elevator) return false;
        if ((target instanceof Cephadrome || target instanceof Dragon || target instanceof AbstractHorse)
                && target.isVehicle()) {
            return false;
        }
        return super.canHitEntity(target);
    }

    /**
     * orig UltimateArrow.java:158-173 — with ultimate_sword_pvp disabled
     * (the default), hitting a player, Girlfriend, Boyfriend, or any tamed
     * pet plays the bow-hit sound and heals the target 1.0 instead of
     * damaging it.
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!OreSpawnConfig.ULTIMATE_SWORD_PVP.get()
                && result.getEntity() instanceof LivingEntity living
                && (living instanceof Player || living instanceof Girlfriend || living instanceof Boyfriend
                        || (living instanceof TamableAnimal tamable && tamable.isTame()))) {
            this.playSound(SoundEvents.ARROW_HIT, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            living.heal(1.0f);
            this.discard();
            return;
        }
        super.onHitEntity(result);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
