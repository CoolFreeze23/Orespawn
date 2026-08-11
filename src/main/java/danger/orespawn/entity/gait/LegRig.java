package danger.orespawn.entity.gait;

import net.minecraft.util.Mth;

/**
 * 2.0 spider overhaul (S5b): one legged-robot rig for the modern gait —
 * the per-leg classic tables, the rig's CLASSIC PROBE GEOMETRY, and the
 * gait tuning that scales with rig size. {@link ModernSpiderGait} holds one
 * of these; {@link SpiderRigProfile} and {@link AntRigProfile} each publish
 * their robot's instance built from that robot's classic solver verbatim
 * (scan-window law: probe numbers are classic geometry per rig, never
 * shared, never tuned).
 *
 * <p><b>Leg indexing contract</b> (both shipped rigs satisfy it; a future
 * rig must too or the gait's group logic needs revisiting): even legs sit
 * on the −X side at yaw 0 and odd legs on +X (the roll split), and the
 * step-inhibitor walk treats legs ±2 as same-side neighbors — an INDEX
 * adjacency: on the ant's mid/front/rear ordering it inhibits mid↔front
 * and front↔rear but not the spatially adjacent mid↔rear, so one side's
 * mid and rear legs may co-swing (review-verified bounded and
 * deadlock-free — pairs never co-swing, ≥3 of 6 legs stay planted, and
 * the classic ant's own scheduler was far looser; accepted as tuning).
 * The PITCH
 * split is per-rig data ({@link #pitchGroup}) because the two shipped rigs
 * index differently: the spider runs front-to-rear (legs 0-3 front), while
 * the ant runs mid/front/rear (legs 2/3 are the front pair, 0/1 the
 * z≈0 mid pair — mids sit OUT of the pitch centroids, value 0).</p>
 *
 * <p>{@link #pitchSpan()}/{@link #rollSpan()} are derived in the ctor from
 * the rig's own rest stance at yaw 0 (S3b review: a shared magic span
 * over-read slopes by 1.4-1.5x), exactly as the S3b static block did for
 * the spider — same groups, same centroids, same numbers.</p>
 */
public final class LegRig {

    private final int legCount;
    private final double segmentLength;
    private final double maxReach;
    private final double[] hipRadial;
    private final double[] neutralYaw;
    private final double[] hipVertical;
    private final double[] swingRange;
    private final int[] pairedWith;
    private final double[] restReach;
    /** Pitch-centroid membership: +1 front, −1 rear, 0 excluded (mid legs). */
    private final int[] pitchGroup;

    // Classic probe geometry (per-rig law). Derivation-convention note
    // (S5b review adjudication): the ANT's scanUp/scanDown 8/8 are the
    // exact foot-height-space translation of its classic yScan loop; the
    // SPIDER's 11/14 are the historical loop-literal reading shipped and
    // gated since S3a — a future rig should copy the ant's derivation.
    // minContractedReach is the classic probe sweep's WHILE-FLOOR (spider
    // 3.5, ant 2.5), never a relocation/comfort window.
    private final int scanUp;
    private final int scanDown;
    private final double minContractedReach;
    /**
     * Modern-only reach-guard fraction (NOT probe geometry — the guard
     * protects the exact-IK solve, which classic never had). Spider: 0.98
     * as shipped since S3. Ant: 0.995 — its law-bound 9.0 probe opening
     * plus the −0.75 hip drop is a 9.03 hip→foot distance, 98.3% of max
     * reach, so the spider's 0.98 cap would reject the ant's own classic
     * rest stance; the harness grid proves FABRIK convergence there.
     */
    private final double reachMargin;

