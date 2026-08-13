package danger.orespawn.entity.gait;

/**
 * 2.0 spider overhaul (S5b): the AntRobot leg rig for the modern gait,
 * published as {@link #RIG}.
 *
 * <p><b>Deliberate duplication</b> (same contract as
 * {@link SpiderRigProfile}): every constant mirrors the classic ant
 * solver's tables in {@code AntRobot.initLegData} ({@code AntRobot.java:
 * 579-624}, orig :187-227) and its probe geometry: the yScan loop
 * ({@code AntRobot.java:892}) runs 8→−8, and the contraction SWEEP floor
 * is 2.5 blocks ({@code findNewFooting}'s {@code while (!found && reach >
 * 2.5f)} — the S5b independent review caught the first cut mirroring
 * 1.375 = 22 px here, which is actually {@code updateLegs}' foot-too-close
 * RELOCATION-trigger window (:856), not probe geometry; the 144 px = 9.0
 * figure is the probe's opening reach). The classic ant path is under the
 * bit-identity parity contract and is never refactored to share a table
 * with this class; the ant construction-snapshot gametest cross-checks the
 * mirror against a live classic ant's tables.</p>
 *
 * <p><b>Leg layout</b> (differs from the spider — mid/front/rear by
 * z-band, pairs MIRRORED −X/+X like the spider's): (0,1) the z≈0 mid
 * pair, (2,3) the front pair, (4,5) the rear pair. Even legs −X at yaw 0,
 * odd +X (roll contract holds); the pitch groups are
 * {mid, mid, front, front, rear, rear}.
 * REST reaches note the classic quirk: the leg-0/1 override runs AFTER the
 * ≥4 branch in the original (orig :839,862-874), so the opening reaches
 * land at 6.0 / 9.0 / 4.0 by pair — mirrored as data, quirk and all.</p>
 *
 * <p><b>Tuning</b> (design S5 as-designed): gait constants scaled
 * ×(3.0625/6.1875) ≈ 0.495 from the spider's as the starting tune —
 * trigger radii 1.0/2.5, step speed 0.55, lift 1.0, vertical retrigger
 * 1.0, dangle drop 2.0, lift/sag ±0.5 — with full speed at the ant's own
 * 0.3 movement-speed attribute and the rest-yaw chase rate lowered to
 * 2.8°/t so the worst-case rest displacement (10-block radius × 0.0489
 * rad ≈ 0.49 b/t) stays under the ant's step speed. Classic-recorded
 * numbers that stay documentation, not modern parameters: yaw trigger
 * ×8/6 and swing bias 0.8 (the modern gait uses drift radii and velocity
 * projection instead, as on the spider). NO trample: the classic ant's
 * foot landing has no block side effects. S6b (THE LEG FIX) replaced the
 * rest-yaw chase (2.8°/t — the sitting-confirmed crossing mechanism at
 * ant scale) with the zero-lag rest frame + comfort radius 3.0.</p>
 */
public final class AntRigProfile {

    public static final int LEG_COUNT = 6;

    /** One ant leg segment is a 49-pixel model box (AntRobot.java:744-746). */
    public static final double SEGMENT_LENGTH = 49.0 / 16.0;

    /** Maximum leg reach in blocks (three straight segments) = 9.1875. */
    public static final double MAX_REACH = 3.0 * SEGMENT_LENGTH;

    /** The ant rig instance the modern gait consumes. */
    public static final LegRig RIG = new LegRig(
            LEG_COUNT, SEGMENT_LENGTH,
            new double[]{0.75, 0.75, 1.0, 1.0, 1.15, 1.15},
            new double[]{0.0, Math.PI, -0.7853982, 3.9269907, 0.7853982, 2.3561945},
            new double[]{-0.75, -0.75, -0.75, -0.75, -0.75, -0.75},
            new double[]{0.2617994, -0.2617994, 0.2617994, -0.2617994, 0.2617994, -0.2617994},
            new int[]{1, 0, 3, 2, 5, 4},
            new double[]{6.0, 6.0, 9.0, 9.0, 4.0, 4.0},
            // Pitch groups: mids out, (2,3) front, (4,5) rear.
            new int[]{0, 0, 1, 1, -1, -1},
            // Classic probe geometry: yScan 8→−8, contraction SWEEP floor
            // 2.5 (the classic while-floor, same convention as the
            // spider's 3.5 — review-corrected from the 22 px relocation
            // window); reach margin 0.995 — see LegRig.reachMargin (the
            // law-bound 9.0 opening + 0.75 hip drop needs 98.3% of reach).
            8, 8, 2.5, 0.995,
            // Scaled tuning (×0.495 starting tune per the design block).
            1.0, 2.5, 0.3,
            0.55, 1.0, 1.0,
            2.0, 0.5, -0.5,
            // S6b comfort radius: > trigger max 2.5, < min lateral rest 3.64.
            3.0,
            0.2, false);

    private AntRigProfile() {
    }
}
