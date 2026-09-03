package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Kraken;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderKraken.java + ClientProxyOreSpawn.java:444:
 * {@code new RenderKraken(new ModelKraken(1.0f), 1.0f, 1.0f)} - RenderLiving shadow = par2 * par3
 * (RenderKraken.java:23) and preRenderScale scales by par3 = 1.0 (RenderKraken.java:24,39-45). The
 * ModelKraken(1.0f) argument is only wingspeed, not a size (ENT-S-092).
 */
public class KrakenRenderer extends MobRenderer<Kraken, ModelKraken> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/kraken.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kraken"), "main");

    /** orig RenderKraken.scale = 1.0f (third constructor argument, ClientProxyOreSpawn.java:444). */
    public static final float SCALE = 1.0F;
    /** orig RenderLiving shadow = 1.0f * 1.0f (RenderKraken.java:23). */
    public static final float SHADOW = 1.0F * 1.0F;

    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelKraken(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Kraken entity, PoseStack poseStack, float partialTick) {
        // orig RenderKraken.preRenderScale (RenderKraken.java:39-45): GL11.glScalef(scale, scale, scale) - same
        // pipeline position as LivingEntityRenderer.scale (after the (-1,-1,1) flip, before the -1.501 lift).
        // The 1.7.10 PlayNicely branch (scale / 3 while getPlayNicely() != 0, :40-43) is deliberately not
        // reproduced: the port Kraken carries no PlayNicely flag and does not port the paired 1.333x5 setSize
        // swap (orig Kraken.java:74-75); the default mode restored here is the 4x15 one (ENT-S-092).
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(Kraken entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Kraken entity) {
        return TEXTURE;
    }
}
