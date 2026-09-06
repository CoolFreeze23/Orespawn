package danger.orespawn.bench;

import java.util.Arrays;

/**
 * Phase G slice (d): the order statistics the reports use. {@link #percentile} is the NEAREST-RANK
 * value, {@code sorted[ceil(p * n) - 1]} — the smallest sample that at least {@code p} of the samples are
 * less than or equal to: p99 of a hundred samples is the 99th smallest, so ONE outlier in a hundred sits
 * ABOVE p99 (it is p100, the max), and two in two hundred sit above it as well (the 198th smallest is
 * p99, the 199th p99.5). The alternative "exclusive" reading ({@code sorted[floor(p * n)]}, the value at
 * least {@code 1 - p} of the samples reach) would make a single slow frame in a hundred the p99; it is
 * NOT used — the slice (d) gate (2026-09-06) corrected a row that had assumed it. The median is the usual
 * midpoint for an even count. The 1 % low is reported as FPS: {@code 1000 / p99(frame ms)}, the frame
 * rate at the p99 frame time (the "p99 FPS") — not the worst frame, and not CapFrameX's / Afterburner's
 * average of the slowest 1 % (refuter A, A6) — alongside the p99 frame time itself.
 */
public final class BenchStats {

    private BenchStats() {
    }

    public static double median(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int n = sorted.length;
        return n % 2 == 1 ? sorted[n / 2] : 0.5D * (sorted[n / 2 - 1] + sorted[n / 2]);
    }

    /**
     * Nearest-rank percentile, {@code p} in (0, 1]: {@code sorted[ceil(p * n) - 1]}, the rank clamped to [1, n]. {@code p * n} is
     * a double product; at the ranks the rows pin (99 of 100; 198, 199 and 200 of 200) it lands on the exact integer.
     */
    public static double percentile(double[] values, double p) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int rank = (int) Math.ceil(p * sorted.length);
        return sorted[Math.min(sorted.length, Math.max(1, rank)) - 1];
    }

    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        double sum = 0.0D;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /**
     * {@code 1000 / p99(frameMs)} — the p99-percentile FPS convention, NOT the "average FPS of the slowest 1 %" that
     * CapFrameX / Afterburner report (which is lower); the p99 itself is reported beside it (refuter A, 2026-09-06).
     */
    public static double onePercentLowFps(double[] frameMs) {
        double p99 = percentile(frameMs, 0.99D);
        return p99 > 0.0D ? 1000.0D / p99 : Double.NaN;
    }
}
