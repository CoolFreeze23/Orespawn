package danger.orespawn.entity.pose;

/**
 * What {@code BeeModel.setupAnim} reads from its entity, as an interface the entity already satisfies
 * (the third Tier-2 slice, 2026-09-13; the Slice 4b form of {@link Robot2Pose}): the attacking flag that
 * switches the abdomen's curl between its slow rest rhythm and its fast attack rhythm (orig
 * ModelBee.java:214 {@code b.getAttacking() == 0}; the port's BeeModel.poseFrom:231). The classic model and the
 * GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared
 * state instead of a live entity.
 */
public interface BeePose {
    int getAttacking();
}
