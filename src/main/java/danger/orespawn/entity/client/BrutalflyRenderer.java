package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBrutalfly;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderBrutalfly.java + ClientProxyOreSpawn.java:507:
 * {@code new RenderBrutalfly(new ModelBrutalfly(0.2f), 0.75f, 9.0f)} - RenderLiving shadow = par2 * par3
 * (RenderBrutalfly.java:24) and preRenderScale scales by par3 = 9.0 (RenderBrutalfly.java:25,40-46).
 * The ModelBrutalfly(0.2f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class BrutalflyRenderer extends MobRenderer<EntityBrutalfly, BrutalflyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/brutalfly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "brutalfly"), "main");
    /** orig RenderBrutalfly.scale = 9.0f (third constructor argument, ClientProxyOreSpawn.java:507). */
    public static final float SCALE = 9.0F;
    /** orig RenderLiving shadow = 0.75f * 9.0f (RenderBrutalfly.java:24). */
    public static final float SHADOW = 0.75F * 9.0F;

    public BrutalflyRenderer(EntityRendererProvider.Context context) {
        super(context, new BrutalflyModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityBrutalfly entity, PoseStack poseStack, float partialTick) {
        // orig RenderBrutalfly.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBrutalfly entity) {
        return TEXTURE;
    }
}
