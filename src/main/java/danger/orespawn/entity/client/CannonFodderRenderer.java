package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCannonFodder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class CannonFodderRenderer extends MobRenderer<EntityCannonFodder, CannonFodderModel<EntityCannonFodder>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cannon_fodder.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cannon_fodder"), "main");

    public CannonFodderRenderer(EntityRendererProvider.Context context) {
        super(context, new CannonFodderModel<>(context.bakeLayer(MODEL_LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCannonFodder entity) {
        return TEXTURE;
    }
}
