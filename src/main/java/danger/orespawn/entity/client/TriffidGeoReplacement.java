package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTriffid;
import danger.orespawn.entity.pose.TriffidPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Triffid (the hooks, landed by the sixth Tier-2 slice T2f): {@link TriffidModel#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). Wingspeed 1.0f (orig ModelTriffid.java:15,196 /
 * ClientProxyOreSpawn.java:459): the OPEN / CLOSED branch (orig :1275 {@code getOpenClosed() == 0} : the fold
 * 0.122522116 rad per link closed, {@code cos(age x 0.25 x ws) x PI x 0.039} open) driving four leaf chains
 * through the four helpers transcribed below (the same names, each a link's rotation as its parent's plus the angle
 * and its pivot FOLLOWING the parent {@code j} units along (sin, cos) of the parent's rotation - the POSITION-write
 * idiom, two coordinates per chain through {@link #moveXY} / {@link #moveYZ} ): l1-l15 (Z, about -0.95, the
 * root's pivot from the angle), l31-l43 (Z, about 0.95, the negative angle), l16-l30 (X, about -0.75, the negative
 * angle) and l44-l57 (X, about 0.75; l44's pivot is never written - the bind, read through {@link #classicPosition}
 * ); the ATTACKING branch on the fifteen-link tentacle (orig :1342 {@code getAttacking() != 0} : {@code |cos(age x
 * 0.25 x ws) x PI x 0.5|} lashing, else a right angle held), the links about Z alternating {@code -+angle - 0.6}
 * from t15 down to t3, each pivot following the link above 6 / 3 units along (cos, sin) of that link's roll (t15's
 * pivot is never written - the bind), t1 and t2 riding t3, and the thirteen links' yaw zeroed; every value the
 * classic reads back from a part it just wrote is held in a local. The entity is read through {@link TriffidPose} (the
 * Slice 4b form). Eleven leaf tips (c1-c11, never written by the hook) are zero-thickness cubes (0 x 5 x 2; ENT-S-161;
 * the classic face order required below; the shipped geo's count, T2f).
 *
 * <p>Shadow follows {@link TriffidRenderer} : a 0.3 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0 with
 * no scale override, so no scale hook.</p>
 */
public final class TriffidGeoReplacement extends OreSpawnGeoReplacement<EntityTriffid> {
    /** orig ModelTriffid.java:15,196 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:459): the chain's third multiply. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<EntityTriffid> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_TRIFFID.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityTriffid.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/triffid.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/triffid.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/triffid.png"),
            TriffidRenderer.SHADOW) {
        /**
         * A rig with zero-thickness cubes (the eleven leaf tips c1-c11, 0 x 5 x 2): the shipped geo carries the classic
         * within-cube face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public TriffidGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        TriffidPose entity = inputs.subject(TriffidPose.class);
        float ageInTicks = inputs.ageInTicks();
        // TriffidModel.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain exactly as the
        // classic casts it; a chain's state (rotation, and the two written coordinates) rides in a float triple from
        // link to link, as the classic reads it back from the part it just wrote.
        float newangle = 0.0f;
        float delta = 0.0f;
        newangle = entity.getOpenClosed() == 0 ? 0.122522116f : Mth.cos(ageInTicks * 0.25f * WINGSPEED) * (float) Math.PI * 0.039f;
        float l1ZRot = -0.95f + newangle;
        rotateZ(processor, "l1", l1ZRot);
        float l1Y = (float) (10.0 - Math.cos(l1ZRot) * 5.0) + 3.0f;
        float l1X = (float) (-7.0 + Math.sin(l1ZRot) * 5.0) + 3.0f;
        moveXY(processor, "l1", l1X, l1Y);
        float[] leaf = {l1ZRot, l1Y, l1X};
        leaf = leafpartA(processor, newangle, leaf, "l2", 3);
        leaf = leafpartA(processor, newangle, leaf, "l3", 4);
        leaf = leafpartA(processor, newangle, leaf, "leaf3", 4);
        leaf = leafpartA(processor, newangle, leaf, "l4", 4);
        leaf = leafpartA(processor, newangle, leaf, "l5", 4);
        leaf = leafpartA(processor, newangle, leaf, "l6", 6);
        leaf = leafpartA(processor, newangle, leaf, "l7", 4);
        leaf = leafpartA(processor, newangle, leaf, "l8", 4);
        leaf = leafpartA(processor, newangle, leaf, "l9", 4);
        leaf = leafpartA(processor, newangle, leaf, "l10", 4);
        leaf = leafpartA(processor, newangle, leaf, "l11", 3);
        leaf = leafpartA(processor, newangle, leaf, "l12", 3);
        leaf = leafpartA(processor, newangle, leaf, "l13", 2);
        leaf = leafpartA(processor, newangle, leaf, "l14", 2);
        leaf = leafpartA(processor, newangle, leaf, "l15", 2);
        float l31ZRot = 0.95f - newangle;
        rotateZ(processor, "l31", l31ZRot);
        float l31Y = (float) (10.0 - Math.cos(l31ZRot) * 5.0) + 3.0f;
        float l31X = (float) (7.0 + Math.sin(l31ZRot) * 5.0) - 3.0f;
        moveXY(processor, "l31", l31X, l31Y);
        leaf = new float[] {l31ZRot, l31Y, l31X};
        leaf = leafpartC(processor, -newangle, leaf, "l32", 3);
        leaf = leafpartC(processor, -newangle, leaf, "leaf32", 3);
        leaf = leafpartC(processor, -newangle, leaf, "l33", 3);
        leaf = leafpartC(processor, -newangle, leaf, "l34", 4);
        leaf = leafpartC(processor, -newangle, leaf, "l35", 4);
        leaf = leafpartC(processor, -newangle, leaf, "l36", 5);
        leaf = leafpartC(processor, -newangle, leaf, "l37", 4);
        leaf = leafpartC(processor, -newangle, leaf, "l38", 4);
        leaf = leafpartC(processor, -newangle, leaf, "l39", 4);
        leaf = leafpartC(processor, -newangle, leaf, "l40", 3);
        leaf = leafpartC(processor, -newangle, leaf, "l41", 3);
        leaf = leafpartC(processor, -newangle, leaf, "l42", 2);
        leaf = leafpartC(processor, -newangle, leaf, "l43", 1);
        float l16XRot = -0.75f - newangle;
        rotateX(processor, "l16", l16XRot);
        float l16Y = (float) (10.0 + Math.cos(l16XRot) * 5.0);
        float l16Z = (float) (-9.0 - Math.sin(l16XRot) * 5.0) - 3.0f;
        moveYZ(processor, "l16", l16Y, l16Z);
        leaf = new float[] {l16XRot, l16Y, l16Z};
        leaf = leafpartB(processor, -newangle, leaf, "l17", 3);
        leaf = leafpartB(processor, -newangle, leaf, "l18", 3);
        leaf = leafpartB(processor, -newangle, leaf, "l19", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l20", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l21", 5);
        leaf = leafpartB(processor, -newangle, leaf, "l22", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l23", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l24", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l25", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l26", 4);
        leaf = leafpartB(processor, -newangle, leaf, "l27", 3);
        leaf = leafpartB(processor, -newangle, leaf, "l28", 2);
        leaf = leafpartB(processor, -newangle, leaf, "l29", 2);
        leaf = leafpartB(processor, -newangle, leaf, "l30", 2);
        float l44XRot = 0.75f + newangle;
        rotateX(processor, "l44", l44XRot);
        float[] l44 = classicPosition(bone(processor, "l44"));  // never written: the bind pivot
        leaf = new float[] {l44XRot, l44[1], l44[2]};
        leaf = leafpartD(processor, newangle, leaf, "l45", 5);
        leaf = leafpartD(processor, newangle, leaf, "l46", 4);
        leaf = leafpartD(processor, newangle, leaf, "l47", 3);
        leaf = leafpartD(processor, newangle, leaf, "l48", 4);
        leaf = leafpartD(processor, newangle, leaf, "l49", 3);
        leaf = leafpartD(processor, newangle, leaf, "leaf49", 5);
        leaf = leafpartD(processor, newangle, leaf, "l50", 3);
        leaf = leafpartD(processor, newangle, leaf, "l51", 3);
        leaf = leafpartD(processor, newangle, leaf, "l52", 3);
        leaf = leafpartD(processor, newangle, leaf, "l53", 3);
        leaf = leafpartD(processor, newangle, leaf, "l54", 3);
        leaf = leafpartD(processor, newangle, leaf, "l55", 2);
        leaf = leafpartD(processor, newangle, leaf, "l56", 2);
        leaf = leafpartD(processor, newangle, leaf, "l57", 2);
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 0.25f * WINGSPEED) * (float) Math.PI * 0.5f;
            newangle = Math.abs(newangle);
        } else {
            newangle = 1.5707964f;
        }
        delta = -0.6f;
        float t15ZRot = -newangle + delta;
        rotateZ(processor, "t15", t15ZRot);
        float t14ZRot = newangle + delta;
        rotateZ(processor, "t14", t14ZRot);
        float[] t15 = classicPosition(bone(processor, "t15"));  // never written: the bind pivot
        float t14Y = (float) ((double) t15[1] - Math.sin(t15ZRot) * 6.0);
        float t14X = (float) ((double) t15[0] - Math.cos(t15ZRot) * 6.0);
        moveXY(processor, "t14", t14X, t14Y);
        float t13ZRot = -newangle + delta;
        rotateZ(processor, "t13", t13ZRot);
        float t13Y = (float) ((double) t14Y - Math.sin(t14ZRot) * 3.0);
        float t13X = (float) ((double) t14X - Math.cos(t14ZRot) * 3.0);
        moveXY(processor, "t13", t13X, t13Y);
        float t12ZRot = newangle + delta;
        rotateZ(processor, "t12", t12ZRot);
        float t12Y = (float) ((double) t13Y - Math.sin(t13ZRot) * 3.0);
        float t12X = (float) ((double) t13X - Math.cos(t13ZRot) * 3.0);
        moveXY(processor, "t12", t12X, t12Y);
        float t11ZRot = -newangle + delta;
        rotateZ(processor, "t11", t11ZRot);
        float t11Y = (float) ((double) t12Y - Math.sin(t12ZRot) * 3.0);
        float t11X = (float) ((double) t12X - Math.cos(t12ZRot) * 3.0);
        moveXY(processor, "t11", t11X, t11Y);
        float t10ZRot = newangle + delta;
        rotateZ(processor, "t10", t10ZRot);
        float t10Y = (float) ((double) t11Y - Math.sin(t11ZRot) * 3.0);
        float t10X = (float) ((double) t11X - Math.cos(t11ZRot) * 3.0);
        moveXY(processor, "t10", t10X, t10Y);
        float t9ZRot = -newangle + delta;
        rotateZ(processor, "t9", t9ZRot);
        float t9Y = (float) ((double) t10Y - Math.sin(t10ZRot) * 3.0);
        float t9X = (float) ((double) t10X - Math.cos(t10ZRot) * 3.0);
        moveXY(processor, "t9", t9X, t9Y);
        float t8ZRot = newangle + delta;
        rotateZ(processor, "t8", t8ZRot);
        float t8Y = (float) ((double) t9Y - Math.sin(t9ZRot) * 6.0);
        float t8X = (float) ((double) t9X - Math.cos(t9ZRot) * 6.0);
        moveXY(processor, "t8", t8X, t8Y);
        float t7ZRot = -newangle + delta;
        rotateZ(processor, "t7", t7ZRot);
        float t7Y = (float) ((double) t8Y - Math.sin(t8ZRot) * 6.0);
        float t7X = (float) ((double) t8X - Math.cos(t8ZRot) * 6.0);
        moveXY(processor, "t7", t7X, t7Y);
        float t6ZRot = newangle + delta;
        rotateZ(processor, "t6", t6ZRot);
        float t6Y = (float) ((double) t7Y - Math.sin(t7ZRot) * 6.0);
        float t6X = (float) ((double) t7X - Math.cos(t7ZRot) * 6.0);
        moveXY(processor, "t6", t6X, t6Y);
        float t5ZRot = -newangle + delta;
        rotateZ(processor, "t5", t5ZRot);
        float t5Y = (float) ((double) t6Y - Math.sin(t6ZRot) * 6.0);
        float t5X = (float) ((double) t6X - Math.cos(t6ZRot) * 6.0);
        moveXY(processor, "t5", t5X, t5Y);
        float t4ZRot = newangle + delta;
        rotateZ(processor, "t4", t4ZRot);
        float t4Y = (float) ((double) t5Y - Math.sin(t5ZRot) * 6.0);
        float t4X = (float) ((double) t5X - Math.cos(t5ZRot) * 6.0);
        moveXY(processor, "t4", t4X, t4Y);
        float t3ZRot = -newangle + delta;
        rotateZ(processor, "t3", t3ZRot);
        float t3Y = (float) ((double) t4Y - Math.sin(t4ZRot) * 6.0);
        float t3X = (float) ((double) t4X - Math.cos(t4ZRot) * 6.0);
        moveXY(processor, "t3", t3X, t3Y);
        float t1Y = (float) ((double) t3Y - Math.sin(t3ZRot) * 3.0);
        float t1X = (float) ((double) t3X - Math.cos(t3ZRot) * 3.0);
        moveXY(processor, "t1", t1X, t1Y);
        moveXY(processor, "t2", t1X, t1Y);
        newangle = 0.0f;
        rotateY(processor, "t3", newangle);
        rotateY(processor, "t4", newangle);
        rotateY(processor, "t5", newangle);
        rotateY(processor, "t6", newangle);
        rotateY(processor, "t7", newangle);
        rotateY(processor, "t8", newangle);
        rotateY(processor, "t9", newangle);
        rotateY(processor, "t10", newangle);
        rotateY(processor, "t11", newangle);
        rotateY(processor, "t12", newangle);
        rotateY(processor, "t13", newangle);
        rotateY(processor, "t14", newangle);
        rotateY(processor, "t15", newangle);
    }

    /** TriffidModel.leafpartA verbatim: {@code l2} from the parent state {@code l1} = (zRot, y, x); returns l2's. */
    private static float[] leafpartA(AnimationProcessor<?> processor, float newangle, float[] l1, String l2, int j) {
        float zRot = l1[0] + newangle;
        rotateZ(processor, l2, zRot);
        float y = (float) ((double) l1[1] - Math.cos(l1[0]) * (double) j);
        float x = (float) ((double) l1[2] + Math.sin(l1[0]) * (double) j);
        moveXY(processor, l2, x, y);
        return new float[] {zRot, y, x};
    }

    /** TriffidModel.leafpartB verbatim: {@code l2} from the parent state {@code l1} = (xRot, y, z); returns l2's. */
    private static float[] leafpartB(AnimationProcessor<?> processor, float newangle, float[] l1, String l2, int j) {
        float xRot = l1[0] + newangle;
        rotateX(processor, l2, xRot);
        float y = (float) ((double) l1[1] + Math.sin(l1[0]) * (double) j);
        float z = (float) ((double) l1[2] - Math.cos(l1[0]) * (double) j);
        moveYZ(processor, l2, y, z);
        return new float[] {xRot, y, z};
    }

    /** TriffidModel.leafpartC verbatim: {@code l2} from the parent state {@code l1} = (zRot, y, x); returns l2's. */
    private static float[] leafpartC(AnimationProcessor<?> processor, float newangle, float[] l1, String l2, int j) {
        float zRot = l1[0] + newangle;
        rotateZ(processor, l2, zRot);
        float y = (float) ((double) l1[1] - Math.cos(l1[0]) * (double) j);
        float x = (float) ((double) l1[2] + Math.sin(l1[0]) * (double) j);
        moveXY(processor, l2, x, y);
        return new float[] {zRot, y, x};
    }

    /** TriffidModel.leafpartD verbatim: {@code l2} from the parent state {@code l1} = (xRot, y, z); returns l2's. */
    private static float[] leafpartD(AnimationProcessor<?> processor, float newangle, float[] l1, String l2, int j) {
        float xRot = l1[0] + newangle;
        rotateX(processor, l2, xRot);
        float y = (float) ((double) l1[1] - Math.sin(l1[0]) * (double) j);
        float z = (float) ((double) l1[2] + Math.cos(l1[0]) * (double) j);
        moveYZ(processor, l2, y, z);
        return new float[] {xRot, y, z};
    }

    /** {@code part.x = x; part.y = y} in classic terms, the part's z left as it is (the bind: the classic never writes it). */
    private static void moveXY(AnimationProcessor<?> processor, String name, float x, float y) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, y, current[2]);
    }

    /** {@code part.y = y; part.z = z} in classic terms, the part's x left as it is (the bind: the classic never writes it). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityTriffid, TriffidGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TriffidGeoReplacement());
        }
    }
}
