package de.dertoaster.multihitboxlib.init;

import de.dertoaster.multihitboxlib.Constants;
import de.dertoaster.multihitboxlib.MHLibMod;
import de.dertoaster.multihitboxlib.api.DatapackRegistry;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = Constants.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MHLibDatapackLoaders {

	public static final DatapackRegistry<HitboxProfile> HITBOX_PROFILE_REGISTRY = new DatapackRegistry<>(MHLibMod.prefix("hitbox_profiles"), HitboxProfile.CODEC);

	// ──────────────────────────────────────────────────────────────────────
	// OPT-018 (and the shared resolve step of OPT-001): memoized profile
	// lookups. The uncached path runs BuiltInRegistries.ENTITY_TYPE.getKey()
	// + a datapack-registry map lookup + Optional allocs for EVERY
	// LivingEntity constructor JVM-wide and for every hot per-tick/per-frame
	// call site.
	//
	// Cache shape: outer map keyed by the resolved Registry<HitboxProfile>
	// INSTANCE, inner map keyed by EntityType. Invalidation story:
	//  1. Identity keying: datapack-registry contents are immutable per
	//     Registry instance (dynamic registries are built and frozen at
	//     world load / connection sync and are never mutated in place), so
	//     a cached entry can only be served while the exact registry
	//     instance the uncached path would consult is still live. A new
	//     world, a rejoined server, or a re-synced client produces a NEW
	//     Registry instance and therefore a cache miss — never a stale hit.
	//  2. Event-driven clear (OPT-001 requirement): AddReloadListenerEvent
	//     (initial server data load and every /reload) and
	//     ServerStoppedEvent call invalidateProfileCache() — see
	//     GameEventHandler. This also bumps PROFILE_CACHE_GENERATION, which
	//     invalidates the per-entity field caches (IMultipartEntity
	//     .getHitboxProfile, OPT-001).
	//  3. Size guard: at most PROFILE_CACHE_MAX_REGISTRIES registries are
	//     retained (singleplayer legitimately has two live at once: server
	//     + synced client copy); beyond that the whole cache is dropped.
	//     Dropping cache entries is always behavior-neutral — the next call
	//     re-resolves from the authoritative registry.
	// Thread-safety: entity constructors can run on worldgen worker
	// threads, hence ConcurrentHashMap everywhere. A racy double-resolve
	// stores the same value (same registry instance -> same result).
	// ──────────────────────────────────────────────────────────────────────
	private static final Map<Registry<HitboxProfile>, Map<EntityType<?>, Optional<HitboxProfile>>> PROFILE_LOOKUP_CACHE = new ConcurrentHashMap<>(2);
	private static final AtomicInteger PROFILE_CACHE_GENERATION = new AtomicInteger();
	private static final int PROFILE_CACHE_MAX_REGISTRIES = 4;

	@SubscribeEvent
	public static void onRegistryRegistration(DataPackRegistryEvent.NewRegistry event) {
		HITBOX_PROFILE_REGISTRY.registerSynchable(event);
	}

	public static Optional<HitboxProfile> getHitboxProfile(ResourceLocation entityID, RegistryAccess registryAccess) {
		return Optional.ofNullable(HITBOX_PROFILE_REGISTRY.get(entityID, registryAccess));
	}

	/*
	 * DONE: Find replacement for getKey
	 */
	public static Optional<HitboxProfile> getHitboxProfile(EntityType<?> entityType, RegistryAccess registryAccess) {
		// OPT-018: memoized per (registry instance, entity type); the vast
		// majority of calls (vanilla mobs during chunk load / spawn waves)
		// hit a cached Optional.empty() and skip the getKey + registry walk.
		final Optional<Registry<HitboxProfile>> optRegistry = HITBOX_PROFILE_REGISTRY.registry(registryAccess);
		if (optRegistry.isEmpty()) {
			// Identical to the uncached path: DatapackRegistry.get() returns
			// null (=> Optional.empty()) when the registry is absent.
			return Optional.empty();
		}
		final Registry<HitboxProfile> registry = optRegistry.get();
		Map<EntityType<?>, Optional<HitboxProfile>> byType = PROFILE_LOOKUP_CACHE.get(registry);
		if (byType == null) {
			if (PROFILE_LOOKUP_CACHE.size() >= PROFILE_CACHE_MAX_REGISTRIES) {
				// Leak guard for registries of departed worlds/servers that no
				// event cleared (e.g. client-side synced registries). Clearing
				// is neutral: next lookups just re-resolve.
				PROFILE_LOOKUP_CACHE.clear();
			}
			byType = PROFILE_LOOKUP_CACHE.computeIfAbsent(registry, r -> new ConcurrentHashMap<>());
		}
		Optional<HitboxProfile> result = byType.get(entityType);
		if (result == null) {
			// Exact legacy resolve path (getKey + datapack lookup), consulting
			// the very registryAccess this registry instance came from.
			result = getHitboxProfile(BuiltInRegistries.ENTITY_TYPE.getKey(entityType), registryAccess);
			byType.put(entityType, result);
		}
		return result;
	}

	/**
	 * OPT-001: generation stamp consumed by the per-entity profile cache in
	 * {@code IMultipartEntity.getHitboxProfile()}. Bumped by
	 * {@link #invalidateProfileCache()} so entity-held profiles are
	 * re-resolved after any datapack reload.
	 */
	public static int getProfileCacheGeneration() {
		return PROFILE_CACHE_GENERATION.get();
	}

	/**
	 * OPT-001/OPT-018: drops all memoized profile lookups and invalidates
	 * every per-entity cached profile (via the generation bump). Wired to
	 * AddReloadListenerEvent (server start + /reload) and ServerStoppedEvent
	 * in {@code GameEventHandler}. Note that in 1.21.1 dynamic-registry
	 * content is only (re)built at world load, so a live Registry instance
	 * can never change behind the cache — this hook is the defense-in-depth
	 * layer OPT-001 mandates, not the sole correctness mechanism.
	 */
	public static void invalidateProfileCache() {
		PROFILE_LOOKUP_CACHE.clear();
		PROFILE_CACHE_GENERATION.incrementAndGet();
	}

}
