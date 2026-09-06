package danger.orespawn.g1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.PhaseLockedKeyframeController;
import danger.orespawn.entity.client.animation.SplineRepair;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;

/**
 * The keyframe reference leg of the G1 animation proof (Phase G, the controller's return; Amendment 1
 * points 3-5, ADDENDA (1)-(2), scope addendum item 24 (5)-(14) and (15), 2026-09-06).
 *
 * <p>A {@code gait_scaled} model that declares {@code keyframe_reference_leg} in its manifest entry is
 * proven twice: the landed code-driven leg as before, and beside it the SHIPPED
 * {@link PhaseLockedKeyframeController} layers the species declares
 * ({@link OreSpawnGeoReplacement#keyframeLayers()}) over the species' regenerated clip file
 * (a scratch path until the owner's look moves it into the jar), through GeckoLib 4.8.4's own loader
 * ({@code FileLoader.loadAnimationsFile} 51-69: {@code KeyFramesAdapter.GEO_GSON} over the file's
 * {@code animations} member) and its own pipeline ({@code AnimationProcessor.tickAnimation} on a
 * persistent manager, as a drawn entity's manager persists), with the production
 * {@link SplineRepair} applied when the entry says so (Q9 (a)). The layers are built on explicit
 * inputs - the state's animation tick and limb-swing amount - exactly as the shipped replacement
 * builds them on its entity readers (the S4 doctrine).</p>
 *
 * <p>What it emits (all OUTPUT, none of it an input; {@code density} in particular - Amendment 1
 * point 4 and ADDENDA (2)):</p>
 * <ul>
 *   <li>per sample of the schedule - the amplitude x fraction grid, the dense probes and the wrap
 *       pairs this class adds to both sides ({@link #wrapRequests}: T - eps vs 0 + eps at every
 *       seam of every frequency group the float phase actually straddles, Amendment 1 point 5) -
 *       the layers' pose of every bone in classic terms, which the parity tool compares with the
 *       compiled {@code setupAnim} at the manifest's named tolerance;</li>
 *   <li>the clip's density (keys per bone per clip), lerp mode and spline-argument state, and the
 *       density SEARCH: the fewest keys per bone holding the tolerance under the evaluator in force,
 *       found by regenerating the clip in memory under the generator's rule
 *       ({@code tools/keyframe_clip.py}, mirrored here and pinned against the file) and confirmed
 *       on the full dense schedule with one key fewer failing;</li>
 *   <li>the wrap pairs' continuity and LUT indices, the length-derived time-warp facts, the
 *       order-independence of the persistent manager, and the density statement in the ruled
 *       wording (Q10).</li>
 * </ul>
 * The classic reference for the dense schedule is the manifest's channels evaluated in the compiled
 * float chain on the internal basis (the {@code G1AnimationRuntime} transcription, generalised); the
 * dumped samples are compared by the parity tool against the compiled model itself.
 */
final class KeyframeLeg {
    static final String KEY = "keyframe_reference_leg";
    static final String SAMPLE_FIELD = "keyframe_rotations";
    /** On every wrap sample of the geo dump: the pair's frequency group and the token its id carries ({@link Prepared#wrapProvenance}). */
    static final String WRAP_FIELD = "keyframe_wrap";
    static final String TOLERANCE_KEY = "keyframe_reference_leg_epsilon_radians";
    private static final String WRAP_TOKEN = "_kfwrap_";
    private static final int LUT_SIZE = PhaseLockedKeyframeController.CLASSIC_TRIG_INDEX_COUNT;
    /** The salvaged probe's straddle rule: the index before the seam near the top of the LUT, after it near the bottom. */
    private static final int WRAP_BEFORE_MIN_INDEX = 60000;
    private static final int WRAP_AFTER_MAX_INDEX = 5000;
    private static final double TWO_PI = 2.0D * Math.PI;
    /** The generator's rounding of a key's degrees (tools/keyframe_clip.py VALUE_DECIMALS). */
    private static final double VALUE_ROUNDING = 1.0e10D;
    /** The file-vs-generator pin: baked radians per key, and key lengths in ticks. */
    private static final double TWIN_EPSILON = 1.0e-9D;
    private static final float FREQUENCY_EPSILON = 1.0e-6F;
    private static final double[] QUADRATURES = {0.0D, 0.25D, 0.5D, 0.75D, 1.0D};
    private static final double[] SEAM_OFFSETS = {-0.0001D, 0.0D, 0.0001D};

    private KeyframeLeg() {
        throw new AssertionError();
    }

    // ------------------------------------------------------------------ the manifest's classic channels and groups

    /** One classic cosine channel from the manifest ({@code channels[]}), on one bone. */
    record Channel(String bone, float omega, float piScale, float sign, boolean scaled) {
        /** The compiled chain: {@code Mth.cos(age * omega) * (float) Math.PI * piScale [* amplitude]}, then the sign. */
        float classicRotX(float age, float amplitude) {
            float value = Mth.cos(age * this.omega) * (float) Math.PI * this.piScale;
            if (this.scaled) {
                value = value * amplitude;
            }
            return this.sign < 0.0F ? -value : value;
        }

        /** The internal basis: rotation X negated (OreSpawnGeoReplacement's basis facts). */
        float internalRotX(float age, float amplitude) {
            return -classicRotX(age, amplitude);
        }
    }

    /** One frequency group: the clip manifest's row joined with the classic channels at its frequency. */
    record Group(String name, String clip, float omega, int keysPerBone, List<Channel> channels, boolean gaitScaled) {
        List<String> bones() {
            List<String> names = new ArrayList<>();
            for (Channel channel : this.channels) {
                names.add(channel.bone());
            }
            return names;
        }

        double naturalPeriodTicks() {
            return TWO_PI / this.omega;
        }
    }

    static boolean declared(JsonObject spec) {
        return spec.has(KEY);
    }

