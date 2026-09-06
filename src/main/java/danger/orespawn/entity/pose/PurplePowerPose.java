package danger.orespawn.entity.pose;

import net.minecraft.util.RandomSource;

/**
 * What {@code ModelPurplePower.poseFrom} reads from its entity (ENT-S-146): the LEVEL's random
 * source, which orig ModelPurplePower.java:57, :66 and :75 roll three times per rendered frame
 * ({@code p.worldObj.rand.nextFloat() * 360.0f}) for the orientations of the three spoke fans.
 * The classic model and the GeckoLib hook both pose from this, so the parity harness can drive
 * them headlessly from a SEEDED source ({@code ProbeSubject}) instead of a live level; see
 * {@link Robot2Pose} and {@link RotatorPose}.
 *
 * <p>Named {@code getLevelRandom}, not {@code getRandom}: {@code Entity.getRandom()} (public in
 * 1.21.1) returns the entity's OWN source, the one orig PurplePower.java:155-163 ({@code this.rand})
 * uses for its flight targets, so a {@code getRandom()} on this interface would be satisfied by the
 * inherited method and hand the model the wrong source without a compile error. The world's
 * ({@code this.worldObj.rand}, orig ModelPurplePower.java:57) is {@code level().getRandom()}.</p>
 */
public interface PurplePowerPose {
    /** The level's random source ({@code Level.getRandom()}): orig ModelPurplePower.java:57 {@code p.worldObj.rand}. */
    RandomSource getLevelRandom();

    /**
     * One fan roll: orig ModelPurplePower.java:57 / :66 / :75 {@code rand.nextFloat() * 360.0f} - exactly
     * one {@code nextFloat()} per call. The one source the classic draw loop and the GeckoLib hook both
     * read, so the two renderers consume the level's sequence identically: three rolls per rendered
     * frame, in X, Y, Z order.
     */
    static float roll(RandomSource random) {
        return random.nextFloat() * 360.0F;
    }
}
