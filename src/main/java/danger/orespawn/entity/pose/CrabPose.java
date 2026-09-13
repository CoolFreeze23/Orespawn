package danger.orespawn.entity.pose;

/**
 * What {@code ModelCrab.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks, the
 * Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that switches the eyes, mouth parts and claw tips
 * between their slow rest rhythm and their fast attack rhythm (orig ModelCrab.java:275 {@code e.getAttacking() == 0};
 * the port's ModelCrab.poseFrom). The classic model and the GeckoLib hook both pose from this, so the parity harness
 * can drive them headlessly with a declared state instead of a live entity.
 */
public interface CrabPose {
    /** orig ModelCrab.java:275 {@code getAttacking()}; rest 0 (the slow rhythm). */
    int getAttacking();
}
