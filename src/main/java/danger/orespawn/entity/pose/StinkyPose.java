package danger.orespawn.entity.pose;

/**
 * What {@code StinkyModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the activity that folds the four legs
 * under the body at 2 (orig ModelStinky.java:166 {@code c.getActivity()}; the port's StinkyModel.poseFrom), and the
 * sitting check that stills the tail (orig :185 {@code c.func_70906_o()}; the port's {@code isInSittingPose()}). The
 * classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a
 * declared state instead of a live entity.
 */
public interface StinkyPose {
    /** orig ModelStinky.java:166 {@code getActivity()}: 2 folds the legs (+-1 rad), anything else walks. */
    int getActivity();

    /** orig ModelStinky.java:185 {@code func_70906_o()} (EntityTameable.isSitting). */
    boolean isInSittingPose();
}
