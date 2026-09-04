package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityStinky;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import danger.orespawn.entity.ai.MyEntityAINearestAttackableTargetGoal;
import danger.orespawn.util.SeasonalDates;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-135 — targeting ledger batch T3b (wave 3): the 1.7.10 scan sets — boxes for spheres, every living thing for
 * players only, the orig cadences, the state gates — restored at the orig positions. One generated {@link TestFunction}
 * per row ({@code scansetparitytests.s135_NN_<species>_<site>}), all in the {@code scanSetParity} batch (TEST-003).
 *
 * <p>Per row the DISCRIMINATING geometry of the ledger's description: where a box replaced a sphere, a survival player (a
 * plain {@link ServerPlayer} on the player list — the framework mock's {@code isCreative()} is hardcoded true) or a pig /
 * zombie at the box corner, beyond the sphere radius (taken with the box, refused by the sphere), and the reverse where the
 * port's sphere was wider (straight above the hunter inside the sphere, outside the box's vertical extent: refused with
 * the box); where players-only scans became living scans, a Zombie taken and orig's ladder run (a pig refused, a villager
 * taken through {@code isAttackableNonMob}, the own kind refused after the player branch, the sight step); cadence rows
 * through the ForcedRoll seam on {@code Entity.random} (the Cater Killer's 1-in-4 pass, the Sea Viper's 1-in-5, the
 * Dragonfly's hunt on a NON-retarget tick, the Ender pair's and the companions' no-roll passes); state-gate rows both
 * ways (the Ant Robot's distmul, the grounded Stinky and its sitting gate, the Ender pair's nearest-then-filter); the
 * Cater Killer's {@code MyCanSee} walk against vanilla's eye ray both ways; the T3b refuters' rows (41-43): orig's grant of a
 * creeper AHEAD of the nearbyOnly reach test (a fence-ringed Creeper taken where row 36's Zombie is refused), the Valentine
 * Player task's grant ahead of the sight step under the {@code SeasonalDates} Feb-14 clock seam (the MOD-036 rows' idiom; a
 * Boyfriend behind the same wall refused), the Dragonfly's near-retarget threshold on the integer cell distSq (2 retargets,
 * 3 hunts). Every probe reads the port's own shape of the
 * orig site — the private {@code findSomethingToAttack} / {@code isSuitableTarget} by reflection on a frozen hunter
 * (TargetScanParityTests), the goals off a {@code spawn} + noAi hunter's target selector (PlayNicelyGateParityTests'
 * GoalProbe), the AI steps under pinned rolls (CreativeGateParityTests' strafe shape). Frozen mobs that must path are
 * set on the ground ({@code setOnGround(true)}: a frozen mob never lands, the T5 refuter B1 precedent); every flip is
 * restored in a finally, every spawn discarded, every player removed; PlayNicely and the difficulty are asserted as
 * preconditions and never flipped. No row pins a hit on a mock player, so no spawn shield is cleared (the Stinky's bite
 * lands on a Zombie).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ScanSetParityTests {

    private static final String BATCH = "scanSetParity";
    private static final String TEST_PREFIX = "scansetparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the templates are named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    /** The 48x34x48 cell for the rows that place a probe straight above a hunter (the empty_large ceiling sits at rel 17). */
    private static final String EMPTY_TALL = OreSpawnMod.MOD_ID + ":empty_tall";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-135";

    /** The hunter on the template floor (the sibling batches' spot). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** The hunter in the south-west corner, so a +x / +z box-corner probe of the widest boxes (30) stays inside the 48-wide cell. */
    private static final BlockPos CORNER_HUNTER_POS = new BlockPos(4, 1, 4);
    /** 8 blocks east on the floor, clear line of sight — inside every box of this batch. */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** 3 blocks east — inside the Stinky's (3 + w/2)^2 bite. */
    private static final BlockPos NEAR_POS = new BlockPos(23, 1, 24);
    /** 2 blocks east — inside the Dragonfly's distSq &lt; 6 bite. */
    private static final BlockPos TOUCH_POS = new BlockPos(22, 1, 24);
    private static final Vec3 PLAYER_8_POS = new Vec3(28.5, 1.0, 24.5);
    private static final Vec3 PLAYER_12_POS = new Vec3(32.5, 1.0, 24.5);
    private static final Vec3 PLAYER_14_POS = new Vec3(34.5, 1.0, 24.5);
    /** A probe's centre sits this far past a box edge (outside) or short of it (inside); a player is 0.6 wide, a pig 0.9. */
    private static final double OUTSIDE_MARGIN = 1.0;
    private static final double INSIDE_MARGIN = 0.5;
    private static final float PREY_HEALTH = 1000.0f;
    /** The vanilla goal's acquisition roll bound, {@code reducedTickDelay(10)} = 5 — pinned to a MISS on the every-pass rows. */
    private static final int GOAL_ROLL_BOUND = 5;
    /** The MOD-036 rows' Feb-14 date for the {@code SeasonalDates} clock seam (row 42). */
    private static final LocalDate VALENTINES = LocalDate.of(2026, 2, 14);

    // ------------------------------------------------------------------
    // The site table
    // ------------------------------------------------------------------

    private record Site(String name, String template, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + this.name;
        }
    }

    private static List<Site> sites() {
        List<Site> s = new ArrayList<>();
        s.add(new Site("s135_01_antrobot_1015_hunt_box_24_unridden_12_ridden", EMPTY_LARGE, ScanSetParityTests::antRobotHuntBox));
        s.add(new Site("s135_02_brutalfly_215_strafe_box_corner_taken", EMPTY_LARGE, h -> strafeCornerTaken(h, ModEntities.ENTITY_BRUTALFLY.get(), EntityBrutalfly.class, 30.0, 20.0, 30.0, 30.0, 4, "Brutalfly.java:215", 200)));
        s.add(new Site("s135_03_brutalfly_215_strafe_box_above_refused", EMPTY_TALL, h -> strafeAboveRefused(h, ModEntities.ENTITY_BRUTALFLY.get(), EntityBrutalfly.class, 30.0, 20.0, 30.0, 30.0, 4, "Brutalfly.java:215", 200)));
        s.add(new Site("s135_04_caterkiller_563_box_scan_zombie_taken_goal_gone", EMPTY_LARGE, h -> livingScanGoalGone(h, ModEntities.ENTITY_CATER_KILLER.get(), "CaterKiller.java:563", "the FOLLOW_RANGE 40 players-only NearestAttackableTargetGoal<Player>")));
        s.add(new Site("s135_05_caterkiller_550_own_kind_refused_after_player_branch", EMPTY_LARGE, h -> ownKindRefused(h, ModEntities.ENTITY_CATER_KILLER.get(), "CaterKiller.java:550-552")));
        s.add(new Site("s135_06_caterkiller_553_556_mob_and_non_mob_ladder", EMPTY_LARGE, h -> preyLadder(h, ModEntities.ENTITY_CATER_KILLER.get(), "CaterKiller.java:553-556")));
        s.add(new Site("s135_07_caterkiller_543_mycansee_walk_not_eye_ray", EMPTY_LARGE, ScanSetParityTests::caterKillerWalk));
        s.add(new Site("s135_08_caterkiller_462_cadence_1_in_4_pass", EMPTY_LARGE, ScanSetParityTests::caterKillerCadence));
        s.add(new Site("s135_09_caterkiller_563_box_20_8_20_pinned", EMPTY_TALL, h -> playerBoxPinned(h, ModEntities.ENTITY_CATER_KILLER.get(), CORNER_HUNTER_POS, 20.0, 8.0, 20.0, "CaterKiller.java:563", "a FOLLOW_RANGE 40 sphere")));
        s.add(new Site("s135_10_dragonfly_142_hunt_on_non_retarget_tick", EMPTY_LARGE, ScanSetParityTests::dragonflyCadence));
        s.add(new Site("s135_11_enderknight_65_nearest_non_starer_shadows_farther_starer", EMPTY_LARGE, h -> enderNearestThenFilter(h, ModEntities.ENDER_KNIGHT.get(), "EnderKnight.java:65-67")));
        s.add(new Site("s135_12_enderknight_61_every_pass_no_acquisition_roll", EMPTY_LARGE, h -> enderEveryPass(h, ModEntities.ENDER_KNIGHT.get(), "EnderKnight.java:61-81")));
        s.add(new Site("s135_13_enderknight_65_plain_sphere_no_visibility_scaling", EMPTY_LARGE, h -> enderPlainSphere(h, ModEntities.ENDER_KNIGHT.get(), "EnderKnight.java:65", 64.0)));
        s.add(new Site("s135_14_enderreaper_65_nearest_non_starer_shadows_farther_starer", EMPTY_LARGE, h -> enderNearestThenFilter(h, ModEntities.ENDER_REAPER.get(), "EnderReaper.java:65-67")));
        s.add(new Site("s135_15_enderreaper_61_every_pass_no_acquisition_roll", EMPTY_LARGE, h -> enderEveryPass(h, ModEntities.ENDER_REAPER.get(), "EnderReaper.java:61-81")));
        s.add(new Site("s135_16_enderreaper_65_plain_sphere_no_visibility_scaling", EMPTY_LARGE, h -> enderPlainSphere(h, ModEntities.ENDER_REAPER.get(), "EnderReaper.java:65", 81.0)));
        s.add(new Site("s135_17_hammerhead_255_living_box_ladder", EMPTY_LARGE, h -> preyLadder(h, ModEntities.HAMMERHEAD.get(), "Hammerhead.java:245-248")));
        s.add(new Site("s135_18_hammerhead_235_238_sight_then_own_kind_refused", EMPTY_LARGE, h -> sightThenOwnKind(h, ModEntities.HAMMERHEAD.get(), "Hammerhead.java:235-237", "Hammerhead.java:238-240")));
        s.add(new Site("s135_19_hammerhead_255_box_18_9_18_corner_taken_above_refused", EMPTY_TALL, h -> playerCornerAndAbove(h, ModEntities.HAMMERHEAD.get(), CORNER_HUNTER_POS, 18.0, 9.0, 18.0, 18.0, "Hammerhead.java:255")));
        s.add(new Site("s135_20_irukandji_294_box_6_4_6_corner_taken_above_refused", EMPTY_LARGE, h -> playerCornerAndAbove(h, ModEntities.IRUKANDJI.get(), HUNTER_POS, 6.0, 4.0, 6.0, 6.0, "Irukandji.java:294")));
        s.add(new Site("s135_21_skate_286_box_10_4_10_corner_taken_above_refused", EMPTY_LARGE, h -> playerCornerAndAbove(h, ModEntities.SKATE.get(), HUNTER_POS, 10.0, 4.0, 10.0, 10.0, "Skate.java:286")));
        s.add(new Site("s135_22_seamonster_517_living_box_ladder", EMPTY_LARGE, h -> preyLadder(h, ModEntities.SEA_MONSTER.get(), "SeaMonster.java:507-510")));
        s.add(new Site("s135_23_seamonster_497_504_sight_then_own_kind_refused", EMPTY_LARGE, h -> sightThenOwnKind(h, ModEntities.SEA_MONSTER.get(), "SeaMonster.java:497-499", "SeaMonster.java:504-506")));
        s.add(new Site("s135_24_seamonster_517_box_16_4_16_corner_taken_above_refused", EMPTY_LARGE, h -> playerCornerAndAbove(h, ModEntities.SEA_MONSTER.get(), HUNTER_POS, 16.0, 4.0, 16.0, 16.0, "SeaMonster.java:517")));
        s.add(new Site("s135_25_seaviper_534_box_scan_zombie_taken_goal_gone", EMPTY_LARGE, h -> livingScanGoalGone(h, ModEntities.SEA_VIPER.get(), "SeaViper.java:534", "the FOLLOW_RANGE 32 players-only NearestAttackableTargetGoal<Player>")));
        s.add(new Site("s135_26_seaviper_521_527_own_kind_and_ladder", EMPTY_LARGE, h -> { ownKindRefused(h, ModEntities.SEA_VIPER.get(), "SeaViper.java:521-523"); preyLadder(h, ModEntities.SEA_VIPER.get(), "SeaViper.java:524-527"); }));
        s.add(new Site("s135_27_seaviper_514_sight_step", EMPTY_LARGE, h -> sightThenOwnKind(h, ModEntities.SEA_VIPER.get(), "SeaViper.java:514-516", "SeaViper.java:521-523")));
        s.add(new Site("s135_28_seaviper_534_box_18_4_18_flat_22_refused_above_refused", EMPTY_TALL, h -> playerBoxPinned(h, ModEntities.SEA_VIPER.get(), HUNTER_POS, 18.0, 4.0, 18.0, "SeaViper.java:534", "a FOLLOW_RANGE 32 sphere")));
        s.add(new Site("s135_29_seaviper_482_cadence_1_in_5_pass", EMPTY_LARGE, ScanSetParityTests::seaViperCadence));
        s.add(new Site("s135_30_stinky_568_grounded_scan_runs_sitting_gate_skips", EMPTY_LARGE, ScanSetParityTests::stinkyStateGate));
        s.add(new Site("s135_31_mothra_224_stage1_box_corner_taken", EMPTY_LARGE, h -> strafeCornerTaken(h, ModEntities.MOTHRA.get(), Mothra.class, 25.0, 20.0, 25.0, 25.0, 4, "Mothra.java:224", 300)));
        s.add(new Site("s135_32_mothra_224_stage1_box_above_refused", EMPTY_TALL, h -> strafeAboveRefused(h, ModEntities.MOTHRA.get(), Mothra.class, 25.0, 20.0, 25.0, 25.0, 4, "Mothra.java:224", 300)));
        s.add(new Site("s135_33_boyfriend_141_box_only_corner_taken_no_sphere", EMPTY_LARGE, h -> companionCorner(h, ModEntities.BOYFRIEND.get(), "Boyfriend.java:141")));
        s.add(new Site("s135_34_boyfriend_138_creeper_goal_20_box_priority_2", EMPTY_LARGE, h -> companionCreeperGoal(h, ModEntities.BOYFRIEND.get(), "Boyfriend.java:138")));
        s.add(new Site("s135_35_boyfriend_53_every_pass_no_acquisition_roll", EMPTY_LARGE, h -> companionEveryPass(h, ModEntities.BOYFRIEND.get(), "Boyfriend.java:141")));
        s.add(new Site("s135_36_boyfriend_117_nearby_only_unreachable_refused", EMPTY_LARGE, h -> companionNearbyOnly(h, ModEntities.BOYFRIEND.get(), "Boyfriend.java:141")));
        s.add(new Site("s135_37_girlfriend_167_box_only_corner_taken_no_sphere", EMPTY_LARGE, h -> companionCorner(h, ModEntities.GIRLFRIEND.get(), "Girlfriend.java:167")));
        s.add(new Site("s135_38_girlfriend_164_creeper_goal_20_box_priority_2", EMPTY_LARGE, h -> companionCreeperGoal(h, ModEntities.GIRLFRIEND.get(), "Girlfriend.java:164")));
        s.add(new Site("s135_39_girlfriend_valentine_60_player_box_corner_taken", EMPTY_LARGE, ScanSetParityTests::valentineCorner));
        s.add(new Site("s135_40_girlfriend_valentine_60_player_above_refused", EMPTY_LARGE, ScanSetParityTests::valentineAbove));
        s.add(new Site("s135_41_boyfriend_111_creeper_granted_before_reach_ringed_taken", EMPTY_LARGE, ScanSetParityTests::companionCreeperBeforeReach));
        s.add(new Site("s135_42_girlfriend_valentine_96_player_unseen_taken_boyfriend_refused", EMPTY_LARGE, ScanSetParityTests::valentineNoSight));
        s.add(new Site("s135_43_dragonfly_124_near_retarget_cell_distsq_3_hunts", EMPTY_LARGE, ScanSetParityTests::dragonflyNearRetarget));
        return s;
    }

    /** One test per row: 43 TestFunctions in the {@code scanSetParity} batch (40 of the T3b lane, 3 of its refuters). */
    @GameTestGenerator
    public Collection<TestFunction> scanSetSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), site.template(), Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    private static void run(GameTestHelper helper, Site site) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — the Stinky's and Dragonfly's passes and the vanilla"
                        + " screens refuse on Peaceful (" + FINDING + " test setup)");
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                "precondition: PlayNicely must be off — every scan of this batch answers nothing under it (ENT-S-115); no row flips it ("
                        + FINDING + " test setup)");
        site.body().accept(helper);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Ant Robot — orig AntRobot.java:1015, distmul 2.0 unridden (:117) / 1.0 ridden (:622)
    // ------------------------------------------------------------------

    /** A pig 20 blocks east: inside the unridden 24/12/24 box (taken), outside the ridden 12/12/12 one (refused); at 8 blocks both take it. */
    private static void antRobotHuntBox(GameTestHelper helper) {
        Mob ant = null;
        Mob pig = null;
        try {
            ant = spawnFrozen(helper, ModEntities.ANT_ROBOT.get(), CORNER_HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, CORNER_HUNTER_POS.east(20));
            assertSees(helper, ant, pig, "a pig 20 blocks east");
            double dist = ant.distanceTo(pig);
            helper.assertTrue(dist > 12.0 + ant.getBbWidth() / 2.0 + pig.getBbWidth() / 2.0 && dist < 24.0,
                    "precondition: the pig's centre " + dist + " blocks off lies past the ridden 12-box and inside the unridden 24-box (" + FINDING + " test geometry)");
            LivingEntity unridden = antHunt(ant, 2.0f);
            helper.assertTrue(unridden == pig, "orig AntRobot.java:1015 with :117 — the unridden hunt's box is (12 * 2.0) x 12 x (12 * 2.0): a pig 20 blocks off is the"
                    + " pick, where HEAD's fixed inflate(12, 12, 12) never saw it (" + FINDING + "); got " + describe(unridden));
            LivingEntity ridden = antHunt(ant, 1.0f);
            helper.assertTrue(ridden == null, "orig AntRobot.java:1015 with :622 — the ridden hunt's box is 12 x 12 x 12: the same pig 20 blocks off is not found ("
                    + FINDING + "); got " + describe(ridden));
            pig.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(CORNER_HUNTER_POS.east(8))));
            helper.assertTrue(antHunt(ant, 1.0f) == pig, "control: the ridden 12-box takes the pig moved to 8 blocks off (" + FINDING + ")");
        } finally {
            discardQuietly(pig);
            discardQuietly(ant);
        }
    }

    /** {@code AntRobot.findSomethingToAttack(float distmul)} by reflection (orig :1011-1026). */
    private static LivingEntity antHunt(Mob ant, float distmul) {
        return (LivingEntity) invoke(ant, ant.getClass(), "findSomethingToAttack", new Class<?>[] {float.class}, distmul);
    }

    // ------------------------------------------------------------------
    // The Brutalfly strafe and Mothra's stage 1 — orig :215 / :224, findNearestEntityWithinAABB(EntityPlayer, box)
    // ------------------------------------------------------------------

    /**
     * The flier frozen in the south-west corner, its flight target parked 10 above (past the reselection distance), the
     * reselection roll pinned quiet, the strafe roll pinned to fire, the fire and mob-hunt rolls quiet: a survival player at
     * the strafe box's +x/+z corner — inside the box, beyond the sphere HEAD searched — is marked (the flight target at the
     * player's mark, orig :219 / :228).
     */
    private static void strafeCornerTaken(GameTestHelper helper, EntityType<? extends Mob> type, Class<? extends Mob> declaring,
                                          double bx, double by, double bz, double sphere, int markUp, String cite, int reselectBound) {
        Mob flier = null;
        ServerPlayer player = null;
        try {
            flier = spawnFrozen(helper, type, CORNER_HUNTER_POS);
            assertMothraGates(helper, flier);
            BlockPos parked = flier.blockPosition().above(10);
            writeField(flier, declaring, "currentFlightTarget", parked);
            AABB box = flier.getBoundingBox().inflate(bx, by, bz);
            Vec3 corner = new Vec3(box.maxX - INSIDE_MARGIN, flier.getY(), box.maxZ - INSIDE_MARGIN); // the entity box is absolute, as the player spot must be
            player = survivalServerPlayerAt(helper, corner);
            helper.assertTrue(box.intersects(player.getBoundingBox()), "precondition: the player at the +x/+z corner meets the " + (int) bx + "/" + (int) by
                    + "/" + (int) bz + " strafe box (" + FINDING + " test geometry)");
            double dist = flier.distanceTo(player);
            helper.assertTrue(dist > sphere, "precondition: the corner player " + dist + " blocks off lies beyond the " + sphere + " sphere HEAD's"
                    + " getNearestPlayer searched (" + FINDING + " test geometry)");
            assertSees(helper, flier, player, "the corner player");
            int shoot = helper.getLevel().getDifficulty() == Difficulty.HARD ? 2 : 3;
            replaceRandom(flier, rolls(reselectBound, 1, 6, 0, 10, 0, shoot, 1, 3, 1, 2, 1));
            invokeAiStep(flier, declaring);
            BlockPos after = (BlockPos) readField(flier, declaring, "currentFlightTarget");
            BlockPos mark = new BlockPos((int) player.getX(), (int) player.getY() + markUp, (int) player.getZ()); // orig :219 / Mothra :228 — (int) casts, truncation toward zero (BUG-027): a cell off a floor on a negative axis
            helper.assertTrue(mark.equals(after), "orig " + cite + " — findNearestEntityWithinAABB(EntityPlayer.class, box " + (int) bx + "/" + (int) by
                    + "/" + (int) bz + ") takes a player at the box's corner, " + String.format("%.1f", dist) + " blocks off: the strafe marks it (flight target "
                    + mark + ") where HEAD's " + sphere + "-sphere left the parked target " + parked + " (" + FINDING + "); got " + after);
        } finally {
            removePlayer(helper, player);
            discardQuietly(flier);
        }
    }

    /**
     * The reverse: a survival player straight above the flier, inside HEAD's sphere but above the strafe box's +y extent, is
     * NOT marked (the parked flight target stands); moved just inside the box's top he is.
     */
    private static void strafeAboveRefused(GameTestHelper helper, EntityType<? extends Mob> type, Class<? extends Mob> declaring,
                                           double bx, double by, double bz, double sphere, int markUp, String cite, int reselectBound) {
        Mob flier = null;
        ServerPlayer player = null;
        try {
            flier = spawnFrozen(helper, type, HUNTER_POS);
            assertMothraGates(helper, flier);
            BlockPos parked = flier.blockPosition().above(10);
            writeField(flier, declaring, "currentFlightTarget", parked);
            AABB box = flier.getBoundingBox().inflate(bx, by, bz);
            Vec3 above = new Vec3(flier.getX(), box.maxY + OUTSIDE_MARGIN, flier.getZ());
            player = survivalServerPlayerAt(helper, above);
            helper.assertTrue(!box.intersects(player.getBoundingBox()), "precondition: the player " + (box.maxY + OUTSIDE_MARGIN - flier.getY())
                    + " above the flier's feet lies past the box's +y edge (" + FINDING + " test geometry)");
            double dist = flier.distanceTo(player);
            helper.assertTrue(dist < sphere, "precondition: the same player " + dist + " blocks off lies inside the " + sphere + " sphere HEAD searched ("
                    + FINDING + " test geometry)");
            assertSees(helper, flier, player, "the player straight above");
            int shoot = helper.getLevel().getDifficulty() == Difficulty.HARD ? 2 : 3;
            replaceRandom(flier, rolls(reselectBound, 1, 6, 0, 10, 0, shoot, 1, 3, 1, 2, 1));
            invokeAiStep(flier, declaring);
            BlockPos after = (BlockPos) readField(flier, declaring, "currentFlightTarget");
            helper.assertTrue(parked.equals(after), "orig " + cite + " — the strafe box reaches " + (int) by + " above the flier's box: a player "
                    + String.format("%.1f", dist) + " straight up is outside it and NOT marked, where HEAD's " + sphere + "-sphere strafed him — the parked"
                    + " flight target " + parked + " must stand (" + FINDING + "); got " + after);
            Vec3 inside = new Vec3(flier.getX(), box.maxY - INSIDE_MARGIN, flier.getZ());
            player.teleportTo(helper.getLevel(), inside.x, inside.y, inside.z, 0.0f, 0.0f);
            helper.assertTrue(box.intersects(player.getBoundingBox()), "control precondition: moved just inside the box's top the player meets it (" + FINDING + ")");
            flier.getSensing().tick();
            replaceRandom(flier, rolls(reselectBound, 1, 6, 0, 10, 0, shoot, 1, 3, 1, 2, 1));
            invokeAiStep(flier, declaring);
            after = (BlockPos) readField(flier, declaring, "currentFlightTarget");
            BlockPos mark = new BlockPos((int) player.getX(), (int) player.getY() + markUp, (int) player.getZ()); // orig :219 / Mothra :228 — (int) casts (BUG-027)
            helper.assertTrue(mark.equals(after), "control: just inside the box's top the player is marked (" + FINDING + "); got " + after + ", mark " + mark);
        } finally {
            removePlayer(helper, player);
            discardQuietly(flier);
        }
    }

    private static void assertMothraGates(GameTestHelper helper, Mob flier) {
        if (flier instanceof Mothra) {
            helper.assertTrue(!OreSpawnConfig.MOTHRA_PEACEFUL.get(), "precondition: MothraPeaceful is off (orig Mothra.java:222) (" + FINDING + " test setup)");
        }
    }

    // ------------------------------------------------------------------
    // The living scans restored for players-only sites — the Cater Killer, Hammerhead, Sea Monster, Sea Viper
    // ------------------------------------------------------------------

    /** A Zombie 8 blocks east is the scan's pick (HEAD's players-only path never saw it) and no NearestAttackableTargetGoal remains. */
    private static void livingScanGoalGone(GameTestHelper helper, EntityType<? extends Mob> type, String cite, String oldPath) {
        Mob hunter = null;
        Mob zombie = null;
        try {
            hunter = spawnWithGoals(helper, type, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            for (Goal goal : targetGoals(hunter)) {
                helper.assertTrue(!(goal instanceof NearestAttackableTargetGoal<?>), name + " must carry no NearestAttackableTargetGoal on its target"
                        + " selector any more: orig " + cite + " searches by an EntityLivingBase box scan, and " + oldPath + " gave way to it (" + FINDING
                        + "); found " + goal.getClass().getSimpleName());
            }
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, hunter, zombie, "a Zombie 8 blocks east");
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == zombie, name + ".findSomethingToAttack (orig " + cite + "): a Zombie 8 blocks off, inside the box and in sight, must be"
                    + " the pick — 1.7.10 scanned EntityLivingBase.class; " + oldPath + " never saw it (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(zombie);
            discardQuietly(hunter);
        }
    }

    /** The hunter's own kind on the prey spot is refused (after the player branch); a Zombie on the same spot is taken. */
    private static void ownKindRefused(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob hunter = null;
        Mob kin = null;
        Mob zombie = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            kin = spawnPrey(helper, type, PREY_POS);
            assertSees(helper, hunter, kin, "a second " + name + " 8 blocks east");
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack: a " + name + " alone in the box must leave the scan empty — orig " + cite
                    + " refuses its own kind, after the player branch (" + FINDING + "); got " + describe(pick));
            kin.discard();
            kin = null;
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, hunter, zombie, "a Zombie 8 blocks east");
            pick = scan(hunter);
            helper.assertTrue(pick == zombie, "control: a Zombie on the same spot is the pick, so the " + name + " was refused by its own step and not by"
                    + " geometry or sight (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(zombie);
            discardQuietly(kin);
            discardQuietly(hunter);
        }
    }

    /** Orig's ladder tail: a pig refused (no EntityMob, not on the attackable-non-mob list), a villager taken (orig MyUtils.java:111), a Zombie taken (EntityMob). */
    private static void preyLadder(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob hunter = null;
        Mob prey = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            prey = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, hunter, prey, "a pig 8 blocks east");
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack: a pig alone in the box must leave the scan empty — orig " + cite + "'s ladder"
                    + " takes EntityMob and the attackable-non-mob list, and a pig is neither (" + FINDING + "); got " + describe(pick));
            prey.discard();
            prey = spawnPrey(helper, EntityType.VILLAGER, PREY_POS);
            assertSees(helper, hunter, prey, "a villager 8 blocks east");
            pick = scan(hunter);
            helper.assertTrue(pick == prey, name + ".findSomethingToAttack: a villager is prey through the attackable-non-mob list (orig " + cite
                    + " -> MyUtils.java:111) — HEAD's players-only path never took one (" + FINDING + "); got " + describe(pick));
            prey.discard();
            prey = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, hunter, prey, "a Zombie 8 blocks east");
            pick = scan(hunter);
            helper.assertTrue(pick == prey, name + ".findSomethingToAttack: a Zombie is prey through the EntityMob step (orig " + cite + ") (" + FINDING
                    + "); got " + describe(pick));
        } finally {
            discardQuietly(prey);
            discardQuietly(hunter);
        }
    }

    /** The sight step: a Zombie behind the stone wall is refused, taken once it is razed; the hunter's own kind in clear sight refused. */
    private static void sightThenOwnKind(GameTestHelper helper, EntityType<? extends Mob> type, String sightCite, String kindCite) {
        Mob hunter = null;
        Mob zombie = null;
        Mob kin = null;
        boolean wallUp = false;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            setWall(helper, Blocks.STONE);
            wallUp = true;
            hunter.getSensing().tick();
            helper.assertTrue(!hunter.hasLineOfSight(zombie), "precondition: the stone wall at x = 24 hides the Zombie 8 blocks east (" + FINDING + " test geometry)");
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack: a Zombie behind the wall must leave the scan empty — orig " + sightCite
                    + "'s canSee refuses it ahead of every species step (" + FINDING + "); got " + describe(pick));
            setWall(helper, Blocks.AIR);
            wallUp = false;
            hunter.getSensing().tick();
            assertSees(helper, hunter, zombie, "the Zombie, the wall razed");
            pick = scan(hunter);
            helper.assertTrue(pick == zombie, "control: razed, the same Zombie is the pick (" + FINDING + "); got " + describe(pick));
            zombie.discard();
            zombie = null;
            kin = spawnPrey(helper, type, PREY_POS);
            hunter.getSensing().tick();
            assertSees(helper, hunter, kin, "a second " + name + " 8 blocks east");
            pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack: its own kind in clear sight is refused (orig " + kindCite + ") (" + FINDING
                    + "); got " + describe(pick));
        } finally {
            if (wallUp) setWall(helper, Blocks.AIR);
            discardQuietly(kin);
            discardQuietly(zombie);
            discardQuietly(hunter);
        }
    }

    /**
     * Box versus sphere with a survival player: at the box's +x/+z corner (beyond the sphere radius HEAD searched) the
     * player is the pick; straight above, inside the sphere but past the box's +y edge, he is not — and just inside that
     * edge he is again.
     */
    private static void playerCornerAndAbove(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos hunterPos, double bx, double by, double bz,
                                             double sphere, String cite) {
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, type, hunterPos);
            String name = hunter.getClass().getSimpleName();
            AABB box = hunter.getBoundingBox().inflate(bx, by, bz);
            Vec3 corner = new Vec3(box.maxX - INSIDE_MARGIN, hunter.getY(), box.maxZ - INSIDE_MARGIN);
            player = survivalServerPlayerAt(helper, corner);
            helper.assertTrue(box.intersects(player.getBoundingBox()), "precondition: the corner player meets the " + (int) bx + "/" + (int) by + "/" + (int) bz
                    + " box (" + FINDING + " test geometry)");
            double dist = hunter.distanceTo(player);
            helper.assertTrue(dist > sphere, "precondition: the corner player " + dist + " blocks off lies beyond the " + sphere + " sphere HEAD searched ("
                    + FINDING + " test geometry)");
            assertSees(helper, hunter, player, "the corner player");
            LivingEntity pick = scan(hunter);
            helper.assertTrue(pick == player, name + ".findSomethingToAttack (orig " + cite + " — a box, getEntitiesWithinAABB): a survival player at the box's"
                    + " corner, " + String.format("%.1f", dist) + " blocks off, is the pick where HEAD's " + sphere + "-sphere getNearestPlayer refused him ("
                    + FINDING + "); got " + describe(pick));
            Vec3 above = new Vec3(hunter.getX(), box.maxY + OUTSIDE_MARGIN, hunter.getZ());
            player.teleportTo(helper.getLevel(), above.x, above.y, above.z, 0.0f, 0.0f);
            hunter.getSensing().tick();
            helper.assertTrue(!box.intersects(player.getBoundingBox()), "precondition: straight above, the player lies past the box's +y edge (" + FINDING + " test geometry)");
            dist = hunter.distanceTo(player);
            helper.assertTrue(dist < sphere, "precondition: the same player " + dist + " up lies inside the " + sphere + " sphere HEAD searched (" + FINDING + " test geometry)");
            assertSees(helper, hunter, player, "the player straight above");
            pick = scan(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack (orig " + cite + "): the box reaches " + (int) by + " above the hunter's box — a player "
                    + String.format("%.1f", dist) + " straight up is outside it and NOT the pick, where HEAD's sphere took him (" + FINDING + "); got " + describe(pick));
            Vec3 inside = new Vec3(hunter.getX(), box.maxY - INSIDE_MARGIN, hunter.getZ());
            player.teleportTo(helper.getLevel(), inside.x, inside.y, inside.z, 0.0f, 0.0f);
            hunter.getSensing().tick();
            helper.assertTrue(box.intersects(player.getBoundingBox()), "control precondition: just inside the box's top the player meets it (" + FINDING + ")");
            pick = scan(hunter);
            helper.assertTrue(pick == player, "control: just inside the box's +y edge the player is the pick (" + FINDING + "); got " + describe(pick));
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
    }

    /**
     * The box pinned against a wider sphere: a survival player just past the box's +x edge (well inside the old sphere) is
     * refused and just inside it taken; the same on +y.
     */
    private static void playerBoxPinned(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos hunterPos, double bx, double by, double bz,
                                        String cite, String oldShape) {
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, type, hunterPos);
            hunter.setYRot(0.0f); // the Cater Killer's walk starts 2.5 blocks ahead along the yaw: pinned south so the samples are the same every run
            String name = hunter.getClass().getSimpleName();
            AABB box = hunter.getBoundingBox().inflate(bx, by, bz);
            double x = hunter.getX();
            double y = hunter.getY();
            double z = hunter.getZ();
            Vec3[] spots = {
                    new Vec3(box.maxX + OUTSIDE_MARGIN, y, z),
                    new Vec3(box.maxX - INSIDE_MARGIN, y, z),
                    new Vec3(x, box.maxY + OUTSIDE_MARGIN, z),
                    new Vec3(x, box.maxY - INSIDE_MARGIN, z),
            };
            boolean[] inside = {false, true, false, true};
            String[] where = {"just past the +x edge", "just inside the +x edge", "just past the +y edge", "just inside the +y edge"};
            player = survivalServerPlayerAt(helper, spots[0]);
            for (int i = 0; i < spots.length; i++) {
                player.teleportTo(helper.getLevel(), spots[i].x, spots[i].y, spots[i].z, 0.0f, 0.0f);
                hunter.getSensing().tick();
                helper.assertTrue(box.intersects(player.getBoundingBox()) == inside[i], "precondition: the player " + where[i] + " of the " + (int) bx + "/"
                        + (int) by + "/" + (int) bz + " box must " + (inside[i] ? "" : "not ") + "meet it (" + FINDING + " test geometry)");
                assertSees(helper, hunter, player, "the player " + where[i]);
                LivingEntity pick = scan(hunter);
                if (inside[i]) {
                    helper.assertTrue(pick == player, name + ".findSomethingToAttack (orig " + cite + "): the survival player " + where[i] + " is the pick ("
                            + FINDING + "); got " + describe(pick));
                } else {
                    helper.assertTrue(pick == null, name + ".findSomethingToAttack (orig " + cite + "): the survival player " + where[i] + ", "
                            + String.format("%.1f", hunter.distanceTo(player)) + " blocks off, is NOT the pick — inside " + oldShape + " HEAD searched, outside"
                            + " orig's box (" + FINDING + "); got " + describe(pick));
                }
            }
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
    }

    // ------------------------------------------------------------------
    // The Cater Killer's own rows — orig CaterKiller.java:543 MyCanSee, :462 the 1-in-4 pass
    // ------------------------------------------------------------------

    /**
     * orig CaterKiller.java:626-676 MyCanSee — a block walk from 2.5 blocks ahead of the body (yaw pinned south: the start is
     * (x, y + 3, z + 2.5)) to the target's mid-height, not vanilla's eye-to-eye ray. The cells the walk reads depend on the
     * layout's absolute origin, so the row replays orig's arithmetic on the hunter's and the Zombie's actual positions
     * ({@link #walkCells}) instead of naming cells: orig's {@code (int)} casts truncate toward zero (BUG-027 faithful — on a
     * negative axis a sample's cell is the one nearer the origin), and orig's {@code float} samples lose the half-block
     * beyond |2^23| (a layout near x = -10^7 drops the start's .5 and lands every step on a whole block). With the Cater
     * Killer at rel (20.5, 1, 24.5) and the Zombie at (28.5, 1, 24.5) the fifth sample is rel (24, 2, 25) at a small positive
     * origin above y 0 and rel (26, 3, 26) at the gate's (-10172895, -60, -3456060); the walk's z runs from z + 2.5 back to
     * the Zombie's z, so that sample sits south of the eye line (z 24.5) at any origin: stoned, the filter refuses the Zombie
     * while {@code hasLineOfSight} still sees it. Then a stone on the eye line — the cell around the eye ray's midpoint,
     * never a walk sample (asserted) — hides the Zombie from the eye ray while the walk, and the filter, still take it.
     */
    private static void caterKillerWalk(GameTestHelper helper) {
        Mob cater = null;
        Mob zombie = null;
        BlockPos onWalk = null;
        BlockPos onRay = null;
        try {
            cater = spawnFrozen(helper, ModEntities.ENTITY_CATER_KILLER.get(), HUNTER_POS);
            cater.setYRot(0.0f);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, cater, zombie, "a Zombie 8 blocks east");
            helper.assertTrue(filter(cater, zombie), "control: on a clear floor the Zombie passes the filter — the walk sees it (orig CaterKiller.java:543) (" + FINDING + ")");
            BlockPos origin = helper.absolutePos(BlockPos.ZERO); // helper.setBlock takes a layout-relative cell: absolute cells are rebased on the origin (row 1's absolutePos idiom)
            List<BlockPos> walk = walkCells(cater, zombie);
            helper.assertTrue(walk.size() == 10, "precondition: ten samples — no axis of this walk advances more than a block per step (" + FINDING + " test geometry); got " + walk.size());
            onWalk = walk.get(4).subtract(origin);
            helper.setBlock(onWalk, Blocks.STONE);
            helper.assertTrue(cater.hasLineOfSight(zombie), "precondition: the stone on the walk's fifth sample (rel " + onWalk + ", abs " + walk.get(4) + ") is off the"
                    + " eye line (z 24.5): vanilla's ray still sees the Zombie (" + FINDING + " test geometry)");
            helper.assertTrue(!filter(cater, zombie), "orig CaterKiller.java:543-545 with :626-676 — MyCanSee marches ten samples from (x, y + 3, z + 2.5) to the"
                    + " Zombie's mid-height; a stone on its fifth sample (rel " + onWalk + ") refuses the Zombie that vanilla's eye ray (HEAD's goal) still admitted ("
                    + FINDING + ")");
            helper.setBlock(onWalk, Blocks.AIR);
            onWalk = null;
            Vec3 rayMid = new Vec3((cater.getX() + zombie.getX()) / 2.0, (cater.getEyeY() + zombie.getEyeY()) / 2.0, (cater.getZ() + zombie.getZ()) / 2.0);
            BlockPos rayCell = BlockPos.containing(rayMid); // the cell holding the eye ray's midpoint (LivingEntity.hasLineOfSight clips eye to eye) — the world's cell, floored
            helper.assertTrue(!walk.contains(rayCell), "precondition: the eye-line cell " + rayCell + " is none of the walk's samples " + walk + " (" + FINDING + " test geometry)");
            onRay = rayCell.subtract(origin);
            helper.setBlock(onRay, Blocks.STONE);
            cater.getSensing().tick();
            helper.assertTrue(!cater.hasLineOfSight(zombie), "precondition: the stone at rel " + onRay + " sits on the eye line — vanilla's ray is blocked (" + FINDING
                    + " test geometry)");
            helper.assertTrue(filter(cater, zombie), "orig CaterKiller.java:543-545 — the walk passes beside the block on the eye line: the Zombie vanilla's ray"
                    + " (HEAD's goal) refused is taken (" + FINDING + ")");
        } finally {
            if (onWalk != null) helper.setBlock(onWalk, Blocks.AIR);
            if (onRay != null) helper.setBlock(onRay, Blocks.AIR);
            discardQuietly(zombie);
            discardQuietly(cater);
        }
    }

    /**
     * orig CaterKiller.java:627-671 replayed float for float (the port's EntityCaterKiller.myCanSee :426-461): the cells the
     * walk reads from this hunter to this target at the layout's actual origin — the yaw start (:629-630), y + 3 (:632), the
     * tenth-part steps (:634-636), the per-axis normalisation (:637-669), then each sample pre-incremented and cast with
     * {@code (int)} (:671 — truncation toward zero, BUG-027). Row 7 places its stones from this list, so the pin holds at any
     * origin: negative axes, and the {@code float} quantisation beyond |2^23|.
     */
    private static List<BlockPos> walkCells(Mob hunter, LivingEntity target) {
        double xzoff = 2.5;
        int nblks = 10;
        double cx = hunter.getX() - xzoff * Math.sin(Math.toRadians(hunter.getYRot()));
        double cz = hunter.getZ() + xzoff * Math.cos(Math.toRadians(hunter.getYRot()));
        float startx = (float) cx;
        float starty = (float) (hunter.getY() + 3.0);
        float startz = (float) cz;
        float dx = (float) ((target.getX() - (double) startx) / 10.0);
        float dy = (float) ((target.getY() + (double) (target.getBbHeight() / 2.0f) - (double) starty) / 10.0);
        float dz = (float) ((target.getZ() - (double) startz) / 10.0);
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

    /** orig CaterKiller.java:462 — the scan runs inside the 1-in-4 pass: pinned to miss, nothing is picked; pinned to fire, the Zombie is the pick under the mark. */
    private static void caterKillerCadence(GameTestHelper helper) {
        Mob cater = null;
        Mob zombie = null;
        try {
            cater = spawnFrozen(helper, ModEntities.ENTITY_CATER_KILLER.get(), HUNTER_POS);
            cater.setYRot(0.0f);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, cater, zombie, "a Zombie 8 blocks east");
            helper.assertTrue(cater.getTarget() == null, "precondition: an empty slot (" + FINDING + " test setup)");
            // the pass 4 -> 1 misses; the 1-in-200, the tree-eat rolls (8, 30) and the cobweb quiet
            replaceRandom(cater, rolls(4, 1, 200, 1, 8, 1, 30, 1));
            invokeAiStep(cater, cater.getClass());
            helper.assertTrue(cater.getTarget() == null, "orig CaterKiller.java:462 — no pass on a missed nextInt(4): nothing scanned, the slot empty (" + FINDING
                    + "); slot " + describe(cater.getTarget()));
            replaceRandom(cater, rolls(4, 0, 200, 1, 8, 1, 30, 1));
            invokeAiStep(cater, cater.getClass());
            helper.assertTrue(cater.getTarget() == zombie && scanPick(cater) == zombie, "orig CaterKiller.java:462, :471-473 — the pass fires 1-in-4 and its scan"
                    + " hands the Zombie to the slot under the ownership mark (HEAD's goal acquired ≈ 1-in-10 and held) (" + FINDING + "); slot "
                    + describe(cater.getTarget()) + ", scanPick " + describe(scanPick(cater)));
        } finally {
            discardQuietly(zombie);
            discardQuietly(cater);
        }
    }

    // ------------------------------------------------------------------
    // The Sea Viper's cadence — orig SeaViper.java:482
    // ------------------------------------------------------------------

    /** orig SeaViper.java:482 — the 1-in-5 pass (`nextInt(5) == 1`): pinned to 0 nothing is picked; pinned to 1 the Zombie is the pick under the mark. */
    private static void seaViperCadence(GameTestHelper helper) {
        Mob viper = null;
        Mob zombie = null;
        try {
            viper = spawnFrozen(helper, ModEntities.SEA_VIPER.get(), HUNTER_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, viper, zombie, "a Zombie 8 blocks east");
            helper.assertTrue(viper.getTarget() == null, "precondition: an empty slot (" + FINDING + " test setup)");
            // the water hunt (25) quiet, the pass 5 -> 0 misses (orig rolls `== 1`), the splash heal (100) quiet
            replaceRandom(viper, rolls(25, 1, 5, 0, 100, 0));
            invokeAiStep(viper, viper.getClass());
            helper.assertTrue(viper.getTarget() == null, "orig SeaViper.java:482 — the pass runs on nextInt(5) == 1 only: on a 0 nothing is scanned (" + FINDING
                    + "); slot " + describe(viper.getTarget()));
            replaceRandom(viper, rolls(25, 1, 5, 1, 100, 0));
            invokeAiStep(viper, viper.getClass());
            helper.assertTrue(viper.getTarget() == zombie && scanPick(viper) == zombie, "orig SeaViper.java:482, :544-550 — the 1-in-5 pass's scan hands the Zombie to"
                    + " the slot under the ownership mark (HEAD's goal acquired players only, ≈ 1-in-10, and held) (" + FINDING + "); slot "
                    + describe(viper.getTarget()) + ", scanPick " + describe(scanPick(viper)));
        } finally {
            discardQuietly(zombie);
            discardQuietly(viper);
        }
    }

    // ------------------------------------------------------------------
    // The Dragonfly's cadence — orig Dragonfly.java:124 / :142
    // ------------------------------------------------------------------

    /**
     * The hunt is the flight retarget's ELSE branch: with the flight target parked, a tick whose 1-in-300 retarget fires
     * (HEAD's only hunting tick) hunts nothing; a tick whose retarget is quiet and whose 1-in-12 fires bites the butterfly
     * 2 blocks off and retains nothing; a tick whose 1-in-12 misses bites nothing.
     */
    private static void dragonflyCadence(GameTestHelper helper) {
        Mob fly = null;
        Mob butterfly = null;
        try {
            fly = spawnFrozen(helper, ModEntities.ENTITY_DRAGONFLY.get(), HUNTER_POS);
            butterfly = spawnPrey(helper, ModEntities.ENTITY_BUTTERFLY.get(), TOUCH_POS);
            DragonflyHuntGoal goal = (DragonflyHuntGoal) readField(fly, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(goal != null, "precondition: the hunt goal (" + FINDING + " test setup)");
            helper.assertTrue(fly.distanceToSqr(butterfly) < 6.0, "precondition: the butterfly stands inside the bite reach distSq < 6 (orig :146) (" + FINDING + " test geometry)");
            assertSees(helper, fly, butterfly, "a butterfly 2 blocks east");
            float health = butterfly.getHealth();
            goal.setFlightTarget(fly.blockPosition().above(10));
            replaceRandom(fly, rolls(300, 0, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() == health && fly.getTarget() == null, "orig Dragonfly.java:124-142 — on a tick the 1-in-300 retarget FIRES the"
                    + " hunt (the else branch) is skipped, 1-in-12 or not: no bite (HEAD hunted only on such ticks) (" + FINDING + "); health "
                    + butterfly.getHealth() + ", slot " + describe(fly.getTarget()));
            goal.setFlightTarget(fly.blockPosition().above(10));
            replaceRandom(fly, rolls(300, 1, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() < health, "orig Dragonfly.java:142-148 — on a tick the retarget is quiet the 1-in-12 hunts: the butterfly is"
                    + " bitten (HEAD never reached the roll on such a tick — one scan in ≈ 3600 ticks for orig's ≈ 12) (" + FINDING + "); health " + butterfly.getHealth());
            helper.assertTrue(fly.getTarget() == null, "orig :144-148 — nothing retained after the pass (ENT-S-129) (" + FINDING + "); slot " + describe(fly.getTarget()));
            float bitten = butterfly.getHealth();
            goal.setFlightTarget(fly.blockPosition().above(10));
            replaceRandom(fly, rolls(300, 1, 12, 1));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() == bitten, "orig Dragonfly.java:142 — the 1-in-12 missed: no hunt, no bite (" + FINDING + "); health " + butterfly.getHealth());
        } finally {
            discardQuietly(butterfly);
            discardQuietly(fly);
        }
    }

    /**
     * orig Dragonfly.java:124 — the near-retarget threshold is {@code < 2.1f} on the INTEGER cell distSq (cells 0, 1, 2 retarget):
     * with the retarget roll pinned quiet and the 1-in-12 pinned to fire, a flight target at cell distSq 2 retargets and hunts
     * nothing; at cell distSq 3 the else branch hunts and the butterfly 2 blocks off is bitten — HEAD's {@code Params.dragonfly}
     * 4.5 retargeted at 3 and 4 too (T3b refuters A-D4 / B-Q4).
     */
    private static void dragonflyNearRetarget(GameTestHelper helper) {
        Mob fly = null;
        Mob butterfly = null;
        try {
            fly = spawnFrozen(helper, ModEntities.ENTITY_DRAGONFLY.get(), HUNTER_POS);
            butterfly = spawnPrey(helper, ModEntities.ENTITY_BUTTERFLY.get(), TOUCH_POS);
            DragonflyHuntGoal goal = (DragonflyHuntGoal) readField(fly, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(goal != null, "precondition: the hunt goal (" + FINDING + " test setup)");
            helper.assertTrue(fly.distanceToSqr(butterfly) < 6.0, "precondition: the butterfly stands inside the bite reach distSq < 6 (orig :146) (" + FINDING + " test geometry)");
            assertSees(helper, fly, butterfly, "a butterfly 2 blocks east");
            float health = butterfly.getHealth();
            BlockPos two = fly.blockPosition().offset(1, 0, 1);
            helper.assertTrue(two.distSqr(fly.blockPosition()) == 2.0, "precondition: a flight target at cell distSq 2 (" + FINDING + " test geometry)");
            goal.setFlightTarget(two);
            replaceRandom(fly, rolls(300, 1, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() == health && fly.getTarget() == null, "orig Dragonfly.java:124 — a flight target at cell distSq 2 is near (2 < 2.1f):"
                    + " the retarget branch, no hunt though the 1-in-12 is pinned to fire (" + FINDING + "); health " + butterfly.getHealth() + ", slot " + describe(fly.getTarget()));
            BlockPos three = fly.blockPosition().offset(1, 1, 1);
            helper.assertTrue(three.distSqr(fly.blockPosition()) == 3.0, "precondition: a flight target at cell distSq 3 (" + FINDING + " test geometry)");
            goal.setFlightTarget(three);
            replaceRandom(fly, rolls(300, 1, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() < health, "orig Dragonfly.java:124 / :142 — a flight target at cell distSq 3 is NOT near (3 < 2.1f is false): the retarget is"
                    + " skipped and the else branch's 1-in-12 hunts — the butterfly is bitten, where Params.dragonfly's 4.5 (HEAD) called 3 near and never rolled (" + FINDING
                    + "); health " + butterfly.getHealth());
            helper.assertTrue(fly.getTarget() == null, "orig :146-148 — nothing retained after the pass (ENT-S-129) (" + FINDING + "); slot " + describe(fly.getTarget()));
        } finally {
            discardQuietly(butterfly);
            discardQuietly(fly);
        }
    }

    // ------------------------------------------------------------------
    // The Ender pair — orig EnderKnight.java / EnderReaper.java:61-81, getClosestPlayerToEntity then shouldAttackPlayer
    // ------------------------------------------------------------------

    /**
     * Nearest-then-filter: a survival player 8 blocks off looking away and a survival starer 12 blocks off — the pick's
     * conditions admit the starer alone, yet the nearest player is the non-starer and he fails the stare test, so nothing is
     * picked (HEAD took the nearest ELIGIBLE player, the starer); with the non-starer gone the starer is taken.
     */
    private static void enderNearestThenFilter(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob mob = null;
        ServerPlayer nonStarer = null;
        ServerPlayer starer = null;
        try {
            mob = spawnWithGoals(helper, type, HUNTER_POS);
            String name = mob.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = goalOfType(helper, mob, Player.class);
            starer = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_12_POS));
            stareAtMid(starer, mob);
            nonStarer = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            lookEast(nonStarer);
            assertSees(helper, mob, starer, "the starer 12 blocks east");
            assertSees(helper, mob, nonStarer, "the non-starer 8 blocks east");
            helper.assertTrue(nonStarer.distanceToSqr(mob) < starer.distanceToSqr(mob), "precondition: the non-starer is the nearer of the two (" + FINDING + " test setup)");
            TargetingConditions conditions = conditionsOf(goal);
            helper.assertTrue(conditions.test(mob, starer), "precondition: the pick's conditions (the stare selector, ENT-S-132) admit the starer (" + FINDING + " test setup)");
            helper.assertTrue(!conditions.test(mob, nonStarer), "precondition: the conditions refuse the player looking away (orig :88-91) (" + FINDING + " test setup)");
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(!can && pick == null, name + " (orig " + cite + " — getClosestPlayerToEntity, THEN shouldAttackPlayer on that one player): the nearest"
                    + " player looks away, so nothing is picked and the farther starer is shadowed; HEAD's findTarget took the nearest player the conditions"
                    + " admitted (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            removePlayer(helper, nonStarer);
            nonStarer = null;
            can = goal.canUse();
            pick = goalTarget(goal);
            helper.assertTrue(can && pick == starer, "control: with the non-starer gone the starer 12 blocks off is the nearest player and is taken (orig " + cite
                    + ") (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            removePlayer(helper, nonStarer);
            removePlayer(helper, starer);
            discardQuietly(mob);
        }
    }

    /** Every pass: the vanilla acquisition roll pinned to a MISS (nextInt(5) -> 1) still yields the starer 8 blocks off (HEAD's 3-arg goal skipped the pass). */
    private static void enderEveryPass(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob mob = null;
        ServerPlayer starer = null;
        try {
            mob = spawnWithGoals(helper, type, HUNTER_POS);
            String name = mob.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = goalOfType(helper, mob, Player.class);
            starer = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            stareAtMid(starer, mob);
            assertSees(helper, mob, starer, "the starer 8 blocks east");
            replaceRandom(mob, rolls(GOAL_ROLL_BOUND, 1));
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can && pick == starer, name + " (orig " + cite + " — findPlayerToAttack on every target-less tick of the legacy loop, td.bq): with"
                    + " vanilla's 1-in-5 acquisition roll pinned to miss the pass still runs and takes the starer; HEAD's interval-10 goal skipped it ("
                    + FINDING + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            removePlayer(helper, starer);
            discardQuietly(mob);
        }
    }

    /**
     * The within-N bound is a plain sphere from the mob's position (orig getClosestPlayerToEntity), not vanilla's range
     * scaled by the target's visibility: FOLLOW_RANGE lowered to 16 (the pick's bound), a SNEAKING starer 14 blocks off
     * (visibility 0.8 -> vanilla's 12.8) is taken.
     */
    private static void enderPlainSphere(GameTestHelper helper, EntityType<? extends Mob> type, String cite, double followRange) {
        Mob mob = null;
        ServerPlayer starer = null;
        try {
            mob = spawnWithGoals(helper, type, HUNTER_POS);
            String name = mob.getClass().getSimpleName();
            helper.assertTrue(mob.getAttributeValue(Attributes.FOLLOW_RANGE) == followRange, "precondition: FOLLOW_RANGE " + followRange + " — orig :65's bound ("
                    + FINDING + " test setup)");
            mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16.0);
            NearestAttackableTargetGoal<?> goal = goalOfType(helper, mob, Player.class);
            starer = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_14_POS));
            stareAtMid(starer, mob);
            starer.setShiftKeyDown(true);
            helper.assertTrue(starer.isShiftKeyDown() && starer.getVisibilityPercent(mob) < 1.0, "precondition: the sneaking starer's visibility percent is below 1"
                    + " (vanilla LivingEntity.getVisibilityPercent: 0.8 while discrete) (" + FINDING + " test setup)");
            double dist = mob.distanceTo(starer);
            helper.assertTrue(dist > 16.0 * starer.getVisibilityPercent(mob) && dist < 16.0, "precondition: the starer " + dist + " blocks off lies inside the"
                    + " plain 16 and outside vanilla's 16 x " + starer.getVisibilityPercent(mob) + " (" + FINDING + " test geometry)");
            assertSees(helper, mob, starer, "the sneaking starer 14 blocks east");
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can && pick == starer, name + " (orig " + cite + " — World.getClosestPlayerToEntity(this, N): a plain sphere from the position, no"
                    + " visibility scaling): the sneaking starer " + String.format("%.1f", dist) + " blocks off is taken where HEAD's range(N) x 0.8 refused"
                    + " him (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            removePlayer(helper, starer);
            discardQuietly(mob);
        }
    }

    // ------------------------------------------------------------------
    // The Stinky's state gate — orig Stinky.java:568 (before the :582 activity == 1 return), :511 the sitting gate
    // ------------------------------------------------------------------

    /**
     * A grounded Stinky (activity 1) runs the 1-in-7 combat pass ahead of its activity-1 return: pinned to fire, a Zombie 3
     * blocks off flips it to activity 2, marks the flight target and is bitten; a sitting Stinky never reaches do_movement
     * (orig :511) — activity stays 1, the Zombie untouched.
     */
    private static void stinkyStateGate(GameTestHelper helper) {
        Mob stinky = null;
        Mob zombie = null;
        try {
            stinky = spawnFrozen(helper, ModEntities.ENTITY_STINKY.get(), HUNTER_POS);
            EntityStinky pet = (EntityStinky) stinky;
            zombie = spawnPrey(helper, EntityType.ZOMBIE, NEAR_POS);
            pet.setActivity(1);
            helper.assertTrue(pet.activity == 1 && pet.getActivity() == 1 && !pet.isTame() && !pet.isOrderedToSit(),
                    "precondition: activity 1 (grounded), untamed, not sitting (" + FINDING + " test setup)");
            assertSees(helper, stinky, zombie, "a Zombie 3 blocks east");
            float reach = 3.0f + zombie.getBbWidth() / 2.0f;
            helper.assertTrue(stinky.distanceToSqr(zombie) < (double) (reach * reach), "precondition: the Zombie stands inside the (3 + w/2)^2 bite (orig :577) ("
                    + FINDING + " test geometry)");
            float health = zombie.getHealth();
            replaceRandom(stinky, rolls(7, 1, 300, 1));
            invoke(stinky, EntityStinky.class, "doMovement");
            helper.assertTrue(pet.activity == 2 && pet.getActivity() == 2 && zombie.getHealth() < health,
                    "orig Stinky.java:568-581 — the combat roll runs BEFORE the activity == 1 return (:582): a grounded Stinky scans, flips to activity 2 and bites"
                            + " (HEAD's doMovement returned first while activity != 2) (" + FINDING + "); activity " + pet.activity + ", Zombie health " + zombie.getHealth());
            pet.setActivity(1);
            pet.setOrderedToSit(true);
            healPrey(zombie);
            health = zombie.getHealth();
            replaceRandom(stinky, rolls(200, 0, 100, 0, 7, 1, 300, 1));
            invokeAiStep(stinky, EntityStinky.class);
            helper.assertTrue(pet.activity == 1 && pet.getActivity() == 1 && zombie.getHealth() == health,
                    "orig Stinky.java:511 — a sitting Stinky never reaches do_movement: the pass is not run, activity stays 1, the Zombie untouched (" + FINDING
                            + "); activity " + pet.activity + ", Zombie health " + zombie.getHealth());
        } finally {
            discardQuietly(zombie);
            discardQuietly(stinky);
        }
    }

    // ------------------------------------------------------------------
    // The Boyfriend / Girlfriend goals — orig MyEntityAINearestAttackableTarget.java:56 (the box), :53 (chance 0),
    // Boyfriend.java:138 / Girlfriend.java:164 (the Creeper task at 20), MyEntityAITarget.java:117-127 (nearbyOnly)
    // ------------------------------------------------------------------

    /**
     * The IMob goal's scan set is the 15/4/15 box alone: a Zombie at the box's +x/+z corner (≈ 21 blocks off, beyond
     * vanilla's range(15) sphere HEAD intersected the box with) is the pick. FOLLOW_RANGE is raised to 40 for the path
     * search behind the nearbyOnly test (orig's own attribute bounded that search, 16 by default — the row isolates the box).
     */
    private static void companionCorner(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob companion = null;
        Mob zombie = null;
        try {
            companion = spawnCompanion(helper, type, CORNER_HUNTER_POS);
            String name = companion.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = goalOfType(helper, companion, Mob.class);
            AABB box = companion.getBoundingBox().inflate(15.0, 4.0, 15.0);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, CORNER_HUNTER_POS);
            zombie.moveTo(box.maxX - INSIDE_MARGIN, companion.getY(), box.maxZ - INSIDE_MARGIN, 0.0f, 0.0f);
            helper.assertTrue(box.intersects(zombie.getBoundingBox()), "precondition: the corner Zombie meets the 15/4/15 box (" + FINDING + " test geometry)");
            double dist = companion.distanceTo(zombie);
            helper.assertTrue(dist > 15.0, "precondition: the corner Zombie " + dist + " blocks off lies beyond the range(15) sphere HEAD's vanilla goal tested ("
                    + FINDING + " test geometry)");
            assertSees(helper, companion, zombie, "the corner Zombie");
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can && pick == zombie, name + "'s IMob hunt (orig " + cite + " -> MyEntityAINearestAttackableTarget.java:56 — selectEntitiesWithinAABB"
                    + " over expand(15, 4, 15), the box alone): the Zombie at the box's corner, " + String.format("%.1f", dist) + " blocks off, is the pick where"
                    + " HEAD's vanilla goal refused it through its range(15) sphere (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            discardQuietly(zombie);
            discardQuietly(companion);
        }
    }

    /**
     * The Creeper task at priority 2 (orig :138 / :164): a Creeper 18 blocks off is inside its 20/4/20 box and taken by it,
     * outside the IMob goal's 15/4/15 box and refused there — HEAD, with the IMob goal alone, never took it.
     */
    private static void companionCreeperGoal(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob companion = null;
        Mob creeper = null;
        try {
            companion = spawnCompanion(helper, type, HUNTER_POS);
            String name = companion.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> creeperGoal = goalOfType(helper, companion, Creeper.class);
            NearestAttackableTargetGoal<?> imobGoal = goalOfType(helper, companion, Mob.class);
            helper.assertTrue(priorityOf(companion, creeperGoal) == 2, name + ": the Creeper task sits at target priority 2 (orig " + cite + "), ahead of the IMob"
                    + " hunt (" + FINDING + "); found " + priorityOf(companion, creeperGoal));
            creeper = spawnPrey(helper, EntityType.CREEPER, HUNTER_POS.east(18));
            double dist = companion.distanceTo(creeper);
            helper.assertTrue(dist > 15.0 + companion.getBbWidth() / 2.0 + creeper.getBbWidth() / 2.0 && dist < 20.0,
                    "precondition: the Creeper " + dist + " blocks off lies outside the IMob goal's 15-box and inside the Creeper goal's 20-box (" + FINDING + " test geometry)");
            assertSees(helper, companion, creeper, "a Creeper 18 blocks east");
            boolean can = creeperGoal.canUse();
            LivingEntity pick = goalTarget(creeperGoal);
            helper.assertTrue(can && pick == creeper, name + "'s Creeper task (orig " + cite + " — MyEntityAINearestAttackableTarget(EntityCreeper.class, 20.0f)): a"
                    + " Creeper 18 blocks off is its pick; HEAD had no such task (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            boolean imob = imobGoal.canUse();
            helper.assertTrue(!imob, "control: the IMob hunt's 15-box does not reach the Creeper 18 blocks off — only the restored Creeper task takes it (" + FINDING
                    + "); canUse=" + imob + ", pick " + describe(goalTarget(imobGoal)));
        } finally {
            discardQuietly(creeper);
            discardQuietly(companion);
        }
    }

    /** Every pass (orig :53, chance 0): vanilla's 1-in-5 acquisition roll pinned to a MISS still yields the Zombie 8 blocks off. */
    private static void companionEveryPass(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob companion = null;
        Mob zombie = null;
        try {
            companion = spawnCompanion(helper, type, HUNTER_POS);
            String name = companion.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = goalOfType(helper, companion, Mob.class);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, companion, zombie, "a Zombie 8 blocks east");
            replaceRandom(companion, rolls(GOAL_ROLL_BOUND, 1));
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can && pick == zombie, name + "'s IMob hunt (orig " + cite + " -> MyEntityAINearestAttackableTarget.java:53, targetChance 0 — every"
                    + " tick): with vanilla's 1-in-5 acquisition roll pinned to miss the pass still runs and takes the Zombie; HEAD's interval-10 goal skipped it ("
                    + FINDING + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            discardQuietly(zombie);
            discardQuietly(companion);
        }
    }

    /**
     * nearbyOnly (orig MyEntityAITarget.java:117-127 through :131-144): a Zombie 8 blocks off ringed by oak fences — in
     * sight over their one-block selection boxes, unreachable by path — is refused; the ring razed and the reach cache
     * expired, it is taken.
     */
    private static void companionNearbyOnly(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob companion = null;
        Mob zombie = null;
        boolean ringUp = false;
        try {
            companion = spawnCompanion(helper, type, HUNTER_POS);
            String name = companion.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = goalOfType(helper, companion, Mob.class);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            setFenceRing(helper, PREY_POS, Blocks.OAK_FENCE);
            ringUp = true;
            companion.getSensing().tick();
            helper.assertTrue(companion.hasLineOfSight(zombie), "precondition: the fence ring's one-block selection boxes leave the eye line to the Zombie clear ("
                    + FINDING + " test geometry)");
            helper.assertTrue(conditionsOf(goal).test(companion, zombie), "precondition: the goal's conditions admit the ringed Zombie — only the reach test can"
                    + " refuse it (" + FINDING + " test setup)");
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(!can && pick == null, name + "'s IMob hunt (orig " + cite + " nearbyOnly -> MyEntityAITarget.java:117-127): the Zombie inside a fence"
                    + " ring is unreachable — the path's end node stands more than 1.5 blocks off — and is refused, where HEAD's mustReach-less goal took it ("
                    + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            setFenceRing(helper, PREY_POS, Blocks.AIR);
            ringUp = false;
            writeField(goal, TargetGoal.class, "reachCacheTime", 0); // orig :118-120 — the 10 + nextInt(5) tick cache expired by hand, so the verdict is recomputed
            companion.getSensing().tick();
            can = goal.canUse();
            pick = goalTarget(goal);
            helper.assertTrue(can && pick == zombie, "control: the ring razed and the reach cache expired, the same Zombie is reachable and taken (" + FINDING
                    + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            if (ringUp) setFenceRing(helper, PREY_POS, Blocks.AIR);
            discardQuietly(zombie);
            discardQuietly(companion);
        }
    }

    /**
     * orig MyEntityAITarget.java:111-113 before :117-127: a Creeper is granted BEFORE the nearbyOnly reach block. The Boyfriend
     * two blocks up (a stone pillar under him: his 1.36 eye at floor level runs UNDER the ring's 1.5-block fence tops to a
     * creeper's 1.445 eye — row 36's Zombie eye is higher; from y + 2 the ray clears them by 15 cm), a Creeper 8 blocks east
     * inside a fence ring: unreachable by path (the goal's own {@code canReach} answers false), admitted by the conditions, and
     * the Creeper task's pick — vanilla's {@code canAttack} reach-tested it and refused it (T3b refuter A, D1); the IMob task
     * grants it the same way. Row 36 keeps the reach-refused Zombie as the control.
     */
    private static void companionCreeperBeforeReach(GameTestHelper helper) {
        Mob companion = null;
        Mob creeper = null;
        boolean ringUp = false;
        boolean pillarUp = false;
        try {
            helper.setBlock(HUNTER_POS, Blocks.STONE);
            helper.setBlock(HUNTER_POS.above(), Blocks.STONE);
            pillarUp = true;
            companion = spawnCompanion(helper, ModEntities.BOYFRIEND.get(), HUNTER_POS.above(2));
            String name = companion.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> creeperGoal = goalOfType(helper, companion, Creeper.class);
            NearestAttackableTargetGoal<?> imobGoal = goalOfType(helper, companion, Mob.class);
            creeper = spawnPrey(helper, EntityType.CREEPER, PREY_POS);
            setFenceRing(helper, PREY_POS, Blocks.OAK_FENCE);
            ringUp = true;
            companion.getSensing().tick();
            assertSees(helper, companion, creeper, "the ringed Creeper from two blocks up, over the fence tops");
            helper.assertTrue(conditionsOf(creeperGoal).test(companion, creeper), "precondition: the Creeper task's conditions (the selector, forCombat, the line of"
                    + " sight) admit the ringed Creeper — only the reach test could refuse it (" + FINDING + " test setup)");
            boolean reachable = (Boolean) invoke(creeperGoal, TargetGoal.class, "canReach", new Class<?>[] {LivingEntity.class}, creeper);
            helper.assertTrue(!reachable, "precondition: the goal's own reach test (TargetGoal.canReach — orig MyEntityAITarget.java:131-144) refuses the ringed Creeper:"
                    + " the path's end node stands more than 1.5 blocks off (" + FINDING + " test setup)");
            boolean can = creeperGoal.canUse();
            LivingEntity pick = goalTarget(creeperGoal);
            helper.assertTrue(can && pick == creeper, name + "'s Creeper task (orig Boyfriend.java:138 -> MyEntityAITarget.java:111-113, BEFORE the nearbyOnly block"
                    + " :117-127): the ringed Creeper is granted ahead of the reach test and is the pick — vanilla's canAttack reach-tests every candidate and refused"
                    + " it (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            can = imobGoal.canUse();
            pick = goalTarget(imobGoal);
            helper.assertTrue(can && pick == creeper, name + "'s IMob hunt (orig Boyfriend.java:141 — the same base-class step :111): the ringed Creeper inside its"
                    + " 15-box is its pick too (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
        } finally {
            if (ringUp) setFenceRing(helper, PREY_POS, Blocks.AIR);
            if (pillarUp) {
                helper.setBlock(HUNTER_POS.above(), Blocks.AIR);
                helper.setBlock(HUNTER_POS, Blocks.AIR);
            }
            discardQuietly(creeper);
            discardQuietly(companion);
        }
    }

    // ------------------------------------------------------------------
    // The Girlfriend's Valentine player goal — orig MyValentineTarget.java:60, expand(16, 4, 16)
    // ------------------------------------------------------------------

    /** The Player goal's scan (findTarget by reflection — the Feb-14 gate of canUse aside): a survival player at the 16/4/16 box's corner, beyond HEAD's 16-sphere, is the pick. */
    private static void valentineCorner(GameTestHelper helper) {
        Mob girlfriend = null;
        ServerPlayer player = null;
        try {
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), CORNER_HUNTER_POS);
            NearestAttackableTargetGoal<?> goal = valentinePlayerGoal(helper, girlfriend);
            AABB box = girlfriend.getBoundingBox().inflate(16.0, 4.0, 16.0);
            player = survivalServerPlayerAt(helper, new Vec3(box.maxX - INSIDE_MARGIN, girlfriend.getY(), box.maxZ - INSIDE_MARGIN));
            helper.assertTrue(box.intersects(player.getBoundingBox()), "precondition: the corner player meets the 16/4/16 box (" + FINDING + " test geometry)");
            double dist = girlfriend.distanceTo(player);
            helper.assertTrue(dist > 16.0, "precondition: the corner player " + dist + " blocks off lies beyond the 16-sphere HEAD's vanilla Player goal searched ("
                    + FINDING + " test geometry)");
            assertSees(helper, girlfriend, player, "the corner player");
            helper.assertTrue(conditionsOf(goal).test(girlfriend, player), "precondition: the goal's conditions (MOD-036's forCombat, the owner / pet selector) admit"
                    + " the survival player (" + FINDING + " test setup)");
            invoke(goal, MyEntityAINearestAttackableTargetGoal.class, "findTarget");
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(pick == player, "Girlfriend's ValentineTargetGoal<Player> (orig MyValentineTarget.java:60 — selectEntitiesWithinAABB over expand(16, 4, 16)):"
                    + " the player at the box's corner, " + String.format("%.1f", dist) + " blocks off, is the pick where HEAD's getNearestPlayer(conditions) over"
                    + " the level's players refused him through its 16-sphere (" + FINDING + "); got " + describe(pick));
        } finally {
            removePlayer(helper, player);
            discardQuietly(girlfriend);
        }
    }

    /** The reverse: a survival player straight above, inside HEAD's 16-sphere but past the box's +y edge, is not the pick; just inside it he is. */
    private static void valentineAbove(GameTestHelper helper) {
        Mob girlfriend = null;
        ServerPlayer player = null;
        try {
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS);
            NearestAttackableTargetGoal<?> goal = valentinePlayerGoal(helper, girlfriend);
            AABB box = girlfriend.getBoundingBox().inflate(16.0, 4.0, 16.0);
            player = survivalServerPlayerAt(helper, new Vec3(girlfriend.getX(), box.maxY + OUTSIDE_MARGIN, girlfriend.getZ()));
            helper.assertTrue(!box.intersects(player.getBoundingBox()), "precondition: straight above, the player lies past the box's +y edge (" + FINDING + " test geometry)");
            double dist = girlfriend.distanceTo(player);
            helper.assertTrue(dist < 16.0, "precondition: the same player " + dist + " up lies inside the 16-sphere HEAD searched (" + FINDING + " test geometry)");
            assertSees(helper, girlfriend, player, "the player straight above");
            invoke(goal, MyEntityAINearestAttackableTargetGoal.class, "findTarget");
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(pick == null, "Girlfriend's ValentineTargetGoal<Player> (orig MyValentineTarget.java:60): the box reaches 4 above her box — a player "
                    + String.format("%.1f", dist) + " straight up is outside it and NOT the pick, where HEAD's 16-sphere took him (" + FINDING + "); got " + describe(pick));
            Vec3 inside = new Vec3(girlfriend.getX(), box.maxY - INSIDE_MARGIN, girlfriend.getZ());
            player.teleportTo(helper.getLevel(), inside.x, inside.y, inside.z, 0.0f, 0.0f);
            girlfriend.getSensing().tick();
            writeField(goal, TargetGoal.class, "reachCacheTime", 0);
            helper.assertTrue(box.intersects(player.getBoundingBox()), "control precondition: just inside the box's top the player meets it (" + FINDING + ")");
            invoke(goal, MyEntityAINearestAttackableTargetGoal.class, "findTarget");
            pick = goalTarget(goal);
            helper.assertTrue(pick == player, "control: just inside the box's +y edge the player is the pick (" + FINDING + "); got " + describe(pick));
        } finally {
            removePlayer(helper, player);
            discardQuietly(girlfriend);
        }
    }

    /**
     * orig MyEntityAITarget.java:96-98 before :108 / :117: on the day a player is answered BEFORE the sight step and the
     * nearbyOnly block. Under the {@code SeasonalDates} Feb-14 clock seam (the MOD-036 rows' idiom) a valentine-angry Girlfriend
     * with the SightStepParityTests wall between her and a survival player 8 blocks east: the Player task's conditions admit
     * him with no line of sight and its {@code canUse} picks him (HEAD's goal refused him on sight; T3b's draft reach-tested
     * him — refuter A, D2); a Boyfriend on the same spot behind the same wall is refused by the Boyfriend task — his class
     * reaches :108 — and taken once the wall is razed. The clock is restored in the finally.
     */
    private static void valentineNoSight(GameTestHelper helper) {
        Mob girlfriend = null;
        ServerPlayer player = null;
        Mob boyfriend = null;
        boolean wallUp = false;
        SeasonalDates.setClockForTesting(() -> VALENTINES);
        try {
            helper.assertTrue(SeasonalDates.isValentines(), "precondition: the Feb-14 clock seam is in place (" + FINDING + " test setup)");
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS);
            helper.assertTrue(girlfriend instanceof Girlfriend gf && gf.isValentineAngry(), "precondition: a Girlfriend spawned on Feb 14 is valentine-angry (orig"
                    + " Girlfriend.java:569-574) (" + FINDING + " test setup)");
            NearestAttackableTargetGoal<?> playerGoal = valentinePlayerGoal(helper, girlfriend);
            NearestAttackableTargetGoal<?> boyfriendGoal = goalOfType(helper, girlfriend, Boyfriend.class);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            setWall(helper, Blocks.STONE);
            wallUp = true;
            girlfriend.getSensing().tick();
            helper.assertTrue(!girlfriend.hasLineOfSight(player), "precondition: the stone wall at x = 24 hides the player 8 blocks east from her eye (" + FINDING + " test geometry)");
            helper.assertTrue(conditionsOf(playerGoal).test(girlfriend, player), "Girlfriend's ValentineTargetGoal<Player>: its conditions carry no line of sight —"
                    + " MOD-036's forCombat screens and the owner / pet selector alone answer, and they admit the survival player behind the wall (orig MyEntityAITarget.java:96"
                    + " returned ahead of the sight step :108) (" + FINDING + ")");
            boolean can = playerGoal.canUse();
            LivingEntity pick = goalTarget(playerGoal);
            helper.assertTrue(can && pick == player, "Girlfriend's ValentineTargetGoal<Player> (orig Girlfriend.java:161 -> MyValentineTarget.java:60 -> MyEntityAITarget.java:96:"
                    + " a player on the day is granted BEFORE the sight step :108 and the nearbyOnly block :117): the survival player behind the wall is the pick — HEAD's goal"
                    + " refused him on sight, T3b's draft reach-tested him (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            removePlayer(helper, player);
            player = null;
            boyfriend = spawnPrey(helper, ModEntities.BOYFRIEND.get(), PREY_POS);
            girlfriend.getSensing().tick();
            helper.assertTrue(!girlfriend.hasLineOfSight(boyfriend), "precondition: the same wall hides the Boyfriend 8 blocks east (" + FINDING + " test geometry)");
            can = boyfriendGoal.canUse();
            pick = goalTarget(boyfriendGoal);
            helper.assertTrue(!can && pick == null, "Girlfriend's ValentineTargetGoal<Boyfriend> (orig Girlfriend.java:162 -> MyEntityAITarget.java:108): a Boyfriend behind the"
                    + " wall is refused by the sight step — no grant precedes it for his class (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            setWall(helper, Blocks.AIR);
            wallUp = false;
            girlfriend.getSensing().tick();
            writeField(boyfriendGoal, TargetGoal.class, "reachCacheTime", 0);
            assertSees(helper, girlfriend, boyfriend, "the Boyfriend, the wall razed");
            can = boyfriendGoal.canUse();
            pick = goalTarget(boyfriendGoal);
            helper.assertTrue(can && pick == boyfriend, "control: razed, the same Boyfriend — in sight, reachable — is the Boyfriend task's pick (" + FINDING + "); canUse=" + can
                    + ", pick " + describe(pick));
        } finally {
            if (wallUp) setWall(helper, Blocks.AIR);
            discardQuietly(boyfriend);
            removePlayer(helper, player);
            discardQuietly(girlfriend);
            SeasonalDates.resetClock();
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the sibling batches' idioms)
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey, String why) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: " + hunter.getClass().getSimpleName() + " (eye " + String.format("%.2f", hunter.getEyeHeight())
                + " above its feet) must see " + why + " inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (a selector is under test) but no AI, so nothing runs by itself. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /**
     * A companion with its goals, on the ground (a frozen mob never lands — LivingEntity.travel is gated on isEffectiveAi — and
     * the goals' nearbyOnly reach cache paths through GroundPathNavigation.canUpdatePath, which needs it; the T5 refuter B1
     * precedent), its FOLLOW_RANGE raised to 40 so the path search behind that test reaches every probe of these rows (orig's
     * own attribute bounded the search, 16 by default; the rows isolate the box).
     */
    private static Mob spawnCompanion(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = spawnWithGoals(helper, type, pos);
        mob.setOnGround(true);
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40.0);
        return mob;
    }

    /** Frozen prey with 1000 HP, so no pinned hit kills it. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
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
     * (PlayNicelyGateParityTests.survivalServerPlayerAt): {@code isCreative()} follows its SURVIVAL mode, its abilities too.
     * The 60-tick spawn shield is kept: no row pins a hit on it.
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
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        player.setHealth(1000.0f);
        player.teleportTo(helper.getLevel(), absolutePos.x, absolutePos.y, absolutePos.z, 0.0f, 0.0f);
        return player;
    }

    private static void removePlayer(GameTestHelper helper, ServerPlayer player) {
        if (player != null) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    /** The player looks at the hunter's mid-height (the Ender pair's :88-91 test; CreativeGateParityTests.stareAtMid). */
    private static void stareAtMid(ServerPlayer player, Mob hunter) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(hunter.getX(), hunter.getY() + hunter.getBbHeight() / 2.0f, hunter.getZ()));
    }

    /** The player looks due east, away from a hunter to its west. */
    private static void lookEast(ServerPlayer player) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(player.getX() + 8.0, player.getEyeY(), player.getZ()));
    }

    /** The SightStepParityTests wall: one block thick at x = 24, z 23..25, y 1..14 — between HUNTER_POS and PREY_POS. */
    private static void setWall(GameTestHelper helper, Block block) {
        for (int y = 1; y <= 14; y++) {
            for (int z = 23; z <= 25; z++) {
                helper.setBlock(new BlockPos(24, y, z), block);
            }
        }
    }

    /** The eight blocks around a prey's cell at floor level. */
    private static void setFenceRing(GameTestHelper helper, BlockPos centre, Block block) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                helper.setBlock(centre.offset(dx, 0, dz), block);
            }
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained. */
    private static RandomSource rolls(int... boundAnswerPairs) {
        RandomSource source = RandomSource.create(1234L);
        for (int i = 0; i < boundAnswerPairs.length; i += 2) {
            source = new VortexParityTests.ForcedRoll(source, boundAnswerPairs[i], boundAnswerPairs[i + 1]);
        }
        return source;
    }

    /** Same seam as VortexParityTests.forceDiscardRoll: swap {@code Entity.random} for a forced source. */
    private static void replaceRandom(Entity entity, RandomSource forced) {
        try {
            Field field = Entity.class.getDeclaredField("random");
            field.setAccessible(true);
            field.set(entity, forced);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot replace Entity.random", exception);
        }
    }

    /** Every goal on the target selector, unwrapped. */
    private static List<Goal> targetGoals(Mob hunter) {
        List<Goal> goals = new ArrayList<>();
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            goals.add(wrapped.getGoal());
        }
        return goals;
    }

    /** The one {@code NearestAttackableTargetGoal} of the given target type on the selector. */
    private static NearestAttackableTargetGoal<?> goalOfType(GameTestHelper helper, Mob hunter, Class<?> targetType) {
        NearestAttackableTargetGoal<?> found = null;
        int count = 0;
        for (Goal goal : targetGoals(hunter)) {
            if (goal instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == targetType) {
                found = nearest;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + hunter.getClass().getSimpleName() + " carries exactly one NearestAttackableTargetGoal<"
                + targetType.getSimpleName() + "> on its target selector — found " + count + " (" + FINDING + " test setup)");
        return found;
    }

    /** The Girlfriend's Player-scanning goal: the ValentineTargetGoal @1 (PortOnlyTargetingTests' idiom). */
    private static NearestAttackableTargetGoal<?> valentinePlayerGoal(GameTestHelper helper, Mob girlfriend) {
        NearestAttackableTargetGoal<?> goal = goalOfType(helper, girlfriend, Player.class);
        helper.assertTrue(goal.getClass().getSimpleName().equals("ValentineTargetGoal") && goal instanceof MyEntityAINearestAttackableTargetGoal<?>,
                "precondition: the Player-scanning goal is the ValentineTargetGoal on the MyEntityAINearestAttackableTargetGoal scan (" + FINDING + " test setup); found "
                        + goal.getClass().getName());
        return goal;
    }

    private static int priorityOf(Mob hunter, Goal goal) {
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() == goal) return wrapped.getPriority();
        }
        throw new IllegalStateException("the goal is not on the target selector (" + FINDING + " test setup)");
    }

    private static LivingEntity goalTarget(NearestAttackableTargetGoal<?> goal) {
        return (LivingEntity) readField(goal, NearestAttackableTargetGoal.class, "target");
    }

    private static TargetingConditions conditionsOf(NearestAttackableTargetGoal<?> goal) {
        return (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
    }

    /** The hunter's private {@code scanPick} ownership mark. */
    private static LivingEntity scanPick(Mob hunter) {
        return (LivingEntity) readField(hunter, hunter.getClass(), "scanPick");
    }

    /** The hunter's private no-arg {@code findSomethingToAttack()} — the port's shape of the orig scan. */
    private static LivingEntity scan(Mob hunter) {
        return (LivingEntity) invoke(hunter, hunter.getClass(), "findSomethingToAttack");
    }

    /** The hunter's private one-arg {@code isSuitableTarget(LivingEntity)}. */
    private static boolean filter(Mob hunter, LivingEntity candidate) {
        return (Boolean) invoke(hunter, hunter.getClass(), "isSuitableTarget", new Class<?>[] {LivingEntity.class}, candidate);
    }

    /** The hunter's protected customServerAiStep, declared on the given class, invoked once. */
    private static void invokeAiStep(Mob hunter, Class<?> declaring) {
        invoke(hunter, declaring, "customServerAiStep");
    }

    private static Object invoke(Object target, Class<?> declaring, String name) {
        return invoke(target, declaring, name, new Class<?>[0]);
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

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    private static void writeField(Object owner, Class<?> declaring, String name, Object value) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            field.set(owner, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    private static String describe(Object entity) {
        return entity == null ? "null" : entity instanceof Entity e ? e.getClass().getSimpleName() + "#" + e.getId() : entity.getClass().getSimpleName();
    }
}
