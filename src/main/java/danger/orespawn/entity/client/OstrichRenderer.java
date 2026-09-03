package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Ostrich;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderOstrich.java + ClientProxyOreSpawn.java:450:
 * {@code new RenderOstrich(new ModelOstrich(0.65f), 0.55f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderOstrich.java:26), this.scale = par3 (:27), and preRenderScale (:42-48, via func_77041_b
 * at :50-52) scales adults by 1.0 (ENT-S-092). The ModelOstrich(0.65f) argument is wingspeed only,
 * not a size.
 */
public class OstrichRenderer extends MobRenderer<Ostrich, OstrichModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ostrich.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ostrich"), "main");
    /** orig RenderOstrich.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:450). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.55f * 1.0f (RenderOstrich.java:26). */
    public static final float SHADOW = 0.55F * 1.0F;

    public OstrichRenderer(EntityRendererProvider.Context context) {
        super(context, new OstrichModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(Ostrich entity) {
        return TEXTURE;
    }
}
