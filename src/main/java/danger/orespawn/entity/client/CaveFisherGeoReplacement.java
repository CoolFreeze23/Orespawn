package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.CaveFisher;
import danger.orespawn.entity.pose.CaveFisherPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cave Fisher (the hooks): {@link ModelCaveFisher#poseFrom} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}).
 * Wingspeed 0.62f (orig ModelCaveFisher.java:15,93 / ClientProxyOreSpawn.java:434): the GAIT-scaled idiom on
 * the thirty-six leg parts about Y - the front, middle and back pairs on {@code cos(age * 2.0f * ws - k * 1.570795f) *
 * PI * 0.12f * limbSwingAmount}, k = 0 / 1 / 2, the right side the negative (orig :551-592; no threshold) - and the
 * CLAW-SNAP LATCH (orig :593-612, the Robot2 precedent): at each falling zero-crossing of a 3.0 ws cosine the per-entity
 * {@link RenderInfo}'s {@code ri1} / {@code ri2} are re-rolled from the entity's RNG, the ranges by the ATTACKING flag
 * (20 / 25 at rest, 4 / 3 attacking), and while {@code ri1} is 1 or 3 the two claws lift on {@code |cos|} through {@link
 * #doLeftClaw} / {@link #doRightClaw} (orig :701-721, the helpers transcribed under their names), else they rest
 * at 0 / -0.54 / +0.35. The entity is read through {@link CaveFisherPose} (ENT-S-093's interface, already on the entity:
 * the render scratch, the attacking flag, the RNG).
 *
 * <p>Scale and shadow follow {@link CaveFisherRenderer}: 0.75 render scale and a 0.35 x 0.75 shadow (ENT-S-092).</p>
 */
public final class CaveFisherGeoReplacement extends OreSpawnGeoReplacement<CaveFisher> {
    /** orig ModelCaveFisher.java:15,93 {@code wingspeed} = 0.62f (ClientProxyOreSpawn.java:434): the chain's third multiply. */
    static final float WINGSPEED = 0.62F;
    private static final GeoReplacementDescriptor<CaveFisher> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CAVE_FISHER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            CaveFisher.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cavefisher.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cavefisher.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cavefisher.png"),
            CaveFisherRenderer.SHADOW) {
        @Override
        public void applyScale(CaveFisher entity, PoseStack poseStack, float partialTick) {
            // orig RenderCaveFisher.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) (CaveFisherRenderer.scale)
            poseStack.scale(CaveFisherRenderer.SCALE, CaveFisherRenderer.SCALE, CaveFisherRenderer.SCALE);
        }
    };

    public CaveFisherGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        CaveFisherPose entity = inputs.subject(CaveFisherPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelCaveFisher.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = Mth.cos((float) (ageInTicks * 2.0f * WINGSPEED)) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "LFLeg1", newangle);
        rotateY(processor, "LFLeg2", newangle);
        rotateY(processor, "LFLeg3", newangle);
        rotateY(processor, "LFLeg4", newangle);
        rotateY(processor, "LFLeg5", newangle);
        rotateY(processor, "LFLeg6", newangle);
        rotateY(processor, "RFLeg1", -newangle);
        rotateY(processor, "RFLeg2", -newangle);
        rotateY(processor, "RFLeg3", -newangle);
        rotateY(processor, "RFLeg4", -newangle);
        rotateY(processor, "RFLeg5", -newangle);
        rotateY(processor, "RFLeg6", -newangle);
        newangle = Mth.cos((float) (ageInTicks * 2.0f * WINGSPEED - 1.0f * pi4)) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "LMLeg1", newangle);
        rotateY(processor, "LMLeg2", newangle);
        rotateY(processor, "LMLeg3", newangle);
        rotateY(processor, "LMLeg4", newangle);
        rotateY(processor, "LMLeg5", newangle);
        rotateY(processor, "LMLeg6", newangle);
        rotateY(processor, "RMLeg1", -newangle);
        rotateY(processor, "RMLeg2", -newangle);
        rotateY(processor, "RMLeg3", -newangle);
        rotateY(processor, "RMLeg4", -newangle);
        rotateY(processor, "RMLeg5", -newangle);
        rotateY(processor, "RMLeg6", -newangle);
        newangle = Mth.cos((float) (ageInTicks * 2.0f * WINGSPEED - 2.0f * pi4)) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "LBLeg1", newangle);
        rotateY(processor, "LBLeg2", newangle);
        rotateY(processor, "LBLeg3", newangle);
        rotateY(processor, "LBLeg4", newangle);
        rotateY(processor, "LBLeg5", newangle);
        rotateY(processor, "LBLeg6", newangle);
        rotateY(processor, "RBLeg1", -newangle);
        rotateY(processor, "RBLeg2", -newangle);
        rotateY(processor, "RBLeg3", -newangle);
        rotateY(processor, "RBLeg4", -newangle);
        rotateY(processor, "RBLeg5", -newangle);
        rotateY(processor, "RBLeg6", -newangle);
        // orig ModelCaveFisher.java:593 - per-entity scratch; ri1 is the claw-snap latch (ENT-S-093).
        RenderInfo r = entity.getRenderInfo();
        newangle = Mth.cos((float) (ageInTicks * 3.0f * WINGSPEED)) * (float) Math.PI * 0.15f;
        nextangle = Mth.cos((float) ((ageInTicks + 0.1f) * 3.0f * WINGSPEED)) * (float) Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (entity.getAttacking() == 0) {
                r.ri1 = entity.getRandom().nextInt(20);
                r.ri2 = entity.getRandom().nextInt(25);
            } else {
                r.ri1 = entity.getRandom().nextInt(4);
                r.ri2 = entity.getRandom().nextInt(3);
            }
        }
        if (r.ri1 == 1 || r.ri1 == 3) {
            this.doLeftClaw(processor, newangle);
            this.doRightClaw(processor, newangle);
        } else {
            this.doLeftClaw(processor, 0.0f);
            this.doRightClaw(processor, 0.0f);
        }
    }

    /** orig ModelCaveFisher.java:701-710 - rectified pitch lift on the five left arm segments and the claw; Top/Low carry the absolute rest pitches -0.54f / +0.35f. */
    private void doLeftClaw(AnimationProcessor<?> processor, float angle) {
        rotateX(processor, "LeftArmSeg1", Math.abs(angle));
        rotateX(processor, "LeftArmSeg2", Math.abs(angle));
        rotateX(processor, "LeftArmSeg3", Math.abs(angle));
        rotateX(processor, "LeftArmSeg4", Math.abs(angle));
        rotateX(processor, "LeftArmSeg5", Math.abs(angle));
        rotateX(processor, "LeftClawBase", Math.abs(angle));
        rotateX(processor, "LeftClawTop", Math.abs(angle) - 0.54f);
        rotateX(processor, "LeftClawLow", Math.abs(angle) + 0.35f);
    }

    /** orig ModelCaveFisher.java:712-721 - mirror of doLeftClaw over the right arm. */
    private void doRightClaw(AnimationProcessor<?> processor, float angle) {
        rotateX(processor, "RightArmSeg1", Math.abs(angle));
        rotateX(processor, "RightArmSeg2", Math.abs(angle));
        rotateX(processor, "RightArmSeg3", Math.abs(angle));
        rotateX(processor, "RightArmSeg4", Math.abs(angle));
        rotateX(processor, "RightArmSeg5", Math.abs(angle));
        rotateX(processor, "RightClawBase", Math.abs(angle));
        rotateX(processor, "RightClawTop", Math.abs(angle) - 0.54f);
        rotateX(processor, "RightClawLow", Math.abs(angle) + 0.35f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<CaveFisher, CaveFisherGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CaveFisherGeoReplacement());
        }
    }
}
