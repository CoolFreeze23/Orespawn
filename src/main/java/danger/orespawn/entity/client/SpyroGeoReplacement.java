package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntitySpyro;
import danger.orespawn.entity.pose.SpyroPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Spyro (the hooks, owner 2026-09-14, addendum item 10): {@link SpyroModel#poseFrom} verbatim on the rig the
 * landing slice converts, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate
 * stays closed until an artist delivers {@code idle} and {@code walk}). The port's classic model as it is: its
 * frequency multiplier {@code ws} is {@code limbSwingAmount} itself (SpyroModel.poseFrom {@code float ws =
 * limbSwingAmount}; orig ModelSpyro.java ran on a wingspeed of 0.65f, ClientProxyOreSpawn.java:427 - the register
 * line of the landing slice records where the port's pose departs from the 1.7.10 one) - the THRESHOLD idiom on the
 * wings about Z ({@code cos(age x 2.3 x ws) x PI x 0.4 x limbSwingAmount} above a walking speed of a tenth, mirrored,
 * halved by the ACTIVITY branch while flying (3)) and on the twelve leg parts about X ({@code cos(age x 2.0 x ws) x PI
 * x 0.25 x limbSwingAmount}, zeroed while flying, alternating about the rests -0.087 / -0.17 / 0 / 0.139 / -0.174;
 * activity 2 folds them at -1 rad (front) / +1 rad (back)); the tail about Y on {@code cos(age x 1.2 x ws) x PI x 0.25},
 * stilled by the SITTING check or flying, the front link at 1.6 of it and its pivot FOLLOWING the back link 3 units
 * along (sin, cos) of the back link's yaw, less 0.5 in x (the POSITION-write idiom, x and z through {@link #moveXZ};
 * the back link's pivot is never written - the bind), the two tail pieces riding the front link; and the HEAD-LOOK
 * idiom in radians on twelve head parts, the horns offset +-0.785 rad in yaw and -0.785 in pitch. The entity is read
 * through {@link SpyroPose} (the Slice 4b form). The two wings are zero-thickness cubes (ENT-S-161; the classic face
 * order required below).
 *
 * <p>Scale and shadow follow {@link SpyroRenderer}: 0.75 render scale and a 0.65 x 0.75 shadow (ENT-S-092).</p>
 */
public final class SpyroGeoReplacement extends OreSpawnGeoReplacement<EntitySpyro> {
    private static final GeoReplacementDescriptor<EntitySpyro> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_SPYRO.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntitySpyro.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/spyro.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/spyro.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/spyro.png"),
            SpyroRenderer.SHADOW) {
        @Override
        public void applyScale(EntitySpyro entity, PoseStack poseStack, float partialTick) {
            // orig RenderSpyro.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) (SpyroRenderer.scale)
            poseStack.scale(SpyroRenderer.SCALE, SpyroRenderer.SCALE, SpyroRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the two wings, 10 x 0 x 4): the shipped geo carries the classic within-cube
         * face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public SpyroGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        SpyroPose entity = inputs.subject(SpyroPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // SpyroModel.poseFrom (the body of the classic setupAnim) verbatim; every value the classic reads back from a
        // part it just wrote is held in a local, the never-written TailBack pivot (the bind) read through classicPosition.
        float ws = limbSwingAmount;
        int currentActivity = entity.getActivity();

        float newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.3F * ws) * (float) Math.PI * 0.4F * limbSwingAmount : 0.0F;
        if (currentActivity == 3) {
            newangle *= 0.5F;
        }
        rotateZ(processor, "WingLeft", newangle);
        rotateZ(processor, "WingRight", -newangle);

        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 2.0F * ws) * (float) Math.PI * 0.25F * limbSwingAmount : 0.0F;
        if (currentActivity == 3) {
            newangle = 0.0F;
        }

        if (currentActivity != 2) {
            rotateX(processor, "LegRightFrontTop", newangle - 0.087F);
            rotateX(processor, "LegRightFrontBottom", newangle - 0.17F);
            rotateX(processor, "RightFrontPaw", newangle);
            rotateX(processor, "LegLeftFrontTop", -newangle - 0.087F);
            rotateX(processor, "LegLeftFrontBottom", -newangle - 0.17F);
            rotateX(processor, "LeftFrontPaw", -newangle);
            rotateX(processor, "LegRightBackBottom", -newangle + 0.139F);
            rotateX(processor, "LegRightBackTop", -newangle - 0.174F);
            rotateX(processor, "RightBackPaw", -newangle);
            rotateX(processor, "LegLeftBackBottom", newangle + 0.139F);
            rotateX(processor, "LegLeftBackTop", newangle - 0.174F);
            rotateX(processor, "LeftBackPaw", newangle);
        } else {
            newangle = -1.0F;
            rotateX(processor, "LegRightFrontTop", newangle - 0.087F);
            rotateX(processor, "LegRightFrontBottom", newangle - 0.17F);
            rotateX(processor, "RightFrontPaw", newangle);
            rotateX(processor, "LegLeftFrontTop", newangle - 0.087F);
            rotateX(processor, "LegLeftFrontBottom", newangle - 0.17F);
            rotateX(processor, "LeftFrontPaw", newangle);
            newangle = 1.0F;
            rotateX(processor, "LegRightBackBottom", newangle + 0.139F);
            rotateX(processor, "LegRightBackTop", newangle - 0.174F);
            rotateX(processor, "RightBackPaw", newangle);
            rotateX(processor, "LegLeftBackBottom", newangle + 0.139F);
            rotateX(processor, "LegLeftBackTop", newangle - 0.174F);
            rotateX(processor, "LeftBackPaw", newangle);
        }

        newangle = Mth.cos(ageInTicks * 1.2F * ws) * (float) Math.PI * 0.25F;
        if (entity.isInSittingPose() || currentActivity == 3) {
            newangle = 0.0F;
        }
        rotateY(processor, "TailBack", newangle);
        rotateY(processor, "ScaleTailPiece", newangle);
        float[] tailBack = classicPosition(bone(processor, "TailBack"));  // never written: the bind pivot
        float tailFrontZ = tailBack[2] + (float) Math.cos(newangle) * 3.0F;
        float tailFrontX = tailBack[0] + (float) Math.sin(newangle) * 3.0F - 0.5F;
        moveXZ(processor, "TailFront", tailFrontX, tailFrontZ);
        float tailFrontY = newangle * 1.6F;
        rotateY(processor, "TailFront", tailFrontY);
        moveXZ(processor, "TailPieceLarge", tailFrontX, tailFrontZ);
        rotateY(processor, "TailPieceLarge", tailFrontY);
        moveXZ(processor, "TailPieceSmall", tailFrontX, tailFrontZ);
        rotateY(processor, "TailPieceSmall", tailFrontY);

        rotateY(processor, "HeadPieceTop", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "HeadPieceBottom", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "JawPiece", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "SnoutRight", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "SnoutLeft", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "ScaleTop1", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "ScaleHead", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "ScaleBackHead", (float) Math.toRadians(netHeadYaw));
        rotateY(processor, "HornRightBottom", (float) Math.toRadians(netHeadYaw) + 0.785F);
        rotateY(processor, "HornRightTop", (float) Math.toRadians(netHeadYaw) + 0.785F);
        rotateY(processor, "HornLeftBottom", (float) Math.toRadians(netHeadYaw) - 0.785F);
        rotateY(processor, "HornLeftTop", (float) Math.toRadians(netHeadYaw) - 0.785F);

        rotateX(processor, "HeadPieceTop", (float) Math.toRadians(headPitch));
        rotateX(processor, "HeadPieceBottom", (float) Math.toRadians(headPitch));
        rotateX(processor, "JawPiece", (float) Math.toRadians(headPitch));
        rotateX(processor, "SnoutRight", (float) Math.toRadians(headPitch));
        rotateX(processor, "SnoutLeft", (float) Math.toRadians(headPitch));
        rotateX(processor, "ScaleTop1", (float) Math.toRadians(headPitch));
        rotateX(processor, "ScaleHead", (float) Math.toRadians(headPitch));
        rotateX(processor, "ScaleBackHead", (float) Math.toRadians(headPitch));
        rotateX(processor, "HornRightBottom", (float) Math.toRadians(headPitch) - 0.785F);
        rotateX(processor, "HornRightTop", (float) Math.toRadians(headPitch) - 0.785F);
        rotateX(processor, "HornLeftBottom", (float) Math.toRadians(headPitch) - 0.785F);
        rotateX(processor, "HornLeftTop", (float) Math.toRadians(headPitch) - 0.785F);
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntitySpyro, SpyroGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new SpyroGeoReplacement());
        }
    }
}
