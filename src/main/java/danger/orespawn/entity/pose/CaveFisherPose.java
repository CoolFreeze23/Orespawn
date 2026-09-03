package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelCaveFisher.setupAnim} reads from its entity (orig
 * ModelCaveFisher.java:593-606: getRenderInfo, getAttacking, the RNG), as an
 * interface the entity already satisfies. Same shape as {@link Robot2Pose} so
 * the parity harness can drive the classic model headlessly from a declared
 * state instead of a live entity (ENT-S-093).
 */
public interface CaveFisherPose {
    RenderInfo getRenderInfo();

    int getAttacking();

    RandomSource getRandom();
}