    private static List<Channel> channels(JsonObject spec) {
        List<Channel> channels = new ArrayList<>();
        for (JsonElement element : spec.getAsJsonArray("channels")) {
            JsonObject channel = element.getAsJsonObject();
            if (!"cosine".equals(channel.get("formula").getAsString()) || !"x".equals(channel.get("axis").getAsString())) {
                throw new IllegalStateException("keyframe reference leg: only cosine X channels are transcribed; "
                        + spec.get("id").getAsString() + " declares " + channel);
            }
            float omega = (float) channel.get("frequency_radians_per_age_tick").getAsDouble();
            float piScale = (float) channel.get("pi_scale").getAsDouble();
            float sign = (float) channel.get("sign").getAsDouble();
            boolean scaled = channel.has("limb_swing_scaled") && channel.get("limb_swing_scaled").getAsBoolean();
            for (JsonElement bone : channel.getAsJsonArray("bones")) {
                channels.add(new Channel(bone.getAsString(), omega, piScale, sign, scaled));
            }
        }
        return channels;
    }

    private static List<Float> frequencies(List<Channel> channels) {
        List<Float> frequencies = new ArrayList<>();
        for (Channel channel : channels) {
            if (frequencies.stream().noneMatch(known -> Math.abs(known - channel.omega()) <= FREQUENCY_EPSILON)) {
                frequencies.add(channel.omega());
            }
        }
        frequencies.sort((a, b) -> Float.compare(b, a));
        return frequencies;
    }

    /** {@code w3_7} for 3.7 rad/tick: the float's own shortest decimal (the parity tool rebuilds it from the manifest's number). */
    static String frequencyToken(float omega) {
        return "w" + new BigDecimal(Float.toString(omega)).stripTrailingZeros().toPlainString().replace('.', '_');
    }

    // ------------------------------------------------------------------ the wrap pairs (both sides)

    /**
     * The wrap sample: for every frequency group, at every seam {@code cycle * 2 pi / omega} inside the
     * schedule's loop period, the two ages {@code seam - eps} and {@code seam + eps} with
     * {@code eps = wrap_epsilon_lut_indices * period / 65536}, kept only when the classic float phase
     * chain ({@link PhaseLockedKeyframeController#classicCosineIndex}) straddles the LUT wrap between
     * them - a seam the float phase does not cross is not a wrap sample. One pair per amplitude, so the
     * amplitude-matrix leg sees a complete group at each age.
     */
    static List<G1ModelProbe.SampleRequest> wrapRequests(JsonObject spec, List<Float> amplitudes) {
        JsonObject leg = spec.getAsJsonObject(KEY);
        int epsilonIndices = leg.get("wrap_epsilon_lut_indices").getAsInt();
        double loopPeriod = spec.get("loop_period_age_ticks").getAsDouble();
        List<G1ModelProbe.SampleRequest> requests = new ArrayList<>();
        for (float omega : frequencies(channels(spec))) {
            double period = TWO_PI / omega;
            double epsilon = epsilonIndices * period / LUT_SIZE;
            String token = frequencyToken(omega);
            for (long cycle = 1; cycle * period + epsilon <= loopPeriod; cycle++) {
                double seam = cycle * period;
                float before = (float) (seam - epsilon);
                float after = (float) (seam + epsilon);
                if (!straddles(before, after, omega)) {
                    continue;
                }
                for (float amplitude : amplitudes) {
                    String prefix = "a" + G1ModelProbe.amplitudeToken(amplitude) + WRAP_TOKEN + token + "_c" + cycle;
                    requests.add(new G1ModelProbe.SampleRequest(prefix + "_before", before, amplitude, false, false));
                    requests.add(new G1ModelProbe.SampleRequest(prefix + "_after", after, amplitude, false, false));
                }
            }
        }
        return requests;
    }

    private static boolean straddles(float before, float after, float omega) {
        int indexBefore = PhaseLockedKeyframeController.classicCosineIndex(before * omega * 1.0F);
        int indexAfter = PhaseLockedKeyframeController.classicCosineIndex(after * omega * 1.0F);
        return indexBefore > WRAP_BEFORE_MIN_INDEX && indexAfter < WRAP_AFTER_MAX_INDEX;
    }

    // ------------------------------------------------------------------ preparation

