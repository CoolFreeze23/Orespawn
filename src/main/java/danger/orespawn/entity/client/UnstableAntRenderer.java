package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityUnstableAnt;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class UnstableAntRenderer extends MobRenderer<EntityUnstableAnt, AntModel<EntityUnstableAnt>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/unstable_ant.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "unstable_ant"), "main");

    public UnstableAntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(MODEL_LAYER)), 0.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityUnstableAnt entity) {
        return TEXTURE;
    }
}
