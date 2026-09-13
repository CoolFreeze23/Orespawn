package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Basilisk;
import danger.orespawn.entity.pose.BasiliskPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Basilisk (the hook lanes, 2026-09-14, addendum item 10): {@link ModelBasilisk#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). Wingspeed 0.3f (orig ModelBasilisk.java:14,38 / ClientProxyOreSpawn.java:420):
 * the GAIT-scaled serpentine ({@code cos(age * 1.3 ws - k * pi4) * PI * 0.1 * limbSwingAmount}, no threshold) down the
 * ten-ring body and tail chain, each ring a quarter turn behind the last and its pivot FOLLOWING the last ring's yaw
 * through {@link #moveTo} (12 / 11 / 12 / 12 / 12 / 12 / 10 / 10 / 10 units along cos / sin, the tail rings a further
 * 0.5 / 1.0 / 1.5 / 1.0 / 1.0 units back); and the ATTACKING-branch jaw ({@code -1.0 + cos(age * 0.45) * PI * 0.18}
 * attacking, -1.1 at rest). The entity is read through {@link BasiliskPose}.
 *
 * <p>Scale and shadow follow {@link BasiliskRenderer}: a 0.5 x 1.25 shadow (ENT-S-092) and a private 1.25 render
 * scale, halved for a baby, applied around {@code super.render} (a uniform scale commutes with the rotations and the
 * flip, so the scale slot carries it; the equal literal is carried here).</p>
 */
public final class BasiliskGeoReplacement extends OreSpawnGeoReplacement<Basilisk> {
    /** orig ModelBasilisk.java:14,38 {@code wingspeed} = 0.3f (ClientProxyOreSpawn.java:420): the chain's third multiply. */
    static final float WINGSPEED = 0.3F;
    /** orig ClientProxyOreSpawn.java:420 {@code new RenderBasilisk(new ModelBasilisk(0.3f), 0.5f, 1.25f)}: BasiliskRenderer's private SCALE. */
    static final float SCALE = 1.25F;
    private static final GeoReplacementDescriptor<Basilisk> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.BASILISK.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Basilisk.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/basilisk.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/basilisk.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/basilisk.png"),
            BasiliskRenderer.SHADOW) {
        @Override
        public void applyScale(Basilisk entity, PoseStack poseStack, float partialTick) {
            // BasiliskRenderer.render: float scale = entity.isBaby() ? SCALE / 2.0f : SCALE; poseStack.scale(scale, scale, scale)
            float scale = entity.isBaby() ? SCALE / 2.0f : SCALE;
            poseStack.scale(scale, scale, scale);
        }
    };

    public BasiliskGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** {@code part.x = x; part.z = z} with the part's y left at the bind (every ring of the chain writes x and z). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] pos = classicPosition(bone(processor, name));
        moveTo(processor, name, x, pos[1], z);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        BasiliskPose entity = inputs.subject(BasiliskPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelBasilisk.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right; body1's
        // pivot is never written (the bind), every value read back from a ring just written is held in a local.
        float pi4 = 0.7853975F;
        float[] body1 = classicPosition(bone(processor, "body1"));
        float body1YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "body1", body1YRot);
        float body2X = body1[0] + (float) Math.cos(body1YRot) * 12.0F;
        float body2Z = body1[2] + (float) Math.sin(body1YRot) * 12.0F;
        moveXZ(processor, "body2", body2X, body2Z);
        float body2YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "body2", body2YRot);
        float body3X = body2X + (float) Math.cos(body2YRot) * 11.0F;
        float body3Z = body2Z + (float) Math.sin(body2YRot) * 11.0F;
        moveXZ(processor, "body3", body3X, body3Z);
        float body3YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 2.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "body3", body3YRot);
        float body4X = body3X + (float) Math.cos(body3YRot) * 12.0F;
        float body4Z = body3Z + (float) Math.sin(body3YRot) * 12.0F;
        moveXZ(processor, "body4", body4X, body4Z);
        float body4YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 3.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "body4", body4YRot);
        float body5X = body4X + (float) Math.cos(body4YRot) * 12.0F;
        float body5Z = body4Z + (float) Math.sin(body4YRot) * 12.0F;
        moveXZ(processor, "body5", body5X, body5Z);
        float body5YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 4.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "body5", body5YRot);
        float body6X = body5X + (float) Math.cos(body5YRot) * 12.0F;
        float body6Z = body5Z + 0.5F + (float) Math.sin(body5YRot) * 12.0F;
        moveXZ(processor, "body6", body6X, body6Z);
        float body6YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 5.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "body6", body6YRot);
        float tail1X = body6X + (float) Math.cos(body6YRot) * 12.0F;
        float tail1Z = body6Z + 1.0F + (float) Math.sin(body6YRot) * 12.0F;
        moveXZ(processor, "tail1", tail1X, tail1Z);
        float tail1YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 6.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "tail1", tail1YRot);
        float tail2X = tail1X + (float) Math.cos(tail1YRot) * 10.0F;
        float tail2Z = tail1Z + 1.5F + (float) Math.sin(tail1YRot) * 10.0F;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 7.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "tail2", tail2YRot);
        float tail3X = tail2X + (float) Math.cos(tail2YRot) * 10.0F;
        float tail3Z = tail2Z + 1.0F + (float) Math.sin(tail2YRot) * 10.0F;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos(ageInTicks * 1.3F * WINGSPEED - 8.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        rotateY(processor, "tail3", tail3YRot);
        float tail4X = tail3X + (float) Math.cos(tail3YRot) * 10.0F;
        float tail4Z = tail3Z + 1.0F + (float) Math.sin(tail3YRot) * 10.0F;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        rotateY(processor, "tail4", Mth.cos(ageInTicks * 1.3F * WINGSPEED - 9.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount);
        rotateX(processor, "jaw", entity.getAttacking() != 0 ? -1.0F + Mth.cos(ageInTicks * 0.45F) * (float) Math.PI * 0.18F : -1.1F);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Basilisk, BasiliskGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BasiliskGeoReplacement());
        }
    }
}
