package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ruby Bird (the first Tier-2 slice, 2026-09-13): the second consumer of the Cockateil rig. The 1.7.10
 * registrations are identical (orig ClientProxyOreSpawn.java:430-431: {@code new RenderCockateil(new
 * ModelCockateil(1.0f), 0.3f, 0.75f)} for both) and the port draws both through {@link CockateilRenderer}, so
 * this descriptor shares {@link CockateilGeoReplacement}'s geo, clip file, keyframe layers and hook under its own
 * registry path ({@code ruby_bird}; design Q9, one profile per registry path even for a shared rig), and the
 * harness proves it on its own manifest entry ({@code model_ruby_bird}). Typed over {@link Cockateil} exactly as the
 * shared classic renderer is ({@code RubyBird extends Cockateil}; the registry type is {@code RUBY_BIRD}, so
 * {@code requireEntity} admits ruby birds only), which lets the dev switch pair it with {@code CockateilRenderer::new}.
 *
 * <p>Texture by {@link Cockateil#getBirdType()} through {@link CockateilRenderer#textureFor}; scale and shadow follow
 * {@link CockateilRenderer}: 0.75 render scale and a 0.3 x 0.75 shadow (ENT-S-092). The gate stands as the
 * Cockateil's: a multi-group species without a bare {@code walk}, CLOSED as ruled.</p>
 */
public final class RubyBirdGeoReplacement extends OreSpawnGeoReplacement<Cockateil> {
    private static final GeoReplacementDescriptor<Cockateil> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.RUBY_BIRD.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Cockateil.class,
            // the Cockateil's shipped rig and clip file, one rig for two consumers (the literals are what the asset
            // audit and the package tool attribute the rig by)
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cockateil.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cockateil.animation.json"),
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

    public RubyBirdGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return CockateilGeoReplacement.KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        CockateilGeoReplacement.pose(processor, inputs.ageInTicks());
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Cockateil, RubyBirdGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RubyBirdGeoReplacement());
        }
    }
}
