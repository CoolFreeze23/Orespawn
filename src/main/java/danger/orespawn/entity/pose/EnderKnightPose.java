package danger.orespawn.entity.pose;

/**
 * What {@code ModelEnderKnight.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link HerculesBeetlePose}): the screaming flag that raises the arms and the blade into the fast
 * 2.7 ws swing (orig ModelEnderKnight.java:332 {@code if (e.isScreaming())}; the port's synched {@code DATA_SCREAMING},
 * its held aggro state). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive
 * them headlessly with a declared state instead of a live entity.
 */
public interface EnderKnightPose {
    /** orig ModelEnderKnight.java:332 {@code isScreaming()}; rest false (the arms and blade at their guard). */
    boolean isScreaming();
}
