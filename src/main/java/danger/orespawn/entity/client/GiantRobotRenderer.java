package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GiantRobot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderGiantRobot.java + ClientProxyOreSpawn.java:516:
 * {@code new RenderGiantRobot(new ModelGiantRobot(0.25f), 0.99f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderGiantRobot.java:22), this.scale = par3 = 1.0 (:23) and preRenderScale (:39-41, wired through
 * func_77041_b at :43-45) does glScalef(1.0, 1.0, 1.0). The ModelGiantRobot(0.25f) argument is wingspeed
 * only, not a size (ENT-S-092).
 */
public class GiantRobotRenderer extends MobRenderer<GiantRobot, ModelGiantRobot> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/giantrobot.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "giantrobot"), "main");
    /** orig RenderGiantRobot.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:516). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.99f * 1.0f (RenderGiantRobot.java:22). */
    public static final float SHADOW = 0.99F * 1.0F;

    public GiantRobotRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGiantRobot(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(GiantRobot entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GiantRobot entity) {
        return TEXTURE;
    }
}
