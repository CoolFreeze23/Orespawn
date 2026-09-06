package danger.orespawn.gametest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.bench.BenchClientSnapshot;
import danger.orespawn.bench.BenchCommand;
import danger.orespawn.bench.BenchGit;
import danger.orespawn.bench.BenchHarness;
import danger.orespawn.bench.BenchReport;
import danger.orespawn.bench.BenchScene;
import danger.orespawn.bench.BenchSceneSpawner;
import danger.orespawn.bench.BenchServerResult;
import danger.orespawn.bench.BenchState;
import danger.orespawn.bench.BenchStats;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.IModernLeggedRobot;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.TheQueen;
import de.dertoaster.multihitboxlib.api.IMHLibFieldAccessor;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.network.client.CPacketBoneInformation;
import de.dertoaster.multihitboxlib.network.server.SPacketUpdateMultipart;
import de.dertoaster.multihitboxlib.util.BoneInformation;
import de.dertoaster.multihitboxlib.util.MHLibCollectorProbe;
import de.dertoaster.multihitboxlib.util.MHLibCounters;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Phase G slice (d) (2026-09-06) -- the spawn-100 benchmark harness's server-side pieces, pinned on the
 * game-test server (scope addendum item 23 (8)(d); morehitboxes_evaluation.md Section 5): the scene
 * spawner's wedge layout (every mob ahead of / behind the camera, inside the 70-degree FOV wedge and under
 * 180 blocks; neighbours one pitch apart; the capacity) / count / species / frozen state per scene, the
 * report writer's JSON shape (fields present; the counters section keyed by name; the coverage object and
 * the per-entity divisors; JSON null -- never a bare NaN -- for undefined metrics, parsed back with a
 * non-lenient reader), the command's gating (absent from the live dispatcher without
 * {@code -Dorespawn.dev.bench=true}; the tree and its permission on a fresh dispatcher), the server-side
 * MHLib counters' increments under the game-test run's {@code -Dmhlib.counters=true}, the byte accounting of
 * both packets against a manual encode, the collector probe's span accounting, the dump order with the
 * four new client names, the order statistics, and the git HEAD / working-tree reader over a synthetic
 * {@code .git}.
 *
 * <p>Dist safety: every class referenced here is common code -- the {@code danger.orespawn.bench} package
 * has no client import; the client sampler ({@code danger.orespawn.client.bench}) is never named. Frozen
 * mobs stand on the template floor (the spawner resolves y from the heightmap; the rows check the block
 * under the feet); the 48x16x48 {@code empty_large} template holds a Queen 40 blocks from an origin four
 * blocks inside either edge. The three counter rows (11, 12, 17) need the counters live: item 17 of the
 * owner's 2026-09-06 rulings removed slice (d)'s test seam from {@code MHLibCounters} (production code), and
 * the {@code gameTestServer} run in {@code build.gradle} sets {@code -Dmhlib.counters=true} instead,
 * which each of those rows asserts as its precondition (a run without the property -- the client run's
 * {@code /test} -- fails them there, naming the property). With the property live, {@code MHLibMod.onServerTick}
 * dumps AND zeroes the server list every 100 server ticks for the whole suite: a synchronous row resets and
 * reads inside one tick (no dump can land between), so its expectations stay exact; the asynchronous row (the
 * S2C broadcast and election counts) waits 39 ticks, so it sums the dumps it sees through a
 * {@link MHLibCounters.DumpListener} and adds the partial it reads, the way {@code BenchSession} does. It runs
 * in its own batch ({@code benchHarnessAsync}): a row's body starts 20 ticks after its OWN structure's chunks are
 * entity-ticking, in the chunk pipeline's order, so within one batch nothing pins the start order -- a batch,
 * though, runs only after the previous batch has completed, so no synchronous row's in-row reset can land inside
 * the asynchronous row's window (refuter B, item 17). A {@link GameTestGenerator} over {@link #rows()}, one
 * {@link TestFunction} per row, {@code benchharnesstests.sliced_NN_<row>}, the batches {@code benchHarness} and
 * {@code benchHarnessAsync} (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BenchHarnessTests {

    private static final String BATCH = "benchHarness";
    /** The asynchronous row (17) runs in its own batch: the framework runs a batch only after the previous one has
     *  completed, so no synchronous row's in-row reset can land inside its 39-tick window (refuter B, item 17). */
    private static final String BATCH_ASYNC = "benchHarnessAsync";
    private static final String TEST_PREFIX = "benchharnesstests.";
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 200;
    private static final String FINDING = "PHASE-G-(d)";
    private static final double EPS = 1.0E-6D;

    /** The nine MHLib client counters (OPT-028) and the four slice (d) names that follow them. */
    private static final List<String> NINE = List.of(
            "client.frames", "client.collecting_passes", "client.recursive_start", "client.recursive_end",
            "client.bones_visited", "client.world_pos_reads", "client.folds", "client.bone_infos_built",
            "client.apply_information");
    private static final List<String> FOUR = List.of(
            "client.collector_ns", "client.collector_alloc_bytes", "net.c2s_bone_packets", "net.c2s_bone_bytes");
    private static final List<String> SERVER_SEVEN = List.of(
            "net.s2c_update_packets", "net.s2c_update_bytes", "net.set_master_packets",
            "server.align_sub_parts_parts", "server.align_synched_parts", "server.part_setpos", "server.placement_ns");

    /**
     * A player standing at rel (24, floor, 4) looking +z (yaw 0): the scenes spawn towards +z (A: the first
     * slot 40 blocks out at z = 44, inside the 48-deep template) or -z (B, from rel z 44: the slot at z = 4).
     */
    private static final Vec3 ORIGIN_FRONT = new Vec3(24.0D, 1.0D, 4.0D);
    private static final Vec3 ORIGIN_BACK = new Vec3(24.0D, 1.0D, 44.0D);
    private static final float LOOK_SOUTH = 0.0F;

    private record Row(int index, String tag, Consumer<GameTestHelper> body, boolean asynchronous) {
        String testName() {
            return TEST_PREFIX + String.format("sliced_%02d_%s", this.index, this.tag);
        }
    }

    private static List<Row> rows() {
        return List.of(
                new Row(1, "layout_wedge_in_front_and_behind", BenchHarnessTests::layoutWedgeInFrontAndBehind, false),
                new Row(2, "scene_a_queen_frozen_on_the_floor", BenchHarnessTests::sceneAQueenFrozenOnTheFloor, false),
                new Row(3, "scene_b_queen_behind_the_origin", BenchHarnessTests::sceneBQueenBehindTheOrigin, false),
                new Row(4, "scene_c_spider_robots", BenchHarnessTests::sceneCSpiderRobots, false),
                new Row(5, "scene_d_ant_robots", BenchHarnessTests::sceneDAntRobots, false),
                new Row(6, "scene_e_f_beaver_and_frog", BenchHarnessTests::sceneEFBeaverAndFrog, false),
                new Row(7, "wander_state_keeps_ai_and_sweep_discards", BenchHarnessTests::wanderStateKeepsAiAndSweepDiscards, false),
                new Row(8, "report_json_shape", BenchHarnessTests::reportJsonShape, false),
                new Row(9, "report_pairing_and_files", BenchHarnessTests::reportPairingAndFiles, false),
                new Row(10, "command_gating_and_tree", BenchHarnessTests::commandGatingAndTree, false),
                // Rows 11-12 keep their slice (d) tags (the gate's test IDs); since item 17 (2026-09-06) they count under the
                // game-test run's -Dmhlib.counters=true, not a seam (assertCountersLive).
                new Row(11, "server_counters_queen_under_the_seam", BenchHarnessTests::serverCountersQueen, false),
                new Row(12, "server_counters_modern_spider_under_the_seam", BenchHarnessTests::serverCountersModernSpider, false),
                new Row(13, "packet_encoded_lengths", BenchHarnessTests::packetEncodedLengths, false),
                new Row(14, "collector_probe_span_accounting", BenchHarnessTests::collectorProbeSpanAccounting, false),
                new Row(15, "dump_order_with_the_new_names", BenchHarnessTests::dumpOrderWithTheNewNames, false),
                new Row(16, "order_statistics", BenchHarnessTests::orderStatistics, false),
                new Row(17, "s2c_broadcasts_and_election_counted", BenchHarnessTests::s2cBroadcastsAndElectionCounted, true),
                new Row(18, "git_head_and_working_tree", BenchHarnessTests::gitHeadAndWorkingTree, false));
    }

    @GameTestGenerator
    public Collection<TestFunction> benchHarnessRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(row.asynchronous() ? BATCH_ASYNC : BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true, row.body()));
        }
        return functions;
    }

    // ------------------------------------------------------------------ 1: the layout

    private static void layoutWedgeInFrontAndBehind(GameTestHelper helper) {
        Vec3 origin = Vec3.ZERO;
        final double tanHalf = Math.tan(Math.toRadians(BenchSceneSpawner.WEDGE_HALF_ANGLE_DEGREES));
        helper.assertTrue(BenchSceneSpawner.WEDGE_HALF_ANGLE_DEGREES == 35.0D && BenchSceneSpawner.MAX_DISTANCE_BLOCKS == 180.0D, FINDING
                + ": the wedge is half the default 70-degree FOV and stays under 180 blocks (simulation distance 12 chunks = 192; tracking cap 256)");
        // Every scene at its default count: ahead (or behind for B), inside the wedge, under the limit, neighbours one pitch apart.
        for (BenchScene scene : BenchScene.values()) {
            List<Vec3> slots = BenchSceneSpawner.layout(origin, LOOK_SOUTH, scene, scene.defaultCount());
            helper.assertTrue(slots.size() == scene.defaultCount(), FINDING + ": layout(" + scene + ", " + scene.defaultCount() + ") has that many slots, actual " + slots.size());
            helper.assertTrue(BenchSceneSpawner.capacity(scene) >= scene.defaultCount(), FINDING + ": scene " + scene + " holds its default count, capacity "
                    + BenchSceneSpawner.capacity(scene));
            for (int i = 0; i < slots.size(); i++) {
                Vec3 p = slots.get(i);
                double along = scene.behindPlayer() ? -p.z : p.z;
                helper.assertTrue(along >= scene.baseDistance() - EPS, FINDING + ": scene " + scene + " slot " + i + " lies " + (scene.behindPlayer() ? "behind" : "ahead of")
                        + " the camera at least the base distance out, actual " + p);
                helper.assertTrue(Math.abs(p.x) <= along * tanHalf + EPS, FINDING + ": scene " + scene + " slot " + i + " lies inside the wedge (|x| <= d tan 35), actual " + p);
                helper.assertTrue(Math.hypot(p.x, p.z) < BenchSceneSpawner.MAX_DISTANCE_BLOCKS, FINDING + ": scene " + scene + " slot " + i + " is under 180 blocks, actual " + p);
                double nearest = Double.MAX_VALUE;
                for (int j = 0; j < slots.size(); j++) {
                    if (j != i) {
                        nearest = Math.min(nearest, p.distanceTo(slots.get(j)));
                    }
                }
                helper.assertTrue(slots.size() < 2 || Math.abs(nearest - scene.spacing()) < EPS, FINDING + ": scene " + scene + " slot " + i
                        + "'s nearest neighbour is exactly one pitch (" + scene.spacing() + ") away -- no duplicate, no gap; actual " + nearest);
            }
            helper.assertTrue(Math.abs(BenchSceneSpawner.maxDistanceBlocks(origin, slots) - farthest(origin, slots)) < EPS, FINDING
                    + ": maxDistanceBlocks reports the farthest slot for scene " + scene);
        }
        // The Queen wedge: 100 at a 12-block pitch from 40 blocks out fill ten rows, 5+7+7+9+11+11+13+15+15 = 93 in the first nine
        // and seven in the tenth (148 blocks out: x = 0, +-12, +-24, +-36); the farthest is a ninth-row corner, hypot(136, 84) = 159.8 blocks.
        List<Vec3> front = BenchSceneSpawner.layout(origin, LOOK_SOUTH, BenchScene.A, 100);
        helper.assertTrue(BenchScene.A.spacing() == 12.0D && BenchScene.A.baseDistance() == 40.0D && BenchScene.B.spacing() == 12.0D
                && BenchScene.B.baseDistance() == 40.0D, FINDING + ": the Queen scenes use a 12-block pitch from 40 blocks out, actual "
                + BenchScene.A.spacing() + " / " + BenchScene.A.baseDistance());
        helper.assertTrue(BenchScene.A.spacing() < 22.0D, FINDING + ": the Queen pitch is below the 22-block box on purpose (overlapping Queens, noPhysics, each drawn in full)");
        TreeSet<Double> rows = new TreeSet<>();
        int tenthRow = 0;
        for (Vec3 p : front) {
            rows.add(p.z);
            if (Math.abs(p.z - 148.0D) < EPS) {
                tenthRow++;
            }
        }
        helper.assertTrue(rows.size() == 10 && Math.abs(rows.first() - 40.0D) < EPS && Math.abs(rows.last() - 148.0D) < EPS && tenthRow == 7, FINDING
                + ": ten rows from 40 to 148 blocks with seven Queens in the tenth, actual rows " + rows + ", tenth row " + tenthRow);
        double expectedFarthest = Math.hypot(136.0D, 84.0D);
        helper.assertTrue(Math.abs(farthest(origin, front) - expectedFarthest) < EPS, FINDING + ": the farthest of 100 Queens is the ninth row's corner, hypot(136, 84) = "
                + expectedFarthest + " blocks out (under the 180-block limit and the 192-block simulation distance), actual " + farthest(origin, front));
        // The fill order: row by row, centre-out (0, then one pitch either side, then two).
        double[] firstRow = {0.0D, 12.0D, 12.0D, 24.0D, 24.0D};
        for (int i = 0; i < firstRow.length; i++) {
            helper.assertTrue(Math.abs(Math.abs(front.get(i).x) - firstRow[i]) < EPS && Math.abs(front.get(i).z - 40.0D) < EPS, FINDING
                    + ": slot " + i + " sits in the first row at |x| = " + firstRow[i] + ", actual " + front.get(i));
        }
        helper.assertTrue(Math.abs(front.get(1).x + front.get(2).x) < EPS && Math.abs(front.get(3).x + front.get(4).x) < EPS, FINDING
                + ": the side slots of a row come in mirrored pairs");
        int capacity = BenchSceneSpawner.capacity(BenchScene.A);
        helper.assertTrue(capacity == 132, FINDING + ": the Queen wedge holds 132 (rows 40..172; the two farthest rows trimmed by the 180-block limit), actual " + capacity);
        boolean refused = false;
        try {
            BenchSceneSpawner.layout(origin, LOOK_SOUTH, BenchScene.A, capacity + 1);
        } catch (IllegalArgumentException expected) {
            refused = true;
        }
        helper.assertTrue(refused, FINDING + ": a count past the capacity is refused (IllegalArgumentException)");
        // B is the same fill mirrored behind the camera.
        List<Vec3> behind = BenchSceneSpawner.layout(origin, LOOK_SOUTH, BenchScene.B, 100);
        for (int i = 0; i < 100; i++) {
            helper.assertTrue(Math.abs(behind.get(i).x - front.get(i).x) < EPS && Math.abs(behind.get(i).z + front.get(i).z) < EPS, FINDING
                    + ": scene B slot " + i + " mirrors scene A's behind the origin, actual " + behind.get(i) + " vs " + front.get(i));
        }
        // Yaw 90 looks -x: scene A goes to -x, the wedge measured against that axis.
        for (Vec3 p : BenchSceneSpawner.layout(origin, 90.0F, BenchScene.A, 10)) {
            helper.assertTrue(p.x <= -BenchScene.A.baseDistance() + EPS && Math.abs(p.z) <= -p.x * tanHalf + EPS, FINDING
                    + ": at yaw 90 scene A lies towards -x inside the wedge, actual " + p);
        }
        helper.assertTrue(Math.abs(BenchSceneSpawner.facingYaw(LOOK_SOUTH, BenchScene.A)) == 180.0F, FINDING
                + ": mobs in front face back at the origin (yaw 180 for a south look), actual " + BenchSceneSpawner.facingYaw(LOOK_SOUTH, BenchScene.A));
        helper.assertTrue(BenchSceneSpawner.facingYaw(LOOK_SOUTH, BenchScene.B) == 0.0F, FINDING
                + ": mobs behind face the origin too (yaw 0 for a south look), actual " + BenchSceneSpawner.facingYaw(LOOK_SOUTH, BenchScene.B));
        helper.assertTrue(BenchScene.parse("c") == BenchScene.C && BenchScene.parse("Q") == null && BenchState.parse("Wander") == BenchState.WANDER,
                FINDING + ": scene and state tokens parse case-insensitively and unknown tokens yield null");
        helper.succeed();
    }

    // ------------------------------------------------------------------ 2-6: the scenes

    private static void sceneAQueenFrozenOnTheFloor(GameTestHelper helper) {
        List<Mob> mobs = new ArrayList<>();
        try {
            mobs.addAll(spawnScene(helper, ORIGIN_FRONT, BenchScene.A, 1, BenchState.IDLE));
            helper.assertTrue(mobs.size() == 1 && mobs.get(0) instanceof TheQueen, FINDING + ": scene A spawns TheQueen, actual " + describe(mobs));
            Mob queen = mobs.get(0);
            Vec3 expected = helper.absoluteVec(new Vec3(ORIGIN_FRONT.x, 0.0D, ORIGIN_FRONT.z + BenchScene.A.baseDistance()));
            helper.assertTrue(Math.abs(queen.getX() - expected.x) < EPS && Math.abs(queen.getZ() - expected.z) < EPS, FINDING
                    + ": one Queen sits on the look axis at the base distance (the wedge's first slot), expected x/z " + expected.x + "/" + expected.z + ", actual " + queen.position());
            assertFrozenOnTheFloor(helper, queen, "the Queen", BenchScene.A);
            helper.assertTrue(helper.getLevel().isPositionEntityTicking(queen.blockPosition()), FINDING
                    + ": the test structure's chunks are forced (ticket level 31 = ENTITY_TICKING), so the coverage count the report divides by counts this Queen");
            helper.assertTrue(BenchSceneSpawner.countTicking(helper.getLevel(), mobs) == 1, FINDING + ": countTicking counts the one ticking Queen, actual "
                    + BenchSceneSpawner.countTicking(helper.getLevel(), mobs));
            helper.assertTrue(queen.getParts() != null && queen.getParts().length == 10, FINDING + ": the Queen carries its ten MHLib parts, actual "
                    + (queen.getParts() == null ? 0 : queen.getParts().length));
            helper.assertTrue(BenchSceneSpawner.partCount(mobs) == 10, FINDING + ": partCount sums the parts, actual " + BenchSceneSpawner.partCount(mobs));
            helper.assertTrue(BenchScene.A.description().contains("hostile") && BenchScene.A.description().contains("idle"), FINDING
                    + ": scene A's description names the fixed state, actual " + BenchScene.A.description());
        } finally {
            discardAll(mobs);
        }
        helper.succeed();
    }

    private static void sceneBQueenBehindTheOrigin(GameTestHelper helper) {
        List<Mob> mobs = new ArrayList<>();
        try {
            mobs.addAll(spawnScene(helper, ORIGIN_BACK, BenchScene.B, 1, BenchState.IDLE));
            helper.assertTrue(mobs.size() == 1 && mobs.get(0) instanceof TheQueen, FINDING + ": scene B spawns TheQueen, actual " + describe(mobs));
            Mob queen = mobs.get(0);
            Vec3 origin = helper.absoluteVec(ORIGIN_BACK);
            helper.assertTrue(queen.getZ() < origin.z - BenchScene.B.baseDistance() + EPS, FINDING + ": scene B's Queen stands behind the origin (z "
                    + queen.getZ() + " vs origin z " + origin.z + ")");
            assertFrozenOnTheFloor(helper, queen, "the off-screen Queen", BenchScene.B);
        } finally {
            discardAll(mobs);
        }
        helper.succeed();
    }

    private static void sceneCSpiderRobots(GameTestHelper helper) {
        sceneRobots(helper, BenchScene.C, SpiderRobot.class, "SpiderRobot", 8);
    }

    private static void sceneDAntRobots(GameTestHelper helper) {
        sceneRobots(helper, BenchScene.D, AntRobot.class, "AntRobot", 6);
    }

    /**
     * {@code legs} is the species' profile part count — {@code data/orespawn/multihitboxlib/hitbox_profiles/spider_robot.json} lists
     * leg0..leg7 (8), {@code ant_robot.json} leg0..leg5 (6, the ant's 49px rig); the row reads the live profile back through the
     * entity and pins both it and the parts array against that count (the slice (d) gate, 2026-09-06: the row had assumed the
     * spider's eight for the ant).
     */
    private static void sceneRobots(GameTestHelper helper, BenchScene scene, Class<?> species, String name, int legs) {
        List<Mob> mobs = new ArrayList<>();
        try {
            mobs.addAll(spawnScene(helper, ORIGIN_FRONT, scene, 2, BenchState.IDLE));
            helper.assertTrue(mobs.size() == 2, FINDING + ": scene " + scene + " spawns two, actual " + describe(mobs));
            for (Mob mob : mobs) {
                helper.assertTrue(species.isInstance(mob), FINDING + ": scene " + scene + " spawns " + name + ", actual " + mob.getType());
                assertFrozenOnTheFloor(helper, mob, name, scene);
                IModernLeggedRobot robot = (IModernLeggedRobot) mob;
                String mode = robot.isModernMovement() ? "modern" : "classic";
                int expected = robot.isModernMovement() ? legs : 0;
                int parts = mob.getParts() == null ? 0 : mob.getParts().length;
                int profileParts = ((IMultipartEntity<?>) (Object) mob).getHitboxProfile().map(profile -> profile.partConfigs().size()).orElse(0);
                helper.assertTrue(profileParts == expected, FINDING + ": a " + mode + " " + name + "'s hitbox profile lists " + expected
                        + " parts (hitbox_profiles/: the spider's eight legs, the ant's six; a classic robot supplies no profile), actual " + profileParts);
                helper.assertTrue(parts == expected, FINDING + ": a " + mode + " " + name + " carries " + expected + " parts, actual " + parts
                        + " (the config decides the mode, the harness records it)");
            }
            double apart = Math.hypot(mobs.get(0).getX() - mobs.get(1).getX(), mobs.get(0).getZ() - mobs.get(1).getZ());
            helper.assertTrue(Math.abs(apart - scene.spacing()) < EPS, FINDING + ": the two robots are neighbours one pitch apart (the wedge's near row at "
                    + scene.baseDistance() + " blocks is one slot wide, so the second stands one pitch further along the axis), actual " + apart);
        } finally {
            discardAll(mobs);
        }
        helper.succeed();
    }

    private static void sceneEFBeaverAndFrog(GameTestHelper helper) {
        List<Mob> mobs = new ArrayList<>();
        try {
            mobs.addAll(spawnScene(helper, ORIGIN_FRONT, BenchScene.E, 2, BenchState.IDLE));
            helper.assertTrue(mobs.size() == 2 && mobs.get(0).getType() == ModEntities.BEAVER.get() && mobs.get(1).getType() == ModEntities.BEAVER.get(),
                    FINDING + ": scene E spawns Beavers, actual " + describe(mobs));
            List<Mob> frogs = spawnScene(helper, new Vec3(ORIGIN_FRONT.x, ORIGIN_FRONT.y, ORIGIN_FRONT.z + 12.0D), BenchScene.F, 2, BenchState.IDLE);
            mobs.addAll(frogs);
            helper.assertTrue(frogs.size() == 2 && frogs.get(0).getType() == ModEntities.FROG.get() && frogs.get(1).getType() == ModEntities.FROG.get(),
                    FINDING + ": scene F spawns Frogs, actual " + describe(frogs));
            for (Mob mob : mobs) {
                assertFrozenOnTheFloor(helper, mob, mob.getType().toString(), mob.getType() == ModEntities.BEAVER.get() ? BenchScene.E : BenchScene.F);
                helper.assertTrue(mob.getParts() == null || mob.getParts().length == 0, FINDING + ": E and F species carry no MHLib part, actual "
                        + (mob.getParts() == null ? 0 : mob.getParts().length));
            }
            helper.assertTrue(BenchReport.LANDED_SPECIES.contains(BenchScene.E.species()) && !BenchReport.LANDED_SPECIES.contains(BenchScene.F.species()),
                    FINDING + ": the Beaver has a candidate renderer behind the dev switch and the Frog has none (the zero line)");
            helper.assertTrue(BenchScene.E.type().getWidth() < 1.0F && BenchScene.F.type().getWidth() < 1.0F, FINDING
                    + ": E and F are the same size class (sub-block boxes), actual " + BenchScene.E.type().getWidth() + " / " + BenchScene.F.type().getWidth());
        } finally {
            discardAll(mobs);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ 7: the wander state and the sweep

    private static void wanderStateKeepsAiAndSweepDiscards(GameTestHelper helper) {
        List<Mob> mobs = new ArrayList<>();
        try {
            mobs.addAll(spawnScene(helper, ORIGIN_FRONT, BenchScene.E, 3, BenchState.WANDER));
            helper.assertTrue(mobs.size() == 3, FINDING + ": three Beavers, actual " + mobs.size());
            for (Mob mob : mobs) {
                helper.assertTrue(!mob.isNoAi() && mob.isPersistenceRequired() && mob.getTags().contains(BenchSceneSpawner.TAG), FINDING
                        + ": the wander state leaves the AI on but keeps persistence and the tag, actual noAi=" + mob.isNoAi());
            }
            int swept = BenchSceneSpawner.discardTagged(helper.getLevel());
            helper.assertTrue(swept == 3, FINDING + ": the sweep discards every tagged entity, expected 3, actual " + swept);
            for (Mob mob : mobs) {
                helper.assertTrue(mob.isRemoved(), FINDING + ": a swept mob is removed");
            }
            int again = BenchSceneSpawner.discardTagged(helper.getLevel());
            helper.assertTrue(again == 0, FINDING + ": a second sweep finds nothing, actual " + again);
        } finally {
            discardAll(mobs);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ 8-9: the report

    /** 100 spawned, 80 of them ticking (the divisor of the per-entity server figures), the farthest 152.3 blocks out. */
    private static final int SYNTHETIC_TICKING = 80;
    /** 90 benchmark entities in the client level (the divisor of the per-entity client figures). */
    private static final int SYNTHETIC_IN_CLIENT_LEVEL = 90;

    private static BenchServerResult syntheticResult() {
        return syntheticResult(SYNTHETIC_TICKING);
    }

    private static BenchServerResult syntheticResult(int ticking) {
        Map<String, Long> totals = new LinkedHashMap<>();
        long value = 1000L;
        for (String name : SERVER_SEVEN) {
            totals.put(name, value);
            value += 1000L;
        }
        return new BenchServerResult(BenchScene.A, BenchState.IDLE, 100, 100, ticking, 152.3D, -1, 1000, 120, 2400, 120.5D,
                3.1D, 4.2D, 5.0D, 3.2D, totals, 24, true, 1, "minecraft:overworld", 1000L, 121500L);
    }

    private static BenchClientSnapshot syntheticClient(double frameMedian) {
        Map<String, Double> perSecond = new LinkedHashMap<>();
        for (String name : NINE) {
            perSecond.put(name, 10.0D);
        }
        for (String name : FOUR) {
            perSecond.put(name, 20.0D);
        }
        Map<String, String> controls = new LinkedHashMap<>();
        controls.put("client.render_distance_chunks", "16");
        return new BenchClientSnapshot(true, 120.0D, 7200, frameMedian, frameMedian + 2.0D, frameMedian + 4.0D, 1000.0D / (frameMedian + 4.0D),
                60.0D, 80.0D, 40.0D, 50.0D, 60.0D, 1.5D, 3.0D, perSecond, 24, controls, BenchReport.devSwitchStates(), "E: 100/100", SYNTHETIC_IN_CLIENT_LEVEL);
    }

    /** Parses with a non-lenient reader: RFC 8259 only (no NaN / Infinity tokens, no unquoted names, nothing after the value). */
    private static JsonElement parseStrict(String text) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(text));
        reader.setLenient(false);
        JsonElement element = new Gson().getAdapter(JsonElement.class).read(reader);
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            throw new IOException("content after the document");
        }
        return element;
    }

    private static void reportJsonShape(GameTestHelper helper) {
        BenchServerResult result = syntheticResult();
        JsonObject report = BenchReport.build(result, null, BenchReport.LABEL_CLASSIC, "abc123", "clean by index stat (synthetic)", "20260906T000000Z");
        for (String key : List.of("schema_version", "harness", "scene", "scene_description", "species", "state", "variant", "variant_source",
                "git_head", "working_tree", "timestamp", "count_requested", "count_spawned", "modern_count", "part_count", "coverage", "run", "server",
                "counters", "network", "client", "controls", "dev_switch", "threshold")) {
            helper.assertTrue(report.has(key), FINDING + ": the report carries '" + key + "', keys " + report.keySet());
        }
        helper.assertTrue("clean by index stat (synthetic)".equals(report.get("working_tree").getAsString()), FINDING + ": working_tree travels with the report");
        JsonObject coverage = report.getAsJsonObject("coverage");
        helper.assertTrue(coverage.get("spawned").getAsInt() == 100 && coverage.get("ticking").getAsInt() == SYNTHETIC_TICKING
                && coverage.get("in_client_level").isJsonNull() && Math.abs(coverage.get("max_distance_blocks").getAsDouble() - 152.3D) < EPS, FINDING
                + ": coverage carries spawned / ticking / in_client_level (null without a client) / max_distance_blocks, actual " + coverage);
        helper.assertTrue(!coverage.get("warning").isJsonNull() && coverage.get("warning").getAsString().contains("80 of 100"), FINDING
                + ": ticking < spawned raises the coverage warning, actual " + coverage.get("warning"));
        JsonObject full = BenchReport.build(syntheticResult(100), null, BenchReport.LABEL_CLASSIC, "abc123", null, "20260906T000000Z");
        helper.assertTrue(full.getAsJsonObject("coverage").get("warning").isJsonNull(), FINDING + ": ticking == spawned raises no warning");
        helper.assertTrue(BenchGit.WORKING_TREE_HEAD_ONLY.equals(full.get("working_tree").getAsString()), FINDING + ": a null working-tree reading is written as head only");
        helper.assertTrue(report.get("schema_version").getAsInt() == BenchReport.SCHEMA_VERSION && "A".equals(report.get("scene").getAsString())
                && "classic".equals(report.get("variant").getAsString()) && "abc123".equals(report.get("git_head").getAsString())
                && "the_queen".equals(report.get("species").getAsString()) && "idle".equals(report.get("state").getAsString()),
                FINDING + ": the header fields carry the run's identity, actual " + report);
        JsonObject run = report.getAsJsonObject("run");
        helper.assertTrue(run.get("server_ticks").getAsInt() == 2400 && run.get("requested_seconds").getAsInt() == 120
                && Math.abs(run.get("wall_seconds").getAsDouble() - 120.5D) < EPS && run.get("players_online").getAsInt() == 1,
                FINDING + ": the run section, actual " + run);
        JsonObject server = report.getAsJsonObject("server");
        helper.assertTrue(Math.abs(server.get("mspt_median_ms").getAsDouble() - 3.1D) < EPS && Math.abs(server.get("mspt_p95_ms").getAsDouble() - 4.2D) < EPS
                && server.get("source").getAsString().contains("getTickTimesNanos"), FINDING + ": the server section, actual " + server);
        JsonObject counters = report.getAsJsonObject("counters");
        helper.assertTrue(counters.get("enabled").getAsBoolean() && counters.get("server_dumps").getAsInt() == 24, FINDING + ": counters.enabled / dumps, actual " + counters);
        List<String> perSecondKeys = new ArrayList<>(counters.getAsJsonObject("server_per_second").keySet());
        helper.assertTrue(perSecondKeys.equals(SERVER_SEVEN), FINDING + ": counters.server_per_second is keyed by the seven server names in order, actual " + perSecondKeys);
        List<String> totalKeys = new ArrayList<>(counters.getAsJsonObject("server_totals").keySet());
        helper.assertTrue(totalKeys.equals(SERVER_SEVEN), FINDING + ": counters.server_totals keyed by name, actual " + totalKeys);
        double bytesPerSecond = counters.getAsJsonObject("server_per_second").get("net.s2c_update_bytes").getAsDouble();
        helper.assertTrue(Math.abs(bytesPerSecond - 2000.0D / 120.5D) < EPS, FINDING + ": per second = total / wall seconds, expected " + (2000.0D / 120.5D) + ", actual " + bytesPerSecond);
        double perEntity = counters.getAsJsonObject("per_entity_per_second").get("net.s2c_update_bytes").getAsDouble();
        helper.assertTrue(Math.abs(perEntity - 2000.0D / 120.5D / SYNTHETIC_TICKING) < EPS, FINDING
                + ": per entity per second divides the server counters by the TICKING count (80), not the count spawned, actual " + perEntity);
        helper.assertTrue(counters.getAsJsonObject("client_per_second").size() == 0, FINDING + ": without a client the client counters are empty");
        JsonObject zeroTicking = BenchReport.build(syntheticResult(0), null, BenchReport.LABEL_CLASSIC, "abc123", null, "20260906T000000Z");
        helper.assertTrue(zeroTicking.getAsJsonObject("counters").getAsJsonObject("per_entity_per_second").get("net.s2c_update_bytes").isJsonNull(), FINDING
                + ": nothing ticking: the per-entity figure is JSON null, not a division by zero");
        JsonObject network = report.getAsJsonObject("network");
        helper.assertTrue(Math.abs(network.get("s2c_packets_per_second").getAsDouble() - (1000.0D + 3000.0D) / 120.5D) < EPS, FINDING
                + ": S2C packets per second sum the update and set-master counts, actual " + network.get("s2c_packets_per_second"));
        helper.assertTrue(network.get("c2s_packets_per_second").isJsonNull() && network.get("c2s_bytes_per_second").isJsonNull(), FINDING
                + ": C2S is JSON null without a client (never a bare NaN), actual " + network);
        String text = BenchReport.toJson(report);
        helper.assertTrue(!text.contains("NaN") && !text.contains("Infinity"), FINDING + ": the on-disk text carries no NaN / Infinity token");
        try {
            JsonElement strict = parseStrict(text);
            helper.assertTrue(strict.isJsonObject() && strict.getAsJsonObject().getAsJsonObject("network").get("c2s_packets_per_second").isJsonNull(), FINDING
                    + ": the on-disk text parses with a non-lenient reader and the null survives the round trip");
        } catch (IOException | RuntimeException malformed) {
            helper.fail(FINDING + ": the on-disk text is not strict JSON: " + malformed);
        }
        try {
            parseStrict("{\"x\": NaN}");
            helper.fail(FINDING + ": the strict reader must reject a bare NaN (the check would be vacuous otherwise)");
        } catch (IOException | RuntimeException expected) {
            // a MalformedJsonException: the reader is really strict
        }
        JsonObject client = report.getAsJsonObject("client");
        helper.assertTrue(!client.get("available").getAsBoolean() && client.has("reason"), FINDING + ": client.available=false with a reason, actual " + client);
        JsonObject controls = report.getAsJsonObject("controls");
        helper.assertTrue(controls.has("jdk") && controls.has("jvm_flags") && controls.has("os") && controls.has("owner_fields"), FINDING + ": the controls, actual " + controls.keySet());
        JsonObject owner = controls.getAsJsonObject("owner_fields");
        helper.assertTrue(owner.keySet().containsAll(List.of("machine", "gpu_and_driver", "camera_path", "world_seed", "notes")), FINDING
                + ": the owner's blank fields, actual " + owner.keySet());
        helper.assertTrue(report.getAsJsonObject("dev_switch").size() == BenchReport.LANDED_SPECIES.size(), FINDING
                + ": one dev-switch line per landed species, actual " + report.getAsJsonObject("dev_switch").size());
        helper.assertTrue(report.getAsJsonObject("threshold").get("status").getAsString().startsWith("PROPOSED"), FINDING + ": the threshold is marked proposed");
        String md = BenchReport.markdown(report);
        helper.assertTrue(md.startsWith("# Live benchmark: scene A (classic)") && md.contains("## MHLib counters") && md.contains("Not available"),
                FINDING + ": the markdown mirrors the report");
        // With a client: the client section and the client counters keyed by name; per-entity client figures divide by in_client_level.
        JsonObject withClient = BenchReport.build(result, syntheticClient(10.0D), BenchReport.LABEL_CANDIDATE, "def456", null, "20260906T000001Z");
        helper.assertTrue(withClient.getAsJsonObject("client").get("available").getAsBoolean()
                && Math.abs(withClient.getAsJsonObject("client").get("frame_median_ms").getAsDouble() - 10.0D) < EPS, FINDING + ": the client section");
        List<String> clientKeys = new ArrayList<>(withClient.getAsJsonObject("counters").getAsJsonObject("client_per_second").keySet());
        List<String> expectedClient = new ArrayList<>(NINE);
        expectedClient.addAll(FOUR);
        helper.assertTrue(clientKeys.equals(expectedClient), FINDING + ": counters.client_per_second keyed by the client names, actual " + clientKeys);
        helper.assertTrue(Math.abs(withClient.getAsJsonObject("network").get("c2s_bytes_per_second").getAsDouble() - 20.0D) < EPS, FINDING
                + ": C2S bytes come from the client's net.c2s_bone_bytes");
        double clientPerEntity = withClient.getAsJsonObject("counters").getAsJsonObject("per_entity_per_second").get("net.c2s_bone_bytes").getAsDouble();
        helper.assertTrue(Math.abs(clientPerEntity - 20.0D / SYNTHETIC_IN_CLIENT_LEVEL) < EPS, FINDING
                + ": per entity per second divides the client counters by the entities in the client level (90), actual " + clientPerEntity);
        JsonObject withClientCoverage = withClient.getAsJsonObject("coverage");
        helper.assertTrue(withClientCoverage.get("in_client_level").getAsInt() == SYNTHETIC_IN_CLIENT_LEVEL
                && withClientCoverage.get("warning").getAsString().contains("90 of 100"), FINDING + ": coverage carries the client count and warns when it is short, actual " + withClientCoverage);
        helper.assertTrue(withClient.getAsJsonObject("controls").has("client.render_distance_chunks"), FINDING + ": the client's controls merge into the controls section");
        helper.assertTrue(BenchReport.markdown(withClient).contains("- Coverage: spawned 100, ticking 80, in the client level 90, farthest 152.3 blocks; WARNING"), FINDING
                + ": the markdown carries the coverage line");
        helper.succeed();
    }

    private static void reportPairingAndFiles(GameTestHelper helper) {
        BenchServerResult result = syntheticResult();
        JsonObject classic = BenchReport.build(result, syntheticClient(10.0D), BenchReport.LABEL_CLASSIC, "aaa", null, "20260906T000000Z");
        JsonObject candidate = BenchReport.build(result, syntheticClient(11.0D), BenchReport.LABEL_CANDIDATE, "bbb", null, "20260906T000100Z");
        JsonObject pairing = BenchReport.pairing(candidate, classic);
        helper.assertTrue(pairing != null && Math.abs(pairing.get("frame_median_regression_percent").getAsDouble() - 10.0D) < EPS, FINDING
                + ": the median regression is candidate over classic in percent, expected 10, actual " + (pairing == null ? "null" : pairing.get("frame_median_regression_percent")));
        helper.assertTrue(Math.abs(pairing.get("frame_p95_delta_ms").getAsDouble() - 1.0D) < EPS, FINDING + ": p95 delta in ms, actual " + pairing.get("frame_p95_delta_ms"));
        helper.assertTrue(Math.abs(pairing.get("render_thread_alloc_ratio_candidate_over_classic").getAsDouble() - 1.0D) < EPS, FINDING + ": the allocation ratio");
        helper.assertTrue("aaa".equals(pairing.get("classic_git_head").getAsString()) && "bbb".equals(pairing.get("candidate_git_head").getAsString()), FINDING
                + ": both heads travel with the pairing");
        JsonObject reversed = BenchReport.pairing(classic, candidate);
        helper.assertTrue(Math.abs(reversed.get("frame_median_regression_percent").getAsDouble() - 10.0D) < EPS, FINDING
                + ": the pairing is candidate-relative whichever report is newer");
        helper.assertTrue(BenchReport.pairing(candidate, null) == null, FINDING + ": no counterpart, no pairing");
        JsonObject serverOnly = BenchReport.build(result, null, BenchReport.LABEL_CANDIDATE, "ccc", null, "20260906T000200Z");
        JsonObject nullPairing = BenchReport.pairing(serverOnly, classic);
        helper.assertTrue(nullPairing.get("frame_median_regression_percent").isJsonNull() && !nullPairing.get("mspt_p95_delta_ms").isJsonNull(), FINDING
                + ": a pairing input that is null on one side makes that delta null (the reader tolerates null), the rest stay numbers; actual " + nullPairing);
        helper.assertTrue(BenchReport.LABEL_CANDIDATE.equals(BenchReport.counterpartLabel(BenchReport.LABEL_CLASSIC))
                && BenchReport.LABEL_CLASSIC.equals(BenchReport.counterpartLabel(BenchReport.LABEL_CANDIDATE))
                && BenchReport.counterpartLabel("harvest6") == null, FINDING + ": counterpart labels");
        helper.assertTrue("A_classic_20260906T000000Z".equals(BenchReport.baseName("A", "classic", "20260906T000000Z")), FINDING + ": the file base name");
        helper.assertTrue(BenchReport.variantFor("frog").equals(BenchReport.LABEL_CLASSIC), FINDING + ": a species without a candidate is classic");
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("orespawn_bench_test");
            List<Path> written = BenchReport.write(tmp, classic, null);
            helper.assertTrue(written.size() == 2 && Files.isRegularFile(written.get(0)) && Files.isRegularFile(written.get(1))
                    && written.get(0).getFileName().toString().equals("A_classic_20260906T000000Z.json")
                    && written.get(1).getFileName().toString().equals("A_classic_20260906T000000Z.md"), FINDING + ": write produces the json and the md, actual " + written);
            JsonObject newest = BenchReport.newest(tmp, "A", BenchReport.LABEL_CLASSIC);
            helper.assertTrue(newest != null && "aaa".equals(newest.get("git_head").getAsString()) && newest.has("_file"), FINDING
                    + ": newest finds the written classic report, actual " + newest);
            helper.assertTrue(BenchReport.newest(tmp, "A", BenchReport.LABEL_CANDIDATE) == null, FINDING + ": no candidate report yet");
            List<Path> paired = BenchReport.write(tmp, candidate, BenchReport.pairing(candidate, newest));
            String md = Files.readString(paired.get(1));
            helper.assertTrue(md.contains("## Pairing") && md.contains("frame_median_regression_percent"), FINDING + ": the paired md carries the pairing table");
        } catch (IOException failed) {
            helper.fail(FINDING + ": temp write failed: " + failed);
        } finally {
            if (tmp != null) {
                try (var stream = Files.walk(tmp)) {
                    stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                } catch (IOException ignored) {
                    // best effort
                }
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ 10: the command gating

    private static void commandGatingAndTree(GameTestHelper helper) {
        boolean enabled = BenchCommand.enabled();
        CommandNode<CommandSourceStack> live = helper.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("orespawn");
        if (!enabled) {
            helper.assertTrue(live == null, FINDING + ": without -D" + BenchCommand.PROPERTY + " no 'orespawn' literal reaches the live dispatcher, actual " + live);
            helper.assertTrue(!BenchHarness.installed(), FINDING + ": without the property the harness listeners are not installed");
        } else {
            helper.assertTrue(live != null && live.getChild("bench") != null, FINDING + ": with the property the live dispatcher carries /orespawn bench");
        }
        CommandDispatcher<CommandSourceStack> fresh = new CommandDispatcher<>();
        BenchCommand.register(fresh);
        CommandNode<CommandSourceStack> orespawn = fresh.getRoot().getChild("orespawn");
        helper.assertTrue(orespawn != null, FINDING + ": register builds the 'orespawn' literal");
        CommandNode<CommandSourceStack> bench = orespawn.getChild("bench");
        helper.assertTrue(bench != null, FINDING + ": ... with the 'bench' literal");
        for (String sub : List.of("scene", "start", "stop", "report", "status")) {
            helper.assertTrue(bench.getChild(sub) != null, FINDING + ": /orespawn bench " + sub + " exists, children " + bench.getChildren());
        }
        CommandNode<CommandSourceStack> scene = bench.getChild("scene").getChild("id");
        helper.assertTrue(scene != null && scene.getChild("count") != null && scene.getChild("count").getChild("state") != null, FINDING
                + ": scene <id> [count] [state]");
        helper.assertTrue(bench.getChild("start").getChild("seconds") != null, FINDING + ": start <seconds>");
        helper.assertTrue(bench.getChild("report").getChild("label") != null, FINDING + ": report [label]");
        CommandSourceStack console = helper.getLevel().getServer().createCommandSourceStack();
        helper.assertTrue(!bench.getRequirement().test(console.withPermission(0)), FINDING + ": permission level 0 is refused at the bench node");
        helper.assertTrue(!bench.getRequirement().test(console.withPermission(1)), FINDING + ": permission level 1 is refused");
        helper.assertTrue(bench.getRequirement().test(console.withPermission(BenchCommand.PERMISSION_LEVEL)), FINDING + ": permission level 2 passes");
        helper.assertTrue("orespawn.dev.bench".equals(BenchCommand.PROPERTY) && BenchCommand.PERMISSION_LEVEL == 2, FINDING + ": the property and level are the documented ones");
        helper.succeed();
    }

    // ------------------------------------------------------------------ 11-12: the server counters under the game-test run's property

    /**
     * Item 17 of the owner's 2026-09-06 rulings: no test seam in production code. The rows that count rely on the
     * game-test run's property instead ({@code build.gradle}, the {@code gameTestServer} run); a run without it
     * (the client run's {@code /test}) stops here, naming the property, rather than reading zeros.
     */
    private static void assertCountersLive(GameTestHelper helper) {
        helper.assertTrue(MHLibCounters.ENABLED, FINDING + ": precondition: this row counts through MHLibCounters.ENABLED, which only the gametest run sets --"
                + " -D" + MHLibCounters.PROPERTY + "=true (build.gradle, runs.gameTestServer: systemProperty 'mhlib.counters', 'true'); the slice (d) test seam"
                + " is gone (item 17, 2026-09-06)");
    }

    private static void serverCountersQueen(GameTestHelper helper) {
        TheQueen queen = null;
        assertCountersLive(helper);
        try {
            queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(24, 1, 24));
            queen.setYRot(0.0F); // wave 5: the fallback offsets below are pinned at yaw 0 (LivingEntity's constructor rolls a random yaw)
            // wave 5 (refuter B): spawnWithNoFreeWill removes the goals and brain behaviours, not gravity -- the noPhysics Queen sinks
            // 0.04704 a tick from her second tick on ((0 - 0.08) x 0.98 x 0.6), which would carry Body1 off the frozen `body1` below by
            // 0.047 >> 1e-6 at the second-tick pin. getGravity() is 0 under noGravity (Entity.getGravity), so she holds her spawn position.
            queen.setNoGravity(true);
            helper.assertTrue(queen.getParts() != null && queen.getParts().length == 10, FINDING + ": precondition: ten parts");
            // Each reset-act-read below is one synchronous stretch of this tick: MHLibMod.onServerTick (the 100-tick dump that
            // zeroes the server list now that the property is live) cannot run between them, so the values stay exact.
            MHLibCounters.sumAndResetServer();
            ((IMultipartEntity<?>) (Object) queen).mhlibAiStep();
            Map<String, Long> after = MHLibCounters.sumAndResetServer();
            helper.assertTrue(after.get("server.align_sub_parts_parts") == 0L, FINDING + ": the Queen's parts are all synched: alignSubParts places none, actual " + after);
            helper.assertTrue(after.get("server.align_synched_parts") == 10L, FINDING + ": alignSynchedSubParts applies the ten synched parts, actual " + after);
            helper.assertTrue(after.get("server.part_setpos") == 10L, FINDING + ": one setPos per applied part from mhlibAiStep alone, actual " + after);
            helper.assertTrue(after.get("server.placement_ns") > 0L, FINDING + ": the placement span is measured, actual " + after);
            helper.assertTrue(after.get("net.s2c_update_packets") == 0L && after.get("net.set_master_packets") == 0L, FINDING
                    + ": mhlibAiStep alone broadcasts nothing (no tracker queued), actual " + after);
            // One full entity tick of a FRESHLY SPAWNED Queen -- the parts' first tick. The aiStep-tail alignment (MixinLivingEntity,
            // alignSynchedSubParts -> applyInformation -> setPos: 10), then the tick-tail part tick (tickParts -> MHLibPartEntity.tick):
            // updateLastPos (a setPos at the current position: 10). 20 on the spawn tick as on every later one. OPT-030 (a) (FIXED 2026-09-06,
            // wave 5): newPosRotationIncrements is initialised to -1 ("no interp target"), so the client-lerp state machine's `== 0` branch --
            // which fired once with the never-set zero target on a part's first tick (setPos(0, 0, 0): the 30 the slice (d) gate found, and the
            // one-tick origin transient of every synched-bone species) -- never runs on the server; readData still arms it on the client with
            // Math.max(updateSteps, 0), the trust-client apply with synchedPartUpdateSteps.
            queen.tick();
            Map<String, Long> tick = MHLibCounters.sumAndResetServer();
            helper.assertTrue(tick.get("server.align_synched_parts") == 10L, FINDING + ": a tick aligns the ten synched parts once, actual " + tick);
            helper.assertTrue(tick.get("server.part_setpos") == 20L, FINDING + ": the SPAWN tick calls setPos twice per part -- the alignment and updateLastPos"
                    + " from the part tick; the first-tick lerp snap to the zero interp target is gone (OPT-030 (a), wave 5: the count starts at -1). The slice"
                    + " (d) gate had pinned 30 with the snap; actual " + tick);
            Entity firstPart = queen.getParts()[0];
            // The transient pin inverted: Body1 (the profile's first part) sits at its fallback offset -- position [0, 14.15, -0.13] minus
            // pivot [0, 3.75, 0.75] at yaw 0, scale 1 (the_queen.json; alignSynchedSubParts' fallback then applyInformation) -- not at the origin.
            Vec3 body1 = queen.position().add(0.0D, 14.15D, -0.13D).subtract(0.0D, 3.75D, 0.75D);
            helper.assertTrue(!(firstPart.getX() == 0.0D && firstPart.getY() == 0.0D && firstPart.getZ() == 0.0D)
                    && firstPart.position().distanceTo(body1) < 1.0E-6D, FINDING + ": after the spawn tick's part tick the Queen's parts sit where the"
                    + " alignment put them (Body1 at its fallback offset " + body1 + "), not at the world origin -- the OPT-030 transient pin inverted (the"
                    + " slice (d) gate had pinned the origin); actual " + firstPart.position());
            queen.tick();
            Map<String, Long> second = MHLibCounters.sumAndResetServer();
            helper.assertTrue(second.get("server.align_synched_parts") == 10L && second.get("server.part_setpos") == 20L, FINDING
                    + ": from the second tick on a tick calls setPos twice per part (the alignment, then updateLastPos from the part tick) -- the steady state the"
                    + " runbook's 400 per Queen per second counts; actual " + second);
            helper.assertTrue(firstPart.position().distanceTo(body1) < 1.0E-6D, FINDING + ": the second tick's alignment keeps the parts on the"
                    + " Queen (Body1 at its fallback offset), actual " + firstPart.position());
        } finally {
            discardQuietly(queen);
        }
        helper.succeed();
    }

    private static void serverCountersModernSpider(GameTestHelper helper) {
        SpiderRobot spider = null;
        assertCountersLive(helper);
        try {
            spider = spawnModernSpider(helper, new BlockPos(24, 1, 24));
            // wave 5 (refuter B): not frozen -- the spider stands on the template floor (rel y 1, F0.7) where the gait's ground scan
            // wants it, and the pins below are counts and a not-at-origin test, none of them position-relative.
            helper.assertTrue(spider.isModernMovement() && spider.getParts() != null && spider.getParts().length == 8, FINDING
                    + ": precondition: a modern spider with eight parts, actual modern=" + spider.isModernMovement());
            // Reset-act-read inside one tick, as in row 11: exact values.
            MHLibCounters.sumAndResetServer();
            ((IMultipartEntity<?>) (Object) spider).mhlibAiStep();
            Map<String, Long> after = MHLibCounters.sumAndResetServer();
            helper.assertTrue(after.get("server.align_sub_parts_parts") == 8L, FINDING + ": alignSubParts places the eight unsynched leg parts, actual " + after);
            helper.assertTrue(after.get("server.align_synched_parts") == 0L, FINDING + ": no synched part on the spider, actual " + after);
            helper.assertTrue(after.get("server.part_setpos") == 8L, FINDING + ": one setPos per placed part from mhlibAiStep alone, actual " + after);
            // The spider's SPAWN tick: the aiStep-tail static alignment (8), each leg part's updateLastPos (8), then SpiderRobot.tick's gait feed
            // after super.tick() (ModernSpiderGait.serverTick -> feedParts: 8): 24 on the spawn tick as after it. The first-tick lerp snap (8) the
            // slice (d) gate counted is gone -- OPT-030 (a) (FIXED 2026-09-06, wave 5), see row 11.
            spider.tick();
            Map<String, Long> tick = MHLibCounters.sumAndResetServer();
            helper.assertTrue(tick.get("server.align_sub_parts_parts") == 8L, FINDING + ": a tick runs the static alignment once, actual " + tick);
            helper.assertTrue(tick.get("server.part_setpos") == 24L, FINDING + ": the SPAWN tick calls setPos three times per leg part: the static alignment,"
                    + " updateLastPos and the gait feed; the first-tick lerp snap is gone (OPT-030 (a), wave 5). The slice (d) gate had pinned 32 with the"
                    + " snap; actual " + tick);
            Entity firstLeg = spider.getParts()[0];
            helper.assertTrue(!(firstLeg.getX() == 0.0D && firstLeg.getY() == 0.0D && firstLeg.getZ() == 0.0D), FINDING + ": the gait feed after super.tick() has"
                    + " the legs on the spider at the end of the spawn tick (and no snap ever moved them off it: OPT-030 (a)), actual " + firstLeg.position());
            helper.assertTrue(tick.get("server.placement_ns") > 0L, FINDING + ": the placement span covers mhlibAiStep and feedParts, actual " + tick);
            spider.tick();
            Map<String, Long> second = MHLibCounters.sumAndResetServer();
            helper.assertTrue(second.get("server.align_sub_parts_parts") == 8L && second.get("server.part_setpos") == 24L, FINDING
                    + ": from the second tick on a tick calls setPos three times per leg part (the static alignment, updateLastPos, the gait feed) -- the steady"
                    + " state the runbook's 480 per robot per second counts; actual " + second);
        } finally {
            discardQuietly(spider);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ 13: the byte accounting

    private static void packetEncodedLengths(GameTestHelper helper) {
        TheQueen queen = null;
        try {
            Map<String, BoneInformation> bones = new LinkedHashMap<>();
            bones.put("Body1", new BoneInformation("Body1", false, new Vec3(1.5D, 2.25D, -3.0D), BoneInformation.DEFAULT_SCALING, new Vec3(0.1D, 0.2D, 0.3D)));
            bones.put("LHead", new BoneInformation("LHead", true, new Vec3(5.05D, 16.3D, 24.29D), new Vec3(2.0D, 2.0D, 2.0D), Vec3.ZERO));
            CPacketBoneInformation c2s = new CPacketBoneInformation(7, bones);
            FriendlyByteBuf manual = new FriendlyByteBuf(Unpooled.buffer());
            int expected;
            try {
                CPacketBoneInformation.STREAM_CODEC.encode(manual, c2s);
                expected = manual.readableBytes();
            } finally {
                manual.release();
            }
            helper.assertTrue(expected > 0 && c2s.encodedLength() == expected, FINDING + ": CPacketBoneInformation.encodedLength is the codec's byte count, expected "
                    + expected + ", actual " + c2s.encodedLength());
            helper.assertTrue(c2s.encodedLength() == expected, FINDING + ": encodedLength is repeatable (its buffer is released), actual " + c2s.encodedLength());
            queen = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(24, 1, 24));
            SPacketUpdateMultipart s2c = new SPacketUpdateMultipart(queen);
            RegistryFriendlyByteBuf manualS2c = new RegistryFriendlyByteBuf(Unpooled.buffer(), queen.registryAccess());
            int expectedS2c;
            try {
                s2c.write(manualS2c);
                expectedS2c = manualS2c.readableBytes();
            } finally {
                manualS2c.release();
            }
            int actualS2c = s2c.encodedLength(queen.registryAccess());
            helper.assertTrue(expectedS2c > 0 && actualS2c == expectedS2c, FINDING + ": SPacketUpdateMultipart.encodedLength is write()'s byte count on a play-channel buffer, expected "
                    + expectedS2c + ", actual " + actualS2c);
            // Ten parts: 4 (id) + 4 (count) + 10 x (3 doubles + 4 floats + 2 booleans = 42) + optional data + 4 (end marker) >= 430 bytes.
            helper.assertTrue(actualS2c >= 4 + 4 + 10 * 42 + 4, FINDING + ": ten fixed part records at least, actual " + actualS2c);
        } finally {
            discardQuietly(queen);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ 14: the collector probe

    private static void collectorProbeSpanAccounting(GameTestHelper helper) {
        MHLibCounters.sumAndResetAll();
        Object key = new Object();
        MHLibCollectorProbe.begin(key);
        helper.assertTrue(MHLibCollectorProbe.isOpen(key), FINDING + ": begin opens the span for its key");
        long[] filler = new long[16384];
        filler[0] = System.nanoTime();
        boolean accounted = MHLibCollectorProbe.end(key);
        Map<String, Long> after = MHLibCounters.sumAndResetAll();
        helper.assertTrue(accounted && !MHLibCollectorProbe.isOpen(key), FINDING + ": end on the open key accounts and closes the span");
        helper.assertTrue(after.get("client.collector_ns") > 0L, FINDING + ": the span's nanoseconds reach client.collector_ns, actual " + after.get("client.collector_ns"));
        helper.assertTrue(!MHLibCollectorProbe.allocationSupported() || after.get("client.collector_alloc_bytes") >= filler.length * 8L, FINDING
                + ": the span's allocated bytes reach client.collector_alloc_bytes (at least the 128 KiB array), actual " + after.get("client.collector_alloc_bytes")
                + " (allocation probe supported: " + MHLibCollectorProbe.allocationSupported() + ")");
        helper.assertTrue(!MHLibCollectorProbe.end(new Object()), FINDING + ": end on a key that is not open accounts nothing");
        Map<String, Long> nothing = MHLibCounters.sumAndResetAll();
        helper.assertTrue(nothing.get("client.collector_ns") == 0L && nothing.get("client.collector_alloc_bytes") == 0L, FINDING
                + ": ... and the counters stay at zero, actual " + nothing);
        Object first = new Object();
        Object second = new Object();
        MHLibCollectorProbe.begin(first);
        MHLibCollectorProbe.begin(second);
        helper.assertTrue(!MHLibCollectorProbe.end(first), FINDING + ": a second begin replaces the slot: the first key's end accounts nothing (an undercount, never a foreign span)");
        helper.assertTrue(MHLibCollectorProbe.end(second), FINDING + ": the second key's end accounts");
        MHLibCounters.sumAndResetAll();
        helper.succeed();
    }

    // ------------------------------------------------------------------ 15: the dump order

    private static void dumpOrderWithTheNewNames(GameTestHelper helper) {
        List<String> client = new ArrayList<>(MHLibCounters.sumAndResetAll().keySet());
        helper.assertTrue(client.size() >= 13 && client.subList(0, 9).equals(NINE), FINDING + ": the nine keep the head of the client dump, actual " + client);
        helper.assertTrue(client.subList(9, 13).equals(FOUR), FINDING + ": the four slice (d) client names follow the nine in order, actual " + client);
        int evictions = client.indexOf("orespawn.geo.evictions");
        helper.assertTrue(evictions < 0 || evictions >= 13, FINDING + ": orespawn.geo.evictions (when registered) follows the four, actual index " + evictions);
        int counters = MHLibCounters.all().size();
        int gauge = client.indexOf("orespawn.geo.managers_held");
        helper.assertTrue(gauge < 0 || gauge >= counters, FINDING + ": the gauge (when registered) follows every counter, actual index " + gauge + " of " + counters);
        for (String name : SERVER_SEVEN) {
            helper.assertTrue(!client.contains(name), FINDING + ": the client dump carries no server name, found " + name);
        }
        List<String> server = new ArrayList<>(MHLibCounters.sumAndResetServer().keySet());
        helper.assertTrue(server.equals(SERVER_SEVEN), FINDING + ": the server dump is exactly the seven server names in order, actual " + server);
        List<String> serverAll = new ArrayList<>();
        for (MHLibCounters.Counter counter : MHLibCounters.serverAll()) {
            serverAll.add(counter.name());
        }
        helper.assertTrue(serverAll.equals(SERVER_SEVEN), FINDING + ": serverAll() lists the same seven, actual " + serverAll);
        String line = MHLibCounters.formatServerDump(300, MHLibCounters.sumAndResetServer());
        helper.assertTrue(line.startsWith("MHLib counters (server, per 100 ticks): server_tick=300 net.s2c_update_packets=0 "), FINDING
                + ": the server INFO line's shape, actual " + line);
        String clientLine = MHLibCounters.formatDump(100, MHLibCounters.sumAndResetAll());
        helper.assertTrue(clientLine.startsWith("MHLib counters (per 100 ticks): client_tick=100 client.frames=0 "), FINDING
                + ": the client INFO line keeps its head, actual " + clientLine);
        // The dump listeners receive what the handlers publish.
        List<String> seen = new ArrayList<>();
        MHLibCounters.DumpListener listener = (side, tick, values) -> seen.add(side + ":" + tick + ":" + values.size());
        MHLibCounters.addDumpListener(listener);
        try {
            MHLibCounters.publishDump(MHLibCounters.SERVER_SIDE, 700, MHLibCounters.sumAndResetServer());
        } finally {
            MHLibCounters.removeDumpListener(listener);
        }
        MHLibCounters.publishDump(MHLibCounters.SERVER_SIDE, 800, MHLibCounters.sumAndResetServer());
        helper.assertTrue(seen.size() == 1 && seen.get(0).equals("server:700:7"), FINDING + ": a registered listener sees one publish and a removed one sees none, actual " + seen);
        helper.succeed();
    }

    // ------------------------------------------------------------------ 16: the statistics

    private static void orderStatistics(GameTestHelper helper) {
        helper.assertTrue(BenchStats.median(new double[]{3.0D, 1.0D, 2.0D}) == 2.0D, FINDING + ": median of an odd count");
        helper.assertTrue(BenchStats.median(new double[]{4.0D, 1.0D, 3.0D, 2.0D}) == 2.5D, FINDING + ": median of an even count is the midpoint");
        double[] hundred = new double[100];
        for (int i = 0; i < 100; i++) {
            hundred[i] = i + 1;
        }
        helper.assertTrue(BenchStats.percentile(hundred, 0.95D) == 95.0D && BenchStats.percentile(hundred, 0.99D) == 99.0D
                && BenchStats.percentile(hundred, 1.0D) == 100.0D, FINDING + ": nearest-rank percentiles");
        // Nearest-rank (BenchStats.percentile: sorted[ceil(p * n) - 1]): p99 of a hundred is the 99th smallest, so ONE 50 ms frame in a
        // hundred is above p99 (it is the max, p100) and the 1 % low stays 1000 / 10 = 100 FPS -- the slice (d) gate (2026-09-06)
        // corrected this row's "20 FPS", which had read the 100th. Two 50 ms frames in two hundred: the 198th smallest is still 10 ms
        // (p99 -> 100 FPS), the 199th is 50 ms (p99.5 -> 20 FPS) and so is the max.
        double[] frames = new double[100];
        java.util.Arrays.fill(frames, 10.0D);
        frames[42] = 50.0D;
        helper.assertTrue(BenchStats.percentile(frames, 0.99D) == 10.0D && BenchStats.percentile(frames, 1.0D) == 50.0D
                && Math.abs(BenchStats.onePercentLowFps(frames) - 100.0D) < EPS, FINDING + ": one 50 ms frame in a hundred is p100, not p99 -- nearest-rank p99"
                + " is the 99th smallest (10 ms) and the 1 % low is 1000 / p99 = 100 FPS, actual " + BenchStats.onePercentLowFps(frames));
        double[] twoHundred = new double[200];
        java.util.Arrays.fill(twoHundred, 10.0D);
        twoHundred[42] = 50.0D;
        twoHundred[142] = 50.0D;
        helper.assertTrue(BenchStats.percentile(twoHundred, 0.99D) == 10.0D && Math.abs(BenchStats.onePercentLowFps(twoHundred) - 100.0D) < EPS
                && BenchStats.percentile(twoHundred, 0.995D) == 50.0D && Math.abs(1000.0D / BenchStats.percentile(twoHundred, 0.995D) - 20.0D) < EPS
                && BenchStats.percentile(twoHundred, 1.0D) == 50.0D, FINDING + ": two 50 ms frames in two hundred: the 198th smallest (p99) is 10 ms -> 100 FPS,"
                + " the 199th (p99.5) and the max are 50 ms -> 20 FPS, actual p99 " + BenchStats.percentile(twoHundred, 0.99D) + " / p99.5 "
                + BenchStats.percentile(twoHundred, 0.995D) + " / max " + BenchStats.percentile(twoHundred, 1.0D));
        helper.assertTrue(Math.abs(BenchStats.mean(new double[]{1.0D, 2.0D, 3.0D}) - 2.0D) < EPS, FINDING + ": mean");
        helper.assertTrue(Double.isNaN(BenchStats.median(new double[0])) && Double.isNaN(BenchStats.percentile(null, 0.5D)), FINDING + ": empty input is NaN, never an exception");
        helper.succeed();
    }

    // ------------------------------------------------------------------ 17: the S2C broadcasts and the election (asynchronous)

    private static void s2cBroadcastsAndElectionCounted(GameTestHelper helper) {
        assertCountersLive(helper);
        final TheQueen[] queen = new TheQueen[1];
        // With the property live, MHLibMod.onServerTick zeroes the server list every 100 server ticks and hands the
        // interval to the dump listeners: a dump inside this row's 39-tick wait would otherwise vanish from the read
        // in the second step, so the row sums what the handler publishes (BenchSession.onDump does the same) and
        // adds the partial it reads itself. The synchronous rows never wait, so they read exact values instead.
        final Map<String, Long> dumped = new LinkedHashMap<>();
        final MHLibCounters.DumpListener listener = (side, tick, values) -> {
            if (MHLibCounters.SERVER_SIDE.equals(side)) {
                for (Map.Entry<String, Long> entry : values.entrySet()) {
                    dumped.merge(entry.getKey(), entry.getValue(), Long::sum);
                }
            }
        };
        helper.runAfterDelay(1L, () -> {
            boolean armed = false;
            try {
                MHLibCounters.addDumpListener(listener);
                queen[0] = helper.spawnWithNoFreeWill(ModEntities.THE_QUEEN.get(), new BlockPos(24, 1, 24));
                Object self = queen[0];
                if (self instanceof IMHLibFieldAccessor<?> access) {
                    access._mhlibAccess_getTrackerQueue().add(UUID.randomUUID());
                }
                MHLibCounters.sumAndResetServer();
                armed = true;
            } finally {
                if (!armed) {
                    // The step failed: leave nothing behind.
                    MHLibCounters.removeDumpListener(listener);
                    discardQuietly(queen[0]);
                }
            }
        });
        helper.runAfterDelay(40L, () -> {
            try {
                Map<String, Long> sums = MHLibCounters.sumAndResetServer();
                for (Map.Entry<String, Long> entry : dumped.entrySet()) {
                    sums.merge(entry.getKey(), entry.getValue(), Long::sum);
                }
                long packets = sums.get("net.s2c_update_packets");
                long bytes = sums.get("net.s2c_update_bytes");
                long masters = sums.get("net.set_master_packets");
                helper.assertTrue(packets >= 1L, FINDING + ": ServerEntity.sendDirtyEntityData runs every updateInterval ticks with or without trackers, so the mixin's broadcast site counts, actual " + sums);
                int floor = new SPacketUpdateMultipart(queen[0]).encodedLength(queen[0].registryAccess());
                helper.assertTrue(bytes >= packets * floor, FINDING + ": every broadcast carried at least the non-dirty payload (" + floor + " B), actual bytes " + bytes + " over " + packets + " packets");
                helper.assertTrue(masters >= 1L, FINDING + ": a queued tracker gets elected: setMasterUUID broadcasts SPacketSetMaster, actual " + sums);
                helper.assertTrue(sums.get("server.align_synched_parts") >= 10L * 30L, FINDING + ": the level ticked the Queen: at least 30 ticks x 10 synched parts, actual " + sums);
            } finally {
                MHLibCounters.removeDumpListener(listener);
                discardQuietly(queen[0]);
            }
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------ 18: git HEAD and the working tree (no git run)

    private static void gitHeadAndWorkingTree(GameTestHelper helper) {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("orespawn_bench_git");
            Path gitDir = tmp.resolve(".git");
            Files.createDirectories(gitDir.resolve("refs/heads"));
            Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main\n", StandardCharsets.UTF_8);
            Files.writeString(gitDir.resolve("refs/heads/main"), "0123456789abcdef0123456789abcdef01234567\n", StandardCharsets.UTF_8);
            helper.assertTrue("0123456789abcdef0123456789abcdef01234567".equals(BenchGit.head(tmp)), FINDING + ": head resolves a symbolic ref through the loose ref, actual " + BenchGit.head(tmp));
            Files.delete(gitDir.resolve("refs/heads/main"));
            Files.writeString(gitDir.resolve("packed-refs"), "# pack-refs with: peeled fully-peeled sorted\nfedcba9876543210fedcba9876543210fedcba98 refs/heads/main\n", StandardCharsets.UTF_8);
            helper.assertTrue("fedcba9876543210fedcba9876543210fedcba98".equals(BenchGit.head(tmp)), FINDING + ": ... and through packed-refs, actual " + BenchGit.head(tmp));
            String noIndex = BenchGit.workingTree(tmp);
            helper.assertTrue(noIndex.startsWith(BenchGit.WORKING_TREE_HEAD_ONLY) && noIndex.contains("no index"), FINDING + ": without an index the working tree is head only, actual " + noIndex);
            Path tracked = tmp.resolve("a.txt");
            Files.writeString(tracked, "hello\n", StandardCharsets.UTF_8);
            Files.write(gitDir.resolve("index"), indexBytes(2, "a.txt", Files.getLastModifiedTime(tracked).to(TimeUnit.SECONDS), (int) Files.size(tracked)));
            String clean = BenchGit.workingTree(tmp);
            helper.assertTrue(clean.startsWith("clean by index stat: 1 tracked"), FINDING + ": a tracked file whose mtime seconds and size match the index reads clean, actual " + clean);
            Files.writeString(tracked, "hello, changed\n", StandardCharsets.UTF_8);
            String dirty = BenchGit.workingTree(tmp);
            helper.assertTrue(dirty.startsWith("DIRTY by index stat: 1 of 1") && dirty.contains("a.txt"), FINDING + ": a changed size reads DIRTY and names the file, actual " + dirty);
            Files.delete(tracked);
            String missing = BenchGit.workingTree(tmp);
            helper.assertTrue(missing.startsWith("DIRTY") && missing.contains("1 missing") && missing.contains("a.txt (missing)"), FINDING + ": a missing tracked file reads DIRTY, actual " + missing);
            Files.write(gitDir.resolve("index"), indexBytes(4, "a.txt", 0L, 0));
            String v4 = BenchGit.workingTree(tmp);
            helper.assertTrue(v4.startsWith(BenchGit.WORKING_TREE_HEAD_ONLY) && v4.contains("version 4"), FINDING + ": an index version 4 (prefix-compressed names) is not read: head only, actual " + v4);
            Files.write(gitDir.resolve("index"), new byte[]{1, 2, 3});
            helper.assertTrue(BenchGit.workingTree(tmp).startsWith(BenchGit.WORKING_TREE_HEAD_ONLY), FINDING + ": garbage in the index is head only, never an exception");
            helper.assertTrue(BenchGit.head(tmp.resolve("nowhere")).equals(BenchGit.UNKNOWN) && BenchGit.workingTree(tmp.resolve("nowhere")).startsWith(BenchGit.WORKING_TREE_HEAD_ONLY), FINDING
                    + ": no repository: unknown head, head-only working tree");
            // The live repository the game-test server runs under: a reading of one of the three shapes, never an exception.
            String live = BenchGit.workingTree(BenchGit.repositoryRoot());
            helper.assertTrue(live.startsWith("clean by index stat") || live.startsWith("DIRTY by index stat") || live.startsWith(BenchGit.WORKING_TREE_HEAD_ONLY), FINDING
                    + ": the live repository reads as clean, DIRTY or head only, actual " + live);
        } catch (IOException failed) {
            helper.fail(FINDING + ": temp .git setup failed: " + failed);
        } finally {
            if (tmp != null) {
                try (var stream = Files.walk(tmp)) {
                    stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
                } catch (IOException ignored) {
                    // best effort
                }
            }
        }
        helper.succeed();
    }

    /** A one-entry git index (v2/v3 layout: 62 fixed bytes, the NUL-terminated name, NUL padding to a multiple of eight, an unverified trailing checksum). */
    private static byte[] indexBytes(int version, String name, long mtimeSeconds, int size) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int unpadded = 62 + nameBytes.length;
        int padded = (unpadded + 8) & ~7;
        ByteBuffer b = ByteBuffer.allocate(12 + padded + 20).order(ByteOrder.BIG_ENDIAN);
        b.put("DIRC".getBytes(StandardCharsets.US_ASCII));
        b.putInt(version);
        b.putInt(1);
        b.putInt((int) mtimeSeconds).putInt(0); // ctime
        b.putInt((int) mtimeSeconds).putInt(0); // mtime
        b.putInt(0).putInt(0);                  // dev, ino
        b.putInt(0100644);                      // a regular file
        b.putInt(0).putInt(0);                  // uid, gid
        b.putInt(size);
        b.put(new byte[20]);                    // the object name (not compared)
        b.putShort((short) nameBytes.length);   // flags: name length, no extended flag
        b.put(nameBytes);
        while (b.position() < 12 + padded) {
            b.put((byte) 0);
        }
        b.put(new byte[20]);                    // the index checksum (not verified)
        return b.array();
    }

    // ------------------------------------------------------------------ helpers

    private static List<Mob> spawnScene(GameTestHelper helper, Vec3 relativeOrigin, BenchScene scene, int count, BenchState state) {
        return BenchSceneSpawner.spawn(helper.getLevel(), helper.absoluteVec(relativeOrigin), LOOK_SOUTH, scene, count, state);
    }

    /** The largest x/z distance of {@code slots} from {@code origin}, computed here independently of the spawner's own figure. */
    private static double farthest(Vec3 origin, List<Vec3> slots) {
        double max = 0.0D;
        for (Vec3 p : slots) {
            max = Math.max(max, Math.hypot(p.x - origin.x, p.z - origin.z));
        }
        return max;
    }

    /** No AI, persistent, tagged, feet on the heightmap top: air at the feet, ground below. */
    private static void assertFrozenOnTheFloor(GameTestHelper helper, Mob mob, String what, BenchScene scene) {
        helper.assertTrue(mob.isNoAi(), FINDING + ": " + what + " is frozen (no AI)");
        helper.assertTrue(mob.isPersistenceRequired(), FINDING + ": " + what + " is persistent");
        helper.assertTrue(mob.getTags().contains(BenchSceneSpawner.TAG), FINDING + ": " + what + " carries the bench tag");
        ServerLevel level = helper.getLevel();
        BlockPos feet = BlockPos.containing(mob.getX(), mob.getY(), mob.getZ());
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, feet.getX(), feet.getZ());
        helper.assertTrue(Math.abs(mob.getY() - top) < EPS, FINDING + ": " + what + " stands at the heightmap top " + top + ", actual y " + mob.getY());
        helper.assertTrue(level.getBlockState(feet).isAir() && !level.getBlockState(feet.below()).isAir(), FINDING + ": " + what
                + " has air at its feet and ground below, actual " + level.getBlockState(feet) + " over " + level.getBlockState(feet.below()));
        helper.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(mob.getYRot() - BenchSceneSpawner.facingYaw(LOOK_SOUTH, scene))) < 0.5F, FINDING
                + ": " + what + " faces the origin, actual yaw " + mob.getYRot());
    }

    private static SpiderRobot spawnModernSpider(GameTestHelper helper, BlockPos pos) {
        boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        OreSpawnConfig.SpiderMovement prior = OreSpawnConfig.SPIDER_MOVEMENT.get();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.SPIDER_MOVEMENT.set(OreSpawnConfig.SpiderMovement.MODERN);
            SpiderRobot spider = helper.spawn(ModEntities.SPIDER_ROBOT.get(), pos);
            spider.setNoAi(true);
            return spider;
        } finally {
            OreSpawnConfig.SPIDER_MOVEMENT.set(prior);
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
        }
    }

    private static String describe(List<Mob> mobs) {
        StringBuilder sb = new StringBuilder();
        for (Mob mob : mobs) {
            sb.append(mob.getType()).append(' ');
        }
        return sb.toString().trim();
    }

    private static void discardAll(List<Mob> mobs) {
        for (Mob mob : mobs) {
            discardQuietly(mob);
        }
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }
}
