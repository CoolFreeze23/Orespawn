package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTshirt;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Tshirt (the first Tier-2 slice, 2026-09-13): {@link TshirtModel#setupAnim} on the converted
 * two-quad rig - one frequency group, both quads turning together about Y on the slow Coin cosine
 * {@code cos(ageInTicks * 0.05F * WINGSPEED) * PI} (WINGSPEED 0.22F, orig ClientProxyOreSpawn.java:418).
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine: the classic formula on the geo bones
 * through {@link PoseInputs}); {@link #keyframeLayers()} declares the one group's transcription, and since
 * the species has ONE frequency group its clip carries the bare name {@code walk} (contract section 2.1),
 * so the shipped {@code tshirt.animation.json} ({@code idle} keying no bone + {@code walk}) opens the gate:
 * on the GeckoLib candidate the layer registers under the default keys and the hook stands down. The
 * harness's keyframe reference leg proves the layer against this hook on the shipped clip at the ruled
 * 2.5e-3 rad.</p>
 *
 * <p>Scale and shadow follow {@link TshirtRenderer}: 0.33 render scale and a 1.0 x 0.33 shadow (ENT-S-092).</p>
 */
public final class TshirtGeoReplacement extends OreSpawnGeoReplacement<EntityTshirt> {
    /** The one frequency group: both quads, {@code ageInTicks * 0.05F * WINGSPEED} (two float multiplies, left to right). */
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("turn", KeyframeLayer.WALK, 0.05F, TshirtModel.WINGSPEED, Set.of("Shape1", "Shape2"), false));
    private static final GeoReplacementDescriptor<EntityTshirt> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_TSHIRT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly (the headless leg instantiates this class registry-free, OPT-029 R0)
            EntityTshirt.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/tshirt.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/tshirt.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/tshirt.png"),
            TshirtRenderer.SHADOW) {
        @Override
        public void applyScale(EntityTshirt entity, PoseStack poseStack, float partialTick) {
            // orig RenderTshirt.preRenderScale: GL11.glScalef(scale, scale, scale) (TshirtRenderer.scale)
            poseStack.scale(TshirtRenderer.SCALE, TshirtRenderer.SCALE, TshirtRenderer.SCALE);
        }
    };

    public TshirtGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // TshirtModel.setupAnim:54-56 verbatim (orig ModelTshirt.render): the same yaw on both quads.
        float yaw = Mth.cos(ageInTicks * 0.05F * TshirtModel.WINGSPEED) * (float) Math.PI;
        rotateY(processor, "Shape1", yaw);
        rotateY(processor, "Shape2", yaw);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityTshirt, TshirtGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TshirtGeoReplacement());
        }
    }
}
