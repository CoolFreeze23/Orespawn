package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cockateil (the first Tier-2 slice, 2026-09-13): {@link ModelCockateil#setupAnim} on the converted rig -
 * five frequency groups (wingspeed 1.0f, orig ModelCockateil.java:13,32 / ClientProxyOreSpawn.java:430-431): the
 * wings about Z at 1.5f ({@code lwing1 = -1.5f + c}, {@code lwing2 = c}, {@code rwing1 = 1.5f - c}, {@code rwing2 = -c},
 * {@code c = cos * PI * 0.35f}), the three tail feathers about X at 0.3f ({@code cos * PI * 0.1f}), and the three crest
 * feathers about Z at 1.1f / 1.2f / 1.3f ({@code cos * PI * 0.08f}), one group each. The wing roots bind rotated about
 * Z (lwing1 -1.561488, rwing1 1.595066), so their keys carry the classic value minus the bind.
 *
 * <p>One rig, two consumers: {@link RubyBirdGeoReplacement} (orig ClientProxyOreSpawn.java:431: the same
 * {@code new RenderCockateil(new ModelCockateil(1.0f), 0.3f, 0.75f)}) shares this class's geo, clip file, layers and
 * hook ({@link #pose}) under its own descriptor - one profile per registry path (design Q9, ruled 2026-08-31) -
 * and both are proven by the harness.</p>
 *
 * <p>The SHIPPED pose is the classic hook (the S4 doctrine); {@link #keyframeLayers()} declares the five groups'
 * transcription. A multi-group species without a gait group carries the bare {@code walk} on its SPEC's primary
 * group - {@code primary_group} in the clip manifest and the seed, the FIRST group ({@code wings}) here (owner
 * 2026-09-13, addendum item 26 (2); contract section 2.1 amended; the bare name is a label, not a semantic: the
 * contract's fly - walk fallback plays it in flight) - and {@code walk_tail}, {@code walk_feather1..3} on the others,
 * so the shipped {@code cockateil.animation.json} ({@code idle} keying no bone + {@code walk} + four {@code walk_<group>})
 * OPENS the gate on the GeckoLib candidate, for both consumers of the rig (the second Tier-2 slice; the first slice
 * shipped it CLOSED as {@code walk_wings}).</p>
 *
 * <p>Texture by {@link Cockateil#getBirdType()} through {@link CockateilRenderer#textureFor}; scale and shadow follow
 * {@link CockateilRenderer}: 0.75 render scale and a 0.3 x 0.75 shadow (ENT-S-092).</p>
 */
public final class CockateilGeoReplacement extends OreSpawnGeoReplacement<Cockateil> {
    /** orig ModelCockateil.java:13,32 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:430-431, both consumers): the chain's second multiply. */
    static final float WINGSPEED = 1.0F;
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            // the SPEC's primary group (the first): the bare walk, a label under the naming rule
            new KeyframeLayer("wings", KeyframeLayer.WALK, 1.5F, WINGSPEED,
                    Set.of("lwing1", "lwing2", "rwing1", "rwing2"), false),
            new KeyframeLayer("tail", KeyframeLayer.walkClip("tail"), 0.3F, WINGSPEED,
                    Set.of("tailfeather1", "tailfeather2", "tailfeather3"), false),
            new KeyframeLayer("feather1", KeyframeLayer.walkClip("feather1"), 1.1F, WINGSPEED, Set.of("feather1"), false),
            new KeyframeLayer("feather2", KeyframeLayer.walkClip("feather2"), 1.2F, WINGSPEED, Set.of("feather2"), false),
            new KeyframeLayer("feather3", KeyframeLayer.walkClip("feather3"), 1.3F, WINGSPEED, Set.of("feather3"), false));
    private static final GeoReplacementDescriptor<Cockateil> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.COCKATEIL.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Cockateil.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cockateil.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cockateil.animation.json"),
            // the descriptor's default texture; the hook picks the bird type's (bird1..bird6)
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/bird1.png"),
            CockateilRenderer.SHADOW) {
        @Override
        public ResourceLocation texture(Cockateil entity) {
            return CockateilRenderer.textureFor(entity.getBirdType());
        }

        @Override
        public void applyScale(Cockateil entity, PoseStack poseStack, float partialTick) {
            // orig RenderCockateil.preRenderCallback: GL11.glScalef(scale, scale, scale) (CockateilRenderer.scale)
            poseStack.scale(CockateilRenderer.SCALE, CockateilRenderer.SCALE, CockateilRenderer.SCALE);
        }
    };

    public CockateilGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        pose(processor, inputs.ageInTicks());
    }

    /** ModelCockateil.setupAnim:124-135 verbatim, the float chain left to right; shared by both consumers of the rig. */
    static void pose(AnimationProcessor<?> processor, float ageInTicks) {
        float newangle = Mth.cos(ageInTicks * 1.5f * WINGSPEED) * (float) Math.PI * 0.35f;
        rotateZ(processor, "lwing1", -1.5f + newangle);
        rotateZ(processor, "lwing2", newangle);
        rotateZ(processor, "rwing1", 1.5f - newangle);
        rotateZ(processor, "rwing2", -newangle);
        newangle = Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "tailfeather1", newangle);
        rotateX(processor, "tailfeather2", newangle);
        rotateX(processor, "tailfeather3", newangle);
        rotateZ(processor, "feather1", Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.08f);
        rotateZ(processor, "feather2", Mth.cos(ageInTicks * 1.2f * WINGSPEED) * (float) Math.PI * 0.08f);
        rotateZ(processor, "feather3", Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.08f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Cockateil, CockateilGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CockateilGeoReplacement());
        }
    }
}
