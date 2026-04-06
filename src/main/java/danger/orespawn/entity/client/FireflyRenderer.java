package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Firefly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class FireflyRenderer extends MobRenderer<Firefly, FireflyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fireflytexture.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "firefly"), "main");

    public FireflyRenderer(EntityRendererProvider.Context context) {
        super(context, new FireflyModel(context.bakeLayer(MODEL_LAYER)), 0.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(Firefly entity) {
        return TEXTURE;
    }
}
