package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code EmperorScorpionModel.setupAnim} reads from its entity, as an interface the entity already satisfies
 * (the hook survey, 2026-09-14; the Slice 4b form of {@link Robot2Pose}): the attacking flag that switches
 * the mandibles between their slow rest rhythm and their fast attack rhythm and picks the claw / tail selector
 * ranges (orig ModelEmperorScorpion.java:613 {@code getAttacking() == 0}; the port's
 * EmperorScorpionModel.poseFrom), the per-entity render scratch whose {@code ri1} / {@code ri2} latch the claw
 * and tail selectors across frames (orig ModelEmperorScorpion.java:613-641, orig EmperorScorpion.java:53;
 * ENT-S-093) and the entity's own random the latch rolls when the 3.0-rhythm crosses zero. The classic model
 * and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state and a seeded source instead of a live entity.
 */
public interface EmperorScorpionPose {
    /** orig ModelEmperorScorpion.java:613 {@code b.getAttacking()}: 0 at rest. */
    int getAttacking();

    /** orig EmperorScorpion.java:110-112 {@code getRenderInfo()}: the per-entity latch ({@code ri1}, {@code ri2}). */
    RenderInfo getRenderInfo();

    /** orig ModelEmperorScorpion.java:616-622 {@code b.worldObj.rand} / the port's {@code entity.getRandom()}: the selector rolls. */
    RandomSource getRandom();
}
