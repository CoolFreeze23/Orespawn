package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.GeoReplacementCaches;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * OPT-029: evicts a replaced renderer's per-entity GeckoLib animation state when
 * the entity leaves the client level, and every replacement cache when the client
 * level is unloaded or a login completes. Client-only game-bus listeners in the idiom of
 * {@link KeybindHandler}: an {@code @EventBusSubscriber(modid, value = Dist.CLIENT)}
 * on the game bus (the annotation's default bus; {@code OreSpawnClient.ClientEvents}
 * names {@code bus = MOD} because its events are mod-bus events) with static
 * {@code @SubscribeEvent} methods. Nothing here is registered on a dedicated
 * server; the static core {@link #evictIfClient} is what the game tests call.
 *
 * <p>The hooks, from the NeoForge 21.1.223 bytecode: {@code ClientLevel$EntityCallbacks
 * .onTrackingEnd(Entity)} calls {@code Entity.onRemovedFromLevel()} (offset 19) and
 * then posts {@code new EntityLeaveLevelEvent(entity, level)} on
 * {@code NeoForge.EVENT_BUS} (offsets 22-37; {@code onDestroyed(Entity)} is a bare
 * {@code return} in this build). {@code Minecraft.setLevel(ClientLevel,
 * ReceivingLevelScreen$Reason)} posts {@code new LevelEvent$Unload(this.level)} for
 * the level being replaced (offsets 7-26) and {@code Minecraft.disconnect(Screen,
 * boolean)} for the level being dropped (offsets 130-149), each ahead of its
 * {@code updateLevelInEngines} call (57 / 211). One level drop posts NO unload
 * (refuter A, 2026-09-06): {@code Minecraft.clearClientLevel(Screen)} — reached from
 * {@code ClientPacketListener.handleConfigurationStart} (offset 55; a server-driven
 * re-entry into the configuration phase, a transfer or reconfigure) — calls
 * {@code ClientPacketListener.clearLevel()} (10), nulls {@code level} (67) and calls
 * {@code updateLevelInEngines(null)} (72) without a bus post, and the next
 * {@code setLevel} then finds {@code level == null} and posts nothing either. The third
 * hook covers it: {@code ClientPlayerNetworkEvent.LoggingIn}, posted by
 * {@code ClientHooks.firePlayerLogin} (offsets 0-13) from
 * {@code ClientPacketListener.handleLogin} (offset 316, after {@code setLevel} at 181)
 * — every manager of the previous level is dropped before the new level's first draw;
 * after an ordinary login the caches are already empty and the call is a no-op. All three
 * run on the client main thread, the thread that draws; the full argument is on
 * {@link GeoReplacementCaches}.</p>
 *
 * <p>The guard is load-bearing: an integrated server posts {@code EntityLeaveLevelEvent}
 * for its server entities on the SERVER thread with the same entity ids, and a
 * {@code LevelEvent.Unload} per server level when it stops; that thread must never
 * touch the render thread's map, so both listeners return without touching anything
 * unless the event's level {@code isClientSide()}.</p>
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, value = Dist.CLIENT)
public final class GeoReplacementCacheEvictor {

    static {
        // The counter and the gauge exist from mod construction on, whatever the dev switch selects.
        GeoReplacementCaches.ensureCountersRegistered();
    }

    private GeoReplacementCacheEvictor() {
    }

    /** One entity left the client level: drop its manager, if its type draws through a replaced renderer. */
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        evictIfClient(event.getLevel(), event.getEntity());
    }

    /** The client level is going away (disconnect, dimension change, respawn into a new level): drop everything. */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && level.isClientSide()) {
            GeoReplacementCaches.evictAll();
        }
    }

    /**
     * A client login completed ({@code handleLogin}, after {@code setLevel}): drop everything the previous
     * level left behind — the only way out of {@code Minecraft.clearClientLevel}, which posts no unload.
     */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        GeoReplacementCaches.evictAll();
    }

    /**
     * The static core: evicts the entity's manager only for a client level.
     * Returns {@code false} without touching anything unless {@code level.isClientSide()}
     * -- the guard that keeps an integrated server's server-thread leave events out of
     * the render thread's map.
     */
    public static boolean evictIfClient(Level level, Entity entity) {
        if (level == null || entity == null || !level.isClientSide()) {
            return false;
        }
        return GeoReplacementCaches.evict(entity);
    }
}
