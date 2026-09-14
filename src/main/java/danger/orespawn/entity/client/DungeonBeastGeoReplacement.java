package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.DungeonBeast;
import danger.orespawn.entity.pose.DungeonBeastPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Dungeon Beast (the hooks, owner 2026-09-14, addendum item 10): {@link ModelDungeonBeast#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays
 * closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.62f (orig ModelDungeonBeast.java:16,83 /
 * ClientProxyOreSpawn.java:481): the GAIT-scaled idiom on the ten leg parts about Z - {@code cos(age * 1.4f * ws) * PI *
 * 0.22f * limbSwingAmount}, the left side the negative, the toes around -0.785 rad (orig :488-497; no threshold); the
 * fourteen body / tail segments' pitch on one 0.5 ws cosine x 0.07 phased by k x 0.3927 down the spine, the last eight
 * negated (orig :498-505); the ATTACKING amplitude on the tail's yaw - {@code limbSwingAmount} at rest, 1.25 attacking
 * (orig :506, read through {@link DungeonBeastPose}) on a 0.75 ws cosine x 0.25, scaled 0.25 / 0.5 / 0.75 / 1 / 1.25 / 1.5
 * / 1.75 down the seven tail parts with the POSITION-write idiom (through {@link #moveTo}) - each tail part's pivot FOLLOWS
 * the previous 6 / 5 / 4.5 / 4 / 3 / 3 units back along (cos, sin) of its yaw, the spine segments copying their tail part's
 * pivot and yaw (orig :507-545); and the JAW LATCH (orig :546-573, the Robot2 precedent): at each falling zero-crossing of a
 * 2.0 ws cosine the per-entity {@link RenderInfo}'s {@code ri1} / {@code ri2} are re-rolled from the entity's RNG (0-14 at
 * rest, 0 attacking) and while {@code ri1} is 0 the six jaw parts chew on that cosine x 0.15 around -+0.349 / +-0.349 /
 * +-0.523 rad, else they rest. Tail1's pivot is never written (the bind), read through {@link #classicPosition}; every
 * value the classic reads back from a part it just wrote is held in a local; a part whose x / z the classic writes keeps
 * its bind y. Orig :573's {@code e.setRenderInfo(r)} is the port's omitted self-copy (ENT-S-093).
 * <p>Shadow follows {@link DungeonBeastRenderer}: a 0.25 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0, so no
 * scale hook. {@code DESCRIPTOR.renderTransform()}: the classic renderToBuffer's YP 90 (:535, orig :574; TEST-013, owner 2026-09-15).</p>
 */
public final class DungeonBeastGeoReplacement extends OreSpawnGeoReplacement<DungeonBeast> {
    /** orig ModelDungeonBeast.java:16,83 {@code wingspeed} = 0.62f (ClientProxyOreSpawn.java:481): the chain's third multiply. */
    static final float WINGSPEED = 0.62F;
    private static final GeoReplacementDescriptor<DungeonBeast> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.DUNGEON_BEAST.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            DungeonBeast.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/dungeonbeast.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/dungeonbeast.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dungeonbeast.png"),
            DungeonBeastRenderer.SHADOW) {
        @Override public RenderTransform renderTransform() { return RenderTransform.rotationDegrees(0.0F, 90.0F, 0.0F); }  // renderToBuffer:535
    };

    public DungeonBeastGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        DungeonBeastPose entity = inputs.subject(DungeonBeastPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelDungeonBeast.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float tailamp = 0.0f;
        float pi4 = 0.39269876f;
        newangle = Mth.cos((float) (ageInTicks * 1.4f * WINGSPEED)) * (float) Math.PI * 0.22f * limbSwingAmount;
        rotateZ(processor, "rheel", newangle);
        rotateZ(processor, "rfoot2", newangle);
        rotateZ(processor, "rfoot", newangle);
        rotateZ(processor, "rleg2", newangle);
        rotateZ(processor, "rleg1", newangle);
        rotateZ(processor, "rtoe2", -0.785f + newangle);
        rotateZ(processor, "lheel", -newangle);
        rotateZ(processor, "lfoot2", -newangle);
        rotateZ(processor, "lfoot", -newangle);
        rotateZ(processor, "lleg2", -newangle);
        rotateZ(processor, "lleg1", -newangle);
        rotateZ(processor, "ltoe2", -0.785f - newangle);
        rotateX(processor, "bodys1", Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED)) * (float) Math.PI * 0.07f);
        rotateX(processor, "bodys2", Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "bodys3", Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 2.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t1s1", Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 3.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t1s2", Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 4.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t1s3", Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 5.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t2s1", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 6.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t2s2", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 7.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t2s3", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 8.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t3s1", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 9.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t3s2", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 10.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t4s1", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 11.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t5s1", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 12.0f * pi4)) * (float) Math.PI * 0.07f);
        rotateX(processor, "t6s1", -Mth.cos((float) (ageInTicks * 0.5f * WINGSPEED + 13.0f * pi4)) * (float) Math.PI * 0.07f);
        tailamp = entity.getAttacking() == 0 ? limbSwingAmount : 1.25f;
        newangle = Mth.cos((float) (ageInTicks * 0.75f * WINGSPEED)) * (float) Math.PI * 0.25f * tailamp;
        float tail1YRot = newangle * 0.25f;
        rotateY(processor, "tail1", tail1YRot);
        rotateY(processor, "t1s3", tail1YRot);
        rotateY(processor, "t1s2", tail1YRot);
        rotateY(processor, "t1s1", tail1YRot);
        float tail2YRot = newangle * 0.5f;
        rotateY(processor, "tail2", tail2YRot);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2X = tail1[0] - (float) Math.cos(tail1YRot) * 6.0f;
        float tail2Z = tail1[2] - (float) Math.sin(tail1YRot) * 6.0f;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        rotateY(processor, "t2s3", tail2YRot);
        rotateY(processor, "t2s2", tail2YRot);
        rotateY(processor, "t2s1", tail2YRot);
        moveXZ(processor, "t2s3", tail2X, tail2Z);
        moveXZ(processor, "t2s2", tail2X, tail2Z);
        moveXZ(processor, "t2s1", tail2X, tail2Z);
        float tail3YRot = newangle * 0.75f;
        rotateY(processor, "tail3", tail3YRot);
        float tail3X = tail2X - (float) Math.cos(tail2YRot) * 5.0f;
        float tail3Z = tail2Z - (float) Math.sin(tail2YRot) * 5.0f;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        rotateY(processor, "t3s2", tail3YRot);
        rotateY(processor, "t3s1", tail3YRot);
        moveXZ(processor, "t3s2", tail3X, tail3Z);
        moveXZ(processor, "t3s1", tail3X, tail3Z);
        float tail4YRot = newangle;
        rotateY(processor, "tail4", tail4YRot);
        float tail4X = tail3X - (float) Math.cos(tail3YRot) * 4.5f;
        float tail4Z = tail3Z - (float) Math.sin(tail3YRot) * 4.5f;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        rotateY(processor, "t4s1", tail4YRot);
        moveXZ(processor, "t4s1", tail4X, tail4Z);
        float tail5YRot = newangle * 1.25f;
        rotateY(processor, "tail5", tail5YRot);
        float tail5X = tail4X - (float) Math.cos(tail4YRot) * 4.0f;
        float tail5Z = tail4Z - (float) Math.sin(tail4YRot) * 4.0f;
        moveXZ(processor, "tail5", tail5X, tail5Z);
        rotateY(processor, "t5s1", tail5YRot);
        moveXZ(processor, "t5s1", tail5X, tail5Z);
        float tail6YRot = newangle * 1.5f;
        rotateY(processor, "tail6", tail6YRot);
        float tail6X = tail5X - (float) Math.cos(tail5YRot) * 3.0f;
        float tail6Z = tail5Z - (float) Math.sin(tail5YRot) * 3.0f;
        moveXZ(processor, "tail6", tail6X, tail6Z);
        rotateY(processor, "t6s1", tail6YRot);
        moveXZ(processor, "t6s1", tail6X, tail6Z);
        rotateY(processor, "tail7", newangle * 1.75f);
        float tail7X = tail6X - (float) Math.cos(tail6YRot) * 3.0f;
        float tail7Z = tail6Z - (float) Math.sin(tail6YRot) * 3.0f;
        moveXZ(processor, "tail7", tail7X, tail7Z);
        // Per-entity scratch as in the original (orig DungeonBeast.java:43, orig ModelDungeonBeast.java:546-573): each
        // DungeonBeast keeps its own jaw latch instead of sharing one field on the model singleton. ENT-S-093.
        RenderInfo r = entity.getRenderInfo();
        newangle = Mth.cos((float) (ageInTicks * 2.0f * WINGSPEED)) * (float) Math.PI * 0.15f;
        nextangle = Mth.cos((float) ((ageInTicks + 0.1f) * 2.0f * WINGSPEED)) * (float) Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            if (entity.getAttacking() == 0) {
                r.ri1 = entity.getRandom().nextInt(15);
                r.ri2 = entity.getRandom().nextInt(15);
            } else {
                r.ri1 = 0;
                r.ri2 = 0;
            }
        }
        if (r.ri1 == 0) {
            rotateY(processor, "ljaw1", -0.349f + newangle);
            rotateY(processor, "ljaw2", 0.349f + newangle);
            rotateY(processor, "ljaw3", 0.523f + newangle);
            rotateY(processor, "rjaw1", 0.349f - newangle);
            rotateY(processor, "rjaw2", -0.349f - newangle);
            rotateY(processor, "rjaw3", -0.523f - newangle);
        } else {
            rotateY(processor, "ljaw1", -0.349f);
            rotateY(processor, "ljaw2", 0.349f);
            rotateY(processor, "ljaw3", 0.523f);
            rotateY(processor, "rjaw1", 0.349f);
            rotateY(processor, "rjaw2", -0.349f);
            rotateY(processor, "rjaw3", -0.523f);
        }
    }

    /** {@code part.x = x; part.z = z} in classic terms: the classic writes x and z only, so y keeps the part's bind. */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<DungeonBeast, DungeonBeastGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new DungeonBeastGeoReplacement());
        }
    }
}
