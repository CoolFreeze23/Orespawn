package danger.orespawn.gametest;

import com.mojang.authlib.GameProfile;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnderKnight;
import danger.orespawn.entity.EnderReaper;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.Irukandji;
import danger.orespawn.entity.Lizard;
import danger.orespawn.entity.ai.AmbientFlightGoal;
import danger.orespawn.entity.ai.ButterflyIslandsHuntGoal;
import danger.orespawn.entity.ai.LunaMothFlightGoal;
import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-141 — targeting ledger batch T10 (wave 3, the last): the five "other" rows and the Luna Moth observation. The Ender
 * Knight and Ender Reaper: orig EnderKnight.java / EnderReaper.java:124-138 ran the stare-driven teleports at the head of
 * {@code onLivingUpdate}, off the target the legacy loop had left — a staring player target within distSq 16 (:126-127) sends
 * the mob away ({@code teleportRandomly}, :128) and clears the far counter (:130); any other target beyond distSq 256 counts a
 * tick and, past 30, is teleported toward ({@code teleportToEntity}, :131-132, the counter reset on a landing); no target clears
 * the scream and the counter (:134-137) — and the pick itself (:68-78) played the stare sound at the player every sixth tick of
 * a held stare and set the scream on (:74) or off (:78); the port's aiStep block, {@code teleportToEntity} and the goal's
 * {@code findTarget} carry them, and the pick runs once EVERY tick again (the legacy loop's cadence, ENT-S-135's disclosed
 * residual: on the tick vanilla's {@code Mob.serverAiStep} only ticks the running goals, {@code customServerAiStep} runs the
 * same full target-selector pass). The Hammerhead's bite rolled two dice (orig Hammerhead.java:213, {@code nextInt(3) == 1 ||
 * nextInt(4) == 1}) and the Irukandji's too (orig Irukandji.java:258, {@code nextInt(4) == 0 || nextInt(5) == 1}), the second
 * drawn only when the first misses; the port had the first alone. The Lizard's filter adopted a Lizard candidate as its buddy on
 * a 1-in-10 while no follow ran (orig Lizard.java:328-330), a side effect of the filter itself; the port's filter had dropped it.
 * The Luna Moth inherited the butterfly's Islands vampire hunt through {@code super.updateAITasks()} (orig EntityLunaMoth.java
 * :122); the port's {@code LunaMothFlightGoal} now extends {@code ButterflyIslandsHuntGoal} (the T3a shape) with its torch
 * retarget kept.
 *
 * <p>A {@link GameTestGenerator} over {@link #rows()} — 21 {@link TestFunction}s, {@code misctargetingparitytests.s141_NN_<row>}
 * — the Ender pair's five rows each (01-10, the Knight's first), the Hammerhead's three (11-13), the Irukandji's three (14-16),
 * the Lizard's three (17-19), the Luna Moth's two (20-21). Tick-driven (the two teleport rows per Ender mob): the mob spawned
 * LIVE with its feet on the floor (rel y 0, {@link #HUNTER_POS}), its movement speed zeroed and its gravity off (so nothing but
 * a teleport moves it) and its {@code Entity.random} pinned ({@link TeleportRolls}: the daylight dice quiet, the teleport offsets fixed) — a survival
 * {@link ServerPlayer} 3 blocks east staring at its mid-height is picked, held and, on the next tick, the mob lands 4 blocks
 * east and 4 south (the pinned random teleport, orig :142-146), the counter 0; a frozen Zombie 17 blocks east primed as the
 * attacker is held by the revenge goal 30 ticks beyond distSq 256 and the mob lands at the transcribed spot 16 blocks along the
 * line toward it (orig :149-156 with the pinned jitter), the counter reset. Synchronous: the pick's side effects through the
 * goal's {@code canUse()} — the scream on, the stare sound at the player's spot heard through the {@code PlayLevelSoundEvent}
 * seam on the first call and the seventh (the :68-73 cadence: the timer 1..5, reset on the sixth), the scream and the timer off
 * when the player looks away; the counter's arithmetic through a direct {@code aiStep()} on a frozen mob — no target clears the
 * scream and a written counter, a non-staring survival target 8 blocks off holds the counter (distSq 64 is inside 256), the same
 * target 17 blocks off counts three ticks, a counter written 30 teleports toward it on the next tick and resets, a staring target
 * 3 blocks off teleports randomly; the cadence — {@code customServerAiStep} driven on the engine's running-only tick parity picks
 * the starer, and on the engine's own pass parity adds nothing. The dice through the {@code ScriptedRolls} seam driving one
 * {@code customServerAiStep}: the pass gate fired, the first die missing and the second firing (the bite lands on a survival
 * player whose spawn shield is cleared, every scripted roll drawn), the first firing with the second left undrawn (the script's
 * remainder is exactly the second die), both missing (no bite, both drawn). The Lizard's filter by reflection with a frozen Lizard
 * candidate: adopted with {@code nextInt(10)} pinned to 1 (the field read back), not adopted on 0 — the roll drawn for a Lizard
 * candidate alone (a Zombie leaves the script untouched) — and not adopted with the follow time above 0 (the roll drawn ahead of
 * the guard, as orig's order). The Luna Moth's goal selector carries exactly one {@code ButterflyIslandsHuntGoal} in the flight
 * slot 8 — its own {@code LunaMothFlightGoal} — and no plain flight goal, and its hunt's scan picks a survival player 5 blocks off
 * as a butterfly's does and refuses the same player in creative. Survival players are plain {@link ServerPlayer}s on the player
 * list ({@link #survivalServerPlayerAt}; the framework mock's {@code isCreative()} is hardcoded true); the spawn shield is
 * cleared only in the six dice rows, whose signal is the bite; frozen mobs are set on the ground; PlayNicely and the difficulty
 * asserted, never flipped; every spawn discarded, every player removed and the sound ear closed in a finally (the tick-driven
 * rows in their delayed step's). Own batch (TEST-003).</p>
 *
 * <p>The floor. The 48x16x48 empty_large is all air (its NBT holds no blocks); the ground under it is the stone the framework
 * lays below the structure block's layer ({@code StructureUtils.clearBlock}: stone under the structure block's y, air from it
 * up), so the floor's top is rel y 0 — the structure block's layer — not the template's own layer 0 at rel y 1. Every spawn of
 * this batch stands with its feet at rel y 0. The teleport rows need it: orig's landing search ({@code teleportTo}, orig
 * :169-186 — the port's {@code LivingEntity.randomTeleport}, the same walk) drops the requested y a whole block at a time,
 * its fraction kept, until the block below blocks motion, then accepts the spot if the mob's box is clear of blocks and liquid
 * — a spot requested a block above the floor lands ON the floor. With the feet at rel y 0 the walk is a no-op (the block below
 * the requested spot is the stone) and the landing is the requested spot exactly; a live mob is given no gravity as well, so
 * the toward landing, a fraction above the floor, is not set down on it by the same tick's physics (the T10 gate's six red rows:
 * spawned at rel y 1, every landing came out one block low, and the live rows' toward vector was taken off a mob that had
 * already fallen).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MiscTargetingParityTests {

    private static final String BATCH = "miscTargetingParity";
    private static final String TEST_PREFIX = "misctargetingparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (PlayNicelyGateParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-141";

    /**
     * The hunter with its feet ON the floor: rel (20.5, 0.0, 24.5) — the IMobConventionTests column, one block lower. The
     * floor's top is rel y 0 (the structure block's layer, over the framework's stone; empty_large itself is all air), so rel y 1
     * floats a block above it — harmless for a frozen mob, wrong for a teleport landing (the class note on the floor).
     */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 0, 24);
    /** 1.2 blocks east — inside the Irukandji's bite reach (orig Irukandji.java:256, distSq &lt; 3). */
    private static final Vec3 PLAYER_1_POS = new Vec3(21.7, 0.0, 24.5);
    /** 3 blocks east — distSq 9, inside the Ender pair's near band (orig :127, distSq &lt; 16). */
    private static final Vec3 PLAYER_3_POS = new Vec3(23.5, 0.0, 24.5);
    /** 5 blocks east — inside the Hammerhead's reach (orig Hammerhead.java:211, 7 + w/2), the Lizard's 12/4/12 and the moth's 8/5/8 box. */
    private static final Vec3 PLAYER_5_POS = new Vec3(25.5, 0.0, 24.5);
    /** 5 blocks west — the Lizard row's Zombie control. */
    private static final Vec3 ZOMBIE_WEST_POS = new Vec3(15.5, 0.0, 24.5);
    /** 8 blocks east — distSq 64: past the near band, inside the far bound (orig :131, distSq &gt; 256). */
    private static final Vec3 PLAYER_8_POS = new Vec3(28.5, 0.0, 24.5);
    /** 17 blocks east — distSq 289, beyond orig :131's 256. */
    private static final Vec3 PLAYER_17_POS = new Vec3(37.5, 0.0, 24.5);
    /** The random teleport's pinned nextDouble: (0.5625 - 0.5) * 64 = 4 blocks on x and on z (orig :143 / :145). */
    private static final double RANDOM_TELEPORT_DOUBLE = 0.5625;
    private static final double RANDOM_TELEPORT_OFFSET = 4.0;
    /** The random teleport's pinned nextInt(64): 32 - 32 = 0 on y (orig :144). */
    private static final int RANDOM_TELEPORT_Y_ROLL = 32;
    /** The toward teleport's pinned nextDouble: (0.5 - 0.5) * 8 = no jitter (orig :153 / :155). */
    private static final double TOWARD_JITTER_DOUBLE = 0.5;
    /** The toward teleport's pinned nextInt(16): 11 - 8 = +3 on y (orig :154), which lands the mob just above the floor after the 16-block y term. */
    private static final int TOWARD_Y_ROLL = 11;
    /** The daylight dice pinned quiet: nextFloat 1.0 → 30 &lt; (f - 0.4) * 2 fails (orig :111). */
    private static final float QUIET_FLOAT = 1.0f;
    /** The far counter's threshold (orig :131). */
    private static final int FAR_TELEPORT_TICKS = 30;
    /** Ticks given to the random-teleport row (the pick within two, the teleport the tick after). */
    private static final int RANDOM_TELEPORT_WAIT = 20;
    /** Ticks given to the toward row: the revenge pick on the first pass, thirty counted ticks, the landing on the next. */
    private static final int TOWARD_TELEPORT_WAIT = 45;
    /** Every landing is asserted at the requested spot, all three axes: the floor walk is a no-op and the live mobs have no gravity. */
    private static final double POSITION_TOLERANCE = 1.0e-3;
    /** A tick count past the goals' initial timestamp 0, so a primed lastHurtByMob is seen (the TargetReleaseParityTests idiom). */
    private static final int PRIMED_TICK_COUNT = 2;
    /** Candidate health, high enough that no pinned bite kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** The stare sound is played at the player's spot (orig :69); the ear listens within this radius of it. */
    private static final double EAR_RADIUS = 2.0;

    // ------------------------------------------------------------------
    // The row table
    // ------------------------------------------------------------------

    private record EnderSite(String key, Supplier<? extends EntityType<? extends Mob>> type, String origFile) {
    }

    private static List<EnderSite> enderSites() {
        return List.of(
                new EnderSite("enderknight", ModEntities.ENDER_KNIGHT, "EnderKnight.java"),
                new EnderSite("enderreaper", ModEntities.ENDER_REAPER, "EnderReaper.java"));
    }

    /** The two dice (orig Hammerhead.java:213 / Irukandji.java:258): which of them fire. */
    private enum DiceCase {
        /** The first misses, the second fires: the bite lands, both drawn. */
        SECOND_ALONE("second_die_alone_bites"),
        /** The first fires: the bite lands, the second never drawn (the short-circuit). */
        FIRST_ALONE("first_die_fires_second_undrawn"),
        /** Both miss: no bite, both drawn. */
        BOTH_MISS("both_dice_miss_no_bite");

        final String tag;

        DiceCase(String tag) {
            this.tag = tag;
        }
    }

    /** orig Lizard.java:328-330 — the buddy adoption's three terms. */
    private enum BuddyCase {
        ADOPTED_ON_1("lizard_candidate_adopted_on_1"),
        NOT_ON_0("not_adopted_on_0_roll_for_lizards_alone"),
        FOLLOW_TIME_GUARD("follow_time_guard_refuses_adoption");

        final String tag;

        BuddyCase(String tag) {
            this.tag = tag;
        }
    }

    private record Row(int index, String name, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + String.format("s141_%02d_%s", this.index, this.name);
        }
    }

    private static void add(List<Row> rows, String name, Consumer<GameTestHelper> body) {
        rows.add(new Row(rows.size() + 1, name, body));
    }

    /** Rows 01-10 the Ender pair (five each), 11-13 the Hammerhead, 14-16 the Irukandji, 17-19 the Lizard, 20-21 the Luna Moth. */
    private static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        for (EnderSite site : enderSites()) {
            add(rows, site.key() + "_126_staring_target_within_4_teleports_randomly", h -> enderStaringTargetTeleportsRandomly(h, site));
            add(rows, site.key() + "_131_far_target_held_30_ticks_teleports_toward_it", h -> enderFarTargetTeleportsToward(h, site));
            add(rows, site.key() + "_74_pick_sets_screaming_and_stare_cadence", h -> enderPickSideEffects(h, site));
            add(rows, site.key() + "_124_far_counter_and_no_target_reset", h -> enderCounterArithmetic(h, site));
            add(rows, site.key() + "_61_pick_every_tick_off_tick_pass", h -> enderPickEveryTick(h, site));
        }
        for (DiceCase diceCase : DiceCase.values()) {
            add(rows, "hammerhead_213_" + diceCase.tag, h -> hammerheadDice(h, diceCase));
        }
        for (DiceCase diceCase : DiceCase.values()) {
            add(rows, "irukandji_258_" + diceCase.tag, h -> irukandjiDice(h, diceCase));
        }
        for (BuddyCase buddyCase : BuddyCase.values()) {
            add(rows, "lizard_328_" + buddyCase.tag, h -> lizardBuddy(h, buddyCase));
        }
        add(rows, "lunamoth_122_hunt_goal_registered_in_flight_slot", MiscTargetingParityTests::lunaMothGoalRegistered);
        add(rows, "lunamoth_122_hunt_picks_as_the_butterfly_does", MiscTargetingParityTests::lunaMothHuntPicks);
        return rows;
    }

    /** One test per row: 21 TestFunctions in the {@code miscTargetingParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> miscTargetingRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true, row.body()));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // The Ender pair — orig EnderKnight.java / EnderReaper.java:124-138 (the teleports), :68-78 (the pick's side effects)
    // ------------------------------------------------------------------

    /**
     * Tick-driven: a live mob (its feet on the floor, speed zeroed, gravity off, the random pinned) and a survival player 3 blocks
     * east staring at its mid-height. The goal pass picks and holds the starer; the next aiStep sees a staring player target inside
     * distSq 16 (orig :126-127) and teleports randomly (:128 — the pinned offsets: 4 east, 4 south, level — the landing search's
     * walk a no-op with the requested spot on the floor, so the mob lands exactly there) with the far counter reset (:130); the
     * stale stare no longer lines up after the move, so the far branch runs quiet (distSq 17 is inside 256) and nothing teleports again.
     */
    private static void enderStaringTargetTeleportsRandomly(GameTestHelper helper, EnderSite site) {
        assertPlayNicelyOff(helper);
        assertNormalDifficulty(helper);
        Mob spawnedMob = null;
        ServerPlayer spawnedPlayer = null;
        try {
            spawnedMob = spawnLive(helper, site.type().get(), HUNTER_POS);
            String name = spawnedMob.getClass().getSimpleName();
            spawnedPlayer = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_3_POS));
            stareAtMid(spawnedPlayer, spawnedMob);
            helper.assertTrue(spawnedMob.hasLineOfSight(spawnedPlayer), "precondition: the " + name + " sees the starer 3 blocks east ("
                    + FINDING + " test geometry)");
            helper.assertTrue(shouldAttackPlayer(spawnedMob, spawnedPlayer), "precondition: the survival player 3 blocks east stares at the "
                    + name + "'s mid-height with a clear ray (orig " + site.origFile() + ":83-93) (" + FINDING + " test setup)");
            helper.assertTrue(spawnedMob.distanceToSqr(spawnedPlayer) < 16.0, "precondition: the starer stands inside distSq 16 (orig "
                    + site.origFile() + ":127) (" + FINDING + " test geometry)");
            helper.assertTrue(spawnedMob.getTarget() == null && teleportDelay(spawnedMob) == 0, "precondition: no target, the counter 0 ("
                    + FINDING + " test setup)");
            replaceRandom(spawnedMob, new TeleportRolls(RandomSource.create(1234L), RANDOM_TELEPORT_DOUBLE, QUIET_FLOAT,
                    64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
        } catch (Throwable e) {
            removePlayer(helper, spawnedPlayer);
            discardQuietly(spawnedMob);
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed", e);
        }
        final Mob mob = spawnedMob;
        final ServerPlayer player = spawnedPlayer;
        final Vec3 start = mob.position();
        final String name = mob.getClass().getSimpleName();
        helper.runAfterDelay(RANDOM_TELEPORT_WAIT, () -> {
            try {
                helper.assertTrue(mob.getTarget() == player, name + ": the staring survival player is the pick and is held (orig "
                        + site.origFile() + ":65-75 with td.bq) — got " + describe(mob.getTarget()) + " (" + FINDING + ")");
                Vec3 expected = start.add(RANDOM_TELEPORT_OFFSET, 0.0, RANDOM_TELEPORT_OFFSET);
                helper.assertTrue(mob.position().distanceTo(expected) < POSITION_TOLERANCE, name + " (orig " + site.origFile() + ":126-128): a staring player target"
                        + " inside distSq 16 must send the mob through teleportRandomly — with the random pinned, 4 blocks east and 4 south of "
                        + start + " (level: the landing search finds the floor right under the requested spot) — once (the stale stare no longer"
                        + " lines up, so the far branch stays quiet); at " + mob.position() + " (" + FINDING + ")");
                helper.assertTrue(teleportDelay(mob) == 0, name + " (orig " + site.origFile() + ":130): the far counter is reset on the staring"
                        + " branch (" + FINDING + "); read " + teleportDelay(mob));
                helper.assertTrue(isScreaming(mob), name + " (orig " + site.origFile() + ":74): the pick set the scream on and a held target"
                        + " keeps it (" + FINDING + ")");
            } finally {
                removePlayer(helper, player);
                discardQuietly(mob);
            }
            helper.succeed();
        });
    }

    /**
     * Tick-driven: a live mob (its feet on the floor, speed zeroed, gravity off, the random pinned) and a frozen Zombie 17 blocks
     * east on the same floor primed as its attacker; the revenge goal picks it on the first pass and holds it (holdsLegacyTarget);
     * every aiStep sees a non-player target beyond distSq 256 (orig :131) and counts, and the tick the counter passes 30 the mob
     * teleports toward it (:131-132 — the landing at the transcribed spot 16 blocks along the line, no jitter, +3 on y before the
     * y term: a fraction above the floor, where the landing search's walk is a no-op and, without gravity, the mob stays) with
     * the counter reset. The expected spot is computed off the spawn spot, which the mob still occupies when the counter fires
     * (nothing moves it before).
     */
    private static void enderFarTargetTeleportsToward(GameTestHelper helper, EnderSite site) {
        assertPlayNicelyOff(helper);
        assertNormalDifficulty(helper);
        Mob spawnedMob = null;
        Mob spawnedZombie = null;
        Vec3 landing = null;
        try {
            spawnedMob = spawnLive(helper, site.type().get(), HUNTER_POS);
            String name = spawnedMob.getClass().getSimpleName();
            spawnedZombie = spawnPrey(helper, EntityType.ZOMBIE, PLAYER_17_POS);
            helper.assertTrue(spawnedMob.distanceToSqr(spawnedZombie) > 256.0, "precondition: the Zombie stands beyond distSq 256 (orig "
                    + site.origFile() + ":131) (" + FINDING + " test geometry)");
            spawnedMob.tickCount = PRIMED_TICK_COUNT;
            spawnedMob.setLastHurtByMob(spawnedZombie);
            helper.assertTrue(spawnedMob.getLastHurtByMob() == spawnedZombie && spawnedMob.getTarget() == null && teleportDelay(spawnedMob) == 0,
                    "precondition: the Zombie is the primed attacker, nothing held yet, the counter 0 (" + FINDING + " test setup)");
            landing = towardLanding(spawnedMob, spawnedZombie, TOWARD_JITTER_DOUBLE, TOWARD_Y_ROLL);
            helper.assertTrue(landing.y > spawnedMob.getY() && landing.y < spawnedMob.getY() + 1.0, "precondition: the pinned landing sits just"
                    + " above the floor (orig " + site.origFile() + ":154 with nextInt(16) = " + TOWARD_Y_ROLL + "), so the landing search"
                    + " accepts it (" + FINDING + " test geometry); " + landing + " from " + spawnedMob.position());
            helper.assertTrue(Math.abs(landing.x - spawnedMob.getX()) > 10.0, "precondition: the landing is well toward the Zombie ("
                    + FINDING + " test geometry); " + landing);
            replaceRandom(spawnedMob, new TeleportRolls(RandomSource.create(1234L), TOWARD_JITTER_DOUBLE, QUIET_FLOAT,
                    64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
        } catch (Throwable e) {
            discardQuietly(spawnedZombie);
            discardQuietly(spawnedMob);
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed", e);
        }
        final Mob mob = spawnedMob;
        final Mob zombie = spawnedZombie;
        final Vec3 expected = landing;
        final String name = mob.getClass().getSimpleName();
        helper.runAfterDelay(TOWARD_TELEPORT_WAIT, () -> {
            try {
                helper.assertTrue(mob.getTarget() == zombie, name + ": the primed attacker is held by the revenge goal (orig "
                        + site.origFile() + " with td.bq, ENT-S-129's hold) — got " + describe(mob.getTarget()) + " (" + FINDING + ")");
                helper.assertTrue(mob.position().distanceTo(expected) < POSITION_TOLERANCE, name + " (orig " + site.origFile()
                        + ":131-132, :149-156): a target beyond distSq 256 held " + FAR_TELEPORT_TICKS + " ticks must be teleported toward —"
                        + " the spot 16 blocks along the line from the target to the mob's mid-height, the pinned jitter 0 and +3 on y — expected "
                        + expected + ", at " + mob.position() + " (" + FINDING + ")");
                helper.assertTrue(teleportDelay(mob) == 0, name + " (orig " + site.origFile() + ":132): the counter is reset by the landing ("
                        + FINDING + "); read " + teleportDelay(mob));
            } finally {
                discardQuietly(zombie);
                discardQuietly(mob);
            }
            helper.succeed();
        });
    }

    /**
     * The pick's side effects (orig :68-78) through the goal's {@code canUse()} on a frozen mob: a survival starer 8 blocks east
     * is picked with the scream on (:74) and the stare sound at the player's spot (:69) on the first call, the timer counting
     * 1..5 with no sound, reset on the sixth (:71-73), the sound again on the seventh; the player looking away clears the timer
     * and the scream (:77-78) and nothing is picked.
     */
    private static void enderPickSideEffects(GameTestHelper helper, EnderSite site) {
        assertPlayNicelyOff(helper);
        Mob mob = null;
        ServerPlayer player = null;
        StareEar ear = null;
        try {
            mob = spawnWithGoals(helper, site.type().get(), HUNTER_POS);
            String name = mob.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = playerGoal(helper, mob);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            stareAtMid(player, mob);
            helper.assertTrue(mob.hasLineOfSight(player), "precondition: the " + name + " sees the starer 8 blocks east (" + FINDING + " test geometry)");
            helper.assertTrue(conditionsOf(goal).test(mob, player), "precondition: the pick's conditions (the stare selector, ENT-S-132) admit the starer ("
                    + FINDING + " test setup)");
            helper.assertTrue(stareTimer(mob) == 0 && !isScreaming(mob), "precondition: a fresh mob — the stare timer 0, not screaming (" + FINDING + " test setup)");
            ear = new StareEar(player);
            boolean can = goal.canUse();
            LivingEntity pick = goalTarget(goal);
            helper.assertTrue(can && pick == player, "precondition: the starer is the pick (orig " + site.origFile() + ":65-75) (" + FINDING
                    + " test setup); canUse=" + can + ", pick " + describe(pick));
            helper.assertTrue(isScreaming(mob), name + " (orig " + site.origFile() + ":74): the pick sets the scream on (" + FINDING + ")");
            helper.assertTrue(stareTimer(mob) == 1, name + " (orig " + site.origFile() + ":71): the stare timer counts the pick — 1 after the first ("
                    + FINDING + "); read " + stareTimer(mob));
            helper.assertTrue(ear.count == 1, name + " (orig " + site.origFile() + ":68-70): the stare sound is played at the player's spot on the"
                    + " first tick of a held stare (the timer at 0) (" + FINDING + "); heard " + ear.count);
            for (int k = 2; k <= 5; k++) {
                goal.canUse();
                helper.assertTrue(stareTimer(mob) == k, name + " (orig " + site.origFile() + ":71): the timer reads " + k + " after " + k
                        + " picks (" + FINDING + "); read " + stareTimer(mob));
                helper.assertTrue(ear.count == 1, name + " (orig " + site.origFile() + ":68): no stare sound while the timer runs (" + FINDING
                        + "); heard " + ear.count + " after " + k + " picks");
            }
            goal.canUse();
            helper.assertTrue(stareTimer(mob) == 0, name + " (orig " + site.origFile() + ":71-73): the sixth pick reads the timer at 5 and resets it ("
                    + FINDING + "); read " + stareTimer(mob));
            helper.assertTrue(ear.count == 1, name + " (orig " + site.origFile() + ":68): the reset itself plays nothing (" + FINDING + "); heard " + ear.count);
            goal.canUse();
            helper.assertTrue(stareTimer(mob) == 1 && ear.count == 2, name + " (orig " + site.origFile() + ":68-71): the seventh pick finds the timer at 0"
                    + " — the sound again, once at the pick, then every sixth tick while the pick is re-asked on target-less ticks (" + FINDING + "); timer " + stareTimer(mob) + ", heard " + ear.count);
            helper.assertTrue(isScreaming(mob), name + " (orig " + site.origFile() + ":74): still screaming while the stare holds (" + FINDING + ")");
            lookEast(player);
            helper.assertTrue(!shouldAttackPlayer(mob, player), "precondition: looking away fails the stare test (orig " + site.origFile()
                    + ":88-91) (" + FINDING + " test setup)");
            can = goal.canUse();
            pick = goalTarget(goal);
            helper.assertTrue(!can && pick == null, name + " (orig " + site.origFile() + ":76-80): the nearest player looks away — nothing is picked ("
                    + FINDING + "); canUse=" + can + ", pick " + describe(pick));
            helper.assertTrue(!isScreaming(mob) && stareTimer(mob) == 0, name + " (orig " + site.origFile() + ":77-78): a nearest player who does"
                    + " not stare resets the timer and the scream (" + FINDING + "); screaming=" + isScreaming(mob) + ", timer " + stareTimer(mob));
            helper.assertTrue(ear.count == 2, name + " (orig " + site.origFile() + ":68): no sound without a stare (" + FINDING + "); heard " + ear.count);
        } finally {
            if (ear != null) ear.close();
            removePlayer(helper, player);
            discardQuietly(mob);
        }
        helper.succeed();
    }

    /**
     * The counter's arithmetic (orig :124-138) through a direct {@code aiStep()} on a frozen mob with the random pinned per step
     * (both teleports draw {@code nextDouble()} for their x / z terms, so the toward jitter is re-pinned at 0.5 → 0 ahead of the
     * toward tick and the random teleport's 0.5625 → +4 / +4 ahead of the staring tick): no target clears a written counter and
     * the scream (:134-137); a non-staring survival target 8 blocks off holds the counter (distSq 64 is inside 256 — :131's first
     * term short-circuits the count); the same target 17 blocks off counts three ticks with no teleport (:131, 30 not reached); a
     * counter written 30 teleports toward it on the next tick and resets (:131-132); a staring target 3 blocks off teleports
     * randomly with the counter 0 (:126-130). Both landings are asserted at the requested spot: the mob's feet start on the floor
     * and each requested y sits in the block right above the stone, so the landing search's walk is a no-op (a noAi mob has no
     * physics to set it down after).
     */
    private static void enderCounterArithmetic(GameTestHelper helper, EnderSite site) {
        assertPlayNicelyOff(helper);
        Mob mob = null;
        ServerPlayer player = null;
        try {
            mob = spawnWithGoals(helper, site.type().get(), HUNTER_POS);
            String name = mob.getClass().getSimpleName();
            replaceRandom(mob, new TeleportRolls(RandomSource.create(1234L), RANDOM_TELEPORT_DOUBLE, QUIET_FLOAT,
                    64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
            Vec3 start = mob.position();
            // no target: the scream and a written counter are cleared (orig :134-137)
            setScreaming(mob, true);
            writeInt(mob, "teleportDelay", 7);
            mob.aiStep();
            helper.assertTrue(!isScreaming(mob) && teleportDelay(mob) == 0, name + " (orig " + site.origFile() + ":134-137): with no target the"
                    + " tick clears the scream and the far counter (" + FINDING + "); screaming=" + isScreaming(mob) + ", counter " + teleportDelay(mob));
            helper.assertTrue(mob.position().equals(start), name + ": no target, no teleport (" + FINDING + "); at " + mob.position());
            // a non-staring survival target 8 blocks off: the counter holds (orig :131 — distSq 64 is not beyond 256)
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            lookEast(player);
            helper.assertTrue(!shouldAttackPlayer(mob, player), "precondition: the player looks away (orig " + site.origFile() + ":126 fails) ("
                    + FINDING + " test setup)");
            mob.setTarget(player);
            setScreaming(mob, true);
            writeInt(mob, "teleportDelay", 7);
            mob.aiStep();
            helper.assertTrue(teleportDelay(mob) == 7 && isScreaming(mob) && mob.position().equals(start), name + " (orig " + site.origFile()
                    + ":131): a non-staring target inside distSq 256 neither counts nor teleports, and the scream stands (" + FINDING + "); counter "
                    + teleportDelay(mob) + ", screaming=" + isScreaming(mob) + ", at " + mob.position());
            // the same target 17 blocks off: three ticks count 7 → 10, no teleport (orig :131 — the count, 30 not reached)
            Vec3 far = helper.absoluteVec(PLAYER_17_POS);
            player.teleportTo(helper.getLevel(), far.x, far.y, far.z, 0.0f, 0.0f);
            lookEast(player);
            helper.assertTrue(mob.distanceToSqr(player) > 256.0, "precondition: the target stands beyond distSq 256 (orig " + site.origFile()
                    + ":131) (" + FINDING + " test geometry)");
            for (int i = 0; i < 3; i++) mob.aiStep();
            helper.assertTrue(teleportDelay(mob) == 10 && mob.position().equals(start), name + " (orig " + site.origFile() + ":131): a target beyond"
                    + " distSq 256 counts one per tick (7 → 10 over three) with no teleport short of 30 (" + FINDING + "); counter "
                    + teleportDelay(mob) + ", at " + mob.position());
            // the counter at 30: the next tick teleports toward the target and resets (orig :131-132, :149-156)
            writeInt(mob, "teleportDelay", FAR_TELEPORT_TICKS);
            // teleportToEntity draws nextDouble() for its x / z jitter (orig :153 / :155): re-pin it at 0.5 → 0, the helper's input
            replaceRandom(mob, new TeleportRolls(RandomSource.create(1234L), TOWARD_JITTER_DOUBLE, QUIET_FLOAT,
                    64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
            Vec3 landing = towardLanding(mob, player, TOWARD_JITTER_DOUBLE, TOWARD_Y_ROLL);
            helper.assertTrue(landing.y > start.y && landing.y < start.y + 1.0, "precondition: the pinned landing sits just above the floor ("
                    + FINDING + " test geometry); " + landing);
            mob.aiStep();
            helper.assertTrue(mob.position().distanceTo(landing) < POSITION_TOLERANCE, name + " (orig " + site.origFile() + ":131-132, :149-156): the"
                    + " tick the counter reads 30 teleports toward the target — 16 blocks along the line from the target to the mob's mid-height, the"
                    + " pinned jitter 0 and +3 on y — expected " + landing + ", at " + mob.position() + " (" + FINDING + ")");
            helper.assertTrue(teleportDelay(mob) == 0, name + " (orig " + site.origFile() + ":132): the landing resets the counter (" + FINDING
                    + "); counter " + teleportDelay(mob));
            // a staring target 3 blocks off: the random teleport, the counter 0 (orig :126-130)
            Vec3 near = mob.position().add(3.0, 0.0, 0.0);
            player.teleportTo(helper.getLevel(), near.x, near.y, near.z, 0.0f, 0.0f);
            stareAtMid(player, mob);
            helper.assertTrue(shouldAttackPlayer(mob, player) && mob.distanceToSqr(player) < 16.0, "precondition: the player 3 blocks east stares"
                    + " (orig " + site.origFile() + ":126-127) (" + FINDING + " test setup)");
            // teleportRandomly draws nextDouble() for its x / z offsets (orig :143 / :145): re-pin it at 0.5625 → +4 / +4
            replaceRandom(mob, new TeleportRolls(RandomSource.create(1234L), RANDOM_TELEPORT_DOUBLE, QUIET_FLOAT,
                    64, RANDOM_TELEPORT_Y_ROLL, 16, TOWARD_Y_ROLL));
            writeInt(mob, "teleportDelay", 7);
            Vec3 before = mob.position();
            mob.aiStep();
            Vec3 expected = before.add(RANDOM_TELEPORT_OFFSET, 0.0, RANDOM_TELEPORT_OFFSET);
            helper.assertTrue(mob.position().distanceTo(expected) < POSITION_TOLERANCE, name + " (orig " + site.origFile() + ":126-128): a staring"
                    + " player target inside distSq 16 teleports the mob randomly — the pinned 4 east, 4 south of " + before + " — at " + mob.position()
                    + " (" + FINDING + ")");
            helper.assertTrue(teleportDelay(mob) == 0, name + " (orig " + site.origFile() + ":130): the staring branch resets the counter ("
                    + FINDING + "); counter " + teleportDelay(mob));
        } finally {
            removePlayer(helper, player);
            discardQuietly(mob);
        }
        helper.succeed();
    }

    /**
     * The cadence: {@code customServerAiStep} on the engine's own pass parity ({@code (tickCount + id) % 2 == 0}) adds nothing —
     * that tick's pass is the engine's, not driven here — and on the running-only parity runs the full target-selector pass, so
     * the starer 8 blocks east is picked and the goal started (orig :61-81 with td.bq: the pick every server tick).
     */
    private static void enderPickEveryTick(GameTestHelper helper, EnderSite site) {
        assertPlayNicelyOff(helper);
        Mob mob = null;
        ServerPlayer player = null;
        try {
            mob = spawnWithGoals(helper, site.type().get(), HUNTER_POS);
            String name = mob.getClass().getSimpleName();
            NearestAttackableTargetGoal<?> goal = playerGoal(helper, mob);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_8_POS));
            stareAtMid(player, mob);
            helper.assertTrue(mob.hasLineOfSight(player) && conditionsOf(goal).test(mob, player), "precondition: the starer 8 blocks east is"
                    + " admitted by the pick's conditions (" + FINDING + " test setup)");
            int enginePass = 10;
            if ((enginePass + mob.getId()) % 2 != 0) enginePass++;
            mob.tickCount = enginePass;
            invokeCustomServerAiStep(mob);
            helper.assertTrue(mob.getTarget() == null && !isRunning(mob, goal), name + ": on the engine's own pass parity (tickCount + id even)"
                    + " customServerAiStep adds no pass — the tick's pass is Mob.serverAiStep's (" + FINDING + "); target " + describe(mob.getTarget()));
            mob.tickCount = enginePass + 1;
            invokeCustomServerAiStep(mob);
            helper.assertTrue(mob.getTarget() == player && isRunning(mob, goal), name + " (orig " + site.origFile() + ":61-81 with td.bq — the pick on"
                    + " EVERY server tick): on the tick vanilla only ticks the running goals (tickCount + id odd) customServerAiStep runs the full"
                    + " target-selector pass, so the starer is picked and the goal started (" + FINDING + "); target " + describe(mob.getTarget()));
        } finally {
            removePlayer(helper, player);
            discardQuietly(mob);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The dice — orig Hammerhead.java:213, Irukandji.java:258
    // ------------------------------------------------------------------

    /**
     * orig Hammerhead.java:191 the pass gate ({@code nextInt(3) == 1}), :213 the dice ({@code nextInt(3) == 1 || nextInt(4) == 1}):
     * scripted in draw order — the gate, the first die, the second — the bite on a survival player 5 blocks east (its spawn
     * shield cleared) is the signal, the script's remainder the draw log.
     */
    private static void hammerheadDice(GameTestHelper helper, DiceCase diceCase) {
        assertPlayNicelyOff(helper);
        assertNormalDifficulty(helper);
        Mob shark = null;
        ServerPlayer player = null;
        try {
            shark = spawnWithGoals(helper, ModEntities.HAMMERHEAD.get(), HUNTER_POS);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_5_POS));
            clearSpawnInvulnerability(player);
            double reach = 7.0 + player.getBbWidth() / 2.0;
            helper.assertTrue(shark.distanceToSqr(player) < reach * reach, "precondition: the player stands inside the bite reach (orig"
                    + " Hammerhead.java:211, 7 + w/2) (" + FINDING + " test geometry)");
            helper.assertTrue(shark.hasLineOfSight(player) && !player.getAbilities().instabuild, "precondition: the survival player is seen and"
                    + " not creative — the scan's pick (orig Hammerhead.java:225-249) (" + FINDING + " test setup)");
            helper.assertTrue(player.getHealth() == PREY_HEALTH, "precondition: full health before the pass (" + FINDING + " test setup)");
            CreativeGateParityTests.ScriptedRolls script = switch (diceCase) {
                case SECOND_ALONE -> scripted(3, 1, 3, 0, 4, 1);
                case FIRST_ALONE -> scripted(3, 1, 3, 1, 4, 1);
                case BOTH_MISS -> scripted(3, 1, 3, 0, 4, 0);
            };
            replaceRandom(shark, script);
            invokeAiStep(Hammerhead.class, shark);
            helper.assertTrue(((Hammerhead) shark).getAttacking() == 1, "precondition: the pass fired (the gate scripted to 1) and reached the"
                    + " melee branch (orig Hammerhead.java:211-212) (" + FINDING + " test setup)");
            assertDice(helper, "Hammerhead (orig Hammerhead.java:213 — nextInt(3) == 1 || nextInt(4) == 1)", diceCase, player, script, "[4->1]");
        } finally {
            removePlayer(helper, player);
            discardQuietly(shark);
        }
        helper.succeed();
    }

    /**
     * orig Irukandji.java:227 the water scan ({@code nextInt(10) == 0}, scripted to miss), :253 the pass gate ({@code nextInt(8) == 1}),
     * :258 the dice ({@code nextInt(4) == 0 || nextInt(5) == 1}): the bite on a survival player 1.2 blocks east (inside distSq 3,
     * :256; its spawn shield cleared) is the signal, the script's remainder the draw log.
     */
    private static void irukandjiDice(GameTestHelper helper, DiceCase diceCase) {
        assertPlayNicelyOff(helper);
        assertNormalDifficulty(helper);
        Mob jelly = null;
        ServerPlayer player = null;
        try {
            jelly = spawnWithGoals(helper, ModEntities.IRUKANDJI.get(), HUNTER_POS);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_1_POS));
            clearSpawnInvulnerability(player);
            helper.assertTrue(jelly.distanceToSqr(player) < 3.0, "precondition: the player stands inside the bite reach (orig Irukandji.java:256,"
                    + " distSq < 3) (" + FINDING + " test geometry)");
            helper.assertTrue(jelly.hasLineOfSight(player) && !player.getAbilities().instabuild && !jelly.isInWater(), "precondition: the survival"
                    + " player is seen and not creative — the scan's pick (orig Irukandji.java:270-288) — and the jelly is dry (" + FINDING + " test setup)");
            helper.assertTrue(player.getHealth() == PREY_HEALTH, "precondition: full health before the pass (" + FINDING + " test setup)");
            CreativeGateParityTests.ScriptedRolls script = switch (diceCase) {
                case SECOND_ALONE -> scripted(10, 1, 8, 1, 4, 1, 5, 1);
                case FIRST_ALONE -> scripted(10, 1, 8, 1, 4, 0, 5, 1);
                case BOTH_MISS -> scripted(10, 1, 8, 1, 4, 1, 5, 0);
            };
            replaceRandom(jelly, script);
            invokeAiStep(Irukandji.class, jelly);
            helper.assertTrue(((Irukandji) jelly).getAttacking() == 1, "precondition: the pass fired (the gate scripted to 1) and reached the"
                    + " melee branch (orig Irukandji.java:256-257) (" + FINDING + " test setup)");
            assertDice(helper, "Irukandji (orig Irukandji.java:258 — nextInt(4) == 0 || nextInt(5) == 1)", diceCase, player, script, "[5->1]");
        } finally {
            removePlayer(helper, player);
            discardQuietly(jelly);
        }
        helper.succeed();
    }

    /** The dice row's verdict: the bite and the draw log for the case. */
    private static void assertDice(GameTestHelper helper, String site, DiceCase diceCase, ServerPlayer player,
            CreativeGateParityTests.ScriptedRolls script, String secondDieEntry) {
        boolean bit = player.getHealth() < PREY_HEALTH;
        switch (diceCase) {
            case SECOND_ALONE -> {
                helper.assertTrue(bit, site + ": the first die missing and the second firing must land the bite — the port rolled the first"
                        + " alone (" + FINDING + "); health " + player.getHealth());
                helper.assertTrue(script.remaining() == 0, site + ": both dice must have been drawn — undrawn: " + script.describeRemaining()
                        + " (" + FINDING + ")");
            }
            case FIRST_ALONE -> {
                helper.assertTrue(bit, site + ": the first die firing lands the bite (" + FINDING + "); health " + player.getHealth());
                helper.assertTrue(script.remaining() == 1 && script.describeRemaining().equals(secondDieEntry), site + ": the second die is"
                        + " drawn only when the first misses — the script's remainder must be exactly the second die's entry " + secondDieEntry
                        + " (" + FINDING + "); undrawn: " + script.describeRemaining());
            }
            case BOTH_MISS -> {
                helper.assertTrue(!bit, site + ": both dice missing lands no bite (" + FINDING + "); health " + player.getHealth());
                helper.assertTrue(script.remaining() == 0, site + ": the second die is drawn when the first misses — undrawn: "
                        + script.describeRemaining() + " (" + FINDING + ")");
            }
        }
    }

    // ------------------------------------------------------------------
    // The Lizard — orig Lizard.java:328-330, the buddy adoption inside the filter
    // ------------------------------------------------------------------

    /**
     * The filter by reflection with a frozen Lizard 5 blocks east: never prey (false), but on {@code nextInt(10)} == 1 with no
     * follow running it becomes the buddy (the field read back); on 0 it does not, and the roll is drawn for a Lizard candidate
     * alone (a Zombie leaves the script untouched); with the follow time above 0 the roll is drawn and the adoption refused.
     */
    private static void lizardBuddy(GameTestHelper helper, BuddyCase buddyCase) {
        assertNormalDifficulty(helper);
        Mob lizard = null;
        Mob other = null;
        Mob zombie = null;
        try {
            lizard = spawnWithGoals(helper, ModEntities.LIZARD.get(), HUNTER_POS);
            other = spawnPrey(helper, ModEntities.LIZARD.get(), PLAYER_5_POS);
            helper.assertTrue(lizard.hasLineOfSight(other), "precondition: the other Lizard 5 blocks east is seen — the sight step (orig"
                    + " Lizard.java:313) sits ahead of :328 (" + FINDING + " test geometry)");
            helper.assertTrue(readObject(lizard, "buddy") == null && (Integer) readObject(lizard, "followTime") == 0, "precondition: no buddy,"
                    + " no follow running (" + FINDING + " test setup)");
            CreativeGateParityTests.ScriptedRolls script;
            switch (buddyCase) {
                case ADOPTED_ON_1 -> {
                    script = scripted(10, 1);
                    replaceRandom(lizard, script);
                    boolean prey = filter(lizard, Lizard.class, other);
                    helper.assertTrue(!prey, "Lizard (orig Lizard.java:328-331): a Lizard candidate is never prey (" + FINDING + ")");
                    helper.assertTrue(readObject(lizard, "buddy") == other, "Lizard (orig Lizard.java:328-330): on nextInt(10) == 1 with no follow"
                            + " running the filter adopts the Lizard candidate as the buddy — the port's filter had dropped the side effect ("
                            + FINDING + "); buddy " + describe((LivingEntity) readObject(lizard, "buddy")));
                    helper.assertTrue(script.remaining() == 0, "Lizard (orig Lizard.java:328): the 1-in-10 is drawn for the Lizard candidate ("
                            + FINDING + "); undrawn: " + script.describeRemaining());
                }
                case NOT_ON_0 -> {
                    script = scripted(10, 0);
                    replaceRandom(lizard, script);
                    zombie = spawnPrey(helper, EntityType.ZOMBIE, ZOMBIE_WEST_POS);
                    helper.assertTrue(lizard.hasLineOfSight(zombie), "precondition: the Zombie 5 blocks west is seen, so the filter reaches the"
                            + " ladder (" + FINDING + " test geometry)");
                    boolean zombiePrey = filter(lizard, Lizard.class, zombie);
                    helper.assertTrue(!zombiePrey && script.remaining() == 1, "Lizard (orig Lizard.java:328): a Zombie is not prey and draws no"
                            + " 1-in-10 — the roll follows the instanceof Lizard term (" + FINDING + "); prey=" + zombiePrey + ", undrawn: "
                            + script.describeRemaining());
                    boolean prey = filter(lizard, Lizard.class, other);
                    helper.assertTrue(!prey && readObject(lizard, "buddy") == null, "Lizard (orig Lizard.java:328-330): on nextInt(10) == 0 the"
                            + " Lizard candidate is neither prey nor adopted (" + FINDING + "); prey=" + prey + ", buddy "
                            + describe((LivingEntity) readObject(lizard, "buddy")));
                    helper.assertTrue(script.remaining() == 0, "Lizard (orig Lizard.java:328): the 1-in-10 is drawn for the Lizard candidate ("
                            + FINDING + "); undrawn: " + script.describeRemaining());
                }
                case FOLLOW_TIME_GUARD -> {
                    writeInt(lizard, "followTime", 50);
                    script = scripted(10, 1);
                    replaceRandom(lizard, script);
                    boolean prey = filter(lizard, Lizard.class, other);
                    helper.assertTrue(!prey && readObject(lizard, "buddy") == null, "Lizard (orig Lizard.java:328-330): with follow_time above 0"
                            + " the adoption is refused even on nextInt(10) == 1 (" + FINDING + "); prey=" + prey + ", buddy "
                            + describe((LivingEntity) readObject(lizard, "buddy")));
                    helper.assertTrue(script.remaining() == 0, "Lizard (orig Lizard.java:328): the roll is drawn ahead of the follow_time term —"
                            + " orig's order (instanceof, the roll, the guard) (" + FINDING + "); undrawn: " + script.describeRemaining());
                }
            }
        } finally {
            discardQuietly(zombie);
            discardQuietly(other);
            discardQuietly(lizard);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The Luna Moth — orig EntityLunaMoth.java:117-122 (super.updateAITasks(): the inherited Islands hunt)
    // ------------------------------------------------------------------

    /** The moth's flight goal in slot 8 is a ButterflyIslandsHuntGoal — its own LunaMothFlightGoal — and no plain flight goal remains. */
    private static void lunaMothGoalRegistered(GameTestHelper helper) {
        Mob moth = null;
        try {
            moth = spawnWithGoals(helper, ModEntities.ENTITY_LUNA_MOTH.get(), HUNTER_POS);
            int hunt = 0;
            int plain = 0;
            boolean torch = false;
            for (WrappedGoal wrapped : moth.goalSelector.getAvailableGoals()) {
                if (wrapped.getGoal() instanceof ButterflyIslandsHuntGoal) {
                    hunt++;
                    torch |= wrapped.getGoal() instanceof LunaMothFlightGoal;
                    helper.assertTrue(wrapped.getPriority() == 8, "the moth's hunt goal sits in the flight goal's slot 8 (" + FINDING + "); got "
                            + wrapped.getPriority());
                } else if (wrapped.getGoal() instanceof AmbientFlightGoal) {
                    plain++;
                }
            }
            helper.assertTrue(hunt == 1 && plain == 0, "EntityLunaMoth.registerGoals (orig EntityLunaMoth.java:117-122 — super.updateAITasks() ran"
                    + " the butterfly's Islands hunt, orig EntityButterfly.java:161-169, beside the moth's own loop): the moth's flight must be a"
                    + " ButterflyIslandsHuntGoal and no plain AmbientFlightGoal may remain (" + FINDING + "); hunt goals=" + hunt + ", plain=" + plain);
            helper.assertTrue(torch, "the moth's hunt goal is its own LunaMothFlightGoal — the torch-seeking retarget kept over the hunt (" + FINDING + ")");
        } finally {
            discardQuietly(moth);
        }
        helper.succeed();
    }

    /** The moth's hunt scan (the goal's private findSomethingToAttack) picks a survival player 5 blocks off as a butterfly's does, and refuses him in creative. */
    private static void lunaMothHuntPicks(GameTestHelper helper) {
        assertNormalDifficulty(helper);
        Mob moth = null;
        Mob butterfly = null;
        ServerPlayer player = null;
        try {
            moth = spawnWithGoals(helper, ModEntities.ENTITY_LUNA_MOTH.get(), HUNTER_POS);
            butterfly = spawnWithGoals(helper, ModEntities.ENTITY_BUTTERFLY.get(), HUNTER_POS);
            Goal mothGoal = huntGoal(moth);
            Goal butterflyGoal = huntGoal(butterfly);
            player = survivalServerPlayerAt(helper, helper.absoluteVec(PLAYER_5_POS));
            helper.assertTrue(moth.hasLineOfSight(player) && butterfly.hasLineOfSight(player), "precondition: both see the survival player 5 blocks"
                    + " east, inside the 8/5/8 box (orig EntityButterfly.java:218) (" + FINDING + " test geometry)");
            LivingEntity butterflyPick = scan(butterflyGoal);
            helper.assertTrue(butterflyPick == player, "precondition: the butterfly's scan takes the survival player (orig EntityButterfly.java:210-213)"
                    + " (" + FINDING + " test setup); got " + describe(butterflyPick));
            LivingEntity mothPick = scan(mothGoal);
            helper.assertTrue(mothPick == player, "EntityLunaMoth (orig EntityLunaMoth.java:122 — the hunt inherited through super.updateAITasks()):"
                    + " the moth's hunt scan takes the survival player as the butterfly's does (" + FINDING + "); got " + describe(mothPick));
            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(player.getAbilities().instabuild, "precondition: creative sets instabuild (" + FINDING + " test setup)");
            helper.assertTrue(scan(butterflyGoal) == null && scan(mothGoal) == null, "EntityLunaMoth: the same player in creative is refused by"
                    + " both scans (orig EntityButterfly.java:210-213, isCreativeMode → instabuild) (" + FINDING + ")");
            helper.assertTrue(!filter(mothGoal, ButterflyIslandsHuntGoal.class, player), "EntityLunaMoth: the moth goal's filter refuses the"
                    + " creative player (orig EntityButterfly.java:210-213) (" + FINDING + ")");
            player.setGameMode(GameType.SURVIVAL);
            helper.assertTrue(filter(mothGoal, ButterflyIslandsHuntGoal.class, player), "control: the moth goal's filter takes the player back in"
                    + " survival (" + FINDING + ")");
        } finally {
            removePlayer(helper, player);
            discardQuietly(butterfly);
            discardQuietly(moth);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers (the CreativeGateParityTests / ScanSetParityTests / TargetReleaseParityTests idioms)
    // ------------------------------------------------------------------

    private static void assertPlayNicelyOff(GameTestHelper helper) {
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(), "precondition: PlayNicely is off — the Ender pair's pick and the Hammerhead's /"
                + " Irukandji's passes answer nothing under it (ENT-S-115) (" + FINDING + " test setup)");
    }

    private static void assertNormalDifficulty(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL, "precondition: the game-test level runs at NORMAL, not"
                + " Peaceful — the Lizard's and the butterfly's filters refuse on Peaceful and a player takes no mob damage there (" + FINDING
                + " test setup)");
    }

    /** With its registered goals but no AI, so nothing runs by itself; on the ground. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setOnGround(true);
        return mob;
    }

    /**
     * LIVE — its AI runs on the engine's ticks — with its movement speed zeroed and its gravity off, so nothing but a teleport
     * moves it (a live mob is otherwise set down by {@code travel} — {@code Entity.getGravity()} answers 0 under noGravity; the
     * toward row's landing sits a fraction above the floor); its feet on the floor.
     */
    private static Mob spawnLive(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setPersistenceRequired();
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
        mob.setNoGravity(true);
        mob.setOnGround(true);
        return mob;
    }

    /** Frozen prey with 1000 HP at an exact spot: goals stripped, noAi, persistence set, on the ground. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob prey = helper.spawnWithNoFreeWill(type, pos);
        prey.setNoAi(true);
        prey.setPersistenceRequired();
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        prey.setOnGround(true);
        return prey;
    }

    /**
     * A plain {@link ServerPlayer} put on the player list the way the framework's mock is, without the framework's override
     * (PlayNicelyGateParityTests.survivalServerPlayerAt): {@code GameTestHelper.makeMockServerPlayerInLevel} answers
     * {@code isCreative()} true whatever its mode; this one's follows its SURVIVAL mode, and its abilities are the mode's.
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

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /**
     * A fresh ServerPlayer refuses every hurt that does not bypass invulnerability for its first 60 ticks ({@code ServerPlayer.hurt}
     * reads {@code spawnInvulnerableTime}); the six dice rows, whose signal is the bite landing on the mock in the tick it was placed,
     * write it to 0 by name — the TargetReleaseParityTests idiom. Every other row keeps the shield.
     */
    private static void clearSpawnInvulnerability(ServerPlayer player) {
        try {
            Field field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            field.setAccessible(true);
            field.setInt(player, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("ServerPlayer.spawnInvulnerableTime is not reachable by reflection (1.21.1: official names at"
                    + " runtime) (" + FINDING + " test setup)", exception);
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

    /**
     * orig EnderKnight.java:149-156 with the pinned rolls — where {@code teleportToEntity} lands: the unit vector from the target to
     * the mob's mid-height ({@code bb.minY + height / 2 - target.y + target.eyeHeight}, the expression as 1.7.10 wrote it), then
     * 16 blocks along it from the mob, x and z jittered by {@code (roll - 0.5) * 8}, y by {@code nextInt(16) - 8}.
     */
    private static Vec3 towardLanding(Mob mob, Entity target, double jitterRoll, int yRoll) {
        Vec3 vec = new Vec3(mob.getX() - target.getX(),
                mob.getBoundingBox().minY + mob.getBbHeight() / 2.0f - target.getY() + target.getEyeHeight(),
                mob.getZ() - target.getZ()).normalize();
        return new Vec3(mob.getX() + (jitterRoll - 0.5) * 8.0 - vec.x * 16.0,
                mob.getY() + (yRoll - 8) - vec.y * 16.0,
                mob.getZ() + (jitterRoll - 0.5) * 8.0 - vec.z * 16.0);
    }

    /** The Ender mob's player goal off its target selector: exactly one NearestAttackableTargetGoal of target type Player. */
    private static NearestAttackableTargetGoal<?> playerGoal(GameTestHelper helper, Mob hunter) {
        NearestAttackableTargetGoal<?> found = null;
        int count = 0;
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest
                    && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == Player.class) {
                found = nearest;
                count++;
            }
        }
        helper.assertTrue(count == 1 && found != null, "precondition: " + hunter.getClass().getSimpleName() + " carries exactly one"
                + " NearestAttackableTargetGoal<Player> on its target selector, found " + count + " (" + FINDING + " test setup)");
        return found;
    }

    private static boolean isRunning(Mob hunter, Goal goal) {
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() == goal) return wrapped.isRunning();
        }
        return false;
    }

    /** The butterfly's / moth's ButterflyIslandsHuntGoal off its goal selector (the ProactiveHuntParityTests idiom). */
    private static Goal huntGoal(Mob flyer) {
        for (WrappedGoal wrapped : flyer.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof ButterflyIslandsHuntGoal) {
                return wrapped.getGoal();
            }
        }
        throw new IllegalStateException("precondition: " + flyer.getClass().getSimpleName() + " must carry a ButterflyIslandsHuntGoal on its"
                + " goal selector (" + FINDING + " test setup)");
    }

    /** The hunt goal's private {@code findSomethingToAttack()} (declared on ButterflyIslandsHuntGoal, the moth's subclass included). */
    private static LivingEntity scan(Goal huntGoal) {
        return (LivingEntity) invoke(huntGoal, ButterflyIslandsHuntGoal.class, "findSomethingToAttack", new Class<?>[0]);
    }

    /** The private one-arg {@code isSuitableTarget(LivingEntity)} of the hunter or the goal. */
    private static boolean filter(Object owner, Class<?> declaring, LivingEntity candidate) {
        return (Boolean) invoke(owner, declaring, "isSuitableTarget", new Class<?>[] {LivingEntity.class}, candidate);
    }

    /** {@code EnderKnight.shouldAttackPlayer(Player)} / {@code EnderReaper.shouldAttackPlayer(Player)} — package-private, orig :83-93. */
    private static boolean shouldAttackPlayer(Mob hunter, Player player) {
        return (Boolean) invoke(hunter, hunter.getClass(), "shouldAttackPlayer", new Class<?>[] {Player.class}, player);
    }

    /** The hunter's protected customServerAiStep, declared on the given class, invoked once. */
    private static void invokeAiStep(Class<? extends Mob> declaring, Mob hunter) {
        invoke(hunter, declaring, "customServerAiStep", new Class<?>[0]);
    }

    /** The Ender mob's own customServerAiStep override. */
    private static void invokeCustomServerAiStep(Mob hunter) {
        invoke(hunter, hunter.getClass(), "customServerAiStep", new Class<?>[0]);
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

    /** orig :31 {@code teleportDelay} — the far counter, read off the mob's own class. */
    private static int teleportDelay(Mob hunter) {
        return (Integer) readObject(hunter, "teleportDelay");
    }

    /** orig :32 {@code stareTimer} — the stare-sound cadence, read off the mob's own class. */
    private static int stareTimer(Mob hunter) {
        return (Integer) readObject(hunter, "stareTimer");
    }

    private static LivingEntity goalTarget(NearestAttackableTargetGoal<?> goal) {
        return (LivingEntity) readField(goal, NearestAttackableTargetGoal.class, "target");
    }

    private static TargetingConditions conditionsOf(NearestAttackableTargetGoal<?> goal) {
        return (TargetingConditions) readField(goal, NearestAttackableTargetGoal.class, "targetConditions");
    }

    private static String describe(LivingEntity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }

    private static CreativeGateParityTests.ScriptedRolls scripted(int... boundAnswerPairs) {
        return new CreativeGateParityTests.ScriptedRolls(RandomSource.create(1234L), boundAnswerPairs);
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

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name + " (1.21.1: official names at runtime)", exception);
        }
    }

    private static Object readObject(Mob mob, String name) {
        return readField(mob, mob.getClass(), name);
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

    /**
     * Every ENDERMAN_STARE the level plays within {@link #EAR_RADIUS} of the player's spot while open — the StinkyIdleParityTests
     * BurpEar seam: orig :69's {@code playSoundAtEntity(player, "mob.endermen.stare", …)} is the port's
     * {@code Level.playSound(null, x, y, z, …)} at the player's position, which reaches {@code ServerLevel.playSeededSound} and
     * its {@code PlayLevelSoundEvent.AtPosition} before the broadcast.
     */
    private static final class StareEar {
        int count;
        private final Consumer<PlayLevelSoundEvent> listener;
        private boolean open;

        StareEar(Entity around) {
            Vec3 centre = around.position();
            this.listener = event -> {
                if (event instanceof PlayLevelSoundEvent.AtPosition at && event.getSound() != null
                        && event.getSound().value() == SoundEvents.ENDERMAN_STARE && at.getPosition().distanceTo(centre) < EAR_RADIUS) {
                    this.count++;
                }
            };
            NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.class, this.listener);
            this.open = true;
        }

        void close() {
            if (this.open) {
                NeoForge.EVENT_BUS.unregister(this.listener);
                this.open = false;
            }
        }
    }

    /**
     * The ForcedRoll seam for the teleport formulas on a live or driven Ender mob: {@code nextInt(bound)} answers the pinned value
     * for the listed bounds (64 for the random teleport's y, 16 for the toward teleport's y) and delegates the rest; every
     * {@code nextDouble()} answers one value (the random teleport's x / z offsets, orig :143 / :145, or the toward jitter, :153 /
     * :155); every {@code nextFloat()} answers one value (the daylight dice, orig :111, pinned quiet — aiStep draws it every day tick).
     */
    static final class TeleportRolls implements RandomSource {
        private final RandomSource delegate;
        private final Map<Integer, Integer> intAnswers = new TreeMap<>();
        private final double doubleAnswer;
        private final float floatAnswer;

        TeleportRolls(RandomSource delegate, double doubleAnswer, float floatAnswer, int... boundAnswerPairs) {
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
