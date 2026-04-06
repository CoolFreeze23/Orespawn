package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SeaViper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class SeaViperRenderer extends MobRenderer<SeaViper, ModelSeaViper> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/seaviper.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper"), "main");

    public SeaViperRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSeaViper(context.bakeLayer(MODEL_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaViper entity) {
        return TEXTURE;
    }
}
