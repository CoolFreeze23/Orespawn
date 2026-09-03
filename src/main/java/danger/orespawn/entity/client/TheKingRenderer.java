package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheKing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * The King entity renderer. Migrated from 1.7.10 {@code RenderTheKing}
 * (see {@code reference_1_7_10_source/sources/danger/orespawn/RenderTheKing.java}).
 *
 * <p>The main difference vs. a stock {@link MobRenderer} is the multi-pass render
 * sequence required to reproduce the King's translucent wing membranes:
 * <ol>
 *   <li>Pass 1 (opaque): delegates to {@code super.render(...)} which invokes
 *       {@link ModelTheKing#renderToBuffer} on the entity-cutout buffer. Renders
 *       the head cluster, neck chains, body, tail, legs, wing bones, etc.</li>
 *   <li>Pass 2 (translucent): rebuilds the entity-space pose and invokes
 *       {@link ModelTheKing#renderWingMembranes} against a
 *       {@link RenderType#entityTranslucent} buffer with a packed tint
 *       reproducing the legacy {@code glColor4f(0.75, 0.75, 0.75, 0.55)} look
 *       the 1.7.10 model used inside its {@code func_78088_a} GL block.</li>
 * </ol>
 *
 * <p>{@link #shouldRender} always returns {@code true} because the King has a huge
 * bounding box that often straddles the view frustum in ways vanilla culling
 * incorrectly rejects (it would cause visible pop-out of body parts).
 *
 * <p>orig RenderTheKing.java + ClientProxyOreSpawn.java:492:
 * {@code new RenderTheKing(new ModelTheKing(0.65f), 1.9f, 2.1f)} - RenderLiving shadow = par2 * par3
 * (RenderTheKing.java:23) and preRenderScale scales by par3 = 2.1, or par3 / 4 while PlayNicely
 * (RenderTheKing.java:24,39-45 via func_77041_b :47-48). The ModelTheKing(0.65f) argument is only
 * wingspeed, not a size (ENT-S-092).
 */
public class TheKingRenderer extends MobRenderer<TheKing, ModelTheKing> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/thekingtexture.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "theking"), "main");

    /** orig RenderTheKing.scale = 2.1f (third constructor argument, ClientProxyOreSpawn.java:492). */
    public static final float SCALE = 2.1F;
    /** orig RenderLiving shadow = 1.9f * 2.1f (RenderTheKing.java:23). */
    public static final float SHADOW = 1.9F * 2.1F;

    // Original 1.7.10 GL state: glColor4f(0.75, 0.75, 0.75, 0.55).
    // Packed as ARGB int for Blaze3D's {@code color} parameter (A, R, G, B):
    //   alpha = 0.55 * 255 = 140, gray = 0.75 * 255 = 191.
    private static final int WING_MEMBRANE_COLOR = (140 << 24) | (191 << 16) | (191 << 8) | 191;

    public TheKingRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTheKing(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(TheKing entity, PoseStack poseStack, float partialTick) {
        // BOSS-017 / ENT-S-092: orig RenderTheKing.preRenderScale (RenderTheKing.java:39-45): a PlayNicely
        // King (getPlayNicely() != 0) gets GL11.glScalef(scale / 4.0f, ...), otherwise
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift). setupEntityTransform re-enters this hook
        // for the wing-membrane pass inside its own push/pop, so the two passes do not compound.
        float effectiveScale = entity.getPlayNicely() != 0 ? SCALE / 4.0F : SCALE;
        poseStack.scale(effectiveScale, effectiveScale, effectiveScale);
    }

    @Override
    public void render(TheKing entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Pass 1: opaque parts via the standard MobRenderer pipeline.
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Pass 2: translucent wing membranes. We must rebuild the entity-space
        // transform super.render already popped (translate to entity, yaw, flip).
        ResourceLocation tex = getTextureLocation(entity);
        poseStack.pushPose();
        setupEntityTransform(entity, poseStack, entityYaw, partialTicks);

        VertexConsumer translucentVC = buffer.getBuffer(RenderType.entityTranslucent(tex));
        this.getModel().renderWingMembranes(poseStack, translucentVC, packedLight,
                OverlayTexture.NO_OVERLAY, WING_MEMBRANE_COLOR);

        poseStack.popPose();
    }

    /**
     * Reproduces the entity-to-model-space transform applied inside
     * {@code LivingEntityRenderer#render}: translate to entity feet, rotate body yaw,
     * scale, flip the model upside-down (Minecraft model space is Y-inverted).
     */
    private void setupEntityTransform(TheKing entity, PoseStack poseStack,
                                      float entityYaw, float partialTicks) {
        float bodyYaw = net.minecraft.util.Mth.rotLerp(partialTicks,
                entity.yBodyRotO, entity.yBodyRot);
        poseStack.translate(0.0F, 1.501F, 0.0F);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(TheKing entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(TheKing entity) {
        return TEXTURE;
    }
}