    /** Everything the geo side needs for one model, built once: the leg is then sampled per request and reported. */
    static Prepared prepare(JsonObject manifest, JsonObject spec, Path repositoryRoot,
                            G1AnimationRuntime.Evaluator evaluator) throws Exception {
        String modelId = spec.get("id").getAsString();
        JsonObject leg = spec.getAsJsonObject(KEY);
        JsonObject thresholds = manifest.getAsJsonObject("thresholds");
        if (!thresholds.has(TOLERANCE_KEY)) {
            throw new IllegalStateException(modelId + ": the keyframe reference leg's tolerance is an owner ruling; "
                    + "name it as thresholds." + TOLERANCE_KEY + " (no default)");
        }
        double tolerance = thresholds.get(TOLERANCE_KEY).getAsDouble();
        String candidateClass = leg.get("candidate_class").getAsString();
        boolean splineRepair = leg.get("spline_repair").getAsBoolean();
        JsonObject search = leg.getAsJsonObject("density_search");
        Path clipManifestPath = resolve(repositoryRoot, leg.get("clip_manifest").getAsString());
        Path clipPath = resolve(repositoryRoot, leg.get("clip_path").getAsString());

        // 1. The groups: the clip manifest's rows joined with the classic channels at each frequency.
        JsonObject clipManifest = readJson(clipManifestPath);
        List<Channel> channels = channels(spec);
        List<Group> groups = new ArrayList<>();
        Set<String> claimed = new LinkedHashSet<>();
        for (JsonElement element : clipManifest.getAsJsonArray("groups")) {
            JsonObject row = element.getAsJsonObject();
            float omega = (float) row.get("frequency_radians_per_age_tick").getAsDouble();
            List<Channel> members = new ArrayList<>();
            Set<Boolean> scaled = new LinkedHashSet<>();
            for (Channel channel : channels) {
                if (Math.abs(channel.omega() - omega) <= FREQUENCY_EPSILON) {
                    members.add(channel);
                    scaled.add(channel.scaled());
                    if (!claimed.add(channel.bone())) {
                        throw new IllegalStateException(modelId + ": bone " + channel.bone() + " in two frequency groups");
                    }
                }
            }
            if (members.isEmpty() || scaled.size() != 1) {
                throw new IllegalStateException(modelId + ": group " + row.get("name").getAsString()
                        + " has no classic channel at its frequency, or its channels disagree on limb_swing_scaled");
            }
            groups.add(new Group(row.get("name").getAsString(), row.get("clip").getAsString(), omega,
                    row.get("keys_per_bone").getAsInt(), members, scaled.iterator().next()));
        }
        if (claimed.size() != channels.size()) {
            throw new IllegalStateException(modelId + ": classic channels outside every frequency group: "
                    + channels.stream().map(Channel::bone).filter(bone -> !claimed.contains(bone)).toList());
        }
        double seconds = clipManifest.get("animation_length_seconds").getAsDouble();
        String lerpMode = clipManifest.get("lerp_mode").getAsString();
        String speciesLabel = clipManifest.get("species_label").getAsString();

        // 2. The production layers of the shipped replacement, and their agreement with the groups.
        OreSpawnGeoReplacement<?> replacement = S4CandidateRuntime.instantiate(candidateClass);
        List<KeyframeLayer> layers = replacement.keyframeLayers();
        if (layers.size() != groups.size()) {
            throw new IllegalStateException(modelId + ": " + candidateClass + " declares " + layers.size()
                    + " keyframe layers, the clip manifest " + groups.size() + " groups");
        }
        for (int index = 0; index < groups.size(); index++) {
            Group group = groups.get(index);
            KeyframeLayer layer = layers.get(index);
            if (!layer.group().equals(group.name()) || !layer.clip().equals(group.clip())
                    || layer.angularFrequencyRadiansPerTick() != group.omega()
                    || !layer.bones().equals(new LinkedHashSet<>(group.bones()))
                    || layer.gaitScaled() != group.gaitScaled()) {
                throw new IllegalStateException(modelId + ": production keyframe layer " + index + " (" + layer
                        + ") does not match the clip manifest's group " + group.name() + " " + group.clip() + " "
                        + group.omega() + " " + group.bones() + " gait=" + group.gaitScaled());
            }
        }

        // 3. The clip file through GeckoLib's own loader, repaired when the entry says so.
        byte[] clipBytes = Files.readAllBytes(clipPath);
        JsonObject clipRoot = JsonParser.parseString(new String(clipBytes, StandardCharsets.UTF_8)).getAsJsonObject();
        BakedAnimations loaded = bake(clipRoot);
        BakedAnimations clips = splineRepair ? SplineRepair.repair(loaded) : loaded;
        Map<String, Integer> fileKeys = new LinkedHashMap<>();
        Map<String, String> fileLerp = new LinkedHashMap<>();
        Map<String, String> fileArguments = new LinkedHashMap<>();
        Map<String, Double> fileLength = new LinkedHashMap<>();
        for (Group group : groups) {
            Animation animation = clips.getAnimation(group.clip());
            if (animation == null) {
                throw new IllegalStateException(modelId + ": clip " + group.clip() + " is not in " + clipPath);
            }
            if (animation.loopType() != Animation.LoopType.LOOP) {
                throw new IllegalStateException(modelId + ": clip " + group.clip() + " must loop");
            }
            boolean loop = true;
            Integer keys = null;
            for (String bone : group.bones()) {
                List<Keyframe<MathValue>> frames = boneAnimation(animation, bone, modelId).rotationKeyFrames().xKeyframes();
                if (keys == null) {
                    keys = frames.size();
                } else if (keys != frames.size()) {
                    throw new IllegalStateException(modelId + ": clip " + group.clip() + " keys per bone differ across "
                            + group.bones());
                }
                Keyframe<MathValue> last = frames.get(frames.size() - 1);
                fileLerp.merge(group.name(), lerpName(last.easingType()), (a, b) -> a.equals(b) ? a : a + "|" + b);
                fileArguments.merge(group.name(), argumentState(frames, loop), (a, b) -> a.equals(b) ? a : a + "|" + b);
            }
            fileKeys.put(group.name(), keys);
            fileLength.put(group.name(), animation.length());
        }

        // 4. The file is the generator's output at its own density (the twin pin), and the twin's bake matches it.
        BakedAnimations twin = maybeRepair(bake(generate(groups, fileKeys, lerpMode, seconds)), splineRepair);
        double twinDelta = 0.0D;
        for (Group group : groups) {
            Animation fromFile = clips.getAnimation(group.clip());
            Animation fromTwin = twin.getAnimation(group.clip());
            for (String bone : group.bones()) {
                List<Keyframe<MathValue>> a = boneAnimation(fromFile, bone, modelId).rotationKeyFrames().xKeyframes();
                List<Keyframe<MathValue>> b = boneAnimation(fromTwin, bone, modelId).rotationKeyFrames().xKeyframes();
                if (a.size() != b.size()) {
                    throw new IllegalStateException(modelId + ": " + group.clip() + "/" + bone + " has " + a.size()
                            + " keys, the generator's rule " + b.size());
                }
                for (int index = 0; index < a.size(); index++) {
                    twinDelta = Math.max(twinDelta, Math.abs(a.get(index).length() - b.get(index).length()));
                    twinDelta = Math.max(twinDelta, Math.abs(a.get(index).endValue().get() - b.get(index).endValue().get()));
                    twinDelta = Math.max(twinDelta, Math.abs(a.get(index).startValue().get() - b.get(index).startValue().get()));
                }
            }
            if (Math.abs(fromFile.length() - fromTwin.length()) > TWIN_EPSILON) {
                throw new IllegalStateException(modelId + ": " + group.clip() + " declared length " + fromFile.length()
                        + " ticks, the generator's rule " + fromTwin.length());
            }
        }
        if (twinDelta > TWIN_EPSILON) {
            throw new IllegalStateException(modelId + ": the clip file is not the generator's output at its density "
                    + "(max key delta " + twinDelta + " > " + TWIN_EPSILON + "); regenerate it with tools/keyframe_clip.py");
        }

        return new Prepared(modelId, speciesLabel, tolerance, candidateClass, splineRepair, clipPath, sha256(clipBytes),
                clipManifestPath, groups, layers, clips, lerpMode, seconds, fileKeys, fileLerp, fileArguments, fileLength,
                twinDelta, search, leg.get("wrap_epsilon_lut_indices").getAsInt(), evaluator);
    }

