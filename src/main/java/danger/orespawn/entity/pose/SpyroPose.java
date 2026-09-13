package danger.orespawn.entity.pose;

/**
 * What {@code SpyroModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link Robot2Pose}): the activity that halves the wing beat and stills the legs and tail while
 * flying (3), and folds the legs under the body while sitting-flat (2) (orig ModelSpyro.java:285 {@code
 * c.getActivity()}; the port's SpyroModel.poseFrom), and the sitting check that stills the tail (orig :328 {@code
 * c.func_70906_o() || current_activity == 3}; the port's {@code isInSittingPose()}). The classic model and the GeckoLib
 * hook both pose from this, so the parity harness can drive them headlessly with a declared state instead of a
 * live entity.
 */
public interface SpyroPose {
    /** orig ModelSpyro.java:285 {@code getActivity()}: 2 folds the legs, 3 (flying) halves the wings and stills legs and tail. */
    int getActivity();

    /** orig ModelSpyro.java:328 {@code func_70906_o()} (EntityTameable.isSitting). */
    boolean isInSittingPose();
}
