package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GodzillaHead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class GodzillaHeadRenderer extends MobRenderer<GodzillaHead, ModelGodzillaHead> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/godzillahead.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzillahead"), "main");

    public GodzillaHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGodzillaHead(context.bakeLayer(MODEL_LAYER)), 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(GodzillaHead entity) {
        return TEXTURE;
    }
}
