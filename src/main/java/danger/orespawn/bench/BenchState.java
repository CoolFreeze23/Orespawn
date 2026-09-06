package danger.orespawn.bench;

import java.util.Locale;

/**
 * Phase G slice (d): the mob state a scene is spawned in. {@link #IDLE} is the protocol's fixed
 * state (no AI: the mob stands where it was placed -- {@code Mob.isEffectiveAi()} is false, so
 * {@code serverAiStep} runs no goals and, although {@code LivingEntity.aiStep} still calls
 * {@code travel} every tick, the body of {@code travel} sits behind
 * {@code isControlledByLocalInstance()} = {@code isEffectiveAi()} and never applies gravity or a
 * move; the spawner's heightmap-top placement is what puts the mob on the ground);
 * {@link #WANDER} leaves the AI on for the owner's looks.
 */
public enum BenchState {
    IDLE(true),
    WANDER(false);

    private final boolean noAi;

    BenchState(boolean noAi) {
        this.noAi = noAi;
    }

    public boolean noAi() {
        return this.noAi;
    }

    public static BenchState parse(String token) {
        if (token == null) {
            return null;
        }
        String key = token.trim().toUpperCase(Locale.ROOT);
        for (BenchState state : values()) {
            if (state.name().equals(key)) {
                return state;
            }
        }
        return null;
    }
}
