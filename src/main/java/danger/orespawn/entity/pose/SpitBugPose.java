package danger.orespawn.entity.pose;

/**
 * What {@code SpitBugModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the attacking flag that switches the
 * three upper-jaw parts and the three teeth between their slow rest rhythm (0.3 x wingspeed, 0.015 x PI) and their
 * fast attack rhythm (2.6 x wingspeed, 0.1 x PI), folded by {@code |cos|} (orig ModelSpitBug.java:698
 * {@code e.getAttacking() == 0}; the port's SpitBugModel.poseFrom). The classic model and the GeckoLib hook both pose
 * from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface SpitBugPose {
    int getAttacking();
}
