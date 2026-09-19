package danger.orespawn.entity.client.animation;

/**
 * The contract's locomotion weights for one frame (contract sections 2.1 and 3.1; weights): the products
 * of {@code w_move = limbSwingAmount} (continuous, no threshold) and the three ramped booleans, with the
 * priority {@code fly > swim > walk / idle} built into the products -
 * <pre>
 *   w_fly   = flyRamp
 *   w_swim  = swimRamp x (1 - flyRamp)
 *   w_walk  = w_move x (1 - swimRamp) x (1 - flyRamp)
 *   w_idle  = (1 - w_move) x (1 - swimRamp) x (1 - flyRamp)
 *   w_aggro_idle = w_idle x aggroRamp;  w_calm_idle = w_idle x (1 - aggroRamp)
 * </pre>
 * which sum to one on every frame, so the layers that carry them compose to a convex blend of the clips. The
 * gait group's {@code walk} carries {@code w_walk} as its whole factor: its {@code limbSwingAmount} is P3's speed
 * scaling AND {@code w_move} at once - one factor, the (e) demonstration's blend {@code walk x a + idle x (1 - a)}
 * ({@code demo_results.json} G) - never squared (decided under doctrine, the weights slice; a second factor would
 * shrink the stride as the square of the speed against the classic's linear one).
 *
 * @param move      {@code limbSwingAmount} this frame
 * @param swimRamp  the {@code inWater} ramp's weight this frame
 * @param flyRamp   the {@code flying} ramp's weight this frame
 * @param aggroRamp the {@code attacking} (STATE) ramp's weight this frame
 */
public record LocomotionWeights(float move, float swimRamp, float flyRamp, float aggroRamp) {
    /** Standing still on the ground, calm: {@code w_idle = 1}. */
    public static final LocomotionWeights REST = new LocomotionWeights(0.0F, 0.0F, 0.0F, 0.0F);

    public LocomotionWeights {
        check("move", move);
        check("swimRamp", swimRamp);
        check("flyRamp", flyRamp);
        check("aggroRamp", aggroRamp);
    }

    private static void check(String name, float value) {
        if (!(value >= 0.0F && value <= 1.0F)) {
            throw new IllegalArgumentException("locomotion weight " + name + " must lie in [0, 1]: " + value);
        }
    }

    public float fly() {
        return this.flyRamp;
    }

    public float swim() {
        return this.swimRamp * (1.0F - this.flyRamp);
    }

    public float walk() {
        return this.move * (1.0F - this.swimRamp) * (1.0F - this.flyRamp);
    }

    public float idle() {
        return (1.0F - this.move) * (1.0F - this.swimRamp) * (1.0F - this.flyRamp);
    }

    public float aggroIdle() {
        return idle() * this.aggroRamp;
    }

    public float calmIdle() {
        return idle() * (1.0F - this.aggroRamp);
    }
}
