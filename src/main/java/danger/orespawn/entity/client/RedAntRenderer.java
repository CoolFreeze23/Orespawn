package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRedAnt;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RedAntRenderer extends MobRenderer<EntityRedAnt, AntModel<EntityRedAnt>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/red_ant.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "red_ant"), "main");

    /** orig ClientProxyOreSpawn: RenderAnt(new ModelAnt(), 0.15f shadow, 0.35f scale). */
    private static final float SCALE = 0.35f;

    public RedAntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.15f * SCALE);
    }

    /** orig RenderAnt.preRenderScale — see AntRenderer.scale for rationale. */
    @Override
    protected void scale(EntityRedAnt entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRedAnt entity) {
        return TEXTURE;
    }
}
