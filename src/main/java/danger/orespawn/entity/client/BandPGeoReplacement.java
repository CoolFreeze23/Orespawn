package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BandP;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Bandit / Pirate (the hook lanes, 2026-09-14, addendum item 10; landed by the fourth Tier-2 slice T2d,
 * 2026-09-14, the owner's item 9): {@link ModelBandP#setupAnim} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). Wingspeed 0.4f (orig ModelBandP.java:14,24 / ClientProxyOreSpawn.java:504):
 * the THRESHOLD idiom - above a walking speed of a tenth the legs swing at {@code cos(age * 1.3 ws) * PI * 0.25 *
 * limbSwingAmount} (mirrored), the arms the same swing (the left the negative), the belly bobs at {@code cos(age * 2.6
 * ws) * PI * 0.025 * limbSwingAmount} around 0.07 and twists on minus half the leg swing; at or below it the legs hold,
 * the belly breathes at {@code cos(age * 0.6 ws) * PI * 0.005} and the arms sway at {@code cos(age * 0.3 ws) * PI *
 * 0.02} - and the HEAD-LOOK idiom (yaw and pitch in radians). No entity state.
 *
 * <p>Scale and shadow follow {@link BandPRenderer}: 1.0 render scale (identity - no scale override, so no scale hook)
 * and a 1.0 x 1.0 shadow (ENT-S-092); the texture per {@code getWhat()} through {@link BandPRenderer#textureFor} (0 the
 * bandit, otherwise the pirate - the classic renderer's own switch, lifted into it by the landing slice as the Fairy's
 * {@code textureFor} was, so the two renderers cannot drift apart).</p>
 */
public final class BandPGeoReplacement extends OreSpawnGeoReplacement<BandP> {
    /** orig ModelBandP.java:14,24 {@code wingspeed} = 0.4f (ClientProxyOreSpawn.java:504): the chain's third multiply. */
    static final float WINGSPEED = 0.4F;
    private static final GeoReplacementDescriptor<BandP> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.BAND_P.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            BandP.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/bandp.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/bandp.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bandp_bandit.png"),  // the bandit sheet (BandPRenderer.TEXTURE_BANDIT):
            // a literal, as the Fairy form - a renderer call here would load a client class in the dedicated-server gametests
            BandPRenderer.SHADOW) {
        /** BandPRenderer.getTextureLocation: {@code textureFor(getWhat())} - 0 the bandit, otherwise the pirate. */
        @Override
        public ResourceLocation texture(BandP entity) {
            return BandPRenderer.textureFor(entity.getWhat());
        }
    };

    public BandPGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelBandP.setupAnim verbatim, the float chain left to right.
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount;
            newangle2 = Mth.cos(ageInTicks * 2.6f * WINGSPEED) * (float) Math.PI * 0.025f * limbSwingAmount;
            newangle3 = newangle;
        } else {
            newangle = 0.0f;
            newangle2 = Mth.cos(ageInTicks * 0.6f * WINGSPEED) * (float) Math.PI * 0.005f;
            newangle3 = Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.02f;
        }
        rotateX(processor, "lleg", newangle);
        rotateX(processor, "rleg", -newangle);
        rotateX(processor, "belly", 0.07f + newangle2);
        rotateX(processor, "larm", -newangle3);
        rotateX(processor, "rarm", newangle3);
        rotateY(processor, "belly", -newangle / 2.0f);
        rotateY(processor, "head", (float) Math.toRadians(netHeadYaw));
        rotateX(processor, "head", (float) Math.toRadians(headPitch));
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<BandP, BandPGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BandPGeoReplacement());
        }
    }
}
