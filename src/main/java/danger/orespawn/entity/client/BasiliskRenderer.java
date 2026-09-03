package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Basilisk;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderBasilisk.java + ClientProxyOreSpawn.java:420:
 * {@code new RenderBasilisk(new ModelBasilisk(0.3f), 0.5f, 1.25f)}:
 * RenderLiving shadow = par2 * par3 (RenderBasilisk.java:23), this.scale = par3
 * (:24), preRenderScale glScalef(scale) (:39-41, via func_77041_b :43-45) (ENT-S-092).
 * The ModelBasilisk(0.3f) argument is wingspeed only, not a size.
 */
public class BasiliskRenderer extends MobRenderer<Basilisk, ModelBasilisk> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/basilisk.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "basilisk"), "main");

    // 1.7.10 ClientProxyOreSpawn: new RenderBasilisk(new ModelBasilisk(0.3f), 0.5f, 1.25f)
    // where the last float is the renderer's world-space scale multiplier.
    private static final float SCALE = 1.25f;
    /** orig RenderLiving shadow = 0.5f * 1.25f (RenderBasilisk.java:23). */
    public static final float SHADOW = 0.5F * 1.25F;

    public BasiliskRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelBasilisk(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(Basilisk entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = entity.isBaby() ? SCALE / 2.0f : SCALE;
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Basilisk entity) {
        return TEXTURE;
    }
}
