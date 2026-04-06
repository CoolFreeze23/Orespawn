package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMosquito;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class MosquitoRenderer extends MobRenderer<EntityMosquito, MosquitoModel<EntityMosquito>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/mosquito.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mosquito"), "main");

    public MosquitoRenderer(EntityRendererProvider.Context context) {
        super(context, new MosquitoModel<>(context.bakeLayer(MODEL_LAYER)), 0.05f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMosquito entity) {
        return TEXTURE;
    }
}
