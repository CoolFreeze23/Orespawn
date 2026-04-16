package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.WaterDragon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class WaterDragonRenderer extends MobRenderer<WaterDragon, ModelWaterDragon> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/waterdragon.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "waterdragon"), "main");

    // 1.7.10 ClientProxyOreSpawn: new RenderWaterDragon(new ModelWaterDragon(0.5f), 0.85f, 1.1f)
    // Third float is the renderer's world-space scale. The earlier 2.5f here
    // inflated the dragon far beyond the 1.7.10 silhouette and caused the
    // visual model to swallow half the hitbox — the value belongs at 1.1f.
    private static final float SCALE = 1.1f;

    public WaterDragonRenderer(EntityRendererProvider.Context context) {
        // Second arg mirrors 1.7.10's shadow radius (0.85f).
        super(context, new ModelWaterDragon(context.bakeLayer(MODEL_LAYER)), 0.85f);
    }

    @Override
    public void render(WaterDragon entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(WaterDragon entity) {
        return TEXTURE;
    }
}
