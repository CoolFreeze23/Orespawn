package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTrooperBug;
import danger.orespawn.entity.pose.TrooperBugPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Trooper Bug (the hooks, owner 2026-09-14, addendum item 10): {@link TrooperBugModel#poseFrom} verbatim on
 * the rig the landing slice converts, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription -
 * the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.22f (orig
 * ModelTrooperBug.java:14,151 / ClientProxyOreSpawn.java:451): five ATTACKING branches (orig :969-989
 * {@code getAttacking() == 0}) - the antennae about Y (0.4 x ws at 0.05 x PI at rest, 1.4 at 0.1 attacking, about
 * +-0.78), the six upper-arm parts about Y (0.5 at 0.05 / 2.5 at 0.15, mirrored), the four forearm tips about X (0.3
 * at 0.05 / 2.6 at 0.2, about 1.56), the head and jaw ridges about Y (0.1 at 0.02 / 1.0 at 0.1, about -+0.25 /
 * -+0.372) and the fourteen jaw parts about X (0.3 at 0.015 / 2.6 at 0.1, about 0.22, jawbase8 / 9 about their own
 * rests in X and Y); and the {@code Mth.sin} gait on the four legs - {@code sin(age x 2.0 x ws) x PI x 0.12 x
 * limbSwingAmount}, the right pair a half turn behind ({@code + PI}), the lift {@code |cos|} of the same rhythm on the
 * rising half-cycle ({@code nextangle > newangle}) - through the four leg helpers transcribed below (the same names):
 * the ten parts of a leg share the upper link's yaw, the second and third links' pivots FOLLOW the link above 26 / 32
 * units along (cos, sin) of that yaw scaled by cos of the link's pitch (the POSITION-write idiom, x and z through
 * {@link #moveXZ}; the upper link's pivot is never written - the bind, read through {@link #classicPosition}); every
 * value the classic reads back from a part it just wrote is held in a local. The entity is read through
 * {@link TrooperBugPose} (the Slice 4b form). Two jaw parts are zero-thickness cubes (3 x 5 x 0; ENT-S-161; the
 * classic face order required below).
 *
 * <p>Scale and shadow follow {@link TrooperBugRenderer}: 1.1 render scale and a 0.95 x 1.1 shadow (ENT-S-092).</p>
 */
public final class TrooperBugGeoReplacement extends OreSpawnGeoReplacement<EntityTrooperBug> {
    /** orig ModelTrooperBug.java:14,151 {@code wingspeed} = 0.22f (ClientProxyOreSpawn.java:451): the chain's third multiply. */
    static final float WINGSPEED = 0.22F;
    private static final GeoReplacementDescriptor<EntityTrooperBug> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_TROOPER_BUG.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityTrooperBug.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/trooperbug.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/trooperbug.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/trooperbug.png"),
            TrooperBugRenderer.SHADOW) {
        @Override
        public void applyScale(EntityTrooperBug entity, PoseStack poseStack, float partialTick) {
            // orig RenderTrooperBug.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) (TrooperBugRenderer.scale)
            poseStack.scale(TrooperBugRenderer.SCALE, TrooperBugRenderer.SCALE, TrooperBugRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (two jaw parts, 3 x 5 x 0): the shipped geo carries the classic within-cube
         * face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public TrooperBugGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        TrooperBugPose entity = inputs.subject(TrooperBugPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // TrooperBugModel.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain exactly as the
        // classic casts it (its unused locals - Object r, pi4 - carry nothing and are not declared).
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        newangle = entity.getAttacking() == 0 ? Mth.cos(ageInTicks * 0.4f * WINGSPEED) * (float) Math.PI * 0.05f : Mth.cos(ageInTicks * 1.4f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateY(processor, "antenna2part2", 0.78f + newangle);
        rotateY(processor, "antenna1part2", -0.78f - newangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.05f : Mth.cos(ageInTicks * 2.5f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "arm4part1", newangle);
        rotateY(processor, "arm4part1b", newangle);
        rotateY(processor, "arm4part1c", newangle);
        rotateY(processor, "arm3part1", -newangle);
        rotateY(processor, "arm3part1b", -newangle);
        rotateY(processor, "arm3part1c", -newangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.05f : Mth.cos(ageInTicks * 2.6f * WINGSPEED) * (float) Math.PI * 0.2f;
        rotateX(processor, "arm1part3", 1.56f + newangle);
        rotateX(processor, "arm1part3b", 1.56f + newangle);
        rotateX(processor, "arm2part3", 1.56f - newangle);
        rotateX(processor, "arm2part3b", 1.56f - newangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos(ageInTicks * 0.1f * WINGSPEED) * (float) Math.PI * 0.02f : Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateY(processor, "headleftridge", -0.25f + newangle);
        rotateY(processor, "headrightridge", 0.25f - newangle);
        rotateY(processor, "upperjawridgeleft", -0.372f + newangle);
        rotateY(processor, "upperjawridgeright", 0.372f - newangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.015f : Mth.cos(ageInTicks * 2.6f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "jawbase", 0.22f + newangle);
        rotateX(processor, "jawbase2", 0.22f + newangle);
        rotateX(processor, "jawbase3", 0.22f + newangle);
        rotateX(processor, "jawbase4", 0.22f + newangle);
        rotateX(processor, "jawbase5", 0.22f + newangle);
        rotateX(processor, "jawbase6", 0.22f + newangle);
        rotateX(processor, "jawbase7", 0.22f + newangle);
        rotateX(processor, "jawbase8", 0.2146f + newangle);
        rotateY(processor, "jawbase8", 0.1487f + newangle);
        rotateX(processor, "jawbase9", 0.1f + newangle);
        rotateY(processor, "jawbase9", 0.07f + newangle);
        rotateX(processor, "jawend", 0.22f + newangle);
        rotateX(processor, "jawleft", 0.22f + newangle);
        rotateX(processor, "jawright", 0.22f + newangle);
        newangle = Mth.sin(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.sin((ageInTicks + 0.1f) * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount);
        }
        doLeftFrontLeg(processor, newangle, upangle);
        doLeftRearLeg(processor, -newangle, upangle);
        newangle = Mth.sin((float) ((double) (ageInTicks * 2.0f * WINGSPEED) + Math.PI)) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.sin((float) ((double) ((ageInTicks + 0.1f) * 2.0f * WINGSPEED) + Math.PI)) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos((float) ((double) (ageInTicks * 2.0f * WINGSPEED) + Math.PI)) * (float) Math.PI * 0.12f * limbSwingAmount);
        }
        doRightFrontLeg(processor, -newangle, upangle);
        doRightRearLeg(processor, newangle, upangle);
    }

    /** TrooperBugModel.doRightFrontLeg verbatim on the leg1 chain. */
    private static void doRightFrontLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg1part1YRot = 1.2f + angle;
        rotateY(processor, "leg1part1", leg1part1YRot);
        rotateY(processor, "leg1part1b", leg1part1YRot);
        rotateY(processor, "leg1elbow", leg1part1YRot);
        rotateY(processor, "leg1part2", leg1part1YRot);
        rotateY(processor, "leg1part2b", leg1part1YRot);
        rotateY(processor, "leg1part2c", leg1part1YRot);
        rotateY(processor, "leg1part3", leg1part1YRot);
        rotateY(processor, "leg1part3b", leg1part1YRot);
        rotateY(processor, "leg1part3c", leg1part1YRot);
        rotateY(processor, "leg1part3d", leg1part1YRot);
        float leg1part1XRot = 1.115f + upangle;
        rotateX(processor, "leg1part1", leg1part1XRot);
        rotateX(processor, "leg1part1b", 1.078f + upangle);
        rotateX(processor, "leg1elbow", leg1part1XRot);
        float dist = 26.0f;
        dist = (float) ((double) dist * Math.cos(leg1part1XRot));
        float[] leg1part1 = classicPosition(bone(processor, "leg1part1"));  // never written: the bind pivot
        float leg1part2Z = (float) ((double) leg1part1[2] - Math.cos(leg1part1YRot) * (double) dist);
        float leg1part2X = (float) ((double) leg1part1[0] - Math.sin(leg1part1YRot) * (double) dist);
        moveXZ(processor, "leg1part2c", leg1part2X, leg1part2Z);
        moveXZ(processor, "leg1part2b", leg1part2X, leg1part2Z);
        moveXZ(processor, "leg1part2", leg1part2X, leg1part2Z);
        float leg1part2XRot = 1.871f - upangle;
        rotateX(processor, "leg1part2", leg1part2XRot);
        rotateX(processor, "leg1part2b", 1.817f - upangle);
        rotateX(processor, "leg1part2c", 1.762f - upangle);
        dist = 32.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg1part2XRot));
        float leg1part3Z = (float) ((double) leg1part2Z - Math.cos(leg1part1YRot) * (double) dist);  // leg1part2.yRot = leg1part1.yRot
        float leg1part3X = (float) ((double) leg1part2X - Math.sin(leg1part1YRot) * (double) dist);
        moveXZ(processor, "leg1part3d", leg1part3X, leg1part3Z);
        moveXZ(processor, "leg1part3c", leg1part3X, leg1part3Z);
        moveXZ(processor, "leg1part3b", leg1part3X, leg1part3Z);
        moveXZ(processor, "leg1part3", leg1part3X, leg1part3Z);
        rotateX(processor, "leg1part3", 1.08f + upangle);
        rotateX(processor, "leg1part3b", 1.08f + upangle);
        rotateX(processor, "leg1part3c", 1.08f + upangle);
        rotateX(processor, "leg1part3d", 1.08f + upangle);
    }

    /** TrooperBugModel.doLeftFrontLeg verbatim on the leg2 chain. */
    private static void doLeftFrontLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg2part1YRot = -1.2f + angle;
        rotateY(processor, "leg2part1", leg2part1YRot);
        rotateY(processor, "leg2part1b", leg2part1YRot);
        rotateY(processor, "leg2elbow", leg2part1YRot);
        rotateY(processor, "leg2part2", leg2part1YRot);
        rotateY(processor, "leg2part2b", leg2part1YRot);
        rotateY(processor, "leg2part2c", leg2part1YRot);
        rotateY(processor, "leg2part3", leg2part1YRot);
        rotateY(processor, "leg2part3b", leg2part1YRot);
        rotateY(processor, "leg2part3c", leg2part1YRot);
        rotateY(processor, "leg2part3d", leg2part1YRot);
        float leg2part1XRot = 1.115f + upangle;
        rotateX(processor, "leg2part1", leg2part1XRot);
        rotateX(processor, "leg2part1b", 1.078f + upangle);
        rotateX(processor, "leg2elbow", leg2part1XRot);
        float dist = 26.0f;
        dist = (float) ((double) dist * Math.cos(leg2part1XRot));
        float[] leg2part1 = classicPosition(bone(processor, "leg2part1"));  // never written: the bind pivot
        float leg2part2Z = (float) ((double) leg2part1[2] - Math.cos(leg2part1YRot) * (double) dist);
        float leg2part2X = (float) ((double) leg2part1[0] - Math.sin(leg2part1YRot) * (double) dist);
        moveXZ(processor, "leg2part2c", leg2part2X, leg2part2Z);
        moveXZ(processor, "leg2part2b", leg2part2X, leg2part2Z);
        moveXZ(processor, "leg2part2", leg2part2X, leg2part2Z);
        float leg2part2XRot = 1.871f - upangle;
        rotateX(processor, "leg2part2", leg2part2XRot);
        rotateX(processor, "leg2part2b", 1.817f - upangle);
        rotateX(processor, "leg2part2c", 1.762f - upangle);
        dist = 32.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg2part2XRot));
        float leg2part3Z = (float) ((double) leg2part2Z - Math.cos(leg2part1YRot) * (double) dist);  // leg2part2.yRot = leg2part1.yRot
        float leg2part3X = (float) ((double) leg2part2X - Math.sin(leg2part1YRot) * (double) dist);
        moveXZ(processor, "leg2part3d", leg2part3X, leg2part3Z);
        moveXZ(processor, "leg2part3c", leg2part3X, leg2part3Z);
        moveXZ(processor, "leg2part3b", leg2part3X, leg2part3Z);
        moveXZ(processor, "leg2part3", leg2part3X, leg2part3Z);
        rotateX(processor, "leg2part3", 1.08f + upangle);
        rotateX(processor, "leg2part3b", 1.08f + upangle);
        rotateX(processor, "leg2part3c", 1.08f + upangle);
        rotateX(processor, "leg2part3d", 1.08f + upangle);
    }

    /** TrooperBugModel.doRightRearLeg verbatim on the leg4 chain. */
    private static void doRightRearLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg4part1YRot = -1.2f + angle;
        rotateY(processor, "leg4part1", leg4part1YRot);
        rotateY(processor, "leg4part1b", leg4part1YRot);
        rotateY(processor, "leg4elbow", leg4part1YRot);
        rotateY(processor, "leg4part2", leg4part1YRot);
        rotateY(processor, "leg4part2b", leg4part1YRot);
        rotateY(processor, "leg4part2c", leg4part1YRot);
        rotateY(processor, "leg4part3", leg4part1YRot);
        rotateY(processor, "leg4part3b", leg4part1YRot);
        rotateY(processor, "leg4part3c", leg4part1YRot);
        rotateY(processor, "leg4part3d", leg4part1YRot);
        float leg4part1XRot = -1.115f + upangle;
        rotateX(processor, "leg4part1", leg4part1XRot);
        rotateX(processor, "leg4part1b", -1.078f + upangle);
        rotateX(processor, "leg4elbow", leg4part1XRot);
        float dist = 26.0f;
        dist = (float) ((double) dist * Math.cos(leg4part1XRot));
        float[] leg4part1 = classicPosition(bone(processor, "leg4part1"));  // never written: the bind pivot
        float leg4part2Z = (float) ((double) leg4part1[2] + Math.cos(leg4part1YRot) * (double) dist);
        float leg4part2X = (float) ((double) leg4part1[0] + Math.sin(leg4part1YRot) * (double) dist);
        moveXZ(processor, "leg4part2c", leg4part2X, leg4part2Z);
        moveXZ(processor, "leg4part2b", leg4part2X, leg4part2Z);
        moveXZ(processor, "leg4part2", leg4part2X, leg4part2Z);
        float leg4part2XRot = -1.871f - upangle;
        rotateX(processor, "leg4part2", leg4part2XRot);
        rotateX(processor, "leg4part2b", -1.817f - upangle);
        rotateX(processor, "leg4part2c", -1.762f - upangle);
        dist = 32.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg4part2XRot));
        float leg4part3Z = (float) ((double) leg4part2Z + Math.cos(leg4part1YRot) * (double) dist);  // leg4part2.yRot = leg4part1.yRot
        float leg4part3X = (float) ((double) leg4part2X + Math.sin(leg4part1YRot) * (double) dist);
        moveXZ(processor, "leg4part3d", leg4part3X, leg4part3Z);
        moveXZ(processor, "leg4part3c", leg4part3X, leg4part3Z);
        moveXZ(processor, "leg4part3b", leg4part3X, leg4part3Z);
        moveXZ(processor, "leg4part3", leg4part3X, leg4part3Z);
        rotateX(processor, "leg4part3", -1.08f + upangle);
        rotateX(processor, "leg4part3b", -1.08f + upangle);
        rotateX(processor, "leg4part3c", -1.08f + upangle);
        rotateX(processor, "leg4part3d", -1.08f + upangle);
    }

    /** TrooperBugModel.doLeftRearLeg verbatim on the leg3 chain. */
    private static void doLeftRearLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg3part1YRot = 1.2f + angle;
        rotateY(processor, "leg3part1", leg3part1YRot);
        rotateY(processor, "leg3part1b", leg3part1YRot);
        rotateY(processor, "leg3elbow", leg3part1YRot);
        rotateY(processor, "leg3part2", leg3part1YRot);
        rotateY(processor, "leg3part2b", leg3part1YRot);
        rotateY(processor, "leg3part2c", leg3part1YRot);
        rotateY(processor, "leg3part3", leg3part1YRot);
        rotateY(processor, "leg3part3b", leg3part1YRot);
        rotateY(processor, "leg3part3c", leg3part1YRot);
        rotateY(processor, "leg3part3d", leg3part1YRot);
        float leg3part1XRot = -1.115f + upangle;
        rotateX(processor, "leg3part1", leg3part1XRot);
        rotateX(processor, "leg3part1b", -1.078f + upangle);
        rotateX(processor, "leg3elbow", leg3part1XRot);
        float dist = 26.0f;
        dist = (float) ((double) dist * Math.cos(leg3part1XRot));
        float[] leg3part1 = classicPosition(bone(processor, "leg3part1"));  // never written: the bind pivot
        float leg3part2Z = (float) ((double) leg3part1[2] + Math.cos(leg3part1YRot) * (double) dist);
        float leg3part2X = (float) ((double) leg3part1[0] + Math.sin(leg3part1YRot) * (double) dist);
        moveXZ(processor, "leg3part2c", leg3part2X, leg3part2Z);
        moveXZ(processor, "leg3part2b", leg3part2X, leg3part2Z);
        moveXZ(processor, "leg3part2", leg3part2X, leg3part2Z);
        float leg3part2XRot = -1.871f - upangle;
        rotateX(processor, "leg3part2", leg3part2XRot);
        rotateX(processor, "leg3part2b", -1.817f - upangle);
        rotateX(processor, "leg3part2c", -1.762f - upangle);
        dist = 32.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg3part2XRot));
        float leg3part3Z = (float) ((double) leg3part2Z + Math.cos(leg3part1YRot) * (double) dist);  // leg3part2.yRot = leg3part1.yRot
        float leg3part3X = (float) ((double) leg3part2X + Math.sin(leg3part1YRot) * (double) dist);
        moveXZ(processor, "leg3part3d", leg3part3X, leg3part3Z);
        moveXZ(processor, "leg3part3c", leg3part3X, leg3part3Z);
        moveXZ(processor, "leg3part3b", leg3part3X, leg3part3Z);
        moveXZ(processor, "leg3part3", leg3part3X, leg3part3Z);
        rotateX(processor, "leg3part3", -1.08f + upangle);
        rotateX(processor, "leg3part3b", -1.08f + upangle);
        rotateX(processor, "leg3part3c", -1.08f + upangle);
        rotateX(processor, "leg3part3d", -1.08f + upangle);
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityTrooperBug, TrooperBugGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TrooperBugGeoReplacement());
        }
    }
}
