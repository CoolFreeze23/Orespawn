package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMantis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderMantis.java + ClientProxyOreSpawn.java:488:
 * {@code new RenderMantis(new ModelMantis(2.0f), 0.9f, 1.1f)} - RenderMantis.java:22-24
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code scale = par3};
 * preRenderScale (RenderMantis.java:39-45) scales by 1.1 unconditionally (ENT-S-092).
 * The ModelMantis(2.0f) argument is wingspeed, not a size factor.
 */
public class MantisRenderer extends MobRenderer<EntityMantis, MantisModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/mantis.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mantis"), "main");
    /** orig RenderMantis.scale = 1.1f (third constructor argument, ClientProxyOreSpawn.java:488). */
    public static final float SCALE = 1.1F;
    /** orig RenderLiving shadow = 0.9f * 1.1f (RenderMantis.java:23, ClientProxyOreSpawn.java:488). */
    public static final float SHADOW = 0.9F * 1.1F;

    public MantisRenderer(EntityRendererProvider.Context context) {
        super(context, new MantisModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityMantis entity, PoseStack poseStack, float partialTick) {
        // orig RenderMantis.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMantis entity) {
        return TEXTURE;
    }
}
