package danger.orespawn.entity.pose;

/**
 * What {@code ModelTRex.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks, the
 * Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that opens the jaw on a 0.45 cosine x PI x 0.18 over
 * 0.52 rad in place of its 0.1 rest (the port's {@code ModelTRex.poseFrom} {@code entity.getAttacking() !=
 * 0}; orig ModelTRex.java setRotationAngles). The classic model and the GeckoLib hook both pose from this, so the
 * parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface TRexPose {
    int getAttacking();
}
