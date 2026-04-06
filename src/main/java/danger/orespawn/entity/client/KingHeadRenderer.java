package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.KingHead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class KingHeadRenderer extends MobRenderer<KingHead, ModelKingHead> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/kinghead.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kinghead"), "main");

    public KingHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelKingHead(context.bakeLayer(MODEL_LAYER)), 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(KingHead entity) {
        return TEXTURE;
    }
}
