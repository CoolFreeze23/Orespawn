package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpitBug;
import danger.orespawn.entity.pose.SpitBugPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib Spit Bug (the hooks, owner 2026-09-14, addendum item 10): {@link SpitBugModel#poseFrom} verbatim on the rig
 * the landing slice converts, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.55f (orig
 * ModelSpitBug.java:14,110 / ClientProxyOreSpawn.java:452): the {@code Mth.sin} gait on the four legs -
 * {@code sin(age x 2.0 x ws) x PI x 0.12 x limbSwingAmount}, the right pair a half turn behind ({@code + PI}), the lift
 * {@code |cos|} of the same rhythm on the rising half-cycle ({@code nextangle > newangle}) - through the four leg
 * helpers transcribed below (the same names): the seven parts of a leg share the root's yaw (about -+1.2 / +-2.1 rad),
 * the four second-link parts' pivots FOLLOW the root 14 units along (cos, sin) of that yaw scaled by cos of the ROOT's
 * pitch, the third link follows the second by 14 scaled by cos of the second's pitch, and the two tips follow the
 * third by 8 scaled by |cos| of the third's pitch (the POSITION-write idiom, x and z through {@link #moveXZ}; the
 * root's pivot and pitch are never written - the bind, read through {@link #classicPosition} and
 * {@link #classicRotX}); every value the classic reads back from a part it just wrote is held in a local. The ATTACKING
 * branch on the three upper-jaw parts and the three teeth about X (orig :698 {@code getAttacking() == 0}: 0.3 x ws at
 * 0.015 x PI at rest, 2.6 x ws at 0.1 x PI attacking, folded by {@code |cos|}, the teeth about 0.26 rad). The entity
 * is read through {@link SpitBugPose} (the Slice 4b form).
 *
 * <p>Scale and shadow follow {@link SpitBugRenderer}: 0.75 render scale and a 0.55 x 0.75 shadow (ENT-S-092).</p>
 */
public final class SpitBugGeoReplacement extends OreSpawnGeoReplacement<EntitySpitBug> {
    /** orig ModelSpitBug.java:14,110 {@code wingspeed} = 0.55f (ClientProxyOreSpawn.java:452): the chain's third multiply. */
    static final float WINGSPEED = 0.55F;
    private static final GeoReplacementDescriptor<EntitySpitBug> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_SPIT_BUG.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntitySpitBug.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/spitbug.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/spitbug.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spitbug.png"),
            SpitBugRenderer.SHADOW) {
        @Override
        public void applyScale(EntitySpitBug entity, PoseStack poseStack, float partialTick) {
            // orig RenderSpitBug.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) (SpitBugRenderer.scale)
            poseStack.scale(SpitBugRenderer.SCALE, SpitBugRenderer.SCALE, SpitBugRenderer.SCALE);
        }
    };

    public SpitBugGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        SpitBugPose entity = inputs.subject(SpitBugPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // SpitBugModel.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain exactly as the
        // classic casts it (its unused locals - Object r, pi4 - carry nothing and are not declared).
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        newangle = Mth.sin(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.sin((ageInTicks + 0.1f) * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos(ageInTicks * 2.0f * WINGSPEED) * (float) Math.PI * 0.12f * limbSwingAmount);
        }
        doLeftFrontLeg(processor, newangle, upangle);
        doLeftRearLeg(processor, -newangle, upangle);
        newangle = Mth.sin((float) ((double) (ageInTicks * 2.0f * WINGSPEED) + Math.PI)) * (float) Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.sin((float) ((double) ((ageInTicks + 0.1f) * 2.0f * WINGSPEED) + Math.PI)) * (float) Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
            upangle = Math.abs(Mth.cos((float) ((double) (ageInTicks * 2.0f * WINGSPEED) + Math.PI)) * (float) Math.PI * 0.12f * limbSwingAmount);
        }
        doRightFrontLeg(processor, -newangle, upangle);
        doRightRearLeg(processor, newangle, upangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.015f : Mth.cos(ageInTicks * 2.6f * WINGSPEED) * (float) Math.PI * 0.1f;
        newangle = Math.abs(newangle);
        rotateX(processor, "upperjawbasepart1", newangle);
        rotateX(processor, "upperjawbasepart2", newangle);
        rotateX(processor, "upperjawbasepart3", newangle);
        rotateX(processor, "tooth1", 0.26f + newangle);
        rotateX(processor, "tooth2", 0.26f + newangle);
        rotateX(processor, "tooth3", 0.26f + newangle);
    }

    /** SpitBugModel.doRightFrontLeg verbatim on the leg1 chain. */
    private static void doRightFrontLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg1YRot = -1.2f + angle;
        rotateY(processor, "leg1", leg1YRot);
        rotateY(processor, "leg1part2", leg1YRot);
        rotateY(processor, "leg1part2b", leg1YRot);
        rotateY(processor, "leg1part2c", leg1YRot);
        rotateY(processor, "leg1part2d", leg1YRot);
        rotateY(processor, "leg1part3", leg1YRot);
        rotateY(processor, "leg1part3b", leg1YRot);
        rotateY(processor, "leg1part3c", leg1YRot);
        GeoBone leg1 = bone(processor, "leg1");
        float dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(classicRotX(leg1)));  // leg1.xRot is never written: the bind
        float[] leg1Pivot = classicPosition(leg1);  // never written: the bind pivot
        float leg1part2Z = (float) ((double) leg1Pivot[2] - Math.cos(leg1YRot) * (double) dist);
        float leg1part2X = (float) ((double) leg1Pivot[0] - Math.sin(leg1YRot) * (double) dist);
        moveXZ(processor, "leg1part2d", leg1part2X, leg1part2Z);
        moveXZ(processor, "leg1part2c", leg1part2X, leg1part2Z);
        moveXZ(processor, "leg1part2b", leg1part2X, leg1part2Z);
        moveXZ(processor, "leg1part2", leg1part2X, leg1part2Z);
        float leg1part2XRot = -1.152f + upangle;
        rotateX(processor, "leg1part2", leg1part2XRot);
        rotateX(processor, "leg1part2b", -0.743f + upangle);
        rotateX(processor, "leg1part2c", -0.632f + upangle);
        rotateX(processor, "leg1part2d", -1.041f + upangle);
        dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(leg1part2XRot));
        float leg1part3Z = (float) ((double) leg1part2Z - Math.cos(leg1YRot) * (double) dist);  // leg1part2.yRot = leg1.yRot
        float leg1part3X = (float) ((double) leg1part2X - Math.sin(leg1YRot) * (double) dist);
        moveXZ(processor, "leg1part3", leg1part3X, leg1part3Z);
        float leg1part3XRot = 0.669f - upangle;
        rotateX(processor, "leg1part3", leg1part3XRot);
        dist = 8.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg1part3XRot));
        float leg1part3bZ = (float) ((double) leg1part3Z - Math.cos(leg1YRot) * (double) dist);  // leg1part3.yRot = leg1.yRot
        float leg1part3bX = (float) ((double) leg1part3X - Math.sin(leg1YRot) * (double) dist);
        moveXZ(processor, "leg1part3c", leg1part3bX, leg1part3bZ);
        moveXZ(processor, "leg1part3b", leg1part3bX, leg1part3bZ);
        rotateX(processor, "leg1part3b", -0.48f - upangle);
        rotateX(processor, "leg1part3c", -0.48f - upangle);
    }

    /** SpitBugModel.doLeftFrontLeg verbatim on the leg2 chain. */
    private static void doLeftFrontLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg2YRot = 1.2f + angle;
        rotateY(processor, "leg2", leg2YRot);
        rotateY(processor, "leg2part2", leg2YRot);
        rotateY(processor, "leg2part2b", leg2YRot);
        rotateY(processor, "leg2part2c", leg2YRot);
        rotateY(processor, "leg2part2d", leg2YRot);
        rotateY(processor, "leg2part3", leg2YRot);
        rotateY(processor, "leg2part3b", leg2YRot);
        rotateY(processor, "leg2part3c", leg2YRot);
        GeoBone leg2 = bone(processor, "leg2");
        float dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(classicRotX(leg2)));  // leg2.xRot is never written: the bind
        float[] leg2Pivot = classicPosition(leg2);  // never written: the bind pivot
        float leg2part2Z = (float) ((double) leg2Pivot[2] - Math.cos(leg2YRot) * (double) dist);
        float leg2part2X = (float) ((double) leg2Pivot[0] - Math.sin(leg2YRot) * (double) dist);
        moveXZ(processor, "leg2part2d", leg2part2X, leg2part2Z);
        moveXZ(processor, "leg2part2c", leg2part2X, leg2part2Z);
        moveXZ(processor, "leg2part2b", leg2part2X, leg2part2Z);
        moveXZ(processor, "leg2part2", leg2part2X, leg2part2Z);
        float leg2part2XRot = -1.152f + upangle;
        rotateX(processor, "leg2part2", leg2part2XRot);
        rotateX(processor, "leg2part2b", -0.743f + upangle);
        rotateX(processor, "leg2part2c", -0.632f + upangle);
        rotateX(processor, "leg2part2d", -1.041f + upangle);
        dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(leg2part2XRot));
        float leg2part3Z = (float) ((double) leg2part2Z - Math.cos(leg2YRot) * (double) dist);  // leg2part2.yRot = leg2.yRot
        float leg2part3X = (float) ((double) leg2part2X - Math.sin(leg2YRot) * (double) dist);
        moveXZ(processor, "leg2part3", leg2part3X, leg2part3Z);
        float leg2part3XRot = 0.669f - upangle;
        rotateX(processor, "leg2part3", leg2part3XRot);
        dist = 8.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg2part3XRot));
        float leg2part3bZ = (float) ((double) leg2part3Z - Math.cos(leg2YRot) * (double) dist);  // leg2part3.yRot = leg2.yRot
        float leg2part3bX = (float) ((double) leg2part3X - Math.sin(leg2YRot) * (double) dist);
        moveXZ(processor, "leg2part3c", leg2part3bX, leg2part3bZ);
        moveXZ(processor, "leg2part3b", leg2part3bX, leg2part3bZ);
        rotateX(processor, "leg2part3b", -0.48f - upangle);
        rotateX(processor, "leg2part3c", -0.48f - upangle);
    }

    /** SpitBugModel.doRightRearLeg verbatim on the leg4 chain. */
    private static void doRightRearLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg4YRot = 2.1f + angle;
        rotateY(processor, "leg4", leg4YRot);
        rotateY(processor, "leg4part2", leg4YRot);
        rotateY(processor, "leg4part2b", leg4YRot);
        rotateY(processor, "leg4part2c", leg4YRot);
        rotateY(processor, "leg4part2d", leg4YRot);
        rotateY(processor, "leg4part3", leg4YRot);
        rotateY(processor, "leg4part3b", leg4YRot);
        rotateY(processor, "leg4part3c", leg4YRot);
        GeoBone leg4 = bone(processor, "leg4");
        float dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(classicRotX(leg4)));  // leg4.xRot is never written: the bind
        float[] leg4Pivot = classicPosition(leg4);  // never written: the bind pivot
        float leg4part2Z = (float) ((double) leg4Pivot[2] - Math.cos(leg4YRot) * (double) dist);
        float leg4part2X = (float) ((double) leg4Pivot[0] - Math.sin(leg4YRot) * (double) dist);
        moveXZ(processor, "leg4part2d", leg4part2X, leg4part2Z);
        moveXZ(processor, "leg4part2c", leg4part2X, leg4part2Z);
        moveXZ(processor, "leg4part2b", leg4part2X, leg4part2Z);
        moveXZ(processor, "leg4part2", leg4part2X, leg4part2Z);
        float leg4part2XRot = -1.152f + upangle;
        rotateX(processor, "leg4part2", leg4part2XRot);
        rotateX(processor, "leg4part2b", -0.743f + upangle);
        rotateX(processor, "leg4part2c", -0.632f + upangle);
        rotateX(processor, "leg4part2d", -1.041f + upangle);
        dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(leg4part2XRot));
        float leg4part3Z = (float) ((double) leg4part2Z - Math.cos(leg4YRot) * (double) dist);  // leg4part2.yRot = leg4.yRot
        float leg4part3X = (float) ((double) leg4part2X - Math.sin(leg4YRot) * (double) dist);
        moveXZ(processor, "leg4part3", leg4part3X, leg4part3Z);
        float leg4part3XRot = 0.669f - upangle;
        rotateX(processor, "leg4part3", leg4part3XRot);
        dist = 8.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg4part3XRot));
        float leg4part3bZ = (float) ((double) leg4part3Z - Math.cos(leg4YRot) * (double) dist);  // leg4part3.yRot = leg4.yRot
        float leg4part3bX = (float) ((double) leg4part3X - Math.sin(leg4YRot) * (double) dist);
        moveXZ(processor, "leg4part3c", leg4part3bX, leg4part3bZ);
        moveXZ(processor, "leg4part3b", leg4part3bX, leg4part3bZ);
        rotateX(processor, "leg4part3b", -0.48f - upangle);
        rotateX(processor, "leg4part3c", -0.48f - upangle);
    }

    /** SpitBugModel.doLeftRearLeg verbatim on the leg3 chain. */
    private static void doLeftRearLeg(AnimationProcessor<?> processor, float angle, float upangle) {
        float leg3YRot = -2.1f + angle;
        rotateY(processor, "leg3", leg3YRot);
        rotateY(processor, "leg3part2", leg3YRot);
        rotateY(processor, "leg3part2b", leg3YRot);
        rotateY(processor, "leg3part2c", leg3YRot);
        rotateY(processor, "leg3part2d", leg3YRot);
        rotateY(processor, "leg3part3", leg3YRot);
        rotateY(processor, "leg3part3b", leg3YRot);
        rotateY(processor, "leg3part3c", leg3YRot);
        GeoBone leg3 = bone(processor, "leg3");
        float dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(classicRotX(leg3)));  // leg3.xRot is never written: the bind
        float[] leg3Pivot = classicPosition(leg3);  // never written: the bind pivot
        float leg3part2Z = (float) ((double) leg3Pivot[2] - Math.cos(leg3YRot) * (double) dist);
        float leg3part2X = (float) ((double) leg3Pivot[0] - Math.sin(leg3YRot) * (double) dist);
        moveXZ(processor, "leg3part2d", leg3part2X, leg3part2Z);
        moveXZ(processor, "leg3part2c", leg3part2X, leg3part2Z);
        moveXZ(processor, "leg3part2b", leg3part2X, leg3part2Z);
        moveXZ(processor, "leg3part2", leg3part2X, leg3part2Z);
        float leg3part2XRot = -1.152f + upangle;
        rotateX(processor, "leg3part2", leg3part2XRot);
        rotateX(processor, "leg3part2b", -0.743f + upangle);
        rotateX(processor, "leg3part2c", -0.632f + upangle);
        rotateX(processor, "leg3part2d", -1.041f + upangle);
        dist = 14.0f;
        dist = (float) ((double) dist * Math.cos(leg3part2XRot));
        float leg3part3Z = (float) ((double) leg3part2Z - Math.cos(leg3YRot) * (double) dist);  // leg3part2.yRot = leg3.yRot
        float leg3part3X = (float) ((double) leg3part2X - Math.sin(leg3YRot) * (double) dist);
        moveXZ(processor, "leg3part3", leg3part3X, leg3part3Z);
        float leg3part3XRot = 0.669f - upangle;
        rotateX(processor, "leg3part3", leg3part3XRot);
        dist = 8.0f;
        dist = (float) Math.abs((double) dist * Math.cos(leg3part3XRot));
        float leg3part3bZ = (float) ((double) leg3part3Z - Math.cos(leg3YRot) * (double) dist);  // leg3part3.yRot = leg3.yRot
        float leg3part3bX = (float) ((double) leg3part3X - Math.sin(leg3YRot) * (double) dist);
        moveXZ(processor, "leg3part3c", leg3part3bX, leg3part3bZ);
        moveXZ(processor, "leg3part3b", leg3part3bX, leg3part3bZ);
        rotateX(processor, "leg3part3b", -0.48f - upangle);
        rotateX(processor, "leg3part3c", -0.48f - upangle);
    }

    /**
     * {@code part.xRot} read back in classic terms for a part the classic never writes: the bind the bake gave the bone
     * (internal X is the negated classic X, the base's basis fact; no controller runs on the classic source and GeckoLib
     * holds an untouched bone at its bake).
     */
    private static float classicRotX(GeoBone bone) {
        return -bone.getRotX();
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntitySpitBug, SpitBugGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new SpitBugGeoReplacement());
        }
    }
}
