package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityEmperorScorpion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderEmperorScorpion.java + ClientProxyOreSpawn.java:426:
 * {@code new RenderEmperorScorpion(new ModelEmperorScorpion(0.22f), 0.95f, 1.5f)} - RenderLiving
 * shadow = par2 * par3 (RenderEmperorScorpion.java:23) and preRenderCallback scales by
 * par3 = 1.5 (RenderEmperorScorpion.java:24,39-45) (ENT-S-092).
 */
public class EmperorScorpionRenderer extends MobRenderer<EntityEmperorScorpion, EmperorScorpionModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/emperorscorpion.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion"), "main");

    /** orig RenderLiving shadow = 0.95f * 1.5f (ClientProxyOreSpawn.java:426, RenderEmperorScorpion.java:23). */
    public static final float SHADOW = 0.95F * 1.5F;
    /** orig RenderEmperorScorpion.scale = 1.5f (third constructor argument, RenderEmperorScorpion.java:24). */
    public static final float SCALE = 1.5F;

    public EmperorScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new EmperorScorpionModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityEmperorScorpion entity, PoseStack poseStack, float partialTick) {
        // orig RenderEmperorScorpion.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEmperorScorpion entity) {
        return TEXTURE;
    }
}
