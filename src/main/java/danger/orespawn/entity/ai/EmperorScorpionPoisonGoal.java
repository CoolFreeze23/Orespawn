package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Emperor Scorpion's 1.7.10 melee attack had a 1-in-3 chance to apply
 * {@link MobEffects#POISON} for 90 ticks (4.5 seconds) on hit. This goal
 * reproduces that by extending {@link BugMeleeAttackGoal} — the per-hit
 * poison dice is rolled inside {@link #onSuccessfulAttack(LivingEntity)}
 * rather than inside {@code doHurtTarget} so it only fires when the
 * vanilla damage event actually lands (respecting invulnerability frames,
 * shields, armor tests, etc.).
 */
public class EmperorScorpionPoisonGoal extends BugMeleeAttackGoal {

    private static final int POISON_DICE = 3;
    private static final int POISON_DURATION_TICKS = 90;
    private static final int POISON_AMPLIFIER = 0;

    public EmperorScorpionPoisonGoal(Mob mob, IntConsumer setAttacking) {
        super(mob, setAttacking, Params.emperorScorpion());
    }

    @Override
    protected void onSuccessfulAttack(LivingEntity target) {
        if (this.mob.getRandom().nextInt(POISON_DICE) == 1) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON, POISON_DURATION_TICKS, POISON_AMPLIFIER));
        }
    }
}
