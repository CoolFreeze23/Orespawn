package danger.orespawn.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

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
 */
final class OffThread {
    private static final long TICK_NANOS = 50_000_000L;
    /** The tick last topped up and when its wait ended (the server thread's own). */
    private static long pacedTick = -1L;
    private static long pacedAt = System.nanoTime();

    private OffThread() {
    }

    static void run(GameTestHelper helper, Runnable scan) {
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
}
