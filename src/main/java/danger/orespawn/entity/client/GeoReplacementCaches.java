package danger.orespawn.entity.client;

import de.dertoaster.multihitboxlib.util.MHLibCounters;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * OPT-029: the registry of every replaced renderer's animation cache, keyed by
 * the entity type it replaces, and the eviction entry points the client evictor
 * ({@code danger.orespawn.client.GeoReplacementCacheEvictor}) calls.
 *
 * <p>Loads on the game-test server: no client class is referenced (the caches
 * are GeckoLib's common {@code AnimatableInstanceCache} family; {@code EntityType}
 * and {@code Entity} are common). The renderer that owns a replacement
 * ({@link OreSpawnGeoReplacedEntityRenderer}) registers the replacement's cache from
 * its constructor, i.e. when the renderer is built on a bootstrapped client — not the
 * replacement's constructor, which the headless s4 probe runs without a bootstrapped
 * registry (the descriptor's entity-type supplier would trip it). A renderer is
 * rebuilt on every resource reload ({@code EntityRenderDispatcher.onResourceManagerReload}
 * runs every provider again, on the client main thread), so {@link #register}
 * REPLACES the entry: the old renderer, its replacement singleton and its cache
 * are dropped together. The previous cache is returned so a test can restore it.</p>
 *
 * <p>Thread argument. The cache map inside each {@link OreSpawnAnimatableInstanceCache}
 * is a plain {@code Long2ObjectOpenHashMap}; it is only ever touched from the
 * client main thread, from three places, all read off the NeoForge 21.1.223 and
 * GeckoLib 4.8.4 bytecode:</p>
 * <ul>
 *   <li>the draw: {@code GeoModel.handleAnimations(T, long, AnimationState, float)}
 *       calls {@code getAnimatableInstanceCache()} (offset 6) and
 *       {@code getManagerForId(long)} (offset 12) from the entity render pass,
 *       which {@code Minecraft.runTick(boolean)} runs through
 *       {@code GameRenderer.render} (offset 422) on the same thread as
 *       {@code tick()} (offset 171) and {@code runAllTasks()} (offset 120);</li>
 *   <li>the per-entity eviction: {@code ClientLevel$EntityCallbacks.onTrackingEnd(Entity)}
 *       calls {@code Entity.onRemovedFromLevel()} (offset 19) and posts
 *       {@code new EntityLeaveLevelEvent(entity, level)} on {@code NeoForge.EVENT_BUS}
 *       (offsets 22-37). It is reached from {@code ClientLevel.removeEntity(int,
 *       RemovalReason)} ({@code Entity.setRemoved}, offset 20), whose packet
 *       caller {@code ClientPacketListener.handleRemoveEntities} first runs
 *       {@code PacketUtils.ensureRunningOnSameThread(packet, this, minecraft)}
 *       (offset 6): the client main thread. ({@code onDestroyed(Entity)} is a
 *       bare {@code return} in this build; it is not the hook.)</li>
 *   <li>the level eviction: {@code Minecraft.setLevel(ClientLevel,
 *       ReceivingLevelScreen$Reason)} posts {@code new LevelEvent$Unload(this.level)}
 *       when a level is replaced (offsets 7-26, ahead of {@code updateLevelInEngines}
 *       at 57) and {@code Minecraft.disconnect(Screen, boolean)} posts it when the
 *       level is dropped (offsets 130-149, ahead of {@code updateLevelInEngines(null)}
 *       at 211); both are {@code Minecraft} methods run on its own thread.</li>
 * </ul>
 * <p>An integrated server posts the same two events for its server-side levels
 * and entities on the SERVER thread, with the same entity ids; the evictor's
 * {@code level.isClientSide()} guard is what keeps that thread out of the render
 * thread's map. This registry's own map is a {@code ConcurrentHashMap} because
 * registration happens at renderer construction and reads at eviction -- in
 * production both on the client main thread; the game-test server exercises the
 * registry from the server thread alone.</p>
 *
 * <p>Measurement (MHLib counters, {@code -Dmhlib.counters=true}): {@link #EVICTIONS}
 * counts managers evicted in the dump interval ({@value #EVICTIONS_COUNTER};
 * incremented only under {@code MHLibCounters.ENABLED}, like every other counter)
 * and the gauge {@value #MANAGERS_HELD_GAUGE} reports {@link #managersHeld()} at
 * each dump, appended after the counters and never reset.</p>
 */
public final class GeoReplacementCaches {

    /** Counter name: managers evicted in the dump interval (per-entity leave plus level unload). */
    public static final String EVICTIONS_COUNTER = "orespawn.geo.evictions";
    /** Gauge name: managers currently held by every registered replacement cache. */
    public static final String MANAGERS_HELD_GAUGE = "orespawn.geo.managers_held";

    private static final Map<EntityType<?>, OreSpawnAnimatableInstanceCache> CACHES = new ConcurrentHashMap<>();

    /** Managers evicted; call sites guard with {@link MHLibCounters#ENABLED}. */
    public static final MHLibCounters.Counter EVICTIONS = MHLibCounters.counter(EVICTIONS_COUNTER);

    static {
        MHLibCounters.gauge(MANAGERS_HELD_GAUGE, GeoReplacementCaches::managersHeld);
    }

    private GeoReplacementCaches() {
    }

    /**
     * Forces class initialisation -- the registration of the counter and the gauge
     * above -- so both names appear in every dump of a client session even while
     * every species draws through its classic renderer and no cache is ever
     * registered. Called from the client evictor's static initialiser.
     */
    public static void ensureCountersRegistered() {
    }

    /** Registers (replacing) the cache for {@code type}; returns the previous cache, or {@code null}. */
    public static OreSpawnAnimatableInstanceCache register(EntityType<?> type, OreSpawnAnimatableInstanceCache cache) {
        return CACHES.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(cache, "cache"));
    }

    /** Removes the cache registered for {@code type}; returns it, or {@code null} when none was registered. */
    public static OreSpawnAnimatableInstanceCache unregister(EntityType<?> type) {
        return CACHES.remove(type);
    }

    /** The cache registered for {@code type}, or {@code null}. */
    public static OreSpawnAnimatableInstanceCache cacheFor(EntityType<?> type) {
        return CACHES.get(type);
    }

    /**
     * Drops the manager the entity's type cache holds under the entity's id.
     * {@code false}, and nothing touched, when the type has no registered cache
     * or the id holds no manager.
     */
    public static boolean evict(Entity entity) {
        OreSpawnAnimatableInstanceCache cache = CACHES.get(entity.getType());
        if (cache == null) {
            return false;
        }
        boolean evicted = cache.evict(entity.getId());
        if (evicted && MHLibCounters.ENABLED) {
            EVICTIONS.increment();
        }
        return evicted;
    }

    /** Drops every manager of every registered cache; returns how many were held. */
    public static int evictAll() {
        int evicted = 0;
        for (OreSpawnAnimatableInstanceCache cache : CACHES.values()) {
            evicted += cache.evictAll();
        }
        if (evicted > 0 && MHLibCounters.ENABLED) {
            EVICTIONS.add(evicted);
        }
        return evicted;
    }

    /** The gauge: managers currently held across every registered cache. */
    public static long managersHeld() {
        long held = 0;
        for (OreSpawnAnimatableInstanceCache cache : CACHES.values()) {
            held += cache.size();
        }
        return held;
    }
}
