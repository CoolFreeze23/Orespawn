package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTshirt;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TshirtRenderer extends MobRenderer<EntityTshirt, TshirtModel<EntityTshirt>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/tshirt.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "tshirt"), "main");

    public TshirtRenderer(EntityRendererProvider.Context context) {
        super(context, new TshirtModel<>(context.bakeLayer(MODEL_LAYER)), 2.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTshirt entity) { return TEXTURE; }
}
