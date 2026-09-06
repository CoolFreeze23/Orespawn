package de.dertoaster.multihitboxlib.client.geckolib;

import java.util.function.Consumer;

import de.dertoaster.multihitboxlib.api.IMHLibExtendedRenderLayer;
import de.dertoaster.multihitboxlib.client.EntityRenderEventHandlerCommonLogic;
import de.dertoaster.multihitboxlib.client.IBoneInformationCollectorLayerCommonLogic;
import de.dertoaster.multihitboxlib.util.MHLibCollectorProbe;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GeckolibEntityRenderEventHandler extends EntityRenderEventHandlerCommonLogic {

	static void callLayers(GeoRenderer<?> renderer, Consumer<IMHLibExtendedRenderLayer> runPerLayer) {
		for(GeoRenderLayer<?> layerGeo : renderer.getRenderLayers()) {
			if (layerGeo instanceof IMHLibExtendedRenderLayer mhlibExtension) {
				runPerLayer.accept(mhlibExtension);
			}
		}
	}

	public static void onPostRenderEntity(GeoRenderEvent.Entity.Post event) {
		Entity animatable = event.getEntity();
		performCommonLogic(event.getPoseStack(), event.getRenderer(), event.getBufferSource(), event.getPartialTick(), event.getPackedLight(), animatable);
		performGlibLogic(event.getRenderer(), animatable);
		if (!event.getEntity().isMultipartEntity()) {
			return;
		}
		callLayers(event.getRenderer(), IMHLibExtendedRenderLayer::onPostRender);
		// Slice (d): close the Pre->Post span of this multipart entity (client.collector_ns / _alloc_bytes).
		if (MHLibCounters.ENABLED) {
			MHLibCollectorProbe.end(animatable);
		}
	}

	public static void onPreRenderEntity(GeoRenderEvent.Entity.Pre event) {
		if (!event.getEntity().isMultipartEntity()) {
			return;
		}
		// Slice (d): open the Pre->Post span of this multipart entity, before MHLib's own pre logic.
		if (MHLibCounters.ENABLED) {
			MHLibCollectorProbe.begin(event.getEntity());
		}
		// BUG-044: decide this pass from the entity's render-tick stamp before its bones are walked.
		performGlibPreLogic(event.getRenderer(), event.getEntity());
		callLayers(event.getRenderer(), IMHLibExtendedRenderLayer::onPreRender);
	}

	public static void onPreRenderReplacedEntity(GeoRenderEvent.ReplacedEntity.Pre event) {
		if (!event.getReplacedEntity().isMultipartEntity()) {
			return;
		}
		// Slice (d): the same span on the replaced path, keyed on the actual entity.
		if (MHLibCounters.ENABLED) {
			MHLibCollectorProbe.begin(event.getReplacedEntity());
		}
		// BUG-044: the replaced path keys the stamp on the actual entity (GeoReplacedEntityRenderer.getCurrentEntity()).
		performGlibPreLogic(event.getRenderer(), event.getReplacedEntity());
		callLayers(event.getRenderer(), IMHLibExtendedRenderLayer::onPreRender);
	}

	public static void onPostRenderReplacedEntity(GeoRenderEvent.ReplacedEntity.Post event) {
		Entity animatable = event.getReplacedEntity();
		performCommonLogic(event.getPoseStack(), event.getRenderer(), event.getBufferSource(), event.getPartialTick(), event.getPackedLight(), animatable);
		if (!animatable.isMultipartEntity()) {
			return;
		}
		// BUG-044: advance the actual entity's stamp on this path too (before the fix the collector's post
		// hook only ran for GeoEntityRenderer entities). Runs BEFORE the layers' onPostRender, which nulls
		// the running vectors this hook resets.
		performGlibLogic(event.getRenderer(), animatable);
		callLayers(event.getRenderer(), IMHLibExtendedRenderLayer::onPostRender);
		// Slice (d): close the span (see onPostRenderEntity).
		if (MHLibCounters.ENABLED) {
			MHLibCollectorProbe.end(animatable);
		}
	}

	/** BUG-044: the collector's per-entity pre hook (onPreRender(Entity)) for every collector layer on the renderer. */
	private static void performGlibPreLogic(GeoRenderer<?> geoRenderer, Entity entityBeingRendered) {
		for (GeoRenderLayer<?> gle : geoRenderer.getRenderLayers()) {
			if (gle instanceof IBoneInformationCollectorLayerCommonLogic<?> bicl) {
				bicl.onPreRender(entityBeingRendered);
			}
		}
	}

	private static void performGlibLogic(GeoRenderer<?> geoRenderer, Entity entitybeingRenderer) {
		for(GeoRenderLayer<?> gle : geoRenderer.getRenderLayers()) {
			if(gle instanceof IBoneInformationCollectorLayerCommonLogic<?> bicl) {
				bicl.onPostRender(entitybeingRenderer);
			}
		}
	}

}
