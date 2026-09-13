package danger.orespawn.entity.pose;

/**
 * What {@code TrooperBugModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hooks, the Slice 4b form of {@link Robot2Pose}): the attacking flag that switches five groups - the antennae, the two
 * upper arms, the two forearm tips, the head and jaw ridges, and the fourteen jaw parts - between their slow rest
 * rhythms and their fast attack rhythms (orig ModelTrooperBug.java:969, :972, :979, :984, :989 {@code
 * e.getAttacking() == 0}; the port's TrooperBugModel.poseFrom). The classic model and the GeckoLib hook both pose
 * from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 *
 */
public interface TrooperBugPose {
    int getAttacking();
}
