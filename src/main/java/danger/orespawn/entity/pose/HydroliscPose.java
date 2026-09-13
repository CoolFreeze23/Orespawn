package danger.orespawn.entity.pose;

/**
 * What {@code HydroliscModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * lanes, 2026-09-14; the Slice 4b form of {@link HerculesBeetlePose}): the sitting check that stills the tail's
 * three-ring sway (orig ModelHydrolisc.java {@code isSitting()}; the port's HydroliscModel.poseFrom) and the health pair
 * whose fraction {@code hf = health / maxHealth} scales both the frequency and the amplitude of the three head
 * feathers (the HEALTH-FREQUENCY idiom). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface HydroliscPose {
    /** orig ModelHydrolisc.java {@code e.isSitting()} / the port's {@code entity.isInSittingPose()}: false at rest. */
    boolean isInSittingPose();

    /** orig ModelHydrolisc.java {@code e.getHealth()}: the numerator of {@code hf}; equal to the max at rest. */
    float getHealth();

    /** orig ModelHydrolisc.java {@code e.getMaxHealth()}: the denominator of {@code hf}. */
    float getMaxHealth();
}
