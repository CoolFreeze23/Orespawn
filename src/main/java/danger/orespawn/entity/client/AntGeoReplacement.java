package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ant (the second Tier-2 slice, 2026-09-13): {@link AntModel#setupAnim} on the converted rig - the first
 * GAIT-SCALED Tier-2 rig after the Beaver, in the Beaver's exact form: the twelve leg parts about X at
 * {@code cos(ageInTicks * 2.7f) * PI * 0.45f * limbSwingAmount} (orig ModelAnt.java:162-172; six positive, six the
 * negative; no threshold, no base, no wingspeed) and the two jaws about Y at {@code cos(ageInTicks * 0.4f) * PI * 0.05f}
 * (orig ModelAnt.java:173-174; the right jaw the negative). Contract section 2.1: the gait group's clip is the bare
 * {@code walk}, its delta scaled by {@code limbSwingAmount} (Amendment 1 point 3); the jaws' is {@code walk_jaws},
 * always on. The leg parts bind rotated about Y and Z and animate about X, so their keys are deltas from a zero X bind.
 *
 * <p>One rig, five consumers (orig ClientProxyOreSpawn.java:412-415 and 476: {@code new RenderAnt(new ModelAnt(), shadow,
 * scale)} with 0.1f / 0.25f for the Ant, Rainbow Ant and Unstable Ant and 0.15f / 0.35f for the Red Ant and Termite):
 * {@link RainbowAntGeoReplacement}, {@link RedAntGeoReplacement}, {@link TermiteGeoReplacement} and
 * {@link UnstableAntGeoReplacement} share this class's geo, clip file, layers and hook ({@link #pose}) under their own
 * descriptors - one profile per registry path (design Q9; the Cockateil / Ruby Bird precedent) - each with its own
 * texture, shadow and scale, and the harness proves every consumer on its own manifest entry.</p>
 *
 * <p>The SHIPPED pose is the classic hook (the S4 doctrine); {@link #keyframeLayers()} declares the two groups'
 * transcription, so the shipped {@code ant.animation.json} ({@code idle} keying no bone + {@code walk} + {@code walk_jaws})
 * opens the gate on the GeckoLib candidate. The harness's keyframe reference leg proves the layers against this hook
 * at 2.5e-3 rad at amplitudes 0 / 0.25 / 0.5 / 1.</p>
 *
 * <p>Scale and shadow follow {@link AntRenderer}: 0.25 render scale and a 0.1 x 0.25 shadow (ENT-S-092; orig
 * RenderAnt.java:22-23 and 38-40).</p>
 */
public final class AntGeoReplacement extends OreSpawnGeoReplacement<EntityAnt> {
    /** The gait group: the twelve leg parts, {@code cos(ageInTicks * 2.7f) * PI * 0.45f * limbSwingAmount}. */
    static final Set<String> GAIT_BONES = Set.of("llegtop1", "llegbot1", "llegtop2", "llegbot2", "llegtop3", "llegbot3",
            "rlegtop1", "rlegbot1", "rlegtop2", "rlegbot2", "rlegtop3", "rlegbot3");
    /** The two frequency groups of {@link AntModel#setupAnim} (2.7 / 0.4 rad per tick, no wingspeed), one clip each. */
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("gait", KeyframeLayer.WALK, 2.7F, GAIT_BONES, true),
            new KeyframeLayer("jaws", KeyframeLayer.walkClip("jaws"), 0.4F, Set.of("jawsl", "jawsr"), false));
    private static final GeoReplacementDescriptor<EntityAnt> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_ANT.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityAnt.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ant.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ant.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ant.png"),
            // orig RenderAnt.java:22 super(model, par2 * par3) with 0.1f x 0.25f: the product AntRenderer's constructor
            // passes (it declares no SHADOW constant; tools/reference_renderer_pins.py evaluates the equal expression)
            0.1F * AntRenderer.SCALE) {
        @Override
        public void applyScale(EntityAnt entity, PoseStack poseStack, float partialTick) {
            // orig RenderAnt.preRenderScale (RenderAnt.java:38-40): GL11.glScalef(scale, scale, scale) (AntRenderer.scale)
            poseStack.scale(AntRenderer.SCALE, AntRenderer.SCALE, AntRenderer.SCALE);
        }
    };

    public AntGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        pose(processor, inputs.ageInTicks(), inputs.limbSwingAmount());
    }

    /** AntModel.setupAnim verbatim (orig ModelAnt.java:162-174), the float chain left to right; shared by the five consumers of the rig. */
    static void pose(AnimationProcessor<?> processor, float ageInTicks, float limbSwingAmount) {
        float newangle = Mth.cos(ageInTicks * 2.7f) * (float) Math.PI * 0.45f * limbSwingAmount;
        rotateX(processor, "llegbot1", newangle);
        rotateX(processor, "llegtop1", newangle);
        rotateX(processor, "rlegtop2", newangle);
        rotateX(processor, "rlegbot2", newangle);
        rotateX(processor, "rlegtop3", newangle);
        rotateX(processor, "rlegbot3", newangle);
        rotateX(processor, "rlegtop1", -newangle);
        rotateX(processor, "rlegbot1", -newangle);
        rotateX(processor, "llegtop2", -newangle);
        rotateX(processor, "llegbot2", -newangle);
        rotateX(processor, "llegtop3", -newangle);
        rotateX(processor, "llegbot3", -newangle);
        newangle = Mth.cos(ageInTicks * 0.4f) * (float) Math.PI * 0.05f;
        rotateY(processor, "jawsl", newangle);
        rotateY(processor, "jawsr", -newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityAnt, AntGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new AntGeoReplacement());
        }
    }
}
