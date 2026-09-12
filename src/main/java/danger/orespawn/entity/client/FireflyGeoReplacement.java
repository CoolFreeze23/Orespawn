package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Firefly;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Firefly (built in the first Tier-2 slice, rejoined by the second, 2026-09-13, once ENT-S-161 landed):
 * {@link FireflyModel#setupAnim} on the converted rig - one frequency group, the two wings about Z:
 * {@code 1.11f + cos(ageInTicks * wingspeed) * PI * 0.35f} on the left and {@code -1.11f - cos(...) * PI * 0.35f} on the
 * right (wingspeed 2.5f, orig ModelFirefly.java:16,33 / ClientProxyOreSpawn.java:406). The wings bind at +-0.6981317 rad
 * about Z, so the transcription's keys carry the classic value minus the bind (GeckoLib adds every key to the bone's
 * initial snapshot). The wings are ROTATED zero-thickness cubes: under stock GeckoLib their lighting normal was
 * mangled ({@code RenderUtil.fixInvertedFlatCube}); the seam now draws every cube with its true transformed normal
 * ({@link TrueNormalCubeRenderer}, ENT-S-161 / PN-027), which is what let this rig rejoin.
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine); {@link #keyframeLayers()} declares the one
 * group's transcription under the bare name {@code walk} (a one-group species, contract section 2.1), so the
 * shipped {@code firefly.animation.json} ({@code idle} keying no bone + {@code walk}) opens the gate on the
 * GeckoLib candidate. The harness's keyframe reference leg proves the layer against this hook at 2.5e-3 rad.</p>
 *
 * <p>Scale and shadow follow {@link FireflyRenderer}: 0.75 render scale and a 0.2 x 0.75 shadow (ENT-S-092).</p>
 */
public final class FireflyGeoReplacement extends OreSpawnGeoReplacement<Firefly> {
    /** orig ModelFirefly.java:16,33 {@code wingspeed} = 2.5f (ClientProxyOreSpawn.java:406): the one frequency, {@code ageInTicks * wingspeed}. */
    static final float WINGSPEED = 2.5F;
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("wings", KeyframeLayer.WALK, WINGSPEED, Set.of("wing_left", "wing_right"), false));
    private static final GeoReplacementDescriptor<Firefly> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.FIREFLY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Firefly.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/firefly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/firefly.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/fireflytexture.png"),
            FireflyRenderer.SHADOW) {
        @Override
        public void applyScale(Firefly entity, PoseStack poseStack, float partialTick) {
            // orig RenderFirefly.preRenderCallback: GL11.glScalef(scale, scale, scale) (FireflyRenderer.scale)
            poseStack.scale(FireflyRenderer.SCALE, FireflyRenderer.SCALE, FireflyRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the wings): their two coplanar faces z-fight, and the winner is the face
         * emitted last, so the shipped geo carries the classic within-cube order ({@link FaceOrder#KEY}; the
         * FaceOrder contract's open item for a cutout rig, met by the second Tier-2 slice) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public FireflyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // FireflyModel.setupAnim:112-113 verbatim (the (float) cast of the classic is a no-op on a float product).
        rotateZ(processor, "wing_left", 1.11f + Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.35f);
        rotateZ(processor, "wing_right", -1.11f - Mth.cos(ageInTicks * WINGSPEED) * (float) Math.PI * 0.35f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Firefly, FireflyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new FireflyGeoReplacement());
        }
    }
}
