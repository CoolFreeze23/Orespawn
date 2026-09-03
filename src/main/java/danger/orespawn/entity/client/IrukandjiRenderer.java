package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Irukandji;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderIrukandji.java + ClientProxyOreSpawn.java:485:
 * {@code new RenderIrukandji(new ModelIrukandji(1.0f), 0.1f, 0.25f)} - RenderLiving shadow = par2 * par3
 * (RenderIrukandji.java:23) and preRenderScale scales by par3 = 0.25 (RenderIrukandji.java:24,39-45).
 * The ModelIrukandji(1.0f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class IrukandjiRenderer extends MobRenderer<Irukandji, ModelIrukandji> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/irukandji.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "irukandji"), "main");
    /** orig RenderIrukandji.scale = 0.25f (third constructor argument, ClientProxyOreSpawn.java:485). */
    public static final float SCALE = 0.25F;
    /** orig RenderLiving shadow = 0.1f * 0.25f (RenderIrukandji.java:23). */
    public static final float SHADOW = 0.1F * 0.25F;

    public IrukandjiRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelIrukandji(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Irukandji entity, PoseStack poseStack, float partialTick) {
        // orig RenderIrukandji.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Irukandji entity) {
        return TEXTURE;
    }
}
