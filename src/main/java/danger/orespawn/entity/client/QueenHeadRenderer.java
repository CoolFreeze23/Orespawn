package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.QueenHead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class QueenHeadRenderer extends MobRenderer<QueenHead, ModelQueenHead> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/queenhead.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "queenhead"), "main");

    public QueenHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelQueenHead(context.bakeLayer(MODEL_LAYER)), 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(QueenHead entity) {
        return TEXTURE;
    }
}
