package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBrutalfly;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-107 and ENT-S-109: the 1.7.10 "creative" test in the hunters' player branches.
 * orig Leon.java:417 and orig Cephadrome.java:557 read {@code capabilities.isCreativeMode}
 * ({@code field_75098_d}); the port's EntityLeon and Cephadrome had mapped it to
 * {@code Abilities.invulnerable}, where the 1.21.1 analogue is {@code Abilities.instabuild}
 * — the port's own idiom at the Kraken's KT-A site (Kraken.java, orig :965) and in TheKing /
 * TheQueen / SpiderRobot: {@code GameType.updatePlayerAbilities} sets {@code instabuild} for
 * CREATIVE only, {@code invulnerable} for CREATIVE and SPECTATOR, and either can be toggled
 * by hand. The two flags agree for a stock creative or survival player and differ for a
 * survival player made invulnerable by other means ({@code Abilities.invulnerable} set
 * without creative): 1.7.10 hunted that player, the old mapping spared it. ENT-S-109 is the
 * same mapping at the ten sibling sites — nine hunters' filters and the Brutalfly's fireball
 * strafe — fixed under the same ruling and pinned here by a {@link GameTestGenerator} over
 * {@link #filterSites()} plus {@link #assertBrutalflyStrafe}.
 *
 * <p>Three cases per site, each reached through the private
 * {@code isSuitableTarget(LivingEntity)} by reflection (the tree's precedent:
 * KrakenTargetingParityTests, IgnoreListParityTests): a creative player is rejected (orig
 * :417 / :557 and each ENT-S-109 site's orig lines); a SURVIVAL player with
 * {@code invulnerable} flipped on is still prey — the discriminating case, which the old
 * {@code invulnerable} mapping failed; and a plain survival player is prey (the control).
 * The Leon is untamed, so orig :420 {@code !isTamed()} answers true; the Cephadrome is put
 * in a bad mood (orig :563-565 {@code badmood != 0} → true, the field spawner-spawned
 * sharks are born with, port Cephadrome.checkSpawnRules) so its player branch reaches an
 * answer past the creative check. The Brutalfly's strafe (orig Brutalfly.java:213-227) is
 * not a filter: its AI tick is driven once by reflection under a forced random, see
 * {@link #assertBrutalflyStrafe}.</p>
 *
 * <p>Synchronous, no global state touched. Own batch (TEST-003). Every hunter stands
 * frozen on the floor of the 48x16x48 empty_large (the Leon's eye at rel 8.0, the
 * Cephadrome's at rel 2.9, the Triffid's at rel 4.4 — all under the barrier ceiling at rel
 * 17), the mock player 8 blocks away on the same floor with clear line of sight, asserted
 * as a precondition. Mock players are the KrakenTargetingParityTests kind: the game-test
 * server defaults them to CREATIVE, so the mode is always set explicitly.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CreativeMappingParityTests {

    private static final String BATCH = "creativeMappingParity";
    private static final String TEST_PREFIX = "creativemappingparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (IgnoreScreenParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;

    /** Hunter on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** The player 8 blocks away on the same floor, clear line of sight. */
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);

    /** The three player states of the ruling, each with its generated-test suffix and the 1.7.10 answer. */
    private enum PlayerCase {
        /** {@code GameType.CREATIVE}: instabuild and invulnerable both set — rejected by the orig creative test. */
        CREATIVE("creative_player_rejected", false),
        /** {@code GameType.SURVIVAL} with {@code Abilities.invulnerable} flipped on by hand: instabuild clear — still prey, the discriminating case. */
        SURVIVAL_INVULNERABLE("invulnerable_survival_player_still_prey", true),
        /** Plain {@code GameType.SURVIVAL} — prey, the control. */
        SURVIVAL("survival_player_prey", true);

        final String suffix;
        /** Whether 1.7.10 takes the player: false for the creative case only. */
        final boolean prey;

        PlayerCase(String suffix, boolean prey) {
            this.suffix = suffix;
            this.prey = prey;
        }
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
    // ENT-S-109 — the ten sibling sites, generated per site and case
    // ------------------------------------------------------------------

    /**
     * One ENT-S-109 filter site: a hunter whose private {@code isSuitableTarget(LivingEntity)}
     * carries the player branch. {@code origCreative} is the orig creative test, {@code origPrey}
     * the orig line that takes a non-creative player, {@code port} the port line ENT-S-109 changed;
     * {@code peacefulGated} marks a filter whose orig rejects everything on Peaceful.
     */
    private record FilterSite(String key, Supplier<? extends EntityType<? extends Mob>> hunter,
                              String origCreative, String origPrey, String port, boolean peacefulGated) {
        String testName(PlayerCase playerCase) {
            return TEST_PREFIX + "s109_" + this.key + "_" + playerCase.suffix;
        }
    }

    /**
     * The nine filter sites of ENT-S-109 in the finding's order; every orig site reads
     * {@code capabilities.isCreativeMode} ({@code field_75098_d}). The tenth site, the
     * Brutalfly's strafe, is not a filter and has its own runner.
     */
    private static List<FilterSite> filterSites() {
        List<FilterSite> sites = new ArrayList<>();
        sites.add(new FilterSite("cryolophosaurus", ModEntities.CRYOLOPHOSAURUS,
                "Cryolophosaurus.java:204-208", "Cryolophosaurus.java:210", "Cryolophosaurus.java:120", false));
        sites.add(new FilterSite("brutalfly_filter", ModEntities.ENTITY_BRUTALFLY,
                "Brutalfly.java:436-439", "Brutalfly.java:438 (!isCreativeMode)", "EntityBrutalfly.java:358", false));
        sites.add(new FilterSite("gamma_metroid", ModEntities.ENTITY_GAMMA_METROID,
                "GammaMetroid.java:281-286", "GammaMetroid.java:287 (untamed, :278)", "EntityGammaMetroid.java:216", true));
        sites.add(new FilterSite("kyuubi", ModEntities.ENTITY_KYUUBI,
                "Kyuubi.java:195-200", "Kyuubi.java:201", "EntityKyuubi.java:145", false));
        sites.add(new FilterSite("leaf_monster", ModEntities.ENTITY_LEAF_MONSTER,
                "LeafMonster.java:200-205", "LeafMonster.java:202-204 (!isCreativeMode → true)", "EntityLeafMonster.java:162", false));
        sites.add(new FilterSite("lurking_terror", ModEntities.ENTITY_LURKING_TERROR,
                "LurkingTerror.java:341-346", "LurkingTerror.java:347", "EntityLurkingTerror.java:248", false));
        sites.add(new FilterSite("rat", ModEntities.ENTITY_RAT,
                "Rat.java:225-229", "Rat.java:248 (a wild rat, myowner null, skips :230-237)", "EntityRat.java:199", false));
        sites.add(new FilterSite("terrible_terror", ModEntities.ENTITY_TERRIBLE_TERROR,
                "TerribleTerror.java:286-291", "TerribleTerror.java:292", "EntityTerribleTerror.java:163", false));
        sites.add(new FilterSite("triffid", ModEntities.ENTITY_TRIFFID,
                "Triffid.java:312-317", "Triffid.java:318", "EntityTriffid.java:253", false));
        return sites;
    }

    /**
     * ENT-S-109: the three cases of the ruling per site — 27 over the nine filter sites plus
     * three for the Brutalfly's strafe — 30 TestFunctions in the {@code creativeMappingParity}
     * batch, named {@code creativemappingparitytests.s109_<site>_<case>}.
     */
    @GameTestGenerator
    public Collection<TestFunction> s109CreativeMappingSites() {
        List<TestFunction> functions = new ArrayList<>();
        for (FilterSite site : filterSites()) {
            for (PlayerCase playerCase : PlayerCase.values()) {
                functions.add(new TestFunction(BATCH, site.testName(playerCase), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                        helper -> assertFilterSite(helper, site, playerCase)));
            }
        }
        for (PlayerCase playerCase : PlayerCase.values()) {
            functions.add(new TestFunction(BATCH, TEST_PREFIX + "s109_brutalfly_strafe_" + strafeSuffix(playerCase), EMPTY_LARGE,
                    Rotation.NONE, TIMEOUT_TICKS, 0L, true, helper -> assertBrutalflyStrafe(helper, playerCase)));
        }
        return functions;
    }

    /** One filter site, one player case: the ENT-S-107 shape with the site's cites. */
    private static void assertFilterSite(GameTestHelper helper, FilterSite site, PlayerCase playerCase) {
        if (site.peacefulGated()) {
            helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                    "precondition: orig GammaMetroid.java:254 rejects everything on Peaceful; the game-test level runs at NORMAL"
                            + " (ENT-S-109 test setup)");
        }
        Mob hunter = null;
        ServerPlayer player = null;
        try {
            hunter = spawnFrozen(helper, site.hunter().get());
            if (hunter instanceof TamableAnimal tame) {
                helper.assertTrue(!tame.isTame(), "precondition: a fresh " + hunter.getClass().getSimpleName()
                        + " must be untamed so the orig filter reaches its player branch (orig GammaMetroid.java:278)"
                        + " (ENT-S-109 test setup)");
            }
            player = playerFor(helper, playerCase);
            assertOutcome(helper, hunter, player, playerCase, playerCase.prey, filterWhy(site, playerCase), "ENT-S-109");
        } finally {
            removePlayer(helper, player);
            if (hunter != null) hunter.discard();
        }
        helper.succeed();
    }

    private static String filterWhy(FilterSite site, PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "a creative player must be rejected (orig " + site.origCreative()
                    + " capabilities.isCreativeMode → Abilities.instabuild; port " + site.port() + ")";
            case SURVIVAL_INVULNERABLE -> "a SURVIVAL player with Abilities.invulnerable set is not creative (orig "
                    + site.origCreative() + " reads isCreativeMode only) and must stay prey (orig " + site.origPrey()
                    + "); the old invulnerable mapping at port " + site.port() + " rejected it";
            case SURVIVAL -> "control: a plain survival player must be prey (orig " + site.origPrey() + ")";
        };
    }

    private static String strafeSuffix(PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "creative_player_not_strafed";
            case SURVIVAL_INVULNERABLE -> "invulnerable_survival_player_still_strafed";
            case SURVIVAL -> "survival_player_strafed";
        };
    }

    private static String strafeWhy(PlayerCase playerCase) {
        return switch (playerCase) {
            case CREATIVE -> "a creative player is never strafed (orig Brutalfly.java:217 capabilities.isCreativeMode"
                    + " → Abilities.instabuild; port EntityBrutalfly.java:205)";
            case SURVIVAL_INVULNERABLE -> "a SURVIVAL player with Abilities.invulnerable set is not creative (orig"
                    + " Brutalfly.java:217 reads isCreativeMode only) and must still be strafed (orig :218-219); the old"
                    + " invulnerable mapping at port EntityBrutalfly.java:205 spared it";
            case SURVIVAL -> "control: a plain survival player is strafed (orig Brutalfly.java:218-219)";
        };
    }

    /**
     * orig Brutalfly.java:213-227, the 1-in-6 player strafe (port EntityBrutalfly.java:201-210):
     * the nearest player within 30 (orig :215 box 30 x 20 x 30; port :204 getNearestPlayer 30) is
     * strafed — the flight target set 4 above it (orig :219, port :206) and a fireball on a
     * 1-in-shoot roll (orig :220-221, port :207-208) — unless creative (orig :217
     * capabilities.isCreativeMode, port :205). Not a filter, so the branch is driven directly:
     * customServerAiStep is invoked once by reflection with the Brutalfly's random replaced (the
     * VortexParityTests.ForcedRoll seam KrakenTargetingParityTests uses) so that the reselection
     * roll nextInt(200) (port :150) answers 1, the strafe roll nextInt(6) (port :201) answers 0 and
     * the fire roll nextInt(shoot) (port :207) answers 1: the strafe block is entered exactly once
     * and no fireball is ever spawned. The flight target is parked 10 blocks above the Brutalfly
     * first (distSq 100, past the port :150 "&lt; 9" reselection) so nothing but the strafe can
     * move it; the strafe's mark — {@code player.blockPosition().above(4)} — is read back through
     * the private currentFlightTarget. The creative case must leave the parked target in place
     * (orig :224-226 nulls the creative target), the two survival cases must set the mark.
     */
    private static void assertBrutalflyStrafe(GameTestHelper helper, PlayerCase playerCase) {
        Mob brutalfly = null;
        ServerPlayer player = null;
        try {
            brutalfly = spawnFrozen(helper, ModEntities.ENTITY_BRUTALFLY.get());
            // port EntityBrutalfly.java:199 — the fire roll's bound follows the difficulty (orig :155,168-170).
            int shoot = helper.getLevel().getDifficulty() == Difficulty.HARD ? 2 : 3;
            replaceRandom(brutalfly, strafeRolls(shoot));
            BlockPos parked = brutalfly.blockPosition().above(10);
            writeObject(brutalfly, "currentFlightTarget", parked);
            player = playerFor(helper, playerCase);
            assertPlayerFlags(helper, player, playerCase);
            helper.assertTrue(brutalfly.hasLineOfSight(player),
                    "precondition: the Brutalfly must see the player 8 blocks away inside the barrier shell (ENT-S-109 test geometry)");
            helper.assertTrue(helper.getLevel().getNearestPlayer(brutalfly, 30.0) == player,
                    "precondition: the mock player must be the nearest player within 30 blocks (port EntityBrutalfly.java:204)"
                            + " (ENT-S-109 test setup)");
            invokeCustomServerAiStep(brutalfly);
            BlockPos after = (BlockPos) readObject(brutalfly, "currentFlightTarget");
            BlockPos mark = player.blockPosition().above(4);
            helper.assertTrue(after != null, "EntityBrutalfly.customServerAiStep left currentFlightTarget null (ENT-S-109)");
            if (playerCase.prey) {
                helper.assertTrue(mark.equals(after), "EntityBrutalfly strafe (" + playerCase + " player): " + strafeWhy(playerCase)
                        + " — expected the flight target at the strafe mark " + mark + ", got " + after + " (ENT-S-109)");
            } else {
                helper.assertTrue(parked.equals(after), "EntityBrutalfly strafe (" + playerCase + " player): " + strafeWhy(playerCase)
                        + " — expected the parked flight target " + parked + " untouched, got " + after + " (ENT-S-109)");
            }
        } finally {
            removePlayer(helper, player);
            if (brutalfly != null) brutalfly.discard();
        }
        helper.succeed();
    }

    /**
     * Entity random for one strafing customServerAiStep, port EntityBrutalfly.java:150 / :201 / :207:
     * nextInt(200) (reselection) 1, nextInt(6) (strafe) 0, nextInt(shoot) (fire) 1.
     */
    private static RandomSource strafeRolls(int shoot) {
        RandomSource rolls = RandomSource.create(1234L);
        rolls = new VortexParityTests.ForcedRoll(rolls, 200, 1);
        rolls = new VortexParityTests.ForcedRoll(rolls, 6, 0);
        return new VortexParityTests.ForcedRoll(rolls, shoot, 1);
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

    /** The Brutalfly's protected customServerAiStep (port EntityBrutalfly.java:131; orig Brutalfly.java:151 updateAITasks). */
    private static void invokeCustomServerAiStep(Mob brutalfly) {
        try {
            Method method = EntityBrutalfly.class.getDeclaredMethod("customServerAiStep");
            method.setAccessible(true);
            method.invoke(brutalfly);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("EntityBrutalfly.customServerAiStep threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke EntityBrutalfly.customServerAiStep", exception);
        }
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
            assertOutcome(helper, leon, player, playerCase, expected, why, "ENT-S-107");
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
            assertOutcome(helper, cephadrome, player, playerCase, expected, why, "ENT-S-107");
        } finally {
            removePlayer(helper, player);
            if (cephadrome != null) cephadrome.discard();
        }
        helper.succeed();
    }

    private static void assertOutcome(GameTestHelper helper, Mob hunter, ServerPlayer player, PlayerCase playerCase,
                                      boolean expected, String why, String finding) {
        String hunterName = hunter.getClass().getSimpleName();
        helper.assertTrue(hunter.hasLineOfSight(player),
                "precondition: the " + hunterName + " must see the player 8 blocks away inside the barrier shell"
                        + " (" + finding + " test geometry)");
        assertPlayerFlags(helper, player, playerCase);
        boolean actual = isSuitableTarget(hunter, player);
        helper.assertTrue(actual == expected, hunterName + ".isSuitableTarget(" + playerCase + " player): " + why
                + " — expected " + expected + ", got " + actual + " (" + finding + ")");
    }

    /** The two abilities flags of each player case, as {@link #playerFor} must have left them. */
    private static void assertPlayerFlags(GameTestHelper helper, ServerPlayer player, PlayerCase playerCase) {
        switch (playerCase) {
            case CREATIVE -> helper.assertTrue(player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: a creative player has both instabuild and invulnerable set (creative-mapping test setup)");
            case SURVIVAL_INVULNERABLE -> helper.assertTrue(!player.getAbilities().instabuild && player.getAbilities().invulnerable,
                    "precondition: the discriminating player is survival (instabuild clear) with invulnerable set by hand"
                            + " (creative-mapping test setup)");
            case SURVIVAL -> helper.assertTrue(!player.getAbilities().instabuild && !player.getAbilities().invulnerable,
                    "precondition: a plain survival player has neither flag (creative-mapping test setup)");
        }
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

    private static Object readObject(Mob mob, String name) {
        String owner = mob.getClass().getSimpleName();
        try {
            Field field = mob.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(mob);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + owner + "." + name, exception);
        }
    }

    private static void writeObject(Mob mob, String name, Object value) {
        String owner = mob.getClass().getSimpleName();
        try {
            Field field = mob.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(mob, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + owner + "." + name, exception);
        }
    }
}
