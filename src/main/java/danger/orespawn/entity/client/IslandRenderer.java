package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Island;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class IslandRenderer extends MobRenderer<Island, ModelIsland> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/island.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "island"), "main");

    // 1.7.10 ClientProxyOreSpawn: new RenderIsland(new ModelIsland(1.0f), 0.25f, 1.0f)
    // The third float is the world-space scale; the earlier 4.0f here
    // bloated the centerpiece model to absurd size and desynced it from
    // the actual placed sand/gravel blocks produced by Island#createIsland.
    // The Island's AABB was also shrunk to 0.5x0.5 in ModEntities so this
    // scale renders the ornamental model at the hitbox's center.
    private static final float SCALE = 1.0f;

    public IslandRenderer(EntityRendererProvider.Context context) {
        // Shadow radius mirrors 1.7.10's 0.25f.
        super(context, new ModelIsland(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public void render(Island entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Island entity) {
        return TEXTURE;
    }
}
