package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLeon;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LeonRenderer extends MobRenderer<EntityLeon, LeonModel<EntityLeon>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leon.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon"), "main");

    public LeonRenderer(EntityRendererProvider.Context context) {
        super(context, new LeonModel<>(context.bakeLayer(MODEL_LAYER)), 3.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLeon entity) {
        return TEXTURE;
    }
}
