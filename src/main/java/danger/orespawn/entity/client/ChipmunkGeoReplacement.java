package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Chipmunk;
import danger.orespawn.entity.pose.ChipmunkPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Chipmunk (the hooks): {@link ModelChipmunk#poseFrom} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}).
 * ANIM_SPEED 1.0f (ModelChipmunk.java:20; orig ModelChipmunk.java: 15,36 wingspeed 1.0f from ClientProxyOreSpawn.java:448):
 * the THRESHOLD idiom on the four legs about X - above a walking speed of a tenth {@code cos(age * 2.3f) * PI * 0.25f *
 * limbSwingAmount}, 0 at or below it (orig :204-211); the HEAD-LOOK idiom about Y at 0.45 of {@code toRadians(netHeadYaw)}
 * on the nine head parts (orig :160-167; no clamp, no pitch); the SITTING branch (orig :168-173, read through {@link
 * ChipmunkPose}): while not sitting the tail pitches on a 0.25 cosine around 0.306 rad plus a gait-scaled 1.3 cosine,
 * the second segment 0.306 rad further; and the two hat parts hidden every frame (through {@link #setVisible}). While
 * sitting the classic leaves the tail's pitch where the last frame left it (a singleton-model latch); the hook leaves
 * the two bones at bind.
 *
 * <p>Scale and shadow follow {@link ChipmunkRenderer}: 0.9 render scale, halved for a baby, and a 0.15 x 0.9 shadow
 * (ENT-S-092).</p>
 */
public final class ChipmunkGeoReplacement extends OreSpawnGeoReplacement<Chipmunk> {
    /** ModelChipmunk.ANIM_SPEED = 1.0f (orig ModelChipmunk.java:15,36 {@code wingspeed}, ClientProxyOreSpawn.java:448). */
    static final float ANIM_SPEED = 1.0F;
    private static final GeoReplacementDescriptor<Chipmunk> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CHIPMUNK.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Chipmunk.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/chipmunk.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/chipmunk.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/chipmunk.png"),
            ChipmunkRenderer.SHADOW) {
        @Override
        public void applyScale(Chipmunk entity, PoseStack poseStack, float partialTick) {
            // orig RenderChipmunk.preRenderScale (:42-48): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(ChipmunkRenderer.SCALE / 2.0F, ChipmunkRenderer.SCALE / 2.0F, ChipmunkRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(ChipmunkRenderer.SCALE, ChipmunkRenderer.SCALE, ChipmunkRenderer.SCALE);
        }
    };

    public ChipmunkGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ChipmunkPose entity = inputs.subject(ChipmunkPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelChipmunk.poseFrom (the body of the classic setupAnim) verbatim.
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 2.3F * ANIM_SPEED) * (float) Math.PI * 0.25F * limbSwingAmount
                : 0.0F;
        rotateX(processor, "leg1", newangle);
        rotateX(processor, "leg3", newangle);
        rotateX(processor, "leg2", -newangle);
        rotateX(processor, "leg4", -newangle);

        float headYawRad = (float) Math.toRadians(netHeadYaw) * 0.45F;
        rotateY(processor, "nose", headYawRad);
        rotateY(processor, "head", headYawRad);
        rotateY(processor, "ear1", headYawRad);
        rotateY(processor, "ear2", headYawRad);
        rotateY(processor, "mouth_under", headYawRad);
        rotateY(processor, "cheek1", headYawRad);
        rotateY(processor, "cheek2", headYawRad);
        rotateY(processor, "hat1", headYawRad);
        rotateY(processor, "hat2", headYawRad);

        if (!entity.isInSittingPose()) {
            float tail1XRot = 0.306F + Mth.cos(ageInTicks * 0.25F) * (float) Math.PI * 0.06F;
            newangle = Mth.cos(ageInTicks * 1.3F * ANIM_SPEED) * (float) Math.PI * 0.25F * limbSwingAmount;
            tail1XRot += newangle;
            rotateX(processor, "tail1", tail1XRot);
            rotateX(processor, "tail2", 0.306F + tail1XRot);
        }

        setVisible(processor, "hat1", false);
        setVisible(processor, "hat2", false);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Chipmunk, ChipmunkGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new ChipmunkGeoReplacement());
        }
    }
}
