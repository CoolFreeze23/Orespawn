package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Chipmunk;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderChipmunk.java + ClientProxyOreSpawn.java:448:
 * {@code new RenderChipmunk(new ModelChipmunk(1.0f), 0.15f, 0.9f)} - RenderLiving shadow = par2 * par3
 * (RenderChipmunk.java:26) and preRenderScale scales by 0.9, or 0.9 / 2 for a child
 * (RenderChipmunk.java:42-48) (ENT-S-092).
 */
public class ChipmunkRenderer extends MobRenderer<Chipmunk, ModelChipmunk> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/chipmunk.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chipmunk"), "main");

    /** orig RenderChipmunk.scale = 0.9f (third constructor argument, RenderChipmunk.java:27). */
    public static final float SCALE = 0.9F;
    /** orig RenderLiving shadow = 0.15f * 0.9f (RenderChipmunk.java:26). */
    public static final float SHADOW = 0.15F * 0.9F;

    public ChipmunkRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelChipmunk(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Chipmunk entity, PoseStack poseStack, float partialTick) {
        // orig RenderChipmunk.preRenderScale (RenderChipmunk.java:42-48): a child (func_70631_g_)
        // gets GL11.glScalef(scale / 2.0f, ...), otherwise GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        if (entity.isBaby()) {
            poseStack.scale(SCALE / 2.0F, SCALE / 2.0F, SCALE / 2.0F);
            return;
        }
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Chipmunk entity) {
        return TEXTURE;
    }
}
