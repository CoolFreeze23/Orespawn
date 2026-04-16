package danger.orespawn.entity.ai;

import java.util.EnumSet;
import java.util.function.IntConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Port of the 1.7.10 "melee-with-random-cadence" combat pattern used by the
 * Orespawn hostile insect family (Scorpion, Emperor Scorpion, Hercules Beetle,
 * Cater Killer, Cave Fisher, Dungeon Beast, Spit Bug, Trooper Bug, etc.).
 *
 * <p>Legacy {@code customServerAiStep} did three things on every tick:
 * <ol>
 *   <li>Every {@code cadence} ticks (via {@code nextInt(cadence) == 0}), fetch
 *       the current target;</li>
 *   <li>If in attack range, flip {@code setAttacking(1)} and roll
 *       {@code nextInt(outer) == 0 || nextInt(inner) == 1} to swing;</li>
 *   <li>Otherwise navigate toward the target at a per-mob speed.</li>
 * </ol>
 * That unusual nested-roll cadence gives the bugs their signature "charge
 * then slam" feel — we preserve it exactly in {@link #tick()} rather than
 * swapping in vanilla {@link net.minecraft.world.entity.ai.goal.MeleeAttackGoal}
 * which animates one-shot-per-second and would change gameplay.
 *
 * <p>Subclasses override {@link #onSuccessfulAttack(LivingEntity)} (for
 * poison, etc.), {@link #onOutOfMeleeRange(LivingEntity, double)} (for ranged
 * branches like Spit Bug's acid stream), or {@link #onAttackPhaseStart(LivingEntity)}
 * (for the Trooper Bug leap).
 */
public class BugMeleeAttackGoal extends Goal {

    protected final Mob mob;
    protected final IntConsumer setAttacking;
    protected final Params params;
    private LivingEntity currentTarget;
    private int navCooldown;

    public record Params(
            double navigateSpeed,
            double attackReachBonus,
            int    cadence,
            int    outerAttackRoll,
            int    innerAttackRoll,
            int    forgetTargetRoll,
            double targetSearchXZ,
            double targetSearchY) {
        public static Params scorpion()        { return new Params(1.2, 3.0, 6, 5, 6,   0,  8.0, 3.0); }
        public static Params emperorScorpion() { return new Params(1.2, 6.0, 4, 4, 6, 100, 24.0, 6.0); }
        public static Params herculesBeetle()  { return new Params(1.2, 5.0, 4, 3, 4,   0, 16.0, 6.0); }
        public static Params caterKiller()     { return new Params(1.25, 5.0, 4, 3, 4, 200, 20.0, 8.0); }
        public static Params caveFisher()      { return new Params(1.2, 2.83, 8, 7, 8,  0, 10.0, 3.0); }
        public static Params dungeonBeast()    { return new Params(1.2, 2.83, 8, 7, 0,  0, 16.0, 3.0); }
        public static Params spitBug()         { return new Params(0.5, 3.0, 5, 6, 7,   0, 12.0, 7.0); }
        public static Params trooperBug()      { return new Params(1.2, 5.0, 5, 6, 7,   0, 12.0, 7.0); }
    }

    public BugMeleeAttackGoal(Mob mob, IntConsumer setAttacking, Params params) {
        this.mob = mob;
        this.setAttacking = setAttacking;
        this.params = params;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        this.currentTarget = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        this.currentTarget = target;
        return true;
    }

    @Override
    public void start() {
        this.navCooldown = 0;
    }

    @Override
    public void stop() {
        this.currentTarget = null;
        this.setAttacking.accept(0);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.currentTarget;
        if (target == null || !target.isAlive()) {
            this.setAttacking.accept(0);
            return;
        }

        if (this.params.forgetTargetRoll() > 0
                && this.mob.getRandom().nextInt(this.params.forgetTargetRoll()) == 0) {
            this.mob.setTarget(null);
            this.currentTarget = null;
            this.setAttacking.accept(0);
            return;
        }

        this.mob.getLookControl().setLookAt(target, 10.0f, 10.0f);

        if (this.mob.getRandom().nextInt(this.params.cadence()) != 0) return;

        double distSq   = this.mob.distanceToSqr(target);
        double reach    = this.params.attackReachBonus() + target.getBbWidth() / 2.0;
        double reachSq  = reach * reach;

        if (this.onCombatPhaseTick(target, distSq, reachSq)) return;

        if (distSq < reachSq) {
            this.setAttacking.accept(1);
            this.onAttackPhaseStart(target);
            if (this.mob.getRandom().nextInt(this.params.outerAttackRoll()) == 0
                    || this.mob.getRandom().nextInt(this.params.innerAttackRoll()) == 1) {
                boolean hit = this.mob.doHurtTarget(target);
                if (hit) this.onSuccessfulAttack(target);
            }
        } else {
            this.setAttacking.accept(0);
            this.onOutOfMeleeRange(target, distSq);
            if (--this.navCooldown <= 0) {
                this.mob.getNavigation().moveTo(target, this.params.navigateSpeed());
                this.navCooldown = this.params.cadence() * 2;
            }
        }
    }

    /**
     * Called just before the mob rolls its swing dice. Subclasses return true
     * to skip the rest of the default melee logic (e.g. if they chose to leap
     * or fire a projectile this tick).
     */
    protected boolean onCombatPhaseTick(LivingEntity target, double distSq, double reachSq) {
        return false;
    }

    /** Fires each tick the mob is within reach, before the swing roll. */
    protected void onAttackPhaseStart(LivingEntity target) {}

    /** Fires once per successful melee swing (for poison, bleed, etc.). */
    protected void onSuccessfulAttack(LivingEntity target) {}

    /** Fires each cadence tick while the mob is outside melee reach. */
    protected void onOutOfMeleeRange(LivingEntity target, double distSq) {}
}
