package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.util.Color;

/**
 * Shared base for every OreSpawn GeckoLib replacement renderer.
 *
 * <p>Left deliberately plain, like {@link QueenRenderer}: MultiHitboxLib's
 * {@code MixinGeoReplacedEntityRenderer} attaches its bone-collector layer to
 * this class's constructor, and that layer is a no-op for entities without a
 * bone-synced hitbox profile.</p>
 *
 * <p>The face-order item carried from the Phase G review (vanilla {@code ModelPart}
 * emits cube faces in down/up/west/north/east/south order while GeckoLib emits
 * west/east/north/south/up/down; opaque rigs are unaffected, a translucent rig is
 * not) is settled by {@link FaceOrder} (ENT-S-146, 2026-09-06): a rig whose manifest
 * asks for it ships the classic face order under {@code orespawn:cube_face_order} and
 * the shared model permutes every cube's quads into it at bake time, so a translucent
 * candidate blends its faces exactly as the classic renderer does.</p>
 *
 * <p>ENT-S-146 render state: the descriptor's {@code renderType} / {@code renderColor} /
 * {@code fullBright} hooks are applied below at the three points GeckoLib and vanilla decide
 * them - {@link #getRenderType} (the visible-body render type), {@link #getRenderColor} (the
 * colour int {@code GeoRenderer.defaultRender} feeds every vertex) and the two light-level
 * getters the dispatcher packs ({@code EntityRenderer.getPackedLightCoords}).</p>
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
        // OPT-029: the replacement's per-entity animation cache is registered here, at renderer
        // construction on the client main thread (a bootstrapped game: the descriptor's entity-type
        // supplier resolves), never in the replacement's constructor, which the headless s4 probe
        // runs without a bootstrapped registry. A resource reload rebuilds this renderer
        // (EntityRenderDispatcher.onResourceManagerReload) and the new registration replaces the old.
        GeoReplacementCaches.register(this.descriptor.entityType(), replacement.animatableCache());
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

    /**
     * ENT-S-146: the descriptor's render type for a VISIBLE body, else GeckoLib's own.
     * GeoReplacedEntityRenderer.getRenderType (4.8.4 bytecode): an invisible entity the viewer can
     * still see draws through {@code RenderType.itemEntityTranslucentCull} (offsets 24-46); a visible
     * one falls through to {@code GeoRenderer.getRenderType} (52-61), i.e. {@code GeoModel
     * .getRenderType} = {@code RenderType.entityCutoutNoCull}; an invisible glowing one gets the outline
     * (62-89). Vanilla's {@code LivingEntityRenderer.getRenderType} has the same three branches (7-16
     * translucent, 17-30 {@code model.renderType(texture)} for a visible body, 31-44 outline), so the
     * hook replaces exactly the branch the classic model's render-type function fills, and only that
     * one. {@code GeoRenderer.defaultRender} calls this once per draw (59-76) with the descriptor's
     * per-entity texture.
     */
    @Override
    public RenderType getRenderType(A animatable, ResourceLocation texture, MultiBufferSource bufferSource,
                                    float partialTick) {
        Entity current = getCurrentEntity();
        if (current != null && !current.isInvisible()) {
            // The descriptor hands over the classic model's own render-type FUNCTION (the same object the
            // classic renderer applies through Model.renderType(texture)), applied to the same texture.
            Function<ResourceLocation, RenderType> own = this.descriptor.renderType(this.descriptor.requireEntity(current));
            if (own != null) {
                return own.apply(texture);
            }
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    /**
     * ENT-S-146: the descriptor's vertex colour, else GeckoLib's white. {@code GeoRenderer.defaultRender}
     * (4.8.4 bytecode) takes {@code getRenderColor(animatable, partialTick, packedLight).argbInt()} once
     * (offsets 4-18) and passes that int to {@code preRender}, {@code actuallyRender} (176; the replaced
     * renderer's override hands it on to {@code GeoRenderer.actuallyRender} at 773, whose
     * {@code renderRecursively} carries it to every bone), {@code postRender} and {@code renderFinal};
     * {@code createVerticesOfQuad} hands it to every {@code addVertex} (81). The classic side's
     * counterpart is the colour argument {@code ModelPart.render} passes to {@code Cube.compile} (176).
     */
    @Override
    public Color getRenderColor(A animatable, float partialTick, int packedLight) {
        Entity current = getCurrentEntity();
        if (current != null) {
            int argb = this.descriptor.renderColor(this.descriptor.requireEntity(current), partialTick);
            if (argb != GeoReplacementDescriptor.WHITE) {
                return new Color(argb);
            }
        }
        return super.getRenderColor(animatable, partialTick, packedLight);
    }

    /**
     * ENT-S-146: a fullbright species answers {@link GeoReplacementDescriptor#FULL_BRIGHT_LEVEL} (15) for
     * both levels, so {@code EntityRenderer.getPackedLightCoords} (bytecode 0-24) packs
     * {@code LightTexture.FULL_BRIGHT} into the light the dispatcher hands {@code render} - which
     * {@code GeoReplacedEntityRenderer.render} passes to {@code defaultRender} (offset 18) and on to
     * every vertex. The {@code MagmaCubeRenderer} idiom, the same override the species' classic renderer
     * carries. The level is the seam's own constant (refuter A, D4): this shared base names no species model.
     */
    @Override
    protected int getBlockLightLevel(E entity, BlockPos pos) {
        return this.descriptor.fullBright(entity) ? GeoReplacementDescriptor.FULL_BRIGHT_LEVEL : super.getBlockLightLevel(entity, pos);
    }

    /** See {@link #getBlockLightLevel}. */
    @Override
    protected int getSkyLightLevel(E entity, BlockPos pos) {
        return this.descriptor.fullBright(entity) ? GeoReplacementDescriptor.FULL_BRIGHT_LEVEL : super.getSkyLightLevel(entity, pos);
    }

    /** GeckoLib sets the current entity before {@code defaultRender} and clears it only after post-render cleanup. */
    protected final E currentEntity() {
        return this.descriptor.requireEntity(getCurrentEntity());
    }

    /**
     * ENT-S-161 (owner 2026-09-13, item 3; PN-027): every cube of a replacement is drawn with its quads'
     * true transformed normals - GeckoLib's own {@code renderCube} minus {@code RenderUtil.fixInvertedFlatCube},
     * which mangles the normal of a ROTATED zero-thickness cube ({@link TrueNormalCubeRenderer}). The
     * harness's capturing renderer draws through the same static, so the proof draws what the seam draws.
     */
    @Override
    public void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        TrueNormalCubeRenderer.render(this, poseStack, cube, buffer, packedLight, packedOverlay, colour);
    }
}
