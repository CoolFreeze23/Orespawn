package danger.orespawn.entity.pose;

/**
 * What {@code VelocityRaptorModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hooks, owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the health fraction
 * {@code getHealth() / getMaxHealth()} that scales both the rate and the amplitude of the head-feather sway and the
 * tail-feather beat (the HEALTH-FREQUENCY idiom; orig ModelVelocityRaptor.java:274 {@code hf = c.func_110143_aJ() /
 * c.func_110138_aP()}; the port's VelocityRaptorModel.poseFrom), and the sitting check that stills the tail feathers
 * (orig :298 {@code c.func_70906_o()}; the port's {@code isInSittingPose()}). The classic model and the GeckoLib hook
 * both pose from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface VelocityRaptorPose {
    /** orig ModelVelocityRaptor.java:274 {@code func_110143_aJ()} (getHealth). */
    float getHealth();

    /** orig ModelVelocityRaptor.java:274 {@code func_110138_aP()} (getMaxHealth). */
    float getMaxHealth();

    /** orig ModelVelocityRaptor.java:298 {@code func_70906_o()} (EntityTameable.isSitting). */
    boolean isInSittingPose();
}
