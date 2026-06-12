package danger.orespawn.entity.client;

/**
 * Per-entity render-scratch holder, mirroring the original generic
 * {@code RenderInfo} (orig RenderInfo.java:6-15: four ints {@code ri1..ri4},
 * four floats {@code rf1..rf4}). The original attached one instance per entity
 * (e.g. orig Kraken.java:58 {@code renderdata = new RenderInfo()}) and let the
 * model mutate it client-side during render (orig ModelKraken.java:1045-1057),
 * so each entity kept its own animation scratch state. It was never
 * datawatcher-synced.
 */
public class RenderInfo {
    public int ri1;
    public int ri2;
    public int ri3;
    public int ri4;
    public float rf1;
    public float rf2;
    public float rf3;
    public float rf4;
}
