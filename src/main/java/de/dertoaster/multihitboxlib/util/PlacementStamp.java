package de.dertoaster.multihitboxlib.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * BUG-045 / ENT-S-176: what a multipart entity's synched parts were last placed for - the tick, the position, the body
 * yaw and the scale - so the placement after the entity's tick ({@code IMultipartEntity#mhlibAfterTick}) can tell
 * whether the entity moved, turned or changed size after aiStep's placement, or whether that placement ran this tick.
 */
public record PlacementStamp(int tick, double x, double y, double z, float bodyYaw, double scale) {

	public static PlacementStamp of(Entity entity, double scale) {
		return new PlacementStamp(entity.tickCount, entity.getX(), entity.getY(), entity.getZ(),
				entity instanceof LivingEntity living ? living.yBodyRot : entity.getYRot(), scale);
	}

	/** Whether the entity stands this tick as the parts were placed for it (field by field, nothing allocated). */
	public boolean matches(Entity entity, double scale) {
		return this.tick == entity.tickCount && this.x == entity.getX() && this.y == entity.getY() && this.z == entity.getZ()
				&& this.bodyYaw == (entity instanceof LivingEntity living ? living.yBodyRot : entity.getYRot()) && this.scale == scale;
	}
}
