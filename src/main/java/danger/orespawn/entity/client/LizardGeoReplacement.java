package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Lizard;
import danger.orespawn.entity.pose.LizardPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Lizard (the hook survey, landed by the fifth Tier-2 slice T2e): {@link LizardModel#poseFrom} verbatim on the converted rig, ON THE HOOK
 * (no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.65f (orig
 * ModelLizard.java:15,89 / ClientProxyOreSpawn.java:445): the THRESHOLD gait on the twenty leg, foot and toe parts
 * ({@code cos(age * 1.0 ws) * PI * 0.25 * limbSwingAmount} above a walking speed of a tenth, the front pair
 * positive, the rear pair the negative, the lower legs about X and the rest about Y); the ATTACKING-branch lower jaw
 * ({@code 0.52 + cos(age * 0.45) * 0.35} attacking, 0.25 at rest, copied to its six teeth) and the ATTACKING-branch tail
 * rhythm ({@code 0.25 ws x PI x 0.05} at rest, {@code 1.25 ws x PI x 0.35} attacking) folded down five rings as 0.25 /
 * 0.5 / 0.75 / 1.0 / 1.25 of the yaw with a POSITION follow through {@link #moveTo} (12 / 9 / 7 / 7 units along
 * cos / sin); and the HEAD-LOOK idiom: the neck yaws a quarter of {@code toRadians(netHeadYaw)}, the upper
 * jaw and its eighteen followers (noses, eyes at +-0.78, teeth, hat) a half, their pivots FOLLOWING the neck by 2
 * units, the lower jaw and its six teeth a half following by 3 units. The entity is read through {@link LizardPose}. The
 * eight fins are zero-thickness cubes (the seam draws every cube with its true transformed normal, ENT-S-161, and
 * its two coplanar faces in the classic order - below).
 *
 * <p>Scale and shadow follow {@link LizardRenderer}: 1.0 render scale, halved for a baby, and a 0.75 x 1.0 shadow
 * (ENT-S-092).</p>
 */
public final class LizardGeoReplacement extends OreSpawnGeoReplacement<Lizard> {
    /** orig ModelLizard.java:15,89 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:445): the chain's third multiply. */
    static final float WINGSPEED = 0.65F;
    private static final GeoReplacementDescriptor<Lizard> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.LIZARD.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Lizard.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/lizard.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/lizard.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lizard.png"),
            LizardRenderer.SHADOW) {
        @Override
        public void applyScale(Lizard entity, PoseStack poseStack, float partialTick) {
            // orig RenderLizard.preRenderScale (:42-48): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            float s = entity.isBaby() ? LizardRenderer.SCALE / 2.0F : LizardRenderer.SCALE;
            poseStack.scale(s, s, s);
        }

        /**
         * A rig with zero-thickness cubes (the eight fins, 0 x 7..12 x 3..6): the within-cube face order decides a flat
         * cube's z-fight, so the shipped geo carries the classic order ({@link FaceOrder#KEY}; TEST-007) and the seam
         * expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    /** The upper jaw's followers: {@code z / x = JawTop's, yRot = JawTop's} (the eyes add +-0.78 to the yaw). */
    private static final String[] JAW_TOP_FOLLOWERS = {"TopNose", "BottomNose", "CenterRightNose", "CenterMiddleNose",
            "CenterLeftNose", "Tooth11", "Tooth10", "Tooth1", "Tooth8", "Tooth4", "Tooth3", "Tooth5", "Tooth6", "Tooth7",
            "Tooth2", "Hat1", "Hat2"};
    /** The lower jaw's followers: their pitch the jaw's, then {@code z / x = BottomJaw's, yRot = BottomJaw's}. */
    private static final String[] BOTTOM_JAW_TEETH = {"Tooth9", "Tooth16", "Tooth15", "Tooth14", "Tooth13", "Tooth12"};

    public LizardGeoReplacement() {
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
        LizardPose entity = inputs.subject(LizardPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // LizardModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateY(processor, "TopFrontLeftLeg", newangle);
        rotateX(processor, "BottomFrontLeftLeg", newangle);
        rotateY(processor, "FrontLeftFoot", newangle);
        rotateY(processor, "Toe8", newangle);
        rotateY(processor, "Toe1", newangle);
        rotateY(processor, "TopFrontRightLeg", newangle);
        rotateX(processor, "BottomFrontRightLeg", -newangle);
        rotateY(processor, "FrontRightFoot", newangle);
        rotateY(processor, "Toe3", newangle);
        rotateY(processor, "Toe2", newangle);
        rotateY(processor, "TopBackLeftLeg", -newangle);
        rotateX(processor, "BottomBackLeftLeg", -newangle);
        rotateY(processor, "BackLeftFoot", -newangle);
        rotateY(processor, "Toe7", -newangle);
        rotateY(processor, "Toe6", -newangle);
        rotateY(processor, "TopBackRightLeg", -newangle);
        rotateX(processor, "BottomBackRightLeg", newangle);
        rotateY(processor, "BackRightFoot", -newangle);
        rotateY(processor, "Toe4", -newangle);
        rotateY(processor, "Toe5", -newangle);
        float bottomJawXRot = entity.getAttacking() != 0 ? 0.52f + Mth.cos(ageInTicks * 0.45f) * 0.35f : 0.25f;
        rotateX(processor, "BottomJaw", bottomJawXRot);
        rotateX(processor, "Tooth9", bottomJawXRot);
        rotateX(processor, "Tooth15", bottomJawXRot);
        rotateX(processor, "Tooth14", bottomJawXRot);
        rotateX(processor, "Tooth13", bottomJawXRot);
        rotateX(processor, "Tooth16", bottomJawXRot);
        rotateX(processor, "Tooth12", bottomJawXRot);
        newangle = Mth.cos(ageInTicks * 0.25f * WINGSPEED) * (float) Math.PI * 0.05f;
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 1.25f * WINGSPEED) * (float) Math.PI * 0.35f;
        }
        float[] tailBase1 = bind(processor, "TailBase1");  // never written: the bind pivot
        float tailBase1YRot = newangle * 0.25f;
        rotateY(processor, "TailBase1", tailBase1YRot);
        float tail2Z = tailBase1[2] + (float) Math.cos(tailBase1YRot) * 12.0f;
        float tail2X = tailBase1[0] + (float) Math.sin(tailBase1YRot) * 12.0f;
        moveXZ(processor, "Tail2", tail2X, tail2Z);
        float tail2YRot = newangle * 0.5f;
        rotateY(processor, "Tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 9.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 9.0f;
        moveXZ(processor, "Tail3", tail3X, tail3Z);
        float tail3YRot = newangle * 0.75f;
        rotateY(processor, "Tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 7.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 7.0f;
        moveXZ(processor, "Tail4", tail4X, tail4Z);
        float tail4YRot = newangle * 1.0f;
        rotateY(processor, "Tail4", tail4YRot);
        float tailTipZ = tail4Z + (float) Math.cos(tail4YRot) * 7.0f;
        float tailTipX = tail4X + (float) Math.sin(tail4YRot) * 7.0f;
        moveXZ(processor, "TailTip", tailTipX, tailTipZ);
        rotateY(processor, "TailTip", newangle * 1.25f);
        float[] neck = bind(processor, "Neck");  // never written: the bind pivot
        float neckYRot = (float) Math.toRadians(netHeadYaw) * 0.25f;
        rotateY(processor, "Neck", neckYRot);
        float jawTopZ = neck[2] - (float) Math.cos(neckYRot) * 2.0f;
        float jawTopX = neck[0] - (float) Math.sin(neckYRot) * 2.0f;
        moveXZ(processor, "JawTop", jawTopX, jawTopZ);
        float jawTopYRot = (float) Math.toRadians(netHeadYaw) * 0.5f;
        rotateY(processor, "JawTop", jawTopYRot);
        for (String part : JAW_TOP_FOLLOWERS) {
            moveXZ(processor, part, jawTopX, jawTopZ);
            rotateY(processor, part, jawTopYRot);
        }
        moveXZ(processor, "RightEye", jawTopX, jawTopZ);
        rotateY(processor, "RightEye", jawTopYRot + 0.78f);
        moveXZ(processor, "LeftEye", jawTopX, jawTopZ);
        rotateY(processor, "LeftEye", jawTopYRot - 0.78f);
        float bottomJawZ = neck[2] - (float) Math.cos(neckYRot) * 3.0f;
        float bottomJawX = neck[0] - (float) Math.sin(neckYRot) * 3.0f;
        moveXZ(processor, "BottomJaw", bottomJawX, bottomJawZ);
        float bottomJawYRot = (float) Math.toRadians(netHeadYaw) * 0.5f;
        rotateY(processor, "BottomJaw", bottomJawYRot);
        for (String tooth : BOTTOM_JAW_TEETH) {
            moveXZ(processor, tooth, bottomJawX, bottomJawZ);
            rotateY(processor, tooth, bottomJawYRot);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Lizard, LizardGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new LizardGeoReplacement());
        }
    }
}
