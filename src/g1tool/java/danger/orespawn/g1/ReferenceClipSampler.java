package danger.orespawn.g1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.entity.client.DrawOrder;
import danger.orespawn.entity.client.FaceOrder;
import danger.orespawn.entity.client.GeoReplacementDescriptor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;

/**
 * The reference-clip sampler (owner 2026-09-13, second set, addendum item 27 (3)): for every seam rig with a
 * manifest entry, the shipped classic hook sampled at FIXED inputs into one reference-only Bedrock clip,
 * {@code tools/reference_clips/<registry>_reference.animation.json}, one file per REGISTRY (a shared rig's
 * consumers each get their own file, identical content), plus the index {@code reference_clips.json} the package
 * generator ({@code tools/artist_package.py package}) reads for the SPEC's "Reference clip (reference-only)"
 * section and the manifest's {@code reference_clip} block. The clip is never shipped and never returned: the
 * package checker refuses it and the asset audit refuses it under {@code src/main/resources}.
 *
 * <p>THE POSE SOURCE (the S4 doctrine): the shipped {@code OreSpawnGeoReplacement} named by the manifest's
 * {@code candidate_class}, instantiated registry-free ({@link S4CandidateRuntime#instantiate}) and posed through
 * {@code OreSpawnGeoReplacement.pose} on explicit {@code PoseInputs} over a fresh bake of the SHIPPED geo the
 * descriptor names ({@code modelResource}), exactly as the harness's geo dumps pose it
 * ({@link S4CandidateRuntime#evaluateProductionHook}). The Beaver's shipped hook takes the renderer's
 * {@code AnimationState} (no {@code PoseInputs} form), so it is sampled through the probe's accepted Beaver path
 * ({@link G1AnimationRuntime.Evaluator#evaluateBeaverCodeDriven}, the G1 legacy-parity exception: the same
 * {@code Mth.cos} formulas in {@code GeoModel.setCustomAnimations}). A static rig without a hook (the Elevator,
 * the Vortex) is one key at bind. A species whose descriptor has no classic hook of either form is skipped and
 * said so (the Queen is native GeckoLib and has no manifest entry: her eight native clips are her reference).</p>
 *
 * <p>THE FIXED INPUTS (the ruling's, pinned): {@code limbSwingAmount} 1.0; {@code limbSwing} advancing 1.0 per tick
 * from 0 - the classic renderer's own feed at full walking speed: vanilla {@code WalkAnimationState.update} does
 * {@code speed += (movementSpeed - speed) * multiplier; position += speed}, and both {@code LivingEntityRenderer}
 * and GeckoLib's {@code GeoReplacedEntityRenderer} hand {@code walkAnimation.position(partialTick)} as limbSwing
 * and {@code min(1, speed)} as limbSwingAmount, so at speed 1 the position advances exactly 1.0 per tick;
 * {@code ageInTicks} advancing 1.0 per tick from 0 ({@code getBob}: {@code tickCount + partialTick}, partialTick 0);
 * {@code netHeadYaw} 0 and {@code headPitch} 0 (looking ahead); every entity-state flag at its rest value - a
 * {@link ProbeSubject} built from an EMPTY state: {@code attacking} 0, {@code ri1} 0, {@code rock_type} 0,
 * {@code rf1} 0 - with its entity RNG seeded 0 ({@code RandomSource.create(0)}) ONCE per clip and evolving across
 * the keys, one pose call per key (a fan angle or a roll advances per call, as it advances per rendered frame
 * in-game); full health (no shipped hook reads health). Twenty samples per second: one key per tick.</p>
 *
 * <p>THE SPAN, per rig (the ruling: one natural period where there is one, two seconds otherwise; static rigs one
 * key; multi-frequency rigs the period of the slowest group up to 40 ticks): a rig whose manifest declares
 * {@code channels} derives its rule from them (the effective frequency {@code omega * wingspeed} per channel; one
 * distinct frequency = its natural period {@code 2 pi / f}; several = the slowest one's period capped at 40 ticks);
 * a hook rig without declared channels (the Slice 4 rigs) carries its rule in {@link #RULES}, each with the source
 * line it was read from. Keys sit at every whole tick inside the span plus a CLOSING key at the span's end (the
 * pose sampled AT the period, which a periodic hook returns to its first key's pose - Bedrock and GeckoLib hold the
 * last key until {@code animation_length}, so without it the loop would hitch by up to a tick); the index records
 * the seam delta (closing key against first key, degrees, reduced mod 360) so a sawtooth channel's wrap is visible.</p>
 *
 * <p>THE KEYS: a rotation key per bone per sample, DELTAS from the bone's bind under the converter's sign rule
 * (authored X = +classic degrees, Y and Z negated; the rule {@code tools/keyframe_clip.py} and {@link KeyframeLeg}
 * document): the bake's internal rotation is classic {@code (-x, y, -z)} ({@code OreSpawnGeoReplacement}'s basis
 * facts), so the classic delta is {@code (-(Ix - Bx), Iy - By, -(Iz - Bz))} and the authored key
 * {@code (+dCx, -dCy, -dCz)} - equivalently {@code (-dIx, -dIy, +dIz)} in internal terms, which is exactly what
 * GeckoLib 4.8.4 undoes at load (X and Y rotation keys negated, Z kept) before adding the key to the bone's initial
 * snapshot. A position key per bone per sample where the hook writes positions (Robot4's cannon follow through
 * {@code moveTo}; every other packaged hook writes rotations only): GeckoLib reads position keys unnegated and sets
 * them absolutely, and a fresh bake's offsets are 0, so the authored key is the internal offset itself -
 * {@code (-dx, -dy, +dz)} of the classic pivot move {@code (dx, dy, dz)}, the numbers {@code moveTo} writes.
 * Values rounded to 1e-10 (degrees / model units), never {@code -0.0}; {@code lerp_mode} linear; two-space JSON,
 * LF, UTF-8; deterministic, so two runs compare byte for byte.</p>
 *
 * <p>Usage: {@code ReferenceClipSampler <output-dir> <manifest>...} (build.gradle {@code referenceClips}).</p>
 */
