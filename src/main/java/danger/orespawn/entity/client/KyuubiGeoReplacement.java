package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityKyuubi;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Kyuubi (the hook survey): {@link KyuubiModel#setupAnim} verbatim on the converted rig, ON THE HOOK
 * (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk}). Wingspeed 0.5f (orig ModelKyuubi.java:15,60 / ClientProxyOreSpawn.java:432): the THRESHOLD gait on the
 * legs ({@code cos(age * 1.1 ws) * PI * 0.2 * limbSwingAmount} above a walking speed of a tenth, the right leg
 * positive around 0.59 / -0.15, the left the negative around 0.26 / -0.44, each lower leg's z FOLLOWING the
 * upper's pitch by 8 units of sine - a POSITION write through {@link #moveTo}); the arms' gait-scaled 1.1 ws swing
 * at {@code PI * 0.08 * limbSwingAmount} plus an always-on 0.5 ws sway at {@code PI * 0.01}, the lower arms around 0.48
 * following by 8 units; the HEAD-LOOK idiom ({@code toRadians(netHeadYaw)}, the flesh head a further {@code 4 *
 * pi4}); the two five-part horn chains sprouting from the fire head's yaw +-pi4 at 3.6 units and following each other
 * (2 / 4 / 3 / 2 units), each ring's yaw the head's +-0.244 plus a 1.3 ws cosine a quarter turn behind the last; and
 * the nine-ring tail chain following itself (3 / 4 / 3.5 / 5 / 4 / 3 / 2 / 1 units) with a 0.9 ws yaw wave and a 0.5 ws
 * pitch wave around rising rests (-0.26 ... 2.0), each ring a quarter turn behind the last. Every part the fire
 * twin doubles (the {@code *Fire} parts) is written with the same values. No entity state.
 *
 * <p>Scale and shadow follow {@link KyuubiRenderer}: 1.0 render scale (identity - no scale override, so no scale hook)
 * and a 0.1 x 1.0 shadow (ENT-S-092).</p>
 */
public final class KyuubiGeoReplacement extends OreSpawnGeoReplacement<EntityKyuubi> {
    /** orig ModelKyuubi.java:15,60 {@code wingspeed} = 0.5f (ClientProxyOreSpawn.java:432): the chain's third multiply. */
    static final float WINGSPEED = 0.5F;
    private static final GeoReplacementDescriptor<EntityKyuubi> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_KYUUBI.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityKyuubi.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/kyuubi.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/kyuubi.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/kyuubi.png"),
            KyuubiRenderer.SHADOW) {
    };

    public KyuubiGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** A part's classic position the hook never writes (or whose written components are not read back): the bind pivot. */
    private static float[] bind(AnimationProcessor<?> processor, String name) {
        return classicPosition(bone(processor, name));
    }

    /** {@code part.z = z} with the part's x / y left at the bind (the classic writes z alone). */
    private static void moveZ(AnimationProcessor<?> processor, String name, float z) {
        float[] pos = bind(processor, name);
        moveTo(processor, name, pos[0], pos[1], z);
    }

    /** {@code part.z = z; part.x = x} with the part's y left at the bind (the horn rings). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] pos = bind(processor, name);
        moveTo(processor, name, x, pos[1], z);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // KyuubiModel.setupAnim verbatim, the float chain left to right; every value the classic reads back from a
        // part it just wrote is held in a local.
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.2f * limbSwingAmount : 0.0f;
        rotateX(processor, "rtLegUpper", 0.59f + newangle);
        float rtLegUpperFireXRot = 0.59f + newangle;
        rotateX(processor, "rtLegUpperFire", rtLegUpperFireXRot);
        rotateX(processor, "rtLegLower", -0.15f + newangle);
        rotateX(processor, "rtLegLowerFire", -0.15f + newangle);
        moveZ(processor, "rtLegLower", (float) (Math.sin(rtLegUpperFireXRot) * 8.0));
        moveZ(processor, "rtLegLowerFire", (float) (Math.sin(rtLegUpperFireXRot) * 8.0));
        rotateX(processor, "lfLegUpper", 0.26f - newangle);
        float lfLegUppperFireXRot = 0.26f - newangle;
        rotateX(processor, "lfLegUppperFire", lfLegUppperFireXRot);
        rotateX(processor, "lfLegLower", -0.44f - newangle);
        rotateX(processor, "lfLegLowerFire", -0.44f - newangle);
        moveZ(processor, "lfLegLower", (float) (Math.sin(lfLegUppperFireXRot) * 8.0));
        moveZ(processor, "lfLegLowerFire", (float) (Math.sin(lfLegUppperFireXRot) * 8.0));
        newangle = Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.08f * limbSwingAmount;
        rotateX(processor, "rtArmUpper", newangle += Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.01f);
        float rtArmUpperFireXRot = newangle;
        rotateX(processor, "rtArmUpperFire", rtArmUpperFireXRot);
        rotateX(processor, "rtArmLower", 0.48f + newangle);
        rotateX(processor, "rtArmLowerFire", 0.48f + newangle);
        moveZ(processor, "rtArmLower", (float) (Math.sin(rtArmUpperFireXRot) * 8.0));
        moveZ(processor, "rtArmLowerFire", (float) (Math.sin(rtArmUpperFireXRot) * 8.0));
        rotateX(processor, "lfArmUpper", -newangle);
        float lfArmUpperFireXRot = -newangle;
        rotateX(processor, "lfArmUpperFire", lfArmUpperFireXRot);
        rotateX(processor, "lfArmLower", 0.48f - newangle);
        rotateX(processor, "lfArmLowerFire", 0.48f - newangle);
        moveZ(processor, "lfArmLower", (float) (Math.sin(lfArmUpperFireXRot) * 8.0));
        moveZ(processor, "lfArmLowerFire", (float) (Math.sin(lfArmUpperFireXRot) * 8.0));
        float pi4 = 0.7853975f;
        rotateY(processor, "head", (float) Math.toRadians(netHeadYaw) + pi4 * 4.0f);
        float headFireYRot = (float) Math.toRadians(netHeadYaw);
        rotateY(processor, "headFire", headFireYRot);
        float[] headFire = bind(processor, "headFire");  // never written: the bind pivot
        float fc = (float) Math.cos(headFireYRot + pi4);
        float fs = (float) Math.sin(headFireYRot + pi4);
        float lfHorn1Z = headFire[2] - fc * 3.6f;
        float lfHorn1X = headFire[0] - fs * 3.6f;
        moveXZ(processor, "lfHorn1", lfHorn1X, lfHorn1Z);
        float lfHorn1YRot = headFireYRot + 0.244f + Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateY(processor, "lfHorn1", lfHorn1YRot);
        float lfHorn2Z = lfHorn1Z - (float) Math.cos(lfHorn1YRot) * 2.0f;
        float lfHorn2X = lfHorn1X - (float) Math.sin(lfHorn1YRot) * 2.0f;
        moveXZ(processor, "lfHorn2", lfHorn2X, lfHorn2Z);
        float lfHorn2YRot = headFireYRot + 0.244f + Mth.cos(ageInTicks * 1.3f * WINGSPEED - pi4) * (float) Math.PI * 0.1f;
        rotateY(processor, "lfHorn2", lfHorn2YRot);
        float lfHorn3Z = lfHorn2Z - (float) Math.cos(lfHorn2YRot) * 4.0f;
        float lfHorn3X = lfHorn2X - (float) Math.sin(lfHorn2YRot) * 4.0f;
        moveXZ(processor, "lfHorn3", lfHorn3X, lfHorn3Z);
        float lfHorn3YRot = headFireYRot + 0.244f + Mth.cos(ageInTicks * 1.3f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.1f;
        rotateY(processor, "lfHorn3", lfHorn3YRot);
        float lfHorn4Z = lfHorn3Z - (float) Math.cos(lfHorn3YRot) * 3.0f;
        float lfHorn4X = lfHorn3X - (float) Math.sin(lfHorn3YRot) * 3.0f;
        moveXZ(processor, "lfHorn4", lfHorn4X, lfHorn4Z);
        float lfHorn4YRot = headFireYRot + 0.244f + Mth.cos(ageInTicks * 1.3f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.1f;
        rotateY(processor, "lfHorn4", lfHorn4YRot);
        float lfHorn5Z = lfHorn4Z - (float) Math.cos(lfHorn4YRot) * 2.0f;
        float lfHorn5X = lfHorn4X - (float) Math.sin(lfHorn4YRot) * 2.0f;
        moveXZ(processor, "lfHorn5", lfHorn5X, lfHorn5Z);
        rotateY(processor, "lfHorn5", headFireYRot + 0.244f + Mth.cos(ageInTicks * 1.3f * WINGSPEED - 4.0f * pi4) * (float) Math.PI * 0.1f);
        fc = (float) Math.cos(headFireYRot - pi4);
        fs = (float) Math.sin(headFireYRot - pi4);
        float rtHorn1Z = headFire[2] - fc * 3.6f;
        float rtHorn1X = headFire[0] - fs * 3.6f;
        moveXZ(processor, "rtHorn1", rtHorn1X, rtHorn1Z);
        float rtHorn1YRot = headFireYRot + -0.244f - Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateY(processor, "rtHorn1", rtHorn1YRot);
        float rtHorn2Z = rtHorn1Z - (float) Math.cos(rtHorn1YRot) * 2.0f;
        float rtHorn2X = rtHorn1X - (float) Math.sin(rtHorn1YRot) * 2.0f;
        moveXZ(processor, "rtHorn2", rtHorn2X, rtHorn2Z);
        float rtHorn2YRot = headFireYRot + -0.244f - Mth.cos(ageInTicks * 1.3f * WINGSPEED - pi4) * (float) Math.PI * 0.1f;
        rotateY(processor, "rtHorn2", rtHorn2YRot);
        float rtHorn3Z = rtHorn2Z - (float) Math.cos(rtHorn2YRot) * 4.0f;
        float rtHorn3X = rtHorn2X - (float) Math.sin(rtHorn2YRot) * 4.0f;
        moveXZ(processor, "rtHorn3", rtHorn3X, rtHorn3Z);
        float rtHorn3YRot = headFireYRot + -0.244f - Mth.cos(ageInTicks * 1.3f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.1f;
        rotateY(processor, "rtHorn3", rtHorn3YRot);
        float rtHorn4Z = rtHorn3Z - (float) Math.cos(rtHorn3YRot) * 3.0f;
        float rtHorn4X = rtHorn3X - (float) Math.sin(rtHorn3YRot) * 3.0f;
        moveXZ(processor, "rtHorn4", rtHorn4X, rtHorn4Z);
        float rtHorn4YRot = headFireYRot + -0.244f - Mth.cos(ageInTicks * 1.3f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.1f;
        rotateY(processor, "rtHorn4", rtHorn4YRot);
        float rtHorn5Z = rtHorn4Z - (float) Math.cos(rtHorn4YRot) * 2.0f;
        float rtHorn5X = rtHorn4X - (float) Math.sin(rtHorn4YRot) * 2.0f;
        moveXZ(processor, "rtHorn5", rtHorn5X, rtHorn5Z);
        rotateY(processor, "rtHorn5", headFireYRot + -0.244f - Mth.cos(ageInTicks * 1.3f * WINGSPEED - 4.0f * pi4) * (float) Math.PI * 0.1f);
        // The tail: x from the yaw wave, then y / z from the pitch wave; one position write per ring once both are known.
        float[] tail1 = bind(processor, "tail1");  // never written: the bind pivot
        float tail1YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail1", tail1YRot);
        float tail2X = tail1[0] - (float) Math.sin(tail1YRot) * 3.0f;
        float tail2YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail2", tail2YRot);
        float tail3X = tail2X - (float) Math.sin(tail2YRot) * 4.0f;
        float tail3YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail3", tail3YRot);
        float tail4X = tail3X - (float) Math.sin(tail3YRot) * 3.5f;
        float tail4YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail4", tail4YRot);
        float tail5X = tail4X - (float) Math.sin(tail4YRot) * 5.0f;
        float tail5YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - 4.0f * pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail5", tail5YRot);
        float tail6X = tail5X - (float) Math.sin(tail5YRot) * 4.0f;
        float tail6YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - 5.0f * pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail6", tail6YRot);
        float tail7X = tail6X - (float) Math.sin(tail6YRot) * 3.0f;
        float tail7YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - 6.0f * pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail7", tail7YRot);
        float tail8X = tail7X - (float) Math.sin(tail7YRot) * 2.0f;
        float tail8YRot = Mth.cos(ageInTicks * 0.9f * WINGSPEED - 7.0f * pi4) * (float) Math.PI * 0.2f;
        rotateY(processor, "tail8", tail8YRot);
        float tail9X = tail8X - (float) Math.sin(tail8YRot) * 1.0f;
        rotateY(processor, "tail9", Mth.cos(ageInTicks * 0.9f * WINGSPEED - 8.0f * pi4) * (float) Math.PI * 0.2f);
        float tail1XRot = -0.26f + Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail1", tail1XRot);
        float tail2Y = tail1[1] + (float) Math.sin(tail1XRot) * 3.0f;
        float tail2Z = tail1[2] - (float) Math.cos(tail1XRot) * 3.0f;
        float tail2XRot = -0.78f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail2", tail2XRot);
        float tail3Y = tail2Y + (float) Math.sin(tail2XRot) * 4.0f;
        float tail3Z = tail2Z - (float) Math.cos(tail2XRot) * 4.0f;
        float tail3XRot = -1.11f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail3", tail3XRot);
        float tail4Y = tail3Y + (float) Math.sin(tail3XRot) * 3.5f;
        float tail4Z = tail3Z - (float) Math.cos(tail3XRot) * 3.5f;
        float tail4XRot = -0.18f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail4", tail4XRot);
        float tail5Y = tail4Y + (float) Math.sin(tail4XRot) * 5.0f;
        float tail5Z = tail4Z - (float) Math.cos(tail4XRot) * 5.0f;
        float tail5XRot = 0.22f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 4.0f * pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail5", tail5XRot);
        float tail6Y = tail5Y + (float) Math.sin(tail5XRot) * 4.0f;
        float tail6Z = tail5Z - (float) Math.cos(tail5XRot) * 4.0f;
        float tail6XRot = 0.63f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 5.0f * pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail6", tail6XRot);
        float tail7Y = tail6Y + (float) Math.sin(tail6XRot) * 3.0f;
        float tail7Z = tail6Z - (float) Math.cos(tail6XRot) * 3.0f;
        float tail7XRot = 0.89f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 6.0f * pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail7", tail7XRot);
        float tail8Y = tail7Y + (float) Math.sin(tail7XRot) * 2.0f;
        float tail8Z = tail7Z - (float) Math.cos(tail7XRot) * 2.0f;
        float tail8XRot = 1.52f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 7.0f * pi4) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail8", tail8XRot);
        float tail9Y = tail8Y + (float) Math.sin(tail8XRot) * 2.0f;
        float tail9Z = tail8Z - (float) Math.cos(tail8XRot) * 2.0f;
        rotateX(processor, "tail9", 2.0f + Mth.cos(ageInTicks * 0.5f * WINGSPEED - 8.0f * pi4) * (float) Math.PI * 0.1f);
        moveTo(processor, "tail2", tail2X, tail2Y, tail2Z);
        moveTo(processor, "tail3", tail3X, tail3Y, tail3Z);
        moveTo(processor, "tail4", tail4X, tail4Y, tail4Z);
        moveTo(processor, "tail5", tail5X, tail5Y, tail5Z);
        moveTo(processor, "tail6", tail6X, tail6Y, tail6Z);
        moveTo(processor, "tail7", tail7X, tail7Y, tail7Z);
        moveTo(processor, "tail8", tail8X, tail8Y, tail8Z);
        moveTo(processor, "tail9", tail9X, tail9Y, tail9Z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityKyuubi, KyuubiGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new KyuubiGeoReplacement());
        }
    }
}
