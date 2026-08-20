package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRainbowAnt;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RainbowAntRenderer extends MobRenderer<EntityRainbowAnt, AntModel<EntityRainbowAnt>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rainbow_ant.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rainbow_ant"), "main");

    /** orig ClientProxyOreSpawn: RenderAnt(new ModelAnt(), 0.1f shadow, 0.25f scale). */
    private static final float SCALE = 0.25f;

    public RainbowAntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.1f * SCALE);
    }

    /** orig RenderAnt.preRenderScale — see AntRenderer.scale for rationale. */
    @Override
    protected void scale(EntityRainbowAnt entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRainbowAnt entity) {
        return TEXTURE;
    }
}
