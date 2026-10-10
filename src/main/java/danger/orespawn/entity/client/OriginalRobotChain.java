package danger.orespawn.entity.client;

import org.joml.Matrix4f;

/**
 * ENT-S-198: the Ant and Spider Robots' render chain as the original drew it (orig RenderAntRobot.renderAntRobot
 * :27-35, RenderSpiderRobot.renderSpiderRobot :27-35): from the entity's anchor, 180 - yaw about Y and the (-1, -1, 1)
 * flip, the model at the anchor. The living chain the port drew them with added a 1.501 lift after the flip, which the
 * original's renderers never took. Kept apart from the renderers (no client class) so the tests read the very matrix
 * they draw with.
 */
public final class OriginalRobotChain {
    private OriginalRobotChain() {
    }

    /** The model-to-anchor matrix of a robot drawn at {@code yaw}, the interpolated yaw the dispatcher passes. */
    public static Matrix4f modelToAnchor(float yaw) {
        return new Matrix4f().rotateY((float) Math.toRadians(180.0F - yaw)).scale(-1.0F, -1.0F, 1.0F);
    }
}
