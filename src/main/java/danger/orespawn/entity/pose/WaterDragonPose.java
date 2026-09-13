package danger.orespawn.entity.pose;

/**
 * What {@code ModelWaterDragon.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hooks, the Slice 4b form of {@link Robot2Pose}): the sitting check that stills the body fin, the neck fin and the
 * head fin (orig ModelWaterDragon.java:222, :227, :232 {@code e.func_70906_o()}; the port's {@code
 * isInSittingPose()}), and the three-way attacking value that picks the jaw's pitch - 1: the 1.2 x wingspeed cosine
 * at 0.25 x PI, 2: 0.45 rad, otherwise -0.25 rad (orig :236 {@code e.getAttacking() == 1 ... == 2}). The classic
 * model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared
 * state instead of a live entity.
 */
public interface WaterDragonPose {
    /** orig ModelWaterDragon.java:222, :227, :232 {@code func_70906_o()} (EntityTameable.isSitting). */
    boolean isInSittingPose();

    /** orig ModelWaterDragon.java:236 {@code getAttacking()}: 0 rest, 1 biting, 2 mouth held open. */
    int getAttacking();
}
