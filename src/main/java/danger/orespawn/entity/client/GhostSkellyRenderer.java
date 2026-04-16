package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GhostSkelly;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class GhostSkellyRenderer extends MobRenderer<GhostSkelly, GhostSkellyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ghost_skelly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ghost_skelly"), "main");

    public GhostSkellyRenderer(EntityRendererProvider.Context context) {
        super(context, new GhostSkellyModel(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(GhostSkelly entity) {
        return TEXTURE;
    }

    // Same reasoning as GhostRenderer — send the skeletal ghost through the
    // translucent pipeline so its soft alpha edges don't get cutout-clipped.
    @Override
    public RenderType getRenderType(GhostSkelly entity, boolean visible, boolean visibleToPlayer, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }
}
