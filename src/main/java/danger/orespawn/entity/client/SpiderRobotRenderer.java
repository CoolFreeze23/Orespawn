package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.gait.ModernSpiderGait;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * orig RenderSpiderRobot.java + ClientProxyOreSpawn.java:514:
 * {@code new RenderSpiderRobot(new ModelSpiderRobot(1.0f), 0.99f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderSpiderRobot.java:22) and this.scale = par3 = 1.0 (:23). The 1.7.10 doRender (:37-43) routes to
 * renderSpiderRobot (:27-35), which never calls super.doRender, so preRenderScale (:45-47) is bypassed;
 * the effective world scale is 1.0 either way. The ModelSpiderRobot(1.0f) argument is wingspeed only,
 * not a size (ENT-S-092).
 */
public class SpiderRobotRenderer extends MobRenderer<SpiderRobot, ModelSpiderRobot> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spiderrobot.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spiderrobot"), "main");
    /** orig RenderSpiderRobot.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:514). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.99f * 1.0f (RenderSpiderRobot.java:22). */
    public static final float SHADOW = 0.99F * 1.0F;

    public SpiderRobotRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSpiderRobot(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(SpiderRobot entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 2.0 S3b: MODERN-ONLY visual body dynamics — a literal PoseStack
        // transcription of ModernSpiderGait.bodyTransform, CONJUGATED ABOUT
        // THE MODEL PIVOT (ModernSpiderGait.MODEL_PIVOT_Y_OFFSET: the
        // original's chain below draws the model at the entity's anchor,
        // ENT-S-198). With the pivot conjugation the world action on
        // solver-frame vectors is exactly bodyTransform, so the client's
        // inverse-compensated leg angles land planted feet back on their
        // true anchors. Values are raw tick
        // values (no partial-tick lerp) — only the values the solve
        // compensated with cancel the transform; the gait's rate limits
        // keep the once-per-tick body stepping sub-visual. Classic mode
        // takes none of this.
        if (entity.isModernMovement()) {
            ModernSpiderGait gait = entity.getModernGait();
            if (gait != null) {
                float conjugation = (float) -Math.toRadians(Mth.wrapDegrees((double) entity.getYRot()));
                poseStack.translate(0.0f, gait.renderLift() + ModernSpiderGait.MODEL_PIVOT_Y_OFFSET, 0.0f);
                poseStack.mulPose(Axis.YP.rotation(conjugation));
                poseStack.mulPose(Axis.XP.rotation(gait.renderPitch()));
                poseStack.mulPose(Axis.ZP.rotation(gait.renderRoll()));
                poseStack.mulPose(Axis.YP.rotation(-conjugation));
                poseStack.translate(0.0f, -ModernSpiderGait.MODEL_PIVOT_Y_OFFSET, 0.0f);
            }
        }
        poseStack.scale(SCALE, SCALE, SCALE);
        // ENT-S-198: the original's chain (OriginalRobotChain; orig RenderSpiderRobot.renderSpiderRobot :27-35):
        // 180 - yaw about Y, the (-1, -1, 1) flip and the model at the entity's anchor, with the arguments the
        // original gave it (0, 0, -0.1, 0, 0). The original's renderer bypassed the living chain, so none of its 1.501
        // lift (the hips are where the leg solver puts them, the planted feet on their footing), death tilt, hurt
        // flash or name tag.
        poseStack.mulPose(OriginalRobotChain.modelToAnchor(entityYaw));
        this.model.setupAnim(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiderRobot entity) {
        return TEXTURE;
    }
}
