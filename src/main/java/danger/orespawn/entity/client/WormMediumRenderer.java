package danger.orespawn.entity.client;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormMedium;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public class WormMediumRenderer extends MobRenderer<EntityWormMedium, WormMediumModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormmedium.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wormmedium"), "main");
    public WormMediumRenderer(EntityRendererProvider.Context context) {
        super(context, new WormMediumModel(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWormMedium entity) { return TEXTURE; }
}
