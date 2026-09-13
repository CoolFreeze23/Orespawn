package danger.orespawn.entity.pose;

/**
 * What {@code MantisModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * survey, 2026-09-14; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that switches the
 * two three-part forearms between their slow rest rhythm around -0.2 rad and their fast strike rhythm around -0.698
 * rad (orig ModelMantis.java {@code getAttacking() == 0}; the port's MantisModel.poseFrom). The classic model and
 * the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state.
 */
public interface MantisPose {
    /** orig ModelMantis.java {@code e.getAttacking()}: 0 at rest. */
    int getAttacking();
}
