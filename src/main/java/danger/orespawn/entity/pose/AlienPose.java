package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelAlien.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * survey, 2026-09-14; the Slice 4b form of {@link Robot2Pose}): the attacking flag that opens the head fan and picks
 * the selector ranges (orig ModelAlien.java {@code getAttacking() == 0}; the port's ModelAlien.poseFrom), the
 * per-entity render scratch whose {@code ri1} / {@code ri2} / {@code ri3} latch the claw, tail and jaw selectors across
 * frames (orig ModelAlien.java:512-552, orig Alien.java:42; ENT-S-093) and the entity's own random the latch rolls
 * when the 3.5-ws rhythm crosses zero. The Alien Boss inherits the implementation. The classic model and the
 * GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state and a seeded source.
 */
public interface AlienPose {
    /** orig ModelAlien.java {@code e.getAttacking()}: 0 at rest. */
    int getAttacking();

    /** orig Alien.java:105-107 {@code getRenderInfo()}: the per-entity latch ({@code ri1}, {@code ri2}, {@code ri3}). */
    RenderInfo getRenderInfo();

    /** orig ModelAlien.java:512-552 {@code rand.nextInt(...)} / the port's {@code entity.getRandom()}: the selector rolls. */
    RandomSource getRandom();
}
