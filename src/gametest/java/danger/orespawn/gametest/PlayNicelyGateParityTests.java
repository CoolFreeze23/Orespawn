package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.MobzillaSpawnTracker;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.Godzilla;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.Irukandji;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.SeaMonster;
import danger.orespawn.entity.Skate;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import danger.orespawn.entity.ai.PointysaurusStareGoal;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-115 (targeting ledger T1, wave 1): the 1.7.10 PlayNicely gates of the hunters' target selection,
 * restored at the orig positions and read live as {@code OreSpawnConfig.PLAY_NICELY} (orig read the static
 * {@code OreSpawnMain.PlayNicely != 0}; the port's convention since ENT-S-110 / BOSS-017). Four shapes:
 * <ul>
 *   <li>the scan gate — {@code if (PlayNicely != 0) return null;} at the head of {@code findSomethingToAttack}
 *       (or the port's renamed scan: the Frog's {@code findInsectTarget}, the Spider Driver's
 *       {@code findSpiderRobot}, the Dragonfly's {@code DragonflyHuntGoal.findPrey}, the Ant Robot's void stomp
 *       {@code feetFindSomethingToHit}); for the Irukandji, Skate and Sea Monster the orig scan method also
 *       held the stored-target read, so the port's inline pick is gated as a whole;</li>
 *   <li>the revenge blanking — orig {@code e = rt; if (PlayNicely != 0) e = null;}: the pass's copy of the
 *       revenge target is nulled, {@code rt} itself kept (Hammerhead; Nastysaurus and TRex in
 *       {@code selectTarget}, where the blanked pass skips the dead-drop and claims no ownership);</li>
 *   <li>the goal-registration gate — orig registered the target task only {@code if (PlayNicely == 0)} at
 *       construction; the port registers its {@code NearestAttackableTargetGoal} always and reads the flag
 *       live in {@code canUse}, so the goal never starts while PlayNicely is on (Leon, ThePrinceAdult,
 *       ThePrinceTeen, Boyfriend, Girlfriend) — the same predicate carries the scan gate of the hunters whose
 *       port targeting is a vanilla goal with no scan method (CaterKiller, EnderKnight, EnderReaper, SeaViper,
 *       Pointysaurus with its stare goal);</li>
 *   <li>Godzilla's semantics — orig :357-359 nulls the pass's LOCAL {@code e}; the port used to
 *       {@code setTarget(null)} (BOSS-017), dropping the stored target every pass.</li>
 * </ul>
 *
 * <p>One generated test per port site — a {@link GameTestGenerator} over {@link #sites()} in orig file
 * order, each a {@link Probe}, the LeonTargetingTests s110 shape: {@code PLAY_NICELY} set false, the site
 * driven once and required to show its effect (the control: the prey returned / the goal's {@code canUse}
 * true / attacking set / the stored target dropped), the effect undone, the flag set true, the same hunter and
 * prey driven once more and required to show nothing of it (null / {@code canUse} false / attacking 0 / the
 * stored target kept), the flag restored in a finally on every path. Scan sites call the private scan by
 * reflection (the IgnoreScreenParityTests idiom); goal sites read every {@code NearestAttackableTargetGoal}
 * of the wanted target type (and the Pointysaurus's stare goal) off the target selector — the hunter spawned
 * with its goals and no AI — and call {@code canUse()} directly under a forced {@code Entity.random}
 * (the VortexParityTests.ForcedRoll seam) that pins the goal's 1-in-5 acquisition roll; the AI-step sites
 * (Hammerhead, Irukandji, Skate, Sea Monster, Godzilla) invoke {@code customServerAiStep} once with every
 * roll on the path pinned. Synchronous — nothing ticks between the flip and the restore; the flag is
 * global, so the batch is this class alone (TEST-003).</p>
 *
 * <p>The ENT-S-108 hunters (CaveFisher, DungeonBeast, EmperorScorpion, HerculesBeetle, SpitBug, TrooperBug,
 * Urchin, and the Nastysaurus / TRex scans) carry their gate since that finding landed (after the survey's
 * snapshot); their rows are pinned here as well. Geometry as CreativeMappingParityTests: the hunter frozen
 * at rel (20,1,24) on the floor of the 48x16x48 empty_large (Godzilla in the 48x34x48 empty_tall), the prey
 * east on the same floor at 8 blocks (inside every scan box, the Ant Robot's 6..9 stomp ring and every
 * goal's follow range), 5 blocks (the Hammerhead's 7 + w/2 reach), 3 blocks (the Sea Monster's 4 + w/2) or
 * 1 block (the Irukandji's distSq &lt; 3, the Skate's &lt; 4); line of sight asserted. Prey: a pig where the
 * hunter takes any living thing, a Zombie where it takes hostile mobs only (the Mob + Enemy goals, ENT-S-124), a
 * butterfly for the insect eaters,
 * a Chicken for the Lizard, a Spider Robot for the Spider Driver's mount scan, a survival mock player where
 * orig took players only (looking at the hunter where the Ender Reaper's stare test and the Pointysaurus's
 * stare goal demand it; the Pointysaurus's row a plain ServerPlayer, as the framework's mock answers
 * isCreative() true whatever its mode and the stare goal refuses creative players). Spawns are frozen and
 * discarded in the finally; mock players are removed there.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PlayNicelyGateParityTests {

    private static final String BATCH = "playNicelyGateParity";
    private static final String TEST_PREFIX = "playnicelygateparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the templates are named in full (IgnoreScreenParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final String EMPTY_TALL = OreSpawnMod.MOD_ID + ":empty_tall";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-115";

    /** The hunter on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside every scan box and goal follow range, inside the Ant Robot's 6..9 stomp ring (orig AntRobot.java:977-986). */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** 5 blocks east: inside the Hammerhead's 7 + w/2 melee reach (orig Hammerhead.java:211). */
    private static final BlockPos MELEE_PREY_POS = new BlockPos(25, 1, 24);
    /** 3 blocks east: inside the Sea Monster's 4 + w/2 melee reach (orig SeaMonster.java:469). */
    private static final BlockPos NEAR_PREY_POS = new BlockPos(23, 1, 24);
    /** 1 block east: inside the Irukandji's distSq &lt; 3 (orig Irukandji.java:256) and the Skate's distSq &lt; 4 (orig Skate.java:248). */
    private static final BlockPos TOUCH_PREY_POS = new BlockPos(21, 1, 24);
    /** Mock-player spots, the same distances (block-centred). */
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);
    private static final Vec3 MELEE_PLAYER_POS = new Vec3(25.5, 1.0, 24.5);
    private static final Vec3 NEAR_PLAYER_POS = new Vec3(23.5, 1.0, 24.5);
    private static final Vec3 TOUCH_PLAYER_POS = new Vec3(21.5, 1.0, 24.5);
    /** Prey health, high enough that no pinned hit kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** The vanilla goal's acquisition roll: {@code NearestAttackableTargetGoal} reduces its 10-tick interval to {@code nextInt(5) != 0 → skip}. */
    private static final int GOAL_ROLL_BOUND = 5;

    private static final Supplier<EntityType<? extends Mob>> PIG = () -> EntityType.PIG;
    private static final Supplier<EntityType<? extends Mob>> ZOMBIE = () -> EntityType.ZOMBIE;
    private static final Supplier<EntityType<? extends Mob>> CHICKEN = () -> EntityType.CHICKEN;

    // ------------------------------------------------------------------
    // The site table, in orig file order
    // ------------------------------------------------------------------

    /**
     * One port site. {@link #setUp} spawns the hunter and its prey with the flag down; {@link #drive}
     * exercises the site once and answers whether the orig-gated effect showed (it may assert
     * phase-specific invariants, e.g. that a stored target was kept under the flag); {@link #reset} undoes
     * the effect so the second drive starts from the same state; {@link #trace} names what the last drive
     * observed, for the failure message; {@link #cleanUp} discards the spawns and runs in the finally,
     * tolerant of a set-up that never finished.
     */
    private interface Probe {
        void setUp(GameTestHelper helper);

        boolean drive(GameTestHelper helper, boolean playNicely);

        void reset(GameTestHelper helper);

        String trace();

        void cleanUp(GameTestHelper helper);
    }

    /** One orig gate and the port site that carries it. */
    private record Site(int index, String tag, String orig, String port, String effect, String template, Supplier<Probe> probe) {
        String testName() {
            return TEST_PREFIX + String.format("s115_%02d_%s", this.index, this.tag);
        }

        String where() {
            return this.port + " (orig " + this.orig + ")";
        }
    }

    private static Site site(int index, String tag, String orig, String port, String effect, Supplier<Probe> probe) {
        return new Site(index, tag, orig, port, effect, EMPTY_LARGE, probe);
    }

    private static List<Site> sites() {
        List<Site> sites = new ArrayList<>();
        // AntRobot — orig :940-942 (stomp) and :1012-1014 (hunt)
        sites.add(site(1, "antrobot_940_stomp", "AntRobot.java:940-942", "AntRobot.feetFindSomethingToHit",
                "the stomp hitting a pig 8 blocks off, inside the 6..9 ring",
                () -> new StompProbe(ModEntities.ANT_ROBOT)));
        sites.add(site(2, "antrobot_1012_hunt", "AntRobot.java:1012-1014", "AntRobot.findSomethingToAttack",
                "the hunt scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ANT_ROBOT, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Boyfriend — orig :140-142 (the IMob task registered only with PlayNicely == 0)
        sites.add(site(3, "boyfriend_140_monster_goal", "Boyfriend.java:140-142", "Boyfriend's NearestAttackableTargetGoal<Mob> + Enemy (ENT-S-124)",
                "the goal's canUse taking a Zombie 8 blocks off",
                () -> new GoalProbe(ModEntities.BOYFRIEND, Mob.class, PreyKind.ZOMBIE, false)));
        // CaterKiller — orig :560-562
        sites.add(site(4, "caterkiller_560_player_goal", "CaterKiller.java:560-562", "EntityCaterKiller's NearestAttackableTargetGoal<Player>",
                "the goal's canUse taking a survival player 8 blocks off",
                () -> new GoalProbe(ModEntities.ENTITY_CATER_KILLER, Player.class, PreyKind.PLAYER, false)));
        // CaveFisher — orig :231-233 (gate landed with ENT-S-108; pinned)
        sites.add(site(5, "cavefisher_231_scan", "CaveFisher.java:231-233", "CaveFisher.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.CAVE_FISHER, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Cephadrome — orig :576-578
        sites.add(site(6, "cephadrome_576_scan", "Cephadrome.java:576-578", "Cephadrome.findSomethingToAttack",
                "the scan returning a Zombie 8 blocks off",
                () -> new ScanProbe(ModEntities.CEPHADROME, ZOMBIE, PREY_POS, "findSomethingToAttack", null, null)));
        // Dragon — orig :577-579
        sites.add(site(7, "dragon_577_scan", "Dragon.java:577-579", "Dragon.findSomethingToAttack",
                "the scan returning a Zombie 8 blocks off",
                () -> new ScanProbe(ModEntities.DRAGON, ZOMBIE, PREY_POS, "findSomethingToAttack", null, null)));
        // Dragonfly — orig :232-234
        sites.add(site(8, "dragonfly_232_scan", "Dragonfly.java:232-234", "DragonflyHuntGoal.findPrey",
                "the scan returning a butterfly 8 blocks off",
                DragonflyScanProbe::new));
        // DungeonBeast — orig :250-252 (ENT-S-108 gate; pinned)
        sites.add(site(9, "dungeonbeast_250_scan", "DungeonBeast.java:250-252", "DungeonBeast.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.DUNGEON_BEAST, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // EmperorScorpion — orig :504-506 (ENT-S-108 gate; pinned)
        sites.add(site(10, "emperorscorpion_504_scan", "EmperorScorpion.java:504-506", "EntityEmperorScorpion.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_EMPEROR_SCORPION, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // EnderKnight — orig :62-64 (the stare test :83-93, restored by ENT-S-132, wants the player looking at the knight's mid-height)
        sites.add(site(11, "enderknight_62_player_goal", "EnderKnight.java:62-64", "EnderKnight's NearestAttackableTargetGoal<Player>",
                "the goal's canUse taking a survival player 8 blocks off who stares at it",
                () -> new GoalProbe(ModEntities.ENDER_KNIGHT, Player.class, PreyKind.PLAYER_STARING_AT_MID, false)));
        // EnderReaper — orig :62-64 (the stare test :83-93 wants the player looking at the reaper's mid-height)
        sites.add(site(12, "enderreaper_62_player_goal", "EnderReaper.java:62-64", "EnderReaper's NearestAttackableTargetGoal<Player>",
                "the goal's canUse taking a survival player 8 blocks off who stares at it",
                () -> new GoalProbe(ModEntities.ENDER_REAPER, Player.class, PreyKind.PLAYER_STARING_AT_MID, false)));
        // Fairy — orig :239-241
        sites.add(site(13, "fairy_239_scan", "Fairy.java:239-241", "Fairy.findSomethingToAttack",
                "the scan returning a Zombie 8 blocks off",
                () -> new ScanProbe(ModEntities.FAIRY, ZOMBIE, PREY_POS, "findSomethingToAttack", null, null)));
        // Frog — orig :308-310
        sites.add(site(14, "frog_308_scan", "Frog.java:308-310", "Frog.findInsectTarget",
                "the scan returning a butterfly 8 blocks off",
                () -> new ScanProbe(ModEntities.FROG, ModEntities.ENTITY_BUTTERFLY, PREY_POS, "findInsectTarget", null, null)));
        // GammaMetroid — orig :291-293
        sites.add(site(15, "gammametroid_291_scan", "GammaMetroid.java:291-293", "EntityGammaMetroid.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_GAMMA_METROID, PIG, PREY_POS, "findSomethingToAttack", null,
                        (helper, mob) -> helper.assertTrue(!((net.minecraft.world.entity.TamableAnimal) mob).isTame() && !mob.isBaby(),
                                "precondition: a fresh Metroid is untamed and grown (orig GammaMetroid.java:278, :294) (" + FINDING + " test setup)"))));
        // GiantRobot — orig :343-345
        sites.add(site(16, "giantrobot_343_scan", "GiantRobot.java:343-345", "GiantRobot.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.GIANT_ROBOT, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Girlfriend — orig :166-168
        sites.add(site(17, "girlfriend_166_monster_goal", "Girlfriend.java:166-168", "Girlfriend's NearestAttackableTargetGoal<Mob> + Enemy (ENT-S-124)",
                "the goal's canUse taking a Zombie 8 blocks off",
                () -> new GoalProbe(ModEntities.GIRLFRIEND, Mob.class, PreyKind.ZOMBIE, false)));
        // Godzilla — orig :357-359 (the local nulled, the stored target kept)
        sites.add(new Site(18, "godzilla_357_stored_target_kept", "Godzilla.java:357-359", "Godzilla.customServerAiStep",
                "the combat pass engaging a stored pig 8 blocks off — attacking set", EMPTY_TALL, GodzillaProbe::new));
        // Hammerhead — orig :194-196 (rt blanked for the pass) and :252-254 (scan)
        sites.add(site(19, "hammerhead_194_revenge_local", "Hammerhead.java:194-196", "Hammerhead.customServerAiStep (the revenge pass)",
                "the pass engaging a written revenge target 5 blocks off — attacking set",
                HammerheadRevengeProbe::new));
        sites.add(site(20, "hammerhead_194_hurtby_slot", "Hammerhead.java:194-209", "Hammerhead.customServerAiStep (the port-only stored-target fallback)",
                "the pass engaging a pig stored through setTarget (the HurtByTargetGoal channel) 5 blocks off — attacking set; the slot untouched either way",
                HammerheadHurtBySlotProbe::new));
        sites.add(site(21, "hammerhead_252_scan", "Hammerhead.java:252-254", "Hammerhead.customServerAiStep (the nearest-player scan)",
                "the scan engaging a survival player 5 blocks off — attacking set",
                HammerheadScanProbe::new));
        // HerculesBeetle — orig :417-419 (ENT-S-108 gate; pinned)
        sites.add(site(22, "herculesbeetle_417_scan", "HerculesBeetle.java:417-419", "EntityHerculesBeetle.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_HERCULES_BEETLE, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Irukandji — orig :291-293 (the stored-target read and the scan sit behind it)
        sites.add(site(23, "irukandji_291_stored_target", "Irukandji.java:291-293 (the :299-302 stored-target read behind it)",
                "Irukandji.customServerAiStep (the pick)",
                "the pick engaging a stored pig 1 block off — attacking set",
                () -> new InlinePickProbe(ModEntities.IRUKANDJI, true, TOUCH_PREY_POS, TOUCH_PLAYER_POS,
                        new int[] {10, 1, 8, 1, 4, 1}, m -> ((Irukandji) m).getAttacking(), (m, v) -> ((Irukandji) m).setAttacking(v))));
        sites.add(site(24, "irukandji_291_scan", "Irukandji.java:291-293 (the :304-309 scan behind it)",
                "Irukandji.customServerAiStep (the pick)",
                "the pick storing and engaging a survival player 1 block off — attacking set",
                () -> new InlinePickProbe(ModEntities.IRUKANDJI, false, TOUCH_PREY_POS, TOUCH_PLAYER_POS,
                        new int[] {10, 1, 8, 1, 4, 1}, m -> ((Irukandji) m).getAttacking(), (m, v) -> ((Irukandji) m).setAttacking(v))));
        // Kyuubi — orig :205-207
        sites.add(site(25, "kyuubi_205_scan", "Kyuubi.java:205-207", "EntityKyuubi.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_KYUUBI, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Leon — orig :92-94
        sites.add(site(26, "leon_92_monster_goal", "Leon.java:92-94", "EntityLeon's NearestAttackableTargetGoal<Mob> + Enemy (ENT-S-124)",
                "the goal's canUse taking a Zombie 8 blocks off",
                () -> new GoalProbe(ModEntities.ENTITY_LEON, Mob.class, PreyKind.ZOMBIE, false)));
        // Lizard — orig :336-338 (ahead of the revenge-first block, whose 1-in-100 roll is pinned quiet)
        sites.add(site(27, "lizard_336_scan", "Lizard.java:336-338", "Lizard.findSomethingToAttack",
                "the scan returning a Chicken 8 blocks off",
                () -> new ScanProbe(ModEntities.LIZARD, CHICKEN, PREY_POS, "findSomethingToAttack", new int[] {100, 1}, null)));
        // Nastysaurus — orig :215-217 (rt blanked for the pass) and :279-281 (scan; ENT-S-108 gate, pinned)
        sites.add(site(28, "nastysaurus_215_revenge_local", "Nastysaurus.java:215-217", "Nastysaurus.selectTarget (the revenge pass)",
                "the pass dropping a dead stored revenge target (orig :219-221)",
                () -> new SelectTargetDeadProbe(ModEntities.NASTYSAURUS, Nastysaurus.class)));
        sites.add(site(29, "nastysaurus_215_scan_pick_cleared", "Nastysaurus.java:215-217 with :240-242", "Nastysaurus.selectTarget (the scan's own pick under the flag)",
                "the scan's own pick taken (slot and scanPick = a pig 8 blocks off) with the flag down, and cleared by the next pass with it up",
                () -> new ScanPickClearedProbe(ModEntities.NASTYSAURUS, Nastysaurus.class)));
        sites.add(site(30, "nastysaurus_279_scan", "Nastysaurus.java:279-281", "Nastysaurus.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.NASTYSAURUS, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // PitchBlack — orig :541-543
        sites.add(site(31, "pitchblack_541_scan", "PitchBlack.java:541-543", "PitchBlack.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.PITCH_BLACK, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Pointysaurus — orig :250-252 (scan) as the pair of proactive goals; :186-188 (rt) has no port pass, deferred
        sites.add(site(32, "pointysaurus_250_player_goals", "Pointysaurus.java:250-252",
                "Pointysaurus's NearestAttackableTargetGoal<Player> and PointysaurusStareGoal",
                "both goals' canUse taking a survival player 8 blocks off who stares at it",
                () -> new GoalProbe(ModEntities.POINTYSAURUS, Player.class, PreyKind.PLAYER_STARING_AT_EYES, true)));
        // PurplePower — orig :268-270
        sites.add(site(33, "purplepower_268_scan", "PurplePower.java:268-270", "PurplePower.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.PURPLE_POWER, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Rat — orig :252-254
        sites.add(site(34, "rat_252_scan", "Rat.java:252-254", "EntityRat.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_RAT, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Robot1 / Robot3 / Robot5 — orig :205-207 / :322-324 / :296-298
        sites.add(site(35, "robot1_205_scan", "Robot1.java:205-207", "Robot1.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ROBOT_1, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        sites.add(site(36, "robot3_322_scan", "Robot3.java:322-324", "Robot3.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ROBOT_3, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        sites.add(site(37, "robot5_296_scan", "Robot5.java:296-298", "Robot5.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ROBOT_5, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // SeaMonster — orig :514-516 (the stored-target read and the scan sit behind it)
        sites.add(site(38, "seamonster_514_stored_target", "SeaMonster.java:514-516 (the :522-525 stored-target read behind it)",
                "SeaMonster.customServerAiStep (the pick)",
                "the pick engaging a stored pig 3 blocks off — attacking set",
                () -> new InlinePickProbe(ModEntities.SEA_MONSTER, true, NEAR_PREY_POS, NEAR_PLAYER_POS,
                        new int[] {25, 1, 5, 1, 4, 1}, m -> ((SeaMonster) m).getAttacking(), (m, v) -> ((SeaMonster) m).setAttacking(v))));
        sites.add(site(39, "seamonster_514_scan", "SeaMonster.java:514-516 (the :527-532 scan behind it)",
                "SeaMonster.customServerAiStep (the pick)",
                "the pick storing and engaging a survival player 3 blocks off — attacking set",
                () -> new InlinePickProbe(ModEntities.SEA_MONSTER, false, NEAR_PREY_POS, NEAR_PLAYER_POS,
                        new int[] {25, 1, 5, 1, 4, 1}, m -> ((SeaMonster) m).getAttacking(), (m, v) -> ((SeaMonster) m).setAttacking(v))));
        // SeaViper — orig :531-533
        sites.add(site(40, "seaviper_531_player_goal", "SeaViper.java:531-533", "SeaViper's NearestAttackableTargetGoal<Player>",
                "the goal's canUse taking a survival player 8 blocks off",
                () -> new GoalProbe(ModEntities.SEA_VIPER, Player.class, PreyKind.PLAYER, false)));
        // Skate — orig :283-285 (the stored-target read and the scan sit behind it)
        sites.add(site(41, "skate_283_stored_target", "Skate.java:283-285 (the :291-294 stored-target read behind it)",
                "Skate.customServerAiStep (the pick)",
                "the pick engaging a stored pig 1 block off — attacking set",
                () -> new InlinePickProbe(ModEntities.SKATE, true, TOUCH_PREY_POS, TOUCH_PLAYER_POS,
                        new int[] {10, 1, 8, 1, 4, 1}, m -> ((Skate) m).getAttacking(), (m, v) -> ((Skate) m).setAttacking(v))));
        sites.add(site(42, "skate_283_scan", "Skate.java:283-285 (the :296-301 scan behind it)",
                "Skate.customServerAiStep (the pick)",
                "the pick storing and engaging a survival player 1 block off — attacking set",
                () -> new InlinePickProbe(ModEntities.SKATE, false, TOUCH_PREY_POS, TOUCH_PLAYER_POS,
                        new int[] {10, 1, 8, 1, 4, 1}, m -> ((Skate) m).getAttacking(), (m, v) -> ((Skate) m).setAttacking(v))));
        // SpiderDriver — orig :104-106 (mount scan) and :160-162 (combat scan)
        sites.add(site(43, "spiderdriver_104_robot_scan", "SpiderDriver.java:104-106", "SpiderDriver.findSpiderRobot",
                "the mount scan returning an unridden Spider Robot 8 blocks off",
                () -> new ScanProbe(ModEntities.SPIDER_DRIVER, ModEntities.SPIDER_ROBOT, PREY_POS, "findSpiderRobot", null, null)));
        sites.add(site(44, "spiderdriver_160_combat_scan", "SpiderDriver.java:160-162", "SpiderDriver.findSomethingToAttack",
                "the combat scan returning a pig 8 blocks off (past the 6-block refusal of orig :156)",
                () -> new ScanProbe(ModEntities.SPIDER_DRIVER, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // SpitBug — orig :371-373 (ENT-S-108 gate; pinned)
        sites.add(site(45, "spitbug_371_scan", "SpitBug.java:371-373", "EntitySpitBug.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_SPIT_BUG, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Spyro — orig :698-700
        sites.add(site(46, "spyro_698_scan", "Spyro.java:698-700", "EntitySpyro.findSomethingToAttack",
                "the scan returning a Zombie 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_SPYRO, ZOMBIE, PREY_POS, "findSomethingToAttack", null, null)));
        // Stinky — orig :688-690
        sites.add(site(47, "stinky_688_scan", "Stinky.java:688-690", "EntityStinky.findSomethingToAttack",
                "the scan returning a Zombie 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_STINKY, ZOMBIE, PREY_POS, "findSomethingToAttack", null, null)));
        // TerribleTerror — orig :296-298
        sites.add(site(48, "terribleterror_296_scan", "TerribleTerror.java:296-298", "EntityTerribleTerror.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_TERRIBLE_TERROR, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // ThePrinceAdult / ThePrinceTeen — orig :112-114 / :116-118
        sites.add(site(49, "theprinceadult_112_monster_goal", "ThePrinceAdult.java:112-114", "ThePrinceAdult's NearestAttackableTargetGoal<Mob> + Enemy (ENT-S-124)",
                "the goal's canUse taking a Zombie 8 blocks off",
                () -> new GoalProbe(ModEntities.THE_PRINCE_ADULT, Mob.class, PreyKind.ZOMBIE, false)));
        sites.add(site(50, "theprinceteen_116_monster_goal", "ThePrinceTeen.java:116-118", "ThePrinceTeen's NearestAttackableTargetGoal<Mob> + Enemy (ENT-S-124)",
                "the goal's canUse taking a Zombie 8 blocks off",
                () -> new GoalProbe(ModEntities.THE_PRINCE_TEEN, Mob.class, PreyKind.ZOMBIE, false)));
        // TRex — orig :185-187 (rt blanked for the pass) and :251-253 (scan; ENT-S-108 gate, pinned)
        sites.add(site(51, "trex_185_revenge_local", "TRex.java:185-187", "TRex.selectTarget (the revenge pass)",
                "the pass dropping a dead stored revenge target (orig :189-191)",
                () -> new SelectTargetDeadProbe(ModEntities.TREX, TRex.class)));
        sites.add(site(52, "trex_185_scan_pick_cleared", "TRex.java:185-187 with :210-212", "TRex.selectTarget (the scan's own pick under the flag)",
                "the scan's own pick taken (slot and scanPick = a pig 8 blocks off) with the flag down, and cleared by the next pass with it up",
                () -> new ScanPickClearedProbe(ModEntities.TREX, TRex.class)));
        sites.add(site(53, "trex_251_scan", "TRex.java:251-253", "TRex.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.TREX, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Triffid — orig :322-324
        sites.add(site(54, "triffid_322_scan", "Triffid.java:322-324", "EntityTriffid.findSomethingToAttack",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_TRIFFID, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // TrooperBug — orig :511-513 (ENT-S-108 gate; pinned)
        sites.add(site(55, "trooperbug_511_scan", "TrooperBug.java:511-513", "EntityTrooperBug.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.ENTITY_TROOPER_BUG, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        // Urchin — orig :273-275 (ENT-S-108 gate; pinned)
        sites.add(site(56, "urchin_273_scan", "Urchin.java:273-275", "Urchin.findSomethingToAttack (ENT-S-108 gate)",
                "the scan returning a pig 8 blocks off",
                () -> new ScanProbe(ModEntities.URCHIN, PIG, PREY_POS, "findSomethingToAttack", null, null)));
        return sites;
    }

    /** One test per port site: 56 TestFunctions in the {@code playNicelyGateParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> playNicelyGateSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), site.template(), Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: control with the flag down, the same site silent with it up, the flag restored
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Site site) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — several of these filters refuse everything"
                        + " on Peaceful (" + FINDING + " test setup)");
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Probe probe = site.probe().get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false (" + FINDING + " test setup)");
            probe.setUp(helper);
            helper.assertTrue(probe.drive(helper, false), "control: with playNicely off " + site.where() + " must show "
                    + site.effect() + " — saw " + probe.trace() + " (" + FINDING + ")");
            probe.reset(helper);
            OreSpawnConfig.PLAY_NICELY.set(true);
            helper.assertTrue(OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(true) must read back true (" + FINDING + " test setup)");
            helper.assertTrue(!probe.drive(helper, true), site.where() + " with playNicely on: orig " + site.orig()
                    + " gates this out while PlayNicely != 0, read live, so the same hunter and prey that showed "
                    + site.effect() + " with the flag off must show nothing of it — saw " + probe.trace() + " (" + FINDING + ")");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            probe.cleanUp(helper);
        }
        helper.succeed();
    }

    /** A hunter and one prey (a mob or a mock player), both frozen; the base of every probe. */
    private abstract static class HunterProbe implements Probe {
        Mob hunter;
        Mob prey;
        ServerPlayer player;
        String trace = "(not driven)";

        LivingEntity preyEntity() {
            return this.player != null ? this.player : this.prey;
        }

        @Override
        public String trace() {
            return this.trace;
        }

        @Override
        public void reset(GameTestHelper helper) {
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            discardQuietly(this.prey);
            removePlayer(helper, this.player);
            discardQuietly(this.hunter);
        }
    }

    // ------------------------------------------------------------------
    // The scan shape: the private scan returns the prey with the flag down, null with it up
    // ------------------------------------------------------------------

    /** A private scan invoked by reflection; the hunter frozen, the prey frozen on its spot, every roll on the path pinned. */
    private static final class ScanProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final Supplier<? extends EntityType<? extends Mob>> preyType;
        private final BlockPos preyPos;
        private final String method;
        private final int[] rolls;
        private final HunterCheck hunterCheck;

        ScanProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, Supplier<? extends EntityType<? extends Mob>> preyType,
                  BlockPos preyPos, String method, int[] rolls, HunterCheck hunterCheck) {
            this.hunterType = hunterType;
            this.preyType = preyType;
            this.preyPos = preyPos;
            this.method = method;
            this.rolls = rolls;
            this.hunterCheck = hunterCheck;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            if (this.hunterCheck != null) {
                this.hunterCheck.check(helper, this.hunter);
            }
            this.prey = spawnPrey(helper, this.preyType.get(), this.preyPos);
            if (this.rolls != null) {
                replaceRandom(this.hunter, rolls(this.rolls));
            }
            assertSees(helper, this.hunter, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            Object found = invoke(this.hunter, this.hunter.getClass(), this.method);
            this.trace = this.method + " -> " + describe((Entity) found);
            return found == this.prey;
        }
    }

    /** A precondition on the freshly spawned hunter. */
    private interface HunterCheck {
        void check(GameTestHelper helper, Mob hunter);
    }

    /** orig AntRobot.java:940-942 (port feetFindSomethingToHit, a void scan): a pig 8 blocks off, inside the 6..9 ring, takes attack/10 with the flag down and nothing with it up. */
    private static final class StompProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;

        StompProbe(Supplier<? extends EntityType<? extends Mob>> hunterType) {
            this.hunterType = hunterType;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, PREY_POS);
            double dist = this.hunter.distanceTo(this.prey);
            helper.assertTrue(dist >= 6.0 && dist <= 9.0, "precondition: the pig must stand inside the stomp ring 6..9"
                    + " (orig AntRobot.java:977-986) — at " + dist + " (" + FINDING + " test geometry)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invoke(this.hunter, this.hunter.getClass(), "feetFindSomethingToHit");
            this.trace = "pig health " + this.prey.getHealth();
            return this.prey.getHealth() < PREY_HEALTH;
        }

        @Override
        public void reset(GameTestHelper helper) {
            healPrey(this.prey);
        }
    }

    /** orig Dragonfly.java:232-234 (port DragonflyHuntGoal.findPrey, reached through the dragonfly's private huntGoal — the PeacefulGateParityTests idiom). */
    private static final class DragonflyScanProbe extends HunterProbe {
        private DragonflyHuntGoal goal;

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, ModEntities.ENTITY_DRAGONFLY.get(), HUNTER_POS);
            this.goal = (DragonflyHuntGoal) readField(this.hunter, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(this.goal != null,
                    "precondition: EntityDragonfly.registerGoals must have built the hunt goal (" + FINDING + " test setup)");
            this.prey = spawnPrey(helper, ModEntities.ENTITY_BUTTERFLY.get(), PREY_POS);
            helper.assertTrue(this.prey.getBbWidth() <= 0.6f, "precondition: a butterfly (" + this.prey.getBbWidth()
                    + " wide) is on the orig whitelist (Dragonfly.java:216) and under the port's 0.6 width rule (" + FINDING + " test setup)");
            assertSees(helper, this.hunter, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            Object found = invoke(this.goal, DragonflyHuntGoal.class, "findPrey");
            this.trace = "findPrey -> " + describe((Entity) found);
            return found == this.prey;
        }
    }

    // ------------------------------------------------------------------
    // The goal shape: every matching target goal's canUse is true with the flag down, false with it up
    // ------------------------------------------------------------------

    private enum PreyKind { ZOMBIE, PLAYER, PLAYER_STARING_AT_EYES, PLAYER_STARING_AT_MID }

    /**
     * The hunter spawned with its goals and no AI; every {@link NearestAttackableTargetGoal} whose target
     * type is the wanted one (and the Pointysaurus's stare goal where asked) is read off the target
     * selector and asked {@code canUse()} directly, its 1-in-5 acquisition roll pinned to fire. The prey
     * is a Zombie for the Mob + Enemy goals (ENT-S-124), a survival mock player for the Player goals — staring at the
     * hunter's eyes (the stare goal's dot &gt; 0.97) or its mid-height (the Ender Reaper's look-vector test)
     * where the goal's own predicate demands it.
     */
    private static final class GoalProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final Class<?> targetType;
        private final PreyKind preyKind;
        private final boolean stareGoalToo;
        private final List<Goal> goals = new ArrayList<>();

        GoalProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, Class<?> targetType, PreyKind preyKind, boolean stareGoalToo) {
            this.hunterType = hunterType;
            this.targetType = targetType;
            this.preyKind = preyKind;
            this.stareGoalToo = stareGoalToo;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnWithGoals(helper, this.hunterType.get(), HUNTER_POS);
            replaceRandom(this.hunter, rolls(GOAL_ROLL_BOUND, 0));
            if (this.preyKind == PreyKind.ZOMBIE) {
                this.prey = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            } else {
                this.player = this.preyKind == PreyKind.PLAYER_STARING_AT_EYES
                        ? survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_POS))
                        : playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
                if (this.preyKind == PreyKind.PLAYER_STARING_AT_EYES) {
                    helper.assertTrue(!this.player.isCreative(), "precondition: the stare goal's control needs a player"
                            + " whose isCreative() follows its SURVIVAL mode — the framework's mock answers creative"
                            + " unconditionally (" + FINDING + " test setup)");
                    this.player.lookAt(EntityAnchorArgument.Anchor.EYES, this.hunter.getEyePosition());
                } else if (this.preyKind == PreyKind.PLAYER_STARING_AT_MID) {
                    this.player.lookAt(EntityAnchorArgument.Anchor.EYES,
                            new Vec3(this.hunter.getX(), this.hunter.getY() + this.hunter.getBbHeight() / 2.0f, this.hunter.getZ()));
                }
                helper.assertTrue(!this.player.getAbilities().invulnerable && !this.player.getAbilities().instabuild,
                        "precondition: the SURVIVAL mock player is neither invulnerable nor instabuild, so the vanilla"
                                + " conditions and the orig creative rule both admit it (" + FINDING + " test setup)");
            }
            String name = this.hunter.getClass().getSimpleName();
            for (WrappedGoal wrapped : this.hunter.targetSelector.getAvailableGoals()) {
                Goal goal = wrapped.getGoal();
                if (goal instanceof NearestAttackableTargetGoal<?> nearest
                        && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == this.targetType) {
                    this.goals.add(goal);
                } else if (this.stareGoalToo && goal instanceof PointysaurusStareGoal) {
                    this.goals.add(goal);
                }
            }
            helper.assertTrue(!this.goals.isEmpty(), "precondition: " + name + " must carry a NearestAttackableTargetGoal<"
                    + this.targetType.getSimpleName() + ">" + (this.stareGoalToo ? " and a PointysaurusStareGoal" : "")
                    + " on its target selector — the port's shape of the orig registration (" + FINDING + " test setup)");
            assertSees(helper, this.hunter, preyEntity());
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            int passing = 0;
            StringBuilder seen = new StringBuilder();
            for (Goal goal : this.goals) {
                boolean can = goal.canUse();
                if (can) passing++;
                seen.append(seen.length() == 0 ? "" : ", ").append(describe(goal)).append(".canUse=").append(can);
            }
            this.trace = seen.toString();
            helper.assertTrue(passing == 0 || passing == this.goals.size(), this.hunter.getClass().getSimpleName()
                    + ": every gated goal must answer alike with playNicely " + (playNicely ? "on" : "off") + " — saw "
                    + this.trace + " (" + FINDING + ")");
            return passing == this.goals.size();
        }
    }

    // ------------------------------------------------------------------
    // The AI-step shapes: customServerAiStep driven once under pinned rolls
    // ------------------------------------------------------------------

    /**
     * orig Nastysaurus.java:215-217 with :240-242 / TRex.java:185-187 with :210-212 (port selectTarget): orig's
     * {@code rt} was only ever the revenge target — the scan's own pick was never stored — so under the flag orig's
     * pass acted on nothing and the mob stood down at its next pass. In the port the single slot also holds the
     * scan's own pick, so the blanking must leave that pick alone: with the flag down the pass takes a visible pig
     * (slot and {@code scanPick} = the pig, the control); with the flag up the next pass finds the scan's own pick,
     * runs it on to the gated scan and clears the slot, as at HEAD (refuter B1: an unconditional blanking made the
     * pick stick, {@code null != null} never clearing it). Nothing is reset between the two drives — the flag-up
     * pass must start from the slot the control filled.
     */
    private static final class ScanPickClearedProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final Class<?> hunterClass;

        ScanPickClearedProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, Class<?> hunterClass) {
            this.hunterType = hunterType;
            this.hunterClass = hunterClass;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, PREY_POS);
            helper.assertTrue(this.hunter.getTarget() == null && readField(this.hunter, this.hunterClass, "scanPick") == null,
                    "precondition: an empty slot and no scan pick before the first pass (" + FINDING + " test setup)");
            assertSees(helper, this.hunter, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invoke(this.hunter, this.hunterClass, "selectTarget");
            LivingEntity after = this.hunter.getTarget();
            Object scanPick = readField(this.hunter, this.hunterClass, "scanPick");
            this.trace = "target after the pass " + describe(after) + ", scanPick " + describe((Entity) scanPick);
            if (playNicely) {
                helper.assertTrue(after == null && scanPick == null, "with playNicely on the scan's own pick must be cleared by"
                        + " the pass — it is not orig's rt, so it is never blanked; the gated scan comes back empty and the slot"
                        + " is cleared as at HEAD (orig " + (this.hunterClass == TRex.class ? "TRex.java:210-212" : "Nastysaurus.java:240-242")
                        + " stood down) — saw " + this.trace + " (" + FINDING + ", refuter B1)");
            }
            return after == this.prey && scanPick == this.prey;
        }
    }

    /**
     * orig Hammerhead.java:194-209 (port customServerAiStep): the port reads the slot — the channel of
     * {@code HurtByTargetGoal}, a read orig never had — when the revenge copy is empty. Under the flag orig's pass
     * consulted nothing and set attacking 0 (:219-221), so that read is gated with the pass (refuter B2). A pig 5
     * blocks off is stored through {@code setTarget}, no revenge target: with the flag down the fallback engages it
     * (attacking 1); with the flag up the pass consults nothing, attacking stays 0 and the slot still holds the pig.
     */
    private static final class HammerheadHurtBySlotProbe extends HunterProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, ModEntities.HAMMERHEAD.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, MELEE_PREY_POS);
            this.hunter.setTarget(this.prey);
            replaceRandom(this.hunter, rolls(3, 1, 250, 0));
            helper.assertTrue(((Hammerhead) this.hunter).getAttacking() == 0 && this.hunter.getTarget() == this.prey
                    && readField(this.hunter, Hammerhead.class, "revengeTarget") == null,
                    "precondition: attacking 0, the pig in the slot and no revenge target before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invokeCustomServerAiStep(this.hunter);
            int attacking = ((Hammerhead) this.hunter).getAttacking();
            this.trace = "attacking=" + attacking + ", slot " + describe(this.hunter.getTarget());
            if (playNicely) {
                helper.assertTrue(this.hunter.getTarget() == this.prey, "with playNicely on the slot itself must be untouched:"
                        + " the pass consults nothing (orig Hammerhead.java:194-209) and writes nothing — saw " + this.trace
                        + " (" + FINDING + ", refuter B2)");
            }
            return attacking == 1;
        }

        @Override
        public void reset(GameTestHelper helper) {
            ((Hammerhead) this.hunter).setAttacking(0);
            healPrey(this.prey);
        }
    }

    /**
     * orig Hammerhead.java:194-196 (port customServerAiStep): the 1-in-3 pass pinned to fire, the 1-in-250
     * revenge drop pinned quiet; a pig 5 blocks off (inside the 7 + w/2 reach, :211) is written as the
     * revenge target, so with the flag down the pass looks at it and attacking goes to 1; with the flag up the
     * pass's copy is blanked (the getTarget fallback holds nothing, the scan is gated), attacking stays 0,
     * and {@code rt} — the port's revengeTarget — is still the pig, as orig never touched it.
     */
    private static final class HammerheadRevengeProbe extends HunterProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, ModEntities.HAMMERHEAD.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, MELEE_PREY_POS);
            writeField(this.hunter, Hammerhead.class, "revengeTarget", this.prey);
            replaceRandom(this.hunter, rolls(3, 1, 250, 0));
            helper.assertTrue(((Hammerhead) this.hunter).getAttacking() == 0 && this.hunter.getTarget() == null,
                    "precondition: attacking 0 and no stored target before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invokeCustomServerAiStep(this.hunter);
            int attacking = ((Hammerhead) this.hunter).getAttacking();
            Object revenge = readField(this.hunter, Hammerhead.class, "revengeTarget");
            this.trace = "attacking=" + attacking + ", revengeTarget " + describe((Entity) revenge);
            if (playNicely) {
                helper.assertTrue(revenge == this.prey, "with playNicely on the revenge target itself must be kept: orig"
                        + " Hammerhead.java:194-196 blanks the pass's copy `e`, never `rt` — saw " + this.trace + " (" + FINDING + ")");
            }
            return attacking == 1;
        }

        @Override
        public void reset(GameTestHelper helper) {
            ((Hammerhead) this.hunter).setAttacking(0);
            healPrey(this.prey);
        }
    }

    /**
     * orig Hammerhead.java:252-254 (port customServerAiStep's nearest-player scan): no revenge target, the
     * 1-in-3 pass pinned to fire; a survival mock player 5 blocks off is found and engaged (attacking 1) with
     * the flag down and not looked for with it up.
     */
    private static final class HammerheadScanProbe extends HunterProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, ModEntities.HAMMERHEAD.get(), HUNTER_POS);
            this.player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(MELEE_PLAYER_POS));
            replaceRandom(this.hunter, rolls(3, 1, 250, 0));
            helper.assertTrue(readField(this.hunter, Hammerhead.class, "revengeTarget") == null && this.hunter.getTarget() == null,
                    "precondition: no revenge target and no stored target, so the scan is the only source (" + FINDING + " test setup)");
            helper.assertTrue(((Hammerhead) this.hunter).getAttacking() == 0,
                    "precondition: attacking 0 before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invokeCustomServerAiStep(this.hunter);
            int attacking = ((Hammerhead) this.hunter).getAttacking();
            this.trace = "attacking=" + attacking + ", player health " + this.player.getHealth();
            return attacking == 1;
        }

        @Override
        public void reset(GameTestHelper helper) {
            ((Hammerhead) this.hunter).setAttacking(0);
            this.player.setHealth(1000.0f);
        }
    }

    /**
     * The Irukandji / Skate / Sea Monster pick (orig findSomethingToAttack, gated as a whole): in the
     * stored shape a pig is written as the target inside melee reach and the pass engages it (attacking 1)
     * with the flag down, leaves it stored and unengaged with the flag up; in the scan shape a survival mock
     * player inside melee reach is found, stored and engaged with the flag down, and neither found nor
     * stored with it up. Every roll on the step's path is pinned (the water hunt quiet, the pass firing,
     * the bite quiet).
     */
    private static final class InlinePickProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final boolean stored;
        private final BlockPos preyPos;
        private final Vec3 playerPos;
        private final int[] rolls;
        private final ToIntFunction<Mob> attacking;
        private final ObjIntConsumer<Mob> setAttacking;

        InlinePickProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, boolean stored, BlockPos preyPos, Vec3 playerPos,
                        int[] rolls, ToIntFunction<Mob> attacking, ObjIntConsumer<Mob> setAttacking) {
            this.hunterType = hunterType;
            this.stored = stored;
            this.preyPos = preyPos;
            this.playerPos = playerPos;
            this.rolls = rolls;
            this.attacking = attacking;
            this.setAttacking = setAttacking;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            if (this.stored) {
                this.prey = spawnPrey(helper, EntityType.PIG, this.preyPos);
                this.hunter.setTarget(this.prey);
                helper.assertTrue(this.hunter.getTarget() == this.prey,
                        "precondition: the pig is the stored target (" + FINDING + " test setup)");
            } else {
                this.player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(this.playerPos));
                helper.assertTrue(this.hunter.getTarget() == null,
                        "precondition: no stored target, so the scan is the only source (" + FINDING + " test setup)");
            }
            replaceRandom(this.hunter, rolls(this.rolls));
            helper.assertTrue(this.attacking.applyAsInt(this.hunter) == 0,
                    "precondition: attacking 0 before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invokeCustomServerAiStep(this.hunter);
            int attacking = this.attacking.applyAsInt(this.hunter);
            this.trace = "attacking=" + attacking + ", target " + describe(this.hunter.getTarget());
            if (playNicely && this.stored) {
                helper.assertTrue(this.hunter.getTarget() == this.prey, "with playNicely on the stored target must be kept:"
                        + " orig's findSomethingToAttack answers null ahead of its stored-target read and clears nothing"
                        + " — saw " + this.trace + " (" + FINDING + ")");
            }
            if (playNicely && !this.stored) {
                helper.assertTrue(this.hunter.getTarget() == null, "with playNicely on nothing may be stored: the scan"
                        + " never ran — saw " + this.trace + " (" + FINDING + ")");
            }
            return attacking == 1;
        }

        @Override
        public void reset(GameTestHelper helper) {
            this.setAttacking.accept(this.hunter, 0);
            if (this.stored) {
                healPrey(this.prey);
            } else {
                this.hunter.setTarget(null);
                this.player.setHealth(1000.0f);
            }
        }
    }

    /**
     * orig Nastysaurus.java:215-217 / TRex.java:185-187 (port selectTarget): {@code e = rt; if (PlayNicely
     * != 0) e = null;} skips the pass's dead-drop of {@code rt} (:219-221 / :189-191). A removed pig is
     * written as the stored target: with the flag down the pass drops it from the slot; with the flag up the
     * pass's copy is blanked, the drop is skipped, the slot still holds the pig, and the scan's bookkeeping
     * ({@code scanPick}) claims nothing.
     */
    private static final class SelectTargetDeadProbe extends HunterProbe {
        private final Supplier<? extends EntityType<? extends Mob>> hunterType;
        private final Class<?> hunterClass;

        SelectTargetDeadProbe(Supplier<? extends EntityType<? extends Mob>> hunterType, Class<?> hunterClass) {
            this.hunterType = hunterType;
            this.hunterClass = hunterClass;
        }

        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, this.hunterType.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, PREY_POS);
            this.hunter.setTarget(this.prey);
            this.prey.discard();
            helper.assertTrue(!this.prey.isAlive() && this.hunter.getTarget() == this.prey,
                    "precondition: the removed pig is the stored target and reads as dead (" + FINDING + " test setup)");
            helper.assertTrue(readField(this.hunter, this.hunterClass, "scanPick") == null,
                    "precondition: a target set from outside the scan is not the scan's own pick (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invoke(this.hunter, this.hunterClass, "selectTarget");
            LivingEntity after = this.hunter.getTarget();
            Object scanPick = readField(this.hunter, this.hunterClass, "scanPick");
            this.trace = "target after the pass " + describe(after) + ", scanPick " + describe((Entity) scanPick)
                    + " (a removed pig was stored)";
            if (playNicely) {
                helper.assertTrue(after == this.prey, "with playNicely on the dead revenge target must still be stored: the pass's"
                        + " copy is blanked, so the dead-drop never runs (orig " + (this.hunterClass == TRex.class ? "TRex.java:185-187" : "Nastysaurus.java:215-217")
                        + ") — saw " + this.trace + " (" + FINDING + ")");
                helper.assertTrue(scanPick == null, "with playNicely on the blanked pass set nothing and must claim nothing:"
                        + " scanPick stays null — saw " + this.trace + " (" + FINDING + ")");
            }
            return after != this.prey;
        }

        @Override
        public void reset(GameTestHelper helper) {
            this.hunter.setTarget(this.prey);
        }
    }

    /**
     * orig Godzilla.java:356-359 (port customServerAiStep): the 1-in-200 release pinned quiet, the combat
     * roll (1-in-5) pinned to fire, the lightning (1-in-65), jump (1-in-20) and bite (1-in-4 / 1-in-3) rolls
     * pinned quiet; a pig 8 blocks off (myGetDistanceSq 64 &lt; 300) is written as the stored target. With
     * the flag down the pass engages it — attacking 1; with the flag up the pass's LOCAL is nulled, the
     * (gated) scan finds nothing, attacking stays 0 — and the stored target is still the pig, where the
     * BOSS-017 mapping used to setTarget(null). The crush loops run on ticker % 4 == 0 only, so two drives
     * (ticker 1 and 2) touch no block; the Mobzilla spawn tracker is reset afterwards if it was clear before.
     */
    private static final class GodzillaProbe extends HunterProbe {
        private MobzillaSpawnTracker tracker;
        private boolean spawnedBefore;

        @Override
        public void setUp(GameTestHelper helper) {
            this.tracker = MobzillaSpawnTracker.get(helper.getLevel());
            this.spawnedBefore = this.tracker.hasSpawned();
            this.hunter = spawnFrozen(helper, ModEntities.GODZILLA.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, PREY_POS);
            this.hunter.setTarget(this.prey);
            replaceRandom(this.hunter, rolls(200, 1, 5, 1, 65, 0, 20, 0, 4, 1, 3, 0));
            helper.assertTrue(this.hunter.getTarget() == this.prey && ((Godzilla) this.hunter).getAttacking() == 0,
                    "precondition: the pig is the stored target and attacking is 0 (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper, boolean playNicely) {
            invokeCustomServerAiStep(this.hunter);
            int attacking = ((Godzilla) this.hunter).getAttacking();
            this.trace = "attacking=" + attacking + ", target " + describe(this.hunter.getTarget());
            if (playNicely) {
                helper.assertTrue(this.hunter.getTarget() == this.prey, "with playNicely on the stored target must be kept:"
                        + " orig Godzilla.java:357-359 nulls the pass's local `e`, not the stored attack target — saw "
                        + this.trace + " (" + FINDING + "; BOSS-017 had mapped it to setTarget(null))");
            }
            return attacking == 1;
        }

        @Override
        public void reset(GameTestHelper helper) {
            ((Godzilla) this.hunter).setAttacking(0);
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            super.cleanUp(helper);
            if (this.tracker != null && !this.spawnedBefore) {
                this.tracker.reset();
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the PeacefulGateParityTests / IgnoreScreenParityTests idiom)
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: the " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see the "
                + prey.getClass().getSimpleName() + " inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
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

    /** Frozen prey with 1000 HP, so no pinned hit kills it and each drive reads a clean health drop. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    /** Back to full health with the hurt cooldown cleared: LivingEntity.hurt refuses a same-size hit while invulnerableTime &gt; 10. */
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
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server defaults to
     * CREATIVE, GameTestServer.java:85). Health is raised so nothing incidental can kill it. Deprecated
     * mock-player factory tolerated the way LeonTargetingTests and PeacefulGateParityTests do.
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
     * A plain {@link ServerPlayer} put on the player list the way the framework's mock is, without the
     * framework's override: {@code GameTestHelper.makeMockServerPlayerInLevel} answers {@code isCreative()}
     * true whatever its game mode, and the Pointysaurus's stare goal (a port-only goal, ledger batch T9)
     * refuses {@code isCreative()} players outright, so its control needs a player whose answer follows the
     * mode. The vanilla target goals read the abilities, which the game mode does set on the mock.
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

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as PeacefulGateParityTests.rolls. */
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

    /** The hunter's protected {@code customServerAiStep} — the port's shape of orig updateAITasks. */
    private static void invokeCustomServerAiStep(Mob hunter) {
        invoke(hunter, hunter.getClass(), "customServerAiStep");
    }

    private static Object invoke(Object target, Class<?> declaring, String name) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name);
            method.setAccessible(true);
            return method.invoke(target);
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

    private static String describe(Entity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }

    private static String describe(Goal goal) {
        if (goal instanceof NearestAttackableTargetGoal<?> nearest) {
            Class<?> targetType = (Class<?>) readField(nearest, NearestAttackableTargetGoal.class, "targetType");
            return "NearestAttackableTargetGoal<" + targetType.getSimpleName() + ">";
        }
        return goal.getClass().getSimpleName();
    }
}
