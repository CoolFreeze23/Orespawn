package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;
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
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-136 — targeting ledger batch T3c (wave 3): the four vanilla-goal hunters whose scan set the port had widened to the
 * FOLLOW_RANGE attribute. 1.7.10's {@code EntityAINearestAttackableTarget.shouldExecute} scanned
 * {@code boundingBox.expand(d, 4, d)} with {@code d = EntityAITarget.getTargetDistance()} — the follow-range attribute,
 * EntityLiving's base 16, which Leon.java:112-118, ThePrinceAdult.java:132-138 and ThePrinceTeen.java:136-142 leave alone — and
 * {@code EntityAITarget.continueExecuting} released beyond the same d; the Pointysaurus's own scan (Pointysaurus.java:253) took
 * the box {@code expand(12, 5, 12)}, sorted it (GenericTargetSorter, :254), took the first suitable (:258-262) and acted on it in
 * the same pass (:201-213), storing nothing. The port's four goals read FOLLOW_RANGE (40 / 24 / 64 / 32), an attribute that
 * also sizes the navigator's path search at every one of these sites, so the fix is the Dragon's ENT-S-117 idiom: a
 * {@code getFollowDistance()} override on the goal instance (16 / 12 / 16 / 16) — vanilla's {@code getTargetSearchArea(d)}
 * inflates (d, 4, d), orig's shape, and the conditions' range and the hold read the same d — with the attribute untouched; at
 * the Pointysaurus the goal's {@code findTarget} scans orig's box itself (its conditions carry no range term) and the 12 is the
 * hold's alone. The three IMob goals pass interval 0 — orig's targetChance 0, ENT-S-117's mapping (vanilla's every-other-tick
 * goal pass against EntityAITasks' every third is that record's residual); the Pointysaurus's goal passes interval 6 —
 * reducedTickDelay(6) = 3, nextInt(3) == 0 on the every-other-tick pass = orig :183's nextInt(6) == 0 per tick, exact (the Q2
 * follow-up after T3b). The class / sight / selector arguments stay as
 * ENT-S-124, ENT-S-127 and T8 left them; the box-vs-sphere ring at the three IMob goals is PN-020's, deliberately not
 * reproduced.
 *
 * <p>A {@link GameTestGenerator} over {@link #rows()} — 18 synchronous {@link TestFunction}s,
 * {@code vanillagoalrangeparitytests.s136_NN_<species>_<row>}: three per site in orig file order (rows 1-12), a cadence row at
 * each IMob site (13-15), two box rows at the Pointysaurus (16-17) and its cadence row (18). The goal is read off the hunter's
 * target selector by its target type (the IMobConventionTests idiom) and asked {@code canUse()} — under a forced
 * {@code Entity.random} (the VortexParityTests.ForcedRoll seam, the Pointysaurus goal's acquisition roll pinned to fire — bound 3,
 * interval 6's reducedTickDelay, with the 3-arg constructor's bound 5 chained; the IMob goals draw nothing, and their cadence
 * rows install a draw counter instead to witness it) — with a candidate of the goal's class — a Zombie
 * for the three IMob goals, a plain survival {@link ServerPlayer} for the Pointysaurus's Player goal (the framework mock's
 * {@code isCreative()} is hardcoded true; PlayNicelyGateParityTests' {@code survivalServerPlayerAt}) — on the same floor:
 * {@code edge_inside} at orig's d minus 0.05 along +x (taken, the pick read back); {@code edge_outside} at d plus the hunter's
 * half-width plus the candidate's half-width plus 0.05 along +x (18.10 / 13.80 / 19.475 / 17.975: orig's
 * {@code selectEntitiesWithinAABB} was a box intersection, so the candidate's box is asserted to clear the goal's search area —
 * {@code getTargetSearchArea(d)} by reflection at the Mob goals, {@code getBoundingBox().inflate(12, 5, 12)} at the
 * Pointysaurus — and the spot is outside orig's box and vanilla's d sphere alike, yet inside the FOLLOW_RANGE-driven range HEAD
 * read, asserted: refused — the discriminating row; d + 0.05 itself is PN-020's ring, left unpinned);
 * {@code follow_range_kept} asserts the attribute unchanged while the goal's own {@code getFollowDistance()}, its conditions'
 * range snapshot (none at the Pointysaurus) and (Mob goals) its search box read orig's; {@code cadence_no_roll} asserts
 * {@code randomInterval} 0 and the inside candidate taken with no roll drawn; {@code box_corner_taken} puts the player at
 * (11.9, 0, 11.9) — inside orig's box, 16.83 off, beyond any 12 sphere — and {@code vertical_band_refused} 7.95 straight up —
 * its feet clearing the box's top (the hunter's 2.9 + 5), 7.95 off, inside any 12 sphere; {@code cadence_1_in_6} asserts the
 * Pointysaurus goal's {@code randomInterval} 3 (interval 6's reducedTickDelay — orig :183's 1-in-6 per tick) and drives the inside
 * player twice, the bound-3 roll forced to 1 (refused, nothing picked) then to 0 (taken). Geometry as IMobConventionTests: the
 * hunter spawned with its goals and no AI at rel (20,1,24) on the floor of the 48x16x48 empty_large, the candidate frozen
 * ({@code setOnGround}), line of sight and the feet-to-feet distance asserted. The PlayNicely flag set false and restored in a
 * finally on every path; players removed and every spawn discarded there. Own batch (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class VanillaGoalRangeParityTests {

    private static final String BATCH = "vanillaGoalRangeParity";
    private static final String TEST_PREFIX = "vanillagoalrangeparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (PlayNicelyGateParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-136";

    /** The hunter on the template floor (the IMobConventionTests spot): its feet at rel (20.5, 1.0, 24.5). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final Vec3 HUNTER_FEET = Vec3.atBottomCenterOf(HUNTER_POS);
    /**
     * The 0.05 the rows keep to orig's edge: the inside candidate at d - 0.05 along +x (feet to feet on the same floor, so the
     * distance is the offset); the outside candidate's box clearing the scan box by it (at d + both half-widths + 0.05); the
     * overhead candidate's feet clearing the box's top by it.
     */
    private static final double EDGE_EPSILON = 0.05;
    /** The 3-arg constructor's acquisition roll: {@code NearestAttackableTargetGoal} reduces its 10-tick interval to {@code nextInt(5) != 0 → skip} (IMobConventionTests.GOAL_ROLL_BOUND) — chained into every forcing; the three IMob goals pass interval 0 and draw nothing (the cadence rows witness it), the Pointysaurus goal draws {@link #POINTYSAURUS_ROLL_BOUND}. */
    private static final int GOAL_ROLL_BOUND = 5;
    /** The Pointysaurus goal's acquisition roll: interval 6 → {@code reducedTickDelay(6)} = 3, {@code nextInt(3) != 0 → skip} on the every-other-tick goal pass = orig Pointysaurus.java:183's {@code nextInt(6) == 0} per tick (ENT-S-136, the Q2 follow-up). */
    private static final int POINTYSAURUS_ROLL_BOUND = 3;
    /** Candidate health, high enough that nothing incidental kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** Tolerance on the geometry preconditions (a Vec3 spawn lands where it is told; the sum of two decimals is not exact). */
    private static final double GEOMETRY_TOLERANCE = 1.0e-6;
    /** Orig Pointysaurus.java:253 {@code expand(12, 5, 12)} — the box the Pointysaurus goal's findTarget scans (its conditions carry no range term). */
    private static final double POINTYSAURUS_BOX_XZ = 12.0;
    private static final double POINTYSAURUS_BOX_Y = 5.0;
    /** The box-corner row's offset along +x and +z: inside orig's 12x5x12 box, 16.83 off — beyond any 12 sphere. */
    private static final double CORNER_OFFSET = 11.9;

    // ------------------------------------------------------------------
    // The site table, in orig file order
    // ------------------------------------------------------------------

    /** What the site's goal takes: a Zombie for the three IMob goals, a plain survival ServerPlayer for the Pointysaurus's Player goal. */
    private enum Candidate { ZOMBIE, SURVIVAL_PLAYER }

    /** One orig scan box and the port goal that carries it: orig's d, the FOLLOW_RANGE attribute that must stay, both cites. */
    private record Site(String species, Supplier<? extends EntityType<? extends Mob>> type, Class<?> goalTargetType, Candidate candidate,
            double origRange, double attribute, String orig, String port) {
    }

    private static List<Site> sites() {
        return List.of(
                new Site("leon", ModEntities.ENTITY_LEON, Mob.class, Candidate.ZOMBIE, 16.0, 40.0,
                        "Leon.java:92-93 — EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector):"
                                + " boundingBox.expand(16, 4, 16), d = EntityAITarget.getTargetDistance() = the follow-range attribute, EntityLiving's"
                                + " base 16 (Leon.java:112-118 sets none); no distance test at acquisition, the hold beyond d",
                        "EntityLeon.registerGoals — the target-priority-4 NearestAttackableTargetGoal<Mob>'s getFollowDistance() → 16"
                                + " (FOLLOW_RANGE 40 kept, it sizes the navigator)"),
                new Site("pointysaurus", ModEntities.POINTYSAURUS, Player.class, Candidate.SURVIVAL_PLAYER, 12.0, 24.0,
                        "Pointysaurus.java:253 — findSomethingToAttack's boundingBox.expand(12, 5, 12) on the 1-in-6 pass (:183), no distance"
                                + " test beyond the box",
                        "Pointysaurus.registerGoals — the target-priority-3 NearestAttackableTargetGoal<Player>: findTarget scans orig's"
                                + " inflate(12, 5, 12) (its conditions carry no range term); getFollowDistance() → 12 for the hold alone"
                                + " (FOLLOW_RANGE 24 kept, it sizes the navigator); interval 6 (1-in-6 per tick)"),
                new Site("theprinceadult", ModEntities.THE_PRINCE_ADULT, Mob.class, Candidate.ZOMBIE, 16.0, 64.0,
                        "ThePrinceAdult.java:112-114 — the same task: expand(16, 4, 16), the follow-range attribute's base 16"
                                + " (ThePrinceAdult.java:132-138 sets none)",
                        "ThePrinceAdult.registerGoals — the target-priority-4 NearestAttackableTargetGoal<Mob>'s getFollowDistance() → 16"
                                + " (FOLLOW_RANGE 64 kept)"),
                new Site("theprinceteen", ModEntities.THE_PRINCE_TEEN, Mob.class, Candidate.ZOMBIE, 16.0, 32.0,
                        "ThePrinceTeen.java:116-118 — the same task: expand(16, 4, 16), the follow-range attribute's base 16"
                                + " (ThePrinceTeen.java:136-142 sets none)",
                        "ThePrinceTeen.registerGoals — the target-priority-4 NearestAttackableTargetGoal<Mob>'s getFollowDistance() → 16"
                                + " (FOLLOW_RANGE 32 kept)"));
    }

    /** What a row drives. */
    private enum Kind {
        /** The candidate 0.05 inside orig's edge along +x: taken, the goal's pick read back. */
        EDGE_INSIDE("edge_inside"),
        /**
         * The candidate along +x at d + the hunter's half-width + its own half-width + 0.05, so its box clears orig's box (orig's
         * {@code selectEntitiesWithinAABB} was a box intersection — at d + 0.05 alone a 0.6-wide candidate still met it) and the
         * spot is outside vanilla's d sphere alike: refused — the discriminating row, inside the FOLLOW_RANGE-driven range HEAD read.
         */
        EDGE_OUTSIDE("edge_outside"),
        /** The FOLLOW_RANGE attribute unchanged (it sizes the navigator) while the goal's own distance, its conditions' range (none at the Pointysaurus) and (Mob goals) its search box read orig's. */
        FOLLOW_RANGE_KEPT("follow_range_kept"),
        /** The three IMob goals: {@code randomInterval} 0 (orig's targetChance 0 — ENT-S-117's mapping) and the inside candidate taken with no roll forced, no {@code nextInt(5)} drawn. */
        CADENCE_NO_ROLL("cadence_no_roll"),
        /** The Pointysaurus: a survival player at (11.9, 0, 11.9) — inside orig's 12x5x12 box, 16.83 off, beyond any 12 sphere — taken (orig :253-262 took the box's corners). */
        BOX_CORNER_TAKEN("box_corner_taken"),
        /** The Pointysaurus: a survival player straight up, its feet 0.05 above the box's top (the hunter's 2.9 + the box's 5) — 7.95 off, inside any 12 sphere — refused (orig's box stopped at +5). */
        VERTICAL_BAND_REFUSED("vertical_band_refused"),
        /** The Pointysaurus: {@code randomInterval} 3 (interval 6's reducedTickDelay — orig :183's {@code nextInt(6) == 0} every tick on vanilla's every-other-tick pass, 1-in-6 per tick) and the inside player driven twice: refused with the bound-3 roll forced to 1 (nothing picked), taken with it forced to 0. */
        CADENCE_1_IN_6("cadence_1_in_6");

        final String tag;

        Kind(String tag) {
            this.tag = tag;
        }
    }

    private record Row(int index, Site site, Kind kind) {
        String testName() {
            return TEST_PREFIX + String.format("s136_%02d_%s_%s", this.index, this.site.species(), this.kind.tag);
        }

        String where() {
            return this.site.port() + " (orig " + this.site.orig() + ")";
        }
    }

    /** Rows 1-12: three per site in orig file order; 13-15: a cadence row at each IMob site; 16-17: the Pointysaurus's box rows; 18: its cadence row. */
    private static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        List<Site> sites = sites();
        int n = 0;
        for (Site site : sites) {
            for (Kind kind : List.of(Kind.EDGE_INSIDE, Kind.EDGE_OUTSIDE, Kind.FOLLOW_RANGE_KEPT)) {
                rows.add(new Row(++n, site, kind));
            }
        }
        for (Site site : sites) {
            if (site.goalTargetType() == Mob.class) {
                rows.add(new Row(++n, site, Kind.CADENCE_NO_ROLL));
            }
        }
        for (Site site : sites) {
            if (site.goalTargetType() == Player.class) {
                rows.add(new Row(++n, site, Kind.BOX_CORNER_TAKEN));
                rows.add(new Row(++n, site, Kind.VERTICAL_BAND_REFUSED));
                rows.add(new Row(++n, site, Kind.CADENCE_1_IN_6));
            }
        }
        return rows;
    }

    /** One test per row: 18 TestFunctions in the {@code vanillaGoalRangeParity} batch (three per site, a cadence row at each IMob site, two box rows and a cadence row at the Pointysaurus). */
    @GameTestGenerator
    public Collection<TestFunction> vanillaGoalRangeRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, row)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: the goal read off the selector by type, asked canUse() for the row's candidate at the edge
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Row row) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — the vanilla conditions refuse players on"
                        + " Peaceful (" + FINDING + " test setup)");
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob hunter = null;
        Mob prey = null;
        ServerPlayer player = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false (" + FINDING + " test setup)");
            Site site = row.site();
            hunter = spawnWithGoals(helper, site.type().get(), HUNTER_POS);
            hunter.setOnGround(true);
            DrawCounter draws = null;
            if (row.kind() == Kind.CADENCE_NO_ROLL) {
                draws = new DrawCounter(RandomSource.create(1234L)); // nothing forced: the row witnesses that no roll is drawn
                replaceRandom(hunter, draws);
            } else {
                replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0, POINTYSAURUS_ROLL_BOUND, 0)); // both acquisition bounds answer 0 — the 3-arg constructor's 5 and the Pointysaurus goal's 3 (ForcedRoll delegates the rest)
            }
            NearestAttackableTargetGoal<?> goal = siteGoal(helper, hunter, site);
            double attribute = hunter.getAttributeValue(Attributes.FOLLOW_RANGE);
            helper.assertTrue(attribute == site.attribute(), row.where() + ": the FOLLOW_RANGE attribute must stay " + site.attribute()
                    + " — it sizes the navigator's path search, so the range is the goal's own, never the attribute's (" + FINDING
                    + "); read " + attribute);
            if (row.kind() == Kind.FOLLOW_RANGE_KEPT) {
                assertGoalRange(helper, row, hunter, goal);
            } else {
                float candidateWidth = site.candidate() == Candidate.ZOMBIE ? EntityType.ZOMBIE.getWidth() : EntityType.PLAYER.getWidth();
                Vec3 offset = candidateOffset(row, hunter, candidateWidth);
                Vec3 at = HUNTER_FEET.add(offset);
                LivingEntity candidate;
                if (site.candidate() == Candidate.ZOMBIE) {
                    prey = spawnPrey(helper, EntityType.ZOMBIE, at);
                    candidate = prey;
                } else {
                    player = survivalServerPlayerAt(helper, helper.absoluteVec(at));
                    helper.assertTrue(!player.getAbilities().instabuild && !player.isCreative(), "precondition: the candidate is a plain"
                            + " survival player — the Pointysaurus goal's selector refuses instabuild (" + FINDING + " test setup)");
                    candidate = player;
                }
                candidate.setOnGround(true);
                helper.assertTrue(candidate.getBbWidth() == candidateWidth, "precondition: the candidate is as wide as its type ("
                        + candidateWidth + ") — the outside offset adds half of it (" + FINDING + " test geometry); got " + candidate.getBbWidth());
                double distance = Math.sqrt(hunter.distanceToSqr(candidate.getX(), candidate.getY(), candidate.getZ()));
                double expected = offset.length();
                helper.assertTrue(Math.abs(distance - expected) < GEOMETRY_TOLERANCE, "precondition: the candidate stands at " + offset
                        + " from the hunter's feet, " + expected + " off — the distance TargetingConditions.range tests, feet to feet ("
                        + FINDING + " test geometry); measured " + distance);
                assertSees(helper, hunter, candidate);
                AABB scanBox = scanBoxOf(hunter, goal, site);
                boolean meetsBox = candidate.getBoundingBox().intersects(scanBox);
                boolean taken = switch (row.kind()) {
                    case EDGE_INSIDE, CADENCE_NO_ROLL, BOX_CORNER_TAKEN, CADENCE_1_IN_6 -> true;
                    case EDGE_OUTSIDE, VERTICAL_BAND_REFUSED -> false;
                    case FOLLOW_RANGE_KEPT -> throw new IllegalStateException("handled above");
                };
                helper.assertTrue(meetsBox == taken, "precondition: the candidate's box " + candidate.getBoundingBox() + " must "
                        + (taken ? "meet" : "clear") + " the goal's scan box " + scanBox + " — orig's selectEntitiesWithinAABB was a box"
                        + " intersection (" + FINDING + " test geometry)");
                switch (row.kind()) {
                    case EDGE_OUTSIDE -> {
                        helper.assertTrue(distance > site.origRange(), "precondition: the outside candidate stands beyond vanilla's "
                                + site.origRange() + " sphere as well as orig's box (" + FINDING + " test geometry); " + distance);
                        helper.assertTrue(distance < attribute, "precondition: the outside candidate stands inside the FOLLOW_RANGE-driven"
                                + " range HEAD read (" + attribute + "), so this row discriminates (" + FINDING + " test geometry)");
                    }
                    case BOX_CORNER_TAKEN -> helper.assertTrue(distance > site.origRange(), "precondition: the corner candidate stands"
                            + " beyond a " + site.origRange() + " sphere — a range term would refuse it (" + FINDING + " test geometry); " + distance);
                    case VERTICAL_BAND_REFUSED -> helper.assertTrue(distance < site.origRange(), "precondition: the overhead candidate"
                            + " stands inside a " + site.origRange() + " sphere — a range term alone would take it (" + FINDING
                            + " test geometry); " + distance);
                    case CADENCE_NO_ROLL -> {
                        int interval = (Integer) readField(goal, NearestAttackableTargetGoal.class, "randomInterval");
                        helper.assertTrue(interval == 0, row.where() + ": orig's task passed targetChance 0 — no roll on EntityAITasks'"
                                + " every-third-tick pass; the port's randomInterval must be 0 (reducedTickDelay(0), ENT-S-117's mapping of"
                                + " the same argument at the Dragon), not the 3-arg constructor's 10 → 5 (" + FINDING + "); got " + interval);
                    }
                    case CADENCE_1_IN_6 -> {
                        int interval = (Integer) readField(goal, NearestAttackableTargetGoal.class, "randomInterval");
                        helper.assertTrue(interval == POINTYSAURUS_ROLL_BOUND, row.where() + ": orig rolled nextInt(6) == 0 every tick (:183); the"
                                + " port's goal passes interval 6, so randomInterval must be reducedTickDelay(6) = " + POINTYSAURUS_ROLL_BOUND
                                + " — nextInt(3) on the every-other-tick pass, 1-in-6 per tick — not the 3-arg constructor's 10 → 5 ("
                                + FINDING + "); got " + interval);
                        replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0, POINTYSAURUS_ROLL_BOUND, 1)); // the bound-3 roll answers 1: this pass is skipped
                        boolean skipped = goal.canUse();
                        LivingEntity skippedPick = goalTarget(goal);
                        helper.assertTrue(!skipped && skippedPick == null, row.where() + ": with the bound-3 roll answering 1 the goal must skip"
                                + " the pass and pick nothing — orig :183's nextInt(6) != 0 scanned nothing that tick (" + FINDING + "); canUse="
                                + skipped + ", pick " + describe(skippedPick));
                        replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0, POINTYSAURUS_ROLL_BOUND, 0)); // the roll answers 0: the pass runs and the player is taken below
                    }
                    default -> { }
                }
                boolean can = goal.canUse();
                LivingEntity pick = goalTarget(goal);
                if (taken) {
                    helper.assertTrue(can && pick == candidate, row.where() + ": " + describeCandidate(site) + " at " + offset + " — "
                            + why(row) + " — must be taken (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
                } else {
                    helper.assertTrue(!can, row.where() + ": " + describeCandidate(site) + " at " + offset + " — " + why(row)
                            + " — must be refused (" + FINDING + "); canUse=" + can + ", pick " + describe(pick));
                }
                if (draws != null) {
                    helper.assertTrue(draws.drawsOf(GOAL_ROLL_BOUND) == 0, row.where() + ": an interval-0 goal draws no acquisition roll —"
                            + " orig's targetChance 0 rolled nothing (" + FINDING + "); " + draws);
                }
            }
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            removePlayer(helper, player);
            discardQuietly(prey);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    /** The goal's own distance, its conditions' range snapshot (none at the Pointysaurus) and — for the Mob goals — its search box read orig's d, not the attribute. */
    private static void assertGoalRange(GameTestHelper helper, Row row, Mob hunter, NearestAttackableTargetGoal<?> goal) {
        Site site = row.site();
        double followDistance = followDistanceOf(goal);
        helper.assertTrue(followDistance == site.origRange(), row.where() + ": the goal's getFollowDistance() must read orig's "
                + site.origRange() + " (" + FINDING + "); read " + followDistance);
        double conditionsRange = conditionsRangeOf(goal);
        if (site.goalTargetType() == Mob.class) {
            helper.assertTrue(conditionsRange == site.origRange(), row.where() + ": the goal's TargetingConditions range — the sphere the"
                    + " pick tests — must read orig's " + site.origRange() + " (" + FINDING + "); read " + conditionsRange);
            AABB box = searchAreaOf(goal, followDistance);
            AABB expected = hunter.getBoundingBox().inflate(site.origRange(), 4.0, site.origRange());
            helper.assertTrue(box.equals(expected), row.where() + ": the goal's search box must be the hunter's box inflated ("
                    + site.origRange() + ", 4, " + site.origRange() + ") — orig's boundingBox.expand(d, 4, d) (" + FINDING + "); got " + box);
        } else {
            helper.assertTrue(conditionsRange <= 0.0, row.where() + ": the goal's TargetingConditions must carry NO range term (the field at"
                    + " vanilla's -1 default) — orig Pointysaurus.java:253's box, scanned in findTarget, is the only bound; a sphere refuses"
                    + " the box's corners and takes its vertical (" + FINDING + "); read " + conditionsRange);
        }
    }

    /** The site's goal off the target selector: exactly one NearestAttackableTargetGoal of the site's target type. */
    private static NearestAttackableTargetGoal<?> siteGoal(GameTestHelper helper, Mob hunter, Site site) {
        NearestAttackableTargetGoal<?> found = null;
        int count = 0;
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == site.goalTargetType()) {
                found = nearest;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + site.port() + " — exactly one NearestAttackableTargetGoal<"
                + site.goalTargetType().getSimpleName() + "> on the target selector, found " + count + " (" + FINDING + " test setup)");
        return found;
    }

    // ------------------------------------------------------------------
    // Helpers (the IMobConventionTests / CreativeGateParityTests idioms)
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity candidate) {
        helper.assertTrue(hunter.hasLineOfSight(candidate), "precondition: the " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see the "
                + candidate.getClass().getSimpleName() + " inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** With its registered goals (the target selector is the site under test) but no AI, so nothing runs by itself. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** Frozen prey with 1000 HP at an exact spot: goals stripped, noAi, persistence set. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob prey = helper.spawnWithNoFreeWill(type, pos);
        prey.setNoAi(true);
        prey.setPersistenceRequired();
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    /**
     * A plain {@link ServerPlayer} put on the player list the way the framework's mock is, without the framework's
     * override (PlayNicelyGateParityTests.survivalServerPlayerAt): {@code GameTestHelper.makeMockServerPlayerInLevel}
     * answers {@code isCreative()} true whatever its mode; this one's follows its SURVIVAL mode, and its abilities are
     * the mode's (instabuild clear), which the Pointysaurus goal's selector reads.
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

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as IMobConventionTests.rolls. */
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

    /** Where the row's candidate stands, relative to the hunter's feet. */
    private static Vec3 candidateOffset(Row row, Mob hunter, float candidateWidth) {
        double d = row.site().origRange();
        return switch (row.kind()) {
            case EDGE_INSIDE, CADENCE_NO_ROLL, CADENCE_1_IN_6 -> new Vec3(d - EDGE_EPSILON, 0.0, 0.0);
            // orig's selectEntitiesWithinAABB was a box intersection: a candidate whose centre sits d + half the hunter's width + half
            // its own width out along an axis still met the box, so the outside spot adds both half-widths — clear of orig's box and
            // of vanilla's d sphere alike; d + 0.05 itself is PN-020's ring, left unpinned
            case EDGE_OUTSIDE -> new Vec3(d + hunter.getBbWidth() / 2.0 + candidateWidth / 2.0 + EDGE_EPSILON, 0.0, 0.0);
            case BOX_CORNER_TAKEN -> new Vec3(CORNER_OFFSET, 0.0, CORNER_OFFSET);
            // boxes are feet-anchored: the hunter's reaches its height, orig's box 5 above that; the candidate's feet 0.05 higher clear it
            case VERTICAL_BAND_REFUSED -> new Vec3(0.0, hunter.getBbHeight() + POINTYSAURUS_BOX_Y + EDGE_EPSILON, 0.0);
            case FOLLOW_RANGE_KEPT -> throw new IllegalArgumentException(row.kind() + " places no candidate");
        };
    }

    /** The box the goal lists candidates from: vanilla's {@code getTargetSearchArea(d)} at the Mob goals, orig :253's inflate(12, 5, 12) at the Pointysaurus's findTarget. */
    private static AABB scanBoxOf(Mob hunter, NearestAttackableTargetGoal<?> goal, Site site) {
        return site.goalTargetType() == Mob.class
                ? searchAreaOf(goal, followDistanceOf(goal))
                : hunter.getBoundingBox().inflate(POINTYSAURUS_BOX_XZ, POINTYSAURUS_BOX_Y, POINTYSAURUS_BOX_XZ);
    }

    /** The row's geometry in a clause, for its message. */
    private static String why(Row row) {
        Site site = row.site();
        return switch (row.kind()) {
            case EDGE_INSIDE -> "0.05 inside orig's " + site.origRange() + " box edge along +x";
            case EDGE_OUTSIDE -> "outside orig's " + site.origRange() + " box (its box clears the scan box) and vanilla's " + site.origRange()
                    + " sphere alike, inside the attribute's " + site.attribute();
            case CADENCE_NO_ROLL -> "0.05 inside orig's " + site.origRange() + " box edge, no roll forced (orig's targetChance 0 rolled nothing)";
            case BOX_CORNER_TAKEN -> "at a corner of orig's 12x5x12 box, 16.83 off — beyond any " + site.origRange()
                    + " sphere; orig :253-262 took the box's corners";
            case VERTICAL_BAND_REFUSED -> "7.95 straight up — its feet clear the box's top (the hunter's 2.9 + 5), 7.95 off, inside any "
                    + site.origRange() + " sphere; orig's box stopped at +5";
            case CADENCE_1_IN_6 -> "0.05 inside orig's " + site.origRange() + " box edge along +x, the bound-3 roll forced to 0 after a"
                    + " refused pass at 1 (interval 6 = orig :183's 1-in-6 per tick)";
            case FOLLOW_RANGE_KEPT -> "no candidate";
        };
    }

    /**
     * A seeded {@link RandomSource} that counts every {@code nextInt(bound)} draw by bound — the ForcedRoll seam recording instead of
     * forcing: the cadence rows' witness that an interval-0 goal never draws its acquisition roll.
     */
    static final class DrawCounter implements RandomSource {
        private final RandomSource delegate;
        private final Map<Integer, Integer> draws = new TreeMap<>();

        DrawCounter(RandomSource delegate) {
            this.delegate = delegate;
        }

        int drawsOf(int bound) {
            return this.draws.getOrDefault(bound, 0);
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
            this.draws.merge(upper, 1, Integer::sum);
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

        @Override
        public String toString() {
            return "draws by bound " + this.draws;
        }
    }

    private static LivingEntity goalTarget(NearestAttackableTargetGoal<?> goal) {
        return (LivingEntity) readField(goal, NearestAttackableTargetGoal.class, "target");
    }

    /** {@code TargetingConditions.range} of the goal's conditions — the sphere the pick tests, snapshotted from {@code getFollowDistance()} at construction. */
    private static double conditionsRangeOf(NearestAttackableTargetGoal<?> goal) {
        TargetingConditions conditions = (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
        return (Double) readField(conditions, TargetingConditions.class, "range");
    }

    /** The goal's own {@code TargetGoal.getFollowDistance()}, dispatched to the site's override. */
    private static double followDistanceOf(NearestAttackableTargetGoal<?> goal) {
        try {
            Method method = TargetGoal.class.getDeclaredMethod("getFollowDistance");
            method.setAccessible(true);
            return (Double) method.invoke(goal);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read TargetGoal.getFollowDistance", exception);
        }
    }

    /** {@code NearestAttackableTargetGoal.getTargetSearchArea(d)} — the box the Mob goals list candidates from. */
    private static AABB searchAreaOf(NearestAttackableTargetGoal<?> goal, double distance) {
        try {
            Method method = NearestAttackableTargetGoal.class.getDeclaredMethod("getTargetSearchArea", double.class);
            method.setAccessible(true);
            return (AABB) method.invoke(goal, distance);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read NearestAttackableTargetGoal.getTargetSearchArea", exception);
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

    private static String describeCandidate(Site site) {
        return site.candidate() == Candidate.ZOMBIE ? "a Zombie (an Enemy, no Creeper: OrigTargets.vanillaTaskPrey takes it)"
                : "a plain survival player (instabuild clear: the rebuilt selector takes it)";
    }

    private static String describe(LivingEntity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }
}
