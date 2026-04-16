package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Extends {@link DinosaurMeleeAttackGoal} for the Sea Viper. Restores the
 * 1.7.10 hunger-on-hit mechanic (amplitude 0, 8 s) that fires on a 1/2 roll
 * after a successful bite. The base Params preset carries the viper's
 * aggressive swing cadence (nextInt(2)==0 || nextInt(4)==1).
 */
public class SeaViperBiteGoal extends DinosaurMeleeAttackGoal {

    public SeaViperBiteGoal(Mob mob, IntConsumer setAttacking) {
        super(mob, setAttacking, Presets.seaViper());
    }

    @Override
    protected void onSuccessfulAttack(LivingEntity target) {
        if (this.mob.getRandom().nextInt(2) == 1) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 8 * 20, 0));
        }
    }
}
