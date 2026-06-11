package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Mothra;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class MothraRenderer extends MobRenderer<Mothra, ButterflyModel<Mothra>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothra"), "main");

    public MothraRenderer(EntityRendererProvider.Context context) {
        // wingspeed 0.2f — orig ClientProxyOreSpawn.java:411 (new ModelButterfly(0.2f))
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER), 0.2f), 1.5f);
    }

    @Override
    protected void scale(Mothra entity, PoseStack poseStack, float partialTick) {
        // 10.0f — orig ClientProxyOreSpawn.java:411 third arg, applied via orig RenderButterfly.java:26,42 (glScalef)
        poseStack.scale(10.0f, 10.0f, 10.0f);
    }

    @Override
    public boolean shouldRender(Mothra entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Mothra entity) {
        return TEXTURE;
    }
}
