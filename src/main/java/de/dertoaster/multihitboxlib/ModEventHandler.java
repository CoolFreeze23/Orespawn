package de.dertoaster.multihitboxlib;

import de.dertoaster.multihitboxlib.api.event.server.AssetEnforcementManagerRegistrationEvent;
import de.dertoaster.multihitboxlib.api.event.server.SynchAssetFinderRegistrationEvent;
import de.dertoaster.multihitboxlib.assetsynch.assetfinders.HitboxProfileAssetFinder;
import de.dertoaster.multihitboxlib.assetsynch.impl.GlibAnimationEnforcementManager;
import de.dertoaster.multihitboxlib.assetsynch.impl.GlibModelEnforcementManager;
import de.dertoaster.multihitboxlib.assetsynch.impl.TextureEnforcementManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Registers MHLib's built-in asset enforcement managers and asset finders.
 *
 * <p>GAME bus: both registration events are posted via {@code NeoForge.EVENT_BUS.post(...)}
 * in {@code AssetEnforcement} (called from common setup), so a MOD-bus listener would
 * never receive them (BUG-001 family). The bus attribute is deprecated on this NeoForge
 * version, so registration is left to per-listener auto-detection.</p>
 */
@EventBusSubscriber(modid = Constants.MODID)
public class ModEventHandler {

	@SubscribeEvent
	public static void onAssetEnforcementRegistration(AssetEnforcementManagerRegistrationEvent event) {
		// TODO: Log
		// Textures
		if (!event.tryAdd(MHLibMod.prefixAssesEnforcementManager("textures"), new TextureEnforcementManager())) {
			
		}
		// Dependency specific
		if (Constants.Dependencies.GECKOLIB_LOADED.get()) {
			if (!event.tryAdd(MHLibMod.prefixAssesEnforcementManager("models/geckolib"), new GlibModelEnforcementManager())) {
				
			}
			if (!event.tryAdd(MHLibMod.prefixAssesEnforcementManager("animations/geckolib"), new GlibAnimationEnforcementManager())) {
				
			}
		}
		// AzureLib asset enforcement managers stripped: the vendored MHLib in
		// OreSpawn excludes all AzureLib-touching classes so the mod's class
		// graph never references mod.azure.* (which would NoClassDefFoundError
		// at runtime since AzureLib is not on the classpath).
	}
	
	@SubscribeEvent
	public static void onAssetFinderRegistration(SynchAssetFinderRegistrationEvent event) {
		if (!event.tryAdd(MHLibMod.prefixAssetFinder("hitbox_profile"), new HitboxProfileAssetFinder()));
	}
	
}
