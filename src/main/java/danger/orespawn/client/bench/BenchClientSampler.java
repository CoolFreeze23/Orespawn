package danger.orespawn.client.bench;

import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.Window;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.bench.BenchClientBridge;
import danger.orespawn.bench.BenchClientSnapshot;
import danger.orespawn.bench.BenchCommand;
import danger.orespawn.bench.BenchReport;
import danger.orespawn.bench.BenchSceneSpawner;
import danger.orespawn.bench.BenchStats;
import de.dertoaster.multihitboxlib.util.MHLibCollectorProbe;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Phase G slice (d): the client half of the in-game benchmark -- installed at client setup only
 * under {@code -Dorespawn.dev.bench=true}, driven by the server command through
 * {@link BenchClientBridge} (the requests arrive on the server thread and are picked up on the
 * render thread's next frame). Client-only: nothing in the game tests references this class.
 *
 * <p>Whole-client frame time: the interval between successive {@code RenderFrameEvent.Pre} events.
 * In the 21.1.223 bytecode {@code Minecraft.runTick(boolean)} calls
 * {@code ClientHooks.fireRenderFramePre} at offset 398, {@code GameRenderer.render} at 422 and
 * {@code fireRenderFramePost} at 438; the blit to screen, the swap ({@code Window.updateDisplay},
 * where vsync and the framerate limit wait) and the game ticks of the next loop iteration all lie
 * between one Pre and the next, so Pre-to-Pre is the frame the player sees (1 / FPS). Run with
 * vsync off and the framerate limit unlimited for the interval to measure work rather than a cap.</p>
 *
 * <p>Client CPU: the render thread's {@code ThreadMXBean.getCurrentThreadCpuTime()} over the run's
 * wall time, and the JVM's {@code OperatingSystemMXBean.getProcessCpuLoad()} averaged over the
 * client ticks of the run. Allocation: the render thread's own
 * {@code getCurrentThreadAllocatedBytes} and, where JDK 21's {@code getTotalThreadAllocatedBytes}
 * is available, every thread's; GC from the collector beans' count and time deltas. The client-side
 * MHLib counters are summed from the dumps that fall inside the run plus the partial interval, as
 * {@code BenchSession} does for the server side.</p>
 *
 * <p>Requests from the server thread (refuter B, 2026-09-06): one latest-wins slot
 * ({@link #request}: a stop, or the seconds of a start) instead of two flags, because two flags
 * cannot tell {@code stop} then {@code start} from {@code start} then {@code stop} within one
 * frame. The render thread takes the slot on its next frame and, before anything else, ENDS the
 * run in flight (stopped early) -- then begins the new one if the request was a start. So a
 * {@code scene} (abort, i.e. stop) followed by {@code start} in the same frame starts the new run
 * instead of killing it a frame later, and a {@code start} followed by {@code stop} leaves nothing
 * running, which is what the server session holds in both cases.</p>
 */
public final class BenchClientSampler implements BenchClientBridge.ClientController {

    private static final BenchClientSampler INSTANCE = new BenchClientSampler();
    private static boolean installed;
    private static final int REQUEST_NONE = 0;
    private static final int REQUEST_STOP = -1;

    private final Object lock = new Object();
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    /** {@link #REQUEST_NONE}, {@link #REQUEST_STOP}, or the seconds of a requested start (> 0); the latest request wins. */
    private final AtomicInteger request = new AtomicInteger(REQUEST_NONE);

    private boolean running;
    private long runStartNanos;
    private long lastPreNanos;
    private long durationNanos;
    private double[] frameMs = new double[4096];
    private int frames;
    private long cpuStart;
    private long allocStart;
    private long totalAllocStart;
    private long gcCountStart;
    private long gcTimeStart;
    private double cpuLoadSum;
    private int cpuLoadSamples;
    private final Map<String, Long> clientSums = new LinkedHashMap<>();
    private final Map<String, Long> startPartial = new LinkedHashMap<>();
    private int clientDumps;
    private Map<String, String> controls = new LinkedHashMap<>();
    private Map<String, String> devSwitch = new LinkedHashMap<>();
    private BenchClientSnapshot last;

    private BenchClientSampler() {
    }

    /** Registers the frame, tick and dump listeners and installs the controller into the bridge. */
    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;
        NeoForge.EVENT_BUS.addListener(INSTANCE::onRenderFramePre);
        NeoForge.EVENT_BUS.addListener(INSTANCE::onClientTick);
        MHLibCounters.addDumpListener(INSTANCE::onDump);
        BenchClientBridge.install(INSTANCE);
        OreSpawnMod.LOGGER.warn("Phase G dev bench: client sampler installed (-D{}=true); frame timer on RenderFrameEvent.Pre", BenchCommand.PROPERTY);
    }

    // ------------------------------------------------------------------ the bridge (server thread)

    @Override
    public void start(int seconds) {
        this.request.set(Math.max(1, seconds));
    }

    @Override
    public void stop() {
        this.request.set(REQUEST_STOP);
    }

    @Override
    public BenchClientSnapshot snapshot() {
        synchronized (this.lock) {
            if (this.running) {
                return this.compute(System.nanoTime(), false, "in progress", -1);
            }
            return this.last;
        }
    }

    // ------------------------------------------------------------------ the render thread

    private void onRenderFramePre(RenderFrameEvent.Pre event) {
        final long now = System.nanoTime();
        final int request = this.request.getAndSet(REQUEST_NONE);
        if (request != REQUEST_NONE) {
            // A stop, or a start while a run is in flight: the old run ends first (stopped early).
            if (this.running) {
                this.finish(now, false);
            }
            if (request > 0) {
                this.begin(now, request);
            }
            return;
        }
        if (!this.running) {
            return;
        }
        synchronized (this.lock) {
            if (this.lastPreNanos != 0L) {
                this.append((now - this.lastPreNanos) / 1.0e6D);
            }
            this.lastPreNanos = now;
        }
        if (now - this.runStartNanos >= this.durationNanos) {
            this.finish(now, true);
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        if (!this.running) {
            return;
        }
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean sun) {
            double load = sun.getProcessCpuLoad();
            if (load >= 0.0D) {
                synchronized (this.lock) {
                    this.cpuLoadSum += load;
                    this.cpuLoadSamples++;
                }
            }
        }
    }

    private void onDump(String side, int tick, Map<String, Long> values) {
        if (!this.running || !MHLibCounters.CLIENT_SIDE.equals(side)) {
            return;
        }
        synchronized (this.lock) {
            for (Map.Entry<String, Long> entry : values.entrySet()) {
                this.clientSums.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
            this.clientDumps++;
        }
    }

    private void begin(long now, int seconds) {
        synchronized (this.lock) {
            this.frames = 0;
            this.runStartNanos = now;
            this.lastPreNanos = now;
            this.durationNanos = seconds * 1_000_000_000L;
            this.cpuStart = this.threads.isCurrentThreadCpuTimeSupported() ? this.threads.getCurrentThreadCpuTime() : 0L;
            this.allocStart = MHLibCollectorProbe.currentThreadAllocatedBytes();
            this.totalAllocStart = MHLibCollectorProbe.totalThreadAllocatedBytes();
            this.gcCountStart = gcCount();
            this.gcTimeStart = gcTime();
            this.cpuLoadSum = 0.0D;
            this.cpuLoadSamples = 0;
            this.clientSums.clear();
            this.clientDumps = 0;
            this.startPartial.clear();
            for (MHLibCounters.Counter counter : MHLibCounters.all()) {
                this.startPartial.put(counter.name(), counter.sum());
            }
            this.controls = readControls();
            this.devSwitch = BenchReport.devSwitchStates();
            this.last = null;
            this.running = true;
        }
        OreSpawnMod.LOGGER.info("bench client: sampling {} s of frames", seconds);
    }

    private void finish(long now, boolean finished) {
        Minecraft mc = Minecraft.getInstance();
        String statistics = mc.levelRenderer != null ? mc.levelRenderer.getEntityStatistics() : "n/a";
        int tagged = 0;
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity.getTags().contains(BenchSceneSpawner.TAG)) {
                    tagged++;
                }
            }
        }
        synchronized (this.lock) {
            this.last = this.compute(now, finished, statistics, tagged);
            this.running = false;
        }
        OreSpawnMod.LOGGER.info(String.format(Locale.ROOT,
                "bench client: %s -- %d frames in %.1f s, median %.3f ms, p95 %.3f ms, 1%% low %.1f FPS, %d client dumps summed",
                finished ? "run finished" : "run stopped early", this.last.frames(), this.last.seconds(), this.last.frameMedianMs(),
                this.last.frameP95Ms(), this.last.onePercentLowFps(), this.last.counterDumps()));
    }

    /** Under {@link #lock}. */
    private BenchClientSnapshot compute(long now, boolean finished, String statistics, int tagged) {
        double seconds = (now - this.runStartNanos) / 1.0e9D;
        double[] sampled = Arrays.copyOf(this.frameMs, this.frames);
        long cpuNow = this.threads.isCurrentThreadCpuTimeSupported() && Thread.currentThread().getName().equals("Render thread")
                ? this.threads.getCurrentThreadCpuTime() : -1L;
        double renderCpuPercent = cpuNow >= 0L && seconds > 0.0D ? 100.0D * (cpuNow - this.cpuStart) / (seconds * 1.0e9D) : Double.NaN;
        long allocNow = Thread.currentThread().getName().equals("Render thread") ? MHLibCollectorProbe.currentThreadAllocatedBytes() : -1L;
        double renderAllocMb = allocNow >= 0L && seconds > 0.0D ? (allocNow - this.allocStart) / (1024.0D * 1024.0D) / seconds : Double.NaN;
        long totalNow = MHLibCollectorProbe.totalThreadAllocatedBytes();
        double jvmAllocMb = totalNow >= 0L && this.totalAllocStart >= 0L && seconds > 0.0D
                ? (totalNow - this.totalAllocStart) / (1024.0D * 1024.0D) / seconds : -1.0D;
        double gcPerSecond = seconds > 0.0D ? (gcCount() - this.gcCountStart) / seconds : Double.NaN;
        double gcMsPerSecond = seconds > 0.0D ? (gcTime() - this.gcTimeStart) / seconds : Double.NaN;
        Map<String, Double> perSecond = new LinkedHashMap<>();
        for (MHLibCounters.Counter counter : MHLibCounters.all()) {
            long dumps = this.clientSums.getOrDefault(counter.name(), 0L);
            long total = Math.max(0L, dumps + counter.sum() - this.startPartial.getOrDefault(counter.name(), 0L));
            perSecond.put(counter.name(), seconds > 0.0D ? total / seconds : Double.NaN);
        }
        return new BenchClientSnapshot(finished, seconds, this.frames,
                BenchStats.median(sampled), BenchStats.percentile(sampled, 0.95D), BenchStats.percentile(sampled, 0.99D),
                BenchStats.onePercentLowFps(sampled), seconds > 0.0D ? this.frames / seconds : Double.NaN,
                renderCpuPercent, this.cpuLoadSamples > 0 ? 100.0D * this.cpuLoadSum / this.cpuLoadSamples : Double.NaN,
                renderAllocMb, jvmAllocMb, gcPerSecond, gcMsPerSecond,
                perSecond, this.clientDumps, new LinkedHashMap<>(this.controls), new LinkedHashMap<>(this.devSwitch),
                statistics, tagged);
    }

    /** Under {@link #lock}. */
    private void append(double ms) {
        if (this.frames == this.frameMs.length) {
            this.frameMs = Arrays.copyOf(this.frameMs, this.frameMs.length * 2);
        }
        this.frameMs[this.frames++] = ms;
    }

    private static long gcCount() {
        long count = 0L;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long c = gc.getCollectionCount();
            if (c > 0L) {
                count += c;
            }
        }
        return count;
    }

    private static long gcTime() {
        long time = 0L;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long t = gc.getCollectionTime();
            if (t > 0L) {
                time += t;
            }
        }
        return time;
    }

    /** The protocol's controls the client can report; the owner fills the rest in the report. */
    private static Map<String, String> readControls() {
        Map<String, String> out = new LinkedHashMap<>();
        try {
            Minecraft mc = Minecraft.getInstance();
            Options o = mc.options;
            Window w = mc.getWindow();
            out.put("client.launched_version", mc.getLaunchedVersion());
            out.put("client.resolution", w.getWidth() + "x" + w.getHeight() + (w.isFullscreen() ? " fullscreen" : " windowed")
                    + ", screen " + w.getScreenWidth() + "x" + w.getScreenHeight() + ", gui scale " + w.getGuiScale());
            out.put("client.render_distance_chunks", String.valueOf(o.renderDistance().get()));
            out.put("client.simulation_distance_chunks", String.valueOf(o.simulationDistance().get()));
            out.put("client.graphics_mode", String.valueOf(o.graphicsMode().get()));
            out.put("client.vsync", String.valueOf(o.enableVsync().get()));
            out.put("client.framerate_limit", String.valueOf(o.framerateLimit().get()));
            out.put("client.fov", String.valueOf(o.fov().get()));
            out.put("client.entity_distance_scaling", String.valueOf(o.entityDistanceScaling().get()));
            out.put("client.entity_shadows", String.valueOf(o.entityShadows().get()));
            out.put("client.particles", String.valueOf(o.particles().get()));
            out.put("client.clouds", String.valueOf(o.cloudStatus().get()));
            out.put("client.ambient_occlusion", String.valueOf(o.ambientOcclusion().get()));
            out.put("client.biome_blend_radius", String.valueOf(o.biomeBlendRadius().get()));
            out.put("client.fps_at_start", String.valueOf(mc.getFps()));
            out.put("gpu.vendor", GlUtil.getVendor());
            out.put("gpu.renderer", GlUtil.getRenderer());
            out.put("gpu.opengl", GlUtil.getOpenGLVersion());
            out.put("cpu", GlUtil.getCpuInfo());
        } catch (RuntimeException | LinkageError partial) {
            out.put("client.controls_error", String.valueOf(partial));
        }
        return out;
    }
}
