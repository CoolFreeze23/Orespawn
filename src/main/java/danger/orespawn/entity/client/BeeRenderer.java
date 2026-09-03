package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderBee.java + ClientProxyOreSpawn.java:425:
 * {@code new RenderBee(new ModelBee(2.0f), 0.9f, 1.1f)} - RenderLiving shadow = par2 * par3
 * (RenderBee.java:23) and preRenderScale scales by 1.1 (RenderBee.java:39-41) (ENT-S-092).
 */
public class BeeRenderer extends MobRenderer<EntityBee, BeeModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bee.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "bee"), "main");
    /** orig RenderBee.scale = 1.1f (third constructor argument, RenderBee.java:24). */
    public static final float SCALE = 1.1F;
    /** orig RenderLiving shadow = 0.9f * 1.1f (RenderBee.java:23). */
    public static final float SHADOW = 0.9F * 1.1F;

    public BeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityBee entity, PoseStack poseStack, float partialTick) {
        // orig RenderBee.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBee entity) {
        return TEXTURE;
    }
}
