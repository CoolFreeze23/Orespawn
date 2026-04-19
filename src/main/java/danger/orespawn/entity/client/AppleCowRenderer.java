package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AppleCow;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Re-uses the vanilla {@link CowModel} (same layout as {@link RedCowRenderer},
 * {@link CrystalCowRenderer}, etc.) so we don't bake an extra body layer.
 * Texture path: {@code assets/orespawn/textures/entity/apple_cow.png}.
 */
public class AppleCowRenderer extends MobRenderer<AppleCow, CowModel<AppleCow>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/apple_cow.png");

    public AppleCowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(AppleCow entity) {
        return TEXTURE;
    }
}
