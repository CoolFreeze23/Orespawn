package danger.orespawn.entity.pose;

/**
 * What {@code ModelUrchin.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link Robot2Pose}): the attacking flag that switches the center spin and the eight spine
 * rhythms between their slow rest rates (0.02 / 0.07 ... x wingspeed, amplitude 0.02 x PI) and their fast attack rates
 * (0.2 / 0.7 ..., amplitude 0.06 x PI) (orig ModelUrchin.java:175 {@code u.getAttacking() != 0};
 * the port's ModelUrchin.poseFrom). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface UrchinPose {
    int getAttacking();
}
