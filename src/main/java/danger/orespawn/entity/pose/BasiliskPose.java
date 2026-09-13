package danger.orespawn.entity.pose;

/**
 * What {@code ModelBasilisk.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * survey, 2026-09-14; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that opens the jaw on
 * its 0.45 rhythm around -1.0 rad (-1.1 rad closed at rest) (orig ModelBasilisk.java {@code getAttacking() != 0};
 * the port's ModelBasilisk.poseFrom). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state.
 */
public interface BasiliskPose {
    /** orig ModelBasilisk.java {@code e.getAttacking()}: 0 at rest. */
    int getAttacking();
}
