package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TRex;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderTRex.java + ClientProxyOreSpawn.java:417:
 * {@code new RenderTRex(new ModelTRex(0.2f), 1.0f, 1.2f)} - RenderLiving shadow = par2 * par3
 * (RenderTRex.java:23) and preRenderScale scales by par3 = 1.2 (RenderTRex.java:24,39-45).
 * The ModelTRex(0.2f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class TRexRenderer extends MobRenderer<TRex, ModelTRex> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/trex.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trex"), "main");

    // 1.7.10 ClientProxyOreSpawn: new RenderTRex(new ModelTRex(0.2f), 1.0f, 1.2f)
    // where the last float is the renderer's world-space scale multiplier.
    private static final float SCALE = 1.2f;
    /** orig RenderLiving shadow = 1.0f * 1.2f (RenderTRex.java:23). */
    public static final float SHADOW = 1.0F * 1.2F;

    public TRexRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTRex(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public void render(TRex entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(TRex entity) {
        return TEXTURE;
    }
}
