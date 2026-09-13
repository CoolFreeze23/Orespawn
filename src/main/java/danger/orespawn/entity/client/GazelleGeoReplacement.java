package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Gazelle;
import danger.orespawn.entity.pose.GazellePose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Gazelle (the hooks, owner 2026-09-14, addendum item 10): {@link ModelGazelle#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays closed
 * until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.65f (orig ModelGazelle.java:14,51 /
 * ClientProxyOreSpawn.java:449): the THRESHOLD idiom on the eighteen leg parts about X - above a walking speed of a tenth
 * {@code cos(age * 1.1f * ws) * PI * 0.12f * limbSwingAmount} around the 0.297 / -0.074 / -0.409 / 0 / 0.185 rad rests,
 * the diagonal pairs opposed, 0 at or below it (orig :342-363); the HEAD-LOOK idiom about Y at 0.45 of
 * {@code toRadians(netHeadYaw)} on the head, nose, mouth and the six antler parts, the ears at 1.57 rad plus it plus a
 * 0.5 cosine x 0.02 (orig :364-375; no clamp, no pitch); and the CROUCH branch (orig :297 {@code func_70906_o()}, the
 * port's {@code isCrouching()}, read through {@link GazellePose}): while not crouching the tail pitches on a 0.1 cosine x
 * 0.06 around 1.0 rad. Crouching, the classic leaves the tail's pitch where the last frame left it (a singleton-model
 * latch); the hook leaves the bone at bind.
 *
 * <p>Scale and shadow follow {@link GazelleRenderer}: 1.0 render scale, halved for a baby, and a 0.45 x 1.0 shadow
 * (ENT-S-092; orig RenderGazelle.java:23-24, 39-45). The renderer's SCALE is private, so the equal literal here.</p>
 */
public final class GazelleGeoReplacement extends OreSpawnGeoReplacement<Gazelle> {
    /** orig ModelGazelle.java:14,51 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:449): the chain's third multiply. */
    static final float WINGSPEED = 0.65F;
    /** GazelleRenderer.SCALE (private) = 1.0f: orig RenderGazelle.java:24 {@code scale = par3}, ClientProxyOreSpawn.java:449. */
    static final float SCALE = 1.0F;
    private static final GeoReplacementDescriptor<Gazelle> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GAZELLE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Gazelle.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/gazelle.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/gazelle.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gazelletexture.png"),
            GazelleRenderer.SHADOW) {
        @Override
        public void applyScale(Gazelle entity, PoseStack poseStack, float partialTick) {
            // orig RenderGazelle.preRenderScale (:39-45): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            // (GazelleRenderer.render: isBaby() ? SCALE / 2 : SCALE)
            float scale = entity.isBaby() ? SCALE / 2.0F : SCALE;
            poseStack.scale(scale, scale, scale);
        }
    };

    public GazelleGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        GazellePose entity = inputs.subject(GazellePose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelGazelle.poseFrom (the body of the classic setupAnim) verbatim.
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 1.1F * WINGSPEED) * (float) Math.PI * 0.12F * limbSwingAmount
                : 0.0F;

        rotateX(processor, "lfleg1", 0.297F + newangle);
        rotateX(processor, "lfleg2", -0.074F + newangle);
        rotateX(processor, "lfleg3", -0.409F + newangle);
        rotateX(processor, "lfleg4", newangle);
        rotateX(processor, "rfleg1", 0.297F - newangle);
        rotateX(processor, "rfleg2", -0.074F - newangle);
        rotateX(processor, "rfleg3", -0.409F - newangle);
        rotateX(processor, "rfleg4", -newangle);
        rotateX(processor, "lrleg1", -newangle);
        rotateX(processor, "lrleg2", 0.185F - newangle);
        rotateX(processor, "lrleg3", -0.074F - newangle);
        rotateX(processor, "lrleg4", -0.409F - newangle);
        rotateX(processor, "lrleg5", -newangle);
        rotateX(processor, "rrleg1", newangle);
        rotateX(processor, "rrleg2", 0.185F + newangle);
        rotateX(processor, "rrleg3", -0.074F + newangle);
        rotateX(processor, "rrleg4", -0.409F + newangle);
        rotateX(processor, "rrleg5", newangle);

        newangle = Mth.cos(ageInTicks * 0.5F) * (float) Math.PI * 0.02F;
        float headYRot = (float) Math.toRadians(netHeadYaw) * 0.45F;
        rotateY(processor, "head", headYRot);
        rotateY(processor, "nose", headYRot);
        rotateY(processor, "mouth", headYRot);
        rotateY(processor, "lear", 1.57F + headYRot + newangle);
        rotateY(processor, "rear", 1.57F + headYRot + newangle);
        rotateY(processor, "la1", headYRot);
        rotateY(processor, "la2", headYRot);
        rotateY(processor, "la3", headYRot);
        rotateY(processor, "ra1", headYRot);
        rotateY(processor, "ra2", headYRot);
        rotateY(processor, "ra3", headYRot);

        if (!entity.isCrouching()) {
            rotateX(processor, "tail", 1.0F + Mth.cos(ageInTicks * 0.1F) * (float) Math.PI * 0.06F);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Gazelle, GazelleGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GazelleGeoReplacement());
        }
    }
}
