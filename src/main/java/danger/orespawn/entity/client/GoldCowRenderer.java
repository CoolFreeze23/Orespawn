package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GoldCow;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GoldCowRenderer extends MobRenderer<GoldCow, CowModel<GoldCow>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gold_cow.png");

    public GoldCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldCow entity) {
        return TEXTURE;
    }
}
