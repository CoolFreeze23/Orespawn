package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityScorpion;
import danger.orespawn.entity.pose.ScorpionPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib Scorpion (the hooks, landed by the remainder slice, 2026-09-15 - TEST-014 cleared under the pair-contested
 * rule): {@link ScorpionModel#poseFrom} verbatim on the converted rig, ON THE HOOK (no keyframe layer, no transcription
 * - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.62f (orig
 * ModelScorpion.java:15,40 / ClientProxyOreSpawn.java:433): the GAIT-scaled legs about Y - four pairs on {@code cos(age x
 * 2.0 x ws - k x pi/2) x PI x 0.12 x limbSwingAmount}, k = 0..3, about +-0.49 / +-0.24 / -+2.9 / -+2.65 rad, no
 * threshold - and the per-entity LATCH the port's classic model keeps in the entity's {@link RenderInfo} (ENT-S-093; the
 * Rotator's RenderInfo and the PurplePower's subject-RNG precedents through {@link ScorpionPose}): on the frame the 3
 * x ws claw rhythm crosses zero upward ({@code nextangle > 0 && newangle < 0}) {@code ri1} and {@code ri2} are rolled from
 * the entity's random - {@code nextInt(20)} / {@code nextInt(25)} at rest, {@code nextInt(4)} / {@code nextInt(3)}
 * attacking (the ATTACKING branch, orig :203) - and then, every frame, {@code ri1} 1 or 3 swings the left claw, 2 or
 * 3 the right, {@code ri2} 1 strikes the tail, each at {@code cos(age x 3.0 x ws) x PI x 0.15} else
 * 0, through the three helpers transcribed below (the same names): a claw's arm yaws about 0.52 / 2.61 rad and the wrist
 * and pincer pivots FOLLOW it 4.5 x sin of that yaw and a further 3 units in z (the POSITION-write idiom, through {@link
 * #moveZ}; the arm's pivot is never written - the bind); the tail's six links pitch cumulatively (0.26 + a, +0.769 + a,
 * +0.701 + a, -5.501 - 1.5 a - 0.4) with each pivot following the link below 4 / 4 / 3 / 4 / 4 units along (-sin, cos) of
 * that link's pitch (y and z through {@link #moveYZ}; tail1's pivot and tail5's pitch are never written - the bind, read
 * through {@link #classicPosition} and {@link #classicRotX}); every value the classic reads back from a part it just
 * wrote is held in a local. Once per rendered frame, as the classic: the ENT-S-147 record of the per-frame dedup applies
 * as it does to the Rotator.
 *
 * <p>Scale and shadow follow {@link ScorpionRenderer}: 0.75 render scale and a 0.35 x 0.75 shadow (ENT-S-092).</p>
 */
public final class ScorpionGeoReplacement extends OreSpawnGeoReplacement<EntityScorpion> {
    /** orig ModelScorpion.java:15,40 {@code wingspeed} = 0.62f (ClientProxyOreSpawn.java:433): the chain's third multiply. */
    static final float WINGSPEED = 0.62F;
    private static final GeoReplacementDescriptor<EntityScorpion> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_SCORPION.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityScorpion.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/scorpion.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/scorpion.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/scorpion.png"),
            ScorpionRenderer.SHADOW) {
        @Override
        public void applyScale(EntityScorpion entity, PoseStack poseStack, float partialTick) {
            // orig RenderScorpion.preRenderCallback (:39-45): GL11.glScalef(scale, scale, scale) (ScorpionRenderer.scale)
            poseStack.scale(ScorpionRenderer.SCALE, ScorpionRenderer.SCALE, ScorpionRenderer.SCALE);
        }
    };

    public ScorpionGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ScorpionPose entity = inputs.subject(ScorpionPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ScorpionModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right (its unused
        // local upangle carries nothing and is not declared).
        float newangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "lleg1", newangle + 0.49f);
        rotateY(processor, "rleg1", -newangle + 2.65f);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 1.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "lleg2", newangle + 0.24f);
        rotateY(processor, "rleg2", -newangle + 2.9f);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "lleg3", newangle - 0.24f);
        rotateY(processor, "rleg3", -newangle - 2.9f);
        newangle = Mth.cos(ageInTicks * 2.0f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.12f * limbSwingAmount;
        rotateY(processor, "lleg4", newangle - 0.49f);
        rotateY(processor, "rleg4", -newangle - 2.65f);
        // orig ModelScorpion.java:198 - the per-entity scratch (ENT-S-093), the same object the classic model latches in.
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
        // orig ModelScorpion.java:226 setRenderInfo(r) is a self-copy of the same object - no-op (the classic omits it too)
    }

    /** ScorpionModel.doLeftClaw verbatim. */
    private static void doLeftClaw(AnimationProcessor<?> processor, float angle) {
        float larm2YRot = 0.52f + angle;
        rotateY(processor, "larm2", larm2YRot);
        float[] larm2 = classicPosition(bone(processor, "larm2"));  // never written: the bind pivot
        float larm1Z = (float) ((double) larm2[2] - Math.sin(larm2YRot) * 4.5);
        moveZ(processor, "larm1", larm1Z);
        moveZ(processor, "lclaw", larm1Z - 3.0f);
        rotateY(processor, "lclaw", 0.381f - angle);
    }

    /** ScorpionModel.doRightClaw verbatim. */
    private static void doRightClaw(AnimationProcessor<?> processor, float angle) {
        float rarm2YRot = 2.61f - angle;
        rotateY(processor, "rarm2", rarm2YRot);
        float[] rarm2 = classicPosition(bone(processor, "rarm2"));  // never written: the bind pivot
        float rarm1Z = (float) ((double) rarm2[2] - Math.sin(rarm2YRot) * 4.5);
        moveZ(processor, "rarm1", rarm1Z);
        moveZ(processor, "rclaw", rarm1Z - 3.0f);
        rotateY(processor, "rclaw", -0.381f + angle);
    }

    /** ScorpionModel.doTail verbatim, the double chain exactly as the classic casts it. */
    private static void doTail(AnimationProcessor<?> processor, float angle) {
        float tail1XRot = 0.26f + angle;
        rotateX(processor, "tail1", tail1XRot);
        float tail2XRot = tail1XRot + 0.76900005f + angle;
        rotateX(processor, "tail2", tail2XRot);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2Y = (float) ((double) tail1[1] - Math.sin(tail1XRot) * 4.0);
        float tail2Z = (float) ((double) tail1[2] + Math.cos(tail1XRot) * 4.0);
        moveYZ(processor, "tail2", tail2Y, tail2Z);
        float tail3XRot = tail2XRot + 0.701f + angle;
        rotateX(processor, "tail3", tail3XRot);
        float tail3Y = (float) ((double) tail2Y - Math.sin(tail2XRot) * 4.0);
        float tail3Z = (float) ((double) tail2Z + Math.cos(tail2XRot) * 4.0);
        moveYZ(processor, "tail3", tail3Y, tail3Z);
        float tail4XRot = tail3XRot + -5.501f - angle * 3.0f / 2.0f - 0.4f;
        rotateX(processor, "tail4", tail4XRot);
        float tail4Y = (float) ((double) tail3Y - Math.sin(tail3XRot) * 3.0);
        float tail4Z = (float) ((double) tail3Z + Math.cos(tail3XRot) * 3.0);
        moveYZ(processor, "tail4", tail4Y, tail4Z);
        float tail5Y = (float) ((double) tail4Y - Math.sin(tail4XRot) * 4.0);
        float tail5Z = (float) ((double) tail4Z + Math.cos(tail4XRot) * 4.0);
        moveYZ(processor, "tail5", tail5Y, tail5Z);
        float tail5XRot = classicRotX(bone(processor, "tail5"));  // tail5.xRot is never written: the bind (3.141593 rad)
        float tail6Y = (float) ((double) tail5Y - Math.sin(tail5XRot) * 4.0);
        float tail6Z = (float) ((double) tail5Z + Math.cos(tail5XRot) * 4.0);
        moveYZ(processor, "tail6", tail6Y, tail6Z);
    }

    /** {@code part.z = z} in classic terms, the part's x and y left as they are (the bind: the classic never writes them). */
    private static void moveZ(AnimationProcessor<?> processor, String name, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], current[1], z);
    }

    /** {@code part.y = y; part.z = z} in classic terms, the part's x left as it is (the bind: the classic never writes it). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityScorpion, ScorpionGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new ScorpionGeoReplacement());
        }
    }
}
