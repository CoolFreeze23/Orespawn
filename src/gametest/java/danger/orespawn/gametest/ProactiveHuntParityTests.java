package danger.orespawn.gametest;

import danger.orespawn.ModDimensionKeys;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AttackSquid;
import danger.orespawn.entity.EntityButterfly;
import danger.orespawn.entity.WaterDragon;
import danger.orespawn.entity.ai.AmbientFlightGoal;
import danger.orespawn.entity.ai.ButterflyIslandsHuntGoal;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-117 (targeting ledger T3a, wave 2): the four hunters whose whole 1.7.10 proactive hunt the port had
 * dropped, restored in classic — the Attack Squid's 1-in-10 pass over a 10/4/10 box with its whitelist ladder,
 * buddy adoption, {@code wasshot} rule and sticky stored target (orig AttackSquid.java:502-518, :551-625); the
 * Water Dragon's {@code !PEACEFUL && nextInt(5)==1} pass over a 14/4/14 box with its Water-Dragon exclusion,
 * tamed and baby rules and the shared non-mob fallthrough, fed to the port's melee goal through the slot under
 * the ENT-S-108 ownership mark (orig WaterDragon.java:597-612, :650-706); the Dragon's continuous vanilla IMob
 * channel — {@code EntityAINearestAttackableTarget(EntityLiving.class, 0, true, false, IMob.mobSelector)} at
 * target priority 1 when {@code PlayNicely == 0}, the revenge task at 2 (orig Dragon.java:115-118) — as a live-
 * gated {@code NearestAttackableTargetGoal<Mob>} with an {@code instanceof Enemy} selector (the IMob convention,
 * ENT-S-124; it was {@code <Monster>} until then) holding orig's follow range 16; and the butterfly's
 * Islands-dimension vampire hunt in the else-branch of its flight retarget (orig EntityButterfly.java:161-169,
 * :183-230) as {@link ButterflyIslandsHuntGoal}.
 *
 * <p>A {@link GameTestGenerator} over {@link #sites()} in orig file order, one synchronous {@link TestFunction}
 * per (species, aspect) row, {@code proactivehuntparitytests.s3a_NN_<species>_<site>}. Scan rows call the private
 * scan by reflection (the TargetScanParityTests idiom) on a frozen hunter (goals stripped, noAi) with its prey in
 * clear line of sight; cadence and pass rows invoke {@code customServerAiStep} (or the goal's {@code tick}) once
 * under a forced {@code Entity.random} (the VortexParityTests.ForcedRoll seam, chained as the PlayNicelyGate
 * tests' {@code rolls}); the term-order rows count the rolls a pass spends through a delegating
 * {@link CountingRandom}. Goal rows read the goal off the hunter's selectors (the PlayNicelyGateParityTests
 * GoalProbe idiom). Every flag flip ({@code PLAY_NICELY.set}, {@code MinecraftServer.setDifficulty(PEACEFUL,
 * true)}) is restored in a finally; every spawn is discarded and every mock player removed in a finally; the
 * mock player's abilities carry the mode (the framework's mock answers {@code isCreative()} true whatever its
 * mode, and every predicate of this batch reads {@code Abilities.instabuild}). Geometry as the ENT-S-106 tests:
 * the hunter at rel (20,1,24) on the floor of the 48x16x48 empty_large, prey east on the same floor; the
 * box_pinned rows put the hunter in the south-west corner (4,1,4) so every +x / +y / +z probe stays inside the
 * template; the sight rows raise a stone column at x 24 between hunter and prey and clear it again, resetting
 * the hunter's {@code Sensing} cache between the two scans. Own batch (TEST-003).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ProactiveHuntParityTests {

    private static final String BATCH = "proactiveHuntParity";
    private static final String TEST_PREFIX = "proactivehuntparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-117";

    /** The hunter on the template floor; prey east of it on the same floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside every box of this batch (10/4/10, 14/4/14, 8/5/8 and the Dragon goal's 16/4/16). */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** 6 and 5 blocks east: the tie-break pairs (the farther creeper / horse must outrank the nearer zombie / player). */
    private static final BlockPos SIX_PREY_POS = new BlockPos(26, 1, 24);
    private static final BlockPos FIVE_PREY_POS = new BlockPos(25, 1, 24);
    /** 3 blocks east: inside the Attack Squid's distSq &lt; 9 melee test only from 2; used as the nearer scan candidate. */
    private static final BlockPos NEAR_PREY_POS = new BlockPos(23, 1, 24);
    /** 2 blocks east: inside the squid's distSq &lt; 9 (orig AttackSquid.java:505) and the butterfly's distSq &lt; 6 bite (orig :166). */
    private static final BlockPos TOUCH_PREY_POS = new BlockPos(22, 1, 24);
    /** 24 blocks east: outside every box of this batch (the widest is 14 + the Water Dragon's half-width 0.625), inside the template. */
    private static final BlockPos FAR_POS = new BlockPos(44, 1, 24);
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);
    private static final Vec3 FIVE_PLAYER_POS = new Vec3(25.5, 1.0, 24.5);
    private static final Vec3 TOUCH_PLAYER_POS = new Vec3(22.5, 1.0, 24.5);
    /** box_pinned: the hunter in the south-west corner, so every +x / +z probe stays inside the template. */
    private static final BlockPos CORNER_HUNTER_POS = new BlockPos(4, 1, 4);
    /** A probe's centre sits this far past the box edge (outside) or short of it (inside). */
    private static final double OUTSIDE_MARGIN = 1.0;
    private static final double INSIDE_MARGIN = 0.5;
    /** The stone column between HUNTER_POS and PREY_POS that blocks the eye-to-eye ray (rel x 24, z 24, y 1..6). */
    private static final int COLUMN_X = 24;
    private static final int COLUMN_Z = 24;
    private static final int COLUMN_TOP = 6;
    /** Prey health, high enough that no pinned hit kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** orig EntityLiving's follow-range base, the Dragon goal's box, range and hold (Dragon.java:135-141 sets none). */
    private static final double DRAGON_GOAL_RANGE = 16.0;

    // ------------------------------------------------------------------
    // The site table, in orig file order
    // ------------------------------------------------------------------

    private record Site(int index, String species, String tag, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + String.format("s3a_%02d_%s_%s", this.index, this.species, this.tag);
        }
    }

    private static List<Site> sites() {
        List<Site> sites = new ArrayList<>();
        int n = 0;
        // AttackSquid — orig AttackSquid.java
        sites.add(new Site(++n, "attacksquid", "502_cadence_pick_transient", ProactiveHuntParityTests::squidCadence));
        sites.add(new Site(++n, "attacksquid", "608_box_pinned", ProactiveHuntParityTests::squidBoxPinned));
        sites.add(new Site(++n, "attacksquid", "561_sight", ProactiveHuntParityTests::squidSight));
        sites.add(new Site(++n, "attacksquid", "566_creative", ProactiveHuntParityTests::squidCreative));
        sites.add(new Site(++n, "attacksquid", "568_girlfriend", h -> squidLadder(h, ModEntities.GIRLFRIEND.get(), true, ":568-570", "a Girlfriend")));
        sites.add(new Site(++n, "attacksquid", "571_boyfriend", h -> squidLadder(h, ModEntities.BOYFRIEND.get(), true, ":571-573", "a Boyfriend")));
        sites.add(new Site(++n, "attacksquid", "574_zombie", h -> squidLadder(h, EntityType.ZOMBIE, true, ":574-576", "a vanilla Zombie")));
        sites.add(new Site(++n, "attacksquid", "577_villager", h -> squidLadder(h, EntityType.VILLAGER, true, ":577-579", "a vanilla Villager")));
        sites.add(new Site(++n, "attacksquid", "580_spider", h -> squidLadder(h, EntityType.SPIDER, true, ":580-582", "a vanilla Spider")));
        sites.add(new Site(++n, "attacksquid", "583_cavespider", h -> squidLadder(h, EntityType.CAVE_SPIDER, true, ":583-585 (through the Spider step of :580, a Spider subclass in both trees)", "a vanilla Cave Spider")));
        sites.add(new Site(++n, "attacksquid", "586_ghost_refused", h -> squidLadder(h, ModEntities.GHOST.get(), false, ":586-588", "a Ghost")));
        sites.add(new Site(++n, "attacksquid", "589_ghostskelly_refused", h -> squidLadder(h, ModEntities.GHOST_SKELLY.get(), false, ":589-591", "a GhostSkelly")));
        sites.add(new Site(++n, "attacksquid", "592_lizard", h -> squidLadder(h, ModEntities.LIZARD.get(), true, ":592-594", "a Lizard")));
        sites.add(new Site(++n, "attacksquid", "595_buddy_adoption", ProactiveHuntParityTests::squidBuddyAdoption));
        sites.add(new Site(++n, "attacksquid", "601_wasshot", ProactiveHuntParityTests::squidWasShot));
        sites.add(new Site(++n, "attacksquid", "609_tiebreak_sorter", ProactiveHuntParityTests::squidTieBreak));
        sites.add(new Site(++n, "attacksquid", "605_playnicely", ProactiveHuntParityTests::squidPlayNicely));
        sites.add(new Site(++n, "attacksquid", "613_sticky_stored_target", ProactiveHuntParityTests::squidStickyStored));
        sites.add(new Site(++n, "attacksquid", "515_buddy_follow", ProactiveHuntParityTests::squidBuddyFollow));
        // Dragon — orig Dragon.java:115-118
        sites.add(new Site(++n, "dragon", "116_goal_registered_priority_range", ProactiveHuntParityTests::dragonGoalRegistered));
        sites.add(new Site(++n, "dragon", "116_monster_taken_and_stored", ProactiveHuntParityTests::dragonMonsterTaken));
        sites.add(new Site(++n, "dragon", "115_playnicely", ProactiveHuntParityTests::dragonPlayNicely));
        sites.add(new Site(++n, "dragon", "116_follow_range_16_box_4", ProactiveHuntParityTests::dragonFollowRange));
        sites.add(new Site(++n, "dragon", "116_sight", ProactiveHuntParityTests::dragonSight));
        // EntityButterfly — orig EntityButterfly.java
        sites.add(new Site(++n, "butterfly", "145_hunt_goal_registered", ProactiveHuntParityTests::butterflyGoalRegistered));
        sites.add(new Site(++n, "butterfly", "218_box_pinned", ProactiveHuntParityTests::butterflyBoxPinned));
        sites.add(new Site(++n, "butterfly", "207_sight", ProactiveHuntParityTests::butterflySight));
        sites.add(new Site(++n, "butterfly", "212_creative", ProactiveHuntParityTests::butterflyCreative));
        sites.add(new Site(++n, "butterfly", "214_horse_only", ProactiveHuntParityTests::butterflyHorseOnly));
        sites.add(new Site(++n, "butterfly", "219_tiebreak_sorter", ProactiveHuntParityTests::butterflyTieBreak));
        sites.add(new Site(++n, "butterfly", "195_filter_peaceful", ProactiveHuntParityTests::butterflyFilterPeaceful));
        sites.add(new Site(++n, "butterfly", "187_bite_roll_and_peaceful", ProactiveHuntParityTests::butterflyBite));
        sites.add(new Site(++n, "butterfly", "161_roll_first_islands_only", ProactiveHuntParityTests::butterflyOverworldNoHunt));
        // WaterDragon — orig WaterDragon.java
        sites.add(new Site(++n, "waterdragon", "597_cadence_pick_stored", ProactiveHuntParityTests::waterDragonCadence));
        sites.add(new Site(++n, "waterdragon", "597_peaceful_before_roll", ProactiveHuntParityTests::waterDragonPeacefulPass));
        sites.add(new Site(++n, "waterdragon", "651_filter_peaceful", ProactiveHuntParityTests::waterDragonFilterPeaceful));
        sites.add(new Site(++n, "waterdragon", "689_box_pinned", ProactiveHuntParityTests::waterDragonBoxPinned));
        sites.add(new Site(++n, "waterdragon", "663_sight", ProactiveHuntParityTests::waterDragonSight));
        sites.add(new Site(++n, "waterdragon", "666_waterdragon_refused", ProactiveHuntParityTests::waterDragonExcluded));
        sites.add(new Site(++n, "waterdragon", "672_tamed_monster_only", ProactiveHuntParityTests::waterDragonTamed));
        sites.add(new Site(++n, "waterdragon", "677_creative", ProactiveHuntParityTests::waterDragonCreative));
        sites.add(new Site(++n, "waterdragon", "679_nonmob_helper", ProactiveHuntParityTests::waterDragonNonMobHelper));
        sites.add(new Site(++n, "waterdragon", "686_baby_never_hunts", ProactiveHuntParityTests::waterDragonBaby));
        sites.add(new Site(++n, "waterdragon", "683_playnicely", ProactiveHuntParityTests::waterDragonPlayNicely));
        sites.add(new Site(++n, "waterdragon", "690_tiebreak_sorter", ProactiveHuntParityTests::waterDragonTieBreak));
        sites.add(new Site(++n, "waterdragon", "694_sticky_stored_target", ProactiveHuntParityTests::waterDragonStickyStored));
        sites.add(new Site(++n, "waterdragon", "598_own_pick_rederived", ProactiveHuntParityTests::waterDragonOwnPickRederived));
        sites.add(new Site(++n, "waterdragon", "683_gated_pass_slot", ProactiveHuntParityTests::waterDragonGatedPassSlot));
        sites.add(new Site(++n, "waterdragon", "slot_reassert_keeps_ownership", ProactiveHuntParityTests::waterDragonSlotReassert));
        sites.add(new Site(++n, "waterdragon", "490_hurt_ends_ownership", ProactiveHuntParityTests::waterDragonHurtHandsOff));
        sites.add(new Site(++n, "waterdragon", "479_swallowed_hit_keeps_ownership", ProactiveHuntParityTests::waterDragonSwallowedHitKeepsOwnership));
        return sites;
    }

    /** One test per row: 50 TestFunctions in the {@code proactiveHuntParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> proactiveHuntSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true, helper -> {
                helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                        "precondition: the game-test level runs at NORMAL, not Peaceful — the Water Dragon and butterfly"
                                + " filters refuse everything on Peaceful (" + FINDING + " test setup)");
                site.body().accept(helper);
                helper.succeed();
            }));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // AttackSquid — orig AttackSquid.java:502-518 (the pass), :551-602 (the filter), :604-625 (the scan)
    // ------------------------------------------------------------------

    /**
     * orig AttackSquid.java:502-506, :622: on the nextInt(10)==1 tick the pass finds a Lizard 2 blocks off (distSq
     * &lt; 9) and sets attacking 1 while the slot stays empty — the pick is never stored; a 0 on the same die is
     * the water-seek roll (:476), not the hunt.
     */
    private static void squidCadence(GameTestHelper helper) {
        AttackSquid squid = null;
        Mob lizard = null;
        try {
            squid = (AttackSquid) spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            lizard = spawnPrey(helper, ModEntities.LIZARD.get(), TOUCH_PREY_POS);
            assertPlayNicelyOff(helper);
            assertSeen(helper, squid, lizard, "a Lizard 2 blocks off");
            helper.assertTrue(squid.getAttacking() == 0 && squid.getTarget() == null && squid.distanceToSqr(lizard) < 9.0,
                    "precondition: attacking 0, an empty slot and the Lizard inside distSq 9 (" + FINDING + " test setup)");
            replaceRandom(squid, rolls(10, 1, 25, 0, 4, 1, 5, 0));
            invokeCustomServerAiStep(squid);
            helper.assertTrue(squid.getAttacking() == 1, "AttackSquid.customServerAiStep (orig AttackSquid.java:502-506): on the"
                    + " nextInt(10)==1 tick the hunt must find the Lizard (orig :592) inside distSq 9 and set attacking 1 — the"
                    + " port used to read the empty slot only (" + FINDING + "); attacking=" + squid.getAttacking());
            helper.assertTrue(squid.getTarget() == null, "orig AttackSquid.java:622 — the hunt's pick is returned, never"
                    + " stored: the slot must still be empty after the pass (" + FINDING + "); got " + describe(squid.getTarget()));
            squid.setAttacking(0);
            replaceRandom(squid, rolls(10, 0, 25, 0, 4, 1, 5, 0));
            invokeCustomServerAiStep(squid);
            helper.assertTrue(squid.getAttacking() == 0, "orig AttackSquid.java:502 rolls nextInt(10) == 1, not == 0 (0 is the"
                    + " water-seek die of :476): a 0 must not hunt (" + FINDING + "); attacking=" + squid.getAttacking());
        } finally {
            discardQuietly(lizard);
            discardQuietly(squid);
        }
    }

    /** orig AttackSquid.java:608 — the 10/4/10 box, pinned on +x, +y, +z from both sides with a Zombie (orig :574). */
    private static void squidBoxPinned(GameTestHelper helper) {
        assertBoxPinned(helper, ModEntities.ATTACK_SQUID.get(), EntityType.ZOMBIE, "a vanilla Zombie (orig :574)", 10.0, 4.0, 10.0,
                "orig AttackSquid.java:608", ProactiveHuntParityTests::scan);
    }

    /** orig AttackSquid.java:561-563 — a Zombie behind the stone column is refused; the same Zombie is the pick once the column is gone. */
    private static void squidSight(GameTestHelper helper) {
        assertSightStep(helper, ModEntities.ATTACK_SQUID.get(), EntityType.ZOMBIE, "a vanilla Zombie (orig :574)",
                "orig AttackSquid.java:561-563", ProactiveHuntParityTests::scan);
    }

    /** orig AttackSquid.java:564-567 — a creative mock player is refused; the same player in survival is the pick. */
    private static void squidCreative(GameTestHelper helper) {
        assertCreativeStep(helper, ModEntities.ATTACK_SQUID.get(), "orig AttackSquid.java:564-567", ProactiveHuntParityTests::scan);
    }

    /** orig AttackSquid.java:568-594 — one whitelist / exclusion member on the prey spot; a Zombie on the same spot as the control for a refusal. */
    private static void squidLadder(GameTestHelper helper, EntityType<? extends Mob> type, boolean accepted, String lines, String why) {
        Mob squid = null;
        Mob species = null;
        Mob control = null;
        try {
            squid = spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            species = spawnPrey(helper, type, PREY_POS);
            assertSeen(helper, squid, species, why);
            LivingEntity pick = scan(squid);
            if (accepted) {
                helper.assertTrue(pick == species, "AttackSquid.findSomethingToAttack (orig AttackSquid.java:604-625): " + why
                        + " 8 blocks off, inside the 10/4/10 box and in sight, must be the pick — the whitelist step of orig " + lines
                        + " takes it (" + FINDING + "); got " + describe(pick));
            } else {
                helper.assertTrue(pick == null, "AttackSquid.findSomethingToAttack (orig AttackSquid.java:604-625): " + why
                        + " alone in the 10/4/10 box must leave the scan empty — orig " + lines + " refuses it (" + FINDING + "); got "
                        + describe(pick));
                species.discard();
                species = null;
                control = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
                assertSeen(helper, squid, control, "a vanilla Zombie");
                pick = scan(squid);
                helper.assertTrue(pick == control, "control: a Zombie on the same spot must be the pick (orig :574), so " + why
                        + " was refused by the ladder and not by geometry or sight (" + FINDING + "); got " + describe(pick));
            }
        } finally {
            discardQuietly(control);
            discardQuietly(species);
            discardQuietly(squid);
        }
    }

    /** orig AttackSquid.java:595-600 — another Attack Squid is refused and adopted as the buddy on nextInt(5)==1, not on a 0. */
    private static void squidBuddyAdoption(GameTestHelper helper) {
        Mob squid = null;
        Mob other = null;
        try {
            squid = spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            other = spawnPrey(helper, ModEntities.ATTACK_SQUID.get(), PREY_POS);
            assertSeen(helper, squid, other, "another Attack Squid");
            helper.assertTrue(readField(squid, AttackSquid.class, "buddy") == null, "precondition: no buddy yet (" + FINDING + " test setup)");
            replaceRandom(squid, rolls(5, 1));
            LivingEntity pick = scan(squid);
            Object buddy = readField(squid, AttackSquid.class, "buddy");
            helper.assertTrue(pick == null, "AttackSquid.isSuitableTarget (orig AttackSquid.java:595-600): another Attack Squid"
                    + " is never prey (" + FINDING + "); got " + describe(pick));
            helper.assertTrue(buddy == other, "orig AttackSquid.java:596-598: on nextInt(5)==1 the other squid is adopted as the"
                    + " buddy (" + FINDING + "); buddy=" + describe((Entity) buddy));
            writeField(squid, AttackSquid.class, "buddy", null);
            replaceRandom(squid, rolls(5, 0));
            pick = scan(squid);
            buddy = readField(squid, AttackSquid.class, "buddy");
            helper.assertTrue(pick == null && buddy == null, "orig AttackSquid.java:596 rolls nextInt(5) == 1: a 0 adopts nothing"
                    + " and the squid stays refused (" + FINDING + "); pick=" + describe(pick) + ", buddy=" + describe((Entity) buddy));
        } finally {
            discardQuietly(other);
            discardQuietly(squid);
        }
    }

    /** orig AttackSquid.java:601 — a pig (on no step of the ladder) is refused, and prey once the squid is a Squid Zooka shot (setWasShot, :94-96). */
    private static void squidWasShot(GameTestHelper helper) {
        AttackSquid squid = null;
        Mob pig = null;
        try {
            squid = (AttackSquid) spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSeen(helper, squid, pig, "a vanilla pig");
            LivingEntity pick = scan(squid);
            helper.assertTrue(pick == null, "AttackSquid.isSuitableTarget (orig AttackSquid.java:601): with wasshot 0 a pig — on"
                    + " no step of the ladder — must be refused (" + FINDING + "); got " + describe(pick));
            squid.setWasShot();
            pick = scan(squid);
            helper.assertTrue(pick == pig, "orig AttackSquid.java:601 — `return this.wasshot != 0`: a Squid Zooka-launched squid"
                    + " (setWasShot, orig :94-96) takes every living thing, the pig included (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(pig);
            discardQuietly(squid);
        }
    }

    /**
     * orig AttackSquid.java:54, :70, :609 — the GenericTargetSorter: with the squid shot, a creeper 6 blocks off
     * (distSq halved, orig GenericTargetSorter.java:21-23) outranks a Zombie 5 blocks off; unshot, the creeper falls
     * to :601 and the Zombie is the pick. A plain-nearest order would answer the Zombie both times.
     */
    private static void squidTieBreak(GameTestHelper helper) {
        AttackSquid squid = null;
        Mob zombie = null;
        Mob creeper = null;
        try {
            squid = (AttackSquid) spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, FIVE_PREY_POS);
            creeper = spawnPrey(helper, EntityType.CREEPER, SIX_PREY_POS);
            assertSeen(helper, squid, zombie, "a Zombie 5 blocks off");
            assertSeen(helper, squid, creeper, "a Creeper 6 blocks off");
            helper.assertTrue(squid.distanceToSqr(zombie) < squid.distanceToSqr(creeper)
                            && squid.distanceToSqr(creeper) / 2.0 < squid.distanceToSqr(zombie),
                    "precondition: the Zombie is nearer, the halved creeper distance nearer still (" + FINDING + " test geometry)");
            LivingEntity pick = scan(squid);
            helper.assertTrue(pick == zombie, "AttackSquid.findSomethingToAttack: unshot, the creeper is refused at orig :601 and"
                    + " the Zombie (orig :574) is the pick (" + FINDING + "); got " + describe(pick));
            squid.setWasShot();
            pick = scan(squid);
            helper.assertTrue(pick == creeper, "AttackSquid.findSomethingToAttack (orig AttackSquid.java:609, GenericTargetSorter"
                    + " :21-23): shot, both are prey and the creeper's halved distance must outrank the nearer Zombie — a"
                    + " plain-nearest order would answer the Zombie (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(creeper);
            discardQuietly(zombie);
            discardQuietly(squid);
        }
    }

    /** orig AttackSquid.java:605-607 — PlayNicely != 0 answers null ahead of the scan, read live. */
    private static void squidPlayNicely(GameTestHelper helper) {
        assertPlayNicelyGate(helper, ModEntities.ATTACK_SQUID.get(), EntityType.ZOMBIE, "a vanilla Zombie (orig :574)",
                "orig AttackSquid.java:605-607", ProactiveHuntParityTests::scan);
    }

    /**
     * orig AttackSquid.java:613-617 — a live stored target (the hurt / revenge channel) is answered ahead of the
     * loop, even with a nearer Lizard in the box; once it is dead the slot is cleared and the Lizard is the pick.
     */
    private static void squidStickyStored(GameTestHelper helper) {
        Mob squid = null;
        Mob zombie = null;
        Mob lizard = null;
        try {
            squid = spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            lizard = spawnPrey(helper, ModEntities.LIZARD.get(), NEAR_PREY_POS);
            assertSeen(helper, squid, zombie, "a Zombie 8 blocks off");
            assertSeen(helper, squid, lizard, "a Lizard 3 blocks off");
            squid.setTarget(zombie);
            LivingEntity pick = scan(squid);
            helper.assertTrue(pick == zombie, "AttackSquid.findSomethingToAttack (orig AttackSquid.java:613-616): the live stored"
                    + " target is answered ahead of the loop, the nearer Lizard notwithstanding (" + FINDING + "); got " + describe(pick));
            helper.assertTrue(squid.getTarget() == zombie, "the sticky return leaves the slot as it was (" + FINDING + "); got "
                    + describe(squid.getTarget()));
            zombie.discard();
            pick = scan(squid);
            helper.assertTrue(pick == lizard, "orig AttackSquid.java:617-623 — a dead stored target is cleared and the scan runs:"
                    + " the Lizard (orig :592) is the pick (" + FINDING + "); got " + describe(pick));
            helper.assertTrue(squid.getTarget() == null, "orig AttackSquid.java:617, :622 — the dead target is cleared and the pick"
                    + " is not stored: the slot must be empty (" + FINDING + "); got " + describe(squid.getTarget()));
        } finally {
            discardQuietly(lizard);
            discardQuietly(zombie);
            discardQuietly(squid);
        }
    }

    /**
     * orig AttackSquid.java:515-518 — with nothing to attack and a buddy adopted, the pass navigates to the buddy at
     * 1.0 and sets attacking 0. The squid is put on the ground so its ground navigation can build a path (a
     * precondition proves one exists before the pass), the navigation is stopped, and the pass must start it again
     * toward the buddy.
     */
    private static void squidBuddyFollow(GameTestHelper helper) {
        AttackSquid squid = null;
        Mob other = null;
        try {
            squid = (AttackSquid) spawnFrozen(helper, ModEntities.ATTACK_SQUID.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            other = spawnPrey(helper, ModEntities.ATTACK_SQUID.get(), PREY_POS);
            writeField(squid, AttackSquid.class, "buddy", other);
            squid.setOnGround(true);
            Path probe = squid.getNavigation().createPath(other, 1);
            helper.assertTrue(probe != null, "precondition: the squid's ground navigation must be able to path to the buddy 8"
                    + " blocks off on the template floor (" + FINDING + " test setup)");
            squid.getNavigation().stop();
            helper.assertTrue(!squid.getNavigation().isInProgress() && squid.getAttacking() == 0 && squid.getTarget() == null,
                    "precondition: navigation idle, attacking 0, an empty slot (" + FINDING + " test setup)");
            replaceRandom(squid, rolls(10, 1, 25, 0, 5, 0));
            invokeCustomServerAiStep(squid);
            helper.assertTrue(squid.getNavigation().isInProgress(), "AttackSquid.customServerAiStep (orig AttackSquid.java:515-517):"
                    + " with the other squid refused (orig :595-600) and the buddy adopted, the pass must navigate toward the buddy"
                    + " (" + FINDING + ")");
            BlockPos navTarget = squid.getNavigation().getTargetPos();
            helper.assertTrue(navTarget != null && navTarget.distSqr(other.blockPosition()) <= 4.0, "the navigation target must be"
                    + " the buddy's block (orig :516) (" + FINDING + "); got " + navTarget + " for the buddy at " + other.blockPosition());
            helper.assertTrue(squid.getAttacking() == 0 && squid.getTarget() == null, "orig AttackSquid.java:518 — attacking 0 and"
                    + " nothing stored on a buddy-follow pass (" + FINDING + "); attacking=" + squid.getAttacking() + ", slot "
                    + describe(squid.getTarget()));
        } finally {
            discardQuietly(other);
            discardQuietly(squid);
        }
    }

    // ------------------------------------------------------------------
    // Dragon — orig Dragon.java:115-118 (the IMob target task at priority 1, the revenge task at 2)
    // ------------------------------------------------------------------

    /**
     * orig Dragon.java:116, :118: the target selector holds a NearestAttackableTargetGoal&lt;Mob&gt; with an
     * {@code instanceof Enemy} selector — the IMob convention (ENT-S-124: orig's IMob was an interface, every EntityMob
     * plus Slime / MagmaCube / Ghast / EnderDragon and orig's Mothra; the {@code <Monster>} form dropped the non-Monster
     * Enemies) — at priority 1 and the HurtByTargetGoal at 2; no Monster-typed goal remains; the goal rolls nothing
     * (orig targetChance 0 → randomInterval 0) and keeps orig's follow range 16 for its box, range and hold, while
     * the port's FOLLOW_RANGE attribute is 40. The selector is asked directly: a Slime (an Enemy that is no Monster —
     * the row the Monster form failed) passes, a pig does not.
     */
    private static void dragonGoalRegistered(GameTestHelper helper) {
        Mob dragon = null;
        Mob slime = null;
        Mob pig = null;
        try {
            dragon = spawnWithGoals(helper, ModEntities.DRAGON.get(), HUNTER_POS);
            WrappedGoal imobGoal = null;
            WrappedGoal hurtBy = null;
            for (WrappedGoal wrapped : dragon.targetSelector.getAvailableGoals()) {
                if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest) {
                    Object targetType = readField(nearest, NearestAttackableTargetGoal.class, "targetType");
                    helper.assertTrue(targetType != Monster.class, "no NearestAttackableTargetGoal<Monster> may remain on the Dragon —"
                            + " Monster stood in for IMob until the IMob convention (ENT-S-124) (" + FINDING + ")");
                    if (targetType == Mob.class) {
                        helper.assertTrue(imobGoal == null, "exactly one NearestAttackableTargetGoal<Mob> on the Dragon (" + FINDING + ")");
                        imobGoal = wrapped;
                    }
                } else if (wrapped.getGoal() instanceof HurtByTargetGoal) {
                    hurtBy = wrapped;
                }
            }
            helper.assertTrue(imobGoal != null, "Dragon.registerGoals (orig Dragon.java:116): the target selector must carry a"
                    + " NearestAttackableTargetGoal<Mob> — the port's shape of EntityAINearestAttackableTarget(EntityLiving.class,"
                    + " IMob.mobSelector) under the IMob convention (ENT-S-124) (" + FINDING + ")");
            helper.assertTrue(imobGoal.getPriority() == 1, "orig Dragon.java:116 registers the IMob task at target priority 1 ("
                    + FINDING + "); got " + imobGoal.getPriority());
            helper.assertTrue(hurtBy != null && hurtBy.getPriority() == 2, "orig Dragon.java:118 registers EntityAIHurtByTarget at"
                    + " target priority 2, behind the IMob task (" + FINDING + "); got " + (hurtBy == null ? "none" : String.valueOf(hurtBy.getPriority())));
            int interval = (Integer) readField(imobGoal.getGoal(), NearestAttackableTargetGoal.class, "randomInterval");
            helper.assertTrue(interval == 0, "orig Dragon.java:116 passes targetChance 0 — no roll; the port's randomInterval must"
                    + " be 0 (reducedTickDelay(0)), not the 4-arg constructor's 10 → 5 (" + FINDING + "); got " + interval);
            double range = (Double) invoke(imobGoal.getGoal(), TargetGoal.class, "getFollowDistance");
            helper.assertTrue(dragon.getAttributeValue(Attributes.FOLLOW_RANGE) == 40.0,
                    "precondition: the port Dragon's FOLLOW_RANGE attribute is 40 (Dragon.createAttributes) (" + FINDING + " test setup)");
            helper.assertTrue(range == DRAGON_GOAL_RANGE, "orig EntityAITarget.getTargetDistance() = the follow-range attribute,"
                    + " EntityLiving's base 16 (Dragon.java:135-141 sets none): the goal's getFollowDistance must answer 16, not the"
                    + " port attribute's 40 (" + FINDING + "); got " + range);
            Object conditions = readField(imobGoal.getGoal(), NearestAttackableTargetGoal.class, "targetConditions");
            @SuppressWarnings("unchecked")
            Predicate<LivingEntity> selector = (Predicate<LivingEntity>) readField(conditions, TargetingConditions.class, "selector");
            helper.assertTrue(selector != null, "the Mob-typed goal must carry a selector — the port's IMob.mobSelector (ENT-S-124)"
                    + " (" + FINDING + ")");
            slime = spawnPrey(helper, EntityType.SLIME, PREY_POS);
            pig = spawnPrey(helper, EntityType.PIG, NEAR_PREY_POS);
            helper.assertTrue(slime instanceof Enemy && !(slime instanceof Monster),
                    "precondition: a vanilla Slime is an Enemy and no Monster (" + FINDING + " test setup)");
            helper.assertTrue(selector.test(slime), "the selector is the Enemy test: a Slime — IMob in 1.7.10 (EntitySlime implements"
                    + " IMob), an Enemy and no Monster here — must pass (ENT-S-124) (" + FINDING + ")");
            helper.assertTrue(!selector.test(pig), "the selector is the Enemy test: a pig — never IMob, no Enemy — must not pass"
                    + " (ENT-S-124) (" + FINDING + ")");
        } finally {
            discardQuietly(pig);
            discardQuietly(slime);
            discardQuietly(dragon);
        }
    }

    /** orig Dragon.java:116 — a Zombie 8 blocks off is taken (canUse) and stored on start; a pig is not IMob and is refused. */
    private static void dragonMonsterTaken(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        Mob pig = null;
        try {
            dragon = spawnWithGoals(helper, ModEntities.DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            Goal goal = dragonImobGoal(dragon);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            helper.assertTrue(goal.canUse(), "Dragon's NearestAttackableTargetGoal<Mob> + Enemy (orig Dragon.java:116): a Zombie 8"
                    + " blocks off, in sight, inside the 16/4/16 box, must be taken (" + FINDING + ")");
            goal.start();
            helper.assertTrue(dragon.getTarget() == zombie, "orig Dragon.java:116-118 — channel (a) stores the IMob it finds as the"
                    + " attack target (the feed the port lacked) (" + FINDING + "); got " + describe(dragon.getTarget()));
            goal.stop();
            helper.assertTrue(dragon.getTarget() == null, "the goal's stop clears the slot (" + FINDING + " test cleanup)");
            zombie.discard();
            zombie = null;
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            assertSeen(helper, dragon, pig, "a vanilla pig 8 blocks off");
            helper.assertTrue(!goal.canUse(), "orig Dragon.java:116 — IMob.mobSelector: a pig is not prey of this channel (the port's"
                    + " Enemy selector, ENT-S-124) (" + FINDING + ")");
        } finally {
            discardQuietly(pig);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /** orig Dragon.java:115 — the task existed only with PlayNicely == 0; the port's goal reads the flag live in canUse. */
    private static void dragonPlayNicely(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        try {
            dragon = spawnWithGoals(helper, ModEntities.DRAGON.get(), HUNTER_POS);
            Goal goal = dragonImobGoal(dragon);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(goal.canUse(), "control: with playNicely off the Dragon's IMob goal takes a Zombie 8 blocks off (" + FINDING + ")");
            OreSpawnConfig.PLAY_NICELY.set(true);
            helper.assertTrue(!goal.canUse(), "Dragon's IMob goal with playNicely on: orig Dragon.java:115 registered the IMob task"
                    + " only when PlayNicely == 0, read live here, so canUse must be false (" + FINDING + ")");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig Dragon.java:116 with EntityAINearestAttackableTarget's box expand(d, 4, d), d = follow range 16: a Zombie 15
     * blocks east is taken, one 17 blocks east is not (the port's FOLLOW_RANGE 40 would have taken it), one 4.5 above
     * the dragon is taken, one 6 above is not (the box's 4).
     */
    private static void dragonFollowRange(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnWithGoals(helper, ModEntities.DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            Goal goal = dragonImobGoal(dragon);
            double x = dragon.getX();
            double y = dragon.getY();
            double z = dragon.getZ();
            Object[][] probes = {
                    {new Vec3(x + 15.0, y, z), true, "15 blocks east (inside orig's follow range 16)"},
                    {new Vec3(x + 18.0, y, z), false, "18 blocks east (outside orig's 16 box and range, inside the port attribute's 40)"},
                    {new Vec3(x, y + 4.5, z), true, "4.5 blocks above (inside the box's +4 over the dragon's 1.25 height)"},
                    {new Vec3(x, y + 6.0, z), false, "6 blocks above (outside the box's +4, inside the 16 range)"},
            };
            for (Object[] probe : probes) {
                Vec3 at = (Vec3) probe[0];
                boolean inside = (Boolean) probe[1];
                String where = "a Zombie " + probe[2];
                zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
                zombie.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
                assertSeen(helper, dragon, zombie, where);
                if (at.y == y) {
                    AABB portBox = dragon.getBoundingBox().inflate(40.0, 4.0, 40.0);
                    helper.assertTrue(portBox.intersects(zombie.getBoundingBox()) && dragon.distanceToSqr(zombie) < 40.0 * 40.0,
                            "precondition: " + where + " sits inside the port attribute's 40 box and sphere, so only the 16 override"
                                    + " can refuse it (" + FINDING + " test geometry)");
                } else {
                    helper.assertTrue(dragon.distanceToSqr(zombie) < DRAGON_GOAL_RANGE * DRAGON_GOAL_RANGE,
                            "precondition: " + where + " sits inside the 16 range, so only the box's +4 can refuse it ("
                                    + FINDING + " test geometry)");
                }
                boolean taken = goal.canUse();
                helper.assertTrue(taken == inside, "Dragon's IMob goal (orig Dragon.java:116, EntityAINearestAttackableTarget's"
                        + " box expand(16, 4, 16)): " + where + " must " + (inside ? "" : "not ") + "be taken (" + FINDING + ")");
                zombie.discard();
                zombie = null;
            }
        } finally {
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /** orig Dragon.java:116 — checkSight true: a Zombie behind the stone column is not taken; the same Zombie is once the column is gone. */
    private static void dragonSight(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnWithGoals(helper, ModEntities.DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            Goal goal = dragonImobGoal(dragon);
            column(helper, true);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            helper.assertTrue(!dragon.hasLineOfSight(zombie), "precondition: the stone column hides the Zombie from the Dragon ("
                    + FINDING + " test geometry)");
            helper.assertTrue(!goal.canUse(), "Dragon's IMob goal (orig Dragon.java:116, checkSight true): a Zombie behind the"
                    + " column must not be taken (" + FINDING + ")");
            column(helper, false);
            resetSight(dragon);
            assertSeen(helper, dragon, zombie, "the same Zombie with the column gone");
            helper.assertTrue(goal.canUse(), "control: the same Zombie in sight must be taken, so the column and not geometry refused"
                    + " it (" + FINDING + ")");
        } finally {
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    // ------------------------------------------------------------------
    // EntityButterfly — orig EntityButterfly.java:161-169 (the hunt branch), :183-192 (the bite), :194-230 (filter, scan)
    // ------------------------------------------------------------------

    /** orig EntityButterfly.java:145-181 — the flight goal in slot 8 is the hunt goal, and no plain AmbientFlightGoal remains. */
    private static void butterflyGoalRegistered(GameTestHelper helper) {
        Mob butterfly = null;
        try {
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            int hunt = 0;
            int plain = 0;
            for (WrappedGoal wrapped : butterfly.goalSelector.getAvailableGoals()) {
                if (wrapped.getGoal() instanceof ButterflyIslandsHuntGoal) {
                    hunt++;
                    helper.assertTrue(wrapped.getPriority() == 8, "the hunt goal sits in the flight goal's slot 8 (" + FINDING + "); got " + wrapped.getPriority());
                } else if (wrapped.getGoal() instanceof AmbientFlightGoal) {
                    plain++;
                }
            }
            helper.assertTrue(hunt == 1 && plain == 0, "EntityButterfly.registerGoals (orig EntityButterfly.java:145-181): the butterfly's"
                    + " flight must be the ButterflyIslandsHuntGoal — the flight plus the :161-169 hunt branch — and no plain"
                    + " AmbientFlightGoal may remain (" + FINDING + "); hunt goals=" + hunt + ", plain=" + plain);
        } finally {
            discardQuietly(butterfly);
        }
    }

    /** orig EntityButterfly.java:218 — the 8/5/8 box, pinned on +x, +y, +z from both sides with a horse (orig :214). */
    private static void butterflyBoxPinned(GameTestHelper helper) {
        assertBoxPinned(helper, ModEntities.ENTITY_BUTTERFLY.get(), EntityType.HORSE, "a vanilla horse (orig :214)", 8.0, 5.0, 8.0,
                "orig EntityButterfly.java:218", ProactiveHuntParityTests::butterflyScan);
    }

    /** orig EntityButterfly.java:207-209 — a horse behind the stone column is refused; the same horse is the pick once it is gone. */
    private static void butterflySight(GameTestHelper helper) {
        assertSightStep(helper, ModEntities.ENTITY_BUTTERFLY.get(), EntityType.HORSE, "a vanilla horse (orig :214)",
                "orig EntityButterfly.java:207-209", ProactiveHuntParityTests::butterflyScan);
    }

    /** orig EntityButterfly.java:210-213 — a creative mock player is refused; the same player in survival is the pick. */
    private static void butterflyCreative(GameTestHelper helper) {
        assertCreativeStep(helper, ModEntities.ENTITY_BUTTERFLY.get(), "orig EntityButterfly.java:210-213", ProactiveHuntParityTests::butterflyScan);
    }

    /** orig EntityButterfly.java:214 — a horse is prey; a pig and a Zombie on the same spot are not. */
    private static void butterflyHorseOnly(GameTestHelper helper) {
        Mob butterfly = null;
        Mob prey = null;
        try {
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            EntityType<? extends Mob>[] refused = new EntityType[] {EntityType.PIG, EntityType.ZOMBIE};
            String[] why = {"a vanilla pig", "a vanilla Zombie"};
            for (int i = 0; i < refused.length; i++) {
                prey = spawnPrey(helper, refused[i], PREY_POS);
                assertSeen(helper, butterfly, prey, why[i]);
                LivingEntity pick = butterflyScan(butterfly);
                helper.assertTrue(pick == null, "ButterflyIslandsHuntGoal.findSomethingToAttack (orig EntityButterfly.java:214): " + why[i]
                        + " is not a horse and must be refused (" + FINDING + "); got " + describe(pick));
                prey.discard();
                prey = null;
            }
            prey = spawnPrey(helper, EntityType.HORSE, PREY_POS);
            assertSeen(helper, butterfly, prey, "a vanilla horse");
            LivingEntity pick = butterflyScan(butterfly);
            helper.assertTrue(pick == prey, "control: a horse on the same spot is the pick (orig EntityButterfly.java:214, EntityHorse →"
                    + " AbstractHorse) (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(prey);
            discardQuietly(butterfly);
        }
    }

    /**
     * orig EntityButterfly.java:56, :219 — the GenericTargetSorter: a horse 6 blocks off (silhouette 1.4 x 1.6 divides
     * its distance, orig GenericTargetSorter.java:24-26) outranks a survival player 5 blocks off; plain-nearest would
     * answer the player.
     */
    private static void butterflyTieBreak(GameTestHelper helper) {
        Mob butterfly = null;
        Mob horse = null;
        ServerPlayer player = null;
        try {
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(FIVE_PLAYER_POS));
            horse = spawnPrey(helper, EntityType.HORSE, SIX_PREY_POS);
            assertSeen(helper, butterfly, player, "a survival player 5 blocks off");
            assertSeen(helper, butterfly, horse, "a horse 6 blocks off");
            double horseSilhouette = horse.getBbHeight() * horse.getBbWidth();
            double playerSilhouette = player.getBbHeight() * player.getBbWidth();
            helper.assertTrue(butterfly.distanceToSqr(player) < butterfly.distanceToSqr(horse)
                            && butterfly.distanceToSqr(horse) / horseSilhouette < butterfly.distanceToSqr(player) / playerSilhouette,
                    "precondition: the player is nearer, the horse's silhouette-weighted distance nearer still (" + FINDING + " test geometry)");
            LivingEntity pick = butterflyScan(butterfly);
            helper.assertTrue(pick == horse, "ButterflyIslandsHuntGoal.findSomethingToAttack (orig EntityButterfly.java:219,"
                    + " GenericTargetSorter :24-26): the horse's weighted distance must outrank the nearer player — plain-nearest"
                    + " would answer the player (" + FINDING + "); got " + describe(pick));
        } finally {
            removePlayer(helper, player);
            discardQuietly(horse);
            discardQuietly(butterfly);
        }
    }

    /** orig EntityButterfly.java:195-197 — the filter answers false on Peaceful for a horse it takes on NORMAL. */
    private static void butterflyFilterPeaceful(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        Mob butterfly = null;
        Mob horse = null;
        try {
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            Goal goal = butterflyHuntGoal(butterfly);
            horse = spawnPrey(helper, EntityType.HORSE, PREY_POS);
            assertSeen(helper, butterfly, horse, "a horse 8 blocks off");
            helper.assertTrue(filter(goal, ButterflyIslandsHuntGoal.class, horse), "control: on " + before + " the filter takes a horse"
                    + " 8 blocks off (orig EntityButterfly.java:214) (" + FINDING + ")");
            server.setDifficulty(Difficulty.PEACEFUL, true);
            assertPeaceful(helper);
            helper.assertTrue(!filter(goal, ButterflyIslandsHuntGoal.class, horse), "ButterflyIslandsHuntGoal.isSuitableTarget on"
                    + " PEACEFUL: orig EntityButterfly.java:195-197 answers false ahead of every other check (" + FINDING + ")");
        } finally {
            server.setDifficulty(before, true);
            discardQuietly(horse);
            discardQuietly(butterfly);
        }
    }

    /**
     * orig EntityButterfly.java:183-192 — the bite: on nextInt(2)==0 and NORMAL the horse loses 1.0; on a 1 nothing;
     * on Peaceful nothing even with the roll pinned.
     */
    private static void butterflyBite(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        Mob butterfly = null;
        Mob horse = null;
        try {
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            horse = spawnPrey(helper, EntityType.HORSE, TOUCH_PREY_POS);
            replaceRandom(butterfly, rolls(2, 0));
            boolean hit = butterfly.doHurtTarget(horse);
            helper.assertTrue(hit && horse.getHealth() == PREY_HEALTH - 1.0f, "EntityButterfly.doHurtTarget (orig EntityButterfly.java:"
                    + "184-191): on nextInt(2)==0 and NORMAL the bite lands exactly 1.0 of mob damage (" + FINDING + "); hit=" + hit
                    + ", horse health " + horse.getHealth());
            healPrey(horse);
            replaceRandom(butterfly, rolls(2, 1));
            hit = butterfly.doHurtTarget(horse);
            helper.assertTrue(!hit && horse.getHealth() == PREY_HEALTH, "orig EntityButterfly.java:184-186 — `OreSpawnRand.nextInt(2) != 0`"
                    + " answers false: a 1 bites nothing (" + FINDING + "); hit=" + hit + ", horse health " + horse.getHealth());
            healPrey(horse);
            replaceRandom(butterfly, rolls(2, 0));
            server.setDifficulty(Difficulty.PEACEFUL, true);
            assertPeaceful(helper);
            hit = butterfly.doHurtTarget(horse);
            helper.assertTrue(!hit && horse.getHealth() == PREY_HEALTH, "orig EntityButterfly.java:187-189 — no bite on Peaceful, the"
                    + " roll notwithstanding (" + FINDING + "); hit=" + hit + ", horse health " + horse.getHealth());
        } finally {
            server.setDifficulty(before, true);
            discardQuietly(horse);
            discardQuietly(butterfly);
        }
    }

    /**
     * orig EntityButterfly.java:154-169 — the hunt is the else-branch of the retarget test and its 1-in-10 roll comes
     * before the dimension check: on a tick whose retarget did not fire, a type-1 butterfly on NORMAL spends exactly
     * one nextInt(10) and, outside the Islands (the GameTestServer has no datapack dimension), hunts nothing — the
     * seeded flight target stays and a survival player 2 blocks off is not bitten; on a tick whose retarget fired,
     * the hunt die is not rolled at all.
     */
    private static void butterflyOverworldNoHunt(GameTestHelper helper) {
        Mob butterfly = null;
        ServerPlayer player = null;
        try {
            helper.assertTrue(helper.getLevel().dimension() != ModDimensionKeys.ISLANDS, "precondition: the game-test level is not"
                    + " the Islands dimension (" + FINDING + " test setup)");
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            ((EntityButterfly) butterfly).setButterflyType(1);
            ButterflyIslandsHuntGoal goal = (ButterflyIslandsHuntGoal) butterflyHuntGoal(butterfly);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(TOUCH_PLAYER_POS));
            assertSeen(helper, butterfly, player, "a survival player 2 blocks off");
            BlockPos seeded = butterfly.blockPosition().offset(10, 0, 0);
            goal.setFlightTarget(seeded);
            CountingRandom counting = new CountingRandom(rolls(100, 1, 10, 0, 2, 0));
            replaceRandom(butterfly, counting);
            goal.tick();
            helper.assertTrue(counting.count(100) == 1 && counting.count(10) == 1, "orig EntityButterfly.java:154, :161 — the retarget"
                    + " die (nextInt(100)) and, in its else-branch, the hunt die (nextInt(10)) are each rolled once; the hunt roll"
                    + " precedes the dimension check (" + FINDING + "); nextInt(100)=" + counting.count(100) + ", nextInt(10)=" + counting.count(10));
            BlockPos after = (BlockPos) readField(goal, AmbientFlightGoal.class, "flightTarget");
            helper.assertTrue(seeded.equals(after) && player.getHealth() == PREY_HEALTH, "orig EntityButterfly.java:161 — outside"
                    + " DimensionID4 the hunt never runs: the flight target must stay where it was seeded and the player unbitten ("
                    + FINDING + "); flight target " + after + ", player health " + player.getHealth());
            counting.reset();
            replaceRandom(butterfly, new CountingRandom(rolls(100, 0, 10, 0, 2, 0)));
            CountingRandom second = (CountingRandom) readField(butterfly, Entity.class, "random");
            goal.tick();
            helper.assertTrue(second.count(100) == 1 && second.count(10) == 0, "orig EntityButterfly.java:154-161 — on a tick whose"
                    + " retarget fired the hunt branch is the else and its die is not rolled (" + FINDING + "); nextInt(100)="
                    + second.count(100) + ", nextInt(10)=" + second.count(10));
        } finally {
            removePlayer(helper, player);
            discardQuietly(butterfly);
        }
    }

    // ------------------------------------------------------------------
    // WaterDragon — orig WaterDragon.java:597-612 (the pass), :650-680 (the filter), :682-706 (the scan)
    // ------------------------------------------------------------------

    /** orig WaterDragon.java:597-599 — on the nextInt(5)==1 tick the pass stores its pick in the slot as the hunt's own; a 0 stores nothing. */
    private static void waterDragonCadence(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            helper.assertTrue(dragon.getTarget() == null && scanPick(dragon) == null, "precondition: an empty slot and no hunt pick ("
                    + FINDING + " test setup)");
            replaceRandom(dragon, rolls(25, 1, 5, 1, 100, 0));
            invokeCustomServerAiStep(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "WaterDragon.customServerAiStep (orig"
                    + " WaterDragon.java:597-599): on the nextInt(5)==1 tick the pass hands its pick to the slot, marked the hunt's"
                    + " own (" + FINDING + "); slot " + describe(dragon.getTarget()) + ", scanPick " + describe((Entity) scanPick(dragon)));
            dragon.setTarget(null);
            replaceRandom(dragon, rolls(25, 1, 5, 0, 100, 0));
            invokeCustomServerAiStep(dragon);
            helper.assertTrue(dragon.getTarget() == null, "orig WaterDragon.java:597 rolls nextInt(5) == 1, not == 0: a 0 must not hunt ("
                    + FINDING + "); slot " + describe(dragon.getTarget()));
        } finally {
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:597 — `difficulty != PEACEFUL && nextInt(5) == 1`: on NORMAL the pass rolls the die once
     * and stores the Zombie; on PEACEFUL the die is not rolled at all and nothing is stored.
     */
    private static void waterDragonPeacefulPass(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            CountingRandom counting = new CountingRandom(rolls(25, 1, 5, 1, 100, 0));
            replaceRandom(dragon, counting);
            invokeCustomServerAiStep(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && counting.count(5) == 1, "control: on " + before + " the pass rolls"
                    + " nextInt(5) once and stores the Zombie (orig WaterDragon.java:597-599) (" + FINDING + "); slot "
                    + describe(dragon.getTarget()) + ", nextInt(5) calls " + counting.count(5));
            dragon.setTarget(null);
            counting.reset();
            server.setDifficulty(Difficulty.PEACEFUL, true);
            assertPeaceful(helper);
            invokeCustomServerAiStep(dragon);
            helper.assertTrue(dragon.getTarget() == null, "WaterDragon.customServerAiStep on PEACEFUL: orig WaterDragon.java:597 gates the"
                    + " pass on the difficulty, so nothing is stored (" + FINDING + "); slot " + describe(dragon.getTarget()));
            helper.assertTrue(counting.count(5) == 0, "orig WaterDragon.java:597 tests the difficulty BEFORE the roll: on Peaceful the"
                    + " nextInt(5) die is not spent (" + FINDING + "); nextInt(5) calls " + counting.count(5));
        } finally {
            server.setDifficulty(before, true);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /** orig WaterDragon.java:651-653 — the filter answers false on Peaceful for a Zombie it takes on NORMAL. */
    private static void waterDragonFilterPeaceful(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            helper.assertTrue(filter(dragon, WaterDragon.class, zombie), "control: on " + before + " the filter takes a Zombie 8 blocks off"
                    + " (orig WaterDragon.java:669-671) (" + FINDING + ")");
            server.setDifficulty(Difficulty.PEACEFUL, true);
            assertPeaceful(helper);
            helper.assertTrue(!filter(dragon, WaterDragon.class, zombie), "WaterDragon.isSuitableTarget on PEACEFUL: orig"
                    + " WaterDragon.java:651-653 answers false ahead of every other check (" + FINDING + ")");
        } finally {
            server.setDifficulty(before, true);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /** orig WaterDragon.java:689 — the 14/4/14 box, pinned on +x, +y, +z from both sides with a Zombie (orig :669). */
    private static void waterDragonBoxPinned(GameTestHelper helper) {
        assertBoxPinned(helper, ModEntities.WATER_DRAGON.get(), EntityType.ZOMBIE, "a vanilla Zombie (orig :669)", 14.0, 4.0, 14.0,
                "orig WaterDragon.java:689", ProactiveHuntParityTests::scan);
    }

    /** orig WaterDragon.java:663-665 — a Zombie behind the stone column is refused; the same Zombie is the pick once it is gone. */
    private static void waterDragonSight(GameTestHelper helper) {
        assertSightStep(helper, ModEntities.WATER_DRAGON.get(), EntityType.ZOMBIE, "a vanilla Zombie (orig :669)",
                "orig WaterDragon.java:663-665", ProactiveHuntParityTests::scan);
    }

    /** orig WaterDragon.java:666-668 — another Water Dragon is refused; a Zombie on the same spot is the pick. */
    private static void waterDragonExcluded(GameTestHelper helper) {
        Mob dragon = null;
        Mob other = null;
        Mob control = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            other = spawnPrey(helper, ModEntities.WATER_DRAGON.get(), PREY_POS);
            assertSeen(helper, dragon, other, "another Water Dragon");
            LivingEntity pick = scan(dragon);
            helper.assertTrue(pick == null, "WaterDragon.isSuitableTarget (orig WaterDragon.java:666-668): another Water Dragon must be"
                    + " refused (" + FINDING + "); got " + describe(pick));
            other.discard();
            other = null;
            control = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, control, "a vanilla Zombie");
            pick = scan(dragon);
            helper.assertTrue(pick == control, "control: a Zombie on the same spot is the pick (orig :669-671), so the Water Dragon was"
                    + " refused by its own step and not by geometry or sight (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(control);
            discardQuietly(other);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:672-674 — a tamed dragon takes EntityMob only: a Zombie yes, a survival player and a
     * Cephadrome (the non-mob fallthrough) no; untamed again, the player is prey (:675-678).
     */
    private static void waterDragonTamed(GameTestHelper helper) {
        WaterDragon dragon = null;
        Mob zombie = null;
        Mob cephadrome = null;
        ServerPlayer player = null;
        try {
            dragon = (WaterDragon) spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            dragon.setTame(true, false);
            helper.assertTrue(dragon.isTame(), "precondition: the dragon reads as tamed (" + FINDING + " test setup)");
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            LivingEntity pick = scan(dragon);
            helper.assertTrue(pick == zombie, "WaterDragon.isSuitableTarget (orig WaterDragon.java:669-671): a tamed dragon still takes"
                    + " a Zombie — the EntityMob step precedes the tamed step (" + FINDING + "); got " + describe(pick));
            zombie.discard();
            zombie = null;
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            assertSeen(helper, dragon, player, "a survival player 8 blocks off");
            pick = scan(dragon);
            helper.assertTrue(pick == null, "orig WaterDragon.java:672-674 — a tamed dragon takes nothing but EntityMob: the survival"
                    + " player must be refused ahead of the player step (" + FINDING + "); got " + describe(pick));
            cephadrome = spawnPrey(helper, ModEntities.CEPHADROME.get(), PREY_POS);
            removePlayer(helper, player);
            player = null;
            assertSeen(helper, dragon, cephadrome, "a Cephadrome 8 blocks off");
            pick = scan(dragon);
            helper.assertTrue(pick == null, "orig WaterDragon.java:672-674 — a tamed dragon takes nothing but EntityMob: the Cephadrome"
                    + " (the isAttackableNonMob fallthrough of :679) must be refused (" + FINDING + "); got " + describe(pick));
            cephadrome.discard();
            cephadrome = null;
            dragon.setTame(false, false);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            assertSeen(helper, dragon, player, "a survival player 8 blocks off");
            pick = scan(dragon);
            helper.assertTrue(pick == player, "control: untamed, the same survival player is the pick (orig WaterDragon.java:675-678), so"
                    + " the tamed step and not the player step refused it (" + FINDING + "); got " + describe(pick));
        } finally {
            removePlayer(helper, player);
            discardQuietly(cephadrome);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /** orig WaterDragon.java:675-678 — a creative mock player is refused by a wild adult; the same player in survival is the pick. */
    private static void waterDragonCreative(GameTestHelper helper) {
        assertCreativeStep(helper, ModEntities.WATER_DRAGON.get(), "orig WaterDragon.java:675-678", ProactiveHuntParityTests::scan);
    }

    /**
     * orig WaterDragon.java:679 — the fallthrough is the shared isAttackableNonMob helper: a Cephadrome (on orig's
     * list and the port's, not a Monster) is the pick; a pig (on neither) is refused.
     */
    private static void waterDragonNonMobHelper(GameTestHelper helper) {
        Mob dragon = null;
        Mob pig = null;
        Mob cephadrome = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            pig = spawnPrey(helper, EntityType.PIG, PREY_POS);
            helper.assertTrue(!MyUtils.isAttackableNonMob(pig), "precondition: a pig is on neither non-mob list (" + FINDING + " test setup)");
            assertSeen(helper, dragon, pig, "a vanilla pig");
            LivingEntity pick = scan(dragon);
            helper.assertTrue(pick == null, "WaterDragon.isSuitableTarget (orig WaterDragon.java:679): a pig falls through to"
                    + " isAttackableNonMob and is refused (" + FINDING + "); got " + describe(pick));
            pig.discard();
            pig = null;
            cephadrome = spawnPrey(helper, ModEntities.CEPHADROME.get(), PREY_POS);
            helper.assertTrue(MyUtils.isAttackableNonMob(cephadrome) && !(cephadrome instanceof Monster), "precondition: the Cephadrome"
                    + " is on the port helper's list (util/MyUtils.java:60) and orig's (orig MyUtils.java:77-115) and is no Monster,"
                    + " so only the :679 fallthrough can take it (" + FINDING + " test setup)");
            assertSeen(helper, dragon, cephadrome, "a Cephadrome");
            pick = scan(dragon);
            helper.assertTrue(pick == cephadrome, "orig WaterDragon.java:679 — the Cephadrome is taken through the shared"
                    + " isAttackableNonMob fallthrough (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(cephadrome);
            discardQuietly(pig);
            discardQuietly(dragon);
        }
    }

    /** orig WaterDragon.java:686-688 — a baby never hunts: the scan answers null with a Zombie the filter itself accepts; grown, the Zombie is the pick. */
    private static void waterDragonBaby(GameTestHelper helper) {
        WaterDragon dragon = null;
        Mob zombie = null;
        try {
            dragon = (WaterDragon) spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            dragon.setBaby(true);
            helper.assertTrue(dragon.isBaby(), "precondition: the dragon reads as a baby (" + FINDING + " test setup)");
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            helper.assertTrue(filter(dragon, WaterDragon.class, zombie), "precondition: the filter alone takes the Zombie, so only the"
                    + " :686 gate can refuse it (" + FINDING + " test setup)");
            LivingEntity pick = scan(dragon);
            helper.assertTrue(pick == null, "WaterDragon.findSomethingToAttack (orig WaterDragon.java:686-688): a baby answers null"
                    + " ahead of the scan (" + FINDING + "); got " + describe(pick));
            dragon.setBaby(false);
            pick = scan(dragon);
            helper.assertTrue(pick == zombie, "control: grown, the same Zombie is the pick (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /** orig WaterDragon.java:683-685 — PlayNicely != 0 answers null ahead of the scan, read live. */
    private static void waterDragonPlayNicely(GameTestHelper helper) {
        assertPlayNicelyGate(helper, ModEntities.WATER_DRAGON.get(), EntityType.ZOMBIE, "a vanilla Zombie (orig :669)",
                "orig WaterDragon.java:683-685", ProactiveHuntParityTests::scan);
    }

    /** orig WaterDragon.java:50, :67, :690 — the GenericTargetSorter: a creeper 6 blocks off (distance halved) outranks a Zombie 5 blocks off. */
    private static void waterDragonTieBreak(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        Mob creeper = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, FIVE_PREY_POS);
            creeper = spawnPrey(helper, EntityType.CREEPER, SIX_PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 5 blocks off");
            assertSeen(helper, dragon, creeper, "a Creeper 6 blocks off");
            helper.assertTrue(dragon.distanceToSqr(zombie) < dragon.distanceToSqr(creeper)
                            && dragon.distanceToSqr(creeper) / 2.0 < dragon.distanceToSqr(zombie),
                    "precondition: the Zombie is nearer, the halved creeper distance nearer still (" + FINDING + " test geometry)");
            LivingEntity pick = scan(dragon);
            helper.assertTrue(pick == creeper, "WaterDragon.findSomethingToAttack (orig WaterDragon.java:690, GenericTargetSorter"
                    + " :21-23): the creeper's halved distance must outrank the nearer Zombie — plain-nearest would answer the Zombie ("
                    + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(creeper);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:694-698 — a live stored target (the hurt / revenge channel, never the hunt's own) is
     * answered ahead of the loop even with a nearer creeper in the box, and the pass leaves it in the slot unmarked;
     * once it is dead the pass drops it and stores the creeper as the hunt's own pick.
     */
    private static void waterDragonStickyStored(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        Mob creeper = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            creeper = spawnPrey(helper, EntityType.CREEPER, NEAR_PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            assertSeen(helper, dragon, creeper, "a Creeper 3 blocks off");
            dragon.setTarget(zombie);
            helper.assertTrue(scanPick(dragon) == null, "precondition: a target set from outside the hunt is not the hunt's own pick ("
                    + FINDING + " test setup)");
            LivingEntity pick = scan(dragon);
            helper.assertTrue(pick == zombie, "WaterDragon.findSomethingToAttack (orig WaterDragon.java:694-697): the live stored target"
                    + " is answered ahead of the loop, the nearer creeper notwithstanding (" + FINDING + "); got " + describe(pick));
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == null, "the pass leaves a sticky stored target in the"
                    + " slot and claims no ownership of it (" + FINDING + "); slot " + describe(dragon.getTarget()) + ", scanPick "
                    + describe((Entity) scanPick(dragon)));
            zombie.discard();
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == creeper && scanPick(dragon) == creeper, "orig WaterDragon.java:698-704 — a dead stored"
                    + " target is cleared and the scan's pick, the creeper, takes the slot as the hunt's own (" + FINDING + "); slot "
                    + describe(dragon.getTarget()) + ", scanPick " + describe((Entity) scanPick(dragon)));
        } finally {
            discardQuietly(creeper);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:598-611 — the hunt's own pick is re-derived every pass: stored on one pass, dropped by
     * the next once the Zombie has left the 14/4/14 box (:611 stood down), stored again when it is back.
     */
    private static void waterDragonOwnPickRederived(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "control: the pass stores the Zombie as the"
                    + " hunt's own pick (" + FINDING + "); slot " + describe(dragon.getTarget()));
            BlockPos far = helper.absolutePos(FAR_POS);
            zombie.moveTo(far.getX() + 0.5, far.getY(), far.getZ() + 0.5, 0.0f, 0.0f);
            helper.assertTrue(!dragon.getBoundingBox().inflate(14.0, 4.0, 14.0).intersects(zombie.getBoundingBox()),
                    "precondition: the Zombie moved 24 blocks east is outside the 14/4/14 box (" + FINDING + " test geometry)");
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == null && scanPick(dragon) == null, "WaterDragon.selectTarget (orig WaterDragon.java:"
                    + "598-611): the hunt's own pick was never stored in 1.7.10 — re-derived each pass, a Zombie outside the box is"
                    + " simply not found and the slot is cleared (" + FINDING + "); slot " + describe(dragon.getTarget()));
            BlockPos back = helper.absolutePos(PREY_POS);
            zombie.moveTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5, 0.0f, 0.0f);
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "control: back inside the box the Zombie is"
                    + " stored again (" + FINDING + "); slot " + describe(dragon.getTarget()));
        } finally {
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:683-688 — under PlayNicely the pass consults nothing: a stored target set from outside the
     * hunt stands untouched; the hunt's own pick, which orig never stored, is cleared by the gated pass (the
     * Nastysaurus / TRex rule of ENT-S-115).
     */
    private static void waterDragonGatedPassSlot(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            OreSpawnConfig.PLAY_NICELY.set(true);
            dragon.setTarget(zombie);
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == null, "WaterDragon.selectTarget with playNicely on:"
                    + " orig WaterDragon.java:683-685 answers null ahead of the stored-target read, so a stored target set from outside"
                    + " the hunt stands and the pass claims nothing (" + FINDING + "); slot " + describe(dragon.getTarget()));
            OreSpawnConfig.PLAY_NICELY.set(false);
            dragon.setTarget(null);
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "control: with the flag down the pass stores the"
                    + " Zombie as the hunt's own pick (" + FINDING + "); slot " + describe(dragon.getTarget()));
            OreSpawnConfig.PLAY_NICELY.set(true);
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == null && scanPick(dragon) == null, "WaterDragon.selectTarget with playNicely on: the"
                    + " hunt's own pick — never orig's stored target — is cleared by the gated pass, as orig stood down (:611) ("
                    + FINDING + "); slot " + describe(dragon.getTarget()));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * WaterDragon.setTarget: a re-assert of the occupant already in the slot (TargetGoal.canContinueToUse's per-pass
     * re-set) keeps the hunt's ownership of its own pick; a change of occupant ends it.
     */
    private static void waterDragonSlotReassert(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        Mob creeper = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, FIVE_PREY_POS);
            creeper = spawnPrey(helper, EntityType.CREEPER, FAR_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 5 blocks off");
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "control: the pass stores the Zombie as the"
                    + " hunt's own pick (" + FINDING + "); slot " + describe(dragon.getTarget()));
            dragon.setTarget(zombie);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "WaterDragon.setTarget: re-asserting the"
                    + " occupant already in the slot — TargetGoal.canContinueToUse's per-pass re-set while HurtByTargetGoal runs —"
                    + " must keep the hunt's ownership, else its pick turns sticky (" + FINDING + "); scanPick "
                    + describe((Entity) scanPick(dragon)));
            dragon.setTarget(creeper);
            helper.assertTrue(dragon.getTarget() == creeper && scanPick(dragon) == null, "WaterDragon.setTarget: a change of occupant by"
                    + " another path ends the hunt's ownership (" + FINDING + "); scanPick " + describe((Entity) scanPick(dragon)));
            dragon.setTarget(null);
            helper.assertTrue(dragon.getTarget() == null && scanPick(dragon) == null, "clearing the slot clears the mark (" + FINDING + ")");
        } finally {
            discardQuietly(creeper);
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:483-493 with :694-697 — a hurt by the hunt's own pick makes that attacker the stored
     * target: the mark is dropped, and the next pass keeps the Zombie even after it has left the box (sticky),
     * where an unhurt pick would have been dropped.
     */
    private static void waterDragonHurtHandsOff(GameTestHelper helper) {
        Mob dragon = null;
        Mob zombie = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            zombie = spawnPrey(helper, EntityType.ZOMBIE, PREY_POS);
            assertSeen(helper, dragon, zombie, "a Zombie 8 blocks off");
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == zombie, "control: the pass stores the Zombie as the"
                    + " hunt's own pick (" + FINDING + "); slot " + describe(dragon.getTarget()));
            float healthBefore = dragon.getHealth();
            dragon.hurt(dragon.damageSources().mobAttack(zombie), 1.0f);
            helper.assertTrue(dragon.getHealth() < healthBefore, "precondition: the hit lands (hurtTimer 0) (" + FINDING + " test setup)");
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == null, "WaterDragon.hurt (orig WaterDragon.java:490):"
                    + " the attacker is the STORED target from here — the hunt's mark on its former pick is dropped (" + FINDING + ");"
                    + " slot " + describe(dragon.getTarget()) + ", scanPick " + describe((Entity) scanPick(dragon)));
            BlockPos far = helper.absolutePos(FAR_POS);
            zombie.moveTo(far.getX() + 0.5, far.getY(), far.getZ() + 0.5, 0.0f, 0.0f);
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == zombie && scanPick(dragon) == null, "orig WaterDragon.java:694-697 — the stored"
                    + " attacker is answered ahead of the loop while alive, box or no box: the next pass keeps it (" + FINDING + ");"
                    + " slot " + describe(dragon.getTarget()));
        } finally {
            discardQuietly(zombie);
            discardQuietly(dragon);
        }
    }

    /**
     * orig WaterDragon.java:479-482 with :483-493 — a hit the 10-tick {@code hurt_timer} swallows reaches neither
     * the revenge timer (no {@code super.attackEntityFrom}) nor the :483-493 store (a player is no
     * {@code EntityLiving}), so a player pick that lands such a hit stays the hunt's transient pick: re-derived
     * next pass, dropped once it has left the box. Refuter B (ENT-S-117, B1): the port's mark must survive the
     * swallowed hit, else the unmarked player sits sticky in the slot through {@code findSomethingToAttack}'s
     * stored-target read. An unrelated landed hit first sets the timer; the player's hit then lands nothing.
     */
    private static void waterDragonSwallowedHitKeepsOwnership(GameTestHelper helper) {
        Mob dragon = null;
        ServerPlayer player = null;
        try {
            dragon = spawnFrozen(helper, ModEntities.WATER_DRAGON.get(), HUNTER_POS);
            assertPlayNicelyOff(helper);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(Vec3.atBottomCenterOf(PREY_POS)));
            assertSeen(helper, dragon, player, "a survival player 8 blocks off");
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == player && scanPick(dragon) == player, "control: the pass stores the player as the"
                    + " hunt's own pick (" + FINDING + "); slot " + describe(dragon.getTarget()));
            float healthBefore = dragon.getHealth();
            dragon.hurt(dragon.damageSources().fall(), 1.0f);
            helper.assertTrue(dragon.getHealth() < healthBefore, "precondition: an unrelated hit lands and starts the 10-tick"
                    + " hurtTimer (orig WaterDragon.java:479-482) (" + FINDING + " test setup)");
            float healthAfterFall = dragon.getHealth();
            dragon.hurt(dragon.damageSources().playerAttack(player), 1.0f);
            helper.assertTrue(dragon.getHealth() == healthAfterFall, "precondition: the player's hit inside the timer is swallowed"
                    + " — no super.hurt, no revenge store (" + FINDING + " test setup)");
            helper.assertTrue(dragon.getTarget() == player && scanPick(dragon) == player, "WaterDragon.hurt (orig WaterDragon.java:479-493):"
                    + " a swallowed hit stores nothing in 1.7.10, so the hunt's mark on its pick must survive it (" + FINDING
                    + ", refuter B B1); slot " + describe(dragon.getTarget()) + ", scanPick " + describe((Entity) scanPick(dragon)));
            BlockPos far = helper.absolutePos(FAR_POS);
            player.teleportTo(helper.getLevel(), far.getX() + 0.5, far.getY(), far.getZ() + 0.5, 0.0f, 0.0f);
            selectTarget(dragon);
            helper.assertTrue(dragon.getTarget() == null && scanPick(dragon) == null, "orig WaterDragon.java:694-704 — a transient pick"
                    + " that left the 14/4/14 box is not found again and the pass stands down (:611); a sticky player here is the"
                    + " refuted defect (" + FINDING + "); slot " + describe(dragon.getTarget()));
        } finally {
            removePlayer(helper, player);
            discardQuietly(dragon);
        }
    }

    // ------------------------------------------------------------------
    // Shared probes
    // ------------------------------------------------------------------

    /** The hunter's private scan: {@code findSomethingToAttack()} on the entity (AttackSquid, WaterDragon) or on the butterfly's hunt goal. */
    private interface Scan {
        LivingEntity pick(Mob hunter);
    }

    /**
     * The box is the original's on every axis: a probe whose box lies just past the +x, +y or +z edge of
     * {@code getBoundingBox().inflate(box)} is not picked, one just short of that edge is (the TargetScanParityTests shape).
     */
    private static void assertBoxPinned(GameTestHelper helper, EntityType<? extends Mob> hunterType, EntityType<? extends Mob> probeType,
                                        String probeWhy, double bx, double by, double bz, String origCite, Scan scan) {
        Mob hunter = null;
        Mob probe = null;
        try {
            hunter = hunterType == ModEntities.ENTITY_BUTTERFLY.get()
                    ? spawnWithGoals(helper, hunterType, CORNER_HUNTER_POS)
                    : spawnFrozen(helper, hunterType, CORNER_HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper);
            AABB box = hunter.getBoundingBox().inflate(bx, by, bz);
            String boxText = (int) bx + "/" + (int) by + "/" + (int) bz;
            double x = hunter.getX();
            double y = hunter.getY();
            double z = hunter.getZ();
            Object[][] probes = {
                    {"+x", false, new Vec3(box.maxX + OUTSIDE_MARGIN, y, z)},
                    {"+x", true, new Vec3(box.maxX - INSIDE_MARGIN, y, z)},
                    {"+y", false, new Vec3(x, box.maxY + OUTSIDE_MARGIN, z)},
                    {"+y", true, new Vec3(x, box.maxY - INSIDE_MARGIN, z)},
                    {"+z", false, new Vec3(x, y, box.maxZ + OUTSIDE_MARGIN)},
                    {"+z", true, new Vec3(x, y, box.maxZ - INSIDE_MARGIN)},
            };
            for (Object[] p : probes) {
                String axis = (String) p[0];
                boolean inside = (Boolean) p[1];
                Vec3 at = (Vec3) p[2];
                probe = spawnPrey(helper, probeType, PREY_POS);
                probe.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
                String where = probeWhy + " " + (inside ? "just inside" : "just past") + " the " + axis + " edge of the " + boxText
                        + " box (" + origCite + ")";
                helper.assertTrue(box.intersects(probe.getBoundingBox()) == inside, "precondition: " + where + " must "
                        + (inside ? "" : "not ") + "meet the box (" + FINDING + " test geometry)");
                assertSeen(helper, hunter, probe, where);
                LivingEntity pick = scan.pick(hunter);
                if (inside) {
                    helper.assertTrue(pick == probe, name + ": " + where + ", in sight, must be the pick — the port's box must reach as far"
                            + " as the original's (" + FINDING + "); got " + describe(pick));
                } else {
                    helper.assertTrue(pick == null, name + ": " + where + ", in sight, must leave the scan empty — the port's box must reach"
                            + " no further than the original's (" + FINDING + "); got " + describe(pick));
                }
                probe.discard();
                probe = null;
            }
        } finally {
            discardQuietly(probe);
            discardQuietly(hunter);
        }
    }

    /** The sight step: prey behind the stone column is refused; the same prey is the pick once the column is gone and the Sensing cache reset. */
    private static void assertSightStep(GameTestHelper helper, EntityType<? extends Mob> hunterType, EntityType<? extends Mob> preyType,
                                        String preyWhy, String origCite, Scan scan) {
        Mob hunter = null;
        Mob prey = null;
        try {
            hunter = hunterType == ModEntities.ENTITY_BUTTERFLY.get()
                    ? spawnWithGoals(helper, hunterType, HUNTER_POS)
                    : spawnFrozen(helper, hunterType, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper);
            column(helper, true);
            prey = spawnPrey(helper, preyType, PREY_POS);
            helper.assertTrue(!hunter.hasLineOfSight(prey), "precondition: the stone column at x 24 hides " + preyWhy + " from the "
                    + name + " (" + FINDING + " test geometry)");
            LivingEntity pick = scan.pick(hunter);
            helper.assertTrue(pick == null, name + " (" + origCite + ", line of sight): " + preyWhy + " behind the column must be refused ("
                    + FINDING + "); got " + describe(pick));
            column(helper, false);
            resetSight(hunter);
            assertSeen(helper, hunter, prey, preyWhy + " with the column gone");
            pick = scan.pick(hunter);
            helper.assertTrue(pick == prey, "control: the same " + preyWhy + " in sight must be the pick, so the column and not geometry"
                    + " refused it (" + FINDING + "); got " + describe(pick));
        } finally {
            discardQuietly(prey);
            discardQuietly(hunter);
        }
    }

    /** The creative step: a creative mock player (instabuild) is refused; the same player set to survival is the pick. */
    private static void assertCreativeStep(GameTestHelper helper, EntityType<? extends Mob> hunterType, String origCite, Scan scan) {
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = hunterType == ModEntities.ENTITY_BUTTERFLY.get()
                    ? spawnWithGoals(helper, hunterType, HUNTER_POS)
                    : spawnFrozen(helper, hunterType, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            assertPlayNicelyOff(helper);
            player = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS));
            helper.assertTrue(player.getAbilities().instabuild, "precondition: a creative player has instabuild set (" + FINDING + " test setup)");
            assertSeen(helper, hunter, player, "the mock player");
            LivingEntity pick = scan.pick(hunter);
            helper.assertTrue(pick == null, name + " (" + origCite + ", capabilities.isCreativeMode → Abilities.instabuild): a creative"
                    + " player alone in the box must leave the scan empty (" + FINDING + "); got " + describe(pick));
            player.setGameMode(GameType.SURVIVAL);
            helper.assertTrue(!player.getAbilities().instabuild, "precondition: the same player set to survival has instabuild clear ("
                    + FINDING + " test setup)");
            pick = scan.pick(hunter);
            helper.assertTrue(pick == player, "control: the same player in survival on the same spot must be the pick, so creative mode"
                    + " alone refused it (" + FINDING + "); got " + describe(pick));
        } finally {
            removePlayer(helper, player);
            discardQuietly(hunter);
        }
    }

    /** The PlayNicely gate: the scan answers the prey with the flag down and null with it up; the flag restored in a finally. */
    private static void assertPlayNicelyGate(GameTestHelper helper, EntityType<? extends Mob> hunterType, EntityType<? extends Mob> preyType,
                                             String preyWhy, String origCite, Scan scan) {
        Mob hunter = null;
        Mob prey = null;
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        try {
            hunter = spawnFrozen(helper, hunterType, HUNTER_POS);
            String name = hunter.getClass().getSimpleName();
            prey = spawnPrey(helper, preyType, PREY_POS);
            assertSeen(helper, hunter, prey, preyWhy);
            OreSpawnConfig.PLAY_NICELY.set(false);
            LivingEntity pick = scan.pick(hunter);
            helper.assertTrue(pick == prey, "control: with playNicely off " + name + ".findSomethingToAttack answers " + preyWhy
                    + " 8 blocks off (" + FINDING + "); got " + describe(pick));
            OreSpawnConfig.PLAY_NICELY.set(true);
            pick = scan.pick(hunter);
            helper.assertTrue(pick == null, name + ".findSomethingToAttack with playNicely on: " + origCite + " answers null ahead of the"
                    + " scan while PlayNicely != 0, read live (" + FINDING + "); got " + describe(pick));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(prey);
            discardQuietly(hunter);
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the PlayNicelyGateParityTests / TargetScanParityTests idioms)
    // ------------------------------------------------------------------

    private static void assertPlayNicelyOff(GameTestHelper helper) {
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(), "precondition: PlayNicely must be off — the squid's and the dragon's"
                + " scans answer nothing under it (" + FINDING + " test setup)");
    }

    private static void assertPeaceful(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getDifficulty() == Difficulty.PEACEFUL,
                "precondition: MinecraftServer.setDifficulty(PEACEFUL, true) must show through level.getDifficulty() (" + FINDING + " test setup)");
    }

    private static void assertSeen(GameTestHelper helper, Mob hunter, LivingEntity prey, String why) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: " + hunter.getClass().getSimpleName() + " (eye "
                + String.format("%.2f", hunter.getEyeHeight()) + " above its feet) must see " + why + " inside the barrier shell ("
                + FINDING + " test geometry)");
    }

    /** The stone column between the hunter and PREY_POS (rel x 24, z 24, y 1..6), raised or cleared. */
    private static void column(GameTestHelper helper, boolean stone) {
        for (int y = 1; y <= COLUMN_TOP; y++) {
            helper.setBlock(new BlockPos(COLUMN_X, y, COLUMN_Z), stone ? Blocks.STONE : Blocks.AIR);
        }
    }

    /** Sensing caches line-of-sight answers per tick; between two scans of the same prey the cache is cleared as the tick would. */
    private static void resetSight(Mob hunter) {
        hunter.getSensing().tick();
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (a selector is under test) but no AI, so nothing runs. */
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
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server defaults to CREATIVE,
     * GameTestServer.java:85). Health is raised so nothing incidental can kill it. Deprecated mock-player factory
     * tolerated the way TargetScanParityTests and PlayNicelyGateParityTests do.
     */
    @SuppressWarnings({"removal", "deprecation"})
    private static ServerPlayer playerAt(GameTestHelper helper, GameType mode, Vec3 absolutePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(mode);
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

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as PlayNicelyGateParityTests.rolls. */
    private static RandomSource rolls(int... boundAnswerPairs) {
        RandomSource source = RandomSource.create(1234L);
        for (int i = 0; i < boundAnswerPairs.length; i += 2) {
            source = new VortexParityTests.ForcedRoll(source, boundAnswerPairs[i], boundAnswerPairs[i + 1]);
        }
        return source;
    }

    /** Same seam as VortexParityTests.forceDiscardRoll: swap {@code Entity.random} for a forced source. */
    private static void replaceRandom(Entity entity, RandomSource forced) {
        writeField(entity, Entity.class, "random", forced);
    }

    /** A delegating RandomSource that counts every {@code nextInt(bound)} call per bound — the term-order rows' witness. */
    static final class CountingRandom implements RandomSource {
        private final RandomSource delegate;
        private final Map<Integer, Integer> counts = new HashMap<>();

        CountingRandom(RandomSource delegate) {
            this.delegate = delegate;
        }

        int count(int bound) {
            return this.counts.getOrDefault(bound, 0);
        }

        void reset() {
            this.counts.clear();
        }

        @Override
        public RandomSource fork() {
            return new CountingRandom(this.delegate.fork());
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
            this.counts.merge(upper, 1, Integer::sum);
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

    /** The hunter's protected {@code customServerAiStep} — the port's shape of orig updateAITasks. */
    private static void invokeCustomServerAiStep(Mob hunter) {
        invoke(hunter, hunter.getClass(), "customServerAiStep");
    }

    /** The hunter's private no-arg {@code findSomethingToAttack()} (AttackSquid, WaterDragon). */
    private static LivingEntity scan(Mob hunter) {
        return (LivingEntity) invoke(hunter, hunter.getClass(), "findSomethingToAttack");
    }

    /** The butterfly's hunt goal's private {@code findSomethingToAttack()}. */
    private static LivingEntity butterflyScan(Mob butterfly) {
        return (LivingEntity) invoke(butterflyHuntGoal(butterfly), ButterflyIslandsHuntGoal.class, "findSomethingToAttack");
    }

    /** The Water Dragon's private {@code selectTarget()} — the pass's hand-off to the slot. */
    private static void selectTarget(Mob dragon) {
        invoke(dragon, WaterDragon.class, "selectTarget");
    }

    /** The Water Dragon's private {@code scanPick} ownership mark. */
    private static Object scanPick(Mob dragon) {
        return readField(dragon, WaterDragon.class, "scanPick");
    }

    /** The private one-arg {@code isSuitableTarget(LivingEntity)} of the hunter or the butterfly's goal. */
    private static boolean filter(Object owner, Class<?> declaring, LivingEntity candidate) {
        return (Boolean) invoke(owner, declaring, "isSuitableTarget", new Class<?>[] {LivingEntity.class}, candidate);
    }

    /** The Dragon's NearestAttackableTargetGoal&lt;Mob&gt; (+ Enemy selector, the IMob convention ENT-S-124) off its target selector. */
    private static Goal dragonImobGoal(Mob dragon) {
        for (WrappedGoal wrapped : dragon.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == Mob.class) {
                return wrapped.getGoal();
            }
        }
        throw new IllegalStateException("precondition: the Dragon must carry a NearestAttackableTargetGoal<Mob> on its target"
                + " selector — the port's shape of orig Dragon.java:116 under the IMob convention (ENT-S-124) (" + FINDING + " test setup)");
    }

    /** The butterfly's ButterflyIslandsHuntGoal off its goal selector. */
    private static Goal butterflyHuntGoal(Mob butterfly) {
        for (WrappedGoal wrapped : butterfly.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof ButterflyIslandsHuntGoal) {
                return wrapped.getGoal();
            }
        }
        throw new IllegalStateException("precondition: the butterfly must carry a ButterflyIslandsHuntGoal on its goal selector — the"
                + " port's shape of orig EntityButterfly.java:145-181 (" + FINDING + " test setup)");
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

    private static String describe(Entity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }
}
