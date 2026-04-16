package danger.orespawn.entity.ai;

import danger.orespawn.entity.TheQueen;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * The Queen's mood-discharge goal. Fires when
 * {@link TheQueen#getAttackLevel()} exceeds 1000; dispatches either a
 * terraforming + butterfly burst (happy mood) or a PurplePower barrage
 * (mad mood); resets attack-level to 1 before yielding.
 *
 * <h2>1.7.10 → 1.21.1 port</h2>
 *
 * <p>In 1.7.10 this was a 90-line block at the top of
 * {@code TheQueen.func_70030_z_()} ({@code reference_1_7_10_source/sources/danger/orespawn/TheQueen.java}
 * lines 488-578) gated on {@code field_attackLevel > 1000}. Moving it
 * into a dedicated Goal removes the coupling with the flight+attack
 * code below it and lets us give the effect its own priority-0 slot
 * (so it fires <i>before</i> the primary goal each tick it activates).</p>
 *
 * <h2>Mutex flags</h2>
 *
 * <p>Intentionally empty — the mood effect is a pure one-shot emitter
 * (block updates + entity spawns) that does not influence movement,
 * rotation, or jump state. Running alongside
 * {@link QueenPrimaryGoal} in the same tick is desired: the flight
 * lerp keeps moving while the effect fires, which matches the 1.7.10
 * feel (Queen doesn't pause for her own buff).</p>
 *
 * <h2>Single-tick lifecycle</h2>
 *
 * <p>{@link #canUse()} returns true only while {@code attackLevel > 1000}.
 * The effect method {@link TheQueen#aiStepMoodEffects()} resets it to 1
 * before returning, so this goal naturally deactivates after one tick.
 * {@link #canContinueToUse()} returns false for the same reason — we
 * want the goal to fully cycle (canUse → start → tick → stop) rather
 * than persist, so the GoalSelector re-evaluates it from scratch each
 * time the threshold is crossed.</p>
 *
 * <h2>Main-thread cost</h2>
 *
 * <p>The happy-mood branch is the heaviest single operation in the
 * boss framework: up to 25 outer iterations × up to 40 inner
 * iterations = 1000 {@link net.minecraft.world.level.block.state.BlockState}
 * lookups, plus up to 25 {@code setBlockAndUpdate} writes and 10
 * butterfly entity spawns. However, the effect fires at most once per
 * ~100 server ticks (attackLevel needs 1000 charge at +10-85/tick). The
 * amortised cost is &lt;1ms/sec per boss, which is within budget for
 * vanilla entity AI. No async offload needed.</p>
 *
 * @see TheQueen#aiStepMoodEffects()
 */
public class QueenMoodGoal extends Goal {

    private final TheQueen queen;

    public QueenMoodGoal(TheQueen queen) {
        this.queen = queen;
        // No mutex flags — runs alongside QueenPrimaryGoal.
    }

    @Override
    public boolean canUse() {
        return this.queen.isAlive() && this.queen.getAttackLevel() > 1000;
    }

    @Override
    public boolean canContinueToUse() {
        // One-shot; tick resets attackLevel to 1 so this returns false
        // on the following tick anyway.
        return false;
    }

    @Override
    public void tick() {
        this.queen.aiStepMoodEffects();
    }
}
