package danger.orespawn.client;

/**
 * Developer switch for the Phase G renderer A/B. Pure policy with no
 * Minecraft imports so the suite can pin it: classic is the default, and only
 * the exact token {@value #CANDIDATE} selects the GeckoLib candidates. One
 * property covers every converted species; the original Beaver-named property
 * is still honored so existing launch configs keep working. Not a player
 * config — it exists so the owner can review converted species in-game before
 * any of them replaces its classic renderer.
 */
public final class DevRendererSwitch {
    public static final String PROPERTY = "orespawn.dev.geckolibRenderers";
    public static final String BEAVER_PROPERTY = "orespawn.dev.beaverRenderer";
    public static final String CANDIDATE = "candidate";

    public enum Variant { CLASSIC, CANDIDATE }

    private DevRendererSwitch() {
    }

    public static Variant resolve(String requested) {
        return CANDIDATE.equals(requested) ? Variant.CANDIDATE : Variant.CLASSIC;
    }

    public static Variant resolve(String general, String beaverAlias) {
        return resolve(general) == Variant.CANDIDATE ? Variant.CANDIDATE : resolve(beaverAlias);
    }

    public static Variant geckolib() {
        return resolve(property(PROPERTY), property(BEAVER_PROPERTY));
    }

    /** The property that selected the candidate, for diagnostics; null when classic. */
    public static String candidateSource() {
        if (resolve(property(PROPERTY)) == Variant.CANDIDATE) {
            return PROPERTY;
        }
        return resolve(property(BEAVER_PROPERTY)) == Variant.CANDIDATE ? BEAVER_PROPERTY : null;
    }

    /** Kept for the Beaver review build; identical to {@link #geckolib()}. */
    public static Variant beaver() {
        return geckolib();
    }

    private static String property(String name) {
        try {
            return System.getProperty(name);
        } catch (SecurityException denied) {
            return null;
        }
    }
}
