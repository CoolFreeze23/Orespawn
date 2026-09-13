package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCricket;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cricket (the third Tier-2 slice, 2026-09-13): {@link CricketModel#setupAnim} verbatim on the converted rig,
 * ON THE HOOK (owner 2026-09-13, second set, item 4 (c) and Amendment 2 to Amendment 1: the threshold question closed
 * with option (c) - the Cricket lands through the seam on its classic hook, no {@code limb_swing_threshold} property,
 * no keyframe layer, no transcription; the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk}). The port's classic model as it is: the THRESHOLD idiom on the four front / rear legs' yaw - above a
 * walking speed of a tenth {@code cos(age * 1.0f) * PI * 0.25f * limbSwingAmount} around their rest yaws (0.47,
 * -0.54, -0.296, 0.384 rad), 0 at or below it (orig ModelCricket.java:104-108, wingspeed 2.5f there where the port's
 * frequency is 1.0f - the register line of this slice records where the port's pose departs from the 1.7.10 one) -
 * and the four hind legs' constant yaws (+-0.436 / +-0.349 rad) and pitches (0.558 / -0.366 rad) written every frame
 * (orig :117-125, the not-singing branch).
 *
 * <p>Scale and shadow follow {@link CricketRenderer}: 0.5 render scale and a 0.15 x 0.5 shadow (ENT-S-092).</p>
 */
public final class CricketGeoReplacement extends OreSpawnGeoReplacement<EntityCricket> {
    private static final GeoReplacementDescriptor<EntityCricket> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_CRICKET.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityCricket.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cricket.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cricket.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cricket.png"),
            CricketRenderer.SHADOW) {
        @Override
        public void applyScale(EntityCricket entity, PoseStack poseStack, float partialTick) {
            // orig RenderCricket.preRenderScale: GL11.glScalef(scale, scale, scale) (CricketRenderer.scale)
            poseStack.scale(CricketRenderer.SCALE, CricketRenderer.SCALE, CricketRenderer.SCALE);
        }
    };

    public CricketGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // CricketModel.setupAnim verbatim.
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 1.0F) * (float) Math.PI * 0.25F * limbSwingAmount
                : 0.0F;
        rotateY(processor, "lfleg", 0.47F + newangle);
        rotateY(processor, "rfleg", -0.54F + newangle);
        rotateY(processor, "lrleg", -0.296F - newangle);
        rotateY(processor, "rrleg", 0.384F - newangle);

        rotateY(processor, "lleg1", 0.436F);
        rotateY(processor, "lleg2", 0.349F);
        rotateY(processor, "rleg1", -0.436F);
        rotateY(processor, "rleg2", -0.349F);
        rotateX(processor, "lleg1", 0.558F);
        rotateX(processor, "lleg2", -0.366F);
        rotateX(processor, "rleg1", 0.558F);
        rotateX(processor, "rleg2", -0.366F);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityCricket, CricketGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CricketGeoReplacement());
        }
    }
}
