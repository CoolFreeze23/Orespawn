package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Lizard;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderLizard.java + ClientProxyOreSpawn.java:445:
 * {@code new RenderLizard(new ModelLizard(0.65f), 0.75f, 1.0f)}:
 * RenderLiving shadow = par2 * par3 (RenderLizard.java:26), this.scale = par3
 * (:27), and preRenderScale (:42-48, wired through func_77041_b at :50-52) draws
 * children at scale / 2 (ENT-S-092). The ModelLizard(0.65f) argument is wingspeed
 * only, not a scale.
 */
public class LizardRenderer extends MobRenderer<Lizard, LizardModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lizard.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "lizard"), "main");

    /** orig RenderLizard.scale = 1.0f (third constructor argument). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 0.75f * 1.0f. */
    public static final float SHADOW = 0.75F * 1.0F;

    public LizardRenderer(EntityRendererProvider.Context context) {
        super(context, new LizardModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Lizard entity, PoseStack poseStack, float partialTick) {
        // orig RenderLizard.preRenderScale (RenderLizard.java:42-48):
        //   if (par1Entity != null && par1Entity.isChild()) { GL11.glScalef(scale / 2.0f, ...); return; }
        //   GL11.glScalef(scale, scale, scale);
        // Same pipeline position as LivingEntityRenderer.scale (after the (-1,-1,1) flip,
        // before the -1.501 lift). The entity is never null here.
        float s = entity.isBaby() ? SCALE / 2.0F : SCALE;
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(Lizard entity) {
        return TEXTURE;
    }
}
