package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityVortex;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VortexRenderer extends MobRenderer<EntityVortex, VortexModel<EntityVortex>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/vortex.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortex"), "main");

    public VortexRenderer(EntityRendererProvider.Context context) {
        super(context, new VortexModel<>(context.bakeLayer(MODEL_LAYER)), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityVortex entity) { return TEXTURE; }
}
