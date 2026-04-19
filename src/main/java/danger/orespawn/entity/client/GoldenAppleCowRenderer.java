package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GoldenAppleCow;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Same {@link CowModel} body, distinct golden texture variant.
 * Texture path: {@code assets/orespawn/textures/entity/golden_apple_cow.png}.
 */
public class GoldenAppleCowRenderer extends MobRenderer<GoldenAppleCow, CowModel<GoldenAppleCow>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/golden_apple_cow.png");

    public GoldenAppleCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenAppleCow entity) {
        return TEXTURE;
    }
}
