package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code LurkingTerrorModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hook survey, 2026-09-14; the Slice 4b form of {@link Robot2Pose}): the per-entity render scratch
 * whose {@code rf1} / {@code ri1} latch the six leg-selector bits and {@code rf2} / {@code ri2} the mouth bit across
 * frames, each re-rolled when its rhythm's phase wraps (orig ModelLurkingTerror.java:435-562, orig
 * LurkingTerror.java:49; ENT-S-093), the entity's own random those rolls draw from, and the attacking flag
 * that forces the mouth open (orig :460 {@code getAttacking() != 0}). The classic model and the GeckoLib hook both
 * pose from this, so the parity harness can drive them headlessly with a declared state and a seeded source.
 */
public interface LurkingTerrorPose {
    /** orig LurkingTerror.java:99-101 {@code getRenderInfo()}: the per-entity latches ({@code ri1} / {@code rf1}, {@code ri2} / {@code rf2}). */
    RenderInfo getRenderInfo();

    /** orig ModelLurkingTerror.java:443-475 {@code rand.nextInt(...)} / the port's {@code entity.getRandom()}: the selector rolls. */
    RandomSource getRandom();

    /** orig ModelLurkingTerror.java:460 {@code getAttacking()}: 0 at rest. */
    int getAttacking();
}
