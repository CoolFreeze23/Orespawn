package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.VelocityRaptor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class VelocityRaptorRenderer extends MobRenderer<VelocityRaptor, VelocityRaptorModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/velocityraptor.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "velocity_raptor"), "main");

    public VelocityRaptorRenderer(EntityRendererProvider.Context context) {
        super(context, new VelocityRaptorModel(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(VelocityRaptor entity) {
        return TEXTURE;
    }
}
