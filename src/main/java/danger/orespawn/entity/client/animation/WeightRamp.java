package danger.orespawn.entity.client.animation;

/**
 * One smoothed boolean of the standard animation contract ({@code inWater}, {@code flying}, {@code attacking};
 * contract section 3.1): a weight in {@code [0, 1]} that moves toward the boolean's value at {@code 1 / 5} per tick
 * from wherever it stood when the boolean last flipped - a five-tick (0.25 s) ramp per direction, a flip mid-ramp
 * turning back from the value it had reached. The ramp is a FUNCTION OF {@code ageInTicks} SINCE THE FLIP, never of
 * the number of calls (ENT-S-147; the weights slice): a repeated frame (the pause screen, a shadow pass, a
 * duplicate partial) recomputes the same weight, a skipped frame is caught up. Stored per manager, in the manager's
 * extra data ({@link ContractLayers#RAMPS}).
 *
 * <p>The first advance sets the weight to the boolean's value outright: a manager built for an entity already in
 * flight (entering render distance, a resource reload, OPT-029's re-entry) starts on its {@code fly} loop, not on a
 * ramp from the ground - the late-prime rule of the phase-locked layers (ANIM-001) in the weights' terms.</p>
 */
public final class WeightRamp {
    /** Contract section 3.1: the ramp per direction, in ticks. */
    public static final float RAMP_TICKS = 5.0F;

    private boolean target;
    private float valueAtFlip;
    private float flipAge = Float.NaN;
    private float value;

    /** The weight for {@code ageInTicks} given the boolean's value THIS frame; idempotent for a repeated frame. */
    public float advance(boolean input, float ageInTicks) {
        if (!Float.isFinite(ageInTicks)) {
            throw new IllegalArgumentException("a weight ramp needs a finite age");
        }
        if (Float.isNaN(this.flipAge)) {
            this.target = input;
            this.value = input ? 1.0F : 0.0F;
            this.valueAtFlip = this.value;
            this.flipAge = ageInTicks;
            return this.value;
        }
        if (input != this.target) {
            this.valueAtFlip = this.value;
            this.flipAge = ageInTicks;
            this.target = input;
        }
        float elapsed = Math.max(0.0F, ageInTicks - this.flipAge);
        float travelled = elapsed / RAMP_TICKS;
        this.value = this.target
                ? Math.min(1.0F, this.valueAtFlip + travelled)
                : Math.max(0.0F, this.valueAtFlip - travelled);
        return this.value;
    }

    /** The weight at the last advance (0 before any). */
    public float value() {
        return this.value;
    }

    /** The boolean's value at the last advance. */
    public boolean target() {
        return this.target;
    }

    /** The age at which the boolean last flipped (or the first advance); NaN before any advance. */
    public float flipAge() {
        return this.flipAge;
    }
}
