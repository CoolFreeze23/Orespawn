package danger.orespawn.entity.client;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormMedium;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
/**
 * orig RenderWormMedium.java + ClientProxyOreSpawn.java:467:
 * {@code new RenderWormMedium(new ModelWormMedium(), 0.25f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderWormMedium.java:23), this.scale = par3
 * (:24), preRenderScale (:39-41, via func_77041_b :43-45) scales by 1.0 (ENT-S-092).
 */
public class WormMediumRenderer extends MobRenderer<EntityWormMedium, WormMediumModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormmedium.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wormmedium"), "main");
    /** orig RenderWormMedium.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:467). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.25f * 1.0f (RenderWormMedium.java:23). */
    public static final float SHADOW = 0.25F * 1.0F;
    public WormMediumRenderer(EntityRendererProvider.Context context) {
        super(context, new WormMediumModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWormMedium entity) { return TEXTURE; }
}
