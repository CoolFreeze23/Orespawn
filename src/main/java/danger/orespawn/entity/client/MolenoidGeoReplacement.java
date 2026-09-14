package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMolenoid;
import danger.orespawn.entity.pose.MolenoidPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Molenoid (the hooks, landed by the fifth Tier-2 slice T2e): {@link MolenoidModel#poseFrom} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.5f
 * (orig ModelMolenoid.java:14,54 / ClientProxyOreSpawn.java:495): the ATTACKING branch over the
 * THRESHOLD idiom on the arms (orig :286 {@code getAttacking() != 0}: {@code cos(age x 1.7 x ws) x PI x 0.25}
 * attacking, else above a walking speed of a tenth {@code cos(age x 1.3 x ws) x PI x 0.25 x limbSwingAmount},
 * else 0), the threshold gait alone on the legs (the negative); each limb a three-link yaw chain (the upper link about
 * its rest +-0.628 rad, the hand / foot at 1.25 of the angle, the four claws / toes at 1.5 about -+0.174 / -+0.261)
 * whose POSITION-write idiom carries the hand 15 units along (cos, -sin) of the upper link's yaw and the claws 10
 * units along the hand's (x and z only, through {@link #moveXZ}; the upper links' pivots are never written - the
 * bind, read through {@link #classicPosition}); every value the classic reads back from a
 * part it just wrote is held in a local. The six nose stars spin about Z on {@code cos(age x 0.1 x ws) x PI} at
 * 0.523-rad steps. The entity is read through {@link MolenoidPose} (the Slice 4b form).
 *
 *
 * <p>Shadow follows {@link MolenoidRenderer}: a 1.0 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0 with no
 * scale override, so no scale hook.</p>
 */
public final class MolenoidGeoReplacement extends OreSpawnGeoReplacement<EntityMolenoid> {
    /** orig ModelMolenoid.java:14,54 {@code wingspeed} = 0.5f (ClientProxyOreSpawn.java:495): the chain's third multiply. */
    static final float WINGSPEED = 0.5F;
    private static final GeoReplacementDescriptor<EntityMolenoid> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_MOLENOID.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityMolenoid.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/molenoid.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/molenoid.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/molenoid.png"),
            MolenoidRenderer.SHADOW) {
    };

    public MolenoidGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        MolenoidPose entity = inputs.subject(MolenoidPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // MolenoidModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        newangle = entity.getAttacking() != 0 ? Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.25f : (limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f);
        float larmY = newangle + 0.628f;
        rotateY(processor, "larm", larmY);
        float[] larm = classicPosition(bone(processor, "larm"));  // never written: the bind pivot
        float lhandZ = larm[2] - (float) Math.sin(larmY) * 15.0f;
        float lhandX = larm[0] + (float) Math.cos(larmY) * 15.0f;
        moveXZ(processor, "lhand", lhandX, lhandZ);
        float lhandY = newangle * 1.25f;
        rotateY(processor, "lhand", lhandY);
        float lclaw1Z = lhandZ - (float) Math.sin(lhandY) * 10.0f;
        float lclaw1X = lhandX + (float) Math.cos(lhandY) * 10.0f;
        moveXZ(processor, "lclaw1", lclaw1X, lclaw1Z);
        float lclaw1Y = newangle * 1.5f - 0.174f;
        rotateY(processor, "lclaw1", lclaw1Y);
        moveXZ(processor, "lclaw2", lclaw1X, lclaw1Z);
        rotateY(processor, "lclaw2", lclaw1Y);
        moveXZ(processor, "lclaw3", lclaw1X, lclaw1Z);
        rotateY(processor, "lclaw3", lclaw1Y);
        moveXZ(processor, "lclaw4", lclaw1X, lclaw1Z);
        rotateY(processor, "lclaw4", lclaw1Y);
        float rarmY = newangle - 0.628f;
        rotateY(processor, "rarm", rarmY);
        float[] rarm = classicPosition(bone(processor, "rarm"));  // never written: the bind pivot
        float rhandZ = rarm[2] + (float) Math.sin(rarmY) * 15.0f;
        float rhandX = rarm[0] - (float) Math.cos(rarmY) * 15.0f;
        moveXZ(processor, "rhand", rhandX, rhandZ);
        float rhandY = newangle * 1.25f;
        rotateY(processor, "rhand", rhandY);
        float rclaw1Z = rhandZ + (float) Math.sin(rhandY) * 10.0f;
        float rclaw1X = rhandX - (float) Math.cos(rhandY) * 10.0f;
        moveXZ(processor, "rclaw1", rclaw1X, rclaw1Z);
        float rclaw1Y = newangle * 1.5f + 0.174f;
        rotateY(processor, "rclaw1", rclaw1Y);
        moveXZ(processor, "rclaw2", rclaw1X, rclaw1Z);
        rotateY(processor, "rclaw2", rclaw1Y);
        moveXZ(processor, "rclaw3", rclaw1X, rclaw1Z);
        rotateY(processor, "rclaw3", rclaw1Y);
        moveXZ(processor, "rclaw4", rclaw1X, rclaw1Z);
        rotateY(processor, "rclaw4", rclaw1Y);
        newangle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        float llegY = -newangle + 0.628f;
        rotateY(processor, "lleg", llegY);
        float[] lleg = classicPosition(bone(processor, "lleg"));  // never written: the bind pivot
        float lfootZ = lleg[2] - (float) Math.sin(llegY) * 15.0f;
        float lfootX = lleg[0] + (float) Math.cos(llegY) * 15.0f;
        moveXZ(processor, "lfoot", lfootX, lfootZ);
        float lfootY = -newangle * 1.25f;
        rotateY(processor, "lfoot", lfootY);
        float ltoe1Z = lfootZ - (float) Math.sin(lfootY) * 10.0f;
        float ltoe1X = lfootX + (float) Math.cos(lfootY) * 10.0f;
        moveXZ(processor, "ltoe1", ltoe1X, ltoe1Z);
        float ltoe1Y = -newangle * 1.5f - 0.261f;
        rotateY(processor, "ltoe1", ltoe1Y);
        moveXZ(processor, "ltoe2", ltoe1X, ltoe1Z);
        rotateY(processor, "ltoe2", ltoe1Y);
        moveXZ(processor, "ltoe3", ltoe1X, ltoe1Z);
        rotateY(processor, "ltoe3", ltoe1Y);
        moveXZ(processor, "ltoe4", ltoe1X, ltoe1Z);
        rotateY(processor, "ltoe4", ltoe1Y);
        float rlegY = -newangle - 0.628f;
        rotateY(processor, "rleg", rlegY);
        float[] rleg = classicPosition(bone(processor, "rleg"));  // never written: the bind pivot
        float rfootZ = rleg[2] + (float) Math.sin(rlegY) * 15.0f;
        float rfootX = rleg[0] - (float) Math.cos(rlegY) * 15.0f;
        moveXZ(processor, "rfoot", rfootX, rfootZ);
        float rfootY = -newangle * 1.25f;
        rotateY(processor, "rfoot", rfootY);
        float rtoe1Z = rfootZ + (float) Math.sin(rfootY) * 10.0f;
        float rtoe1X = rfootX - (float) Math.cos(rfootY) * 10.0f;
        moveXZ(processor, "rtoe1", rtoe1X, rtoe1Z);
        float rtoe1Y = -newangle * 1.5f + 0.261f;
        rotateY(processor, "rtoe1", rtoe1Y);
        moveXZ(processor, "rtoe2", rtoe1X, rtoe1Z);
        rotateY(processor, "rtoe2", rtoe1Y);
        moveXZ(processor, "rtoe3", rtoe1X, rtoe1Z);
        rotateY(processor, "rtoe3", rtoe1Y);
        moveXZ(processor, "rtoe4", rtoe1X, rtoe1Z);
        rotateY(processor, "rtoe4", rtoe1Y);
        newangle = Mth.cos(ageInTicks * 0.1f * WINGSPEED) * (float) Math.PI;
        rotateZ(processor, "nosestar1", newangle);
        rotateZ(processor, "nosestar2", newangle + 0.523f);
        rotateZ(processor, "nosestar3", newangle + 1.047f);
        rotateZ(processor, "nosestar4", newangle + 1.57f);
        rotateZ(processor, "nosestar5", newangle - 1.047f);
        rotateZ(processor, "nosestar6", newangle - 0.523f);
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityMolenoid, MolenoidGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new MolenoidGeoReplacement());
        }
    }
}
