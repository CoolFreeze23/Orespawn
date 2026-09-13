package danger.orespawn.entity.pose;

/**
 * What {@code LizardModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * lanes, 2026-09-14; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that opens the lower jaw on
 * its 0.45 rhythm and switches the five-ring tail from its slow 0.25-ws sway to the fast 1.25-ws lash (orig
 * ModelLizard.java {@code getAttacking() != 0}; the port's LizardModel.poseFrom). The classic model and the GeckoLib
 * hook both pose from this, so the parity harness can drive them headlessly with a declared state.
 */
public interface LizardPose {
    /** orig ModelLizard.java {@code e.getAttacking()}: 0 at rest. */
    int getAttacking();
}
