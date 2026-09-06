package danger.orespawn.g1;

import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacementModel;
import danger.orespawn.entity.client.PoseInputs;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

/**
 * Headless execution of the PRODUCTION Slice 4 replacement hook.
 *
 * <p>Unlike {@link G1AnimationRuntime}, which re-states the Beaver formulas
 * inside the probe, this path instantiates the shipped
 * {@link OreSpawnGeoReplacement} named by the manifest, binds the shipped
 * {@link OreSpawnGeoReplacementModel}'s processor to a fresh bake and poses
 * through {@link OreSpawnGeoReplacement#pose}, so the harness proves the code
 * that renders in-game. The pose runs on explicit {@link PoseInputs} rather
 * than an {@code AnimationState}: GeckoLib's {@code DataTickets} registers a
 * data component in its initialiser and cannot load without a bootstrapped
 * game. The state adapter ({@code PoseInputs.fromState}) is a
 * bytecode-derived, three-field read. The subject is a {@link ProbeSubject}
 * carrying a declared entity state; a hook that needs the real entity class
 * fails here by design.</p>
 */
final class S4CandidateRuntime {
    static final String CANDIDATE_PATH = "production_replacement_hook";
    static final String POSE_SOURCE =
            "fresh BakedGeoModel + production OreSpawnGeoReplacement.pose on explicit PoseInputs";

    private S4CandidateRuntime() {
    }

    record Inputs(float ageTicks, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
    }

    static G1AnimationRuntime.EvaluatedModel evaluateProductionHook(Model rawModel, List<String> drawOrder,
                                                                    String candidateClass, Inputs inputs,
                                                                    Object subject) throws Exception {
        BakedGeoModel baked = freshBaked(rawModel, drawOrder);
        OreSpawnGeoReplacement<?> replacement = instantiate(candidateClass);
        pose(baked, replacement, inputs, subject);
        return snapshot(baked);
    }

    /**
     * A fresh bake through GeckoLib's own factory, then the G2 root-order contract through the
     * PRODUCTION {@link DrawOrder#apply} - the static the shipped {@code OreSpawnGeoReplacementModel
     * .getBakedModel} calls on the cached bake - so the harness draws in the shipped order.
     *
     * <p>No fallback here, on purpose: the shipped model answers a rig without the key with
     * GeckoLib's own order ({@link DrawOrder#applyOrFallback}, the resource-pack courtesy of the
     * owner's ruling 2026-09-06), but the harness proves SHIPPED rigs, every one of which must
     * carry the key - a generated geo without it is a harness failure, never a proof.</p>
     */
    private static BakedGeoModel freshBaked(Model rawModel, List<String> drawOrder) {
        if (drawOrder.isEmpty()) {
            throw new IllegalStateException("harness failure: the generated geo ships without " + DrawOrder.KEY
                    + "; the harness proves shipped rigs and takes no fallback");
        }
        BakedGeoModel baked = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(rawModel));
        DrawOrder.apply(baked, drawOrder);
        return baked;
    }

    private static OreSpawnGeoReplacement<?> instantiate(String className) throws Exception {
        Class<?> type = Class.forName(className);
        if (!OreSpawnGeoReplacement.class.isAssignableFrom(type)) {
            throw new IllegalStateException(className + " is not an OreSpawnGeoReplacement");
        }
        return (OreSpawnGeoReplacement<?>) type.getDeclaredConstructor().newInstance();
    }

    private static <E extends Entity> void pose(BakedGeoModel baked, OreSpawnGeoReplacement<E> replacement,
                                                Inputs inputs, Object subject) {
        OreSpawnGeoReplacementModel<E, OreSpawnGeoReplacement<E>> model =
                new OreSpawnGeoReplacementModel<>(replacement.descriptor());
        model.getAnimationProcessor().setActiveModel(baked);
        replacement.pose(model.getAnimationProcessor(), new PoseInputs(subject, inputs.ageTicks(),
                inputs.limbSwing(), inputs.limbSwingAmount(), inputs.netHeadYaw(), inputs.headPitch()));
    }

    private static G1AnimationRuntime.EvaluatedModel snapshot(BakedGeoModel model) {
        Map<String, GeoBone> bones = collect(model);
        Map<String, float[]> rotations = new TreeMap<>();
        bones.forEach((name, bone) -> rotations.put(name,
                new float[]{bone.getRotX(), bone.getRotY(), bone.getRotZ()}));
        return new G1AnimationRuntime.EvaluatedModel(model, bones, rotations);
    }

    private static Map<String, GeoBone> collect(BakedGeoModel model) {
        Map<String, GeoBone> bones = new TreeMap<>();
        for (GeoBone bone : model.topLevelBones()) {
            collectBone(bone, bones);
        }
        return bones;
    }

    private static void collectBone(GeoBone bone, Map<String, GeoBone> bones) {
        if (bones.put(bone.getName(), bone) != null) {
            throw new IllegalStateException("Generated GeckoLib model has duplicate bone " + bone.getName());
        }
        bone.getChildBones().forEach(child -> collectBone(child, bones));
    }
}
