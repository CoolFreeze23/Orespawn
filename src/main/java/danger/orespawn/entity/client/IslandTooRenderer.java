package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.IslandToo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class IslandTooRenderer extends MobRenderer<IslandToo, ModelIslandToo> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/islandtoo.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "islandtoo"), "main");

    // 1.7.10 ClientProxyOreSpawn: new RenderIslandToo(new ModelIsland(1.0f), 0.25f, 1.0f)
    // See IslandRenderer for the full reasoning behind the 4.0f -> 1.0f fix.
    private static final float SCALE = 1.0f;

    public IslandTooRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelIslandToo(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public void render(IslandToo entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(IslandToo entity) {
        return TEXTURE;
    }
}
