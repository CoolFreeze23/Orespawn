package danger.orespawn.entity.gait;

/**
 * 2.0 spider overhaul (S2, rig-abstracted in S5b): the SpiderRobot leg rig
 * for the modern (server-side) gait solver, published as {@link #RIG} and
 * mirrored by the static facade the S2-S4 tests and call sites use.
 *
 * <p><b>Deliberate duplication:</b> every constant here mirrors the classic
 * solver's per-leg tables in {@code SpiderRobot.initLegData}
 * ({@code SpiderRobot.java:404-421}) and its probe geometry
 * ({@code findNewFooting}, {@code SpiderRobot.java:666-745}: yScan 11→−14,
 * reach sweep 16→3.5). The classic path is under the bit-identity parity
 * contract and may not be refactored to share a table with this class — if
 * the classic values ever change (they must not), this mirror must be
 * updated by hand. The {@code s2_movement_mode_construction_snapshot}
 * gametest cross-checks the mirror against a live classic spider's tables.</p>
 *
 * <p><b>Leg indexing</b> (unchanged from classic): legs form mirrored pairs
 * — even legs sit on the −X side at yaw 0, odd on +X (avoid left/right
 * labels: at Minecraft yaw 0 the entity faces +Z, making +X its LEFT) —
 * front to rear: (0,1) front,
 * (2,3) mid-front, (4,5) mid-rear, (6,7) rear. {@code neutralYaw} is the
 * leg's resting direction as a body-relative bearing <i>offset added inside
 * the classic hip formula</i>; the {@link LegRig} formulas reproduce that
 * formula exactly rather than re-deriving a cleaner equivalent.</p>
 *
 * <p>Gait tuning riding on the rig (S5b): trigger radii 2.0/5.0 at full
 * speed 0.35, step speed 1.1, lift 2.0, vertical retrigger 2.0, dangle
 * drop 4.0, lift/sag ±1.0, rest-yaw dead-band 8.6°/3.4°·t — exactly the
 * S2-S5a shipped constants, moved, not retuned. S6b (THE LEG FIX)
 * replaced the S5a rest-yaw dead-band chase with the zero-lag rest
 * frame + comfort radius 6.0 (see LegRig).</p>
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

    /** The spider rig instance the modern gait consumes. */
    public static final LegRig RIG = new LegRig(
            LEG_COUNT, SEGMENT_LENGTH,
            // legoff / ymid / yoff / yrange / pairedwith / probe opening reach
            new double[]{1.25, 1.25, 2.0, 2.0, 1.75, 1.75, 3.4, 3.4},
            new double[]{-0.32, 3.4615927, -1.0, 4.1415925, 0.62831855, 2.5132742, 1.05, 2.0915928},
            new double[]{-0.3, -0.3, -0.1, -0.1, -0.3, -0.3, -0.1, -0.1},
            new double[]{0.2617994, -0.2617994, 0.2617994, -0.2617994,
                    0.2617994, -0.2617994, 0.2617994, -0.2617994},
            new int[]{1, 0, 3, 2, 5, 4, 7, 6},
            new double[]{16.0, 16.0, 16.0, 16.0, 10.0, 10.0, 10.0, 10.0},
            // Pitch groups: index-ordered front-to-rear (legs 0-3 front).
            new int[]{1, 1, 1, 1, -1, -1, -1, -1},
            // Classic probe geometry (scan-window law): yScan 11→−14, floor
            // 3.5; reach-guard margin 0.98 as shipped (modern tuning).
            11, 14, 3.5, 0.98,
            // S2-S5a shipped tuning, moved verbatim.
            2.0, 5.0, 0.35,
            1.1, 2.0, 2.0,
            4.0, 1.0, -1.0,
            // S6b comfort radius: > trigger max 5.0, < min lateral rest 6.67.
            6.0,
            0.3, true);

    private SpiderRigProfile() {
    }

    public static double hipRadial(int leg) {
        return RIG.hipRadial(leg);
    }

    public static double neutralYaw(int leg) {
        return RIG.neutralYaw(leg);
    }

    public static double hipVertical(int leg) {
        return RIG.hipVertical(leg);
    }

    public static double swingRange(int leg) {
        return RIG.swingRange(leg);
    }

    public static int pairedWith(int leg) {
        return RIG.pairedWith(leg);
    }

    public static double restReach(int leg) {
        return RIG.restReach(leg);
    }

    /** See {@link LegRig#legBearing}. */
    public static double legBearing(int leg, float bodyYawDeg) {
        return RIG.legBearing(leg, bodyYawDeg);
    }

    /** Hip socket world X (classic SpiderRobot.java:516-517). */
    public static double hipX(int leg, double bodyX, float bodyYawDeg) {
        return RIG.hipX(leg, bodyX, bodyYawDeg);
    }

    /** Hip socket world Z (classic SpiderRobot.java:518-519). */
    public static double hipZ(int leg, double bodyZ, float bodyYawDeg) {
        return RIG.hipZ(leg, bodyZ, bodyYawDeg);
    }

    /** Hip socket world Y (classic SpiderRobot.java:520). */
    public static double hipY(int leg, double bodyY) {
        return RIG.hipY(leg, bodyY);
    }

    /** Rest foot target X (the classic un-biased probe aim, SpiderRobot.java:712-716). */
    public static double restFootX(int leg, double bodyX, float bodyYawDeg) {
        return RIG.restFootX(leg, bodyX, bodyYawDeg);
    }

    /** Rest foot target Z; see {@link #restFootX}. */
    public static double restFootZ(int leg, double bodyZ, float bodyYawDeg) {
        return RIG.restFootZ(leg, bodyZ, bodyYawDeg);
    }
}
