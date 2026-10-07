package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheKing;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * The King entity renderer. Migrated from 1.7.10 {@code RenderTheKing}
 * (see {@code reference_1_7_10_source/sources/danger/orespawn/RenderTheKing.java}).
 *
 * <p>The main difference vs. a stock {@link MobRenderer} is the multi-pass render
 * sequence required to reproduce the King's translucent wing membranes:
 * <ol>
 *   <li>Pass 1 (opaque): delegates to {@code super.render(...)} which invokes
 *       {@link ModelTheKing#renderToBuffer} on the entity-cutout buffer. Renders
 *       the head cluster, neck chains, body, tail, legs, wing bones, etc.</li>
 *   <li>Pass 2 (translucent): {@link WingMembraneLayer}, inside the living renderer's own pose, invokes
 *       {@link ModelTheKing#renderWingMembranes} against a
 *       {@link RenderType#entityTranslucent} buffer with a packed tint
 *       reproducing the legacy {@code glColor4f(0.75, 0.75, 0.75, 0.55)} look
 *       the 1.7.10 model used inside its {@code func_78088_a} GL block.</li>
 * </ol>
 *
 * <p>{@link #shouldRender} always returns {@code true} because the King has a huge
 * bounding box that often straddles the view frustum in ways vanilla culling
 * incorrectly rejects (it would cause visible pop-out of body parts).
 *
 * <p>orig RenderTheKing.java + ClientProxyOreSpawn.java:492:
 * {@code new RenderTheKing(new ModelTheKing(0.65f), 1.9f, 2.1f)} - RenderLiving shadow = par2 * par3
 * (RenderTheKing.java:23) and preRenderScale scales by par3 = 2.1, or par3 / 4 while PlayNicely
 * (RenderTheKing.java:24,39-45 via func_77041_b :47-48). The ModelTheKing(0.65f) argument is only
 * wingspeed, not a size (ENT-S-092).
 */
public class TheKingRenderer extends MobRenderer<TheKing, ModelTheKing> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/thekingtexture.png");

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "theking"), "main");

    /** orig RenderTheKing.scale = 2.1f (third constructor argument, ClientProxyOreSpawn.java:492). */
    public static final float SCALE = 2.1F;
    /** orig RenderLiving shadow = 1.9f * 2.1f (RenderTheKing.java:23). */
    public static final float SHADOW = 1.9F * 2.1F;

    // Original 1.7.10 GL state: glColor4f(0.75, 0.75, 0.75, 0.55), packed as the ARGB int for Blaze3D's {@code color}
    // parameter - lifted onto the model as ModelTheKing.WING_MEMBRANE_COLOR beside the pass's render-type function
    // ModelTheKing.WING_MEMBRANE_RENDER_TYPE (the remainder slice, 2026-09-15; TEST-018: the ENT-S-146 form, so the GeckoLib
    // descriptor's second pass hands over the same objects and the parity harness proves the two renderers' pass equal).

    public TheKingRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTheKing(context.bakeLayer(MODEL_LAYER)), SHADOW);
        // the translucent wing membranes, a second pass inside the living renderer's own pose (yaw, death tilt, flip,
        // scale): this pass once rebuilt the transform itself with an extra 1.501 lift and drew the membranes 1.5
        // blocks above the wings, also while the King was invisible
        this.addLayer(new WingMembraneLayer<>(this, ModelTheKing::renderWingMembranes, ModelTheKing.WING_MEMBRANE_RENDER_TYPE,
                ModelTheKing.WING_MEMBRANE_COLOR));
    }

    @Override
    protected void scale(TheKing entity, PoseStack poseStack, float partialTick) {
        // BOSS-017 / ENT-S-092: orig RenderTheKing.preRenderScale (RenderTheKing.java:39-45): a PlayNicely
        // King (getPlayNicely() != 0) gets GL11.glScalef(scale / 4.0f, ...), otherwise
        // GL11.glScalef(scale, scale, scale) - same pipeline position as LivingEntityRenderer.scale
        // (after the (-1,-1,1) flip, before the -1.501 lift); the wing-membrane layer draws under it.
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
    public boolean shouldRender(TheKing entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(TheKing entity) {
        return TEXTURE;
    }
}