public final class ReferenceClipSampler {
    static final String CLIP_NAME = "reference";
    static final String INDEX_FILE = "reference_clips.json";
    static final String FILE_SUFFIX = "_reference.animation.json";
    static final double TICKS_PER_SECOND = 20.0D;
    static final double TWO_SECONDS_TICKS = 40.0D;
    static final double TWO_PI = 2.0D * Math.PI;
    /** The generator's rounding of a key (tools/keyframe_clip.py VALUE_DECIMALS; KeyframeLeg.VALUE_ROUNDING). */
    static final double VALUE_ROUNDING = 1.0e10D;
    static final double FREQUENCY_EPSILON = 1.0e-9D;
    static final float LIMB_SWING_AMOUNT = 1.0F;
    static final float NET_HEAD_YAW = 0.0F;
    static final float HEAD_PITCH = 0.0F;
    /** limbSwing advance per tick at limbSwingAmount 1 (WalkAnimationState: position += speed, speed 1.0). */
    static final double LIMB_SWING_PER_TICK = 1.0D;
    static final String INPUTS_STATEMENT = "limbSwingAmount 1.0; limbSwing = t (advancing 1.0 per tick from 0: vanilla "
            + "WalkAnimationState.update does speed += (movementSpeed - speed) * multiplier; position += speed, and the classic "
            + "renderer hands walkAnimation.position(partialTick) as limbSwing and min(1, speed) as limbSwingAmount, so at "
            + "speed 1 the position advances exactly 1.0 per tick); ageInTicks = t (getBob: tickCount + partialTick, "
            + "partialTick 0); netHeadYaw 0; headPitch 0 (looking ahead); every entity-state flag at its rest value "
            + "(attacking 0, ri1 0, rock_type 0, rf1 0: a ProbeSubject built from an empty state); the entity RNG seeded 0 "
            + "once per clip and evolving across the keys; one pose call per key, 20 keys per second; full health "
            + "(no shipped hook reads health)";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** The registry each manifest id packages under (the file name); an id without a row is refused. */
    static final Map<String, String> REGISTRIES = new TreeMap<>(Map.ofEntries(
            Map.entry("model_elevator", "elevator"),
            Map.entry("model_beaver", "beaver"),
            Map.entry("model_vortex", "vortex"),
            Map.entry("model_coin", "coin"),
            Map.entry("model_island", "island"),
            Map.entry("model_islandtoo", "island_too"),
            Map.entry("model_robot1", "robot_1"),
            Map.entry("model_robot2", "robot_2"),
            Map.entry("model_robot3", "robot_3"),
            Map.entry("model_robot4", "robot_4"),
            Map.entry("model_robot5", "robot_5"),
            Map.entry("model_rockbase", "rock_base"),
            Map.entry("model_rotator", "rotator"),
            Map.entry("model_purplepower", "purple_power"),
            Map.entry("model_tshirt", "tshirt"),
            Map.entry("model_mosquito", "mosquito"),
            Map.entry("model_cliffracer", "cliff_racer"),
            Map.entry("model_brutalfly", "brutalfly"),
            Map.entry("model_dragonfly", "dragonfly"),
            Map.entry("model_cockateil", "cockateil"),
            Map.entry("model_ruby_bird", "ruby_bird"),
            Map.entry("model_firefly", "firefly"),
            Map.entry("model_goldfish", "gold_fish"),
            Map.entry("model_ant", "ant"),
            Map.entry("model_rainbow_ant", "rainbow_ant"),
            Map.entry("model_red_ant", "red_ant"),
            Map.entry("model_termite", "termite"),
            Map.entry("model_unstable_ant", "unstable_ant")));

