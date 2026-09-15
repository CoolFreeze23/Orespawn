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
 * GeckoLib Alien (the hook lanes 2026-09-14; the FK slice 2026-09-15: a real parent-child hierarchy of 26 links through
 * {@link FlatRig}, in-game behind the dev switch; the entity read through {@link AlienPose}): {@link ModelAlien#poseFrom}
 * verbatim on the flat rig, ON THE HOOK (no keyframe layer, no transcription; the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). Wingspeed 0.22f (orig ModelAlien.java:15,73 / ClientProxyOreSpawn.java:435): the
 * GAIT-scaled legs ({@code cos(age * 4.0 ws) * PI * 0.5 * limbSwingAmount}, no threshold, the four parts of each leg at
 * offsets 0 / -0.4 / -0.8 / -0.8, the right leg the negative); the ATTACKING-branch head fan - folded flat at -1.85 rad at
 * rest, attacking the fifteen fan parts spread about Z in 0.261-rad steps and ripple about X on {@code cos(age * 1.22 ws -
 * k * pi/6) * PI * 0.1}; the HEAD-LOOK idiom (the neck 0.35 and the head 0.75 of {@code toRadians(netHeadYaw)}, the head
 * and its six followers FOLLOWING the neck by 3 units, the two jaws 8 units on - POSITION writes through {@link
 * FlatRig#moveXZ}); and the per-entity LATCH (orig :512-552, the Robot2 precedent): when the 3.5-ws rhythm crosses zero (a
 * 0.2-tick look-ahead) the entity's own random rolls {@code ri1} (claws: 1 left, 2 right, 3 both), {@code ri2} (1: the
 * tail) and {@code ri3} (1: the jaw) from 15 each at rest, 4 / 2 / 1 attacking; the five-ring tail and its spikes sway on
 * the fast {@code PI * 0.5} or the slow {@code PI * 0.05} wave (10-unit follows), the jaws open on {@code |cos|} of the
 * fast {@code PI * 0.35} or the slow {@code PI * 0.02}, and the two claws swing on {@code |angle|} multiples of the fast
 * {@code PI * 0.2} or the slow {@code PI * 0.03} with 9 and 14-unit follows.
 * <p>Two registries, one rig: {@link AlienBossGeoReplacement} shares this class's geo, clip file, texture, scale, shadow
 * and hook ({@link #poseRig}) under its own descriptor (orig ClientProxyOreSpawn.java:435 drew both with the one
 * RenderAlien). Scale and shadow follow {@link AlienRenderer}: 1.1 render scale and a 0.35 x 1.1 shadow (ENT-S-092).</p>
 */
public final class AlienGeoReplacement extends OreSpawnGeoReplacement<Alien> {
    /** orig ModelAlien.java:15,73 {@code wingspeed} = 0.22f (ClientProxyOreSpawn.java:435): the chain's third multiply. */
    static final float WINGSPEED = 0.22F;
    private static final GeoReplacementDescriptor<Alien> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ALIEN.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Alien.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/alien.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/alien.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/alien.png"), AlienRenderer.SHADOW) {
        @Override
        public void applyScale(Alien entity, PoseStack poseStack, float partialTick) {
            // orig RenderAlien.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (AlienRenderer.scale)
            poseStack.scale(AlienRenderer.SCALE, AlienRenderer.SCALE, AlienRenderer.SCALE);
        }
    };

    public AlienGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** A part's classic position as the statements see it: the flat bind until this frame's statements wrote it. */
    private static float[] bind(FlatRig rig, String name) {
        return rig.position(name);
    }

    /** {@code part.z = z; part.x = x} with the part's y left at the bind (every follow in this rig writes z and x). */
    private static void moveXZ(FlatRig rig, String name, float x, float z) {
        rig.moveXZ(name, x, z);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        FlatRig rig = FlatRig.bind(processor);  // the hierarchy form: the statements write the flat rig, resolve maps it
        poseRig(rig, inputs);
        rig.resolve();
    }

    /** ModelAlien.poseFrom (the body of the classic setupAnim) verbatim on the flat rig, left to right; both registries. */
    static void poseRig(FlatRig rig, PoseInputs inputs) {
        AlienPose entity = inputs.subject(AlienPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float newangle = 0.0f;
        float nextangle = 0.0f;
        newangle = Mth.cos(ageInTicks * 4.0f * WINGSPEED) * (float) Math.PI * 0.5f * limbSwingAmount;
        doLeftLeg(rig, newangle);
        doRightLeg(rig, -newangle);
        if (entity.getAttacking() == 0) {
            rig.rotateZ("fan", 0.0f);
            rig.rotateZ("fanl1", 0.0f);
            rig.rotateZ("fanl2", 0.0f);
            rig.rotateZ("fanl3", 0.0f);
            rig.rotateZ("fanl4", 0.0f);
            rig.rotateZ("fanl5", 0.0f);
            rig.rotateZ("fanl6", 0.0f);
            rig.rotateZ("fanl7", 0.0f);
            rig.rotateZ("fanr1", 0.0f);
            rig.rotateZ("fanr2", 0.0f);
            rig.rotateZ("fanr3", 0.0f);
            rig.rotateZ("fanr4", 0.0f);
            rig.rotateZ("fanr5", 0.0f);
            rig.rotateZ("fanr6", 0.0f);
            rig.rotateZ("fanr7", 0.0f);
            rig.rotateX("fan", -1.85f);
            rig.rotateX("fanl1", -1.85f);
            rig.rotateX("fanl2", -1.85f);
            rig.rotateX("fanl3", -1.85f);
            rig.rotateX("fanl4", -1.85f);
            rig.rotateX("fanl5", -1.85f);
            rig.rotateX("fanl6", -1.85f);
            rig.rotateX("fanl7", -1.85f);
            rig.rotateX("fanr1", -1.85f);
            rig.rotateX("fanr2", -1.85f);
            rig.rotateX("fanr3", -1.85f);
            rig.rotateX("fanr4", -1.85f);
            rig.rotateX("fanr5", -1.85f);
            rig.rotateX("fanr6", -1.85f);
            rig.rotateX("fanr7", -1.85f);
        } else {
            float pi6 = 0.5235988f;
            float fanspeed = 1.22f;
            float fanamp = 0.1f;
            rig.rotateX("fan", Mth.cos(ageInTicks * fanspeed * WINGSPEED) * (float) Math.PI * fanamp);
            rig.rotateX("fanl1", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 1.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanl2", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 2.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanl3", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 3.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanl4", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 4.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanl5", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 5.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanl6", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 6.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanl7", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 7.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr1", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 1.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr2", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 2.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr3", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 3.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr4", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 4.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr5", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 5.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr6", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 6.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateX("fanr7", Mth.cos(ageInTicks * fanspeed * WINGSPEED - 7.0f * pi6) * (float) Math.PI * fanamp);
            rig.rotateZ("fan", 0.0f);
            rig.rotateZ("fanl1", 0.261f);
            rig.rotateZ("fanl2", 0.523f);
            rig.rotateZ("fanl3", 0.785f);
            rig.rotateZ("fanl4", 1.047f);
            rig.rotateZ("fanl5", 1.309f);
            rig.rotateZ("fanl6", 1.571f);
            rig.rotateZ("fanl7", 1.832f);
            rig.rotateZ("fanr1", -0.261f);
            rig.rotateZ("fanr2", -0.523f);
            rig.rotateZ("fanr3", -0.785f);
            rig.rotateZ("fanr4", -1.047f);
            rig.rotateZ("fanr5", -1.309f);
            rig.rotateZ("fanr6", -1.571f);
            rig.rotateZ("fanr7", -1.832f);
        }
        float[] neck = bind(rig, "neck");  // never written: the bind pivot
        float neckYRot = (float) Math.toRadians(netHeadYaw) * 0.35f;
        rig.rotateY("neck", neckYRot);
        float headYRot = (float) Math.toRadians(netHeadYaw) * 0.75f;
        rig.rotateY("head", headYRot);
        float headZ = neck[2] - (float) Math.cos(neckYRot) * 3.0f;
        float headX = neck[0] + (float) Math.sin(neckYRot) * 3.0f;
        moveXZ(rig, "head", headX, headZ);
        rig.rotateY("head1", headYRot);
        moveXZ(rig, "head1", headX, headZ);
        rig.rotateY("head2", headYRot);
        moveXZ(rig, "head2", headX, headZ);
        rig.rotateY("fang1", headYRot);
        moveXZ(rig, "fang1", headX, headZ);
        rig.rotateY("fang2", headYRot);
        moveXZ(rig, "fang2", headX, headZ);
        rig.rotateY("fang3", headYRot);
        moveXZ(rig, "fang3", headX, headZ);
        rig.rotateY("fang4", headYRot);
        moveXZ(rig, "fang4", headX, headZ);
        rig.rotateY("jaw1", headYRot);
        float jaw1Z = headZ - (float) Math.cos(headYRot) * 8.0f;
        float jaw1X = headX - (float) Math.sin(headYRot) * 8.0f;
        moveXZ(rig, "jaw1", jaw1X, jaw1Z);
        rig.rotateY("jaw2", headYRot);
        moveXZ(rig, "jaw2", jaw1X, jaw1Z);
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
            doTail(rig, newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.05f;
            doTail(rig, newangle);
        }
        if (r.ri3 == 1) {
            newangle = Mth.cos(ageInTicks * 3.5f * WINGSPEED) * (float) Math.PI * 0.35f;
            doJaw(rig, newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.02f;
            doJaw(rig, newangle);
        }
        newangle = Mth.cos(ageInTicks * WINGSPEED * 3.5f) * (float) Math.PI * 0.2f;
        if (r.ri1 == 1 || r.ri1 == 3) {
            doLeftClaw(rig, newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.03f;
            doLeftClaw(rig, newangle);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            doRightClaw(rig, -newangle);
        } else {
            newangle = Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.03f;
            doRightClaw(rig, -newangle);
        }
    }

    /** ModelAlien.doLeftLeg verbatim. */
    private static void doLeftLeg(FlatRig rig, float angle) {
        rig.rotateX("lFoot", angle);
        rig.rotateX("lShin", angle - 0.4f);
        rig.rotateX("lShin1", angle - 0.8f);
        rig.rotateX("lThigh", angle - 0.8f);
    }

    /** ModelAlien.doRightLeg verbatim. */
    private static void doRightLeg(FlatRig rig, float angle) {
        rig.rotateX("rFoot", angle);
        rig.rotateX("rShin", angle - 0.4f);
        rig.rotateX("rShin1", angle - 0.8f);
        rig.rotateX("rThigh", angle - 0.8f);
    }

    /** ModelAlien.doJaw verbatim: {@code jaw2.xRot = jaw1.xRot = |angle|}. */
    private static void doJaw(FlatRig rig, float angle) {
        rig.rotateX("jaw1", Math.abs(angle));
        rig.rotateX("jaw2", Math.abs(angle));
    }

    /** ModelAlien.doTail verbatim: tail1's pivot is the bind; every ring and spike writes z and x, y stays the bind. */
    private static void doTail(FlatRig rig, float angle) {
        float[] tail1 = bind(rig, "tail1");  // never written: the bind pivot
        float tail1YRot = angle * 0.25f;
        rig.rotateY("spike1", tail1YRot);
        rig.rotateY("tail1", tail1YRot);
        float tail2YRot = angle * 0.5f;
        rig.rotateY("tail2", tail2YRot);
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 10.0f;
        float tail2X = tail1[0] + (float) Math.sin(tail1YRot) * 10.0f;
        moveXZ(rig, "tail2", tail2X, tail2Z);
        rig.rotateY("spike2", tail2YRot);
        moveXZ(rig, "spike2", tail2X, tail2Z);
        float tail3YRot = angle * 0.8f;
        rig.rotateY("tail3", tail3YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 10.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 10.0f;
        moveXZ(rig, "tail3", tail3X, tail3Z);
        rig.rotateY("spike3", tail3YRot);
        moveXZ(rig, "spike3", tail3X, tail3Z);
        float tail4YRot = angle * 1.25f;
        rig.rotateY("tail4", tail4YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 10.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 10.0f;
        moveXZ(rig, "tail4", tail4X, tail4Z);
        rig.rotateY("spike4", tail4YRot + 0.52f);
        moveXZ(rig, "spike4", tail4X, tail4Z);
        rig.rotateY("spike5", tail4YRot - 0.52f);
        moveXZ(rig, "spike5", tail4X, tail4Z);
        rig.rotateY("tail5", angle * 1.5f);
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 10.0f;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 10.0f;
        moveXZ(rig, "tail5", tail5X, tail5Z);
    }

    /** ModelAlien.doLeftClaw verbatim: arml1's pivot is the bind; the followers write z and x. */
    private static void doLeftClaw(FlatRig rig, float angle) {
        float[] arml1 = bind(rig, "arml1");  // never written: the bind pivot
        float arml1YRot = -0.52f + Math.abs(angle * 2.0f);
        rig.rotateY("arml1", arml1YRot);
        float arml2Z = arml1[2] - (float) Math.sin(arml1YRot) * 9.0f;
        float arml2X = arml1[0] + (float) Math.cos(arml1YRot) * 9.0f;
        moveXZ(rig, "arml2", arml2X, arml2Z);
        float arml2YRot = 0.855f + Math.abs(angle);
        rig.rotateY("arml2", arml2YRot);
        float clawl1Z = arml2Z - (float) Math.sin(arml2YRot) * 14.0f;
        float clawl1X = arml2X + (float) Math.cos(arml2YRot) * 14.0f;
        moveXZ(rig, "clawl1", clawl1X, clawl1Z);
        rig.rotateY("clawl1", 2.7f + Math.abs(angle * 4.0f));
        moveXZ(rig, "clawl2", clawl1X, clawl1Z);
        rig.rotateY("clawl2", 2.27f + Math.abs(angle * 4.0f));
        moveXZ(rig, "clawl3", clawl1X, clawl1Z);
        rig.rotateY("clawl3", 2.7f + Math.abs(angle * 4.0f));
    }

    /** ModelAlien.doRightClaw verbatim. */
    private static void doRightClaw(FlatRig rig, float angle) {
        float[] armr1 = bind(rig, "armr1");  // never written: the bind pivot
        float armr1YRot = -2.61f - Math.abs(angle * 2.0f);
        rig.rotateY("armr1", armr1YRot);
        float armr2Z = armr1[2] - (float) Math.sin(armr1YRot) * 9.0f;
        float armr2X = armr1[0] + (float) Math.cos(armr1YRot) * 9.0f;
        moveXZ(rig, "armr2", armr2X, armr2Z);
        float armr2YRot = 2.27f - Math.abs(angle);
        rig.rotateY("armr2", armr2YRot);
        float clawr1Z = armr2Z - (float) Math.sin(armr2YRot) * 14.0f;
        float clawr1X = armr2X + (float) Math.cos(armr2YRot) * 14.0f;
        moveXZ(rig, "clawr1", clawr1X, clawr1Z);
        rig.rotateY("clawr1", 0.436f - Math.abs(angle * 4.0f));
        moveXZ(rig, "clawr2", clawr1X, clawr1Z);
        rig.rotateY("clawr2", 0.87f - Math.abs(angle * 4.0f));
        moveXZ(rig, "clawr3", clawr1X, clawr1Z);
        rig.rotateY("clawr3", 0.436f - Math.abs(angle * 4.0f));
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Alien, AlienGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new AlienGeoReplacement());
        }
    }
}
