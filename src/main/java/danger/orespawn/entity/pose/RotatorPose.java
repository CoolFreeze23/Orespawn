package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;

/**
 * What {@code RotatorModel.poseFrom} reads from its entity: the per-entity
 * render scratch whose {@code rf1} is the accumulating gyroscope fan angle
 * (orig ModelRotator.java:51, 75-78). The classic model and the GeckoLib hook
 * both pose from this, so the parity harness can drive them headlessly from a
 * declared {@code rf1} preset instead of a live entity; see {@link Robot2Pose}.
 */
public interface RotatorPose {
    RenderInfo getRenderInfo();
}
