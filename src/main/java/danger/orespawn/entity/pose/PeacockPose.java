package danger.orespawn.entity.pose;

/**
 * What {@code ModelPeacock.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link HerculesBeetlePose}): the display counter that fans the three head feathers and the seven
 * tail feathers out while positive (the port's {@code ModelPeacock.poseFrom} {@code entity.getBlink() > 0};
 * orig ModelPeacock.java setRotationAngles). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface PeacockPose {
    int getBlink();
}
