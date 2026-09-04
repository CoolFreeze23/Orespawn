package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.EnderKnight;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityEmperorScorpion;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.Irukandji;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.PitchBlack;
import danger.orespawn.entity.Pointysaurus;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.WaterDragon;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import danger.orespawn.entity.ai.SeaViperBiteGoal;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-129 (targeting ledger T5, wave 2) and the two ENT-S-122 reproductions: the 1.7.10 target set / release
 * rules, transcribed at the orig positions, and the ownership convention chosen for the scan's mark.
 *
 * <p>Three groups, one generated {@link TestFunction} per row ({@code targetreleaseparitytests.s129_NN_<species>_<site>}
 * and {@code s122_NN_*}), all in the {@code targetReleaseParity} batch (TEST-003):
 * <ul>
 *   <li>the six ownership cases of the convention (Part 1): the scan's own pick behind a wall dropped on the next
 *       pass; a revenge target dying with the pass firing in the cleanup re-assert window (the tick-driven
 *       KrakenHoldReleaseTests shape, the hunter's speed zeroed so the geometry stands); the same-entity re-assert
 *       keeping the mark and a change ending it; a hit the hurt timer swallowed keeping it and a stored hit ending
 *       it; PlayNicely flipping on with the scan's pick in the slot after a re-assert; a foreign revenge set ending
 *       the mark and standing;</li>
 *   <li>the T5 rows: the 1-in-N forgets inside the pass at orig cadence, polarity and field, final against the
 *       revenge goal's memory (no vanilla re-assert), the revenge target kept through sight loss, the clear-before-read
 *       orders, the attacker stores (Mob only), the inert revenge tasks unregistered, the scan every pass, the picks
 *       re-derived and the dead stored targets cleared, the daylight drop and the legacy holds of the Ender pair,
 *       Robot2's alert and exemption, the revenge-memory clears on {@code lastHurtByMob}, the helper goal's 15-block
 *       hold, the Dragonfly's one bite per pass, the melee goal's PlayNicely stand-down, the Water Dragon's hand-off
 *       pinned to this hit;</li>
 *   <li>the ENT-S-122 memo: seen kept through a wall, unseen kept after exposure, the state boundary clearing it, for
 *       the Ant Robot and the Nightmare, and the Nightmare's orig :259-280 branch (the heal, the null-scan
 *       deactivation, the spontaneous activation).</li>
 * </ul>
 *
 * <p>Geometry as the sibling batches: the hunter at rel (20, 1, 24) on the floor of the 48x16x48 empty_large, prey
 * east on the same floor; the one-block stone wall at x = 24 (z 23..25, y 1..14) of SightStepParityTests breaks the
 * eye line. Synchronous rows drive private passes and goals by reflection on frozen hunters ({@code spawnWithNoFreeWill}
 * + noAi; the goal objects survive in the hunters' fields or on the selectors of a {@code spawn} + noAi hunter) under
 * pinned rolls (the VortexParityTests.ForcedRoll seam). A hunter's {@code tickCount} is set to 100 before a hit it
 * must remember: vanilla's {@code HurtByTargetGoal.canUse} ignores a hit whose timestamp equals the goal's initial 0.
 * Every flag flip is restored in a finally, every spawn discarded, every mock player removed; mock players are
 * SURVIVAL unless a row wants creative, and every predicate of this batch reads the abilities, not
 * {@code isCreative()}.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TargetReleaseParityTests {

    private static final String BATCH = "targetReleaseParity";
    private static final String TEST_PREFIX = "targetreleaseparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-129";
    private static final String MEMO = "ENT-S-122";

    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside every box of this batch, inside the Ant Robot's 6..9 stomp ring. */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** 3 blocks east: in front of the wall. */
    private static final BlockPos NEAR_POS = new BlockPos(23, 1, 24);
    /** 2 blocks east: inside the Dragonfly's distSq &lt; 6 bite. */
    private static final BlockPos TOUCH_POS = new BlockPos(22, 1, 24);
    /** 6 blocks east: behind the wall, inside the Hammerhead's 7 + w/2 reach. */
    private static final BlockPos BEHIND_WALL_POS = new BlockPos(26, 1, 24);
    /** 24 blocks east: outside the Sea Monster's 16-sphere, inside the template. */
    private static final BlockPos FAR_POS = new BlockPos(44, 1, 24);
    private static final Vec3 PLAYER_5_POS = new Vec3(25.5, 1.0, 24.5);
    private static final Vec3 PLAYER_8_POS = new Vec3(28.5, 1.0, 24.5);
    private static final Vec3 PLAYER_FRONT_POS = new Vec3(22.5, 1.0, 24.5);
    private static final Vec3 PLAYER_15_POS = new Vec3(35.5, 1.0, 24.5);
    private static final Vec3 PLAYER_24_POS = new Vec3(44.5, 1.0, 24.5);
    private static final int WALL_X = 24;
    private static final int WALL_Z_MIN = 23;
    private static final int WALL_Z_MAX = 25;
    private static final int WALL_Y_MIN = 1;
    private static final int WALL_Y_MAX = 14;
    private static final float PREY_HEALTH = 1000.0f;
    /** A hunter's tickCount before a hit it must remember (see the class javadoc). */
    private static final int TICKS_ALIVE = 100;

    // ------------------------------------------------------------------
    // The site table
    // ------------------------------------------------------------------

    private record Site(String name, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + this.name;
        }
    }

    private static List<Site> sites() {
        List<Site> s = new ArrayList<>();
        // Part 1 — the ownership convention, measured on the Nastysaurus (the ENT-S-108 shape) and the Emperor Scorpion
        s.add(new Site("s129_01_nastysaurus_ownership_a_own_pick_behind_wall_dropped", TargetReleaseParityTests::ownershipA));
        s.add(new Site("s129_02_nastysaurus_ownership_b_revenge_dies_reassert_window", TargetReleaseParityTests::ownershipB));
        s.add(new Site("s129_03_nastysaurus_ownership_c_same_entity_reassert_keeps_change_ends", TargetReleaseParityTests::ownershipC));
        s.add(new Site("s129_04_emperorscorpion_ownership_d_swallowed_hit_keeps_stored_hit_ends", TargetReleaseParityTests::ownershipD));
        s.add(new Site("s129_05_nastysaurus_ownership_e_playnicely_after_reassert_clears_own_pick", TargetReleaseParityTests::ownershipE));
        s.add(new Site("s129_06_nastysaurus_ownership_f_foreign_revenge_set_ends_mark_and_stands", TargetReleaseParityTests::ownershipF));
        // Part 2 — the rows
        s.add(new Site("s129_07_nastysaurus_219_revenge_forget_in_pass_final", h -> rtForget(h, ModEntities.NASTYSAURUS.get(), Nastysaurus.class, 5, 0, 250, "Nastysaurus.java:219-221")));
        s.add(new Site("s129_08_nastysaurus_223_revenge_out_of_sight_skipped_kept", h -> rtSightSkip(h, ModEntities.NASTYSAURUS.get(), Nastysaurus.class, 5, 0, 250, "Nastysaurus.java:223-225")));
        s.add(new Site("s129_09_trex_189_revenge_forget_in_pass_final", h -> rtForget(h, ModEntities.TREX.get(), TRex.class, 5, 1, 200, "TRex.java:189-191")));
        s.add(new Site("s129_10_trex_193_revenge_out_of_sight_skipped_kept", h -> rtSightSkip(h, ModEntities.TREX.get(), TRex.class, 5, 1, 200, "TRex.java:193-195")));
        s.add(new Site("s129_11_pointysaurus_189_revenge_forget_in_pass_playnicely_blanks", TargetReleaseParityTests::pointysaurusForget));
        s.add(new Site("s129_12_emperorscorpion_414_forget_in_pass_not_goal_tick", TargetReleaseParityTests::emperorForget));
        s.add(new Site("s129_13_caterkiller_468_forget_in_pass_not_goal_tick", TargetReleaseParityTests::caterKillerForget));
        s.add(new Site("s129_14_giantrobot_243_clear_before_read", TargetReleaseParityTests::giantRobotClearBeforeRead));
        s.add(new Site("s129_15_crab_342_forget_in_pass", TargetReleaseParityTests::crabForget));
        s.add(new Site("s129_16_crab_232_mob_attacker_stored_player_not", TargetReleaseParityTests::crabAttackerStore));
        s.add(new Site("s129_17_antrobot_109_clear_before_read", TargetReleaseParityTests::antRobotClearBeforeRead));
        s.add(new Site("s129_18_antrobot_579_mob_attacker_stored_player_not", TargetReleaseParityTests::antRobotAttackerStore));
        s.add(new Site("s129_19_hammerhead_203_revenge_out_of_sight_skipped", TargetReleaseParityTests::hammerheadSightSkip));
        s.add(new Site("s129_20_hammerhead_56_no_revenge_goal_takes_attacker", h -> noRevengeGoal(h, ModEntities.HAMMERHEAD.get(), "Hammerhead.java:56")));
        s.add(new Site("s129_21_alosaurus_54_no_revenge_goal_takes_attacker", h -> noRevengeGoal(h, ModEntities.ALOSAURUS.get(), "Alosaurus.java:54")));
        s.add(new Site("s129_22_cavefisher_169_scans_every_pass_no_revenge_goal", h -> scanEveryPass(h, ModEntities.CAVE_FISHER.get(), "CaveFisher.java:169", "CaveFisher.java:55")));
        s.add(new Site("s129_23_dungeonbeast_173_scans_every_pass_no_revenge_goal", h -> scanEveryPass(h, ModEntities.DUNGEON_BEAST.get(), "DungeonBeast.java:173", "DungeonBeast.java:59")));
        s.add(new Site("s129_24_urchin_196_scans_every_pass_no_revenge_goal", h -> scanEveryPass(h, ModEntities.URCHIN.get(), "Urchin.java:196", "Urchin.java:61")));
        s.add(new Site("s129_25_scorpion_176_empty_scan_clears_no_revenge_goal", TargetReleaseParityTests::scorpionEmptyScan));
        s.add(new Site("s129_26_irukandji_304_pick_rederived_dead_stored_cleared", h -> inlinePick(h, ModEntities.IRUKANDJI.get(), PLAYER_8_POS, new int[] {10, 1, 8, 1, 4, 1}, "Irukandji.java:299-309")));
        s.add(new Site("s129_27_irukandji_135_attacker_store_ends_mark", TargetReleaseParityTests::irukandjiHandOff));
        s.add(new Site("s129_28_skate_296_pick_rederived_dead_stored_cleared", h -> inlinePick(h, ModEntities.SKATE.get(), PLAYER_15_POS, new int[] {10, 1, 8, 1, 4, 1}, "Skate.java:291-301")));
        s.add(new Site("s129_29_seamonster_527_pick_rederived_dead_stored_cleared", h -> inlinePick(h, ModEntities.SEA_MONSTER.get(), PLAYER_24_POS, new int[] {25, 1, 5, 1, 4, 1}, "SeaMonster.java:522-532")));
        s.add(new Site("s129_30_enderknight_111_daylight_drop", TargetReleaseParityTests::enderKnightDaylight));
        s.add(new Site("s129_31_enderknight_hold_no_range_release_nulled_is_gone", h -> legacyHold(h, ModEntities.ENDER_KNIGHT.get(), "EnderKnight.java", 64)));
        s.add(new Site("s129_32_enderreaper_hold_no_range_release_nulled_is_gone", h -> legacyHold(h, ModEntities.ENDER_REAPER.get(), "EnderReaper.java", 81)));
        s.add(new Site("s129_33_robot2_57_no_alert_no_same_kind_exemption", TargetReleaseParityTests::robot2Revenge));
        s.add(new Site("s129_34_creepinghorror_136_forget_revenge_memory", h -> revengeMemoryClear(h, ModEntities.CREEPING_HORROR.get(), new int[] {200, 1, 5, 0}, "CreepingHorror.java:136-138")));
        s.add(new Site("s129_35_cannonfodder_346_forget_revenge_memory", h -> revengeMemoryClear(h, ModEntities.CHIPMUNK.get(), new int[] {200, 1}, "EntityCannonFodder.java:346-348 (a Chipmunk)")));
        s.add(new Site("s129_36_kyuubi_157_forget_revenge_memory", h -> revengeMemoryClear(h, ModEntities.ENTITY_KYUUBI.get(), new int[] {200, 1, 10, 0}, "Kyuubi.java:157-159")));
        s.add(new Site("s129_37_leafmonster_160_forget_revenge_memory", h -> revengeMemoryClear(h, ModEntities.ENTITY_LEAF_MONSTER.get(), new int[] {100, 1, 4, 0}, "LeafMonster.java:160-162")));
        s.add(new Site("s129_38_rat_156_forget_revenge_memory", h -> revengeMemoryClear(h, ModEntities.ENTITY_RAT.get(), new int[] {200, 1, 5, 0}, "Rat.java:156-158")));
        s.add(new Site("s129_39_boyfriend_141_hold_distance_15", h -> helperHoldDistance(h, ModEntities.BOYFRIEND.get(), "Boyfriend.java:141")));
        s.add(new Site("s129_40_girlfriend_167_hold_distance_15", h -> helperHoldDistance(h, ModEntities.GIRLFRIEND.get(), "Girlfriend.java:167")));
        s.add(new Site("s129_41_dragonfly_146_one_bite_per_pass_nothing_retained", TargetReleaseParityTests::dragonflyOneBite));
        s.add(new Site("s129_42_seaviper_539_melee_goal_stands_down_under_playnicely", TargetReleaseParityTests::standDown));
        s.add(new Site("s129_43_waterdragon_490_handoff_pinned_to_this_hit", TargetReleaseParityTests::waterDragonHandOff));
        // ENT-S-122 — the sight memo
        s.add(new Site("s122_01_antrobot_974_stomp_seen_kept_through_wall", h -> memoSeenKept(h, ModEntities.ANT_ROBOT.get(), "feetIsSuitableTarget", "AntRobot.java:974-976")));
        s.add(new Site("s122_02_antrobot_974_stomp_unseen_kept_after_exposure", h -> memoUnseenKept(h, ModEntities.ANT_ROBOT.get(), "feetIsSuitableTarget", "AntRobot.java:974-976")));
        s.add(new Site("s122_03_antrobot_1047_hunt_seen_kept_through_wall", h -> memoSeenKept(h, ModEntities.ANT_ROBOT.get(), "isSuitableTarget", "AntRobot.java:1047-1049")));
        s.add(new Site("s122_04_antrobot_1047_hunt_unseen_kept_after_exposure", h -> memoUnseenKept(h, ModEntities.ANT_ROBOT.get(), "isSuitableTarget", "AntRobot.java:1047-1049")));
        s.add(new Site("s122_05_antrobot_98_unridden_step_clears_ridden_step_keeps", TargetReleaseParityTests::antRobotMemoBoundary));
        s.add(new Site("s122_06_pitchblack_501_seen_kept_through_wall", h -> memoSeenKept(h, ModEntities.PITCH_BLACK.get(), "isSuitableTarget", "PitchBlack.java:501-503")));
        s.add(new Site("s122_07_pitchblack_501_unseen_kept_after_exposure", h -> memoUnseenKept(h, ModEntities.PITCH_BLACK.get(), "isSuitableTarget", "PitchBlack.java:501-503")));
        s.add(new Site("s122_08_pitchblack_330_activity0_step_clears_active_keeps", TargetReleaseParityTests::pitchBlackMemoBoundary));
        s.add(new Site("s122_09_pitchblack_259_heal_and_null_scan_deactivates", TargetReleaseParityTests::pitchBlackHealBranch));
        s.add(new Site("s122_10_pitchblack_277_spontaneous_activation_stops_navigation", TargetReleaseParityTests::pitchBlackActivation));
        return s;
    }

    /** One test per row: 53 TestFunctions in the {@code targetReleaseParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> targetReleaseSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    private static void run(GameTestHelper helper, Site site) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — several passes and every vanilla target"
                        + " screen refuse players on Peaceful (" + FINDING + " test setup)");
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                "precondition: PlayNicely must be off — the scans answer nothing under it (ENT-S-115); rows that flip it"
                        + " restore it themselves (" + FINDING + " test setup)");
        site.body().accept(helper);
    }

    // ------------------------------------------------------------------
    // Part 1 — the ownership convention
    // ------------------------------------------------------------------

    /** (a) The scan's own pick, once behind a wall, is not found again and the next pass clears the slot (orig Nastysaurus.java:240-242 stood down). */
    private static void ownershipA(GameTestHelper helper) {
        Mob nasty = null;
        Mob pig = null;
        try {
            nasty = spawnFrozen(helper, ModEntities.NASTYSAURUS.get(), HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, nasty, pig, "a pig 8 blocks east");
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == pig && scanPick(nasty, Nastysaurus.class) == pig,
                    "control: the pass takes the pig as the scan's own pick (" + FINDING + "); slot " + describe(nasty.getTarget()));
            setWall(helper, true);
            nasty.getSensing().tick();
            helper.assertTrue(!nasty.hasLineOfSight(pig), "precondition: the wall hides the pig (" + FINDING + " test geometry)");
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == null && scanPick(nasty, Nastysaurus.class) == null,
                    "(a) orig Nastysaurus.java:227-229, :240-242 — the scan's pick was transient: not found again behind the wall,"
                            + " the pass stands down and the slot is cleared (" + FINDING + "); slot " + describe(nasty.getTarget()));
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(nasty);
        }
        helper.succeed();
    }

    /**
     * (b) + (c), tick-driven: a Cryolophosaurus (which the Nastysaurus scan refuses, orig :262-264) hits the hunter and
     * becomes the revenge occupant; it dies; the pass (pinned to fire every tick) takes the pig as the scan's own pick,
     * and the revenge goal's cleanup re-asserts that pig into the slot — the same-entity re-set. Under the every-set
     * clear that re-assert turned the pig foreign and sticky (ENT-S-117 refuter B's window); under the ruled
     * convention the mark survives, and once the wall hides the pig the pass clears it and the goal ends. The
     * hunter's speed is zeroed so the geometry stands while its AI runs.
     */
    private static void ownershipB(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        Mob spawnedNasty = null;
        Mob spawnedCryo = null;
        Mob spawnedPig = null;
        try {
            spawnedNasty = helper.spawn(ModEntities.NASTYSAURUS.get(), HUNTER_POS);
            spawnedNasty.setPersistenceRequired();
            spawnedNasty.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
            // the pass every tick (5 -> 0), the 1-in-250 quiet, the swing dice quiet (4 -> 1; the second die shares bound 5)
            replaceRandom(spawnedNasty, rolls(5, 0, 250, 0, 4, 1));
            spawnedCryo = spawnPrey(helper, ModEntities.CRYOLOPHOSAURUS.get(), NEAR_POS);
            spawnedPig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, spawnedNasty, spawnedPig, "a pig 8 blocks east");
        } catch (Throwable e) {
            discardQuietly(spawnedPig);
            discardQuietly(spawnedCryo);
            discardQuietly(spawnedNasty);
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed", e);
        }
        final Mob nasty = spawnedNasty;
        final Mob cryo = spawnedCryo;
        final Mob pig = spawnedPig;
        final Runnable cleanup = () -> {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(cryo);
            discardQuietly(nasty);
        };
        helper.runAfterDelay(2, () -> guarded(cleanup, () -> {
            nasty.hurt(nasty.damageSources().mobAttack(cryo), 1.0f);
            helper.assertTrue(nasty.getLastHurtByMob() == cryo, "precondition: the hit is recorded as lastHurtByMob (" + FINDING + " test setup)");
        }));
        helper.runAfterDelay(6, () -> guarded(cleanup, () -> {
            helper.assertTrue(nasty.getTarget() == cryo && scanPick(nasty, Nastysaurus.class) == null,
                    "precondition: within two cleanup passes the revenge goal has started on the Cryolophosaurus, a foreign occupant"
                            + " (" + FINDING + " test setup); slot " + describe(nasty.getTarget()) + ", scanPick " + describe(scanPick(nasty, Nastysaurus.class)));
            cryo.kill();
            helper.assertTrue(!cryo.isAlive(), "precondition: the revenge target is dead (" + FINDING + " test setup)");
            // Pin the next serverAiStep to a running-only tick (Mob.serverAiStep runs the target-selector cleanup on even
            // tickCount + id ticks before the pass): the pass must fire before the cleanup re-assert for the (b)/(c) window
            // to open; in the other parity the goal simply stops on the dead target and nothing re-asserts (refuter B, B2).
            if ((nasty.tickCount + 1 + nasty.getId()) % 2 == 0) nasty.tickCount++;
        }));
        helper.runAfterDelay(10, () -> guarded(cleanup, () -> {
            Goal rg = revengeGoal(nasty, Nastysaurus.class);
            boolean running = nasty.targetSelector.getAvailableGoals().stream().anyMatch(w -> w.getGoal() == rg && w.isRunning());
            helper.assertTrue(running, "precondition: the revenge goal is still running on the pig it re-asserted — the (b)/(c) window is"
                    + " open (refuter B) (" + FINDING + " test setup)");
            helper.assertTrue(nasty.getTarget() == pig && scanPick(nasty, Nastysaurus.class) == pig,
                    "(b)/(c) after the dead revenge target was dropped the pass took the pig, and the revenge goal's cleanup re-asserted"
                            + " it into the slot: the same-entity re-set must keep the scan's mark (the ruled convention; an every-set clear"
                            + " turned the pick foreign here — ENT-S-117 refuter B) (" + FINDING + "); slot " + describe(nasty.getTarget())
                            + ", scanPick " + describe(scanPick(nasty, Nastysaurus.class)));
            setWall(helper, true);
        }));
        helper.runAfterDelay(14, () -> {
            try {
                helper.assertTrue(nasty.getTarget() == null,
                        "(b)/(c) with the pig hidden the pass re-derived its own pick, found nothing and cleared the slot, and the goal"
                                + " whose memory was released ended — a sticky pig here is the every-set clear's window (" + FINDING + ");"
                                + " slot " + describe(nasty.getTarget()) + ", scanPick " + describe(scanPick(nasty, Nastysaurus.class)));
            } finally {
                cleanup.run();
            }
            helper.succeed();
        });
    }

    /** (c) setTarget: a re-assert of the occupant already in the slot keeps the mark; a change of occupant ends it. */
    private static void ownershipC(GameTestHelper helper) {
        Mob nasty = null;
        Mob pig = null;
        Mob zombie = null;
        try {
            nasty = spawnFrozen(helper, ModEntities.NASTYSAURUS.get(), HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, FAR_POS);
            assertSees(helper, nasty, pig, "a pig 8 blocks east");
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == pig && scanPick(nasty, Nastysaurus.class) == pig,
                    "control: the pass takes the pig as the scan's own pick (" + FINDING + ")");
            nasty.setTarget(pig);
            helper.assertTrue(scanPick(nasty, Nastysaurus.class) == pig,
                    "(c) Nastysaurus.setTarget: re-asserting the occupant already in the slot — TargetGoal.canContinueToUse's per-pass"
                            + " re-set while the revenge goal runs — must keep the scan's ownership, else its pick turns sticky (" + FINDING + ")");
            nasty.setTarget(zombie);
            helper.assertTrue(nasty.getTarget() == zombie && scanPick(nasty, Nastysaurus.class) == null,
                    "(c) a change of occupant by another path ends the scan's ownership (" + FINDING + ")");
            nasty.setTarget(null);
            helper.assertTrue(scanPick(nasty, Nastysaurus.class) == null, "clearing the slot clears the mark (" + FINDING + ")");
        } finally {
            discardQuietly(zombie);
            discardQuietly(pig);
            discardQuietly(nasty);
        }
        helper.succeed();
    }

    /**
     * (d) A hit the Emperor Scorpion's 30-tick hurt timer swallows (orig EmperorScorpion.java:383-385) stores nothing
     * and keeps the pick transient; a stored hit by the pick (:389-393) ends the mark — the attacker is the stored
     * target from then on.
     */
    private static void ownershipD(GameTestHelper helper) {
        Mob scorp = null;
        Mob pig = null;
        try {
            scorp = spawnFrozen(helper, ModEntities.ENTITY_EMPEROR_SCORPION.get(), HUNTER_POS);
            scorp.tickCount = TICKS_ALIVE;
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, scorp, pig, "a pig 8 blocks east");
            invoke(scorp, EntityEmperorScorpion.class, "selectTarget");
            helper.assertTrue(scorp.getTarget() == pig && scanPick(scorp, EntityEmperorScorpion.class) == pig,
                    "control: the pass takes the pig as the scan's own pick (" + FINDING + ")");
            float before = scorp.getHealth();
            scorp.hurt(scorp.damageSources().fall(), 1.0f);
            helper.assertTrue(scorp.getHealth() < before, "precondition: an unrelated hit lands and arms the 30-tick hurt timer (" + FINDING + " test setup)");
            float armed = scorp.getHealth();
            scorp.hurt(scorp.damageSources().mobAttack(pig), 1.0f);
            helper.assertTrue(scorp.getHealth() == armed && scorp.getLastHurtByMob() != pig,
                    "precondition: the pig's hit inside the timer is swallowed — no super.hurt, no store (" + FINDING + " test setup)");
            helper.assertTrue(scanPick(scorp, EntityEmperorScorpion.class) == pig && scorp.getTarget() == pig,
                    "(d) a swallowed hit stores nothing in 1.7.10 (orig EmperorScorpion.java:383-385), so the scan's mark on its pick"
                            + " survives it (" + FINDING + "); scanPick " + describe(scanPick(scorp, EntityEmperorScorpion.class)));
            writeField(scorp, EntityEmperorScorpion.class, "hurtTimer", 0);
            scorp.hurt(scorp.damageSources().mobAttack(pig), 1.0f);
            helper.assertTrue(scorp.getTarget() == pig && scanPick(scorp, EntityEmperorScorpion.class) == null,
                    "(d) a stored hit by the pick (orig :389-393, the Mob store) ends the mark: the pig is the STORED target from here"
                            + " (" + FINDING + "); scanPick " + describe(scanPick(scorp, EntityEmperorScorpion.class)));
            setWall(helper, true);
            scorp.getSensing().tick();
            replaceRandom(scorp, rolls(100, 1));
            invoke(scorp, EntityEmperorScorpion.class, "selectTarget");
            helper.assertTrue(scorp.getTarget() == pig,
                    "orig EmperorScorpion.java:409-417 — the stored attack target stands, wall or no wall (" + FINDING + "); slot " + describe(scorp.getTarget()));
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(scorp);
        }
        helper.succeed();
    }

    /**
     * (e) PlayNicely flips on with the scan's own pick in the slot after the revenge goal's cleanup re-asserted it
     * (ENT-S-115 refuter B1's row, in the re-assert window): the next pass must still see its own pick, run it on
     * to the gated scan and clear it, as 1.7.10 stood down (orig Nastysaurus.java:240-242).
     */
    private static void ownershipE(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob nasty = null;
        Mob cryo = null;
        Mob pig = null;
        try {
            nasty = spawnFrozen(helper, ModEntities.NASTYSAURUS.get(), HUNTER_POS);
            nasty.tickCount = TICKS_ALIVE;
            cryo = spawnPrey(helper, ModEntities.CRYOLOPHOSAURUS.get(), NEAR_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            replaceRandom(nasty, rolls(250, 0));
            Goal goal = revengeGoal(nasty, Nastysaurus.class);
            nasty.hurt(nasty.damageSources().mobAttack(cryo), 1.0f);
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes the Cryolophosaurus (" + FINDING + " test setup)");
            goal.start();
            helper.assertTrue(nasty.getTarget() == cryo, "precondition: the revenge occupant (" + FINDING + " test setup)");
            cryo.kill();
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == pig && scanPick(nasty, Nastysaurus.class) == pig,
                    "precondition: the dead revenge target dropped, the pig taken as the scan's own pick (" + FINDING + " test setup)");
            helper.assertTrue(goal.canContinueToUse() && nasty.getTarget() == pig,
                    "precondition: the goal's cleanup re-asserts the pig (" + FINDING + " test setup)");
            helper.assertTrue(scanPick(nasty, Nastysaurus.class) == pig, "the re-assert keeps the mark (the ruled convention) (" + FINDING + ")");
            OreSpawnConfig.PLAY_NICELY.set(true);
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == null && scanPick(nasty, Nastysaurus.class) == null,
                    "(e) with PlayNicely on the scan's own pick — still its own after the re-assert — runs on to the gated scan and is"
                            + " cleared (orig Nastysaurus.java:240-242 stood down); a foreign pig here is the every-set clear's residue"
                            + " (" + FINDING + "); slot " + describe(nasty.getTarget()));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(pig);
            discardQuietly(cryo);
            discardQuietly(nasty);
        }
        helper.succeed();
    }

    /** (f) A foreign revenge set by the revenge goal while the scan's pick is stored ends the mark; the next pass leaves it standing. */
    private static void ownershipF(GameTestHelper helper) {
        Mob nasty = null;
        Mob cryo = null;
        Mob pig = null;
        try {
            nasty = spawnFrozen(helper, ModEntities.NASTYSAURUS.get(), HUNTER_POS);
            nasty.tickCount = TICKS_ALIVE;
            cryo = spawnPrey(helper, ModEntities.CRYOLOPHOSAURUS.get(), NEAR_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            replaceRandom(nasty, rolls(250, 0));
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == pig && scanPick(nasty, Nastysaurus.class) == pig,
                    "control: the pig is the scan's own pick (" + FINDING + ")");
            Goal goal = revengeGoal(nasty, Nastysaurus.class);
            nasty.hurt(nasty.damageSources().mobAttack(cryo), 1.0f);
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes the Cryolophosaurus (" + FINDING + " test setup)");
            goal.start();
            helper.assertTrue(nasty.getTarget() == cryo && scanPick(nasty, Nastysaurus.class) == null,
                    "(f) the revenge goal's set of another entity ends the scan's ownership (" + FINDING + "); scanPick "
                            + describe(scanPick(nasty, Nastysaurus.class)));
            invoke(nasty, Nastysaurus.class, "selectTarget");
            helper.assertTrue(nasty.getTarget() == cryo && scanPick(nasty, Nastysaurus.class) == null,
                    "(f) orig Nastysaurus.java:214-227 — a live, visible rt stands and the scan does not run (" + FINDING + "); slot "
                            + describe(nasty.getTarget()));
        } finally {
            discardQuietly(pig);
            discardQuietly(cryo);
            discardQuietly(nasty);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Part 2 — the rows
    // ------------------------------------------------------------------

    /**
     * The rt hunters' forget (Nastysaurus 1-in-250, TRex 1-in-200): rolled inside the pass on the revenge occupant,
     * final against the goal's memory (no vanilla re-assert), and no longer rolled by the melee goal every tick.
     */
    private static void rtForget(GameTestHelper helper, EntityType<? extends Mob> type, Class<?> cls, int passBound, int passAnswer,
                                 int forgetBound, String cite) {
        Mob hunter = null;
        Mob cryo = null;
        try {
            hunter = spawnWithGoals(helper, type, HUNTER_POS);
            hunter.tickCount = TICKS_ALIVE;
            cryo = spawnPrey(helper, ModEntities.CRYOLOPHOSAURUS.get(), NEAR_POS);
            Goal goal = revengeGoal(hunter, cls);
            hunter.hurt(hunter.damageSources().mobAttack(cryo), 1.0f);
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes the Cryolophosaurus (" + FINDING + " test setup)");
            goal.start();
            helper.assertTrue(hunter.getTarget() == cryo, "precondition: the revenge occupant (" + FINDING + " test setup)");
            // the melee goal, ticked with the old every-tick forget pinned to fire: the slot must be kept
            Goal melee = goalOf(hunter.goalSelector, DinosaurMeleeAttackGoal.class);
            replaceRandom(hunter, rolls(forgetBound, 0, passBound, passAnswer == 0 ? 1 : 0));
            helper.assertTrue(melee.canUse(), "precondition: the melee goal engages the stored target (" + FINDING + " test setup)");
            melee.start();
            melee.tick();
            helper.assertTrue(hunter.getTarget() == cryo, "the melee goal no longer rolls the forget every tick (Presets forget 0): the"
                    + " revenge target is kept through a goal tick with nextInt(" + forgetBound + ") pinned to 0 (" + FINDING + "); slot " + describe(hunter.getTarget()));
            // the pass with the forget pinned quiet keeps rt; pinned to fire, releases it — finally
            replaceRandom(hunter, rolls(passBound, passAnswer, forgetBound, 0));
            invoke(hunter, cls, "selectTarget");
            helper.assertTrue(hunter.getTarget() == cryo && goal.canContinueToUse() && hunter.getTarget() == cryo,
                    "control: with nextInt(" + forgetBound + ") pinned to 0 the pass keeps rt and the goal keeps holding it (orig " + cite + ") ("
                            + FINDING + "); slot " + describe(hunter.getTarget()));
            replaceRandom(hunter, rolls(passBound, passAnswer, forgetBound, 1));
            invoke(hunter, cls, "selectTarget");
            helper.assertTrue(hunter.getTarget() == null, "orig " + cite + " — `nextInt(" + forgetBound + ") == 1` inside the pass drops rt: the slot is"
                    + " cleared (" + FINDING + "); slot " + describe(hunter.getTarget()));
            helper.assertTrue(!goal.canContinueToUse() && hunter.getTarget() == null,
                    "orig " + cite + " — rt = null is final: the revenge goal's memory went with the slot, so its cleanup does not re-assert"
                            + " the Cryolophosaurus (vanilla TargetGoal would) (" + FINDING + "); slot " + describe(hunter.getTarget()));
        } finally {
            discardQuietly(cryo);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /**
     * The rt hunters' out-of-sight skip: rt behind the wall is skipped for the pass and KEPT — the scan runs and its
     * pick takes the slot for the goal; once that pick is gone the revenge goal restores rt; with nothing else in
     * sight the pass leaves rt in the slot.
     */
    private static void rtSightSkip(GameTestHelper helper, EntityType<? extends Mob> type, Class<?> cls, int passBound, int passAnswer,
                                    int forgetBound, String cite) {
        Mob hunter = null;
        Mob cryo = null;
        Mob pig = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            hunter.tickCount = TICKS_ALIVE;
            cryo = spawnPrey(helper, ModEntities.CRYOLOPHOSAURUS.get(), BEHIND_WALL_POS);
            pig = spawnPrey(helper, EntityType.PIG, TOUCH_POS);
            replaceRandom(hunter, rolls(passBound, passAnswer, forgetBound, 0));
            Goal goal = revengeGoal(hunter, cls);
            hunter.hurt(hunter.damageSources().mobAttack(cryo), 1.0f);
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes the Cryolophosaurus (" + FINDING + " test setup)");
            goal.start();
            setWall(helper, true);
            hunter.getSensing().tick();
            helper.assertTrue(!hunter.hasLineOfSight(cryo) && hunter.hasLineOfSight(pig),
                    "precondition: the wall hides the Cryolophosaurus, the pig in front of it stays in sight (" + FINDING + " test geometry)");
            invoke(hunter, cls, "selectTarget");
            helper.assertTrue(hunter.getTarget() == pig && scanPick(hunter, cls) == pig,
                    "orig " + cite + " — rt out of sight is skipped for the pass: the scan runs and its pick takes the slot (" + FINDING + ");"
                            + " slot " + describe(hunter.getTarget()));
            helper.assertTrue(goal.canContinueToUse() && hunter.getTarget() == pig && scanPick(hunter, cls) == pig,
                    "the goal's cleanup re-asserts the pig (same entity), the mark kept (" + FINDING + ")");
            pig.discard();
            hunter.getSensing().tick();
            invoke(hunter, cls, "selectTarget");
            helper.assertTrue(hunter.getTarget() == null, "precondition: the pig gone, the scan finds nothing and clears its own pick (" + FINDING + " test setup)");
            helper.assertTrue(goal.canContinueToUse() && hunter.getTarget() == cryo,
                    "orig " + cite + " — rt was KEPT through the sight loss: with the slot empty the revenge goal's memory restores it"
                            + " (" + FINDING + "); slot " + describe(hunter.getTarget()));
            hunter.getSensing().tick();
            invoke(hunter, cls, "selectTarget");
            helper.assertTrue(hunter.getTarget() == cryo,
                    "orig " + cite + " with :240-242 — rt still hidden and nothing else in sight: the pass stands down and rt stays in the"
                            + " slot (disclosed: the goal keeps chasing it) (" + FINDING + "); slot " + describe(hunter.getTarget()));
            setWall(helper, false);
            hunter.getSensing().tick();
            invoke(hunter, cls, "selectTarget");
            helper.assertTrue(hunter.getTarget() == cryo && scanPick(hunter, cls) == null,
                    "control: rt back in sight stands, foreign (" + FINDING + "); slot " + describe(hunter.getTarget()));
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(cryo);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** Pointysaurus: the 1-in-250 inside the 1-in-6 pass on rt alone, final; under PlayNicely the pass reads nothing (orig :185-187). */
    private static void pointysaurusForget(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob pointy = null;
        Mob pig = null;
        try {
            pointy = spawnFrozen(helper, ModEntities.POINTYSAURUS.get(), HUNTER_POS);
            pointy.tickCount = TICKS_ALIVE;
            pig = spawnPrey(helper, EntityType.PIG, NEAR_POS);
            Goal goal = revengeGoal(pointy, Pointysaurus.class);
            pointy.hurt(pointy.damageSources().mobAttack(pig), 1.0f);
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes the pig (" + FINDING + " test setup)");
            goal.start();
            helper.assertTrue(pointy.getTarget() == pig, "precondition: the revenge occupant (" + FINDING + " test setup)");
            OreSpawnConfig.PLAY_NICELY.set(true);
            replaceRandom(pointy, rolls(6, 0, 250, 1));
            invokeCustomServerAiStep(pointy);
            helper.assertTrue(pointy.getTarget() == pig && goal.canContinueToUse(),
                    "orig Pointysaurus.java:185-187 — under PlayNicely the pass's copy of rt is blanked: no roll, rt kept (the ENT-S-115"
                            + " deferral) (" + FINDING + "); slot " + describe(pointy.getTarget()));
            OreSpawnConfig.PLAY_NICELY.set(prior);
            replaceRandom(pointy, rolls(6, 0, 250, 0));
            invokeCustomServerAiStep(pointy);
            helper.assertTrue(pointy.getTarget() == pig, "control: nextInt(250) pinned to 0 keeps rt (" + FINDING + ")");
            replaceRandom(pointy, rolls(6, 0, 250, 1));
            invokeCustomServerAiStep(pointy);
            helper.assertTrue(pointy.getTarget() == null && !goal.canContinueToUse() && pointy.getTarget() == null,
                    "orig Pointysaurus.java:189-191 — `nextInt(250) == 1` inside the 1-in-6 pass drops rt, finally: the slot cleared and the"
                            + " revenge goal's memory gone (" + FINDING + "); slot " + describe(pointy.getTarget()));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(pig);
            discardQuietly(pointy);
        }
        helper.succeed();
    }

    /** Emperor Scorpion: the 1-in-100 inside the 1-in-4 pass (`== 0`), on the stored attack target, final; not the goal's every tick. */
    private static void emperorForget(GameTestHelper helper) {
        Mob scorp = null;
        Mob minion = null;
        try {
            scorp = spawnWithGoals(helper, ModEntities.ENTITY_EMPEROR_SCORPION.get(), HUNTER_POS);
            scorp.tickCount = TICKS_ALIVE;
            minion = spawnPrey(helper, ModEntities.ENTITY_SCORPION.get(), NEAR_POS);
            Goal goal = revengeGoal(scorp, EntityEmperorScorpion.class);
            writeField(scorp, EntityEmperorScorpion.class, "hurtTimer", 0);
            scorp.hurt(scorp.damageSources().mobAttack(minion), 1.0f);
            helper.assertTrue(scorp.getTarget() == minion && scanPick(scorp, EntityEmperorScorpion.class) == null,
                    "precondition: the Scorpion attacker is stored by hurt (orig EmperorScorpion.java:389-393) (" + FINDING + " test setup)");
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes it too (" + FINDING + " test setup)");
            goal.start();
            Goal melee = goalOf(scorp.goalSelector, BugMeleeAttackGoal.class);
            replaceRandom(scorp, rolls(100, 0, 4, 1));
            helper.assertTrue(melee.canUse(), "precondition: the melee goal engages the stored target (" + FINDING + " test setup)");
            melee.start();
            melee.tick();
            helper.assertTrue(scorp.getTarget() == minion, "the melee goal no longer rolls the 1-in-100 every tick (Params.emperorScorpion forget 0)"
                    + " (" + FINDING + "); slot " + describe(scorp.getTarget()));
            replaceRandom(scorp, rolls(4, 0, 100, 1, 20, 0));
            invokeCustomServerAiStep(scorp);
            helper.assertTrue(scorp.getTarget() == minion && goal.canContinueToUse(),
                    "control: nextInt(100) pinned to 1 keeps the stored target (orig :414 rolls `== 0`) (" + FINDING + ")");
            replaceRandom(scorp, rolls(4, 0, 100, 0, 20, 0));
            invokeCustomServerAiStep(scorp);
            helper.assertTrue(scorp.getTarget() == null && !goal.canContinueToUse() && scorp.getTarget() == null,
                    "orig EmperorScorpion.java:414-416 — `nextInt(100) == 0` inside the 1-in-4 pass clears the stored attack target, finally"
                            + " (the revenge goal's memory dropped with it) (" + FINDING + "); slot " + describe(scorp.getTarget()));
        } finally {
            discardQuietly(minion);
            discardQuietly(scorp);
        }
        helper.succeed();
    }

    /** Cater Killer: the 1-in-200 inside the 1-in-4 pass (`== 0`), after the read, final; not the goal's every tick. */
    private static void caterKillerForget(GameTestHelper helper) {
        Mob cater = null;
        Mob pig = null;
        try {
            cater = spawnWithGoals(helper, ModEntities.ENTITY_CATER_KILLER.get(), HUNTER_POS);
            cater.tickCount = TICKS_ALIVE;
            pig = spawnPrey(helper, EntityType.PIG, NEAR_POS);
            Goal goal = goalOf(cater.targetSelector, HurtByTargetGoal.class);
            cater.hurt(cater.damageSources().mobAttack(pig), 1.0f);
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes the pig (" + FINDING + " test setup)");
            goal.start();
            helper.assertTrue(cater.getTarget() == pig, "precondition: the revenge occupant (" + FINDING + " test setup)");
            Goal melee = goalOf(cater.goalSelector, BugMeleeAttackGoal.class);
            replaceRandom(cater, rolls(200, 0, 4, 1));
            helper.assertTrue(melee.canUse(), "precondition: the melee goal engages the stored target (" + FINDING + " test setup)");
            melee.start();
            melee.tick();
            helper.assertTrue(cater.getTarget() == pig, "the melee goal no longer rolls the 1-in-200 every tick (Params.caterKiller forget 0)"
                    + " (" + FINDING + "); slot " + describe(cater.getTarget()));
            // the pass: 4 -> 0 fires it; 200 -> 1 quiet / 0 fires; the tree-eat rolls (8, 30) quiet
            replaceRandom(cater, rolls(4, 0, 200, 1, 8, 1, 30, 1));
            invokeCustomServerAiStep(cater);
            helper.assertTrue(cater.getTarget() == pig && goal.canContinueToUse(), "control: nextInt(200) pinned to 1 keeps the stored target"
                    + " (orig CaterKiller.java:468 rolls `== 0`) (" + FINDING + ")");
            replaceRandom(cater, rolls(4, 0, 200, 0, 8, 1, 30, 1));
            invokeCustomServerAiStep(cater);
            helper.assertTrue(cater.getTarget() == null && !goal.canContinueToUse() && cater.getTarget() == null,
                    "orig CaterKiller.java:468-470 — `nextInt(200) == 0` inside the 1-in-4 pass clears the stored attack target, finally"
                            + " (" + FINDING + "); slot " + describe(cater.getTarget()));
        } finally {
            discardQuietly(pig);
            discardQuietly(cater);
        }
        helper.succeed();
    }

    /** Giant Robot: the 1-in-100 clear BEFORE the read — the cleared target is not engaged this pass (no bite), and the goal ends. */
    private static void giantRobotClearBeforeRead(GameTestHelper helper) {
        Mob robot = null;
        Mob zombie = null;
        try {
            robot = spawnFrozen(helper, ModEntities.GIANT_ROBOT.get(), HUNTER_POS);
            robot.tickCount = TICKS_ALIVE;
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            writeField(robot, danger.orespawn.entity.GiantRobot.class, "reloadTicker", 1000);
            // orig GiantRobot.java:256-263 — the pass engages only within 0.5 rad of the HEAD bearing (rotationYawHead, :257);
            // faceEntity (:254, the port's lookAt) turns the body alone and nothing ticks a frozen hunter's head, so the spawned
            // robot (facing south, yaw 0) is turned to face the Zombie due east before any pass: the bite is the row's signal
            robot.setYRot(-90.0f);
            robot.setYBodyRot(-90.0f);
            robot.setYHeadRot(-90.0f);
            helper.assertTrue(headBearingError(robot, zombie) < 0.5, "precondition: the robot's head faces the Zombie within 0.5 rad"
                    + " (orig GiantRobot.java:256-263) (" + FINDING + " test geometry); error " + headBearingError(robot, zombie));
            Goal goal = revengeGoal(robot, danger.orespawn.entity.GiantRobot.class);
            robot.hurt(robot.damageSources().mobAttack(zombie), 1.0f);
            helper.assertTrue(robot.getTarget() == zombie, "precondition: the Zombie attacker is stored by hurt (" + FINDING + " test setup)");
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes it (" + FINDING + " test setup)");
            goal.start();
            double reach = 8.0 + zombie.getBbWidth() / 2.0;
            helper.assertTrue(robot.distanceToSqr(zombie) < reach * reach && robot.distanceToSqr(zombie) < 256.0,
                    "precondition: the Zombie stands inside the melee reach (8 + w/2) and the 16-block engagement (" + FINDING + " test geometry)");
            float health = zombie.getHealth();
            replaceRandom(robot, rolls(5, 0, 100, 1));
            invokeCustomServerAiStep(robot);
            helper.assertTrue(zombie.getHealth() == health && robot.getTarget() == null,
                    "orig GiantRobot.java:243-246 — the 1-in-100 clear BEFORE the read: the cleared target is not engaged this pass (no"
                            + " bite; the scan refuses a Monster) and the slot is empty (" + FINDING + "); health " + zombie.getHealth() + " of " + health
                            + ", slot " + describe(robot.getTarget()));
            helper.assertTrue(!goal.canContinueToUse() && robot.getTarget() == null,
                    "the clear is final: the revenge goal's memory went with it (" + FINDING + "); slot " + describe(robot.getTarget()));
            robot.setTarget(zombie);
            replaceRandom(robot, rolls(5, 0, 100, 0));
            invokeCustomServerAiStep(robot);
            helper.assertTrue(zombie.getHealth() < health, "control: with the clear quiet the stored Zombie is engaged and bitten this pass ("
                    + FINDING + "); health " + zombie.getHealth());
        } finally {
            discardQuietly(zombie);
            discardQuietly(robot);
        }
        helper.succeed();
    }

    /** Crab: the missing 1-in-100 (`== 1`) inside the 1-in-5 pass (`== 1`), ahead of the read, final. */
    private static void crabForget(GameTestHelper helper) {
        Mob crab = null;
        Mob pig = null;
        try {
            crab = spawnFrozen(helper, ModEntities.CRAB.get(), HUNTER_POS);
            crab.tickCount = TICKS_ALIVE;
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            Goal goal = revengeGoal(crab, Crab.class);
            crab.hurt(crab.damageSources().mobAttack(pig), 1.0f);
            helper.assertTrue(crab.getTarget() == pig, "precondition: the pig attacker is stored by hurt (orig Crab.java:232-238) (" + FINDING + " test setup)");
            helper.assertTrue(goal.canUse(), "precondition: the revenge goal takes it (" + FINDING + " test setup)");
            goal.start();
            replaceRandom(crab, rolls(25, 1, 5, 1, 100, 0));
            invokeCustomServerAiStep(crab);
            helper.assertTrue(crab.getTarget() == pig && goal.canContinueToUse(), "control: nextInt(100) pinned to 0 keeps the stored target"
                    + " (orig Crab.java:342 rolls `== 1`) (" + FINDING + ")");
            replaceRandom(crab, rolls(25, 1, 5, 1, 100, 1));
            invokeCustomServerAiStep(crab);
            helper.assertTrue(crab.getTarget() == null && !goal.canContinueToUse() && crab.getTarget() == null,
                    "orig Crab.java:342-344 — `nextInt(100) == 1` inside the 1-in-5 pass clears the stored attack target, finally"
                            + " (" + FINDING + "); slot " + describe(crab.getTarget()));
        } finally {
            discardQuietly(pig);
            discardQuietly(crab);
        }
        helper.succeed();
    }

    /** Crab: a Mob attacker is stored, hurt timer or no hurt timer (orig :232-238); a player attacker is not. */
    private static void crabAttackerStore(GameTestHelper helper) {
        Mob crab = null;
        Mob zombie = null;
        ServerPlayer player = null;
        try {
            crab = spawnFrozen(helper, ModEntities.CRAB.get(), HUNTER_POS);
            crab.tickCount = TICKS_ALIVE;
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_5_POS));
            float health = crab.getHealth();
            crab.hurt(crab.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(crab.getHealth() < health, "precondition: the player's hit lands (hurt timer 0) (" + FINDING + " test setup)");
            helper.assertTrue(crab.getTarget() == null, "orig Crab.java:232-238 — an EntityLiving attacker only: a player who hits the crab"
                    + " is not stored by hurt (" + FINDING + "); slot " + describe(crab.getTarget()));
            float armed = crab.getHealth();
            crab.hurt(crab.damageSources().mobAttack(zombie), 1.0f);
            helper.assertTrue(crab.getHealth() == armed, "precondition: the Zombie's hit inside the 8-tick hurt timer is swallowed (" + FINDING + " test setup)");
            helper.assertTrue(crab.getTarget() == zombie, "orig Crab.java:232-238 — the EntityLiving store sits outside the hurt timer: the Zombie"
                    + " is stored though its hit was swallowed (" + FINDING + "); slot " + describe(crab.getTarget()));
        } finally {
            removePlayer(helper, player);
            discardQuietly(zombie);
            discardQuietly(crab);
        }
        helper.succeed();
    }

    /** Ant Robot: the 1-in-150 clear BEFORE the read — a stored creative player (which the scan refuses) is not chased this pass. */
    private static void antRobotClearBeforeRead(GameTestHelper helper) {
        Mob ant = null;
        ServerPlayer player = null;
        try {
            ant = spawnFrozen(helper, ModEntities.ANT_ROBOT.get(), HUNTER_POS);
            player = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_8_POS));
            helper.assertTrue(player.getAbilities().instabuild, "precondition: a creative player has instabuild (" + FINDING + " test setup)");
            ant.setTarget(player);
            ant.setDeltaMovement(0.0, 0.0, 0.0);
            replaceRandom(ant, rolls(20, 1, 150, 0, 15, 1));
            invokeCustomServerAiStep(ant);
            helper.assertTrue(ant.getTarget() == null && ant.getDeltaMovement().horizontalDistanceSqr() == 0.0,
                    "orig AntRobot.java:109-112 — the 1-in-150 clear BEFORE the read: the cleared target is not engaged this pass — no chase"
                            + " impulse toward the creative player the scan refuses (" + FINDING + "); slot " + describe(ant.getTarget())
                            + ", motion " + ant.getDeltaMovement());
            ant.setTarget(player);
            replaceRandom(ant, rolls(20, 1, 150, 1, 15, 1));
            invokeCustomServerAiStep(ant);
            helper.assertTrue(ant.getDeltaMovement().horizontalDistanceSqr() > 0.0,
                    "control: with the clear quiet the stored player is chased this pass (the 0.2 impulse of orig :114-118) (" + FINDING + ")");
        } finally {
            removePlayer(helper, player);
            discardQuietly(ant);
        }
        helper.succeed();
    }

    /** Ant Robot: an EntityLiving (Mob) attacker is stored by hurt; a player attacker is not (orig :579-583). */
    private static void antRobotAttackerStore(GameTestHelper helper) {
        Mob ant = null;
        Mob zombie = null;
        ServerPlayer player = null;
        try {
            ant = spawnFrozen(helper, ModEntities.ANT_ROBOT.get(), HUNTER_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_5_POS));
            ant.hurt(ant.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(ant.getTarget() == null, "orig AntRobot.java:579-583 — an EntityLiving attacker only: a player who hits the ant is"
                    + " not stored (" + FINDING + "); slot " + describe(ant.getTarget()));
            ant.hurt(ant.damageSources().mobAttack(zombie), 1.0f);
            helper.assertTrue(ant.getTarget() == zombie, "control: a Mob attacker is stored (" + FINDING + "); slot " + describe(ant.getTarget()));
        } finally {
            removePlayer(helper, player);
            discardQuietly(zombie);
            discardQuietly(ant);
        }
        helper.succeed();
    }

    /** Hammerhead: rt out of sight is skipped for the pass and kept; the scan's player is engaged instead. */
    private static void hammerheadSightSkip(GameTestHelper helper) {
        Mob hammer = null;
        Mob pig = null;
        ServerPlayer player = null;
        try {
            hammer = spawnFrozen(helper, ModEntities.HAMMERHEAD.get(), HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, BEHIND_WALL_POS);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_FRONT_POS));
            clearSpawnInvulnerability(player); // the pinned bite must land this tick: ServerPlayer.hurt's 60-tick spawn shield would swallow it
            writeField(hammer, Hammerhead.class, "revengeTarget", pig);
            setWall(helper, true);
            hammer.getSensing().tick();
            helper.assertTrue(!hammer.hasLineOfSight(pig) && hammer.hasLineOfSight(player),
                    "precondition: the wall hides the pig, the player in front of it stays in sight (" + FINDING + " test geometry)");
            double reach = 7.0 + pig.getBbWidth() / 2.0;
            helper.assertTrue(hammer.distanceToSqr(pig) < reach * reach, "precondition: the hidden pig stands inside the 7 + w/2 reach, so"
                    + " engaging it would bite it (" + FINDING + " test geometry)");
            float pigHealth = pig.getHealth();
            float playerHealth = player.getHealth();
            replaceRandom(hammer, rolls(3, 1, 250, 0));
            invokeCustomServerAiStep(hammer);
            helper.assertTrue(pig.getHealth() == pigHealth && player.getHealth() < playerHealth && ((Hammerhead) hammer).getAttacking() == 1,
                    "orig Hammerhead.java:203-205 — rt out of sight is skipped for the pass: the scan's player is engaged (attacking 1, the"
                            + " 1-in-3 bite pinned) and the hidden pig is untouched (" + FINDING + "); pig " + pig.getHealth() + ", player "
                            + player.getHealth() + ", attacking " + ((Hammerhead) hammer).getAttacking());
            helper.assertTrue(readField(hammer, Hammerhead.class, "revengeTarget") == pig,
                    "orig :203-205 — the skip keeps rt (" + FINDING + ")");
        } finally {
            setWall(helper, false);
            removePlayer(helper, player);
            discardQuietly(pig);
            discardQuietly(hammer);
        }
        helper.succeed();
    }

    /** The inert revenge task of 1.7.10 is not registered: no target goal takes an attacker. */
    private static void noRevengeGoal(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob hunter = null;
        Mob pig = null;
        try {
            hunter = spawnWithGoals(helper, type, HUNTER_POS);
            hunter.tickCount = TICKS_ALIVE;
            pig = spawnPrey(helper, EntityType.PIG, FAR_POS);
            assertNoGoalTakesAttacker(helper, hunter, pig, cite);
        } finally {
            discardQuietly(pig);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** CaveFisher / DungeonBeast / Urchin: the scan runs every pass regardless of a stored target; no revenge goal. */
    private static void scanEveryPass(GameTestHelper helper, EntityType<? extends Mob> type, String cite, String taskCite) {
        Mob hunter = null;
        Mob pig = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            player = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_5_POS));
            assertSees(helper, hunter, pig, "a pig 8 blocks east");
            hunter.setTarget(player);
            invoke(hunter, hunter.getClass(), "selectTarget");
            helper.assertTrue(hunter.getTarget() == pig, "orig " + cite + " — the scan runs every pass, whatever the slot held (a stored"
                    + " creative player the scan refuses is overwritten by the pig) (" + FINDING + "); slot " + describe(hunter.getTarget()));
            setWall(helper, true);
            hunter.getSensing().tick();
            invoke(hunter, hunter.getClass(), "selectTarget");
            helper.assertTrue(hunter.getTarget() == null, "orig " + cite + " — the pig behind the wall is not found again: the slot is cleared"
                    + " (" + FINDING + "); slot " + describe(hunter.getTarget()));
            discardQuietly(hunter);
            hunter = spawnWithGoals(helper, type, HUNTER_POS);
            hunter.tickCount = TICKS_ALIVE;
            assertNoGoalTakesAttacker(helper, hunter, pig, taskCite);
        } finally {
            setWall(helper, false);
            removePlayer(helper, player);
            discardQuietly(pig);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** Scorpion: an empty scan clears the slot (orig :189-191 stood down); no revenge goal. */
    private static void scorpionEmptyScan(GameTestHelper helper) {
        Mob scorp = null;
        Mob pig = null;
        try {
            scorp = spawnFrozen(helper, ModEntities.ENTITY_SCORPION.get(), HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, NEAR_POS);
            assertSees(helper, scorp, pig, "a pig 3 blocks east");
            replaceRandom(scorp, rolls(6, 0));
            invokeCustomServerAiStep(scorp);
            helper.assertTrue(scorp.getTarget() == pig, "control: the pass hands the pig to the slot (" + FINDING + ")");
            setWall(helper, true);
            pig.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(BEHIND_WALL_POS)));
            scorp.getSensing().tick();
            helper.assertTrue(!scorp.hasLineOfSight(pig), "precondition: the pig moved behind the wall is hidden (" + FINDING + " test geometry)");
            invokeCustomServerAiStep(scorp);
            helper.assertTrue(scorp.getTarget() == null, "orig Scorpion.java:176-191 — the pass acts on this pass's pick alone: the pig not found"
                    + " again, the slot is cleared and the goal stands down (" + FINDING + "); slot " + describe(scorp.getTarget()));
            discardQuietly(scorp);
            scorp = spawnWithGoals(helper, ModEntities.ENTITY_SCORPION.get(), HUNTER_POS);
            scorp.tickCount = TICKS_ALIVE;
            assertNoGoalTakesAttacker(helper, scorp, pig, "Scorpion.java:62");
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(scorp);
        }
        helper.succeed();
    }

    /** Irukandji / Skate / Sea Monster: the scan's pick is re-derived (dropped once out of the sphere); a dead stored target is cleared. */
    private static void inlinePick(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 awayPos, int[] rolls, String cite) {
        Mob hunter = null;
        Mob pig = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_5_POS));
            replaceRandom(hunter, rolls(rolls));
            invokeCustomServerAiStep(hunter);
            helper.assertTrue(hunter.getTarget() == player && scanPick(hunter, hunter.getClass()) == player,
                    "control: the pick stores the player 5 blocks off under the ownership mark (" + FINDING + "); slot " + describe(hunter.getTarget()));
            Vec3 away = helper.absoluteVec(awayPos);
            player.teleportTo(helper.getLevel(), away.x, away.y, away.z, 0.0f, 0.0f);
            invokeCustomServerAiStep(hunter);
            helper.assertTrue(hunter.getTarget() == null && scanPick(hunter, hunter.getClass()) == null,
                    "orig " + cite + " — the scan's pick was never stored: out of the player sphere it is not found again and the slot is cleared"
                            + " (" + FINDING + "); slot " + describe(hunter.getTarget()));
            pig = spawnPrey(helper, EntityType.PIG, NEAR_POS);
            hunter.setTarget(pig);
            pig.kill();
            helper.assertTrue(!pig.isAlive() && hunter.getTarget() == pig, "precondition: a dead stored target (" + FINDING + " test setup)");
            invokeCustomServerAiStep(hunter);
            helper.assertTrue(hunter.getTarget() == null, "orig " + cite + " — a stored target not alive is cleared (setAttackTarget(null)) ("
                    + FINDING + "); slot " + describe(hunter.getTarget()));
        } finally {
            removePlayer(helper, player);
            discardQuietly(pig);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** Irukandji: an empty-hand hit (:135-145, no super.hurt) keeps the mark; a stored hit ends it and the player becomes sticky. */
    private static void irukandjiHandOff(GameTestHelper helper) {
        Mob jelly = null;
        ServerPlayer player = null;
        try {
            jelly = spawnFrozen(helper, ModEntities.IRUKANDJI.get(), HUNTER_POS);
            jelly.tickCount = TICKS_ALIVE;
            jelly.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH); // the 1-HP jelly (orig OreSpawnMain.java:6509) must outlive the armed 1.0 hit whose store is the signal
            jelly.setHealth(PREY_HEALTH);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_5_POS));
            clearSpawnInvulnerability(player); // the 200 counter-damage must land this tick: ServerPlayer.hurt's 60-tick spawn shield would swallow it
            replaceRandom(jelly, rolls(10, 1, 8, 1, 4, 1));
            invokeCustomServerAiStep(jelly);
            helper.assertTrue(jelly.getTarget() == player && scanPick(jelly, Irukandji.class) == player, "control: the player is the scan's own pick (" + FINDING + ")");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "precondition: an empty main hand (" + FINDING + " test setup)");
            float playerHealth = player.getHealth();
            jelly.hurt(jelly.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(player.getHealth() < playerHealth && jelly.getLastHurtByMob() != player,
                    "precondition: the empty-hand hit is answered with 200 damage and never reaches super.hurt (orig Irukandji.java:135-145) ("
                            + FINDING + " test setup)");
            helper.assertTrue(scanPick(jelly, Irukandji.class) == player, "a hit that stores nothing keeps the pick transient (" + FINDING + ")");
            Vec3 away = helper.absoluteVec(PLAYER_8_POS);
            player.teleportTo(helper.getLevel(), away.x, away.y, away.z, 0.0f, 0.0f);
            invokeCustomServerAiStep(jelly);
            helper.assertTrue(jelly.getTarget() == null, "the transient pick out of the 6-sphere is dropped (" + FINDING + "); slot " + describe(jelly.getTarget()));
            Vec3 back = helper.absoluteVec(PLAYER_5_POS);
            player.teleportTo(helper.getLevel(), back.x, back.y, back.z, 0.0f, 0.0f);
            invokeCustomServerAiStep(jelly);
            helper.assertTrue(jelly.getTarget() == player && scanPick(jelly, Irukandji.class) == player, "control: back inside, the player is the pick again (" + FINDING + ")");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            jelly.hurt(jelly.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(jelly.getLastHurtByMob() == player, "precondition: the armed hit is stored as lastHurtByMob (" + FINDING + " test setup)");
            helper.assertTrue(jelly.getTarget() == player && scanPick(jelly, Irukandji.class) == null,
                    "orig Irukandji.java:299-302 with the revenge task — the stored hit ends the mark: the player is the STORED target from here"
                            + " (" + FINDING + "); scanPick " + describe(scanPick(jelly, Irukandji.class)));
            player.teleportTo(helper.getLevel(), away.x, away.y, away.z, 0.0f, 0.0f);
            invokeCustomServerAiStep(jelly);
            helper.assertTrue(jelly.getTarget() == player, "orig Irukandji.java:299-302 — a live stored target is answered ahead of the scan, sphere"
                    + " or no sphere (" + FINDING + "); slot " + describe(jelly.getTarget()));
        } finally {
            removePlayer(helper, player);
            discardQuietly(jelly);
        }
        helper.succeed();
    }

    /**
     * Ender Knight: the daylight roll (orig :111-115) drops the target — tick-driven: noon is set and the sky darken
     * settles before the drive; a float-forcing random makes the brightness dice land.
     */
    /* The drive also fires teleportRandomly() (EnderKnight :136): the Knight relocates within the cell's reach; harmless, discarded in the finally (refuter B, N4). */
    private static void enderKnightDaylight(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final long priorDayTime = level.getDayTime();
        Mob spawnedKnight = null;
        ServerPlayer spawnedPlayer = null;
        try {
            spawnedKnight = spawnFrozen(helper, ModEntities.ENDER_KNIGHT.get(), HUNTER_POS);
            spawnedPlayer = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_8_POS));
            spawnedKnight.setTarget(spawnedPlayer);
            replaceRandom(spawnedKnight, new ForcedFloat(RandomSource.create(1234L), 1.0f)); // aiStep runs on a noAi Knight every tick, so the daylight dice must be pinned quiet through the settle ticks (refuter B, B3)
            level.setDayTime(6000L); // noon; skyDarken settles next tick
        } catch (Throwable e) {
            level.setDayTime(priorDayTime);
            removePlayer(helper, spawnedPlayer);
            discardQuietly(spawnedKnight);
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed", e);
        }
        final Mob knight = spawnedKnight;
        final ServerPlayer player = spawnedPlayer;
        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(level.isDay(), "precondition: noon reads as day (" + FINDING + " test setup)");
                helper.assertTrue(level.canSeeSky(knight.blockPosition()), "precondition: the sky is visible over the Knight (the barrier shell"
                        + " does not occlude light) (" + FINDING + " test geometry)");
                float brightness = knight.getLightLevelDependentMagicValue();
                helper.assertTrue(brightness > 0.5f, "precondition: the Knight's brightness exceeds 0.5 at noon under the open sky (orig :111) — saw "
                        + brightness + " (" + FINDING + " test geometry)");
                helper.assertTrue(knight.getTarget() == player, "precondition: the stored target (" + FINDING + " test setup)");
                replaceRandom(knight, new ForcedFloat(RandomSource.create(1234L), 1.0f));
                knight.aiStep();
                helper.assertTrue(knight.getTarget() == player, "control: with the brightness dice missing (nextFloat 1.0: 30 < (f-0.4)*2 fails) the"
                        + " target is kept (" + FINDING + ")");
                replaceRandom(knight, new ForcedFloat(RandomSource.create(1234L), 0.0f));
                knight.aiStep();
                helper.assertTrue(knight.getTarget() == null && !((EnderKnight) knight).isScreaming(),
                        "orig EnderKnight.java:111-115 — the daylight roll (rand*30 < (f-0.4)*2, pinned to land) drops the target and stops the"
                                + " screaming (" + FINDING + "); slot " + describe(knight.getTarget()));
            } finally {
                level.setDayTime(priorDayTime);
                removePlayer(helper, player);
                discardQuietly(knight);
            }
            helper.succeed();
        });
    }

    /**
     * Ender Knight / Reaper: both target goals hold by the legacy loop's rule — no FOLLOW_RANGE release, no unseen memory;
     * a creative player dropped; a nulled slot is gone (no re-assert).
     */
    private static void legacyHold(GameTestHelper helper, EntityType<? extends Mob> type, String cite, double followRange) {
        Mob mob = null;
        Mob zombie = null;
        ServerPlayer player = null;
        try {
            mob = spawnWithGoals(helper, type, HUNTER_POS);
            mob.tickCount = TICKS_ALIVE;
            helper.assertTrue(mob.getAttributeValue(Attributes.FOLLOW_RANGE) == followRange, "precondition: FOLLOW_RANGE " + followRange + " (" + FINDING + " test setup)");
            mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_5_POS));
            zombie = spawnPrey(helper, EntityType.ZOMBIE, NEAR_POS);
            NearestAttackableTargetGoal<?> playerGoal = (NearestAttackableTargetGoal<?>) goalOf(mob.targetSelector, NearestAttackableTargetGoal.class);
            playerGoal.setTarget(player);
            playerGoal.start();
            helper.assertTrue(mob.getTarget() == player, "precondition: the player goal holds the player (" + FINDING + " test setup)");
            Vec3 far = helper.absoluteVec(PLAYER_15_POS);
            player.teleportTo(helper.getLevel(), far.x, far.y, far.z, 0.0f, 0.0f);
            helper.assertTrue(mob.distanceToSqr(player) > 64.0, "precondition: the player is beyond the lowered follow range 8 (" + FINDING + " test geometry)");
            helper.assertTrue(playerGoal.canContinueToUse() && mob.getTarget() == player,
                    "orig " + cite + " (the legacy loop, V10) — entityToAttack was held at any range: the player goal keeps its target beyond the"
                            + " follow range where vanilla TargetGoal released it (" + FINDING + ")");
            setWall(helper, true);
            for (int i = 0; i < 40; i++) {
                mob.getSensing().tick();
                helper.assertTrue(playerGoal.canContinueToUse(), "orig " + cite + " — no unseen-ticks memory: held through " + (i + 1) + " unseen cleanup"
                        + " passes (" + FINDING + ")");
            }
            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(!playerGoal.canContinueToUse(), "orig " + cite + " (V10) — a creative player is dropped (vanilla canAttack) (" + FINDING + ")");
            playerGoal.stop();
            player.setGameMode(GameType.SURVIVAL);
            Goal revenge = goalOf(mob.targetSelector, HurtByTargetGoal.class);
            mob.hurt(mob.damageSources().mobAttack(zombie), 1.0f);
            helper.assertTrue(revenge.canUse(), "precondition: the revenge goal takes the Zombie (" + FINDING + " test setup)");
            revenge.start();
            helper.assertTrue(mob.getTarget() == zombie, "precondition: the revenge occupant (" + FINDING + " test setup)");
            zombie.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(FAR_POS)));
            helper.assertTrue(mob.distanceToSqr(zombie) > 64.0, "precondition: the Zombie is beyond follow range 8 (" + FINDING + " test geometry)");
            helper.assertTrue(revenge.canContinueToUse() && mob.getTarget() == zombie, "orig " + cite + " — the attacker is held at any range (" + FINDING + ")");
            mob.setTarget(null);
            helper.assertTrue(!revenge.canContinueToUse() && mob.getTarget() == null,
                    "orig " + cite + " — a nulled entityToAttack was gone: the revenge goal does not re-assert its memory into the emptied slot"
                            + " (vanilla TargetGoal would) (" + FINDING + "); slot " + describe(mob.getTarget()));
        } finally {
            setWall(helper, false);
            removePlayer(helper, player);
            discardQuietly(zombie);
            discardQuietly(mob);
        }
        helper.succeed();
    }

    /** Robot2: no call for help (a second Robot2 is not retargeted) and no same-kind exemption (a Robot2 attacker is taken). */
    private static void robot2Revenge(GameTestHelper helper) {
        Mob robot = null;
        Mob other = null;
        Mob attacker = null;
        Mob zombie = null;
        try {
            robot = spawnWithGoals(helper, ModEntities.ROBOT_2.get(), HUNTER_POS);
            robot.tickCount = TICKS_ALIVE;
            other = spawnWithGoals(helper, ModEntities.ROBOT_2.get(), PREY_POS);
            attacker = spawnPrey(helper, ModEntities.ROBOT_2.get(), NEAR_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, TOUCH_POS);
            Goal revenge = goalOf(robot.targetSelector, HurtByTargetGoal.class);
            robot.hurt(robot.damageSources().mobAttack(zombie), 1.0f);
            robot.setTarget(null);
            helper.assertTrue(revenge.canUse(), "precondition: the revenge goal takes the Zombie (" + FINDING + " test setup)");
            revenge.start();
            helper.assertTrue(robot.getTarget() == zombie, "precondition: the revenge occupant (" + FINDING + " test setup)");
            helper.assertTrue(other.getTarget() == null, "orig Robot2.java:57 — EntityAIHurtByTarget(this, false): no call for help — the second Robot2"
                    + " 8 blocks off is not retargeted (the port-only setAlertOthers is gone) (" + FINDING + "); other slot " + describe(other.getTarget()));
            revenge.stop();
            robot.tickCount = TICKS_ALIVE + 10;
            robot.invulnerableTime = 0; // the Zombie's hit armed the 20-tick hurt timer, inside which a hit of the same amount is swallowed ahead of the store (LivingEntity.hurt: amount <= lastHurt, the ownership-D shape); cleared so the row measures the goal's same-kind rule
            robot.hurt(robot.damageSources().mobAttack(attacker), 1.0f);
            robot.setTarget(null);
            helper.assertTrue(robot.getLastHurtByMob() == attacker, "precondition: the Robot2 attacker is recorded (" + FINDING + " test setup)");
            helper.assertTrue(revenge.canUse(), "orig Robot2.java:57, :338-351 — a Robot2 attacker is stored like any other: no same-kind damage"
                    + " exemption (the port-only Robot2.class ignore is gone) (" + FINDING + ")");
        } finally {
            discardQuietly(zombie);
            discardQuietly(attacker);
            discardQuietly(other);
            discardQuietly(robot);
        }
        helper.succeed();
    }

    /** The five revenge-memory clears: the roll forgets lastHurtByMob (orig setRevengeTarget(null)), the attack target untouched. */
    private static void revengeMemoryClear(GameTestHelper helper, EntityType<? extends Mob> type, int[] rolls, String cite) {
        Mob mob = null;
        Mob pig = null;
        Mob zombie = null;
        try {
            mob = spawnFrozen(helper, type, HUNTER_POS);
            mob.tickCount = TICKS_ALIVE;
            pig = spawnPrey(helper, EntityType.PIG, FAR_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, FAR_POS);
            mob.setLastHurtByMob(pig);
            mob.setTarget(zombie);
            replaceRandom(mob, rolls(rolls));
            invokeCustomServerAiStep(mob);
            helper.assertTrue(mob.getLastHurtByMob() == null, "orig " + cite + " — func_70604_c(null) = setRevengeTarget(null): the revenge MEMORY is"
                    + " forgotten (lastHurtByMob) (" + FINDING + "); lastHurtByMob " + describe(mob.getLastHurtByMob()));
            helper.assertTrue(mob.getTarget() == zombie, "orig " + cite + " — the attack target is not what orig cleared (" + FINDING + "); slot "
                    + describe(mob.getTarget()));
        } finally {
            discardQuietly(zombie);
            discardQuietly(pig);
            discardQuietly(mob);
        }
        helper.succeed();
    }

    /** Boyfriend / Girlfriend: the monster goal's box and hold are 15 (orig targetDistance), not the FOLLOW_RANGE attribute's 16. */
    private static void helperHoldDistance(GameTestHelper helper, EntityType<? extends Mob> type, String cite) {
        Mob mob = null;
        Mob zombie = null;
        try {
            mob = spawnWithGoals(helper, type, HUNTER_POS);
            helper.assertTrue(mob.getAttributeValue(Attributes.FOLLOW_RANGE) == 16.0, "precondition: the FOLLOW_RANGE attribute is vanilla's 16 (" + FINDING + " test setup)");
            NearestAttackableTargetGoal<?> goal = null;
            for (WrappedGoal wrapped : mob.targetSelector.getAvailableGoals()) {
                if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest
                        && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == Mob.class) {
                    goal = nearest;
                }
            }
            helper.assertTrue(goal != null, "precondition: the NearestAttackableTargetGoal<Mob> of orig " + cite + " (ENT-S-124) (" + FINDING + " test setup)");
            Object distance = invoke(goal, TargetGoal.class, "getFollowDistance");
            helper.assertTrue(distance instanceof Double d && d == 15.0, "orig " + cite + " targetDistance 15.0f (MyEntityAINearestAttackableTarget.java:36,"
                    + " MyEntityAITarget.java:52) — getFollowDistance answers 15, not the attribute's 16 (" + FINDING + "); saw " + distance);
            replaceRandom(mob, rolls(5, 0));
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            // The hunter stands at its block's bottom centre (x 20.5). Two terms gate the goal, both fed by getFollowDistance: the
            // box (orig MyEntityAINearestAttackableTarget.java:56 expand(d, 4, d) / vanilla inflate — an intersection test, so it
            // admits a centre up to d + w/2 + w'/2) and the centre-distance test (vanilla TargetingConditions.range: <= d; orig
            // MyEntityAITarget.java:52 the hold's > d²). The Zombie's centre at x 36.3 is 15.8 off: outside both 15 terms (box
            // 15.55, distance 15), inside both 16 terms (box 16.55, distance 16); at the old 35.5 (15.0 off) both 15 terms took it.
            Vec3 outside = helper.absoluteVec(new Vec3(36.3, 1.0, 24.5));
            zombie.moveTo(outside.x, outside.y, outside.z, 0.0f, 0.0f);
            double halfWidths = mob.getBbWidth() / 2.0 + zombie.getBbWidth() / 2.0;
            double centreDistance = Math.sqrt(mob.distanceToSqr(zombie));
            helper.assertTrue(centreDistance > 15.0 && centreDistance >= 15.0 + halfWidths && centreDistance <= 16.0 && centreDistance < 16.0 + halfWidths,
                    "precondition: the Zombie's centre " + centreDistance + " blocks off lies outside both 15 terms (box " + (15.0 + halfWidths)
                            + ", distance 15) and inside both 16 terms (box " + (16.0 + halfWidths) + ", distance 16) (" + FINDING + " test geometry)");
            assertSees(helper, mob, zombie, "a Zombie 15.8 blocks east");
            helper.assertTrue(!goal.canUse(), "orig " + cite + " targetDistance 15 — the 15/4/15 box (to " + (15.0 + halfWidths) + " with both"
                    + " half-widths) and the <= 15 distance test both refuse a Zombie 15.8 blocks off: not taken, where the attribute's 16 took it ("
                    + FINDING + ")");
            Vec3 inside = helper.absoluteVec(new Vec3(34.5, 1.0, 24.5));
            zombie.moveTo(inside.x, inside.y, inside.z, 0.0f, 0.0f);
            mob.getSensing().tick();
            helper.assertTrue(goal.canUse(), "control: a Zombie 14 blocks off is taken (" + FINDING + ")");
            goal.start();
            helper.assertTrue(mob.getTarget() == zombie, "precondition: the goal holds the Zombie (" + FINDING + " test setup)");
            zombie.moveTo(outside.x, outside.y, outside.z, 0.0f, 0.0f);
            mob.getSensing().tick();
            helper.assertTrue(!goal.canContinueToUse(), "orig MyEntityAITarget.java:52 — the hold ends beyond targetDistance² (15²): 15.8 blocks off"
                    + " releases where 16 held (" + FINDING + ")");
        } finally {
            discardQuietly(zombie);
            discardQuietly(mob);
        }
        helper.succeed();
    }

    /** Dragonfly: the pass bites once (distSq &lt; 6) and retains nothing; the next tick without a pass bites no more. */
    private static void dragonflyOneBite(GameTestHelper helper) {
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
            replaceRandom(fly, rolls(300, 0, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() < health, "control: the hunt pass (1-in-300 retarget, 1-in-12 hunt, both pinned) bites the butterfly ("
                    + FINDING + "); health " + butterfly.getHealth());
            helper.assertTrue(fly.getTarget() == null, "orig Dragonfly.java:144-148 — the prey is never stored: nothing is retained after the pass ("
                    + FINDING + "); slot " + describe(fly.getTarget()));
            float bitten = butterfly.getHealth();
            replaceRandom(fly, rolls(300, 1, 12, 1));
            goal.tick();
            goal.tick();
            helper.assertTrue(butterfly.getHealth() == bitten, "orig Dragonfly.java:146-148 — one bite per pass: ticks without a hunt pass bite no more"
                    + " (HEAD bit the retained target every tick) (" + FINDING + "); health " + butterfly.getHealth());
        } finally {
            discardQuietly(butterfly);
            discardQuietly(fly);
        }
        helper.succeed();
    }

    /** The melee goals of the four presets stand down under PlayNicely (the slot untouched); the Emperor Scorpion's does not. */
    private static void standDown(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob viper = null;
        Mob nasty = null;
        Mob trex = null;
        Mob pointy = null;
        Mob emperor = null;
        Mob pig = null;
        try {
            pig = spawnPrey(helper, EntityType.PIG, NEAR_POS);
            viper = spawnWithGoals(helper, ModEntities.SEA_VIPER.get(), HUNTER_POS);
            nasty = spawnWithGoals(helper, ModEntities.NASTYSAURUS.get(), HUNTER_POS);
            trex = spawnWithGoals(helper, ModEntities.TREX.get(), HUNTER_POS);
            pointy = spawnWithGoals(helper, ModEntities.POINTYSAURUS.get(), HUNTER_POS);
            emperor = spawnWithGoals(helper, ModEntities.ENTITY_EMPEROR_SCORPION.get(), HUNTER_POS);
            Mob[] standing = {viper, nasty, trex, pointy};
            String[] cites = {"SeaViper.java:531-543", "Nastysaurus.java:215-217", "TRex.java:185-187", "Pointysaurus.java:185-187"};
            Goal[] goals = new Goal[standing.length];
            for (int i = 0; i < standing.length; i++) {
                standing[i].setTarget(pig);
                goals[i] = goalOf(standing[i].goalSelector, standing[i] == viper ? SeaViperBiteGoal.class : DinosaurMeleeAttackGoal.class);
                helper.assertTrue(goals[i].canUse(), "control: " + standing[i].getClass().getSimpleName() + "'s melee goal engages the stored pig with"
                        + " the flag down (" + FINDING + ")");
                goals[i].start();
                helper.assertTrue(goals[i].canContinueToUse(), "control: and keeps engaging it (" + FINDING + ")");
            }
            emperor.setTarget(pig);
            Goal emperorGoal = goalOf(emperor.goalSelector, BugMeleeAttackGoal.class);
            helper.assertTrue(emperorGoal.canUse(), "control: the Emperor Scorpion's melee goal engages the stored pig (" + FINDING + ")");
            OreSpawnConfig.PLAY_NICELY.set(true);
            for (int i = 0; i < standing.length; i++) {
                helper.assertTrue(!goals[i].canContinueToUse() && !goals[i].canUse(), "orig " + cites[i] + " — under PlayNicely the pass acted on"
                        + " nothing and stood down: the melee goal stops and does not start (" + FINDING + ")");
                goals[i].stop();
                helper.assertTrue(standing[i].getTarget() == pig, "orig " + cites[i] + " — the stored target itself is kept (" + FINDING + "); slot "
                        + describe(standing[i].getTarget()));
            }
            helper.assertTrue(emperorGoal.canUse(), "the stand-down is per preset: orig EmperorScorpion.java:409 read the stored target ungated, so its"
                    + " goal engages under the flag (" + FINDING + ")");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(pig);
            discardQuietly(emperor);
            discardQuietly(pointy);
            discardQuietly(trex);
            discardQuietly(nasty);
            discardQuietly(viper);
        }
        helper.succeed();
    }

    /** Water Dragon: the hand-off's lastHurtByMob half is pinned to THIS hit — a swallowed hit under an older record keeps the mark. */
    private static void waterDragonHandOff(GameTestHelper helper) {
        Mob dragon = null;
        ServerPlayer player = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            dragon.tickCount = TICKS_ALIVE;
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_8_POS));
            assertSees(helper, dragon, player, "a survival player 8 blocks east");
            invoke(dragon, WaterDragon.class, "selectTarget");
            helper.assertTrue(dragon.getTarget() == player && scanPick(dragon, WaterDragon.class) == player, "control: the player is the hunt's own pick (" + FINDING + ")");
            dragon.setLastHurtByMob(player);
            writeField(dragon, LivingEntity.class, "lastHurtByMobTimestamp", TICKS_ALIVE - 50);
            helper.assertTrue(dragon.getLastHurtByMob() == player && dragon.getLastHurtByMobTimestamp() != dragon.tickCount,
                    "precondition: an older lastHurtByMob record of the player (50 ticks old) (" + FINDING + " test setup)");
            float health = dragon.getHealth();
            dragon.hurt(dragon.damageSources().fall(), 1.0f);
            helper.assertTrue(dragon.getHealth() < health, "precondition: an unrelated hit arms the 10-tick hurt timer (" + FINDING + " test setup)");
            float armed = dragon.getHealth();
            dragon.hurt(dragon.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(dragon.getHealth() == armed, "precondition: the player's hit inside the timer is swallowed (" + FINDING + " test setup)");
            helper.assertTrue(scanPick(dragon, WaterDragon.class) == player,
                    "orig WaterDragon.java:479-493 — a swallowed hit stores nothing: the mark survives it even though an older lastHurtByMob"
                            + " record names the attacker — the hand-off reads THIS hit's timestamp (" + FINDING + "); scanPick "
                            + describe(scanPick(dragon, WaterDragon.class)));
        } finally {
            removePlayer(helper, player);
            discardQuietly(dragon);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // ENT-S-122 — the sight memo
    // ------------------------------------------------------------------

    /** A verdict of "seen" is kept through a wall raised afterwards until the memo is cleared. */
    private static void memoSeenKept(GameTestHelper helper, EntityType<? extends Mob> type, String filter, String cite) {
        Mob hunter = null;
        Mob pig = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, hunter, pig, "a pig 8 blocks east");
            helper.assertTrue(invokeFilter(hunter, filter, pig), "control: the filter accepts the pig on the open floor (" + MEMO + ")");
            setWall(helper, true);
            hunter.getSensing().tick();
            helper.assertTrue(!hunter.hasLineOfSight(pig), "precondition: the wall hides the pig from the vanilla ray (" + MEMO + " test geometry)");
            helper.assertTrue(invokeFilter(hunter, filter, pig), "orig " + cite + " with EntitySenses — the first verdict (seen) is held until the"
                    + " senses are cleared: the filter still accepts the pig behind the wall (" + MEMO + ")");
            clearMemo(hunter);
            helper.assertTrue(!invokeFilter(hunter, filter, pig), "control: with the memo cleared the wall refuses the pig (" + MEMO + ")");
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** A verdict of "unseen" is kept after the wall is razed until the memo is cleared. */
    private static void memoUnseenKept(GameTestHelper helper, EntityType<? extends Mob> type, String filter, String cite) {
        Mob hunter = null;
        Mob pig = null;
        try {
            hunter = spawnFrozen(helper, type, HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            setWall(helper, true);
            hunter.getSensing().tick();
            helper.assertTrue(!invokeFilter(hunter, filter, pig), "control: behind the wall the filter refuses the pig (" + MEMO + ")");
            setWall(helper, false);
            hunter.getSensing().tick();
            helper.assertTrue(hunter.hasLineOfSight(pig), "precondition: the razed wall exposes the pig to the vanilla ray (" + MEMO + " test geometry)");
            helper.assertTrue(!invokeFilter(hunter, filter, pig), "orig " + cite + " with EntitySenses — the first verdict (unseen) is held until"
                    + " the senses are cleared: the filter still refuses the exposed pig (" + MEMO + ")");
            clearMemo(hunter);
            helper.assertTrue(invokeFilter(hunter, filter, pig), "control: with the memo cleared the exposed pig is accepted (" + MEMO + ")");
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** Ant Robot: an unridden step clears the memo (super.updateAITasks), a ridden step keeps it (orig :98-104, :884-886). */
    private static void antRobotMemoBoundary(GameTestHelper helper) {
        Mob ant = null;
        Mob pig = null;
        Mob rider = null;
        try {
            ant = spawnFrozen(helper, ModEntities.ANT_ROBOT.get(), HUNTER_POS);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            rider = spawnPrey(helper, EntityType.PIG, HUNTER_POS);
            replaceRandom(ant, rolls(20, 1, 150, 1, 15, 1));
            assertSees(helper, ant, pig, "a pig 8 blocks east");
            helper.assertTrue(invokeFilter(ant, "isSuitableTarget", pig), "control: seen (" + MEMO + ")");
            setWall(helper, true);
            ant.getSensing().tick();
            helper.assertTrue(rider.startRiding(ant, true) && ant.getFirstPassenger() == rider, "precondition: a passenger aboard — the ridden state ("
                    + MEMO + " test setup)");
            invokeCustomServerAiStep(ant);
            helper.assertTrue(invokeFilter(ant, "isSuitableTarget", pig), "orig AntRobot.java:98-104 — while ridden the step returns before"
                    + " super.updateAITasks: the memo is not cleared, the pig behind the wall still reads as seen (" + MEMO + ")");
            rider.stopRiding();
            helper.assertTrue(ant.getFirstPassenger() == null, "precondition: dismounted (" + MEMO + " test setup)");
            invokeCustomServerAiStep(ant);
            helper.assertTrue(!invokeFilter(ant, "isSuitableTarget", pig), "orig AntRobot.java:98-104 — an unridden step reaches super"
                    + ".updateAITasks and clears the senses: the pig behind the wall is refused (" + MEMO + ")");
        } finally {
            setWall(helper, false);
            discardQuietly(rider);
            discardQuietly(pig);
            discardQuietly(ant);
        }
        helper.succeed();
    }

    /** Nightmare: the activity-0 step clears the memo (orig :330-332), an active step keeps it. */
    private static void pitchBlackMemoBoundary(GameTestHelper helper) {
        Mob pb = null;
        Mob pig = null;
        try {
            pb = spawnFrozen(helper, ModEntities.PITCH_BLACK.get(), HUNTER_POS);
            ((PitchBlack) pb).setSizeTier(1); // a frozen spawn keeps the max tier and a 40-block scan inflation that reaches neighbouring cells; tier 1 keeps the scan inside the cell (refuter B, N2)
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            replaceRandom(pb, rolls(10, 0, 150, 1, 8, 1));
            assertSees(helper, pb, pig, "a pig 8 blocks east");
            helper.assertTrue(invokeFilter(pb, "isSuitableTarget", pig), "control: seen (" + MEMO + ")");
            setWall(helper, true);
            pb.getSensing().tick();
            ((PitchBlack) pb).setActivity(1);
            invokeCustomServerAiStep(pb);
            helper.assertTrue(invokeFilter(pb, "isSuitableTarget", pig), "orig PitchBlack.java:330-332 — an active step never reaches super"
                    + ".updateAITasks: the memo is kept, the pig behind the wall still reads as seen (" + MEMO + ")");
            ((PitchBlack) pb).setActivity(0);
            invokeCustomServerAiStep(pb);
            helper.assertTrue(!invokeFilter(pb, "isSuitableTarget", pig), "orig PitchBlack.java:330-332 — the activity-0 step clears the senses:"
                    + " the pig behind the wall is refused (" + MEMO + ")");
        } finally {
            setWall(helper, false);
            discardQuietly(pig);
            discardQuietly(pb);
        }
        helper.succeed();
    }

    /** Nightmare: orig :259-275 — the 1-in-250 heal of 1 + scale, the 1-in-5 ground probe and scan, an empty scan → activity 0. */
    private static void pitchBlackHealBranch(GameTestHelper helper) {
        Mob pb = null;
        Mob pig = null;
        try {
            pb = spawnFrozen(helper, ModEntities.PITCH_BLACK.get(), HUNTER_POS);
            ((PitchBlack) pb).setSizeTier(1); // a frozen spawn keeps the max tier and a 40-block scan inflation that reaches neighbouring cells; tier 1 keeps the scan inside the cell (refuter B, N2)
            PitchBlack nightmare = (PitchBlack) pb;
            nightmare.setActivity(1);
            pb.setHealth(pb.getMaxHealth() - 10.0f);
            float before = pb.getHealth();
            float scale = nightmare.getPitchBlackScale();
            replaceRandom(pb, rolls(250, 1, 5, 0));
            pb.tick();
            helper.assertTrue(Math.abs(pb.getHealth() - (before + 1.0f + scale)) < 0.01f, "orig PitchBlack.java:259-260 — the 1-in-250 heals 1 + scale ("
                    + MEMO + " / " + FINDING + "); health " + pb.getHealth() + " from " + before + ", scale " + scale);
            helper.assertTrue(nightmare.getActivity() == 0, "orig PitchBlack.java:261-275 — the 1-in-5 probe's y <= 10 leg (orig :266-268; the game-test grid sits below y 10), the scan finds"
                    + " nothing and the Nightmare goes back to activity 0 — its only way back (" + MEMO + " / " + FINDING + "); activity "
                    + nightmare.getActivity());
            nightmare.setActivity(1);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSees(helper, pb, pig, "a pig 8 blocks east");
            clearMemo(pb);
            pb.tick();
            helper.assertTrue(nightmare.getActivity() == 1, "control: with a pig in the box the scan finds it and the Nightmare stays active ("
                    + MEMO + " / " + FINDING + "); activity " + nightmare.getActivity());
            replaceRandom(pb, rolls(250, 0, 5, 0));
            pig.discard();
            pb.tick();
            helper.assertTrue(nightmare.getActivity() == 1, "control: without the 1-in-250 nothing runs (" + MEMO + " / " + FINDING + ")");
        } finally {
            discardQuietly(pig);
            discardQuietly(pb);
        }
        helper.succeed();
    }

    /** Nightmare: orig :277-279 — the four-in-five of the heal branch wakes it (activity 1) and drops its navigation. */
    private static void pitchBlackActivation(GameTestHelper helper) {
        Mob pb = null;
        try {
            pb = spawnFrozen(helper, ModEntities.PITCH_BLACK.get(), HUNTER_POS);
            ((PitchBlack) pb).setSizeTier(1); // a frozen spawn keeps the max tier and a 40-block scan inflation that reaches neighbouring cells; tier 1 keeps the scan inside the cell (refuter B, N2)
            PitchBlack nightmare = (PitchBlack) pb;
            nightmare.setActivity(0);
            pb.setHealth(pb.getMaxHealth() - 10.0f);
            BlockPos away = helper.absolutePos(PREY_POS);
            pb.setOnGround(true); // a frozen mob never lands (LivingEntity.travel is gated on isEffectiveAi) and GroundPathNavigation.canUpdatePath needs the ground (refuter B, B1)
            pb.getNavigation().moveTo(away.getX() + 0.5, away.getY(), away.getZ() + 0.5, 1.0);
            helper.assertTrue(pb.getNavigation().isInProgress(), "precondition: a navigation path in progress (" + MEMO + " / " + FINDING + " test setup)");
            replaceRandom(pb, rolls(250, 1, 5, 1));
            pb.tick();
            helper.assertTrue(nightmare.getActivity() == 1 && !pb.getNavigation().isInProgress(),
                    "orig PitchBlack.java:277-279 — the 1-in-250 with nextInt(5) != 0 sets activity 1 and clears the path (setPath(null, 0)) ("
                            + MEMO + " / " + FINDING + "); activity " + nightmare.getActivity() + ", navigating " + pb.getNavigation().isInProgress());
        } finally {
            discardQuietly(pb);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers (the sibling batches' idioms)
    // ------------------------------------------------------------------

    /** A step of a tick-driven row: on any failure the cleanup runs before the assertion propagates. */
    private static void guarded(Runnable cleanup, Runnable step) {
        boolean ok = false;
        try {
            step.run();
            ok = true;
        } finally {
            if (!ok) cleanup.run();
        }
    }

    /** No goal on the target selector takes a recorded attacker (the 1.7.10 revenge task was inert and is not registered). */
    private static void assertNoGoalTakesAttacker(GameTestHelper helper, Mob hunter, Mob attacker, String cite) {
        hunter.setTarget(null);
        hunter.hurt(hunter.damageSources().mobAttack(attacker), 1.0f);
        helper.assertTrue(hunter.getLastHurtByMob() == attacker, "precondition: the hit is recorded as lastHurtByMob (" + FINDING + " test setup)");
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            helper.assertTrue(!(goal instanceof HurtByTargetGoal), "orig " + cite + " — EntityAIHurtByTarget(this, false) set an attack target"
                    + " nothing read: an inert task; a port revenge goal would chase the attacker, so none is registered (" + FINDING + "); found " + describe(goal));
            helper.assertTrue(!goal.canUse(), "orig " + cite + " — no target goal takes the attacker (" + FINDING + "); " + describe(goal) + " would");
        }
        helper.assertTrue(hunter.getTarget() == null, "orig " + cite + " — nothing stored the attacker (" + FINDING + "); slot " + describe(hunter.getTarget()));
    }

    private static void setWall(GameTestHelper helper, boolean present) {
        Block block = present ? Blocks.STONE : Blocks.AIR;
        for (int y = WALL_Y_MIN; y <= WALL_Y_MAX; y++) {
            for (int z = WALL_Z_MIN; z <= WALL_Z_MAX; z++) {
                helper.setBlock(new BlockPos(WALL_X, y, z), block);
            }
        }
    }

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey, String why) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: " + hunter.getClass().getSimpleName() + " (eye "
                + String.format("%.2f", hunter.getEyeHeight()) + " above its feet) must see " + why + " inside the barrier shell (" + FINDING + " test geometry)");
    }

    /**
     * orig GiantRobot.java:256-263 — the pass's head-bearing error to a target: {@code |atan2(dz, dx) - rad((yHeadRot + 90) % 360)|}
     * folded into [0, π]; the pass engages below 0.5.
     */
    private static double headBearingError(Mob hunter, Entity target) {
        double targetBearing = Math.atan2(target.getZ() - hunter.getZ(), target.getX() - hunter.getX());
        double headBearing = Math.toRadians((hunter.getYHeadRot() + 90.0f) % 360.0f);
        double error = Math.abs(targetBearing - headBearing) % (Math.PI * 2.0);
        if (error > Math.PI) error -= Math.PI * 2.0;
        return Math.abs(error);
    }

    /**
     * A fresh ServerPlayer refuses every hurt that does not bypass invulnerability for its first 60 ticks
     * ({@code ServerPlayer.hurt} reads {@code spawnInvulnerableTime}, initialised to 60 and counted down in
     * {@code ServerPlayer.tick}); a row whose signal is a bite landing on the mock in the tick it was placed writes it to 0
     * by name — the ProjectileTypeParityTests idiom (ENT-S-111). {@link #playerAt} keeps the shield, as the sibling batches'
     * mocks do; only the rows whose signal is that bite clear it.
     */
    private static void clearSpawnInvulnerability(ServerPlayer player) {
        try {
            Field field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            field.setAccessible(true);
            field.setInt(player, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("ServerPlayer.spawnInvulnerableTime is not reachable by reflection (1.21.1: private int;"
                    + " official names at runtime) (" + FINDING + " test setup)", exception);
        }
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

    /** Frozen prey with 1000 HP, so no pinned hit kills it. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server defaults to CREATIVE). The
     * framework's mock answers {@code isCreative()} true whatever its mode; every predicate of this batch reads the
     * abilities, which the game mode does set. Deprecated mock-player factory tolerated as the sibling batches do.
     */
    @SuppressWarnings({"removal", "deprecation"})
    private static ServerPlayer playerAt(GameTestHelper helper, GameType mode, Vec3 absolutePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(mode);
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

    /** The ForcedRoll seam for a float dice: {@code nextFloat} answers a fixed value, everything else delegates. */
    private static final class ForcedFloat implements RandomSource {
        private final RandomSource delegate;
        private final float answer;

        ForcedFloat(RandomSource delegate, float answer) {
            this.delegate = delegate;
            this.answer = answer;
        }

        @Override
        public RandomSource fork() {
            return new ForcedFloat(this.delegate.fork(), this.answer);
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            return this.delegate.forkPositional();
        }

        @Override
        public void setSeed(long seed) {
            this.delegate.setSeed(seed);
        }

        @Override
        public int nextInt() {
            return this.delegate.nextInt();
        }

        @Override
        public int nextInt(int upper) {
            return this.delegate.nextInt(upper);
        }

        @Override
        public long nextLong() {
            return this.delegate.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return this.delegate.nextBoolean();
        }

        @Override
        public float nextFloat() {
            return this.answer;
        }

        @Override
        public double nextDouble() {
            return this.delegate.nextDouble();
        }

        @Override
        public double nextGaussian() {
            return this.delegate.nextGaussian();
        }
    }

    /** The hunter's protected {@code customServerAiStep} — the port's shape of orig updateAITasks. */
    private static void invokeCustomServerAiStep(Mob hunter) {
        invoke(hunter, hunter.getClass(), "customServerAiStep");
    }

    /** The hunter's private one-arg filter — {@code isSuitableTarget(LivingEntity)} or the Ant Robot's {@code feetIsSuitableTarget}. */
    private static boolean invokeFilter(Mob hunter, String name, LivingEntity candidate) {
        return (Boolean) invoke(hunter, hunter.getClass(), name, new Class<?>[] {LivingEntity.class}, candidate);
    }

    /** The hunter's private {@code scanPick} ownership mark. */
    private static LivingEntity scanPick(Mob hunter, Class<?> declaring) {
        return (LivingEntity) readField(hunter, declaring, "scanPick");
    }

    /** The hunter's private {@code revengeGoal} (its RevengeGoal, a HurtByTargetGoal), built in registerGoals and kept in the field. */
    private static Goal revengeGoal(Mob hunter, Class<?> declaring) {
        Goal goal = (Goal) readField(hunter, declaring, "revengeGoal");
        if (goal == null) throw new IllegalStateException("precondition: " + declaring.getSimpleName() + ".revengeGoal must be assigned by registerGoals (" + FINDING + " test setup)");
        return goal;
    }

    /** The ENT-S-122 memo, cleared as its state boundary clears it. */
    private static void clearMemo(Mob hunter) {
        for (String name : new String[] {"sightMemoSeen", "sightMemoUnseen"}) {
            ((it.unimi.dsi.fastutil.ints.IntOpenHashSet) readField(hunter, hunter.getClass(), name)).clear();
        }
    }

    /** The first goal of the class on the selector. */
    private static Goal goalOf(net.minecraft.world.entity.ai.goal.GoalSelector selector, Class<?> goalClass) {
        for (WrappedGoal wrapped : selector.getAvailableGoals()) {
            if (goalClass.isInstance(wrapped.getGoal())) return wrapped.getGoal();
        }
        throw new IllegalStateException("precondition: a " + goalClass.getSimpleName() + " on the selector (" + FINDING + " test setup)");
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
