package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PurplePower;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PurplePowerRenderer extends MobRenderer<PurplePower, ModelPurplePower> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture.png");
    private static final ResourceLocation TEXTURE_2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture2.png");
    private static final ResourceLocation TEXTURE_3 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture3.png");
    private static final ResourceLocation TEXTURE_4 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture4.png");
    private static final ResourceLocation TEXTURE_10 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture10.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "purplepower"), "main");

    public PurplePowerRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPurplePower(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public void render(PurplePower entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getPurpleType() != 0 ? 0.55f : 1.0f;
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PurplePower entity) {
        int type = entity.getPurpleType();
        return switch (type) {
            case 1 -> TEXTURE_2;
            case 2 -> TEXTURE_3;
            case 3 -> TEXTURE_4;
            case 10 -> TEXTURE_10;
            default -> TEXTURE;
        };
    }
}
