package danger.orespawn.entity.pose;

/**
 * What {@code LeafMonsterModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * survey, 2026-09-14; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that switches the
 * rig between its folded bush (the body and both arms lowered, every rotation 0) and its walking, arm-waving
 * monster (orig ModelLeafMonster.java {@code getAttacking() == 0}; the port's LeafMonsterModel.poseFrom). The classic
 * model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state.
 */
public interface LeafMonsterPose {
    /** orig ModelLeafMonster.java {@code e.getAttacking()}: 0 at rest (the bush). */
    int getAttacking();
}
