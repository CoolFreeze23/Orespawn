package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRotator;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderRotator.java + ClientProxyOreSpawn.java:479:
 * {@code new RenderRotator(new ModelRotator(0.25f), 0.1f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderRotator.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-41, wired through func_77041_b at :43-45) scales
 * by 1.0 (ENT-S-092). The ModelRotator(0.25f) argument is a model parameter, not a scale.
 */
public class RotatorRenderer extends MobRenderer<EntityRotator, RotatorModel<EntityRotator>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rotator.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rotator"), "main");

    /** orig RenderRotator.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:479). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.1f * 1.0f (RenderRotator.java:23). */
    public static final float SHADOW = 0.1F * 1.0F;

    public RotatorRenderer(EntityRendererProvider.Context context) {
        super(context, new RotatorModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRotator entity) { return TEXTURE; }
}
