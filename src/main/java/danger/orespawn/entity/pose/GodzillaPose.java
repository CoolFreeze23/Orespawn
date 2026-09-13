package danger.orespawn.entity.pose;

/**
 * What {@code ModelGodzilla.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag that picks the tail's fast sweep over its slow
 * sway, opens the jaw on a 1.5 cosine and swings the arms at 1.75 x PI x 0.16 in place of the 0.1 x PI x 0.02 idle
 * drift (the port's {@code ModelGodzilla.poseFrom} {@code boolean attacking = entity.getAttacking() != 0}). The
 * classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a
 * declared state instead of a live entity.
 */
public interface GodzillaPose {
    int getAttacking();
}
