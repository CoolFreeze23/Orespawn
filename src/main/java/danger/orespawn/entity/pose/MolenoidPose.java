package danger.orespawn.entity.pose;

/**
 * What {@code MolenoidModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the attacking flag that switches the
 * arm chain between the 1.7 x wingspeed attack swing (0.25 x PI, no gait scale) and the walking gait (1.3 x wingspeed
 * x 0.25 x PI x limbSwingAmount above the 0.1 threshold) (orig ModelMolenoid.java:286 {@code e.getAttacking() != 0};
 * the port's MolenoidModel.poseFrom). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface MolenoidPose {
    int getAttacking();
}
