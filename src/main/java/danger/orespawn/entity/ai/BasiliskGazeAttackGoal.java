package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Extends {@link DinosaurMeleeAttackGoal} with the Basilisk's signature
 * debilitating "gaze" — an aura of Slowness V applied to any target that
 * enters its 6-block reach, plus a chance of Poison on a successful bite.
 *
 * <p>Fidelity to 1.7.10 {@code Basilisk.func_70619_bc} + {@code func_70652_k}:
 * <ul>
 *   <li>While in melee reach, the target gets {@code Slowness amplifier 5}
 *       for 100 ticks (5 s) regardless of whether the bite lands — this is
 *       the legacy "aura" emitted every cadence tick.</li>
 *   <li>On a successful bite, a 1/3 roll applies {@code Poison amplifier 0}
 *       for a difficulty-scaled duration (8/10/12/14 s on peaceful/easy/
 *       normal/hard — peaceful is skipped by the spawn rules anyway).</li>
 * </ul>
 * The 1.7.10 Basilisk has <b>no projectile attack</b>. Its "ranged" flavour
 * is entirely this slow-aura; a true projectile would be new content and a
 * fidelity break, so it is intentionally not added here.
 */
public class BasiliskGazeAttackGoal extends DinosaurMeleeAttackGoal {

    public BasiliskGazeAttackGoal(Mob mob, IntConsumer setAttacking) {
        super(mob, setAttacking, Presets.basilisk());
    }

    @Override
    protected void onAttackPhaseStart(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5));
    }

    @Override
    protected void onSuccessfulAttack(LivingEntity target) {
        if (this.mob.getRandom().nextInt(3) != 0) return;
        int durationSeconds = switch (this.mob.level().getDifficulty()) {
            case HARD   -> 14;
            case NORMAL -> 12;
            case EASY   -> 10;
            default     -> 8;
        };
        target.addEffect(new MobEffectInstance(MobEffects.POISON, durationSeconds * 20, 0));
    }
}
