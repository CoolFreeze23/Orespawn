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
 * GeckoLib Emperor Scorpion (the hook survey 2026-09-14; the FK slice 2026-09-15: a real parent-child hierarchy of 52
 * links through {@link FlatRig}, in-game behind the dev switch; the entity read through {@link EmperorScorpionPose}):
 * {@link EmperorScorpionModel#poseFrom} verbatim on the flat rig, ON THE HOOK (no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed
 * 0.22f (orig ModelEmperorScorpion.java:15,96 / ClientProxyOreSpawn.java:426): the GAIT-scaled eight-leg walk - four
 * phases a quarter turn apart ({@code cos(age * 2.0 ws - k * 1.570795) * PI * 0.12 * limbSwingAmount}, the left leg
 * positive, the mirrored right leg the negative) with a LOOK-AHEAD lift ({@code nextangle} at age + 0.1
 * rising selects {@code 0.47 * limbSwingAmount - |newangle|}) folded down each five-segment leg as yaw, a zRot pair and a
 * POSITION follow through {@link FlatRig#moveTo} (6 / 9 / 1 units along, 11.5 x sin of the zRots up); the ATTACKING-branch
 * mandibles ({@code 0.5 ws x PI x 0.05} at rest, {@code 2.5 ws x PI x 0.15} attacking, about Z, mirrored); and the
 * per-entity LATCH (orig :613-641, the Robot2 precedent): when the 3.0-ws rhythm crosses zero the entity's own random rolls
 * {@code ri1} (claws: 1 left, 2 right, 3 both) and {@code ri2} (1: the tail) from 20 / 25 at rest, 4 / 3 attacking, and
 * the claws (a yaw and a 12-unit cosine follow of four parts) and the eleven-part tail chain (pitch offsets accumulating,
 * each ring following the last by 9 / 10 / 3 units of sin / cos) pose on {@code newangle} or 0.
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

    /** A part's classic position as the statements see it: the flat bind until this frame's statements wrote it. */
    private static float[] bind(FlatRig rig, String name) {
        return rig.position(name);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        FlatRig rig = FlatRig.bind(processor);  // the hierarchy form: the statements write the flat rig, resolve maps it
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
        doLeftLeg(rig, "Leg1Seg1", "Leg1Seg2", "Leg1Seg3", "Leg1Seg4", "Leg1Seg5", newangle, upangle);
        doRightLeg(rig, "Leg5Seg1", "Leg5Seg2", "Leg5Seg3", "Leg5Seg4", "Leg5Seg5", -newangle, upangle);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 1.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED - 1.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(rig, "Leg2Seg1", "Leg2Seg2", "Leg2Seg3", "Leg2Seg4", "Leg2Seg5", newangle, upangle);
        doRightLeg(rig, "Leg6Seg1", "Leg6Seg2", "Leg6Seg3", "Leg6Seg4", "Leg6Seg5", -newangle, upangle);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(rig, "Leg3Seg1", "Leg3Seg2", "Leg3Seg3", "Leg3Seg4", "Leg3Seg5", newangle, upangle);
        doRightLeg(rig, "Leg7Seg1", "Leg7Seg2", "Leg7Seg3", "Leg7Seg4", "Leg7Seg5", -newangle, upangle);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.cos((ageInTicks + 0.1f) * 2.0f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = 0.47f * limbSwingAmount - Math.abs(newangle);
        }
        doLeftLeg(rig, "Leg4Seg1", "Leg4Seg2", "Leg4Seg3", "Leg4Seg4", "Leg4Seg5", newangle, upangle);
        doRightLeg(rig, "Leg8Seg1", "Leg8Seg2", "Leg8Seg3", "Leg8Seg4", "Leg8Seg5", -newangle, upangle);
        newangle = entity.getAttacking() == 0
                ? Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.05f
                : Mth.cos(ageInTicks * 2.5f * WINGSPEED) * (float) Math.PI * 0.15f;
        rig.rotateZ("LeftManPart2", newangle);
        rig.rotateZ("RightManPart2", -newangle);
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
            doLeftClaw(rig, newangle);
        } else {
            doLeftClaw(rig, 0.0f);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            doRightClaw(rig, newangle);
        } else {
            doRightClaw(rig, 0.0f);
        }
        if (r.ri2 == 1) {
            doTail(rig, newangle);
        } else {
            doTail(rig, 0.0f);
        }
        rig.resolve();
    }

    /** EmperorScorpionModel.doLeftLeg verbatim: seg1 is passed and unused as in the classic; seg2's pivot is the bind. */
    private static void doLeftLeg(FlatRig rig, String seg1, String seg2, String seg3, String seg4, String seg5, float angle, float upangle) {
        rig.rotateY(seg2, angle);
        rig.rotateY(seg3, angle);
        rig.rotateY(seg4, angle);
        rig.rotateY(seg5, angle);
        float[] seg2Pos = bind(rig, seg2);  // never written: the bind pivot
        float seg3Z = (float) ((double) seg2Pos[2] - Math.sin(angle) * 6.0);
        float seg3X = (float) ((double) seg2Pos[0] - Math.abs(Math.sin(angle) * 6.0) + 6.0);
        float seg4Z = (float) ((double) seg3Z - Math.sin(angle) * 9.0);
        float seg4X = (float) ((double) seg3X - Math.abs(Math.sin(angle) * 9.0) + 9.0);
        float seg5Z = (float) ((double) seg4Z - Math.sin(angle) * 1.0);
        float seg5X = (float) ((double) seg4X - Math.abs(Math.sin(angle) * 1.0) + 1.0);
        float seg2ZRot = -upangle - 0.929f;
        float seg3ZRot = -upangle + 0.632f;
        rig.rotateZ(seg2, seg2ZRot);
        rig.rotateZ(seg3, seg3ZRot);
        float seg3Y = seg2Pos[1] + (float) (11.5 * Math.sin(seg2ZRot));
        float seg4Y = seg3Y + (float) (11.5 * Math.sin(seg3ZRot));
        float seg5Y = seg4Y + 6.5f;
        rig.moveTo(seg3, seg3X, seg3Y, seg3Z);
        rig.moveTo(seg4, seg4X, seg4Y, seg4Z);
        rig.moveTo(seg5, seg5X, seg5Y, seg5Z);
    }

    /** EmperorScorpionModel.doRightLeg verbatim (seg5's yaw the negative; the follows mirrored). */
    private static void doRightLeg(FlatRig rig, String seg1, String seg2, String seg3, String seg4, String seg5, float angle, float upangle) {
        rig.rotateY(seg2, angle);
        rig.rotateY(seg3, angle);
        rig.rotateY(seg4, angle);
        rig.rotateY(seg5, -angle);
        float[] seg2Pos = bind(rig, seg2);  // never written: the bind pivot
        float seg3Z = (float) ((double) seg2Pos[2] + Math.sin(angle) * 6.0);
        float seg3X = (float) ((double) seg2Pos[0] + Math.abs(Math.sin(angle) * 6.0) - 6.0);
        float seg4Z = (float) ((double) seg3Z + Math.sin(angle) * 9.0);
        float seg4X = (float) ((double) seg3X + Math.abs(Math.sin(angle) * 9.0) - 9.0);
        float seg5Z = (float) ((double) seg4Z + Math.sin(angle) * 1.0);
        float seg5X = (float) ((double) seg4X + Math.abs(Math.sin(angle) * 1.0) - 1.0);
        float seg2ZRot = upangle + 0.929f;
        float seg3ZRot = upangle - 0.632f;
        rig.rotateZ(seg2, seg2ZRot);
        rig.rotateZ(seg3, seg3ZRot);
        float seg3Y = seg2Pos[1] - (float) (11.5 * Math.sin(seg2ZRot));
        float seg4Y = seg3Y - (float) (11.5 * Math.sin(seg3ZRot));
        float seg5Y = seg4Y + 6.5f;
        rig.moveTo(seg3, seg3X, seg3Y, seg3Z);
        rig.moveTo(seg4, seg4X, seg4Y, seg4Z);
        rig.moveTo(seg5, seg5X, seg5Y, seg5Z);
    }

    /** EmperorScorpionModel.doLeftClaw verbatim: only z is written on the four following parts, x / y stay the bind. */
    private static void doLeftClaw(FlatRig rig, float angle) {
        float leftArmSeg1YRot = -1.57f + angle;
        rig.rotateY("LeftArmSeg1", leftArmSeg1YRot);
        float leftArmSeg2Z = (float) (-22.0 - Math.cos(leftArmSeg1YRot) * 12.0);
        float[] leftArmSeg2 = bind(rig, "LeftArmSeg2");
        float[] leftArmSeg3 = bind(rig, "LeftArmSeg3");
        float[] leftArmSeg4 = bind(rig, "LeftArmSeg4");
        float[] leftPincer = bind(rig, "LeftPincer");
        rig.moveTo("LeftArmSeg2", leftArmSeg2[0], leftArmSeg2[1], leftArmSeg2Z);
        rig.moveTo("LeftArmSeg3", leftArmSeg3[0], leftArmSeg3[1], leftArmSeg2Z - 11.0f);
        rig.moveTo("LeftArmSeg4", leftArmSeg4[0], leftArmSeg4[1], leftArmSeg2Z - 11.0f);
        rig.moveTo("LeftPincer", leftPincer[0], leftPincer[1], leftArmSeg2Z - 11.0f);
        rig.rotateY("LeftArmSeg3", 0.074f + angle);
        rig.rotateY("LeftPincer", 0.371f - angle);
    }

    /** EmperorScorpionModel.doRightClaw verbatim. */
    private static void doRightClaw(FlatRig rig, float angle) {
        float rightArmSeg1YRot = 1.57f - angle;
        rig.rotateY("RightArmSeg1", rightArmSeg1YRot);
        float rightArmSeg2Z = (float) (-22.0 - Math.cos(rightArmSeg1YRot) * 12.0);
        float[] rightArmSeg2 = bind(rig, "RightArmSeg2");
        float[] rightArmSeg3 = bind(rig, "RightArmSeg3");
        float[] rightArmSeg4 = bind(rig, "RightArmSeg4");
        float[] rightPincer = bind(rig, "RightPincer");
        rig.moveTo("RightArmSeg2", rightArmSeg2[0], rightArmSeg2[1], rightArmSeg2Z);
        rig.moveTo("RightArmSeg3", rightArmSeg3[0], rightArmSeg3[1], rightArmSeg2Z - 11.0f);
        rig.moveTo("RightArmSeg4", rightArmSeg4[0], rightArmSeg4[1], rightArmSeg2Z - 11.0f);
        rig.moveTo("RightPincer", rightPincer[0], rightPincer[1], rightArmSeg2Z - 11.0f);
        rig.rotateY("RightArmSeg3", -0.074f - angle);
        rig.rotateY("RightPincer", -0.371f + angle);
    }

    /** EmperorScorpionModel.doTail verbatim: Tailseg1's pivot is the bind; every ring's x stays its bind. */
    private static void doTail(FlatRig rig, float angle) {
        float[] tailseg1 = bind(rig, "Tailseg1");  // never written: the bind pivot
        float tailseg1XRot = 0.594f + angle;
        rig.rotateX("Tailseg1", tailseg1XRot);
        float tailseg2XRot = tailseg1XRot + 0.48399997f + angle;
        rig.rotateX("Tailseg2", tailseg2XRot);
        float tailseg2Y = (float) ((double) tailseg1[1] - Math.sin(tailseg1XRot) * 9.0);
        float tailseg2Z = (float) ((double) tailseg1[2] + Math.cos(tailseg1XRot) * 9.0);
        rig.moveTo("Tailseg2", bind(rig, "Tailseg2")[0], tailseg2Y, tailseg2Z);
        float tailseg3XRot = tailseg2XRot + 0.6320001f + angle;
        rig.rotateX("Tailseg3", tailseg3XRot);
        float tailseg3Y = (float) ((double) tailseg2Y - Math.sin(tailseg2XRot) * 10.0);
        float tailseg3Z = (float) ((double) tailseg2Z + Math.cos(tailseg2XRot) * 10.0);
        rig.moveTo("Tailseg3", bind(rig, "Tailseg3")[0], tailseg3Y, tailseg3Z);
        float tailseg4XRot = tailseg3XRot + 0.5569999f - angle;
        rig.rotateX("Tailseg4", tailseg4XRot);
        float tailseg4Y = (float) ((double) tailseg3Y - Math.sin(tailseg3XRot) * 10.0);
        float tailseg4Z = (float) ((double) tailseg3Z + Math.cos(tailseg3XRot) * 10.0);
        rig.moveTo("Tailseg4", bind(rig, "Tailseg4")[0], tailseg4Y, tailseg4Z);
        float tailseg5XRot = tailseg4XRot + 0.63199997f - angle;
        rig.rotateX("Tailseg5", tailseg5XRot);
        float tailseg5Y = (float) ((double) tailseg4Y - Math.sin(tailseg4XRot) * 10.0);
        float tailseg5Z = (float) ((double) tailseg4Z + Math.cos(tailseg4XRot) * 10.0);
        rig.moveTo("Tailseg5", bind(rig, "Tailseg5")[0], tailseg5Y, tailseg5Z);
        float tailseg6XRot = tailseg5XRot + -5.501f - angle * 3.0f / 2.0f - 0.4f;
        rig.rotateX("Tailseg6", tailseg6XRot);
        float tailseg6Y = (float) ((double) tailseg5Y - Math.sin(tailseg5XRot) * 10.0);
        float tailseg6Z = (float) ((double) tailseg5Z + Math.cos(tailseg5XRot) * 10.0);
        rig.moveTo("Tailseg6", bind(rig, "Tailseg6")[0], tailseg6Y, tailseg6Z);
        float tailseg7XRot = tailseg6XRot + -2.822f - angle * 2.5f - 2.2f;
        rig.rotateX("Tailseg7", tailseg7XRot);
        float tailseg7Y = (float) ((double) tailseg6Y - Math.sin(tailseg6XRot) * 10.0);
        float tailseg7Z = (float) ((double) tailseg6Z + Math.cos(tailseg6XRot) * 10.0);
        rig.moveTo("Tailseg7", bind(rig, "Tailseg7")[0], tailseg7Y, tailseg7Z);
        float tailseg8XRot = tailseg7XRot;
        rig.rotateX("Tailseg8", tailseg8XRot);
        float tailseg8Y = tailseg7Y;
        float tailseg8Z = tailseg7Z;
        rig.moveTo("Tailseg8", bind(rig, "Tailseg8")[0], tailseg8Y, tailseg8Z);
        float stinger1XRot = tailseg7XRot + 0.0f + angle * 0.66f;
        rig.rotateX("Stinger1", stinger1XRot);
        float stinger1Y = (float) ((double) tailseg7Y - Math.sin(tailseg7XRot) * 10.0);
        float stinger1Z = (float) ((double) tailseg7Z + Math.cos(tailseg7XRot) * 10.0);
        rig.moveTo("Stinger1", bind(rig, "Stinger1")[0], stinger1Y, stinger1Z);
        float stinger2XRot = stinger1XRot + -0.48f + angle;
        rig.rotateX("Stinger2", stinger2XRot);
        float stinger2Y = (float) ((double) stinger1Y - Math.sin(stinger1XRot) * 3.0);
        float stinger2Z = (float) ((double) stinger1Z + Math.cos(stinger1XRot) * 3.0);
        rig.moveTo("Stinger2", bind(rig, "Stinger2")[0], stinger2Y, stinger2Z);
        float stinger3XRot = stinger2XRot + -1.01f + angle * 1.7f;
        rig.rotateX("Stinger3", stinger3XRot);
        float stinger3Y = (float) ((double) stinger2Y - Math.sin(stinger2XRot) * 3.0);
        float stinger3Z = (float) ((double) stinger2Z + Math.cos(stinger2XRot) * 3.0);
        rig.moveTo("Stinger3", bind(rig, "Stinger3")[0], stinger3Y, stinger3Z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityEmperorScorpion, EmperorScorpionGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new EmperorScorpionGeoReplacement());
        }
    }
}
