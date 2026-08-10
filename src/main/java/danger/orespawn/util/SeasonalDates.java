package danger.orespawn.util;

import java.time.LocalDate;
import java.time.Month;
import java.util.function.Supplier;

/**
 * Seasonal date gates, port of orig OreSpawnMain.java:4518-4521,4567-4571.
 * The original read a GregorianCalendar once at mod init (month 0-based:
 * 9/31 = Oct 31 Halloween, 1/14 = Feb 14 Valentine's, 3/20 = Apr 20 "Easter")
 * and froze the flags for the whole session. The port evaluates the real-time
 * date on each query (per the audit's ANIM-016 fix recommendation) so a server
 * crossing midnight picks the holiday up without a restart; the dates
 * themselves are unchanged, including the original's fixed April 20 Easter.
 */
public final class SeasonalDates {
    private SeasonalDates() {
    }

    /**
     * Clock seam for the GameTest suite (2026-08-10): production code always
     * runs on the real clock; tests inject a fixed date instead of touching
     * the system clock (per the ANIM-016 test plan). Behavior is unchanged —
     * the default supplier is exactly the {@code LocalDate.now()} each gate
     * called before.
     */
    private static volatile Supplier<LocalDate> clock = LocalDate::now;

    /** Test-only: override the date source. ALWAYS pair with {@link #resetClock}. */
    public static void setClockForTesting(Supplier<LocalDate> fixed) {
        clock = fixed;
    }

    /** Test-only: restore the real-time clock. */
    public static void resetClock() {
        clock = LocalDate::now;
    }

    /** orig OreSpawnMain.java:4521 — Oct 31: Ghost/GhostSkelly everywhere. */
    public static boolean isHalloween() {
        LocalDate now = clock.get();
        return now.getMonth() == Month.OCTOBER && now.getDayOfMonth() == 31;
    }

    /** orig OreSpawnMain.java:4567-4569 — Feb 14: giant angry Girlfriend. */
    public static boolean isValentines() {
        LocalDate now = clock.get();
        return now.getMonth() == Month.FEBRUARY && now.getDayOfMonth() == 14;
    }

    /** orig OreSpawnMain.java:4570-4571 — Apr 20: EasterBunny spawns. */
    public static boolean isEaster() {
        LocalDate now = clock.get();
        return now.getMonth() == Month.APRIL && now.getDayOfMonth() == 20;
    }
}
