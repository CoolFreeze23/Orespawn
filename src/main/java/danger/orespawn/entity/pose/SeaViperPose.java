package danger.orespawn.entity.pose;

/**
 * What {@code ModelSeaViper.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that chatters the jaw wide (0.65 + a 1.7 x
 * wingspeed cosine x PI x 0.17) and flicks the tongue fast (4.7 ws / 1.5 ws) in place of the idle breath (0.45 + 0.2
 * ws x PI x 0.02; 1.7 ws / 0.5 ws) (orig ModelSeaViper.java:324 {@code getAttacking() != 0}; the port's {@code
 * ModelSeaViper.poseFrom}). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive
 * them headlessly with a declared state instead of a live entity.
 */
public interface SeaViperPose {
    int getAttacking();
}
