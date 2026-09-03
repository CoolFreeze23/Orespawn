package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Frog;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderFrog.java + ClientProxyOreSpawn.java:512:
 * {@code new RenderFrog(new ModelFrog(1.0f), 0.35f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderFrog.java:23), this.scale = par3 (:24) and preRenderScale (:39-40, wired through
 * func_77041_b at :43-44) scales by 1.0 = identity (ENT-S-092). The ModelFrog(1.0f) argument is
 * wingspeed only, not a scale.
 */
public class FrogRenderer extends MobRenderer<Frog, ModelFrog> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/frogtexture.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "frog"), "main");

    private static final float SCALE = 1.0f;
    /** orig RenderLiving shadow = 0.35f * 1.0f (RenderFrog.java:23). */
    public static final float SHADOW = 0.35F * 1.0F;

    public FrogRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelFrog(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Frog entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = entity.isBaby() ? SCALE / 2.0f : SCALE;
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Frog entity) {
        return TEXTURE;
    }
}