    private static Path resolve(Path repositoryRoot, String path) {
        Path candidate = Path.of(path);
        return candidate.isAbsolute() ? candidate.normalize() : repositoryRoot.resolve(candidate).normalize();
    }

    private static JsonObject readJson(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static String sha256(byte[] bytes) throws Exception {
        StringBuilder text = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            text.append(String.format(Locale.ROOT, "%02x", b));
        }
        return text.toString();
    }

    private static BoneAnimation boneAnimation(Animation animation, String bone, String modelId) {
        for (BoneAnimation candidate : animation.boneAnimations()) {
            if (candidate.boneName().equals(bone)) {
                return candidate;
            }
        }
        throw new IllegalStateException(modelId + ": clip " + animation.name() + " does not animate bone " + bone);
    }

    private static String lerpName(EasingType easing) {
        if (easing == EasingType.LINEAR) {
            return "linear";
        }
        if (easing == EasingType.CATMULLROM) {
            return "catmullrom";
        }
        return String.valueOf(easing);
    }

    /** Whether the loaded catmullrom arguments are the textbook neighbours (repaired) or GeckoLib's own (P0 == P1). */
    private static String argumentState(List<Keyframe<MathValue>> frames, boolean loop) {
        boolean repaired = true;
        boolean any = false;
        for (int index = 0; index < frames.size(); index++) {
            MathValue[] expected = SplineRepair.repairedArguments(frames, index, loop);
            if (expected == null) {
                continue;
            }
            any = true;
            List<MathValue> actual = frames.get(index).easingArgs();
            if (actual.size() != 2 || actual.get(0).get() != expected[0].get() || actual.get(1).get() != expected[1].get()) {
                repaired = false;
            }
        }
        if (!any) {
            return "not catmullrom";
        }
        return repaired ? "spline arguments repaired at load" : "spline arguments as GeckoLib 4.8.4 stores them (P0 == P1)";
    }

    // ------------------------------------------------------------------ GeckoLib's loader, and the generator's twin

    /** {@code FileLoader.loadAnimationsFile} 51-69: the file's {@code animations} member through {@code KeyFramesAdapter.GEO_GSON}. */
    static BakedAnimations bake(JsonObject root) {
        BakedAnimations baked = KeyFramesAdapter.GEO_GSON.fromJson(GsonHelper.getAsJsonObject(root, "animations"),
                BakedAnimations.class);
        if (baked == null) {
            throw new IllegalStateException("GeckoLib baked no animations from the clip document");
        }
        return baked;
    }

    private static BakedAnimations maybeRepair(BakedAnimations baked, boolean splineRepair) {
        return splineRepair ? SplineRepair.repair(baked) : baked;
    }

    /**
     * The generator's rule ({@code tools/keyframe_clip.py}), in memory: one looping clip per group of the
     * declared length, keys at {@code L k / (N - 1)} holding {@code sign * (float) Math.PI * piScale} degrees
     * times {@code cos(2 pi k / (N - 1))}, rounded to 1e-10 degrees, authored X = +classic degrees.
     */
    static JsonObject generate(List<Group> groups, Map<String, Integer> keysPerGroup, String lerpMode, double seconds) {
        JsonObject animations = new JsonObject();
        for (Group group : groups) {
            int keys = keysPerGroup.get(group.name());
            int segments = keys - 1;
            JsonObject bones = new JsonObject();
            for (Channel channel : group.channels()) {
                double amplitudeDegrees = channel.sign() * Math.toDegrees((float) ((float) Math.PI * channel.piScale()));
                JsonObject rotation = new JsonObject();
                for (int index = 0; index <= segments; index++) {
                    double value = Math.round(amplitudeDegrees * Math.cos(TWO_PI * index / segments) * VALUE_ROUNDING)
                            / VALUE_ROUNDING;
                    if (value == 0.0D) {
                        value = 0.0D;
                    }
                    JsonObject key = new JsonObject();
                    JsonArray post = new JsonArray();
                    post.add(value);
                    post.add(0);
                    post.add(0);
                    key.add("post", post);
                    key.addProperty("lerp_mode", lerpMode);
                    rotation.add(timeKey(index, segments, seconds), key);
                }
                JsonObject bone = new JsonObject();
                bone.add("rotation", rotation);
                bones.add(channel.bone(), bone);
            }
            JsonObject clip = new JsonObject();
            clip.addProperty("loop", true);
            clip.addProperty("animation_length", seconds);
            clip.add("bones", bones);
            animations.add(group.clip(), clip);
        }
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.8.0");
        root.add("animations", animations);
        return root;
    }

    private static String timeKey(int index, int segments, double seconds) {
        if (index == 0) {
            return "0.0";
        }
        String text = String.format(Locale.ROOT, "%.10f", seconds * index / segments);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text + "0" : text;
    }

    // ------------------------------------------------------------------ the prepared leg

    static final class Prepared {
        private final String modelId;
        private final String speciesLabel;
        private final double tolerance;
        private final String candidateClass;
        private final boolean splineRepair;
        private final Path clipPath;
        private final String clipSha256;
        private final Path clipManifestPath;
        private final List<Group> groups;
        private final List<KeyframeLayer> layers;
        private final BakedAnimations clips;
        private final String lerpMode;
        private final double seconds;
        private final Map<String, Integer> fileKeys;
        private final Map<String, String> fileLerp;
        private final Map<String, String> fileArguments;
        private final Map<String, Double> fileLength;
        private final double twinDelta;
        private final JsonObject search;
        private final int wrapEpsilonIndices;
        private final G1AnimationRuntime.Evaluator evaluator;
        private final Harness harness;
        private final Map<String, Map<String, float[]>> forward = new LinkedHashMap<>();

