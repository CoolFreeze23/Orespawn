package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityVortex;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderVortex.java + ClientProxyOreSpawn.java:480:
 * {@code new RenderVortex(new ModelVortex(0.25f), 0.1f, 1.0f)}: RenderLiving shadow = par2 * par3
 * (RenderVortex.java:23), this.scale = par3 (:24), and preRenderScale (:39-41, wired through
 * func_77041_b at :43-45) scales by 1.0 (ENT-S-092). The ModelVortex(0.25f) argument is not a scale.
 */
public class VortexRenderer extends MobRenderer<EntityVortex, VortexModel<EntityVortex>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/vortex.png");
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortex"), "main");
    /** orig RenderVortex.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:480). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.1f * 1.0f (RenderVortex.java:23). */
    public static final float SHADOW = 0.1F * 1.0F;

    public VortexRenderer(EntityRendererProvider.Context context) {
        super(context, new VortexModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityVortex entity) { return TEXTURE; }
}
