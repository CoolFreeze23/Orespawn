package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SeaMonster;
import danger.orespawn.entity.pose.SeaMonsterPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Sea Monster (the hooks, owner 2026-09-14, addendum item 10; landed by the first Tier-1 slice T1a,
 * 2026-09-15, the owner's closing set item 4): {@link ModelSeaMonster#poseFrom} verbatim on the converted rig, ON THE
 * HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk} ; the geo, the wiring and the proofs landed with T1a). Wingspeed 0.5f (orig ModelSeaMonster.java:14,40 /
 * ClientProxyOreSpawn.java:496): the THRESHOLD-OR-ATTACKING idiom ({@code (double) limbSwingAmount > 0.1 ||
 * getAttacking() != 0}) on three rhythms - the tail's yaw fan ({@code cos(age x 1.3 ws) x PI x 0.2 x amount}, else 0,
 * in sevenths down to the tip, the seven rings FOLLOWING one another by 10 / 7 / 5 / 5 / 5 / 5 units in x / z: POSITION
 * writes through {@link #moveTo} ), the four fins' pitch and yaw (1.2 ws x PI x 0.2 x amount, else a 0.02 breath,
 * around -0.523 / -+0.698), and the neck's pitch chain (0.455 x amount + 0.9 ws x PI x 0.25 x amount, else a 0.3 ws x
 * 0.02 breath: the base at 0.455 + a fifth, each ring adding a quarter / third / half and the last two subtracting a
 * half / third, the rings FOLLOWING by 9 / 9 / 9 / 9 / 5 units in y / z, the jaws and eyes riding 5 units past the
 * sixth); the HEAD-LOOK idiom (yaw {@code toRadians(netHeadYaw) x 0.5} on the top jaw, the eyes and the bottom jaw);
 * and the ATTACKING branch on the bottom jaw ({@code getAttacking() != 0}: a 1.7 ws cosine x PI x 0.17 over 0.45, else
 * a 0.2 ws cosine x PI x 0.05 over 0.17). The entity is read through {@link SeaMonsterPose} (the Slice 4b doctrine).
 * Every value the classic reads back from a part it just wrote is held in a local; the tail base's and the neck base's
 * pivots, never written, are read through {@link #classicPosition} (the bind), as are the tail's y and the neck's x.
 *
 * <p>Shadow follows {@link SeaMonsterRenderer} : a 1.0 x 1.0 shadow (ENT-S-092); its {@code SCALE} is 1.0 (identity),
 * so no scale hook. No zero-thickness cube.</p>
 */
public final class SeaMonsterGeoReplacement extends OreSpawnGeoReplacement<SeaMonster> {
    /** orig ModelSeaMonster.java:14,40 {@code wingspeed} = 0.5f (ClientProxyOreSpawn.java:496): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.5f;
    private static final GeoReplacementDescriptor<SeaMonster> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.SEA_MONSTER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            SeaMonster.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/seamonster.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/seamonster.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/seamonster.png"),
            SeaMonsterRenderer.SHADOW) {
    };

    public SeaMonsterGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        SeaMonsterPose entity = inputs.subject(SeaMonsterPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelSeaMonster.poseFrom verbatim, the float / double chain exactly as the classic casts it.
        float newangle = 0.0f;
        float pi4 = 0.7853982f;
        newangle = (double) limbSwingAmount > 0.1 || entity.getAttacking() != 0 ? Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.2f * limbSwingAmount : 0.0f;
        float tailBaseYRot = newangle / 7.0f;
        rotateY(processor, "TailBase", tailBaseYRot);
        float[] tailBase = classicPosition(bone(processor, "TailBase"));  // never written: the bind pivot
        float tail2Z = tailBase[2] + (float) Math.cos(tailBaseYRot) * 10.0f;
        float tail2X = tailBase[0] + (float) Math.sin(tailBaseYRot) * 10.0f;
        moveXZ(processor, "Tail2", tail2X, tail2Z);
        float tail2YRot = newangle / 6.0f;
        rotateY(processor, "Tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 7.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 7.0f;
        moveXZ(processor, "Tail3", tail3X, tail3Z);
        float tail3YRot = newangle / 5.0f;
        rotateY(processor, "Tail3", tail3YRot);
        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 5.0f;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 5.0f;
        moveXZ(processor, "Tail4", tail4X, tail4Z);
        float tail4YRot = newangle / 4.0f;
        rotateY(processor, "Tail4", tail4YRot);
        float tail5Z = tail4Z + (float) Math.cos(tail4YRot) * 5.0f;
        float tail5X = tail4X + (float) Math.sin(tail4YRot) * 5.0f;
        moveXZ(processor, "Tail5", tail5X, tail5Z);
        float tail5YRot = newangle / 3.0f;
        rotateY(processor, "Tail5", tail5YRot);
        float tail6Z = tail5Z + (float) Math.cos(tail5YRot) * 5.0f;
        float tail6X = tail5X + (float) Math.sin(tail5YRot) * 5.0f;
        moveXZ(processor, "Tail6", tail6X, tail6Z);
        float tail6YRot = newangle / 2.0f;
        rotateY(processor, "Tail6", tail6YRot);
        float tailTipZ = tail6Z + (float) Math.cos(tail6YRot) * 5.0f;
        float tailTipX = tail6X + (float) Math.sin(tail6YRot) * 5.0f;
        moveXZ(processor, "TailTip", tailTipX, tailTipZ);
        rotateY(processor, "TailTip", newangle);
        newangle = (double) limbSwingAmount > 0.1 || entity.getAttacking() != 0 ? Mth.cos((float) (ageInTicks * 1.2f * WINGSPEED)) * (float) Math.PI * 0.2f * limbSwingAmount : Mth.cos((float) (ageInTicks * 1.2f * WINGSPEED)) * (float) Math.PI * 0.02f;
        rotateX(processor, "FinFrontLeft", newangle - 0.523f);
        rotateY(processor, "FinFrontLeft", newangle + 0.698f);
        rotateX(processor, "FinBackLeft", -newangle - 0.523f);
        rotateY(processor, "FinBackLeft", -newangle + 0.698f);
        rotateX(processor, "FinFrontRight", newangle - 0.523f);
        rotateY(processor, "FinFrontRight", newangle - 0.698f);
        rotateX(processor, "FinBackRight", -newangle - 0.523f);
        rotateY(processor, "FinBackRight", -newangle - 0.698f);
        newangle = (double) limbSwingAmount > 0.1 || entity.getAttacking() != 0 ? 0.455f * limbSwingAmount + Mth.cos((float) (ageInTicks * 0.9f * WINGSPEED)) * (float) Math.PI * 0.25f * limbSwingAmount : Mth.cos((float) (ageInTicks * 0.3f * WINGSPEED)) * (float) Math.PI * 0.02f;
        float neckBaseXRot = 0.455f + newangle / 5.0f;
        rotateX(processor, "NeckBase", neckBaseXRot);
        float[] neckBase = classicPosition(bone(processor, "NeckBase"));  // never written: the bind pivot
        float neck2Z = neckBase[2] - (float) Math.sin(neckBaseXRot) * 9.0f;
        float neck2Y = neckBase[1] - (float) Math.cos(neckBaseXRot) * 9.0f;
        moveYZ(processor, "Neck2", neck2Y, neck2Z);
        float neck2XRot = neckBaseXRot + newangle / 4.0f;
        rotateX(processor, "Neck2", neck2XRot);
        float neck3Z = neck2Z - (float) Math.sin(neck2XRot) * 9.0f;
        float neck3Y = neck2Y - (float) Math.cos(neck2XRot) * 9.0f;
        moveYZ(processor, "Neck3", neck3Y, neck3Z);
        float neck3XRot = neck2XRot + newangle / 3.0f;
        rotateX(processor, "Neck3", neck3XRot);
        float neck4Z = neck3Z - (float) Math.sin(neck3XRot) * 9.0f;
        float neck4Y = neck3Y - (float) Math.cos(neck3XRot) * 9.0f;
        moveYZ(processor, "Neck4", neck4Y, neck4Z);
        float neck4XRot = neck3XRot + newangle / 2.0f;
        rotateX(processor, "Neck4", neck4XRot);
        float neck5Z = neck4Z - (float) Math.sin(neck4XRot) * 9.0f;
        float neck5Y = neck4Y - (float) Math.cos(neck4XRot) * 9.0f;
        moveYZ(processor, "Neck5", neck5Y, neck5Z);
        float neck5XRot = neck4XRot - newangle / 2.0f;
        rotateX(processor, "Neck5", neck5XRot);
        float neck6Z = neck5Z - (float) Math.sin(neck5XRot) * 5.0f;
        float neck6Y = neck5Y - (float) Math.cos(neck5XRot) * 5.0f;
        moveYZ(processor, "Neck6", neck6Y, neck6Z);
        float neck6XRot = neck5XRot - newangle / 3.0f;
        rotateX(processor, "Neck6", neck6XRot);
        float topJawZ = neck6Z - (float) Math.sin(neck6XRot) * 5.0f;   // RightEye.z = TopJaw.z = ...; LeftEye.z, BottomJaw.z copy it
        float topJawY = neck6Y - (float) Math.cos(neck6XRot) * 5.0f;   // RightEye.y = TopJaw.y = ...; LeftEye.y, BottomJaw.y copy it
        moveYZ(processor, "TopJaw", topJawY, topJawZ);
        moveYZ(processor, "RightEye", topJawY, topJawZ);
        moveYZ(processor, "LeftEye", topJawY, topJawZ);
        moveYZ(processor, "BottomJaw", topJawY, topJawZ);
        float topJawYRot = (newangle = (float) Math.toRadians(netHeadYaw) * 0.5f);   // RightEye.yRot = TopJaw.yRot = (newangle = ...)
        rotateY(processor, "TopJaw", topJawYRot);
        rotateY(processor, "RightEye", topJawYRot);
        rotateY(processor, "LeftEye", topJawYRot);
        rotateY(processor, "BottomJaw", topJawYRot);
        if (entity.getAttacking() != 0) {
            newangle = Mth.cos((float) (ageInTicks * 1.7f * WINGSPEED)) * (float) Math.PI * 0.17f;
            rotateX(processor, "BottomJaw", 0.45f + newangle);
        } else {
            newangle = Mth.cos((float) (ageInTicks * 0.2f * WINGSPEED)) * (float) Math.PI * 0.05f;
            rotateX(processor, "BottomJaw", 0.17f + newangle);
        }
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    /** {@code part.y = y; part.z = z} on a part whose x the classic never writes (the bind). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<SeaMonster, SeaMonsterGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new SeaMonsterGeoReplacement());
        }

        /** The classic {@link SeaMonsterRenderer#shouldRender} (OPT-013): unconditionally drawn, never frustum-culled by its hitbox (T1a). */
        @Override
        public boolean shouldRender(SeaMonster entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return true;
        }
    }
}
