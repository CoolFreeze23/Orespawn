package danger.orespawn.entity.pose;

/**
 * What {@code RatModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks, the
 * Slice 4b form of {@link Robot2Pose}): the attacking flag that switches the tail between its fast wide thrash (1.5
 * rad/tick, 0.25 x PI) and its gentle idle sway (0.4, 0.05 x PI) (orig ModelRat.java:116 {@code
 * r.getAttacking() != 0}; the port's RatModel.poseFrom). The classic model and the GeckoLib hook both pose from
 * this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 *
 */
public interface RatPose {
    int getAttacking();
}
