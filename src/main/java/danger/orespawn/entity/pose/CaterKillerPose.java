package danger.orespawn.entity.pose;

/**
 * What {@code CaterKillerModel.setupAnim} reads from its entity, as an interface the entity already satisfies
 * (the third Tier-2 slice, 2026-09-13; the Slice 4b form of {@link Robot2Pose}): the attacking flag that
 * switches the jaws, the head bob and the first three segments' legs between their rest rhythms and their
 * attack rhythms (orig ModelCaterKiller.java:248, 251, 289 {@code e.getAttacking() != 0}; the port's
 * CaterKillerModel.poseFrom:274). The classic model and the GeckoLib hook both pose from this, so the parity
 * harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface CaterKillerPose {
    int getAttacking();
}
