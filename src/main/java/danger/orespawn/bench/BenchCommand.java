package danger.orespawn.bench;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import danger.orespawn.OreSpawnMod;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Phase G slice (d): the dev command {@code /orespawn bench ...}, registered on
 * {@code RegisterCommandsEvent} ONLY when {@code -Dorespawn.dev.bench=true} is set
 * ({@link BenchHarness#init} adds the listener under that property; without it no {@code orespawn}
 * literal reaches the dispatcher), permission level 2 on the {@code bench} node.
 * <ul>
 *   <li>{@code scene <A-F> [count] [idle|wander]} -- spawns the scene relative to the player
 *       (despawning any previous scene); count defaults to the scene's 100.</li>
 *   <li>{@code start <seconds>} -- begins sampling on both halves for that many seconds.</li>
 *   <li>{@code stop} -- aborts an unfinished run and despawns the scene (the last finished run stays
 *       reportable).</li>
 *   <li>{@code report [label]} -- writes the last finished run to
 *       {@code phase_g_reports/benchmark/live/<scene>_<label>_<timestamp>.json} and {@code .md},
 *       pairing classic and candidate when both exist; the label defaults to the dev switch's
 *       state for the scene's species.</li>
 *   <li>{@code status} -- what the session holds.</li>
 * </ul>
 * {@link #register(CommandDispatcher)} is what the game tests call on a fresh dispatcher.
 */
public final class BenchCommand {

    public static final String PROPERTY = "orespawn.dev.bench";
    public static final int PERMISSION_LEVEL = 2;
    public static final int MAX_COUNT = 1000;
    public static final int MAX_SECONDS = 3600;

    private BenchCommand() {
    }

    /** Read at every call (not a constant), so the game tests can reason about it without a JVM flag. */
    public static boolean enabled() {
        try {
            return Boolean.getBoolean(PROPERTY);
        } catch (SecurityException denied) {
            return false;
        }
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    /** Builds {@code orespawn/bench/{scene,start,stop,report,status}} into {@code dispatcher}. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("orespawn")
                .then(Commands.literal("bench")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .then(Commands.literal("scene")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> scene(ctx, -1, null))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_COUNT))
                                                .executes(ctx -> scene(ctx, IntegerArgumentType.getInteger(ctx, "count"), null))
                                                .then(Commands.argument("state", StringArgumentType.word())
                                                        .executes(ctx -> scene(ctx, IntegerArgumentType.getInteger(ctx, "count"),
                                                                StringArgumentType.getString(ctx, "state")))))))
                        .then(Commands.literal("start")
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(1, MAX_SECONDS))
                                        .executes(ctx -> start(ctx, IntegerArgumentType.getInteger(ctx, "seconds")))))
                        .then(Commands.literal("stop").executes(BenchCommand::stop))
                        .then(Commands.literal("report")
                                .executes(ctx -> report(ctx, null))
                                .then(Commands.argument("label", StringArgumentType.word())
                                        .executes(ctx -> report(ctx, StringArgumentType.getString(ctx, "label")))))
                        .then(Commands.literal("status").executes(BenchCommand::status))));
    }

    private static int scene(CommandContext<CommandSourceStack> ctx, int count, String stateToken) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        BenchScene scene = BenchScene.parse(StringArgumentType.getString(ctx, "id"));
        if (scene == null) {
            source.sendFailure(Component.literal("bench: unknown scene; use A, B, C, D, E or F"));
            return 0;
        }
        BenchState state = stateToken == null ? BenchState.IDLE : BenchState.parse(stateToken);
        if (state == null) {
            source.sendFailure(Component.literal("bench: unknown state; use idle or wander"));
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        int wanted = count > 0 ? count : scene.defaultCount();
        int capacity = BenchSceneSpawner.capacity(scene);
        if (wanted > capacity) {
            source.sendFailure(Component.literal("bench: scene " + scene.name() + " holds at most " + capacity + " mobs within "
                    + (int) BenchSceneSpawner.MAX_DISTANCE_BLOCKS + " blocks of the camera (the " + (int) (2 * BenchSceneSpawner.WEDGE_HALF_ANGLE_DEGREES)
                    + "-degree wedge at a " + (int) scene.spacing() + "-block pitch); asked for " + wanted));
            return 0;
        }
        String message = BenchSession.get().spawnScene(source.getLevel(), player.position(), player.getYRot(), scene, wanted, state);
        source.sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> ctx, int seconds) {
        String message = BenchSession.get().start(ctx.getSource().getServer(), seconds);
        ctx.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        String message = BenchSession.get().stop();
        ctx.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        String message = BenchSession.get().status();
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    private static int report(CommandContext<CommandSourceStack> ctx, String labelArg) {
        CommandSourceStack source = ctx.getSource();
        BenchServerResult result = BenchSession.get().lastResult();
        if (result == null) {
            source.sendFailure(Component.literal("bench: no finished run to report -- /orespawn bench start <seconds> and wait for it to finish"));
            return 0;
        }
        if (BenchSession.get().isRunning()) {
            source.sendFailure(Component.literal("bench: a run is in progress; wait for it to finish (or stop it) before reporting"));
            return 0;
        }
        String label = labelArg != null ? labelArg : BenchReport.variantFor(result.scene().species());
        BenchClientSnapshot client = BenchClientBridge.snapshot();
        Path root = BenchGit.repositoryRoot();
        String head = BenchGit.head(root);
        String workingTree = BenchGit.workingTree(root);
        String timestamp = BenchReport.timestamp(Instant.now());
        JsonObject report = BenchReport.build(result, client, label, head, workingTree, timestamp);
        Path liveDir = BenchReport.liveDirectory(root);
        String counterpart = BenchReport.counterpartLabel(label);
        JsonObject pairing = counterpart == null ? null
                : BenchReport.pairing(report, BenchReport.newest(liveDir, result.scene().name(), counterpart));
        try {
            List<Path> written = BenchReport.write(liveDir, report, pairing);
            String message = "bench: wrote " + written.get(0) + " and " + written.get(1)
                    + (pairing != null ? " (paired with " + pairing.get("paired_with").getAsString() + ")" : " (no " + counterpart + " report of scene " + result.scene().name() + " to pair with)")
                    + (client == null ? "; NOTE: no client sampler in this JVM, frame metrics absent" : "")
                    + (result.countTicking() < result.countSpawned() ? "; WARNING: only " + result.countTicking() + " of " + result.countSpawned()
                    + " mobs stood in entity-ticking chunks (per-entity server figures divide by that count)" : "")
                    + (workingTree.startsWith("DIRTY") ? "; NOTE: working tree " + workingTree : "");
            OreSpawnMod.LOGGER.info(message);
            source.sendSuccess(() -> Component.literal(message), true);
            return 1;
        } catch (IOException failed) {
            source.sendFailure(Component.literal("bench: could not write the report: " + failed));
            return 0;
        }
    }
}
