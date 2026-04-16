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
 */
public class TheKingRenderer extends MobRenderer<TheKing, ModelTheKing> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/thekingtexture.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "theking"), "main");

    private static final float SCALE = 1.0F;

    // Original 1.7.10 GL state: glColor4f(0.75, 0.75, 0.75, 0.55).
    // Packed as ARGB int for Blaze3D's {@code color} parameter (A, R, G, B):
    //   alpha = 0.55 * 255 = 140, gray = 0.75 * 255 = 191.
    private static final int WING_MEMBRANE_COLOR = (140 << 24) | (191 << 16) | (191 << 8) | 191;

    public TheKingRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTheKing(context.bakeLayer(MODEL_LAYER)), 5.0F);
    }

    @Override
    protected void scale(TheKing entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
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

    @Override
    public boolean shouldRender(TheKing entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(TheKing entity) {
        return TEXTURE;
    }
}
