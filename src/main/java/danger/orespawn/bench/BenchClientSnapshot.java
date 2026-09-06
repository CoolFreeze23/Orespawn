package danger.orespawn.bench;

import java.util.Map;

/**
 * Phase G slice (d): what the client half of the harness measured over a run, handed to the server
 * half through {@link BenchClientBridge} (integrated server: same JVM). Common code -- a plain
 * record with no client import -- so the report writer and the game tests can build one.
 *
 * @param finished             whether the run reached its duration (else the values are in-progress)
 * @param seconds              the sampled wall-clock duration
 * @param frames               frames sampled (RenderFrameEvent.Pre to Pre intervals)
 * @param frameMedianMs        median whole-client frame time
 * @param frameP95Ms           p95 whole-client frame time
 * @param frameP99Ms           p99 whole-client frame time
 * @param onePercentLowFps     1000 / p99
 * @param averageFps           frames / seconds
 * @param renderThreadCpuPercent render-thread CPU time over wall time, in percent
 * @param processCpuPercent    the JVM process' CPU load averaged over the client ticks of the run, in percent
 * @param renderThreadAllocMbPerSec bytes the render thread allocated per second, in MB
 * @param jvmAllocMbPerSec     bytes every thread allocated per second, in MB (-1 when unavailable)
 * @param gcCollectionsPerSec  collections of every GC bean per second
 * @param gcMsPerSec           collection time of every GC bean per second, in ms
 * @param countersPerSecond    the client-side MHLib counters per second (empty when the counters are off)
 * @param counterDumps         how many client dumps the run summed
 * @param controls             what the client can report of the protocol's controls (JDK, flags, resolution, options, GPU)
 * @param devSwitch            the Phase G dev switch per landed species (classic / candidate)
 * @param entityStatistics     the level renderer's entity statistics line at the end of the run
 * @param benchEntitiesInClientLevel benchmark-tagged entities present in the client level at the end
 */
public record BenchClientSnapshot(
        boolean finished,
        double seconds,
        int frames,
        double frameMedianMs,
        double frameP95Ms,
        double frameP99Ms,
        double onePercentLowFps,
        double averageFps,
        double renderThreadCpuPercent,
        double processCpuPercent,
        double renderThreadAllocMbPerSec,
        double jvmAllocMbPerSec,
        double gcCollectionsPerSec,
        double gcMsPerSec,
        Map<String, Double> countersPerSecond,
        int counterDumps,
        Map<String, String> controls,
        Map<String, String> devSwitch,
        String entityStatistics,
        int benchEntitiesInClientLevel) {
}
