package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityStinky;
import danger.orespawn.entity.pose.StinkyPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Stinky (the hooks, landed by the sixth Tier-2 slice T2f): {@link StinkyModel#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). The port's classic model as it is: its frequency multiplier {@code ws} is
 * {@code limbSwingAmount} itself (StinkyModel.poseFrom {@code float ws = limbSwingAmount} ; orig ModelStinky.java ran
 * on a wingspeed of 0.65f, ClientProxyOreSpawn.java:490 - the register line of the landing slice records where the
 * port's pose departs from the 1.7.10 one) - the THRESHOLD idiom on the wings about Z ({@code cos(age x 2.3 x ws) x PI
 * x 0.4 x limbSwingAmount} above a walking speed of a tenth about -+0.4 rad) and on the four legs about X ({@code
 * cos(age x 2.0 x ws) x PI x 0.25 x limbSwingAmount}, alternating; the ACTIVITY branch folds them at -1 / +1 rad at
 * 2); the tail about Y on {@code cos(age x 1.0 x ws) x PI x 0.2} , stilled by the SITTING check, the second and
 * third links at 1.6 and 2.6 of it, each link's pivot FOLLOWING its parent 4 / 3 units along (sin, cos) of the
 * parent's yaw, less 0.5 in x (the POSITION-write idiom, x and z through {@link #moveXZ} ; the first link's pivot
 * is never written - the bind); and the HEAD-LOOK idiom on seven head parts, the yaw in radians (the neck at half),
 * the pitch in radians at a third. The entity is read through {@link StinkyPose} (the Slice 4b form). The two
 * wings are zero-thickness cubes (ENT-S-161; the classic face order required below).
 *
 *
 * <p>Shadow follows {@link StinkyRenderer} : a 0.75 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0 with
 * no scale override, so no scale hook.</p>
 */
public final class StinkyGeoReplacement extends OreSpawnGeoReplacement<EntityStinky> {
    private static final GeoReplacementDescriptor<EntityStinky> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_STINKY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityStinky.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/stinky.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/stinky.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/stinky.png"),
            StinkyRenderer.SHADOW) {
        /**
         * A rig with zero-thickness cubes (the two wings, 18 x 0 x 10): the shipped geo carries the classic within-cube
         * face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public StinkyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        StinkyPose entity = inputs.subject(StinkyPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // StinkyModel.poseFrom (the body of the classic setupAnim) verbatim; every value the classic reads back from a
        // part it just wrote is held in a local, the never-written tail2 pivot (the bind) read through classicPosition.
        float ws = limbSwingAmount;

        float newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.3F * ws) * (float) Math.PI * 0.4F * limbSwingAmount : 0.0F;
        rotateZ(processor, "Rwing", newangle - 0.4F);
        rotateZ(processor, "Lwing", -newangle + 0.4F);

        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.0F * ws) * (float) Math.PI * 0.25F * limbSwingAmount : 0.0F;
        int currentActivity = entity.getActivity();
        if (currentActivity != 2) {
            rotateX(processor, "Rleg1", newangle);
            rotateX(processor, "Lleg1", -newangle);
            rotateX(processor, "Rleg2", -newangle);
            rotateX(processor, "Lleg2", newangle);
        } else {
            newangle = -1.0F;
            rotateX(processor, "Rleg2", newangle);
            rotateX(processor, "Lleg2", newangle);
            newangle = 1.0F;
            rotateX(processor, "Rleg1", newangle);
            rotateX(processor, "Lleg1", newangle);
        }

        newangle = Mth.cos(ageInTicks * 1.0F * ws) * (float) Math.PI * 0.2F;
        if (entity.isInSittingPose()) {
            newangle = 0.0F;
        }
        rotateY(processor, "tail2", newangle);
        float[] tail2 = classicPosition(bone(processor, "tail2"));  // never written: the bind pivot
        float tail3Z = tail2[2] + (float) Math.cos(newangle) * 4.0F;
        float tail3X = tail2[0] + (float) Math.sin(newangle) * 4.0F - 0.5F;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3Y = newangle * 1.6F;
        rotateY(processor, "tail3", tail3Y);
        float tail4Z = tail3Z + (float) Math.cos(tail3Y) * 3.0F;
        float tail4X = tail3X + (float) Math.sin(tail3Y) * 3.0F - 0.5F;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        rotateY(processor, "tail4", newangle * 2.6F);

        rotateY(processor, "head", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "snout", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "neck", (float) Math.toRadians(netHeadYaw) / 2.0F);
        rotateY(processor, "Rhorn1", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "Rhorn2", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "Lhorn1", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "Lhorn2", (float) Math.toRadians(netHeadYaw));

        rotateX(processor, "head", (float) Math.toRadians(headPitch) / 3.0F);
        rotateX(processor, "snout", (float) Math.toRadians(headPitch) / 3.0F);
        rotateX(processor, "neck", (float) Math.toRadians(headPitch) / 3.0F);
        rotateX(processor, "Rhorn1", (float) Math.toRadians(headPitch) / 3.0F);
        rotateX(processor, "Rhorn2", (float) Math.toRadians(headPitch) / 3.0F);
        rotateX(processor, "Lhorn1", (float) Math.toRadians(headPitch) / 3.0F);
        rotateX(processor, "Lhorn2", (float) Math.toRadians(headPitch) / 3.0F);
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityStinky, StinkyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new StinkyGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: flyer} (tools/artist_specs/stinky.json): {@code flying} is {@code !onGround()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.FLYER;
    }
}
