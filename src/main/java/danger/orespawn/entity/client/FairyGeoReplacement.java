package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Fairy;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Fairy (the third Tier-2 slice, 2026-09-13): {@link FairyModel#setupAnim} verbatim on the converted rig, ON
 * THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays
 * closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 1.5f (orig ModelFairy.java:16,34 /
 * ClientProxyOreSpawn.java:477): the outer wings about Y at the wingspeed itself, the inner wings at 0.85 of it, the
 * arms about X at 0.15 / 0.12 and about Z at 0.1 / 0.11 of it (orig :134-137, :146-149), and the HEAD-LOOK idiom (orig
 * :138-145): yaw = {@code toRadians(netHeadYaw) * 0.45f} clamped to +-0.45 rad, pitch = {@code toRadians(headPitch)}.
 * The four wings are zero-thickness cubes (the seam draws every cube with its true transformed normal, ENT-S-161, and
 * its two coplanar faces in the classic order - below).
 *
 * <p>Scale and shadow follow {@link FairyRenderer}: 0.35 render scale and a 0.1 x 0.35 shadow (ENT-S-092); the
 * texture is the fairy type's sheet ({@link FairyRenderer#textureFor}); the render type is the classic model's own
 * translucent function object ({@link FairyModel#RENDER_TYPE}, the ENT-S-146 form) so the harness proves the two
 * renderers' render state equal by identity.</p>
 */
public final class FairyGeoReplacement extends OreSpawnGeoReplacement<Fairy> {
    /** orig ModelFairy.java:16,34 {@code wingspeed} = 1.5f (ClientProxyOreSpawn.java:477): the chain's second multiply. */
    static final float WINGSPEED = 1.5F;
    private static final GeoReplacementDescriptor<Fairy> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.FAIRY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Fairy.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/fairy.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/fairy.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fairytexture.png"),
            FairyRenderer.SHADOW) {
        @Override
        public ResourceLocation texture(Fairy entity) {
            return FairyRenderer.textureFor(entity.getFairyType());
        }

        @Override
        public void applyScale(Fairy entity, PoseStack poseStack, float partialTick) {
            // orig RenderFairy.preRenderScale: GL11.glScalef(scale, scale, scale) (FairyRenderer.scale)
            poseStack.scale(FairyRenderer.SCALE, FairyRenderer.SCALE, FairyRenderer.SCALE);
        }

        /**
         * The classic model's own render-type function object ({@link FairyModel#RENDER_TYPE},
         * {@code RenderType::entityTranslucent}: the gradient wing alpha the classic renderer keeps): the SAME
         * {@code Function} the classic path applies, so the harness proves the two sides equal by identity.
         */
        @Override
        public Function<ResourceLocation, RenderType> renderType(Fairy entity) {
            return FairyModel.RENDER_TYPE;
        }

        /**
         * A blending rig with zero-thickness cubes (the four wings, 24 x 16 x 0 and 26 x 16 x 0): the within-cube face
         * order is visible under blending and decides a flat cube's z-fight, so the shipped geo carries the classic
         * order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public FairyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // FairyModel.setupAnim verbatim, the float chain left to right.
        rotateY(processor, "lwing1", -0.6f + Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.35f);
        rotateY(processor, "rwing1", -2.55f - Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.35f);
        rotateY(processor, "lwing2", -0.6f + Mth.cos(ageInTicks * WINGSPEED * 0.85f) * (float) Math.PI * 0.25f);
        rotateY(processor, "rwing2", -2.55f - Mth.cos(ageInTicks * WINGSPEED * 0.85f) * (float) Math.PI * 0.25f);
        float headYaw = (float) Math.toRadians(netHeadYaw) * 0.45f;
        if (headYaw > 0.45f) {
            headYaw = 0.45f;
        }
        if (headYaw < -0.45f) {
            headYaw = -0.45f;
        }
        rotateY(processor, "head", headYaw);
        rotateX(processor, "head", (float) Math.toRadians(headPitch));
        rotateX(processor, "larm", -0.2f + Mth.cos(ageInTicks * WINGSPEED * 0.15f) * (float) Math.PI * 0.05f);
        rotateX(processor, "rarm", -0.2f + Mth.cos(ageInTicks * WINGSPEED * 0.12f) * (float) Math.PI * 0.05f);
        rotateZ(processor, "larm", -0.15f + Mth.cos(ageInTicks * WINGSPEED * 0.1f) * (float) Math.PI * 0.03f);
        rotateZ(processor, "rarm", 0.15f + Mth.cos(ageInTicks * WINGSPEED * 0.11f) * (float) Math.PI * 0.03f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Fairy, FairyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new FairyGeoReplacement());
        }
    }
}
