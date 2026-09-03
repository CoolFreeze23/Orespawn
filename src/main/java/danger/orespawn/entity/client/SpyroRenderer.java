package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpyro;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig ClientProxyOreSpawn.java:427
 * {@code new RenderSpyro(new ModelSpyro(0.65f), 0.65f, 0.75f)}: RenderSpyro.java:22-25 passes
 * {@code par2 * par3} to RenderLiving as the shadow and keeps {@code par3} as the
 * preRenderCallback scale (ENT-S-092). The 0.65f model argument is wingspeed, not a size.
 */
public class SpyroRenderer extends MobRenderer<EntitySpyro, SpyroModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spyro.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spyro"), "main");
    /** orig RenderSpyro.scale = 0.75f (third constructor argument). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = 0.65f * 0.75f. */
    public static final float SHADOW = 0.65F * 0.75F;

    public SpyroRenderer(EntityRendererProvider.Context context) {
        super(context, new SpyroModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntitySpyro entity, PoseStack poseStack, float partialTick) {
        // orig RenderSpyro.preRenderCallback (func_77041_b -> preRenderScale, RenderSpyro.java:39-45):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpyro entity) {
        return TEXTURE;
    }
}
