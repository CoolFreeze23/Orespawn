package danger.orespawn.entity.client;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormLarge;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
public class WormLargeRenderer extends MobRenderer<EntityWormLarge, WormLargeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormlarge.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wormlarge"), "main");
    public WormLargeRenderer(EntityRendererProvider.Context context) {
        super(context, new WormLargeModel(context.bakeLayer(MODEL_LAYER)), 1.0f);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWormLarge entity) { return TEXTURE; }
}
