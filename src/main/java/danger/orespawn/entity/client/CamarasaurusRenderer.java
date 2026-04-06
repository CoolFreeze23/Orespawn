package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Camarasaurus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class CamarasaurusRenderer extends MobRenderer<Camarasaurus, ModelCamarasaurus> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/camarasaurus.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "camarasaurus"), "main");

    private static final float SCALE = 0.65f;

    public CamarasaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCamarasaurus(context.bakeLayer(MODEL_LAYER)), 0.65f * 0.65f);
    }

    @Override
    public void render(Camarasaurus entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = entity.isBaby() ? SCALE / 2.0f : SCALE;
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Camarasaurus entity) {
        return TEXTURE;
    }
}
