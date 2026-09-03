package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GhostSkelly;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderGhostSkelly.java + ClientProxyOreSpawn.java:410:
 * {@code new RenderGhostSkelly(new ModelGhostSkelly(), 0.0f, 1.05f)} - RenderGhostSkelly.java:22-24
 * passes {@code par2 * par3} to RenderLiving as the shadow and keeps {@code scale = par3};
 * preRenderScale (RenderGhostSkelly.java:39-45) scales by 1.05 unconditionally (ENT-S-092).
 * The shadow product is 0.0, so 1.7.10 drew no shadow under the hovering skelly; a 0.0
 * shadowRadius fails the EntityRenderDispatcher {@code f > 0.0F} gate the same way here.
 */
public class GhostSkellyRenderer extends MobRenderer<GhostSkelly, GhostSkellyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ghost_skelly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ghost_skelly"), "main");
    /** orig RenderGhostSkelly.scale = 1.05f (third constructor argument, ClientProxyOreSpawn.java:410). */
    public static final float SCALE = 1.05F;
    /** orig RenderLiving shadow = 0.0f * 1.05f (RenderGhostSkelly.java:23, ClientProxyOreSpawn.java:410). */
    public static final float SHADOW = 0.0F * 1.05F;

    public GhostSkellyRenderer(EntityRendererProvider.Context context) {
        super(context, new GhostSkellyModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(GhostSkelly entity, PoseStack poseStack, float partialTick) {
        // orig RenderGhostSkelly.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(GhostSkelly entity) {
        return TEXTURE;
    }

    // Same reasoning as GhostRenderer — send the skeletal ghost through the
    // translucent pipeline so its soft alpha edges don't get cutout-clipped.
    @Override
    public RenderType getRenderType(GhostSkelly entity, boolean visible, boolean visibleToPlayer, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }
}
