package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelPitchBlack.poseFrom} reads from its entity, as an interface
 * the entity already satisfies (Slice 4b harness pattern, ENT-S-093). The five
 * reads: getPitchBlackScale, getActivity, getAttacking, getRandom (orig
 * ModelPitchBlack.java:820 world RNG, entity RNG per the Kraken convention)
 * and getRenderInfo (orig ModelPitchBlack.java:741, orig PitchBlack.java:193-195).
 */
public interface PitchBlackPose {
    RenderInfo getRenderInfo();

    int getAttacking();

    int getActivity();

    float getPitchBlackScale();

    RandomSource getRandom();
}
