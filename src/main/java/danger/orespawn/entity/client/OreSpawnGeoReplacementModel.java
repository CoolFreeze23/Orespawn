package danger.orespawn.entity.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

/** The one GeoModel shared by every replacement: resources and the code-driven pose hook come from the descriptor and animatable. */
public final class OreSpawnGeoReplacementModel<E extends Entity, A extends OreSpawnGeoReplacement<E>>
        extends GeoModel<A> {
    private final GeoReplacementDescriptor<E> descriptor;
    /** G2: the bake {@link DrawOrder#apply} last ran on; a resource reload replaces it. */
    private BakedGeoModel ordered;

    public OreSpawnGeoReplacementModel(GeoReplacementDescriptor<E> descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public ResourceLocation getModelResource(A animatable) {
        return this.descriptor.modelResource();
    }

    @Override
    public ResourceLocation getAnimationResource(A animatable) {
        return this.descriptor.animationResource();
    }

    @Override
    public ResourceLocation getTextureResource(A animatable) {
        return this.descriptor.textureResource();
    }

    /**
     * G2 root-order contract: the cached bake is sorted into the classic draw order once
     * per bake, before its first draw. GeckoLib 4.8.4 {@code GeoModel.getBakedModel}
     * (bytecode) fetches {@code GeckoLibCache.getBakedModels().get(location)} (offsets
     * 0-9), throws when absent (13-42) and, when the instance differs from
     * {@code currentModel}, re-registers the processor's bones and remembers it (43-61);
     * {@code GeoRenderer.defaultRender} calls it on every draw (46-49).
     * {@code GeckoLibCache.reload} bakes every model again into fresh
     * {@code Object2ObjectOpenHashMap}s (0-16; {@code lambda$loadModels$5} constructs each
     * {@code BakedGeoModel} anew at 94-110) and {@code lambda$reload$0} swaps
     * {@code MODELS} (4-5), so after a resource reload the instance changes and the
     * identity test below runs the reorder again. The order is read from the geo
     * resource itself ({@link DrawOrder#KEY}, written by the converter next to the bones
     * it orders), through the same resource manager GeckoLib loaded the bake from. The
     * reorder is idempotent (sorting a sorted list), so a second model instance over the
     * same cached bake is harmless.
     */
    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel baked = super.getBakedModel(location);
        if (baked != this.ordered) {
            DrawOrder.apply(baked, DrawOrder.load(Minecraft.getInstance().getResourceManager(), location));
            this.ordered = baked;
        }
        return baked;
    }

    @Override
    public void setCustomAnimations(A animatable, long instanceId, AnimationState<A> animationState) {
        animatable.applyCustomAnimations(getAnimationProcessor(), animationState);
    }
}
