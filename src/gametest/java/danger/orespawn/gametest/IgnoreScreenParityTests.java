package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-106: the shared ignore screen in every hunter. 1.7.10 calls
 * {@code MyUtils.isIgnoreable} from 38 sites in 37 species (every hunter's
 * {@code isSuitableTarget}, plus the AntRobot's stomp filter
 * {@code feetisSuitableTarget}); the port had kept it in eleven. The owner
 * ruled every site restored in its original position in the check order, with
 * one generated test per original call site — this class, a
 * {@link GameTestGenerator} over the 38-row table in {@link #sites()}, in orig
 * file order, each row citing its orig line.
 *
 * <p>Two shapes, by where the port keeps the hunter's filter:</p>
 * <ul>
 *   <li><b>PRIVATE_FILTER</b> (37 sites: the eleven the port already kept,
 *       seventeen restored by ENT-S-106, and the nine ENT-S-108 hunters —
 *       CaveFisher, DungeonBeast, EmperorScorpion, HerculesBeetle, Nastysaurus,
 *       SpitBug, TRex, TrooperBug, Urchin — whose vanilla target goals and the
 *       Urchin's players-only nearest scan gave way to the original's
 *       {@code EntityLivingBase} box scan over a private filter, so the screen
 *       now bites in play; TargetScanParityTests covers the scans themselves) —
 *       a private {@code isSuitableTarget(LivingEntity)} (the AntRobot's stomp
 *       site: {@code feetIsSuitableTarget}), reached by reflection as
 *       IgnoreListParityTests and KrakenTargetingParityTests do. The hunter and
 *       a list species stand frozen with clear line of sight; the filter must
 *       reject the species, and a control on the same spot — a vanilla pig, or
 *       a vanilla Zombie where the hunter takes only monsters — must pass, so
 *       geometry, sight and the rest of the chain are not what rejected it.
 *       Each species is chosen so that nothing but the ignore step rejects it
 *       (the row notes say where a hunter's own species rule would too, as in
 *       1.7.10).</li>
 *   <li><b>TARGET_GOAL_PREDICATE</b> (1 site, the Pointysaurus) — the port
 *       replaced the original's scan with a vanilla
 *       {@code NearestAttackableTargetGoal}; the screen is restored as that
 *       goal's predicate, which runs inside the goal's
 *       {@link TargetingConditions} ahead of the sight check. The test reads
 *       every such goal off the hunter's target selector (the hunter is spawned
 *       with its goals and no AI, since {@code spawnWithNoFreeWill} strips
 *       selectors) and asks its conditions directly: the species is refused,
 *       the pig on the same spot is accepted. The goal scans {@code Player.class}
 *       only, as the original's own selection did (orig Pointysaurus.java:242-246),
 *       so the screen's own effect stays unobservable in play, in either tree.</li>
 * </ul>
 *
 * <p>Synchronous, no global state touched. Own batch (TEST-003). Geometry:
 * hunter and prey on the floor of the 48x16x48 empty_large, 8 blocks apart,
 * every eye under the barrier ceiling at rel 17; the four tall hunters
 * (Godzilla 25, TheKing and TheQueen 24, whose eyes sit above 17) run in the
 * 48x34x48 empty_tall (ceiling at rel 35). The royal pair also gets its home
 * point written to its own position (the first-tick init at port
 * TheKing.java:585-588 / TheQueen.java:821-824 never runs while frozen) and
 * faces east so TheKing's block-marching {@code MyCanSee} starts inside the
 * shell; the Kraken's prey is set on the ground for orig Kraken.java:1083; the
 * SpiderRobot's prey stands 5 blocks off, inside the point-blank bypass of its
 * bearing cone (orig SpiderRobot.java:1027-1029). Spawns are frozen and
 * discarded in a finally.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class IgnoreScreenParityTests {

    private static final String BATCH = "ignoreScreenParity";
    private static final String TEST_PREFIX = "ignorescreenparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the templates are named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final String EMPTY_TALL = OreSpawnMod.MOD_ID + ":empty_tall";
    private static final int TIMEOUT_TICKS = 100;

    /** Hunter and prey 8 blocks apart on the floor, clear line of sight. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** SpiderRobot: 5 blocks, inside the point-blank bypass of orig SpiderRobot.java:1027-1029 (distSq &lt; 36). */
    private static final BlockPos POINT_BLANK_PREY_POS = new BlockPos(25, 1, 24);
    /** TheKing / TheQueen (22 wide): box 5.5..27.5, prey 18 blocks off (home leash 144, orig TheKing.java:942-946). */
    private static final BlockPos ROYAL_HUNTER_POS = new BlockPos(16, 1, 24);
    private static final BlockPos ROYAL_PREY_POS = new BlockPos(34, 1, 24);

    private static final Supplier<EntityType<? extends Mob>> PIG = () -> EntityType.PIG;
    private static final Supplier<EntityType<? extends Mob>> ZOMBIE = () -> EntityType.ZOMBIE;
    private static final String PIG_WHY = "a vanilla pig";
    private static final String ZOMBIE_WHY = "a vanilla Zombie (an EntityMob)";

    private static final Consumer<Mob> NO_SETUP = mob -> { };
    /** orig Kraken.java:1083-1085 {@code !onGround && !isInWater → false}: nothing ticks, so the flag is set by hand. */
    private static final Consumer<Mob> GROUNDED = mob -> mob.setOnGround(true);
    /**
     * The royal pair: home = own position (the first-tick init, port TheKing.java:585-588 /
     * TheQueen.java:821-824, unreachable while frozen), and a fixed yaw: TheKing.MyCanSee
     * marches from 22 blocks ahead of the facing at 7/8 of the body height, and a
     * LivingEntity is born with a random yaw, so facing east keeps that start 9 blocks
     * inside the shell.
     */
    private static final Consumer<Mob> ROYAL_SETUP = mob -> {
        writeInt(mob, "homeX", (int) mob.getX());
        writeInt(mob, "homeZ", (int) mob.getZ());
        mob.setYRot(-90.0f);
        mob.yBodyRot = -90.0f;
        mob.yHeadRot = -90.0f;
    };

    private enum Shape { PRIVATE_FILTER, TARGET_GOAL_PREDICATE }

    /** One orig call site. */
    private static final class Site {
        final String id;
        final String orig;
        final Shape shape;
        final String filter;
        final Supplier<? extends EntityType<? extends Mob>> hunter;
        final Supplier<? extends EntityType<? extends Mob>> species;
        final String speciesWhy;
        final String note;
        Supplier<? extends EntityType<? extends Mob>> control = PIG;
        String controlWhy = PIG_WHY;
        BlockPos hunterPos = HUNTER_POS;
        BlockPos preyPos = PREY_POS;
        String template = EMPTY_LARGE;
        Consumer<Mob> hunterSetup = NO_SETUP;
        Consumer<Mob> preySetup = NO_SETUP;
        boolean peacefulGated = false;

        Site(int index, String tag, String orig, Shape shape, String filter,
             Supplier<? extends EntityType<? extends Mob>> hunter,
             Supplier<? extends EntityType<? extends Mob>> species, String speciesWhy, String note) {
            this.id = String.format("s106_%02d_%s", index, tag);
            this.orig = orig;
            this.shape = shape;
            this.filter = filter;
            this.hunter = hunter;
            this.species = species;
            this.speciesWhy = speciesWhy;
            this.note = note;
        }

        Site control(Supplier<? extends EntityType<? extends Mob>> type, String why) {
            this.control = type;
            this.controlWhy = why;
            return this;
        }

        Site tall() {
            this.template = EMPTY_TALL;
            return this;
        }

        Site at(BlockPos hunterAt, BlockPos preyAt) {
            this.hunterPos = hunterAt;
            this.preyPos = preyAt;
            return this;
        }

        Site hunterSetup(Consumer<Mob> setup) {
            this.hunterSetup = setup;
            return this;
        }

        Site preySetup(Consumer<Mob> setup) {
            this.preySetup = setup;
            return this;
        }

        Site peacefulGated() {
            this.peacefulGated = true;
            return this;
        }

        String testName() {
            return TEST_PREFIX + this.id;
        }
    }

    private static Site privateFilter(int index, String tag, String orig, String filter,
                                      Supplier<? extends EntityType<? extends Mob>> hunter,
                                      Supplier<? extends EntityType<? extends Mob>> species, String speciesWhy, String note) {
        return new Site(index, tag, orig, Shape.PRIVATE_FILTER, filter, hunter, species, speciesWhy, note);
    }

    private static Site goalPredicate(int index, String tag, String orig,
                                      Supplier<? extends EntityType<? extends Mob>> hunter,
                                      Supplier<? extends EntityType<? extends Mob>> species, String speciesWhy, String note) {
        return new Site(index, tag, orig, Shape.TARGET_GOAL_PREDICATE, null, hunter, species, speciesWhy, note);
    }

    // ------------------------------------------------------------------
    // The 38 orig call sites, in orig file order
    // ------------------------------------------------------------------

    private static List<Site> sites() {
        List<Site> sites = new ArrayList<>();
        sites.add(privateFilter(1, "alosaurus_192", "Alosaurus.java:192-194", "isSuitableTarget",
                ModEntities.ALOSAURUS, ModEntities.ELEVATOR, "an Elevator (orig MyUtils.java:151)",
                "already present (port Alosaurus.java:184); the Alosaurus otherwise hunts everything living (orig :211)"));
        sites.add(privateFilter(2, "antrobot_feet_971", "AntRobot.java:971-973", "feetIsSuitableTarget",
                ModEntities.ANT_ROBOT, ModEntities.GHOST, "a Ghost (orig MyUtils.java:145)",
                "restored (port AntRobot.feetIsSuitableTarget); the stomp ring of orig :977-986 wants 6..9 blocks, the prey stands 8 off"));
        sites.add(privateFilter(3, "antrobot_1044", "AntRobot.java:1044-1046", "isSuitableTarget",
                ModEntities.ANT_ROBOT, ModEntities.ENTITY_ANT, "an ant (orig MyUtils.java:121)",
                "restored (port AntRobot.isSuitableTarget)"));
        sites.add(privateFilter(4, "basilisk_394", "Basilisk.java:394-396", "isSuitableTarget",
                ModEntities.BASILISK, ModEntities.ENTITY_MOSQUITO, "a mosquito (orig MyUtils.java:127)",
                "already present (port Basilisk.java:257)"));
        sites.add(privateFilter(5, "brutalfly_427", "Brutalfly.java:427-429", "isSuitableTarget",
                ModEntities.ENTITY_BRUTALFLY, ModEntities.ROCK_BASE, "a RockBase (orig MyUtils.java:118)",
                "already present (port EntityBrutalfly.java:355); the Brutalfly hunts EntityMobs and players only"
                        + " (orig :433-441), so every list member fails that rule too — the step's own effect is"
                        + " unobservable in this hunter, in either tree")
                .control(ZOMBIE, ZOMBIE_WHY));
        sites.add(privateFilter(6, "cavefisher_203", "CaveFisher.java:203-205", "isSuitableTarget",
                ModEntities.CAVE_FISHER, ModEntities.ENTITY_CRICKET, "a cricket (orig MyUtils.java:136, an Animal)",
                "restored (port CaveFisher.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of line of"
                        + " sight (orig :206); a cricket is no EntityMob, so orig :218 would not refuse it either"));
        sites.add(privateFilter(7, "dungeonbeast_210", "DungeonBeast.java:210-212", "isSuitableTarget",
                ModEntities.DUNGEON_BEAST, ModEntities.COCKATEIL, "a Cockateil (orig MyUtils.java:139)",
                "restored (port DungeonBeast.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of line of"
                        + " sight (orig :213)"));
        sites.add(privateFilter(8, "emperorscorpion_473", "EmperorScorpion.java:473-475", "isSuitableTarget",
                ModEntities.ENTITY_EMPEROR_SCORPION, ModEntities.ENTITY_DRAGONFLY, "a dragonfly (orig MyUtils.java:130)",
                "restored (port EntityEmperorScorpion.isSuitableTarget, the filter of the ENT-S-108 box scan), after"
                        + " the line-of-sight check as in orig (:470)"));
        sites.add(privateFilter(9, "gammametroid_266", "GammaMetroid.java:266-268", "isSuitableTarget",
                ModEntities.ENTITY_GAMMA_METROID, ModEntities.FIREFLY, "a Firefly (orig MyUtils.java:133)",
                "restored (port EntityGammaMetroid.isSuitableTarget), ahead of line of sight (orig :269); untamed, so"
                        + " orig :278 does not refuse"));
        sites.add(privateFilter(10, "giantrobot_324", "GiantRobot.java:324-326", "isSuitableTarget",
                ModEntities.GIANT_ROBOT, ModEntities.COCKATEIL, "a Cockateil (orig MyUtils.java:139)",
                "restored (port GiantRobot.isSuitableTarget)"));
        sites.add(privateFilter(11, "godzilla_442", "Godzilla.java:442-444", "isSuitableTarget",
                ModEntities.GODZILLA, ModEntities.ENTITY_TERMITE, "a Termite (orig MyUtils.java:142)",
                "restored (port Godzilla.isSuitableTarget), ahead of line of sight (orig :445); 25 tall, so empty_tall")
                .tall());
        sites.add(privateFilter(12, "herculesbeetle_395", "HerculesBeetle.java:395-397", "isSuitableTarget",
                ModEntities.ENTITY_HERCULES_BEETLE, ModEntities.ENTITY_BUTTERFLY, "a butterfly (orig MyUtils.java:124)",
                "restored (port EntityHerculesBeetle.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of"
                        + " line of sight (orig :398)"));
        sites.add(privateFilter(13, "kraken_1070", "Kraken.java:1070-1072", "isSuitableTarget",
                ModEntities.KRAKEN, ModEntities.FIREFLY, "a Firefly (orig MyUtils.java:133)",
                "already present (port Kraken.java:662, ENT-S-100 KT-B1); prey grounded for orig :1083-1085")
                .preySetup(GROUNDED));
        sites.add(privateFilter(14, "kyuubi_183", "Kyuubi.java:183-185", "isSuitableTarget",
                ModEntities.ENTITY_KYUUBI, ModEntities.ENTITY_CRICKET, "a cricket (orig MyUtils.java:136)",
                "restored (port EntityKyuubi.isSuitableTarget), ahead of line of sight (orig :186)"));
        sites.add(privateFilter(15, "leon_403", "Leon.java:403-405", "isSuitableTarget",
                ModEntities.ENTITY_LEON, ModEntities.ENTITY_MOSQUITO, "a mosquito (orig MyUtils.java:127)",
                "restored (port EntityLeon.isSuitableTarget), ahead of line of sight (orig :406); untamed, so the"
                        + " orig :422-427 tail grants only attackable non-mobs (ENT-S-110): a pig is refused there,"
                        + " hence the Zombie control (orig :412 EntityMob). The mosquito is refused by that tail as"
                        + " well, so this row cannot isolate the ignore step -- unobservable in either tree, like"
                        + " the Brutalfly, King and Queen rows")
                .control(ZOMBIE, ZOMBIE_WHY).peacefulGated());
        sites.add(privateFilter(16, "mothra_437", "Mothra.java:437-439", "isSuitableTarget",
                ModEntities.MOTHRA, ModEntities.GHOST_SKELLY, "a GhostSkelly (orig MyUtils.java:148)",
                "already present (port Mothra.java:266); Mothra's own chain (orig :443-475) names no list member")
                .peacefulGated());
        sites.add(privateFilter(17, "nastysaurus_256", "Nastysaurus.java:256-258", "isSuitableTarget",
                ModEntities.NASTYSAURUS, ModEntities.ENTITY_TERMITE, "a Termite (orig MyUtils.java:142)",
                "restored (port Nastysaurus.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of the"
                        + " species chain and line of sight (orig :268)"));
        sites.add(privateFilter(18, "pitchblack_498", "PitchBlack.java:498-500", "isSuitableTarget",
                ModEntities.PITCH_BLACK, ModEntities.GHOST_SKELLY, "a GhostSkelly (orig MyUtils.java:148)",
                "restored (port PitchBlack.isSuitableTarget)"));
        sites.add(goalPredicate(19, "pointysaurus_227", "Pointysaurus.java:227-229",
                ModEntities.POINTYSAURUS, ModEntities.ROCK_BASE, "a RockBase (orig MyUtils.java:118)",
                "restored as the target goal's predicate (port Pointysaurus.java:69); orig takes players only anyway"
                        + " (:242-246), and so does the goal"));
        sites.add(privateFilter(20, "purplepower_248", "PurplePower.java:248-250", "isSuitableTarget",
                ModEntities.PURPLE_POWER, ModEntities.ROCK_BASE, "a RockBase (orig MyUtils.java:118)",
                "restored (port PurplePower.isSuitableTarget); a non-player, non-royal is prey (orig :264)"));
        sites.add(privateFilter(21, "rat_195", "Rat.java:195-197", "isSuitableTarget",
                ModEntities.ENTITY_RAT, ModEntities.ENTITY_ANT, "an ant (orig MyUtils.java:121)",
                "restored (port EntityRat.isSuitableTarget), ahead of line of sight (orig :198); a wild rat, no owner"));
        sites.add(privateFilter(22, "robot1_186", "Robot1.java:186-188", "isSuitableTarget",
                ModEntities.ROBOT_1, ModEntities.ENTITY_BUTTERFLY, "a butterfly (orig MyUtils.java:124)",
                "restored (port Robot1.isSuitableTarget)"));
        sites.add(privateFilter(23, "robot2_363", "Robot2.java:363-365", "isSuitableTarget",
                ModEntities.ROBOT_2, ModEntities.ENTITY_DRAGONFLY, "a dragonfly (orig MyUtils.java:130)",
                "restored (port Robot2.isSuitableTarget)"));
        sites.add(privateFilter(24, "robot3_303", "Robot3.java:303-305", "isSuitableTarget",
                ModEntities.ROBOT_3, ModEntities.FIREFLY, "a Firefly (orig MyUtils.java:133)",
                "restored (port Robot3.isSuitableTarget)"));
        sites.add(privateFilter(25, "robot4_367", "Robot4.java:367-369", "isSuitableTarget",
                ModEntities.ROBOT_4, ModEntities.ENTITY_CRICKET, "a cricket (orig MyUtils.java:136)",
                "restored (port Robot4.isSuitableTarget)"));
        sites.add(privateFilter(26, "robot5_277", "Robot5.java:277-279", "isSuitableTarget",
                ModEntities.ROBOT_5, ModEntities.COCKATEIL, "a Cockateil (orig MyUtils.java:139)",
                "restored (port Robot5.isSuitableTarget)"));
        sites.add(privateFilter(27, "rotator_304", "Rotator.java:304-306", "isSuitableTarget",
                ModEntities.ENTITY_ROTATOR, ModEntities.ENTITY_MOSQUITO, "a mosquito (orig MyUtils.java:127)",
                "already present (port EntityRotator.java:230); not the Termite, which the Rotator names itself (orig :316)"));
        sites.add(privateFilter(28, "scorpion_213", "Scorpion.java:213-215", "isSuitableTarget",
                ModEntities.ENTITY_SCORPION, ModEntities.ELEVATOR, "an Elevator (orig MyUtils.java:151)",
                "already present (port EntityScorpion.java:197); not a Ghost or GhostSkelly, which the Scorpion names"
                        + " itself (orig :219-224)"));
        sites.add(privateFilter(29, "spiderdriver_134", "SpiderDriver.java:134-136", "isSuitableTarget",
                ModEntities.SPIDER_DRIVER, ModEntities.ENTITY_TERMITE, "a Termite (orig MyUtils.java:142)",
                "restored (port SpiderDriver.isSuitableTarget); the control stands 8 off, past the orig :156 six-block refusal")
                .peacefulGated());
        sites.add(privateFilter(30, "spiderrobot_1013", "SpiderRobot.java:1013-1015", "isSuitableTarget",
                ModEntities.SPIDER_ROBOT, ModEntities.COCKATEIL, "a Cockateil (orig MyUtils.java:139)",
                "already present (port SpiderRobot.java:593); prey 5 blocks off, inside the point-blank bypass of the"
                        + " bearing cone (orig :1027-1029)")
                .at(HUNTER_POS, POINT_BLANK_PREY_POS));
        sites.add(privateFilter(31, "spitbug_334", "SpitBug.java:334-336", "isSuitableTarget",
                ModEntities.ENTITY_SPIT_BUG, ModEntities.GHOST, "a Ghost (orig MyUtils.java:145)",
                "restored (port EntitySpitBug.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of line"
                        + " of sight (orig :337)"));
        sites.add(privateFilter(32, "trex_226", "TRex.java:226-228", "isSuitableTarget",
                ModEntities.TREX, ModEntities.GHOST_SKELLY, "a GhostSkelly (orig MyUtils.java:148)",
                "restored (port TRex.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of line of sight"
                        + " (orig :229)"));
        sites.add(privateFilter(33, "theking_947", "TheKing.java:947-949", "isSuitableTarget",
                ModEntities.THE_KING, ModEntities.ELEVATOR, "an Elevator (orig MyUtils.java:151)",
                "already present (port TheKing.java:1169); the King takes players, horses, EntityMobs, the dragon and"
                        + " the attackable non-mobs (orig :959-977), so every list member fails that chain too — the"
                        + " step's own effect is unobservable here, in either tree; 24 tall, so empty_tall")
                .control(ZOMBIE, ZOMBIE_WHY).tall().at(ROYAL_HUNTER_POS, ROYAL_PREY_POS).hunterSetup(ROYAL_SETUP));
        sites.add(privateFilter(34, "thequeen_910", "TheQueen.java:910-912", "isSuitableTarget",
                ModEntities.THE_QUEEN, ModEntities.ENTITY_ANT, "an ant (orig MyUtils.java:121)",
                "already present (port TheQueen.java:1377); the Queen's chain is the King's (players, horses, EntityMobs,"
                        + " the dragon, attackable non-mobs), so every list member fails it too — the step's own effect"
                        + " is unobservable here, in either tree; 24 tall, so empty_tall")
                .control(ZOMBIE, ZOMBIE_WHY).tall().at(ROYAL_HUNTER_POS, ROYAL_PREY_POS).hunterSetup(ROYAL_SETUP));
        sites.add(privateFilter(35, "triffid_285", "Triffid.java:285-287", "isSuitableTarget",
                ModEntities.ENTITY_TRIFFID, ModEntities.GHOST, "a Ghost (orig MyUtils.java:145)",
                "restored (port EntityTriffid.isSuitableTarget), ahead of line of sight (orig :288)"));
        sites.add(privateFilter(36, "trooperbug_474", "TrooperBug.java:474-476", "isSuitableTarget",
                ModEntities.ENTITY_TROOPER_BUG, ModEntities.ENTITY_MOSQUITO, "a mosquito (orig MyUtils.java:127)",
                "restored (port EntityTrooperBug.isSuitableTarget, the filter of the ENT-S-108 box scan), ahead of"
                        + " line of sight (orig :477)"));
        sites.add(privateFilter(37, "urchin_230", "Urchin.java:230-232", "isSuitableTarget",
                ModEntities.URCHIN, ModEntities.ENTITY_BUTTERFLY, "a butterfly (orig MyUtils.java:124)",
                "restored (port Urchin.isSuitableTarget, the filter of the ENT-S-108 box scan that replaced the"
                        + " players-only getNearestPlayer scan), ahead of line of sight (orig :233)"));
        sites.add(privateFilter(38, "vortex_296", "Vortex.java:296-298", "isSuitableTarget",
                ModEntities.ENTITY_VORTEX, ModEntities.FIREFLY, "a Firefly (orig MyUtils.java:133)",
                "already present (port EntityVortex.java:345); the Vortex's own exclusions (orig :303-338) name no list member"));
        return sites;
    }

    /** One test per orig call site: 38 TestFunctions in the {@code ignoreScreenParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> ignoreScreenSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), site.template, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runners
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Site site) {
        if (site.peacefulGated) {
            helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                    "precondition: the filter behind orig " + site.orig + " rejects everything on Peaceful; the"
                            + " game-test level runs at NORMAL (ENT-S-106 test setup)");
        }
        switch (site.shape) {
            case PRIVATE_FILTER -> assertPrivateFilterScreens(helper, site);
            case TARGET_GOAL_PREDICATE -> assertGoalPredicateScreens(helper, site);
        }
        helper.succeed();
    }

    /**
     * The hunter's private filter rejects the list species and accepts the control on
     * the same spot, so the ignore step — and nothing else in the chain — is what
     * rejected the species.
     */
    private static void assertPrivateFilterScreens(GameTestHelper helper, Site site) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnFrozen(helper, site.hunter.get(), site.hunterPos);
            site.hunterSetup.accept(hunter);
            String where = hunter.getClass().getSimpleName() + "." + site.filter + " (orig " + site.orig + ")";
            species = spawnFrozen(helper, site.species.get(), site.preyPos);
            site.preySetup.accept(species);
            assertPreconditions(helper, hunter, species, site);
            boolean accepted = invokeFilter(hunter, site.filter, species);
            helper.assertTrue(!accepted, where + ": " + site.speciesWhy + " is on the shared ignore list and must be"
                    + " rejected by the ignore step, but it was accepted — " + site.note + " (ENT-S-106)");
            species.discard();
            species = null;
            control = spawnFrozen(helper, site.control.get(), site.preyPos);
            site.preySetup.accept(control);
            helper.assertTrue(hunter.hasLineOfSight(control), "precondition: the hunter must see the control ("
                    + site.controlWhy + ") on the prey spot inside the barrier shell (ENT-S-106 test geometry)");
            helper.assertTrue(invokeFilter(hunter, site.filter, control), "control: " + where + " must accept "
                    + site.controlWhy + " on the same spot, so " + site.speciesWhy + " was rejected by the ignore"
                    + " step and not by geometry, sight or the species chain — " + site.note + " (ENT-S-106)");
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
    }

    /**
     * Every {@link NearestAttackableTargetGoal} on the hunter's target selector carries
     * the screen as its predicate: its {@link TargetingConditions} refuse the list
     * species and accept the control on the same spot.
     */
    private static void assertGoalPredicateScreens(GameTestHelper helper, Site site) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnWithGoals(helper, site.hunter.get(), site.hunterPos);
            site.hunterSetup.accept(hunter);
            String name = hunter.getClass().getSimpleName();
            List<NearestAttackableTargetGoal<?>> goals = nearestAttackableTargetGoals(hunter);
            helper.assertTrue(!goals.isEmpty(), "precondition: " + name + " must carry at least one"
                    + " NearestAttackableTargetGoal on its target selector — the port's shape of the filter behind"
                    + " orig " + site.orig + " (ENT-S-106 test setup)");
            species = spawnFrozen(helper, site.species.get(), site.preyPos);
            site.preySetup.accept(species);
            assertPreconditions(helper, hunter, species, site);
            for (NearestAttackableTargetGoal<?> goal : goals) {
                String where = name + "'s " + describe(goal) + " (orig " + site.orig + ")";
                helper.assertTrue(!targetConditionsOf(goal).test(hunter, species), where + ": " + site.speciesWhy
                        + " is on the shared ignore list and must be refused by the goal's predicate, but its"
                        + " TargetingConditions accepted it — " + site.note + " (ENT-S-106)");
            }
            species.discard();
            species = null;
            control = spawnFrozen(helper, site.control.get(), site.preyPos);
            site.preySetup.accept(control);
            helper.assertTrue(hunter.hasLineOfSight(control), "precondition: the hunter must see the control ("
                    + site.controlWhy + ") on the prey spot inside the barrier shell (ENT-S-106 test geometry)");
            for (NearestAttackableTargetGoal<?> goal : goals) {
                String where = name + "'s " + describe(goal) + " (orig " + site.orig + ")";
                helper.assertTrue(targetConditionsOf(goal).test(hunter, control), "control: " + where
                        + " must accept " + site.controlWhy + " on the same spot (in range, in sight, attackable),"
                        + " so " + site.speciesWhy + " was refused by the predicate alone — " + site.note + " (ENT-S-106)");
            }
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
    }

    private static void assertPreconditions(GameTestHelper helper, Mob hunter, Mob species, Site site) {
        helper.assertTrue(MyUtils.isIgnoreable(species), "precondition: " + site.speciesWhy
                + " must be on the shared list (orig MyUtils.java:117-152) (ENT-S-106 test setup)");
        helper.assertTrue(hunter.hasLineOfSight(species), "precondition: " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see "
                + site.speciesWhy + " on the prey spot inside the barrier shell (ENT-S-106 test geometry)");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (the target selector is the site under test) but no AI, so nothing runs. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static List<NearestAttackableTargetGoal<?>> nearestAttackableTargetGoals(Mob hunter) {
        List<NearestAttackableTargetGoal<?>> goals = new ArrayList<>();
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> goal) {
                goals.add(goal);
            }
        }
        return goals;
    }

    private static String describe(NearestAttackableTargetGoal<?> goal) {
        Class<?> targetType = (Class<?>) readField(goal, NearestAttackableTargetGoal.class, "targetType");
        return "NearestAttackableTargetGoal<" + targetType.getSimpleName() + ">";
    }

    /** The goal's {@code protected TargetingConditions targetConditions}, which carries the predicate. */
    private static TargetingConditions targetConditionsOf(NearestAttackableTargetGoal<?> goal) {
        return (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
    }

    /** The hunter's private one-arg filter — the port's shape of the orig two-arg {@code isSuitableTarget}. */
    private static boolean invokeFilter(Mob hunter, String filter, LivingEntity candidate) {
        String name = hunter.getClass().getSimpleName() + "." + filter;
        try {
            Method method = hunter.getClass().getDeclaredMethod(filter, LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name, exception);
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

    private static void writeInt(Mob mob, String name, int value) {
        String owner = mob.getClass().getSimpleName();
        try {
            Field field = mob.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.setInt(mob, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + owner + "." + name, exception);
        }
    }
}
