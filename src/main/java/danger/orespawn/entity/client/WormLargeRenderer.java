package danger.orespawn.entity.client;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormLarge;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
/**
 * orig RenderWormLarge.java + ClientProxyOreSpawn.java:468:
 * {@code new RenderWormLarge(new ModelWormLarge(), 0.9f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderWormLarge.java:23), this.scale = par3
 * (:24), preRenderScale (:39-41, via func_77041_b :43-45) scales by 1.0 (ENT-S-092).
 */
public class WormLargeRenderer extends MobRenderer<EntityWormLarge, WormLargeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormlarge.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wormlarge"), "main");
    /** orig RenderWormLarge.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:468). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.9f * 1.0f (RenderWormLarge.java:23). */
    public static final float SHADOW = 0.9F * 1.0F;
    public WormLargeRenderer(EntityRendererProvider.Context context) {
        super(context, new WormLargeModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWormLarge entity) { return TEXTURE; }
}
