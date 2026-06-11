package de.dertoaster.multihitboxlib;

import de.dertoaster.multihitboxlib.assetsynch.AssetEnforcement;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

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
	
}
