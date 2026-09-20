package de.dertoaster.multihitboxlib.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * ENT-S-173 (the port's extension of the vendored library): a per-entity-type size scale for profiled species that do
 * not implement {@link IMHLibSizeCallback} themselves. {@code IMultipartEntity.mhlibGetEntitySizeInternally} resolves
 * a callback first, then a function registered here, then the library's own rule (an {@code AgeableMob} baby is 0.5,
 * everything else 1); {@code MHLibPartEntity.applyInformation} scales the pivot by the same value.
 *
 * <p>Why a registry: the port's hitbox profiles cover a hundred species drawn through GeckoLib rigs whose render scale
 * follows the entity's state in the renderer's descriptor (halved for a baby in some species and not in others; the
 * Crab's growth, Pitch Black's size tier, the Girlfriend's valentine form, the CaterKiller under PlayNicely), and the
 * part boxes must follow the same rule. Registering the rule per type keeps it in one table beside the descriptors
 * instead of an interface on a hundred classes that extend vanilla's. Filled once during common setup; reads are
 * lock-free.</p>
 */
public final class MHLibEntitySizeScales {

	private static final Map<EntityType<?>, ToDoubleFunction<Entity>> SCALES = new ConcurrentHashMap<>();

	private MHLibEntitySizeScales() {
	}

	public static void register(EntityType<?> type, ToDoubleFunction<Entity> scale) {
		SCALES.put(type, scale);
	}

	/** The registered scale of {@code entity}'s type, or {@code null} when none is registered. */
	public static ToDoubleFunction<Entity> get(EntityType<?> type) {
		return SCALES.get(type);
	}

	public static int size() {
		return SCALES.size();
	}
}