    // Rig-size-scaled gait tuning (design: spider values as S2-S3 shipped;
    // ant scaled ~x(3.0625/6.1875) as the starting tune).
    private final double triggerRadiusMin;
    private final double triggerRadiusMax;
    private final double fullSpeed;
    private final double stepSpeed;
    private final double liftHeight;
    private final double verticalRetrigger;
    private final double dangleDrop;
    private final double maxLift;
    private final double maxSag;
    private final float restYawDeadbandDeg;
    private final float restYawRateDeg;
    private final double partHalfHeight;
    /**
     * Whether settled ridden feet run the classic trample (SHORT_GRASS /
     * GRASS_BLOCK). Spider: yes — classic trampled client-side. Ant: NO —
     * classic ant foot landing has no block side effects (AntRobot parity),
     * so the modern rig must not invent one.
     */
    private final boolean tramples;

    // Derived body-dynamics spans (rest stance at yaw 0).
    private final double pitchSpan;
    private final double rollSpan;

    public LegRig(int legCount, double segmentLength,
                  double[] hipRadial, double[] neutralYaw, double[] hipVertical,
                  double[] swingRange, int[] pairedWith, double[] restReach,
                  int[] pitchGroup,
                  int scanUp, int scanDown, double minContractedReach, double reachMargin,
                  double triggerRadiusMin, double triggerRadiusMax, double fullSpeed,
                  double stepSpeed, double liftHeight, double verticalRetrigger,
                  double dangleDrop, double maxLift, double maxSag,
                  float restYawDeadbandDeg, float restYawRateDeg,
                  double partHalfHeight, boolean tramples) {
        this.legCount = legCount;
        this.segmentLength = segmentLength;
        this.maxReach = 3.0 * segmentLength;
        this.hipRadial = hipRadial;
        this.neutralYaw = neutralYaw;
        this.hipVertical = hipVertical;
        this.swingRange = swingRange;
        this.pairedWith = pairedWith;
        this.restReach = restReach;
        this.pitchGroup = pitchGroup;
        this.scanUp = scanUp;
        this.scanDown = scanDown;
        this.minContractedReach = minContractedReach;
        this.reachMargin = reachMargin;
        this.triggerRadiusMin = triggerRadiusMin;
        this.triggerRadiusMax = triggerRadiusMax;
        this.fullSpeed = fullSpeed;
        this.stepSpeed = stepSpeed;
        this.liftHeight = liftHeight;
        this.verticalRetrigger = verticalRetrigger;
        this.dangleDrop = dangleDrop;
        this.maxLift = maxLift;
        this.maxSag = maxSag;
        this.restYawDeadbandDeg = restYawDeadbandDeg;
        this.restYawRateDeg = restYawRateDeg;
        this.partHalfHeight = partHalfHeight;
        this.tramples = tramples;

        // Rest-stance centroid spans, same per-term division order as the
        // S3b static block (spider numbers bit-identical).
        int frontCount = 0;
        int rearCount = 0;
        for (int leg = 0; leg < legCount; ++leg) {
            if (pitchGroup[leg] > 0) {
                ++frontCount;
            } else if (pitchGroup[leg] < 0) {
                ++rearCount;
            }
        }
        int lateralHalf = legCount / 2;
        double frontAxis = 0.0;
        double rearAxis = 0.0;
        double evenAxis = 0.0;
        double oddAxis = 0.0;
        for (int leg = 0; leg < legCount; ++leg) {
            double z = restFootZ(leg, 0.0, 0.0f);
            double x = restFootX(leg, 0.0, 0.0f);
            if (pitchGroup[leg] > 0) {
                frontAxis += z / frontCount;
            } else if (pitchGroup[leg] < 0) {
                rearAxis += z / rearCount;
            }
            if ((leg & 1) == 0) {
                evenAxis += x / lateralHalf;
            } else {
                oddAxis += x / lateralHalf;
            }
        }
        this.pitchSpan = Math.abs(frontAxis - rearAxis);
        this.rollSpan = Math.abs(oddAxis - evenAxis);
    }

    public int pitchGroup(int leg) {
        return pitchGroup[leg];
    }

