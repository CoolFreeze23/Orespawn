package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRedAnt;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RedAntRenderer extends MobRenderer<EntityRedAnt, AntModel<EntityRedAnt>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/red_ant.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "red_ant"), "main");

    public RedAntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRedAnt entity) {
        return TEXTURE;
    }
}
