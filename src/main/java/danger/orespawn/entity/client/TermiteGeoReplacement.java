package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTermite;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Termite (the second Tier-2 slice, 2026-09-13): a consumer of the Ant rig. orig ClientProxyOreSpawn.java:476
 * registers {@code new RenderAnt(new ModelAnt(), 0.15f, 0.35f)} - the Red Ant's numbers on the same model - over the
 * termite's texture (its UV layout is the ant model's), so this descriptor shares {@link AntGeoReplacement}'s geo, clip
 * file, keyframe layers and hook under its own registry path ({@code termite}; design Q9, one profile per registry path
 * even for a shared rig), and the harness proves it on its own manifest entry ({@code model_termite}).
 *
 * <p>Scale and shadow follow {@link TermiteRenderer}: 0.35 render scale and a 0.15 x 0.35 shadow (ENT-S-092). The gate
 * stands as the Ant's: {@code idle} + {@code walk} shipped, OPEN on the candidate.</p>
 */
public final class TermiteGeoReplacement extends OreSpawnGeoReplacement<EntityTermite> {
    private static final GeoReplacementDescriptor<EntityTermite> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_TERMITE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityTermite.class,
            // the Ant's shipped rig and clip file, one rig for five consumers
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ant.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ant.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/termite.png"),
            // orig RenderAnt.java:22 super(model, par2 * par3) with 0.15f x 0.35f: the product TermiteRenderer's constructor passes
            0.15F * TermiteRenderer.SCALE) {
        @Override
        public void applyScale(EntityTermite entity, PoseStack poseStack, float partialTick) {
            // orig RenderAnt.preRenderScale (RenderAnt.java:38-40): GL11.glScalef(scale, scale, scale) (TermiteRenderer.scale)
            poseStack.scale(TermiteRenderer.SCALE, TermiteRenderer.SCALE, TermiteRenderer.SCALE);
        }
    };

    public TermiteGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return AntGeoReplacement.KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        AntGeoReplacement.pose(processor, inputs.ageInTicks(), inputs.limbSwingAmount());
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityTermite, TermiteGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TermiteGeoReplacement());
        }
    }
}
