package danger.orespawn.entity.client;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormSmall;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public class WormSmallRenderer extends MobRenderer<EntityWormSmall, WormSmallModel<EntityWormSmall>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormsmall.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wormsmall"), "main");
    public WormSmallRenderer(EntityRendererProvider.Context context) {
        super(context, new WormSmallModel<>(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWormSmall entity) { return TEXTURE; }
}
