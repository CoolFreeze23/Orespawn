package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Godzilla;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class GodzillaRenderer extends MobRenderer<Godzilla, ModelGodzilla> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/godzillatexture.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla"), "main");

    public GodzillaRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGodzilla(context.bakeLayer(MODEL_LAYER)), 5.0f);
    }

    @Override
    protected void scale(Godzilla entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(3.0F, 3.0F, 3.0F);
    }

    @Override
    public boolean shouldRender(Godzilla entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Godzilla entity) {
        return TEXTURE;
    }
}
