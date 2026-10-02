package de.dertoaster.multihitboxlib;

import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Game-bus event hooks for multipart entities: custom main-hitbox dimensions and
 * per-player tracking notifications used by the part-synchronization system.
 *
 * <p>{@code EntityEvent.Size}, {@code PlayerEvent.StartTracking} and
 * {@code PlayerEvent.StopTracking} are NeoForge GAME-bus events; the previous explicit
 * {@code bus = MOD} registration was wrong (BUG-001). The bus attribute is deprecated on
 * this NeoForge version, so registration is left to per-listener auto-detection.</p>
 */
@EventBusSubscriber(modid = Constants.MODID)
public class EntityEventHandler {

	@SubscribeEvent
	public static void onEntitySizeEvent(EntityEvent.Size event) {
		Entity ent = event.getEntity();
		if (ent instanceof IMultipartEntity<?> ime) {
			if (ime.getHitboxProfile().isPresent()) {
				HitboxProfile hp = ime.getHitboxProfile().get();
				
				if (hp.mainHitboxConfig().baseSize().equals(Vec2.ZERO)) {
					return;
				}
				Vec2 customDims = hp.mainHitboxConfig().baseSize();
				event.setNewSize(EntityDimensions.scalable(customDims.x, customDims.y));
			}
		}
	}
	
	/**
	 * BUG-045 / ENT-S-176: after an entity's tick (the event fires right after {@code tick()}, in the level's tick of it
	 * and in a rider's rideTick), a multipart entity's synched parts follow its last own move
	 * ({@link IMultipartEntity#mhlibAfterTick}). The lowest priority, so a listener that moves the entity runs first.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityTickPost(EntityTickEvent.Post event) {
		Entity ent = event.getEntity();
		if (ent instanceof IMultipartEntity<?> ime && ent.isMultipartEntity()) {
			ime.mhlibAfterTick();
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof LivingEntity && event.getTarget().isMultipartEntity()) {
			if (event.getTarget() instanceof IMultipartEntity<?> ime  && event.getEntity() instanceof ServerPlayer sp) {
				ime.mhLibOnStartTrackingEvent(sp);
			}
		}
	}
	
	@SubscribeEvent
	public static void onStopTracking(PlayerEvent.StopTracking event) {
		if (event.getTarget() instanceof LivingEntity && event.getTarget().isMultipartEntity()) {
			if (event.getTarget() instanceof IMultipartEntity<?> ime  && event.getEntity() instanceof ServerPlayer sp) {
				ime.mhLibOnStopTrackingEvent(sp);
			}
		}
	}
	
}
