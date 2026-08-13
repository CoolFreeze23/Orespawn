package danger.orespawn.client;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.IModernLeggedRobot;
import danger.orespawn.entity.SpiderRobot;
import net.minecraft.world.entity.Entity;

/**
 * S7b — client-only smoothed state for the riding camera arm (see
 * MountCameraMixin). Smoothing is TIME-based (exponential toward the
 * target with a ~0.25s time constant — the reference's per-tick 0.25
 * lerp, framerate-independent), applied to the ARM only, never the
 * pivot. Collision response is asymmetric: shortening snaps instantly
 * (never clip), recovery glides. Dismount/mode-off resets to inactive
 * and the very next frame is pure vanilla.
 */
public final class MountCameraState {

    private static final double SPIDER_DISTANCE = 10.0;
    private static final double ANT_DISTANCE = 6.0;
    private static final double HEIGHT = 2.0;
    private static final double LATERAL = 1.5;
    /** Exponential time constant ~0.25s (rate 4/s). */
    private static final double SMOOTH_RATE = 4.0;

    private static boolean active;
    private static double distance;
    private static double height;
    private static double lateral;
    private static double collisionFraction = 1.0;
    private static long lastNanos;
    private static double lastDt;

    private MountCameraState() {
    }

    /** The mount's target camera distance, or 0 when the vanilla camera must run. */
    public static double targetDistance(Entity vehicle) {
        if (!OreSpawnConfig.MOUNT_CAMERA.get()) {
            return 0.0;
        }
        if (vehicle instanceof IModernLeggedRobot robot && robot.isModernMovement()) {
            return vehicle instanceof SpiderRobot ? SPIDER_DISTANCE
                    : vehicle instanceof AntRobot ? ANT_DISTANCE : 0.0;
        }
        return 0.0;
    }

    public static void smoothTowards(double targetDistance) {
        long now = System.nanoTime();
        // Reviewer finding 4: a dismount+remount entirely in first person
        // never runs reset() — treat a >1s gap as a fresh activation.
        if (active && now - lastNanos > 1_000_000_000L) {
            active = false;
        }
        double dt = active ? Math.min(0.25, (now - lastNanos) / 1.0E9) : 0.0;
        lastNanos = now;
        lastDt = dt;
        if (!active) {
            // Start from the vanilla arm and glide out.
            active = true;
            distance = 4.0;
            height = 0.0;
            lateral = 0.0;
            collisionFraction = 1.0;
            return;
        }
        double alpha = 1.0 - Math.exp(-dt * SMOOTH_RATE);
        distance += (targetDistance - distance) * alpha;
        height += (HEIGHT - height) * alpha;
        lateral += (LATERAL - lateral) * alpha;
    }

    /** Asymmetric collision: snap in instantly, glide back out (time-based — reviewer finding 3: a per-frame lerp was framerate-dependent). */
    public static double applyCollision(double fraction) {
        if (fraction < collisionFraction) {
            collisionFraction = fraction;
        } else {
            double alpha = 1.0 - Math.exp(-lastDt * 6.0);
            collisionFraction += (fraction - collisionFraction) * alpha;
        }
        return collisionFraction;
    }

    public static double distance() {
        return distance;
    }

    public static double height() {
        return height;
    }

    public static double lateral() {
        return lateral;
    }

    public static void reset() {
        active = false;
        collisionFraction = 1.0;
    }
}
