package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Robot3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRobot3.java + ClientProxyOreSpawn.java:441:
 * {@code new RenderRobot3(new ModelRobot3(1.0f), 1.0f, 0.5f)} - RenderLiving
 * shadow = par2 * par3 (RenderRobot3.java:23) and preRenderCallback scales by
 * par3 = 0.5 (RenderRobot3.java:24,39-45) (ENT-S-092).
 */
public class Robot3Renderer extends MobRenderer<Robot3, ModelRobot3> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/robot3.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot3"), "main");

    /** orig RenderLiving shadow = 1.0f * 0.5f (ClientProxyOreSpawn.java:441, RenderRobot3.java:23). */
    public static final float SHADOW = 1.0F * 0.5F;
    /** orig RenderRobot3.scale = 0.5f (third constructor argument, RenderRobot3.java:24). */
    public static final float SCALE = 0.5F;

    public Robot3Renderer(EntityRendererProvider.Context context) {
        super(context, new ModelRobot3(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Robot3 entity, PoseStack poseStack, float partialTick) {
        // orig RenderRobot3.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Robot3 entity) {
        return TEXTURE;
    }
}
