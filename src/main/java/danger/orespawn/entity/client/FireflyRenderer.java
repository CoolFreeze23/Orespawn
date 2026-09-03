package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Firefly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig ClientProxyOreSpawn.java:406
 * {@code new RenderFirefly(new ModelFirefly(2.5f), 0.2f, 0.75f)}: RenderFirefly.java:21-24
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code par3} as the
 * preRenderCallback scale (ENT-S-092). The 2.5f model argument is wingspeed, not a size.
 */
public class FireflyRenderer extends MobRenderer<Firefly, FireflyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fireflytexture.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "firefly"), "main");
    /** orig RenderFirefly.scale = 0.75f (third constructor argument). */
    public static final float SCALE = 0.75F;
    /** orig RenderLiving shadow = 0.2f * 0.75f. */
    public static final float SHADOW = 0.2F * 0.75F;

    public FireflyRenderer(EntityRendererProvider.Context context) {
        super(context, new FireflyModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Firefly entity, PoseStack poseStack, float partialTick) {
        // orig RenderFirefly.preRenderCallback (func_77041_b -> preRenderScale, RenderFirefly.java:38-44):
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Firefly entity) {
        return TEXTURE;
    }
}