    /** The sampling rule of a hook rig whose manifest declares no channels, with the source it was read from. */
    record Rule(String kind, double spanTicks, String note) {
        static Rule oneKey(String note) {
            return new Rule("one_key", 0.0D, note);
        }

        static Rule naturalPeriod(double ticks, String note) {
            return new Rule("natural_period", ticks, note);
        }

        static Rule slowestCapped(double slowestTicks, String note) {
            return slowestTicks > TWO_SECONDS_TICKS
                    ? new Rule("multi_frequency_capped_two_seconds", TWO_SECONDS_TICKS,
                            note + "; the slowest group's period " + fmt(slowestTicks) + " ticks exceeds 40, capped at 40 ticks (two seconds)")
                    : new Rule("multi_frequency_slowest_group", slowestTicks, note);
        }

        static Rule twoSeconds(String note) {
            return new Rule("two_seconds_no_period", TWO_SECONDS_TICKS, note);
        }
    }

    static final Map<String, Rule> RULES = new TreeMap<>(Map.ofEntries(
            Map.entry("model_coin", Rule.naturalPeriod(TWO_PI / (double) (0.05F * 0.22F),
                    "one channel: coin.yRot = cos(ageInTicks * 0.05F * 0.22F) * PI (CoinGeoReplacement.java:39, ModelCoin.java:49; "
                            + "orig ModelCoin.java:32, wingspeed 0.22): one natural period 2 pi / (0.05 x 0.22) = 571.2 ticks")),
            Map.entry("model_island", Rule.slowestCapped(TWO_PI / (double) (0.05F * 1.0F),
                    "nine channels at 0.05..0.058 rad/tick x wingspeed 1.0 (IslandGeoReplacement.poseIslandRig:34-42, "
                            + "ModelIsland.java:63-71; orig ModelIsland.java:45-53): nine frequencies, the slowest 0.05 rad/tick")),
            Map.entry("model_islandtoo", Rule.slowestCapped(TWO_PI / (double) (0.05F * 1.0F),
                    "the Island's nine channels (IslandTooGeoReplacement.java:27 -> IslandGeoReplacement.poseIslandRig; "
                            + "ModelIslandToo.java:33-41; orig ModelIsland.java:45-53): the slowest 0.05 rad/tick")),
            Map.entry("model_robot1", Rule.slowestCapped(360.0D / 0.75D,
                    "two frequencies: the feet cos(limbSwing * 1.5F) x PI x 0.75 x limbSwingAmount (period 2 pi / 1.5 = 4.19 ticks "
                            + "at limbSwing +1 per tick; Robot1GeoReplacement.java:36-40, ModelRobot1.java:215-219; orig ModelRobot1.java:215-217 "
                            + "reads f2 = ageInTicks there) and the five keys toRadians(ageInTicks * 0.75) (one turn per 480 ticks; "
                            + "Robot1GeoReplacement.java:42-47, ModelRobot1.java:221-226; orig :218-222)")),
            Map.entry("model_robot5", Rule.naturalPeriod(TWO_PI / (double) 0.15F,
                    "one channel: the wheels |limbSwing * 0.15F mod 2 pi| (a sawtooth that wraps every 2 pi / 0.15 = 41.9 ticks at "
                            + "limbSwing +1 per tick; Robot5GeoReplacement.java:32-41, ModelRobot5.java:104-113; orig ModelRobot5.java:104-112 "
                            + "reads f2 = ageInTicks there); the turret yaw follows netHeadYaw / 2 = 0")),
            Map.entry("model_robot2", Rule.naturalPeriod(TWO_PI / (double) 0.3F,
                    "at rest one channel is live: the legs cos(ageInTicks * 0.3F) x PI x 0.12 x limbSwingAmount (Robot2GeoReplacement.java:40-46, "
                            + "ModelRobot2.java:141-147; orig ModelRobot2.java:133-137); the arms' windmill (rad(ageInTicks * 20)) needs a "
                            + "re-roll of ri1 while attacking (orig :139-170) and ri1 stays 0 at rest; the head follows netHeadYaw = 0")),
            Map.entry("model_robot3", Rule.naturalPeriod(TWO_PI / (double) 0.55F,
                    "at rest one channel is live: the legs cos(ageInTicks * 0.55F) x PI x 0.12 x limbSwingAmount (Robot3GeoReplacement.java:45-51, "
                            + "ModelRobot3.java:169-175; orig ModelRobot3.java:163-167); the arms' swing is latched off while ri1 is 0 "
                            + "(orig :169-186), holding their bent rest (-1.0 / +1.0 rad); the turret follows netHeadYaw / 2 = 0")),
            Map.entry("model_robot4", Rule.naturalPeriod(TWO_PI / (double) 0.5F,
                    "at rest one channel is live: the legs cos(ageInTicks * 0.5F) x PI x 0.15 x limbSwingAmount with the fixed calf / knee "
                            + "guard / thigh offsets (Robot4GeoReplacement.java:46-66, ModelRobot4.java:430-450; orig ModelRobot4.java:421-437); "
                            + "the shield pump and the cannon aim need attacking (orig :439-474) and rest at 0; the cannon assembly's "
                            + "pivot follows the upper arm (a constant position at rest; orig :475-500)")),
            Map.entry("model_rockbase", Rule.oneKey(
                    "no rotation or position channel: the pose is which cubes are visible for the rock type "
                            + "(RockBaseGeoReplacement.java:46-85, ModelRockBase.java:186-221; orig ModelRockBase.java:182-222), "
                            + "and visibility has no Bedrock animation channel; the SPEC's plain-language transcription carries it")),
            Map.entry("model_rotator", Rule.naturalPeriod(180.0D,
                    "the three fans turn by RenderInfo.rf1 degrees, advanced 2 degrees per pose call and wrapped to 0 past 359 "
                            + "(RotatorGeoReplacement.java:58-67, RotatorModel.java:85-90; orig ModelRotator.java:52-77): the call "
                            + "sequence 0, 2, ..., 358, 0 closes every 180 calls - one call per key, so 180 ticks (in-game the advance "
                            + "is per rendered frame, ENT-S-147)")),
            Map.entry("model_purplepower", Rule.twoSeconds(
                    "no period: the three fans take three fresh rolls of the level random per pose call (nextFloat() * 360 in X, Y, Z "
                            + "order; PurplePowerGeoReplacement.java:121-136, ModelPurplePower.java:187-194; orig ModelPurplePower.java:57 / "
                            + ":66 / :75) - two seconds from the seed-0 random"))));

