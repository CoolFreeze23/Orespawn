package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Dragonfly (the first Tier-2 slice, 2026-09-13): {@link DragonflyModel#setupAnim} on the converted rig -
 * two frequency groups (wingspeed 2.0f, orig ModelDragonfly.java:13,42 / ClientProxyOreSpawn.java:424): the four
 * wings about Z at {@code cos(age * 1.3f * ws) * PI * 0.25f} (the front-left positive, the front-right negative, the
 * rear pair the same plus 3.14f), and the two jaws about X at {@code cos(age * 0.3f * ws) * PI * 0.1f} (the left
 * positive, the right negative; the jaws bind rotated 0.4363323 rad about X, so their keys carry the classic value
 * minus the bind).
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine); {@link #keyframeLayers()} declares the two
 * groups' transcription. A multi-group species without a gait group has no bare {@code walk} under contract
 * section 2.1 ({@code walk_wings}, {@code walk_jaws}), so the shipped {@code dragonfly.animation.json} leaves the
 * gate CLOSED as ruled until the owner rules which group carries the bare name (presented with the slice).</p>
 *
 * <p>Scale and shadow follow {@link DragonflyRenderer}: 1.5 render scale and a 0.3 x 1.5 shadow (ENT-S-092).</p>
 */
public final class DragonflyGeoReplacement extends OreSpawnGeoReplacement<EntityDragonfly> {
    /** orig ModelDragonfly.java:13,42 {@code wingspeed} = 2.0f (ClientProxyOreSpawn.java:424): the chain's second multiply. */
    static final float WINGSPEED = 2.0F;
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("wings", KeyframeLayer.walkClip("wings"), 1.3F, WINGSPEED,
                    Set.of("lfwing", "rfwing", "lrwing", "rrwing"), false),
            new KeyframeLayer("jaws", KeyframeLayer.walkClip("jaws"), 0.3F, WINGSPEED, Set.of("ljaw", "rjaw"), false));
    private static final GeoReplacementDescriptor<EntityDragonfly> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_DRAGONFLY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityDragonfly.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/dragonfly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/dragonfly.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dragonfly.png"),
            DragonflyRenderer.SHADOW) {
        @Override
        public void applyScale(EntityDragonfly entity, PoseStack poseStack, float partialTick) {
            // orig RenderDragonfly.preRenderCallback: GL11.glScalef(scale, scale, scale) (DragonflyRenderer.scale)
            poseStack.scale(DragonflyRenderer.SCALE, DragonflyRenderer.SCALE, DragonflyRenderer.SCALE);
        }
    };

    public DragonflyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // DragonflyModel.setupAnim:210-216 verbatim, the float chain left to right.
        float newangle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f;
        rotateZ(processor, "lfwing", newangle);
        rotateZ(processor, "rfwing", -newangle);
        rotateZ(processor, "lrwing", newangle + 3.14f);
        rotateZ(processor, "rrwing", -newangle + 3.14f);
        newangle = Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "ljaw", newangle);
        rotateX(processor, "rjaw", -newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityDragonfly, DragonflyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new DragonflyGeoReplacement());
        }
    }
}
