package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code GhostSkellyModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hook
 * lanes, 2026-09-14; the Slice 4b form of {@link Robot2Pose}): the per-entity render scratch whose {@code rf2} holds
 * the last frame's phase of the 0.05 head rhythm and whose {@code ri2} latches, once per wrap of that phase, whether
 * the head swivels this period (orig ModelGhostSkelly.java:99-122, orig GhostSkelly.java:22; ENT-S-093), and the
 * entity's own random that rolls the latch ({@code nextInt(3) == 1}). The classic model and the GeckoLib hook both pose
 * from this, so the parity harness can drive them headlessly with a declared state and a seeded source.
 */
public interface GhostSkellyPose {
    /** orig GhostSkelly.java:55-57 {@code getRenderInfo()}: the per-entity latch ({@code rf2}, {@code ri2}). */
    RenderInfo getRenderInfo();

    /** orig ModelGhostSkelly.java:113 {@code rand.nextInt(3)} / the port's {@code entity.getRandom()}: the swivel roll. */
    RandomSource getRandom();
}
