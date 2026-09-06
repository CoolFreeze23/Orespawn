package danger.orespawn.bench;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.client.DevRendererSwitch;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Phase G slice (d): the report of one run -- JSON for the tools, Markdown for the owner -- written to
 * {@code phase_g_reports/benchmark/live/<scene>_<label>_<timestamp>.json} and {@code .md}. A label
 * is {@code classic} or {@code candidate} (the Phase G dev switch's state for the scene's species,
 * or a label the owner passes to {@code /orespawn bench report <label>} for harvest before/afters);
 * when the live directory already holds a report of the same scene under the other of the two
 * labels, the newest one is paired and the deltas the proposed threshold would read are appended --
 * as information: the threshold is proposed, not adopted, and nothing here gates anything.
 *
 * <p>Common code: {@link DevRendererSwitch} is pure policy with no Minecraft import. The game tests
 * pin the JSON shape through {@link #build} with a synthetic server result and no client.</p>
 *
 * <p>Refuter B (2026-09-06): every metric that can be undefined (no client, no samples, a zero
 * divisor) is written as JSON {@code null}, never as a bare {@code NaN} -- Gson's tree writer emits
 * {@code NaN} verbatim (RFC 8259 forbids it) whatever the builder options say, so the mapping
 * happens here in {@link #number}; the pairing reader tolerates null. Per-entity figures divide by
 * what actually ran: the server counters by {@code coverage.ticking} (the mobs in entity-ticking
 * chunks at the end of the run), the client counters by {@code coverage.in_client_level} (the
 * benchmark-tagged entities in the client level); {@code count_spawned} stays as the scene size.</p>
 */
public final class BenchReport {

    public static final int SCHEMA_VERSION = 1;
    public static final String LIVE_DIR = "phase_g_reports/benchmark/live";
    public static final String LABEL_CLASSIC = "classic";
    public static final String LABEL_CANDIDATE = "candidate";
    /** The landed species with a GeckoLib candidate renderer behind the dev switch (PhaseGDevRenderers). */
    public static final List<String> LANDED_SPECIES = List.of("beaver", "elevator", "vortex", "coin", "island", "island_too",
            "robot_1", "robot_2", "robot_3", "robot_4", "robot_5", "rock_base", "purple_power", "rotator");

    /** {@code serializeNulls}: an undefined metric is written as {@code null} (a stable schema), not dropped from the file. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private BenchReport() {
    }

    /** {@code yyyyMMddTHHmmssZ} in UTC: sortable, file-name safe. */
    public static String timestamp(Instant instant) {
        return TIMESTAMP.format(instant);
    }

    /** The label the dev switch implies for a species: candidate when selected and the species has one, classic otherwise. */
    public static String variantFor(String species) {
        if (!LANDED_SPECIES.contains(species)) {
            return LABEL_CLASSIC;
        }
        return DevRendererSwitch.geckolib(species) == DevRendererSwitch.Variant.CANDIDATE ? LABEL_CANDIDATE : LABEL_CLASSIC;
    }

    /** The dev switch's state for every landed species. */
    public static Map<String, String> devSwitchStates() {
        Map<String, String> out = new java.util.LinkedHashMap<>();
        for (String species : LANDED_SPECIES) {
            out.put(species, DevRendererSwitch.geckolib(species) == DevRendererSwitch.Variant.CANDIDATE ? LABEL_CANDIDATE : LABEL_CLASSIC);
        }
        return out;
    }

    // ------------------------------------------------------------------ build

    /**
     * The report object. {@code client} may be null (a dedicated server, or a client that never
     * sampled); the counters section is keyed by counter name and lists every server counter even
     * when zero. {@code workingTree} is {@link BenchGit#workingTree}'s reading.
     */
    public static JsonObject build(BenchServerResult server, BenchClientSnapshot client, String label, String gitHead,
                                   String workingTree, String timestamp) {
        JsonObject report = new JsonObject();
        report.addProperty("schema_version", SCHEMA_VERSION);
        report.addProperty("harness", "orespawn bench (Phase G slice (d), 2026-09-06)");
        report.addProperty("scene", server.scene().name());
        report.addProperty("scene_description", server.scene().description());
        report.addProperty("species", server.scene().species());
        report.addProperty("state", server.state().name().toLowerCase(Locale.ROOT));
        report.addProperty("variant", label);
        report.addProperty("variant_source", LANDED_SPECIES.contains(server.scene().species())
                ? "the Phase G dev switch (-D" + DevRendererSwitch.PROPERTY + ") for " + server.scene().species() + ", unless a label was passed to the report command"
                : "no candidate renderer exists for " + server.scene().species() + ": classic unless a label was passed; pair harvest before/afters by git_head");
        report.addProperty("git_head", gitHead);
        report.addProperty("working_tree", workingTree == null ? BenchGit.WORKING_TREE_HEAD_ONLY : workingTree);
        report.addProperty("timestamp", timestamp);
        report.addProperty("count_requested", server.countRequested());
        report.addProperty("count_spawned", server.countSpawned());
        report.addProperty("modern_count", server.modernCount());
        report.addProperty("part_count", server.partCount());

        final int inClientLevel = client == null ? -1 : client.benchEntitiesInClientLevel();
        JsonObject coverage = new JsonObject();
        coverage.addProperty("spawned", server.countSpawned());
        coverage.addProperty("ticking", server.countTicking());
        if (inClientLevel >= 0) {
            coverage.addProperty("in_client_level", inClientLevel);
        } else {
            coverage.add("in_client_level", JsonNull.INSTANCE);
        }
        number(coverage, "max_distance_blocks", server.maxDistanceBlocks());
        coverage.addProperty("layout", "a wedge within " + (int) BenchSceneSpawner.WEDGE_HALF_ANGLE_DEGREES + " degrees of the look axis, every mob under "
                + (int) BenchSceneSpawner.MAX_DISTANCE_BLOCKS + " blocks (the default simulation distance is 12 chunks = 192 blocks, the entity-ticking range; "
                + "the Queen's client tracking range is 256 blocks, capped by the view distance)");
        StringBuilder warning = new StringBuilder();
        if (server.countTicking() < server.countSpawned()) {
            warning.append("only ").append(server.countTicking()).append(" of ").append(server.countSpawned())
                    .append(" spawned mobs stood in entity-ticking chunks at the end of the run (raise the simulation distance; per-entity server figures divide by the ticking count)");
        }
        if (inClientLevel >= 0 && inClientLevel < server.countSpawned()) {
            if (warning.length() > 0) {
                warning.append("; ");
            }
            warning.append("only ").append(inClientLevel).append(" of ").append(server.countSpawned())
                    .append(" spawned mobs were in the client level at the end of the run (raise the render distance; per-entity client figures divide by that count)");
        }
        coverage.add("warning", warning.length() == 0 ? JsonNull.INSTANCE : new com.google.gson.JsonPrimitive(warning.toString()));
        coverage.addProperty("note", "per-entity server figures divide by ticking, per-entity client figures by in_client_level; count_spawned is the scene size");
        report.add("coverage", coverage);

        JsonObject run = new JsonObject();
        run.addProperty("requested_seconds", server.requestedSeconds());
        run.addProperty("server_ticks", server.serverTicks());
        number(run, "wall_seconds", server.wallSeconds());
        run.addProperty("players_online", server.playersOnline());
        run.addProperty("dimension", server.dimension());
        run.addProperty("started_at_epoch_ms", server.startedAtEpochMs());
        run.addProperty("finished_at_epoch_ms", server.finishedAtEpochMs());
        report.add("run", run);

        JsonObject serverJson = new JsonObject();
        number(serverJson, "mspt_median_ms", server.msptMedianMs());
        number(serverJson, "mspt_p95_ms", server.msptP95Ms());
        number(serverJson, "mspt_p99_ms", server.msptP99Ms());
        number(serverJson, "mspt_mean_ms", server.msptMeanMs());
        serverJson.addProperty("tick_samples", server.serverTicks());
        serverJson.addProperty("source", "MinecraftServer.getTickTimesNanos()[getTickCount() % 100] at ServerTickEvent.Post");
        report.add("server", serverJson);

        double seconds = server.wallSeconds() > 0.0D ? server.wallSeconds() : Double.NaN;
        final int serverDivisor = server.countTicking();
        JsonObject counters = new JsonObject();
        counters.addProperty("enabled", server.countersEnabled());
        JsonObject serverTotals = new JsonObject();
        JsonObject serverPerSecond = new JsonObject();
        JsonObject perEntityPerSecond = new JsonObject();
        for (Map.Entry<String, Long> entry : server.serverCounterTotals().entrySet()) {
            serverTotals.addProperty(entry.getKey(), entry.getValue());
            double perSecond = entry.getValue() / seconds;
            number(serverPerSecond, entry.getKey(), perSecond);
            number(perEntityPerSecond, entry.getKey(), serverDivisor > 0 ? perSecond / serverDivisor : Double.NaN);
        }
        counters.add("server_totals", serverTotals);
        counters.add("server_per_second", serverPerSecond);
        counters.addProperty("server_dumps", server.serverDumps());
        JsonObject clientPerSecond = new JsonObject();
        if (client != null) {
            for (Map.Entry<String, Double> entry : client.countersPerSecond().entrySet()) {
                number(clientPerSecond, entry.getKey(), entry.getValue());
                number(perEntityPerSecond, entry.getKey(), inClientLevel > 0 ? entry.getValue() / inClientLevel : Double.NaN);
            }
            counters.addProperty("client_dumps", client.counterDumps());
        }
        counters.add("client_per_second", clientPerSecond);
        counters.add("per_entity_per_second", perEntityPerSecond);
        counters.addProperty("per_entity_divisors", "server counters / coverage.ticking; client counters / coverage.in_client_level");
        report.add("counters", counters);

        JsonObject network = new JsonObject();
        number(network, "s2c_packets_per_second", perSecond(server, "net.s2c_update_packets", seconds) + perSecond(server, "net.set_master_packets", seconds));
        number(network, "s2c_bytes_per_second", perSecond(server, "net.s2c_update_bytes", seconds));
        number(network, "c2s_packets_per_second", client == null ? Double.NaN : client.countersPerSecond().getOrDefault("net.c2s_bone_packets", Double.NaN));
        number(network, "c2s_bytes_per_second", client == null ? Double.NaN : client.countersPerSecond().getOrDefault("net.c2s_bone_bytes", Double.NaN));
        network.addProperty("note", "S2C counts one per broadcast call (every TrackedEntity, tracked or not — an upper bound on delivered "
                + "packets; coverage.in_client_level is the honest single-player count) and the encoded payload "
                + "length (no packet header, no compression); the protocol's scenes have one tracker. C2S is the client's own count. "
                + "Under the counters property the byte accounting re-encodes each packet on the sampled thread (the client tick for C2S, "
                + "the server thread for S2C; the real encode runs on the netty thread), so frame time, render-thread allocation, GC and "
                + "MSPT carry that extra encode — identical under both labels, outside collector_ns and placement_ns.");
        report.add("network", network);

        JsonObject clientJson = new JsonObject();
        if (client == null) {
            clientJson.addProperty("available", false);
            clientJson.addProperty("reason", "no client sampler in this JVM (dedicated server, or the bench property unset on the client)");
        } else {
            clientJson.addProperty("available", true);
            clientJson.addProperty("finished", client.finished());
            number(clientJson, "seconds", client.seconds());
            clientJson.addProperty("frames", client.frames());
            number(clientJson, "frame_median_ms", client.frameMedianMs());
            number(clientJson, "frame_p95_ms", client.frameP95Ms());
            number(clientJson, "frame_p99_ms", client.frameP99Ms());
            number(clientJson, "one_percent_low_fps", client.onePercentLowFps());
            number(clientJson, "average_fps", client.averageFps());
            number(clientJson, "render_thread_cpu_percent", client.renderThreadCpuPercent());
            number(clientJson, "process_cpu_percent", client.processCpuPercent());
            number(clientJson, "render_thread_alloc_mb_per_second", client.renderThreadAllocMbPerSec());
            number(clientJson, "jvm_alloc_mb_per_second", client.jvmAllocMbPerSec() < 0.0D ? Double.NaN : client.jvmAllocMbPerSec());
            number(clientJson, "gc_collections_per_second", client.gcCollectionsPerSec());
            number(clientJson, "gc_ms_per_second", client.gcMsPerSec());
            clientJson.addProperty("entity_statistics", client.entityStatistics());
            clientJson.addProperty("bench_entities_in_client_level", client.benchEntitiesInClientLevel());
            clientJson.addProperty("frame_source", "RenderFrameEvent.Pre to the next RenderFrameEvent.Pre (Minecraft.runTick: ClientHooks.fireRenderFramePre at offset 398, GameRenderer.render at 422, fireRenderFramePost at 438; Pre-to-Pre spans the whole frame incl. swap and ticks)");
        }
        report.add("client", clientJson);

        JsonObject controls = new JsonObject();
        controls.addProperty("jdk", System.getProperty("java.version", BenchGit.UNKNOWN) + " (" + System.getProperty("java.vm.name", BenchGit.UNKNOWN)
                + ", " + System.getProperty("java.vendor", BenchGit.UNKNOWN) + ")");
        JsonArray flags = new JsonArray();
        try {
            for (String flag : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                flags.add(flag);
            }
        } catch (RuntimeException unavailable) {
            flags.add("unavailable: " + unavailable);
        }
        controls.add("jvm_flags", flags);
        controls.addProperty("os", System.getProperty("os.name", BenchGit.UNKNOWN) + " " + System.getProperty("os.version", "")
                + " " + System.getProperty("os.arch", ""));
        controls.addProperty("mhlib_counters_property", String.valueOf(server.countersEnabled()));
        if (client != null) {
            for (Map.Entry<String, String> entry : client.controls().entrySet()) {
                controls.addProperty(entry.getKey(), entry.getValue());
            }
        }
        JsonObject ownerFields = new JsonObject();
        ownerFields.addProperty("machine", "");
        ownerFields.addProperty("gpu_and_driver", "");
        ownerFields.addProperty("camera_path", "");
        ownerFields.addProperty("world_seed", "");
        ownerFields.addProperty("notes", "");
        controls.add("owner_fields", ownerFields);
        report.add("controls", controls);

        JsonObject devSwitch = new JsonObject();
        Map<String, String> states = client != null ? client.devSwitch() : devSwitchStates();
        for (Map.Entry<String, String> entry : states.entrySet()) {
            devSwitch.addProperty(entry.getKey(), entry.getValue());
        }
        report.add("dev_switch", devSwitch);

        JsonObject threshold = new JsonObject();
        threshold.addProperty("status", "PROPOSED, NOT ADOPTED -- the owner rules; nothing here gates");
        threshold.addProperty("reference", "Phase G slice (d) threshold_proposal.md (draft) and tools/g1_performance_benchmark.json live_acceptance_protocol.budgets");
        report.add("threshold", threshold);
        return report;
    }

    /** Adds {@code value} under {@code key}, as JSON null when it is NaN or infinite (RFC 8259 has no NaN token). */
    static void number(JsonObject object, String key, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            object.add(key, JsonNull.INSTANCE);
        } else {
            object.addProperty(key, value);
        }
    }

    private static double perSecond(BenchServerResult server, String counter, double seconds) {
        Long total = server.serverCounterTotals().get(counter);
        return total == null ? Double.NaN : total / seconds;
    }

    // ------------------------------------------------------------------ write and pair

    /** The live directory under the repository root. */
    public static Path liveDirectory(Path repositoryRoot) {
        return repositoryRoot.resolve(LIVE_DIR);
    }

    public static String baseName(String scene, String label, String timestamp) {
        return scene + "_" + label + "_" + timestamp;
    }

    /** The JSON text written for a report (what {@link #write} puts on disk). */
    public static String toJson(JsonObject report) {
        return GSON.toJson(report);
    }

    /**
     * Writes {@code <scene>_<label>_<timestamp>.json} and {@code .md} into {@code liveDir}, pairing
     * with the newest report of the same scene under the other label when one exists. Returns the two
     * paths written.
     */
    public static List<Path> write(Path liveDir, JsonObject report, JsonObject pairing) throws IOException {
        Files.createDirectories(liveDir);
        String base = baseName(report.get("scene").getAsString(), report.get("variant").getAsString(), report.get("timestamp").getAsString());
        if (pairing != null) {
            report.add("pairing", pairing);
        }
        Path json = liveDir.resolve(base + ".json");
        Path md = liveDir.resolve(base + ".md");
        Files.writeString(json, toJson(report), StandardCharsets.UTF_8);
        Files.writeString(md, markdown(report), StandardCharsets.UTF_8);
        List<Path> out = new ArrayList<>();
        out.add(json);
        out.add(md);
        return out;
    }

    /** The other of the two labels, or {@code null} for an owner label (no automatic pairing). */
    public static String counterpartLabel(String label) {
        if (LABEL_CLASSIC.equals(label)) {
            return LABEL_CANDIDATE;
        }
        if (LABEL_CANDIDATE.equals(label)) {
            return LABEL_CLASSIC;
        }
        return null;
    }

    /** The newest report of {@code scene} under {@code label} in {@code liveDir}, or {@code null}. */
    public static JsonObject newest(Path liveDir, String scene, String label) {
        if (!Files.isDirectory(liveDir)) {
            return null;
        }
        Path best = null;
        String prefix = scene + "_" + label + "_";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(liveDir, prefix + "*.json")) {
            for (Path candidate : stream) {
                if (best == null || candidate.getFileName().toString().compareTo(best.getFileName().toString()) > 0) {
                    best = candidate;
                }
            }
        } catch (IOException unreadable) {
            return null;
        }
        if (best == null) {
            return null;
        }
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(best, StandardCharsets.UTF_8));
            if (parsed.isJsonObject()) {
                JsonObject other = parsed.getAsJsonObject();
                other.addProperty("_file", best.getFileName().toString());
                return other;
            }
        } catch (IOException | RuntimeException unreadable) {
            return null;
        }
        return null;
    }

    /**
     * The deltas the proposed threshold reads, {@code report} against {@code other} (the other label's
     * newest report of the same scene). Regressions are expressed candidate-relative-to-classic
     * whichever report is newer. An input that is null in either report makes the delta null.
     */
    public static JsonObject pairing(JsonObject report, JsonObject other) {
        if (other == null) {
            return null;
        }
        JsonObject classic = LABEL_CLASSIC.equals(report.get("variant").getAsString()) ? report : other;
        JsonObject candidate = classic == report ? other : report;
        JsonObject out = new JsonObject();
        out.addProperty("paired_with", other.has("_file") ? other.get("_file").getAsString() : BenchGit.UNKNOWN);
        out.addProperty("classic_git_head", stringOf(classic, "git_head"));
        out.addProperty("candidate_git_head", stringOf(candidate, "git_head"));
        out.addProperty("classic_working_tree", stringOf(classic, "working_tree"));
        out.addProperty("candidate_working_tree", stringOf(candidate, "working_tree"));
        double classicMedian = numberOf(classic, "client", "frame_median_ms");
        double candidateMedian = numberOf(candidate, "client", "frame_median_ms");
        number(out, "frame_median_regression_percent", 100.0D * (candidateMedian - classicMedian) / classicMedian);
        number(out, "frame_p95_delta_ms", numberOf(candidate, "client", "frame_p95_ms") - numberOf(classic, "client", "frame_p95_ms"));
        number(out, "one_percent_low_fps_delta", numberOf(candidate, "client", "one_percent_low_fps") - numberOf(classic, "client", "one_percent_low_fps"));
        double classicAlloc = numberOf(classic, "client", "render_thread_alloc_mb_per_second");
        double candidateAlloc = numberOf(candidate, "client", "render_thread_alloc_mb_per_second");
        number(out, "render_thread_alloc_ratio_candidate_over_classic", candidateAlloc / classicAlloc);
        number(out, "mspt_p95_delta_ms", numberOf(candidate, "server", "mspt_p95_ms") - numberOf(classic, "server", "mspt_p95_ms"));
        number(out, "collector_ns_per_entity_per_second_delta", perEntity(candidate, "client.collector_ns") - perEntity(classic, "client.collector_ns"));
        number(out, "c2s_bone_bytes_per_entity_per_second_delta", perEntity(candidate, "net.c2s_bone_bytes") - perEntity(classic, "net.c2s_bone_bytes"));
        number(out, "s2c_bytes_per_second_delta", numberOf(candidate, "network", "s2c_bytes_per_second") - numberOf(classic, "network", "s2c_bytes_per_second"));
        out.addProperty("note", "information for the owner's ruling; the proposed rule shape is in threshold_proposal.md and nothing is gated");
        return out;
    }

    private static String stringOf(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : BenchGit.UNKNOWN;
    }

    private static double numberOf(JsonObject object, String section, String key) {
        if (object == null || !object.has(section) || !object.get(section).isJsonObject()) {
            return Double.NaN;
        }
        return asDouble(object.getAsJsonObject(section).get(key));
    }

    private static double perEntity(JsonObject object, String counter) {
        if (object == null || !object.has("counters") || !object.get("counters").isJsonObject()) {
            return Double.NaN;
        }
        JsonObject counters = object.getAsJsonObject("counters");
        if (!counters.has("per_entity_per_second") || !counters.get("per_entity_per_second").isJsonObject()) {
            return Double.NaN;
        }
        return asDouble(counters.getAsJsonObject("per_entity_per_second").get(counter));
    }

    /** A JSON number as a double; NaN for null, absent, or a non-number. */
    private static double asDouble(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber() ? element.getAsDouble() : Double.NaN;
    }

    // ------------------------------------------------------------------ markdown

    public static String markdown(JsonObject r) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("# Live benchmark: scene ").append(r.get("scene").getAsString()).append(" (").append(r.get("variant").getAsString()).append(")\n\n");
        sb.append("- Harness: ").append(r.get("harness").getAsString()).append("\n");
        sb.append("- Scene: ").append(r.get("scene_description").getAsString()).append("\n");
        sb.append("- Species / state / count: ").append(r.get("species").getAsString()).append(" / ").append(r.get("state").getAsString())
                .append(" / ").append(r.get("count_spawned").getAsInt()).append(" of ").append(r.get("count_requested").getAsInt())
                .append(" (modern gait: ").append(r.get("modern_count").getAsInt() < 0 ? "n/a" : String.valueOf(r.get("modern_count").getAsInt()))
                .append("; MHLib parts: ").append(r.get("part_count").getAsInt()).append(")\n");
        JsonObject coverage = r.getAsJsonObject("coverage");
        sb.append("- Coverage: spawned ").append(coverage.get("spawned").getAsInt()).append(", ticking ").append(coverage.get("ticking").getAsInt())
                .append(", in the client level ").append(text(coverage.get("in_client_level"))).append(", farthest ").append(fmt(coverage.get("max_distance_blocks")))
                .append(" blocks").append(coverage.get("warning").isJsonNull() ? "" : "; WARNING: " + coverage.get("warning").getAsString()).append("\n");
        sb.append("- Variant: ").append(r.get("variant").getAsString()).append(" -- ").append(r.get("variant_source").getAsString()).append("\n");
        sb.append("- git HEAD: `").append(r.get("git_head").getAsString()).append("`; working tree: ").append(text(r.get("working_tree")))
                .append("; timestamp ").append(r.get("timestamp").getAsString()).append("\n\n");
        JsonObject run = r.getAsJsonObject("run");
        sb.append("## Run\n\n| requested s | server ticks | wall s | players | dimension |\n|---|---|---|---|---|\n| ")
                .append(run.get("requested_seconds").getAsInt()).append(" | ").append(run.get("server_ticks").getAsInt()).append(" | ")
                .append(fmt(run.get("wall_seconds"))).append(" | ").append(run.get("players_online").getAsInt()).append(" | ")
                .append(run.get("dimension").getAsString()).append(" |\n\n");
        JsonObject server = r.getAsJsonObject("server");
        sb.append("## Server (MSPT, ms)\n\n| median | p95 | p99 | mean | samples |\n|---|---|---|---|---|\n| ")
                .append(fmt(server.get("mspt_median_ms"))).append(" | ").append(fmt(server.get("mspt_p95_ms"))).append(" | ")
                .append(fmt(server.get("mspt_p99_ms"))).append(" | ").append(fmt(server.get("mspt_mean_ms"))).append(" | ")
                .append(server.get("tick_samples").getAsInt()).append(" |\n\n");
        JsonObject client = r.getAsJsonObject("client");
        sb.append("## Client\n\n");
        if (!client.get("available").getAsBoolean()) {
            sb.append("Not available: ").append(client.get("reason").getAsString()).append("\n\n");
        } else {
            sb.append("| frames | s | median ms | p95 ms | p99 ms | 1% low FPS | avg FPS | render CPU % | process CPU % | render alloc MB/s | JVM alloc MB/s | GC/s | GC ms/s |\n|---|---|---|---|---|---|---|---|---|---|---|---|---|\n| ")
                    .append(client.get("frames").getAsInt()).append(" | ").append(fmt(client.get("seconds"))).append(" | ")
                    .append(fmt(client.get("frame_median_ms"))).append(" | ").append(fmt(client.get("frame_p95_ms"))).append(" | ")
                    .append(fmt(client.get("frame_p99_ms"))).append(" | ").append(fmt(client.get("one_percent_low_fps"))).append(" | ")
                    .append(fmt(client.get("average_fps"))).append(" | ").append(fmt(client.get("render_thread_cpu_percent"))).append(" | ")
                    .append(fmt(client.get("process_cpu_percent"))).append(" | ").append(fmt(client.get("render_thread_alloc_mb_per_second"))).append(" | ")
                    .append(fmt(client.get("jvm_alloc_mb_per_second"))).append(" | ").append(fmt(client.get("gc_collections_per_second"))).append(" | ")
                    .append(fmt(client.get("gc_ms_per_second"))).append(" |\n\n");
            sb.append("- Entity statistics at the end: ").append(client.get("entity_statistics").getAsString()).append("; benchmark entities in the client level: ")
                    .append(client.get("bench_entities_in_client_level").getAsInt()).append("\n");
            sb.append("- Frame source: ").append(client.get("frame_source").getAsString()).append("\n\n");
        }
        JsonObject counters = r.getAsJsonObject("counters");
        sb.append("## MHLib counters (per second; enabled: ").append(counters.get("enabled").getAsBoolean()).append(")\n\n| counter | per second | per entity per second |\n|---|---|---|\n");
        JsonObject perEntity = counters.getAsJsonObject("per_entity_per_second");
        appendCounterRows(sb, counters.getAsJsonObject("server_per_second"), perEntity);
        appendCounterRows(sb, counters.getAsJsonObject("client_per_second"), perEntity);
        sb.append("\n").append(counters.get("per_entity_divisors").getAsString()).append("\n\n");
        JsonObject network = r.getAsJsonObject("network");
        sb.append("## Network (per second)\n\n| S2C packets | S2C bytes | C2S packets | C2S bytes |\n|---|---|---|---|\n| ")
                .append(fmt(network.get("s2c_packets_per_second"))).append(" | ").append(fmt(network.get("s2c_bytes_per_second"))).append(" | ")
                .append(fmt(network.get("c2s_packets_per_second"))).append(" | ").append(fmt(network.get("c2s_bytes_per_second"))).append(" |\n\n")
                .append(network.get("note").getAsString()).append("\n\n");
        JsonObject controls = r.getAsJsonObject("controls");
        sb.append("## Controls\n\n| control | value |\n|---|---|\n");
        for (Map.Entry<String, JsonElement> entry : controls.entrySet()) {
            if ("owner_fields".equals(entry.getKey())) {
                continue;
            }
            sb.append("| ").append(entry.getKey()).append(" | ").append(entry.getValue().isJsonPrimitive() ? entry.getValue().getAsString() : entry.getValue().toString()).append(" |\n");
        }
        sb.append("\nOwner fields (fill in): ");
        JsonObject owner = controls.getAsJsonObject("owner_fields");
        for (Map.Entry<String, JsonElement> entry : owner.entrySet()) {
            sb.append(entry.getKey()).append("=`").append(entry.getValue().getAsString()).append("` ");
        }
        sb.append("\n\n## Dev switch per landed species\n\n");
        JsonObject devSwitch = r.getAsJsonObject("dev_switch");
        for (Map.Entry<String, JsonElement> entry : devSwitch.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue().getAsString()).append("\n");
        }
        if (r.has("pairing")) {
            JsonObject pairing = r.getAsJsonObject("pairing");
            sb.append("\n## Pairing (candidate relative to classic; information only)\n\n| metric | value |\n|---|---|\n");
            for (Map.Entry<String, JsonElement> entry : pairing.entrySet()) {
                sb.append("| ").append(entry.getKey()).append(" | ").append(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()
                        ? fmt(entry.getValue()) : text(entry.getValue())).append(" |\n");
            }
        }
        JsonObject threshold = r.getAsJsonObject("threshold");
        sb.append("\n## Threshold\n\n").append(threshold.get("status").getAsString()).append(" -- ").append(threshold.get("reference").getAsString()).append("\n");
        return sb.toString();
    }

    private static void appendCounterRows(StringBuilder sb, JsonObject perSecond, JsonObject perEntity) {
        for (Map.Entry<String, JsonElement> entry : perSecond.entrySet()) {
            sb.append("| ").append(entry.getKey()).append(" | ").append(fmt(entry.getValue())).append(" | ")
                    .append(perEntity.has(entry.getKey()) ? fmt(perEntity.get(entry.getKey())) : "n/a").append(" |\n");
        }
    }

    /** A number element for the tables; {@code n/a} for null or a non-number. */
    private static String fmt(JsonElement element) {
        double value = asDouble(element);
        if (Double.isNaN(value)) {
            return "n/a";
        }
        return Math.abs(value) >= 100.0D ? String.format(Locale.ROOT, "%.1f", value) : String.format(Locale.ROOT, "%.3f", value);
    }

    /** A string element; {@code n/a} for null. */
    private static String text(JsonElement element) {
        return element == null || element.isJsonNull() ? "n/a" : element.isJsonPrimitive() ? element.getAsString() : element.toString();
    }
}
