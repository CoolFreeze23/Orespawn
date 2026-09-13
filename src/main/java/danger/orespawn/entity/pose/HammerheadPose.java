package danger.orespawn.entity.pose;

/**
 * What {@code ModelHammerhead.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that nods the
 * sixteen neck / head / horn / fan / ear parts on a {@code 1.3 x wingspeed} cosine x PI x 0.13 over their rest pitches
 * (the port's {@code ModelHammerhead.poseFrom} {@code entity.getAttacking() != 0}; orig ModelHammerhead.java
 * setRotationAngles). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them
 * headlessly with a declared state instead of a live entity.
 */
public interface HammerheadPose {
    int getAttacking();
}
