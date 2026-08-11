package danger.orespawn.entity;

import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.entity.gait.ModernSpiderGait;

/**
 * 2.0 spider overhaul (S5b): the shared surface of the legged robots the
 * modern gait system drives ({@link SpiderRobot}, {@link AntRobot}). The
 * gait payload handlers and tracking events accept either robot through
 * this interface; each implementor keeps its own rig
 * ({@code SpiderRigProfile.RIG} / {@code AntRigProfile.RIG}), its own
 * construction-snapshot mode gate, and its own classic path untouched.
 */
public interface IModernLeggedRobot {

    /**
     * The modern gait controller, or {@code null} for classic-mode robots.
     * On the client the controller materializes lazily once the synced
     * mode flag says modern.
     */
    ModernSpiderGait getModernGait();

    /** True when this robot runs the modern gait (server snapshot; synced). */
    boolean isModernMovement();

    /** The classic render-info block both robots' models consume. */
    RenderSpiderRobotInfo getRenderSpiderRobotInfo();
}
