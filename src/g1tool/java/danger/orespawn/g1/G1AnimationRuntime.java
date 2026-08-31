package danger.orespawn.g1;

import java.util.Map;
import java.util.TreeMap;
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

    static Evaluator evaluator(Model rawModel) {
        return new Evaluator(GeometryTree.fromModel(rawModel));
    }

    static final class Evaluator {
        private final GeometryTree geometryTree;

        private Evaluator(GeometryTree geometryTree) {
            this.geometryTree = geometryTree;
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

        private BakedGeoModel freshBaked() {
            return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(this.geometryTree);
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
