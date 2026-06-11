package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Extends {@link DinosaurMeleeAttackGoal} for the Sea Viper. Restores the
 * 1.7.10 poison-on-hit mechanic that fires on a 1/2 roll after a successful
 * bite. The base Params preset carries the viper's aggressive swing cadence
 * (nextInt(2)==0 || nextInt(4)==1).
 */
public class SeaViperBiteGoal extends DinosaurMeleeAttackGoal {

    public SeaViperBiteGoal(Mob mob, IntConsumer setAttacking) {
        super(mob, setAttacking, Presets.seaViper());
    }

    @Override
    protected void onSuccessfulAttack(LivingEntity target) {
        // orig SeaViper.java:337,347-356 — POISON (field_76436_u) var2*20 ticks on a 1/2
        // roll; var2 = 6, bumped to 8 on EASY (the NORMAL/HARD branches are nested
        // inside the EASY check and unreachable — decompile quirk preserved).
        int seconds = this.mob.level().getDifficulty() == Difficulty.EASY ? 8 : 6;
        if (this.mob.getRandom().nextInt(2) == 1) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0));
        }
    }
}
