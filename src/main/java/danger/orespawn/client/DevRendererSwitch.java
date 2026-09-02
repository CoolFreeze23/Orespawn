package danger.orespawn.client;

/**
 * Developer switch for the Phase G renderer A/B. Pure policy with no
 * Minecraft imports so the suite can pin it: classic is the default, and only
 * the exact token {@value #CANDIDATE} in the JVM property selects the
 * GeckoLib candidate. Not a player config — it exists so the owner can review
 * a converted species in-game before it replaces its classic renderer.
 */
public final class DevRendererSwitch {
    public static final String BEAVER_PROPERTY = "orespawn.dev.beaverRenderer";
    public static final String CANDIDATE = "candidate";

    public enum Variant { CLASSIC, CANDIDATE }

    private DevRendererSwitch() {
    }

    public static Variant resolve(String requested) {
        return CANDIDATE.equals(requested) ? Variant.CANDIDATE : Variant.CLASSIC;
    }

    public static Variant beaver() {
        String requested;
        try {
            requested = System.getProperty(BEAVER_PROPERTY);
        } catch (SecurityException denied) {
            requested = null;
        }
        return resolve(requested);
    }
}
