package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Alien;
import danger.orespawn.entity.pose.AlienPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Alien (the hook lanes, 2026-09-14, addendum item 10): {@link ModelAlien#poseFrom} verbatim on the converted
 * rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk}). Wingspeed 0.22f (orig ModelAlien.java:15,73 / ClientProxyOreSpawn.java:435): the
 * GAIT-scaled legs ({@code cos(age * 4.0 ws) * PI * 0.5 * limbSwingAmount}, no threshold, the four parts of each leg at
 * offsets 0 / -0.4 / -0.8 / -0.8, the right leg the negative); the ATTACKING-branch head fan - folded flat at -1.85 rad
 * at rest, attacking the fifteen fan parts spread about Z in 0.261-rad steps and ripple about X on
 * {@code cos(age * 1.22 ws - k * pi/6) * PI * 0.1}; the HEAD-LOOK idiom (the neck 0.35 and the head 0.75 of
 * {@code toRadians(netHeadYaw)}, the head and its six followers FOLLOWING the neck by 3 units, the two jaws 8 units on -
 * POSITION writes through {@link #moveTo}); and the per-entity LATCH (orig :512-552, the Robot2 precedent): when the
 * 3.5-ws rhythm crosses zero (a look-ahead of 0.2 ticks) the entity's own random rolls {@code ri1} (claws: 1 left, 2
 * right, 3 both), {@code ri2} (1: the tail) and {@code ri3} (1: the jaw) from 15 each at rest, 4 / 2 / 1 attacking; the
 * five-ring tail with its five spikes sways on the fast {@code PI * 0.5} or the slow {@code PI * 0.05} wave (10-unit
 * follows), the jaws open on {@code |cos|} of the fast {@code PI * 0.35} or the slow {@code PI * 0.02}, and the two
 * three-part claws swing on {@code |angle|} multiples of the fast {@code PI * 0.2} or the slow {@code PI * 0.03} with 9
 * and 14-unit follows. The entity is read through {@link AlienPose}; the helpers keep the classic names.
 *
 * <p>Two registries, one rig: {@link AlienBossGeoReplacement} shares this class's geo, clip file, texture, scale,
 * shadow and hook ({@link #poseRig}) under its own descriptor (orig ClientProxyOreSpawn.java:435 drew both with the one
 * RenderAlien). Scale and shadow follow {@link AlienRenderer}: 1.1 render scale and a 0.35 x 1.1 shadow (ENT-S-092).</p>
 */
public final class AlienGeoReplacement extends OreSpawnGeoReplacement<Alien> {
    /** orig ModelAlien.java:15,73 {@code wingspeed} = 0.22f (ClientProxyOreSpawn.java:435): the chain's third multiply. */
    static final float WINGSPEED = 0.22F;
    static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/alien.geo.json");
    static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/alien.animation.json");
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/alien.png");
    private static final GeoReplacementDescriptor<Alien> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ALIEN.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Alien.class, MODEL, ANIMATION, TEXTURE, AlienRenderer.SHADOW) {
        @Override
        public void applyScale(Alien entity, PoseStack poseStack, float partialTick) {
            // orig RenderAlien.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (AlienRenderer.scale)
            poseStack.scale(AlienRenderer.SCALE, AlienRenderer.SCALE, AlienRenderer.SCALE);
        }
    };

    public AlienGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** A part's classic position the hook never writes (or whose written components are not read back): the bind pivot. */
    private static float[] bind(AnimationProcessor<?> processor, String name) {
        return classicPosition(bone(processor, name));
    }

    /** {@code part.z = z; part.x = x} with the part's y left at the bind (every follow in this rig writes z and x). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] pos = bind(processor, name);
        moveTo(processor, name, x, pos[1], z);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        poseRig(processor, inputs);
    }

    /** ModelAlien.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right; shared by the two registries. */
    static void poseRig(AnimationProcessor<?> processor, PoseInputs inputs) {
        AlienPose entity = inputs.subject(AlienPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float newangle = 0.0f;
        float nextangle = 0.0f;
        newangle = Mth.cos(ageInTicks * 4.0f * WINGSPEED) * (float) Math.PI * 0.5f * limbSwingAmount;
        doLeftLeg(processor, newangle);
        doRightLeg(processor, -newangle);
        if (entity.getAttacking() == 0) {
            rotateZ(processor, "fan", 0.0f);
            rotateZ(processor, "fanl1", 0.0f);
            rotateZ(processor, "fanl2", 0.0f);
            rotateZ(processor, "fanl3", 0.0f);
            rotateZ(processor, "fanl4", 0.0f);
            rotateZ(processor, "fanl5", 0.0f);
            rotateZ(processor, "fanl6", 0.0f);
            rotateZ(processor, "fanl7", 0.0f);
            rotateZ(processor, "fanr1", 0.0f);
            rotateZ(processor, "fanr2", 0.0f);
            rotateZ(processor, "fanr3", 0.0f);
            rotateZ(processor, "fanr4", 0.0f);
            rotateZ(processor, "fanr5", 0.0f);
            rotateZ(processor, "fanr6", 0.0f);
            rotateZ(processor, "fanr7", 0.0f);
            rotateX(processor, "fan", -1.85f);
            rotateX(processor, "fanl1", -1.85f);
            rotateX(processor, "fanl2", -1.85f);
            rotateX(processor, "fanl3", -1.85f);
            rotateX(processor, "fanl4", -1.85f);
            rotateX(processor, "fanl5", -1.85f);
            rotateX(processor, "fanl6", -1.85f);
            rotateX(processor, "fanl7", -1.85f);
            rotateX(processor, "fanr1", -1.85f);
            rotateX(processor, "fanr2", -1.85f);
            rotateX(processor, "fanr3", -1.85f);
            rotateX(processor, "fanr4", -1.85f);
            rotateX(processor, "fanr5", -1.85f);
            rotateX(processor, "fanr6", -1.85f);
            rotateX(processor, "fanr7", -1.85f);
        } else {
            float pi6 = 0.5235988f;
            float fanspeed = 1.22f;
            float fanamp = 0.1f;
            rotateX(processor, "fan", Mth.cos(ageInTicks * fanspeed * WINGSPEED) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl1", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 1.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl2", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 2.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl3", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 3.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl4", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 4.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl5", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 5.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl6", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 6.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanl7", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 7.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr1", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 1.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr2", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 2.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr3", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 3.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr4", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 4.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr5", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 5.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr6", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 6.0f * pi6) * (float) Math.PI * fanamp);
            rotateX(processor, "fanr7", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 7.0f * pi6) * (float) Math.PI * fanamp);
            rotateZ(processor, "fan", 0.0f);
            rotateZ(processor, "fanl1", 0.261f);
            rotateZ(processor, "fanl2", 0.523f);
            rotateZ(processor, "fanl3", 0.785f);
            rotateZ(processor, "fanl4", 1.047f);
            rotateZ(processor, "fanl5", 1.309f);
            rotateZ(processor, "fanl6", 1.571f);
            rotateZ(processor, "fanl7", 1.832f);
            rotateZ(processor, "fanr1", -0.261f);
            rotateZ(processor, "fanr2", -0.523f);
            rotateZ(processor, "fanr3", -0.785f);
            rotateZ(processor, "fanr4", -1.047f);
            rotateZ(processor, "fanr5", -1.309f);
            rotateZ(processor, "fanr6", -1.571f);
            rotateZ(processor, "fanr7", -1.832f);
        }
        float[] neck = bind(processor, "neck");  // never written: the bind pivot
        float neckYRot = (float) Math.toRadians(netHeadYaw) * 0.35f;
        rotateY(processor, "neck", neckYRot);
        float headYRot = (float) Math.toRadians(netHeadYaw) * 0.75f;
        rotateY(processor, "head", headYRot);
        float headZ = neck[2] - (float) Math.cos(neckYRot) * 3.0f;
        float headX = neck[0] + (float) Math.sin(neckYRot) * 3.0f;
        moveXZ(processor, "head", headX, headZ);
        rotateY(processor, "head1", headYRot);
        moveXZ(processor, "head1", headX, headZ);
        rotateY(processor, "head2", headYRot);
        moveXZ(processor, "head2", headX, headZ);
        rotateY(processor, "fang1", headYRot);
        moveXZ(processor, "fang1", headX, headZ);
        rotateY(processor, "fang2", headYRot);
        moveXZ(processor, "fang2", headX, headZ);
        rotateY(processor, "fang3", headYRot);
        moveXZ(processor, "fang3", headX, headZ);
        rotateY(processor, "fang4", headYRot);
        moveXZ(processor, "fang4", headX, headZ);
        rotateY(processor, "jaw1", headYRot);
        float jaw1Z = headZ - (float) Math.cos(headYRot) * 8.0f;
        float jaw1X = headX - (float) Math.sin(headYRot) * 8.0f;
        moveXZ(processor, "jaw1", jaw1X, jaw1Z);
        rotateY(processor, "jaw2", headYRot);
        moveXZ(processor, "jaw2", jaw1X, jaw1Z);
        // Per-entity scratch as in the original (orig Alien.java:42, orig ModelAlien.java:512-552): the tail / jaw /
        // claw latch, rolled on the entity's own random (ENT-S-093; the Robot2 precedent).
        RenderInfo r = entity.getRenderInfo();
        newangle = Mth.cos(ageInTicks * 3.5f * WINGSPEED) * (float) Math.PI * 0.5f;
        nextangle = Mth.cos((ageInTicks + 0.2f) * 3.5f * WINGSPEED) * (float) Math.PI * 0.5f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            if (entity.getAttacking() == 0) {
                r.ri1 = entity.getRandom().nextInt(15);
                r.ri2 = entity.getRandom().nextInt(15);
                r.ri3 = entity.getRandom().nextInt(15);
            } else {
                r.ri1 = entity.getRandom().nextInt(4);
                r.ri2 = entity.getRandom().nextInt(2);
                r.ri3 = 1;
            }
        }
        if (r.ri2 == 1) {
            doTail(processor, newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.05f;
            doTail(processor, newangle);
        }
        if (r.ri3 == 1) {
            newangle = Mth.cos(ageInTicks * 3.5f * WINGSPEED) * (float) Math.PI * 0.35f;
            doJaw(processor, newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.02f;
            doJaw(processor, newangle);
        }
        newangle = Mth.cos(ageInTicks * WINGSPEED * 3.5f) * (float) Math.PI * 0.2f;
        if (r.ri1 == 1 || r.ri1 == 3) {
            doLeftClaw(processor, newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.03f;
            doLeftClaw(processor, newangle);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            doRightClaw(processor, -newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.03f;
            doRightClaw(processor, -newangle);
        }
    }

    /** ModelAlien.doLeftLeg verbatim. */
    private static void doLeftLeg(AnimationProcessor<?> processor, float angle) {
        rotateX(processor, "lFoot", angle);
        rotateX(processor, "lShin", angle - 0.4f);
        rotateX(processor, "lShin1", angle - 0.8f);
        rotateX(processor, "lThigh", angle - 0.8f);
    }

    /** ModelAlien.doRightLeg verbatim. */
    private static void doRightLeg(AnimationProcessor<?> processor, float angle) {
        rotateX(processor, "rFoot", angle);
        rotateX(processor, "rShin", angle - 0.4f);
        rotateX(processor, "rShin1", angle - 0.8f);
        rotateX(processor, "rThigh", angle - 0.8f);
    }

    /** ModelAlien.doJaw verbatim: {@code jaw2.xRot = jaw1.xRot = |angle|}. */
    private static void doJaw(AnimationProcessor<?> processor, float angle) {
        rotateX(processor, "jaw1", Math.abs(angle));
        rotateX(processor, "jaw2", Math.abs(angle));
    }

    /** ModelAlien.doTail verbatim: tail1's pivot is the bind; every ring and spike writes z and x, y stays the bind. */
    private static void doTail(AnimationProcessor<?> processor, float angle) {
        float[] tail1 = bind(processor, "tail1");  // never written: the bind pivot
        float tail1YRot = angle * 0.25f;
        rotateY(processor, "spike1", tail1YRot);
        rotateY(processor, "tail1", tail1YRot);
        float tail2YRot = angle * 0.5f;
        rotateY(processor, "tail2", tail2YRot);
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 10.0f;
        float tail2X = tail1[0] + (float) Math.sin(tail1YRot) * 10.0f;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        rotateY(processor, "spike2", tail2YRot);
        moveXZ(processor, "spike2", tail2X, tail2Z);
        float tail3YRot = angle * 0.8f;
        rotateY(processor, "tail3", tail3YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 10.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 10.0f;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        rotateY(processor, "spike3", tail3YRot);
        moveXZ(processor, "spike3", tail3X, tail3Z);
        float tail4YRot = angle * 1.25f;
        rotateY(processor, "tail4", tail4YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 10.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 10.0f;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        rotateY(processor, "spike4", tail4YRot + 0.52f);
        moveXZ(processor, "spike4", tail4X, tail4Z);
        rotateY(processor, "spike5", tail4YRot - 0.52f);
        moveXZ(processor, "spike5", tail4X, tail4Z);
        rotateY(processor, "tail5", angle * 1.5f);
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 10.0f;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 10.0f;
        moveXZ(processor, "tail5", tail5X, tail5Z);
    }

    /** ModelAlien.doLeftClaw verbatim: arml1's pivot is the bind; the followers write z and x. */
    private static void doLeftClaw(AnimationProcessor<?> processor, float angle) {
        float[] arml1 = bind(processor, "arml1");  // never written: the bind pivot
        float arml1YRot = -0.52f + Math.abs(angle * 2.0f);
        rotateY(processor, "arml1", arml1YRot);
        float arml2Z = arml1[2] - (float) Math.sin(arml1YRot) * 9.0f;
        float arml2X = arml1[0] + (float) Math.cos(arml1YRot) * 9.0f;
        moveXZ(processor, "arml2", arml2X, arml2Z);
        float arml2YRot = 0.855f + Math.abs(angle);
        rotateY(processor, "arml2", arml2YRot);
        float clawl1Z = arml2Z - (float) Math.sin(arml2YRot) * 14.0f;
        float clawl1X = arml2X + (float) Math.cos(arml2YRot) * 14.0f;
        moveXZ(processor, "clawl1", clawl1X, clawl1Z);
        rotateY(processor, "clawl1", 2.7f + Math.abs(angle * 4.0f));
        moveXZ(processor, "clawl2", clawl1X, clawl1Z);
        rotateY(processor, "clawl2", 2.27f + Math.abs(angle * 4.0f));
        moveXZ(processor, "clawl3", clawl1X, clawl1Z);
        rotateY(processor, "clawl3", 2.7f + Math.abs(angle * 4.0f));
    }

    /** ModelAlien.doRightClaw verbatim. */
    private static void doRightClaw(AnimationProcessor<?> processor, float angle) {
        float[] armr1 = bind(processor, "armr1");  // never written: the bind pivot
        float armr1YRot = -2.61f - Math.abs(angle * 2.0f);
        rotateY(processor, "armr1", armr1YRot);
        float armr2Z = armr1[2] - (float) Math.sin(armr1YRot) * 9.0f;
        float armr2X = armr1[0] + (float) Math.cos(armr1YRot) * 9.0f;
        moveXZ(processor, "armr2", armr2X, armr2Z);
        float armr2YRot = 2.27f - Math.abs(angle);
        rotateY(processor, "armr2", armr2YRot);
        float clawr1Z = armr2Z - (float) Math.sin(armr2YRot) * 14.0f;
        float clawr1X = armr2X + (float) Math.cos(armr2YRot) * 14.0f;
        moveXZ(processor, "clawr1", clawr1X, clawr1Z);
        rotateY(processor, "clawr1", 0.436f - Math.abs(angle * 4.0f));
        moveXZ(processor, "clawr2", clawr1X, clawr1Z);
        rotateY(processor, "clawr2", 0.87f - Math.abs(angle * 4.0f));
        moveXZ(processor, "clawr3", clawr1X, clawr1Z);
        rotateY(processor, "clawr3", 0.436f - Math.abs(angle * 4.0f));
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Alien, AlienGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new AlienGeoReplacement());
        }
    }
}
