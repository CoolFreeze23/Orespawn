package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Mothra;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderButterfly.java + ClientProxyOreSpawn.java:411:
 * {@code new RenderButterfly(new ModelButterfly(0.2f), 0.75f, 10.0f)} - RenderLiving shadow = par2 * par3
 * (RenderButterfly.java:25) and preRenderScale scales by par3 = 10.0 (RenderButterfly.java:26,41-47).
 * The ModelButterfly(0.2f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class MothraRenderer extends MobRenderer<Mothra, ButterflyModel<Mothra>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothra"), "main");
    /** orig RenderButterfly.scale = 10.0f (third constructor argument, ClientProxyOreSpawn.java:411). */
    public static final float SCALE = 10.0F;
    /** orig RenderLiving shadow = 0.75f * 10.0f (RenderButterfly.java:25). */
    public static final float SHADOW = 0.75F * 10.0F;

    public MothraRenderer(EntityRendererProvider.Context context) {
        // wingspeed 0.2f — orig ClientProxyOreSpawn.java:411 (new ModelButterfly(0.2f))
        super(context, new ButterflyModel<>(context.bakeLayer(MODEL_LAYER), 0.2f), SHADOW);
    }

    @Override
    protected void scale(Mothra entity, PoseStack poseStack, float partialTick) {
        // 10.0f — orig ClientProxyOreSpawn.java:411 third arg, applied via orig RenderButterfly.java:26,42 (glScalef)
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(Mothra entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Mothra entity) {
        return TEXTURE;
    }
}
