package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.Cephadrome;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityGammaMetroid;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-114 (targeting ledger T7, wave 1): the 1.7.10 PEACEFUL gates of five hunters the engine never
 * despawns on Peaceful — the Ant Robot (EntityLiving / Mob), the Cephadrome's hunt roll (EntityCreature /
 * PathfinderMob), the Dragonfly (EntityAnimal / Animal), an untamed Gamma Metroid (EntityTameable /
 * TamableAnimal) and the Purple Power (EntityLiving / Mob) — restored at the orig positions with the
 * orig polarity:
 * <ul>
 *   <li>AntRobot.java:105 {@code owned == 0 && difficulty != PEACEFUL} around the whole unridden block;
 *       :617 and :620 {@code != PEACEFUL} leading the ridden stomp and the ridden hunt (port
 *       AntRobot.customServerAiStep / tick);</li>
 *   <li>Cephadrome.java:488 {@code nextInt(7)==1 && difficulty != PEACEFUL} around the hunt block, a
 *       stored revenge target included (port customServerAiStep; the :516 filter head is ENT-S-113's,
 *       pinned by CephadromeGateTests and not repeated here);</li>
 *   <li>Dragonfly.java:142 around the hunt branch — the scan, the retarget and the :147-148 bite — and
 *       :198 at the head of the filter (port DragonflyHuntGoal.onRetargetSkipped / tick / findPrey);</li>
 *   <li>GammaMetroid.java:241 leading the 1-in-5 hunt and :254 at the head of the filter (port
 *       EntityGammaMetroid.customServerAiStep / isSuitableTarget);</li>
 *   <li>PurplePower.java:173 on the hunt call site, :180-182 {@code setDead} on every AI tick on
 *       Peaceful, :236 at the head of the filter (port PurplePower.customServerAiStep /
 *       isSuitableTarget).</li>
 * </ul>
 *
 * <p>One generated test per port site — twelve {@link TestFunction}s from a {@link GameTestGenerator}
 * over {@link #sites()} in orig file order, each a {@link Probe} — in the CephadromeGateTests shape:
 * the game-test level runs NORMAL, asserted as the precondition; the site is driven once on NORMAL and
 * must show its effect (the control); the effect is undone; the difficulty is flipped with
 * {@code MinecraftServer.setDifficulty(PEACEFUL, true)} inside the test and asserted through
 * {@code level.getDifficulty()}; the same hunter and prey are driven once more and must show nothing of
 * it; the difficulty is restored in a finally on every path. Filter sites go through the private filter
 * by reflection (the CreativeMappingParityTests idiom). The AI-step sites invoke the hunter's
 * {@code customServerAiStep} — the ridden Ant Robot its public {@code tick}, the Dragonfly its hunt
 * goal's {@code onRetargetSkipped} / {@code tick} / {@code findPrey} — once under a forced
 * {@code Entity.random} (the VortexParityTests.ForcedRoll seam, as the ENT-S-109 Brutalfly strafe test
 * drives it) with every roll on the path pinned, and read an observable back: the synched attacking
 * flag, damage on a 1000-HP prey (its hurt cooldown cleared between drives), the stored target, the
 * private flight target, or the entity's own removal for the Purple Power's :180-182 discard.</p>
 *
 * <p>Synchronous — nothing ticks between the flip and the restore, so the flip can despawn nothing and
 * no concurrent test can observe it. Own batch {@code peacefulGateParity} (TEST-003). Geometry as
 * CreativeMappingParityTests: the hunter frozen at rel (20,1,24) on the floor of the 48x16x48
 * empty_large, the prey east on the same floor at 8 blocks (inside every scan box, outside every melee
 * reach, inside the Ant Robot's 6..9 stomp ring), 5 blocks (inside the Ant Robot's 6 + w/2 melee reach)
 * or 2 blocks (the Metroid's distSq &lt;= 9 and the Dragonfly's distSq &lt; 6 bites); line of sight
 * asserted. The Dragonfly's prey is a butterfly — on the orig whitelist (:216) and under the port's 0.6
 * width rule, so the row survives the allies fix either way; everyone else's is a vanilla pig. Spawns
 * are frozen and discarded in the finally; the Ant Robot's rider is a mock survival player dismounted
 * and removed there.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PeacefulGateParityTests {

    private static final String BATCH = "peacefulGateParity";
    private static final String TEST_PREFIX = "peacefulgateparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (IgnoreScreenParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-114";

    /** The hunter on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside every scan box, outside every melee reach, inside the Ant Robot's 6..9 stomp ring (orig AntRobot.java:977-986). */
    private static final BlockPos FAR_PREY_POS = new BlockPos(28, 1, 24);
    /** 5 blocks east: inside the Ant Robot's 6 + w/2 melee reach (orig AntRobot.java:136, :624), outside its stomp ring. */
    private static final BlockPos MELEE_PREY_POS = new BlockPos(25, 1, 24);
    /** 2 blocks east: inside the Metroid's distSq &lt;= 9 bite (orig GammaMetroid.java:243) and the Dragonfly's distSq &lt; 6 bite (orig Dragonfly.java:147). */
    private static final BlockPos CLOSE_PREY_POS = new BlockPos(22, 1, 24);
    /** Prey health, high enough that no pinned hit kills it. */
    private static final float PREY_HEALTH = 1000.0f;

    // ------------------------------------------------------------------
    // The site table, in orig file order
    // ------------------------------------------------------------------

    /**
     * One port site. {@link #setUp} spawns the hunter and its prey (on NORMAL); {@link #drive}
     * exercises the site once and answers whether the orig-gated effect showed; {@link #reset} undoes
     * that effect so the second drive starts from the same state; {@link #trace} names what the last
     * drive observed, for the failure message; {@link #cleanUp} discards the spawns and runs in the
     * finally, tolerant of a set-up that never finished.
     */
    private interface Probe {
        void setUp(GameTestHelper helper);

        boolean drive(GameTestHelper helper);

        void reset(GameTestHelper helper);

        String trace();

        void cleanUp(GameTestHelper helper);
    }

    /** One orig gate and the port site that carries it. */
    private record Site(int index, String tag, String orig, String port, String effect, Supplier<Probe> probe) {
        String testName() {
            return TEST_PREFIX + String.format("s114_%02d_%s", this.index, this.tag);
        }

        String where() {
            return this.port + " (orig " + this.orig + ")";
        }
    }

    private static List<Site> sites() {
        List<Site> sites = new ArrayList<>();
        sites.add(new Site(1, "antrobot_105_unridden_hunt", "AntRobot.java:105", "AntRobot.customServerAiStep",
                "the unridden hunt — a pig 5 blocks off picked, attacking set and the 1-in-15 melee landed",
                AntRobotUnriddenHunt::new));
        sites.add(new Site(2, "antrobot_617_ridden_stomp", "AntRobot.java:617", "AntRobot.tick (the ridden stomp)",
                "the ridden 1-in-50 stomp — a pig 8 blocks off, inside the 6..9 ring, hit for attack/10",
                AntRobotRiddenStomp::new));
        sites.add(new Site(3, "antrobot_620_ridden_melee", "AntRobot.java:620", "AntRobot.tick (the ridden hunt)",
                "the ridden 1-in-9 hunt — a pig 5 blocks off picked, attacking set and the melee landed",
                AntRobotRiddenMelee::new));
        sites.add(new Site(4, "cephadrome_488_hunt_roll", "Cephadrome.java:488", "Cephadrome.customServerAiStep",
                "the 1-in-7 hunt block pursuing a stored revenge target — attacking set",
                CephadromeHuntRoll::new));
        sites.add(new Site(5, "dragonfly_142_hunt_roll", "Dragonfly.java:142", "DragonflyHuntGoal.onRetargetSkipped",
                "the 1-in-12 hunt roll — a butterfly 8 blocks off scanned and stored as the target",
                DragonflyHuntRoll::new));
        sites.add(new Site(6, "dragonfly_147_bite", "Dragonfly.java:147-148 (inside the :142 branch)", "DragonflyHuntGoal.tick",
                "the bite on a held target 2 blocks off",
                DragonflyBite::new));
        sites.add(new Site(7, "dragonfly_198_filter", "Dragonfly.java:198", "DragonflyHuntGoal.findPrey (the filter)",
                "the filter taking a butterfly 8 blocks off",
                DragonflyFilter::new));
        sites.add(new Site(8, "gammametroid_241_hunt_roll", "GammaMetroid.java:241", "EntityGammaMetroid.customServerAiStep",
                "the 1-in-5 hunt — a pig 2 blocks off picked and bitten",
                GammaMetroidHuntRoll::new));
        sites.add(new Site(9, "gammametroid_254_filter", "GammaMetroid.java:254", "EntityGammaMetroid.isSuitableTarget",
                "the filter taking a pig 8 blocks off",
                GammaMetroidFilter::new));
        sites.add(new Site(10, "purplepower_173_hunt_roll", "PurplePower.java:173", "PurplePower.customServerAiStep",
                "the 1-in-7 hunt — the flight target moved onto a pig 8 blocks off",
                PurplePowerHuntRoll::new));
        sites.add(new Site(11, "purplepower_180_peaceful_discard", "PurplePower.java:180-182",
                "PurplePower.customServerAiStep (after the flight-target block)",
                "the Purple Power outliving one AI step",
                PurplePowerDiscard::new));
        sites.add(new Site(12, "purplepower_236_filter", "PurplePower.java:236", "PurplePower.isSuitableTarget",
                "the filter taking a pig 8 blocks off",
                PurplePowerFilter::new));
        return sites;
    }

    /** One test per port site: 12 TestFunctions in the {@code peacefulGateParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> peacefulGateSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (Site site : sites()) {
            functions.add(new TestFunction(BATCH, site.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, site)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: control on NORMAL, the same site silent on PEACEFUL
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Site site) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        helper.assertTrue(before != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful (" + FINDING + " test setup)");
        Probe probe = site.probe().get();
        try {
            probe.setUp(helper);
            helper.assertTrue(probe.drive(helper), "control: on " + before + " " + site.where() + " must show "
                    + site.effect() + " — saw " + probe.trace() + " (" + FINDING + ")");
            probe.reset(helper);
            server.setDifficulty(Difficulty.PEACEFUL, true);
            helper.assertTrue(helper.getLevel().getDifficulty() == Difficulty.PEACEFUL,
                    "precondition: MinecraftServer.setDifficulty(PEACEFUL, true) must show through level.getDifficulty()"
                            + " (" + FINDING + " test setup)");
            helper.assertTrue(!probe.drive(helper), site.where() + " on PEACEFUL: orig " + site.orig()
                    + " gates this out on Peaceful, so the same hunter and prey that showed " + site.effect() + " on "
                    + before + " must show nothing of it — saw " + probe.trace() + " (" + FINDING + ")");
        } finally {
            server.setDifficulty(before, true);
            probe.cleanUp(helper);
        }
        helper.succeed();
    }

    /** A hunter and one prey, both frozen; the base of every probe. */
    private abstract static class HunterProbe implements Probe {
        Mob hunter;
        Mob prey;
        String trace = "(not driven)";

        @Override
        public String trace() {
            return this.trace;
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            discardQuietly(this.prey);
            discardQuietly(this.hunter);
        }
    }

    // ------------------------------------------------------------------
    // AntRobot — orig AntRobot.java:105, :617, :620
    // ------------------------------------------------------------------

    /**
     * orig AntRobot.java:105 (port customServerAiStep): the stomp (1-in-20) and the release (1-in-150)
     * pinned quiet, the melee (1-in-15) pinned to fire; a pig 5 blocks off is inside the 6 + w/2 reach
     * (:136), so on NORMAL the transient pick is looked at, attacking goes to 1 and the melee lands
     * (:137-138); on Peaceful the block is never entered.
     */
    private static final class AntRobotUnriddenHunt extends HunterProbe {
        AntRobot ant;

        @Override
        public void setUp(GameTestHelper helper) {
            this.ant = spawnFrozen(helper, ModEntities.ANT_ROBOT.get(), HUNTER_POS);
            this.hunter = this.ant;
            this.prey = spawnPrey(helper, EntityType.PIG, MELEE_PREY_POS);
            replaceRandom(this.ant, rolls(20, 1, 150, 1, 15, 0));
            helper.assertTrue(this.ant.getOwned() == 0 && this.ant.getFirstPassenger() == null,
                    "precondition: a fresh Ant Robot is unowned and unridden — orig AntRobot.java:101 and the other"
                            + " term of :105 (" + FINDING + " test setup)");
            assertSees(helper, this.ant, this.prey);
            helper.assertTrue(this.ant.getAttacking() == 0 && this.ant.getTarget() == null,
                    "precondition: attacking 0 and no stored target before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            invokeCustomServerAiStep(this.ant);
            this.trace = "attacking=" + this.ant.getAttacking() + ", pig health " + this.prey.getHealth();
            return this.ant.getAttacking() == 1 || this.prey.getHealth() < PREY_HEALTH;
        }

        @Override
        public void reset(GameTestHelper helper) {
            this.ant.setAttacking(0);
            healPrey(this.prey);
        }
    }

    /**
     * The ridden Ant Robot: a mock survival rider mounted with {@code startRiding(ant, true)} (the
     * RideTests idiom) — refused by the rider identity check in both scans (orig :968, :1041) — dismounted
     * and removed in the finally. The site lives in the public {@code tick}, so the whole entity tick
     * runs (the VortexParityTests precedent for a hand-ticked entity); the ant is noAi and its rider a
     * ServerPlayer, so the ridden travel zeroes motion and nothing moves.
     */
    private abstract static class RiddenAntProbe extends HunterProbe {
        AntRobot ant;
        ServerPlayer rider;

        void mount(GameTestHelper helper) {
            this.ant = spawnFrozen(helper, ModEntities.ANT_ROBOT.get(), HUNTER_POS);
            this.hunter = this.ant;
            this.rider = playerAt(helper, GameType.SURVIVAL, this.ant.position());
            helper.assertTrue(this.rider.startRiding(this.ant, true) && this.ant.getFirstPassenger() == this.rider,
                    "precondition: the mock player must mount the Ant Robot — orig AntRobot.java:617 / :620 read"
                            + " riddenByEntity != null (" + FINDING + " test setup)");
        }

        @Override
        public void cleanUp(GameTestHelper helper) {
            if (this.rider != null) {
                this.rider.stopRiding();
                removePlayer(helper, this.rider);
            }
            super.cleanUp(helper);
        }
    }

    /**
     * orig AntRobot.java:617-619 (port tick): the ridden 1-in-50 stomp pinned to fire, the 1-in-9 hunt
     * (:620) pinned quiet; a pig 8 blocks off stands inside the stomp ring 6..9 (:977-986) and takes
     * attack/10 (:1000) on NORMAL; the {@code != PEACEFUL} term leads :617.
     */
    private static final class AntRobotRiddenStomp extends RiddenAntProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            mount(helper);
            this.prey = spawnPrey(helper, EntityType.PIG, FAR_PREY_POS);
            replaceRandom(this.ant, rolls(50, 0, 9, 1));
            double dist = this.ant.distanceTo(this.prey);
            helper.assertTrue(dist >= 6.0 && dist <= 9.0, "precondition: the pig must stand inside the stomp ring 6..9"
                    + " (orig AntRobot.java:977-986) — at " + dist + " (" + FINDING + " test geometry)");
            assertSees(helper, this.ant, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            this.ant.tick();
            this.trace = "pig health " + this.prey.getHealth();
            return this.prey.getHealth() < PREY_HEALTH;
        }

        @Override
        public void reset(GameTestHelper helper) {
            healPrey(this.prey);
        }
    }

    /**
     * orig AntRobot.java:620-631 (port tick): the ridden 1-in-9 hunt pinned to fire, the stomp (:617)
     * pinned quiet; a pig 5 blocks off is inside the 6 + w/2 reach (:624), so on NORMAL the pick is
     * bitten and attacking goes to 1 (:625-626); the {@code != PEACEFUL} term leads :620.
     */
    private static final class AntRobotRiddenMelee extends RiddenAntProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            mount(helper);
            this.prey = spawnPrey(helper, EntityType.PIG, MELEE_PREY_POS);
            replaceRandom(this.ant, rolls(50, 1, 9, 0));
            assertSees(helper, this.ant, this.prey);
            helper.assertTrue(this.ant.getAttacking() == 0,
                    "precondition: attacking 0 before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            this.ant.tick();
            this.trace = "attacking=" + this.ant.getAttacking() + ", pig health " + this.prey.getHealth();
            return this.ant.getAttacking() == 1 || this.prey.getHealth() < PREY_HEALTH;
        }

        @Override
        public void reset(GameTestHelper helper) {
            this.ant.setAttacking(0);
            healPrey(this.prey);
        }
    }

    // ------------------------------------------------------------------
    // Cephadrome — orig Cephadrome.java:488
    // ------------------------------------------------------------------

    /**
     * orig Cephadrome.java:488-512 (port customServerAiStep): the 1-in-7 roll pinned to fire, the 1-in-100
     * heal (:482) pinned quiet; a pig 8 blocks off is written as the stored target — what a hit stores at
     * :443-444 (port hurt) — and the block reads it at :489 without the filter, so on NORMAL it is chased,
     * looked at and attacking goes to 1 (:503) with no bite at 8 blocks (:504); on Peaceful the block is
     * skipped and the revenge target spared.
     */
    private static final class CephadromeHuntRoll extends HunterProbe {
        Cephadrome shark;

        @Override
        public void setUp(GameTestHelper helper) {
            this.shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            this.hunter = this.shark;
            this.prey = spawnPrey(helper, EntityType.PIG, FAR_PREY_POS);
            this.shark.setTarget(this.prey);
            replaceRandom(this.shark, rolls(7, 1, 100, 0));
            helper.assertTrue(this.shark.getTarget() == this.prey && this.shark.getAttacking() == 0,
                    "precondition: the pig is the stored revenge target and attacking is 0 (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            invokeCustomServerAiStep(this.shark);
            this.trace = "attacking=" + this.shark.getAttacking() + ", target " + describe(this.shark.getTarget());
            return this.shark.getAttacking() == 1;
        }

        @Override
        public void reset(GameTestHelper helper) {
            this.shark.setAttacking(0);
        }
    }

    // ------------------------------------------------------------------
    // Dragonfly — orig Dragonfly.java:142, :147-148, :198
    // ------------------------------------------------------------------

    /**
     * The Dragonfly and its hunt goal: the private {@code EntityDragonfly.huntGoal}, built by
     * registerGoals in the Mob constructor and still referenced after the goal selector is stripped.
     */
    private abstract static class DragonflyProbe extends HunterProbe {
        EntityDragonfly dragonfly;
        DragonflyHuntGoal goal;

        void spawnDragonfly(GameTestHelper helper, BlockPos preyPos) {
            this.dragonfly = spawnFrozen(helper, ModEntities.ENTITY_DRAGONFLY.get(), HUNTER_POS);
            this.hunter = this.dragonfly;
            this.goal = (DragonflyHuntGoal) readField(this.dragonfly, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(this.goal != null,
                    "precondition: EntityDragonfly.registerGoals must have built the hunt goal (" + FINDING + " test setup)");
            this.prey = spawnPrey(helper, ModEntities.ENTITY_BUTTERFLY.get(), preyPos);
            helper.assertTrue(this.prey.getBbWidth() <= 0.6f, "precondition: a butterfly (" + this.prey.getBbWidth()
                    + " wide) passes the port's 0.6 width rule and is on the orig whitelist (Dragonfly.java:216)"
                    + " (" + FINDING + " test setup)");
            assertSees(helper, this.dragonfly, this.prey);
        }
    }

    /**
     * orig Dragonfly.java:142-150 (port onRetargetSkipped — the flight retarget's else branch, ENT-S-135): the 1-in-12
     * roll pinned to fire; on NORMAL the butterfly 8 blocks off is scanned (:144) and, in the port's shape, handed to the
     * target slot; on Peaceful the branch is skipped and nothing is set.
     */
    private static final class DragonflyHuntRoll extends DragonflyProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            spawnDragonfly(helper, FAR_PREY_POS);
            replaceRandom(this.dragonfly, rolls(12, 0));
            helper.assertTrue(this.dragonfly.getTarget() == null,
                    "precondition: no target before the first drive (" + FINDING + " test setup)");
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            invoke(this.goal, DragonflyHuntGoal.class, "onRetargetSkipped");
            this.trace = "target " + describe(this.dragonfly.getTarget());
            return this.dragonfly.getTarget() == this.prey;
        }

        @Override
        public void reset(GameTestHelper helper) {
            this.dragonfly.setTarget(null);
        }
    }

    /**
     * orig Dragonfly.java:147-148 (port tick), the bite inside the :142 branch: the target written by hand
     * 2 blocks off (distSq 4 &lt; 6), the hunt roll (1-in-12) and the flight retarget (1-in-300) pinned
     * quiet; on NORMAL the goal's tick bites for the attack attribute, on Peaceful it does not, target or
     * no target.
     */
    private static final class DragonflyBite extends DragonflyProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            spawnDragonfly(helper, CLOSE_PREY_POS);
            replaceRandom(this.dragonfly, rolls(12, 1, 300, 1));
            this.dragonfly.setTarget(this.prey);
            helper.assertTrue(this.dragonfly.distanceToSqr(this.prey) < 6.0, "precondition: the butterfly must stand"
                    + " inside the bite reach distSq < 6 (orig Dragonfly.java:147) (" + FINDING + " test geometry)");
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            this.goal.tick();
            this.trace = "butterfly health " + this.prey.getHealth() + ", target " + describe(this.dragonfly.getTarget());
            return this.prey.getHealth() < PREY_HEALTH;
        }

        @Override
        public void reset(GameTestHelper helper) {
            healPrey(this.prey);
            this.dragonfly.setTarget(this.prey);
        }
    }

    /**
     * orig Dragonfly.java:197-229 (port findPrey's predicate): on NORMAL the scan returns the butterfly 8
     * blocks off (whitelisted at :216; under the port's width rule), on Peaceful :198 answers false for
     * every candidate and the scan returns nothing.
     */
    private static final class DragonflyFilter extends DragonflyProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            spawnDragonfly(helper, FAR_PREY_POS);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            Object found = invoke(this.goal, DragonflyHuntGoal.class, "findPrey");
            this.trace = "findPrey -> " + describe((Entity) found);
            return found == this.prey;
        }

        @Override
        public void reset(GameTestHelper helper) {
        }
    }

    // ------------------------------------------------------------------
    // GammaMetroid — orig GammaMetroid.java:241, :254
    // ------------------------------------------------------------------

    /**
     * orig GammaMetroid.java:241-250 (port customServerAiStep): the 1-in-5 roll and the 1-in-4 bite (:244)
     * pinned to fire, the stone scan (:435, 1-in-20 / 1-in-100) pinned quiet; a pig 2 blocks off (distSq
     * 4 &lt;= 9, :243) is bitten for the attack attribute on NORMAL; {@code != PEACEFUL} leads :241.
     */
    private static final class GammaMetroidHuntRoll extends HunterProbe {
        EntityGammaMetroid metroid;

        @Override
        public void setUp(GameTestHelper helper) {
            this.metroid = spawnFrozen(helper, ModEntities.ENTITY_GAMMA_METROID.get(), HUNTER_POS);
            this.hunter = this.metroid;
            helper.assertTrue(!this.metroid.isTame() && !this.metroid.isBaby(),
                    "precondition: a fresh Metroid is untamed and grown, so orig GammaMetroid.java:278 and :294 do not"
                            + " refuse (" + FINDING + " test setup)");
            this.prey = spawnPrey(helper, EntityType.PIG, CLOSE_PREY_POS);
            replaceRandom(this.metroid, rolls(5, 0, 4, 0, 20, 1, 100, 1));
            helper.assertTrue(this.metroid.distanceToSqr(this.prey) <= 9.0, "precondition: the pig must stand inside"
                    + " the bite reach distSq <= 9 (orig GammaMetroid.java:243) (" + FINDING + " test geometry)");
            assertSees(helper, this.metroid, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            invokeCustomServerAiStep(this.metroid);
            this.trace = "pig health " + this.prey.getHealth();
            return this.prey.getHealth() < PREY_HEALTH;
        }

        @Override
        public void reset(GameTestHelper helper) {
            healPrey(this.prey);
        }
    }

    /** orig GammaMetroid.java:253-288 (port isSuitableTarget): a pig 8 blocks off is taken on NORMAL (:287) and refused on Peaceful (:254). */
    private static final class GammaMetroidFilter extends HunterProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            EntityGammaMetroid metroid = spawnFrozen(helper, ModEntities.ENTITY_GAMMA_METROID.get(), HUNTER_POS);
            this.hunter = metroid;
            helper.assertTrue(!metroid.isTame(),
                    "precondition: untamed, so orig GammaMetroid.java:278 does not refuse (" + FINDING + " test setup)");
            this.prey = spawnPrey(helper, EntityType.PIG, FAR_PREY_POS);
            assertSees(helper, this.hunter, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            boolean accepted = invokeFilter(this.hunter, this.prey);
            this.trace = "isSuitableTarget(pig) = " + accepted;
            return accepted;
        }

        @Override
        public void reset(GameTestHelper helper) {
        }
    }

    // ------------------------------------------------------------------
    // PurplePower — orig PurplePower.java:173, :180-182, :236
    // ------------------------------------------------------------------

    /**
     * The Purple Power with its private flight target parked 10 blocks up (distSqr 100, past the
     * 1-in-300 / &lt; 4 retarget of port :112) — the Brutalfly-strafe idiom of CreativeMappingParityTests.
     */
    private abstract static class PurplePowerProbe extends HunterProbe {
        PurplePower power;
        BlockPos parked;

        void spawnParked(GameTestHelper helper, int huntRoll) {
            this.power = spawnFrozen(helper, ModEntities.PURPLE_POWER.get(), HUNTER_POS);
            this.hunter = this.power;
            this.parked = this.power.blockPosition().above(10);
            writeField(this.power, PurplePower.class, "currentFlightTarget", this.parked);
            replaceRandom(this.power, rolls(300, 1, 7, huntRoll));
        }

        BlockPos flightTarget() {
            return (BlockPos) readField(this.power, PurplePower.class, "currentFlightTarget");
        }
    }

    /**
     * orig PurplePower.java:173-179 (port customServerAiStep): the 1-in-300 retarget pinned quiet, the
     * 1-in-7 hunt roll pinned to fire; on NORMAL the pig 8 blocks off is scanned and the flight target
     * moves onto it (:174; 8 blocks is past the 4 + w/2 reach of :175, so no hit and no discard); on
     * Peaceful the roll is spent, the gate refuses and the parked target stays — :180-182 then discards.
     */
    private static final class PurplePowerHuntRoll extends PurplePowerProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            spawnParked(helper, 2);
            this.prey = spawnPrey(helper, EntityType.PIG, FAR_PREY_POS);
            assertSees(helper, this.power, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            invokeCustomServerAiStep(this.power);
            BlockPos mark = new BlockPos((int) this.prey.getX(), (int) (this.prey.getY() + this.prey.getBbHeight() / 2.0f),
                    (int) this.prey.getZ());
            BlockPos after = flightTarget();
            this.trace = "flight target " + after + " (the pig mark " + mark + ", parked " + this.parked + "), removed="
                    + this.power.isRemoved();
            return mark.equals(after);
        }

        @Override
        public void reset(GameTestHelper helper) {
            writeField(this.power, PurplePower.class, "currentFlightTarget", this.parked);
        }
    }

    /**
     * orig PurplePower.java:180-182 (port customServerAiStep, after the flight-target block): every AI
     * tick on Peaceful calls setDead — the port discards. Both rolls pinned quiet, no prey: on NORMAL the
     * Purple Power outlives the step, on Peaceful the step removes it.
     */
    private static final class PurplePowerDiscard extends PurplePowerProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            spawnParked(helper, 0);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            invokeCustomServerAiStep(this.power);
            this.trace = "removed=" + this.power.isRemoved() + ", alive=" + this.power.isAlive();
            return !this.power.isRemoved() && this.power.isAlive();
        }

        @Override
        public void reset(GameTestHelper helper) {
        }
    }

    /** orig PurplePower.java:234-265 (port isSuitableTarget): a pig 8 blocks off — no player, no royal (:264) — is taken on NORMAL and refused on Peaceful (:236). */
    private static final class PurplePowerFilter extends HunterProbe {
        @Override
        public void setUp(GameTestHelper helper) {
            this.hunter = spawnFrozen(helper, ModEntities.PURPLE_POWER.get(), HUNTER_POS);
            this.prey = spawnPrey(helper, EntityType.PIG, FAR_PREY_POS);
            assertSees(helper, this.hunter, this.prey);
        }

        @Override
        public boolean drive(GameTestHelper helper) {
            boolean accepted = invokeFilter(this.hunter, this.prey);
            this.trace = "isSuitableTarget(pig) = " + accepted;
            return accepted;
        }

        @Override
        public void reset(GameTestHelper helper) {
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the CephadromeGateTests / CreativeMappingParityTests idiom)
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: the " + hunter.getClass().getSimpleName()
                + " must see the " + prey.getClass().getSimpleName() + " inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** Frozen prey with 1000 HP, so no pinned hit kills it and each drive reads a clean health drop. */
    private static <E extends Mob> E spawnPrey(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E prey = spawnFrozen(helper, type, pos);
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
     * mock-player factory tolerated the way CephadromeGateTests and CreativeMappingParityTests do.
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

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as CreativeMappingParityTests.strafeRolls. */
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

    /** The hunter's private {@code isSuitableTarget(LivingEntity)} — the port's one-arg shape of the orig two-arg filter. */
    private static boolean invokeFilter(Mob hunter, LivingEntity candidate) {
        String where = hunter.getClass().getSimpleName() + ".isSuitableTarget";
        try {
            Method method = hunter.getClass().getDeclaredMethod("isSuitableTarget", LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
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
