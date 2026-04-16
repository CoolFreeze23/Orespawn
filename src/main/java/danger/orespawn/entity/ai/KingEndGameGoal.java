package danger.orespawn.entity.ai;

import java.util.EnumSet;

import danger.orespawn.entity.TheKing;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Goal that plays the "Prepare to die!" end-phase-1 dialogue cutscene.
 *
 * <h2>1.7.10 → 1.21.1 port</h2>
 *
 * <p>In the 1.7.10 original
 * ({@code reference_1_7_10_source/sources/danger/orespawn/TheKing.java}
 * lines 611-709), the dialogue sequence was an if/else chain inside
 * {@code func_70030_z_} gated on {@code field_isEnd == 1}. It froze the
 * boss's motion, pinned the nearest player to face the boss, and emitted
 * one scripted chat line every 20 ticks for 500 ticks before flipping
 * {@code isEnd = 2} (enraged phase).</p>
 *
 * <p>In 1.21.1 we lift the entire block into this Goal class so that the
 * vanilla {@link net.minecraft.world.entity.ai.goal.GoalSelector} can
 * preempt the normal {@link KingPrimaryGoal} behaviour via standard
 * priority + mutex-flag arbitration. Concretely:
 * <ul>
 *   <li><b>Priority 0</b> — wins over {@link KingPrimaryGoal} (priority 1)
 *       whenever {@link #canUse()} returns true.</li>
 *   <li><b>Flags MOVE | LOOK | JUMP</b> — full mutex lock; while this goal
 *       is ticking, nothing else can set motion, yaw, pitch, or jump state
 *       on the entity. Without this lock the smooth-motion lerp would
 *       keep running and the cutscene wouldn't read as a cutscene.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 *
 * <p>Runs on the server tick thread via
 * {@code GoalSelector.tick()}. Sends chat messages via
 * {@link TheKing#aiStepEndGameDialogue()} which delegates to
 * {@code TheKing#msgToPlayers} — those internally use
 * {@link net.minecraft.server.level.ServerLevel#players()}
 * which is a copy-on-read snapshot (safe to iterate on the tick thread).</p>
 *
 * @see KingPrimaryGoal
 * @see TheKing#aiStepEndGameDialogue()
 */
public class KingEndGameGoal extends Goal {

    private final TheKing king;

    public KingEndGameGoal(TheKing king) {
        this.king = king;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    /**
     * Activate only during end-phase 1 (scripted dialogue). End-phase 2
     * (enraged) is NOT owned by this goal — it's just a config-override
     * state that {@link KingPrimaryGoal} continues to drive with faster
     * attack cadence and max ammunition.
     */
    @Override
    public boolean canUse() {
        return this.king.getEndPhase() == 1;
    }

    @Override
    public boolean canContinueToUse() {
        return this.king.getEndPhase() == 1;
    }

    /**
     * Delegates to {@link TheKing#aiStepEndGameDialogue()} which contains
     * the per-tick counter + dialogue-line emission. Kept as a one-liner
     * here so the dialogue strings stay with the entity (where they can
     * reference private state) and the goal stays behavioural.
     */
    @Override
    public void tick() {
        this.king.aiStepEndGameDialogue();
    }
}
