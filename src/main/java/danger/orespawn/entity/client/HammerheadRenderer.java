package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Hammerhead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderHammerhead.java + ClientProxyOreSpawn.java:501:
 * {@code new RenderHammerhead(new ModelHammerhead(0.33f), 1.0f, 2.5f)} - RenderHammerhead.java:23
 * passes {@code par2 * par3} to RenderLiving as the shadow and :24 keeps {@code scale = par3};
 * preRenderScale :39-41 scales by scale unconditionally (ENT-S-092).
 */
public class HammerheadRenderer extends MobRenderer<Hammerhead, ModelHammerhead> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/hammerhead.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead"), "main");
    /** orig RenderHammerhead.scale = 2.5f (third constructor argument). */
    public static final float SCALE = 2.5F;
    /** orig RenderLiving shadow = 1.0f * 2.5f. */
    public static final float SHADOW = 1.0F * 2.5F;

    public HammerheadRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelHammerhead(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Hammerhead entity, PoseStack poseStack, float partialTick) {
        // orig RenderHammerhead.preRenderScale (:39-41, the preRenderCallback slot):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift). The port's former render() wrapper
        // (poseStack.scale(2.0) around super.render) was a port invention; 1.7.10 only scaled here.
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Hammerhead entity) {
        return TEXTURE;
    }
}
