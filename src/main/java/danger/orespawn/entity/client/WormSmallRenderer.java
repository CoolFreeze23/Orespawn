package danger.orespawn.entity.client;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormSmall;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
/**
 * orig RenderWormSmall.java + ClientProxyOreSpawn.java:466:
 * {@code new RenderWormSmall(new ModelWormSmall(), 0.1f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderWormSmall.java:23), this.scale = par3
 * (:24), preRenderScale (:39-41, via func_77041_b :43-45) scales by 1.0 (ENT-S-092).
 */
public class WormSmallRenderer extends MobRenderer<EntityWormSmall, WormSmallModel<EntityWormSmall>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormsmall.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wormsmall"), "main");
    /** orig RenderWormSmall.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:466). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.1f * 1.0f (RenderWormSmall.java:23). */
    public static final float SHADOW = 0.1F * 1.0F;
    public WormSmallRenderer(EntityRendererProvider.Context context) {
        super(context, new WormSmallModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWormSmall entity) { return TEXTURE; }
}
