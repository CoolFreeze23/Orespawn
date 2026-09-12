package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRedAnt;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Red Ant (the second Tier-2 slice, 2026-09-13): a consumer of the Ant rig. orig ClientProxyOreSpawn.java:413
 * registers {@code new RenderAnt(new ModelAnt(), 0.15f, 0.35f)} - a larger ant on the same model - over the red ant's
 * texture, so this descriptor shares {@link AntGeoReplacement}'s geo, clip file, keyframe layers and hook under its own
 * registry path ({@code red_ant}; design Q9, one profile per registry path even for a shared rig), and the harness
 * proves it on its own manifest entry ({@code model_red_ant}).
 *
 * <p>Scale and shadow follow {@link RedAntRenderer}: 0.35 render scale and a 0.15 x 0.35 shadow (ENT-S-092). The gate
 * stands as the Ant's: {@code idle} + {@code walk} shipped, OPEN on the candidate.</p>
 */
public final class RedAntGeoReplacement extends OreSpawnGeoReplacement<EntityRedAnt> {
    private static final GeoReplacementDescriptor<EntityRedAnt> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_RED_ANT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityRedAnt.class,
            // the Ant's shipped rig and clip file, one rig for five consumers
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ant.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ant.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/red_ant.png"),
            // orig RenderAnt.java:22 super(model, par2 * par3) with 0.15f x 0.35f: the product RedAntRenderer's constructor passes
            0.15F * RedAntRenderer.SCALE) {
        @Override
        public void applyScale(EntityRedAnt entity, PoseStack poseStack, float partialTick) {
            // orig RenderAnt.preRenderScale (RenderAnt.java:38-40): GL11.glScalef(scale, scale, scale) (RedAntRenderer.scale)
            poseStack.scale(RedAntRenderer.SCALE, RedAntRenderer.SCALE, RedAntRenderer.SCALE);
        }
    };

    public RedAntGeoReplacement() {
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

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityRedAnt, RedAntGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RedAntGeoReplacement());
        }
    }
}
