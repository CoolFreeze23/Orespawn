package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Gazelle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderGazelle.java + ClientProxyOreSpawn.java:449:
 * {@code new RenderGazelle(new ModelGazelle(0.65f), 0.45f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderGazelle.java:23), this.scale = par3 (:24), and preRenderScale (:39-45, via func_77041_b)
 * draws children at scale / 2 (ENT-S-092). The ModelGazelle(0.65f) argument is wingspeed only,
 * not a size.
 */
public class GazelleRenderer extends MobRenderer<Gazelle, ModelGazelle> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gazelletexture.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gazelle"), "main");

    /** orig RenderGazelle.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:449). */
    private static final float SCALE = 1.0f;
    /** orig RenderLiving shadow = 0.45f * 1.0f (RenderGazelle.java:23). */
    public static final float SHADOW = 0.45F * 1.0F;

    public GazelleRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGazelle(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Gazelle entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = entity.isBaby() ? SCALE / 2.0f : SCALE;
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Gazelle entity) {
        return TEXTURE;
    }
}
