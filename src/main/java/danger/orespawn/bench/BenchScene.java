package danger.orespawn.bench;

import danger.orespawn.ModEntities;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Phase G slice (d) (2026-09-06): the six isolation scenes of the spawn-100 benchmark
 * (phase_g_reports/morehitboxes_evaluation.md Section 5), as the dev command
 * {@code /orespawn bench scene <A-F> [count] [idle|wander]} spawns them. Every scene is a wedge of
 * one species laid out relative to the commanding player ({@link BenchSceneSpawner#layout}: rows
 * along the look axis at the scene's pitch, each row's columns within 35 degrees of the axis, every
 * mob under 180 blocks away); the base distance keeps the nearest row clear of the player. The
 * robots' 6-block and the Beaver's / Frog's 2-block pitch exceed their boxes; the Queen's 12-block
 * pitch is below its 22-block box on purpose (refuter B, 2026-09-06): a hundred Queens at a
 * 24-block pitch cannot fit inside the tracking and simulation ranges, the Queen is
 * {@code noPhysics}, and overlapping Queens are still drawn in full -- every one of them costs the
 * collector its whole bone walk, which is the cost the scene measures. What each scene fixes:
 * <ul>
 *   <li>A: 100 Queens in view, hostile size (the PlayNicely config must be off -- recorded in the
 *       report), idle: no AI, so they stand and animate their idle clip while the collector streams
 *       their bones every tick.</li>
 *   <li>B: the same Queens loaded but behind the player. NOTE: TheQueen sets {@code noCulling} and
 *       answers {@code shouldRenderAtSqrDistance} with true, so the level renderer draws her even
 *       off-screen -- scene B measures what loaded-but-unseen Queens cost when the renderer draws
 *       them anyway (the collector runs per frame), not frustum culling; a true culling control
 *       needs a species the frustum culls.</li>
 *   <li>C / D: 100 SpiderRobots / AntRobots, idle: no AI. The modern gait runs from {@code tick()}
 *       (not from the AI step), so a standing modern robot still feeds its eight leg parts every
 *       server tick and streams a keyframe every 40 ticks; the report records how many spawned
 *       modern (the {@code [tweaks] spiderMovement} config decides, not the harness).</li>
 *   <li>E: 100 Beavers, idle: the MHLib-free GeckoLib candidate (classic vs candidate through the
 *       Phase G dev switch, {@code -Dorespawn.dev.geckolibRenderers=beaver}).</li>
 *   <li>F: 100 Frogs, idle: a classic-only vanilla-path renderer of the Beaver's size class, the
 *       zero line (identical in both runs).</li>
 * </ul>
 * The {@code wander} state leaves the AI on (mobs roam, so the robots' gaits step and stream); it is
 * not the protocol's fixed state and is offered for the owner's looks only.
 */
public enum BenchScene {
    A("the_queen", () -> ModEntities.THE_QUEEN.get(), 100, 12.0D, 40.0D, false,
            "100 Queens in view, hostile size, idle (no AI)"),
    B("the_queen", () -> ModEntities.THE_QUEEN.get(), 100, 12.0D, 40.0D, true,
            "100 Queens loaded behind the player (drawn anyway: TheQueen is noCulling), hostile size, idle (no AI)"),
    C("spider_robot", () -> ModEntities.SPIDER_ROBOT.get(), 100, 6.0D, 8.0D, false,
            "100 SpiderRobots (modern when the config says so), idle (no AI): server-fed parts, no bone collection"),
    D("ant_robot", () -> ModEntities.ANT_ROBOT.get(), 100, 6.0D, 8.0D, false,
            "100 AntRobots (modern when the config says so), idle (no AI): server-fed parts, no bone collection"),
    E("beaver", () -> ModEntities.BEAVER.get(), 100, 2.0D, 6.0D, false,
            "100 Beavers, idle (no AI): the MHLib-free GeckoLib candidate vs its classic renderer"),
    F("frog", () -> ModEntities.FROG.get(), 100, 2.0D, 6.0D, false,
            "100 Frogs, idle (no AI): classic vanilla-path renderer, the zero line");

    private final String species;
    private final Supplier<EntityType<? extends Mob>> type;
    private final int defaultCount;
    private final double spacing;
    private final double baseDistance;
    private final boolean behindPlayer;
    private final String description;

    BenchScene(String species, Supplier<EntityType<? extends Mob>> type, int defaultCount, double spacing,
               double baseDistance, boolean behindPlayer, String description) {
        this.species = species;
        this.type = type;
        this.defaultCount = defaultCount;
        this.spacing = spacing;
        this.baseDistance = baseDistance;
        this.behindPlayer = behindPlayer;
        this.description = description;
    }

    /** The species' registry name (the id the Phase G dev switch uses). */
    public String species() {
        return this.species;
    }

    public EntityType<? extends Mob> type() {
        return this.type.get();
    }

    public int defaultCount() {
        return this.defaultCount;
    }

    /** Wedge pitch in blocks, along and across the look direction. */
    public double spacing() {
        return this.spacing;
    }

    /** Distance from the origin to the nearest row, in blocks. */
    public double baseDistance() {
        return this.baseDistance;
    }

    /** B: the wedge lies behind the origin (against the look direction). */
    public boolean behindPlayer() {
        return this.behindPlayer;
    }

    public String description() {
        return this.description;
    }

    /** The scene for a command token ({@code a} / {@code A}), or {@code null}. */
    public static BenchScene parse(String token) {
        if (token == null) {
            return null;
        }
        String key = token.trim().toUpperCase(Locale.ROOT);
        for (BenchScene scene : values()) {
            if (scene.name().equals(key)) {
                return scene;
            }
        }
        return null;
    }
}
