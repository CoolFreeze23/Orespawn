package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GhostSkelly;
import danger.orespawn.entity.pose.GhostSkellyPose;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ghost Skelly (the hook survey, landed by the fifth Tier-2 slice T2e): {@link GhostSkellyModel#poseFrom}
 * verbatim on the converted rig, ON THE HOOK (no keyframe layer, no
 * transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). No
 * wingspeed: the two arm groups (sleeve, chains, arm) sway about Z at 0.2 / 0.22 and about Y at 0.24 / 0.26, every one
 * {@code PI * 0.05}; and the HEAD-SWIVEL LATCH (orig ModelGhostSkelly.java: 99-122): the 0.05 rhythm's phase {@code
 * |age * 0.05 mod 2 PI|} is compared with the last frame's ({@code rf2}) and on each wrap the entity's own random
 * decides ({@code nextInt(3) == 1}) whether {@code ri2} carries the swivel bit; the head yaws a full {@code cos(age *
 * 0.05) * PI * 2} while it does and holds 0 otherwise. The entity is read through {@link GhostSkellyPose} (the
 * Robot2 precedent for a per-entity RenderInfo latch).
 *
 * <p>Scale and shadow follow {@link GhostSkellyRenderer}: 1.05 render scale and a 0.0 x 1.05 shadow (ENT-S-092: no
 * shadow under the hovering skelly). The render type is the classic renderer's translucent pipeline
 * ({@link GhostSkellyRenderer#getRenderType}: {@code RenderType.entityTranslucent}) - handed over as the classic model's
 * own function object {@link GhostSkellyModel#RENDER_TYPE} (the ENT-S-146 form, {@link FairyModel#RENDER_TYPE}; the hook
 * survey returned the bare factory because the model carried no such object, and the landing slice T2e re-based it)
 * so the harness proves the two renderers' render type equal by identity.</p>
 */
public final class GhostSkellyGeoReplacement extends OreSpawnGeoReplacement<GhostSkelly> {
    private static final GeoReplacementDescriptor<GhostSkelly> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GHOST_SKELLY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            GhostSkelly.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ghostskelly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ghostskelly.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ghost_skelly.png"),
            GhostSkellyRenderer.SHADOW) {
        @Override
        public void applyScale(GhostSkelly entity, PoseStack poseStack, float partialTick) {
            // orig RenderGhostSkelly.preRenderCallback: GL11.glScalef(scale, scale, scale) (GhostSkellyRenderer.scale)
            poseStack.scale(GhostSkellyRenderer.SCALE, GhostSkellyRenderer.SCALE, GhostSkellyRenderer.SCALE);
        }

        /**
         * GhostSkellyRenderer.getRenderType: {@code RenderType.entityTranslucent(texture)} - the soft alpha edges kept. The
         * fifth Tier-2 slice (T2e, 2026-09-15) re-based it onto the classic model's own function object
         * ({@link GhostSkellyModel#RENDER_TYPE}, the ENT-S-146 / Fairy form): the very {@code Function} the classic path
         * applies, so the harness proves the two sides equal by identity.
         */
        @Override
        public Function<ResourceLocation, RenderType> renderType(GhostSkelly entity) {
            return GhostSkellyModel.RENDER_TYPE;
        }

        /**
         * A blending rig (entity_translucent) with no zero-thickness cube: the order a cube's six faces are emitted in is
         * visible in the blend, so the shipped geo carries the classic within-cube order ({@link FaceOrder#KEY}; ENT-S-146,
         * the Fairy form - the harness refuses a blending rig whose generated geo carries none) and the seam expects it (T2e).
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public GhostSkellyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        GhostSkellyPose entity = inputs.subject(GhostSkellyPose.class);
        float ageInTicks = inputs.ageInTicks();
        // GhostSkellyModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        float newrf1 = 0.0f;
        // Per-entity scratch as in the original (orig GhostSkelly.java:22, orig ModelGhostSkelly.java:99-122): the
        // head-swivel latch (ENT-S-093).
        RenderInfo r = entity.getRenderInfo();
        float lchainsZRot = Mth.cos(ageInTicks * 0.2f) * (float) Math.PI * 0.05f;
        rotateZ(processor, "lsleeve", lchainsZRot);
        rotateZ(processor, "lchains", lchainsZRot);
        rotateZ(processor, "larm", lchainsZRot);
        float rchainsZRot = Mth.cos(ageInTicks * 0.22f) * (float) Math.PI * 0.05f;
        rotateZ(processor, "rsleeve", rchainsZRot);
        rotateZ(processor, "rchains", rchainsZRot);
        rotateZ(processor, "rarm", rchainsZRot);
        float lchainsYRot = Mth.cos(ageInTicks * 0.24f) * (float) Math.PI * 0.05f;
        rotateY(processor, "lsleeve", lchainsYRot);
        rotateY(processor, "lchains", lchainsYRot);
        rotateY(processor, "larm", lchainsYRot);
        float rchainsYRot = Mth.cos(ageInTicks * 0.26f) * (float) Math.PI * 0.05f;
        rotateY(processor, "rsleeve", rchainsYRot);
        rotateY(processor, "rchains", rchainsYRot);
        rotateY(processor, "rarm", rchainsYRot);
        newangle = Mth.cos(ageInTicks * 0.05f) * (float) Math.PI * 2.0f;
        newrf1 = ageInTicks * 0.05f % ((float) Math.PI * 2);
        newrf1 = Math.abs(newrf1);
        if (newrf1 < r.rf2) {
            r.ri2 = 0;
            if (entity.getRandom().nextInt(3) == 1) {
                r.ri2 |= 1;
            }
        }
        r.rf2 = newrf1;
        if ((r.ri2 & 1) == 0) {
            newangle = 0.0f;
        }
        rotateY(processor, "head", newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<GhostSkelly, GhostSkellyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GhostSkellyGeoReplacement());
        }
    }
}
