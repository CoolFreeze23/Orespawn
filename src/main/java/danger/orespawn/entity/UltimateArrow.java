package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the original UltimateArrow (orig UltimateArrow.java). The original
 * was a near-verbatim copy of the 1.7.10 EntityArrow.onUpdate with three
 * deltas, all ported below; everything else (ignite-on-fire, crit trail
 * particles, bow-hit sound, ground sticking) is stock arrow behavior that the
 * 1.21.1 {@link AbstractArrow} base class already provides. Punch knockback is
 * the exception: vanilla keys it on {@code #minecraft:arrows}, which this arrow
 * is ruled out of, so it is self-implemented in {@link #doKnockback}
 * (ENT-S-103). Damage = ceil(velocity x UltimateBowDamage) via {@code setBaseDamage}
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

    /**
     * ENT-S-103 (owner ruling 2026-09-04: a parity bug, fixed in classic) — Punch
     * knockback the 1.7.10 way. orig UltimateBow.java:52-54 seeded the arrow with the
     * bow's Punch level ({@code func_70240_a} = setKnockbackStrength) and orig
     * UltimateArrow.java:189-191 spent it after a successful hurt:
     * <pre>
     * if (this.knockbackStrength > 0 && (var26 = MathHelper.func_76133_a((double)(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y))) > 0.0f) {
     *     var4.field_72308_g.func_70024_g(this.field_70159_w * (double)this.knockbackStrength * (double)0.6f / (double)var26, 0.1, this.field_70179_y * (double)this.knockbackStrength * (double)0.6f / (double)var26);
     * }
     * </pre>
     * i.e. addVelocity(0.6 x level along the unit flat (x, z) flight line, +0.1 lift).
     * Vanilla 1.21.1's {@code AbstractArrow.doKnockback} takes the level from
     * {@code EnchantmentHelper.modifyKnockback}, and Punch's knockback effect requires the
     * direct attacker to be in {@code #minecraft:arrows} (data/minecraft/enchantment/punch.json),
     * which this arrow is ruled OUT of (tag ruling 2026-09-04, pinned by
     * ProjectileTypeParityTests), so the bow's self-applied Punch 2 (item/UltimateBow.java:31)
     * never landed. This override is the IrukandjiArrow shape (IrukandjiArrow.java:90-98 push,
     * :120-127 weapon read): the level comes off the firing weapon's Punch enchantment
     * ({@link #getWeaponItem}, the copy AbstractArrow keeps of the bow stack), the push is
     * 0.6 x level along the flat flight line with the 0.1 lift, raw as orig's addVelocity was
     * (no knockback-resistance factor). super is not called: it contributes 0 under the tag
     * ruling and would double the push if that ruling ever changed. Vanilla calls this only
     * after a successful hurt on a LivingEntity (AbstractArrow.onHitEntity), orig :182's gate;
     * orig :183 further required EntityLiving (1.21.1 {@link Mob}), so a player hit with
     * ultimate_sword_pvp on takes the vanilla 0.4 hurt knockback but no Punch push, as in
     * 1.7.10 (with pvp off, {@link #onHitEntity} no-sells players before this point).
     */
    @Override
    protected void doKnockback(LivingEntity target, DamageSource damageSource) {
        if (!(target instanceof Mob)) return;
        int punch = this.punchLevel();
        if (punch > 0) {
            Vec3 flat = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (flat.lengthSqr() > 0.0) {
                // orig :189-190 — 0.6 * punch along the flight line, +0.1 lift
                Vec3 push = flat.normalize().scale(punch * 0.6);
                target.push(push.x, 0.1, push.z);
            }
        }
    }

    /**
     * Punch level read from the firing weapon (orig UltimateBow.java:52-54 seeded
     * knockbackStrength from the bow's Punch); the IrukandjiArrow.java:120-127 read.
     */
    private int punchLevel() {
        ItemStack weapon = this.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(
                this.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.PUNCH),
                weapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
