package danger.orespawn.entity.pose;

/**
 * What {@code ModelEnderReaper.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link HerculesBeetlePose}): the screaming flag that swings the scythe, raises the left arm and
 * puts the wings on their fast 2.7 ws beat (orig ModelEnderReaper.java:492 {@code if (e.isScreaming())}; the port's
 * synched {@code DATA_SCREAMING}, its held aggro state). The classic model and the GeckoLib hook both pose from this, so
 * the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface EnderReaperPose {
    /** orig ModelEnderReaper.java:492 {@code isScreaming()}; rest false (the scythe on its walk fold, the wings' slow beat). */
    boolean isScreaming();
}
