package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SeaViper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderSeaViper.java + ClientProxyOreSpawn.java:497:
 * {@code new RenderSeaViper(new ModelSeaViper(0.5f), 1.0f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderSeaViper.java:23), this.scale = par3 (:24) and preRenderScale (:39-40, wired through
 * func_77041_b at :43-44) scales by 1.0 = identity (ENT-S-092). The ModelSeaViper(0.5f) argument is
 * wingspeed only, not a scale.
 */
public class SeaViperRenderer extends MobRenderer<SeaViper, ModelSeaViper> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/seaviper.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper"), "main");

    /** orig RenderSeaViper.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:497). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 1.0f * 1.0f (RenderSeaViper.java:23). */
    public static final float SHADOW = 1.0F * 1.0F;

    public SeaViperRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSeaViper(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(SeaViper entity) {
        return TEXTURE;
    }
}
