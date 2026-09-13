package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Skate;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Skate (the third Tier-2 slice, 2026-09-13): {@link ModelSkate#setupAnim} verbatim on the converted rig, ON
 * THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays
 * closed until an artist delivers {@code idle} and {@code walk}). One channel with the THRESHOLD idiom (orig
 * ModelSkate.java:48-49): above a walking speed of a tenth the tail tip pitches by {@code cos(age * 1.2f) * PI * 0.15f
 * * limbSwingAmount}, at or below it by the slow {@code cos(age * 0.4f) * PI * 0.05f}, about a 0.785 rad rest (the
 * second set's item 4 (c): the Cricket, and this rig with it, lands under the threshold as the code has it - no
 * {@code limb_swing_threshold} property).
 *
 * <p>Scale and shadow follow {@link SkateRenderer}: 0.75 render scale and a 0.1 x 0.75 shadow (ENT-S-092).</p>
 */
public final class SkateGeoReplacement extends OreSpawnGeoReplacement<Skate> {
    private static final GeoReplacementDescriptor<Skate> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.SKATE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Skate.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/skate.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/skate.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/skate.png"),
            SkateRenderer.SHADOW) {
        @Override
        public void applyScale(Skate entity, PoseStack poseStack, float partialTick) {
            // orig preRenderScale: GL11.glScalef(scale, scale, scale), the LivingEntityRenderer.scale slot (SkateRenderer.scale)
            poseStack.scale(SkateRenderer.SCALE, SkateRenderer.SCALE, SkateRenderer.SCALE);
        }
    };

    public SkateGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelSkate.setupAnim verbatim: moving -> cos(f2*1.2)*PI*0.15*f1, idle -> cos(f2*0.4)*PI*0.05; tip rests at 0.785
        float newangle = (double) limbSwingAmount > 0.1
                ? Mth.cos(ageInTicks * 1.2F) * (float) Math.PI * 0.15F * limbSwingAmount
                : Mth.cos(ageInTicks * 0.4F) * (float) Math.PI * 0.05F;
        rotateX(processor, "Shape1", 0.785F + newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Skate, SkateGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new SkateGeoReplacement());
        }
    }
}
