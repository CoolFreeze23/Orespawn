package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Kraken;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-097: the Kraken's four behavioural PlayNicely gates. In 1.7.10, while
 * {@code OreSpawnMain.PlayNicely != 0}, the Kraken skips the thunderstorm
 * timer (orig Kraken.java:171 — the whole block, decrement included, so the
 * countdown freezes where it stands), the random lightning bolt (:915), the
 * prey search and grab (:961, with the :974-981 findSomethingToAttack
 * fallback inside the same branch) and {@code findSomethingToAttack()}
 * returns null ahead of its search (:1131-1133). Every one of those sites
 * read the LIVE static — never the constructor snapshot (:70-76) nor the
 * DataWatcher copy (:97/:914) — and the port reads
 * {@code OreSpawnConfig.PLAY_NICELY.get()} at each site, TheKing's BOSS-017
 * convention. Each test therefore constructs its Kraken with the flag OFF and
 * flips it ON before the gated code first runs, so a snapshot-based gate
 * would fail the ON assertion; the target test flips the same Kraken both
 * ways to pin the live read directly.
 *
 * <p>One test per gate, each in its own batch (TEST-003: the flag is GLOBAL
 * and three of the four hold it flipped across ticks). Every flip is undone
 * on every path, including inside the delayed lambdas. The game-test server
 * runs with {@code doWeatherCycle} OFF (GameTestServer.TEST_GAME_RULES), so a
 * storm the Kraken summons would never expire on its own: the three tests
 * whose Kraken ticks snapshot the weather, force it clear (the server's own
 * start-up values, GameTestServer.java:160) and restore the snapshot in their
 * cleanup.</p>
 *
 * <p>Seams: the private {@code findSomethingToAttack()} and {@code caught}
 * are reached by reflection — the tree's precedent for private entity
 * members (CrashReproTests on Godzilla.doJumpDamage, EntityLogicTestsB on
 * LaserBall.isSpecial) — and {@code Entity.random} is replaced the way
 * VortexParityTests does it, reusing its {@link VortexParityTests.ForcedRoll}.
 * Nothing in Kraken.java was widened for these tests.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KrakenPlayNicelyGateTests {

    /*
     * Geometry rule for every position below: the framework encases a running
     * test in a BARRIER shell (StructureUtils.encaseStructure, called from
     * GameTestInfo.prepareTestStructure) — walls one block outside the x/z
     * bounds and, because @GameTest.skyAccess defaults to false, a barrier
     * ceiling in the row just above the template's top layer (rel y=17 in the
     * 16-tall empty_large, rel y=35 in the 34-tall empty_tall; the template
     * itself occupies rel y=1.., the structure block sits at rel y=0). The
     * Kraken's eye is 12.75 above its feet (4x15 box, eyeHeight = 0.85 h) and
     * both line-of-sight gates clip from that eye to the victim, so the eye
     * must stay INSIDE the shell: feet no higher than rel 4 in empty_large or
     * rel 22 in empty_tall. (First run: feet at 8 and 15 put the eye above the
     * ceiling, every ray hit the barrier, and both control phases returned
     * null.)
     */
    /** Weather and lightning tests (no line of sight involved): mid-template, like the ENT-S-096 tests. */
    private static final BlockPos KRAKEN_POS = new BlockPos(24, 8, 24);
    /** Target test: feet on the template floor, eye at rel 13.75 — under empty_large's barrier ceiling at rel 17. */
    private static final BlockPos TARGET_KRAKEN_POS = new BlockPos(24, 1, 24);
    /** Target search: 5 blocks away, inside the 20/40/20 inflation of the Kraken's box (orig Kraken.java:1134), clear line of sight. */
    private static final Vec3 TARGET_VICTIM_POS = new Vec3(29.5, 1.0, 24.5);
    /**
     * Prey geometry: orig Kraken.java:1047-1056 attackWithSomething grabs only
     * when the victim is within sqrt(30) of the point 15 blocks straight below
     * the Kraken. That needs 15 blocks of drop under the feet plus 12.75 of
     * eye above them, all inside the shell, so the prey test runs in the
     * 34-tall empty_tall: feet at rel 18 (eye 30.75, ceiling at 35), victim at
     * rel 3 on the same column.
     */
    private static final BlockPos PREY_KRAKEN_POS = new BlockPos(24, 18, 24);
    private static final Vec3 PREY_VICTIM_POS = new Vec3(24.5, 3.0, 24.5);

    /** orig Kraken.java:63 {@code weather_set = 10}: armed at construction, fires on its 10th tick (orig :172-184, port BUG-018 block). */
    private static final int WEATHER_ARM_TICKS = 10;
    /** ON hold, longer than the arm: a missing gate summons the storm inside this window. */
    private static final int WEATHER_ON_TICKS = WEATHER_ARM_TICKS + 5;
    /** First OFF check, shorter than the arm: the countdown resumes from 10, so nothing has fired yet. */
    private static final int WEATHER_RESUME_EARLY_TICKS = WEATHER_ARM_TICKS - 5;
    /** Second OFF check, {@value #WEATHER_ARM_TICKS} ticks later (15 after the flip): the resumed countdown has fired. */
    private static final int WEATHER_RESUME_LATE_TICKS = WEATHER_ARM_TICKS;
    /** AI-step holds for the lightning and prey gates; both stay under the 10-tick weather arm so no storm is summoned as a side effect. */
    private static final int AI_ON_TICKS = 8;
    private static final int AI_OFF_TICKS = 5;

    // ------------------------------------------------------------------
    // Gate 1 — thunderstorm timer, orig Kraken.java:171
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:171 {@code weather_set > 0 && PlayNicely == 0}: the
     * timer block is skipped whole while nice, so (a) {@value #WEATHER_ON_TICKS}
     * ticks of PlayNicely over a freshly armed Kraken summon nothing and (b)
     * the countdown resumes from 10 once the flag clears — still clear
     * {@value #WEATHER_RESUME_EARLY_TICKS} ticks after the flip, raining and
     * thundering 15 ticks after it (port BUG-018 block:
     * {@code setWeatherParameters(0, 300, true, true)} when not raining).
     * The Kraken is frozen: the timer lives in {@code tick()}, not the AI step.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "krakenGateWeather")
    public void s097_weather_timer_frozen_while_play_nicely(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        final WeatherSnapshot weather = WeatherSnapshot.take(level);
        Kraken spawned = null;
        try {
            WeatherSnapshot.forceClear(level);
            OreSpawnConfig.PLAY_NICELY.set(false);
            spawned = spawnFrozen(helper, KRAKEN_POS);
            // Before its first tick: live flag ON, constructor snapshot OFF (4x15).
            OreSpawnConfig.PLAY_NICELY.set(true);
        } catch (Throwable e) {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            weather.restore(level);
            if (spawned != null) spawned.discard();
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed after restoring the PlayNicely flag", e);
        }
        final Kraken kraken = spawned;
        final Runnable cleanup = () -> {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            weather.restore(level);
            kraken.discard();
        };
        helper.runAfterDelay(WEATHER_ON_TICKS, () -> {
            boolean flipped = false;
            try {
                assertClear(helper, level, "playNicely=true held " + WEATHER_ON_TICKS
                        + " ticks over a Kraken armed at construction (orig Kraken.java:63 weather_set = 10)"
                        + " must not summon the storm (orig :171 gate)");
                OreSpawnConfig.PLAY_NICELY.set(false);
                flipped = true;
            } finally {
                if (!flipped) cleanup.run();
            }
            helper.runAfterDelay(WEATHER_RESUME_EARLY_TICKS, () -> {
                boolean stillClear = false;
                try {
                    assertClear(helper, level, WEATHER_RESUME_EARLY_TICKS
                            + " ticks after clearing playNicely the countdown must have resumed from 10, not fired"
                            + " (orig Kraken.java:171 skips the decrement while nice)");
                    stillClear = true;
                } finally {
                    if (!stillClear) cleanup.run();
                }
                helper.runAfterDelay(WEATHER_RESUME_LATE_TICKS, () -> {
                    try {
                        // ServerLevelData flags: what setWeatherParameters sets (ENT-S-097 refuter: Level.isRaining /
                        // isThundering are the RAMPED levels, +0.01 per tick even with doWeatherCycle=false, so they
                        // cannot be read a few ticks after the fire)
                        ServerLevelData stormData = (ServerLevelData) level.getLevelData();
                        helper.assertTrue(stormData.isRaining() && stormData.isThundering(),
                                (WEATHER_RESUME_EARLY_TICKS + WEATHER_RESUME_LATE_TICKS)
                                        + " ticks after clearing playNicely the resumed 10-tick countdown must have summoned"
                                        + " rain+thunder (orig Kraken.java:171-185, port BUG-018 block): raining="
                                        + stormData.isRaining() + " thundering=" + stormData.isThundering() + " (ENT-S-097)");
                    } finally {
                        cleanup.run();
                    }
                    helper.succeed();
                });
            });
        });
    }

    // ------------------------------------------------------------------
    // Gate 2 — random lightning bolt, orig Kraken.java:915
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:915 {@code nextInt(400) == 1 && PlayNicely == 0}: with
     * the entity random forced so every {@code nextInt(400)} answers 1
     * ({@link VortexParityTests.ForcedRoll} on {@code Entity.random}),
     * {@value #AI_ON_TICKS} AI steps under PlayNicely spawn no
     * {@link LightningBolt}; {@value #AI_OFF_TICKS} steps after the flip at
     * least one exists (the port spawns one bolt per hitting step, orig
     * :916, and a bolt lives 3+ ticks). AI stays ON: the roll lives in
     * customServerAiStep. Weather is forced clear so no natural strike can
     * pollute the ON count.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200, batch = "krakenGateLightning")
    public void s097_lightning_roll_vetoed_while_play_nicely(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        final WeatherSnapshot weather = WeatherSnapshot.take(level);
        Kraken spawned = null;
        try {
            WeatherSnapshot.forceClear(level);
            OreSpawnConfig.PLAY_NICELY.set(false);
            spawned = spawnLive(helper, KRAKEN_POS);
            RandomSource forced = new VortexParityTests.ForcedRoll(RandomSource.create(1234L), 400, 1);
            replaceRandom(spawned, forced);
            helper.assertTrue(spawned.getRandom() == forced,
                    "Entity.random replacement did not take (ENT-S-097 lightning test seam)");
            OreSpawnConfig.PLAY_NICELY.set(true);
        } catch (Throwable e) {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            weather.restore(level);
            if (spawned != null) spawned.discard();
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed after restoring the PlayNicely flag", e);
        }
        final Kraken kraken = spawned;
        final Runnable cleanup = () -> {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            weather.restore(level);
            for (LightningBolt bolt : boltsNear(level, kraken)) bolt.discard();
            kraken.discard();
        };
        helper.runAfterDelay(AI_ON_TICKS, () -> {
            boolean flipped = false;
            try {
                int bolts = boltsNear(level, kraken).size();
                helper.assertTrue(bolts == 0, AI_ON_TICKS + " AI steps with playNicely=true and nextInt(400) forced to 1"
                        + " must summon no lightning (orig Kraken.java:915 gate), saw " + bolts + " bolt(s) (ENT-S-097)");
                OreSpawnConfig.PLAY_NICELY.set(false);
                flipped = true;
            } finally {
                if (!flipped) cleanup.run();
            }
            helper.runAfterDelay(AI_OFF_TICKS, () -> {
                try {
                    int bolts = boltsNear(level, kraken).size();
                    helper.assertTrue(bolts >= 1, AI_OFF_TICKS + " AI steps with playNicely=false and nextInt(400) forced to 1"
                            + " must summon lightning (orig Kraken.java:915-916), saw none (ENT-S-097)");
                } finally {
                    cleanup.run();
                }
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------
    // Gate 3 — prey search and grab, orig Kraken.java:961
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:961 {@code caught == null && nextInt(8) == 1 &&
     * PlayNicely == 0}: a survival player stands 15 blocks under the Kraken
     * (the :1047-1056 grab window) and the prey roll is forced to hit every
     * step. {@value #AI_ON_TICKS} AI steps under PlayNicely leave
     * {@code caught} null — the branch is skipped before the player lookup,
     * so this pins the CALL-SITE gate, not the findSomethingToAttack one —
     * and {@value #AI_OFF_TICKS} steps after the flip the player is held.
     * The re-target (:921) and random-release (:1002) rolls on 250, the
     * in-grip bite (:999) on 50 and the lightning roll (:915) on 400 are
     * forced to miss, so the outcome is deterministic and the victim is
     * neither hurt nor dropped. The grab geometry is re-pinned at the flip
     * because the ON phase's flight (orig :1015-1020) drifts the Kraken.
     * Runs in the 34-tall empty_tall so the eye (feet + 12.75) and the
     * 15-block drop both fit under the barrier ceiling (see the geometry
     * note on the position constants); line of sight is asserted as a
     * precondition at setup and again after the re-pin.
     */
    @GameTest(template = "empty_tall", timeoutTicks = 200, batch = "krakenGatePrey")
    public void s097_prey_search_skipped_while_play_nicely(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        final WeatherSnapshot weather = WeatherSnapshot.take(level);
        final Vec3 krakenAt = helper.absoluteVec(Vec3.atBottomCenterOf(PREY_KRAKEN_POS));
        final Vec3 victimAt = helper.absoluteVec(PREY_VICTIM_POS);
        Kraken spawned = null;
        ServerPlayer player = null;
        try {
            WeatherSnapshot.forceClear(level);
            OreSpawnConfig.PLAY_NICELY.set(false);
            spawned = spawnLive(helper, PREY_KRAKEN_POS);
            replaceRandom(spawned, preyRolls());
            player = survivalPlayerAt(helper, victimAt);
            helper.assertTrue(spawned.hasLineOfSight(player),
                    "precondition: the Kraken (eye rel 30.75) must see the victim 15 blocks below inside the"
                            + " barrier shell (ceiling at rel 35 in empty_tall) (ENT-S-097 test geometry)");
            OreSpawnConfig.PLAY_NICELY.set(true);
        } catch (Throwable e) {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            weather.restore(level);
            removePlayer(helper, player);
            if (spawned != null) spawned.discard();
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed after restoring the PlayNicely flag", e);
        }
        final Kraken kraken = spawned;
        final ServerPlayer victim = player;
        final Runnable cleanup = () -> {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            weather.restore(level);
            removePlayer(helper, victim);
            kraken.discard();
        };
        helper.runAfterDelay(AI_ON_TICKS, () -> {
            boolean flipped = false;
            try {
                LivingEntity caught = caughtOf(kraken);
                helper.assertTrue(caught == null, AI_ON_TICKS + " AI steps with playNicely=true, nextInt(8) forced to 1"
                        + " and a survival player 15 blocks below must grab nobody (orig Kraken.java:961 gate), caught="
                        + caught + " (ENT-S-097)");
                // The ON phase's flight drifted the Kraken; re-pin the grab geometry, then clear the flag.
                kraken.moveTo(krakenAt.x, krakenAt.y, krakenAt.z, kraken.getYRot(), kraken.getXRot());
                kraken.setDeltaMovement(Vec3.ZERO);
                victim.teleportTo(level, victimAt.x, victimAt.y, victimAt.z, 0.0f, 0.0f);
                helper.assertTrue(kraken.hasLineOfSight(victim),
                        "precondition: the re-pinned Kraken must see the victim inside the barrier shell (ENT-S-097 test geometry)");
                OreSpawnConfig.PLAY_NICELY.set(false);
                flipped = true;
            } finally {
                if (!flipped) cleanup.run();
            }
            helper.runAfterDelay(AI_OFF_TICKS, () -> {
                try {
                    LivingEntity caught = caughtOf(kraken);
                    helper.assertTrue(caught == victim, AI_OFF_TICKS + " AI steps with playNicely=false, nextInt(8) forced to 1"
                            + " and a survival player 15 blocks below must grab that player (orig Kraken.java:961-969,"
                            + " :1047-1056), caught=" + caught + " (ENT-S-097)");
                } finally {
                    cleanup.run();
                }
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------
    // Gate 4 — findSomethingToAttack, orig Kraken.java:1131-1133
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:1131-1133: {@code findSomethingToAttack()} returns null
     * while nice, ahead of its 20/40/20 search (:1134-1143). Synchronous: a
     * frozen Kraken with its feet on the template floor (eye at rel 13.75,
     * inside the barrier shell — see the geometry note on the position
     * constants), a survival player 5 blocks away with clear line of sight
     * (:1060-1074 isSuitableTarget accepts a non-creative, non-flying player),
     * then off -> player, on -> null, off -> player again on the same Kraken,
     * pinning a live read. The private method is reached by reflection.
     */
    @GameTest(template = "empty_large", batch = "krakenGateTarget")
    public void s097_find_something_to_attack_null_while_play_nicely(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Kraken kraken = null;
        ServerPlayer victim = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            kraken = spawnFrozen(helper, TARGET_KRAKEN_POS);
            victim = survivalPlayerAt(helper, helper.absoluteVec(TARGET_VICTIM_POS));
            helper.assertTrue(kraken.hasLineOfSight(victim),
                    "precondition: the Kraken (eye rel 13.75) must see the victim 5 blocks away inside the"
                            + " barrier shell (ceiling at rel 17 in empty_large) (ENT-S-097 test geometry)");
            LivingEntity found = findSomethingToAttack(kraken);
            helper.assertTrue(found == victim, "control: playNicely=false must find the survival player 5 blocks away"
                    + " (orig Kraken.java:1134-1143), got " + found + " (ENT-S-097)");
            OreSpawnConfig.PLAY_NICELY.set(true);
            found = findSomethingToAttack(kraken);
            helper.assertTrue(found == null, "playNicely=true: findSomethingToAttack() must return null ahead of the search"
                    + " (orig Kraken.java:1131-1133), got " + found + " (ENT-S-097)");
            OreSpawnConfig.PLAY_NICELY.set(false);
            found = findSomethingToAttack(kraken);
            helper.assertTrue(found == victim, "the gate must read the live flag: clearing playNicely on the same Kraken"
                    + " must find the player again, got " + found + " (ENT-S-097)");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            removePlayer(helper, victim);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Kraken spawnFrozen(GameTestHelper helper, BlockPos pos) {
        Kraken kraken = helper.spawnWithNoFreeWill(ModEntities.KRAKEN.get(), pos);
        kraken.setNoAi(true);
        kraken.setPersistenceRequired();
        return kraken;
    }

    /** AI left ON: the lightning roll and the prey search run from customServerAiStep, like the ENT-S-096 datum test. */
    private static Kraken spawnLive(GameTestHelper helper, BlockPos pos) {
        Kraken kraken = helper.spawn(ModEntities.KRAKEN.get(), pos);
        kraken.setPersistenceRequired();
        return kraken;
    }

    private static void assertClear(GameTestHelper helper, ServerLevel level, String why) {
        // the ServerLevelData flags, not the ramped Level.isRaining()/isThundering() (see the final assertion)
        ServerLevelData data = (ServerLevelData) level.getLevelData();
        helper.assertTrue(!data.isRaining() && !data.isThundering(),
                why + ": raining=" + data.isRaining() + " thundering=" + data.isThundering() + " (ENT-S-097)");
    }

    private static List<LightningBolt> boltsNear(ServerLevel level, Kraken kraken) {
        return level.getEntitiesOfClass(LightningBolt.class, kraken.getBoundingBox().inflate(64.0, 64.0, 64.0));
    }

    /**
     * Entity random for the prey test: orig Kraken.java:961 {@code nextInt(8) == 1}
     * always hits; orig :921 re-target and :1002 random release
     * ({@code nextInt(250) == 1}), :999 in-grip bite ({@code nextInt(50) == 1})
     * and :915 lightning ({@code nextInt(400) == 1}) never do. ForcedRoll
     * delegates unmatched bounds, so the wrappers chain.
     */
    private static RandomSource preyRolls() {
        RandomSource rolls = RandomSource.create(1234L);
        rolls = new VortexParityTests.ForcedRoll(rolls, 400, 0);
        rolls = new VortexParityTests.ForcedRoll(rolls, 250, 0);
        rolls = new VortexParityTests.ForcedRoll(rolls, 50, 0);
        return new VortexParityTests.ForcedRoll(rolls, 8, 1);
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

    /** orig Kraken.java:60 {@code caught}, port private field of the same name. */
    private static LivingEntity caughtOf(Kraken kraken) {
        try {
            Field field = Kraken.class.getDeclaredField("caught");
            field.setAccessible(true);
            return (LivingEntity) field.get(kraken);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read Kraken.caught", exception);
        }
    }

    /** orig Kraken.java:1130 {@code findSomethingToAttack()}, port private method of the same name. */
    private static LivingEntity findSomethingToAttack(Kraken kraken) {
        try {
            Method method = Kraken.class.getDeclaredMethod("findSomethingToAttack");
            method.setAccessible(true);
            return (LivingEntity) method.invoke(kraken);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke Kraken.findSomethingToAttack", exception);
        }
    }

    /**
     * A survival ServerPlayer that lives in the level: the game-test server
     * defaults to CREATIVE (GameTestServer.java:85) and the Kraken skips
     * instabuild players (orig Kraken.java:965/:1069, port
     * findNearestValidPlayer / isSuitableTarget). Health is raised so nothing
     * incidental can kill the victim and clear {@code caught}. Deprecated
     * mock-player factory tolerated the way EntityLogicTestsA does.
     */
    @SuppressWarnings({"removal", "deprecation"})
    private static ServerPlayer survivalPlayerAt(GameTestHelper helper, Vec3 absolutePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
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

    /**
     * doWeatherCycle is OFF in the game-test server (GameTestServer.TEST_GAME_RULES),
     * so weather flags only ever change through explicit sets: whatever a test
     * does to them it must undo itself, exactly.
     */
    private record WeatherSnapshot(int clearTime, int rainTime, int thunderTime, boolean raining, boolean thundering) {
        static WeatherSnapshot take(ServerLevel level) {
            ServerLevelData data = (ServerLevelData) level.getLevelData();
            return new WeatherSnapshot(data.getClearWeatherTime(), data.getRainTime(), data.getThunderTime(),
                    data.isRaining(), data.isThundering());
        }

        /** The server's own start-up weather (GameTestServer.java:160). */
        static void forceClear(ServerLevel level) {
            level.setWeatherParameters(20000000, 20000000, false, false);
        }

        void restore(ServerLevel level) {
            ServerLevelData data = (ServerLevelData) level.getLevelData();
            data.setClearWeatherTime(this.clearTime);
            data.setRainTime(this.rainTime);
            data.setThunderTime(this.thunderTime);
            data.setRaining(this.raining);
            data.setThundering(this.thundering);
        }
    }
}
