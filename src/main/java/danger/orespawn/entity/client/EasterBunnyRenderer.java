package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EasterBunny;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderEasterBunny.java + ClientProxyOreSpawn.java:498:
 * {@code new RenderEasterBunny(new ModelEasterBunny(0.55f), 0.5f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderEasterBunny.java:23), this.scale = par3 (:24) and preRenderScale (:39-44, wired through
 * func_77041_b at :47-48) draws children at scale / 2, adults at scale = 1.0 (ENT-S-092). The
 * ModelEasterBunny(0.55f) argument is wingspeed only, not a scale.
 */
public class EasterBunnyRenderer extends MobRenderer<EasterBunny, ModelEasterBunny> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/easterbunny.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "easter_bunny"), "main");

    /** orig RenderEasterBunny.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:498). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.5f * 1.0f (RenderEasterBunny.java:23). */
    public static final float SHADOW = 0.5F * 1.0F;

    public EasterBunnyRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelEasterBunny(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(EasterBunny entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EasterBunny entity) {
        return TEXTURE;
    }
}
