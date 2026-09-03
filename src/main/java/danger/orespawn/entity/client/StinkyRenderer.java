package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityStinky;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderStinky.java + ClientProxyOreSpawn.java:490:
 * {@code new RenderStinky(new ModelStinky(0.65f), 0.75f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderStinky.java:41), this.scale = par3 (:42), and preRenderScale (:57-59, wired through
 * func_77041_b at :61-63) scales by 1.0 (ENT-S-092). The ModelStinky(0.65f) argument is not a scale.
 */
public class StinkyRenderer extends MobRenderer<EntityStinky, StinkyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/stinky.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "stinky"), "main");

    /** orig RenderStinky.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:490). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.75f * 1.0f (RenderStinky.java:41). */
    public static final float SHADOW = 0.75F * 1.0F;

    public StinkyRenderer(EntityRendererProvider.Context context) {
        super(context, new StinkyModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityStinky entity) {
        return TEXTURE;
    }
}
