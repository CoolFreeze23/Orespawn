package danger.orespawn.entity.client;

/**
 * Per-entity scratch state for the multi-legged robots' procedural gait
 * solver. Ports {@code orig RenderSpiderRobotInfo.java:6-34} field-for-field;
 * the solver itself lives on the entities ({@code SpiderRobot.updateLegs},
 * {@code AntRobot.updateLegs}), exactly as in 1.7.10 where the entity owned
 * both the data and the math and the model only consumed the display fields.
 *
 * <p>Sides: written on the CLIENT only ({@code updateLegs} runs in the
 * client tick, matching the original's {@code isRemote} gate) and read by the
 * model on the render thread. The original declared the display fields
 * {@code volatile} for that same producer/consumer split; kept.</p>
 *
 * <p>The original hardcoded 8-slot arrays even for the 6-legged AntRobot
 * (which simply left slots 6-7 untouched); the port sizes the arrays by leg
 * count instead — same live data, no behavior difference.</p>
 *
 * <p>Field naming preserved from the original for 1:1 traceability with the
 * ported solver math:</p>
 * <ul>
 *   <li>{@code y*} — leg yaw (swing around the body): wanted/current/display
 *       angle, angular velocity, per-leg neutral direction ({@code ymid}),
 *       swing range and vertical hip offset ({@code yoff})</li>
 *   <li>{@code ud*} — leg elevation (up/down tilt of the whole leg)</li>
 *   <li>{@code p1x/p2x/p3xangle} — the three leg-segment pitch angles solved
 *       so the foot reaches its planted position; {@code pxvelocity} is the
 *       shared fold/unfold rate</li>
 *   <li>{@code foot_*pos} — world position the foot is currently planted at;
 *       {@code realpos*} — where the hip socket currently sits in the world</li>
 *   <li>{@code footup}/{@code uppoint} — mid-step lift state when relocating
 *       a foot; {@code footingticker}/{@code pairedwith} — alternating-pair
 *       step scheduler</li>
 *   <li>{@code gpcounter} — client frame counter (drives the jaw snap)</li>
 * </ul>
 */
public class RenderSpiderRobotInfo {
    public volatile float[] ydisplayangle;
    public float[] ywantedangle;
    public float[] ycurrentangle;
    public float[] yvelocity;
    public float[] ymid;
    public float[] yoff;
    public volatile float[] yrange;
    public volatile float[] uddisplayangle;
    public float[] udwantedangle;
    public float[] udcurrentangle;
    public float[] udvelocity;
    public volatile double[] p1xangle;
    public volatile double[] p2xangle;
    public volatile double[] p3xangle;
    public float[] pxvelocity;
    public float[] foot_xpos;
    public float[] foot_ypos;
    public float[] foot_zpos;
    public float[] legoff;
    public float[] realposx;
    public float[] realposy;
    public float[] realposz;
    public int[] footup;
    public float[] uppoint;
    public int[] footingticker;
    public int[] pairedwith;
    public int gpcounter;

    public RenderSpiderRobotInfo(int legCount) {
        ydisplayangle = new float[legCount];
        ywantedangle = new float[legCount];
        ycurrentangle = new float[legCount];
        yvelocity = new float[legCount];
        ymid = new float[legCount];
        yoff = new float[legCount];
        yrange = new float[legCount];
        uddisplayangle = new float[legCount];
        udwantedangle = new float[legCount];
        udcurrentangle = new float[legCount];
        udvelocity = new float[legCount];
        p1xangle = new double[legCount];
        p2xangle = new double[legCount];
        p3xangle = new double[legCount];
        pxvelocity = new float[legCount];
        foot_xpos = new float[legCount];
        foot_ypos = new float[legCount];
        foot_zpos = new float[legCount];
        legoff = new float[legCount];
        realposx = new float[legCount];
        realposy = new float[legCount];
        realposz = new float[legCount];
        footup = new int[legCount];
        uppoint = new float[legCount];
        footingticker = new int[legCount];
        pairedwith = new int[legCount];
        gpcounter = 0;
    }
}
