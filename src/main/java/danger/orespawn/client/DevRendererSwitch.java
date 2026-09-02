package danger.orespawn.client;

import java.util.Locale;

/**
 * Developer switch for the Phase G renderer A/B. Pure policy with no
 * Minecraft imports so the suite can pin it. Classic is the default. The
 * property value is either the exact token {@value #CANDIDATE}, which
 * selects every GeckoLib candidate, or a comma-separated list of species ids
 * (registry names, e.g. {@code beaver,elevator}) for bisecting during in-game
 * looks. The original Beaver-named property is honored as an alias with the
 * same grammar. Not a player config: it exists so the owner can review
 * converted species in-game before any of them replaces its classic renderer.
 */
public final class DevRendererSwitch {
    public static final String PROPERTY = "orespawn.dev.geckolibRenderers";
    public static final String BEAVER_PROPERTY = "orespawn.dev.beaverRenderer";
    public static final String CANDIDATE = "candidate";

    public enum Variant { CLASSIC, CANDIDATE }

    private DevRendererSwitch() {
    }

    /** One property value against one species id: the all-species token, or the species listed. */
    public static Variant resolve(String requested, String species) {
        if (requested == null || species == null) {
            return Variant.CLASSIC;
        }
        if (CANDIDATE.equals(requested)) {
            return Variant.CANDIDATE;
        }
        String wanted = species.toLowerCase(Locale.ROOT);
        for (String token : requested.split(",")) {
            if (token.trim().toLowerCase(Locale.ROOT).equals(wanted)) {
                return Variant.CANDIDATE;
            }
        }
        return Variant.CLASSIC;
    }

    /** Both properties; either one selecting the species wins. */
    public static Variant resolve(String general, String beaverAlias, String species) {
        return resolve(general, species) == Variant.CANDIDATE ? Variant.CANDIDATE : resolve(beaverAlias, species);
    }

    public static Variant geckolib(String species) {
        return resolve(property(PROPERTY), property(BEAVER_PROPERTY), species);
    }

    /** The property that selected this species' candidate, for diagnostics; null when classic. */
    public static String candidateSource(String species) {
        if (resolve(property(PROPERTY), species) == Variant.CANDIDATE) {
            return PROPERTY;
        }
        return resolve(property(BEAVER_PROPERTY), species) == Variant.CANDIDATE ? BEAVER_PROPERTY : null;
    }

    private static String property(String name) {
        try {
            return System.getProperty(name);
        } catch (SecurityException denied) {
            return null;
        }
    }
}
