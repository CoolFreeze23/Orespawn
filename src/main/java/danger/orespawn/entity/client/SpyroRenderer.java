package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpyro;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SpyroRenderer extends MobRenderer<EntitySpyro, SpyroModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spyro.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spyro"), "main");

    public SpyroRenderer(EntityRendererProvider.Context context) {
        super(context, new SpyroModel(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpyro entity) {
        return TEXTURE;
    }
}
