package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Alien;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderAlien.java + ClientProxyOreSpawn.java:435:
 * {@code new RenderAlien(new ModelAlien(0.22f), 0.35f, 1.1f)} - RenderLiving shadow = par2 * par3
 * (RenderAlien.java:23) and preRenderScale scales by 1.1 (RenderAlien.java:39-41) (ENT-S-092).
 * Shared by ALIEN and ALIEN_BOSS (OreSpawnClient), exactly as both were drawn by RenderAlien in 1.7.10.
 */
public class AlienRenderer extends MobRenderer<Alien, ModelAlien> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/alien.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien"), "main");

    /** orig RenderAlien.scale = 1.1f (third constructor argument, RenderAlien.java:24). */
    public static final float SCALE = 1.1F;
    /** orig RenderLiving shadow = 0.35f * 1.1f (RenderAlien.java:23). */
    public static final float SHADOW = 0.35F * 1.1F;

    public AlienRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelAlien(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Alien entity, PoseStack poseStack, float partialTick) {
        // orig RenderAlien.preRenderScale: GL11.glScalef(scale, scale, scale) - same pipeline
        // position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Alien entity) {
        return TEXTURE;
    }
}
