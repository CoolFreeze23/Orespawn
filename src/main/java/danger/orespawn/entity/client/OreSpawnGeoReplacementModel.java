package danger.orespawn.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** The one GeoModel shared by every replacement: resources and the code-driven pose hook come from the descriptor and animatable. */
public final class OreSpawnGeoReplacementModel<E extends Entity, A extends OreSpawnGeoReplacement<E>>
        extends GeoModel<A> {
    private final GeoReplacementDescriptor<E> descriptor;

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

    @Override
    public void setCustomAnimations(A animatable, long instanceId, AnimationState<A> animationState) {
        animatable.applyCustomAnimations(getAnimationProcessor(), animationState);
    }
}
