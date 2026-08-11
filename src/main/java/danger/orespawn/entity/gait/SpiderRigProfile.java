package danger.orespawn.entity.gait;

import net.minecraft.util.Mth;

/**
 * 2.0 spider overhaul (S2): the SpiderRobot leg rig as static data for the
 * modern (server-side) gait solver.
 *
 * <p><b>Deliberate duplication:</b> every constant here mirrors the classic
 * solver's per-leg tables in {@code SpiderRobot.initLegData}
 * ({@code SpiderRobot.java:404-421}) and its probe geometry
 * ({@code findNewFooting}, {@code SpiderRobot.java:666-745}). The classic
 * path is under the bit-identity parity contract and may not be refactored
 * to share a table with this class — if the classic values ever change
 * (they must not), this mirror must be updated by hand. The
 * {@code s2_movement_mode_construction_snapshot} gametest cross-checks the
 * mirror against a live classic spider's tables.</p>
 *
 * <p><b>Leg indexing</b> (unchanged from classic): legs form mirrored pairs
 * — even = left of body, odd = right — front to rear: (0,1) front,
 * (2,3) mid-front, (4,5) mid-rear, (6,7) rear. {@code neutralYawRad} is the
 * leg's resting direction as a body-relative bearing <i>offset added inside
 * the classic hip formula</i>; all uses below reproduce that formula
 * exactly rather than re-deriving a cleaner equivalent.</p>
 */
public final class SpiderRigProfile {

    public static final int LEG_COUNT = 8;

    /**
     * One leg segment is a 99-pixel model box at the standard 1/16 block
     * scale ({@code SpiderRobot.java:556-558} projects reach as
     * {@code 99*cos(...)} against distances scaled ×16).
     */
    public static final double SEGMENT_LENGTH = 99.0 / 16.0;

    /** Maximum leg reach in blocks (three straight segments). */
    public static final double MAX_REACH = 3.0 * SEGMENT_LENGTH;

    /** Radial hip offset from the body center, blocks (classic legoff). */
    private static final double[] HIP_RADIAL = {1.25, 1.25, 2.0, 2.0, 1.75, 1.75, 3.4, 3.4};

    /** Leg neutral bearing offset, radians (classic ymid). */
    private static final double[] NEUTRAL_YAW = {
            -0.32, 3.4615927, -1.0, 4.1415925, 0.62831855, 2.5132742, 1.05, 2.0915928};

    /** Hip height offset from the body origin, blocks (classic yoff). */
    private static final double[] HIP_VERTICAL = {-0.3, -0.3, -0.1, -0.1, -0.3, -0.3, -0.1, -0.1};

    /**
     * Swing half-range, radians (classic yrange, ±15°). The sign encodes the
     * mirror side exactly as in classic; magnitude is what the gait uses.
     */
    private static final double[] SWING_RANGE = {
            0.2617994, -0.2617994, 0.2617994, -0.2617994,
            0.2617994, -0.2617994, 0.2617994, -0.2617994};

    /** Alternating-pair partner (classic pairedwith). */
    private static final int[] PAIRED_WITH = {1, 0, 3, 2, 5, 4, 7, 6};

    /**
     * Rest-pose horizontal distance from hip to foot, blocks — the classic
     * probe's opening reach ({@code SpiderRobot.java:667,690-692}: 16 for
     * legs 0-3, 10 for legs 4-7), which is where the classic foot lands on
     * open flat ground and therefore the stance silhouette to preserve.
     */
    private static final double[] REST_REACH = {16.0, 16.0, 16.0, 16.0, 10.0, 10.0, 10.0, 10.0};

    private SpiderRigProfile() {
    }

    public static double hipRadial(int leg) {
        return HIP_RADIAL[leg];
    }

    public static double neutralYaw(int leg) {
        return NEUTRAL_YAW[leg];
    }

    public static double hipVertical(int leg) {
        return HIP_VERTICAL[leg];
    }

    public static double swingRange(int leg) {
        return Math.abs(SWING_RANGE[leg]);
    }

    public static int pairedWith(int leg) {
        return PAIRED_WITH[leg];
    }

    public static double restReach(int leg) {
        return REST_REACH[leg];
    }

    /**
     * The classic hip/probe bearing for a leg: {@code toRadians(wrapDegrees(
     * yawDeg + 90)) + ymid}, reproduced from {@code SpiderRobot.java:516-519}
     * (hip) and {@code :712-715} (probe — same expression). Positions derived
     * from it use the classic axis convention {@code x -= r*sin(theta)},
     * {@code z += r*cos(theta)}.
     */
    public static double legBearing(int leg, float bodyYawDeg) {
        return Math.toRadians(Mth.wrapDegrees((double) (bodyYawDeg + 90.0f))) + NEUTRAL_YAW[leg];
    }

    /** Hip socket world X (classic SpiderRobot.java:516-517). */
    public static double hipX(int leg, double bodyX, float bodyYawDeg) {
        return bodyX - HIP_RADIAL[leg] * Math.sin(legBearing(leg, bodyYawDeg));
    }

    /** Hip socket world Z (classic SpiderRobot.java:518-519). */
    public static double hipZ(int leg, double bodyZ, float bodyYawDeg) {
        return bodyZ + HIP_RADIAL[leg] * Math.cos(legBearing(leg, bodyYawDeg));
    }

    /** Hip socket world Y (classic SpiderRobot.java:520). */
    public static double hipY(int leg, double bodyY) {
        return bodyY + HIP_VERTICAL[leg];
    }

    /**
     * Rest foot target X: the hip pushed {@link #restReach} further along the
     * same bearing (the classic un-biased probe aim, SpiderRobot.java:712-716).
     */
    public static double restFootX(int leg, double bodyX, float bodyYawDeg) {
        return hipX(leg, bodyX, bodyYawDeg) - REST_REACH[leg] * Math.sin(legBearing(leg, bodyYawDeg));
    }

    /** Rest foot target Z; see {@link #restFootX}. */
    public static double restFootZ(int leg, double bodyZ, float bodyYawDeg) {
        return hipZ(leg, bodyZ, bodyYawDeg) + REST_REACH[leg] * Math.cos(legBearing(leg, bodyYawDeg));
    }
}
