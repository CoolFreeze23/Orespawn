package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCliffRacer;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cliff Racer (the first Tier-2 slice, 2026-09-13): {@link CliffRacerModel#setupAnim} on the
 * converted rig - one frequency group, the two wings flapping about Z at {@code cos(ageInTicks * 1.3f) * PI * 0.25f},
 * the left wing positive and the right wing the negative.
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine); {@link #keyframeLayers()} declares the one
 * group's transcription under the bare name {@code walk} (a one-group species, contract section 2.1), so the
 * shipped {@code cliffracer.animation.json} ({@code idle} keying no bone + {@code walk}) opens the gate on the
 * GeckoLib candidate. The harness's keyframe reference leg proves the layer against this hook at 2.5e-3 rad.</p>
 *
 * <p>Shadow follows {@link CliffRacerRenderer} (0.3 x 1.0, ENT-S-092); the classic renderer scales by 1.0, so no
 * scale hook.</p>
 */
public final class CliffRacerGeoReplacement extends OreSpawnGeoReplacement<EntityCliffRacer> {
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("wings", KeyframeLayer.WALK, 1.3F, Set.of("lwing", "rwing"), false));
    private static final GeoReplacementDescriptor<EntityCliffRacer> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_CLIFF_RACER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityCliffRacer.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cliffracer.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cliffracer.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cliffracer.png"),
            CliffRacerRenderer.SHADOW) {
    };

    public CliffRacerGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // CliffRacerModel.setupAnim:49-51 verbatim.
        float wingAngle = Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
        rotateZ(processor, "lwing", wingAngle);
        rotateZ(processor, "rwing", -wingAngle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityCliffRacer, CliffRacerGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CliffRacerGeoReplacement());
        }
    }
}
