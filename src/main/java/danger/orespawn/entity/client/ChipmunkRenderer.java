package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.custom.ChipmunkEntity;
import danger.orespawn.entity.model.ChipmunkModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ChipmunkRenderer extends MobRenderer<ChipmunkEntity, ChipmunkModel<ChipmunkEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/chipmunk.png");
    private static final ResourceLocation TEXTURE_2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/chipmunk2.png");
    private static final ResourceLocation TEXTURE_3 =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/chipmunk3.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chipmunk"), "main");

    private static final float SCALE = 1.0f;

    public ChipmunkRenderer(EntityRendererProvider.Context context) {
        super(context, new ChipmunkModel<>(context.bakeLayer(MODEL_LAYER)), 0.3f);
    }

    @Override
    public void render(ChipmunkEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(SCALE / 2.0f, SCALE / 2.0f, SCALE / 2.0f);
        } else {
            poseStack.scale(SCALE, SCALE, SCALE);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ChipmunkEntity entity) {
        if (entity.isActivated()) {
            int hatColor = entity.getHatColor();
            if (hatColor == 2) {
                return TEXTURE_2;
            }
            if (hatColor == 3) {
                return TEXTURE_3;
            }
        }
        return TEXTURE;
    }
}
