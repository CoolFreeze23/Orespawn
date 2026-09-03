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
 * (RenderKraken.java:23) and preRenderScale scales by par3 = 1.0, or par3 / 3 while PlayNicely
 * (RenderKraken.java:24,39-45 via func_77041_b :47-49). The ModelKraken(1.0f) argument is only
 * wingspeed, not a size (ENT-S-092).
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
        // ENT-S-096 / ENT-S-092: orig RenderKraken.preRenderScale (RenderKraken.java:39-45): a PlayNicely
        // Kraken (getPlayNicely() != 0) gets GL11.glScalef(scale / 3.0f, ...), otherwise
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift). getPlayNicely() is the synced watcher copy
        // of the flag (orig Kraken.java:97/:111/:914), paired with the 1.3333334x5 constructor-time hitbox
        // snapshot in Kraken#getDefaultDimensions (orig Kraken.java:70-76) - the BOSS-017 King pattern.
        float effectiveScale = entity.getPlayNicely() != 0 ? SCALE / 3.0F : SCALE;
        poseStack.scale(effectiveScale, effectiveScale, effectiveScale);
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
