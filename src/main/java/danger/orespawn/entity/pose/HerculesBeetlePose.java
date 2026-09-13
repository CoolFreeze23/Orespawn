package danger.orespawn.entity.pose;

/**
 * What {@code HerculesBeetleModel.setupAnim} reads from its entity, as an interface the entity already satisfies
 * (the third Tier-2 slice, 2026-09-13; the Slice 4b form of {@link Robot2Pose}): the attacking flag that switches
 * the nine jaw parts between their slow rest rhythm and their fast attack rhythm (orig ModelHerculesBeetle.java:293
 * {@code b.getAttacking() == 0}; the port's HerculesBeetleModel.poseFrom:310). The classic model and the GeckoLib
 * hook both pose from this, so the parity harness can drive them headlessly with a declared state instead of a
 * live entity.
 */
public interface HerculesBeetlePose {
    int getAttacking();
}