        private Prepared(String modelId, String speciesLabel, double tolerance, String candidateClass, boolean splineRepair,
                         Path clipPath, String clipSha256, Path clipManifestPath, List<Group> groups,
                         List<KeyframeLayer> layers, BakedAnimations clips, String lerpMode, double seconds,
                         Map<String, Integer> fileKeys, Map<String, String> fileLerp, Map<String, String> fileArguments,
                         Map<String, Double> fileLength, double twinDelta, JsonObject search, int wrapEpsilonIndices,
                         G1AnimationRuntime.Evaluator evaluator) {
            this.modelId = modelId;
            this.speciesLabel = speciesLabel;
            this.tolerance = tolerance;
            this.candidateClass = candidateClass;
            this.splineRepair = splineRepair;
            this.clipPath = clipPath;
            this.clipSha256 = clipSha256;
            this.clipManifestPath = clipManifestPath;
            this.groups = groups;
            this.layers = layers;
            this.clips = clips;
            this.lerpMode = lerpMode;
            this.seconds = seconds;
            this.fileKeys = fileKeys;
            this.fileLerp = fileLerp;
            this.fileArguments = fileArguments;
            this.fileLength = fileLength;
            this.twinDelta = twinDelta;
            this.search = search;
            this.wrapEpsilonIndices = wrapEpsilonIndices;
            this.evaluator = evaluator;
            this.harness = new Harness(evaluator.freshBake(), clips, layers);
        }

        /** The layers' pose at this request, every bone of the rig in classic terms ({@code (-x, y, -z)} of the internal basis). */
        JsonObject classicRotations(G1ModelProbe.SampleRequest request) {
            Map<String, float[]> internal = this.harness.sample(request.ageTicks(), request.limbSwingAmount());
            this.forward.put(request.id(), internal);
            return classicJson(internal);
        }

        private static JsonObject classicJson(Map<String, float[]> internal) {
            JsonObject rotations = new JsonObject();
            internal.forEach((name, rotation) -> {
                JsonArray array = new JsonArray();
                array.add(-rotation[0]);
                array.add(rotation[1]);
                array.add(-rotation[2]);
                rotations.add(name, array);
            });
            return rotations;
        }

        /**
         * The wrap sample's provenance, written beside its id for the parity tool to match on (item 15 refuter B,
         * D10): the frequency group by name and the token the id carries. The token is resolved by the same
         * {@link #frequencyToken} over the same channel float that {@link #wrapRequests} built the id from, so the
         * tool never recomputes a token from the manifest's number (Java's {@code Float.toString} and Python's
         * {@code .7g} diverge past seven significant digits). {@code null} for a request that is not a wrap sample;
         * a wrap id whose token matches no group is a harness fault and throws.
         */
        JsonObject wrapProvenance(G1ModelProbe.SampleRequest request) {
            String id = request.id();
            int at = id.indexOf(WRAP_TOKEN);
            if (at < 0) {
                return null;
            }
            String tail = id.substring(at + WRAP_TOKEN.length());
            String token = tail.substring(0, tail.lastIndexOf("_c"));
            for (Group group : this.groups) {
                if (frequencyToken(group.channels().get(0).omega()).equals(token)) {
                    JsonObject out = new JsonObject();
                    out.addProperty("group", group.name());
                    out.addProperty("frequency_token", token);
                    return out;
                }
            }
            throw new IllegalStateException(this.modelId + ": wrap sample " + id + " names no frequency group (token " + token + ")");
        }

        /** The leg's block: provenance, the file's density, the search, the confirmation, the wraps, the time-warp, the statement. */
        JsonObject report(List<G1ModelProbe.SampleRequest> requests) {
            JsonObject out = new JsonObject();
            out.addProperty("candidate_class", this.candidateClass);
            out.addProperty("controller_class", PhaseLockedKeyframeController.class.getName());
            out.addProperty("clip_path", this.clipPath.toString().replace('\\', '/'));
            out.addProperty("clip_sha256", this.clipSha256);
            out.addProperty("clip_manifest", this.clipManifestPath.toString().replace('\\', '/'));
            out.addProperty("clip_source", "keyframe_reference_leg.clip_path through KeyFramesAdapter.GEO_GSON (FileLoader"
                    + ".loadAnimationsFile 51-69); not the converter's reference-only artifact");
            out.addProperty("spline_repair", this.splineRepair);
            out.addProperty("spline_repair_class", SplineRepair.class.getName());
            out.addProperty("tolerance_radians", this.tolerance);
            out.addProperty("pose_source", "persistent BakedGeoModel + production KeyframeLayer.controller (PhaseLockedKeyframe"
                    + "Controller) x " + this.layers.size() + " through AnimationProcessor.tickAnimation on explicit inputs");
            out.addProperty("clip_matches_generator_rule", true);
            out.addProperty("clip_vs_generator_max_key_delta", this.twinDelta);
            out.addProperty("authoring_length_seconds", this.seconds);
            out.addProperty("lerp_mode_declared", this.lerpMode);

            JsonArray layerArray = new JsonArray();
            for (KeyframeLayer layer : this.layers) {
                JsonObject row = new JsonObject();
                row.addProperty("group", layer.group());
                row.addProperty("clip", layer.clip());
                row.addProperty("controller", layer.controllerName());
                row.addProperty("frequency_radians_per_age_tick", layer.angularFrequencyRadiansPerTick());
                row.add("bones", names(new TreeSet<>(layer.bones())));
                row.addProperty("gait_scaled", layer.gaitScaled());
                row.addProperty("additive", layer.gaitScaled());
                layerArray.add(row);
            }
            out.add("layers", layerArray);

            // The file's density, an output read from what GeckoLib baked.
            JsonObject clipBlock = new JsonObject();
            for (Group group : this.groups) {
                JsonObject row = new JsonObject();
                row.addProperty("clip", group.clip());
                row.addProperty("keys_per_bone", this.fileKeys.get(group.name()));
                row.addProperty("lerp_mode", this.fileLerp.get(group.name()));
                row.addProperty("spline_arguments", this.fileArguments.get(group.name()));
                row.addProperty("declared_length_ticks", this.fileLength.get(group.name()));
                row.add("bones", names(group.bones()));
                clipBlock.add(group.name(), row);
            }
            out.add("clip", clipBlock);

            // The time-warp (ADDENDA (1)): the declared length, the natural period, the ratio.
            JsonObject warp = new JsonObject();
            for (Group group : this.groups) {
                PhaseLockedKeyframeController<?> controller = this.harness.controller(group.name());
                JsonObject row = new JsonObject();
                row.addProperty("declared_clip_ticks", controller.declaredClipTicks());
                row.addProperty("natural_period_age_ticks", group.naturalPeriodTicks());
                row.addProperty("clip_ticks_per_age_tick", controller.clipTicksPerSourceTick());
                warp.add(group.name(), row);
            }
            out.add("time_warp", warp);

            // The density search and its confirmation.
            out.add("density", densitySearch());

            // The file clip on the full dense schedule at every amplitude.
            RunResult full = runDense(this.clips, this.search.get("confirm_uniform_intervals").getAsInt(), amplitudes());
            out.add("clip_full_schedule", full.toJson(this.tolerance));

            // The wrap sample on the file clip.
            out.add("wrap_sample", wrapSample());

            // Order independence of the persistent manager: the dumped requests reversed.
            double orderDelta = 0.0D;
            List<G1ModelProbe.SampleRequest> reversed = new ArrayList<>(requests);
            java.util.Collections.reverse(reversed);
            for (G1ModelProbe.SampleRequest request : reversed) {
                Map<String, float[]> before = this.forward.get(request.id());
                if (before == null) {
                    continue;
                }
                Map<String, float[]> again = this.harness.sample(request.ageTicks(), request.limbSwingAmount());
                for (Map.Entry<String, float[]> entry : again.entrySet()) {
                    float[] first = before.get(entry.getKey());
                    for (int axis = 0; axis < 3; axis++) {
                        orderDelta = Math.max(orderDelta, Math.abs(first[axis] - entry.getValue()[axis]));
                    }
                }
            }
            out.addProperty("schedule_reversed_max_delta_radians", orderDelta);
            out.addProperty("samples_with_keyframe_rotations", this.forward.size());

            out.addProperty("density_statement", statement());
            return out;
        }

