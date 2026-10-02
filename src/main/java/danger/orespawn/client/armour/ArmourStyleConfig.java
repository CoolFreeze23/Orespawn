package danger.orespawn.client.armour;

import java.util.Arrays;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The client settings, {@code config/orespawn-client.toml}: how OreSpawn's armour sets look (MOD-039). A cosmetic
 * choice, per player and never synced, so it lives here, in a client config, and outside {@code [modern] enabled}. It
 * is read at every use, so a change from the OreSpawn Visuals screen or in the file shows at once, but for Doggy Talents
 * Next's dogs: that mod keeps each piece's texture for the session, so they follow it after a restart.
 */
public final class ArmourStyleConfig {
    public static final String MODERN = "modern";
    public static final String CLASSIC = "classic";

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> ARMOUR_STYLE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("client");
        ARMOUR_STYLE = builder
                .comment("How OreSpawn's 14 armour sets look: \"modern\" (3D pieces, new textures and icons) or "
                        + "\"classic\" (the original textures and icons). A change shows at once, without a reload; "
                        + "Doggy Talents Next's dogs keep each piece's texture for the session and follow it after a "
                        + "restart.")
                .translation("orespawn.configuration.armourStyle")
                // a list that answers contains(null): the spec tests a missing value with it when it writes the
                // file for the first time, and List.of throws there
                .defineInList("armourStyle", MODERN, Arrays.asList(MODERN, CLASSIC));
        builder.pop();
        SPEC = builder.build();
    }

    private ArmourStyleConfig() {
    }

    /** Whether the modern style is on: the setting, or modern before the config has loaded. */
    public static boolean modern() {
        return !SPEC.isLoaded() || !CLASSIC.equals(ARMOUR_STYLE.get());
    }

    /** Sets the style and writes the config file. */
    public static void setModern(boolean modern) {
        ARMOUR_STYLE.set(modern ? MODERN : CLASSIC);
        ARMOUR_STYLE.save();
    }
}
