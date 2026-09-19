package danger.orespawn.client;

import java.util.Locale;

/**
 * Developer switch for the Phase G renderer A/B, INVERTED on the branch {@code default-flip}. Pure policy with no
 * Minecraft imports so the suite can pin it. The GeckoLib candidate is the default for every species that reaches
 * {@code PhaseGDevRenderers.select} - every landed rig; a species with no landed rig never reaches the switch and
 * keeps its classic renderer regardless, and the Queen is native. The property value is either the exact token {@value
 * #CLASSIC}, which keeps every landed rig on its classic renderer, or a comma-separated list of species ids (registry
 * names, e.g. {@code beaver,elevator}) that keep theirs, for bisecting during in-game looks. The original Beaver-named
 * property is honored as an alias with the same grammar. Master's property {@code orespawn.dev.geckolibRenderers},
 * which named the candidates while classic was the default, is not read on this branch. Not a player
 * config: it exists so the owner can review the flipped default in-game before the branch merges.
 *
 */
public final class DevRendererSwitch {
    /** The species that keep their classic renderer: the token {@value #CLASSIC}, or a comma list of species ids. */
    public static final String PROPERTY = "orespawn.dev.classicRenderers";
    public static final String BEAVER_PROPERTY = "orespawn.dev.beaverRenderer";
    /** The all-species token: every landed rig keeps its classic renderer. */
    public static final String CLASSIC = "classic";

    public enum Variant { CLASSIC, CANDIDATE }

    private DevRendererSwitch() {
    }

    /** One property value against one species id: the all-species token, or the species listed, keeps classic; otherwise the candidate. */
    public static Variant resolve(String requested, String species) {
        if (requested == null || species == null) {
            return Variant.CANDIDATE;
        }
        if (CLASSIC.equals(requested)) {
            return Variant.CLASSIC;
        }
        String wanted = species.toLowerCase(Locale.ROOT);
        for (String token : requested.split(",")) {
            if (token.trim().toLowerCase(Locale.ROOT).equals(wanted)) {
                return Variant.CLASSIC;
            }
        }
        return Variant.CANDIDATE;
    }

    /** Both properties; either one naming the species keeps it classic. */
    public static Variant resolve(String general, String beaverAlias, String species) {
        return resolve(general, species) == Variant.CLASSIC ? Variant.CLASSIC : resolve(beaverAlias, species);
    }

    public static Variant geckolib(String species) {
        return resolve(property(PROPERTY), property(BEAVER_PROPERTY), species);
    }

    /** The property that kept this species classic, for diagnostics; null when it draws with its candidate (the default). */
    public static String classicSource(String species) {
        if (resolve(property(PROPERTY), species) == Variant.CLASSIC) {
            return PROPERTY;
        }
        return resolve(property(BEAVER_PROPERTY), species) == Variant.CLASSIC ? BEAVER_PROPERTY : null;
    }

    private static String property(String name) {
        try {
            return System.getProperty(name);
        } catch (SecurityException denied) {
            return null;
        }
    }
}
