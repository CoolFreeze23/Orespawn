package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Baryonyx;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Baryonyx (the hook survey, landed by the fourth Tier-2 slice T2d): {@link ModelBaryonyx#setupAnim}
 * verbatim on the converted rig, ON THE HOOK (no keyframe layer, no
 * transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}).
 * Wingspeed 0.25f (orig ModelBaryonyx.java:14,69 / ClientProxyOreSpawn.java: 428): the THRESHOLD gait on the six
 * hind-leg parts ({@code cos(age * 1.3 ws) * PI * 0.15 * limbSwingAmount} above a walking speed of a tenth, the right
 * leg positive, the left the negative, the calves around -0.17) and the two front claws' always-on 0.7-ws wave about
 * Z ({@code PI * 0.25}, mirrored). No entity state. The nineteen sail spines, the six crest segments and the snout
 * ornament are zero-thickness cubes (the seam draws every cube with its true transformed normal, ENT-S-161, and its
 * two coplanar faces in the classic order - below).
 *
 * <p>Shadow follows {@link BaryonyxRenderer}'s constructor ({@code super(context, model, 1.0f)}: the literal it
 * passes - it declares no SHADOW constant); its render scale is {@link BaryonyxRenderer#SCALE} (1.0, lifted to public
 * by the landing slice so both renderers read the one constant), halved for a baby, applied around {@code super.render}
 * (a uniform scale commutes with the rotations and the flip, so the scale slot carries it): the identity for an adult,
 * 0.5 for a baby.</p>
 */
public final class BaryonyxGeoReplacement extends OreSpawnGeoReplacement<Baryonyx> {
    /** orig ModelBaryonyx.java:14,69 {@code wingspeed} = 0.25f (ClientProxyOreSpawn.java:428): the chain's third multiply. */
    static final float WINGSPEED = 0.25F;
    private static final GeoReplacementDescriptor<Baryonyx> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.BARYONYX.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Baryonyx.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/baryonyx.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/baryonyx.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/baryonyx.png"),
            // BaryonyxRenderer's constructor passes the literal 1.0f (no SHADOW constant): the equal literal
            1.0F) {
        @Override
        public void applyScale(Baryonyx entity, PoseStack poseStack, float partialTick) {
            // BaryonyxRenderer.render: float scale = entity.isBaby() ? SCALE / 2.0f : SCALE; poseStack.scale(scale, scale, scale)
            if (entity.isBaby()) {
                poseStack.scale(BaryonyxRenderer.SCALE / 2.0f, BaryonyxRenderer.SCALE / 2.0f, BaryonyxRenderer.SCALE / 2.0f);
                return;
            }
            poseStack.scale(BaryonyxRenderer.SCALE, BaryonyxRenderer.SCALE, BaryonyxRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (shape27-45 and shape46-51, 0 x 2 x 1 and 0 x 1 x 1; shape52, 0 x 2 x 2): the
         * within-cube face order decides a flat cube's z-fight, so the shipped geo carries the classic order
         * ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public BaryonyxGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelBaryonyx.setupAnim verbatim, the float chain left to right; the rig's names: shape24 / 25 / 26 the
        // right back leg, calf and foot, shape13 / 15 / 17 the left, shape21 / 16 the right / left front claw.
        float newangle = 0.0F;
        newangle = limbSwingAmount > 0.1F ? Mth.cos(ageInTicks * 1.3F * WINGSPEED) * (float) Math.PI * 0.15F * limbSwingAmount : 0.0F;
        rotateX(processor, "shape24", newangle);
        rotateX(processor, "shape25", -0.17F + newangle);
        rotateX(processor, "shape26", newangle);
        rotateX(processor, "shape13", -newangle);
        rotateX(processor, "shape15", -0.17F - newangle);
        rotateX(processor, "shape17", -newangle);
        newangle = Mth.cos(ageInTicks * 0.7F * WINGSPEED) * (float) Math.PI * 0.25F;
        rotateZ(processor, "shape21", newangle);
        rotateZ(processor, "shape16", -newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Baryonyx, BaryonyxGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BaryonyxGeoReplacement());
        }
    }
}
