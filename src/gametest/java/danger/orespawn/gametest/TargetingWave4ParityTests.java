package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.EnderKnight;
import danger.orespawn.entity.EnderReaper;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityStinky;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.Robot2;
import danger.orespawn.entity.Robot3;
import danger.orespawn.entity.Robot4;
import danger.orespawn.entity.Robot5;
import danger.orespawn.entity.ai.AmbientFlightGoal;
import danger.orespawn.entity.ai.ButterflyIslandsHuntGoal;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import danger.orespawn.entity.ai.JealousyTargetGoal;
import danger.orespawn.entity.ai.LunaMothFlightGoal;
import danger.orespawn.util.SeasonalDates;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Targeting wave 4 (owner ruling 2026-09-05, scope addendum item 23 (6)-(7)): the eight register entries ENT-S-130 / 133 / 134 /
 * 137 / 138 / 142 / 143 / 144 — parity, classic — restored at the orig positions (ENT-S-145, the Ender pair's +6.2 attacking boost, was
 * transcribed and withdrawn before the gate — HELD for the owner's value ruling under the modern mover, ENT-S-150). One generated {@link TestFunction} per
 * row ({@code targetingwave4paritytests.s<finding>_NN_<species>_<what>}), all in the {@code targetingWave4Parity} batch (TEST-003).
 *
 * <p>Per row the discriminating case of the entry: the pick ORDER with two goals eligible through a direct {@code targetSelector
 * .tick()} on a frozen companion (ENT-S-130 — Leon's hunt @3 over its revenge @4, the Girlfriend's hunt @3 over Jealousy @4:
 * a Zombie in the box beats a Pig attacker / an untamed rival, and pre-empts a revenge / jealousy goal already running); the
 * robots' forgetting pass under a forced 1-in-50 engaging NOTHING — no look, no path, no attack pose, no laser — where the
 * read-then-clear order of HEAD engaged the attacker it was forgetting (ENT-S-133); the Stinky's three flight boxes through
 * the private {@code doMovement} under pinned dice — the owner on the ground {@code nextInt(4) + 6}, the owner flying
 * {@code nextInt(8)}, wild {@code nextInt(5) + 6} (ENT-S-134); the companions' state gates on both hunts — an untamed Boyfriend
 * or Girlfriend refuses a Zombie and a Creeper, a sitting Girlfriend refuses, a sitting Boyfriend still hunts, the Valentine
 * Player task takes an untamed Girlfriend's pick (ENT-S-137); the five cast sites at a fractional NEGATIVE y — the harness grid
 * sits below y 0 (its corner at y -59; x / z random per run within ±14999992), the rows on the INTEGER x / z lattice (rel 20.0 /
 * 24.0), so the cast and the floor agree on x / z at any origin and y alone discriminates: a parked flight target at the cell the
 * cast reads inside the near-retarget band and the floor outside it, and the reverse, decide the retarget for the Brutalfly,
 * Mothra, the Dragonfly (AmbientFlightGoal's base cast — the Firefly's, the Mosquito's and the VampireButterfly's site too) and the
 * butterfly (ButterflyIslandsHuntGoal's own copy — the Luna Moth's loop runs through it), the Dragonfly's prey cell
 * {@code (int) (posY + 1.0)} read back (ENT-S-138); the Knight's wet
 * teleport (water at its feet, {@code aiStep()} once under pinned offsets: 4 east, 4 south, the scream off; dry: nothing) with
 * the Reaper's as the twin control (ENT-S-142); the Luna Moth's preset numbers, its torch sought at night under an open sky on a
 * non-retarget tick, never by day, on the +z face HEAD's ±x scan never read, and never on a retarget tick (ENT-S-143); the
 * Ender pair's portal sound heard through a {@code PlayLevelSoundEvent} ear once at the origin and once at the landing on a
 * landed random teleport and not at all on a refused one — a request 32 below the floor (ENT-S-144).
 * Every flip (the config keys, the day time) restored in a finally; frozen mobs that teleport spawn with their feet ON the floor
 * (rel y 0, F0.7, the MiscTargetingParityTests floor note); survival players are plain {@link ServerPlayer}s; PlayNicely and the
 * difficulty asserted, never flipped; the spawn shield is never cleared (no row pins a hit on a mock player); dice through the
 * ForcedRoll chain; every spawn discarded, every player removed, every ear closed in a finally.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TargetingWave4ParityTests {

    private static final String BATCH = "targetingWave4Parity";
    private static final String TEST_PREFIX = "targetingwave4paritytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;

    /** The hunter on the template floor (the sibling batches' spot, rel y 1 — a frozen mob floats a block above the floor). */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** The hunter with its feet ON the floor (rel y 0 — the MiscTargetingParityTests floor note): the teleport rows' spot. */
    private static final BlockPos FLOOR_POS = new BlockPos(20, 0, 24);
    /** 8 blocks east on the floor, clear line of sight. */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** 4 blocks east — inside the Jealousy goal's 6-block reach. */
    private static final BlockPos RIVAL_POS = new BlockPos(24, 1, 24);
    /** 4 blocks west — the Pig attacker of the Leon row. */
    private static final BlockPos ATTACKER_POS = new BlockPos(16, 1, 24);
    private static final Vec3 PLAYER_8_POS = new Vec3(28.5, 1.0, 24.5);
    /** The owner of the Girlfriend / Stinky rows, 4 blocks north. */
    private static final Vec3 OWNER_POS = new Vec3(20.5, 1.0, 20.5);
    /**
     * A hunter at a fractional NEGATIVE y — rel 1.5 over the floor at rel 0 (the harness grid sits below y 0: its corner at y -59, x / z
     * random per run within ±14999992), so the (int) cast reads one cell above the floor's. On the INTEGER x / z lattice (rel 20.0 / 24.0,
     * not the block centre): an exact integer casts and floors alike on any origin, so the two cells differ on y alone — at .5 a negative
     * origin on x or z shifted the cast cell there too, and the rows' floor-cell arithmetic read other values (the wave-4 refuter A).
     */
    private static final Vec3 FRACTIONAL_POS = new Vec3(20.0, 1.5, 24.0);
    /** The Dragonfly row's prey: 2 east of FRACTIONAL_POS on the floor, on the same lattice — distSq 4.25, inside the bite reach (orig :147 &lt; 6). */
    private static final Vec3 FRACTIONAL_ROW_TOUCH_POS = new Vec3(22.0, 1.0, 24.0);
    /**
     * s138_04's prey at a fractional negative y on integer x / z: its x / z assertions derive from the cast on both sides, and the
     * shared precondition ({@link #assertFractionalNegativeY}) asks the cast and the floor to agree on x / z on ANY grid origin — an
     * exact integer casts and floors alike, a half-block does not on a negative origin axis (the w4b gate's one red row, at x
     * -5804967.5). 2 east of the hunter's cell, 1.58 blocks off its centre: inside the bite reach (orig :147 &lt; 6).
     */
    private static final Vec3 FRACTIONAL_TOUCH_POS = new Vec3(22.0, 1.5, 24.0);
    private static final float PREY_HEALTH = 1000.0f;
    /** A tick count past the goals' initial timestamp 0, so a primed lastHurtByMob is seen (the TargetReleaseParityTests idiom). */
    private static final int TICKS_ALIVE = 100;
    /** The random teleport's pinned nextDouble: (0.5625 - 0.5) * 64 = 4 blocks on x and on z (orig :143 / :145). */
    private static final double RANDOM_TELEPORT_DOUBLE = 0.5625;
    private static final double RANDOM_TELEPORT_OFFSET = 4.0;
    /** The random teleport's pinned nextInt(64): 32 - 32 = 0 on y (orig :144); 0 - 32 requests a spot 32 below the floor (refused). */
    private static final int RANDOM_TELEPORT_Y_ROLL = 32;
    private static final int REFUSED_TELEPORT_Y_ROLL = 0;
    /** The toward teleport's pinned nextInt(16) — never drawn by these rows, pinned for the seam's completeness. */
    private static final int TOWARD_Y_ROLL = 11;
    /** The daylight dice pinned quiet: nextFloat 1.0 → 30 &lt; (f - 0.4) * 2 fails (orig :111). */
    private static final float QUIET_FLOAT = 1.0f;
    private static final double POSITION_TOLERANCE = 1.0e-3;
    /** The portal sound is played at the origin and at the entity; the ear listens within this radius of each. */
    private static final double EAR_RADIUS = 1.5;
    private static final long NIGHT_TIME = 18000L;
    private static final long DAY_TIME = 6000L;
    private static final LocalDate VALENTINES = LocalDate.of(2026, 2, 14);

    // ------------------------------------------------------------------
    // The row table
    // ------------------------------------------------------------------

    private record Row(String name, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + this.name;
        }
    }

    private record EnderSite(String key, Supplier<? extends EntityType<? extends Mob>> type, String origFile) {
    }

    private static final List<EnderSite> ENDER_SITES = List.of(
            new EnderSite("enderknight", ModEntities.ENDER_KNIGHT, "EnderKnight.java"),
            new EnderSite("enderreaper", ModEntities.ENDER_REAPER, "EnderReaper.java"));

    private static List<Row> rows() {
        List<Row> r = new ArrayList<>();
        r.add(new Row("s130_01_leon_92_hunt_ahead_of_hurtby_pick_order", TargetingWave4ParityTests::leonHuntAheadOfRevenge));
        r.add(new Row("s130_02_girlfriend_167_hunt_ahead_of_jealousy_pick_order", TargetingWave4ParityTests::girlfriendHuntAheadOfJealousy));
        r.add(new Row("s133_01_robot2_281_forget_rolled_before_read_engages_nothing", h -> robotForgetEngagesNothing(h, ModEntities.ROBOT_2.get(), Robot2.class,
                new int[] {6, 1, 50, 0, 450, 0}, new int[] {6, 1, 50, 1, 450, 0}, "Robot2.java:281-284", "ENT-S-133"))); // 450 → 0: the Pounder's idle tantrum roll (orig :320-335) pinned quiet
        r.add(new Row("s133_02_robot3_242_forget_rolled_before_read_engages_nothing", h -> robotForgetEngagesNothing(h, ModEntities.ROBOT_3.get(), Robot3.class,
                new int[] {50, 0}, new int[] {50, 1}, "Robot3.java:242-245", "ENT-S-133")));
        r.add(new Row("s133_03_robot4_282_forget_rolled_before_read_engages_nothing", h -> robotForgetEngagesNothing(h, ModEntities.ROBOT_4.get(), Robot4.class,
                new int[] {8, 1, 50, 0}, new int[] {8, 1, 50, 1}, "Robot4.java:282-285", "ENT-S-133")));
        r.add(new Row("s133_04_robot5_214_forget_rolled_before_read_engages_nothing", h -> robotForgetEngagesNothing(h, ModEntities.ROBOT_5.get(), Robot5.class,
                new int[] {50, 0}, new int[] {50, 1}, "Robot5.java:214-217", "ENT-S-133")));
        r.add(new Row("s134_01_stinky_621_tame_owner_grounded_box_nextint4_plus_6", h -> stinkyFlightBox(h, StinkyCase.OWNER_GROUNDED)));
        r.add(new Row("s134_02_stinky_624_tame_owner_flying_box_nextint8", h -> stinkyFlightBox(h, StinkyCase.OWNER_FLYING)));
        r.add(new Row("s134_03_stinky_628_wild_box_nextint5_plus_6", h -> stinkyFlightBox(h, StinkyCase.WILD)));
        r.add(new Row("s137_01_boyfriend_44_untamed_refuses_both_hunts_tamed_takes", TargetingWave4ParityTests::boyfriendUntamedGate));
        r.add(new Row("s137_02_boyfriend_50_sitting_tamed_still_hunts", TargetingWave4ParityTests::boyfriendSittingHunts));
        r.add(new Row("s137_03_girlfriend_44_untamed_refuses_both_hunts", TargetingWave4ParityTests::girlfriendUntamedGate));
        r.add(new Row("s137_04_girlfriend_50_sitting_refuses_standing_takes", TargetingWave4ParityTests::girlfriendSittingGate));
        r.add(new Row("s137_05_girlfriend_valentine_47_untamed_player_task_still_picks", TargetingWave4ParityTests::girlfriendValentineUngated));
        r.add(new Row("s138_01_brutalfly_174_self_cell_cast_decides_near_retarget", TargetingWave4ParityTests::brutalflySelfCellCast));
        r.add(new Row("s138_02_mothra_184_self_cell_cast_decides_near_retarget", TargetingWave4ParityTests::mothraSelfCellCast));
        r.add(new Row("s138_03_dragonfly_124_flight_cell_cast_decides_near_retarget", TargetingWave4ParityTests::dragonflyFlightCellCast));
        r.add(new Row("s138_04_dragonfly_145_prey_cell_cast_posy_plus_1", TargetingWave4ParityTests::dragonflyPreyCellCast));
        r.add(new Row("s138_05_butterfly_154_flight_cell_cast_decides_near_retarget", TargetingWave4ParityTests::butterflyFlightCellCast));
        for (EnderSite site : ENDER_SITES) {
            r.add(new Row("s142_0" + (site == ENDER_SITES.get(0) ? 1 : 2) + "_" + site.key() + "_116_wet_teleports_scream_off", h -> enderWetTeleport(h, site)));
        }
        r.add(new Row("s143_01_lunamoth_126_flight_numbers_10_068_075", TargetingWave4ParityTests::lunaMothNumbers));
        r.add(new Row("s143_02_lunamoth_133_night_open_sky_torch_sought_in_else_branch", h -> lunaMothTorch(h, MothCase.NIGHT_OPEN_SKY)));
        r.add(new Row("s143_03_lunamoth_133_day_no_torch_sought", h -> lunaMothTorch(h, MothCase.DAY)));
        r.add(new Row("s143_04_lunamoth_99_torch_on_plus_z_face_found", h -> lunaMothTorch(h, MothCase.PLUS_Z_FACE)));
        r.add(new Row("s143_05_lunamoth_126_retarget_tick_wanders_never_the_torch", h -> lunaMothTorch(h, MothCase.RETARGET_TICK)));
        for (EnderSite site : ENDER_SITES) {
            r.add(new Row("s144_0" + (site == ENDER_SITES.get(0) ? 1 : 2) + "_" + site.key() + "_203_portal_sound_at_origin_and_landing", h -> enderPortalSound(h, site)));
        }
        return r;
    }

    /** One test per row: 28 TestFunctions in the {@code targetingWave4Parity} batch (24 rows plus the two Ender-pair pairs). */
    @GameTestGenerator
    public Collection<TestFunction> targetingWave4Rows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, row)));
        }
        return functions;
    }

    private static void run(GameTestHelper helper, Row row) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — the hunts and the vanilla screens refuse on Peaceful (wave 4 test setup)");
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                "precondition: PlayNicely must be off — every hunt of this batch answers nothing under it (ENT-S-115); no row flips it (wave 4 test setup)");
        row.body().accept(helper);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // ENT-S-130 — orig Leon.java:92-95 (hunt @1, HurtBy @2), Girlfriend.java:161-174 (hunt @3, Jealousy @4 / @5)
    // ------------------------------------------------------------------

    /**
     * Leon: a Pig attacker (its hunt refuses a Pig — no Enemy) and a Zombie 8 blocks east in the hunt's 16-box. With the Pig
     * alone the revenge goal holds it; the Zombie appearing, the hunt @3 pre-empts the running revenge @4 (HEAD's hunt @4 could
     * not replace HurtBy @3); a fresh Leon with both present picks the Zombie in the same pass (the revenge goal, registered
     * first, starts first and is replaced within the pass by the lower-numbered hunt).
     */
    private static void leonHuntAheadOfRevenge(GameTestHelper helper) {
        final String finding = "ENT-S-130";
        Mob leon = null;
        Mob second = null;
        Mob pig = null;
        Mob zombie = null;
        try {
            leon = spawnWithGoals(helper, ModEntities.ENTITY_LEON.get(), HUNTER_POS);
            helper.assertTrue(priorityOfType(leon, Mob.class) == 3, "EntityLeon.registerGoals (orig Leon.java:93 @1 over :95 @2): the IMob hunt sits at target"
                    + " priority 3 (" + finding + "); found " + priorityOfType(leon, Mob.class));
            helper.assertTrue(priorityOfClass(leon, "RevengeGoal") == 4, "EntityLeon.registerGoals (orig Leon.java:95): the revenge goal sits at target priority 4,"
                    + " behind the hunt (" + finding + "); found " + priorityOfClass(leon, "RevengeGoal"));
            pig = spawnPrey(helper, EntityType.PIG, ATTACKER_POS);
            leon.tickCount = TICKS_ALIVE;
            leon.setLastHurtByMob(pig);
            leon.getSensing().tick();
            leon.targetSelector.tick();
            helper.assertTrue(leon.getTarget() == pig, "control: with the Pig attacker alone the revenge goal (orig Leon.java:95) holds it (" + finding
                    + "); slot " + describe(leon.getTarget()));
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            leon.getSensing().tick();
            assertSees(helper, leon, zombie, "a Zombie 8 blocks east", finding);
            leon.targetSelector.tick();
            helper.assertTrue(leon.getTarget() == zombie, "orig Leon.java:92-95 — the IMob hunt @1 pre-empts the running EntityAIHurtByTarget @2 (both engines"
                    + " let a strictly lower-numbered target goal replace a running higher one): a Zombie in sight displaces the Pig the revenge goal held; HEAD's"
                    + " HurtBy @3 blocked its hunt @4 until the attacker was dead or gone (" + finding + "); slot " + describe(leon.getTarget()));
            second = spawnWithGoals(helper, ModEntities.ENTITY_LEON.get(), HUNTER_POS);
            second.tickCount = TICKS_ALIVE;
            second.setLastHurtByMob(pig);
            second.getSensing().tick();
            second.targetSelector.tick();
            helper.assertTrue(second.getTarget() == zombie, "orig Leon.java:92-95 — both eligible in one pass, the hunt @3 wins over the revenge @4: the Zombie,"
                    + " not the Pig attacker (" + finding + "); slot " + describe(second.getTarget()));
        } finally {
            discardQuietly(zombie);
            discardQuietly(pig);
            discardQuietly(second);
            discardQuietly(leon);
        }
    }

    /**
     * The Girlfriend (tamed with an owner, standing): an untamed rival Girlfriend 4 blocks east inside the Jealousy goal's 6-box
     * and a Zombie 8 blocks east in the hunt's 15-box. The rival alone: Jealousy @4 holds her; the Zombie appearing, the hunt @3
     * pre-empts the running Jealousy (HEAD's hunt @5 never could); a fresh Girlfriend with both present picks the Zombie.
     */
    private static void girlfriendHuntAheadOfJealousy(GameTestHelper helper) {
        final String finding = "ENT-S-130";
        Mob girlfriend = null;
        Mob second = null;
        Mob rival = null;
        Mob zombie = null;
        ServerPlayer owner = null;
        try {
            owner = survivalServerPlayerAt(helper, helper.absoluteVec(OWNER_POS));
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS, owner);
            helper.assertTrue(priorityOfType(girlfriend, Mob.class) == 3, "Girlfriend.registerGoals (orig Girlfriend.java:167): the IMob hunt sits at target priority 3,"
                    + " ahead of the Jealousy tasks @4 / @5 (" + finding + "); found " + priorityOfType(girlfriend, Mob.class));
            replaceRandom(girlfriend, rolls(3, 0, 8, 0));
            rival = spawnPrey(helper, ModEntities.GIRLFRIEND.get(), RIVAL_POS);
            helper.assertTrue(rival instanceof TamableAnimal t && !t.isTame(), "precondition: the rival Girlfriend is wild (" + finding + " test setup)");
            girlfriend.getSensing().tick();
            assertSees(helper, girlfriend, rival, "the rival Girlfriend 4 blocks east", finding);
            girlfriend.targetSelector.tick();
            helper.assertTrue(girlfriend.getTarget() == rival, "control: with the untamed rival alone the Jealousy goal (orig Girlfriend.java:170, @4) holds her ("
                    + finding + "); slot " + describe(girlfriend.getTarget()));
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            girlfriend.getSensing().tick();
            assertSees(helper, girlfriend, zombie, "a Zombie 8 blocks east", finding);
            girlfriend.targetSelector.tick();
            helper.assertTrue(girlfriend.getTarget() == zombie, "orig Girlfriend.java:167 / :170 — the IMob hunt @3 pre-empts the running Jealousy @4: a monster"
                    + " within reach beats an untamed rival near the owner; HEAD's hunt @5 was pre-empted by Jealousy @4 and tied with @5 (" + finding + "); slot "
                    + describe(girlfriend.getTarget()));
            second = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS, owner);
            replaceRandom(second, rolls(3, 0, 8, 0));
            second.getSensing().tick();
            second.targetSelector.tick();
            helper.assertTrue(second.getTarget() == zombie, "orig Girlfriend.java:161-174 — both eligible in one pass, the hunt @3 wins over Jealousy @4: the Zombie,"
                    + " not the rival (" + finding + "); slot " + describe(second.getTarget()));
        } finally {
            discardQuietly(zombie);
            discardQuietly(rival);
            discardQuietly(second);
            discardQuietly(girlfriend);
            removePlayer(helper, owner);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-133 — orig Robot2.java:281-284, Robot3.java:242-245, Robot4.java:282-285, Robot5.java:214-217: the 1-in-50 BEFORE the read
    // ------------------------------------------------------------------

    /**
     * A Zombie attacker held by the revenge goal, 8 blocks east in full sight and within every robot's engage range (Robot2's body
     * cone and Robot4's head cone at the target: yaw -90 puts the +x facing on it; Robot2 out of its melee reach so the control
     * swings at nothing and grieves nothing). The forgetting pass (the 1-in-50 pinned to fire) rolls BEFORE it reads the slot
     * (orig), so it reads null, its scan refuses the Zombie (a Monster, orig :309 / :373 / :283 / EntityMob → false) and engages
     * nothing: the robot's yaw stands (no lookAt), no path, the attack pose 0, no LaserBall — HEAD read the Zombie first and engaged
     * it once more. The control (the 1-in-50 pinned quiet) engages it: the yaw turned toward it, a path or a shot.
     */
    private static void robotForgetEngagesNothing(GameTestHelper helper, EntityType<? extends Mob> type, Class<?> cls, int[] quiet, int[] fire,
                                                  String cite, String finding) {
        Mob robot = null;
        Mob zombie = null;
        try {
            robot = spawnWithGoals(helper, type, HUNTER_POS);
            String name = robot.getClass().getSimpleName();
            robot.tickCount = TICKS_ALIVE;
            robot.setYRot(-90.0f);
            robot.setYHeadRot(-90.0f);
            robot.yBodyRot = -90.0f;
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            robot.getSensing().tick();
            assertSees(helper, robot, zombie, "the Zombie 8 blocks east", finding);
            Goal revenge = (Goal) readField(robot, cls, "revengeGoal");
            robot.setLastHurtByMob(zombie); // primed by name (the ENT-S-141 idiom): the robots' own hurt handlers path toward a Mob attacker, and Robot4's 65-tick gate refuses a second hit
            helper.assertTrue(robot.getLastHurtByMob() == zombie, "precondition: the Zombie is the primed attacker (" + finding + " test setup)");
            helper.assertTrue(revenge.canUse(), "precondition: the revenge goal takes the Zombie (" + finding + " test setup)");
            revenge.start();
            helper.assertTrue(robot.getTarget() == zombie, "precondition: the revenge occupant (" + finding + " test setup)");
            robot.getNavigation().stop();
            robot.setYRot(-90.0f);
            robot.setYHeadRot(-90.0f);
            int lasersBefore = helper.getEntities(ModEntities.LASER_BALL.get()).size();
            // the forgetting pass: rolled ahead of the read, nothing engaged
            writeReloadTicker(robot, cls);
            replaceRandom(robot, rolls(fire));
            invokeCustomServerAiStep(robot);
            helper.assertTrue(robot.getTarget() == null, "orig " + cite + " — nextInt(50) == 1 clears the attack target (" + finding + "); slot " + describe(robot.getTarget()));
            helper.assertTrue(robot.getYRot() == -90.0f, name + " (orig " + cite + " — the 1-in-50 BEFORE e = getAttackTarget()): the forgetting pass reads null and"
                    + " engages nothing — no lookAt turned it; HEAD read the Zombie first and looked at it (" + finding + "); yaw " + robot.getYRot());
            helper.assertTrue(robot.getNavigation().getPath() == null || robot.getNavigation().isDone(), name + " (orig " + cite + "): no path toward the forgotten"
                    + " attacker (" + finding + ")");
            helper.assertTrue(attacking(robot) == 0, name + " (orig " + cite + "): the attack pose stays 0 in the forgetting pass (" + finding + "); read " + attacking(robot));
            int lasersAfter = helper.getEntities(ModEntities.LASER_BALL.get()).size();
            helper.assertTrue(lasersAfter == lasersBefore, name + " (orig " + cite + "): no laser is fired at the forgotten attacker (" + finding + "); lasers "
                    + lasersBefore + " -> " + lasersAfter);
            helper.assertTrue(!revenge.canContinueToUse() && robot.getTarget() == null, "orig " + cite + " — the nulled target is final (ENT-S-131) (" + finding + ")");
            // the control: the same slot, the 1-in-50 quiet — the pass engages the Zombie
            robot.tickCount = TICKS_ALIVE + 10;
            robot.setLastHurtByMob(zombie); // a fresh timestamp for the goal's memory
            helper.assertTrue(revenge.canUse(), "precondition: the revenge goal takes the Zombie again (" + finding + " test setup)");
            revenge.start();
            helper.assertTrue(robot.getTarget() == zombie, "precondition: the revenge occupant, again (" + finding + " test setup)");
            robot.getNavigation().stop();
            robot.setYRot(-90.0f);
            robot.setYHeadRot(-90.0f);
            robot.getSensing().tick();
            writeReloadTicker(robot, cls);
            replaceRandom(robot, rolls(quiet));
            invokeCustomServerAiStep(robot);
            boolean looked = robot.getYRot() != -90.0f;
            boolean pathed = robot.getNavigation().getPath() != null && !robot.getNavigation().isDone();
            boolean posed = attacking(robot) == 1;
            boolean fired = helper.getEntities(ModEntities.LASER_BALL.get()).size() > lasersBefore;
            helper.assertTrue(robot.getTarget() == zombie && (looked || pathed || posed || fired), "control: with the 1-in-50 pinned quiet the pass reads the held"
                    + " Zombie and engages it (" + finding + "); slot " + describe(robot.getTarget()) + ", looked=" + looked + ", pathed=" + pathed + ", posed=" + posed
                    + ", fired=" + fired);
        } finally {
            for (Entity laser : helper.getEntities(ModEntities.LASER_BALL.get())) discardQuietly(laser);
            discardQuietly(zombie);
            discardQuietly(robot);
        }
    }

    private static void writeReloadTicker(Mob robot, Class<?> cls) {
        if (robot instanceof Robot2) return; // Robot2's pass has no reload gate (orig :279 — the 1-in-6 alone)
        writeField(robot, cls, "reloadTicker", 0);
    }

    private static int attacking(Mob robot) {
        return (Integer) invoke(robot, robot.getClass(), "getAttacking", new Class<?>[0]);
    }

    // ------------------------------------------------------------------
    // ENT-S-134 — orig Stinky.java:617-631: the flight box around an owner
    // ------------------------------------------------------------------

    private enum StinkyCase {
        /** orig :621-623 — the owner on the ground: nextInt(4) + 6 (pinned 2 → 8); y nextInt(6) - 2 (pinned 2 → 0). */
        OWNER_GROUNDED(true, 0, 8, 0, "Stinky.java:621-623", "nextInt(4) + 6"),
        /** orig :624-626 — the owner flying: nextInt(8) (pinned 3 → 3); y nextInt(6 + 2) - 2 (the same bound, pinned 3 → +1). */
        OWNER_FLYING(true, 1, 3, 1, "Stinky.java:624-626", "nextInt(8)"),
        /** orig :628-631 — no owner: nextInt(5) + 6 (pinned 4 → 10); y nextInt(6) - 2 (pinned 2 → 0). */
        WILD(false, 0, 10, 0, "Stinky.java:628-631", "nextInt(5) + 6");

        final boolean tame;
        final int ownerFlying;
        final int expectedXz;
        final int expectedY;
        final String cite;
        final String dice;

        StinkyCase(boolean tame, int ownerFlying, int expectedXz, int expectedY, String cite, String dice) {
            this.tame = tame;
            this.ownerFlying = ownerFlying;
            this.expectedXz = expectedXz;
            this.expectedY = expectedY;
            this.cite = cite;
            this.dice = dice;
        }
    }

    /**
     * The private {@code doMovement} in activity 2 with the 1-in-300 retarget pinned to fire, the 1-in-7 attack pass quiet, both
     * sign rolls positive and every box bound pinned (4 → 2, 8 → 3, 5 → 4, the y rolls 6 → 2 / 8 → 3): the flight target is written
     * (orig :638, before the test — ENT-S-126) at the branch's offset from the owner's truncated cell (:618-620) or, wild, from the
     * Stinky's own. HEAD rolled nextInt(5) + 6 for every Stinky (pinned 4 → 10).
     */
    private static void stinkyFlightBox(GameTestHelper helper, StinkyCase c) {
        final String finding = "ENT-S-134";
        Mob stinky = null;
        ServerPlayer owner = null;
        try {
            stinky = spawnFrozen(helper, ModEntities.ENTITY_STINKY.get(), HUNTER_POS);
            EntityStinky pet = (EntityStinky) stinky;
            int gox;
            int goy;
            int goz;
            if (c.tame) {
                owner = survivalServerPlayerAt(helper, helper.absoluteVec(OWNER_POS));
                pet.setTame(true, false);
                pet.setOwnerUUID(owner.getUUID());
                helper.assertTrue(pet.isTame() && pet.getOwner() == owner, "precondition: the Stinky is tame and finds its owner on the player list (" + finding
                        + " test setup)");
                gox = (int) owner.getX();
                goy = (int) owner.getY();
                goz = (int) owner.getZ();
            } else {
                helper.assertTrue(!pet.isTame(), "precondition: a wild Stinky (" + finding + " test setup)");
                gox = (int) pet.getX();
                goy = (int) pet.getY();
                goz = (int) pet.getZ();
            }
            writeField(pet, EntityStinky.class, "ownerFlying", c.ownerFlying);
            pet.setActivity(2);
            writeField(pet, EntityStinky.class, "currentFlightTarget", new BlockPos(0, 0, 0));
            replaceRandom(pet, rolls(300, 0, 7, 0, 4, 2, 8, 3, 5, 4, 2, 1, 6, 2));
            invoke(pet, EntityStinky.class, "doMovement", new Class<?>[0]);
            BlockPos after = (BlockPos) readField(pet, EntityStinky.class, "currentFlightTarget");
            BlockPos expected = new BlockPos(gox + c.expectedXz, goy + c.expectedY, goz + c.expectedXz);
            helper.assertTrue(expected.equals(after), "EntityStinky.doMovement (orig " + c.cite + " — " + c.dice + " on x and z" + (c.tame ? " around the owner's"
                    + " truncated cell (:618-620)" : " around its own cell") + "): the flight target is " + expected + " under the pinned dice; HEAD rolled the ownerless"
                    + " nextInt(5) + 6 (pinned 4 → 10) for every Stinky (" + finding + "); got " + after);
        } finally {
            discardQuietly(stinky);
            removePlayer(helper, owner);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-137 — orig MyEntityAINearestAttackableTarget.java:44-52: untamed / sitting refusals on both hunts
    // ------------------------------------------------------------------

    /**
     * An untamed Boyfriend refuses a Zombie (the IMob task) and a Creeper (the Creeper task) in sight; tamed, both take — the IMob hunt's
     * pick the Creeper (its sorter halves a creeper's distance²: 68 / 2 = 34 beats the Zombie's 64 — ENT-S-139, s139_82), the Creeper task's the
     * Creeper; the gate lifted is the canUse flip on the same geometry.
     */
    private static void boyfriendUntamedGate(GameTestHelper helper) {
        final String finding = "ENT-S-137";
        Mob boyfriend = null;
        Mob zombie = null;
        Mob creeper = null;
        try {
            boyfriend = spawnCompanion(helper, ModEntities.BOYFRIEND.get(), HUNTER_POS, null);
            NearestAttackableTargetGoal<?> imob = goalOfType(helper, boyfriend, Mob.class, finding);
            NearestAttackableTargetGoal<?> creeperGoal = goalOfType(helper, boyfriend, net.minecraft.world.entity.monster.Creeper.class, finding);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            creeper = spawnPrey(helper, EntityType.CREEPER, PREY_POS.south(2));
            boyfriend.getSensing().tick();
            assertSees(helper, boyfriend, zombie, "a Zombie 8 blocks east", finding);
            assertSees(helper, boyfriend, creeper, "a Creeper 8 blocks east", finding);
            helper.assertTrue(!((TamableAnimal) boyfriend).isTame(), "precondition: a freshly spawned Boyfriend is untamed (" + finding + " test setup)");
            boolean canImob = imob.canUse();
            boolean canCreeper = creeperGoal.canUse();
            helper.assertTrue(!canImob && goalTarget(imob) == null, "Boyfriend's IMob hunt (orig Boyfriend.java:141 -> MyEntityAINearestAttackableTarget.java:44-46:"
                    + " taskOwner instanceof EntityTameable && !isTamed() → false, ahead of the chance roll :53 and the scan :56): an untamed Boyfriend hunts no Zombie;"
                    + " HEAD's registration gated on PlayNicely alone and hunted from spawn (" + finding + "); canUse=" + canImob + ", pick " + describe(goalTarget(imob)));
            helper.assertTrue(!canCreeper && goalTarget(creeperGoal) == null, "Boyfriend's Creeper task (orig Boyfriend.java:138, the same :44-46 gate): an untamed"
                    + " Boyfriend hunts no Creeper (" + finding + "); canUse=" + canCreeper + ", pick " + describe(goalTarget(creeperGoal)));
            ((TamableAnimal) boyfriend).setTame(true, false);
            boyfriend.getSensing().tick();
            canImob = imob.canUse();
            helper.assertTrue(canImob && goalTarget(imob) == creeper, "control: tamed, the IMob hunt takes — its pick the Creeper, the sorter's first: orig"
                    + " MyEntityAINearestAttackableTargetSorter.java:23-25 halves a creeper's distance² (the Creeper 8 east, 2 south at 68 / 2 = 34 beats the Zombie"
                    + " 8 east at 64) and orig MyEntityAITarget.java:111 grants a Creeper ahead of the reach block (ENT-S-139, pinned s139_82); the gate lifted is"
                    + " the canUse flip on the same geometry (" + finding + "); canUse=" + canImob + ", pick " + describe(goalTarget(imob)));
            canCreeper = creeperGoal.canUse();
            helper.assertTrue(canCreeper && goalTarget(creeperGoal) == creeper, "control: tamed, the Creeper task takes the Creeper (" + finding + "); canUse="
                    + canCreeper + ", pick " + describe(goalTarget(creeperGoal)));
        } finally {
            discardQuietly(creeper);
            discardQuietly(zombie);
            discardQuietly(boyfriend);
        }
    }

    /** orig :50-52 names the Girlfriend alone: a tamed Boyfriend ordered to sit still hunts the Zombie. */
    private static void boyfriendSittingHunts(GameTestHelper helper) {
        final String finding = "ENT-S-137";
        Mob boyfriend = null;
        Mob zombie = null;
        try {
            boyfriend = spawnCompanion(helper, ModEntities.BOYFRIEND.get(), HUNTER_POS, null);
            ((TamableAnimal) boyfriend).setTame(true, false);
            ((TamableAnimal) boyfriend).setOrderedToSit(true);
            NearestAttackableTargetGoal<?> imob = goalOfType(helper, boyfriend, Mob.class, finding);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            boyfriend.getSensing().tick();
            assertSees(helper, boyfriend, zombie, "a Zombie 8 blocks east", finding);
            boolean can = imob.canUse();
            helper.assertTrue(can && goalTarget(imob) == zombie, "Boyfriend's IMob hunt (orig MyEntityAINearestAttackableTarget.java:50-52 — the sitting refusal tests"
                    + " `taskOwner instanceof Girlfriend` alone): a tamed Boyfriend ordered to sit still takes the Zombie (" + finding + "); canUse=" + can + ", pick "
                    + describe(goalTarget(imob)));
        } finally {
            discardQuietly(zombie);
            discardQuietly(boyfriend);
        }
    }

    /**
     * An untamed Girlfriend refuses a Zombie and a Creeper in sight on both hunts (orig :44-49, the two tame tests); tamed and standing,
     * the IMob hunt takes — its pick the Creeper (the sorter's halved 34 beats the Zombie's 64 — ENT-S-139, s139_82).
     */
    private static void girlfriendUntamedGate(GameTestHelper helper) {
        final String finding = "ENT-S-137";
        Mob girlfriend = null;
        Mob zombie = null;
        Mob creeper = null;
        try {
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS, null);
            NearestAttackableTargetGoal<?> imob = goalOfType(helper, girlfriend, Mob.class, finding);
            NearestAttackableTargetGoal<?> creeperGoal = goalOfType(helper, girlfriend, net.minecraft.world.entity.monster.Creeper.class, finding);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            creeper = spawnPrey(helper, EntityType.CREEPER, PREY_POS.south(2));
            girlfriend.getSensing().tick();
            assertSees(helper, girlfriend, zombie, "a Zombie 8 blocks east", finding);
            assertSees(helper, girlfriend, creeper, "a Creeper 8 blocks east, 2 south", finding);
            helper.assertTrue(!((TamableAnimal) girlfriend).isTame(), "precondition: a freshly spawned Girlfriend is untamed (" + finding + " test setup)");
            boolean canImob = imob.canUse();
            boolean canCreeper = creeperGoal.canUse();
            helper.assertTrue(!canImob && goalTarget(imob) == null, "Girlfriend's IMob hunt (orig Girlfriend.java:167 -> MyEntityAINearestAttackableTarget.java:44-49):"
                    + " an untamed Girlfriend hunts no Zombie; HEAD hunted from spawn (" + finding + "); canUse=" + canImob + ", pick " + describe(goalTarget(imob)));
            helper.assertTrue(!canCreeper && goalTarget(creeperGoal) == null, "Girlfriend's Creeper task (orig Girlfriend.java:164, the same gates): an untamed"
                    + " Girlfriend hunts no Creeper (" + finding + "); canUse=" + canCreeper + ", pick " + describe(goalTarget(creeperGoal)));
            ((TamableAnimal) girlfriend).setTame(true, false);
            girlfriend.getSensing().tick();
            canImob = imob.canUse();
            helper.assertTrue(canImob && goalTarget(imob) == creeper, "control: tamed and standing, the IMob hunt takes — its pick the Creeper, the sorter's first:"
                    + " orig MyEntityAINearestAttackableTargetSorter.java:23-25 halves a creeper's distance² (the Creeper 8 east, 2 south at 68 / 2 = 34 beats the"
                    + " Zombie 8 east at 64) and orig MyEntityAITarget.java:111 grants a Creeper ahead of the reach block (ENT-S-139, pinned s139_82); the gate"
                    + " lifted is the canUse flip on the same geometry (" + finding + "); canUse=" + canImob + ", pick " + describe(goalTarget(imob)));
        } finally {
            discardQuietly(creeper);
            discardQuietly(zombie);
            discardQuietly(girlfriend);
        }
    }

    /** orig :50-52 — a tamed Girlfriend ordered to sit refuses the Zombie on both hunts; standing again, she takes it. */
    private static void girlfriendSittingGate(GameTestHelper helper) {
        final String finding = "ENT-S-137";
        Mob girlfriend = null;
        Mob zombie = null;
        try {
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS, null);
            ((TamableAnimal) girlfriend).setTame(true, false);
            ((TamableAnimal) girlfriend).setOrderedToSit(true);
            NearestAttackableTargetGoal<?> imob = goalOfType(helper, girlfriend, Mob.class, finding);
            NearestAttackableTargetGoal<?> creeperGoal = goalOfType(helper, girlfriend, net.minecraft.world.entity.monster.Creeper.class, finding);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            girlfriend.getSensing().tick();
            assertSees(helper, girlfriend, zombie, "a Zombie 8 blocks east", finding);
            boolean can = imob.canUse();
            helper.assertTrue(!can && goalTarget(imob) == null, "Girlfriend's IMob hunt (orig MyEntityAINearestAttackableTarget.java:50-52 — taskOwner instanceof Girlfriend"
                    + " && isSitting() → false): a tamed Girlfriend ordered to sit hunts no Zombie; HEAD's hunt read no sitting state (" + finding + "); canUse=" + can
                    + ", pick " + describe(goalTarget(imob)));
            boolean canCreeper = creeperGoal.canUse();
            helper.assertTrue(!canCreeper, "Girlfriend's Creeper task (orig :50-52, the same gate): a sitting Girlfriend refuses (" + finding + "); canUse=" + canCreeper);
            ((TamableAnimal) girlfriend).setOrderedToSit(false);
            girlfriend.getSensing().tick();
            can = imob.canUse();
            helper.assertTrue(can && goalTarget(imob) == zombie, "control: standing again, the IMob hunt takes the Zombie (" + finding + "); canUse=" + can + ", pick "
                    + describe(goalTarget(imob)));
        } finally {
            discardQuietly(zombie);
            discardQuietly(girlfriend);
        }
    }

    /**
     * MyValentineTarget.java:47-59 carries none of the :44-52 refusals: under the Feb-14 clock seam an UNTAMED Girlfriend's Valentine
     * Player task takes the survival player 8 blocks east while her IMob hunt, on the same untamed Girlfriend, refuses the Zombie.
     */
    private static void girlfriendValentineUngated(GameTestHelper helper) {
        final String finding = "ENT-S-137";
        Mob girlfriend = null;
        Mob zombie = null;
        ServerPlayer player = null;
        SeasonalDates.setClockForTesting(() -> VALENTINES);
        try {
            helper.assertTrue(SeasonalDates.isValentines(), "precondition: the Feb-14 clock seam is in place (" + finding + " test setup)");
            girlfriend = spawnCompanion(helper, ModEntities.GIRLFRIEND.get(), HUNTER_POS, null);
            helper.assertTrue(girlfriend instanceof Girlfriend gf && gf.isValentineAngry() && !gf.isTame(), "precondition: an untamed Girlfriend spawned on Feb 14"
                    + " is valentine-angry (orig Girlfriend.java:569-574) (" + finding + " test setup)");
            NearestAttackableTargetGoal<?> playerGoal = goalOfType(helper, girlfriend, Player.class, finding);
            NearestAttackableTargetGoal<?> imob = goalOfType(helper, girlfriend, Mob.class, finding);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS.south(2));
            girlfriend.getSensing().tick();
            boolean can = playerGoal.canUse();
            helper.assertTrue(can && goalTarget(playerGoal) == player, "Girlfriend's ValentineTargetGoal<Player> (orig Girlfriend.java:161 -> MyValentineTarget.java:47-59:"
                    + " no tame or sitting refusal): the untamed Girlfriend's Valentine task takes the survival player (" + finding + "); canUse=" + can + ", pick "
                    + describe(goalTarget(playerGoal)));
            boolean canImob = imob.canUse();
            helper.assertTrue(!canImob, "the same untamed Girlfriend's IMob hunt refuses the Zombie beside him (orig :44-49) (" + finding + "); canUse=" + canImob);
        } finally {
            discardQuietly(zombie);
            removePlayer(helper, player);
            discardQuietly(girlfriend);
            SeasonalDates.resetClock();
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-138 — the (int) cast at a fractional negative y: the cell the cast reads is one above the floor's
    // ------------------------------------------------------------------

    /**
     * The mob at a fractional negative y (rel 1.5 over the floor): its cast cell is one ABOVE its floor cell, and on x / z the two agree —
     * the position sits on the integer lattice, so the cast and the floor read the same column on any origin (the harness's x / z are
     * random per run; at a block-centre .5 a negative origin on either axis shifted the cast cell there too).
     */
    private static void assertFractionalNegativeY(GameTestHelper helper, Entity mob, String finding) {
        double y = mob.getY();
        helper.assertTrue(y < 0.0 && y != Math.floor(y), "precondition: the mob stands at a fractional NEGATIVE y (the harness grid sits below y 0; rel 1.5 over the"
                + " floor) so the (int) cast and the floor read different cells (" + finding + " test geometry); y " + y);
        BlockPos cast = AmbientFlightGoal.castCell((Mob) mob);
        BlockPos floor = mob.blockPosition();
        helper.assertTrue(cast.getX() == floor.getX() && cast.getZ() == floor.getZ(), "precondition: the cast cell " + cast + " and blockPosition() " + floor
                + " agree on x and z — an integer-valued x / z casts and floors alike on any origin, so y alone discriminates (" + finding + " test geometry); at x "
                + mob.getX() + ", z " + mob.getZ());
        helper.assertTrue(cast.getY() == floor.getY() + 1, "precondition: the cast cell " + cast + " sits one above blockPosition() " + floor
                + " (" + finding + " test geometry)");
    }

    /**
     * Brutalfly (orig Brutalfly.java:174 — {@code getDistanceSquared((int) posX, (int) posY, (int) posZ) < 9.0f}): the flight target
     * parked 2 east, 2 south on the CAST cell's level is distSq 8 from it (near: the retarget fires and rewrites the target under the
     * pinned loop — 8 east, 8 south) and 9 from the floor cell (HEAD: not near); parked one lower it is 9 from the cast cell (no
     * retarget: the target stands) and 8 from the floor cell (HEAD retargeted).
     */
    private static void brutalflySelfCellCast(GameTestHelper helper) {
        final String finding = "ENT-S-138";
        Mob fly = null;
        try {
            fly = spawnFrozen(helper, ModEntities.ENTITY_BRUTALFLY.get(), FRACTIONAL_POS);
            assertFractionalNegativeY(helper, fly, finding);
            BlockPos cast = AmbientFlightGoal.castCell(fly);
            BlockPos near = cast.offset(2, 0, 2);
            BlockPos far = cast.offset(2, -1, 2);
            helper.assertTrue(near.distSqr(cast) == 8.0 && near.distSqr(fly.blockPosition()) == 9.0 && far.distSqr(cast) == 9.0 && far.distSqr(fly.blockPosition()) == 8.0,
                    "precondition: the parked cells discriminate the cast cell (8 / 9) from the floor cell (9 / 8) against orig's < 9.0f (" + finding + " test geometry)");
            writeField(fly, EntityBrutalfly.class, "currentFlightTarget", near);
            replaceRandom(fly, rolls(200, 1, 6, 1, 3, 1, 2, 1, 20, 0, 7, 1));
            invokeCustomServerAiStep(fly);
            BlockPos after = (BlockPos) readField(fly, EntityBrutalfly.class, "currentFlightTarget");
            helper.assertTrue(!after.equals(near), "EntityBrutalfly.customServerAiStep (orig Brutalfly.java:174 — the own cell read with (int) casts, BUG-027): a flight"
                    + " target at distSq 8 from the CAST cell is near (< 9.0f) and the retarget rewrites it; HEAD's blockPosition() read it 9 off and stood (" + finding
                    + "); target " + near + " -> " + after);
            writeField(fly, EntityBrutalfly.class, "currentFlightTarget", far);
            replaceRandom(fly, rolls(200, 1, 6, 1, 3, 1, 2, 1, 20, 0, 7, 1));
            invokeCustomServerAiStep(fly);
            after = (BlockPos) readField(fly, EntityBrutalfly.class, "currentFlightTarget");
            helper.assertTrue(after.equals(far), "orig Brutalfly.java:174 — a flight target at distSq 9 from the CAST cell is not near: no retarget, the target stands;"
                    + " HEAD's floor cell read it 8 off and retargeted (" + finding + "); target " + far + " -> " + after);
        } finally {
            discardQuietly(fly);
        }
    }

    /** Mothra (orig Mothra.java:184 — the same read against &lt; 9.0f; the loop pinned to 8 east, 8 south on the cast level). */
    private static void mothraSelfCellCast(GameTestHelper helper) {
        final String finding = "ENT-S-138";
        Mob mothra = null;
        try {
            helper.assertTrue(!OreSpawnConfig.MOTHRA_PEACEFUL.get(), "precondition: mothraPeaceful is off (its default; never flipped) (" + finding + " test setup)");
            mothra = spawnFrozen(helper, ModEntities.MOTHRA.get(), FRACTIONAL_POS);
            assertFractionalNegativeY(helper, mothra, finding);
            BlockPos cast = AmbientFlightGoal.castCell(mothra);
            BlockPos near = cast.offset(2, 0, 2);
            BlockPos far = cast.offset(2, -1, 2);
            writeField(mothra, Mothra.class, "currentFlightTarget", near);
            replaceRandom(mothra, rolls(300, 1, 10, 1, 3, 1, 2, 1, 20, 0, 7, 1));
            invokeCustomServerAiStep(mothra);
            BlockPos after = (BlockPos) readField(mothra, Mothra.class, "currentFlightTarget");
            helper.assertTrue(!after.equals(near), "Mothra.customServerAiStep (orig Mothra.java:184 — the own cell read with (int) casts, BUG-027): a flight target at"
                    + " distSq 8 from the CAST cell is near (< 9.0f) and the retarget rewrites it; HEAD's blockPosition() read it 9 off and stood (" + finding
                    + "); target " + near + " -> " + after);
            writeField(mothra, Mothra.class, "currentFlightTarget", far);
            replaceRandom(mothra, rolls(300, 1, 10, 1, 3, 1, 2, 1, 20, 0, 7, 1));
            invokeCustomServerAiStep(mothra);
            after = (BlockPos) readField(mothra, Mothra.class, "currentFlightTarget");
            helper.assertTrue(after.equals(far), "orig Mothra.java:184 — a flight target at distSq 9 from the CAST cell is not near: the target stands; HEAD's floor"
                    + " cell read it 8 off and retargeted (" + finding + "); target " + far + " -> " + after);
        } finally {
            discardQuietly(mothra);
        }
    }

    /**
     * The Dragonfly's flight goal (AmbientFlightGoal.tick, orig Dragonfly.java:124 — {@code < 2.1f} on the cast cell): a flight target
     * at distSq 2 from the cast cell (3 from the floor cell) retargets and the else-branch hunt, pinned to fire, bites nothing; one
     * lower (3 from the cast cell, 2 from the floor cell) the retarget is skipped and the butterfly 2 blocks east is bitten. The
     * ScanSetParityTests row s135_43 at an integer y, re-derived at the fractional negative one.
     */
    private static void dragonflyFlightCellCast(GameTestHelper helper) {
        final String finding = "ENT-S-138";
        Mob fly = null;
        Mob butterfly = null;
        try {
            fly = spawnFrozen(helper, ModEntities.ENTITY_DRAGONFLY.get(), FRACTIONAL_POS);
            assertFractionalNegativeY(helper, fly, finding);
            butterfly = spawnPreyAt(helper, ModEntities.ENTITY_BUTTERFLY.get(), FRACTIONAL_ROW_TOUCH_POS); // on the hunter's lattice, 2 east on the floor
            DragonflyHuntGoal goal = (DragonflyHuntGoal) readField(fly, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(fly.distanceToSqr(butterfly) < 6.0, "precondition: the butterfly stands inside the bite reach distSq < 6 (orig :146) (" + finding + " test geometry)");
            assertSees(helper, fly, butterfly, "a butterfly 2 blocks east", finding);
            BlockPos cast = AmbientFlightGoal.castCell(fly);
            BlockPos near = cast.offset(1, 0, 1);
            BlockPos far = cast.offset(1, -1, 1);
            helper.assertTrue(near.distSqr(cast) == 2.0 && near.distSqr(fly.blockPosition()) == 3.0 && far.distSqr(cast) == 3.0 && far.distSqr(fly.blockPosition()) == 2.0,
                    "precondition: the parked cells discriminate the cast cell (2 / 3) from the floor cell (3 / 2) against orig's < 2.1f (" + finding + " test geometry)");
            float health = butterfly.getHealth();
            goal.setFlightTarget(near);
            replaceRandom(fly, rolls(300, 1, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() == health && fly.getTarget() == null, "AmbientFlightGoal.tick (orig Dragonfly.java:124 — the own cell read with"
                    + " (int) casts, BUG-027): a flight target at cell distSq 2 from the CAST cell is near (< 2.1f): the retarget, no hunt though the 1-in-12 is pinned to"
                    + " fire; HEAD's blockPosition() read it 3 off and hunted (" + finding + "); health " + butterfly.getHealth() + ", slot " + describe(fly.getTarget()));
            goal.setFlightTarget(far);
            replaceRandom(fly, rolls(300, 1, 12, 0));
            goal.tick();
            helper.assertTrue(butterfly.getHealth() < health, "orig Dragonfly.java:124 / :142 — a flight target at cell distSq 3 from the CAST cell is not near: the"
                    + " else branch's 1-in-12 hunts and the butterfly is bitten; HEAD's floor cell read it 2 off and retargeted (" + finding + "); health "
                    + butterfly.getHealth());
        } finally {
            discardQuietly(butterfly);
            discardQuietly(fly);
        }
    }

    /**
     * DragonflyHuntGoal.onRetargetSkipped (orig Dragonfly.java:145-146 — {@code set((int) posX, (int) (posY + 1.0), (int) posZ)}): the
     * prey at a fractional negative y (rel 1.5 over the floor): the cast reads {@code (int) (y + 1.0)}, one above the cell the cast reads
     * for y itself; {@code blockPosition().above()} read one lower. Kept as landed — its x / z assertions derive from the cast on both sides.
     */
    private static void dragonflyPreyCellCast(GameTestHelper helper) {
        final String finding = "ENT-S-138";
        Mob fly = null;
        Mob butterfly = null;
        try {
            fly = spawnFrozen(helper, ModEntities.ENTITY_DRAGONFLY.get(), HUNTER_POS);
            butterfly = spawnPreyAt(helper, ModEntities.ENTITY_BUTTERFLY.get(), FRACTIONAL_TOUCH_POS);
            assertFractionalNegativeY(helper, butterfly, finding);
            DragonflyHuntGoal goal = (DragonflyHuntGoal) readField(fly, EntityDragonfly.class, "huntGoal");
            assertSees(helper, fly, butterfly, "a butterfly 2 blocks east", finding);
            goal.setFlightTarget(AmbientFlightGoal.castCell(fly).above(10));
            replaceRandom(fly, rolls(300, 1, 12, 0));
            float health = butterfly.getHealth();
            goal.tick();
            helper.assertTrue(butterfly.getHealth() < health, "precondition: the hunt pass bites the butterfly (" + finding + " test setup)");
            BlockPos flightTarget = (BlockPos) readField(goal, AmbientFlightGoal.class, "flightTarget");
            int castY = (int) (butterfly.getY() + 1.0);
            int floorY = butterfly.blockPosition().above().getY();
            helper.assertTrue(castY != floorY, "precondition: the cast and the floor differ on the prey's y (" + finding + " test geometry); cast " + castY + ", floor " + floorY);
            helper.assertTrue(flightTarget.getY() == castY && flightTarget.getX() == (int) butterfly.getX() && flightTarget.getZ() == (int) butterfly.getZ(),
                    "DragonflyHuntGoal.onRetargetSkipped (orig Dragonfly.java:145-146 — (int) (posY + 1.0), BUG-027): the flight target is set on the prey's CAST cell,"
                    + " y " + castY + "; HEAD's blockPosition().above() gave " + floorY + " (" + finding + "); got " + flightTarget);
        } finally {
            discardQuietly(butterfly);
            discardQuietly(fly);
        }
    }

    /**
     * ButterflyIslandsHuntGoal.tick (orig EntityButterfly.java:152-154 — the base's :105 / :108 re-stated): a flight target at distSq 3
     * from the cast cell (6 from the floor cell) is near (&lt; 4.0f) — the retarget, pinned to the butterfly's own cast cell; one at
     * distSq 4 from the cast cell (1 from the floor cell) is not — the target stands.
     */
    private static void butterflyFlightCellCast(GameTestHelper helper) {
        final String finding = "ENT-S-138";
        Mob butterfly = null;
        try {
            butterfly = spawnWithGoalsAt(helper, ModEntities.ENTITY_BUTTERFLY.get(), FRACTIONAL_POS);
            assertFractionalNegativeY(helper, butterfly, finding);
            Goal goal = huntGoal(butterfly, finding);
            BlockPos cast = AmbientFlightGoal.castCell(butterfly);
            BlockPos near = cast.offset(1, 1, 1);
            BlockPos far = cast.offset(0, -2, 0);
            helper.assertTrue(near.distSqr(cast) == 3.0 && near.distSqr(butterfly.blockPosition()) == 6.0 && far.distSqr(cast) == 4.0 && far.distSqr(butterfly.blockPosition()) == 1.0,
                    "precondition: the parked cells discriminate the cast cell (3 / 4) from the floor cell (6 / 1) against orig's < 4.0f (" + finding + " test geometry)");
            helper.assertTrue(helper.getLevel().getBlockState(cast).isAir(), "precondition: the butterfly's cast cell is air — the pinned wander candidate (" + finding + " test geometry)");
            ((AmbientFlightGoal) goal).setFlightTarget(near);
            replaceRandom(butterfly, rolls(100, 1, 7, 0, 6, 2, 10, 1));
            goal.tick();
            BlockPos after = (BlockPos) readField(goal, AmbientFlightGoal.class, "flightTarget");
            helper.assertTrue(after.equals(cast), "ButterflyIslandsHuntGoal.tick (orig EntityButterfly.java:154 — the own cell read with (int) casts, BUG-027): a flight"
                    + " target at distSq 3 from the CAST cell is near (< 4.0f) and the retarget rewrites it to the pinned wander (the cast cell itself); HEAD's"
                    + " blockPosition() read it 6 off and stood (" + finding + "); target " + near + " -> " + after);
            ((AmbientFlightGoal) goal).setFlightTarget(far);
            replaceRandom(butterfly, rolls(100, 1, 7, 0, 6, 2, 10, 1));
            goal.tick();
            after = (BlockPos) readField(goal, AmbientFlightGoal.class, "flightTarget");
            helper.assertTrue(after.equals(far), "orig EntityButterfly.java:154 — a flight target at distSq 4 from the CAST cell is not near: the target stands;"
                    + " HEAD's floor cell read it 1 off and retargeted (" + finding + "); target " + far + " -> " + after);
        } finally {
            discardQuietly(butterfly);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-142 — orig EnderKnight.java:116-119: isWet || isBurning → scream off, teleportRandomly
    // ------------------------------------------------------------------

    /**
     * A frozen Ender mob with its feet on the floor and a water source in its feet cell: {@code aiStep()} once under the pinned
     * random (the daylight dice quiet, the offsets 4 east / 4 south, level) lands it 4 east and 4 south with the scream off; the
     * water gone and the mob dry, the same tick moves it nowhere.
     */
    private static void enderWetTeleport(GameTestHelper helper, EnderSite site) {
        final String finding = "ENT-S-142";
        Mob mob = null;
        boolean wet = false;
        try {
            mob = spawnWithGoals(helper, site.type().get(), FLOOR_POS);
            String name = mob.getClass().getSimpleName();
            helper.setBlock(FLOOR_POS, Blocks.WATER);
            wet = true;
            invoke(mob, Entity.class, "updateInWaterStateAndDoFluidPushing", new Class<?>[0]);
            helper.assertTrue(mob.isInWater() && mob.isInWaterRainOrBubble() && !mob.isOnFire(), "precondition: the " + name + " stands in water and not on fire (orig "
                    + site.origFile() + ":116 isWet) (" + finding + " test setup)");
            replaceRandom(mob, new PinnedRolls(RandomSource.create(1234L), RANDOM_TELEPORT_DOUBLE, QUIET_FLOAT, 64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
            setScreaming(mob, true);
            Vec3 start = mob.position();
            mob.aiStep();
            Vec3 expected = start.add(RANDOM_TELEPORT_OFFSET, 0.0, RANDOM_TELEPORT_OFFSET);
            helper.assertTrue(mob.position().distanceTo(expected) < POSITION_TOLERANCE, name + " (orig " + site.origFile() + ":116-119 — isWet() || isBurning() → teleportRandomly):"
                    + " a wet " + name + " blinks away — the pinned 4 east, 4 south of " + start + "; HEAD's Knight teleported on fire alone and stood in the water ("
                    + finding + "); at " + mob.position());
            helper.assertTrue(!isScreaming(mob), name + " (orig " + site.origFile() + ":117): the wet teleport sets the scream off (" + finding + ")");
            helper.setBlock(FLOOR_POS, Blocks.AIR);
            wet = false;
            invoke(mob, Entity.class, "updateInWaterStateAndDoFluidPushing", new Class<?>[0]);
            helper.assertTrue(!mob.isInWaterRainOrBubble() && !mob.isOnFire(), "precondition: dry and not on fire (" + finding + " test setup)");
            Vec3 landed = mob.position();
            mob.aiStep();
            helper.assertTrue(mob.position().equals(landed), "control: dry and not burning, the tick moves the " + name + " nowhere (" + finding + "); at " + mob.position());
        } finally {
            if (wet) helper.setBlock(FLOOR_POS, Blocks.AIR);
            discardQuietly(mob);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-143 — orig EntityLunaMoth.java:117-156: the moth's own loop
    // ------------------------------------------------------------------

    /** {@code Params.lunaMoth()} carries orig :126-155's numbers: range 10, y 6 / bias 2, steer 0.5 / 0.68, blend 0.1, forward 0.75, yaw 1, retarget 100, near 4.0, 25 tries. */
    private static void lunaMothNumbers(GameTestHelper helper) {
        final String finding = "ENT-S-143";
        Mob moth = null;
        try {
            moth = spawnWithGoals(helper, ModEntities.ENTITY_LUNA_MOTH.get(), HUNTER_POS);
            Goal goal = huntGoal(moth, finding);
            helper.assertTrue(goal instanceof LunaMothFlightGoal, "precondition: the moth's flight goal is its LunaMothFlightGoal (" + finding + " test setup); found "
                    + goal.getClass().getSimpleName());
            AmbientFlightGoal.Params p = (AmbientFlightGoal.Params) readField(goal, AmbientFlightGoal.class, "params");
            AmbientFlightGoal.Params expected = new AmbientFlightGoal.Params(10, 6, 2, 0.5, 0.68, 0.1, 0.75f, 1.0f, 100, 4.0, 25);
            helper.assertTrue(expected.equals(p), "LunaMothFlightGoal's preset (orig EntityLunaMoth.java:129 nextInt(10) - nextInt(10) / nextInt(6) - 2, :126 nextInt(100)"
                    + " / < 4.0f, :118 25 tries, :149-151 0.5 / 0.68 / 0.5 with 0.1f, :154 moveForward 0.75f): expected " + expected + "; HEAD flew the butterfly's row"
                    + " (7, 0.7, 0.5f) (" + finding + "); got " + p);
        } finally {
            discardQuietly(moth);
        }
    }

    private enum MothCase {
        /** Night, the sky open, a non-retarget tick, the 1-in-10 pinned to fire, a torch 5 east: sought (HEAD sought under cover, on a retarget tick alone). */
        NIGHT_OPEN_SKY(NIGHT_TIME, new BlockPos(25, 2, 24), false, true),
        /** Day, the same: never sought. */
        DAY(DAY_TIME, new BlockPos(25, 2, 24), false, false),
        /** Night, a torch 5 SOUTH — on the +z face of the shell, which HEAD's ±x scan never read. */
        PLUS_Z_FACE(NIGHT_TIME, new BlockPos(20, 2, 29), false, true),
        /** Night, the retarget pinned to fire: the wander (pinned to the moth's own cast cell), never the torch. */
        RETARGET_TICK(NIGHT_TIME, new BlockPos(25, 2, 24), true, false);

        final long dayTime;
        final BlockPos torch;
        final boolean retarget;
        final boolean sought;

        MothCase(long dayTime, BlockPos torch, boolean retarget, boolean sought) {
            this.dayTime = dayTime;
            this.torch = torch;
            this.retarget = retarget;
            this.sought = sought;
        }
    }

    /**
     * The moth's flight goal ticked once with the day time set (restored in the finally), a TORCH on a stone at the case's spot,
     * the flight target parked far (10 up) and the dice pinned: the retarget 100 → 1 (quiet) or 0 (fire), the 1-in-10 hunt / torch
     * roll → 0, the wander 10 → 0 and 6 → 2 (the moth's own cast cell). Sought: the flight target is the cell above the torch.
     */
    private static void lunaMothTorch(GameTestHelper helper, MothCase c) {
        final String finding = "ENT-S-143";
        ServerLevel level = helper.getLevel();
        final long priorTime = level.getDayTime();
        Mob moth = null;
        boolean placed = false;
        try {
            level.setDayTime(c.dayTime);
            level.updateSkyBrightness();
            helper.assertTrue(level.isDay() == (c.dayTime == DAY_TIME), "precondition: the level reads " + (c.dayTime == DAY_TIME ? "day" : "night") + " after setDayTime("
                    + c.dayTime + ") (" + finding + " test setup); isDay=" + level.isDay());
            moth = spawnWithGoals(helper, ModEntities.ENTITY_LUNA_MOTH.get(), HUNTER_POS);
            Goal goal = huntGoal(moth, finding);
            helper.setBlock(c.torch.below(), Blocks.STONE);
            helper.setBlock(c.torch, Blocks.TORCH);
            placed = true;
            BlockPos torchAbs = helper.absolutePos(c.torch);
            helper.assertTrue(level.getBlockState(torchAbs).is(Blocks.TORCH), "precondition: the torch stands at " + torchAbs + " (" + finding + " test setup)");
            BlockPos cast = AmbientFlightGoal.castCell(moth);
            BlockPos parked = cast.above(10);
            ((AmbientFlightGoal) goal).setFlightTarget(parked);
            replaceRandom(moth, rolls(100, c.retarget ? 0 : 1, 10, 0, 6, 2));
            goal.tick();
            BlockPos after = (BlockPos) readField(goal, AmbientFlightGoal.class, "flightTarget");
            BlockPos aboveTorch = torchAbs.above();
            switch (c) {
                case NIGHT_OPEN_SKY -> helper.assertTrue(after.equals(aboveTorch), "LunaMothFlightGoal.onRetargetSkipped (orig EntityLunaMoth.java:133-144 — the"
                        + " retarget's ELSE branch at night on a 1-in-10, the shells 2..14, the target above the nearest torch): under an OPEN sky at night, on a tick"
                        + " the retarget did not fire, the moth seeks the torch 5 east — the flight target " + aboveTorch + "; HEAD sought only inside the retarget"
                        + " under a covered sky (" + finding + "); got " + after);
                case DAY -> helper.assertTrue(after.equals(parked), "orig EntityLunaMoth.java:133 — !isDaytime(): by day the else branch seeks no torch though the"
                        + " 1-in-10 fires; the flight target stands (" + finding + "); got " + after + " (the torch's above " + aboveTorch + ")");
                case PLUS_Z_FACE -> helper.assertTrue(after.equals(aboveTorch), "orig EntityLunaMoth.java:96-113 (scan_it's ±z faces): a torch 5 SOUTH sits on the"
                        + " +z face of the radius-5 shell and is found — HEAD's findClosestTorch walked the ±x faces alone (" + finding + "); got " + after
                        + ", expected " + aboveTorch);
                case RETARGET_TICK -> helper.assertTrue(after.equals(cast) && !after.equals(aboveTorch), "orig EntityLunaMoth.java:126-132 — a retarget tick picks"
                        + " the wander (pinned to the moth's own cell " + cast + "), never the torch: the torch scan is the else branch's alone (" + finding + "); got "
                        + after);
            }
        } finally {
            if (placed) {
                helper.setBlock(c.torch, Blocks.AIR);
                helper.setBlock(c.torch.below(), Blocks.AIR);
            }
            discardQuietly(moth);
            level.setDayTime(priorTime);
            level.updateSkyBrightness();
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-144 — orig EnderKnight.java:203-204: "mob.endermen.portal" at the origin and at the landing
    // ------------------------------------------------------------------

    /**
     * A frozen Ender mob with its feet on the floor: {@code teleportRandomly} under the pinned offsets (4 east, 4 south, level) lands,
     * and the ear hears ENDERMAN_TELEPORT once at the origin and once at the landing; put back and asked for a spot 32 below the
     * floor, the landing search refuses (the walk starts below the world's bottom), the mob stands and nothing is heard.
     */
    private static void enderPortalSound(GameTestHelper helper, EnderSite site) {
        final String finding = "ENT-S-144";
        Mob mob = null;
        PortalEar ear = null;
        try {
            mob = spawnWithGoals(helper, site.type().get(), FLOOR_POS);
            String name = mob.getClass().getSimpleName();
            Vec3 start = mob.position();
            Vec3 expected = start.add(RANDOM_TELEPORT_OFFSET, 0.0, RANDOM_TELEPORT_OFFSET);
            ear = new PortalEar(start, expected);
            replaceRandom(mob, new PinnedRolls(RandomSource.create(1234L), RANDOM_TELEPORT_DOUBLE, QUIET_FLOAT, 64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
            boolean landed = (Boolean) invoke(mob, mob.getClass(), "teleportRandomly", new Class<?>[0]);
            helper.assertTrue(landed && mob.position().distanceTo(expected) < POSITION_TOLERANCE, "precondition: the pinned random teleport lands 4 east, 4 south ("
                    + finding + " test setup); landed=" + landed + ", at " + mob.position());
            helper.assertTrue(ear.originCount == 1, name + " (orig " + site.origFile() + ":203 — worldObj.playSoundEffect(d3, d4, d5, \"mob.endermen.portal\", 1, 1) at the"
                    + " origin read ahead of the move): ENDERMAN_TELEPORT heard once at the origin " + start + "; HEAD's randomTeleport mapping played nothing (" + finding
                    + "); heard " + ear.originCount);
            helper.assertTrue(ear.landingCount == 1, name + " (orig " + site.origFile() + ":204 — this.playSound(\"mob.endermen.portal\", 1, 1) at the entity, now at the"
                    + " landing): ENDERMAN_TELEPORT heard once at the landing " + expected + " (" + finding + "); heard " + ear.landingCount);
            mob.teleportTo(start.x, start.y, start.z);
            ear.reset();
            replaceRandom(mob, new PinnedRolls(RandomSource.create(1234L), RANDOM_TELEPORT_DOUBLE, QUIET_FLOAT, 64, REFUSED_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
            landed = (Boolean) invoke(mob, mob.getClass(), "teleportRandomly", new Class<?>[0]);
            helper.assertTrue(!landed && mob.position().distanceTo(start) < POSITION_TOLERANCE, "precondition: a spot 32 below the floor is refused and the origin"
                    + " restored (orig " + site.origFile() + ":188-191) (" + finding + " test setup); landed=" + landed + ", at " + mob.position());
            helper.assertTrue(ear.originCount == 0 && ear.landingCount == 0, name + " (orig " + site.origFile() + ":188-191 ahead of :203-204): a refused landing plays"
                    + " nothing (" + finding + "); heard " + ear.originCount + " / " + ear.landingCount);
        } finally {
            if (ear != null) ear.close();
            discardQuietly(mob);
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the sibling batches' idioms)
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey, String why, String finding) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: " + hunter.getClass().getSimpleName() + " (eye " + String.format("%.2f", hunter.getEyeHeight())
                + " above its feet) must see " + why + " (" + finding + " test geometry)");
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (a selector is under test) but no AI, so nothing runs by itself; on the ground. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setOnGround(true);
        return mob;
    }

    private static Mob spawnWithGoalsAt(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setOnGround(true);
        return mob;
    }

    /**
     * A companion with its goals, on the ground (the nearbyOnly reach cache paths through GroundPathNavigation.canUpdatePath — the
     * ScanSetParityTests idiom), its FOLLOW_RANGE raised to 40 so the path search reaches every probe; tamed with the owner when one
     * is given (ENT-S-137: the hunts refuse an untamed companion), else left as spawned — untamed.
     */
    private static Mob spawnCompanion(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos, ServerPlayer owner) {
        Mob mob = spawnWithGoals(helper, type, pos);
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40.0);
        if (owner != null && mob instanceof TamableAnimal tamable) {
            tamable.setTame(true, false);
            tamable.setOwnerUUID(owner.getUUID());
        }
        return mob;
    }

    /** Frozen prey with 1000 HP, so no pinned hit kills it. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    private static Mob spawnPreyAt(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
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

    /** The one {@code NearestAttackableTargetGoal} of the given target type on the selector (the ScanSetParityTests idiom). */
    private static NearestAttackableTargetGoal<?> goalOfType(GameTestHelper helper, Mob hunter, Class<?> targetType, String finding) {
        NearestAttackableTargetGoal<?> found = null;
        int count = 0;
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == targetType) {
                found = nearest;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + hunter.getClass().getSimpleName() + " carries exactly one NearestAttackableTargetGoal<"
                + targetType.getSimpleName() + "> on its target selector — found " + count + " (" + finding + " test setup)");
        return found;
    }

    /** The priority of the one NearestAttackableTargetGoal of the given target type (-1 if none), the Jealousy goals excluded. */
    private static int priorityOfType(Mob hunter, Class<?> targetType) {
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest && !(nearest instanceof JealousyTargetGoal<?>)
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == targetType) {
                return wrapped.getPriority();
            }
        }
        return -1;
    }

    /** The priority of the one goal whose class has the given simple name (-1 if none). */
    private static int priorityOfClass(Mob hunter, String simpleName) {
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal().getClass().getSimpleName().equals(simpleName)) {
                return wrapped.getPriority();
            }
        }
        return -1;
    }

    /** The butterfly's / moth's ButterflyIslandsHuntGoal off its goal selector (the MiscTargetingParityTests idiom). */
    private static Goal huntGoal(Mob flyer, String finding) {
        for (WrappedGoal wrapped : flyer.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof ButterflyIslandsHuntGoal) {
                return wrapped.getGoal();
            }
        }
        throw new IllegalStateException("precondition: " + flyer.getClass().getSimpleName() + " must carry a ButterflyIslandsHuntGoal on its goal selector ("
                + finding + " test setup)");
    }

    private static LivingEntity goalTarget(NearestAttackableTargetGoal<?> goal) {
        return (LivingEntity) readField(goal, NearestAttackableTargetGoal.class, "target");
    }

    private static boolean isScreaming(Mob hunter) {
        if (hunter instanceof EnderKnight knight) return knight.isScreaming();
        if (hunter instanceof EnderReaper reaper) return reaper.isScreaming();
        throw new IllegalStateException("not an Ender mob: " + hunter.getClass().getSimpleName());
    }

    private static void setScreaming(Mob hunter, boolean value) {
        if (hunter instanceof EnderKnight knight) knight.setScreaming(value);
        else if (hunter instanceof EnderReaper reaper) reaper.setScreaming(value);
        else throw new IllegalStateException("not an Ender mob: " + hunter.getClass().getSimpleName());
    }

    /** The hunter's protected customServerAiStep, declared on its own class, invoked once. */
    private static void invokeCustomServerAiStep(Mob hunter) {
        invoke(hunter, hunter.getClass(), "customServerAiStep", new Class<?>[0]);
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
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name + " (1.21.1: official names at runtime)", exception);
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

    /**
     * Every ENDERMAN_TELEPORT the level plays within {@link #EAR_RADIUS} of the origin or of the landing while open — the
     * MiscTargetingParityTests StareEar seam widened to the portal sound: orig :203's {@code playSoundEffect(d3, d4, d5, …)} and :204's
     * {@code playSound(…)} are the port's {@code Level.playSound(null, x, y, z, …)} at the origin and at the entity, which reach
     * {@code ServerLevel.playSeededSound} and its {@code PlayLevelSoundEvent.AtPosition} before the broadcast.
     */
    private static final class PortalEar {
        int originCount;
        int landingCount;
        private final Consumer<PlayLevelSoundEvent> listener;
        private boolean open;

        PortalEar(Vec3 origin, Vec3 landing) {
            this.listener = event -> {
                if (event instanceof PlayLevelSoundEvent.AtPosition at && event.getSound() != null && event.getSound().value() == SoundEvents.ENDERMAN_TELEPORT) {
                    if (at.getPosition().distanceTo(origin) < EAR_RADIUS) this.originCount++;
                    if (at.getPosition().distanceTo(landing) < EAR_RADIUS) this.landingCount++;
                }
            };
            NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.class, this.listener);
            this.open = true;
        }

        void reset() {
            this.originCount = 0;
            this.landingCount = 0;
        }

        void close() {
            if (this.open) {
                NeoForge.EVENT_BUS.unregister(this.listener);
                this.open = false;
            }
        }
    }

    /**
     * The MiscTargetingParityTests.TeleportRolls seam: {@code nextInt(bound)} answers the pinned value for the listed bounds (64 for
     * the random teleport's y, 16 for the toward teleport's) and delegates the rest; every {@code nextDouble()} answers one value
     * (the random teleport's x / z offsets); every {@code nextFloat()} answers one value (the daylight dice, pinned quiet).
     */
    private static final class PinnedRolls implements RandomSource {
        private final RandomSource delegate;
        private final Map<Integer, Integer> intAnswers = new TreeMap<>();
        private final double doubleAnswer;
        private final float floatAnswer;

        PinnedRolls(RandomSource delegate, double doubleAnswer, float floatAnswer, int... boundAnswerPairs) {
            this.delegate = delegate;
            this.doubleAnswer = doubleAnswer;
            this.floatAnswer = floatAnswer;
            for (int i = 0; i < boundAnswerPairs.length; i += 2) {
                this.intAnswers.put(boundAnswerPairs[i], boundAnswerPairs[i + 1]);
            }
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
            Integer answer = this.intAnswers.get(upper);
            return answer != null ? answer : this.delegate.nextInt(upper);
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
            return this.floatAnswer;
        }

        @Override
        public double nextDouble() {
            return this.doubleAnswer;
        }

        @Override
        public double nextGaussian() {
            return this.delegate.nextGaussian();
        }
    }
}
