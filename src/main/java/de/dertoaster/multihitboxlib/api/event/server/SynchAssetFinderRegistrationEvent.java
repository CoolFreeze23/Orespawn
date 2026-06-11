package de.dertoaster.multihitboxlib.api.event.server;

import java.util.Map;

import de.dertoaster.multihitboxlib.api.event.AbstractRegistrationEvent;
import de.dertoaster.multihitboxlib.assetsynch.assetfinders.AbstractAssetFinder;
import net.minecraft.resources.ResourceLocation;

/**
 * Posted on the GAME bus ({@code NeoForge.EVENT_BUS}, see {@code AssetEnforcement.initializeAssetFinders})
 * during common setup so other mods can contribute asset finders.
 *
 * <p>Must NOT implement {@code IModBusEvent}: the game bus rejects mod-bus event types at
 * post time (BUG-001 family).</p>
 */
public class SynchAssetFinderRegistrationEvent extends AbstractRegistrationEvent<ResourceLocation, AbstractAssetFinder> {

	public SynchAssetFinderRegistrationEvent(Map<ResourceLocation, AbstractAssetFinder> map) {
		super(map);
	}

}
