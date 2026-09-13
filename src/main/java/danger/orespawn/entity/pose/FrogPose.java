package danger.orespawn.entity.pose;

import net.minecraft.world.phys.Vec3;

/**
 * What {@code ModelFrog.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks, the
 * Slice 4b form of {@link Robot2Pose}): the singing flag that works the jaw (orig ModelFrog.java:102 {@code
 * c.getSinging() != 0}) and the vertical motion that folds the hind legs into the jump (orig :104 {@code c.field_70181_x} -
 * motionY, beyond +-0.1; the port's {@code getDeltaMovement().y}, {@code Entity}'s own). The classic model and the
 * GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state instead of a
 * live entity.
 */
public interface FrogPose {
    /** orig ModelFrog.java:102 {@code getSinging()}; rest 0 (the jaw at 1.22 rad). */
    int getSinging();

    /** orig ModelFrog.java:104 {@code field_70181_x} (motionY), read as the y of the delta; rest {@code Vec3.ZERO} (the crouch, 0.227 rad). */
    Vec3 getDeltaMovement();
}