        private List<Float> amplitudes() {
            return List.of(0.0F, 0.25F, 0.5F, 1.0F);
        }

        private String statement() {
            StringBuilder keys = new StringBuilder();
            StringBuilder lerps = new StringBuilder();
            for (Group group : this.groups) {
                if (keys.length() > 0) {
                    keys.append(" / ");
                }
                keys.append(this.fileKeys.get(group.name()));
                String lerp = this.fileLerp.get(group.name());
                if (lerps.indexOf(lerp) < 0) {
                    if (lerps.length() > 0) {
                        lerps.append("|");
                    }
                    lerps.append(lerp);
                }
            }
            String arguments = this.splineRepair
                    ? "with spline arguments repaired at load"
                    : "with spline arguments as GeckoLib 4.8.4 evaluates them";
            return toleranceText(this.tolerance) + " rad; " + this.speciesLabel + " reference leg " + keys + " " + lerps
                    + " keys per bone " + arguments + "; wrap sample T−ε vs 0+ε included";
        }

        private static String toleranceText(double tolerance) {
            String text = String.format(Locale.ROOT, "%.1e", tolerance);
            // 2.5e-03 -> 2.5e-3, as the ruling writes it
            return text.replaceAll("e([+-])0*(\\d)", "e$1$2");
        }

        // ------------------------------------------------------------------ the dense schedule

        private TreeSet<Double> denseAges(int uniformIntervals) {
            double start = this.search.get("late_start_age_ticks").getAsDouble();
            double span = this.search.get("span_age_ticks").getAsDouble();
            double end = start + span;
            TreeSet<Double> ages = new TreeSet<>();
            for (int index = 0; index <= uniformIntervals; index++) {
                ages.add(start + span * index / uniformIntervals);
            }
            // exact quadratures, clip endpoints and both sides of every cyclic seam for every frequency group
            for (Group group : this.groups) {
                double frequency = group.omega();
                long firstCycle = (long) Math.floor(start * frequency / TWO_PI) - 1L;
                long lastCycle = (long) Math.ceil(end * frequency / TWO_PI) + 1L;
                for (long cycle = firstCycle; cycle <= lastCycle; cycle++) {
                    for (double fraction : QUADRATURES) {
                        double age = (cycle + fraction) * TWO_PI / frequency;
                        for (double offset : SEAM_OFFSETS) {
                            if (age + offset >= start && age + offset <= end) {
                                ages.add(age + offset);
                            }
                        }
                    }
                }
            }
            return ages;
        }

        private RunResult runDense(BakedAnimations candidateClips, int uniformIntervals, List<Float> amplitudes) {
            RunResult result = new RunResult();
            Harness candidate = new Harness(this.evaluator.freshBake(), candidateClips, this.layers);
            TreeSet<Double> ages = denseAges(uniformIntervals);
            for (float amplitude : amplitudes) {
                for (double age : ages) {
                    Map<String, float[]> pose = candidate.sample((float) age, amplitude);
                    for (Group group : this.groups) {
                        for (Channel channel : group.channels()) {
                            float reference = channel.internalRotX((float) age, amplitude);
                            double error = Math.abs(pose.get(channel.bone())[0] - reference);
                            result.record(group.name(), error, age, amplitude, channel.bone());
                        }
                    }
                }
            }
            return result;
        }

