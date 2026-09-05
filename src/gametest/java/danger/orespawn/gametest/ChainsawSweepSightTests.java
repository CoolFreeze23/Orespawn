package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.item.Chainsaw;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ITEM-070 (ruled B2, 2026-09-05; MOD-037): the Chainsaw's sweep sight — 1.7.10's air-only ten-sample walk from the player's
 * feet + 1.4 to the target's mid-body (orig UltimateSword.java:198-247 {@code MyCanSee}, the port's {@code Chainsaw.myCanSee}) in
 * classic, vanilla's collision ray ({@code Player.hasLineOfSight}) in modern under {@code [modern] chainsawSweepVanillaSight}
 * (default on, read live per swing). One generated {@link TestFunction} per row ({@code chainsawsweepsighttests.i070_NN_<row>_<what>}),
 * all in the {@code chainsawSweepSight} batch (TEST-003).
 *
 * <p>The rows are ITEM-070's felling-site cases 9b, 4a, 4c, 8c, 6, 10, 12 and 13 with the table's answers: a survival player on
 * the floor, the target 5 blocks south on the same column, the occluder placed on a cell the walk READS — computed by
 * {@link #sweepWalkCells}, orig :198-247 replayed float for float at the layout's actual origin (the ScanSetParityTests row 7
 * idiom: the {@code float} quantisation beyond |2^23| and the {@code (int)} cast's shift toward the origin on a negative axis move
 * the sampled cells, so the cells are derived, never assumed; at a small positive origin the tenth sample is the target's own
 * cell, the fifth its head cell between, the seventh the ground cell before a low target's, the first the head-height cell just
 * ahead of the player (z + 0.5 — never the player's own cell for a target five blocks off) — the table's pictures. The rows place
 * their occluders on replayed sample cells: the pictures are the positive-origin readings, and at the gate's negative y the
 * {@code (int)} truncation reads the cell above the true one, so the facts pinned are the walk's own — a non-air block on a
 * sampled cell stops the walk, the vanilla ray passes collision-less and fluid blocks) — and the sweep driven through the private {@code findSomethingToHit} on the registered
 * Chainsaw, its 56 damage on a frozen 1000-HP target the signal: with the key OFF (the walk) the row's 1.7.10 answer, with the key
 * ON (vanilla's ray) the port's; rows 9b / 4a / 4c / 8c / 6 / 10 not swept by the walk and swept by the ray (a mob standing in
 * short grass; a cobweb in the head cell between; a pig behind ground grass; a pig behind a bottom slab; a cow with water between;
 * a cobweb on the first sampled cell — the sweep dead); row 12 the reverse (a log at a trunk corner the segment enters between
 * two samples: swept by the walk, refused by the ray); row 13 the cast's column shift (a stone on the walk's replayed cell stops it;
 * a stone on the segment's true cell stops it only where the cast reads that cell — the positive quadrant — and is skipped where the
 * cast reads the neighbour toward the origin); and the master switch off forcing the walk with the key on. Every key flip restored
 * in a finally, every block razed, every spawn discarded, every player removed; PlayNicely and pvp never flipped; the spawn shield
 * kept (no row hits the player).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ChainsawSweepSightTests {

    private static final String BATCH = "chainsawSweepSight";
    private static final String TEST_PREFIX = "chainsawsweepsighttests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ITEM-070";

    /** The player on the template floor (feet rel y 1), the target 5 blocks south on the same column — the z axis, whose float grid is finer than x's at the layout's origins. */
    private static final Vec3 PLAYER_POS = new Vec3(20.5, 1.0, 24.5);
    private static final Vec3 TARGET_POS = new Vec3(20.5, 1.0, 29.5);
    /** Row 12: the player three blocks up, so the descending walk clips a corner cell between two samples that the eye ray crosses. */
    private static final Vec3 ELEVATED_PLAYER_POS = new Vec3(20.5, 4.0, 24.5);
    /** Row 12's log: the cell (x 20, y 2, z 27) the exact segment enters between the fifth and the seventh samples and the eye line crosses. */
    private static final BlockPos CORNER_LOG = new BlockPos(20, 2, 27);
    private static final float PREY_HEALTH = 1000.0f;

    // ------------------------------------------------------------------
    // The row table
    // ------------------------------------------------------------------

    private record Row(String name, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + this.name;
        }
    }

    private static List<Row> rows() {
        List<Row> r = new ArrayList<>();
        r.add(new Row("i070_01_9b_tall_mob_standing_in_short_grass_not_swept", h -> occluderRow(h, EntityType.ZOMBIE, 9, Blocks.SHORT_GRASS.defaultBlockState(),
                "9b", "a Zombie standing in short grass — the tenth sample IS the mid-body point and reads the grass")));
        r.add(new Row("i070_02_4a_cobweb_in_head_cell_between_not_swept", h -> occluderRow(h, EntityType.ZOMBIE, 4, Blocks.COBWEB.defaultBlockState(),
                "4a", "a cobweb (collision-less, non-air) in the head cell between the player and a Zombie")));
        r.add(new Row("i070_03_4c_pig_behind_ground_grass_not_swept", h -> occluderRow(h, EntityType.PIG, 6, Blocks.SHORT_GRASS.defaultBlockState(),
                "4c", "short grass on the ground in the cell before the pig's (the seventh sample, z + 3.5) — the walk to a low target's mid-body dips into the ground cells from the fifth sample")));
        r.add(new Row("i070_04_8c_pig_behind_bottom_slab_not_swept", h -> occluderRow(h, EntityType.PIG, 6, Blocks.STONE_SLAB.defaultBlockState(),
                "8c", "a bottom slab on the ground in the cell before the pig's — the eye line at 0.77 and above clears its 0.5 collision")));
        r.add(new Row("i070_05_6_cow_with_water_between_not_swept", h -> occluderRow(h, EntityType.COW, 6, Blocks.WATER.defaultBlockState(),
                "6", "a water cell between the player and a Cow (the cell before the cow's) — the walk reads the fluid, the ray's Fluid.NONE clip ignores it")));
        r.add(new Row("i070_06_10_cobweb_on_first_sampled_cell_sweep_dead", h -> occluderRow(h, EntityType.ZOMBIE, 0, Blocks.COBWEB.defaultBlockState(),
                "10", "a cobweb on the FIRST sampled cell — with the target 5 blocks along z the first sample lands at z + 0.5 (25.0 at a positive origin), the"
                        + " head-height cell just ahead of the player, never the player's own cell 24: the walk dies on its first read and the sweep with it (the"
                        + " table's row-10 picture, the player standing in the cobweb, is this same first-sample read for a target nearer than five blocks)")));
        r.add(new Row("i070_07_12_trunk_corner_graze_swept_by_walk_not_by_ray", ChainsawSweepSightTests::cornerGrazeRow));
        r.add(new Row("i070_08_13_negative_quadrant_cast_reads_neighbour_cell", ChainsawSweepSightTests::negativeQuadrantRow));
        r.add(new Row("i070_09_master_off_forces_walk_with_key_on", ChainsawSweepSightTests::masterOffRow));
        return r;
    }

    /** One test per row: 9 TestFunctions in the {@code chainsawSweepSight} batch. */
    @GameTestGenerator
    public Collection<TestFunction> chainsawSweepSightRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, row)));
        }
        return functions;
    }

    private static void run(GameTestHelper helper, Row row) {
        helper.assertTrue(OreSpawnConfig.MODERN_ENABLED.get() && OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.get(),
                "precondition: the game-test config runs modern with chainsawSweepVanillaSight at its code default (true) — the rows flip the key around their"
                        + " walk half and restore it (" + FINDING + " test setup)");
        row.body().accept(helper);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Rows 9b / 4a / 4c / 8c / 6 / 10 — the walk refuses, the ray admits
    // ------------------------------------------------------------------

    /**
     * The target 5 south of the player; the occluder on the walk's replayed sample {@code index} (0-based): a clear line sweeps the
     * target (the control, the walk); the occluder placed, the walk sweeps it no more and vanilla's ray, under the key, still does.
     */
    private static void occluderRow(GameTestHelper helper, EntityType<? extends Mob> type, int index, BlockState occluder, String tableRow, String what) {
        final boolean priorKey = OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.get();
        Mob prey = null;
        ServerPlayer player = null;
        BlockPos placedRel = null;
        try {
            prey = spawnPrey(helper, type, TARGET_POS);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS));
            List<BlockPos> walk = sweepWalkCells(player, prey);
            helper.assertTrue(walk.size() == 10, "precondition: ten samples — no axis of this walk advances more than a block per step (" + FINDING
                    + " test geometry); got " + walk.size());
            helper.assertTrue(player.hasLineOfSight(prey), "precondition: on a clear floor vanilla's ray sees the target (" + FINDING + " test geometry)");
            helper.assertTrue(walkSees(player, prey), "precondition: on a clear floor the walk sees the target (orig :246) (" + FINDING + " test geometry)");
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(false);
            helper.assertTrue(!OreSpawnConfig.chainsawSweepVanillaSight(), "precondition: the key off reads through the helper as the walk (" + FINDING + " test setup)");
            sweep(player);
            helper.assertTrue(prey.getHealth() < PREY_HEALTH, "control: on a clear line the walk's sweep lands the 56 on the " + type.toShortString() + " ("
                    + FINDING + "); health " + prey.getHealth());
            healPrey(prey);
            BlockPos cell = walk.get(index);
            BlockPos origin = helper.absolutePos(BlockPos.ZERO); // helper.setBlock takes a layout-relative cell: absolute cells are rebased on the origin
            helper.assertTrue(helper.getLevel().getBlockState(cell).isAir(), "precondition: the walk's sample " + (index + 1) + " cell " + cell + " (rel "
                    + cell.subtract(origin) + ") is air before the occluder goes in (" + FINDING + " test geometry)");
            placedRel = cell.subtract(origin);
            helper.setBlock(placedRel, occluder);
            boolean walkSees = walkSees(player, prey);
            helper.assertTrue(!walkSees, "Chainsaw.myCanSee (orig UltimateSword.java:198-247, ITEM-070 row " + tableRow + " — " + what + "): a non-air block on a"
                    + " sampled cell stops the walk (orig :243-244, air alone passes) (" + FINDING + "); the cell " + cell + " holds " + occluder.getBlock().getName().getString());
            sweep(player);
            helper.assertTrue(prey.getHealth() == PREY_HEALTH, "Chainsaw.findSomethingToHit under the walk (classic; ITEM-070 row " + tableRow + "): the "
                    + type.toShortString() + " is NOT swept — 1.7.10 spared it (" + FINDING + "); health " + prey.getHealth());
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(true);
            helper.assertTrue(OreSpawnConfig.chainsawSweepVanillaSight(), "precondition: the key on reads through the helper as the vanilla ray (" + FINDING + " test setup)");
            boolean raySees = player.hasLineOfSight(prey);
            helper.assertTrue(raySees, "Player.hasLineOfSight (the COLLIDER clip, fluids ignored; ITEM-070 row " + tableRow + "): vanilla's ray still admits the target"
                    + " past " + occluder.getBlock().getName().getString() + " (" + FINDING + ")");
            sweep(player);
            helper.assertTrue(prey.getHealth() < PREY_HEALTH, "Chainsaw.findSomethingToHit under the key (modern, MOD-037; ITEM-070 row " + tableRow + "): the "
                    + type.toShortString() + " IS swept by vanilla's ray — the port's pre-ruling behaviour, kept in modern (" + FINDING + "); health " + prey.getHealth());
        } finally {
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(priorKey);
            if (placedRel != null) helper.setBlock(placedRel, Blocks.AIR);
            removePlayer(helper, player);
            discardQuietly(prey);
        }
    }

    // ------------------------------------------------------------------
    // Row 12 — the walk passes a corner the ray enters
    // ------------------------------------------------------------------

    /**
     * The player three blocks up, a Pig 5 south on the floor: the walk descends from feet + 1.4 to the pig's mid-body in half-block
     * z steps and crosses the cell (20, 2, 27) between its fifth and seventh samples without landing in it, while the eye-to-eye
     * ray (steeper start, shallower end) enters that cell too. A log there: the walk sees the pig (swept with the key off), the
     * ray does not (spared with the key on) — 1.7.10 swept around trunk corners the port did not.
     */
    private static void cornerGrazeRow(GameTestHelper helper) {
        final boolean priorKey = OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.get();
        Mob pig = null;
        ServerPlayer player = null;
        boolean placed = false;
        try {
            pig = spawnPrey(helper, EntityType.PIG, TARGET_POS);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(ELEVATED_PLAYER_POS));
            List<BlockPos> walk = sweepWalkCells(player, pig);
            BlockPos logAbs = helper.absolutePos(CORNER_LOG);
            helper.assertTrue(walk.size() == 10 && !walk.contains(logAbs), "precondition: the log's cell " + logAbs + " is none of the walk's ten samples " + walk
                    + " — the segment only grazes it between two samples (" + FINDING + " test geometry)");
            helper.assertTrue(player.hasLineOfSight(pig) && walkSees(player, pig), "precondition: on a clear floor both rays see the pig (" + FINDING + " test geometry)");
            helper.setBlock(CORNER_LOG, Blocks.OAK_LOG);
            placed = true;
            helper.assertTrue(!player.hasLineOfSight(pig), "precondition: the log on the eye line blocks vanilla's ray — the DDA enters every voxel the segment does ("
                    + FINDING + " test geometry)");
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(false);
            helper.assertTrue(walkSees(player, pig), "Chainsaw.myCanSee (orig UltimateSword.java:241-246, ITEM-070 row 12): no sample lands in the grazed corner cell,"
                    + " so the walk passes the log (" + FINDING + ")");
            sweep(player);
            helper.assertTrue(pig.getHealth() < PREY_HEALTH, "Chainsaw.findSomethingToHit under the walk (classic; ITEM-070 row 12): the pig past a trunk corner IS"
                    + " swept, as 1.7.10 swept it (" + FINDING + "); health " + pig.getHealth());
            healPrey(pig);
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(true);
            sweep(player);
            helper.assertTrue(pig.getHealth() == PREY_HEALTH, "Chainsaw.findSomethingToHit under the key (modern, MOD-037; ITEM-070 row 12): vanilla's ray hits the"
                    + " log's full cube and the pig is spared (" + FINDING + "); health " + pig.getHealth());
        } finally {
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(priorKey);
            if (placed) helper.setBlock(CORNER_LOG, Blocks.AIR);
            removePlayer(helper, player);
            discardQuietly(pig);
        }
    }

    // ------------------------------------------------------------------
    // Row 13 — the (int) cast reads the neighbour cell toward the origin on a negative fractional axis
    // ------------------------------------------------------------------

    /**
     * The sixth sample sits at z + 3.0 from the player, half a block into a cell (the layout's z 24.5 + 3.0 = 27.5): where that
     * coordinate is negative the cast reads the cell toward the origin (rel 28), not the cell the point lies in (rel 27); where it is
     * positive the two coincide. A stone on the replayed cell stops the walk in either frame; a stone on the point's TRUE cell — on
     * the eye line, so vanilla's ray refuses it in both frames — stops the walk only where the two cells coincide, and is skipped
     * where the cast reads its neighbour (the table's "a block on the segment is skipped when its +z neighbour is air"). The frame
     * this run sits in is reported; the assertion holds in both.
     */
    private static void negativeQuadrantRow(GameTestHelper helper) {
        final boolean priorKey = OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.get();
        Mob zombie = null;
        ServerPlayer player = null;
        BlockPos placedRel = null;
        try {
            zombie = spawnPrey(helper, EntityType.ZOMBIE, TARGET_POS);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS));
            List<BlockPos> walk = sweepWalkCells(player, zombie);
            helper.assertTrue(walk.size() == 10, "precondition: ten samples (" + FINDING + " test geometry); got " + walk.size());
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            BlockPos replayCell = walk.get(5);
            Vec3 start = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());
            Vec3 end = new Vec3(zombie.getX(), zombie.getY() + zombie.getBbHeight() / 2.0, zombie.getZ());
            Vec3 sixth = start.add(end.subtract(start).scale(0.6));
            BlockPos trueCell = BlockPos.containing(sixth);
            boolean coincide = trueCell.equals(replayCell);
            String frame = "the sixth sample's point " + sixth + " (the layout's z " + (sixth.z - origin.getZ()) + "): the point's cell rel " + trueCell.subtract(origin)
                    + ", the cast's rel " + replayCell.subtract(origin) + " — " + (coincide ? "coincide (a positive or whole-numbered frame)" : "differ (the negative quadrant's shift toward the origin)");
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(false);
            helper.assertTrue(walkSees(player, zombie), "precondition: on a clear floor the walk sees the Zombie (" + FINDING + " test geometry)");
            placedRel = replayCell.subtract(origin);
            helper.assertTrue(helper.getLevel().getBlockState(replayCell).isAir(), "precondition: the replayed sixth cell is air (" + FINDING + " test geometry); " + frame);
            helper.setBlock(placedRel, Blocks.STONE);
            helper.assertTrue(!walkSees(player, zombie), "Chainsaw.myCanSee (orig :242 — getBlock((int) x, (int) y, (int) z), ITEM-070 row 13): a stone on the cell the"
                    + " cast reads stops the walk (" + FINDING + "); " + frame);
            sweep(player);
            helper.assertTrue(zombie.getHealth() == PREY_HEALTH, "Chainsaw.findSomethingToHit under the walk: not swept past the stone on the cast's cell (" + FINDING
                    + "); health " + zombie.getHealth());
            helper.setBlock(placedRel, Blocks.AIR);
            placedRel = trueCell.subtract(origin);
            helper.assertTrue(helper.getLevel().getBlockState(trueCell).isAir(), "precondition: the point's true cell is air (" + FINDING + " test geometry); " + frame);
            helper.setBlock(placedRel, Blocks.STONE);
            boolean walkSees = walkSees(player, zombie);
            helper.assertTrue(walkSees == !coincide, "Chainsaw.myCanSee (orig :242's (int) cast, BUG-027 kept — ITEM-070 row 13): a stone on the segment's TRUE cell"
                    + " stops the walk only where the cast reads that cell, and is skipped where the cast reads its neighbour toward the origin (" + FINDING + "); walk sees="
                    + walkSees + "; " + frame);
            sweep(player);
            helper.assertTrue((zombie.getHealth() < PREY_HEALTH) == !coincide, "Chainsaw.findSomethingToHit under the walk (ITEM-070 row 13): swept exactly where the"
                    + " cast skips the stone (" + FINDING + "); health " + zombie.getHealth() + "; " + frame);
            healPrey(zombie);
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(true);
            helper.assertTrue(!player.hasLineOfSight(zombie), "Player.hasLineOfSight (ITEM-070 row 13): vanilla's exact DDA refuses the stone on the segment's true"
                    + " cell in every frame (" + FINDING + "); " + frame);
            sweep(player);
            helper.assertTrue(zombie.getHealth() == PREY_HEALTH, "Chainsaw.findSomethingToHit under the key (modern): not swept past the stone on the true cell ("
                    + FINDING + "); health " + zombie.getHealth());
        } finally {
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(priorKey);
            if (placedRel != null) helper.setBlock(placedRel, Blocks.AIR);
            removePlayer(helper, player);
            discardQuietly(zombie);
        }
    }

    // ------------------------------------------------------------------
    // The master switch — modern.enabled = false forces the walk with the key on
    // ------------------------------------------------------------------

    /** Row 9b's geometry with the key ON: the master off runs the walk (not swept), the master back on runs the ray (swept). */
    private static void masterOffRow(GameTestHelper helper) {
        final boolean priorKey = OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.get();
        final boolean priorMaster = OreSpawnConfig.MODERN_ENABLED.get();
        Mob zombie = null;
        ServerPlayer player = null;
        BlockPos placedRel = null;
        try {
            zombie = spawnPrey(helper, EntityType.ZOMBIE, TARGET_POS);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS));
            List<BlockPos> walk = sweepWalkCells(player, zombie);
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            placedRel = walk.get(9).subtract(origin);
            helper.setBlock(placedRel, Blocks.SHORT_GRASS);
            helper.assertTrue(!walkSees(player, zombie) && player.hasLineOfSight(zombie), "precondition: row 9b's grass — the walk refuses, the ray admits ("
                    + FINDING + " test geometry)");
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(true);
            OreSpawnConfig.MODERN_ENABLED.set(false);
            helper.assertTrue(!OreSpawnConfig.chainsawSweepVanillaSight(), "precondition: the master off reads through the helper as the walk whatever the key says ("
                    + FINDING + " test setup)");
            sweep(player);
            helper.assertTrue(zombie.getHealth() == PREY_HEALTH, "MOD-037 / modern.enabled = false: classic — the 1.7.10 walk runs with the key on, the Zombie in"
                    + " grass is not swept (" + FINDING + "); health " + zombie.getHealth());
            OreSpawnConfig.MODERN_ENABLED.set(true);
            helper.assertTrue(OreSpawnConfig.chainsawSweepVanillaSight(), "precondition: the master back on defers to the key (" + FINDING + " test setup)");
            sweep(player);
            helper.assertTrue(zombie.getHealth() < PREY_HEALTH, "MOD-037 (key on, master on): vanilla's ray runs and the Zombie in grass is swept (" + FINDING
                    + "); health " + zombie.getHealth());
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(priorMaster);
            OreSpawnConfig.MODERN_CHAINSAW_SWEEP_VANILLA_SIGHT.set(priorKey);
            if (placedRel != null) helper.setBlock(placedRel, Blocks.AIR);
            removePlayer(helper, player);
            discardQuietly(zombie);
        }
    }

    // ------------------------------------------------------------------
    // The walk replayed, the sweep driven, the two rays asked
    // ------------------------------------------------------------------

    /**
     * orig UltimateSword.java:198-246 replayed float for float (the port's {@code Chainsaw.myCanSee}): the cells the walk reads from
     * this player to this target at the layout's actual origin — the start at the player's position and feet + 1.4f (:200-204), the
     * tenth-part steps to the target's mid-body (:205-207), the per-axis normalisation with the {@code (int)} count (:208-240), then
     * each sample pre-incremented and cast with {@code (int)} (:242 — truncation toward zero, BUG-027). The rows place their
     * occluders from this list, so the pins hold at any origin: negative axes, and the {@code float} quantisation beyond |2^23|.
     */
    private static List<BlockPos> sweepWalkCells(Player player, LivingEntity e) {
        int nblks = 10;
        double cx = player.getX();
        double cz = player.getZ();
        float startx = (float) cx;
        float starty = (float) (player.getY() + (double) 1.4f);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - (double) startx) / 10.0);
        float dy = (float) ((e.getY() + (double) (e.getBbHeight() / 2.0f) - (double) starty) / 10.0);
        float dz = (float) ((e.getZ() - (double) startz) / 10.0);
        if ((double) Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) ((float) nblks * Math.abs(dx));
            if (dx > 1.0f) dx = 1.0f;
            if (dx < -1.0f) dx = -1.0f;
        }
        if ((double) Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) ((float) nblks * Math.abs(dy));
            if (dy > 1.0f) dy = 1.0f;
            if (dy < -1.0f) dy = -1.0f;
        }
        if ((double) Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) ((float) nblks * Math.abs(dz));
            if (dz > 1.0f) dz = 1.0f;
            if (dz < -1.0f) dz = -1.0f;
        }
        List<BlockPos> cells = new ArrayList<>();
        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            cells.add(new BlockPos((int) startx, (int) starty, (int) startz));
        }
        return cells;
    }

    /** The registered Chainsaw's private {@code findSomethingToHit(Player)} — orig UltimateSword.java:163-174, the 5-block sweep. */
    private static void sweep(Player player) {
        Chainsaw chainsaw = (Chainsaw) ModItems.CHAINSAW.get();
        invoke(chainsaw, Chainsaw.class, "findSomethingToHit", new Class<?>[] {Player.class}, player);
    }

    /** {@code Chainsaw.myCanSee(Player, LivingEntity)} — package-private, orig :198-247. */
    private static boolean walkSees(Player player, LivingEntity target) {
        return (Boolean) invoke(null, Chainsaw.class, "myCanSee", new Class<?>[] {Player.class, LivingEntity.class}, player, target);
    }

    /** Frozen prey with 1000 HP, so no sweep kills it: goals stripped, noAi, persistence set. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob prey = helper.spawnWithNoFreeWill(type, pos);
        prey.setNoAi(true);
        prey.setPersistenceRequired();
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    /** Back to full health with the hurt cooldown cleared. */
    private static void healPrey(LivingEntity prey) {
        prey.setHealth(PREY_HEALTH);
        prey.invulnerableTime = 0;
        prey.hurtTime = 0;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /**
     * A plain {@link ServerPlayer} put on the player list the way the framework's mock is, without the framework's override
     * (PlayNicelyGateParityTests.survivalServerPlayerAt); the sweep's attacker. The 60-tick spawn shield is kept: no row hits it.
     */
    private static ServerPlayer survivalServerPlayerAt(GameTestHelper helper, Vec3 absolutePos) {
        MinecraftServer server = helper.getLevel().getServer();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "test-survival-player"), false);
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        player.setHealth(PREY_HEALTH);
        player.teleportTo(helper.getLevel(), absolutePos.x, absolutePos.y, absolutePos.z, 0.0f, 0.0f);
        return player;
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    private static Object invoke(Object target, Class<?> declaring, String name, Class<?>[] types, Object... args) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
    }
}
