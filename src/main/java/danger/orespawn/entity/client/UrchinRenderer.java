package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Urchin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class UrchinRenderer extends MobRenderer<Urchin, ModelUrchin> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/urchin.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "urchin"), "main");

    public UrchinRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelUrchin(context.bakeLayer(MODEL_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Urchin entity) {
        return TEXTURE;
    }
}
