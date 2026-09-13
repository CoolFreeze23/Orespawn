package danger.orespawn.entity.pose;

/**
 * What {@code ModelPointysaurus.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hooks, owner 2026-09-14, addendum item 10; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that
 * lashes the tail on a 1.3 x wingspeed cosine x PI x 0.25 in place of its 0.3 x PI x 0.05 idle sway (the port's
 * {@code ModelPointysaurus.poseFrom} {@code entity.getAttacking() != 0}; orig ModelPointysaurus.java setRotationAngles).
 * The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a
 * declared state instead of a live entity.
 */
public interface PointysaurusPose {
    int getAttacking();
}
