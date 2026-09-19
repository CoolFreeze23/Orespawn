package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Flounder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Flounder (the hooks, landed by the fourth Tier-2 slice T2d): {@link ModelFlounder#setupAnim} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription
 * - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). No wingspeed (orig
 * ModelFlounder.java has none): the THRESHOLD idiom with a live idle branch - above a walking speed of a tenth the two
 * fins roll on {@code cos(age * 1.3f) * PI * 0.25f * limbSwingAmount} and {@code cos(age * 1.7f) * PI * 0.25f *
 * limbSwingAmount} and the two tail parts pitch on a 1.2 cosine x 0.25 x limbSwingAmount; at or below it the fins hold
 * 0 and the tail sways on a 0.7 cosine x 0.05 (orig :91-103).
 *
 *
 * <p>Scale and shadow follow {@link FlounderRenderer}: 1.0 render scale, halved for a baby, and a 0.1 x 1.0 shadow
 * (ENT-S-092).</p>
 */
public final class FlounderGeoReplacement extends OreSpawnGeoReplacement<Flounder> {
    private static final GeoReplacementDescriptor<Flounder> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.FLOUNDER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Flounder.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/flounder.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/flounder.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/flounder.png"),
            FlounderRenderer.SHADOW) {
        @Override
        public void applyScale(Flounder entity, PoseStack poseStack, float partialTick) {
            // orig RenderFlounder.preRenderScale (:39-45): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            // (FlounderRenderer.scale)
            float s = entity.isBaby() ? FlounderRenderer.SCALE / 2.0F : FlounderRenderer.SCALE;
            poseStack.scale(s, s, s);
        }
    };

    public FlounderGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelFlounder.setupAnim verbatim, the float chain left to right.
        float newangle2;
        float newangle;
        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos((float) (ageInTicks * 1.3f)) * (float) Math.PI * 0.25f * limbSwingAmount;
            newangle2 = Mth.cos((float) (ageInTicks * 1.7f)) * (float) Math.PI * 0.25f * limbSwingAmount;
        } else {
            newangle = 0.0f;
            newangle2 = 0.0f;
        }
        rotateZ(processor, "lfin", newangle);
        rotateZ(processor, "rfin", newangle2);
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos((float) (ageInTicks * 1.2f)) * (float) Math.PI * 0.25f * limbSwingAmount : Mth.cos((float) (ageInTicks * 0.7f)) * (float) Math.PI * 0.05f;
        rotateX(processor, "tail2", newangle);
        rotateX(processor, "tail1", newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Flounder, FlounderGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new FlounderGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: swimmer} (tools/artist_specs/flounder.json): {@code inWater} is {@code isInWater()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.SWIMMER;
    }
}
