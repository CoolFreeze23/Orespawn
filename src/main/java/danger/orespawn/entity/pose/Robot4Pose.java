package danger.orespawn.entity.pose;

/** What {@code ModelRobot4.setupAnim} reads from and writes to its entity (ENT-K-070 shielding side effect); see {@link Robot2Pose}. */
public interface Robot4Pose {
    int getAttacking();

    void setShielding(int shielding);
}
