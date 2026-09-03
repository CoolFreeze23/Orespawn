package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCliffRacer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCliffRacer.java + ClientProxyOreSpawn.java:458:
 * {@code new RenderCliffRacer(new ModelCliffRacer(1.0f), 0.3f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderCliffRacer.java:23), this.scale = par3 (:24), and preRenderScale (:39-41, via func_77041_b
 * at :43-45) scales by 1.0 (ENT-S-092). The ModelCliffRacer(1.0f) argument is wingspeed only,
 * not a size.
 */
public class CliffRacerRenderer extends MobRenderer<EntityCliffRacer, CliffRacerModel<EntityCliffRacer>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cliffracer.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cliffracer"), "main");
    /** orig RenderCliffRacer.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:458). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.3f * 1.0f (RenderCliffRacer.java:23). */
    public static final float SHADOW = 0.3F * 1.0F;

    public CliffRacerRenderer(EntityRendererProvider.Context context) {
        super(context, new CliffRacerModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCliffRacer entity) {
        return TEXTURE;
    }
}
