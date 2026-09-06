package danger.orespawn.g1;

import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.FaceOrder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.model.GeoModel;

/** Headless execution of the accepted G1 GeckoLib custom-animation path. */
final class G1AnimationRuntime {
    private G1AnimationRuntime() {
    }

    /**
     * The draw order must be the geo's own ({@link DrawOrder#read}): the shipped model answers a rig
     * without the key with GeckoLib's own order ({@link DrawOrder#applyOrFallback}, the resource-pack
     * courtesy of the owner's ruling 2026-09-06), but the harness - the probe's geo dumps and the
     * benchmark both bake through this evaluator - proves SHIPPED rigs, every one of which must carry
     * the key, so an empty order is a harness failure here, never a fallback.
     */
    static Evaluator evaluator(Model rawModel, List<String> drawOrder) {
        // The opaque-only form: the benchmark (G1PerformanceBenchmark) bakes the g1 rigs through it without
        // reading a face order; a translucent rig reaches only the probe's four-argument form below.
        return evaluator(rawModel, drawOrder, Map.of(), false);
    }

    /**
     * ENT-S-146: with the geo's within-cube face order ({@link FaceOrder#KEY}) as well - applied through
     * the strict production {@link FaceOrder#apply} on every fresh bake when present; an absent order
     * (every opaque rig) applies nothing. {@code faceOrderRequired} is the shipped descriptor's
     * {@code cubeFaceOrderRequired} (a translucent rig; {@code S4CandidateRuntime.cubeFaceOrderRequired}):
     * an absent key is then the same harness failure as an absent draw order - the shipped model would
     * WARN and fall back to GeckoLib's own face order, and the harness takes no fallback (refuter A, D1).
     * This evaluator bakes the bind sample of EVERY probed rig, translucent ones included.
     */
    static Evaluator evaluator(Model rawModel, List<String> drawOrder, Map<String, List<List<Direction>>> faceOrder,
                               boolean faceOrderRequired) {
        if (drawOrder.isEmpty()) {
            throw new IllegalStateException("harness failure: the generated geo ships without " + DrawOrder.KEY
                    + "; the harness proves shipped rigs and takes no fallback");
        }
        if (faceOrderRequired && faceOrder.isEmpty()) {
            throw new IllegalStateException("harness failure: the generated geo ships without " + FaceOrder.KEY
                    + ", which the shipped descriptor requires (cubeFaceOrderRequired: a translucent rig); the "
                    + "harness proves shipped rigs and takes no fallback");
        }
        return new Evaluator(GeometryTree.fromModel(rawModel), drawOrder, faceOrder);
    }

    static final class Evaluator {
        private final GeometryTree geometryTree;
        private final List<String> drawOrder;
        private final Map<String, List<List<Direction>>> faceOrder;

        private Evaluator(GeometryTree geometryTree, List<String> drawOrder, Map<String, List<List<Direction>>> faceOrder) {
            this.geometryTree = geometryTree;
            this.drawOrder = drawOrder;
            this.faceOrder = faceOrder;
        }

        EvaluatedModel bindPose() {
            return snapshot(freshBaked());
        }

        EvaluatedModel evaluateBeaverCodeDriven(double ageTicks, float limbSwingAmount) {
            BakedGeoModel baked = freshBaked();
            ProbeAnimatable animatable = new ProbeAnimatable();
            BeaverRuntimeGeoModel geoModel = new BeaverRuntimeGeoModel();
            geoModel.getAnimationProcessor().setActiveModel(baked);
            AnimationState<ProbeAnimatable> state = new AnimationState<>(
                    animatable, 0.0F, limbSwingAmount, 0.0F, limbSwingAmount != 0.0F);
            state.animationTick = ageTicks;
            geoModel.setCustomAnimations(animatable, 0L, state);
            return snapshot(baked);
        }

        /** The keyframe reference leg's persistent bake ({@link KeyframeLeg}): the same fresh bake, kept across the schedule as a per-entity manager keeps its bones in-game. */
        BakedGeoModel freshBake() {
            return freshBaked();
        }

