package danger.orespawn.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A test's long scan of the world generators run off the server thread (TEST-024: the altar and terrain scans held
 * single ticks for minutes), on the common pool, which no server code uses. The scan reads detached generators and
 * registries only; the test passes when it returns and fails with its assertion when it throws. While a scan runs
 * each tick is topped up to 50 ms waiting for it (once a tick, however many scans run): the game-test server does
 * not wait between ticks (GameTestServer.waitUntilNextTick only runs its tasks), so without the wait the test's
 * timeout would go by in seconds of a scan that takes minutes; with it the server keeps 20 ticks a second while a
 * scan runs, the timeout counts real time, and no tick runs behind the server's schedule ("Can't keep up").
 * TEST-025: a scan calls {@link #check} between its steps, so that once its test is over (timed out, failed or
 * passed) it stops there instead of running on in the pool behind the rest of the suite; the global state a scan
 * needs is set and restored on the server thread around it (EntityLogicTestsA.onTestExit), never by the scan.
 */
final class OffThread {
    private static final long TICK_NANOS = 50_000_000L;
    /** The tick last topped up and when its wait ended (the server thread's own). */
    private static long pacedTick = -1L;
    private static long pacedAt = System.nanoTime();
    /** Each scan's test and whether it is over, set on the server thread when the test ends on any path. */
    private static final Map<GameTestHelper, AtomicBoolean> OVER = Collections.synchronizedMap(new WeakHashMap<>());

    private OffThread() {
    }

    static void run(GameTestHelper helper, Runnable scan) {
        AtomicBoolean over = new AtomicBoolean();
        OVER.put(helper, over);
        EntityLogicTestsA.onTestExit(helper, () -> over.set(true));
        CompletableFuture<Void> result = CompletableFuture.runAsync(scan);
        AtomicBoolean reported = new AtomicBoolean();
        helper.onEachTick(() -> {
            if (reported.get()) return;
            long tick = helper.getLevel().getServer().getTickCount();
            boolean pacing = tick != pacedTick;
            if (pacing) pacedTick = tick;
            long wait = pacing ? Math.max(0L, TICK_NANOS - (System.nanoTime() - pacedAt)) : 0L;
            try {
                result.get(wait, TimeUnit.NANOSECONDS);
            } catch (TimeoutException e) {
                if (pacing) pacedAt = System.nanoTime();
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                reported.set(true);
                Throwable cause = e.getCause() == null ? e : e.getCause();
                if (cause instanceof RuntimeException failure) throw failure;
                throw new RuntimeException(cause.getMessage() == null ? cause.toString() : cause.getMessage(), cause);
            }
            reported.set(true);
            helper.succeed();
        });
    }

    /**
     * Called by a scan between its steps, on whichever thread runs the step (a scan's parallel streams too): throws
     * once the scan's test is over, which ends the scan where it stands; nothing reads its result any more.
     */
    static void check(GameTestHelper helper) {
        AtomicBoolean over = OVER.get(helper);
        if (over != null && over.get()) throw new CancellationException("the test is over: the scan stops");
    }
}
