package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Per-registry description of a client-only GeckoLib replacement renderer.
 *
 * <p>The registry identity and the geo/animation/texture triple are fixed.
 * Anything that legitimately varies by species (texture variant, render
 * scale) is a hook over the actual entity being drawn. The entity class
 * itself is never touched: this is what lets a species move to GeckoLib
 * without inheriting {@code GeoEntity}.</p>
 */
public abstract class GeoReplacementDescriptor<E extends Entity> {
    private final Supplier<? extends EntityType<? extends E>> entityType;
    private final Class<E> entityClass;
    private final ResourceLocation modelResource;
    private final ResourceLocation animationResource;
    private final ResourceLocation textureResource;
    private final float shadowRadius;

    protected GeoReplacementDescriptor(Supplier<? extends EntityType<? extends E>> entityType,
                                       Class<E> entityClass,
                                       ResourceLocation modelResource,
                                       ResourceLocation animationResource,
                                       ResourceLocation textureResource,
                                       float shadowRadius) {
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
        this.modelResource = Objects.requireNonNull(modelResource, "modelResource");
        this.animationResource = Objects.requireNonNull(animationResource, "animationResource");
        this.textureResource = Objects.requireNonNull(textureResource, "textureResource");
        this.shadowRadius = shadowRadius;
    }

    public final EntityType<?> entityType() {
        return Objects.requireNonNull(this.entityType.get(), "replacement entity type");
    }

    public final ResourceLocation modelResource() {
        return this.modelResource;
    }

    public final ResourceLocation animationResource() {
        return this.animationResource;
    }

    public final ResourceLocation textureResource() {
        return this.textureResource;
    }

    public final float shadowRadius() {
        return this.shadowRadius;
    }

    /** The replaced renderer only ever sees its own registry's entities; anything else is a wiring bug. */
    public final E requireEntity(Object candidate) {
        if (!(candidate instanceof Entity entity)
                || entity.getType() != entityType()
                || !this.entityClass.isInstance(entity)) {
            throw new IllegalArgumentException(
                    "Replacement for " + entityType() + " received " + candidate);
        }
        return this.entityClass.cast(entity);
    }

    /** Texture for the entity being drawn; the descriptor texture by default. */
    public ResourceLocation texture(E entity) {
        return this.textureResource;
    }

    /** Applied before GeckoLib's own scaling, i.e. where the classic renderer scaled its pose stack. */
    public void applyScale(E entity, PoseStack poseStack, float partialTick) {
    }

    /** Applied after GeckoLib's yaw/death rotations, i.e. where a classic {@code setupRotations} override added its own. */
    public void applyRotations(E entity, PoseStack poseStack, float ageInTicks, float partialTick) {
    }
}
