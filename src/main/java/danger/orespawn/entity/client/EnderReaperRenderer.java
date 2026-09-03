package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnderReaper;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderEnderReaper.java + ClientProxyOreSpawn.java:474:
 * {@code new RenderEnderReaper(new ModelEnderReaper(0.23f), 0.2f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderEnderReaper.java:23), this.scale = par3
 * (:24), and preRenderScale (:39-41, wired through func_77041_b at :43-45) scales
 * by 1.0 (ENT-S-092). The ModelEnderReaper(0.23f) argument is a model parameter, not a scale.
 */
public class EnderReaperRenderer extends MobRenderer<EnderReaper, ModelEnderReaper> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/enderreaper.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ender_reaper"), "main");

    /** orig RenderEnderReaper.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:474). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.2f * 1.0f (RenderEnderReaper.java:23). */
    public static final float SHADOW = 0.2F * 1.0F;

    public EnderReaperRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelEnderReaper(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderReaper entity) {
        return TEXTURE;
    }
}
