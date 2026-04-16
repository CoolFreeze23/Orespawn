package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TRex;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class TRexRenderer extends MobRenderer<TRex, ModelTRex> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/trex.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trex"), "main");

    // 1.7.10 ClientProxyOreSpawn: new RenderTRex(new ModelTRex(0.2f), 1.0f, 1.2f)
    // where the last float is the renderer's world-space scale multiplier.
    private static final float SCALE = 1.2f;

    public TRexRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTRex(context.bakeLayer(MODEL_LAYER)), 1.0f);
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
