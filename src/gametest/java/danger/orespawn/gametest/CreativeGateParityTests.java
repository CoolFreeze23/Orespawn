package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnderKnight;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.Mothra;
import danger.orespawn.util.OreSpawnSight;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-132 — targeting ledger batch T8 (wave 3): the 1.7.10 creative gates the survey found missing or mapped to
 * vanilla's {@code abilities.invulnerable}, plus the Brutalfly / Mothra creative fall-through, the Ender pair's
 * same-tick creative drop (the legacy loop's shadowing, {@code td.bq} :155-182) and the Ender Knight's restored
 * pumpkin-stare gate (orig EnderKnight.java:83-93).
 *
 * <p>The ruling under test is ENT-S-107's mapping: orig {@code capabilities.isCreativeMode} (and, for the Ender pair,
 * the legacy loop's game-type {@code isCreative}) is 1.21.1 {@code Abilities.instabuild} — never
 * {@code invulnerable} / {@code canBeSeenAsEnemy}, which also refuses spectators and hand-toggled invulnerability.
 * Per site the three player states of that ruling: a creative player refused (orig's own test), a SURVIVAL player with
 * {@code Abilities.invulnerable} flipped on by hand still taken (the discriminating row — vanilla's {@code forCombat}
 * conditions and {@code canAttack} refuse it; every row of this kind fails with its port line reverted), and a plain
 * survival player taken (the control). Ten sites reach a private filter by reflection (the eight ENT-S-108 filters,
 * present at HEAD and pinned here — CreativeMappingParityTests' shape — and, since ENT-S-135 replaced their T8 goals
 * with orig's scans, the CaterKiller's and SeaViper's {@code isSuitableTarget}); one asks the vanilla
 * {@code NearestAttackableTargetGoal<Player>} rebuilt non-combat with orig's creative selector (Pointysaurus) through
 * {@code canUse()} (PlayNicelyGateParityTests' GoalProbe shape, the acquisition roll pinned — bound 3 since ENT-S-136's interval 6, bound 5 chained); the Ender pair's goal rows add the
 * shadowing (a creative starer nearer than a survival starer leaves nothing picked; alone, the survival starer is
 * taken), the hold ({@code holdsLegacyTarget} through both target goals' {@code canContinueToUse}), the Knight's
 * stare gate (pumpkin refused, look-away refused, a stare through a stone wall refused by the :92 player-side ray with
 * the cone asserted satisfied, a clear stare taken — each refusal with its within-row control), and the revenge pick
 * (the anonymous {@code HurtByTargetGoal}'s {@code canUse}, its {@code canAttack} overridden to the same mapping — T8
 * refuter D1: a creative attacker refused with the slot empty, an invulnerable survival attacker taken and stored,
 * where vanilla's {@code canAttack} refused it). The Brutalfly and
 * Mothra AI steps are driven once by reflection under scripted rolls (the ENT-S-109 strafe shape with a per-call
 * script, since the mob-hunt gate and the fire roll share the bound 3): a creative nearest falls through to the mob
 * hunt (the zombie's mark), a survival nearest is strafed and shadows the hunt (the mob-hunt roll never drawn), and for
 * Mothra an unseen survival nearest is neither strafed nor stepped past (the stage-1 sight step).</p>
 *
 * <p>Synchronous; PlayNicely, the difficulty and MothraPeaceful are asserted as preconditions and never flipped;
 * walls razed, players removed and spawns discarded in a finally. Creative players are the framework's mock
 * (CREATIVE set explicitly — the mock's {@code isCreative()} is hardcoded true, its abilities follow the mode);
 * survival players are plain {@link ServerPlayer}s put on the player list ({@link #survivalServerPlayerAt}, the
 * PlayNicelyGateParityTests helper). No row pins a hit, so no spawn shield is cleared. Own batch (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CreativeGateParityTests {

    private static final String BATCH = "creativeGateParity";
    private static final String TEST_PREFIX = "creativegateparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-132";

    /** Hunter on the template floor (the CreativeMappingParityTests spot). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** The player 8 blocks east on the same floor, clear line of sight. */
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);
    /** A second player 12 blocks east, on the same line — the farther starer of the shadowing rows. */
    private static final Vec3 FAR_PLAYER_POS = new Vec3(32.5, 1.0, 24.5);
    /** Mob prey 12 blocks east (past the Brutalfly's distSq 25 melee ring, inside every mob-hunt box). */
    private static final BlockPos PREY_EAST_POS = new BlockPos(32, 1, 24);
    /** Mob prey 12 blocks west — the side the wall never blocks. */
    private static final BlockPos PREY_WEST_POS = new BlockPos(8, 1, 24);
    /** A stone wall between the hunter and PLAYER_POS: three columns at x = 24, z = 23..25, WALL_HEIGHT tall. */
    private static final BlockPos WALL_BASE = new BlockPos(24, 1, 24);
    private static final int WALL_HEIGHT = 6;
    /** The vanilla goal's acquisition roll: {@code reducedTickDelay(10)} = 5 (PlayNicelyGateParityTests.GOAL_ROLL_BOUND). */
    private static final int GOAL_ROLL_BOUND = 5;
    private static final float PREY_HEALTH = 1000.0f;
    /**
     * The hunter's tickCount before an attacker is recorded (TargetReleaseParityTests' TICKS_ALIVE): vanilla's
     * {@code HurtByTargetGoal.canUse} ignores a record whose timestamp equals the goal's initial 0.
     */
    private static final int TICKS_ALIVE = 100;

    /** The three player states of the ruling, each with its generated-test suffix and the 1.7.10 answer. */
    private enum PlayerCase {
        /** {@code GameType.CREATIVE}: instabuild and invulnerable both set — refused by the orig creative test. */
        CREATIVE("creative_player_refused", false),
        /** SURVIVAL with {@code Abilities.invulnerable} flipped on by hand: instabuild clear — still taken, the discriminating case. */
        SURVIVAL_INVULNERABLE("invulnerable_survival_player_taken", true),
        /** Plain SURVIVAL — taken, the control. */
        SURVIVAL("survival_player_taken", true);

        final String suffix;
        /** Whether 1.7.10 takes the player: false for the creative case only. */
        final boolean prey;

        PlayerCase(String suffix, boolean prey) {
            this.suffix = suffix;
            this.prey = prey;
        }
    }

    // ------------------------------------------------------------------
    // The sites
    // ------------------------------------------------------------------

    /** An ENT-S-108 hunter whose private {@code isSuitableTarget(LivingEntity)} carries orig's player branch. */
    private record FilterSite(String key, Supplier<? extends EntityType<? extends Mob>> hunter, String origCreative, String port) {
    }

    /** A hunter whose proactive pick is a vanilla {@code NearestAttackableTargetGoal<Player>} rebuilt with orig's creative selector. */
    private record GoalSite(String key, Supplier<? extends EntityType<? extends Mob>> hunter, String origCreative, String port) {
    }

    /**
     * The Ender pair: the stare-selected player goal with the legacy loop's same-tick creative drop and hold, and the
     * revenge goal's pick ({@code revenge}: the anonymous HurtByTargetGoal's canAttack override, T8 refuter D1).
     */
    private record EnderSite(String key, Supplier<? extends EntityType<? extends Mob>> hunter, String origFile, String port, String revenge) {
    }

    /**
     * The eight ENT-S-108 filters in the ledger's T8 order — every orig site tests {@code capabilities.isCreativeMode} —
     * plus the Cater Killer's and Sea Viper's, whose T8 vanilla goals gave way to their orig scans under ENT-S-135: the same
     * creative term (orig :548 / :519), now a private {@code isSuitableTarget} at its orig ladder position, so their rows
     * moved from the goal shape to this one (the row names changed with the shape; the count did not).
     */
    private static List<FilterSite> filterSites() {
        List<FilterSite> sites = new ArrayList<>();
        sites.add(new FilterSite("cave_fisher", ModEntities.CAVE_FISHER, "CaveFisher.java:221-226", "CaveFisher.java:191"));
        sites.add(new FilterSite("dungeon_beast", ModEntities.DUNGEON_BEAST, "DungeonBeast.java:240-245", "DungeonBeast.java:183"));
        sites.add(new FilterSite("emperor_scorpion", ModEntities.ENTITY_EMPEROR_SCORPION, "EmperorScorpion.java:494-499", "EntityEmperorScorpion.java:345"));
        sites.add(new FilterSite("hercules_beetle", ModEntities.ENTITY_HERCULES_BEETLE, "HerculesBeetle.java:407-412", "EntityHerculesBeetle.java:267"));
        sites.add(new FilterSite("nastysaurus", ModEntities.NASTYSAURUS, "Nastysaurus.java:271-274", "Nastysaurus.java:262"));
        sites.add(new FilterSite("spit_bug", ModEntities.ENTITY_SPIT_BUG, "SpitBug.java:361-366", "EntitySpitBug.java:301"));
        sites.add(new FilterSite("trex", ModEntities.TREX, "TRex.java:241-246", "TRex.java:245"));
        sites.add(new FilterSite("trooper_bug", ModEntities.ENTITY_TROOPER_BUG, "TrooperBug.java:501-506", "EntityTrooperBug.java:335"));
        sites.add(new FilterSite("cater_killer", ModEntities.ENTITY_CATER_KILLER, "CaterKiller.java:546-549", "EntityCaterKiller.java:407 (ENT-S-135)"));
        sites.add(new FilterSite("sea_viper", ModEntities.SEA_VIPER, "SeaViper.java:517-520", "SeaViper.java:353 (ENT-S-135)"));
        return sites;
    }

    /** The vanilla-goal site whose conditions ENT-S-132 rebuilt non-combat with orig's creative selector (the Cater Killer's and Sea Viper's goals are gone, ENT-S-135). */
    private static List<GoalSite> goalSites() {
        List<GoalSite> sites = new ArrayList<>();
        sites.add(new GoalSite("pointysaurus", ModEntities.POINTYSAURUS, "Pointysaurus.java:242-245", "Pointysaurus.java:107"));
        return sites;
    }

    private static List<EnderSite> enderSites() {
        List<EnderSite> sites = new ArrayList<>();
        sites.add(new EnderSite("ender_knight", ModEntities.ENDER_KNIGHT, "EnderKnight.java", "EnderKnight.java:91 (pick) / :118 (hold)", "EnderKnight.java:58-62"));
        sites.add(new EnderSite("ender_reaper", ModEntities.ENDER_REAPER, "EnderReaper.java", "EnderReaper.java:88 (pick) / :115 (hold)", "EnderReaper.java:55-59"));
        return sites;
    }

    private enum StareCase { PUMPKIN, LOOK_AWAY, WALL, CLEAR }

    private enum MothraCase { CREATIVE_FALLS_THROUGH, SURVIVAL_SEEN_STRAFED, SURVIVAL_UNSEEN_SHADOWS_HUNT }

    // ------------------------------------------------------------------
    // The generator
    // ------------------------------------------------------------------

    /**
     * One TestFunction per row, named {@code creativegateparitytests.s132_NN_<site>_<case>} in the order below:
     * 30 filter rows (10 x 3 — the Cater Killer's and Sea Viper's since ENT-S-135), 3 goal rows (the Pointysaurus), the
     * Ender pair's 3 + 2 + 2 each (14), the Knight's 4 stare rows, the Brutalfly's 2 and Mothra's 3, then the Ender
     * pair's 2 revenge-pick rows each (4, T8 refuter D1) — 60 rows in the {@code creativeGateParity} batch.
     */
    @GameTestGenerator
    public Collection<TestFunction> s132CreativeGateRows() {
        List<TestFunction> functions = new ArrayList<>();
        int[] counter = {0};
        for (FilterSite site : filterSites()) {
            for (PlayerCase playerCase : PlayerCase.values()) {
                add(functions, counter, site.key() + "_filter_" + playerCase.suffix, helper -> assertFilterSite(helper, site, playerCase));
            }
        }
        for (GoalSite site : goalSites()) {
            for (PlayerCase playerCase : PlayerCase.values()) {
                add(functions, counter, site.key() + "_goal_" + playerCase.suffix, helper -> assertGoalSite(helper, site, playerCase));
            }
        }
        for (EnderSite site : enderSites()) {
            for (PlayerCase playerCase : PlayerCase.values()) {
                add(functions, counter, site.key() + "_stare_goal_" + playerCase.suffix, helper -> assertEnderGoal(helper, site, playerCase));
            }
            add(functions, counter, site.key() + "_creative_starer_shadows_survival_starer", helper -> assertEnderShadow(helper, site, true));
            add(functions, counter, site.key() + "_survival_starer_taken_alone", helper -> assertEnderShadow(helper, site, false));
            add(functions, counter, site.key() + "_hold_creative_target_dropped", helper -> assertEnderHold(helper, site, PlayerCase.CREATIVE));
            add(functions, counter, site.key() + "_hold_invulnerable_survival_target_kept", helper -> assertEnderHold(helper, site, PlayerCase.SURVIVAL_INVULNERABLE));
        }
        add(functions, counter, "ender_knight_pumpkin_helmet_refused", helper -> assertKnightStare(helper, StareCase.PUMPKIN));
        add(functions, counter, "ender_knight_look_away_refused", helper -> assertKnightStare(helper, StareCase.LOOK_AWAY));
        add(functions, counter, "ender_knight_stare_through_wall_refused", helper -> assertKnightStare(helper, StareCase.WALL));
        add(functions, counter, "ender_knight_clear_stare_taken", helper -> assertKnightStare(helper, StareCase.CLEAR));
        add(functions, counter, "brutalfly_creative_nearest_falls_through_to_mob_hunt", helper -> assertBrutalflyStrafe(helper, true));
        add(functions, counter, "brutalfly_survival_nearest_strafed_mob_hunt_shadowed", helper -> assertBrutalflyStrafe(helper, false));
        add(functions, counter, "mothra_creative_nearest_falls_through_to_mob_hunt", helper -> assertMothraStage1(helper, MothraCase.CREATIVE_FALLS_THROUGH));
        add(functions, counter, "mothra_survival_nearest_seen_strafed", helper -> assertMothraStage1(helper, MothraCase.SURVIVAL_SEEN_STRAFED));
        add(functions, counter, "mothra_survival_nearest_unseen_shadows_mob_hunt", helper -> assertMothraStage1(helper, MothraCase.SURVIVAL_UNSEEN_SHADOWS_HUNT));
        for (EnderSite site : enderSites()) {
            add(functions, counter, site.key() + "_revenge_creative_attacker_refused", helper -> assertEnderRevenge(helper, site, PlayerCase.CREATIVE));
            add(functions, counter, site.key() + "_revenge_invulnerable_survival_attacker_taken", helper -> assertEnderRevenge(helper, site, PlayerCase.SURVIVAL_INVULNERABLE));
        }
        return functions;
    }

    private static void add(List<TestFunction> functions, int[] counter, String tag, Consumer<GameTestHelper> body) {
        String name = TEST_PREFIX + "s132_" + String.format("%02d", ++counter[0]) + "_" + tag;
        functions.add(new TestFunction(BATCH, name, EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true, body));
    }

    // ------------------------------------------------------------------
    // A — the ENT-S-108 filters (present at HEAD, pinned)
    // ------------------------------------------------------------------

    private static void assertFilterSite(GameTestHelper helper, FilterSite site, PlayerCase playerCase) {
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, site.hunter().get(), HUNTER_POS);
            player = playerFor(helper, playerCase, PLAYER_POS);
            assertSees(helper, hunter, player, "the player 8 blocks east");
            assertPlayerFlags(helper, player, playerCase);
            boolean actual = isSuitableTarget(hunter, player);
            helper.assertTrue(actual == playerCase.prey, hunter.getClass().getSimpleName() + ".isSuitableTarget(" + playerCase
                    + " player): " + creativeWhy(site.origCreative(), site.port(), playerCase) + " — expected " + playerCase.prey
                    + ", got " + actual + " (" + FINDING + ")");
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    private static String creativeWhy(String origCreative, String port, PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "a creative player must be refused (orig " + origCreative
                    + " capabilities.isCreativeMode -> Abilities.instabuild; port " + port + ")";
            case SURVIVAL_INVULNERABLE -> "a SURVIVAL player with Abilities.invulnerable set is not creative (orig " + origCreative
                    + " reads isCreativeMode only) and must be taken; vanilla's invulnerable / canBeSeenAsEnemy mapping refuses it"
                    + " (port " + port + ")";
            case SURVIVAL -> "control: a plain survival player must be taken (orig " + origCreative + ", the branch's other arm)";
        };
    }

    // ------------------------------------------------------------------
    // B — the vanilla goals rebuilt with orig's creative selector
    // ------------------------------------------------------------------

    private static void assertGoalSite(GameTestHelper helper, GoalSite site, PlayerCase playerCase) {
        assertPlayNicelyOff(helper);
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnWithGoals(helper, site.hunter().get(), HUNTER_POS);
            replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0, 3, 0)); // bound 3: the Pointysaurus goal's interval 6 (reducedTickDelay 3, ENT-S-136); bound 5 (the 3-arg constructor's) chained
            NearestAttackableTargetGoal<?> goal = playerGoal(helper, hunter);
            player = playerFor(helper, playerCase, PLAYER_POS);
            assertSees(helper, hunter, player, "the player 8 blocks east");
            assertPlayerFlags(helper, player, playerCase);
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can == playerCase.prey, hunter.getClass().getSimpleName() + "'s NearestAttackableTargetGoal<Player>.canUse("
                    + playerCase + " player): " + creativeWhy(site.origCreative(), site.port(), playerCase) + " — expected "
                    + playerCase.prey + ", got " + can + " (pick " + describe(pick) + ") (" + FINDING + ")");
            if (playerCase.prey) {
                helper.assertTrue(pick == player, hunter.getClass().getSimpleName() + ": the goal's pick must be the " + playerCase
                        + " player, got " + describe(pick) + " (" + FINDING + ")");
            }
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // C / D / E — the Ender pair: the stare goal's pick, the shadowing, the hold
    // ------------------------------------------------------------------

    /**
     * The player stares at the hunter's mid-height (orig :88-91), so only the creative term decides. For the creative
     * case the pick's own conditions are shown to ADMIT the player (the drop, not the pick, refuses it — the
     * shadowing mechanism of td.bq :155-182) and the goal's target is read back null: picked and nulled the same tick.
     */
    private static void assertEnderGoal(GameTestHelper helper, EnderSite site, PlayerCase playerCase) {
        assertPlayNicelyOff(helper);
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnWithGoals(helper, site.hunter().get(), HUNTER_POS);
            replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0));
            NearestAttackableTargetGoal<?> goal = playerGoal(helper, hunter);
            player = playerFor(helper, playerCase, PLAYER_POS);
            stareAtMid(player, hunter);
            assertSees(helper, hunter, player, "the starer 8 blocks east");
            assertPlayerFlags(helper, player, playerCase);
            String name = hunter.getClass().getSimpleName();
            if (playerCase == PlayerCase.CREATIVE) {
                helper.assertTrue(conditionsOf(goal).test(hunter, player), name + ": the pick's conditions must ADMIT a creative starer"
                        + " — orig's pick (" + site.origFile() + ":65-67, td.bq) had no creative term; the drop comes after it (port "
                        + site.port() + ") (" + FINDING + ")");
            }
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can == playerCase.prey, name + "'s stare goal canUse(" + playerCase + " starer): "
                    + enderWhy(site, playerCase) + " — expected " + playerCase.prey + ", got " + can + " (pick " + describe(pick)
                    + ") (" + FINDING + ")");
            if (playerCase.prey) {
                helper.assertTrue(pick == player, name + ": the goal's pick must be the " + playerCase + " starer, got "
                        + describe(pick) + " (" + FINDING + ")");
            } else {
                helper.assertTrue(pick == null, name + ": a creative starer is nulled the same tick it was picked (td.bq :155-182)"
                        + " — the goal's target must read null, got " + describe(pick) + " (" + FINDING + ")");
            }
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    private static String enderWhy(EnderSite site, PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "the legacy loop nulled a creative EntityPlayerMP target the same tick (td.bq :155-182, isCreative ->"
                    + " Abilities.instabuild; port " + site.port() + ")";
            case SURVIVAL_INVULNERABLE -> "a SURVIVAL starer with Abilities.invulnerable set is not creative (td.bq :155-182 read the"
                    + " game type only) and must be taken; vanilla's forCombat canAttack / canBeSeenAsEnemy refused it (port " + site.port() + ")";
            case SURVIVAL -> "control: a plain survival starer is taken (orig " + site.origFile() + ":67-75)";
        };
    }

    /**
     * td.bq :155-182 with :65 — the nearest player of ANY mode is the pick, and a creative one is nulled the same tick:
     * a creative starer 8 blocks off shadows a survival starer 12 blocks off (nothing picked); with no creative starer
     * the survival one is the pick (the control).
     */
    private static void assertEnderShadow(GameTestHelper helper, EnderSite site, boolean creativeNearer) {
        assertPlayNicelyOff(helper);
        Mob hunter = null;
        ServerPlayer survival = null;
        ServerPlayer creative = null;
        try {
            hunter = spawnWithGoals(helper, site.hunter().get(), HUNTER_POS);
            replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0));
            NearestAttackableTargetGoal<?> goal = playerGoal(helper, hunter);
            String name = hunter.getClass().getSimpleName();
            survival = survivalServerPlayerAt(helper, helper.absoluteVec(FAR_PLAYER_POS));
            stareAtMid(survival, hunter);
            assertPlayerFlags(helper, survival, PlayerCase.SURVIVAL);
            assertSees(helper, hunter, survival, "the survival starer 12 blocks east");
            helper.assertTrue(conditionsOf(goal).test(hunter, survival), "precondition: the pick's conditions admit the survival"
                    + " starer 12 blocks east (" + FINDING + " test setup)");
            if (creativeNearer) {
                creative = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS));
                stareAtMid(creative, hunter);
                assertPlayerFlags(helper, creative, PlayerCase.CREATIVE);
                assertSees(helper, hunter, creative, "the creative starer 8 blocks east");
                helper.assertTrue(creative.distanceToSqr(hunter) < survival.distanceToSqr(hunter),
                        "precondition: the creative starer is the nearer of the two (" + FINDING + " test setup)");
                helper.assertTrue(conditionsOf(goal).test(hunter, creative), "precondition: the pick's conditions admit the creative"
                        + " starer too — no creative term in the pick, as td.bq (" + FINDING + " test setup)");
            }
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            if (creativeNearer) {
                helper.assertTrue(!can && pick == null, name + ": the nearest starer is creative, so td.bq :155-182 nulls the pick the"
                        + " same tick and the survival starer 12 blocks off stays shadowed (the Kraken KT-A pattern; port " + site.port()
                        + ") — expected no pick, got canUse=" + can + " pick " + describe(pick) + " (" + FINDING + ")");
            } else {
                helper.assertTrue(can && pick == survival, name + ": control — alone, the survival starer 12 blocks off is the pick"
                        + " (orig " + site.origFile() + ":65-75) — got canUse=" + can + " pick " + describe(pick) + " (" + FINDING + ")");
            }
        } finally {
            removePlayer(helper, creative);
            removePlayer(helper, survival);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /**
     * The hold (holdsLegacyTarget, ENT-S-129) through both target goals' {@code canContinueToUse}: a creative held
     * target is dropped (td.bq :155-182), an invulnerable SURVIVAL one is kept — HEAD's {@code canAttack} dropped it.
     */
    private static void assertEnderHold(GameTestHelper helper, EnderSite site, PlayerCase playerCase) {
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnWithGoals(helper, site.hunter().get(), HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            player = playerFor(helper, playerCase, PLAYER_POS);
            assertPlayerFlags(helper, player, playerCase);
            hunter.setTarget(player);
            helper.assertTrue(hunter.getTarget() == player && player.isAlive(), "precondition: the " + playerCase
                    + " player is the stored target (" + FINDING + " test setup)");
            List<Goal> goals = targetGoals(hunter);
            helper.assertTrue(goals.size() == 2, "precondition: " + name + " carries exactly two target goals (the revenge goal and the"
                    + " stare goal, both on holdsLegacyTarget) — found " + goals.size() + " (" + FINDING + " test setup)");
            for (Goal goal : goals) {
                boolean holds = goal.canContinueToUse();
                helper.assertTrue(holds == playerCase.prey, name + ": " + describeGoal(goal) + ".canContinueToUse with a " + playerCase
                        + " target — " + holdWhy(site, playerCase) + " — expected " + playerCase.prey + ", got " + holds + " (" + FINDING + ")");
            }
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    private static String holdWhy(EnderSite site, PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "td.bq :155-182 nulled a creative EntityPlayerMP target (isCreative -> Abilities.instabuild; port " + site.port() + ")";
            case SURVIVAL_INVULNERABLE -> "td.bq read the game type only, so an invulnerable SURVIVAL target is held; HEAD's canAttack"
                    + " (canBeSeenAsEnemy = !abilities.invulnerable) dropped it (port " + site.port() + ")";
            case SURVIVAL -> "a survival target is held (td.bq :107-152)";
        };
    }

    // ------------------------------------------------------------------
    // E2 — the Ender pair's revenge pick: the anonymous HurtByTargetGoal's canAttack (T8 refuter D1)
    // ------------------------------------------------------------------

    /**
     * orig {@code EntityMob.attackEntityFrom} stored ANY living attacker as {@code entityToAttack} and td.bq :155-182
     * nulled only a creative one (the game type; ENT-S-107: {@code Abilities.instabuild}). The port's counterpart is the
     * revenge goal's pick, vanilla's {@code HurtByTargetGoal.canUse} → {@code TargetGoal.canAttack(LivingEntity,
     * TargetingConditions)} with the private {@code HURT_BY_TARGETING} ({@code forCombat}: {@code LivingEntity.canAttack}
     * → {@code Player.canBeSeenAsEnemy} = {@code !abilities.invulnerable}), which the port overrides to the mapping
     * (T8 refuter D1). The attacker is primed through {@code setLastHurtByMob} with the hunter's {@code tickCount}
     * raised first (TargetReleaseParityTests' idiom: vanilla's {@code canUse} ignores a record whose timestamp equals
     * the goal's initial 0) and the goal is driven as the selector drives it — {@code canUse}, then {@code start} if
     * taken: a creative attacker is refused and the slot stays empty; an invulnerable SURVIVAL attacker is taken and
     * stored — the row that fails with the override removed.
     */
    private static void assertEnderRevenge(GameTestHelper helper, EnderSite site, PlayerCase playerCase) {
        helper.assertTrue(!helper.getLevel().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER),
                "precondition: universalAnger is off — under it vanilla's HurtByTargetGoal.canUse refuses every player attacker ahead"
                        + " of canAttack (" + FINDING + " test setup)");
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnWithGoals(helper, site.hunter().get(), HUNTER_POS);
            hunter.tickCount = TICKS_ALIVE;
            String name = hunter.getClass().getSimpleName();
            Goal revenge = revengeGoal(helper, hunter);
            player = playerFor(helper, playerCase, PLAYER_POS);
            if (playerCase == PlayerCase.SURVIVAL_INVULNERABLE) {
                player.onUpdateAbilities(); // the hand-toggled flag announced as a command's toggle would be; the read under test is the flag
            }
            assertPlayerFlags(helper, player, playerCase);
            helper.assertTrue(hunter.getTarget() == null, "precondition: the slot is empty ahead of the pick (" + FINDING + " test setup)");
            hunter.setLastHurtByMob(player);
            helper.assertTrue(hunter.getLastHurtByMob() == player && hunter.getLastHurtByMobTimestamp() == TICKS_ALIVE,
                    "precondition: the " + playerCase + " player is the recorded attacker at timestamp " + TICKS_ALIVE
                            + ", not the goal's initial 0 (" + FINDING + " test setup)");
            boolean can = revenge.canUse();
            if (can) {
                revenge.start(); // the selector's sequence: a taken pick is stored by start (mob.setTarget(lastHurtByMob))
            }
            LivingEntity slot = hunter.getTarget();
            helper.assertTrue(can == playerCase.prey, name + "'s revenge goal canUse(" + playerCase + " attacker): " + revengeWhy(site, playerCase)
                    + " — expected " + playerCase.prey + ", got " + can + " (slot " + describe(slot) + ") (" + FINDING + ")");
            if (playerCase.prey) {
                helper.assertTrue(slot == player, name + ": started, the revenge goal stores the " + playerCase + " attacker in the slot"
                        + " (HurtByTargetGoal.start), got " + describe(slot) + " (" + FINDING + ")");
            } else {
                helper.assertTrue(slot == null, name + ": refused at the pick, the slot stays empty, got " + describe(slot) + " (" + FINDING + ")");
            }
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    private static String revengeWhy(EnderSite site, PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "a creative attacker is refused at the pick — orig EntityMob.attackEntityFrom stored it and td.bq :155-182"
                    + " nulled it the same tick (isCreative -> Abilities.instabuild; port " + site.revenge() + ")";
            case SURVIVAL_INVULNERABLE -> "a SURVIVAL attacker with Abilities.invulnerable set is not creative (EntityMob.attackEntityFrom"
                    + " stored any attacker; td.bq :155-182 read the game type only) and must be taken — with the port's canAttack override ("
                    + site.revenge() + ") removed this row fails: vanilla HurtByTargetGoal.canUse -> TargetGoal.canAttack(HURT_BY_TARGETING,"
                    + " forCombat) -> LivingEntity.canAttack -> Player.canBeSeenAsEnemy (= !abilities.invulnerable) refuses it (T8 refuter D1)";
            case SURVIVAL -> "control: a plain survival attacker is taken (EntityMob.attackEntityFrom; port " + site.revenge() + ")";
        };
    }

    // ------------------------------------------------------------------
    // F — the Ender Knight's restored stare gate (orig EnderKnight.java:83-93)
    // ------------------------------------------------------------------

    private static void assertKnightStare(GameTestHelper helper, StareCase stareCase) {
        assertPlayNicelyOff(helper);
        Mob knight = null;
        ServerPlayer player = null;
        boolean wallUp = false;
        try {
            knight = spawnWithGoals(helper, ModEntities.ENDER_KNIGHT.get(), HUNTER_POS);
            helper.assertTrue(knight instanceof EnderKnight, "precondition: the hunter is an EnderKnight (" + FINDING + " test setup)");
            replaceRandom(knight, rolls(GOAL_ROLL_BOUND, 0));
            NearestAttackableTargetGoal<?> goal = playerGoal(helper, knight);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS));
            assertPlayerFlags(helper, player, PlayerCase.SURVIVAL);
            stareAtMid(player, knight);
            switch (stareCase) {
                case PUMPKIN -> player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
                case LOOK_AWAY -> player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(player.getX() + 8.0, player.getEyeY(), player.getZ()));
                case WALL -> {
                    setWall(helper, Blocks.STONE);
                    wallUp = true;
                }
                case CLEAR -> {
                }
            }
            boolean expected = stareCase == StareCase.CLEAR;
            switch (stareCase) {
                case PUMPKIN -> {
                    helper.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN),
                            "precondition: the carved pumpkin sits in the head slot (orig :84 armor slot 3) (" + FINDING + " test setup)");
                    helper.assertTrue(stareConeSatisfied(knight, player), "precondition: the pumpkin-wearer still stares inside the cone"
                            + " (orig :88-91), so only :84-87 refuses (" + FINDING + " test setup)");
                }
                case LOOK_AWAY -> helper.assertTrue(!stareConeSatisfied(knight, player), "precondition: looking east, away from the"
                        + " knight, the look vector is outside the cone (orig :88-91) (" + FINDING + " test setup)");
                case WALL -> {
                    helper.assertTrue(stareConeSatisfied(knight, player), "precondition: the stare through the wall is inside the"
                            + " cone (orig :88-91), so only the :92 ray refuses (" + FINDING + " test setup)");
                    helper.assertTrue(!OreSpawnSight.canSee(player, knight), "precondition: the stone wall at x = 24 blocks the"
                            + " player's eye line to the knight (the :92 ray, OreSpawnSight.canSee(player, knight)) (" + FINDING + " test setup)");
                }
                case CLEAR -> {
                    helper.assertTrue(stareConeSatisfied(knight, player), "precondition: the stare is inside the cone (" + FINDING + " test setup)");
                    helper.assertTrue(OreSpawnSight.canSee(player, knight), "precondition: the player's eye line to the knight is clear"
                            + " (" + FINDING + " test setup)");
                }
            }
            boolean stare = shouldAttackPlayer(knight, player);
            helper.assertTrue(stare == expected, "EnderKnight.shouldAttackPlayer (" + stareCase + "): " + stareWhy(stareCase)
                    + " — expected " + expected + ", got " + stare + " (" + FINDING + ")");
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can == expected, "EnderKnight's stare goal canUse (" + stareCase + "): " + stareWhy(stareCase)
                    + " — expected " + expected + ", got " + can + " (pick " + describe(pick) + ") (" + FINDING + ")");
            if (expected) {
                helper.assertTrue(pick == player, "EnderKnight: the clear starer must be the goal's pick, got " + describe(pick)
                        + " (" + FINDING + ")");
            }
            // the within-row control: the one refusing term undone, the same stare is taken
            switch (stareCase) {
                case PUMPKIN -> {
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    helper.assertTrue(shouldAttackPlayer(knight, player), "control: bare-headed, the same stare is taken — orig :84-87"
                            + " is the pumpkin alone (" + FINDING + ")");
                }
                case LOOK_AWAY -> {
                    stareAtMid(player, knight);
                    helper.assertTrue(shouldAttackPlayer(knight, player), "control: looking back at the knight's mid-height the same"
                            + " player is taken (orig :88-91) (" + FINDING + ")");
                }
                case WALL -> {
                    setWall(helper, Blocks.AIR);
                    wallUp = false;
                    helper.assertTrue(OreSpawnSight.canSee(player, knight), "control precondition: razed, the player's eye line is clear"
                            + " (" + FINDING + " test setup)");
                    helper.assertTrue(shouldAttackPlayer(knight, player), "control: razed, the same stare is taken — the :92 ray was the"
                            + " one refusing term (" + FINDING + ")");
                }
                case CLEAR -> {
                }
            }
        } finally {
            if (wallUp) setWall(helper, Blocks.AIR);
            removePlayer(helper, player);
            discardQuietly(knight);
        }
        helper.succeed();
    }

    private static String stareWhy(StareCase stareCase) {
        return switch (stareCase) {
            case PUMPKIN -> "a carved pumpkin on the head hides the player (orig EnderKnight.java:84-87)";
            case LOOK_AWAY -> "a look vector outside the 1 - 0.025/d cone about the knight's mid-height is refused (orig :88-91)";
            case WALL -> "the player's own eye line to the knight is blocked, so the :92 ray (player.canEntityBeSeen(knight),"
                    + " OreSpawnSight.canSee) refuses";
            case CLEAR -> "a bare-headed survival player staring at the knight's mid-height with a clear eye line is taken (orig :83-93)";
        };
    }

    // ------------------------------------------------------------------
    // G — the Brutalfly's strafe: a creative nearest falls through to the mob hunt (orig Brutalfly.java:216-241)
    // ------------------------------------------------------------------

    /**
     * orig Brutalfly.java:213-241 driven once (port EntityBrutalfly.customServerAiStep) under scripted rolls: the
     * reselection roll nextInt(200) answers 1 and the flight target is parked 10 above (distSq 100, past the
     * "&lt; 9" reselection), the strafe roll nextInt(6) answers 0. Creative nearest: orig :224-226 nulls it, the
     * mob-hunt gate nextInt(3) (orig :228) answers 0, the zombie 12 blocks east is the hunt's pick and its fireball roll
     * nextInt(shoot) answers 1 — the flight target reads the zombie's mark (:232, above(5)) and every scripted roll was
     * drawn. Survival nearest: the strafe's fire roll answers 1 and the flight target reads the player's mark (:219,
     * above(4)); the mob-hunt gate's scripted roll must stay undrawn (orig :228 — a non-null target skips the hunt).
     */
    private static void assertBrutalflyStrafe(GameTestHelper helper, boolean creative) {
        assertPlayNicelyOff(helper);
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL, "precondition: not Peaceful (" + FINDING + " test setup)");
        Mob brutalfly = null;
        ServerPlayer player = null;
        Mob zombie = null;
        try {
            brutalfly = spawnFrozen(helper, ModEntities.ENTITY_BRUTALFLY.get(), HUNTER_POS);
            // port EntityBrutalfly.java:199 — the fire roll's bound follows the difficulty (orig :155, 168-170).
            int shoot = helper.getLevel().getDifficulty() == Difficulty.HARD ? 2 : 3;
            BlockPos parked = brutalfly.blockPosition().above(10);
            writeObject(brutalfly, "currentFlightTarget", parked);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_EAST_POS);
            player = creative ? playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS))
                    : survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS));
            assertPlayerFlags(helper, player, creative ? PlayerCase.CREATIVE : PlayerCase.SURVIVAL);
            assertSees(helper, brutalfly, player, "the player 8 blocks east");
            assertSees(helper, brutalfly, zombie, "the zombie 12 blocks east");
            helper.assertTrue(helper.getLevel().getNearestPlayer(brutalfly, 30.0) == player,
                    "precondition: the player is the nearest within 30 (port EntityBrutalfly.java:206) (" + FINDING + " test setup)");
            helper.assertTrue(brutalfly.distanceToSqr(zombie) > 25.0,
                    "precondition: the zombie sits past the distSq 25 melee ring, on the fireball branch whose roll is pinned quiet"
                            + " (orig :233-236) (" + FINDING + " test setup)");
            ScriptedRolls script = creative
                    ? scripted(200, 1, 6, 0, 3, 0, shoot, 1)
                    : scripted(200, 1, 6, 0, shoot, 1, 3, 0);
            replaceRandom(brutalfly, script);
            invokeAiStep(EntityBrutalfly.class, brutalfly);
            BlockPos after = (BlockPos) readObject(brutalfly, "currentFlightTarget");
            BlockPos playerMark = new BlockPos((int) player.getX(), (int) player.getY() + 4, (int) player.getZ()); // orig :219's (int) casts (BUG-027) — the Mothra row's idiom; blockPosition() floors a cell short on a negative axis
            BlockPos zombieMark = new BlockPos((int) zombie.getX(), (int) zombie.getY() + 5, (int) zombie.getZ()); // orig :232
            if (creative) {
                helper.assertTrue(zombieMark.equals(after), "EntityBrutalfly strafe (creative nearest): orig Brutalfly.java:224-226 nulls a"
                        + " creative nearest and the 1-in-3 mob hunt (:228-232) takes the zombie — expected the zombie's mark " + zombieMark
                        + ", got " + after + " (parked " + parked + ", player mark " + playerMark + ") (" + FINDING + ")");
                helper.assertTrue(script.remaining() == 0, "EntityBrutalfly strafe (creative nearest): the mob-hunt gate (:228) and the"
                        + " hunt's fire roll (:234) must both have been drawn — undrawn: " + script.describeRemaining() + " (" + FINDING + ")");
            } else {
                helper.assertTrue(playerMark.equals(after), "EntityBrutalfly strafe (survival nearest): orig :217-219 marks the seen"
                        + " survival nearest — expected the player's mark " + playerMark + ", got " + after + " (" + FINDING + ")");
                helper.assertTrue(script.remaining() == 1 && script.describeRemaining().equals("[3->0]"),
                        "EntityBrutalfly strafe (survival nearest): a non-null target skips the mob hunt (orig :228), so its gate roll"
                                + " nextInt(3) must never be drawn — undrawn: " + script.describeRemaining() + " (" + FINDING + ")");
            }
        } finally {
            removePlayer(helper, player);
            discardQuietly(zombie);
            discardQuietly(brutalfly);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // H — Mothra's stage 1: the sight step and the creative fall-through (orig Mothra.java:222-246)
    // ------------------------------------------------------------------

    /**
     * orig Mothra.java:222-246 driven once (port Mothra.customServerAiStep) under scripted rolls: the reselection
     * roll nextInt(300) answers 1 with the flight target parked 10 above, the stage-1 roll nextInt(10) answers 0.
     * Creative nearest (:226 / :233-235): the mob-hunt gate nextInt(3) (:237) answers 0 and the zombie 12 blocks east
     * is the hunt's pick, its fire roll nextInt(shoot) answers 1 — the flight target reads the zombie's mark (:241,
     * y + 5). Seen survival nearest (:227-229): strafed — the player's mark (:228, y + 4) — and the mob-hunt gate's
     * scripted roll stays undrawn (a non-null target skips :237). Unseen survival nearest (a stone wall between): not
     * strafed (:227) and, `target` still set, the hunt never reached — the parked target stands and the zombie 12 blocks
     * WEST, in clear sight, is never marked.
     */
    private static void assertMothraStage1(GameTestHelper helper, MothraCase mothraCase) {
        assertPlayNicelyOff(helper);
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL, "precondition: not Peaceful (orig Mothra.java:222) (" + FINDING + " test setup)");
        helper.assertTrue(!OreSpawnConfig.MOTHRA_PEACEFUL.get(), "precondition: MothraPeaceful is off (orig Mothra.java:222) (" + FINDING + " test setup)");
        Mob mothra = null;
        ServerPlayer player = null;
        Mob zombie = null;
        boolean wallUp = false;
        try {
            mothra = spawnFrozen(helper, ModEntities.MOTHRA.get(), HUNTER_POS);
            helper.assertTrue(mothra instanceof Mothra, "precondition: the hunter is a Mothra (" + FINDING + " test setup)");
            // port Mothra.java:367 — the fire roll's bound follows the difficulty (orig :165, 178-180).
            int shoot = helper.getLevel().getDifficulty() == Difficulty.HARD ? 2 : 3;
            BlockPos parked = mothra.blockPosition().above(10);
            writeObject(mothra, "currentFlightTarget", parked);
            boolean creative = mothraCase == MothraCase.CREATIVE_FALLS_THROUGH;
            zombie = spawnPrey(helper, EntityType.ZOMBIE, creative ? PREY_EAST_POS : PREY_WEST_POS);
            player = creative ? playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS))
                    : survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS));
            assertPlayerFlags(helper, player, creative ? PlayerCase.CREATIVE : PlayerCase.SURVIVAL);
            if (mothraCase == MothraCase.SURVIVAL_UNSEEN_SHADOWS_HUNT) {
                setWall(helper, Blocks.STONE);
                wallUp = true;
                helper.assertTrue(!mothra.hasLineOfSight(player), "precondition: the stone wall at x = 24 hides the player 8 blocks east from"
                        + " Mothra (orig :227 canSee) (" + FINDING + " test setup)");
            } else {
                assertSees(helper, mothra, player, "the player 8 blocks east");
            }
            assertSees(helper, mothra, zombie, "the zombie 12 blocks " + (creative ? "east" : "west"));
            helper.assertTrue(helper.getLevel().getNearestPlayer(mothra, 25.0) == player,
                    "precondition: the player is the nearest within 25 (port Mothra.java:392) (" + FINDING + " test setup)");
            ScriptedRolls script = switch (mothraCase) {
                case CREATIVE_FALLS_THROUGH -> scripted(300, 1, 10, 0, 3, 0, shoot, 1);
                case SURVIVAL_SEEN_STRAFED -> scripted(300, 1, 10, 0, shoot, 1, 3, 0);
                case SURVIVAL_UNSEEN_SHADOWS_HUNT -> scripted(300, 1, 10, 0, 3, 0);
            };
            replaceRandom(mothra, script);
            invokeAiStep(Mothra.class, mothra);
            BlockPos after = (BlockPos) readObject(mothra, "currentFlightTarget");
            BlockPos playerMark = new BlockPos((int) player.getX(), (int) player.getY() + 4, (int) player.getZ());
            BlockPos zombieMark = new BlockPos((int) zombie.getX(), (int) zombie.getY() + 5, (int) zombie.getZ());
            switch (mothraCase) {
                case CREATIVE_FALLS_THROUGH -> {
                    helper.assertTrue(zombieMark.equals(after), "Mothra stage 1 (creative nearest): orig Mothra.java:233-235 nulls a creative"
                            + " nearest and the 1-in-3 mob hunt (:237-241) takes the zombie — expected the zombie's mark " + zombieMark
                            + ", got " + after + " (parked " + parked + ", player mark " + playerMark + ") (" + FINDING + ")");
                    helper.assertTrue(script.remaining() == 0, "Mothra stage 1 (creative nearest): the mob-hunt gate (:237) and the hunt's"
                            + " fire roll (:242) must both have been drawn — undrawn: " + script.describeRemaining() + " (" + FINDING + ")");
                }
                case SURVIVAL_SEEN_STRAFED -> {
                    helper.assertTrue(playerMark.equals(after), "Mothra stage 1 (seen survival nearest): orig :226-228 marks the seen"
                            + " survival nearest — expected the player's mark " + playerMark + ", got " + after + " (" + FINDING + ")");
                    helper.assertTrue(script.remaining() == 1 && script.describeRemaining().equals("[3->0]"),
                            "Mothra stage 1 (seen survival nearest): a non-null target skips the mob hunt (orig :237), so its gate roll"
                                    + " nextInt(3) must never be drawn — undrawn: " + script.describeRemaining() + " (" + FINDING + ")");
                }
                case SURVIVAL_UNSEEN_SHADOWS_HUNT -> {
                    helper.assertTrue(parked.equals(after), "Mothra stage 1 (unseen survival nearest): orig :227 refuses the strafe without"
                            + " sight and :237 never runs while `target` is set — expected the parked flight target " + parked
                            + " untouched, got " + after + " (player mark " + playerMark + ", zombie mark " + zombieMark + ") (" + FINDING + ")");
                    helper.assertTrue(script.remaining() == 1 && script.describeRemaining().equals("[3->0]"),
                            "Mothra stage 1 (unseen survival nearest): the mob-hunt gate roll nextInt(3) (:237) must never be drawn — undrawn: "
                                    + script.describeRemaining() + " (" + FINDING + ")");
                }
            }
        } finally {
            if (wallUp) setWall(helper, Blocks.AIR);
            removePlayer(helper, player);
            discardQuietly(zombie);
            discardQuietly(mothra);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers — the CreativeMappingParityTests / PlayNicelyGateParityTests idioms
    // ------------------------------------------------------------------

    private static void assertPlayNicelyOff(GameTestHelper helper) {
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(), "precondition: PlayNicely is off — the ENT-S-115 gate composes ahead of every"
                + " site under test and is not flipped here (" + FINDING + " test setup)");
    }

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey, String what) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: the " + hunter.getClass().getSimpleName() + " (eye "
                + String.format("%.2f", hunter.getEyeHeight()) + " above its feet) must see " + what + " inside the barrier shell ("
                + FINDING + " test geometry)");
    }

    /** The two abilities flags of each player case, as {@link #playerFor} must have left them. */
    private static void assertPlayerFlags(GameTestHelper helper, ServerPlayer player, PlayerCase playerCase) {
        switch (playerCase) {
            case CREATIVE -> helper.assertTrue(player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: a creative player has both instabuild and invulnerable set (" + FINDING + " test setup)");
            case SURVIVAL_INVULNERABLE -> helper.assertTrue(!player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: the discriminating player is survival (instabuild clear) with invulnerable set by hand (" + FINDING + " test setup)");
            case SURVIVAL -> helper.assertTrue(!player.getAbilities().instabuild && !player.getAbilities().invulnerable,
                    "precondition: a plain survival player has neither flag (" + FINDING + " test setup)");
        }
    }

    /** orig EnderKnight.java:88-91 — the look vector against the vector to the knight's mid-height, {@code d1 > 1 - 0.025/d0}. */
    private static boolean stareConeSatisfied(Mob hunter, Player player) {
        Vec3 look = player.getViewVector(1.0f).normalize();
        Vec3 toHunter = new Vec3(hunter.getX() - player.getX(), hunter.getY() + hunter.getBbHeight() / 2.0f - player.getEyeY(),
                hunter.getZ() - player.getZ());
        double dist = toHunter.length();
        return look.dot(toHunter.normalize()) > 1.0 - 0.025 / dist;
    }

    /** The player looks at the hunter's mid-height (the Ender pair's :88-91 test; PlayNicelyGateParityTests' PLAYER_STARING_AT_MID). */
    private static void stareAtMid(ServerPlayer player, Mob hunter) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(hunter.getX(), hunter.getY() + hunter.getBbHeight() / 2.0f, hunter.getZ()));
    }

    /** Three WALL_HEIGHT-tall columns at x = 24, z = 23..25 — between HUNTER_POS and PLAYER_POS; {@code Blocks.AIR} razes them. */
    private static void setWall(GameTestHelper helper, net.minecraft.world.level.block.Block block) {
        for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy < WALL_HEIGHT; dy++) {
                helper.setBlock(WALL_BASE.offset(0, dy, dz), block);
            }
        }
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (the target selector is under test) but no AI, so nothing runs by itself. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** Frozen prey with 1000 HP. */
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
     * The player of a case: creative is the framework's mock set to CREATIVE (its abilities follow the mode); the two
     * survival cases are plain ServerPlayers, the discriminating one with {@code Abilities.invulnerable} flipped by hand
     * and {@code instabuild} untouched.
     */
    private static ServerPlayer playerFor(GameTestHelper helper, PlayerCase playerCase, Vec3 relativePos) {
        Vec3 pos = helper.absoluteVec(relativePos);
        if (playerCase == PlayerCase.CREATIVE) {
            return playerAt(helper, GameType.CREATIVE, pos);
        }
        ServerPlayer player = survivalServerPlayerAt(helper, pos);
        if (playerCase == PlayerCase.SURVIVAL_INVULNERABLE) {
            player.getAbilities().invulnerable = true; // invulnerable by other means than creative: the flag alone
        }
        return player;
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server defaults to CREATIVE,
     * GameTestServer.java:85). The framework's mock answers {@code isCreative()} true whatever its mode; every port
     * line under test reads the abilities, which the game mode does set. Deprecated mock-player factory tolerated as
     * the sibling batches do.
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

    /**
     * A plain {@link ServerPlayer} put on the player list the way the framework's mock is, without the framework's
     * override (PlayNicelyGateParityTests.survivalServerPlayerAt): {@code isCreative()} follows its SURVIVAL mode.
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

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained. */
    private static RandomSource rolls(int... boundAnswerPairs) {
        RandomSource source = RandomSource.create(1234L);
        for (int i = 0; i < boundAnswerPairs.length; i += 2) {
            source = new VortexParityTests.ForcedRoll(source, boundAnswerPairs[i], boundAnswerPairs[i + 1]);
        }
        return source;
    }

    private static ScriptedRolls scripted(int... boundAnswerPairs) {
        return new ScriptedRolls(RandomSource.create(1234L), boundAnswerPairs);
    }

    /**
     * A per-call script over the ForcedRoll seam: each scripted (bound, answer) is consumed by the first
     * {@code nextInt(bound)} that asks for it, in script order among equal bounds, so two draws with the same bound
     * (the Brutalfly's / Mothra's mob-hunt gate and fire roll, both nextInt(3) on Normal) can answer differently;
     * unscripted bounds fall through to the seeded delegate. {@link #remaining()} reports the undrawn entries — an
     * undrawn mob-hunt gate is the proof that the hunt was never reached.
     */
    static final class ScriptedRolls implements RandomSource {
        private final RandomSource delegate;
        private final List<int[]> script = new ArrayList<>();

        ScriptedRolls(RandomSource delegate, int... boundAnswerPairs) {
            this.delegate = delegate;
            for (int i = 0; i < boundAnswerPairs.length; i += 2) {
                this.script.add(new int[] {boundAnswerPairs[i], boundAnswerPairs[i + 1]});
            }
        }

        int remaining() {
            return this.script.size();
        }

        String describeRemaining() {
            StringBuilder out = new StringBuilder("[");
            for (int[] entry : this.script) {
                if (out.length() > 1) out.append(", ");
                out.append(entry[0]).append("->").append(entry[1]);
            }
            return out.append("]").toString();
        }

        @Override
        public RandomSource fork() {
            return this.delegate.fork();
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
            for (Iterator<int[]> it = this.script.iterator(); it.hasNext(); ) {
                int[] entry = it.next();
                if (entry[0] == upper) {
                    it.remove();
                    return entry[1];
                }
            }
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
            return this.delegate.nextFloat();
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

    /** The one {@code NearestAttackableTargetGoal} whose target type is {@code Player} — the site under test. */
    private static NearestAttackableTargetGoal<?> playerGoal(GameTestHelper helper, Mob hunter) {
        NearestAttackableTargetGoal<?> found = null;
        int count = 0;
        for (Goal goal : targetGoals(hunter)) {
            if (goal instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == Player.class) {
                found = nearest;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + hunter.getClass().getSimpleName()
                + " carries exactly one NearestAttackableTargetGoal<Player> on its target selector — found " + count + " (" + FINDING + " test setup)");
        return found;
    }

    /** The one {@code HurtByTargetGoal} on the target selector — the anonymous revenge goal, its {@code canAttack} under test. */
    private static Goal revengeGoal(GameTestHelper helper, Mob hunter) {
        Goal found = null;
        int count = 0;
        for (Goal goal : targetGoals(hunter)) {
            if (goal instanceof HurtByTargetGoal) {
                found = goal;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + hunter.getClass().getSimpleName()
                + " carries exactly one HurtByTargetGoal on its target selector — found " + count + " (" + FINDING + " test setup)");
        return found;
    }

    private static LivingEntity goalTarget(NearestAttackableTargetGoal<?> goal) {
        return (LivingEntity) readField(goal, NearestAttackableTargetGoal.class, "target");
    }

    private static TargetingConditions conditionsOf(NearestAttackableTargetGoal<?> goal) {
        return (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
    }

    private static String describeGoal(Goal goal) {
        if (goal instanceof HurtByTargetGoal) return "HurtByTargetGoal";
        if (goal instanceof NearestAttackableTargetGoal<?>) return "NearestAttackableTargetGoal<Player>";
        return goal.getClass().getSimpleName();
    }

    private static String describe(LivingEntity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name + " (1.21.1: official names at runtime)", exception);
        }
    }

    /** Each ENT-S-108 hunter's private {@code isSuitableTarget(LivingEntity)} — the port's one-arg shape of the orig two-arg method. */
    private static boolean isSuitableTarget(Mob hunter, LivingEntity candidate) {
        String name = hunter.getClass().getSimpleName();
        try {
            Method method = hunter.getClass().getDeclaredMethod("isSuitableTarget", LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + ".isSuitableTarget threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name + ".isSuitableTarget", exception);
        }
    }

    /** {@code EnderKnight.shouldAttackPlayer(Player)} — package-private, orig EnderKnight.java:83-93. */
    private static boolean shouldAttackPlayer(Mob knight, Player player) {
        try {
            Method method = EnderKnight.class.getDeclaredMethod("shouldAttackPlayer", Player.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(knight, player);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("EnderKnight.shouldAttackPlayer threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke EnderKnight.shouldAttackPlayer", exception);
        }
    }

    /** The hunter's protected customServerAiStep, declared on the given class, invoked once. */
    private static void invokeAiStep(Class<? extends Mob> declaring, Mob hunter) {
        try {
            Method method = declaring.getDeclaredMethod("customServerAiStep");
            method.setAccessible(true);
            method.invoke(hunter);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(declaring.getSimpleName() + ".customServerAiStep threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + declaring.getSimpleName() + ".customServerAiStep", exception);
        }
    }

    private static Object readObject(Mob mob, String name) {
        return readField(mob, mob.getClass(), name);
    }

    private static void writeObject(Mob mob, String name, Object value) {
        String owner = mob.getClass().getSimpleName();
        try {
            Field field = mob.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(mob, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + owner + "." + name, exception);
        }
    }
}
