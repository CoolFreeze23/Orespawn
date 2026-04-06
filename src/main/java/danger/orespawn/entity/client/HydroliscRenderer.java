package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityHydrolisc;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HydroliscRenderer extends MobRenderer<EntityHydrolisc, HydroliscModel<EntityHydrolisc>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/hydrolisc.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hydrolisc"), "main");

    public HydroliscRenderer(EntityRendererProvider.Context context) {
        super(context, new HydroliscModel<>(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityHydrolisc entity) {
        return TEXTURE;
    }
}
