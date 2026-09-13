package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelKraken.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the Slice 4b form of {@link Robot2Pose}): the attacking flag that stiffens the
 * two front tentacles (0.5 / 0.03 over 0.2 / 0.1) and picks the twitch re-roll ranges, the per-entity render scratch
 * whose {@code ri1} / {@code ri2} are latched on the falling zero crossing of a 0.66 cosine (orig ModelKraken.java
 * :1045-1057; orig Kraken.java:58 {@code renderdata}), and the entity's own RNG that rolls them ({@code nextInt(10)} /
 * {@code nextInt(15)} idle, {@code nextInt(4)} / {@code nextInt(3)} attacking) - {@code Entity.getRandom()} already
 * satisfies it. The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them
 * headlessly with a declared state instead of a live entity.
 */
public interface KrakenPose {
    RenderInfo getRenderInfo();

    int getAttacking();

    RandomSource getRandom();
}
