package danger.orespawn.bench;

import danger.orespawn.OreSpawnMod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Phase G slice (d): wires the server half of the in-game benchmark when, and only when,
 * {@code -Dorespawn.dev.bench=true} is set: the command's {@code RegisterCommandsEvent} listener and
 * the session's {@code ServerTickEvent.Post} listener. Called once from the mod constructor. Not a
 * player feature -- a review harness for the owner's live runs.
 */
public final class BenchHarness {

    private static boolean installed;

    private BenchHarness() {
    }

    public static synchronized void init() {
        if (installed || !BenchCommand.enabled()) {
            return;
        }
        installed = true;
        OreSpawnMod.LOGGER.warn("Phase G dev bench enabled (-D{}=true): /orespawn bench is registered for op level {}; this is a review harness, not a production feature.",
                BenchCommand.PROPERTY, BenchCommand.PERMISSION_LEVEL);
        NeoForge.EVENT_BUS.addListener(BenchCommand::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(BenchSession::onServerTick);
    }

    public static synchronized boolean installed() {
        return installed;
    }
}
