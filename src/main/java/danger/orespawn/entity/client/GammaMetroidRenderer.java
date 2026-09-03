package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityGammaMetroid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderGammaMetroid.java + ClientProxyOreSpawn.java:429:
 * {@code new RenderGammaMetroid(new ModelGammaMetroid(0.45f), 0.75f, 0.9f)} - RenderLiving shadow = par2 * par3
 * (RenderGammaMetroid.java:23) and preRenderScale scales by 0.9, or 0.9 / 2 for a child
 * (RenderGammaMetroid.java:39-45) (ENT-S-092).
 */
public class GammaMetroidRenderer extends MobRenderer<EntityGammaMetroid, GammaMetroidModel<EntityGammaMetroid>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gammametroid.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gammametroid"), "main");
    /** orig RenderGammaMetroid.scale = 0.9f (third constructor argument, RenderGammaMetroid.java:24). */
    public static final float SCALE = 0.9F;
    /** orig RenderLiving shadow = 0.75f * 0.9f (RenderGammaMetroid.java:23). */
    public static final float SHADOW = 0.75F * 0.9F;

    public GammaMetroidRenderer(EntityRendererProvider.Context context) {
        super(context, new GammaMetroidModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityGammaMetroid entity, PoseStack poseStack, float partialTick) {
        // orig RenderGammaMetroid.preRenderScale (RenderGammaMetroid.java:39-45): a child (func_70631_g_)
        // gets GL11.glScalef(scale / 2.0f, ...), otherwise GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        if (entity.isBaby()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGammaMetroid entity) {
        return TEXTURE;
    }
}
