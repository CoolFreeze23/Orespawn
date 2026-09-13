package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Godzilla;
import danger.orespawn.entity.pose.GodzillaPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Godzilla (the hooks, owner 2026-09-14, addendum item 10): {@link ModelGodzilla#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk}; the landing slice adds the geo, the wiring and the proofs). The port's classic model as
 * it is, {@code ANIM_SPEED} 1.0f (orig ClientProxyOreSpawn.java:462 passes {@code new ModelGodzilla(0.2f)}; the port's
 * model is what the hook transcribes): the THRESHOLD idiom on each leg ({@code limbSwingAmount > 0.001f} selects the
 * 0.75 cosines / sine, else 0) with the right leg four and five eighth-turns behind; the toes' POSITION writes (y lifted
 * {@code sin x 18 x amount} while the sine is positive, z {@code 6 + 35 x cos x amount}, through {@link #moveTo}) copied
 * to the nine toes of each foot and the lower leg, the upper leg FOLLOWING the lower by 55 units along its pitch, the
 * thigh's z a quarter of the toes' sweep; the eighteen toes' pitch reset to 0 every frame; the ATTACKING branch
 * ({@code getAttacking() != 0}): the tail's yaw fan (0.25 .. 2.25 x a 1.75 / 0.75 cosine, the eight tail parts
 * FOLLOWING one another by 25 / 20 / 20 / 25 / 27 / 28 / 18 units in x / z, the spikes copying their ring), the jaw's
 * 1.5 cosine over 0.52 rad, the arms' 1.75 x PI x 0.16 swing over the 0.1 x PI x 0.02 idle drift; the HEAD-LOOK idiom
 * (yaw {@code toRadians(netHeadYaw) x 0.55} on the head and both jaws, the lower jaw's pivot FOLLOWING the head's yaw
 * by 11 units, pitch {@code toRadians(headPitch)} on the head and the top jaw, the lower jaw adding it to its opening);
 * the two arm chains (upper arm / lower arm / hand about Y at 1x / 1.5x / 2x the swing, the lower arm and the hand
 * FOLLOWING by 50 / 45 units with 10-unit sine dips, the six finger parts riding the hand and curling at 1x / 2x / 3x).
 * The entity is read through {@link GodzillaPose} (the Slice 4b doctrine). Every value the classic reads back from a
 * part it just wrote is held in a local; the coordinates the classic never writes (the toes' and thighs' x, the head's,
 * the upper arms' and the tail base's pivots) are read through {@link #classicPosition} (the bind).
 *
 * <p>Scale and shadow follow {@link GodzillaRenderer}: 2.0 render scale, a quarter of it while {@code getPlayNicely()
 * != 0}, and a 1.0 x 2.0 shadow (ENT-S-092). The twelve back spikes are zero-thickness cubes (the seam draws every cube
 * with its true transformed normal and its two coplanar faces in the classic order, ENT-S-161 / TEST-007 - below).</p>
 */
public final class GodzillaGeoReplacement extends OreSpawnGeoReplacement<Godzilla> {
    /** The port's {@code ModelGodzilla.ANIM_SPEED} = 1.0f: the chain's frequency multiplier. */
    static final float ANIM_SPEED = 1.0F;
    private static final GeoReplacementDescriptor<Godzilla> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GODZILLA.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Godzilla.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/godzilla.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/godzilla.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/godzillatexture.png"),
            GodzillaRenderer.SHADOW) {
        @Override
        public void applyScale(Godzilla entity, PoseStack poseStack, float partialTick) {
            // orig RenderGodzilla.preRenderScale (:39-45): PlayNicely gets glScalef(scale / 4), otherwise glScalef(scale)
            // (GodzillaRenderer.scale)
            float effectiveScale = entity.getPlayNicely() != 0 ? GodzillaRenderer.SCALE / 4.0F : GodzillaRenderer.SCALE;
            poseStack.scale(effectiveScale, effectiveScale, effectiveScale);
        }

        /**
         * A rig with zero-thickness cubes (the twelve back spikes, 0 x 10..50 x 11..36): the within-cube face order
         * decides a flat cube's z-fight, so the shipped geo carries the classic order ({@link FaceOrder#KEY}; TEST-007)
         * and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    private static final String[] L_TOES = {"LToe1", "LToe2", "LToe3", "LToe4", "LToe5", "LToe6", "LToe7", "LToe8", "LToe9"};
    private static final String[] R_TOES = {"RToe1", "RToe2", "RToe3", "RToe4", "RToe5", "RToe6", "RToe7", "RToe8", "RToe9"};
    private static final String[] L_FINGERS = {"LThumbBase", "L3rdFingerBase", "LIndexBase", "LThumbTip", "L3rdFingerTip", "LIndexTip"};
    private static final String[] R_FINGERS = {"RThumbBase", "R3rdFingerBase", "RIndexBase", "RThumbTip", "R3rdFingerTip", "RIndexTip"};

    public GodzillaGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        GodzillaPose entity = inputs.subject(GodzillaPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelGodzilla.poseFrom verbatim.
        float pi4 = 0.7853982F;
        float clawZ = 6.0F;
        float clawY = 16.0F;
        float clawZamp = 35.0F;
        float clawYamp = 18.0F;

        float newangle;
        float newangle2;
        float t1;
        float t2;

        // ---- Left leg walking ----
        if (limbSwingAmount > 0.001F) {
            newangle = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED);
            newangle2 = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED + pi4);
            t1 = Mth.sin(ageInTicks * 0.75F * ANIM_SPEED);
        } else {
            newangle = 0.0F;
            newangle2 = 0.0F;
            t1 = 0.0F;
        }

        float lToe1Y;
        if (t1 > 0.0F) {
            t2 = t1 * clawYamp * limbSwingAmount;
            lToe1Y = clawY - t2;
        } else {
            lToe1Y = clawY;
        }
        float lToe1Z = clawZ + clawZamp * newangle * limbSwingAmount;
        // LToe2..9.z = LToe1.z; LToe2..9.y = LToe1.y (x never written: the bind)
        for (String toe : L_TOES) {
            moveYZ(processor, toe, lToe1Y, lToe1Z);
        }

        float lLowerLegZ = lToe1Z;
        float lLowerLegY = lToe1Y;
        moveYZ(processor, "LLowerLeg", lLowerLegY, lLowerLegZ);
        float lLowerLegXRot = 0.22F + newangle * (float) Math.PI * 0.09F * limbSwingAmount;
        rotateX(processor, "LLowerLeg", lLowerLegXRot);

        rotateX(processor, "LUpperLeg", -0.17F + newangle2 * (float) Math.PI * 0.15F * limbSwingAmount);
        float lUpperLegY = lLowerLegY - Mth.cos(lLowerLegXRot) * 55.0F;
        float lUpperLegZ = lLowerLegZ - Mth.sin(lLowerLegXRot) * 55.0F;
        moveYZ(processor, "LUpperLeg", lUpperLegY, lUpperLegZ);

        rotateX(processor, "LThigh", -0.558F + newangle2 * (float) Math.PI * 0.1F * limbSwingAmount);
        moveZ(processor, "LThigh", 2.0F + clawZamp * newangle * limbSwingAmount / 4.0F);

        // ---- Right leg walking (phase shifted by PI) ----
        if (limbSwingAmount > 0.001F) {
            newangle = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED + pi4 * 4.0F);
            newangle2 = Mth.cos(ageInTicks * 0.75F * ANIM_SPEED + pi4 * 5.0F);
            t1 = Mth.sin(ageInTicks * 0.75F * ANIM_SPEED + pi4 * 4.0F);
        } else {
            newangle = 0.0F;
            newangle2 = 0.0F;
            t1 = 0.0F;
        }

        float rToe1Y;
        if (t1 > 0.0F) {
            t2 = t1 * clawYamp * limbSwingAmount;
            rToe1Y = clawY - t2;
        } else {
            rToe1Y = clawY;
        }
        float rToe1Z = clawZ + clawZamp * newangle * limbSwingAmount;
        // RToe2..9.z = RToe1.z; RToe2..9.y = RToe1.y
        for (String toe : R_TOES) {
            moveYZ(processor, toe, rToe1Y, rToe1Z);
        }

        float rLegLowerZ = rToe1Z;
        float rLegLowerY = rToe1Y;
        moveYZ(processor, "RLegLower", rLegLowerY, rLegLowerZ);
        float rLegLowerXRot = 0.22F + newangle * (float) Math.PI * 0.09F * limbSwingAmount;
        rotateX(processor, "RLegLower", rLegLowerXRot);

        rotateX(processor, "RLegUpper", -0.17F + newangle2 * (float) Math.PI * 0.15F * limbSwingAmount);
        float rLegUpperY = rLegLowerY - Mth.cos(rLegLowerXRot) * 55.0F;
        float rLegUpperZ = rLegLowerZ - Mth.sin(rLegLowerXRot) * 55.0F;
        moveYZ(processor, "RLegUpper", rLegUpperY, rLegUpperZ);

        rotateX(processor, "RThigh", -0.558F + newangle2 * (float) Math.PI * 0.1F * limbSwingAmount);
        moveZ(processor, "RThigh", 2.0F + clawZamp * newangle * limbSwingAmount / 4.0F);

        // Reset all toe rotations
        for (String toe : L_TOES) {
            rotateX(processor, toe, 0.0F);
        }
        for (String toe : R_TOES) {
            rotateX(processor, toe, 0.0F);
        }

        // ---- Tail ----
        boolean attacking = entity.getAttacking() != 0;
        newangle = attacking
                ? Mth.cos(ageInTicks * ANIM_SPEED * 1.75F) * (float) Math.PI * 0.2F
                : Mth.cos(ageInTicks * ANIM_SPEED * 0.75F) * (float) Math.PI * 0.05F;
        doTail(processor, newangle);

        // ---- Head rotation ----
        newangle = (float) Math.toRadians(netHeadYaw) * 0.55F;
        float headYRot = newangle;
        rotateY(processor, "Head", headYRot);
        rotateY(processor, "TopJaw", newangle);
        rotateY(processor, "LowerJaw", newangle);
        float[] head = classicPosition(bone(processor, "Head"));  // never written: the bind pivot
        float lowerJawZ = head[2] - Mth.cos(headYRot) * 11.0F;
        float lowerJawX = head[0] - Mth.sin(headYRot) * 11.0F;
        moveXZ(processor, "LowerJaw", lowerJawX, lowerJawZ);
        float topJawXRot = (float) Math.toRadians(headPitch);   // TopJaw.xRot = Head.xRot = toRadians(headPitch)
        rotateX(processor, "Head", topJawXRot);
        rotateX(processor, "TopJaw", topJawXRot);

        // ---- Jaw animation ----
        float jawAngle = attacking
                ? Mth.cos(ageInTicks * ANIM_SPEED * 1.5F) * (float) Math.PI * 0.12F
                : 0.0F;
        rotateX(processor, "LowerJaw", 0.52F + jawAngle + topJawXRot);

        // ---- Arm animation ----
        float armAngle = attacking
                ? Mth.sin(ageInTicks * ANIM_SPEED * 1.75F) * (float) Math.PI * 0.16F
                : Mth.sin(ageInTicks * ANIM_SPEED * 0.1F) * (float) Math.PI * 0.02F;
        newangle = armAngle;
        newangle2 = armAngle;

        // Left arm chain
        float lUpperArmYRot = 0.65F + newangle;
        rotateY(processor, "LUpperArm", lUpperArmYRot);
        float lLowerArmYRot = 0.78F + newangle * 3.0F / 2.0F;
        rotateY(processor, "LLowerArm", lLowerArmYRot);
        float[] lUpperArm = classicPosition(bone(processor, "LUpperArm"));  // never written: the bind pivot
        float lLowerArmZ = lUpperArm[2] - Mth.sin(lUpperArmYRot) * 50.0F;
        float lLowerArmX = lUpperArm[0] + Mth.cos(lUpperArmYRot) * 50.0F;
        float lLowerArmY = lUpperArm[1] - Mth.sin(lUpperArmYRot) * 10.0F + 18.0F;
        moveTo(processor, "LLowerArm", lLowerArmX, lLowerArmY, lLowerArmZ);

        float lHandZ = lLowerArmZ - Mth.sin(lLowerArmYRot) * 45.0F;
        float lHandX = lLowerArmX + Mth.cos(lLowerArmYRot) * 45.0F;
        float lHandY = lLowerArmY - Mth.sin(lLowerArmYRot) * 10.0F + 15.0F;
        moveTo(processor, "LHand", lHandX, lHandY, lHandZ);

        // the six finger parts take the hand's z, y and x
        for (String finger : L_FINGERS) {
            moveTo(processor, finger, lHandX, lHandY, lHandZ);
        }

        rotateY(processor, "LHand", 1.308F + newangle * 2.0F);
        rotateY(processor, "LIndexBase", -0.139F + newangle * 2.0F);
        rotateY(processor, "LIndexTip", -0.034F + newangle * 2.0F);
        rotateY(processor, "LThumbBase", 0.261F + newangle);
        rotateY(processor, "LThumbTip", 0.139F + newangle);
        rotateY(processor, "L3rdFingerBase", -0.471F + newangle * 3.0F);
        rotateY(processor, "L3rdFingerTip", -0.331F + newangle * 3.0F);

        // Right arm chain
        float rUpperArmYRot = -0.65F - newangle2;
        rotateY(processor, "RUpperArm", rUpperArmYRot);
        float rLowerArmYRot = -0.78F - newangle2 * 3.0F / 2.0F;
        rotateY(processor, "RLowerArm", rLowerArmYRot);
        float[] rUpperArm = classicPosition(bone(processor, "RUpperArm"));  // never written: the bind pivot
        float rLowerArmZ = rUpperArm[2] + Mth.sin(rUpperArmYRot) * 50.0F;
        float rLowerArmX = rUpperArm[0] - Mth.cos(rUpperArmYRot) * 50.0F;
        float rLowerArmY = rUpperArm[1] + Mth.sin(rUpperArmYRot) * 10.0F + 18.0F;
        moveTo(processor, "RLowerArm", rLowerArmX, rLowerArmY, rLowerArmZ);

        float rHandZ = rLowerArmZ + Mth.sin(rLowerArmYRot) * 45.0F;
        float rHandX = rLowerArmX - Mth.cos(rLowerArmYRot) * 45.0F;
        float rHandY = rLowerArmY + Mth.sin(rLowerArmYRot) * 10.0F + 15.0F;
        moveTo(processor, "RHand", rHandX, rHandY, rHandZ);

        for (String finger : R_FINGERS) {
            moveTo(processor, finger, rHandX, rHandY, rHandZ);
        }

        rotateY(processor, "RHand", -2.0F - newangle2 * 2.0F);
        rotateY(processor, "RIndexBase", 0.157F - newangle2 * 2.0F);
        rotateY(processor, "RIndexTip", 0.174F - newangle2 * 2.0F);
        rotateY(processor, "RThumbBase", -0.104F - newangle2);
        rotateY(processor, "RThumbTip", 0.001F - newangle2);
        rotateY(processor, "R3rdFingerTip", 0.68F - newangle2 * 3.0F);
        rotateY(processor, "R3rdFingerBase", 0.645F - newangle2 * 3.0F);
    }

    /** ModelGodzilla.doTail verbatim: the yaw fan down the tail, each ring's pivot following the last along its yaw. */
    private static void doTail(AnimationProcessor<?> processor, float angle) {
        float tailBaseYRot = angle * 0.25F;
        rotateY(processor, "TailBase", tailBaseYRot);
        rotateY(processor, "Lspike5", tailBaseYRot);
        rotateY(processor, "Rspike5", tailBaseYRot);
        float[] tailBase = classicPosition(bone(processor, "TailBase"));  // never written: the bind pivot

        float tail2YRot = angle * 0.5F;
        rotateY(processor, "Tail2", tail2YRot);
        float tail2Z = tailBase[2] + Mth.cos(tailBaseYRot) * 25.0F;
        float tail2X = tailBase[0] + Mth.sin(tailBaseYRot) * 25.0F;
        moveXZ(processor, "Tail2", tail2X, tail2Z);

        rotateY(processor, "Spike6", tail2YRot);
        moveXZ(processor, "Spike6", tail2X, tail2Z);

        float tail3YRot = angle * 0.75F;
        rotateY(processor, "Tail3", tail3YRot);
        float tail3Z = tail2Z + Mth.cos(tail2YRot) * 20.0F;
        float tail3X = tail2X + Mth.sin(tail2YRot) * 20.0F;
        moveXZ(processor, "Tail3", tail3X, tail3Z);

        rotateY(processor, "Spikes7", tail3YRot);
        moveXZ(processor, "Spikes7", tail3X, tail3Z);

        float tail4YRot = angle * 1.25F;
        rotateY(processor, "Tail4", tail4YRot);
        float tail4Z = tail3Z + Mth.cos(tail3YRot) * 20.0F;
        float tail4X = tail3X + Mth.sin(tail3YRot) * 20.0F;
        moveXZ(processor, "Tail4", tail4X, tail4Z);

        float tail5YRot = angle * 1.5F;
        rotateY(processor, "Tail5", tail5YRot);
        float tail5Z = tail4Z + Mth.cos(tail4YRot) * 25.0F;
        float tail5X = tail4X + Mth.sin(tail4YRot) * 25.0F;
        moveXZ(processor, "Tail5", tail5X, tail5Z);

        float tail6YRot = angle * 1.75F;
        rotateY(processor, "Tail6", tail6YRot);
        float tail6Z = tail5Z + Mth.cos(tail5YRot) * 27.0F;
        float tail6X = tail5X + Mth.sin(tail5YRot) * 27.0F;
        moveXZ(processor, "Tail6", tail6X, tail6Z);

        float tail7YRot = angle * 2.0F;
        rotateY(processor, "Tail7", tail7YRot);
        float tail7Z = tail6Z + Mth.cos(tail6YRot) * 28.0F;
        float tail7X = tail6X + Mth.sin(tail6YRot) * 28.0F;
        moveXZ(processor, "Tail7", tail7X, tail7Z);

        rotateY(processor, "TailTip", angle * 2.25F);
        float tailTipZ = tail7Z + Mth.cos(tail7YRot) * 18.0F;
        float tailTipX = tail7X + Mth.sin(tail7YRot) * 18.0F;
        moveXZ(processor, "TailTip", tailTipX, tailTipZ);
    }

    /** {@code part.y = y; part.z = z} on a part whose x the classic never writes (the bind). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], y, z);
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    /** {@code part.z = z} on a part whose x and y the classic never writes (the bind). */
    private static void moveZ(AnimationProcessor<?> processor, String name, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], position[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Godzilla, GodzillaGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GodzillaGeoReplacement());
        }
    }
}
