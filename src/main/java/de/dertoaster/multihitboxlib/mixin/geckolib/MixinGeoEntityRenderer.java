package de.dertoaster.multihitboxlib.mixin.geckolib;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.dertoaster.multihitboxlib.api.IMHLibExtendedRenderLayer;
import de.dertoaster.multihitboxlib.client.geckolib.renderlayer.GeckolibBoneInformationCollectorLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

@Mixin(value = GeoEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class MixinGeoEntityRenderer<T extends Entity & GeoAnimatable> extends EntityRenderer<T> implements GeoRenderer<T> {

	protected MixinGeoEntityRenderer(EntityRendererProvider.Context pContext) {
		super(pContext);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Inject(
			method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lsoftware/bernie/geckolib/model/GeoModel;)V",
			at = @At("TAIL")
			)
	private void mixinConstructor(CallbackInfo ci) {
		GeoEntityRenderer self = (GeoEntityRenderer)(Object)this;
		self.addRenderLayer(new GeckolibBoneInformationCollectorLayer(self));
	}

	@Unique
	private void _mhlib_callLayers(final Consumer<IMHLibExtendedRenderLayer> runPerLayer) {
		GeoRenderer self = (GeoRenderer) this;
		for (Object layerGeo : self.getRenderLayers()) {
			if (layerGeo instanceof IMHLibExtendedRenderLayer mhlibExtension) {
				runPerLayer.accept(mhlibExtension);
			}
		}
	}

	// OPT-028 (2026-09-04): descriptor-exact selector. GeckoLib 4.8.4's GeoEntityRenderer carries the typed
	// renderRecursively(PoseStack, Entity, GeoBone, ...) AND a synthetic bridge renderRecursively(PoseStack,
	// GeoAnimatable, GeoBone, ...) that delegates to it (javap -s over the 4.8.4 jar), and every bone enters
	// through the bridge (GeoRenderer.renderChildBones' erased invokeinterface). The bare name hooked both, so
	// onRenderRecursivelyStart/End ran twice per bone per frame (220 instead of 110 for the Queen); the full
	// descriptor names the typed method only. remap = false: NeoForge runs Mojang names at runtime, so the
	// literal descriptor is the runtime one.
	@Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/Entity;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V", at = @At("HEAD"), remap = false)
	private void mixinRenderRecursivelyStart(CallbackInfo ci) {
		this._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyStart);
	}

	@Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/Entity;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V", at = @At("TAIL"), remap = false)
	private void mixinRenderRecursivelyEnd(CallbackInfo ci) {
		this._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyEnd);
	}

}
