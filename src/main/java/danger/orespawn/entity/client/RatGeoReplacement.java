package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRat;
import danger.orespawn.entity.pose.RatPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Rat (the hooks, owner 2026-09-14, addendum item 10): {@link RatModel#poseFrom} verbatim on the rig the
 * landing slice converts, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate
 * stays closed until an artist delivers {@code idle} and {@code walk}). The port's classic model as it is (wingspeed
 * 1.0, folded into the literals): the THRESHOLD idiom on the four legs about X - above a walking speed of a tenth
 * {@code cos(age x 1.7) x PI x 0.25 x limbSwingAmount}, the right-front and left-rear positive, the other two the
 * negative, 0 at or below it (orig ModelRat.java:111-115); the ATTACKING branch on the tail about Y (orig :116
 * {@code getAttacking() != 0}: {@code cos(age x 1.5) x PI x 0.25} attacking, {@code cos(age x 0.4) x PI x 0.05} at
 * rest) at 0.5 of it on the first link and 1.25 on the second; and the POSITION-write idiom: the second link's pivot
 * FOLLOWS the first 9 units along (sin, cos) of its yaw (x and z only, through {@link #moveXZ}; the first link's pivot
 * is never written - the bind, read through {@link #classicPosition}). The entity is read through {@link RatPose}
 * (the Slice 4b form).
 *
 * <p>Scale and shadow follow {@link RatRenderer}: 0.75 render scale and a 0.1 x 0.75 shadow (ENT-S-092).</p>
 */
public final class RatGeoReplacement extends OreSpawnGeoReplacement<EntityRat> {
    private static final GeoReplacementDescriptor<EntityRat> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_RAT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityRat.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/rat.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/rat.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rat.png"),
            RatRenderer.SHADOW) {
        @Override
        public void applyScale(EntityRat entity, PoseStack poseStack, float partialTick) {
            // orig RenderRat.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) (RatRenderer.scale)
            poseStack.scale(RatRenderer.SCALE, RatRenderer.SCALE, RatRenderer.SCALE);
        }
    };

    public RatGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        RatPose entity = inputs.subject(RatPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // RatModel.poseFrom (the body of the classic setupAnim) verbatim.
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 1.7f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateX(processor, "rfleg", legAngle);
        rotateX(processor, "lfleg", -legAngle);
        rotateX(processor, "rrleg", -legAngle);
        rotateX(processor, "lrleg", legAngle);

        float tailAngle = entity.getAttacking() != 0
                ? Mth.cos(ageInTicks * 1.5f) * (float) Math.PI * 0.25f
                : Mth.cos(ageInTicks * 0.4f) * (float) Math.PI * 0.05f;
        float tail1Y = tailAngle * 0.5f;
        rotateY(processor, "tail1", tail1Y);
        rotateY(processor, "tail2", tailAngle * 1.25f);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1Y) * 9.0f;
        float tail2X = tail1[0] + (float) Math.sin(tail1Y) * 9.0f;
        moveXZ(processor, "tail2", tail2X, tail2Z);
    }

    /** {@code part.x = x; part.z = z} in classic terms, the part's y left as it is (the bind: the classic never writes it). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityRat, RatGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RatGeoReplacement());
        }
    }
}
