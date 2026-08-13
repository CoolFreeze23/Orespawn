package danger.orespawn.mixin;

import danger.orespawn.client.MountCameraState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.At;

/**
 * S7b — the riding camera (sitting FAIL-4, owner-approved design).
 * Technique per Shoulder Surfing Reloaded (MIT; studied, not copied):
 * wrap the vanilla third-person zoom-arm move inside {@code Camera.setup}
 * (ordinal 0 — setup has a SECOND move in its sleeping branch, and
 * {@code WrapOperation} instead of a redirect so Shoulder Surfing itself,
 * which redirects the same instruction, chains rather than crashes —
 * reviewer findings 1-2). Scope-gated hard: the redirect falls through to
 * the vanilla move verbatim unless the camera entity rides a
 * MODERN-mode OreSpawn legged robot AND the mountCamera config is on —
 * zero effect unridden, in classic mode, or config-off. The pivot (the
 * rider's eye, which rides the S7a seat) is deliberately NOT smoothed —
 * only the arm is — so camera smoothing cannot stack with/amplify the
 * seat's own motion (the flick-sweep interaction the owner flagged).
 * Collision: 8-corner clip with instant snap-in and eased glide-out
 * (never clips, never pops); F5 front view shares the same basis math.
 */
@Mixin(Camera.class)
public abstract class MountCameraMixin {

    @Shadow
    protected abstract void move(float zoom, float dy, float dx);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    public abstract Vec3 getPosition();

    @WrapOperation(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V", ordinal = 0))
    private void orespawn$mountCameraArm(Camera camera, float zoom, float dy, float dx, Operation<Void> original) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity cameraEntity = minecraft.cameraEntity;
        Entity vehicle = cameraEntity != null ? cameraEntity.getVehicle() : null;
        double targetDistance = MountCameraState.targetDistance(vehicle);
        if (targetDistance <= 0.0 || minecraft.level == null) {
            MountCameraState.reset();
            original.call(camera, zoom, dy, dx); // vanilla arm, untouched
            return;
        }
        MountCameraState.smoothTowards(targetDistance);

        // Arm vector in the camera basis (already rotated — works for the
        // F5 front view too): back along look, up, and over the shoulder.
        Vec3 look = new Vec3(camera.getLookVector());
        Vec3 up = new Vec3(camera.getUpVector());
        Vec3 left = new Vec3(camera.getLeftVector());
        Vec3 arm = look.scale(-MountCameraState.distance())
                .add(up.scale(MountCameraState.height()))
                .add(left.scale(-MountCameraState.lateral()));

        // 8-corner clip (vanilla getMaxZoom's pattern at our arm): fraction
        // of the arm that stays out of terrain; snap-in / glide-out is
        // handled asymmetrically in the state.
        Vec3 eye = this.getPosition();
        double length = arm.length();
        double fraction = 1.0;
        for (int i = 0; i < 8; ++i) {
            double ox = ((i & 1) * 2 - 1) * 0.1;
            double oy = ((i >> 1 & 1) * 2 - 1) * 0.1;
            double oz = ((i >> 2 & 1) * 2 - 1) * 0.1;
            Vec3 from = eye.add(ox, oy, oz);
            HitResult hit = minecraft.level.clip(new ClipContext(from, from.add(arm),
                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, cameraEntity));
            if (hit.getType() != HitResult.Type.MISS) {
                fraction = Math.min(fraction, hit.getLocation().distanceTo(from) / length);
            }
        }
        double applied = MountCameraState.applyCollision(fraction);
        Vec3 pos = eye.add(arm.scale(applied));
        this.setPosition(pos.x, pos.y, pos.z);
    }
}
