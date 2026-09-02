package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Skate;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class SkateRenderer extends MobRenderer<Skate, ModelSkate> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/skate.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "skate"), "main");

    /** orig ClientProxyOreSpawn.java:486 new RenderSkate(new ModelSkate(1.0f), 0.1f, 0.75f): RenderLiving shadow = par2 * par3 (ENT-S-092). */
    public static final float SHADOW = 0.075F;
    /** orig preRenderCallback scale = par3 (ENT-S-092). */
    public static final float SCALE = 0.75F;

    public SkateRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelSkate(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(Skate entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(Skate entity, PoseStack poseStack, float partialTick) {
        // orig preRenderScale: GL11.glScalef(scale, scale, scale), the LivingEntityRenderer.scale slot
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
