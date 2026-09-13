package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cryolophosaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cryolophosaurus (the hooks, owner 2026-09-14, addendum item 10): {@link ModelCryolophosaurus#setupAnim}
 * verbatim on the converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.75f (orig
 * ModelCryolophosaurus.java:13,38 / ClientProxyOreSpawn.java:419): the THRESHOLD idiom on the eight leg parts about X -
 * above a walking speed of a tenth {@code cos(age * 1.3f * ws) * PI * 0.25f * limbSwingAmount} around the -0.279 / 0.384
 * / -0.68 / 0 rad rests, the left side the negative, 0 at or below it (orig :203-214) - and the jaw about X on a 0.28
 * cosine x 0.1 around -1.15 rad, always (orig :215; no wingspeed on that one).
 *
 * <p>Scale and shadow follow {@link CryolophosaurusRenderer}: 0.5 render scale and a 0.75 x 0.5 shadow (ENT-S-092; orig
 * RenderCryolophosaurus.java:23-24, 40). The renderer's SCALE is private and its shadow a constructor literal, so both are
 * the equal literals here.</p>
 */
public final class CryolophosaurusGeoReplacement extends OreSpawnGeoReplacement<Cryolophosaurus> {
    /** orig ModelCryolophosaurus.java:13,38 {@code wingspeed} = 0.75f (ClientProxyOreSpawn.java:419): the chain's third multiply. */
    static final float WINGSPEED = 0.75F;
    /** CryolophosaurusRenderer.SCALE (private) = 0.5f: orig RenderCryolophosaurus.java:24 {@code scale = par3}, ClientProxyOreSpawn.java:419. */
    static final float SCALE = 0.5F;
    private static final GeoReplacementDescriptor<Cryolophosaurus> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CRYOLOPHOSAURUS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Cryolophosaurus.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cryolophosaurus.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cryolophosaurus.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cryolophosaurus.png"),
            // orig RenderCryolophosaurus.java:23 super(model, par2 * par3) with 0.75f x 0.5f: the product
            // CryolophosaurusRenderer's constructor passes (it declares no SHADOW constant)
            0.75F * 0.5F) {
        @Override
        public void applyScale(Cryolophosaurus entity, PoseStack poseStack, float partialTick) {
            // orig RenderCryolophosaurus.preRenderScale (:40): GL11.glScalef(scale, scale, scale) (CryolophosaurusRenderer.render)
            poseStack.scale(SCALE, SCALE, SCALE);
        }
    };

    public CryolophosaurusGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelCryolophosaurus.setupAnim verbatim.
        float newangle = 0.0F;
        newangle = limbSwingAmount > 0.1F ? Mth.cos(ageInTicks * 1.3F * WINGSPEED) * (float) Math.PI * 0.25F * limbSwingAmount : 0.0F;
        rotateX(processor, "rightleg", -0.2792527F + newangle);
        rotateX(processor, "rightleg2", 0.384F + newangle);
        rotateX(processor, "rightleg3", -0.68F + newangle);
        rotateX(processor, "rightleg4", newangle);
        rotateX(processor, "leftleg", -0.2792527F - newangle);
        rotateX(processor, "leftleg2", 0.384F - newangle);
        rotateX(processor, "leftleg3", -0.68F - newangle);
        rotateX(processor, "leftleg4", -newangle);
        rotateX(processor, "jaw", -1.15F + Mth.cos(ageInTicks * 0.28F) * (float) Math.PI * 0.1F);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Cryolophosaurus, CryolophosaurusGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CryolophosaurusGeoReplacement());
        }
    }
}
