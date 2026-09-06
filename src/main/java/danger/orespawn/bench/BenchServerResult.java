package danger.orespawn.bench;

import java.util.Map;

/**
 * Phase G slice (d): what the server half of the harness measured over a run. Plain data, common
 * code; the report writer and the game tests build one directly.
 *
 * @param scene             the scene spawned
 * @param state             the mob state it was spawned in
 * @param countRequested    the count asked for
 * @param countSpawned      the mobs actually added to the level
 * @param countTicking      of those, the mobs alive and standing in an entity-ticking chunk at the end of the run
 *                          ({@code ServerLevel.isPositionEntityTicking}) -- what the per-entity server figures divide by
 * @param maxDistanceBlocks the farthest slot's x/z distance from the camera origin ({@code BenchSceneSpawner.maxDistanceBlocks})
 * @param modernCount       modern-gait robots among them, or -1 when the species has no gait mode
 * @param partCount         MHLib parts across the mobs at the end of the run
 * @param requestedSeconds  the duration asked for
 * @param serverTicks       server ticks sampled
 * @param wallSeconds       wall-clock duration of the sampling
 * @param msptMedianMs      median tick time (MinecraftServer.getTickTimesNanos)
 * @param msptP95Ms         p95 tick time
 * @param msptP99Ms         p99 tick time
 * @param msptMeanMs        mean tick time
 * @param serverCounterTotals the server-side MHLib counters summed over the run (dumps plus the partial interval)
 * @param serverDumps       server dumps that fell inside the run
 * @param countersEnabled   whether {@code -Dmhlib.counters=true} was set in this JVM
 * @param playersOnline     players online at the end of the run
 * @param dimension         the level's dimension id
 * @param startedAtEpochMs  wall-clock start
 * @param finishedAtEpochMs wall-clock end
 */
public record BenchServerResult(
        BenchScene scene,
        BenchState state,
        int countRequested,
        int countSpawned,
        int countTicking,
        double maxDistanceBlocks,
        int modernCount,
        int partCount,
        int requestedSeconds,
        int serverTicks,
        double wallSeconds,
        double msptMedianMs,
        double msptP95Ms,
        double msptP99Ms,
        double msptMeanMs,
        Map<String, Long> serverCounterTotals,
        int serverDumps,
        boolean countersEnabled,
        int playersOnline,
        String dimension,
        long startedAtEpochMs,
        long finishedAtEpochMs) {
}
