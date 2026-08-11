package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.gait.ModernSpiderGait;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiderRobotRenderer extends MobRenderer<SpiderRobot, ModelSpiderRobot> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spiderrobot.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spiderrobot"), "main");

    public SpiderRobotRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSpiderRobot(context.bakeLayer(MODEL_LAYER)), 3.0f);
    }

    @Override
    public void render(SpiderRobot entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 2.0 S3b: MODERN-ONLY visual body dynamics — a literal PoseStack
        // transcription of ModernSpiderGait.bodyTransform, CONJUGATED ABOUT
        // THE MODEL PIVOT: vanilla draws the model +1.501 blocks above the
        // entity anchor, and rotating about the bare anchor would displace
        // every planted foot by (R−I)·(0,1.501,0) — up to ~0.52 blocks at
        // the tilt clamp (independent-review BLOCKER). With the pivot
        // conjugation the world action on solver-frame vectors is exactly
        // bodyTransform, so the client's inverse-compensated leg angles land
        // planted feet back on their true anchors. Values are raw tick
        // values (no partial-tick lerp) — only the values the solve
        // compensated with cancel the transform; the gait's rate limits
        // keep the once-per-tick body stepping sub-visual. Classic mode
        // takes none of this and renders byte-identically to 1.0.
        if (entity.isModernMovement()) {
            ModernSpiderGait gait = entity.getModernGait();
            if (gait != null) {
                float conjugation = (float) -Math.toRadians(Mth.wrapDegrees((double) entity.getYRot()));
                poseStack.translate(0.0f, gait.renderLift() + ModernSpiderGait.VANILLA_RENDER_Y_OFFSET, 0.0f);
                poseStack.mulPose(Axis.YP.rotation(conjugation));
                poseStack.mulPose(Axis.XP.rotation(gait.renderPitch()));
                poseStack.mulPose(Axis.ZP.rotation(gait.renderRoll()));
                poseStack.mulPose(Axis.YP.rotation(-conjugation));
                poseStack.translate(0.0f, -ModernSpiderGait.VANILLA_RENDER_Y_OFFSET, 0.0f);
            }
        }
        poseStack.scale(1.0f, 1.0f, 1.0f);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiderRobot entity) {
        return TEXTURE;
    }
}
