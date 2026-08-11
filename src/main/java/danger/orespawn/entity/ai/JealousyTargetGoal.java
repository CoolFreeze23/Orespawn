package danger.orespawn.entity.ai;

import danger.orespawn.OreSpawnConfig;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Port of the original MyEntityAIJealousy (orig MyEntityAIJealousy.java) —
 * a tamed, non-sitting companion with an owner hunts nearby UNTAMED rivals
 * of its own kind (a tamed victim is never targeted, orig :43-45; the goal
 * bails without a tame + owner, orig :31-36,47-48). Registered twice per
 * companion with a near/frequent and far/rare profile (orig
 * Boyfriend.java:146-147 / Girlfriend.java:170-171: range 6 chance 5, and
 * range 3 chance 15), both inside the original's {@code PlayNicely == 0}
 * gate — checked dynamically here so config flips apply live.
 *
 * <p>The custom range overrides {@link #getFollowDistance()} because the
 * original MyEntityAINearestAttackableTarget carried a per-goal
 * targetDistance instead of reading follow-range (orig :21).</p>
 */
public class JealousyTargetGoal<T extends TamableAnimal> extends NearestAttackableTargetGoal<T> {

    private final TamableAnimal jealousOwner;
    private final double targetDistance;

    public JealousyTargetGoal(TamableAnimal owner, Class<T> rivalClass, double targetDistance, int chance) {
        super(owner, rivalClass, chance, true, false,
                rival -> !(rival instanceof TamableAnimal tamedRival && tamedRival.isTame()));
        this.jealousOwner = owner;
        this.targetDistance = targetDistance;
    }

    @Override
    protected double getFollowDistance() {
        return this.targetDistance;
    }

    @Override
    public boolean canUse() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return false;
        if (!this.jealousOwner.isTame() || this.jealousOwner.isOrderedToSit()) return false;
        if (this.jealousOwner.getOwner() == null) return false;
        return super.canUse();
    }
}
