package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpitBug;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderSpitBug.java + ClientProxyOreSpawn.java:452:
 * {@code new RenderSpitBug(new ModelSpitBug(0.55f), 0.55f, 0.75f)} - RenderSpitBug.java:23
 * passes {@code par2 * par3} = 0.55f * 0.75f to RenderLiving as the shadow and :24 keeps
 * {@code scale = par3} = 0.75f for preRenderScale (ENT-S-092).
 */
public class SpitBugRenderer extends MobRenderer<EntitySpitBug, SpitBugModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spitbug.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spitbug"), "main");

    /** orig RenderSpitBug.scale = 0.75f (third constructor argument, ClientProxyOreSpawn.java:452). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = par2 * par3 = 0.55f * 0.75f (RenderSpitBug.java:23). */
    public static final float SHADOW = 0.55F * 0.75F;

    public SpitBugRenderer(EntityRendererProvider.Context context) {
        super(context, new SpitBugModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntitySpitBug entity, PoseStack poseStack, float partialTick) {
        // orig RenderSpitBug.preRenderScale (func_77041_b, RenderSpitBug.java:39-45):
        // GL11.glScalef(scale, scale, scale) unconditionally - same pipeline position as
        // LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpitBug entity) {
        return TEXTURE;
    }
}
