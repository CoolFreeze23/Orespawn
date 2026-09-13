package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SeaViper;
import danger.orespawn.entity.pose.SeaViperPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib Sea Viper (the hooks, owner 2026-09-14, addendum item 10): {@link ModelSeaViper#poseFrom} verbatim on the
 * converted rig (ENT-S-091 slice C's line-for-line transcription of orig ModelSeaViper.java:295-402), ON THE HOOK (no
 * keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk};
 * the landing slice adds the geo, the wiring and the proofs). Wingspeed 0.5f (orig ModelSeaViper.java:14,51 /
 * ClientProxyOreSpawn.java:497): a negative walking speed clamped to 0 (orig :298-301); the base segment's yaw
 * {@code cos(age x 1.3 ws) x PI x 0.1 x amount} (orig :302) and the twenty-one segments chained off their predecessor
 * through {@link #doseg} (the classic helper, the same name; orig :394-402): each 9 units along the previous segment's
 * yaw foreshortened by {@code |cos(pitch)|} (POSITION writes through {@link #moveTo}; the segments' pitch is never
 * written - the bind, read through {@link #classicXRot}) and yawed on a travelling wave lagged {@code pi / 4} per index
 * blended toward the static S-curve as the swing amount drops (index 2 twice, as the original); the ATTACKING branch
 * (orig :324-346 {@code getAttacking() != 0}): the jaw chattering wide (0.65 + 1.7 ws x PI x 0.17) and the tongue's
 * four parts flicking on a 4.7 ws cosine with a 1.5 ws z offset, else the idle breath (0.45 + 0.2 ws x PI x 0.02, the
 * tongue at 1.7 ws with a 0.5 ws offset) - the offset the original wrote as {@code offsetZ} in block units, folded
 * into the pivot's z as the bind plus sixteen times it (orig :332-334 / :343-345, the port's fold); and the HEAD-LOOK
 * idiom (orig :347-357: yaw {@code toRadians(netHeadYaw) x 0.5} on the eyes, the mouth, the head, the fangs and the
 * tongue, the forks splayed -+0.436, the lower jaw's pivot FOLLOWING the head's yaw by 2 units). The entity is read
 * through {@link SeaViperPose} (the Slice 4b doctrine). Every value the classic reads back from a part it just wrote is
 * held in a local; the base segment's and the head's pivots, never written, are read through
 * {@link #classicPosition} (the bind).
 *
 * <p>Shadow follows {@link SeaViperRenderer}: a 1.0 x 1.0 shadow (ENT-S-092); its {@code SCALE} is 1.0 (identity,
 * never applied), so no scale hook. No zero-thickness cube.</p>
 */
public final class SeaViperGeoReplacement extends OreSpawnGeoReplacement<SeaViper> {
    /** orig ModelSeaViper.java:14,51 {@code wingspeed} = 0.5f (ClientProxyOreSpawn.java:497): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.5f;
    private static final GeoReplacementDescriptor<SeaViper> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.SEA_VIPER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            SeaViper.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/seaviper.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/seaviper.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/seaviper.png"),
            SeaViperRenderer.SHADOW) {
    };

    /** Indices into a segment's chained state ({@link #doseg}): the classic reads {@code inn.x}, {@code inn.z} and {@code inn.yRot} back. */
    private static final int X = 0;
    private static final int Z = 1;
    private static final int Y_ROT = 2;

    public SeaViperGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        SeaViperPose entity = inputs.subject(SeaViperPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelSeaViper.poseFrom verbatim. orig :298-301 - clamp negative limb swing to 0.
        float newangle = 0.0f;
        if (limbSwingAmount < 0.0f) {
            limbSwingAmount = 0.0f;
        }

        // orig :302 - base segment yaw: cos(t * 1.3 * wingspeed) * PI * 0.1, scaled by limbSwingAmount.
        float tBaseYRot = newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.1f * limbSwingAmount;
        rotateY(processor, "tBase", tBaseYRot);
        float[] tBasePosition = classicPosition(bone(processor, "tBase"));  // never written: the bind pivot
        float[] seg = {tBasePosition[0], tBasePosition[2], tBaseYRot};

        // orig :303-323 - chain every segment off the previous one (doseg), segment index 2..21.
        seg = doseg(processor, "tBase", seg, "t2", 2.0f, limbSwingAmount, ageInTicks);   // orig :303
        seg = doseg(processor, "t2", seg, "t3", 2.0f, limbSwingAmount, ageInTicks);      // orig :304 (index 2 again, as in the original)
        seg = doseg(processor, "t3", seg, "t4", 3.0f, limbSwingAmount, ageInTicks);      // orig :305
        seg = doseg(processor, "t4", seg, "t5", 4.0f, limbSwingAmount, ageInTicks);      // orig :306
        seg = doseg(processor, "t5", seg, "t6", 5.0f, limbSwingAmount, ageInTicks);      // orig :307
        seg = doseg(processor, "t6", seg, "t7", 6.0f, limbSwingAmount, ageInTicks);      // orig :308
        seg = doseg(processor, "t7", seg, "t8", 7.0f, limbSwingAmount, ageInTicks);      // orig :309
        seg = doseg(processor, "t8", seg, "t9", 8.0f, limbSwingAmount, ageInTicks);      // orig :310
        seg = doseg(processor, "t9", seg, "t10", 9.0f, limbSwingAmount, ageInTicks);     // orig :311
        seg = doseg(processor, "t10", seg, "t11", 10.0f, limbSwingAmount, ageInTicks);   // orig :312
        seg = doseg(processor, "t11", seg, "t12", 11.0f, limbSwingAmount, ageInTicks);   // orig :313
        seg = doseg(processor, "t12", seg, "t13", 12.0f, limbSwingAmount, ageInTicks);   // orig :314
        seg = doseg(processor, "t13", seg, "t14", 13.0f, limbSwingAmount, ageInTicks);   // orig :315
        seg = doseg(processor, "t14", seg, "t15", 14.0f, limbSwingAmount, ageInTicks);   // orig :316
        seg = doseg(processor, "t15", seg, "t16", 15.0f, limbSwingAmount, ageInTicks);   // orig :317
        seg = doseg(processor, "t16", seg, "t17", 16.0f, limbSwingAmount, ageInTicks);   // orig :318
        seg = doseg(processor, "t17", seg, "t18", 17.0f, limbSwingAmount, ageInTicks);   // orig :319
        seg = doseg(processor, "t18", seg, "t19", 18.0f, limbSwingAmount, ageInTicks);   // orig :320
        seg = doseg(processor, "t19", seg, "t20", 19.0f, limbSwingAmount, ageInTicks);   // orig :321
        seg = doseg(processor, "t20", seg, "t21", 20.0f, limbSwingAmount, ageInTicks);   // orig :322
        doseg(processor, "t21", seg, "TailTip", 21.0f, limbSwingAmount, ageInTicks);     // orig :323

        // orig :324-346 - jaw / tongue: attacking branch vs idle branch. The original's offsetZ (block units) folds
        // into the pivot's z as initialPose.z + offsetZ * 16 (the port's fold; tongueOffsetZ below).
        float tongueOffsetZ;
        if (entity.getAttacking() != 0) {                                     // orig :324
            // orig :325-326 - jaw chatters wide open.
            newangle = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.17f;
            rotateX(processor, "MouthBottom", 0.65f + newangle);
            // orig :327-331 - tongue flicks fast.
            newangle = Mth.cos(ageInTicks * 4.7f * WINGSPEED) * (float) Math.PI * 0.07f;
            rotateX(processor, "ToungBase", 0.261f + newangle);
            rotateX(processor, "MiddleTounge", 0.174f + newangle);
            rotateX(processor, "ForkLeft", 0.087f + newangle);
            rotateX(processor, "ForkRight", 0.087f + newangle);
            // orig :332-334 - ForkLeft.offsetZ = ForkRight.offsetZ = newangle; MiddleTounge / ToungBase copy it.
            tongueOffsetZ = newangle = Mth.cos(ageInTicks * 1.5f * WINGSPEED) * (float) Math.PI * 0.05f;
        } else {                                                              // orig :335
            // orig :336-337 - jaw nearly closed, slow breathing.
            newangle = Mth.cos(ageInTicks * 0.2f * WINGSPEED) * (float) Math.PI * 0.02f;
            rotateX(processor, "MouthBottom", 0.45f + newangle);
            // orig :338-342 - tongue flicks slowly.
            newangle = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.03f;
            rotateX(processor, "ToungBase", 0.261f + newangle);
            rotateX(processor, "MiddleTounge", 0.174f + newangle);
            rotateX(processor, "ForkLeft", 0.087f + newangle);
            rotateX(processor, "ForkRight", 0.087f + newangle);
            // orig :343-345 - same offsetZ fan-out at 0.5 Hz factor.
            tongueOffsetZ = newangle = Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.05f;
        }
        // orig :332-334 / :343-345 - offsetZ (blocks) folded into pivot z (pixels, f5 = 1/16 -> x16): the bind z plus it.
        moveZ(processor, "ForkLeft", classicBindZ(bone(processor, "ForkLeft")) + tongueOffsetZ * 16.0f);
        moveZ(processor, "ForkRight", classicBindZ(bone(processor, "ForkRight")) + tongueOffsetZ * 16.0f);
        moveZ(processor, "MiddleTounge", classicBindZ(bone(processor, "MiddleTounge")) + tongueOffsetZ * 16.0f);
        moveZ(processor, "ToungBase", classicBindZ(bone(processor, "ToungBase")) + tongueOffsetZ * 16.0f);

        // orig :347-351 - head group yaws at half the net head yaw.
        float eyeRightYRot = (newangle = (float) Math.toRadians(netHeadYaw) * 0.5f);   // EyeLeft.yRot = EyeRight.yRot = (newangle = ...)
        rotateY(processor, "EyeRight", eyeRightYRot);
        rotateY(processor, "EyeLeft", eyeRightYRot);
        rotateY(processor, "MouthTop", eyeRightYRot);
        float headYRot = eyeRightYRot;
        rotateY(processor, "Head", headYRot);
        rotateY(processor, "FangRight", newangle);   // FangLeft.yRot = FangRight.yRot = newangle
        rotateY(processor, "FangLeft", newangle);
        rotateY(processor, "MouthBottom", newangle);
        // orig :352-353 - lower-jaw pivot trails the head pivot by 2 px along the head yaw.
        float[] head = classicPosition(bone(processor, "Head"));  // never written: the bind pivot
        float mouthBottomZ = head[2] - (float) Math.cos(headYRot) * 2.0f;
        float mouthBottomX = head[0] - (float) Math.sin(headYRot) * 2.0f;
        moveXZ(processor, "MouthBottom", mouthBottomX, mouthBottomZ);
        // orig :354-357 - tongue follows head yaw; forks splay +/-0.436 rad.
        rotateY(processor, "ToungBase", newangle);
        rotateY(processor, "MiddleTounge", newangle);
        rotateY(processor, "ForkLeft", newangle - 0.436f);
        rotateY(processor, "ForkRight", newangle + 0.436f);
    }

    /**
     * ModelSeaViper.doseg verbatim (orig ModelSeaViper.java:394-402): segment {@code notinn} positioned and yawed off
     * its predecessor {@code inn}, whose x / z / yaw are the chained state (the base segment's pivot is the bind) and
     * whose pitch is never written (the bind, {@link #classicXRot}); returns {@code notinn}'s x / z / yaw for the next.
     */
    private static float[] doseg(AnimationProcessor<?> processor, String inn, float[] innState, String notinn,
                                 float f, float f1, float f2) {
        float pi4 = 0.7853982f;                                               // orig :395
        float newangle = 0.0f;                                                // orig :396
        float innXRot = classicXRot(bone(processor, inn));
        // orig :397-398 - 9 px along the previous segment's yaw, foreshortened by |cos(pitch)|.
        // Math.cos / casts kept verbatim (double math) so the result is bit-identical to the original.
        float notinnZ = (float) ((double) innState[Z] + (double) ((float) Math.cos(innState[Y_ROT])) * (9.0 * Math.abs(Math.cos(innXRot))));
        float notinnX = (float) ((double) innState[X] + (double) ((float) Math.sin(innState[Y_ROT]) * 9.0f) * Math.abs(Math.cos(innXRot)));
        moveXZ(processor, notinn, notinnX, notinnZ);
        // orig :399-401 - travelling wave lagged pi/4 per segment, scaled by limbSwingAmount;
        // blends toward the static S-curve cos(-pi4 * f) as the swing amount drops to 0.
        newangle = Mth.cos(f2 * 1.3f * WINGSPEED - pi4 * f) * (float) Math.PI * 0.2f * f1;
        float a = Mth.cos(-(pi4 * f));
        float notinnYRot = newangle + a - a * f1;
        rotateY(processor, notinn, notinnYRot);
        return new float[] {notinnX, notinnZ, notinnYRot};
    }

    /** {@code part.xRot} read back on a part whose pitch the classic never writes (the bind): the inverse of {@link #rotateX}'s mapping. */
    private static float classicXRot(GeoBone bone) {
        return -bone.getRotX();
    }

    /**
     * The bone's bind pivot z in classic terms - the classic's {@code getInitialPose().z} - for a top-level bone (every
     * bone of this rig): GeckoLib's pivot is absolute and never animated, so it is the bind whatever the frame wrote.
     */
    private static float classicBindZ(GeoBone bone) {
        return bone.getPivotZ();
    }

    /** {@code part.z = z} on a part whose x and y the classic never writes (the bind). */
    private static void moveZ(AnimationProcessor<?> processor, String name, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], position[1], z);
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<SeaViper, SeaViperGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new SeaViperGeoReplacement());
        }
    }
}
