package danger.orespawn.g1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Repeatable G1 renderer-component smoke proxy.
 *
 * <p>This deliberately benchmarks the concrete classic EntityModel vertex path
 * and GeckoLib GeoRenderer vertex path without claiming whole-client, GPU,
 * server, or MHLib measurements. It is never Q6 acceptance evidence. The
 * checked-in protocol carries the binding future live pre-cutover gate and
 * names every live-only metric explicitly.</p>
 */
public final class G1PerformanceBenchmark {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final int COLOR = -1;

    private G1PerformanceBenchmark() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 7) {
            throw new IllegalArgumentException(
                    "Usage: G1PerformanceBenchmark <manifest> <generated-dir> <compiled-dir> "
                            + "<protocol> <profile> <repository-root> <output-json>");
        }
        Path manifestPath = Path.of(args[0]).toAbsolutePath().normalize();
        Path generatedDir = Path.of(args[1]).toAbsolutePath().normalize();
        Path compiledDir = Path.of(args[2]).toAbsolutePath().normalize();
        Path protocolPath = Path.of(args[3]).toAbsolutePath().normalize();
        String profileName = args[4];
        Path repositoryRoot = Path.of(args[5]).toAbsolutePath().normalize();
        Path outputPath = Path.of(args[6]).toAbsolutePath().normalize();

        JsonObject manifest = readJson(manifestPath);
        JsonObject protocol = readJson(protocolPath);
        if (!"smoke".equals(profileName)) {
            throw new IllegalArgumentException(
                    "G1 component benchmark only permits the non-acceptance smoke profile");
        }
        JsonObject profile = requireObject(protocol.getAsJsonObject("profiles"), profileName);
        Map<String, BenchmarkTarget> targets = loadTargets(manifest, generatedDir, compiledDir);

        int warmupSeconds = profile.get("warmup_seconds").getAsInt();
        int runSeconds = profile.get("run_seconds").getAsInt();
        int runCount = profile.get("runs").getAsInt();
        int allocationProbeFrames = profile.get("allocation_probe_frames").getAsInt();
        if (warmupSeconds <= 0 || runSeconds <= 0 || runCount <= 0 || allocationProbeFrames <= 0) {
            throw new IllegalArgumentException("benchmark profile durations/counts must be positive");
        }

        NoopVertexConsumer consumer = new NoopVertexConsumer();
        JsonArray sceneReports = new JsonArray();
        for (JsonElement element : protocol.getAsJsonArray("scenes")) {
            JsonObject scene = element.getAsJsonObject();
            configureSceneTargets(scene, targets);
            JsonObject sceneReport = benchmarkScene(
                    scene, targets, consumer, warmupSeconds, runSeconds,
                    runCount, allocationProbeFrames);
            sceneReports.add(sceneReport);
        }

        JsonObject report = new JsonObject();
        report.addProperty("schema_version", 1);
        report.addProperty("status", "SMOKE_ONLY");
        report.addProperty("qualification", "COMPONENT_PROXY_ONLY");
        report.addProperty("q6_status", "PENDING_LIVE_PRECUTOVER");
        report.addProperty("benchmark_kind", protocol.get("benchmark_kind").getAsString());
        report.add("fixed_seed", protocol.get("fixed_seed").deepCopy());
        report.addProperty("profile", profileName);
        report.addProperty("captured_at_utc", Instant.now().toString());
        report.addProperty("manifest_sha256", sha256(Files.readAllBytes(manifestPath)));
        report.addProperty("protocol_sha256", sha256(Files.readAllBytes(protocolPath)));
        report.add("provenance", provenance(
                repositoryRoot, manifest, generatedDir, compiledDir, manifestPath, protocolPath));
        report.add("profile_settings", profile.deepCopy());
        report.add("fixed_settings", protocol.getAsJsonObject("settings").deepCopy());
        report.add("environment", environment());
        report.add("scenes", sceneReports);
        report.add("deferred_live_scenes", protocol.getAsJsonArray("deferred_live_scenes").deepCopy());
        report.add("live_only_metrics", protocol.getAsJsonArray("live_only_metrics").deepCopy());
        report.add("provisional_budget", protocol.getAsJsonObject("provisional_budget").deepCopy());
        report.add("binding_live_acceptance_protocol",
                protocol.getAsJsonObject("live_acceptance_protocol").deepCopy());
        report.add("budget_evaluation", evaluateProxyBudget(protocol, sceneReports));
        report.addProperty("mhlib_part_count", 0);
        report.addProperty("mhlib_packets_per_second", "not measured: no runtime entity/server in G1 proxy");
        report.addProperty("server_mspt_p95", "not measured: no runtime server in G1 proxy");
        report.addProperty("gpu_frame_time_ms", "not measured: no GPU submission in G1 proxy");
        report.addProperty("vertex_checksum", Long.toUnsignedString(consumer.checksum));

        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, GSON.toJson(report) + "\n", StandardCharsets.UTF_8);
        System.out.printf(Locale.ROOT,
                "G1 BENCHMARK SMOKE_ONLY: COMPONENT_PROXY_ONLY / PENDING_LIVE_PRECUTOVER; "
                        + "%d scenes measured; report %s%n",
                sceneReports.size(), outputPath);
    }

    private static Map<String, BenchmarkTarget> loadTargets(JsonObject manifest,
                                                             Path generatedDir,
                                                             Path compiledDir) throws Exception {
        Map<String, BenchmarkTarget> targets = new TreeMap<>();
        for (JsonElement element : manifest.getAsJsonArray("models")) {
            JsonObject spec = element.getAsJsonObject();
            BenchmarkTarget target = BenchmarkTarget.load(spec, generatedDir, compiledDir);
            if (targets.put(target.id, target) != null) {
                throw new IllegalStateException("duplicate benchmark model " + target.id);
            }
        }
        return targets;
    }

    private static void configureSceneTargets(JsonObject scene,
                                              Map<String, BenchmarkTarget> targets) throws Exception {
        for (JsonElement element : scene.getAsJsonArray("models")) {
            JsonObject model = element.getAsJsonObject();
            String id = model.get("id").getAsString();
            BenchmarkTarget target = targets.get(id);
            if (target == null) {
                throw new IllegalStateException("scene references missing model " + id);
            }
            target.configurePose(model.get("sample").getAsString());
        }
    }

    private static JsonObject benchmarkScene(JsonObject scene,
                                             Map<String, BenchmarkTarget> targets,
                                             NoopVertexConsumer consumer,
                                             int warmupSeconds, int runSeconds,
                                             int runCount, int allocationProbeFrames) {
        String sceneId = scene.get("id").getAsString();
        boolean visible = scene.get("visible").getAsBoolean();
        int batchFrames = scene.get("timing_batch_frames").getAsInt();
        List<SceneTarget> sceneTargets = sceneTargets(scene, targets);
        if (sceneTargets.stream().mapToInt(SceneTarget::count).sum() != 100) {
            throw new IllegalStateException(sceneId + " must contain exactly 100 model instances");
        }

        System.out.printf(Locale.ROOT,
                "G1 BENCHMARK %s: warmup %ds, %d x %ds, batch %d%n",
                sceneId, warmupSeconds, runCount, runSeconds, batchFrames);
        warmScene(sceneTargets, visible, consumer, warmupSeconds, batchFrames);

        JsonArray runs = new JsonArray();
        List<Double> classicMedians = new ArrayList<>();
        List<Double> candidateMedians = new ArrayList<>();
        List<Double> classicP95s = new ArrayList<>();
        List<Double> candidateP95s = new ArrayList<>();
        List<Double> classicP99s = new ArrayList<>();
        List<Double> candidateP99s = new ArrayList<>();

        for (int run = 1; run <= runCount; run++) {
            GcSnapshot gcBefore = GcSnapshot.capture();
            java.lang.management.ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long cpuBefore = threadBean.isCurrentThreadCpuTimeSupported()
                    ? threadBean.getCurrentThreadCpuTime() : -1L;
            long wallBefore = System.nanoTime();
            long deadline = wallBefore + runSeconds * 1_000_000_000L;
            LongSamples classic = new LongSamples();
            LongSamples candidate = new LongSamples();
            boolean reverse = (run & 1) == 0;
            while (System.nanoTime() < deadline) {
                if (reverse) {
                    candidate.add(timeBatch(sceneTargets, Mode.CANDIDATE, visible, consumer, batchFrames));
                    classic.add(timeBatch(sceneTargets, Mode.CLASSIC, visible, consumer, batchFrames));
                } else {
                    classic.add(timeBatch(sceneTargets, Mode.CLASSIC, visible, consumer, batchFrames));
                    candidate.add(timeBatch(sceneTargets, Mode.CANDIDATE, visible, consumer, batchFrames));
                }
                reverse = !reverse;
            }
            long wallAfter = System.nanoTime();
            long cpuAfter = cpuBefore >= 0 ? threadBean.getCurrentThreadCpuTime() : -1L;
            GcSnapshot gcAfter = GcSnapshot.capture();

            JsonObject classicStats = statistics(classic);
            JsonObject candidateStats = statistics(candidate);
            classicMedians.add(classicStats.get("median_frame_ms").getAsDouble());
            candidateMedians.add(candidateStats.get("median_frame_ms").getAsDouble());
            classicP95s.add(classicStats.get("p95_frame_ms").getAsDouble());
            candidateP95s.add(candidateStats.get("p95_frame_ms").getAsDouble());
            classicP99s.add(classicStats.get("p99_frame_ms").getAsDouble());
            candidateP99s.add(candidateStats.get("p99_frame_ms").getAsDouble());

            JsonObject runReport = new JsonObject();
            runReport.addProperty("run", run);
            runReport.addProperty("starting_order",
                    (run & 1) == 0 ? "candidate/classic" : "classic/candidate");
            runReport.addProperty("alternation_unit", "timing batch");
            runReport.add("classic", classicStats);
            runReport.add("candidate", candidateStats);
            runReport.addProperty("paired_wall_seconds", (wallAfter - wallBefore) / 1_000_000_000.0);
            runReport.addProperty("client_cpu_percent_single_thread",
                    cpuBefore >= 0 ? 100.0 * (cpuAfter - cpuBefore) / (wallAfter - wallBefore) : -1.0);
            runReport.addProperty("gc_collections", gcAfter.collections - gcBefore.collections);
            runReport.addProperty("gc_time_ms", gcAfter.timeMillis - gcBefore.timeMillis);
            runs.add(runReport);
            System.out.printf(Locale.ROOT,
                    "G1 BENCHMARK %s run %d/%d: classic %.6fms, candidate %.6fms%n",
                    sceneId, run, runCount,
                    classicStats.get("median_frame_ms").getAsDouble(),
                    candidateStats.get("median_frame_ms").getAsDouble());
        }

        JsonObject aggregateClassic = aggregate(classicMedians, classicP95s, classicP99s);
        JsonObject aggregateCandidate = aggregate(candidateMedians, candidateP95s, candidateP99s);
        JsonObject allocations = allocationProbe(
                sceneTargets, visible, consumer, allocationProbeFrames);
        long classicVertices = countVertices(sceneTargets, Mode.CLASSIC, visible, consumer);
        long candidateVertices = countVertices(sceneTargets, Mode.CANDIDATE, visible, consumer);
        if (visible && classicVertices != candidateVertices) {
            throw new IllegalStateException(sceneId + " vertex count differs classic="
                    + classicVertices + " candidate=" + candidateVertices);
        }

        JsonObject report = new JsonObject();
        report.addProperty("id", sceneId);
        report.addProperty("status", "COMPONENT_PROXY_MEASURED");
        report.addProperty("visible", visible);
        report.addProperty("proxy_scope", scene.has("proxy_scope")
                ? scene.get("proxy_scope").getAsString()
                : "renderer vertex submission only; no window, GPU, client tick, or server");
        report.addProperty("timing_batch_frames", batchFrames);
        report.addProperty("entity_count", 100);
        report.addProperty("model_bone_count", sceneTargets.stream()
                .mapToInt(target -> target.target.boneCount * target.count).sum());
        report.addProperty("mhlib_part_count", 0);
        report.addProperty("classic_vertices_per_frame", classicVertices);
        report.addProperty("candidate_vertices_per_frame", candidateVertices);
        report.add("runs", runs);
        report.add("classic", aggregateClassic);
        report.add("candidate", aggregateCandidate);
        report.add("allocation", allocations);
        double classicMedian = aggregateClassic.get("median_frame_ms").getAsDouble();
        double candidateMedian = aggregateCandidate.get("median_frame_ms").getAsDouble();
        double classicP95 = aggregateClassic.get("p95_frame_ms").getAsDouble();
        double candidateP95 = aggregateCandidate.get("p95_frame_ms").getAsDouble();
        report.addProperty("median_regression_percent",
                classicMedian == 0.0 ? 0.0 : 100.0 * (candidateMedian / classicMedian - 1.0));
        report.addProperty("p95_regression_ms", candidateP95 - classicP95);
        report.addProperty("budget_scene", scene.get("budget_scene").getAsBoolean());
        return report;
    }

    private static List<SceneTarget> sceneTargets(JsonObject scene,
                                                   Map<String, BenchmarkTarget> targets) {
        List<SceneTarget> result = new ArrayList<>();
        for (JsonElement element : scene.getAsJsonArray("models")) {
            JsonObject model = element.getAsJsonObject();
            result.add(new SceneTarget(
                    targets.get(model.get("id").getAsString()),
                    model.get("count").getAsInt()));
        }
        return result;
    }

    private static void warmScene(List<SceneTarget> targets, boolean visible,
                                  NoopVertexConsumer consumer, int seconds, int batchFrames) {
        long deadline = System.nanoTime() + seconds * 1_000_000_000L;
        boolean reverse = false;
        while (System.nanoTime() < deadline) {
            if (reverse) {
                renderBatch(targets, Mode.CANDIDATE, visible, consumer, batchFrames);
                renderBatch(targets, Mode.CLASSIC, visible, consumer, batchFrames);
            } else {
                renderBatch(targets, Mode.CLASSIC, visible, consumer, batchFrames);
                renderBatch(targets, Mode.CANDIDATE, visible, consumer, batchFrames);
            }
            reverse = !reverse;
        }
    }

    private static long timeBatch(List<SceneTarget> targets, Mode mode, boolean visible,
                                  NoopVertexConsumer consumer, int batchFrames) {
        long start = System.nanoTime();
        renderBatch(targets, mode, visible, consumer, batchFrames);
        return Math.max(0L, (System.nanoTime() - start) / batchFrames);
    }

    private static void renderBatch(List<SceneTarget> targets, Mode mode, boolean visible,
                                    NoopVertexConsumer consumer, int batchFrames) {
        for (int frame = 0; frame < batchFrames; frame++) {
            PoseStack poseStack = new PoseStack();
            for (SceneTarget sceneTarget : targets) {
                for (int copy = 0; copy < sceneTarget.count; copy++) {
                    if (visible) {
                        poseStack.pushPose();
                        if (mode == Mode.CLASSIC) {
                            sceneTarget.target.classic.renderToBuffer(
                                    poseStack, consumer, 0, 0, COLOR);
                        } else {
                            sceneTarget.target.geoRenderer.actuallyRender(
                                    poseStack, null, sceneTarget.target.geo, null, null,
                                    consumer, true, 0.0F, 0, 0, COLOR);
                        }
                        poseStack.popPose();
                    } else if (mode == Mode.CLASSIC) {
                        sceneTarget.target.touchClassic(consumer);
                    } else {
                        sceneTarget.target.touchGeo(consumer);
                    }
                }
            }
        }
    }

    private static long countVertices(List<SceneTarget> targets, Mode mode, boolean visible,
                                      NoopVertexConsumer consumer) {
        long before = consumer.vertices;
        renderBatch(targets, mode, visible, consumer, 1);
        return consumer.vertices - before;
    }

    private static JsonObject allocationProbe(List<SceneTarget> targets, boolean visible,
                                              NoopVertexConsumer consumer, int frames) {
        JsonObject out = new JsonObject();
        java.lang.management.ThreadMXBean base = ManagementFactory.getThreadMXBean();
        if (!(base instanceof com.sun.management.ThreadMXBean bean)
                || !bean.isThreadAllocatedMemorySupported()) {
            out.addProperty("supported", false);
            return out;
        }
        if (!bean.isThreadAllocatedMemoryEnabled()) {
            bean.setThreadAllocatedMemoryEnabled(true);
        }
        long thread = Thread.currentThread().threadId();
        for (Mode mode : Mode.values()) {
            long before = bean.getThreadAllocatedBytes(thread);
            renderBatch(targets, mode, visible, consumer, frames);
            long after = bean.getThreadAllocatedBytes(thread);
            out.addProperty(mode.jsonName + "_bytes_per_frame", (after - before) / (double) frames);
        }
        out.addProperty("supported", true);
        return out;
    }

    private static JsonObject statistics(LongSamples samples) {
        long[] sorted = samples.sorted();
        JsonObject out = new JsonObject();
        double median = percentile(sorted, 0.50) / 1_000_000.0;
        double p95 = percentile(sorted, 0.95) / 1_000_000.0;
        double p99 = percentile(sorted, 0.99) / 1_000_000.0;
        out.addProperty("sample_count", sorted.length);
        out.addProperty("median_frame_ms", median);
        out.addProperty("p95_frame_ms", p95);
        out.addProperty("p99_frame_ms", p99);
        out.addProperty("one_percent_low_fps", p99 <= 0.0 ? 0.0 : 1000.0 / p99);
        return out;
    }

    private static JsonObject aggregate(List<Double> medians, List<Double> p95s,
                                        List<Double> p99s) {
        JsonObject out = new JsonObject();
        double median = median(medians);
        double p95 = median(p95s);
        double p99 = median(p99s);
        out.addProperty("aggregation", "median of per-run metric");
        out.addProperty("median_frame_ms", median);
        out.addProperty("p95_frame_ms", p95);
        out.addProperty("p99_frame_ms", p99);
        out.addProperty("one_percent_low_fps", p99 <= 0.0 ? 0.0 : 1000.0 / p99);
        return out;
    }

    private static double median(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 0
                ? (sorted.get(middle - 1) + sorted.get(middle)) * 0.5
                : sorted.get(middle);
    }

    private static long percentile(long[] sorted, double percentile) {
        if (sorted.length == 0) {
            throw new IllegalStateException("benchmark produced no samples");
        }
        int index = Math.min(sorted.length - 1,
                Math.max(0, (int) Math.ceil(percentile * sorted.length) - 1));
        return sorted[index];
    }

    private static JsonObject evaluateProxyBudget(JsonObject protocol, JsonArray scenes) {
        JsonObject budget = protocol.getAsJsonObject("provisional_budget");
        String sceneId = budget.get("scene").getAsString();
        JsonObject scene = null;
        for (JsonElement element : scenes) {
            JsonObject candidate = element.getAsJsonObject();
            if (candidate.get("id").getAsString().equals(sceneId)) {
                scene = candidate;
                break;
            }
        }
        if (scene == null) {
            throw new IllegalStateException("budget scene was not measured: " + sceneId);
        }
        double medianRegression = scene.get("median_regression_percent").getAsDouble();
        double p95Regression = scene.get("p95_regression_ms").getAsDouble();
        JsonObject out = new JsonObject();
        out.addProperty("status", "Q6_PENDING_LIVE_PRECUTOVER");
        out.addProperty("scene", sceneId);
        out.addProperty("comparison_to_q6_budget_prohibited", true);
        out.addProperty("component_proxy_warning_median_regression_percent", medianRegression);
        out.addProperty("component_proxy_warning_p95_regression_ms", p95Regression);
        JsonObject limits = new JsonObject();
        limits.addProperty("max_median_frame_regression_percent",
                budget.get("max_median_frame_regression_percent").getAsDouble());
        limits.addProperty("max_p95_frame_regression_ms",
                budget.get("max_p95_frame_regression_ms").getAsDouble());
        limits.addProperty("max_server_p95_ms",
                budget.get("max_server_p95_ms").getAsDouble());
        limits.addProperty("sustained_mhlib_packet_growth_allowed",
                budget.get("sustained_mhlib_packet_growth_allowed").getAsBoolean());
        out.add("provisional_limits_not_applied_to_component_proxy", limits);
        out.addProperty("reason",
                "The reported percentage uses renderer-component time as its denominator and cannot "
                        + "be compared to Q6's whole-client frame percentage. Final Q6 evaluation "
                        + "requires an installed candidate renderer, GPU submission, server, and "
                        + "MHLib packet stream; the absolute component p95 delta is recorded now.");
        return out;
    }

    private static JsonObject environment() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        JsonObject out = new JsonObject();
        out.addProperty("os_name", System.getProperty("os.name"));
        out.addProperty("os_version", System.getProperty("os.version"));
        out.addProperty("os_arch", System.getProperty("os.arch"));
        out.addProperty("processor_identifier", System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "unknown"));
        out.addProperty("available_processors", runtime.availableProcessors());
        out.addProperty("max_heap_bytes", runtime.maxMemory());
        out.addProperty("java_version", System.getProperty("java.version"));
        out.addProperty("java_vendor", System.getProperty("java.vendor"));
        out.addProperty("java_vm", System.getProperty("java.vm.name"));
        out.addProperty("java_home", System.getProperty("java.home"));
        JsonArray flags = new JsonArray();
        runtimeBean.getInputArguments().forEach(flags::add);
        out.add("jvm_flags", flags);
        return out;
    }

    private static JsonObject provenance(Path repositoryRoot, JsonObject manifest,
                                         Path generatedDir, Path compiledDir,
                                         Path manifestPath, Path protocolPath) throws Exception {
        JsonObject out = new JsonObject();
        out.addProperty("repository_base_revision", gitRevision(repositoryRoot));
        out.addProperty("revision_semantics",
                "base revision plus exact working-tree source/input hashes below");

        JsonObject sources = new JsonObject();
        for (String relative : List.of(
                "src/g1tool/java/danger/orespawn/g1/G1PerformanceBenchmark.java",
                "src/g1tool/java/danger/orespawn/g1/G1AnimationRuntime.java",
                "tools/g1_benchmark_gate.py",
                "tools/g1_performance_benchmark.json",
                "tools/g1_model_proofs.json",
                "build.gradle")) {
            Path path = repositoryRoot.resolve(relative);
            sources.addProperty(relative, sha256(Files.readAllBytes(path)));
        }
        out.add("source_files_sha256", sources);
        out.addProperty("manifest_path", repositoryRoot.relativize(manifestPath).toString().replace('\\', '/'));
        out.addProperty("protocol_path", repositoryRoot.relativize(protocolPath).toString().replace('\\', '/'));

        JsonObject modelInputs = new JsonObject();
        for (JsonElement element : manifest.getAsJsonArray("models")) {
            JsonObject spec = element.getAsJsonObject();
            String id = spec.get("id").getAsString();
            JsonObject input = new JsonObject();
            input.addProperty("compiled_dump_sha256",
                    sha256(Files.readAllBytes(compiledDir.resolve(id + ".compiled.json"))));
            input.addProperty("generated_geo_sha256",
                    sha256(Files.readAllBytes(generatedDir.resolve(id + ".geo.json"))));
            input.addProperty("conversion_report_sha256",
                    sha256(Files.readAllBytes(generatedDir.resolve(id + ".conversion.json"))));
            input.addProperty("accepted_candidate_path",
                    spec.has("candidate_animation_path")
                            ? spec.get("candidate_animation_path").getAsString()
                            : "static_bind_pose");
            input.addProperty("reference_animation_used", false);
            input.addProperty("stale_pose_artifact_present",
                    Files.exists(generatedDir.resolve(id + ".poses.json")));
            modelInputs.add(id, input);
        }
        out.add("model_inputs", modelInputs);
        out.add("declared_runtime_versions", manifest.getAsJsonObject("runtime_versions").deepCopy());

        JsonObject runtimeClasspath = new JsonObject();
        runtimeClasspath.add("benchmark_harness", runtimeClassEvidence(
                "G1 benchmark harness", G1PerformanceBenchmark.class));
        runtimeClasspath.add("candidate_animation_runtime", runtimeClassEvidence(
                "G1 candidate custom-animation runtime", G1AnimationRuntime.class));
        runtimeClasspath.add("geckolib", runtimeClassEvidence(
                "GeckoLib " + manifest.getAsJsonObject("runtime_versions").get("geckolib").getAsString(),
                GeoRenderer.class));
        runtimeClasspath.add("neoforge", runtimeClassEvidence(
                "NeoForge " + manifest.getAsJsonObject("runtime_versions").get("neoforge").getAsString(),
                Class.forName("net.neoforged.neoforge.common.NeoForge")));
        runtimeClasspath.add("minecraft", runtimeClassEvidence(
                "Minecraft " + manifest.getAsJsonObject("runtime_versions").get("minecraft").getAsString(),
                ModelPart.class));
        out.add("runtime_classpath", runtimeClasspath);
        return out;
    }

    private static JsonObject runtimeClassEvidence(String declaredVersion, Class<?> type) throws Exception {
        String classResource = type.getName().replace('.', '/') + ".class";
        byte[] classBytes;
        try (InputStream stream = type.getClassLoader().getResourceAsStream(classResource)) {
            if (stream == null) {
                throw new IllegalStateException("Cannot read runtime class " + classResource);
            }
            classBytes = stream.readAllBytes();
        }
        Path codeSource = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toAbsolutePath().normalize();
        JsonObject out = new JsonObject();
        out.addProperty("declared_version", declaredVersion);
        out.addProperty("representative_class", type.getName());
        out.addProperty("class_resource", classResource);
        out.addProperty("class_sha256", sha256(classBytes));
        out.addProperty("classpath_entry_name", codeSource.getFileName().toString());
        out.addProperty("classpath_entry_kind", Files.isDirectory(codeSource) ? "directory" : "file");
        out.addProperty("classpath_entry_sha256", sha256ClasspathEntry(codeSource));
        return out;
    }

    private static String sha256ClasspathEntry(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            return sha256(Files.readAllBytes(path));
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (Stream<Path> stream = Files.walk(path)) {
                for (Path file : stream.filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(item -> path.relativize(item).toString())).toList()) {
                    String relative = path.relativize(file).toString().replace('\\', '/');
                    digest.update(relative.getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    digest.update(Files.readAllBytes(file));
                }
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String gitRevision(Path repositoryRoot) throws Exception {
        Process process = new ProcessBuilder(
                "git", "-C", repositoryRoot.toString(), "rev-parse", "HEAD")
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (process.waitFor() != 0 || !output.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalStateException("Unable to bind benchmark to git revision: " + output);
        }
        return output.toLowerCase(Locale.ROOT);
    }

    private static JsonObject requireObject(JsonObject parent, String name) {
        if (!parent.has(name) || !parent.get(name).isJsonObject()) {
            throw new IllegalArgumentException("missing object " + name);
        }
        return parent.getAsJsonObject(name);
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private enum Mode {
        CLASSIC("classic"), CANDIDATE("candidate");

        private final String jsonName;

        Mode(String jsonName) {
            this.jsonName = jsonName;
        }
    }

    private record SceneTarget(BenchmarkTarget target, int count) {
    }

    private record GcSnapshot(long collections, long timeMillis) {
        static GcSnapshot capture() {
            long collections = 0;
            long time = 0;
            for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
                collections += Math.max(0L, bean.getCollectionCount());
                time += Math.max(0L, bean.getCollectionTime());
            }
            return new GcSnapshot(collections, time);
        }
    }

    private static final class LongSamples {
        private long[] values = new long[4096];
        private int size;

        void add(long value) {
            if (this.size == this.values.length) {
                this.values = Arrays.copyOf(this.values, this.values.length * 2);
            }
            this.values[this.size++] = value;
        }

        long[] sorted() {
            long[] copy = Arrays.copyOf(this.values, this.size);
            Arrays.sort(copy);
            return copy;
        }
    }

    private static final class BenchmarkTarget {
        private final String id;
        private final EntityModel<?> classic;
        private final ModelPart classicRoot;
        private final List<ModelPart> classicParts;
        private final Method setupAnim;
        private final float limbSwing;
        private final String animationKind;
        private final Map<String, BenchmarkSample> compiledSamples;
        private final G1AnimationRuntime.Evaluator candidateEvaluator;
        private BakedGeoModel geo;
        private Map<String, GeoBone> geoBones;
        private final BenchmarkGeoRenderer geoRenderer;
        private final int boneCount;

        private BenchmarkTarget(String id, EntityModel<?> classic, ModelPart classicRoot,
                                Method setupAnim, float limbSwing, String animationKind,
                                Map<String, BenchmarkSample> compiledSamples,
                                G1AnimationRuntime.Evaluator candidateEvaluator,
                                G1AnimationRuntime.EvaluatedModel initial,
                                BenchmarkGeoRenderer geoRenderer) {
            this.id = id;
            this.classic = classic;
            this.classicRoot = classicRoot;
            this.classicParts = classicRoot.getAllParts().toList();
            this.setupAnim = setupAnim;
            this.limbSwing = limbSwing;
            this.animationKind = animationKind;
            this.compiledSamples = compiledSamples;
            this.candidateEvaluator = candidateEvaluator;
            this.geo = initial.model();
            this.geoBones = initial.bones();
            this.geoRenderer = geoRenderer;
            this.boneCount = initial.bones().size();
        }

        static BenchmarkTarget load(JsonObject spec, Path generatedDir,
                                    Path compiledDir) throws Exception {
            String id = spec.get("id").getAsString();
            Path stalePose = generatedDir.resolve(id + ".poses.json");
            if (Files.exists(stalePose)) {
                throw new IllegalStateException(
                        "obsolete self-confirming pose artifact must not exist: " + stalePose);
            }
            Class<?> modelClass = Class.forName(spec.get("class").getAsString());
            Method factory = modelClass.getDeclaredMethod("createBodyLayer");
            if (!Modifier.isStatic(factory.getModifiers())) {
                throw new IllegalStateException(modelClass.getName() + ".createBodyLayer is not static");
            }
            factory.setAccessible(true);
            ModelPart root = ((LayerDefinition) factory.invoke(null)).bakeRoot();
            Constructor<?> constructor = modelClass.getDeclaredConstructor(ModelPart.class);
            constructor.setAccessible(true);
            EntityModel<?> classic = (EntityModel<?>) constructor.newInstance(root);
            Method setupAnim = Arrays.stream(modelClass.getDeclaredMethods())
                    .filter(method -> method.getName().equals("setupAnim"))
                    .filter(method -> method.getParameterCount() == 6)
                    .filter(method -> !method.isBridge())
                    .findFirst()
                    .orElseThrow();
            setupAnim.setAccessible(true);

            Path geoPath = generatedDir.resolve(id + ".geo.json");
            Model raw = KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(geoPath), Model.class);
            G1AnimationRuntime.Evaluator evaluator = G1AnimationRuntime.evaluator(raw);
            G1AnimationRuntime.EvaluatedModel initial = evaluator.bindPose();
            Map<String, BenchmarkSample> samples = new TreeMap<>();
            JsonObject compiled = readJson(compiledDir.resolve(id + ".compiled.json"));
            for (JsonElement element : compiled.getAsJsonArray("samples")) {
                JsonObject sample = element.getAsJsonObject();
                BenchmarkSample value = new BenchmarkSample(
                        sample.get("age_ticks").getAsFloat(),
                        sample.get("limb_swing_amount").getAsFloat());
                if (samples.put(sample.get("id").getAsString(), value) != null) {
                    throw new IllegalStateException(id + " has duplicate compiled sample id");
                }
            }
            return new BenchmarkTarget(
                    id, classic, root, setupAnim, spec.get("limb_swing").getAsFloat(),
                    spec.get("animation_kind").getAsString(), samples, evaluator, initial,
                    new BenchmarkGeoRenderer());
        }

        void configurePose(String sampleId) throws Exception {
            BenchmarkSample sample = this.compiledSamples.get(sampleId);
            if (sample == null) {
                throw new IllegalStateException(this.id + " missing compiled benchmark sample " + sampleId);
            }
            this.classicRoot.getAllParts().forEach(ModelPart::resetPose);
            if (!"bind".equals(sampleId)) {
                this.setupAnim.invoke(this.classic, null, this.limbSwing,
                        sample.limbSwingAmount(), sample.ageTicks(), 0.0F, 0.0F);
            }
            G1AnimationRuntime.EvaluatedModel evaluated;
            if ("static".equals(this.animationKind)) {
                evaluated = this.candidateEvaluator.bindPose();
            } else if ("gait_scaled".equals(this.animationKind)) {
                evaluated = this.candidateEvaluator.evaluateBeaverCodeDriven(
                        sample.ageTicks(), sample.limbSwingAmount());
            } else {
                throw new IllegalStateException(this.id + " unsupported benchmark animation kind "
                        + this.animationKind);
            }
            this.geo = evaluated.model();
            this.geoBones = evaluated.bones();
        }

        void touchClassic(NoopVertexConsumer consumer) {
            for (ModelPart part : this.classicParts) {
                consumer.touch(part.xRot, part.yRot, part.zRot);
            }
        }

        void touchGeo(NoopVertexConsumer consumer) {
            for (GeoBone bone : this.geoBones.values()) {
                consumer.touch(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            }
        }
    }

    private record BenchmarkSample(float ageTicks, float limbSwingAmount) {
    }

    private static final class BenchmarkGeoRenderer implements GeoRenderer<GeoAnimatable> {
        @Override
        public GeoModel<GeoAnimatable> getGeoModel() {
            return null;
        }

        @Override
        public GeoAnimatable getAnimatable() {
            return null;
        }

        @Override
        public void fireCompileRenderLayersEvent() {
        }

        @Override
        public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model,
                                          MultiBufferSource bufferSource, float partialTick,
                                          int packedLight) {
            return true;
        }

        @Override
        public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model,
                                        MultiBufferSource bufferSource, float partialTick,
                                        int packedLight) {
        }

        @Override
        public void updateAnimatedTextureFrame(GeoAnimatable animatable) {
        }
    }

    private static final class NoopVertexConsumer implements VertexConsumer {
        private long vertices;
        private long checksum;

        void touch(float x, float y, float z) {
            this.checksum = Long.rotateLeft(this.checksum, 7)
                    ^ Float.floatToRawIntBits(x)
                    ^ ((long) Float.floatToRawIntBits(y) << 17)
                    ^ ((long) Float.floatToRawIntBits(z) << 33);
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                              int packedOverlay, int packedLight,
                              float normalX, float normalY, float normalZ) {
            this.vertices++;
            touch(x + u, y + v, z + normalX + normalY + normalZ);
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            throw new IllegalStateException("benchmark requires atomic vertex writes");
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }
}
