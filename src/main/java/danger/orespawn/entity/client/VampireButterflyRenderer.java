package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.VampireButterfly;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for {@link VampireButterfly}. Re-uses the generic
 * {@link ButterflyModel} body layer (same as the ambient butterfly,
 * Mothra, and Leonopteryx) so we don't duplicate the wing-flap math.
 *
 * <p>Texture defaults to {@code vampire_butterfly.png}; if that asset is
 * missing the user will see Minecraft's standard purple/black "missing
 * texture" checkerboard rather than a registry warning. The asset slot
 * is reserved here so a textures pack (or future PR) can drop a real
 * sprite in place without code changes.
 */
public class VampireButterflyRenderer extends MobRenderer<VampireButterfly, ButterflyModel<VampireButterfly>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/vampire_butterfly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vampire_butterfly"), "main");

    public VampireButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER)), 0.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(VampireButterfly entity) {
        return TEXTURE;
    }
}
