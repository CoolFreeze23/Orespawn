package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLeon;
import danger.orespawn.entity.pose.LeonPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Leon (the hook survey, landed by the first Tier-1 slice T1a): {@link LeonModel#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk} ). Wingspeed 0.22f (orig ModelLeon.java:15,116 /
 * ClientProxyOreSpawn.java:500). The rig carries TWO part sets and draws exactly one per frame by the entity's
 * activity (orig ModelLeon.java:729/852; the port's {@code visible} toggles, TF-030) - here {@link #setVisible} on
 * the forty-nine standing and forty-nine flying parts. STANDING (activity 0): the THRESHOLD gait ({@code cos(age *
 * 1.8 ws) * PI * 0.25 * limbSwingAmount} above a walking speed of a tenth) on the two-part legs around -+0.611 with
 * the feet FOLLOWING through {@link #moveTo} (9 units of sin / cos, then 13 / 11), the halved gait on the arms and
 * inner wings around -0.07 / -0.17 / -0.471 / -0.523 with an 11-unit follow and the outer wing group 20 units on
 * (the claws 1 / 9 units behind, 2 down), the 0.9-ws wing sway ({@code PI * 0.25 * limbSwingAmount} walking,
 * {@code PI * 0.02} standing, 0 SITTING) at a half / quarter / eighth on the three outer wing parts, the 0.6-ws
 * breath on the chest (an eighth) and jaw and lower sails (a half), and the HEAD-LOOK idiom ({@code
 * toRadians(netHeadYaw) * 0.5} on the head, jaws and sails, the eye ridges and antennae around +-0.558 / 0.366 /
 * 0.139, the lower jaw following the head by 5 units). FLYING: the ATTACKING flag speeds the beat (spd 1.7) and
 * widens it (amp 1.4); the chest bobs {@code sin(cos(age * 1.6 ws spd) * PI * 0.06) * 10 * amp} unless RIDDEN, the
 * abdomen and the four rear wings follow it; the legs hang at pi/2 (attacking: -pi/4 with a 3.6-ws flutter and fixed x
 * offsets 7 / 11 / -9 / -13); the two three-part wings beat about Z on {@code cos(age * 1.6 ws spd) * PI * 0.26 * amp}
 * around -+pi/2 with 1.3 and 1.65 multiples down the chain and 14 / 20-unit follows; the three neck rings and the head
 * follow the chest (10 / 7 / 7 / 16 along, 8 / 6 / 5 / 15 up) pitching -newangle / 12, 10, 8; the head yaw is
 * {@code toRadians(netHeadYaw) * 0.5} unless RIDDEN, when it is the per-entity ACCUMULATOR
 * {@code rf1 += ((yRotO - yRot) * -8 - rf1) / 60} clamped to +-50 (orig :1013-1024; the Rotator's precedent); the jaw
 * opens on a 2.6-ws {@code PI * 0.16} while attacking, the three lower sails at fixed offsets. The entity is read
 * through {@link LeonPose} .
 *
 * <p>Two registries, one rig: {@link LeonopteryxGeoReplacement} (the canonical {@code leonopteryx} id, TF-030) shares
 * this class's geo, clip file and hook ({@link #poseRig}) under its own descriptor. Scale and shadow follow
 * {@link LeonRenderer} : 1.75 render scale ({@link LeonRenderer#SCALE}, made public by the landing slice T1a; the hook
 * survey carried an equal literal while it was private - the T2d form) and a 1.0 x 1.75 shadow
 * (orig ClientProxyOreSpawn.java:500 / RenderLeon.java:22-25; the renderer passes the shadow as a literal,
 * so both descriptors' constructors pass the equal literal - the pins tool reads a literal or the renderer's constant,
 * never a descriptor static, T1a). The four sails are zero-thickness cubes (the seam draws every cube with
 * its true transformed normal, ENT-S-161, and its two coplanar faces in the classic order - below). The classic
 * renderer's unconditional {@code shouldRender} (OPT-013) is carried by {@link Renderer#shouldRender} of both
 * registries (T1a).</p>
 */
public final class LeonGeoReplacement extends OreSpawnGeoReplacement<EntityLeon> {
    /** orig ModelLeon.java:15,116 {@code wingspeed} = 0.22f (ClientProxyOreSpawn.java:500): the chain's third multiply. */
    static final float WINGSPEED = 0.22F;
    static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/leon.animation.json");
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leon.png");
    /** orig ModelLeon.java:803-851: the standing set, drawn when {@code getActivity() == 0}. */
    static final String[] STANDING_PARTS = {
            "chest", "neck_1", "neck_2", "neck_3", "abdomen", "head", "upper_jaw", "bottom_jaw",
            "chest_ridge", "upper_sail_1", "upper_sail2_", "upper_sail3",
            "lower_sail1", "lower_sail2", "lower_sail_3", "eye_ridge_L", "eye_ridge_R",
            "anntena_1_L", "anntena_1_R", "anntena_2_L", "anntena_2_R",
            "arm_1_L", "arm_2_L", "wing_1_L", "wing_2_L", "arm_1_R", "arm_2_R", "wing_1_R", "wing_2_R",
            "leg_1_L", "leg_1_R", "leg_2_L", "leg_2_R", "footL", "footR",
            "wing_3_L", "wing_3_R", "wing_4_L", "wing_4_R",
            "claw_L", "claw_R", "claw_L2", "claw_R_2",
            "wing_5_L", "wing_6_L", "wing_7_L", "wing_5_R", "wing_6_R", "wing_7_R",
    };
    /** orig ModelLeon.java:1054-1102: the flying set, drawn otherwise. */
    static final String[] FLYING_PARTS = {
            "fchest", "fneck_1", "fneck_2", "fneck_3", "fabdomen", "fhead", "fupper_jaw", "fbottom_jaw",
            "fchest_ridge", "fupper_sail_1", "fupper_sail2_", "fupper_sail3",
            "flower_sail1", "flower_sail2", "flower_sail_3", "feye_ridge_L", "feye_ridge_R",
            "fanntena_1_L", "fanntena_1_R", "fanntena_2_L", "fanntena_2_R",
            "farm_1_L", "farm_2_L", "fwing_1_L", "fwing_2_L", "farm_1_R", "farm_2_R", "fwing_1_R", "fwing_2_R",
            "fleg_1_L", "fleg_1_R", "fleg_2_L", "fleg_2_R", "ffootL", "ffootR",
            "fwing_3_L", "fwing_3_R", "fwing_4_L", "fwing_4_R",
            "fclaw_L", "fclaw_R", "fclaw_L2", "fclaw_R_2",
            "fwing_5_L", "fwing_6_L", "fwing_7_L", "fwing_5_R", "fwing_6_R", "fwing_7_R",
    };
    private static final GeoReplacementDescriptor<EntityLeon> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_LEON.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityLeon.class, ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/leon.geo.json"),
            ANIMATION, TEXTURE, 1.75F) {
        @Override
        public void applyScale(EntityLeon entity, PoseStack poseStack, float partialTick) {
            // orig RenderLeon.java:39-41 preRenderScale: GL11.glScalef(scale, scale, scale) (LeonRenderer.scale; public since T1a)
            poseStack.scale(LeonRenderer.SCALE, LeonRenderer.SCALE, LeonRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the four sails, 0 x 9 x 13 and 0 x 9 x 7 in each set): the within-cube face
         * order decides a flat cube's z-fight, so the shipped geo carries the classic order ({@link FaceOrder#KEY};
         * TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public LeonGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** A part's classic position the hook never writes (or whose written components are not read back): the bind pivot. */
    private static float[] bind(AnimationProcessor<?> processor, String name) {
        return classicPosition(bone(processor, name));
    }

    /** {@code part.y = y} with x / z left at the bind. */
    private static void moveY(AnimationProcessor<?> processor, String name, float y) {
        float[] pos = bind(processor, name);
        moveTo(processor, name, pos[0], y, pos[2]);
    }

    /** {@code part.z = z; part.y = y} with x left at the bind. */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] pos = bind(processor, name);
        moveTo(processor, name, pos[0], y, z);
    }

    /** {@code part.x = x; part.y = y} with z left at the bind. */
    private static void moveXY(AnimationProcessor<?> processor, String name, float x, float y) {
        float[] pos = bind(processor, name);
        moveTo(processor, name, x, y, pos[2]);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        poseRig(processor, inputs);
    }

    /** LeonModel.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain exactly as the classic casts it; shared by the two registries. */
    static void poseRig(AnimationProcessor<?> processor, PoseInputs inputs) {
        LeonPose entity = inputs.subject(LeonPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        float spd = 1.0f;
        float amp = 1.0f;
        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos(ageInTicks * 1.8f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount;
            newangle2 = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount;
        } else {
            newangle = 0.0f;
            newangle2 = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.02f;
            if (entity.isInSittingPose()) {
                newangle2 = 0.0f;
            }
        }
        // Orig ModelLeon.java:729/852 - exactly one part set renders per frame, selected by activity (0 = standing,
        // else flying); the classic toggles .visible, the hook hides the other set's bones.
        boolean flying = entity.getActivity() != 0;
        for (String part : STANDING_PARTS) {
            setVisible(processor, part, !flying);
        }
        for (String part : FLYING_PARTS) {
            setVisible(processor, part, flying);
        }
        if (entity.getActivity() == 0) {
            float[] leg1L = bind(processor, "leg_1_L");  // never written: the bind pivot
            float[] leg1R = bind(processor, "leg_1_R");
            float leg1LXRot = -0.611f + newangle;
            rotateX(processor, "leg_1_L", leg1LXRot);
            float leg1RXRot = -0.611f - newangle;
            rotateX(processor, "leg_1_R", leg1RXRot);
            float leg2LXRot = 0.611f + newangle;
            rotateX(processor, "leg_2_L", leg2LXRot);
            float leg2LZ = (float) ((double) leg1L[2] + Math.sin(leg1LXRot) * 9.0);
            float leg2LY = (float) ((double) leg1L[1] + Math.cos(leg1LXRot) * 9.0);
            moveYZ(processor, "leg_2_L", leg2LY, leg2LZ);
            float leg2RXRot = 0.611f - newangle;
            rotateX(processor, "leg_2_R", leg2RXRot);
            float leg2RZ = (float) ((double) leg1R[2] + Math.sin(leg1RXRot) * 9.0);
            float leg2RY = (float) ((double) leg1R[1] + Math.cos(leg1RXRot) * 9.0);
            moveYZ(processor, "leg_2_R", leg2RY, leg2RZ);
            moveYZ(processor, "footL", (float) ((double) leg2LY + Math.cos(leg2LXRot) * 13.0), (float) ((double) leg2LZ + Math.sin(leg2LXRot) * 13.0));
            moveYZ(processor, "footR", (float) ((double) leg2RY + Math.cos(leg2RXRot) * 11.0), (float) ((double) leg2RZ + Math.sin(leg2RXRot) * 11.0));
            rotateY(processor, "wing_3_R", 0.523f - newangle / 10.0f);
            rotateY(processor, "wing_3_L", -0.523f - newangle / 10.0f);
            float arm1LXRot = -0.07f - (newangle /= 2.0f);
            rotateX(processor, "arm_1_L", arm1LXRot);
            float arm1RXRot = -0.07f + newangle;
            rotateX(processor, "arm_1_R", arm1RXRot);
            rotateX(processor, "wing_1_L", -0.17f - newangle);
            rotateX(processor, "wing_1_R", -0.17f + newangle);
            float arm2LXRot = -0.471f - newangle;
            rotateX(processor, "arm_2_L", arm2LXRot);
            rotateX(processor, "wing_2_L", -0.523f - newangle);
            float[] arm1L = bind(processor, "arm_1_L");  // never written: the bind pivot
            float arm2LZ = (float) ((double) arm1L[2] + Math.sin(arm1LXRot) * 11.0);
            float arm2LY = (float) ((double) arm1L[1] + Math.cos(arm1LXRot) * 11.0);
            moveYZ(processor, "arm_2_L", arm2LY, arm2LZ);
            moveYZ(processor, "wing_2_L", arm2LY, arm2LZ);
            rotateX(processor, "wing_5_L", 0.68f + newangle2 / 2.0f);
            rotateX(processor, "wing_6_L", 0.453f + newangle2 / 4.0f);
            rotateX(processor, "wing_7_R", 0.119f + newangle2 / 8.0f);
            float wing5LZ = (float) ((double) arm2LZ + Math.sin(arm2LXRot) * 20.0);
            float wing5LY = (float) ((double) arm2LY + Math.cos(arm2LXRot) * 20.0);
            moveYZ(processor, "wing_5_L", wing5LY, wing5LZ);
            moveYZ(processor, "wing_6_L", wing5LY, wing5LZ);
            moveYZ(processor, "wing_7_R", wing5LY, wing5LZ);
            moveYZ(processor, "claw_L", wing5LY + 2.0f, wing5LZ - 1.0f);
            moveYZ(processor, "claw_R_2", wing5LY + 2.0f, wing5LZ - 9.0f);
            float arm2RXRot = -0.471f + newangle;
            rotateX(processor, "arm_2_R", arm2RXRot);
            rotateX(processor, "wing_2_R", -0.523f + newangle);
            float[] arm1R = bind(processor, "arm_1_R");  // never written: the bind pivot
            float arm2RZ = (float) ((double) arm1R[2] + Math.sin(arm1RXRot) * 11.0);
            float arm2RY = (float) ((double) arm1R[1] + Math.cos(arm1RXRot) * 11.0);
            moveYZ(processor, "arm_2_R", arm2RY, arm2RZ);
            moveYZ(processor, "wing_2_R", arm2RY, arm2RZ);
            rotateX(processor, "wing_5_R", 0.68f + newangle2 / 2.0f);
            rotateX(processor, "wing_6_R", 0.453f + newangle2 / 4.0f);
            rotateX(processor, "wing_7_L", 0.119f + newangle2 / 8.0f);
            float wing5RZ = (float) ((double) arm2RZ + Math.sin(arm2RXRot) * 20.0);
            float wing5RY = (float) ((double) arm2RY + Math.cos(arm2RXRot) * 20.0);
            moveYZ(processor, "wing_5_R", wing5RY, wing5RZ);
            moveYZ(processor, "wing_6_R", wing5RY, wing5RZ);
            moveYZ(processor, "wing_7_L", wing5RY, wing5RZ);
            moveYZ(processor, "claw_R", wing5RY + 2.0f, wing5RZ - 1.0f);
            moveYZ(processor, "claw_L2", wing5RY + 2.0f, wing5RZ - 9.0f);
            newangle2 = Mth.cos(ageInTicks * 0.6f * WINGSPEED) * (float) Math.PI * 0.02f;
            float chestXRot = -0.436f + newangle2 / 8.0f;
            rotateX(processor, "chest_ridge", chestXRot);
            rotateX(processor, "chest", chestXRot);
            rotateX(processor, "bottom_jaw", -1.308f + newangle2 / 2.0f);
            rotateX(processor, "lower_sail1", 0.297f + newangle2 / 2.0f);
            rotateX(processor, "lower_sail2", 0.384f + newangle2 / 2.0f);
            rotateX(processor, "lower_sail_3", -0.384f + newangle2 / 2.0f);
            newangle = (float) Math.toRadians(netHeadYaw) * 0.5f;
            rotateY(processor, "upper_sail2_", newangle);
            rotateY(processor, "upper_sail3", newangle);
            rotateY(processor, "upper_sail_1", newangle);
            rotateY(processor, "upper_jaw", newangle);
            rotateY(processor, "head", newangle);
            rotateY(processor, "eye_ridge_L", 0.558f + newangle);
            rotateY(processor, "anntena_1_L", 0.366f + newangle);
            rotateY(processor, "anntena_2_L", 0.139f + newangle);
            rotateY(processor, "eye_ridge_R", -0.558f + newangle);
            rotateY(processor, "anntena_1_R", -0.366f + newangle);
            rotateY(processor, "anntena_2_R", -0.139f + newangle);
            rotateY(processor, "lower_sail2", newangle);
            rotateY(processor, "lower_sail_3", newangle);
            rotateY(processor, "lower_sail1", newangle);
            rotateY(processor, "bottom_jaw", newangle);
            float[] head = bind(processor, "head");  // never written: the bind pivot
            float[] bottomJaw = bind(processor, "bottom_jaw");  // y never written: the bind
            moveTo(processor, "bottom_jaw",
                    (float) ((double) head[0] - Math.sin(newangle) * 5.0),
                    bottomJaw[1],
                    (float) ((double) head[2] - Math.cos(newangle) * 5.0));
        } else {
            if (entity.getAttacking() != 0) {
                spd = 1.7f;
                amp = 1.4f;
            }
            newangle2 = Mth.cos(ageInTicks * 1.6f * WINGSPEED * spd) * (float) Math.PI * 0.06f;
            float fchestXRot = newangle2 / 8.0f;
            rotateX(processor, "fchest", fchestXRot);
            rotateX(processor, "fchest_ridge", -0.18f + fchestXRot);
            float[] fchest = bind(processor, "fchest");  // x / z never written: the bind
            float fchestY = entity.getBeingRidden() == 0 ? (float) (-2.0 + Math.sin(newangle2) * 10.0 * (double) amp) : -2.0f;
            moveY(processor, "fchest", fchestY);
            moveY(processor, "fchest_ridge", fchestY);
            rotateX(processor, "fabdomen", 0.0f);
            float fabdomenZ = (float) ((double) fchest[2] + Math.cos(fchestXRot) * 8.0);
            float fabdomenY = (float) ((double) fchestY - Math.sin(fchestXRot) * 8.0 - 6.0);
            moveYZ(processor, "fabdomen", fabdomenY, fabdomenZ);
            moveY(processor, "fwing_3_R", fabdomenY);
            moveY(processor, "fwing_3_L", fabdomenY);
            rotateZ(processor, "fwing_3_R", 0.0f);
            rotateX(processor, "fwing_3_R", 0.0f);
            rotateZ(processor, "fwing_3_L", 0.0f);
            rotateX(processor, "fwing_3_L", 0.0f);
            rotateY(processor, "fwing_3_R", 0.785f);
            rotateY(processor, "fwing_3_L", -0.785f);
            // fwing_4_R.x = fabdomen.z + 8.0f and fwing_4_L.x = fabdomen.z - 9.0f: the classic reads z for x, kept.
            moveTo(processor, "fwing_4_R", fabdomenZ + 8.0f, fabdomenY + 0.55f, fabdomenZ + 26.0f);
            moveTo(processor, "fwing_4_L", fabdomenZ - 9.0f, fabdomenY + 0.55f, fabdomenZ + 26.0f);
            rotateX(processor, "fwing_4_R", newangle2 / 10.0f);
            rotateX(processor, "fwing_4_L", -newangle2 / 10.0f);
            float[] fleg1L = bind(processor, "fleg_1_L");  // x / z never written: the bind
            float[] fleg1R = bind(processor, "fleg_1_R");
            if (entity.getAttacking() == 0) {
                newangle = 1.5707964f;
                float fleg1LY = fabdomenY + 5.0f;
                float fleg1RY = fabdomenY + 5.0f;
                moveY(processor, "fleg_1_L", fleg1LY);
                moveY(processor, "fleg_1_R", fleg1RY);
                float fleg1LXRot = -0.1f + newangle;
                rotateX(processor, "fleg_1_L", fleg1LXRot);
                float fleg1RXRot = -0.1f + newangle;
                rotateX(processor, "fleg_1_R", fleg1RXRot);
                float fleg2LXRot = 0.1f + newangle;
                rotateX(processor, "fleg_2_L", fleg2LXRot);
                float fleg2LZ = (float) ((double) fleg1L[2] + Math.sin(fleg1LXRot) * 9.0);
                float fleg2LY = (float) ((double) fleg1LY + Math.cos(fleg1LXRot) * 9.0);
                float fleg2RXRot = 0.1f + newangle;
                rotateX(processor, "fleg_2_R", fleg2RXRot);
                float fleg2RZ = (float) ((double) fleg1R[2] + Math.sin(fleg1RXRot) * 9.0);
                float fleg2RY = (float) ((double) fleg1RY + Math.cos(fleg1RXRot) * 9.0);
                float ffootLZ = (float) ((double) fleg2LZ + Math.sin(fleg2LXRot) * 13.0);
                float ffootLY = (float) ((double) fleg2LY + Math.cos(fleg2LXRot) * 13.0);
                float ffootRZ = (float) ((double) fleg2RZ + Math.sin(fleg2RXRot) * 11.0);
                float ffootRY = (float) ((double) fleg2RY + Math.cos(fleg2RXRot) * 11.0);
                rotateX(processor, "ffootL", (float) Math.PI);
                rotateX(processor, "ffootR", (float) Math.PI);
                moveTo(processor, "fleg_2_L", fleg1L[0], fleg2LY, fleg2LZ);
                moveTo(processor, "ffootL", fleg1L[0], ffootLY, ffootLZ);
                moveTo(processor, "fleg_2_R", fleg1R[0], fleg2RY, fleg2RZ);
                moveTo(processor, "ffootR", fleg1R[0], ffootRY, ffootRZ);
            } else {
                newangle = -0.7853982f;
                newangle3 = Mth.cos(ageInTicks * 3.6f * WINGSPEED) * (float) Math.PI * 0.1f;
                float fleg1LY = fabdomenY + 5.0f;
                float fleg1RY = fabdomenY + 5.0f;
                moveY(processor, "fleg_1_L", fleg1LY);
                moveY(processor, "fleg_1_R", fleg1RY);
                float fleg1LXRot = -0.1f + newangle + newangle3;
                rotateX(processor, "fleg_1_L", fleg1LXRot);
                float fleg1RXRot = -0.1f + newangle - newangle3;
                rotateX(processor, "fleg_1_R", fleg1RXRot);
                float fleg2LXRot = 0.2f + newangle + newangle3 * 3.0f / 2.0f;
                rotateX(processor, "fleg_2_L", fleg2LXRot);
                float fleg2LZ = (float) ((double) fleg1L[2] + Math.sin(fleg1LXRot) * 9.0);
                float fleg2LY = (float) ((double) fleg1LY + Math.cos(fleg1LXRot) * 9.0);
                float fleg2RXRot = 0.2f + newangle - newangle3 * 3.0f / 2.0f;
                rotateX(processor, "fleg_2_R", fleg2RXRot);
                float fleg2RZ = (float) ((double) fleg1R[2] + Math.sin(fleg1RXRot) * 9.0);
                float fleg2RY = (float) ((double) fleg1RY + Math.cos(fleg1RXRot) * 9.0);
                float ffootLZ = (float) ((double) fleg2LZ + Math.sin(fleg2LXRot) * 13.0);
                float ffootLY = (float) ((double) fleg2LY + Math.cos(fleg2LXRot) * 13.0);
                float ffootRZ = (float) ((double) fleg2RZ + Math.sin(fleg2RXRot) * 11.0);
                float ffootRY = (float) ((double) fleg2RY + Math.cos(fleg2RXRot) * 11.0);
                rotateX(processor, "ffootL", -0.7853982f + newangle3 * 2.0f);
                rotateX(processor, "ffootR", -0.7853982f - newangle3 * 2.0f);
                moveTo(processor, "fleg_2_L", 7.0f, fleg2LY, fleg2LZ);
                moveTo(processor, "ffootL", 11.0f, ffootLY, ffootLZ);
                moveTo(processor, "fleg_2_R", -9.0f, fleg2RY, fleg2RZ);
                moveTo(processor, "ffootR", -13.0f, ffootRY, ffootRZ);
            }
            newangle = Mth.cos(ageInTicks * 1.6f * WINGSPEED * spd) * (float) Math.PI * 0.26f * amp;
            rotateZ(processor, "farm_1_L", (float) (-1.5707963267948966 - (double) newangle));
            rotateZ(processor, "farm_1_R", (float) (1.5707963267948966 + (double) newangle));
            rotateZ(processor, "fwing_1_L", (float) (-1.5707963267948966 - (double) newangle));
            rotateZ(processor, "fwing_1_R", (float) (1.5707963267948966 + (double) newangle));
            rotateZ(processor, "farm_2_L", (float) (-1.5707963267948966 - (double) (newangle * 1.3f)));
            rotateZ(processor, "fwing_2_L", (float) (-1.5707963267948966 - (double) (newangle * 1.3f)));
            float[] farm1L = bind(processor, "farm_1_L");  // never written: the bind pivot
            float farm2LX = (float) ((double) farm1L[0] + Math.cos(newangle) * 14.0);
            float farm2LY = (float) ((double) farm1L[1] - Math.sin(newangle) * 14.0);
            moveXY(processor, "farm_2_L", farm2LX, farm2LY);
            moveXY(processor, "fwing_2_L", farm2LX, farm2LY);
            float fwing5LX = (float) ((double) farm2LX + Math.cos(newangle * 1.3f) * 20.0);
            float fwing5LY = (float) ((double) farm2LY - Math.sin(newangle * 1.3f) * 20.0);
            moveXY(processor, "fwing_5_L", fwing5LX, fwing5LY);
            moveXY(processor, "fwing_6_L", fwing5LX, fwing5LY);
            moveXY(processor, "fwing_7_R", fwing5LX, fwing5LY);
            moveXY(processor, "fclaw_L", fwing5LX, fwing5LY);
            moveXY(processor, "fclaw_R_2", fwing5LX, fwing5LY);
            rotateZ(processor, "fwing_5_L", (float) (-1.5707963267948966 - (double) (newangle * 1.65f)));
            rotateZ(processor, "fwing_6_L", (float) (-1.5707963267948966 - (double) (newangle * 1.65f)));
            rotateZ(processor, "fwing_7_R", (float) (-1.5707963267948966 - (double) (newangle * 1.65f)));
            rotateX(processor, "fwing_7_R", -1.5707964f);
            rotateX(processor, "fwing_6_L", -1.1780972f);
            rotateX(processor, "fwing_5_L", -0.7853982f);
            rotateZ(processor, "farm_2_R", (float) (1.5707963267948966 + (double) (newangle * 1.3f)));
            rotateZ(processor, "fwing_2_R", (float) (1.5707963267948966 + (double) (newangle * 1.3f)));
            float[] farm1R = bind(processor, "farm_1_R");  // never written: the bind pivot
            float farm2RX = (float) ((double) farm1R[0] - Math.cos(newangle) * 14.0);
            float farm2RY = (float) ((double) farm1R[1] - Math.sin(newangle) * 14.0);
            moveXY(processor, "farm_2_R", farm2RX, farm2RY);
            moveXY(processor, "fwing_2_R", farm2RX, farm2RY);
            float fwing5RX = (float) ((double) farm2RX - Math.cos(newangle * 1.3f) * 20.0);
            float fwing5RY = (float) ((double) farm2RY - Math.sin(newangle * 1.3f) * 20.0);
            moveXY(processor, "fwing_5_R", fwing5RX, fwing5RY);
            moveXY(processor, "fwing_6_R", fwing5RX, fwing5RY);
            moveXY(processor, "fwing_7_L", fwing5RX, fwing5RY);
            moveXY(processor, "fclaw_R", fwing5RX, fwing5RY);
            moveXY(processor, "fclaw_L2", fwing5RX, fwing5RY);
            rotateZ(processor, "fwing_5_R", (float) (1.5707963267948966 + (double) (newangle * 1.65f)));
            rotateZ(processor, "fwing_6_R", (float) (1.5707963267948966 + (double) (newangle * 1.65f)));
            rotateZ(processor, "fwing_7_L", (float) (1.5707963267948966 + (double) (newangle * 1.65f)));
            rotateX(processor, "fwing_7_L", -1.5707964f);
            rotateX(processor, "fwing_6_R", -1.1780972f);
            rotateX(processor, "fwing_5_R", -0.7853982f);
            float fneck1XRot = -newangle / 12.0f;
            rotateX(processor, "fneck_1", fneck1XRot);
            float fneck1Z = (float) ((double) fchest[2] - Math.cos(fchestXRot) * 10.0);
            float fneck1Y = (float) ((double) fchestY + Math.sin(fchestXRot) * 8.0 - 1.0);
            moveYZ(processor, "fneck_1", fneck1Y, fneck1Z);
            float fneck2XRot = -newangle / 10.0f;
            rotateX(processor, "fneck_2", fneck2XRot);
            float fneck2Z = (float) ((double) fneck1Z - Math.cos(fneck1XRot) * 7.0);
            float fneck2Y = (float) ((double) fneck1Y + Math.sin(fneck1XRot) * 6.0 - 1.0);
            moveYZ(processor, "fneck_2", fneck2Y, fneck2Z);
            float fneck3XRot = -newangle / 8.0f;
            rotateX(processor, "fneck_3", fneck3XRot);
            float fneck3Z = (float) ((double) fneck2Z - Math.cos(fneck2XRot) * 7.0);
            float fneck3Y = (float) ((double) fneck2Y + Math.sin(fneck2XRot) * 5.0);
            moveYZ(processor, "fneck_3", fneck3Y, fneck3Z);
            float fheadZ = (float) ((double) fneck3Z - Math.cos(fneck3XRot) * 16.0);
            float fheadY = (float) ((double) fneck3Y + Math.sin(fneck3XRot) * 15.0);
            float[] fhead = bind(processor, "fhead");  // x never written: the bind
            moveYZ(processor, "fhead", fheadY, fheadZ);
            moveYZ(processor, "fupper_jaw", fheadY, fheadZ);
            moveYZ(processor, "fupper_sail_1", fheadY, fheadZ);
            moveYZ(processor, "fupper_sail2_", fheadY, fheadZ);
            moveYZ(processor, "fupper_sail3", fheadY, fheadZ);
            moveYZ(processor, "feye_ridge_L", fheadY, fheadZ);
            moveYZ(processor, "fanntena_1_L", fheadY, fheadZ);
            moveYZ(processor, "fanntena_2_L", fheadY, fheadZ);
            moveYZ(processor, "feye_ridge_R", fheadY, fheadZ);
            moveYZ(processor, "fanntena_1_R", fheadY, fheadZ);
            moveYZ(processor, "fanntena_2_R", fheadY, fheadZ);
            float fbottomJawZ = fheadZ - 5.0f;  // overwritten below (fbottom_jaw.z = fhead.z - cos(newangle) * 5), as the classic does
            moveYZ(processor, "flower_sail1", fheadY + 4.0f, fheadZ - 5.0f);
            moveYZ(processor, "flower_sail2", fheadY + 4.0f, fheadZ - 5.0f);
            moveYZ(processor, "flower_sail_3", fheadY + 4.0f, fheadZ - 5.0f);
            float fbottomJawY = fheadY + 4.0f;
            if (entity.getBeingRidden() == 0) {
                newangle = (float) Math.toRadians(netHeadYaw) * 0.5f;
            } else {
                // ENT-S-093: per-entity scratch as in the original (orig Leon.java:64, orig ModelLeon.java:1013-1024)
                // instead of one rf1 shared on the model; yaw source is prevRotationYaw/rotationYaw (orig ModelLeon.java:1014).
                RenderInfo r = entity.getRenderInfo();
                netHeadYaw = (entity.getYRotO() - entity.getYRot()) * 8.0f;
                netHeadYaw = -netHeadYaw;
                r.rf1 += (netHeadYaw - r.rf1) / 60.0f;
                if (r.rf1 > 50.0f) {
                    r.rf1 = 50.0f;
                }
                if (r.rf1 < -50.0f) {
                    r.rf1 = -50.0f;
                }
                netHeadYaw = r.rf1;
                // orig ModelLeon.java:1024 e.setRenderInfo(r) has no counterpart: it copied the same instance back onto
                // itself (orig Leon.java:180-189), a no-op.
                newangle = (float) Math.toRadians(netHeadYaw) * 0.5f;
            }
            rotateY(processor, "fupper_sail2_", newangle);
            rotateY(processor, "fupper_sail3", newangle);
            rotateY(processor, "fupper_sail_1", newangle);
            rotateY(processor, "fupper_jaw", newangle);
            rotateY(processor, "fhead", newangle);
            rotateY(processor, "feye_ridge_L", 0.558f + newangle);
            rotateY(processor, "fanntena_1_L", 0.366f + newangle);
            rotateY(processor, "fanntena_2_L", 0.139f + newangle);
            rotateY(processor, "feye_ridge_R", -0.558f + newangle);
            rotateY(processor, "fanntena_1_R", -0.366f + newangle);
            rotateY(processor, "fanntena_2_R", -0.139f + newangle);
            rotateY(processor, "flower_sail2", newangle);
            rotateY(processor, "flower_sail_3", newangle);
            rotateY(processor, "flower_sail1", newangle);
            rotateY(processor, "fbottom_jaw", newangle);
            fbottomJawZ = (float) ((double) fheadZ - Math.cos(newangle) * 5.0);
            float fbottomJawX = (float) ((double) fhead[0] - Math.sin(newangle) * 5.0);
            moveTo(processor, "fbottom_jaw", fbottomJawX, fbottomJawY, fbottomJawZ);
            float tf1 = 1.605f;
            float tf2 = 1.6919999f;
            float tf3 = 0.92399997f;
            float fbottomJawXRot;
            if (entity.getAttacking() == 0) {
                fbottomJawXRot = -1.308f + newangle2 / 2.0f;
            } else {
                newangle2 = Mth.cos(ageInTicks * 2.6f * WINGSPEED) * (float) Math.PI * 0.16f;
                fbottomJawXRot = -0.9f + newangle2;
            }
            rotateX(processor, "fbottom_jaw", fbottomJawXRot);
            rotateX(processor, "flower_sail1", fbottomJawXRot + tf1);
            rotateX(processor, "flower_sail2", fbottomJawXRot + tf2);
            rotateX(processor, "flower_sail_3", fbottomJawXRot + tf3);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityLeon, LeonGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new LeonGeoReplacement());
        }

        /** The classic {@link LeonRenderer#shouldRender} (OPT-013): unconditionally drawn, never frustum-culled by its hitbox (T1a). */
        @Override
        public boolean shouldRender(EntityLeon entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return true;
        }
    }
}
