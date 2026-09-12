package danger.orespawn.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.client.BeaverGeoReplacement;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CoinGeoReplacement;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.ElevatorGeoReplacement;
import danger.orespawn.entity.client.IslandGeoReplacement;
import danger.orespawn.entity.client.IslandTooGeoReplacement;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.OreSpawnGeoReplacementModel;
import danger.orespawn.entity.client.PurplePowerGeoReplacement;
import danger.orespawn.entity.client.Robot1GeoReplacement;
import danger.orespawn.entity.client.Robot2GeoReplacement;
import danger.orespawn.entity.client.Robot3GeoReplacement;
import danger.orespawn.entity.client.Robot4GeoReplacement;
import danger.orespawn.entity.client.Robot5GeoReplacement;
import danger.orespawn.entity.client.RockBaseGeoReplacement;
import danger.orespawn.entity.client.RotatorGeoReplacement;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.VortexGeoReplacement;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import danger.orespawn.entity.client.animation.SplineRepair;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.model.GeoModel;

/**
 * Phase G, the keyframe leg's return (item 15; owner rulings 2026-09-06, scope addendum item 24 (5)-(14)):
 * what the game-test server can pin of the controller, the Q9 repair and the Beaver's self-gated
 * registration. Every GeckoLib class used here is free of {@code @OnlyIn} ({@code javap -v} over the pinned
 * 4.8.4 jar: {@code GeoModel}, {@code AnimationController}, {@code AnimatableManager} and its
 * {@code ControllerRegistrar}, {@code AnimationProcessor}, {@code AnimationState}, {@code Animation},
 * {@code EasingType}, {@code RawAnimation}, the keyframe records, {@code BakedAnimations},
 * {@code KeyFramesAdapter}, {@code GeckoLibCache} - whose class initialiser installs empty maps and touches no
 * client class - and {@code DataTickets}, whose initialiser only builds tickets into its own map), so the
 * dedicated server loads them; the gametest run has already proven {@code new BeaverGeoReplacement()} on
 * this server ({@link GeoCacheEvictionTests}).
 * <ul>
 * <li>{@code kf_001} (item 15 landed, 2026-09-12): the SHIPPED {@code beaver.animation.json} is the
 *     transcription - {@code idle} (no bone keyed), {@code walk}, {@code walk_teeth}, {@code walk_tail} - and
 *     registers the three layers under the default keys through {@code registerKeyframeLayers}; no bake at
 *     all, a file with only {@code idle}, and a file with only {@code walk} each register nothing (the gate
 *     opens on idle AND walk together, owner 2026-09-12 item 12); the production {@code registerControllers}
 *     path registers nothing on this server (GeckoLib's animation cache holds no bake here).</li>
 * <li>{@code kf_002}: the reference clip (the generator's output, a test resource, byte-equal to the shipped
 *     file) registers the three contract layers - in order, on the named controllers, the gait layer
 *     additive and scaled on the four feet - and the config gates them: {@code artistAnimations} off,
 *     {@code classicAnimationSpecies} listing the Beaver (either spelling), or the modern master off, each
 *     registers none; {@code walk} alone registers none, {@code idle} + {@code walk} registers the gait layer
 *     only (the teeth and tail hold bind), {@code idle} + {@code walk_teeth} registers none.</li>
 * <li>{@code kf_003} (CLIENT-ONLY, not a gate row - GeckoLib's controller processing initialises
 *     {@code MolangQueries}, which the dedicated server refuses; the harness leg carries these facts): on this server, through the production code on both sides, the three layers
 *     (built by the production registration on a real frozen Beaver's age and the state's limb-swing
 *     amount, the reference clip repaired by the production {@link SplineRepair}, driven through GeckoLib's
 *     own {@code AnimationProcessor.tickAnimation}) pose the six animated bones within the ruled 2.5e-3 rad
 *     of the shipped classic hook ({@code OreSpawnGeoReplacementModel.setCustomAnimations} on a manager
 *     without controllers) over a late-start schedule, and that model's gate stands the hook down on a
 *     manager that holds the layers.</li>
 * <li>{@code kf_004}: the Q9 defect pinned against the pinned jar (every loaded catmullrom segment carries
 *     P0 == its own start value, P3 == its own end on the last) and the repair's arithmetic on a synthetic
 *     clip: the textbook neighbours, periodic on a loop, clamped on a play-once clip, the anchor and linear
 *     frames untouched, idempotent, everything but the arguments the loaded objects; GeckoLib's own
 *     evaluator over the repaired arguments equals the textbook Catmull-Rom.</li>
 * <li>{@code kf_005} (CLIENT-ONLY, not a gate row - GeckoLib's controller processing initialises
 *     {@code MolangQueries}, which the dedicated server refuses; the harness leg carries these facts): two additive layers on one bone compose to the sum of the layers measured alone on
 *     every one of six consecutive frames of one persistent manager (no accumulation across frames); the
 *     same two layers non-additive are last-registered-wins.</li>
 * <li>{@code kf_006} (CLIENT-ONLY, not a gate row - GeckoLib's controller processing initialises
 *     {@code MolangQueries}, which the dedicated server refuses; the harness leg carries these facts): the declared-length time-warp (the same shape at 0.5 s and 2.0 s poses identically;
 *     the declared ticks and the ratio), the late prime (a controller first processed at a late age poses
 *     as one that ran from age zero), the LUT index chain straddling a seam and saturating at an absurd age.</li>
 * <li>{@code kf_007} (item 15 refuter A, D1; the first Tier-2 slice, 2026-09-13): the presented state - every one
 *     of the twenty-one shipped replacements, constructed registry-free on this server, declares no keyframe layer
 *     but the Beaver (its three) and the seven Tier-2 descriptors (their transcriptions, pinned group by group in
 *     {@link T2SeamTests}); {@code registerControllers} is final on the base and no replacement declares its own, so
 *     the base's self-gated registration is the single path, and it registers nothing here for every species.</li>
 * <li>{@code kf_008} (item 15 refuter B, D2): the production repair site - GeckoLib's animation cache map
 *     seeded by reflection with a bake of the reference clip under the Beaver's animation resource, the
 *     shipped {@code OreSpawnGeoReplacementModel} constructed registry-free: {@code getAnimation} serves a
 *     repaired copy (never the loaded object), the same instance on every call while the bake is the same
 *     (identity), a fresh repaired copy once the map holds a new {@code BakedAnimations} (a resource reload)
 *     or the map itself is swapped ({@code GeckoLibCache.reload}), and {@code null} for a clip the present
 *     file lacks (GeckoLib's own answer, no fallback walk).</li>
 * </ul>
 * Synchronous rows, no level state beyond row 3's frozen Beaver (discarded in its finally); the config flags
 * row 2 flips are global and restored in a finally, and row 8's seeded cache map is restored in a finally, so
 * the class sits in its own batch {@code keyframeLeg} (TEST-003).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KeyframeLegTests {
    private static final String BATCH = "keyframeLeg";
    /** Amendment 1 point 4, ratified: the animation-leg tolerance. */
    private static final double TOLERANCE_RAD = 2.5e-3D;
    private static final String SHIPPED_CLIP = "/assets/orespawn/animations/entity/beaver.animation.json";
    private static final String SHIPPED_GEO = "/assets/orespawn/geo/entity/beaver.geo.json";
    /** tools/keyframe_clip.py over tools/keyframe_clips/beaver.json: 15 / 13 / 8 catmullrom keys per bone. */
    private static final String REFERENCE_CLIP = "/orespawn_gametest/keyframes/beaver.animation.json";
    private static final List<String> ANIMATED_BONES = List.of("rff", "lrf", "lff", "rrf", "teeth", "tail");
    private static final Set<String> GAIT_BONES = Set.of("rff", "lrf", "lff", "rrf");
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final BlockPos BEAVER_POS = new BlockPos(24, 0, 24);
    private static final float GAIT_W = 3.7F;
    private static final float TEETH_W = 2.7F;
    private static final float TAIL_W = 0.5F;
    /** A two-bone rig for the synthetic rows. */
    private static final String RIG = """
            {"format_version": "1.12.0", "minecraft:geometry": [{"description": {
            "identifier": "geometry.orespawn.gametest.keyframe_leg", "texture_width": 16, "texture_height": 16},
            "bones": [
            {"name": "a", "pivot": [0, 0, 0], "cubes": [{"origin": [-1, 0, -1], "size": [2, 2, 2], "uv": [0, 0]}]},
            {"name": "b", "pivot": [0, 2, 0], "cubes": [{"origin": [-1, 2, -1], "size": [2, 2, 2], "uv": [0, 4]}]}
            ]}]}
            """;

    // ------------------------------------------------------------------ row 1: the shipped clip registers the layers; partial files none

    @GameTest(template = "empty", batch = BATCH)
    public static void kf_001_shipped_clip_registers_the_layers_and_partial_files_none(GameTestHelper helper) {
        BeaverGeoReplacement replacement = new BeaverGeoReplacement();
        List<KeyframeLayer> layers = replacement.keyframeLayers();
        helper.assertTrue(layers.size() == 3, "the Beaver declares its three frequency groups");
        helper.assertTrue(layers.get(0).group().equals("gait") && layers.get(0).clip().equals(KeyframeLayer.WALK)
                && layers.get(0).angularFrequencyRadiansPerTick() == GAIT_W && layers.get(0).bones().equals(GAIT_BONES)
                && layers.get(0).gaitScaled(), "gait layer: walk, 3.7 rad/tick, the four feet, scaled");
        helper.assertTrue(layers.get(1).group().equals("teeth") && layers.get(1).clip().equals("walk_teeth")
                && layers.get(1).angularFrequencyRadiansPerTick() == TEETH_W && layers.get(1).bones().equals(Set.of("teeth"))
                && !layers.get(1).gaitScaled(), "teeth layer: walk_teeth, 2.7 rad/tick, unscaled");
        helper.assertTrue(layers.get(2).group().equals("tail") && layers.get(2).clip().equals("walk_tail")
                && layers.get(2).angularFrequencyRadiansPerTick() == TAIL_W && layers.get(2).bones().equals(Set.of("tail"))
                && !layers.get(2).gaitScaled(), "tail layer: walk_tail, 0.5 rad/tick, unscaled");

        BakedAnimations shipped = bakeClips(resource(SHIPPED_CLIP));
        helper.assertTrue(shipped.animations().keySet().equals(Set.of(KeyframeLayer.IDLE, KeyframeLayer.WALK, "walk_teeth", "walk_tail")),
                "the shipped beaver.animation.json is the transcription: idle, walk, walk_teeth, walk_tail (item 15 landed 2026-09-12)");
        helper.assertTrue(shipped.getAnimation(KeyframeLayer.IDLE).boneAnimations().length == 0
                        && shipped.getAnimation(KeyframeLayer.IDLE).loopType() == Animation.LoopType.LOOP,
                "the transcription's idle keys no bone (the resting motion is the always-on groups); it loops");
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            AnimatableManager.ControllerRegistrar registrar = registrar();
            helper.assertTrue(replacement.registerKeyframeLayers(registrar, shipped) == 3 && registrar.controllers().size() == 3,
                    "the shipped clip registers the three layers under the default keys");
            helper.assertTrue(replacement.registerKeyframeLayers(registrar(), null) == 0, "no bake at all registers no layer");
            BakedAnimations idleOnly = new BakedAnimations(Map.of(KeyframeLayer.IDLE, syntheticLoop("idle", "rff", 5, 10.0D, "linear", 1.0D)));
            helper.assertTrue(replacement.registerKeyframeLayers(registrar(), idleOnly) == 0,
                    "a file with idle but no walk registers no layer (the gate opens on idle AND walk together)");
            BakedAnimations walkOnly = new BakedAnimations(Map.of(KeyframeLayer.WALK, shipped.getAnimation(KeyframeLayer.WALK)));
            helper.assertTrue(replacement.registerKeyframeLayers(registrar(), walkOnly) == 0,
                    "a file with walk but no idle registers no layer (one without the other stays classic)");
        } finally {
            flags.restore();
        }
        AnimatableManager<BeaverGeoReplacement> manager = new AnimatableManager<>(replacement);
        helper.assertTrue(manager.getAnimationControllers().isEmpty(),
                "the production registerControllers path registers nothing: GeckoLib's animation cache holds no bake on this server");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 2: the reference clip and the config gates

    @GameTest(template = "empty", batch = BATCH)
    public static void kf_002_reference_clip_registers_the_contract_layers_under_the_key(GameTestHelper helper) {
        BeaverGeoReplacement replacement = new BeaverGeoReplacement();
        BakedAnimations reference = bakeClips(resource(REFERENCE_CLIP));
        for (KeyframeLayer layer : replacement.keyframeLayers()) {
            helper.assertTrue(reference.getAnimation(layer.clip()) != null, "the reference clip carries " + layer.clip());
        }
        Flags flags = Flags.read();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            EntityType<?> type = ModEntities.BEAVER.get();
            helper.assertTrue(OreSpawnConfig.artistAnimations(type), "master on, key on, list empty: artist animations");

            AnimatableManager.ControllerRegistrar registrar = registrar();
            helper.assertTrue(replacement.registerKeyframeLayers(registrar, reference) == 3, "the reference clip registers the three layers");
            List<AnimationController<? extends GeoAnimatable>> controllers = registrar.controllers();
            helper.assertTrue(controllers.size() == 3, "three controllers in the registrar");
            String[] names = {"keyframe:walk", "keyframe:walk_teeth", "keyframe:walk_tail"};
            float[] omegas = {GAIT_W, TEETH_W, TAIL_W};
            for (int index = 0; index < 3; index++) {
                helper.assertTrue(controllers.get(index) instanceof PhaseLockedKeyframeController<?> phaseLocked
                                && phaseLocked.getName().equals(names[index])
                                && phaseLocked.angularFrequencyRadiansPerSourceTick() == omegas[index],
                        "controller " + index + " is the phase-locked " + names[index]);
            }
            PhaseLockedKeyframeController<?> gait = (PhaseLockedKeyframeController<?>) controllers.get(0);
            helper.assertTrue(gait.additive() && gait.amplitudeScaledRotationBones().equals(GAIT_BONES),
                    "the gait layer is additive and scales the four feet");
            for (int index = 1; index < 3; index++) {
                PhaseLockedKeyframeController<?> other = (PhaseLockedKeyframeController<?>) controllers.get(index);
                helper.assertTrue(!other.additive() && other.amplitudeScaledRotationBones().isEmpty(),
                        names[index] + " plays unscaled");
            }

            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(false);
            helper.assertTrue(!OreSpawnConfig.artistAnimations(type) && replacement.registerKeyframeLayers(registrar(), reference) == 0,
                    "artistAnimations off: the classic source");
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of("orespawn:beaver"));
            helper.assertTrue(!OreSpawnConfig.artistAnimations(type) && replacement.registerKeyframeLayers(registrar(), reference) == 0,
                    "classicAnimationSpecies lists orespawn:beaver: the classic source");
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of(" beaver "));
            helper.assertTrue(!OreSpawnConfig.artistAnimations(type), "the bare name (trimmed) is accepted for the mod's own species");
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of("orespawn:coin", "minecraft:beaver"));
            helper.assertTrue(OreSpawnConfig.artistAnimations(type) && replacement.registerKeyframeLayers(registrar(), reference) == 3,
                    "another species listed, or a foreign namespace: the Beaver stays artist");
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            OreSpawnConfig.MODERN_ENABLED.set(false);
            helper.assertTrue(!OreSpawnConfig.artistAnimations(type) && replacement.registerKeyframeLayers(registrar(), reference) == 0,
                    "the modern master off forces the classic source whatever the key says");
            OreSpawnConfig.MODERN_ENABLED.set(true);

            BakedAnimations walkOnly = new BakedAnimations(Map.of(KeyframeLayer.WALK, reference.getAnimation(KeyframeLayer.WALK)));
            helper.assertTrue(replacement.registerKeyframeLayers(registrar(), walkOnly) == 0,
                    "walk alone registers none: the gate opens on idle AND walk together (owner 2026-09-12, item 12)");
            BakedAnimations idleAndWalk = new BakedAnimations(Map.of(KeyframeLayer.IDLE, reference.getAnimation(KeyframeLayer.IDLE),
                    KeyframeLayer.WALK, reference.getAnimation(KeyframeLayer.WALK)));
            AnimatableManager.ControllerRegistrar partial = registrar();
            helper.assertTrue(replacement.registerKeyframeLayers(partial, idleAndWalk) == 1
                            && partial.controllers().get(0).getName().equals("keyframe:walk"),
                    "idle + walk registers the gait layer only; the teeth and tail hold bind (contract section 2.4's fallback)");
            BakedAnimations idleAndTeeth = new BakedAnimations(Map.of(KeyframeLayer.IDLE, reference.getAnimation(KeyframeLayer.IDLE),
                    "walk_teeth", reference.getAnimation("walk_teeth")));
            helper.assertTrue(replacement.registerKeyframeLayers(registrar(), idleAndTeeth) == 0,
                    "idle + walk_teeth registers none: a partial delivery without walk stays classic");
        } finally {
            flags.restore();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 3: the layers against the classic hook, on this server

    // CLIENT-ONLY, not a gate row: GeckoLib's AnimationController.processCurrentAnimation calls MathParser.setVariable
    // unconditionally, which initialises MolangQueries (its static initialiser reaches ClientLevel), and the dedicated
    // game-test server's RuntimeDistCleaner refuses that class (the kf17c / kf17d gates, 2026-09-06). GeckoLib controller
    // processing is client-only by construction; this check runs under the client run and its facts are the harness leg's
    // (KeyframeLeg: the same tickAnimation path in the probe JVM). Kept unannotated so the body stays reviewable.
    public static void kf_003_layers_pose_as_the_classic_hook_on_the_server(GameTestHelper helper) {
        Flags flags = Flags.read();
        Beaver beaver = null;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(true);
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(List.of());
            beaver = spawnFrozen(helper, ModEntities.BEAVER.get(), BEAVER_POS);
            BeaverGeoReplacement replacement = new BeaverGeoReplacement();
            BakedAnimations clips = SplineRepair.repair(bakeClips(resource(REFERENCE_CLIP)));
            String geo = resource(SHIPPED_GEO);

            // The keyframe side: the production layers on the entity's own readers, added to a manager of this replacement.
            AnimatableManager.ControllerRegistrar registrar = registrar();
            helper.assertTrue(replacement.registerKeyframeLayers(registrar, clips) == 3, "the three layers registered");
            AnimatableManager<BeaverGeoReplacement> layered = replacement.getAnimatableInstanceCache().getManagerForId(7L);
            helper.assertTrue(layered.getAnimationControllers().isEmpty(), "a fresh manager holds no controller on this server");
            for (AnimationController<? extends GeoAnimatable> controller : registrar.controllers()) {
                layered.addController(controller);
            }
            helper.assertTrue(layered.getAnimationControllers().size() == 3, "the manager holds the three layers");
            BakedGeoModel keyframeBake = bakeRig(geo);
            ServingModel<BeaverGeoReplacement> keyframeModel = new ServingModel<>(clips, replacement.descriptor().animationResource());
            keyframeModel.getAnimationProcessor().setActiveModel(keyframeBake);
            Map<String, GeoBone> keyframeBones = bones(keyframeBake);

            // The classic side: the shipped model's hook through its own gate, on a manager without controllers.
            OreSpawnGeoReplacementModel<Beaver, BeaverGeoReplacement> classicModel =
                    new OreSpawnGeoReplacementModel<>(replacement.descriptor());
            BakedGeoModel classicBake = bakeRig(geo);
            classicModel.getAnimationProcessor().setActiveModel(classicBake);
            Map<String, GeoBone> classicBones = bones(classicBake);
            helper.assertTrue(replacement.getAnimatableInstanceCache().getManagerForId(8L).getAnimationControllers().isEmpty(),
                    "manager 8 holds no controller: the classic source");

            double maxDelta = 0.0D;
            String worst = "none";
            int compared = 0;
            for (int tick = 137; tick < 137 + 24; tick++) {
                for (float partial : new float[] {0.0F, 0.371F, 0.5F, 0.875F}) {
                    for (float amplitude : new float[] {0.0F, 0.5F, 1.0F}) {
                        beaver.tickCount = tick;
                        AnimationState<BeaverGeoReplacement> state = new AnimationState<>(replacement, 0.0F, amplitude, partial,
                                amplitude != 0.0F);
                        state.setData(DataTickets.ENTITY, beaver);
                        double age = (double) ((float) tick + partial);
                        state.animationTick = age;
                        keyframeModel.getAnimationProcessor().tickAnimation(replacement, keyframeModel, layered, age, state, true);
                        classicModel.setCustomAnimations(replacement, 8L, state);
                        for (String bone : ANIMATED_BONES) {
                            double delta = Math.abs(keyframeBones.get(bone).getRotX() - classicBones.get(bone).getRotX());
                            compared++;
                            if (delta > maxDelta) {
                                maxDelta = delta;
                                worst = bone + "@" + age + "x" + amplitude;
                            }
                        }
                        for (String bone : List.of("head", "nose", "body")) {
                            helper.assertTrue(keyframeBones.get(bone).getRotX() == 0.0F && keyframeBones.get(bone).getRotY() == 0.0F
                                    && keyframeBones.get(bone).getRotZ() == 0.0F, "a bone outside every layer holds bind: " + bone);
                        }
                    }
                }
            }
            helper.assertTrue(maxDelta <= TOLERANCE_RAD, String.format(Locale.ROOT,
                    "the layers pose within %.4g rad of the classic hook over %d bone samples (max %.6g at %s)",
                    TOLERANCE_RAD, compared, maxDelta, worst));
            helper.assertTrue(maxDelta > 1.0e-6D, "the leg is a keyframe approximation, not the hook itself (max " + maxDelta + ")");

            // The gate: the same hook on the manager that holds the layers leaves the bones alone.
            beaver.tickCount = 150;
            AnimationState<BeaverGeoReplacement> state = new AnimationState<>(replacement, 0.0F, 1.0F, 0.25F, true);
            state.setData(DataTickets.ENTITY, beaver);
            for (GeoBone bone : classicBones.values()) {
                bone.setRotX(0.0F);
            }
            classicModel.setCustomAnimations(replacement, 7L, state);
            for (String bone : ANIMATED_BONES) {
                helper.assertTrue(classicBones.get(bone).getRotX() == 0.0F, "with layers registered the classic hook stands down: " + bone);
            }
            classicModel.setCustomAnimations(replacement, 8L, state);
            helper.assertTrue(classicBones.get("teeth").getRotX() != 0.0F, "without layers the classic hook poses");
        } finally {
            flags.restore();
            if (beaver != null && !beaver.isRemoved()) {
                beaver.discard();
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 4: the Q9 defect and the repair's arithmetic

    @GameTest(template = "empty", batch = BATCH)
    public static void kf_004_spline_repair_arithmetic(GameTestHelper helper) {
        double[] degrees = {0.0D, 30.0D, -10.0D, 20.0D, 5.0D, 0.0D};
        for (boolean loop : new boolean[] {true, false}) {
            BakedAnimations loaded = new BakedAnimations(Map.of("probe", syntheticClip("probe", "a", degrees, "catmullrom", 1.0D, loop)));
            List<Keyframe<MathValue>> frames = frames(loaded.getAnimation("probe"));
            helper.assertTrue(frames.size() == degrees.length, "GeckoLib builds one keyframe per authored key (the first a zero-length anchor)");
            double[] radians = new double[degrees.length];
            for (int index = 0; index < degrees.length; index++) {
                radians[index] = Math.toRadians(-degrees[index]); // the loader negates constant X keys
                helper.assertTrue(close(frames.get(index).endValue().get(), radians[index]), "key " + index + " loaded as -deg in radians");
            }
            // The defect, against the pinned jar: P0 is the segment's own start value; P3 on the last segment its own end.
            for (int index = 1; index < frames.size(); index++) {
                Keyframe<MathValue> frame = frames.get(index);
                helper.assertTrue(frame.easingType() == EasingType.CATMULLROM && frame.easingArgs().size() == 2, "catmullrom with two args");
                helper.assertTrue(frame.easingArgs().get(0).get() == frame.startValue().get(),
                        "GeckoLib 4.8.4 stores P0 == P1 on segment " + index + " (BakedAnimationsAdapter.addSplineArgs 131-143)");
                double expectedP3 = index + 1 < frames.size() ? frames.get(index + 1).endValue().get() : frame.endValue().get();
                helper.assertTrue(frame.easingArgs().get(1).get() == expectedP3, "GeckoLib's P3 on segment " + index);
            }
            // The repair.
            Animation repaired = SplineRepair.repair(loaded.getAnimation("probe"));
            List<Keyframe<MathValue>> fixed = frames(repaired);
            helper.assertTrue(fixed.size() == frames.size() && fixed.get(0) == frames.get(0), "the anchor is the loaded object");
            for (int index = 1; index < fixed.size(); index++) {
                Keyframe<MathValue> before = frames.get(index);
                Keyframe<MathValue> after = fixed.get(index);
                helper.assertTrue(after.length() == before.length() && after.startValue() == before.startValue()
                        && after.endValue() == before.endValue() && after.easingType() == before.easingType(),
                        "only the arguments change on segment " + index);
                double expectedP0;
                if (index >= 2) {
                    expectedP0 = radians[index - 2];
                } else {
                    expectedP0 = loop ? radians[degrees.length - 2] : radians[0];
                }
                double expectedP3 = index + 1 < fixed.size() ? radians[index + 1] : (loop ? radians[1] : radians[index]);
                helper.assertTrue(after.easingArgs().get(0).get() == expectedP0,
                        (loop ? "loop" : "play-once") + " segment " + index + " P0 = the key before its start");
                helper.assertTrue(after.easingArgs().get(1).get() == expectedP3,
                        (loop ? "loop" : "play-once") + " segment " + index + " P3 = the key after its end");
                MathValue[] rule = SplineRepair.repairedArguments(frames, index, loop);
                helper.assertTrue(rule != null && rule[0].get() == expectedP0 && rule[1].get() == expectedP3, "repairedArguments agrees");
            }
            helper.assertTrue(SplineRepair.repairedArguments(frames, 0, loop) == null, "the anchor is never repaired");
            helper.assertTrue(repaired.name().equals("probe") && repaired.length() == 20.0D
                    && repaired.loopType() == loaded.getAnimation("probe").loopType()
                    && repaired.keyFrames() == loaded.getAnimation("probe").keyFrames(), "name, length, loop type and event keyframes are the loaded ones");
            // Idempotent: repairing the repaired clip yields the same arguments.
            List<Keyframe<MathValue>> twice = frames(SplineRepair.repair(repaired));
            for (int index = 1; index < twice.size(); index++) {
                helper.assertTrue(twice.get(index).easingArgs().get(0).get() == fixed.get(index).easingArgs().get(0).get()
                        && twice.get(index).easingArgs().get(1).get() == fixed.get(index).easingArgs().get(1).get(), "idempotent on segment " + index);
            }
            // GeckoLib's own evaluator over the repaired arguments IS the textbook Catmull-Rom; over its own, the kinked one.
            int segment = 2;
            double p1 = radians[1];
            double p2 = radians[2];
            double textbook = catmullRom(0.5D, radians[0], p1, p2, radians[3]);
            double kinked = catmullRom(0.5D, p1, p1, p2, radians[3]);
            helper.assertTrue(close(evaluate(fixed.get(segment), 0.5D), textbook), "repaired: the textbook spline at t = 0.5");
            helper.assertTrue(close(evaluate(frames.get(segment), 0.5D), kinked), "GeckoLib's own: the kinked spline at t = 0.5");
            helper.assertTrue(Math.abs(textbook - kinked) > 1.0e-3D, "the two curves differ visibly on this shape");
        }
        // Linear and short stacks are untouched objects.
        Animation linear = syntheticClip("lin", "a", degrees, "linear", 1.0D, true);
        List<Keyframe<MathValue>> linearFrames = frames(linear);
        List<Keyframe<MathValue>> linearFixed = frames(SplineRepair.repair(linear));
        for (int index = 0; index < linearFrames.size(); index++) {
            helper.assertTrue(linearFixed.get(index) == linearFrames.get(index), "a linear keyframe is the loaded object");
        }
        Animation twoKeys = syntheticClip("two", "a", new double[] {0.0D, 15.0D}, "catmullrom", 1.0D, true);
        List<Keyframe<MathValue>> twoFrames = frames(twoKeys);
        helper.assertTrue(frames(SplineRepair.repair(twoKeys)).get(1) == twoFrames.get(1), "one segment has no neighbours: untouched");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 5: additive layers across frames

    // CLIENT-ONLY, not a gate row: GeckoLib's AnimationController.processCurrentAnimation calls MathParser.setVariable
    // unconditionally, which initialises MolangQueries (its static initialiser reaches ClientLevel), and the dedicated
    // game-test server's RuntimeDistCleaner refuses that class (the kf17c / kf17d gates, 2026-09-06). GeckoLib controller
    // processing is client-only by construction; this check runs under the client run and its facts are the harness leg's
    // (KeyframeLeg: the same tickAnimation path in the probe JVM). Kept unannotated so the body stays reviewable.
    public static void kf_005_additive_layer_composes_without_accumulating(GameTestHelper helper) {
        Animation walk = syntheticLoop("walk", "a", 9, 40.0D, "catmullrom", 1.0D);
        Animation crouch = syntheticClip("crouch", "a", new double[] {10.0D, 10.0D}, "linear", 1.0D, true);
        BakedAnimations clips = SplineRepair.repair(new BakedAnimations(Map.of("walk", walk, "crouch", crouch)));
        float amplitude = 0.25F;
        double[] ages = {10.0D, 10.4D, 11.0D, 11.9D, 12.5D, 13.0D};
        Rig walkOnly = new Rig(clips, List.of(Layer.WALK), true);
        Rig idleOnly = new Rig(clips, List.of(Layer.IDLE), true);
        Rig both = new Rig(clips, List.of(Layer.WALK, Layer.IDLE), true);
        Rig lastWins = new Rig(clips, List.of(Layer.WALK, Layer.IDLE), false);
        double maxAdditive = 0.0D;
        double maxLastWins = 0.0D;
        for (double age : ages) {
            float w = walkOnly.sample(age, amplitude);
            float i = idleOnly.sample(age, amplitude);
            float sum = both.sample(age, amplitude);
            float last = lastWins.sample(age, amplitude);
            maxAdditive = Math.max(maxAdditive, Math.abs(sum - (w + i)));
            maxLastWins = Math.max(maxLastWins, Math.abs(last - i));
            helper.assertTrue(Math.abs(w) > 1.0e-3D, "the walk layer moves the bone at " + age);
        }
        helper.assertTrue(maxAdditive <= 1.0e-6D, "additive layers compose to the sum of the layers on every frame (max " + maxAdditive + ")");
        helper.assertTrue(maxLastWins <= 1.0e-6D, "non-additive layers: the last registered wins (max " + maxLastWins + ")");
        helper.assertTrue(both.manager.getAnimationControllers().size() == 2, "both layers on one persistent manager");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 6: the time-warp, the prime, the LUT chain

    // CLIENT-ONLY, not a gate row: GeckoLib's AnimationController.processCurrentAnimation calls MathParser.setVariable
    // unconditionally, which initialises MolangQueries (its static initialiser reaches ClientLevel), and the dedicated
    // game-test server's RuntimeDistCleaner refuses that class (the kf17c / kf17d gates, 2026-09-06). GeckoLib controller
    // processing is client-only by construction; this check runs under the client run and its facts are the harness leg's
    // (KeyframeLeg: the same tickAnimation path in the probe JVM). Kept unannotated so the body stays reviewable.
    public static void kf_006_declared_length_time_warp_and_late_prime(GameTestHelper helper) {
        Animation half = syntheticLoop("walk", "a", 9, 40.0D, "catmullrom", 0.5D);
        Animation twice = syntheticLoop("walk", "a", 9, 40.0D, "catmullrom", 2.0D);
        Rig short_ = new Rig(SplineRepair.repair(new BakedAnimations(Map.of("walk", half))), List.of(Layer.WALK), true);
        Rig long_ = new Rig(SplineRepair.repair(new BakedAnimations(Map.of("walk", twice))), List.of(Layer.WALK), true);
        double maxDelta = 0.0D;
        for (int index = 0; index <= 50; index++) {
            double age = 137.371D + 0.31D * index;
            maxDelta = Math.max(maxDelta, Math.abs(short_.sample(age, 1.0F) - long_.sample(age, 1.0F)));
        }
        helper.assertTrue(maxDelta <= 1.0e-9D, "the same shape at 0.5 s and 2.0 s poses identically (max " + maxDelta + ")");
        PhaseLockedKeyframeController<?> shortController = short_.controller(Layer.WALK);
        PhaseLockedKeyframeController<?> longController = long_.controller(Layer.WALK);
        helper.assertTrue(shortController.declaredClipTicks() == 10.0D && longController.declaredClipTicks() == 40.0D,
                "the declared lengths in ticks (animation_length x 20)");
        helper.assertTrue(close(shortController.clipTicksPerSourceTick(), 10.0D * GAIT_W / (2.0D * Math.PI))
                && close(longController.clipTicksPerSourceTick(), 40.0D * GAIT_W / (2.0D * Math.PI)),
                "the time-warp ratio is declared length x omega / 2 pi");

        // The late prime: a controller first seen at a late age poses as one that ran from age zero.
        Rig fromZero = new Rig(SplineRepair.repair(new BakedAnimations(Map.of("walk", twice))), List.of(Layer.WALK), true);
        for (int tick = 0; tick < 137; tick++) {
            fromZero.sample(tick, 1.0F);
        }
        Rig late = new Rig(SplineRepair.repair(new BakedAnimations(Map.of("walk", twice))), List.of(Layer.WALK), true);
        float continuous = fromZero.sample(137.371D, 1.0F);
        float primed = late.sample(137.371D, 1.0F);
        helper.assertTrue(continuous == primed, "the late-primed controller poses at the entity's phase, not at t = 0");
        float atZero = new Rig(SplineRepair.repair(new BakedAnimations(Map.of("walk", twice))), List.of(Layer.WALK), true).sample(0.0D, 1.0F);
        helper.assertTrue(Math.abs(primed - atZero) > 1.0e-3D, "and that phase is not the clip's start");

        // The LUT chain: a seam is straddled by T - eps / 0 + eps, and an absurd age saturates without wrapping into nonsense.
        double period = 2.0D * Math.PI / GAIT_W;
        double epsilon = 4.0D * period / PhaseLockedKeyframeController.CLASSIC_TRIG_INDEX_COUNT;
        double seam = 81.0D * period;
        int before = PhaseLockedKeyframeController.classicCosineIndex((float) (seam - epsilon) * GAIT_W * 1.0F);
        int after = PhaseLockedKeyframeController.classicCosineIndex((float) (seam + epsilon) * GAIT_W * 1.0F);
        helper.assertTrue(before > 60000 && after < 5000, "the seam is straddled: " + before + " -> " + after);
        int saturated = PhaseLockedKeyframeController.classicCosineIndex(1.0e12F * GAIT_W);
        helper.assertTrue(saturated == ((Integer.MAX_VALUE & 65535) - 16384 & 65535), "f2i saturation at an absurd age: " + saturated);
        helper.assertTrue(late.controller(Layer.WALK).lastCosineIndex() >= 0 && late.controller(Layer.WALK).lastClipTick() < 40.0D,
                "the instrumentation reports the last index and a clip time inside the clip");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 7: the presented state - only the Beaver declares layers (refuter A, D1)

    /** Every shipped replacement, constructed registry-free (OPT-029 R0: the descriptors' lazy entity-type suppliers). */
    private static List<OreSpawnGeoReplacement<?>> everyReplacement() {
        return List.of(new BeaverGeoReplacement(), new CoinGeoReplacement(), new ElevatorGeoReplacement(),
                new IslandGeoReplacement(), new IslandTooGeoReplacement(), new PurplePowerGeoReplacement(),
                new Robot1GeoReplacement(), new Robot2GeoReplacement(), new Robot3GeoReplacement(),
                new Robot4GeoReplacement(), new Robot5GeoReplacement(), new RockBaseGeoReplacement(),
                new RotatorGeoReplacement(), new VortexGeoReplacement(),
                // the first Tier-2 slice (2026-09-13): the simple-cyclic rigs, transcribed
                new TshirtGeoReplacement(), new MosquitoGeoReplacement(), new CliffRacerGeoReplacement(),
                new BrutalflyGeoReplacement(), new DragonflyGeoReplacement(), new CockateilGeoReplacement(),
                new RubyBirdGeoReplacement());
    }

    /** The Tier-2 descriptors' declared group counts (pinned group by group in T2SeamTests). */
    private static final Map<Class<?>, Integer> TIER_2_GROUPS = Map.of(
            TshirtGeoReplacement.class, 1, MosquitoGeoReplacement.class, 1, CliffRacerGeoReplacement.class, 1,
            BrutalflyGeoReplacement.class, 1, DragonflyGeoReplacement.class, 2, CockateilGeoReplacement.class, 5,
            RubyBirdGeoReplacement.class, 5);

    @GameTest(template = "empty", batch = BATCH)
    public static void kf_007_every_shipped_replacement_declares_its_layers_and_the_seam_registers_nothing_here(GameTestHelper helper) throws ReflectiveOperationException {
        List<OreSpawnGeoReplacement<?>> replacements = everyReplacement();
        helper.assertTrue(replacements.size() == 21, "the twenty-one shipped replacements (G1, Slice 4 and the first Tier-2 slice)");
        Method sealed = OreSpawnGeoReplacement.class.getMethod("registerControllers", AnimatableManager.ControllerRegistrar.class);
        helper.assertTrue(Modifier.isFinal(sealed.getModifiers()),
                "registerControllers is final on the base: registerKeyframeLayers over loadedClips is the single self-gating path");
        int beavers = 0;
        int tier2 = 0;
        for (OreSpawnGeoReplacement<?> replacement : replacements) {
            String name = replacement.getClass().getSimpleName();
            Method own = replacement.getClass().getMethod("registerControllers", AnimatableManager.ControllerRegistrar.class);
            helper.assertTrue(own.getDeclaringClass() == OreSpawnGeoReplacement.class, name + " declares no registerControllers of its own");
            Integer groups = TIER_2_GROUPS.get(replacement.getClass());
            if (replacement instanceof BeaverGeoReplacement) {
                beavers++;
                helper.assertTrue(replacement.keyframeLayers().size() == 3, "the Beaver declares its three frequency groups");
            } else if (groups != null) {
                tier2++;
                helper.assertTrue(replacement.keyframeLayers().size() == groups, name + " declares its " + groups + " frequency groups (Tier 2)");
            } else {
                helper.assertTrue(replacement.keyframeLayers().isEmpty(), name + " declares no keyframe layer: the classic source");
            }
            helper.assertTrue(new AnimatableManager<>(replacement).getAnimationControllers().isEmpty(),
                    name + ": the production registerControllers registers nothing on this server (no bake; the layers empty, or the clips absent)");
        }
        helper.assertTrue(beavers == 1, "exactly one Beaver among the twenty-one");
        helper.assertTrue(tier2 == 7, "the seven Tier-2 descriptors among the twenty-one");
        helper.succeed();
    }

    // ------------------------------------------------------------------ row 8: the seam serves the repaired bake, once per loaded file (refuter B, D2)

    @GameTest(template = "empty", batch = BATCH)
    public static void kf_008_seam_serves_the_repaired_bake_once_per_loaded_file(GameTestHelper helper) throws ReflectiveOperationException {
        // GeckoLibCache.ANIMATIONS: a private static, non-final Map (4.8.4 javap), Collections.emptyMap() on this server
        // (the client reload listener never runs here); GeckoLib's jar carries no module-info (an automatic module,
        // every package open), so the field is seedable and restored in the finally.
        Field cache = GeckoLibCache.class.getDeclaredField("ANIMATIONS");
        cache.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<ResourceLocation, BakedAnimations> original = (Map<ResourceLocation, BakedAnimations>) cache.get(null);
        BeaverGeoReplacement replacement = new BeaverGeoReplacement();
        ResourceLocation resource = replacement.descriptor().animationResource();
        helper.assertTrue(!original.containsKey(resource), "this server holds no bake of the Beaver's animation file");
        BakedAnimations first = bakeClips(resource(REFERENCE_CLIP));
        Map<ResourceLocation, BakedAnimations> seeded = new HashMap<>();
        seeded.put(resource, first);
        try {
            cache.set(null, seeded);
            helper.assertTrue(GeckoLibCache.getBakedAnimations().get(resource) == first, "the cache map seeded by reflection");
            OreSpawnGeoReplacementModel<Beaver, BeaverGeoReplacement> model = new OreSpawnGeoReplacementModel<>(replacement.descriptor());
            Animation walk = model.getAnimation(replacement, KeyframeLayer.WALK);
            helper.assertTrue(walk != null && walk != first.getAnimation(KeyframeLayer.WALK), "the seam serves a copy, never the loaded object");
            List<Keyframe<MathValue>> loaded = frames(first.getAnimation(KeyframeLayer.WALK));
            List<Keyframe<MathValue>> served = frames(walk);
            helper.assertTrue(served.size() == loaded.size() && served.size() == 15, "the same fifteen keyframes (the reference clip's gait density)");
            helper.assertTrue(served.get(2).easingArgs().get(0).get() == loaded.get(1).startValue().get()
                            && loaded.get(2).easingArgs().get(0).get() == loaded.get(2).startValue().get()
                            && served.get(2).easingArgs().get(0).get() != served.get(2).startValue().get(),
                    "the served copy is repaired: segment 2's P0 is the key before its start, where the loaded one's is its own start");
            helper.assertTrue(model.getAnimation(replacement, KeyframeLayer.WALK) == walk, "the same loaded file: the same repaired instance (identity)");
            helper.assertTrue(model.getAnimation(replacement, "walk_teeth") != null && model.getAnimation(replacement, "walk_tail") != null,
                    "every clip of the file is served from the repaired copy");
            helper.assertTrue(model.getAnimation(replacement, "no_such_clip") == null,
                    "a clip the present file lacks: null, GeckoLib's own answer for a present file (no fallback walk)");

            BakedAnimations second = bakeClips(resource(REFERENCE_CLIP));
            seeded.put(resource, second);
            Animation again = model.getAnimation(replacement, KeyframeLayer.WALK);
            helper.assertTrue(again != walk && again != second.getAnimation(KeyframeLayer.WALK),
                    "a new bake under the same key (a resource reload): repaired afresh, never the loaded object");
            helper.assertTrue(frames(again).get(2).easingArgs().get(0).get() == frames(second.getAnimation(KeyframeLayer.WALK)).get(1).startValue().get(),
                    "the fresh copy is repaired too");
            helper.assertTrue(model.getAnimation(replacement, KeyframeLayer.WALK) == again, "and served by identity again");

            Map<ResourceLocation, BakedAnimations> swapped = new HashMap<>();
            swapped.put(resource, first);
            cache.set(null, swapped);
            Animation third = model.getAnimation(replacement, KeyframeLayer.WALK);
            helper.assertTrue(third != again && third != walk && third != first.getAnimation(KeyframeLayer.WALK),
                    "the whole map swapped (GeckoLibCache.reload): the identity test repairs whatever bake is served now");
            helper.assertTrue(model.getAnimation(replacement, KeyframeLayer.WALK) == third, "one repaired pair is retained, never accumulated");
        } finally {
            cache.set(null, original);
        }
        helper.assertTrue(GeckoLibCache.getBakedAnimations() == original, "the cache map restored");
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    /** Every global flag the rows flip, read once and restored in every finally (the ModernMasterOverrideTests idiom). */
    private record Flags(boolean modernEnabled, boolean artistAnimations, List<? extends String> classicSpecies) {
        static Flags read() {
            return new Flags(OreSpawnConfig.MODERN_ENABLED.get(), OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.get(),
                    List.copyOf(OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.get()));
        }

        void restore() {
            OreSpawnConfig.MODERN_CLASSIC_ANIMATION_SPECIES.set(this.classicSpecies);
            OreSpawnConfig.MODERN_ARTIST_ANIMATIONS.set(this.artistAnimations);
            OreSpawnConfig.MODERN_ENABLED.set(this.modernEnabled);
        }
    }

    private static AnimatableManager.ControllerRegistrar registrar() {
        return new AnimatableManager.ControllerRegistrar(new ArrayList<>());
    }

    private static String resource(String path) {
        try (InputStream stream = KeyframeLegTests.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("missing classpath resource " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException(failure);
        }
    }

    /** FileLoader.loadAnimationsFile 51-69: the document's animations member through KeyFramesAdapter.GEO_GSON. */
    private static BakedAnimations bakeClips(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(root, "animations"), BakedAnimations.class);
    }

    private static BakedGeoModel bakeRig(String json) {
        Model model = KeyFramesAdapter.GEO_GSON.fromJson(json, Model.class);
        return BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
    }

    private static Map<String, GeoBone> bones(BakedGeoModel model) {
        Map<String, GeoBone> bones = new TreeMap<>();
        for (GeoBone bone : model.topLevelBones()) {
            collect(bone, bones);
        }
        return bones;
    }

    private static void collect(GeoBone bone, Map<String, GeoBone> bones) {
        bones.put(bone.getName(), bone);
        bone.getChildBones().forEach(child -> collect(child, bones));
    }

    /** A frozen mob with its feet ON the floor (rel y 0, F0.7; the GeoCacheEvictionTests idiom). */
    private static <M extends Mob> M spawnFrozen(GameTestHelper helper, EntityType<M> type, BlockPos pos) {
        M mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setNoGravity(true);
        mob.setOnGround(true);
        return mob;
    }

    /** A clip of {@code degrees} on one bone's rotation X at uniform times over {@code seconds}, baked through GeckoLib's loader. */
    private static Animation syntheticClip(String name, String bone, double[] degrees, String lerp, double seconds, boolean loop) {
        JsonObject rotation = new JsonObject();
        int segments = degrees.length - 1;
        for (int index = 0; index < degrees.length; index++) {
            JsonObject key = new JsonObject();
            JsonArray post = new JsonArray();
            post.add(degrees[index]);
            post.add(0);
            post.add(0);
            key.add("post", post);
            key.addProperty("lerp_mode", lerp);
            rotation.add(index == 0 ? "0.0" : String.format(Locale.ROOT, "%.10f", seconds * index / segments), key);
        }
        JsonObject boneObject = new JsonObject();
        boneObject.add("rotation", rotation);
        JsonObject bones = new JsonObject();
        bones.add(bone, boneObject);
        JsonObject clip = new JsonObject();
        clip.addProperty("loop", loop);
        clip.addProperty("animation_length", seconds);
        clip.add("bones", bones);
        JsonObject animations = new JsonObject();
        animations.add(name, clip);
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.8.0");
        root.add("animations", animations);
        Animation animation = bakeClips(root.toString()).getAnimation(name);
        if (animation == null) {
            throw new IllegalStateException("GeckoLib baked no clip " + name);
        }
        return animation;
    }

    /** A full-amplitude cosine loop of {@code keys} keys (both ends), the generator's shape. */
    private static Animation syntheticLoop(String name, String bone, int keys, double amplitudeDegrees, String lerp, double seconds) {
        double[] degrees = new double[keys];
        for (int index = 0; index < keys; index++) {
            degrees[index] = amplitudeDegrees * Math.cos(2.0D * Math.PI * index / (keys - 1));
        }
        return syntheticClip(name, bone, degrees, lerp, seconds, true);
    }

    private static List<Keyframe<MathValue>> frames(Animation animation) {
        return animation.boneAnimations()[0].rotationKeyFrames().xKeyframes();
    }

    /** EasingType$CatmullRomEasing.getPointOnSpline, verbatim. */
    private static double catmullRom(double t, double p0, double p1, double p2, double p3) {
        return 0.5D * (2.0D * p1 + (p2 - p0) * t + (2.0D * p0 - 5.0D * p1 + 4.0D * p2 - p3) * t * t
                + (3.0D * p1 - p0 - 3.0D * p2 + p3) * t * t * t);
    }

    /** GeckoLib's own evaluator on one keyframe at normalised time {@code t} (CatmullRomEasing.apply 17-113). */
    private static double evaluate(Keyframe<MathValue> frame, double t) {
        AnimationPoint point = new AnimationPoint(frame, t * frame.length(), frame.length(), frame.startValue().get(), frame.endValue().get());
        return EasingType.CATMULLROM.apply(point, null, t);
    }

    private static boolean close(double a, double b) {
        return Math.abs(a - b) <= 1.0e-9D;
    }

    // ------------------------------------------------------------------ the synthetic pipeline: a rig, a serving model, layers

    private enum Layer { WALK, IDLE }

    private static final class ProbeAnimatable implements GeoAnimatable {
        private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
        private final List<AnimationController<ProbeAnimatable>> controllers = new ArrayList<>();

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
            this.controllers.forEach(registrar::add);
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }

        @Override
        public double getTick(Object relatedObject) {
            return 0.0D;
        }
    }

    /** A GeoModel serving the given clips - the stand-in for the shipped model's getAnimation on this server. */
    private static final class ServingModel<T extends GeoAnimatable> extends GeoModel<T> {
        private final BakedAnimations clips;
        private final ResourceLocation resource;

        ServingModel(BakedAnimations clips, ResourceLocation resource) {
            this.clips = clips;
            this.resource = resource;
        }

        @Override
        public Animation getAnimation(T animatable, String name) {
            Animation animation = this.clips.getAnimation(name);
            if (animation == null) {
                throw new IllegalStateException("no clip " + name);
            }
            return animation;
        }

        /**
         * No Molang query setup: {@code AnimationProcessor.tickAnimation} calls this first, and GeckoLib's
         * implementation reaches {@code MolangQueries}, whose client-side references the dedicated server's
         * {@code RuntimeDistCleaner} refuses (the kf17c gate: three rows failed at class load). The reference
         * clips carry constants only (the package tool rejects Molang, rule 7), so the queries feed nothing here;
         * the evaluator itself - the controller, {@code AnimationProcessor}, {@code EasingType} - is untouched.
         */
        @Override
        public void applyMolangQueries(AnimationState<T> animationState, double animTime) {
        }

        @Override
        public ResourceLocation getModelResource(T animatable) {
            return this.resource;
        }

        @Override
        public ResourceLocation getTextureResource(T animatable) {
            return this.resource;
        }

        @Override
        public ResourceLocation getAnimationResource(T animatable) {
            return this.resource;
        }
    }

    /** The two-bone rig with the requested layers on bone {@code a}: walk (x amplitude) and idle (x (1 - amplitude)). */
    private static final class Rig {
        private final ProbeAnimatable animatable = new ProbeAnimatable();
        private final ServingModel<ProbeAnimatable> model;
        private final Map<String, GeoBone> bones;
        private final Map<Layer, PhaseLockedKeyframeController<ProbeAnimatable>> controllers = new LinkedHashMap<>();
        private final AnimatableManager<ProbeAnimatable> manager;

        Rig(BakedAnimations clips, List<Layer> layers, boolean additive) {
            this.model = new ServingModel<>(clips, ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gametest/keyframe_leg"));
            BakedGeoModel baked = bakeRig(RIG);
            this.model.getAnimationProcessor().setActiveModel(baked);
            this.bones = bones(baked);
            for (Layer layer : layers) {
                RawAnimation loop = RawAnimation.begin().thenLoop(layer == Layer.WALK ? "walk" : "crouch");
                PhaseLockedKeyframeController<ProbeAnimatable> controller = PhaseLockedKeyframeController.amplitudeScaledRotations(
                        this.animatable, layer.name(), GAIT_W, state -> (float) state.getAnimationTick(), Set.of("a"),
                        layer == Layer.WALK ? AnimationState::getLimbSwingAmount : state -> 1.0F - state.getLimbSwingAmount(),
                        additive, state -> state.setAndContinue(loop));
                this.animatable.controllers.add(controller);
                this.controllers.put(layer, controller);
            }
            this.manager = new AnimatableManager<>(this.animatable);
        }

        PhaseLockedKeyframeController<ProbeAnimatable> controller(Layer layer) {
            return this.controllers.get(layer);
        }

        /** One frame: bone a's rotation X after the layers ran through GeckoLib's processor. */
        float sample(double age, float amplitude) {
            AnimationState<ProbeAnimatable> state = new AnimationState<>(this.animatable, 0.0F, amplitude, 0.0F, amplitude != 0.0F);
            state.animationTick = age;
            this.model.getAnimationProcessor().tickAnimation(this.animatable, this.model, this.manager, age, state, true);
            return this.bones.get("a").getRotX();
        }
    }
}
