package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityKyuubi;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderKyuubi.java + ClientProxyOreSpawn.java:432:
 * {@code new RenderKyuubi(new ModelKyuubi(0.5f), 0.1f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderKyuubi.java:23), this.scale = par3
 * (:24), preRenderScale glScalef(scale) (:39-41, via func_77041_b :43-45) (ENT-S-092).
 * The ModelKyuubi(0.5f) argument is wingspeed only, not a size.
 */
public class KyuubiRenderer extends MobRenderer<EntityKyuubi, KyuubiModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/kyuubi.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi"), "main");

    /** orig RenderKyuubi.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:432): identity, no scale() override needed. */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.1f * 1.0f (RenderKyuubi.java:23). */
    public static final float SHADOW = 0.1F * 1.0F;

    public KyuubiRenderer(EntityRendererProvider.Context context) {
        super(context, new KyuubiModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityKyuubi entity) {
        return TEXTURE;
    }
}
