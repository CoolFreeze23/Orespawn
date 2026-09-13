package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ScorpionModel.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link Robot2Pose}, the {@link RotatorPose} RenderInfo and the {@link Robot2Pose#getRandom}
 * RNG): the per-entity render scratch whose {@code ri1} / {@code ri2} latch which claw and whether the tail strikes,
 * rolled once per rhythm cycle on the frame the 3 x wingspeed cosine crosses zero upward (orig
 * ModelScorpion.java:198 {@code r = e.getRenderInfo()}, :200-211; the port's ScorpionModel.poseFrom); the
 * attacking flag that picks the roll's ranges (orig :203 {@code e.getAttacking() == 0}: 20 / 25 at rest, 4 / 3
 * attacking); and the random source the roll consumes - the ENTITY's own, as the port's classic model reads it
 * (ENT-S-093, the Kraken precedent; orig :204-208 rolled the world's). The classic model and the GeckoLib hook both
 * pose from this, so the parity harness can drive them headlessly from a declared latch and a seeded source instead
 * of a live entity.
 */
public interface ScorpionPose {
    /** orig ModelScorpion.java:198 {@code getRenderInfo()} (orig Scorpion.java:101-103): {@code ri1} the claw selector, {@code ri2} the tail selector. */
    RenderInfo getRenderInfo();

    /** orig ModelScorpion.java:203 {@code getAttacking() == 0}. */
    int getAttacking();

    /** The entity's random source ({@code Entity.getRandom()}): the port's ScorpionModel roll (orig :204-208 {@code nextInt}). */
    RandomSource getRandom();
}
