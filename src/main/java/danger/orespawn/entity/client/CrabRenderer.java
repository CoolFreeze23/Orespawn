package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Crab;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCrab.java + ClientProxyOreSpawn.java:518:
 * {@code new RenderCrab(new ModelCrab(1.0f), 0.99f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderCrab.java:22) - a constant, never multiplied by the crab's per-entity scale. this.scale = par3
 * (:23) is stored but unused: preRenderScale (:39-42, wired through func_77041_b at :44-46) scales by
 * {@code par1Entity.getCrabScale()} instead, which render() below reproduces. The ModelCrab(1.0f)
 * argument is unused by the model (ENT-S-092).
 */
public class CrabRenderer extends MobRenderer<Crab, ModelCrab> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/crab.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crab"), "main");
    /** orig RenderLiving shadow = 0.99f * 1.0f (RenderCrab.java:22); constant, not crab-scaled. */
    public static final float SHADOW = 0.99F * 1.0F;

    public CrabRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCrab(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Crab entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getCrabScale();
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Crab entity) {
        return TEXTURE;
    }
}
