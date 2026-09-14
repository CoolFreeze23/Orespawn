package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.pose.AlosaurusPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Alosaurus (the hook lanes, 2026-09-14, addendum item 10; landed by the fourth Tier-2 slice T2d, 2026-09-14,
 * the owner's item 9): {@link ModelAlosaurus#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). The classic model carries its wingspeed inline ({@code 1.3f * 0.22f}, orig
 * ClientProxyOreSpawn.java:428's 0.22f): the THRESHOLD gait on the eight leg parts ({@code cos(age * 1.3 * 0.22) * PI *
 * 0.25 * limbSwingAmount} above a walking speed of a tenth, the right leg positive around -0.174 / 0.506 / -0.401 / 0,
 * the left the negative); the ATTACKING-branch jaw ({@code 0.52 + cos(age * 0.45) * PI * 0.18} attacking, 0.1 at rest);
 * and the two forelimbs' always-on sway ({@code cos(age * 0.1) * PI * 0.05} around -0.523). The entity is read through
 * {@link AlosaurusPose}.
 *
 * <p>Shadow follows {@link AlosaurusRenderer}'s constructor ({@code super(context, model, 1.0f)}: the literal it passes -
 * it declares no SHADOW constant); its render scale is a private 1.0 applied around {@code super.render} - the identity,
 * so no scale hook.</p>
 */
public final class AlosaurusGeoReplacement extends OreSpawnGeoReplacement<Alosaurus> {
    private static final GeoReplacementDescriptor<Alosaurus> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ALOSAURUS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Alosaurus.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/alosaurus.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/alosaurus.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/alosaurus.png"),
            // AlosaurusRenderer's constructor passes the literal 1.0f (no SHADOW constant): the equal literal
            1.0F) {
    };

    public AlosaurusGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        AlosaurusPose entity = inputs.subject(AlosaurusPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelAlosaurus.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right; the
        // forelimbs are the rig's shape17 (right) and shape11 (left).
        float newangle = 0.0f;
        if (limbSwingAmount > 0.1f) {
            newangle = Mth.cos(ageInTicks * 1.3f * 0.22f) * (float) Math.PI * 0.25f * limbSwingAmount;
        }
        rotateX(processor, "rightleg", -0.174f + newangle);
        rotateX(processor, "rightleg2", 0.506f + newangle);
        rotateX(processor, "rightleg3", -0.401f + newangle);
        rotateX(processor, "rightleg4", newangle);
        rotateX(processor, "leftleg", -0.174f - newangle);
        rotateX(processor, "leftleg2", 0.506f - newangle);
        rotateX(processor, "leftleg3", -0.401f - newangle);
        rotateX(processor, "leftleg4", -newangle);
        rotateX(processor, "jaw", entity.getAttacking() != 0
                ? 0.52f + Mth.cos(ageInTicks * 0.45f) * (float) Math.PI * 0.18f
                : 0.1f);
        rotateX(processor, "shape17", -0.523f + Mth.cos(ageInTicks * 0.1f) * (float) Math.PI * 0.05f);
        rotateX(processor, "shape11", -0.523f + Mth.cos(ageInTicks * 0.1f) * (float) Math.PI * 0.05f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Alosaurus, AlosaurusGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new AlosaurusGeoReplacement());
        }
    }
}
