package danger.orespawn.bench;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.IModernLeggedRobot;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Phase G slice (d): the server half of the in-game benchmark -- the spawned scene, the sampling
 * window and its result. One session per JVM (the dev command drives it; everything runs on the
 * server thread). Common code with no client import.
 *
 * <p>Server MSPT: on every {@code ServerTickEvent.Post} of a run the tick just completed is read from
 * {@code MinecraftServer.getTickTimesNanos()[getTickCount() % 100]}. In the 21.1.223 bytecode
 * {@code tickServer(BooleanSupplier)} increments {@code tickCount} (offsets 6-11) before
 * {@code EventHooks.fireServerTickPre} (16), runs {@code tickChildren} (28), stores
 * {@code Util.getNanos() - start} into {@code tickTimesNanos[tickCount % 100]} (153-204) and fires
 * {@code EventHooks.fireServerTickPost} last (246) -- so at the Post event the slot for the current
 * {@code tickCount} holds this tick's time.</p>
 *
 * <p>Server-side MHLib counters: the run sums every server dump that falls inside it (the
 * {@link MHLibCounters.DumpListener}) and adds the partial interval at the end
 * ({@code end partial - start partial}, read without resetting), so a run's total is exact
 * whatever its alignment with the 100-tick dump cadence. The client sampler does the same for the
 * client side.</p>
 *
 * <p>Coverage (refuter B, 2026-09-06): the scene's farthest slot distance is kept from the layout,
 * and at the end of a run the mobs alive in entity-ticking chunks are counted
 * ({@link BenchSceneSpawner#countTicking}) -- entities tick only in ENTITY_TICKING chunks (the
 * simulation-distance tickets), so the report normalises the per-entity server figures by that
 * count, not by what was spawned. Counted at the end of the run rather than at {@code report}
 * time because {@code stop} may have despawned the scene by then.</p>
 */
public final class BenchSession {

    private static final BenchSession INSTANCE = new BenchSession();

    private BenchScene scene;
    private BenchState state;
    private int countRequested;
    private double maxDistanceBlocks;
    private final List<Mob> mobs = new ArrayList<>();
    private ServerLevel level;

    private boolean running;
    private int ticksWanted;
    private int ticksDone;
    private int requestedSeconds;
    private double[] tickMs;
    private long runStartNanos;
    private long runStartEpochMs;
    private final Map<String, Long> serverSums = new LinkedHashMap<>();
    private final Map<String, Long> startPartial = new LinkedHashMap<>();
    private int serverDumps;
    private BenchServerResult lastResult;

    private final MHLibCounters.DumpListener dumpListener = this::onDump;

    private BenchSession() {
    }

    public static BenchSession get() {
        return INSTANCE;
    }

    /** The game-bus listener the harness registers when the bench is enabled. */
    public static void onServerTick(ServerTickEvent.Post event) {
        INSTANCE.tick(event.getServer());
    }

    // ------------------------------------------------------------------ the scene

    /** Despawns any current scene and spawns {@code scene}; returns the message for the command. */
    public synchronized String spawnScene(ServerLevel level, Vec3 origin, float yawDegrees, BenchScene scene, int count,
                                          BenchState state) {
        if (this.running) {
            this.abortRun();
        }
        int swept = this.despawn();
        this.level = level;
        this.scene = scene;
        this.state = state;
        this.countRequested = count;
        // The layout is computed once more for the coverage figure (cheap; it throws for a count past the capacity).
        this.maxDistanceBlocks = BenchSceneSpawner.maxDistanceBlocks(origin, BenchSceneSpawner.layout(origin, yawDegrees, scene, count));
        this.mobs.addAll(BenchSceneSpawner.spawn(level, origin, yawDegrees, scene, count, state));
        int parts = BenchSceneSpawner.partCount(this.mobs);
        int modern = this.modernCount();
        String message = String.format(Locale.ROOT,
                "bench scene %s: spawned %d/%d %s (%s), %d MHLib parts%s%s, farthest %.1f blocks out (wedge within %.0f degrees of the look axis) -- %s",
                scene.name(), this.mobs.size(), count, scene.species(), state.name().toLowerCase(Locale.ROOT), parts,
                modern >= 0 ? ", modern gait on " + modern : "",
                swept > 0 ? ", swept " + swept + " previous" : "",
                this.maxDistanceBlocks, BenchSceneSpawner.WEDGE_HALF_ANGLE_DEGREES,
                scene.description());
        OreSpawnMod.LOGGER.info(message);
        return message;
    }

    /** Discards the scene's mobs (and any tagged leftovers of the level); returns how many went. */
    public synchronized int despawn() {
        int swept = 0;
        if (this.level != null) {
            swept = BenchSceneSpawner.discardTagged(this.level);
        }
        this.mobs.clear();
        return swept;
    }

    private int modernCount() {
        boolean gaited = false;
        int modern = 0;
        for (Mob mob : this.mobs) {
            if (mob instanceof IModernLeggedRobot robot) {
                gaited = true;
                if (robot.isModernMovement()) {
                    modern++;
                }
            }
        }
        return gaited ? modern : -1;
    }

    // ------------------------------------------------------------------ the run

    /** Begins sampling for {@code seconds} of server ticks (and asks the client to sample the same wall time). */
    public synchronized String start(MinecraftServer server, int seconds) {
        if (this.scene == null || this.mobs.isEmpty()) {
            return "bench: no scene spawned -- run /orespawn bench scene <A-F> first";
        }
        if (this.running) {
            return "bench: a run is in progress (" + this.ticksDone + "/" + this.ticksWanted + " ticks); stop it first";
        }
        this.requestedSeconds = seconds;
        this.ticksWanted = seconds * 20;
        this.ticksDone = 0;
        this.tickMs = new double[this.ticksWanted];
        this.serverSums.clear();
        this.serverDumps = 0;
        this.startPartial.clear();
        for (MHLibCounters.Counter counter : MHLibCounters.serverAll()) {
            this.startPartial.put(counter.name(), counter.sum());
        }
        MHLibCounters.addDumpListener(this.dumpListener);
        this.runStartNanos = System.nanoTime();
        this.runStartEpochMs = System.currentTimeMillis();
        this.running = true;
        BenchClientBridge.start(seconds);
        String message = String.format(Locale.ROOT, "bench: sampling scene %s for %d s (%d server ticks); client sampler %s; MHLib counters %s",
                this.scene.name(), seconds, this.ticksWanted, BenchClientBridge.available() ? "started" : "NOT available in this JVM",
                MHLibCounters.ENABLED ? "enabled" : "DISABLED (start the game with -Dmhlib.counters=true)");
        OreSpawnMod.LOGGER.info(message);
        return message;
    }

    private void onDump(String side, int tick, Map<String, Long> values) {
        synchronized (this) {
            if (!this.running || !MHLibCounters.SERVER_SIDE.equals(side)) {
                return;
            }
            for (Map.Entry<String, Long> entry : values.entrySet()) {
                this.serverSums.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
            this.serverDumps++;
        }
    }

    private synchronized void tick(MinecraftServer server) {
        if (!this.running) {
            return;
        }
        long[] times = server.getTickTimesNanos();
        if (times != null && times.length > 0 && this.ticksDone < this.tickMs.length) {
            this.tickMs[this.ticksDone] = times[Math.floorMod(server.getTickCount(), times.length)] / 1.0e6D;
        }
        this.ticksDone++;
        if (this.ticksDone >= this.ticksWanted) {
            this.finish(server);
        }
    }

    private void finish(MinecraftServer server) {
        MHLibCounters.removeDumpListener(this.dumpListener);
        this.running = false;
        double wallSeconds = (System.nanoTime() - this.runStartNanos) / 1.0e9D;
        Map<String, Long> totals = new LinkedHashMap<>();
        for (MHLibCounters.Counter counter : MHLibCounters.serverAll()) {
            long dumps = this.serverSums.getOrDefault(counter.name(), 0L);
            long endPartial = counter.sum();
            long begin = this.startPartial.getOrDefault(counter.name(), 0L);
            totals.put(counter.name(), Math.max(0L, dumps + endPartial - begin));
        }
        int samples = Math.min(this.ticksDone, this.tickMs.length);
        double[] sampled = new double[samples];
        System.arraycopy(this.tickMs, 0, sampled, 0, samples);
        int ticking = this.level == null ? 0 : BenchSceneSpawner.countTicking(this.level, this.mobs);
        this.lastResult = new BenchServerResult(
                this.scene, this.state, this.countRequested, this.mobs.size(), ticking, this.maxDistanceBlocks, this.modernCount(),
                BenchSceneSpawner.partCount(this.mobs), this.requestedSeconds, samples, wallSeconds,
                BenchStats.median(sampled), BenchStats.percentile(sampled, 0.95D), BenchStats.percentile(sampled, 0.99D),
                BenchStats.mean(sampled), totals, this.serverDumps, MHLibCounters.ENABLED,
                server.getPlayerList().getPlayerCount(),
                this.level == null ? BenchGit.UNKNOWN : this.level.dimension().location().toString(),
                this.runStartEpochMs, System.currentTimeMillis());
        OreSpawnMod.LOGGER.info(String.format(Locale.ROOT,
                "bench: run finished -- scene %s, %d ticks in %.1f s, MSPT median %.3f ms / p95 %.3f ms, %d server dumps summed, %d of %d mobs in entity-ticking chunks%s; /orespawn bench report writes it",
                this.scene.name(), samples, wallSeconds, this.lastResult.msptMedianMs(), this.lastResult.msptP95Ms(), this.serverDumps,
                ticking, this.mobs.size(), ticking < this.mobs.size() ? " (WARNING: not every mob ticked -- raise the simulation distance)" : ""));
    }

    private void abortRun() {
        MHLibCounters.removeDumpListener(this.dumpListener);
        this.running = false;
        BenchClientBridge.stop();
    }

    /** Stops sampling (discarding an unfinished run) and despawns the scene. */
    public synchronized String stop() {
        boolean wasRunning = this.running;
        if (wasRunning) {
            this.abortRun();
        }
        int swept = this.despawn();
        String message = String.format(Locale.ROOT, "bench: %sdespawned %d entities%s", wasRunning ? "run aborted, " : "", swept,
                this.lastResult != null ? "; the last finished run is still reportable" : "");
        OreSpawnMod.LOGGER.info(message);
        return message;
    }

    public synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized BenchServerResult lastResult() {
        return this.lastResult;
    }

    public synchronized BenchScene scene() {
        return this.scene;
    }

    public synchronized String status() {
        StringBuilder sb = new StringBuilder("bench status: ");
        if (this.scene == null) {
            sb.append("no scene");
        } else {
            sb.append("scene ").append(this.scene.name()).append(' ').append(this.mobs.size()).append(" mobs (")
                    .append(this.state.name().toLowerCase(Locale.ROOT)).append(")");
        }
        sb.append("; run ").append(this.running ? this.ticksDone + "/" + this.ticksWanted + " ticks" : "idle");
        sb.append("; last result ").append(this.lastResult == null ? "none" : "scene " + this.lastResult.scene().name());
        sb.append("; client sampler ").append(BenchClientBridge.available() ? "available" : "not available");
        sb.append("; MHLib counters ").append(MHLibCounters.ENABLED ? "enabled" : "disabled");
        return sb.toString();
    }
}
