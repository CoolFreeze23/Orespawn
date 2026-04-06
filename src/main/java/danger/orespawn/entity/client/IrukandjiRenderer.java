package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Irukandji;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class IrukandjiRenderer extends MobRenderer<Irukandji, ModelIrukandji> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/irukandji.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "irukandji"), "main");

    public IrukandjiRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelIrukandji(context.bakeLayer(MODEL_LAYER)), 0.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(Irukandji entity) {
        return TEXTURE;
    }
}
