package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/**
 * Shared base for every OreSpawn GeckoLib replacement renderer.
 *
 * <p>Left deliberately plain, like {@link QueenRenderer}: MultiHitboxLib's
 * {@code MixinGeoReplacedEntityRenderer} attaches its bone-collector layer to
 * this class's constructor, and that layer is a no-op for entities without a
 * bone-synced hitbox profile.</p>
 *
 * <p>Open item carried from the Phase G review: vanilla {@code ModelPart}
 * emits cube faces in down/up/west/north/east/south order while GeckoLib
 * emits west/east/north/south/up/down. Opaque rigs are unaffected; any
 * translucent or self-overlapping rig needs a face-order pass here before it
 * replaces its classic renderer.</p>
 */
public abstract class OreSpawnGeoReplacedEntityRenderer<E extends Entity, A extends OreSpawnGeoReplacement<E>>
        extends GeoReplacedEntityRenderer<E, A> {
    private final GeoReplacementDescriptor<E> descriptor;

    protected OreSpawnGeoReplacedEntityRenderer(EntityRendererProvider.Context context, A replacement) {
        super(context, new OreSpawnGeoReplacementModel<E, A>(replacement.descriptor()), replacement);
        this.descriptor = replacement.descriptor();
        this.shadowRadius = this.descriptor.shadowRadius();
    }

    @Override
    public ResourceLocation getTextureLocation(E entity) {
        return this.descriptor.texture(this.descriptor.requireEntity(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(A animatable) {
        return this.descriptor.texture(currentEntity());
    }

    @Override
    public void preRender(PoseStack poseStack, A animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        if (!isReRender) {
            this.descriptor.applyScale(currentEntity(), poseStack, partialTick);
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }

    /**
     * GeckoLib 4.8.4's replaced renderer calls {@code EntityRenderer.render} from
     * BOTH {@code postRender} and {@code renderFinal}, drawing the name tag twice
     * (the first time under the model transform) and, with its own
     * {@code renderLeash} on top, the leash up to three times. Vanilla draws each
     * once from {@code EntityRenderer.render}, which {@code renderFinal} still
     * calls — so the extra two call sites are neutralized here.
     */
    @Override
    public void postRender(PoseStack poseStack, A animatable, BakedGeoModel model,
                           MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                           float partialTick, int packedLight, int packedOverlay, int colour) {
    }

    @Override
    public <H extends Entity, M extends Mob> void renderLeash(M mob, float partialTick, PoseStack poseStack,
                                                              MultiBufferSource bufferSource, H leashHolder) {
    }

    /** {@code MobRenderer} scales the shadow by the entity's age scale (0.5 for babies). */
    @Override
    protected float getShadowRadius(E entity) {
        float radius = this.descriptor.shadowRadius();
        return entity instanceof LivingEntity living ? radius * living.getAgeScale() : radius;
    }

    /** GeckoLib sets the current entity before {@code defaultRender} and clears it only after post-render cleanup. */
    protected final E currentEntity() {
        return this.descriptor.requireEntity(getCurrentEntity());
    }
}
