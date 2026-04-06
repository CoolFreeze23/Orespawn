package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Skate;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class SkateRenderer extends MobRenderer<Skate, ModelSkate> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/skate.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "skate"), "main");

    public SkateRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSkate(context.bakeLayer(MODEL_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(Skate entity) {
        return TEXTURE;
    }
}
