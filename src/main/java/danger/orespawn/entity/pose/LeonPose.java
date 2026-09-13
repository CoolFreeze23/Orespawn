package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;

/**
 * What {@code LeonModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * survey, 2026-09-14; the Slice 4b form of {@link Robot2Pose} and {@link RotatorPose}): the activity that selects
 * the standing or the flying part set (orig ModelLeon.java:729/852 {@code getActivity() == 0}; the
 * port's LeonModel.poseFrom), the attacking flag that speeds and widens the flight beat and opens the flying
 * jaw, the being-ridden flag that stills the flight bob and swaps the head yaw source, the sitting check that
 * stills the standing wing sway, the per-entity render scratch whose {@code rf1} ACCUMULATES the ridden head yaw
 * across frames (orig ModelLeon.java:1013-1024, orig Leon.java:64; ENT-S-093 - the Rotator's precedent for
 * per-frame RenderInfo accumulation) and the yaw pair that feeds it ({@code prevRotationYaw - rotationYaw}, orig
 * :1014). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state.
 */
public interface LeonPose {
    /** orig ModelLeon.java:729 {@code e.getActivity()}: 0 at rest (standing; any other value the flying set). */
    int getActivity();

    /** orig ModelLeon.java {@code e.getAttacking()}: 0 at rest. */
    int getAttacking();

    /** orig ModelLeon.java {@code e.getBeingRidden()}: 0 at rest (no rider). */
    int getBeingRidden();

    /** orig ModelLeon.java {@code e.isSitting()} / the port's {@code entity.isInSittingPose()}: false at rest. */
    boolean isInSittingPose();

    /** orig Leon.java:176-178 {@code getRenderInfo()}: the ridden head-yaw accumulator ({@code rf1}), 0 at rest. */
    RenderInfo getRenderInfo();

    /** orig ModelLeon.java:1014 {@code e.rotationYaw} / the port's {@code Entity.getYRot()}: 0 at rest. */
    float getYRot();

    /** orig ModelLeon.java:1014 {@code e.prevRotationYaw} / the port's {@code Entity.yRotO} through a one-line delegate: 0 at rest. */
    float getYRotO();
}
