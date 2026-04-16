package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Trooper Bug's 1.7.10 "leap" attack — a 1-in-10 chance each combat tick
 * (while out of melee range and on the ground) to invoke
 * {@link Mob#jumpFromGround()}, which the entity overrides to add a large
 * forward + upward impulse, clear the navigation path, and raise its Y by
 * {@code JUMP_POS_RAISE}. The leap is the defining visual for this mob —
 * "trooper" bugs behaving like paratroopers jumping onto the player.
 *
 * <p>We only leap when out of melee range so the entity doesn't pogo-jump
 * on top of the player endlessly. The navigation stop is handled by the
 * entity's {@code tick()} override which watches {@code hasImpulse}.
 */
public class TrooperBugLeapAttackGoal extends BugMeleeAttackGoal {

    private static final int LEAP_DICE = 10;
    private static final double MIN_LEAP_DIST_SQ = 16.0;
    private static final double MAX_LEAP_DIST_SQ = 64.0;

    public TrooperBugLeapAttackGoal(Mob mob, IntConsumer setAttacking) {
        super(mob, setAttacking, Params.trooperBug());
    }

    @Override
    protected boolean onCombatPhaseTick(LivingEntity target, double distSq, double reachSq) {
        if (distSq < reachSq) return false;
        if (this.mob.onGround()
                && distSq > MIN_LEAP_DIST_SQ
                && distSq < MAX_LEAP_DIST_SQ
                && this.mob.getRandom().nextInt(LEAP_DICE) == 1) {
            this.mob.getLookControl().setLookAt(target, 10.0f, 10.0f);
            this.mob.jumpFromGround();
            this.setAttacking.accept(1);
            return true;
        }
        return false;
    }
}
