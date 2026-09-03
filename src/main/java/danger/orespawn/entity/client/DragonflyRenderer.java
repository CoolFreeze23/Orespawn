package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityDragonfly;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderDragonfly.java + ClientProxyOreSpawn.java:424:
 * {@code new RenderDragonfly(new ModelDragonfly(2.0f), 0.3f, 1.5f)} - RenderLiving
 * shadow = par2 * par3 (RenderDragonfly.java:23) and preRenderCallback scales by
 * par3 = 1.5 (RenderDragonfly.java:24,39-45) (ENT-S-092).
 */
public class DragonflyRenderer extends MobRenderer<EntityDragonfly, DragonflyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dragonfly.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "dragonfly"), "main");

    /** orig RenderLiving shadow = 0.3f * 1.5f (ClientProxyOreSpawn.java:424, RenderDragonfly.java:23). */
    public static final float SHADOW = 0.3F * 1.5F;
    /** orig RenderDragonfly.scale = 1.5f (third constructor argument, RenderDragonfly.java:24). */
    public static final float SCALE = 1.5F;

    public DragonflyRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonflyModel(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityDragonfly entity, PoseStack poseStack, float partialTick) {
        // orig RenderDragonfly.preRenderCallback: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityDragonfly entity) {
        return TEXTURE;
    }
}
