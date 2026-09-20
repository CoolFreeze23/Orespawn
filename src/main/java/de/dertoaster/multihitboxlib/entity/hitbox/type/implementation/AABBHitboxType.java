package de.dertoaster.multihitboxlib.entity.hitbox.type.implementation;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.SubPartConfig;
import de.dertoaster.multihitboxlib.entity.hitbox.type.IHitboxType;
import de.dertoaster.multihitboxlib.init.MHLibHitboxTypes;
import de.dertoaster.multihitboxlib.util.UtilityCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class AABBHitboxType implements IHitboxType {

	protected final Vec2 baseSize;
	protected final Vec3 basePosition;
	protected final Vec3 pivot;
	/**
	 * ENT-S-173 (the port's extension): the anchor bone's summed rest rotation, radians, in the collector's Euler order -
	 * what the server-side fallback rotates the pivot by (with the body yaw folded in) when no client has sent bone
	 * positions, so the box sits on the drawn rest segment. Optional; a profile without it (the Queen's hand-made one,
	 * the robots') keeps {@link Vec3#ZERO}, the fallback of before.
	 */
	protected final Vec3 rotation;

	public AABBHitboxType(Vec2 size, Vec3 pos, Vec3 pivot) {
		this(size, pos, pivot, Vec3.ZERO);
	}

	public AABBHitboxType(Vec2 size, Vec3 pos, Vec3 pivot, Vec3 rotation) {
		this.baseSize = size;
		this.basePosition = pos;
		this.pivot = pivot;
		this.rotation = rotation;
	}

	public static final MapCodec<AABBHitboxType> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				UtilityCodecs.VEC2_CODEC.fieldOf("size").forGetter(obj -> obj.baseSize),
				Vec3.CODEC.fieldOf("position").forGetter(obj -> obj.basePosition),
				Vec3.CODEC.optionalFieldOf("pivot", Vec3.ZERO).forGetter(obj -> obj.pivot),
				Vec3.CODEC.optionalFieldOf("rotation", Vec3.ZERO).forGetter(obj -> obj.rotation)
			).apply(instance, AABBHitboxType::new);
	});

	@Override
	public Vec3 getBaseRotation() {
		return this.rotation;
	}

	@Override
	public MapCodec<? extends IHitboxType> getType() {
		return MHLibHitboxTypes.AABB.get();
	}

	@Override
	public <T extends Entity> MHLibPartEntity<T> createPartEntity(SubPartConfig config, T parent, int partNumber) {
		return new MHLibPartEntity<T>(parent, config, EntityDimensions.scalable(this.baseSize.x, this.baseSize.y), this.basePosition, this.pivot);
	}
}
