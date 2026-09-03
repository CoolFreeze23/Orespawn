package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-113: two 1.7.10 gates in the Cephadrome's target filter (orig Cephadrome.java:515-573)
 * that the port's {@code isSuitableTarget} lacked. orig :516-518 answers false on PEACEFUL before
 * any other check — ahead even of the {@code instanceof EntityMob → true} short-circuit at
 * :534-536; orig :566-569 spends {@code shouldattack}, the stalk flag an unfed shark arms when it
 * refuses a mount (orig :897-900, port {@code Cephadrome.mobInteract}), on the one answer it
 * grants: the player branch reads it only behind the creative test (:557) and the hit-by-player
 * (:560-562) and bad-mood (:563-565) answers, and zeroes it as it returns true, so the would-be
 * rider is stalked for a single scan and the next answer is :570 false.
 *
 * <p>Six tests, all through the private filter and the private counters by reflection (the
 * CreativeMappingParityTests idiom). The PEACEFUL pair flips the difficulty with
 * {@code MinecraftServer.setDifficulty(…, true)} inside the test and restores it in a finally
 * (the EntityLogicTestsB idiom); the game-test server runs NORMAL, asserted as a precondition,
 * and the same shark and target are asked before and after the flip so nothing but the
 * difficulty separates the two answers. The reset tests write {@code shouldattack} by hand (the
 * refused-mount path in mobInteract is the one that arms it in play), run the filter and read the
 * counter back: spent by the player branch, untouched by a vanilla Zombie (the :534 answer
 * precedes the branch), untouched behind a bad mood (:563 answers first), and untouched with a
 * creative player (:557 answers first — the ENT-S-107 case still holds).</p>
 *
 * <p>Synchronous; the one piece of global state touched, the difficulty, is restored before the
 * test returns. Own batch {@code cephadromeGates} (TEST-003). Geometry as
 * CreativeMappingParityTests: the shark frozen at rel (20,1,24) on the floor of the 48x16x48
 * empty_large, the player or Zombie 8 blocks east on the same floor, clear line of sight asserted.
 * Mock players are the KrakenTargetingParityTests kind with the mode set explicitly.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CephadromeGateTests {

    private static final String BATCH = "cephadromeGates";
    /** The shark on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** A mob target 8 blocks east on the same floor. */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** The player 8 blocks east on the same floor. */
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);

    // ------------------------------------------------------------------
    // (a) orig Cephadrome.java:516-518 — PEACEFUL rejects everything first
    // ------------------------------------------------------------------

    /** orig Cephadrome.java:516-518: PEACEFUL rejects the survival player a bad-mood shark takes on NORMAL (:563-565). */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s113_cephadrome_peaceful_rejects_survival_player(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        helper.assertTrue(before != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful (ENT-S-113 test setup)");
        Mob shark = null;
        ServerPlayer player = null;
        try {
            shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            writeInt(shark, "badmood", 1); // orig Cephadrome.java:563-565 — the branch's "yes" for a survival player
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            assertSees(helper, shark, player);
            helper.assertTrue(isSuitableTarget(shark, player), "control: on " + before
                    + " a bad-mood Cephadrome must take a survival player (orig Cephadrome.java:563-565) (ENT-S-113)");
            server.setDifficulty(Difficulty.PEACEFUL, true);
            assertPeaceful(helper);
            helper.assertTrue(!isSuitableTarget(shark, player), "Cephadrome.isSuitableTarget on PEACEFUL: orig"
                    + " Cephadrome.java:516-518 answers false before any other check, so the same bad-mood shark must"
                    + " reject the same survival player it took on " + before + " (ENT-S-113)");
        } finally {
            server.setDifficulty(before, true);
            removePlayer(helper, player);
            if (shark != null) shark.discard();
        }
        helper.succeed();
    }

    /**
     * orig Cephadrome.java:516 sits ahead of the EntityMob short-circuit (:534-536): PEACEFUL rejects
     * the vanilla Zombie NORMAL takes, so the guard is the filter's first line and not a player-branch
     * gate. The Zombie is never ticked, so PEACEFUL cannot despawn it inside the test.
     */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s113_cephadrome_peaceful_rejects_monster(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Difficulty before = helper.getLevel().getDifficulty();
        helper.assertTrue(before != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful (ENT-S-113 test setup)");
        Mob shark = null;
        Mob zombie = null;
        try {
            shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            zombie = spawnFrozen(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, shark, zombie);
            helper.assertTrue(isSuitableTarget(shark, zombie), "control: on " + before
                    + " a Cephadrome must take a vanilla Zombie, an EntityMob (orig Cephadrome.java:534-536) (ENT-S-113)");
            server.setDifficulty(Difficulty.PEACEFUL, true);
            assertPeaceful(helper);
            helper.assertTrue(zombie.isAlive(), "precondition: the frozen Zombie is still alive on PEACEFUL — nothing ticks"
                    + " inside the test (ENT-S-113 test setup)");
            helper.assertTrue(!isSuitableTarget(shark, zombie), "Cephadrome.isSuitableTarget on PEACEFUL: orig"
                    + " Cephadrome.java:516-518 answers false ahead of the EntityMob answer at :534-536, so the Zombie taken"
                    + " on " + before + " must be rejected (ENT-S-113)");
        } finally {
            server.setDifficulty(before, true);
            if (zombie != null) zombie.discard();
            if (shark != null) shark.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // (b) orig Cephadrome.java:566-569 — the player branch spends shouldattack
    // ------------------------------------------------------------------

    /**
     * orig Cephadrome.java:566-569: a fresh shark (hit_by_player 0, badmood 0) with
     * {@code shouldattack} armed takes the survival player once and zeroes the flag as it answers;
     * asked again, nothing says yes and :570 answers false.
     */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s113_cephadrome_player_branch_spends_shouldattack(GameTestHelper helper) {
        Mob shark = null;
        ServerPlayer player = null;
        try {
            shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            helper.assertTrue(readInt(shark, "hitByPlayer") == 0 && readInt(shark, "badmood") == 0,
                    "precondition: a fresh Cephadrome has hit_by_player 0 and badmood 0, so only shouldattack can answer"
                            + " (orig Cephadrome.java:560-565) (ENT-S-113 test setup)");
            writeInt(shark, "shouldattack", 1); // orig Cephadrome.java:899 — armed by the refused mount
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            assertSees(helper, shark, player);
            helper.assertTrue(isSuitableTarget(shark, player), "Cephadrome.isSuitableTarget: orig Cephadrome.java:566-568"
                    + " answers true while shouldattack > 0 (ENT-S-113)");
            int spent = readInt(shark, "shouldattack");
            helper.assertTrue(spent == 0, "Cephadrome.isSuitableTarget: orig Cephadrome.java:567 zeroes shouldattack as it"
                    + " answers true — expected 0, got " + spent + " (ENT-S-113)");
            helper.assertTrue(!isSuitableTarget(shark, player), "Cephadrome.isSuitableTarget asked again: the flag was spent"
                    + " and nothing else says yes, so orig Cephadrome.java:570 answers false (ENT-S-113)");
        } finally {
            removePlayer(helper, player);
            if (shark != null) shark.discard();
        }
        helper.succeed();
    }

    /** A vanilla Zombie is answered at orig Cephadrome.java:534-536, before the player branch: {@code shouldattack} stays armed. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s113_cephadrome_shouldattack_untouched_by_non_player(GameTestHelper helper) {
        Mob shark = null;
        Mob zombie = null;
        try {
            shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            writeInt(shark, "shouldattack", 1);
            zombie = spawnFrozen(helper, EntityType.ZOMBIE, PREY_POS);
            assertSees(helper, shark, zombie);
            helper.assertTrue(isSuitableTarget(shark, zombie),
                    "control: a Cephadrome takes a vanilla Zombie, an EntityMob (orig Cephadrome.java:534-536) (ENT-S-113)");
            int left = readInt(shark, "shouldattack");
            helper.assertTrue(left == 1, "Cephadrome.isSuitableTarget: the reset at orig Cephadrome.java:567 lives in the"
                    + " player branch only, so a Zombie answer must leave shouldattack at 1 — got " + left + " (ENT-S-113)");
        } finally {
            if (zombie != null) zombie.discard();
            if (shark != null) shark.discard();
        }
        helper.succeed();
    }

    /** orig Cephadrome.java:563-565 answers before :566: a bad-mood shark takes the player and leaves {@code shouldattack} armed. */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s113_cephadrome_shouldattack_untouched_behind_badmood(GameTestHelper helper) {
        Mob shark = null;
        ServerPlayer player = null;
        try {
            shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            writeInt(shark, "badmood", 1);
            writeInt(shark, "shouldattack", 1);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(PLAYER_POS));
            assertSees(helper, shark, player);
            helper.assertTrue(isSuitableTarget(shark, player),
                    "control: a bad-mood Cephadrome takes a survival player (orig Cephadrome.java:563-565) (ENT-S-113)");
            int left = readInt(shark, "shouldattack");
            helper.assertTrue(left == 1, "Cephadrome.isSuitableTarget: orig Cephadrome.java:563-565 returns before the"
                    + " :566-569 reset, so a bad-mood answer must leave shouldattack at 1 — got " + left + " (ENT-S-113)");
        } finally {
            removePlayer(helper, player);
            if (shark != null) shark.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // (c) orig Cephadrome.java:557-559 — the ENT-S-107 creative case still holds
    // ------------------------------------------------------------------

    /**
     * orig Cephadrome.java:557-559 answers before everything in the branch: a creative player is
     * rejected by a bad-mood shark with {@code shouldattack} armed, and the flag is left armed
     * (the ENT-S-107 mapping, {@code Abilities.instabuild}, still in place after ENT-S-113).
     */
    @GameTest(template = "empty_large", batch = BATCH)
    public void s113_cephadrome_creative_player_rejected_before_shouldattack(GameTestHelper helper) {
        Mob shark = null;
        ServerPlayer player = null;
        try {
            shark = spawnFrozen(helper, ModEntities.CEPHADROME.get(), HUNTER_POS);
            writeInt(shark, "badmood", 1);
            writeInt(shark, "shouldattack", 1);
            player = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(PLAYER_POS));
            helper.assertTrue(player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: a creative player has both instabuild and invulnerable set (ENT-S-113 test setup)");
            assertSees(helper, shark, player);
            helper.assertTrue(!isSuitableTarget(shark, player), "Cephadrome.isSuitableTarget: a creative player must be"
                    + " rejected even by a bad-mood shark with shouldattack armed (orig Cephadrome.java:557-559"
                    + " capabilities.isCreativeMode → Abilities.instabuild, ENT-S-107) (ENT-S-113)");
            int left = readInt(shark, "shouldattack");
            helper.assertTrue(left == 1, "Cephadrome.isSuitableTarget: orig Cephadrome.java:557-559 returns before the"
                    + " :566-569 reset, so a creative answer must leave shouldattack at 1 — got " + left + " (ENT-S-113)");
        } finally {
            removePlayer(helper, player);
            if (shark != null) shark.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers (the CreativeMappingParityTests idiom)
    // ------------------------------------------------------------------

    private static void assertPeaceful(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getDifficulty() == Difficulty.PEACEFUL,
                "precondition: MinecraftServer.setDifficulty(PEACEFUL, true) must show through level.getDifficulty()"
                        + " (ENT-S-113 test setup)");
    }

    private static void assertSees(GameTestHelper helper, Mob shark, LivingEntity target) {
        helper.assertTrue(shark.hasLineOfSight(target), "precondition: the Cephadrome must see the "
                + target.getClass().getSimpleName() + " 8 blocks away inside the barrier shell (ENT-S-113 test geometry)");
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server
     * defaults to CREATIVE, GameTestServer.java:85). Health is raised so nothing incidental
     * can kill it. Deprecated mock-player factory tolerated the way KrakenTargetingParityTests
     * and CreativeMappingParityTests do.
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

    /** The Cephadrome's private {@code isSuitableTarget(LivingEntity)} — the port's one-arg shape of orig :515. */
    private static boolean isSuitableTarget(Mob shark, LivingEntity candidate) {
        String name = shark.getClass().getSimpleName();
        try {
            Method method = shark.getClass().getDeclaredMethod("isSuitableTarget", LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(shark, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + ".isSuitableTarget threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name + ".isSuitableTarget", exception);
        }
    }

    private static int readInt(Mob mob, String name) {
        String owner = mob.getClass().getSimpleName();
        try {
            Field field = mob.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(mob);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + owner + "." + name, exception);
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
