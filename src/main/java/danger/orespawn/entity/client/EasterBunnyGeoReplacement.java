package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EasterBunny;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Easter Bunny (the hooks, landed by the fourth Tier-2 slice T2d): {@link ModelEasterBunny#setupAnim}
 * verbatim on the converted rig, ON THE HOOK (no keyframe layer, no transcription
 * - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.55f (orig
 * ModelEasterBunny.java:14,30 / ClientProxyOreSpawn.java:498): the THRESHOLD idiom with a live idle branch - above a
 * walking speed of a tenth the two legs and two feet pitch on {@code cos(age * 2.6f * ws) * PI * 0.15f *
 * limbSwingAmount} (the right pair the negative) and the ears on {@code cos(age * 1.3f * ws) * PI * 0.1f *
 * limbSwingAmount}; at or below it the legs hold 0 and the ears twitch on the same 1.3 ws cosine x 0.01 (orig
 * :151-167); the ears around -0.226 / -0.418 rad, the right one the negative.
 *
 * <p>Scale and shadow follow {@link EasterBunnyRenderer}: 1.0 render scale, halved for a baby, and a 0.5 x 1.0 shadow
 * (ENT-S-092).</p>
 */
public final class EasterBunnyGeoReplacement extends OreSpawnGeoReplacement<EasterBunny> {
    /** orig ModelEasterBunny.java:14,30 {@code wingspeed} = 0.55f (ClientProxyOreSpawn.java:498): the chain's third multiply. */
    static final float WINGSPEED = 0.55F;
    private static final GeoReplacementDescriptor<EasterBunny> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.EASTER_BUNNY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EasterBunny.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/easterbunny.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/easterbunny.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/easterbunny.png"),
            EasterBunnyRenderer.SHADOW) {
        @Override
        public void applyScale(EasterBunny entity, PoseStack poseStack, float partialTick) {
            // orig RenderEasterBunny.preRenderScale (:39-44): a child gets glScalef(scale / 2), adults scale = 1.0
            // (EasterBunnyRenderer.render: isBaby() ? scale(0.5f) : nothing)
            if (entity.isBaby()) {
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
        }
    };

    public EasterBunnyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelEasterBunny.setupAnim verbatim, the float chain left to right.
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos((float) (ageInTicks * 2.6f * WINGSPEED)) * (float) Math.PI * 0.15f * limbSwingAmount;
            newangle2 = Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.1f * limbSwingAmount;
        } else {
            newangle = 0.0f;
            newangle2 = Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.01f;
        }
        rotateX(processor, "lfoot", newangle);
        rotateX(processor, "lleg", newangle);
        rotateX(processor, "rfoot", -newangle);
        rotateX(processor, "rleg", -newangle);
        rotateX(processor, "lear", -0.226f + newangle2);
        rotateX(processor, "rear", -0.418f - newangle2);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EasterBunny, EasterBunnyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new EasterBunnyGeoReplacement());
        }
    }
}
