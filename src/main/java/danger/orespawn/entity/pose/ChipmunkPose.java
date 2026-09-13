package danger.orespawn.entity.pose;

/**
 * What {@code ModelChipmunk.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the sitting flag that stills the tail -
 * orig ModelChipmunk.java:168 {@code if (!c.func_70906_o())} (EntityTameable.isSitting; the port's
 * {@code TamableAnimal.isInSittingPose()}). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface ChipmunkPose {
    /** orig ModelChipmunk.java:168 {@code func_70906_o()}; rest false (the tail wags). */
    boolean isInSittingPose();
}
