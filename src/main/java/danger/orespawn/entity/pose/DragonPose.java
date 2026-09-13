package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;

/**
 * What {@code ModelDragon.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks, the
 * {@link OstrichPose} shape) - the Dragon and, through it, the Baby Dragon, one rig for two registries: the per-entity
 * render scratch whose {@code rf1} is the ridden-flight neck-yaw latch (orig ModelDragon.java:417,
 * :538-551), the activity and attacking flags that pick the leg, wing, tail and mouth rhythms (orig :425, :457, :492,
 * :496, :538, :575), the sitting flag that stills the tail (orig :500 {@code func_70906_o()}, the port's {@code
 * isInSittingPose()}), and the movement delta the walk amplitude is built from (orig :419 {@code field_70169_q -
 * field_70165_t}, {@code field_70166_s - field_70161_v}; :539 {@code field_70126_B - field_70177_z}). The classic model
 * and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state
 * instead of a live entity. Everything but {@link #getRenderInfo}, {@link #getAttacking}, {@link #getActivity},
 * {@link #xOld}, {@link #zOld} and {@link #yRotO} is {@code Entity} / {@code TamableAnimal}'s own.
 */
public interface DragonPose {
    /** orig ModelDragon.java:417 {@code e.getRenderInfo()} (orig Dragon.java:199-201); rf1 rests at 0. */
    RenderInfo getRenderInfo();

    /** orig ModelDragon.java:457, :492, :496, :575 {@code e.getAttacking()}; rest 0. */
    int getAttacking();

    /** orig ModelDragon.java:425, :457, :496, :538 {@code e.getActivity()}; rest 0 (the walking branch, the head look halved). */
    int getActivity();

    /** orig ModelDragon.java:500 {@code e.func_70906_o()} (EntityTameable.isSitting); rest false (the tail sways). */
    boolean isInSittingPose();

    /** orig ModelDragon.java:419 {@code field_70165_t} (posX). */
    double getX();

    /** orig ModelDragon.java:419 {@code field_70161_v} (posZ). */
    double getZ();

    /** orig ModelDragon.java:419 {@code field_70169_q} (prevPosX). */
    double xOld();

    /** orig ModelDragon.java:419 {@code field_70166_s} (prevPosZ). */
    double zOld();

    /** orig ModelDragon.java:539 {@code field_70177_z} (rotationYaw). */
    float getYRot();

    /** orig ModelDragon.java:539 {@code field_70126_B} (prevRotationYaw). */
    float yRotO();
}
