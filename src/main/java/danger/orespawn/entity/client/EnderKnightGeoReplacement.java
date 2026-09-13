package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnderKnight;
import danger.orespawn.entity.pose.EnderKnightPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ender Knight (the hooks, owner 2026-09-14, addendum item 10): {@link ModelEnderKnight#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays closed
 * until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.21f (orig ModelEnderKnight.java:54,57 /
 * ClientProxyOreSpawn.java:473): the THRESHOLD idiom on the fourteen leg / foot parts about X - above a walking speed of a
 * tenth {@code cos(age * 1.3f * ws) * PI * 0.25f * limbSwingAmount} around 0 / 0.6 / -0.1 rad, the right side the
 * negative, 0 at or below it (orig :408-425) - with the cape rolling on a quarter of it and pitching on a 0.7 ws cosine x
 * 0.02 (orig :426-427); the HEAD-LOOK idiom (orig :428-434): yaw = {@code toRadians(netHeadYaw) * 0.45f} clamped to
 * +-0.45 rad (no pitch); the SCREAMING branch (orig :331-352, read through {@link EnderKnightPose}): screaming, the six
 * arm parts swing on a 2.7 ws cosine x 0.3 around -1.2 / -1.8 rad and the blade / handle on 3/2 of it around 0.5; else the
 * arms hold their guard (-0.5 / -1.0, the shoulders yawed +-1.0, rolled 0) and the blade / handle 0.35; and the
 * POSITION-write idiom (through {@link #moveTo}) - each forearm's pivot FOLLOWS its upper arm 10 units along (cos, sin) of
 * the upper arm's pitch and the blade / handle the right forearm 7 units on, lifted 1 (orig :353-358, double arithmetic as
 * the classic casts it). The upper arms' pivots are never written (the bind), read through {@link #classicPosition}; every
 * value the classic reads back from a part it just wrote is held in a local; the followers keep their bind x. Screaming,
 * the classic leaves the shoulders' yaw / roll where the last frame left them (a singleton-model latch); the hook leaves
 * those channels at bind.
 *
 * <p>Shadow follows {@link EnderKnightRenderer}: a 0.3 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0, so no
 * scale hook. The rig has a zero-thickness cube (the cape), so the shipped geo carries the classic within-cube face order
 * ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.</p>
 */
public final class EnderKnightGeoReplacement extends OreSpawnGeoReplacement<EnderKnight> {
    /** orig ModelEnderKnight.java:54,57 {@code wingspeed} = 0.21f (ClientProxyOreSpawn.java:473): the chain's third multiply. */
    static final float WINGSPEED = 0.21F;
    private static final GeoReplacementDescriptor<EnderKnight> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENDER_KNIGHT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EnderKnight.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/enderknight.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/enderknight.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/enderknight.png"),
            EnderKnightRenderer.SHADOW) {
        /** One zero-thickness cube (cape2, 9 x 24 x 0): the seam expects {@link FaceOrder#KEY} in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public EnderKnightGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        EnderKnightPose entity = inputs.subject(EnderKnightPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelEnderKnight.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain as the classic casts it.
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateX(processor, "lfoot1", newangle);
        rotateX(processor, "lfoot2", 0.6f + newangle);
        rotateX(processor, "lfoot3", newangle);
        rotateX(processor, "lfoot4", newangle);
        rotateX(processor, "lleg1", newangle);
        rotateX(processor, "lleg2", -0.1f + newangle);
        rotateX(processor, "lleg3", -0.1f + newangle);
        rotateX(processor, "rfoot1", -newangle);
        rotateX(processor, "rfoot2", 0.6f - newangle);
        rotateX(processor, "rfoot3", -newangle);
        rotateX(processor, "rfoot4", -newangle);
        rotateX(processor, "rleg1", -newangle);
        rotateX(processor, "rleg2", -0.1f - newangle);
        rotateX(processor, "rleg3", -0.1f - newangle);
        rotateZ(processor, "cape2", newangle / 4.0f);
        rotateX(processor, "cape2", newangle = Mth.cos((float) (ageInTicks * 0.7f * WINGSPEED)) * (float) Math.PI * 0.02f);
        float headYRot = (float) Math.toRadians(netHeadYaw) * 0.45f;
        if (headYRot > 0.45f) {
            headYRot = 0.45f;
        }
        if (headYRot < -0.45f) {
            headYRot = -0.45f;
        }
        rotateY(processor, "head", headYRot);
        newangle = Mth.cos((float) (ageInTicks * 2.7f * WINGSPEED)) * (float) Math.PI * 0.3f;
        float larm2XRot;
        float larm1XRot;
        float rarm2XRot;
        float rarm1XRot;
        float handleXRot;
        if (entity.isScreaming()) {
            larm2XRot = -1.2f + newangle;
            rotateX(processor, "larm2", larm2XRot);
            rotateX(processor, "larm3", -1.2f + newangle);
            rarm2XRot = -1.2f + newangle;
            rotateX(processor, "rarm2", rarm2XRot);
            rotateX(processor, "rarm3", -1.2f + newangle);
            larm1XRot = -1.8f + newangle;
            rotateX(processor, "larm1", larm1XRot);
            rarm1XRot = -1.8f + newangle;
            rotateX(processor, "rarm1", rarm1XRot);
            handleXRot = 0.5f + newangle * 3.0f / 2.0f;
            rotateX(processor, "handle", handleXRot);
            rotateX(processor, "blade", handleXRot);
        } else {
            larm2XRot = -0.5f;
            rotateX(processor, "larm2", larm2XRot);
            rotateX(processor, "larm3", -0.5f);
            rotateZ(processor, "larm1", 0.0f);
            rotateY(processor, "larm1", 1.0f);
            larm1XRot = -1.0f;
            rotateX(processor, "larm1", larm1XRot);
            rarm2XRot = -0.5f;
            rotateX(processor, "rarm2", rarm2XRot);
            rotateX(processor, "rarm3", -0.5f);
            rotateZ(processor, "rarm1", 0.0f);
            rotateY(processor, "rarm1", -1.0f);
            rarm1XRot = -1.0f;
            rotateX(processor, "rarm1", rarm1XRot);
            handleXRot = 0.35f;
            rotateX(processor, "handle", handleXRot);
            rotateX(processor, "blade", 0.35f);
        }
        float[] larm2 = classicPosition(bone(processor, "larm2"));  // never written: the bind pivot
        float larm1Y = (float) ((double) larm2[1] + Math.cos(larm2XRot) * 10.0);
        float larm1Z = (float) ((double) larm2[2] + Math.sin(larm2XRot) * 10.0);
        moveYZ(processor, "larm1", larm1Y, larm1Z);
        float[] rarm2 = classicPosition(bone(processor, "rarm2"));  // never written: the bind pivot
        float rarm1Y = (float) ((double) rarm2[1] + Math.cos(rarm2XRot) * 10.0);
        float rarm1Z = (float) ((double) rarm2[2] + Math.sin(rarm2XRot) * 10.0);
        moveYZ(processor, "rarm1", rarm1Y, rarm1Z);
        float handleY = (float) ((double) rarm1Y + Math.cos(rarm1XRot) * 7.0) + 1.0f;
        float handleZ = (float) ((double) rarm1Z + Math.sin(rarm1XRot) * 7.0);
        moveYZ(processor, "handle", handleY, handleZ);
        moveYZ(processor, "blade", handleY, handleZ);
    }

    /** {@code part.y = y; part.z = z} in classic terms: the classic writes y and z only, so x keeps the part's bind. */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EnderKnight, EnderKnightGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new EnderKnightGeoReplacement());
        }
    }
}
