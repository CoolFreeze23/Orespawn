package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-107: the 1.7.10 "creative" test in two hunters' player branches. orig
 * Leon.java:417 and orig Cephadrome.java:557 read
 * {@code capabilities.isCreativeMode} ({@code field_75098_d}); the port's
 * EntityLeon and Cephadrome had mapped it to {@code Abilities.invulnerable},
 * where the 1.21.1 analogue is {@code Abilities.instabuild} — the port's own
 * idiom at the Kraken's KT-A site (Kraken.java, orig :965) and in TheKing /
 * TheQueen / SpiderRobot. The two flags agree for a stock creative or survival
 * player and differ for a survival player made invulnerable by other means
 * ({@code Abilities.invulnerable} set without creative): 1.7.10 hunted that
 * player, the old mapping spared it.
 *
 * <p>Three cases per hunter, each reached through the private
 * {@code isSuitableTarget(LivingEntity)} by reflection (the tree's precedent:
 * KrakenTargetingParityTests, IgnoreListParityTests): a creative player is
 * rejected (orig :417 / :557); a SURVIVAL player with {@code invulnerable}
 * flipped on is still prey — the discriminating case, which the old
 * {@code invulnerable} mapping failed; and a plain survival player is prey
 * (the control). The Leon is untamed, so orig :420 {@code !isTamed()} answers
 * true; the Cephadrome is put in a bad mood (orig :563-565 {@code badmood != 0}
 * → true, the field spawner-spawned sharks are born with, port
 * Cephadrome.checkSpawnRules) so its player branch reaches an answer past the
 * creative check.</p>
 *
 * <p>Synchronous, no global state touched. Own batch (TEST-003). Both hunters
 * stand frozen on the floor of the 48x16x48 empty_large (the Leon's eye at
 * rel 8.0, the Cephadrome's at rel 2.9 — under the barrier ceiling at rel 17),
 * the mock player 8 blocks away on the same floor with clear line of sight,
 * asserted as a precondition. Mock players are the KrakenTargetingParityTests
 * kind: the game-test server defaults them to CREATIVE, so the mode is always
 * set explicitly.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CreativeMappingParityTests {

    /** Hunter on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** The player 8 blocks away on the same floor, clear line of sight. */
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);

    /** The three player states of the ruling. */
    private enum PlayerCase {
        /** {@code GameType.CREATIVE}: instabuild and invulnerable both set. */
        CREATIVE,
        /** {@code GameType.SURVIVAL} with {@code Abilities.invulnerable} flipped on by hand: instabuild clear. */
        SURVIVAL_INVULNERABLE,
        /** Plain {@code GameType.SURVIVAL}. */
        SURVIVAL
    }

    // ------------------------------------------------------------------
    // EntityLeon — orig Leon.java:415-421
    // ------------------------------------------------------------------

    /** orig Leon.java:417-419: a creative player is never prey. */
    @GameTest(template = "empty_large", batch = "creativeMappingParity")
    public void s107_leon_creative_player_rejected(GameTestHelper helper) {
        assertLeonCase(helper, PlayerCase.CREATIVE, false,
                "a creative player must be rejected (orig Leon.java:417 capabilities.isCreativeMode → Abilities.instabuild)");
    }

    /**
     * The discriminating case: orig Leon.java:417 tests {@code isCreativeMode} only, so a
     * survival player who is invulnerable by other means is still prey (:420, untamed).
     * The port's old {@code invulnerable} mapping rejected it.
     */
    @GameTest(template = "empty_large", batch = "creativeMappingParity")
    public void s107_leon_invulnerable_survival_player_still_prey(GameTestHelper helper) {
        assertLeonCase(helper, PlayerCase.SURVIVAL_INVULNERABLE, true,
                "a SURVIVAL player with Abilities.invulnerable set is not creative (orig Leon.java:417 reads"
                        + " isCreativeMode only) and must stay prey of an untamed Leon (orig :420); the old"
                        + " invulnerable mapping rejected it");
    }

    /** Control: orig Leon.java:420 — a plain survival player is prey of an untamed Leon. */
    @GameTest(template = "empty_large", batch = "creativeMappingParity")
    public void s107_leon_survival_player_prey(GameTestHelper helper) {
        assertLeonCase(helper, PlayerCase.SURVIVAL, true,
                "control: a plain survival player must be prey of an untamed Leon (orig Leon.java:420)");
    }

    // ------------------------------------------------------------------
    // Cephadrome — orig Cephadrome.java:555-569
    // ------------------------------------------------------------------

    /** orig Cephadrome.java:557-559: a creative player is never prey, bad mood or not. */
    @GameTest(template = "empty_large", batch = "creativeMappingParity")
    public void s107_cephadrome_creative_player_rejected(GameTestHelper helper) {
        assertCephadromeCase(helper, PlayerCase.CREATIVE, false,
                "a creative player must be rejected even by a bad-mood Cephadrome (orig Cephadrome.java:557"
                        + " capabilities.isCreativeMode → Abilities.instabuild)");
    }

    /**
     * The discriminating case: orig Cephadrome.java:557 tests {@code isCreativeMode} only,
     * so a survival player who is invulnerable by other means still reaches the bad-mood
     * answer (:563-565, true). The port's old {@code invulnerable} mapping rejected it.
     */
    @GameTest(template = "empty_large", batch = "creativeMappingParity")
    public void s107_cephadrome_invulnerable_survival_player_still_prey(GameTestHelper helper) {
        assertCephadromeCase(helper, PlayerCase.SURVIVAL_INVULNERABLE, true,
                "a SURVIVAL player with Abilities.invulnerable set is not creative (orig Cephadrome.java:557"
                        + " reads isCreativeMode only) and must stay prey of a bad-mood Cephadrome (orig :563-565);"
                        + " the old invulnerable mapping rejected it");
    }

    /** Control: orig Cephadrome.java:563-565 — a plain survival player is prey of a bad-mood Cephadrome. */
    @GameTest(template = "empty_large", batch = "creativeMappingParity")
    public void s107_cephadrome_survival_player_prey(GameTestHelper helper) {
        assertCephadromeCase(helper, PlayerCase.SURVIVAL, true,
                "control: a plain survival player must be prey of a bad-mood Cephadrome (orig Cephadrome.java:563-565)");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void assertLeonCase(GameTestHelper helper, PlayerCase playerCase, boolean expected, String why) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: orig Leon.java:388 rejects everything on Peaceful; the game-test level runs at NORMAL"
                        + " (ENT-S-107 test setup)");
        Mob leon = null;
        ServerPlayer player = null;
        try {
            leon = spawnFrozen(helper, ModEntities.ENTITY_LEON.get());
            helper.assertTrue(leon instanceof TamableAnimal tame && !tame.isTame(),
                    "precondition: a fresh Leon must be untamed so orig Leon.java:420 answers true (ENT-S-107 test setup)");
            player = playerFor(helper, playerCase);
            assertOutcome(helper, leon, player, playerCase, expected, why);
        } finally {
            removePlayer(helper, player);
            if (leon != null) leon.discard();
        }
        helper.succeed();
    }

    private static void assertCephadromeCase(GameTestHelper helper, PlayerCase playerCase, boolean expected, String why) {
        Mob cephadrome = null;
        ServerPlayer player = null;
        try {
            cephadrome = spawnFrozen(helper, ModEntities.CEPHADROME.get());
            // orig Cephadrome.java:563-565 — badmood != 0 is the branch's "yes"; the port's
            // spawner bypass (Cephadrome.checkSpawnRules) is the one place that sets it, so
            // the field is written directly, the tree's reflection precedent for private state.
            writeInt(cephadrome, "badmood", 1);
            player = playerFor(helper, playerCase);
            assertOutcome(helper, cephadrome, player, playerCase, expected, why);
        } finally {
            removePlayer(helper, player);
            if (cephadrome != null) cephadrome.discard();
        }
        helper.succeed();
    }

    private static void assertOutcome(GameTestHelper helper, Mob hunter, ServerPlayer player, PlayerCase playerCase,
                                      boolean expected, String why) {
        String hunterName = hunter.getClass().getSimpleName();
        helper.assertTrue(hunter.hasLineOfSight(player),
                "precondition: the " + hunterName + " must see the player 8 blocks away inside the barrier shell"
                        + " (ENT-S-107 test geometry)");
        switch (playerCase) {
            case CREATIVE -> helper.assertTrue(player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: a creative player has both instabuild and invulnerable set (ENT-S-107 test setup)");
            case SURVIVAL_INVULNERABLE -> helper.assertTrue(!player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: the discriminating player is survival (instabuild clear) with invulnerable set by hand"
                            + " (ENT-S-107 test setup)");
            case SURVIVAL -> helper.assertTrue(!player.getAbilities().instabuild && !player.getAbilities().invulnerable,
                    "precondition: a plain survival player has neither flag (ENT-S-107 test setup)");
        }
        boolean actual = isSuitableTarget(hunter, player);
        helper.assertTrue(actual == expected, hunterName + ".isSuitableTarget(" + playerCase + " player): " + why
                + " — expected " + expected + ", got " + actual + " (ENT-S-107)");
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type) {
        E mob = helper.spawnWithNoFreeWill(type, HUNTER_POS);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static ServerPlayer playerFor(GameTestHelper helper, PlayerCase playerCase) {
        ServerPlayer player = playerAt(helper,
                playerCase == PlayerCase.CREATIVE ? GameType.CREATIVE : GameType.SURVIVAL,
                helper.absoluteVec(PLAYER_POS));
        if (playerCase == PlayerCase.SURVIVAL_INVULNERABLE) {
            // Invulnerable by other means than creative: the flag alone, instabuild untouched.
            player.getAbilities().invulnerable = true;
        }
        return player;
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server
     * defaults to CREATIVE, GameTestServer.java:85). Health is raised so nothing incidental
     * can kill it. Deprecated mock-player factory tolerated the way KrakenTargetingParityTests
     * and KrakenPlayNicelyGateTests do.
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

    /** Each hunter's private {@code isSuitableTarget(LivingEntity)} — the port's one-arg shape of the orig two-arg method. */
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
