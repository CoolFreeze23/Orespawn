package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTriffid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderTriffid.java + ClientProxyOreSpawn.java:459:
 * {@code new RenderTriffid(new ModelTriffid(1.0f), 0.3f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderTriffid.java:23), this.scale = par3 (:24), and preRenderScale (:39-41, via func_77041_b
 * at :43-45) scales by 1.0 (ENT-S-092). The ModelTriffid(1.0f) argument is wingspeed only,
 * not a size.
 */
public class TriffidRenderer extends MobRenderer<EntityTriffid, TriffidModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/triffid.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid"), "main");
    /** orig RenderTriffid.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:459). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.3f * 1.0f (RenderTriffid.java:23). */
    public static final float SHADOW = 0.3F * 1.0F;

    public TriffidRenderer(EntityRendererProvider.Context context) {
        super(context, new TriffidModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTriffid entity) {
        return TEXTURE;
    }
}
