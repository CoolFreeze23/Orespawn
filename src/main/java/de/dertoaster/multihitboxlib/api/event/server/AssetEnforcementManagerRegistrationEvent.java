package de.dertoaster.multihitboxlib.api.event.server;

import java.util.Map;

import de.dertoaster.multihitboxlib.api.event.AbstractRegistrationEvent;
import de.dertoaster.multihitboxlib.assetsynch.AbstractAssetEnforcementManager;
import net.minecraft.resources.ResourceLocation;

/**
 * Posted on the GAME bus ({@code NeoForge.EVENT_BUS}, see {@code AssetEnforcement.initializeManagers})
 * during common setup so other mods can contribute enforcement managers.
 *
 * <p>Must NOT implement {@code IModBusEvent}: the game bus rejects mod-bus event types at
 * post time, and the previous marker made {@code AssetEnforcement.init()} throw (BUG-001 family).</p>
 */
public class AssetEnforcementManagerRegistrationEvent extends AbstractRegistrationEvent<ResourceLocation, AbstractAssetEnforcementManager> {

	public AssetEnforcementManagerRegistrationEvent(Map<ResourceLocation, AbstractAssetEnforcementManager> map) {
		super(map);
	}
	
}
