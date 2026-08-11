package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.gait.ModernSpiderGait;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AntRobotRenderer extends MobRenderer<AntRobot, ModelAntRobot> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/antrobot.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "antrobot"), "main");

    public AntRobotRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelAntRobot(context.bakeLayer(MODEL_LAYER)), 2.0f);
    }

    @Override
    public void render(AntRobot entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 2.0 S5b: MODERN-ONLY visual body dynamics — the SpiderRobotRenderer
        // S3b transcription verbatim (bodyTransform conjugated about the
        // vanilla +1.501 model pivot, raw tick values, no partial-tick lerp;
        // see that class for the full review lineage). The pivot constant is
        // the same for every LivingEntity render chain. Classic ants take
        // none of this and render byte-identically to 1.0.
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
    public ResourceLocation getTextureLocation(AntRobot entity) {
        return TEXTURE;
    }
}
