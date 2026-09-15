package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GiantRobot;
import danger.orespawn.entity.pose.GiantRobotPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Giant Robot (the hooks, owner 2026-09-14, addendum item 10; landed by the first Tier-1 slice T1a,
 * 2026-09-15, the owner's closing set item 4): {@link ModelGiantRobot#poseFrom} verbatim on the
 * render-instance-expanded rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an
 * artist delivers {@code idle} and {@code walk} ; the geo, the wiring and the proofs landed with T1a). Wingspeed 0.25f
 * (orig ClientProxyOreSpawn.java:516 {@code new ModelGiantRobot(0.25f)} , the model's {@code WING_SPEED} ): the
 * MOVESCALE idiom ({@code limbSwingAmount * 0.65f} clamped to 1, orig ModelGiantRobot.java:158-161) scaling every walk
 * term; the hip's sway (X on {@code cos(-age ws)} , Y on {@code sin(-age ws)} , both x PI x 0.1, the yaw plus a
 * constant quarter turn) and its 4-unit BOB at twice the walk frequency (a POSITION write through {@link #moveTo} ,
 * orig :168-171) that the three back parts, the shoulders, the neck and the head FOLLOW (orig :267-272); the two-phase
 * thigh / shin angles (0.15 / 0.2 x PI around -0.19634954 / +0.62831854 x movescale, orig :162-167); the ATTACKING
 * branch (orig :227-240 {@code getAttacking() != 0} ): the shoulders twist on {@code -sin(age ws 2) PI 0.2} where idle
 * they twist on the hip's negated sway, and the arms windmill ({@code sin(age ws 2) PI / 5 - PI / 4} for the upper
 * arm, {@code PI - upper} for the lower, both + 0.62831853) where idle they copy the thigh angles; the torso
 * counter-twist (back3 at half the shoulders', orig :241-242); and the HEAD-LOOK idiom (yaw
 * {@code toRadians(netHeadYaw)} , pitch {@code toRadians(headPitch) / 3} ). The entity is read through
 * {@link GiantRobotPose} (the Slice 4b doctrine).
 * <p>THE RENDER-INSTANCE FORM ({@code step_scope: explicit} - the Giant Robot's reference entry, THE FOLDER'S GAPS
 * 2026-09-14): the classic draws its eleven shared leg and arm parts twice per frame, re-posed between the draws by
 * {@code renderLeg} (orig :173-199 the sign +1 pass, :200-226 the sign -1 pass) and {@code renderArm} (orig :243-254 /
 * :255-266), so the converted rig carries one top-level clone per draw - {@code <part>__i0} the sign +1 pass,
 * {@code <part>__i1} the sign -1 pass, twenty-two bones - and this hook poses every clone directly with the classic
 * helpers' statements ({@link #renderLeg} / {@link #renderArm} below: the same names, the pose statements only - the
 * draw calls are the rig's {@code orespawn:bone_draw_order} ). Every value the classic reads back from a part it just
 * wrote is held in a local; the coordinates the classic never writes (the hip's and the followers' x / z, the
 * shoulders' z) are read through {@link #classicPosition} (the bind).</p>
 * <p>Scale and shadow follow {@link GiantRobotRenderer} : {@code SCALE} 1.0 (identity, so no scale hook) and a 0.99 x
 * 1.0 shadow (ENT-S-092). No zero-thickness cube. The Jeffery shares this rig and hook under its own registry ({@link
 * JefferyGeoReplacement}, the Ant precedent).</p>
 */
public final class GiantRobotGeoReplacement extends OreSpawnGeoReplacement<GiantRobot> {
    /** orig ClientProxyOreSpawn.java:516 {@code new ModelGiantRobot(0.25f)}: the model's {@code WING_SPEED}. */
    static final float WING_SPEED = 0.25f;
    /** orig ModelGiantRobot.java:41 - the hip's rotation-point Y captured as {@code hipy} (the model's {@code HIP_BASE_Y}). */
    static final float HIP_BASE_Y = -60.0f;
    private static final GeoReplacementDescriptor<GiantRobot> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GIANT_ROBOT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            GiantRobot.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/giantrobot.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/giantrobot.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/giantrobot.png"),
            GiantRobotRenderer.SHADOW) {
    };

    public GiantRobotGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        poseRig(processor, inputs);
    }

    /** The rig's hook, shared with the Jeffery: ModelGiantRobot.poseFrom verbatim, then the two-pass leg / arm poses over the clones. */
    static void poseRig(AnimationProcessor<?> processor, PoseInputs inputs) {
        GiantRobotPose entity = inputs.subject(GiantRobotPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelGiantRobot.poseFrom verbatim. orig ModelGiantRobot.java:158-161 - movescale = limbSwingAmount * 0.65 clamped to 1.
        float movescale = limbSwingAmount * 0.65f;
        if (movescale > 1.0f) {
            movescale = 1.0f;
        }

        // orig ModelGiantRobot.java:162-167 - hip sway and two-phase thigh/shin angles.
        float hipXAngle = (float) (Math.cos(-ageInTicks * WING_SPEED) * Math.PI * 0.1f * movescale);
        float hipYAngle = (float) (Math.sin(-ageInTicks * WING_SPEED) * Math.PI * 0.1f * movescale);
        float thighAngle0 = (float) (Math.cos((double) (-ageInTicks * WING_SPEED) + Math.PI / 2.0) * Math.PI * 0.15f * movescale)
                - (float) (0.19634954084936207 * movescale);
        float thighAngle1 = (float) (Math.cos((double) (-ageInTicks * WING_SPEED) + Math.PI + Math.PI / 2.0) * Math.PI * 0.15f * movescale)
                - (float) (0.19634954084936207 * movescale);
        float shinAngle0 = (float) ((double) ((float) (Math.cos((double) (-ageInTicks * WING_SPEED) + Math.PI) * Math.PI * 0.2f * movescale))
                + 0.6283185400806344 * movescale);
        float shinAngle1 = (float) ((double) ((float) (Math.cos(-ageInTicks * WING_SPEED) * Math.PI * 0.2f * movescale))
                + 0.6283185400806344 * movescale);

        // orig ModelGiantRobot.java:168-171 - hip bob (4px at twice the walk frequency) and hip rotation.
        float bob = (float) (Math.cos(-ageInTicks * WING_SPEED * 2.0f) * movescale);
        float[] hipBind = classicPosition(bone(processor, "hip"));  // x and z never written: the bind pivot
        float hipX = hipBind[0];
        float hipY = HIP_BASE_Y + bob * 4.0f;
        float hipZ = hipBind[2];
        moveTo(processor, "hip", hipX, hipY, hipZ);
        float hipXRot = hipXAngle;
        rotateX(processor, "hip", hipXRot);
        float hipYRot = (float) ((double) hipYAngle + Math.PI / 2.0);
        rotateY(processor, "hip", hipYRot);

        // orig ModelGiantRobot.java:227-240 - arms follow the thigh swing when idle,
        // windmill punch + shoulder twist when getAttacking() != 0.
        float shoulderAngle = -hipYAngle;
        float armA1Angle;
        float armA2Angle;
        float armB1Angle;
        float armB2Angle;
        armA1Angle = armA2Angle = thighAngle1;
        armB1Angle = armB2Angle = thighAngle0;
        if (entity.getAttacking() != 0) {
            shoulderAngle = (float) (-(Math.sin(ageInTicks * WING_SPEED * 2.0f) * Math.PI * (double) 0.2f));
            armA1Angle = (float) ((double) ((float) (Math.sin(ageInTicks * WING_SPEED * 2.0f) * Math.PI / 5.0)) - 0.7853981633974483);
            armA2Angle = (float) ((double) (-armA1Angle) + Math.PI);
            armA1Angle = (float) ((double) armA1Angle + 0.6283185307179586);
            armA2Angle = (float) ((double) armA2Angle + 0.6283185307179586);
            armB1Angle = (float) ((double) ((float) (-(Math.sin(ageInTicks * WING_SPEED * 2.0f) * Math.PI / 5.0))) - 0.7853981633974483);
            armB2Angle = (float) ((double) (-armB1Angle) + Math.PI);
            armB1Angle = (float) ((double) armB1Angle + 0.6283185307179586);
            armB2Angle = (float) ((double) armB2Angle + 0.6283185307179586);
        }

        // orig ModelGiantRobot.java:241-242 - torso counter-twist.
        rotateY(processor, "back3", shoulderAngle / 2.0f);
        float shouldersYRot = shoulderAngle;
        rotateY(processor, "shoulders", shouldersYRot);

        // orig ModelGiantRobot.java:267-272 - torso parts ride the hip bob; head look.
        moveY(processor, "back3", hipY);       // back2.y = back3.y = hip.y
        moveY(processor, "back2", hipY);
        moveY(processor, "back1", hipY);       // back1.y = back3.y
        moveY(processor, "head", hipY);        // neck.y = head.y = hip.y
        moveY(processor, "neck", hipY);
        moveY(processor, "shoulders", hipY);   // shoulders.y = head.y
        rotateY(processor, "head", (float) Math.toRadians(netHeadYaw));
        rotateX(processor, "head", (float) Math.toRadians(headPitch) / 3.0f);

        // ModelGiantRobot.renderToBuffer's two passes over the shared parts, each on its draw's clones:
        // orig :173-199 the first leg (sign +1), :200-226 the second (sign -1); :243-254 / :255-266 the two arms.
        renderLeg(processor, 0, hipX, hipY, hipZ, hipXRot, hipYRot, thighAngle0, shinAngle0, 1.0f);
        renderLeg(processor, 1, hipX, hipY, hipZ, hipXRot, hipYRot, thighAngle1, shinAngle1, -1.0f);
        float shouldersZ = classicPosition(bone(processor, "shoulders"))[2];  // never written: the bind pivot
        renderArm(processor, 0, hipX, hipY, shouldersZ, shouldersYRot, armA1Angle, armA2Angle, 1.0f);
        renderArm(processor, 1, hipX, hipY, shouldersZ, shouldersYRot, armB1Angle, armB2Angle, -1.0f);
    }

    /**
     * ModelGiantRobot.renderLeg's pose statements (orig ModelGiantRobot.java:173-199 sign +1 / draw 0, :200-226 sign -1 /
     * draw 1 - the three hip offsets negated) on that draw's thigh / shin / foot clones. The hip values are the ones the
     * hook just wrote, held in locals; the draw calls are the rig's draw order.
     */
    private static void renderLeg(AnimationProcessor<?> processor, int draw, float hipX, float hipY, float hipZ,
                                  float hipXRot, float hipYRot, float thighAngle, float shinAngle, float sign) {
        String thigh = clone("thigh", draw);
        String thigh2 = clone("thigh2", draw);
        String thigh3 = clone("thigh3", draw);
        String shin = clone("shin", draw);
        String foot1 = clone("foot1", draw);
        String foot2 = clone("foot2", draw);
        String foot3 = clone("foot3", draw);
        float thighXRot = thighAngle;   // thigh2.xRot = thigh3.xRot = thighAngle; thigh.xRot = thigh3.xRot
        rotateX(processor, thigh2, thighXRot);
        rotateX(processor, thigh3, thighXRot);
        rotateX(processor, thigh, thighXRot);
        float thighY = hipY - sign * Mth.sin(hipXRot) * 13.0f;
        float thighZ = hipZ + sign * Mth.cos(hipXRot) * Mth.cos(hipYRot) * 13.0f;
        float thighX = hipX + sign * Mth.cos(hipXRot) * Mth.sin(hipYRot) * 13.0f;
        moveTo(processor, thigh2, thighX, thighY, thighZ);
        moveTo(processor, thigh3, thighX, thighY, thighZ);
        moveTo(processor, thigh, thighX, thighY, thighZ);

        float shinXRot = shinAngle;
        rotateX(processor, shin, shinXRot);
        float shinY = thighY + Mth.cos(thighXRot) * 40.0f;
        float shinZ = thighZ + Mth.sin(thighXRot) * 40.0f;
        float shinX = thighX;
        moveTo(processor, shin, shinX, shinY, shinZ);

        rotateX(processor, foot2, shinAngle);   // foot2.xRot = foot3.xRot = shinAngle; foot1.xRot = foot3.xRot
        rotateX(processor, foot3, shinAngle);
        rotateX(processor, foot1, shinAngle);
        moveTo(processor, foot2, shinX, shinY, shinZ);
        moveTo(processor, foot3, shinX, shinY, shinZ);
        moveTo(processor, foot1, shinX, shinY, shinZ);
    }

    /**
     * ModelGiantRobot.renderArm's pose statements (orig ModelGiantRobot.java:243-254 sign +1 / draw 0: x offset +26, z
     * offset {@code -sin(shoulders.yRot) * 26}; :255-266 sign -1 / draw 1) on that draw's arm / knuckle clones.
     */
    private static void renderArm(AnimationProcessor<?> processor, int draw, float hipX, float hipY, float shouldersZ,
                                  float shouldersYRot, float upperAngle, float lowerAngle, float sign) {
        String arm1 = clone("arm1", draw);
        String arm2 = clone("arm2", draw);
        String arm3 = clone("arm3", draw);
        String knuckles = clone("knuckles", draw);
        float arm1Y = hipY - 60.0f;
        float arm1X = hipX + sign * 26.0f;
        float arm1Z = shouldersZ - sign * Mth.sin(shouldersYRot) * 26.0f;
        moveTo(processor, arm1, arm1X, arm1Y, arm1Z);
        moveTo(processor, arm2, arm1X, arm1Y, arm1Z);
        float arm1XRot = upperAngle;
        rotateX(processor, arm1, arm1XRot);
        rotateX(processor, arm2, arm1XRot);

        // orig ModelGiantRobot.java:249 - forearm leads the upper arm by -0.19634954 rad.
        float arm3XRot = (float) ((double) lowerAngle - 0.19634954084936207);
        rotateX(processor, arm3, arm3XRot);
        rotateX(processor, knuckles, arm3XRot);
        float arm3Y = arm1Y + Mth.cos(arm1XRot) * 41.0f;
        float arm3Z = arm1Z + Mth.sin(arm1XRot) * 41.0f;
        float arm3X = arm1X;
        moveTo(processor, arm3, arm3X, arm3Y, arm3Z);
        moveTo(processor, knuckles, arm3X, arm3Y, arm3Z);
    }

    /** Draw {@code k} of a shared part is the clone bone {@code <part>__i<k>} (the reference entry's explicit instances). */
    private static String clone(String part, int draw) {
        return part + "__i" + draw;
    }

    /** {@code part.y = y} on a part whose x and z the classic never writes (the bind): the single-coordinate write. */
    private static void moveY(AnimationProcessor<?> processor, String name, float y) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], y, position[2]);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<GiantRobot, GiantRobotGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GiantRobotGeoReplacement());
        }
    }
}
