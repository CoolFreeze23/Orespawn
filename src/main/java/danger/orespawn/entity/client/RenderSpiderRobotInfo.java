package danger.orespawn.entity.client;

public class RenderSpiderRobotInfo {
    public float[] ydisplayangle;
    public double[] p1xangle;
    public double[] p2xangle;
    public double[] p3xangle;
    public float[] uddisplayangle;
    public double[] ymid;
    public float[] legoff;
    public float[] yoff;
    public int gpcounter;

    public RenderSpiderRobotInfo(int legCount) {
        ydisplayangle = new float[legCount];
        p1xangle = new double[legCount];
        p2xangle = new double[legCount];
        p3xangle = new double[legCount];
        uddisplayangle = new float[legCount];
        ymid = new double[legCount];
        legoff = new float[legCount];
        yoff = new float[legCount];
        gpcounter = 0;
    }
}