        /** GeckoLib's own bake, then the production G2 reorder ({@link DrawOrder#apply}) and, when present, the face order ({@link FaceOrder#apply}), as in S4CandidateRuntime. */
        private BakedGeoModel freshBaked() {
            BakedGeoModel baked = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(this.geometryTree);
            DrawOrder.apply(baked, this.drawOrder);
            if (!this.faceOrder.isEmpty()) {
                FaceOrder.apply(baked, this.faceOrder);
            }
            return baked;
        }
    }

    record EvaluatedModel(BakedGeoModel model, Map<String, GeoBone> bones,
                          Map<String, float[]> internalRotations) {
    }

    private static EvaluatedModel snapshot(BakedGeoModel model) {
        Map<String, GeoBone> bones = new TreeMap<>();
        for (GeoBone bone : model.topLevelBones()) {
            collectBone(bone, bones);
        }
        Map<String, float[]> rotations = new TreeMap<>();
        bones.forEach((name, bone) -> rotations.put(name,
                new float[]{bone.getRotX(), bone.getRotY(), bone.getRotZ()}));
        return new EvaluatedModel(model, bones, rotations);
    }

    private static void collectBone(GeoBone bone, Map<String, GeoBone> bones) {
        if (bones.put(bone.getName(), bone) != null) {
            throw new IllegalStateException("Generated GeckoLib model has duplicate bone " + bone.getName());
        }
        bone.getChildBones().forEach(child -> collectBone(child, bones));
    }

    private static final class ProbeAnimatable implements GeoAnimatable {
        private final InstancedAnimatableInstanceCache cache =
                new InstancedAnimatableInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            // The accepted Beaver exception uses GeoModel.setCustomAnimations,
            // not the retained reference animation JSON or a controller.
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }

        @Override
        public double getBoneResetTime() {
            return 0.0;
        }

        @Override
        public double getTick(Object relatedObject) {
            return 0.0;
        }
    }

    /** Approved Beaver exception: exact legacy formulas in GeckoLib's custom hook. */
    private static final class BeaverRuntimeGeoModel extends GeoModel<ProbeAnimatable> {
        private static final ResourceLocation PROBE_RESOURCE =
                ResourceLocation.fromNamespaceAndPath("orespawn", "g1/beaver_custom_animation");

        @Override
        public void setCustomAnimations(ProbeAnimatable animatable, long instanceId,
                                        AnimationState<ProbeAnimatable> state) {
            float ageTicks = (float) state.getAnimationTick();
            float amount = state.getLimbSwingAmount();
            float gait = Mth.cos(ageTicks * 3.7F) * (float) Math.PI * 0.45F * amount;
            // GeckoLib's internal rotation basis is [-javaX, javaY, -javaZ].
            setX("rff", -gait);
            setX("lrf", -gait);
            setX("lff", gait);
            setX("rrf", gait);
            setX("teeth", -Mth.cos(ageTicks * 2.7F) * (float) Math.PI * 0.25F);
            setX("tail", -Mth.cos(ageTicks * 0.5F) * (float) Math.PI * 0.05F);
        }

        private void setX(String boneName, float rotation) {
            GeoBone bone = getAnimationProcessor().getBone(boneName);
            if (bone == null) {
                throw new IllegalStateException("Beaver custom animation is missing bone " + boneName);
            }
            bone.setRotX(rotation);
            bone.markRotationAsChanged();
        }

        @Override
        public ResourceLocation getModelResource(ProbeAnimatable animatable) {
            return PROBE_RESOURCE;
        }

        @Override
        public ResourceLocation getTextureResource(ProbeAnimatable animatable) {
            return PROBE_RESOURCE;
        }

        @Override
        public ResourceLocation getAnimationResource(ProbeAnimatable animatable) {
            throw new IllegalStateException(
                    "REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE animation must never be loaded");
        }
    }
}
