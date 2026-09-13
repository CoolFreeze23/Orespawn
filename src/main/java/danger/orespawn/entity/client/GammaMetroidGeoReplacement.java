package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityGammaMetroid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Gamma Metroid (the third Tier-2 slice, 2026-09-13): {@link GammaMetroidModel#setupAnim} verbatim on the
 * converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). The port's classic model as it is:
 * the three tusks about X at 0.81 rad/tick, the first shell at 0.4 (a quarter of its swing) and the lower beak's
 * {@code |cos|} idiom at 0.75 plus 0.14 rad (orig ModelGammaMetroid.java:175, :193-197, :204-206 for the lines the port
 * keeps; the port's model carries no wingspeed multiply - the register line of this slice records where the port's
 * pose departs from the 1.7.10 one).
 *
 * <p>Scale and shadow follow {@link GammaMetroidRenderer}: 0.9 render scale, halved for a baby, and a 0.75 x 0.9 shadow
 * (ENT-S-092).</p>
 */
public final class GammaMetroidGeoReplacement extends OreSpawnGeoReplacement<EntityGammaMetroid> {
    private static final GeoReplacementDescriptor<EntityGammaMetroid> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_GAMMA_METROID.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityGammaMetroid.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/gammametroid.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/gammametroid.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/gammametroid.png"),
            GammaMetroidRenderer.SHADOW) {
        @Override
        public void applyScale(EntityGammaMetroid entity, PoseStack poseStack, float partialTick) {
            // orig RenderGammaMetroid.preRenderScale (:39-45): a child gets glScalef(scale / 2.0f), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(GammaMetroidRenderer.SCALE / 2.0F, GammaMetroidRenderer.SCALE / 2.0F, GammaMetroidRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(GammaMetroidRenderer.SCALE, GammaMetroidRenderer.SCALE, GammaMetroidRenderer.SCALE);
        }
    };

    public GammaMetroidGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // GammaMetroidModel.setupAnim verbatim, the float chain left to right.
        float tuskAngle = Mth.cos(ageInTicks * 0.81f) * (float) Math.PI * 0.08f;
        rotateX(processor, "lefttusk", tuskAngle);
        rotateX(processor, "righttusk", tuskAngle);
        rotateX(processor, "middletusk", tuskAngle);
        float shellAngle = Mth.cos(ageInTicks * 0.4f) * (float) Math.PI * 0.05f;
        rotateX(processor, "shell1", shellAngle / 4.0f);
        float beakAngle = Math.abs(Mth.cos(ageInTicks * 0.75f) * (float) Math.PI * 0.1f);
        rotateX(processor, "beaklower", beakAngle + 0.14f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityGammaMetroid, GammaMetroidGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GammaMetroidGeoReplacement());
        }
    }
}
