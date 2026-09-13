package danger.orespawn.entity.pose;

/**
 * What {@code ModelSeaMonster.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that, with
 * the walking-speed threshold, opens the swimming rhythms of the tail, the fins and the neck, and snaps the jaw on a
 * 1.7 x wingspeed cosine x PI x 0.17 over 0.45 rad in place of its 0.2 x PI x 0.05 idle breath over 0.17 (the port's
 * {@code ModelSeaMonster.poseFrom} {@code entity.getAttacking() != 0}; orig ModelSeaMonster.java setRotationAngles).
 * The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a
 * declared state instead of a live entity.
 */
public interface SeaMonsterPose {
    int getAttacking();
}
