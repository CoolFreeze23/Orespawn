package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;

/** What {@code ModelRobot3.setupAnim} reads from its entity; see {@link Robot2Pose}. */
public interface Robot3Pose {
    RenderInfo getRenderInfo();

    int getAttacking();
}
