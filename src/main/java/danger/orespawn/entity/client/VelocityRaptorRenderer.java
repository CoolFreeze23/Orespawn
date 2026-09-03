package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.VelocityRaptor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig ClientProxyOreSpawn.java:423
 * {@code new RenderVelocityRaptor(new ModelVelocityRaptor(1.25f), 0.55f, 0.75f)}:
 * RenderVelocityRaptor.java:25-28 passes {@code par2 * par3} to RenderLiving as the shadow and
 * keeps {@code par3} as the preRenderCallback scale (ENT-S-092). The 1.25f model argument is
 * wingspeed, not a size.
 */
public class VelocityRaptorRenderer extends MobRenderer<VelocityRaptor, VelocityRaptorModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/velocityraptor.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "velocity_raptor"), "main");
    /** orig RenderVelocityRaptor.scale = 0.75f (third constructor argument). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = 0.55f * 0.75f. */
    public static final float SHADOW = 0.55F * 0.75F;

    public VelocityRaptorRenderer(EntityRendererProvider.Context context) {
        super(context, new VelocityRaptorModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(VelocityRaptor entity, PoseStack poseStack, float partialTick) {
        // orig RenderVelocityRaptor.preRenderCallback (func_77041_b -> preRenderScale,
        // RenderVelocityRaptor.java:42-52) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift):
        //   if (par1Entity != null && par1Entity.func_70631_g_()) { GL11.glScalef(scale / 2.0f, ...); return; }
        //   GL11.glScalef(scale, scale, scale);
        if (entity != null && entity.isBaby()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(VelocityRaptor entity) {
        return TEXTURE;
    }
}
