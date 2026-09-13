package danger.orespawn.entity.pose;

/**
 * What {@code ModelGazelle.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks, owner
 * 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the crouch that stills the tail - orig
 * ModelGazelle.java:297 {@code if (!g.func_70906_o())} (EntityTameable.isSitting there; the port's classic reads
 * {@code Entity.isCrouching()}, and the port's model is what the hook transcribes). The classic model and the GeckoLib hook
 * both pose from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface GazellePose {
    /** orig ModelGazelle.java:297 {@code func_70906_o()} as the port reads it ({@code isCrouching()}); rest false (the tail sways). */
    boolean isCrouching();
}
