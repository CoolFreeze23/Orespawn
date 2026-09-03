package de.dertoaster.multihitboxlib.mixin.geckolib;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.dertoaster.multihitboxlib.api.IMHLibExtendedRenderLayer;
import software.bernie.geckolib.renderer.GeoRenderer;

@Mixin(value = GeoRenderer.class, priority = Integer.MAX_VALUE - 1)
public interface MixinGeoRenderer {

	@Unique
	default void _mhlib_callLayers(final Consumer<IMHLibExtendedRenderLayer> runPerLayer) {
		GeoRenderer self = (GeoRenderer) this;
		for (Object layerGeo : self.getRenderLayers()) {
			if (layerGeo instanceof IMHLibExtendedRenderLayer mhlibExtension) {
				runPerLayer.accept(mhlibExtension);
			}
		}
	}

	// OPT-028 (2026-09-04): descriptor-exact selector for the interface default GeoRenderer<T extends
	// GeoAnimatable>.renderRecursively (T erases to GeoAnimatable; javap -s over the 4.8.4 jar shows one method).
	@Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V", at = @At("HEAD"), remap = false)
	default void mixinRenderRecursivelyStart(CallbackInfo ci) {
		this._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyStart);
	}

	@Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V", at = @At("TAIL"), remap = false)
	default void mixinRenderRecursivelyEnd(CallbackInfo ci) {
		this._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyEnd);
	}

}
