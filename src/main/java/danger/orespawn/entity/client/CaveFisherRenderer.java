package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.CaveFisher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCaveFisher.java + ClientProxyOreSpawn.java:434:
 * {@code new RenderCaveFisher(new ModelCaveFisher(0.62f), 0.35f, 0.75f)} - RenderCaveFisher.java:23
 * passes {@code par2 * par3} = 0.35f * 0.75f to RenderLiving as the shadow and :24 keeps
 * {@code scale = par3} = 0.75f for preRenderScale (ENT-S-092). The 0.62f model argument is only
 * ModelCaveFisher.wingspeed, not a size factor.
 */
public class CaveFisherRenderer extends MobRenderer<CaveFisher, ModelCaveFisher> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cavefisher.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cavefisher"), "main");

    /** orig RenderCaveFisher.scale = 0.75f (third constructor argument, ClientProxyOreSpawn.java:434). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = par2 * par3 = 0.35f * 0.75f (RenderCaveFisher.java:23). */
    public static final float SHADOW = 0.35F * 0.75F;

    public CaveFisherRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCaveFisher(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(CaveFisher entity, PoseStack poseStack, float partialTick) {
        // orig RenderCaveFisher.preRenderScale (func_77041_b, RenderCaveFisher.java:39-45):
        // GL11.glScalef(scale, scale, scale) unconditionally - same pipeline position as
        // LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(CaveFisher entity) {
        return TEXTURE;
    }
}
