package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCricket;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderCricket.java + ClientProxyOreSpawn.java:510:
 * {@code new RenderCricket(new ModelCricket(2.5f), 0.15f, 0.5f)} - RenderLiving shadow = par2 * par3
 * (RenderCricket.java:23) and preRenderScale scales by par3 = 0.5 (RenderCricket.java:24,39-45).
 * The ModelCricket(2.5f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class CricketRenderer extends MobRenderer<EntityCricket, CricketModel<EntityCricket>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cricket.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cricket"), "main");
    /** orig RenderCricket.scale = 0.5f (third constructor argument, ClientProxyOreSpawn.java:510). */
    public static final float SCALE = 0.5F;
    /** orig RenderLiving shadow = 0.15f * 0.5f (RenderCricket.java:23). */
    public static final float SHADOW = 0.15F * 0.5F;

    public CricketRenderer(EntityRendererProvider.Context context) {
        super(context, new CricketModel<>(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(EntityCricket entity, PoseStack poseStack, float partialTick) {
        // orig RenderCricket.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCricket entity) {
        return TEXTURE;
    }
}
