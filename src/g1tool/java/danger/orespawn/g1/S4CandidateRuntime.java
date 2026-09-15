package danger.orespawn.g1;

import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacementModel;
import danger.orespawn.entity.client.PoseInputs;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.Direction;
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

    /**
     * The hook's inputs; {@code partialTick} the frame's partial tick the seam carries since the remainder slice (owner
     * 2026-09-15, closing set, item 3: {@code PoseInputs.partialTick}) - the probe fills it from the sample's (the fractional
     * part of the sample's age), the reference-clip sampler keeps the five-float form (0: a whole tick per key).
     */
    record Inputs(float ageTicks, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, float partialTick) {
        /** The five-float form: the partial tick 0 (the sampler's fixed inputs; every clip byte-identical to its pre-slice file). */
        Inputs(float ageTicks, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
            this(ageTicks, limbSwing, limbSwingAmount, netHeadYaw, headPitch, 0.0F);
        }
    }

    static G1AnimationRuntime.EvaluatedModel evaluateProductionHook(Model rawModel, List<String> drawOrder,
                                                                    Map<String, List<List<Direction>>> faceOrder,
                                                                    String candidateClass, Inputs inputs,
                                                                    Object subject) throws Exception {
        OreSpawnGeoReplacement<?> replacement = instantiate(candidateClass);
        return evaluateProductionHook(rawModel, drawOrder, faceOrder, candidateClass, inputs, subject,
                replacement.descriptor().cubeFaceOrderRequired());
    }

    /**
     * The same pose with the face-order strictness stated by the caller (owner 2026-09-14, addendum item 11): the
     * reference-clip sampler poses an UNLANDED hook over the reference leg's converter output, which carries the draw-order
     * key but not yet the face-order key its descriptor may require (the landing slice's TEST-007 writes it) - the face
     * order only orders cube vertices for rendering and moves no bone, so the sampler passes {@code false} for a reference
     * rig and the shipped descriptor's own requirement for a shipped one.
     */
    static G1AnimationRuntime.EvaluatedModel evaluateProductionHook(Model rawModel, List<String> drawOrder,
                                                                    Map<String, List<List<Direction>>> faceOrder,
                                                                    String candidateClass, Inputs inputs,
                                                                    Object subject, boolean requireFaceOrder) throws Exception {
        OreSpawnGeoReplacement<?> replacement = instantiate(candidateClass);
        BakedGeoModel baked = freshBaked(rawModel, drawOrder, faceOrder, requireFaceOrder, candidateClass);
        pose(baked, replacement, inputs, subject);
        return snapshot(baked);
    }

    /**
     * ENT-S-146 (refuter A, D1): whether the shipped descriptor of {@code candidateClass} requires the
     * within-cube face-order key ({@code GeoReplacementDescriptor.cubeFaceOrderRequired}) - the probe hands
     * it to the {@link G1AnimationRuntime} evaluator, whose bind bake must be exactly as strict as this one.
     */
    static boolean cubeFaceOrderRequired(String candidateClass) throws Exception {
        return instantiate(candidateClass).descriptor().cubeFaceOrderRequired();
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
     *
     * <p>ENT-S-146: a present within-cube face order ({@link FaceOrder#KEY}, written for a translucent
     * rig) is applied through the same strict production static the shipped model uses
     * ({@link FaceOrder#apply}); an absent one is the norm for an opaque rig and applies nothing - but
     * for a rig whose descriptor REQUIRES the key ({@code cubeFaceOrderRequired}: a translucent rig, where
     * the shipped model would WARN and fall back to GeckoLib's own face order) an absent key is the same
     * harness failure as an absent draw order (refuter A, D1): the harness takes no fallback.</p>
     */
    private static BakedGeoModel freshBaked(Model rawModel, List<String> drawOrder,
                                            Map<String, List<List<Direction>>> faceOrder,
                                            boolean faceOrderRequired, String candidateClass) {
        if (drawOrder.isEmpty()) {
            throw new IllegalStateException("harness failure: the generated geo ships without " + DrawOrder.KEY
                    + "; the harness proves shipped rigs and takes no fallback");
        }
        if (faceOrderRequired && faceOrder.isEmpty()) {
            throw new IllegalStateException("harness failure: the generated geo ships without " + FaceOrder.KEY
                    + ", which the descriptor of " + candidateClass + " requires (cubeFaceOrderRequired: a "
                    + "translucent rig); the harness proves shipped rigs and takes no fallback");
        }
        BakedGeoModel baked = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(rawModel));
        DrawOrder.apply(baked, drawOrder);
        if (!faceOrder.isEmpty()) {
            FaceOrder.apply(baked, faceOrder);
        }
        return baked;
    }

    /**
     * The descriptor's constant render transform (owner 2026-09-15, closing set continued, item 2; TEST-013), read WITHOUT an
     * entity - the renderType(null) form: a descriptor answers it from constants, and both probe sides apply it.
     */
    static danger.orespawn.entity.client.GeoReplacementDescriptor.RenderTransform renderTransform(String candidateClass) throws Exception {
        return instantiate(candidateClass).descriptor().renderTransform();
    }

    /** The shipped replacement, constructed registry-free (OPT-029 R0); the probe also reads its descriptor's render-state hooks through it. */
    static OreSpawnGeoReplacement<?> instantiate(String className) throws Exception {
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
                inputs.limbSwing(), inputs.limbSwingAmount(), inputs.netHeadYaw(), inputs.headPitch(), inputs.partialTick()));
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
