package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRat;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RatRenderer extends MobRenderer<EntityRat, RatModel<EntityRat>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rat.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rat"), "main");

    public RatRenderer(EntityRendererProvider.Context context) {
        super(context, new RatModel<>(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRat entity) {
        return TEXTURE;
    }
}
