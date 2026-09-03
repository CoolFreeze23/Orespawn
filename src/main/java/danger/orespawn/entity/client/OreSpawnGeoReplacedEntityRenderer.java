package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
    /** ENT-S-094: {@link OreSpawnGeoReplacement#nonLivingRender()}, fixed per species at construction. */
    private final boolean nonLiving;

    protected OreSpawnGeoReplacedEntityRenderer(EntityRendererProvider.Context context, A replacement) {
        super(context, new OreSpawnGeoReplacementModel<E, A>(replacement.descriptor()), replacement);
        this.descriptor = replacement.descriptor();
        this.nonLiving = replacement.nonLivingRender();
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

    /**
     * GeckoLib's own leash draw, neutralized: GeoReplacedEntityRenderer (4.8.4 bytecode)
     * calls it from actuallyRender 59-81 (Mob.getLeashHolder != null, !isReRender) and
     * again from renderFinal 37-59, on top of the vanilla line.
     *
     * <p>ENT-S-094: this does NOT remove the leash for a non-living species, and that is
     * deliberate. The vanilla line is drawn by EntityRenderer.renderLeash, which is PRIVATE
     * (javap 1.21.1: private &lt;E extends Entity&gt; void renderLeash(T, float, PoseStack,
     * MultiBufferSource, E)) and reached from EntityRenderer.render offsets 0-36 (instanceof
     * Leashable -&gt; getLeashHolder -&gt; renderLeash); renderFinal invokes EntityRenderer.render
     * by invokespecial at 0-13 (postRender's second call at 5-18 is the one neutralized above), so
     * the GeckoLib base can still draw the line that way, once. The 1.7.10 plain Render never drew
     * one (orig RenderElevator.java:19; bno has no leash routine), but the classic path has no
     * hook for it either (see ElevatorRenderer), so it is left on both paths rather than made a
     * classic/GeckoLib difference; skipping renderFinal for a non-living species would also drop
     * the NeoForge RenderNameTagEvent post at EntityRenderer.render 39-71.</p>
     */
    @Override
    public <H extends Entity, M extends Mob> void renderLeash(M mob, float partialTick, PoseStack poseStack,
                                                              MultiBufferSource bufferSource, H leashHolder) {
    }

    /**
     * ENT-S-094 non-living mode: GeoReplacedEntityRenderer.applyRotations (4.8.4
     * bytecode) is offsets 0-35 isShaking yaw jitter, 37-67 the SLEEPING-gated
     * yaw, then for a LivingEntity 89-145 the deathTime Z-flip (× getDeathMaxRotation
     * 90), 148-202 the riptide spin, 205-282 the SLEEPING bed rotations and
     * 285-322 the static isEntityUpsideDown flip. A non-living species re-applies
     * only the yaw of offsets 50-65, unconditionally, like the 1.7.10 plain Render
     * (orig RenderElevator.java:30), but from the ENTITY yaw, not {@code rotationYaw}:
     * actuallyRender computes that argument at 120-135 as Mth.rotLerp(partialTick,
     * yBodyRotO, yBodyRot) for a LivingEntity (fconst_0 at 116 for a true non-living
     * entity, which is why the mode lives in the renderer) and passes it at 396,
     * whereas the 1.7.10 RenderManager.renderEntityStatic (bnn.a(sa,F,Z) offsets
     * 88-104: getfield prevRotationYaw, getfield rotationYaw, getfield prevRotationYaw,
     * fsub, fload partial, fmul, fadd) handed the plain Render prevRotationYaw +
     * (rotationYaw - prevRotationYaw) * partialTicks with no wrap. Mth.lerp(delta,
     * start, end) (bytecode 0-7) is start + delta * (end - start): the same three
     * operations, bit-identical since IEEE fmul is commutative. Mth.rotLerp (0-10)
     * would pass (end - start) through wrapDegrees first; it cannot differ here
     * because LivingEntity.tick (411-466, after the aiStep yaw writes at 171) leaves
     * yRot - yRotO within [-180, 180), where wrapDegrees is the identity, and
     * Entity.absRotateTo (27-32) resets yRotO = yRot; the plain lerp is used because
     * it is the exact 1.7.10 formula. The descriptor hook runs after the yaw exactly
     * as before, so a slice-B translate placed there is unaffected.
     */
    @Override
    protected void applyRotations(A animatable, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                  float partialTick, float nativeScale) {
        if (this.nonLiving) {
            E entity = currentEntity();
            float entityYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        } else {
            super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        }
        this.descriptor.applyRotations(currentEntity(), poseStack, ageInTicks, partialTick);
    }

    /**
     * ENT-S-094 non-living mode: no hurt/death red tint. GeoReplacedEntityRenderer
     * .getPackedOverlay (bytecode 0-59) returns NO_OVERLAY for a non-LivingEntity
     * (offsets 24-27) and otherwise packs OverlayTexture.v(hurtTime > 0 ||
     * deathTime > 0); GeoRenderer.defaultRender calls it once (offsets 20-30, u =
     * 0.0f) and forwards the value to every bone and layer, so this one override
     * covers the whole draw. Taking the non-living branch matches the 1.7.10
     * plain Render, which had no RendererLivingEntity red pass.
     */
    @Override
    public int getPackedOverlay(A animatable, float u, float partialTick) {
        return this.nonLiving ? OverlayTexture.NO_OVERLAY : super.getPackedOverlay(animatable, u, partialTick);
    }

    /**
     * ENT-S-094 non-living mode: no name tag, matching the classic renderer's
     * {@code shouldShowName} for the same species. In 1.7.10 the label was drawn
     * only from RendererLivingEntity.passSpecialRender; a plain Render never
     * called it. GeoReplacedEntityRenderer.shouldShowName (bytecode 0-286) routes
     * a LivingEntity through the living distance/Mob/team gating, which is what
     * is skipped here; renderFinal's single EntityRenderer.render call stays.
     */
    @Override
    public boolean shouldShowName(E entity) {
        return !this.nonLiving && super.shouldShowName(entity);
    }

    /** {@code MobRenderer}: shadow radius times the entity's scale attribute and its age scale (0.5 for babies). */
    @Override
    protected float getShadowRadius(E entity) {
        float radius = this.descriptor.shadowRadius();
        return entity instanceof LivingEntity living ? radius * living.getScale() * living.getAgeScale() : radius;
    }

    /** GeckoLib sets the current entity before {@code defaultRender} and clears it only after post-render cleanup. */
    protected final E currentEntity() {
        return this.descriptor.requireEntity(getCurrentEntity());
    }
}
