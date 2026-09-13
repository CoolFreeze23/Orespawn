package danger.orespawn.entity.pose;

/**
 * What {@code ModelGiantRobot.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that switches
 * the shoulders from the hip's negated sway to the punch twist and the two arm pairs from the thigh angles to the
 * windmill punch (orig ModelGiantRobot.java:230-240 {@code getAttacking() != 0}; the port's
 * {@code ModelGiantRobot.poseFrom}). The classic model and the GeckoLib hook both pose from this, so the parity harness
 * can drive them headlessly with a declared state instead of a live entity. The Jeffery extends the Giant Robot and so
 * satisfies it too.
 */
public interface GiantRobotPose {
    int getAttacking();
}
