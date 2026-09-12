package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GoldFish;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Gold Fish (built in the first Tier-2 slice, rejoined by the second, 2026-09-13, once ENT-S-161 landed):
 * {@link ModelGoldFish#setupAnim} on the converted rig - six frequency groups (wingspeed 0.7f, orig
 * ModelGoldFish.java:13,32 / ClientProxyOreSpawn.java:470): the four pectoral fins about Y at 1.3f / 1.2f / 1.1f / 1.0f
 * ({@code +-0.4f + cos(age * f * ws) * PI * 0.15f}), the two bottom fins about Y at 1.7f ({@code cos * PI * 0.25f}, the
 * second the negative) and the jaw about X at 0.7f ({@code -0.25f + cos * PI * 0.1f}). The pectoral fins bind rotated
 * about Y (+-0.3346) and the jaw about X (-0.2284419), so their keys carry the classic value minus the bind. The phase
 * is the chain's two float multiplies left to right ({@code (age * 0.7f) * 0.7f} for the jaw is not {@code age * 0.49f}).
 * The pectoral and bottom fins are ROTATED zero-thickness cubes: the seam now draws every cube with its true
 * transformed normal ({@link TrueNormalCubeRenderer}, ENT-S-161 / PN-027), which is what let this rig rejoin.
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine); {@link #keyframeLayers()} declares the six
 * groups' transcription. A multi-group species without a gait group carries the bare {@code walk} on its SPEC's
 * primary group - {@code primary_group} in the clip manifest and the seed, the FIRST group ({@code pectoral1}) here
 * (owner 2026-09-13, addendum item 26 (2); contract section 2.1 amended; the bare name is a label, not a semantic)
 * - and {@code walk_<group>} on the other five, so the shipped {@code goldfish.animation.json} ({@code idle} keying
 * no bone + {@code walk} + five {@code walk_<group>}) OPENS the gate on the GeckoLib candidate. The harness's keyframe
 * reference leg proves the layers against this hook at 2.5e-3 rad.</p>
 *
 * <p>Shadow follows {@link GoldFishRenderer} (0.2 x 1.0, ENT-S-092); the classic renderer scales by 1.0, so no scale hook.</p>
 */
public final class GoldFishGeoReplacement extends OreSpawnGeoReplacement<GoldFish> {
    /** orig ModelGoldFish.java:13,32 {@code wingspeed} = 0.7f (ClientProxyOreSpawn.java:470): the chain's second multiply. */
    static final float WINGSPEED = 0.7F;
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            // the SPEC's primary group (the first): the bare walk, a label under the naming rule
            new KeyframeLayer("pectoral1", KeyframeLayer.WALK, 1.3F, WINGSPEED, Set.of("Pectoralfin1"), false),
            new KeyframeLayer("pectoral2", KeyframeLayer.walkClip("pectoral2"), 1.2F, WINGSPEED, Set.of("Pectoralfin2"), false),
            new KeyframeLayer("pectoral3", KeyframeLayer.walkClip("pectoral3"), 1.1F, WINGSPEED, Set.of("Pectoralfin3"), false),
            new KeyframeLayer("pectoral4", KeyframeLayer.walkClip("pectoral4"), 1.0F, WINGSPEED, Set.of("Pectoralfin4"), false),
            new KeyframeLayer("bottomfins", KeyframeLayer.walkClip("bottomfins"), 1.7F, WINGSPEED, Set.of("Bottomfin1", "Bottomfin2"), false),
            new KeyframeLayer("jaw", KeyframeLayer.walkClip("jaw"), 0.7F, WINGSPEED, Set.of("Jaw"), false));
    private static final GeoReplacementDescriptor<GoldFish> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GOLD_FISH.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            GoldFish.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/goldfish.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/goldfish.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/goldfish.png"),
            GoldFishRenderer.SHADOW) {
        /**
         * A rig with zero-thickness cubes (the pectoral and lower fins, mirrored): their two coplanar faces z-fight, and
         * the winner is the face emitted last, so the shipped geo carries the classic within-cube order
         * ({@link FaceOrder#KEY}; the FaceOrder contract's open item for a cutout rig, met by the second Tier-2 slice)
         * and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public GoldFishGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // ModelGoldFish.setupAnim:124-136 verbatim, the float chain left to right.
        float newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "Pectoralfin1", 0.4f + newangle);
        newangle = Mth.cos(ageInTicks * 1.2f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "Pectoralfin2", -0.4f + newangle);
        newangle = Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "Pectoralfin3", 0.4f + newangle);
        newangle = Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "Pectoralfin4", -0.4f + newangle);
        newangle = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.25f;
        rotateY(processor, "Bottomfin1", newangle);
        rotateY(processor, "Bottomfin2", -newangle);
        newangle = Mth.cos(ageInTicks * 0.7f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "Jaw", -0.25f + newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<GoldFish, GoldFishGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GoldFishGeoReplacement());
        }
    }
}
