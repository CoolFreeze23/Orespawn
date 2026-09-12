package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib Beaver on the converted rig, animated by the classic formulas.
 *
 * <p>The SHIPPED pose is {@link ModelBeaver#setupAnim} verbatim, evaluated on
 * the geo bones of the same names — the G1 harness proved this path matches
 * the classic renderer to within float rounding (FIX_LOG "PHASE G1"). No
 * keyframe clip is involved in it, so no approximation tolerance applies.</p>
 *
 * <p>The species also declares its three keyframe layers ({@link #keyframeLayers()}:
 * the contract's {@code walk} on the gait group, {@code walk_teeth}, {@code walk_tail} —
 * the Tier-2 pilot's reference-leg clips, Q16 (a)). Since item 15 landed (2026-09-12)
 * the shipped {@code beaver.animation.json} is the transcription ({@code tools/keyframe_clip.py}:
 * {@code idle} keying no bone, {@code walk}, {@code walk_teeth}, {@code walk_tail}), so on the
 * GeckoLib candidate renderer the layers register under the default keys and the hook below
 * stands down ({@link OreSpawnGeoReplacementModel#setCustomAnimations}); the hook poses when the
 * gate (idle AND walk) or {@code [modern] artistAnimations} says no. The harness's keyframe
 * reference leg proves the layers against this hook on the SHIPPED clip at the ruled 2.5e-3 rad.</p>
 *
 * <p>Scale and shadow follow {@link BeaverRenderer}: 0.75 render scale
 * (0.75 / 2 for a baby) and a 0.15 x 0.75 shadow (ENT-S-092, from
 * ClientProxyOreSpawn.java:475 / RenderBeaver.java:23-24,39-49).</p>
 */
public final class BeaverGeoReplacement extends OreSpawnGeoReplacement<Beaver> {
    /** The gait group: the four feet, {@code cos(ageInTicks * 3.7F) * PI * 0.45F * limbSwingAmount}. */
    private static final Set<String> GAIT_BONES = Set.of("rff", "lrf", "lff", "rrf");
    /**
     * The three frequency groups of {@link ModelBeaver#setupAnim} (3.7 / 2.7 / 0.5 rad per tick;
     * {@code ANIM_SPEED} 1.0F), one clip each (Amendment 1 point 5): {@code walk} scaled by
     * {@code limbSwingAmount}, {@code walk_teeth} and {@code walk_tail} free-running, as the teeth
     * chew and the tail wags at rest in 1.7.10.
     */
    private static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("gait", KeyframeLayer.WALK, 3.7F, GAIT_BONES, true),
            new KeyframeLayer("teeth", KeyframeLayer.walkClip("teeth"), 2.7F, Set.of("teeth"), false),
            new KeyframeLayer("tail", KeyframeLayer.walkClip("tail"), 0.5F, Set.of("tail"), false));
    private static final GeoReplacementDescriptor<Beaver> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.BEAVER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly (the headless keyframe leg instantiates this class registry-free, OPT-029 R0)
            Beaver.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/beaver.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/beaver.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/beaver.png"),
            BeaverRenderer.SHADOW) {
        @Override
        public void applyScale(Beaver entity, PoseStack poseStack, float partialTick) {
            // orig RenderBeaver.preRenderScale: if (isChild) glScalef(scale / 2.0f) else glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(BeaverRenderer.SCALE / 2.0F, BeaverRenderer.SCALE / 2.0F, BeaverRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(BeaverRenderer.SCALE, BeaverRenderer.SCALE, BeaverRenderer.SCALE);
        }
    };

    public BeaverGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, AnimationState<?> state) {
        Beaver beaver = entity(state);
        float ageInTicks = ageInTicks(beaver, state);
        float limbSwingAmount = limbSwingAmount(state);

        // ModelBeaver.setupAnim with every X rotation negated: the converter
        // maps ModelPart space onto the geo with a reflection, and the G1
        // harness proved the X sign. No converted species has exercised Y or
        // Z rotations yet — derive and harness-prove those signs before
        // relying on them (see the basis notes in tools/layer_definition_to_geo.py).
        float gait = Mth.cos(ageInTicks * 3.7F) * (float) Math.PI * 0.45F * limbSwingAmount;
        setInternalRotX(processor, "rff", -gait);
        setInternalRotX(processor, "lrf", -gait);
        setInternalRotX(processor, "lff", gait);
        setInternalRotX(processor, "rrf", gait);
        setInternalRotX(processor, "teeth", -Mth.cos(ageInTicks * 2.7F) * (float) Math.PI * 0.25F);
        setInternalRotX(processor, "tail", -Mth.cos(ageInTicks * 0.5F) * (float) Math.PI * 0.05F);
    }

    /** Internal-basis write (the caller negates X); predates the classic-vocabulary helpers on the base class. */
    private static void setInternalRotX(AnimationProcessor<?> processor, String boneName, float rotation) {
        GeoBone bone = processor.getBone(boneName);
        if (bone == null) {
            throw new IllegalStateException("beaver.geo.json is missing bone " + boneName);
        }
        bone.setRotX(rotation);
        bone.markRotationAsChanged();
    }
}
