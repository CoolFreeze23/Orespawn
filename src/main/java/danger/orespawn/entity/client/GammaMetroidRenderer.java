package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityGammaMetroid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GammaMetroidRenderer extends MobRenderer<EntityGammaMetroid, GammaMetroidModel<EntityGammaMetroid>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gammametroid.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gammametroid"), "main");

    public GammaMetroidRenderer(EntityRendererProvider.Context context) {
        super(context, new GammaMetroidModel<>(context.bakeLayer(MODEL_LAYER)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGammaMetroid entity) {
        return TEXTURE;
    }
}
