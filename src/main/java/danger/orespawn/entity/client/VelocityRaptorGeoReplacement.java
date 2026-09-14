package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.VelocityRaptor;
import danger.orespawn.entity.pose.VelocityRaptorPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Velocity Raptor (the hooks, landed by the fifth Tier-2 slice T2e): {@link VelocityRaptorModel#poseFrom} verbatim on the converted rig, ON THE HOOK
 * (no keyframe layer, no transcription
 * - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}).
 * Wingspeed 1.25f (orig ModelVelocityRaptor.java:15,52 / ClientProxyOreSpawn.java:423): the THRESHOLD idiom on the
 * eight leg parts about X - above a walking speed of a tenth {@code cos(age x 1.3 x ws) x PI x 0.25 x
 * limbSwingAmount} (the left positive, the right the negative) about 0 / 0.488 / 0 / 0.628 rad, 0 at or below
 * it; the HEALTH-FREQUENCY idiom {@code hf = getHealth() / getMaxHealth()} scaling both the rate and the
 * amplitude of the four head feathers' yaw ({@code cos(age x 1.25 x ws x hf) x PI x 0.1 x hf}, alternating) and of
 * the four tail feathers' roll ({@code cos(age x 1.4 x ws x hf) x PI x 0.25 x hf}, alternating,
 * stilled by the SITTING check, orig :298); the two arm feathers and the six fingers about X on a bare 0.3 cosine at
 * 0.05 x PI about their rests, the fingers about Y on 1.3 x ws at 0.1 x PI, alternating. The entity is read through
 * {@link VelocityRaptorPose} (the Slice 4b form). Fourteen feathers - the four head feathers, the six fingers and the
 * four tail feathers, every one written each frame - are zero-thickness cubes (ENT-S-161; the classic face order
 * required below; the shipped geo's count, T2e).
 *
 * <p>Scale and shadow follow {@link VelocityRaptorRenderer}: 0.75 render scale, halved for a baby, and a 0.55 x 0.75
 * shadow (ENT-S-092).</p>
 */
public final class VelocityRaptorGeoReplacement extends OreSpawnGeoReplacement<VelocityRaptor> {
    /** orig ModelVelocityRaptor.java:15,52 {@code wingspeed} = 1.25f (ClientProxyOreSpawn.java:423): the chain's third multiply. */
    static final float WINGSPEED = 1.25F;
    private static final GeoReplacementDescriptor<VelocityRaptor> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.VELOCITY_RAPTOR.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            VelocityRaptor.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/velocityraptor.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/velocityraptor.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/velocityraptor.png"),
            VelocityRaptorRenderer.SHADOW) {
        @Override
        public void applyScale(VelocityRaptor entity, PoseStack poseStack, float partialTick) {
            // orig RenderVelocityRaptor.preRenderScale (:42-52): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(VelocityRaptorRenderer.SCALE / 2.0F, VelocityRaptorRenderer.SCALE / 2.0F, VelocityRaptorRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(VelocityRaptorRenderer.SCALE, VelocityRaptorRenderer.SCALE, VelocityRaptorRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the fourteen feathers hf1-4 / lff1-3 / rff1-3 / tf1-4, 0 x 1 x 3 and hf1 0 x 1 x 2):
         * the shipped geo carries the classic within-cube face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public VelocityRaptorGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        VelocityRaptorPose entity = inputs.subject(VelocityRaptorPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // VelocityRaptorModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float hf = 0.0f;
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateX(processor, "bl1", newangle);
        rotateX(processor, "bl2", newangle + 0.488f);
        rotateX(processor, "bl3", newangle);
        rotateX(processor, "bl4", newangle + 0.628f);
        rotateX(processor, "br1", -newangle);
        rotateX(processor, "br2", -newangle + 0.488f);
        rotateX(processor, "br3", -newangle);
        rotateX(processor, "br4", -newangle + 0.628f);
        hf = entity.getHealth() / entity.getMaxHealth();
        newangle = Mth.cos(ageInTicks * 1.25f * WINGSPEED * hf) * (float) Math.PI * 0.1f * hf;
        rotateY(processor, "hf1", newangle);
        rotateY(processor, "hf2", -newangle);
        rotateY(processor, "hf3", newangle);
        rotateY(processor, "hf4", -newangle);
        newangle = Mth.cos(ageInTicks * 0.3f) * (float) Math.PI * 0.05f;
        rotateX(processor, "lf1", newangle + 0.279f);
        rotateX(processor, "lf2", newangle - 0.436f);
        rotateX(processor, "lff1", newangle - 0.279f);
        rotateX(processor, "lff2", newangle - 0.453f);
        rotateX(processor, "lff3", newangle - 1.047f);
        rotateX(processor, "rf1", -newangle + 0.279f);
        rotateX(processor, "rf2", -newangle - 0.436f);
        rotateX(processor, "rff1", -newangle - 0.279f);
        rotateX(processor, "rff2", -newangle - 0.453f);
        rotateX(processor, "rff3", -newangle - 1.047f);
        newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateY(processor, "lff1", newangle);
        rotateY(processor, "lff2", -newangle);
        rotateY(processor, "lff3", newangle);
        rotateY(processor, "rff1", -newangle);
        rotateY(processor, "rff2", newangle);
        rotateY(processor, "rff3", -newangle);
        newangle = entity.isInSittingPose() ? 0.0f : Mth.cos(ageInTicks * 1.4f * WINGSPEED * hf) * (float) Math.PI * 0.25f * hf;
        rotateZ(processor, "tf1", newangle);
        rotateZ(processor, "tf2", -newangle);
        rotateZ(processor, "tf3", newangle);
        rotateZ(processor, "tf4", -newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<VelocityRaptor, VelocityRaptorGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new VelocityRaptorGeoReplacement());
        }
    }
}
