package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTermite;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Termite entity using the shared {@link AntModel}.
 * In the original 1.7.10 mod, termites reused the ant model (ModelAnt)
 * with a termite-specific texture. The termite.png UV layout matches
 * the ant model's geometry, not a custom termite model.
 */
public class TermiteRenderer extends MobRenderer<EntityTermite, AntModel<EntityTermite>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/termite.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "termite"), "main");

    public TermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTermite entity) {
        return TEXTURE;
    }
}
