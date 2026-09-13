package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelNastysaurus.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link Robot2Pose}): the attacking flag that opens the jaw on a 0.85 x wingspeed cosine and
 * speeds the tail (0.76 / 0.25 over 0.26 / 0.08), the per-entity render scratch whose {@code rf1} holds the last
 * phase of the 0.7-wingspeed idle rhythm and whose {@code ri1} is the chew bit re-rolled on each phase wrap (orig
 * ModelNastysaurus.java:438,450; ENT-S-093), and the LEVEL's RNG that rolls it ({@code nextInt(20) == 1};
 * the classic reads {@code entity.level().random} - the PurplePower precedent's {@code
 * getLevelRandom()}, a one-line delegate on the entity). The classic model and the GeckoLib hook both pose from this,
 * so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface NastysaurusPose {
    RenderInfo getRenderInfo();

    int getAttacking();

    /** The level's RNG the classic chew latch rolls ({@code entity.level().random}); the harness hands a seeded source. */
    RandomSource getLevelRandom();
}