    public double reachMargin() {
        return reachMargin;
    }

    public int legCount() {
        return legCount;
    }

    public double segmentLength() {
        return segmentLength;
    }

    public double maxReach() {
        return maxReach;
    }

    public double hipRadial(int leg) {
        return hipRadial[leg];
    }

    public double neutralYaw(int leg) {
        return neutralYaw[leg];
    }

    public double hipVertical(int leg) {
        return hipVertical[leg];
    }

    public double swingRange(int leg) {
        return Math.abs(swingRange[leg]);
    }

    public int pairedWith(int leg) {
        return pairedWith[leg];
    }

    public double restReach(int leg) {
        return restReach[leg];
    }

    public int scanUp() {
        return scanUp;
    }

    public int scanDown() {
        return scanDown;
    }

    public double minContractedReach() {
        return minContractedReach;
    }

    public double fullSpeed() {
        return fullSpeed;
    }

    public double stepSpeed() {
        return stepSpeed;
    }

    public double liftHeight() {
        return liftHeight;
    }

    public double verticalRetrigger() {
        return verticalRetrigger;
    }

    public double dangleDrop() {
        return dangleDrop;
    }

    public double maxLift() {
        return maxLift;
    }

    public double maxSag() {
        return maxSag;
    }

    public float restYawDeadbandDeg() {
        return restYawDeadbandDeg;
    }

    public float restYawRateDeg() {
        return restYawRateDeg;
    }

    public double partHalfHeight() {
        return partHalfHeight;
    }

    public boolean tramples() {
        return tramples;
    }

    public double pitchSpan() {
        return pitchSpan;
    }

    public double rollSpan() {
        return rollSpan;
    }

    /** The speed-widened trigger radius (pure; exposed for the gait tests). */
    public double triggerRadius(double bodySpeed) {
        double speedFrac = Math.min(1.0, bodySpeed / fullSpeed);
        return Mth.lerp(speedFrac, triggerRadiusMin, triggerRadiusMax);
    }

    /**
     * The classic hip/probe bearing for a leg: {@code toRadians(wrapDegrees(
     * yawDeg + 90)) + ymid}, reproduced from the classic solvers
     * ({@code SpiderRobot.java:516-519}/{@code :712-715};
     * {@code AntRobot.java:294-296} is the structurally identical ant form).
     * Positions derived from it use the classic axis convention
     * {@code x -= r*sin(theta)}, {@code z += r*cos(theta)}.
     */
    public double legBearing(int leg, float bodyYawDeg) {
        return Math.toRadians(Mth.wrapDegrees((double) (bodyYawDeg + 90.0f))) + neutralYaw[leg];
    }

    /** Hip socket world X (classic formula). */
    public double hipX(int leg, double bodyX, float bodyYawDeg) {
        return bodyX - hipRadial[leg] * Math.sin(legBearing(leg, bodyYawDeg));
    }

    /** Hip socket world Z (classic formula). */
    public double hipZ(int leg, double bodyZ, float bodyYawDeg) {
        return bodyZ + hipRadial[leg] * Math.cos(legBearing(leg, bodyYawDeg));
    }

    /** Hip socket world Y (classic formula). */
    public double hipY(int leg, double bodyY) {
        return bodyY + hipVertical[leg];
    }

    /** Rest foot target X: the hip pushed {@link #restReach} further along the bearing. */
    public double restFootX(int leg, double bodyX, float bodyYawDeg) {
        return hipX(leg, bodyX, bodyYawDeg) - restReach[leg] * Math.sin(legBearing(leg, bodyYawDeg));
    }

    /** Rest foot target Z; see {@link #restFootX}. */
    public double restFootZ(int leg, double bodyZ, float bodyYawDeg) {
        return hipZ(leg, bodyZ, bodyYawDeg) + restReach[leg] * Math.cos(legBearing(leg, bodyYawDeg));
    }
}
