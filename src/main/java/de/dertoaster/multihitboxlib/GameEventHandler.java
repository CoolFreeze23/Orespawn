package de.dertoaster.multihitboxlib;

import de.dertoaster.multihitboxlib.assetsynch.AssetEnforcement;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Game-bus login hook that pushes synchronized asset data (hitbox profiles, models) to
 * each player on join.
 *
 * <p>{@code PlayerEvent.PlayerLoggedInEvent} is a GAME-bus event; the previous explicit
 * MOD-bus registration crashed at startup (BUG-002). The bus attribute is deprecated on
 * this NeoForge version, so registration is left to per-listener auto-detection.</p>
 */
@EventBusSubscriber(modid = Constants.MODID)
public class GameEventHandler {

	@SubscribeEvent
	public static void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer sp && sp != null) {
			AssetEnforcement.sendSynchData(sp);
		}
	}

	/**
	 * OPT-001/OPT-018: invalidate the hitbox-profile lookup caches whenever
	 * server data (re)loads. AddReloadListenerEvent fires on the initial
	 * datapack load at server start AND on every {@code /reload}, which is
	 * exactly the invalidation hook OPT-001 requires for the cached
	 * profiles. Clearing a cache is always behavior-neutral — subsequent
	 * lookups re-resolve from the authoritative datapack registry.
	 */
	@SubscribeEvent
	public static void onAddReloadListeners(AddReloadListenerEvent event) {
		MHLibDatapackLoaders.invalidateProfileCache();
	}

	/**
	 * OPT-001/OPT-018: hygiene clear when a (integrated or dedicated)
	 * server shuts down, so no registry of a closed world is retained and
	 * the next world starts from a cold, provably-fresh cache.
	 */
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		MHLibDatapackLoaders.invalidateProfileCache();
	}

}
