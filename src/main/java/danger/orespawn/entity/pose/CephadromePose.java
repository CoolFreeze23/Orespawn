package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;

/**
 * What {@code ModelCephadrome.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the {@link OstrichPose} shape): the per-entity render scratch whose {@code rf1} is the ridden-flight neck-yaw latch
 * (orig ModelCephadrome.java:383, :469-482), the activity and attacking flags that pick the leg, wing, tail and mouth
 * rhythms (orig :397, :417, :437, :469, :503), and the movement delta the walk amplitude is built from (orig :385
 * {@code field_70169_q - field_70165_t}, {@code field_70166_s - field_70161_v}; :470 {@code field_70126_B
 * - field_70177_z}). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive
 * them headlessly with a declared state instead of a live entity. Everything but {@link #getRenderInfo}, {@link
 * #getAttacking}, {@link #getActivity}, {@link #xOld}, {@link #zOld} and {@link #yRotO} is {@code Entity}'s own.
 */
public interface CephadromePose {
    /** orig ModelCephadrome.java:383 {@code e.getRenderInfo()} (orig Cephadrome.java:156-158); rf1 rests at 0. */
    RenderInfo getRenderInfo();

    /** orig ModelCephadrome.java:417, :437, :503 {@code e.getAttacking()}; rest 0. */
    int getAttacking();

    /** orig ModelCephadrome.java:397, :417, :437, :469 {@code e.getActivity()}; rest 0 (the walking branch, the head look halved). */
    int getActivity();

    /** orig ModelCephadrome.java:385 {@code field_70165_t} (posX). */
    double getX();

    /** orig ModelCephadrome.java:385 {@code field_70161_v} (posZ). */
    double getZ();

    /** orig ModelCephadrome.java:385 {@code field_70169_q} (prevPosX). */
    double xOld();

    /** orig ModelCephadrome.java:385 {@code field_70166_s} (prevPosZ). */
    double zOld();

    /** orig ModelCephadrome.java:470 {@code field_70177_z} (rotationYaw). */
    float getYRot();

    /** orig ModelCephadrome.java:470 {@code field_70126_B} (prevRotationYaw). */
    float yRotO();
}
