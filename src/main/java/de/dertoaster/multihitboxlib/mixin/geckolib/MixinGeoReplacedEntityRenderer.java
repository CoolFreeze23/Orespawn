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
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

@Mixin(value = GeoReplacedEntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class MixinGeoReplacedEntityRenderer<E extends Entity, T extends GeoAnimatable> extends EntityRenderer<E> implements GeoRenderer<T> {

	protected MixinGeoReplacedEntityRenderer(EntityRendererProvider.Context pContext) {
		super(pContext);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Inject(
			method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lsoftware/bernie/geckolib/model/GeoModel;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;)V",
			at = @At("TAIL")
			)
	private void mixinConstructor(CallbackInfo ci) {
		GeoReplacedEntityRenderer self = (GeoReplacedEntityRenderer)(Object)this;
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

	// OPT-028 (2026-09-04): descriptor-exact selector. GeoReplacedEntityRenderer<E, T extends GeoAnimatable> has
	// ONE renderRecursively, whose T erases to GeoAnimatable, and no bridge (javap -s over the 4.8.4 jar); the
	// full erased descriptor pins the exact signature the layer lifecycle depends on. remap = false as before.
	@Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V", at = @At("HEAD"), remap = false)
	private void mixinRenderRecursivelyStart(CallbackInfo ci) {
		this._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyStart);
	}

	@Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V", at = @At("TAIL"), remap = false)
	private void mixinRenderRecursivelyEnd(CallbackInfo ci) {
		this._mhlib_callLayers(IMHLibExtendedRenderLayer::onRenderRecursivelyEnd);
	}
	
}