    private ReferenceClipSampler() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: ReferenceClipSampler <output-dir> <manifest>...");
        }
        Path outputDir = Path.of(args[0]).toAbsolutePath().normalize();
        Files.createDirectories(outputDir);
        Map<String, JsonObject> index = new TreeMap<>();
        Map<String, String> sampledClasses = new TreeMap<>();
        for (int i = 1; i < args.length; i++) {
            Path manifestPath = Path.of(args[i]).toAbsolutePath().normalize();
            JsonObject manifest = readJson(manifestPath);
            Path repositoryRoot = manifestPath.getParent().getParent();
            for (JsonElement element : manifest.getAsJsonArray("models")) {
                JsonObject spec = element.getAsJsonObject();
                String id = spec.get("id").getAsString();
                String registry = REGISTRIES.get(id);
                if (registry == null) {
                    throw new IllegalStateException(id + " (" + manifestPath.getFileName() + "): no registry row in "
                            + "ReferenceClipSampler.REGISTRIES - add the manifest id with its ModEntities registry name");
                }
                String modelClass = spec.get("class").getAsString();
                String previous = sampledClasses.get(registry);
                if (previous != null) {
                    if (!previous.equals(modelClass)) {
                        throw new IllegalStateException(id + ": a second manifest entry for registry " + registry
                                + " names class " + modelClass + " but the first named " + previous);
                    }
                    System.out.println("skip  " + registry + ": " + id + " in " + manifestPath.getFileName()
                            + " duplicates an entry already sampled (the same class " + modelClass + ")");
                    continue;
                }
                JsonObject entry = sample(spec, id, registry, manifestPath, repositoryRoot, outputDir);
                sampledClasses.put(registry, modelClass);
                if (entry != null) {
                    index.put(registry, entry);
                }
            }
        }
        JsonObject root = new JsonObject();
        root.addProperty("schema_version", 1);
        root.addProperty("generated_by", "danger.orespawn.g1.ReferenceClipSampler (build.gradle referenceClips)");
        root.addProperty("purpose", "reference-only clips sampled from every packaged species' classic hook (owner 2026-09-13, "
                + "second set, addendum item 27 (3)); never shipped, never returned - the package checker and the asset audit refuse them");
        root.addProperty("clip_name", CLIP_NAME);
        root.addProperty("samples_per_second", TICKS_PER_SECOND);
        root.addProperty("fixed_inputs", INPUTS_STATEMENT);
        root.addProperty("span_rule", "one natural period where there is one (a closing key at the period); two seconds (40 ticks) "
                + "where the rig has no single natural period; a static rig one key; a multi-frequency rig the period of the slowest "
                + "group up to 40 ticks; keys at every whole tick inside the span plus the closing key at its end");
        root.addProperty("rotation_rule", "a rotation key per bone per sample as the DELTA from the bone's bind under the converter's "
                + "sign rule: authored X = +classic degrees, Y and Z negated (tools/keyframe_clip.py; KeyframeLeg); rounded to 1e-10 "
                + "degrees, never -0.0; lerp_mode linear");
        root.addProperty("position_rule", "a position key per bone per sample where the hook writes positions (OreSpawnGeoReplacement.moveTo): "
                + "the internal offset the hook wrote, (-dx, -dy, +dz) of the classic pivot move (dx, dy, dz) in model units - GeckoLib reads "
                + "position keys unnegated and sets them absolutely over a fresh bake's zero offsets");
        JsonArray clips = new JsonArray();
        index.values().forEach(clips::add);
        root.add("clips", clips);
        writeJson(outputDir.resolve(INDEX_FILE), root);
        System.out.println("wrote " + outputDir.resolve(INDEX_FILE) + " (" + index.size() + " clips)");
    }

    /** One manifest entry: the clip file and its index row, or {@code null} for a species without a classic hook (said so). */
    private static JsonObject sample(JsonObject spec, String id, String registry, Path manifestPath, Path repositoryRoot,
                                     Path outputDir) throws Exception {
        String animationKind = spec.get("animation_kind").getAsString();
        String candidateClass = spec.has("candidate_class") ? spec.get("candidate_class").getAsString() : null;
        boolean beaverPath = candidateClass == null && "gait_scaled".equals(animationKind)
                && spec.has("keyframe_reference_leg")
                && "geckolib_custom_animation_code".equals(spec.has("candidate_animation_path")
                        ? spec.get("candidate_animation_path").getAsString() : "");
        boolean isStatic = "static".equals(animationKind);
        String hook;
        Path geoPath;
        boolean faceOrderRequired = false;
        if (candidateClass != null) {
            GeoReplacementDescriptor<?> descriptor = S4CandidateRuntime.instantiate(candidateClass).descriptor();
            geoPath = repositoryRoot.resolve("src/main/resources/assets/orespawn").resolve(descriptor.modelResource().getPath());
            faceOrderRequired = descriptor.cubeFaceOrderRequired();
            hook = candidateClass + ".applyCustomAnimations(AnimationProcessor, PoseInputs) through OreSpawnGeoReplacement.pose "
                    + "(the S4 doctrine: the shipped replacement, registry-free, on explicit PoseInputs)";
        } else if (beaverPath) {
            String legClass = spec.getAsJsonObject("keyframe_reference_leg").get("candidate_class").getAsString();
            GeoReplacementDescriptor<?> descriptor = S4CandidateRuntime.instantiate(legClass).descriptor();
            geoPath = repositoryRoot.resolve("src/main/resources/assets/orespawn").resolve(descriptor.modelResource().getPath());
            faceOrderRequired = descriptor.cubeFaceOrderRequired();
            hook = "G1AnimationRuntime.Evaluator.evaluateBeaverCodeDriven (the probe's accepted Beaver path - the G1 legacy-parity "
                    + "exception's GeoModel.setCustomAnimations with ModelBeaver's exact Mth.cos formulas): the shipped "
                    + legClass + " hook takes the renderer's AnimationState and has no PoseInputs form to pose registry-free";
        } else if (isStatic) {
            geoPath = repositoryRoot.resolve("src/main/resources/assets/orespawn/geo/entity")
                    .resolve(id.substring("model_".length()) + ".geo.json");
            hook = "none (a static rig: no classic hook; one key at bind)";
        } else {
            System.out.println("skip  " + registry + ": " + id + " declares no classic hook (animation_kind " + animationKind
                    + ", no candidate_class) - no sampled clip; its own clips are its reference");
            return null;
        }
        if (!Files.isRegularFile(geoPath)) {
            throw new IllegalStateException(id + ": the shipped geo " + geoPath + " does not exist");
        }
        Rule rule = ruleFor(spec, id, isStatic);
        Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(geoPath, StandardCharsets.UTF_8), Model.class);
        JsonObject geoJson = readJson(geoPath);
        List<String> drawOrder = DrawOrder.read(geoJson);
        Map<String, List<List<Direction>>> faceOrder = FaceOrder.read(geoJson);
        G1AnimationRuntime.Evaluator evaluator = G1AnimationRuntime.evaluator(rawModel, drawOrder, faceOrder, faceOrderRequired);
        Map<String, float[]> bindRotations = new TreeMap<>();
        Map<String, float[]> bindPositions = new TreeMap<>();
        G1AnimationRuntime.EvaluatedModel bind = evaluator.bindPose();
        bind.bones().forEach((name, bone) -> {
            bindRotations.put(name, new float[]{bone.getRotX(), bone.getRotY(), bone.getRotZ()});
            bindPositions.put(name, new float[]{bone.getPosX(), bone.getPosY(), bone.getPosZ()});
        });

        // The sample times: every whole tick inside the span, then the closing key at the span's end.
        List<Double> ticks = new ArrayList<>();
        if ("one_key".equals(rule.kind())) {
            ticks.add(0.0D);
        } else {
            int whole = (int) Math.ceil(rule.spanTicks() - 1.0e-9D);
            for (int k = 0; k < whole; k++) {
                ticks.add((double) k);
            }
            ticks.add(rule.spanTicks());
        }

        ProbeSubject subject = new ProbeSubject(restState());  // every flag at its rest value; the RNG seeded 0, once
        Map<String, List<double[]>> rotationKeys = new TreeMap<>();   // bone -> authored (x, y, z) degrees per sample
        Map<String, List<double[]>> positionKeys = new TreeMap<>();   // bone -> authored (x, y, z) units per sample
        TreeSet<String> hiddenBones = new TreeSet<>();
        for (double t : ticks) {
            G1AnimationRuntime.EvaluatedModel posed;
            if (candidateClass != null) {
                posed = S4CandidateRuntime.evaluateProductionHook(rawModel, drawOrder, faceOrder, candidateClass,
                        new S4CandidateRuntime.Inputs((float) t, (float) (t * LIMB_SWING_PER_TICK), LIMB_SWING_AMOUNT,
                                NET_HEAD_YAW, HEAD_PITCH), subject);
            } else if (beaverPath) {
                posed = evaluator.evaluateBeaverCodeDriven(t, LIMB_SWING_AMOUNT);
            } else {
                posed = evaluator.bindPose();
            }
            if (!posed.bones().keySet().equals(bindRotations.keySet())) {
                throw new IllegalStateException(id + ": the posed bake's bones differ from the bind bake's");
            }
            for (Map.Entry<String, GeoBone> boneEntry : posed.bones().entrySet()) {
                String name = boneEntry.getKey();
                GeoBone bone = boneEntry.getValue();
                float[] bindRotation = bindRotations.get(name);
                // classic = (-Ix, Iy, -Iz); the classic delta from bind; authored = (+dCx, -dCy, -dCz).
                double dCx = -(double) bone.getRotX() + (double) bindRotation[0];
                double dCy = (double) bone.getRotY() - (double) bindRotation[1];
                double dCz = -(double) bone.getRotZ() + (double) bindRotation[2];
                rotationKeys.computeIfAbsent(name, key -> new ArrayList<>()).add(new double[]{
                        round(Math.toDegrees(dCx)), round(-Math.toDegrees(dCy)), round(-Math.toDegrees(dCz))});
                float[] bindPosition = bindPositions.get(name);
                positionKeys.computeIfAbsent(name, key -> new ArrayList<>()).add(new double[]{
                        round((double) bone.getPosX() - (double) bindPosition[0]),
                        round((double) bone.getPosY() - (double) bindPosition[1]),
                        round((double) bone.getPosZ() - (double) bindPosition[2])});
                if (bone.isHidden()) {
                    hiddenBones.add(name);
                }
            }
        }
        // Position keys only where the hook wrote a position on the bone at any sample.
        List<String> positionBones = new ArrayList<>();
        for (Map.Entry<String, List<double[]>> entry : positionKeys.entrySet()) {
            if (entry.getValue().stream().anyMatch(v -> v[0] != 0.0D || v[1] != 0.0D || v[2] != 0.0D)) {
                positionBones.add(entry.getKey());
            }
        }
        List<String> movingBones = new ArrayList<>();
        for (Map.Entry<String, List<double[]>> entry : rotationKeys.entrySet()) {
            if (entry.getValue().stream().anyMatch(v -> v[0] != 0.0D || v[1] != 0.0D || v[2] != 0.0D)) {
                movingBones.add(entry.getKey());
            }
        }

        double spanSeconds = "one_key".equals(rule.kind()) ? 1.0D / TICKS_PER_SECOND : round(rule.spanTicks() / TICKS_PER_SECOND);
        JsonObject bones = new JsonObject();
        for (Map.Entry<String, List<double[]>> entry : rotationKeys.entrySet()) {
            JsonObject bone = new JsonObject();
            bone.add("rotation", keys(ticks, entry.getValue()));
            if (positionBones.contains(entry.getKey())) {
                bone.add("position", keys(ticks, positionKeys.get(entry.getKey())));
            }
            bones.add(entry.getKey(), bone);
        }
        JsonObject clip = new JsonObject();
        clip.addProperty("loop", true);
        clip.addProperty("animation_length", spanSeconds);
        clip.add("bones", bones);
        JsonObject animations = new JsonObject();
        animations.add(CLIP_NAME, clip);
        JsonObject document = new JsonObject();
        document.addProperty("format_version", "1.8.0");
        document.add("animations", animations);
        Path clipPath = outputDir.resolve(registry + FILE_SUFFIX);
        writeJson(clipPath, document);
        String sha256 = sha256(Files.readAllBytes(clipPath));

        // The seam: the closing key against the first key, per bone and axis, degrees reduced to (-180, 180].
        double seamRotation = 0.0D;
        double seamPosition = 0.0D;
        if (ticks.size() > 1) {
            for (Map.Entry<String, List<double[]>> entry : rotationKeys.entrySet()) {
                double[] first = entry.getValue().get(0);
                double[] last = entry.getValue().get(entry.getValue().size() - 1);
                for (int axis = 0; axis < 3; axis++) {
                    seamRotation = Math.max(seamRotation, Math.abs(wrapDegrees(last[axis] - first[axis])));
                }
            }
            for (String name : positionBones) {
                List<double[]> values = positionKeys.get(name);
                double[] first = values.get(0);
                double[] last = values.get(values.size() - 1);
                for (int axis = 0; axis < 3; axis++) {
                    seamPosition = Math.max(seamPosition, Math.abs(last[axis] - first[axis]));
                }
            }
        }

        JsonObject entry = new JsonObject();
        entry.addProperty("registry", registry);
        entry.addProperty("model_id", id);
        entry.addProperty("manifest", manifestPath.getFileName().toString());
        entry.addProperty("model_class", spec.get("class").getAsString());
        entry.addProperty("hook", hook);
        entry.addProperty("geo", repositoryRoot.relativize(geoPath).toString().replace('\\', '/'));
        entry.addProperty("file", clipPath.getFileName().toString());
        entry.addProperty("sha256", sha256);
        entry.addProperty("rule", rule.kind());
        entry.addProperty("rule_note", rule.note());
        entry.addProperty("span_ticks", "one_key".equals(rule.kind()) ? 0.0D : round(rule.spanTicks()));
        entry.addProperty("animation_length_seconds", spanSeconds);
        entry.addProperty("keys_per_bone", ticks.size());
        entry.addProperty("bones", rotationKeys.size());
        entry.add("moving_bones", names(movingBones));
        entry.add("position_bones", names(positionBones));
        entry.add("hidden_bones_at_rest", names(new ArrayList<>(hiddenBones)));
        entry.addProperty("seam_delta_degrees", round(seamRotation));
        entry.addProperty("seam_delta_position_units", round(seamPosition));
        entry.add("subject_after", subject.after());
        entry.addProperty("sampled_inputs", INPUTS_STATEMENT);
        System.out.println(String.format(Locale.ROOT, "wrote %s: %s, %d keys per bone over %d bones (%d moving, %d positioned), span %s ticks, seam %s deg, sha256 %s",
                clipPath.getFileName(), rule.kind(), ticks.size(), rotationKeys.size(), movingBones.size(), positionBones.size(),
                fmt(rule.spanTicks()), fmt(seamRotation), sha256));
        return entry;
    }

    /**
     * The ruling's rest state, declared explicitly (the {@link ProbeSubject} defaults, spelled out so the index's
     * {@code subject_after} reports every flag the pose may write - the Rotator's fan angle included).
     */
    static JsonObject restState() {
        JsonObject state = new JsonObject();
        state.addProperty("name", "rest");
        state.addProperty("attacking", 0);
        state.addProperty("ri1", 0);
        state.addProperty("rock_type", 0);
        state.addProperty("rf1", 0.0F);
        state.addProperty("seed", 0L);
        return state;
    }

    /** The span rule: static = one key; declared channels = derived from their frequencies; otherwise the {@link #RULES} row. */
    static Rule ruleFor(JsonObject spec, String id, boolean isStatic) {
        if (isStatic) {
            return Rule.oneKey("a static rig (animation_kind static, no hook): one key at bind");
        }
        JsonArray channels = spec.has("channels") ? spec.getAsJsonArray("channels") : new JsonArray();
        if (channels.size() > 0) {
            List<Double> frequencies = new ArrayList<>();
            for (JsonElement element : channels) {
                JsonObject channel = element.getAsJsonObject();
                double omega = channel.get("frequency_radians_per_age_tick").getAsDouble();
                double wingspeed = channel.has("wingspeed") ? channel.get("wingspeed").getAsDouble() : 1.0D;
                double effective = omega * wingspeed;
                if (frequencies.stream().noneMatch(f -> Math.abs(f - effective) <= FREQUENCY_EPSILON)) {
                    frequencies.add(effective);
                }
            }
            double slowest = frequencies.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
            String declared = "the manifest's channels: " + frequencies.size() + " distinct frequency group(s) "
                    + frequencies.stream().map(ReferenceClipSampler::fmt).toList() + " rad/tick (omega x wingspeed)";
            if (frequencies.size() == 1) {
                return Rule.naturalPeriod(TWO_PI / slowest, declared + ": one natural period 2 pi / f = " + fmt(TWO_PI / slowest) + " ticks");
            }
            return Rule.slowestCapped(TWO_PI / slowest, declared + ": the slowest group's period 2 pi / " + fmt(slowest) + " = "
                    + fmt(TWO_PI / slowest) + " ticks");
        }
        Rule rule = RULES.get(id);
        if (rule == null) {
            throw new IllegalStateException(id + ": a hook rig without declared channels and without a ReferenceClipSampler.RULES "
                    + "row - state its sampling rule (one natural period, or two seconds) with the source line it is read from");
        }
        return rule;
    }

    private static JsonObject keys(List<Double> ticks, List<double[]> values) {
        JsonObject out = new JsonObject();
        for (int i = 0; i < ticks.size(); i++) {
            JsonObject key = new JsonObject();
            JsonArray post = new JsonArray();
            for (int axis = 0; axis < 3; axis++) {
                post.add(values.get(i)[axis]);
            }
            key.add("post", post);
            key.addProperty("lerp_mode", "linear");
            out.add(timeKey(ticks.get(i)), key);
        }
        return out;
    }

    /** The key's time in seconds as {@link KeyframeLeg} writes it: ten decimals, trailing zeros dropped, {@code 0.0} at zero. */
    static String timeKey(double tick) {
        if (tick == 0.0D) {
            return "0.0";
        }
        String text = String.format(Locale.ROOT, "%.10f", tick / TICKS_PER_SECOND);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text + "0" : text;
    }

    static double round(double value) {
        double rounded = Math.round(value * VALUE_ROUNDING) / VALUE_ROUNDING;
        return rounded == 0.0D ? 0.0D : rounded;  // never -0.0
    }

    static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0D;
        if (wrapped > 180.0D) {
            wrapped -= 360.0D;
        } else if (wrapped <= -180.0D) {
            wrapped += 360.0D;
        }
        return wrapped;
    }

    private static String fmt(double value) {
        return new java.math.BigDecimal(value).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static JsonArray names(List<String> names) {
        JsonArray out = new JsonArray();
        names.forEach(out::add);
        return out;
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void writeJson(Path path, JsonObject value) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(value) + "\n", StandardCharsets.UTF_8);
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

}
