package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.WaterDragon;
import danger.orespawn.entity.pose.WaterDragonPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Water Dragon (the hooks, landed by the second Tier-1 slice T1b): {@link ModelWaterDragon#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk}; the geo, the wiring and the proofs landed with T1b). Wingspeed 0.5f (orig
 * ModelWaterDragon.java:14,41 / ClientProxyOreSpawn.java:436): the THRESHOLD idiom on the four legs about
 * Y - above a walking speed of a tenth {@code cos(age x 1.3 x ws) x PI x 0.2 x limbSwingAmount} about +-0.58
 * rad, 0 at or below it - and the GAIT-scaled body wave, no threshold: body3, body4, tail1 and the three tail plates yaw
 * on the same 1.3 x ws cosine at 0.4 x PI x limbSwingAmount a quarter turn apart ({@code - k x pi/4}), each
 * link's pivot FOLLOWING its parent 7 / 5 / 3 units along (sin, cos) of the parent's yaw (the POSITION-write idiom, x and
 * z through {@link #moveXZ}; body3's pivot is never written - the bind); the ears about Y on 0.8 x ws at 0.1 x PI about
 * +-0.62 rad plus the head yaw; the body fin (Z, 0.7 x ws, 0.02 x PI) and the neck fin (Y, 0.6 x ws, 0.1 x PI) stilled by
 * the SITTING check (orig :222-232); the jaw's three-way ATTACKING branch about X (orig :236: 1 bites on 1.2 x ws at 0.25
 * x PI, 2 holds 0.45 rad, else -0.25); and the HEAD-LOOK idiom {@code toRadians(netHeadYaw) x 0.75} on the head,
 * nose, jaw, head fin and both ears, the nose / jaw / head fin / ears' pivots following the head 8 / 7 / 3 / sqrt 13 /
 * sqrt 20 units along (sin, cos) of the head yaw (+-pi/4 for the ears; the head's pivot is never written - the bind). The
 * entity is read through {@link WaterDragonPose} (the Slice 4b form). The head fin, both ears, the neck fin and the body fin
 * are zero-thickness cubes (ENT-S-161; the classic face order required below).
 *
 * <p>Scale and shadow follow {@link WaterDragonRenderer}: 1.1 render scale, halved for a baby, and a 0.85 x 1.1 shadow
 * (ENT-S-092).</p>
 */
public final class WaterDragonGeoReplacement extends OreSpawnGeoReplacement<WaterDragon> {
    /** orig ModelWaterDragon.java:14,41 {@code wingspeed} = 0.5f (ClientProxyOreSpawn.java:436): the chain's third multiply. */
    static final float WINGSPEED = 0.5F;
    private static final GeoReplacementDescriptor<WaterDragon> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.WATER_DRAGON.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            WaterDragon.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/waterdragon.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/waterdragon.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/waterdragon.png"),
            WaterDragonRenderer.SHADOW) {
        @Override
        public void applyScale(WaterDragon entity, PoseStack poseStack, float partialTick) {
            // orig RenderWaterDragon.preRenderScale (:39-45): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(WaterDragonRenderer.SCALE / 2.0F, WaterDragonRenderer.SCALE / 2.0F, WaterDragonRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(WaterDragonRenderer.SCALE, WaterDragonRenderer.SCALE, WaterDragonRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (headfin 0 x 10 x 9, rightear and leftear 0 x 5 x 5, neackfin 0 x 5 x 5,
         * Bodyfin 0 x 10 x 9): the shipped geo carries the classic within-cube face order ({@link FaceOrder#KEY};
         * TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public WaterDragonGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        WaterDragonPose entity = inputs.subject(WaterDragonPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelWaterDragon.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right; every
        // value the classic reads back from a part it just wrote is held in a local, the never-written pivots (body3,
        // Head: the binds) are read through classicPosition, and a part the classic writes twice is written twice.
        float newangle = 0.0f;
        float pi4 = 0.7853982f;
        float root13 = (float) Math.sqrt(13.0);
        float root20 = (float) Math.sqrt(20.0);
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.2f * limbSwingAmount : 0.0f;
        float body3Y = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
        rotateY(processor, "body3", body3Y);
        float[] body3 = classicPosition(bone(processor, "body3"));  // never written: the bind pivot
        float body4Z = body3[2] + (float) Math.cos(body3Y) * 7.0f;
        float body4X = body3[0] - 1.0f + (float) Math.sin(body3Y) * 7.0f;
        moveXZ(processor, "body4", body4X, body4Z);
        float body4Y = Mth.cos(ageInTicks * 1.3f * WINGSPEED - pi4) * (float) Math.PI * 0.4f * limbSwingAmount;
        rotateY(processor, "body4", body4Y);
        float tail1Z = body4Z + (float) Math.cos(body4Y) * 5.0f;
        float tail1X = body4X + (float) Math.sin(body4Y) * 5.0f;
        moveXZ(processor, "tail1", tail1X, tail1Z);
        float tail1Y = Mth.cos(ageInTicks * 1.3f * WINGSPEED - 2.0f * pi4) * (float) Math.PI * 0.4f * limbSwingAmount;
        rotateY(processor, "tail1", tail1Y);
        float tailmiddleZ = tail1Z + (float) Math.cos(tail1Y) * 3.0f;
        float tailmiddleX = tail1X + (float) Math.sin(tail1Y) * 3.0f;
        moveXZ(processor, "tailmiddle", tailmiddleX, tailmiddleZ);
        float tailmiddleY = Mth.cos(ageInTicks * 1.3f * WINGSPEED - 3.0f * pi4) * (float) Math.PI * 0.4f * limbSwingAmount;
        rotateY(processor, "tailmiddle", tailmiddleY);
        rotateY(processor, "tailtop", tailmiddleY);
        moveXZ(processor, "tailtop", tailmiddleX, tailmiddleZ);
        rotateY(processor, "tailbottom", tailmiddleY);
        moveXZ(processor, "tailbottom", tailmiddleX, tailmiddleZ);
        rotateY(processor, "Leg8", 0.58f + newangle);
        rotateY(processor, "Leg2", -0.58f + newangle);
        rotateY(processor, "Leg7", -0.58f - newangle);
        rotateY(processor, "Leg1", 0.58f - newangle);
        newangle = Mth.cos(ageInTicks * 0.8f * WINGSPEED) * (float) Math.PI * 0.1f;
        float leftearY = 0.62f + newangle;
        rotateY(processor, "leftear", leftearY);
        float rightearY = -0.62f - newangle;
        rotateY(processor, "rightear", rightearY);
        newangle = Mth.cos(ageInTicks * 0.7f * WINGSPEED) * (float) Math.PI * 0.02f;
        if (entity.isInSittingPose()) {
            newangle = 0.0f;
        }
        rotateZ(processor, "Bodyfin", newangle);
        newangle = Mth.cos(ageInTicks * 0.6f * WINGSPEED) * (float) Math.PI * 0.1f;
        if (entity.isInSittingPose()) {
            newangle = 0.0f;
        }
        rotateY(processor, "neackfin", newangle);
        newangle = Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.05f;
        if (entity.isInSittingPose()) {
            newangle = 0.0f;
        }
        rotateY(processor, "headfin", newangle);
        rotateX(processor, "jaw", entity.getAttacking() == 1 ? (newangle = Mth.cos(ageInTicks * 1.2f * WINGSPEED) * (float) Math.PI * 0.25f) : (entity.getAttacking() == 2 ? 0.45f : -0.25f));
        float headY = newangle = (float) Math.toRadians(netHeadYaw) * 0.75f;
        rotateY(processor, "Head", headY);
        float[] head = classicPosition(bone(processor, "Head"));  // never written: the bind pivot
        rotateY(processor, "nose", newangle);
        moveXZ(processor, "nose", head[0] - (float) Math.sin(headY) * 8.0f, head[2] - (float) Math.cos(headY) * 8.0f);
        rotateY(processor, "jaw", newangle);
        moveXZ(processor, "jaw", head[0] - (float) Math.sin(headY) * 7.0f - 1.0f, head[2] - (float) Math.cos(headY) * 7.0f);
        rotateY(processor, "headfin", newangle);
        moveXZ(processor, "headfin", head[0] - (float) Math.sin(headY) * 3.0f, head[2] - (float) Math.cos(headY) * 3.0f);
        rotateY(processor, "leftear", leftearY + newangle);
        moveXZ(processor, "leftear", head[0] - (float) Math.sin(headY - pi4) * root13, head[2] - (float) Math.cos(headY - pi4) * root13);
        rotateY(processor, "rightear", rightearY + newangle);
        moveXZ(processor, "rightear", head[0] - (float) Math.sin(headY + pi4) * root20, head[2] - (float) Math.cos(headY + pi4) * root20);
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<WaterDragon, WaterDragonGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new WaterDragonGeoReplacement());
        }
    }
}
