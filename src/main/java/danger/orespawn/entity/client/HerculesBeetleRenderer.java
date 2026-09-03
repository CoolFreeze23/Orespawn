package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityHerculesBeetle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderHerculesBeetle.java + ClientProxyOreSpawn.java:489:
 * {@code new RenderHerculesBeetle(new ModelHerculesBeetle(1.0f), 0.99f, 1.1f)} - RenderHerculesBeetle.java:22-24
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code scale = par3};
 * preRenderScale (RenderHerculesBeetle.java:39-45) scales by 1.1 unconditionally (ENT-S-092).
 * The ModelHerculesBeetle(1.0f) argument is wingspeed, not a size factor.
 */
public class HerculesBeetleRenderer extends MobRenderer<EntityHerculesBeetle, HerculesBeetleModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/herculesbeetle.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "herculesbeetle"), "main");
    /** orig RenderHerculesBeetle.scale = 1.1f (third constructor argument, ClientProxyOreSpawn.java:489). */
    public static final float SCALE = 1.1F;
    /** orig RenderLiving shadow = 0.99f * 1.1f (RenderHerculesBeetle.java:23, ClientProxyOreSpawn.java:489). */
    public static final float SHADOW = 0.99F * 1.1F;

    public HerculesBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new HerculesBeetleModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityHerculesBeetle entity, PoseStack poseStack, float partialTick) {
        // orig RenderHerculesBeetle.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityHerculesBeetle entity) {
        return TEXTURE;
    }
}