        private JsonObject densitySearch() {
            int minKeys = this.search.get("min_keys").getAsInt();
            int maxKeys = this.search.get("max_keys").getAsInt();
            int searchIntervals = this.search.get("search_uniform_intervals").getAsInt();
            int confirmIntervals = this.search.get("confirm_uniform_intervals").getAsInt();
            JsonObject out = new JsonObject();
            out.addProperty("rule", "the fewest keys per bone (both ends included, uniform in time, the cosine sampled "
                    + "exactly at the keys, full amplitude) whose max |candidate - classic| over the search schedule at "
                    + "amplitude 1 holds the tolerance; confirmed on the full schedule at every amplitude with one key "
                    + "fewer failing");
            out.addProperty("lerp_mode", this.lerpMode);
            out.addProperty("spline_repair", this.splineRepair);
            out.addProperty("min_keys", minKeys);
            out.addProperty("max_keys", maxKeys);
            out.addProperty("search_uniform_intervals", searchIntervals);
            out.addProperty("confirm_uniform_intervals", confirmIntervals);
            out.addProperty("late_start_age_ticks", this.search.get("late_start_age_ticks").getAsDouble());
            out.addProperty("span_age_ticks", this.search.get("span_age_ticks").getAsDouble());
            JsonObject table = new JsonObject();
            Map<String, Integer> fewest = new LinkedHashMap<>();
            for (int keys = minKeys; keys <= maxKeys; keys++) {
                Map<String, Integer> uniform = new LinkedHashMap<>();
                for (Group group : this.groups) {
                    uniform.put(group.name(), keys);
                }
                RunResult run = runDense(maybeRepair(bake(generate(this.groups, uniform, this.lerpMode, this.seconds)),
                        this.splineRepair), searchIntervals, List.of(1.0F));
                JsonObject row = new JsonObject();
                for (Group group : this.groups) {
                    double error = run.maxError.getOrDefault(group.name(), Double.NaN);
                    row.addProperty(group.name(), error);
                    if (!fewest.containsKey(group.name()) && error <= this.tolerance) {
                        fewest.put(group.name(), keys);
                    }
                }
                table.add(Integer.toString(keys), row);
                if (fewest.size() == this.groups.size()) {
                    break;
                }
            }
            out.add("search_max_error_radians_by_keys_per_bone", table);
            JsonObject groupsBlock = new JsonObject();
            boolean allFound = fewest.size() == this.groups.size();
            RunResult atFewest = null;
            RunResult oneFewer = null;
            if (!fewest.isEmpty()) {
                // Confirm every group that reached the tolerance (groups are independent: disjoint bones); a group
                // that never reached it rides along at max_keys so the clip still bakes, its rows saying NOT_REACHED.
                Map<String, Integer> confirm = new LinkedHashMap<>();
                Map<String, Integer> below = new LinkedHashMap<>();
                for (Group group : this.groups) {
                    Integer keys = fewest.get(group.name());
                    confirm.put(group.name(), keys == null ? maxKeys : keys);
                    below.put(group.name(), keys == null ? maxKeys : keys - 1);
                }
                atFewest = runDense(maybeRepair(bake(generate(this.groups, confirm, this.lerpMode, this.seconds)),
                        this.splineRepair), confirmIntervals, amplitudes());
                oneFewer = runDense(maybeRepair(bake(generate(this.groups, below, this.lerpMode, this.seconds)),
                        this.splineRepair), confirmIntervals, amplitudes());
            }
            boolean densityMatches = allFound;
            for (Group group : this.groups) {
                JsonObject row = new JsonObject();
                Integer keys = fewest.get(group.name());
                if (keys == null) {
                    row.addProperty("fewest_keys_per_bone", "NOT_REACHED_WITHIN_" + maxKeys + "_KEYS_PER_BONE");
                    row.addProperty("clip_keys_per_bone", this.fileKeys.get(group.name()));
                    row.addProperty("clip_density_is_fewest", false);
                } else {
                    row.addProperty("fewest_keys_per_bone", keys);
                    row.addProperty("confirmed_max_error_radians", atFewest.maxError.get(group.name()));
                    row.addProperty("confirmed_comparisons", atFewest.comparisons.get(group.name()));
                    row.addProperty("confirmed_holds_tolerance", atFewest.maxError.get(group.name()) <= this.tolerance);
                    row.addProperty("one_fewer_keys_per_bone", keys - 1);
                    row.addProperty("one_fewer_max_error_radians", oneFewer.maxError.get(group.name()));
                    row.addProperty("one_fewer_fails_tolerance", oneFewer.maxError.get(group.name()) > this.tolerance);
                    row.addProperty("clip_keys_per_bone", this.fileKeys.get(group.name()));
                    boolean matches = keys.intValue() == this.fileKeys.get(group.name());
                    row.addProperty("clip_density_is_fewest", matches);
                    densityMatches &= matches;
                }
                groupsBlock.add(group.name(), row);
            }
            out.add("groups", groupsBlock);
            out.addProperty("every_group_reached_tolerance", allFound);
            out.addProperty("clip_density_is_fewest_for_every_group", densityMatches);
            return out;
        }

        // ------------------------------------------------------------------ the wrap sample on the file clip

        private JsonObject wrapSample() {
            JsonObject out = new JsonObject();
            double start = this.search.get("late_start_age_ticks").getAsDouble();
            double end = start + this.search.get("span_age_ticks").getAsDouble();
            for (Group group : this.groups) {
                double period = group.naturalPeriodTicks();
                double epsilon = this.wrapEpsilonIndices * period / LUT_SIZE;
                Channel probe = group.channels().get(0);
                PhaseLockedKeyframeController<?> controller = this.harness.controller(group.name());
                int seams = 0;
                int minIndexBefore = Integer.MAX_VALUE;
                int maxIndexAfter = -1;
                double maxContinuity = 0.0D;
                double maxErrorBefore = 0.0D;
                double maxErrorAt = 0.0D;
                double maxErrorAfter = 0.0D;
                for (long cycle = (long) Math.ceil(start / period); cycle <= (long) Math.floor(end / period); cycle++) {
                    double seam = cycle * period;
                    float beforeAge = (float) (seam - epsilon);
                    float atAge = (float) seam;
                    float afterAge = (float) (seam + epsilon);
                    float before = this.harness.sample(beforeAge, 1.0F).get(probe.bone())[0];
                    int indexBefore = controller.lastCosineIndex();
                    float at = this.harness.sample(atAge, 1.0F).get(probe.bone())[0];
                    float after = this.harness.sample(afterAge, 1.0F).get(probe.bone())[0];
                    int indexAfter = controller.lastCosineIndex();
                    if (!(indexBefore > WRAP_BEFORE_MIN_INDEX && indexAfter < WRAP_AFTER_MAX_INDEX)) {
                        continue;
                    }
                    seams++;
                    minIndexBefore = Math.min(minIndexBefore, indexBefore);
                    maxIndexAfter = Math.max(maxIndexAfter, indexAfter);
                    maxContinuity = Math.max(maxContinuity, Math.abs(before - after));
                    maxErrorBefore = Math.max(maxErrorBefore, Math.abs(before - probe.internalRotX(beforeAge, 1.0F)));
                    maxErrorAt = Math.max(maxErrorAt, Math.abs(at - probe.internalRotX(atAge, 1.0F)));
                    maxErrorAfter = Math.max(maxErrorAfter, Math.abs(after - probe.internalRotX(afterAge, 1.0F)));
                }
                JsonObject row = new JsonObject();
                row.addProperty("probe_bone", probe.bone());
                row.addProperty("natural_period_age_ticks", period);
                row.addProperty("epsilon_age_ticks", epsilon);
                row.addProperty("epsilon_lut_indices", this.wrapEpsilonIndices);
                row.addProperty("seams_sampled", seams);
                row.addProperty("min_lut_index_before_seam", seams == 0 ? -1 : minIndexBefore);
                row.addProperty("max_lut_index_after_seam", maxIndexAfter);
                row.addProperty("max_abs_T_minus_eps_vs_0_plus_eps_radians", maxContinuity);
                row.addProperty("max_error_vs_classic_before_radians", maxErrorBefore);
                row.addProperty("max_error_vs_classic_at_seam_radians", maxErrorAt);
                row.addProperty("max_error_vs_classic_after_radians", maxErrorAfter);
                row.addProperty("holds_tolerance", seams > 0
                        && Math.max(Math.max(maxErrorBefore, maxErrorAfter), Math.max(maxErrorAt, maxContinuity)) <= this.tolerance);
                out.add(group.name(), row);
            }
            return out;
        }
    }

