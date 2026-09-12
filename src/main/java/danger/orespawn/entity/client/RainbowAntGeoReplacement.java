package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRainbowAnt;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Rainbow Ant (the second Tier-2 slice, 2026-09-13): a consumer of the Ant rig. orig ClientProxyOreSpawn.java:414
 * registers {@code new RenderAnt(new ModelAnt(), 0.1f, 0.25f)} - the Ant's own numbers - over the rainbow ant's texture,
 * so this descriptor shares {@link AntGeoReplacement}'s geo, clip file, keyframe layers and hook under its own registry
 * path ({@code rainbow_ant}; design Q9, one profile per registry path even for a shared rig), and the harness proves it
 * on its own manifest entry ({@code model_rainbow_ant}).
 *
 * <p>Scale and shadow follow {@link RainbowAntRenderer}: 0.25 render scale and a 0.1 x 0.25 shadow (ENT-S-092). The
 * gate stands as the Ant's: {@code idle} + {@code walk} shipped, OPEN on the candidate.</p>
 */
public final class RainbowAntGeoReplacement extends OreSpawnGeoReplacement<EntityRainbowAnt> {
    private static final GeoReplacementDescriptor<EntityRainbowAnt> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_RAINBOW_ANT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityRainbowAnt.class,
            // the Ant's shipped rig and clip file, one rig for five consumers (the literals are what the asset audit and
            // the package tool attribute the rig by)
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ant.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ant.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rainbow_ant.png"),
            // orig RenderAnt.java:22 super(model, par2 * par3) with 0.1f x 0.25f: the product RainbowAntRenderer's constructor passes
            0.1F * RainbowAntRenderer.SCALE) {
        @Override
        public void applyScale(EntityRainbowAnt entity, PoseStack poseStack, float partialTick) {
            // orig RenderAnt.preRenderScale (RenderAnt.java:38-40): GL11.glScalef(scale, scale, scale) (RainbowAntRenderer.scale)
            poseStack.scale(RainbowAntRenderer.SCALE, RainbowAntRenderer.SCALE, RainbowAntRenderer.SCALE);
        }
    };

    public RainbowAntGeoReplacement() {
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

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityRainbowAnt, RainbowAntGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RainbowAntGeoReplacement());
        }
    }
}
