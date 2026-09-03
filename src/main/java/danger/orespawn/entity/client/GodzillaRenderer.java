package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Godzilla;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * orig RenderGodzilla.java + ClientProxyOreSpawn.java:462:
 * {@code new RenderGodzilla(new ModelGodzilla(0.2f), 1.0f, 2.0f)} - RenderLiving shadow = par2 * par3
 * (RenderGodzilla.java:23) and preRenderScale scales by par3 = 2.0, or par3 / 4 while PlayNicely
 * (RenderGodzilla.java:24,39-45 via func_77041_b :47-49). The ModelGodzilla(0.2f) argument is only
 * wingspeed, not a size (ENT-S-092).
 */
public class GodzillaRenderer extends MobRenderer<Godzilla, ModelGodzilla> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/godzillatexture.png");
    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "godzilla"), "main");
    /** orig RenderGodzilla.scale = 2.0f (third constructor argument, ClientProxyOreSpawn.java:462). */
    public static final float SCALE = 2.0F;
    /** orig RenderLiving shadow = 1.0f * 2.0f (RenderGodzilla.java:23). */
    public static final float SHADOW = 1.0F * 2.0F;

    public GodzillaRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelGodzilla(context.bakeLayer(MODEL_LAYER)), SHADOW);
    }

    @Override
    protected void scale(Godzilla entity, PoseStack poseStack, float partialTick) {
        // BOSS-017 / ENT-S-092: orig RenderGodzilla.preRenderScale (RenderGodzilla.java:39-45): a PlayNicely
        // Godzilla (getPlayNicely() != 0) gets GL11.glScalef(scale / 4.0f, ...), otherwise
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift).
        float effectiveScale = entity.getPlayNicely() != 0 ? SCALE / 4.0F : SCALE;
        poseStack.scale(effectiveScale, effectiveScale, effectiveScale);
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(Godzilla entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Godzilla entity) {
        return TEXTURE;
    }
}
