package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code OstrichModel.poseFrom} reads from its entity, as an interface the
 * entity already satisfies (ENT-S-093, Robot2Pose shape). The classic model
 * poses from this so the parity harness can drive it headlessly with a declared
 * state instead of a live entity. Everything except {@code getRenderInfo},
 * {@code getIsActivated}, {@code xOld()}, {@code zOld()} and {@code yRotO()} is
 * already provided by Entity/TamableAnimal.
 */
public interface OstrichPose {
    /** orig Ostrich.java:105-107. */
    RenderInfo getRenderInfo();

    RandomSource getRandom();

    /** orig ModelOstrich.java:349 {@code func_70906_o()} (EntityTameable.isSitting). */
    boolean isInSittingPose();

    /** orig EntityCannonFodder.java:228-230 {@code get_is_activated()}. */
    int getIsActivated();

    /** orig ModelOstrich.java:335 {@code field_70153_n != null}. */
    boolean isVehicle();

    double getX();

    double getZ();

    /** orig ModelOstrich.java:299 {@code field_70169_q} (prevPosX). */
    double xOld();

    /** orig ModelOstrich.java:299 {@code field_70166_s} (prevPosZ). */
    double zOld();

    /** orig ModelOstrich.java:336 {@code field_70177_z} (rotationYaw). */
    float getYRot();

    /** orig ModelOstrich.java:336 {@code field_70126_B} (prevRotationYaw). */
    float yRotO();
}
