package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.CaveFisher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class CaveFisherRenderer extends MobRenderer<CaveFisher, ModelCaveFisher> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cavefisher.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cavefisher"), "main");

    public CaveFisherRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCaveFisher(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CaveFisher entity) {
        return TEXTURE;
    }
}
