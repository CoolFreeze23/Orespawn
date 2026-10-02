package de.dertoaster.multihitboxlib.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * BUG-045: a rider's seat is set after its own tick. Entity.rideTick runs the tick (EntityTickEvent.Pre, tick(),
 * EntityTickEvent.Post) and then the vehicle's positionRider, which moves the rider and may turn its body (a boat's
 * clampRotation); an override's own work after that (AbstractSkeleton copying the body yaw of a PathfinderMob vehicle
 * it controls) also comes before this hook. So once the level's tick of a passenger has run its rideTick, a riding
 * multipart entity's synched parts are placed again for where it now sits ({@link IMultipartEntity#mhlibAfterTick}:
 * nothing when it stands as its last placement left it, PlacementStamp). {@code allow = expect = 1}: one rideTick call.
 */
@Mixin(ServerLevel.class)
public abstract class MixinServerLevelPassengers {

	@Inject(
			method = "tickPassenger(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V", shift = At.Shift.AFTER),
			allow = 1,
			expect = 1
	)
	private void mhlibAfterRideTick(Entity vehicle, Entity passenger, CallbackInfo ci) {
		if (passenger instanceof IMultipartEntity<?> ime && passenger.isMultipartEntity()) {
			ime.mhlibAfterTick();
		}
	}
}
