package danger.orespawn.entity.pose;

/**
 * What {@code ModelTheKing.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that picks
 * every fast rhythm - the wings' 0.75 x 0.21 beat over the 0.35 x 0.15 glide, the claws' 0.75 x 0.25 flex and the
 * legs' 0.6 x 0.45 swing over rest, the tail's 0.56 / 0.19 lash over 0.26 / 0.08, and the three heads' 0.25-rad sweeps
 * and 0.12 jaw snaps over their idle nods (the port's {@code ModelTheKing.poseFrom} {@code boolean attacking =
 * entity.getAttacking() != 0}; orig ModelTheKing.java setRotationAngles). The classic model and the GeckoLib hook both
 * pose from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface TheKingPose {
    int getAttacking();
}
