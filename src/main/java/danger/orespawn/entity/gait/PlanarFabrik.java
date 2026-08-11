package danger.orespawn.entity.gait;

/**
 * 2.0 spider overhaul (S2): planar FABRIK solver for a 3-segment leg.
 *
 * <p><b>Why planar:</b> the shipped robot models pose each leg with one yaw
 * ({@code ydisplayangle}), one whole-leg elevation ({@code uddisplayangle})
 * and three segment pitches ({@code p1x/p2x/p3xangle}) — five DOF that can
 * only place joints inside the single vertical plane through the hip whose
 * bearing is the leg yaw. Solving in that plane from the start means every
 * solved pose is exactly representable by the model with zero projection
 * loss; a general 3-D FABRIK could bend knees out of plane and would then
 * have to be flattened anyway.</p>
 *
 * <p><b>Plane frame:</b> 2-D coordinates {@code (u, v)} with the hip at the
 * origin, {@code u} the horizontal unit axis pointing from the hip toward
 * the foot target (so {@code targetU >= 0} always) and {@code v} straight up
 * (world +Y). The caller builds the frame from world positions and maps the
 * solved joints back (see {@code ModernSpiderGait.solveLegAngles}).</p>
 *
 * <p><b>Technique</b> (from the S1 reference study, re-implemented): FABRIK
 * — forward-and-backward reaching — with the reference's "straighten bias"
 * knee seed: before iterating, the chain is laid out straight along the
 * hip→target direction rotated <i>upward</i> by {@code kneeBiasRad}. The
 * backward pass then folds the chain down onto the target from above, so the
 * knee always bows up (the spider-leg arch of the shipped rest pose
 * {@code p1=+45°, p2=0°, p3=-45°}) and never collapses into the degenerate
 * straight-then-snap pose FABRIK produces from an unbiased seed.</p>
 *
 * <p><b>Iteration budget:</b> FABRIK's convergence is linear with a rate
 * that approaches 1 at the straight-chain singularity — independent review
 * measured 21 iterations needed at a 18.0-block target, 97 at 18.5 and 275
 * at 18.56 (max reach 18.5625), where a 20-iteration budget left the foot
 * up to 0.145 blocks short. The walking gait sweeps this near-extension
 * band every stride, so the budget is sized for the worst reachable case
 * (300); typical mid-range targets still exit in ≤5 iterations via the
 * tolerance check, so the average cost is unchanged.</p>
 *
 * <p><b>Determinism:</b> pure {@code double} arithmetic, no randomness, no
 * world access. The client replay runs this same solver on the same inputs
 * and must produce bit-identical joints — do not introduce platform-varying
 * math here ({@code Math.sin/cos/sqrt/atan2} are exactly-rounded or
 * semantically specified and safe; {@code StrictMath} not required).</p>
 */
public final class PlanarFabrik {

    /**
     * Iteration cap sized for the near-extension convergence cliff (see
     * class javadoc); mid-range targets exit early on the tolerance check.
     */
    public static final int MAX_ITERATIONS = 300;
    /** Convergence tolerance on the foot-to-target distance, in blocks. */
    public static final double TOLERANCE = 0.01;
    /**
     * Upward seed rotation (radians) applied to the straightened chain before
     * iterating; keeps the knee bowing up. 45° matches the shipped rest arc.
     */
    public static final double DEFAULT_KNEE_BIAS = Math.PI / 4.0;

    private PlanarFabrik() {
    }

    /**
     * Solves a 3-segment chain of equal length {@code segmentLength} from the
     * origin to {@code (targetU, targetV)}.
     *
     * @param joints out-parameter, {@code double[4][2]}: hip, knee1, knee2,
     *               foot as {@code {u, v}}. Overwritten entirely; the hip is
     *               always {@code (0,0)} and the foot lands on the target when
     *               it is within reach (else on the closest reachable point,
     *               with the chain straight toward the target).
     */
    public static void solve(double segmentLength, double targetU, double targetV,
                             double kneeBiasRad, double[][] joints) {
        final double reach = 3.0 * segmentLength;
        final double targetDist = Math.sqrt(targetU * targetU + targetV * targetV);

        // Out of reach: the converged FABRIK answer is the straight chain
        // toward the target; emit it directly instead of iterating to it.
        if (targetDist >= reach) {
            double du = targetDist > 1.0E-9 ? targetU / targetDist : 1.0;
            double dv = targetDist > 1.0E-9 ? targetV / targetDist : 0.0;
            for (int i = 0; i <= 3; ++i) {
                joints[i][0] = du * segmentLength * i;
                joints[i][1] = dv * segmentLength * i;
            }
            return;
        }

        // Straighten-bias seed: straight chain along the target direction
        // rotated up by the knee bias. For a degenerate (near-hip) target,
        // seed pointing along +u so the fold still has a defined plane side.
        double du;
        double dv;
        if (targetDist > 1.0E-9) {
            du = targetU / targetDist;
            dv = targetV / targetDist;
        } else {
            du = 1.0;
            dv = 0.0;
        }
        final double cosB = Math.cos(kneeBiasRad);
        final double sinB = Math.sin(kneeBiasRad);
        // 2-D rotation toward +v (counterclockwise with u right, v up).
        final double su = du * cosB - dv * sinB;
        final double sv = du * sinB + dv * cosB;
        joints[0][0] = 0.0;
        joints[0][1] = 0.0;
        for (int i = 1; i <= 3; ++i) {
            joints[i][0] = su * segmentLength * i;
            joints[i][1] = sv * segmentLength * i;
        }

        for (int iter = 0; iter < MAX_ITERATIONS; ++iter) {
            // Backward pass: pin the foot to the target, walk toward the hip
            // re-normalizing each segment to its length.
            joints[3][0] = targetU;
            joints[3][1] = targetV;
            for (int i = 2; i >= 0; --i) {
                constrain(joints[i + 1], joints[i], segmentLength);
            }
            // Forward pass: pin the hip back to the origin, walk to the foot.
            joints[0][0] = 0.0;
            joints[0][1] = 0.0;
            for (int i = 1; i <= 3; ++i) {
                constrain(joints[i - 1], joints[i], segmentLength);
            }
            final double eu = joints[3][0] - targetU;
            final double ev = joints[3][1] - targetV;
            if (eu * eu + ev * ev < TOLERANCE * TOLERANCE) {
                break;
            }
        }
    }

    /**
     * Moves {@code point} onto the sphere (circle) of radius {@code length}
     * around {@code anchor}, preserving its direction from the anchor. A
     * zero-length direction (coincident points) falls back to +u so the chain
     * can never collapse to NaN.
     */
    private static void constrain(double[] anchor, double[] point, double length) {
        double du = point[0] - anchor[0];
        double dv = point[1] - anchor[1];
        double dist = Math.sqrt(du * du + dv * dv);
        if (dist < 1.0E-9) {
            du = 1.0;
            dv = 0.0;
            dist = 1.0;
        }
        point[0] = anchor[0] + du / dist * length;
        point[1] = anchor[1] + dv / dist * length;
    }
}
