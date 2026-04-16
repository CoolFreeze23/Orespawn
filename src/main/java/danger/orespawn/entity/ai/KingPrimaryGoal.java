package danger.orespawn.entity.ai;

import java.util.EnumSet;

import danger.orespawn.entity.TheKing;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * The King's primary behaviour goal — flight pathing, targeting, melee,
 * and multi-phase ranged attacks all in one.
 *
 * <h2>Why one goal instead of three</h2>
 *
 * <p>A previous refactor draft split this into separate {@code KingFlightGoal},
 * {@code KingAttackGoal}, and {@code KingTargetingGoal} classes. That
 * design broke the 1.7.10 invariant that <b>wander target selection and
 * combat target selection are mutually exclusive per tick</b> — the
 * original {@code func_70030_z_} body used an if/else-if chain (see
 * {@code reference_1_7_10_source/sources/danger/orespawn/TheKing.java}
 * lines 715-855), and replicating that invariant across three separate
 * goals requires either:
 * <ul>
 *   <li>a shared per-tick "combat-active" flag on the entity (couples
 *       the goals via entity state), or</li>
 *   <li>cross-goal mutex flags that ape the if/else-if ordering (brittle
 *       and non-obvious).</li>
 * </ul>
 *
 * <p>Keeping flight + target + attack together in one goal (that
 * delegates to {@link TheKing#aiStepPrimary()}) preserves the original
 * invariant trivially and keeps the RNG-roll semantics identical. The
 * three concerns are still <i>logically</i> separated inside
 * {@code aiStepPrimary()} — see its JavaDoc for the flow diagram.</p>
 *
 * <h2>Multi-phase attack logic</h2>
 *
 * <p>Phase gating is encoded inside {@link TheKing#aiStepPrimary()} via
 * the {@code attackChance} local (5 in normal phase; 3 when
 * {@code getHealth() < maxHealth/2} OR when {@code isEnd == 2}) and the
 * per-phase damage multipliers applied on the entity in
 * {@code TheKing#tick()}. In short:</p>
 *
 * <ul>
 *   <li><b>Full HP → 2/3 HP</b>: attackChance=5, 1× damage.</li>
 *   <li><b>2/3 HP → 1/2 HP</b>: still attackChance=5 (slow cadence), but
 *       {@code TheKing#tick()} doubles melee/ranged damage.</li>
 *   <li><b>&lt; 1/2 HP</b>: attackChance=3 (faster cadence), damage
 *       multipliers cascade up to ×16 at &lt;1/8 HP.</li>
 *   <li><b>isEnd == 2 (enraged)</b>: attackChance=3, bookkeeping core
 *       pins all ranged-stream ammo to max every tick, and purple-power
 *       bombs trail the attacker.</li>
 * </ul>
 *
 * <h2>Mutex flags</h2>
 *
 * <p>Owns {@code MOVE} and {@code LOOK}. Does not own {@code JUMP}
 * because The King never jumps — {@code noPhysics = true} is always set
 * via bookkeeping, so jump state is irrelevant. Intentionally does not
 * own {@code TARGET} even though it <em>writes</em> to the entity's
 * revenge target — target acquisition runs on a different throttle
 * (~every 3-5 ticks via the attackChance RNG gate) rather than the
 * continuous target-selector tick.</p>
 *
 * <h2>Main-thread safety</h2>
 *
 * <p>The single heavy operation is {@code findSomethingToAttack()} — an
 * 80×64×80 {@code getEntitiesOfClass} AABB scan. It is naturally
 * throttled by the {@code nextInt(attackChance) == 0} gate: ~1 scan per
 * 3-5 ticks per boss in the worst case, ≤0.1ms on populated servers.
 * No async work is required.</p>
 *
 * @see TheKing#aiStepPrimary()
 * @see KingEndGameGoal
 */
public class KingPrimaryGoal extends Goal {

    private final TheKing king;

    public KingPrimaryGoal(TheKing king) {
        this.king = king;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Active whenever the boss is alive and not in end-phase-1 dialogue.
     * End-phase 2 (enraged) is NOT excluded — the enraged phase is just
     * an attribute boost layered on top of this goal's normal flow.
     */
    @Override
    public boolean canUse() {
        return this.king.isAlive() && this.king.getEndPhase() != 1;
    }

    @Override
    public boolean canContinueToUse() {
        return this.king.isAlive() && this.king.getEndPhase() != 1;
    }

    /**
     * Runs every server tick while active. Delegates to
     * {@link TheKing#aiStepPrimary()} which owns the actual logic (kept
     * on the entity because it reads/writes ~15 private fields including
     * flight target, ranged-stream counters, and the revenge target).
     */
    @Override
    public void tick() {
        this.king.aiStepPrimary();
    }
}
