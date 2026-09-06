package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
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

    /** GeckoLib's own default colour: {@code GeoRenderer.getRenderColor} returns {@code Color.WHITE}, {@code new Color(-1)} (4.8.4 bytecode). */
    public static final int WHITE = -1;

    /**
     * ENT-S-146: the light level a fullbright species ({@link #fullBright}) answers for BOTH
     * {@code getBlockLightLevel} and {@code getSkyLightLevel}, so {@code EntityRenderer.getPackedLightCoords}
     * (21.1.223 bytecode 0-24) packs {@code LightTexture.pack(15, 15)} = 15728880 = {@code LightTexture
     * .FULL_BRIGHT}, whose lightmap texel is (240, 240). The seam's own constant (refuter A on ENT-S-146,
     * D4): the shared renderer reads it here, never from a species model; a species' classic renderer
     * carries its own transcription of the same level (e.g. {@code ModelPurplePower.LIGHT_LEVEL}, pinned
     * equal by the game-test row).
     */
    public static final int FULL_BRIGHT_LEVEL = 15;

    /**
     * ENT-S-146: the render-type FUNCTION the candidate draws its visible body with, or {@code null} for
     * GeckoLib's own ({@code GeoReplacedEntityRenderer.getRenderType} 47-61 falls through to
     * {@code GeoModel.getRenderType} = {@code RenderType.entityCutoutNoCull}, 4.8.4 bytecode offset 1).
     * The classic side holds its render type the same way - {@code Model.renderType}, a
     * {@code Function<ResourceLocation, RenderType>} stored by {@code EntityModel(Function)} and applied
     * by {@code Model.renderType(ResourceLocation)}, which {@code LivingEntityRenderer.getRenderType}
     * returns for a visible body (offsets 17-30) - so a species hands over the classic model's OWN
     * function object and the two renderers cannot drift apart. Returned as the function rather than
     * the {@code RenderType} (refuter B on ENT-S-146, D3) because the headless parity probe cannot
     * initialise {@code RenderType} at all ({@code RenderType.<clinit>} reaches {@code Items} through
     * {@code ItemRenderer.<clinit>} and trips {@code Bootstrap.checkBootstrapCalled}; OPT-029 R0,
     * measured 2026-09-06) but can observe a function object: its identity against the classic model's
     * field and the {@code RenderType} factory its owner class references. {@link
     * OreSpawnGeoReplacedEntityRenderer#getRenderType} applies it to the texture on the same condition
     * as vanilla (the entity not invisible) and leaves GeckoLib's invisible / glowing branches alone.
     */
    public Function<ResourceLocation, RenderType> renderType(E entity) {
        return null;
    }

    /**
     * ENT-S-146: the ARGB colour every vertex of the candidate is multiplied by - the colour int
     * {@code GeoRenderer.defaultRender} takes from {@code getRenderColor(...).argbInt()} (offsets 4-18)
     * and hands to every {@code addVertex} ({@code createVerticesOfQuad}, offset 81). {@link #WHITE}
     * (the default) keeps GeckoLib's own. The classic side's counterpart is the colour argument of
     * {@code ModelPart.render}.
     */
    public int renderColor(E entity, float partialTick) {
        return WHITE;
    }

    /**
     * ENT-S-146: {@code true} to draw the candidate fullbright - both light levels 15, so
     * {@code EntityRenderer.getPackedLightCoords} (0-24) packs {@code LightTexture.FULL_BRIGHT} into the
     * light the dispatcher hands {@code render}; the {@code MagmaCubeRenderer} idiom, the same the classic
     * renderer of such a species uses. {@code false} (the default) leaves the world's light.
     */
    public boolean fullBright(E entity) {
        return false;
    }

    /**
     * ENT-S-146: {@code true} when the rig's within-cube face order is visible - a blending render type -
     * so the seam expects the shipped geo to carry {@link FaceOrder#KEY} and warns once when a resource
     * pack dropped it; {@code false} (the default) for an opaque rig, where an absent key is the norm.
     */
    public boolean cubeFaceOrderRequired() {
        return false;
    }
}
