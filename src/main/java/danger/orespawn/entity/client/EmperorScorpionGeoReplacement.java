package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityEmperorScorpion;
import danger.orespawn.entity.pose.EmperorScorpionPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Emperor Scorpion (the hook lanes, 2026-09-14, addendum item 10): {@link EmperorScorpionModel#poseFrom}
 * verbatim on the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until
 * an artist delivers {@code idle} and {@code walk}). Wingspeed 0.22f (orig ModelEmperorScorpion.java:15,96 /
 * ClientProxyOreSpawn.java:426): the GAIT-scaled eight-leg walk - four phases a quarter turn apart
 * ({@code cos(age * 2.0 ws - k * 1.570795) * PI * 0.12 * limbSwingAmount}, the left leg positive, the mirrored right
 * leg the negative) with a LOOK-AHEAD lift ({@code nextangle} at age + 0.1 rising selects
 * {@code 0.47 * limbSwingAmount - |newangle|}) folded down each five-segment leg as yaw, a zRot pair and a POSITION
 * follow through {@link #moveTo} (6 / 9 / 1 units along, 11.5 x sin of the zRots up); the ATTACKING-branch mandibles
 * ({@code 0.5 ws x PI x 0.05} at rest, {@code 2.5 ws x PI x 0.15} attacking, about Z, mirrored); and the per-entity
 * LATCH (orig :613-641, the Robot2 precedent): when the 3.0-ws rhythm crosses zero the entity's own random rolls
 * {@code ri1} (claws: 1 left, 2 right, 3 both) and {@code ri2} (1: the tail) from 20 / 25 at rest, 4 / 3 attacking, and
 * the claws (a yaw and a 12-unit cosine follow of four parts) and the eleven-part tail chain (pitch offsets
 * accumulating, each ring following the last by 9 / 10 / 3 units of sin / cos) pose on {@code newangle} or 0. The
 * entity is read through {@link EmperorScorpionPose}; the helpers keep the classic names.
 *
 * <p>Scale and shadow follow {@link EmperorScorpionRenderer}: 1.5 render scale and a 0.95 x 1.5 shadow (ENT-S-092).</p>
 */
public final class EmperorScorpionGeoReplacement extends OreSpawnGeoReplacement<EntityEmperorScorpion> {
    /** orig ModelEmperorScorpion.java:15,96 {@code wingspeed} = 0.22f (ClientProxyOreSpawn.java:426): the chain's third multiply. */
    static final float WINGSPEED = 0.22F;
    private static final GeoReplacementDescriptor<EntityEmperorScorpion> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_EMPEROR_SCORPION.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityEmperorScorpion.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/emperorscorpion.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/emperorscorpion.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/emperorscorpion.png"),
            EmperorScorpionRenderer.SHADOW) {
        @Override
        public void applyScale(EntityEmperorScorpion entity, PoseStack poseStack, float partialTick) {
            // orig RenderEmperorScorpion.preRenderCallback: GL11.glScalef(scale, scale, scale) (EmperorScorpionRenderer.scale)
            poseStack.scale(EmperorScorpionRenderer.SCALE, EmperorScorpionRenderer.SCALE, EmperorScorpionRenderer.SCALE);
        }
    };

    public EmperorScorpionGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** A part's classic position the hook never writes (or whose written component is not read): the bind pivot. */
    private static float[] bind(AnimationProcessor<?> processor, String name) {
        return classicPosition(bone(processor, name));
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        EmperorScorpionPose entity = inputs.subject(EmperorScorpionPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // EmperorScorpionModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(processor, "Leg1Seg1", "Leg1Seg2", "Leg1Seg3", "Leg1Seg4", "Leg1Seg5", newangle, upangle);
        doRightLeg(processor, "Leg5Seg1", "Leg5Seg2", "Leg5Seg3", "Leg5Seg4", "Leg5Seg5", -newangle, upangle);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 1.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED - 1.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(processor, "Leg2Seg1", "Leg2Seg2", "Leg2Seg3", "Leg2Seg4", "Leg2Seg5", newangle, upangle);
        doRightLeg(processor, "Leg6Seg1", "Leg6Seg2", "Leg6Seg3", "Leg6Seg4", "Leg6Seg5", -newangle, upangle);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(processor, "Leg3Seg1", "Leg3Seg2", "Leg3Seg3", "Leg3Seg4", "Leg3Seg5", newangle, upangle);
        doRightLeg(processor, "Leg7Seg1", "Leg7Seg2", "Leg7Seg3", "Leg7Seg4", "Leg7Seg5", -newangle, upangle);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(processor, "Leg4Seg1", "Leg4Seg2", "Leg4Seg3", "Leg4Seg4", "Leg4Seg5", newangle, upangle);
        doRightLeg(processor, "Leg8Seg1", "Leg8Seg2", "Leg8Seg3", "Leg8Seg4", "Leg8Seg5", -newangle, upangle);
        newangle = entity.getAttacking() == 0
                ? Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.05f
                : Mth.cos(ageInTicks * 2.5f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateZ(processor, "LeftManPart2", newangle);
        rotateZ(processor, "RightManPart2", -newangle);
        // Per-entity scratch as in the original (orig EmperorScorpion.java:53, orig ModelEmperorScorpion.java:613-641):
        // the claw / tail selector latch, rolled on the entity's own random (ENT-S-093; the Robot2 precedent).
        RenderInfo r = entity.getRenderInfo();
        newangle = Mth.cos(ageInTicks * 3.0f * WINGSPEED) * (float) Math.PI * 0.15f;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 3.0f * WINGSPEED) * (float) Math.PI * 0.15f;
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
            doLeftClaw(processor, newangle);
        } else {
            doLeftClaw(processor, 0.0f);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            doRightClaw(processor, newangle);
        } else {
            doRightClaw(processor, 0.0f);
        }
        if (r.ri2 == 1) {
            doTail(processor, newangle);
        } else {
            doTail(processor, 0.0f);
        }
    }

    /** EmperorScorpionModel.doLeftLeg verbatim: seg1 is passed and unused as in the classic; seg2's pivot is the bind. */
    private static void doLeftLeg(AnimationProcessor<?> processor, String seg1, String seg2, String seg3, String seg4, String seg5, float angle, float upangle) {
        rotateY(processor, seg2, angle);
        rotateY(processor, seg3, angle);
        rotateY(processor, seg4, angle);
        rotateY(processor, seg5, angle);
        float[] seg2Pos = bind(processor, seg2);  // never written: the bind pivot
        float seg3Z = (float) ((double) seg2Pos[2] - Math.sin(angle) * 6.0);
        float seg3X = (float) ((double) seg2Pos[0] - Math.abs(Math.sin(angle) * 6.0) + 6.0);
        float seg4Z = (float) ((double) seg3Z - Math.sin(angle) * 9.0);
        float seg4X = (float) ((double) seg3X - Math.abs(Math.sin(angle) * 9.0) + 9.0);
        float seg5Z = (float) ((double) seg4Z - Math.sin(angle) * 1.0);
        float seg5X = (float) ((double) seg4X - Math.abs(Math.sin(angle) * 1.0) + 1.0);
        float seg2ZRot = -upangle - 0.929f;
        float seg3ZRot = -upangle + 0.632f;
        rotateZ(processor, seg2, seg2ZRot);
        rotateZ(processor, seg3, seg3ZRot);
        float seg3Y = seg2Pos[1] + (float) (11.5 * Math.sin(seg2ZRot));
        float seg4Y = seg3Y + (float) (11.5 * Math.sin(seg3ZRot));
        float seg5Y = seg4Y + 6.5f;
        moveTo(processor, seg3, seg3X, seg3Y, seg3Z);
        moveTo(processor, seg4, seg4X, seg4Y, seg4Z);
        moveTo(processor, seg5, seg5X, seg5Y, seg5Z);
    }

    /** EmperorScorpionModel.doRightLeg verbatim (seg5's yaw the negative; the follows mirrored). */
    private static void doRightLeg(AnimationProcessor<?> processor, String seg1, String seg2, String seg3, String seg4, String seg5, float angle, float upangle) {
        rotateY(processor, seg2, angle);
        rotateY(processor, seg3, angle);
        rotateY(processor, seg4, angle);
        rotateY(processor, seg5, -angle);
        float[] seg2Pos = bind(processor, seg2);  // never written: the bind pivot
        float seg3Z = (float) ((double) seg2Pos[2] + Math.sin(angle) * 6.0);
        float seg3X = (float) ((double) seg2Pos[0] + Math.abs(Math.sin(angle) * 6.0) - 6.0);
        float seg4Z = (float) ((double) seg3Z + Math.sin(angle) * 9.0);
        float seg4X = (float) ((double) seg3X + Math.abs(Math.sin(angle) * 9.0) - 9.0);
        float seg5Z = (float) ((double) seg4Z + Math.sin(angle) * 1.0);
        float seg5X = (float) ((double) seg4X + Math.abs(Math.sin(angle) * 1.0) - 1.0);
        float seg2ZRot = upangle + 0.929f;
        float seg3ZRot = upangle - 0.632f;
        rotateZ(processor, seg2, seg2ZRot);
        rotateZ(processor, seg3, seg3ZRot);
        float seg3Y = seg2Pos[1] - (float) (11.5 * Math.sin(seg2ZRot));
        float seg4Y = seg3Y - (float) (11.5 * Math.sin(seg3ZRot));
        float seg5Y = seg4Y + 6.5f;
        moveTo(processor, seg3, seg3X, seg3Y, seg3Z);
        moveTo(processor, seg4, seg4X, seg4Y, seg4Z);
        moveTo(processor, seg5, seg5X, seg5Y, seg5Z);
    }

    /** EmperorScorpionModel.doLeftClaw verbatim: only z is written on the four following parts, x / y stay the bind. */
    private static void doLeftClaw(AnimationProcessor<?> processor, float angle) {
        float leftArmSeg1YRot = -1.57f + angle;
        rotateY(processor, "LeftArmSeg1", leftArmSeg1YRot);
        float leftArmSeg2Z = (float) (-22.0 - Math.cos(leftArmSeg1YRot) * 12.0);
        float[] leftArmSeg2 = bind(processor, "LeftArmSeg2");
        float[] leftArmSeg3 = bind(processor, "LeftArmSeg3");
        float[] leftArmSeg4 = bind(processor, "LeftArmSeg4");
        float[] leftPincer = bind(processor, "LeftPincer");
        moveTo(processor, "LeftArmSeg2", leftArmSeg2[0], leftArmSeg2[1], leftArmSeg2Z);
        moveTo(processor, "LeftArmSeg3", leftArmSeg3[0], leftArmSeg3[1], leftArmSeg2Z - 11.0f);
        moveTo(processor, "LeftArmSeg4", leftArmSeg4[0], leftArmSeg4[1], leftArmSeg2Z - 11.0f);
        moveTo(processor, "LeftPincer", leftPincer[0], leftPincer[1], leftArmSeg2Z - 11.0f);
        rotateY(processor, "LeftArmSeg3", 0.074f + angle);
        rotateY(processor, "LeftPincer", 0.371f - angle);
    }

    /** EmperorScorpionModel.doRightClaw verbatim. */
    private static void doRightClaw(AnimationProcessor<?> processor, float angle) {
        float rightArmSeg1YRot = 1.57f - angle;
        rotateY(processor, "RightArmSeg1", rightArmSeg1YRot);
        float rightArmSeg2Z = (float) (-22.0 - Math.cos(rightArmSeg1YRot) * 12.0);
        float[] rightArmSeg2 = bind(processor, "RightArmSeg2");
        float[] rightArmSeg3 = bind(processor, "RightArmSeg3");
        float[] rightArmSeg4 = bind(processor, "RightArmSeg4");
        float[] rightPincer = bind(processor, "RightPincer");
        moveTo(processor, "RightArmSeg2", rightArmSeg2[0], rightArmSeg2[1], rightArmSeg2Z);
        moveTo(processor, "RightArmSeg3", rightArmSeg3[0], rightArmSeg3[1], rightArmSeg2Z - 11.0f);
        moveTo(processor, "RightArmSeg4", rightArmSeg4[0], rightArmSeg4[1], rightArmSeg2Z - 11.0f);
        moveTo(processor, "RightPincer", rightPincer[0], rightPincer[1], rightArmSeg2Z - 11.0f);
        rotateY(processor, "RightArmSeg3", -0.074f - angle);
        rotateY(processor, "RightPincer", -0.371f + angle);
    }

    /** EmperorScorpionModel.doTail verbatim: Tailseg1's pivot is the bind; every ring's x stays its bind. */
    private static void doTail(AnimationProcessor<?> processor, float angle) {
        float[] tailseg1 = bind(processor, "Tailseg1");  // never written: the bind pivot
        float tailseg1XRot = 0.594f + angle;
        rotateX(processor, "Tailseg1", tailseg1XRot);
        float tailseg2XRot = tailseg1XRot + 0.48399997f + angle;
        rotateX(processor, "Tailseg2", tailseg2XRot);
        float tailseg2Y = (float) ((double) tailseg1[1] - Math.sin(tailseg1XRot) * 9.0);
        float tailseg2Z = (float) ((double) tailseg1[2] + Math.cos(tailseg1XRot) * 9.0);
        moveTo(processor, "Tailseg2", bind(processor, "Tailseg2")[0], tailseg2Y, tailseg2Z);
        float tailseg3XRot = tailseg2XRot + 0.6320001f + angle;
        rotateX(processor, "Tailseg3", tailseg3XRot);
        float tailseg3Y = (float) ((double) tailseg2Y - Math.sin(tailseg2XRot) * 10.0);
        float tailseg3Z = (float) ((double) tailseg2Z + Math.cos(tailseg2XRot) * 10.0);
        moveTo(processor, "Tailseg3", bind(processor, "Tailseg3")[0], tailseg3Y, tailseg3Z);
        float tailseg4XRot = tailseg3XRot + 0.5569999f - angle;
        rotateX(processor, "Tailseg4", tailseg4XRot);
        float tailseg4Y = (float) ((double) tailseg3Y - Math.sin(tailseg3XRot) * 10.0);
        float tailseg4Z = (float) ((double) tailseg3Z + Math.cos(tailseg3XRot) * 10.0);
        moveTo(processor, "Tailseg4", bind(processor, "Tailseg4")[0], tailseg4Y, tailseg4Z);
        float tailseg5XRot = tailseg4XRot + 0.63199997f - angle;
        rotateX(processor, "Tailseg5", tailseg5XRot);
        float tailseg5Y = (float) ((double) tailseg4Y - Math.sin(tailseg4XRot) * 10.0);
        float tailseg5Z = (float) ((double) tailseg4Z + Math.cos(tailseg4XRot) * 10.0);
        moveTo(processor, "Tailseg5", bind(processor, "Tailseg5")[0], tailseg5Y, tailseg5Z);
        float tailseg6XRot = tailseg5XRot + -5.501f - angle * 3.0f / 2.0f - 0.4f;
        rotateX(processor, "Tailseg6", tailseg6XRot);
        float tailseg6Y = (float) ((double) tailseg5Y - Math.sin(tailseg5XRot) * 10.0);
        float tailseg6Z = (float) ((double) tailseg5Z + Math.cos(tailseg5XRot) * 10.0);
        moveTo(processor, "Tailseg6", bind(processor, "Tailseg6")[0], tailseg6Y, tailseg6Z);
        float tailseg7XRot = tailseg6XRot + -2.822f - angle * 2.5f - 2.2f;
        rotateX(processor, "Tailseg7", tailseg7XRot);
        float tailseg7Y = (float) ((double) tailseg6Y - Math.sin(tailseg6XRot) * 10.0);
        float tailseg7Z = (float) ((double) tailseg6Z + Math.cos(tailseg6XRot) * 10.0);
        moveTo(processor, "Tailseg7", bind(processor, "Tailseg7")[0], tailseg7Y, tailseg7Z);
        float tailseg8XRot = tailseg7XRot;
        rotateX(processor, "Tailseg8", tailseg8XRot);
        float tailseg8Y = tailseg7Y;
        float tailseg8Z = tailseg7Z;
        moveTo(processor, "Tailseg8", bind(processor, "Tailseg8")[0], tailseg8Y, tailseg8Z);
        float stinger1XRot = tailseg7XRot + 0.0f + angle * 0.66f;
        rotateX(processor, "Stinger1", stinger1XRot);
        float stinger1Y = (float) ((double) tailseg7Y - Math.sin(tailseg7XRot) * 10.0);
        float stinger1Z = (float) ((double) tailseg7Z + Math.cos(tailseg7XRot) * 10.0);
        moveTo(processor, "Stinger1", bind(processor, "Stinger1")[0], stinger1Y, stinger1Z);
        float stinger2XRot = stinger1XRot + -0.48f + angle;
        rotateX(processor, "Stinger2", stinger2XRot);
        float stinger2Y = (float) ((double) stinger1Y - Math.sin(stinger1XRot) * 3.0);
        float stinger2Z = (float) ((double) stinger1Z + Math.cos(stinger1XRot) * 3.0);
        moveTo(processor, "Stinger2", bind(processor, "Stinger2")[0], stinger2Y, stinger2Z);
        float stinger3XRot = stinger2XRot + -1.01f + angle * 1.7f;
        rotateX(processor, "Stinger3", stinger3XRot);
        float stinger3Y = (float) ((double) stinger2Y - Math.sin(stinger2XRot) * 3.0);
        float stinger3Z = (float) ((double) stinger2Z + Math.cos(stinger2XRot) * 3.0);
        moveTo(processor, "Stinger3", bind(processor, "Stinger3")[0], stinger3Y, stinger3Z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityEmperorScorpion, EmperorScorpionGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new EmperorScorpionGeoReplacement());
        }
    }
}
