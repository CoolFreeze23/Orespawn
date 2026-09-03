package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrinceTeen;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderThePrinceTeen.java + ClientProxyOreSpawn.java:503:
 * {@code new RenderThePrinceTeen(new ModelThePrinceTeen(0.65f), 1.0f, 1.25f)} - RenderLiving
 * shadow = par2 * par3 (RenderThePrinceTeen.java:23) and preRenderCallback scales by
 * par3 = 1.25 (RenderThePrinceTeen.java:24,39-45) (ENT-S-092).
 */
public class ThePrinceTeenRenderer extends MobRenderer<ThePrinceTeen, ModelThePrinceTeen> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/theprince_teen.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "theprinceteen"), "main");

    /** orig RenderLiving shadow = 1.0f * 1.25f (ClientProxyOreSpawn.java:503, RenderThePrinceTeen.java:23). */
    public static final float SHADOW = 1.0F * 1.25F;
    /** orig RenderThePrinceTeen.scale = 1.25f (third constructor argument, RenderThePrinceTeen.java:24). */
    public static final float SCALE = 1.25F;

    public ThePrinceTeenRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelThePrinceTeen(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(ThePrinceTeen entity, PoseStack poseStack, float partialTick) {
        // orig RenderThePrinceTeen.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(ThePrinceTeen entity) {
        return TEXTURE;
    }
}
