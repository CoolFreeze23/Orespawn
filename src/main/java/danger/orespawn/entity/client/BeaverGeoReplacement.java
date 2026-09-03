package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib Beaver on the converted rig, animated by the classic formulas.
 *
 * <p>The pose is {@link ModelBeaver#setupAnim} verbatim, evaluated on the
 * geo bones of the same names — the G1 harness proved this path matches the
 * classic renderer to within float rounding (FIX_LOG "PHASE G1"). No keyframe
 * clip is involved, so no approximation tolerance applies. Motion only
 * becomes artist-editable once the owner rules on a keyframe tolerance.</p>
 *
 * <p>Scale and shadow follow {@link BeaverRenderer}: 0.75 render scale
 * (0.75 / 2 for a baby) and a 0.15 x 0.75 shadow (ENT-S-092, from
 * ClientProxyOreSpawn.java:475 / RenderBeaver.java:23-24,39-49).</p>
 */
public final class BeaverGeoReplacement extends OreSpawnGeoReplacement<Beaver> {
    private static final GeoReplacementDescriptor<Beaver> DESCRIPTOR = new GeoReplacementDescriptor<>(
            ModEntities.BEAVER::get,
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
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
