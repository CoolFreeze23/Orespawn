package danger.orespawn.entity.ai;

import java.util.EnumSet;

import danger.orespawn.entity.TheQueen;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * The Queen's primary behaviour goal — flight pathing, target
 * acquisition, melee, and the two ranged streams (fireball + lightning).
 *
 * <h2>1.7.10 → 1.21.1 port</h2>
 *
 * <p>Wraps {@link TheQueen#aiStepPrimary()} which preserves the 1.7.10
 * {@code func_70030_z_} combat flow byte-for-byte minus the mood-effect
 * block (that lives in {@link QueenMoodGoal}) and the bookkeeping
 * block (that lives in {@link TheQueen#customServerAiStep()}).</p>
 *
 * <p>For the "why one goal instead of three" rationale, see
 * {@link KingPrimaryGoal} — same reasoning applies here. In summary:
 * the 1.7.10 original enforced a strict if/else-if between wander-pick
 * and combat-pick inside a single method body; splitting that into
 * multiple independent goals would require cross-goal coordination
 * state that's more complex than the current monolithic helper.</p>
 *
 * <h2>Queen-specific mechanics driven by this goal</h2>
 *
 * <ul>
 *   <li><b>Follow-The-King-when-happy</b>: if mood is 0 (happy) and
 *       there's a {@link danger.orespawn.entity.TheKing} within 64×32×64,
 *       the wander target is overridden to trail the King. Produces the
 *       "royal couple flight" pattern from 1.7.10.</li>
 *   <li><b>Happy mood suppresses revenge</b>: while happy, the revenge
 *       target is cleared at the top of target-acquisition so the
 *       Queen won't aggro mid-escort. Players have to <i>hurt her
 *       enough</i> to flip the mood via
 *       {@link TheQueen#hurt(net.minecraft.world.damagesource.DamageSource, float)}.</li>
 *   <li><b>Two elemental streams, not three</b>: fireball and lightning
 *       only — no ice stream (that's exclusive to {@link KingPrimaryGoal}).</li>
 *   <li><b>Attack-level charge loop</b>: target-size-dependent charge
 *       per successful attack tick (15-85 added per tick) feeds the
 *       {@link QueenMoodGoal} trigger threshold at 1000.</li>
 * </ul>
 *
 * <h2>Mutex flags</h2>
 *
 * <p>Owns {@code MOVE} and {@code LOOK}, matching {@link KingPrimaryGoal}.
 * Does not own {@code JUMP} (Queen has {@code noPhysics = true} so
 * jump state is irrelevant). Does not own {@code TARGET} even though
 * it writes to the revenge target — target acquisition is on an
 * independent throttle (attackChance RNG gate) rather than the
 * continuous target-selector tick.</p>
 *
 * <h2>Main-thread safety</h2>
 *
 * <p>Heaviest per-tick cost: {@link TheQueen#findSomethingToAttack()}
 * 80×64×80 AABB scan, throttled to ~1 per 3-5 ticks via the attackChance
 * gate. All other work (angle trig, ranged stream spawns) is O(1) on
 * the tick thread.</p>
 *
 * @see TheQueen#aiStepPrimary()
 * @see QueenMoodGoal
 */
public class QueenPrimaryGoal extends Goal {

    private final TheQueen queen;

    public QueenPrimaryGoal(TheQueen queen) {
        this.queen = queen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.queen.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.queen.isAlive();
    }

    @Override
    public void tick() {
        this.queen.aiStepPrimary();
    }
}
