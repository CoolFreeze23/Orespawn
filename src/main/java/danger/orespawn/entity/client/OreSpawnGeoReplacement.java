package danger.orespawn.entity.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * One replaced animatable per registry entry. GeckoLib keys its per-entity
 * animation state by entity id through this singleton, so the entity class
 * carries no cache, controllers, or GeckoLib interface.
 */
public abstract class OreSpawnGeoReplacement<E extends Entity> implements GeoReplacedEntity {
    private final GeoReplacementDescriptor<E> descriptor;
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    protected OreSpawnGeoReplacement(GeoReplacementDescriptor<E> descriptor) {
        this.descriptor = descriptor;
    }

    public final GeoReplacementDescriptor<E> descriptor() {
        return this.descriptor;
    }

    @Override
    public final EntityType<?> getReplacingEntityType() {
        return this.descriptor.entityType();
    }

    @Override
    public final AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    /**
     * Code-driven pose hook, called by the shared model from
     * {@code GeoModel.setCustomAnimations} after keyframe controllers have
     * run. Species that keep their classic trig animation implement it here
     * with the original formulas; species animated purely by clips leave it.
     */
    protected void applyCustomAnimations(AnimationProcessor<?> processor, AnimationState<?> state) {
    }

    /** The entity being drawn, installed by the replaced renderer before animations run. */
    protected final E entity(AnimationState<?> state) {
        return this.descriptor.requireEntity(state.getData(DataTickets.ENTITY));
    }

    /** Vanilla {@code LivingEntityRenderer#getBob}: integer tick count widened to float, then the partial tick added. */
    protected static float ageInTicks(Entity entity, AnimationState<?> state) {
        return (float) entity.tickCount + state.getPartialTick();
    }

    /**
     * The replaced renderer computes {@code limbSwingAmount} exactly as vanilla
     * {@code LivingEntityRenderer#render} does (walk speed clamped to 1, zero when
     * dead or seated on a vehicle), so it is read from the state, not re-derived.
     */
    protected static float limbSwingAmount(AnimationState<?> state) {
        return state.getLimbSwingAmount();
    }
}
