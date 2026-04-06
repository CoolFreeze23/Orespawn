package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTriffid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TriffidRenderer extends MobRenderer<EntityTriffid, TriffidModel<EntityTriffid>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/triffid.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid"), "main");

    public TriffidRenderer(EntityRendererProvider.Context context) {
        super(context, new TriffidModel<>(context.bakeLayer(MODEL_LAYER)), 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTriffid entity) {
        return TEXTURE;
    }
}
