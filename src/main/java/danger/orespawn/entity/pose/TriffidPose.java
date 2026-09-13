package danger.orespawn.entity.pose;

/**
 * What {@code TriffidModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the open / closed flag that holds the
 * four leaf chains at their closed fold (0.122522116 rad per link) or sways them on the 0.25 x wingspeed cosine
 * (0.039 x PI) (orig ModelTriffid.java:1275 {@code e.getOpenClosed() == 0}; the port's TriffidModel.poseFrom), and the
 * attacking flag that lashes the fifteen-link tentacle on {@code |cos|} of the same rhythm at 0.5 x PI instead of
 * holding it at a right angle (orig :1342 {@code e.getAttacking() != 0}). The classic model and the GeckoLib hook both
 * pose from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface TriffidPose {
    /** orig ModelTriffid.java:1275 {@code getOpenClosed()}: 0 closed (the fixed fold), otherwise swaying open. */
    int getOpenClosed();

    /** orig ModelTriffid.java:1342 {@code getAttacking() != 0}: the tentacle lash. */
    int getAttacking();
}
