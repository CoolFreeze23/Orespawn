package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityAnt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class AntRenderer extends MobRenderer<EntityAnt, AntModel<EntityAnt>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ant.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ant"), "main");

    /** orig ClientProxyOreSpawn: RenderAnt(new ModelAnt(), 0.1f shadow, 0.25f scale). */
    private static final float SCALE = 0.25f;

    public AntRenderer(EntityRendererProvider.Context context) {
        // orig RenderAnt ctor: shadow = par2 * par3 = 0.1 * 0.25
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.1f * SCALE);
    }

    /**
     * orig RenderAnt.preRenderScale (RenderAnt.java:38-40): glScalef by the
     * per-type scale. Without this the ant renders 4x the original size,
     * which makes the body-center-pivoted leg swing look wildly glitched.
     */
    @Override
    protected void scale(EntityAnt entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAnt entity) {
        return TEXTURE;
    }
}