    private static JsonArray names(Iterable<String> names) {
        JsonArray array = new JsonArray();
        names.forEach(array::add);
        return array;
    }

    // ------------------------------------------------------------------ a dense run's result

    private static final class RunResult {
        final Map<String, Double> maxError = new TreeMap<>();
        final Map<String, Long> comparisons = new TreeMap<>();
        final Map<String, Double> worstAge = new TreeMap<>();
        final Map<String, Double> worstAmplitude = new TreeMap<>();
        final Map<String, String> worstBone = new TreeMap<>();

        void record(String group, double error, double age, float amplitude, String bone) {
            this.comparisons.merge(group, 1L, Long::sum);
            if (error > this.maxError.getOrDefault(group, -1.0D)) {
                this.maxError.put(group, error);
                this.worstAge.put(group, age);
                this.worstAmplitude.put(group, (double) amplitude);
                this.worstBone.put(group, bone);
            }
        }

        JsonObject toJson(double tolerance) {
            JsonObject out = new JsonObject();
            for (String group : this.maxError.keySet()) {
                JsonObject row = new JsonObject();
                row.addProperty("max_error_radians", this.maxError.get(group));
                row.addProperty("holds_tolerance", this.maxError.get(group) <= tolerance);
                row.addProperty("comparisons", this.comparisons.get(group));
                row.addProperty("worst_age_ticks", this.worstAge.get(group));
                row.addProperty("worst_amplitude", this.worstAmplitude.get(group));
                row.addProperty("worst_bone", this.worstBone.get(group));
                out.add(group, row);
            }
            return out;
        }
    }

    // ------------------------------------------------------------------ the GeckoLib pipeline on explicit inputs

    private static final class ProbeAnimatable implements GeoAnimatable {
        private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
        private final List<AnimationController<ProbeAnimatable>> controllers = new ArrayList<>();

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
            for (AnimationController<ProbeAnimatable> controller : this.controllers) {
                registrar.add(controller);
            }
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

    /** A model serving the leg's clips (repaired or not) - the probe's stand-in for the shipped model's getAnimation. */
    private static final class ClipModel extends GeoModel<ProbeAnimatable> {
        private static final ResourceLocation PROBE = ResourceLocation.fromNamespaceAndPath("orespawn", "g1/keyframe_reference_leg");
        private final BakedAnimations clips;

        ClipModel(BakedAnimations clips) {
            this.clips = clips;
        }

        @Override
        public Animation getAnimation(ProbeAnimatable animatable, String name) {
            Animation animation = this.clips.getAnimation(name);
            if (animation == null) {
                throw new IllegalStateException("keyframe reference leg: clip missing: " + name);
            }
            return animation;
        }

        @Override
        public ResourceLocation getModelResource(ProbeAnimatable animatable) {
            return PROBE;
        }

        @Override
        public ResourceLocation getTextureResource(ProbeAnimatable animatable) {
            return PROBE;
        }

        @Override
        public ResourceLocation getAnimationResource(ProbeAnimatable animatable) {
            return PROBE;
        }
    }

    /** The production layers on one persistent bake and manager, ticked through GeckoLib's processor. */
    private static final class Harness {
        private final ProbeAnimatable animatable = new ProbeAnimatable();
        private final ClipModel model;
        private final Map<String, GeoBone> bones;
        private final Map<String, PhaseLockedKeyframeController<ProbeAnimatable>> byGroup = new LinkedHashMap<>();
        private final AnimatableManager<ProbeAnimatable> manager;

        Harness(BakedGeoModel baked, BakedAnimations clips, List<KeyframeLayer> layers) {
            this.model = new ClipModel(clips);
            this.model.getAnimationProcessor().setActiveModel(baked);
            this.bones = collect(baked);
            for (KeyframeLayer layer : layers) {
                PhaseLockedKeyframeController<ProbeAnimatable> controller = layer.controller(this.animatable,
                        state -> (float) state.getAnimationTick(), AnimationState::getLimbSwingAmount);
                this.animatable.controllers.add(controller);
                this.byGroup.put(layer.group(), controller);
            }
            this.manager = new AnimatableManager<>(this.animatable);
        }

        PhaseLockedKeyframeController<ProbeAnimatable> controller(String group) {
            return this.byGroup.get(group);
        }

        /** One frame at {@code (age, amplitude)}: every bone's internal rotation after the layers ran. */
        Map<String, float[]> sample(float age, float amplitude) {
            AnimationState<ProbeAnimatable> state = new AnimationState<>(this.animatable, 0.0F, amplitude, 0.0F,
                    amplitude != 0.0F);
            state.animationTick = age;
            this.model.getAnimationProcessor().tickAnimation(this.animatable, this.model, this.manager, age, state, true);
            Map<String, float[]> pose = new TreeMap<>();
            this.bones.forEach((name, bone) -> pose.put(name, new float[] {bone.getRotX(), bone.getRotY(), bone.getRotZ()}));
            return pose;
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
}
