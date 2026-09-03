package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-110 (owner ruling 2026-09-04: "ENT-S-108 through 113: all parity, fix in classic"):
 * the untamed Leon's prey rule and the PlayNicely gate. orig Leon.java:387-428
 * {@code isSuitableTarget}: Peaceful refuses everything (:388-390); so does
 * {@code OreSpawnMain.PlayNicely != 0} (:391-393), ahead of the null / self / dead checks
 * (:394-402), the shared ignore screen (:403-405), line of sight (:406-408) and the
 * species chain -- no Leon (:409-411), every EntityMob (:412-414), a player unless creative
 * and then only for an untamed Leon (:415-421), and finally the untamed tail (:422-426):
 * {@code !isTamed() && MyUtils.isAttackableNonMob(target)} (orig MyUtils.java:77-115:
 * EntityMob, Mothra, Leon, Dragon, Spyro, the royalty, GammaMetroid, Cephadrome,
 * WaterDragon, Girlfriend, Boyfriend, EntityVillager, Stinky), everything else :427's
 * false. The port's filter had no PlayNicely gate and its untamed tail granted any living
 * thing -- a pig, a cow, a sheep -- so a wild Leonopteryx hunted the whole farm.
 *
 * <p>Six cases, each reached through the private {@code isSuitableTarget(LivingEntity)} by
 * reflection (the CreativeMappingParityTests / IgnoreScreenParityTests precedent): an
 * untamed Leon accepts a vanilla Villager (orig :422-425 with MyUtils.java:111) and rejects
 * a vanilla pig (:427; the port used to accept it -- the discriminating case); a tamed Leon
 * rejects the Villager (:422's gate); with PlayNicely raised, a Zombie that the :412
 * EntityMob rule grants with the flag down is refused (:391 -- read live, as the port reads
 * {@code OreSpawnConfig.PLAY_NICELY} at the site); and the ENT-S-107 player cases still
 * hold: a creative player is refused (:417), a survival player is prey of an untamed Leon
 * (:420).</p>
 *
 * <p>Own batch (TEST-003; the PlayNicely case flips the global flag, synchronously, and
 * restores it in a finally). Geometry as CreativeMappingParityTests: the Leon frozen on the
 * floor of the 48x16x48 empty_large (eye at rel 8.0, under the barrier ceiling at rel 17),
 * the candidate 8 blocks away on the same floor with line of sight asserted as a
 * precondition, spawns discarded in a finally. Mock players get an explicit game mode (the
 * game-test server defaults them to CREATIVE).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LeonTargetingTests {

    /** The Leon on the template floor. */
    private static final BlockPos LEON_POS = new BlockPos(20, 1, 24);
    /** The candidate 8 blocks away on the same floor, clear line of sight. */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    private static final Vec3 PLAYER_POS = new Vec3(28.5, 1.0, 24.5);

    // ------------------------------------------------------------------
    // The untamed tail -- orig Leon.java:422-427
    // ------------------------------------------------------------------

    /** orig Leon.java:422-425 with MyUtils.java:111: an untamed Leon takes a villager. */
    @GameTest(template = "empty_large", batch = "leonTargeting")
    public void s110_untamed_leon_accepts_a_villager_attackable_non_mob(GameTestHelper helper) {
        assertSpeciesCase(helper, EntityType.VILLAGER, "a vanilla Villager", false, true,
                "an untamed Leon must accept a villager: orig Leon.java:422-425 grants the untamed tail to"
                        + " MyUtils.isAttackableNonMob targets and orig MyUtils.java:111 lists EntityVillager");
    }

    /**
     * The discriminating case: orig Leon.java:427 -- a pig is no EntityMob, no player and not on
     * the attackable-non-mob list, so the untamed tail falls through to false; the port's old
     * tail ({@code !isTame() -> true}) accepted it.
     */
    @GameTest(template = "empty_large", batch = "leonTargeting")
    public void s110_untamed_leon_rejects_a_pig_outside_the_attackable_non_mobs(GameTestHelper helper) {
        assertSpeciesCase(helper, EntityType.PIG, "a vanilla pig", false, false,
                "an untamed Leon must reject a pig: not an EntityMob (orig Leon.java:412), not a player (:415), not on"
                        + " MyUtils.isAttackableNonMob (orig MyUtils.java:77-115), so :427 answers false; the port's old"
                        + " tail granted any living thing");
    }

    /** orig Leon.java:422: the tail is for an untamed Leon only -- a tamed one leaves the villager alone (:427). */
    @GameTest(template = "empty_large", batch = "leonTargeting")
    public void s110_tamed_leon_rejects_the_villager(GameTestHelper helper) {
        assertSpeciesCase(helper, EntityType.VILLAGER, "a vanilla Villager", true, false,
                "a tamed Leon must reject a villager: orig Leon.java:422 gates the attackable-non-mob tail on !isTamed(),"
                        + " so :427 answers false");
    }

    // ------------------------------------------------------------------
    // The PlayNicely gate -- orig Leon.java:391-393
    // ------------------------------------------------------------------

    /**
     * orig Leon.java:391-393: {@code PlayNicely != 0} refuses everything, ahead of the species
     * chain. A vanilla Zombie is the probe: with the flag down the :412 EntityMob rule grants it
     * (the control, asserted first); with the flag raised the same Leon and the same Zombie on
     * the same spot are refused, so :391 -- read live -- is the only thing that changed.
     */
    @GameTest(template = "empty_large", batch = "leonTargeting")
    public void s110_play_nicely_refuses_the_zombie_the_entity_mob_rule_grants(GameTestHelper helper) {
        assertNotPeaceful(helper);
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob leon = null;
        Mob zombie = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            leon = spawnUntamedLeon(helper);
            zombie = spawnFrozen(helper, EntityType.ZOMBIE, PREY_POS);
            helper.assertTrue(zombie instanceof Monster,
                    "precondition: a Zombie is a Monster, orig Leon.java:412's EntityMob (ENT-S-110 test setup)");
            assertSees(helper, leon, zombie, "a vanilla Zombie");
            helper.assertTrue(isSuitableTarget(leon, zombie),
                    "control: with playNicely off an untamed Leon must accept a Zombie (orig Leon.java:412-414 EntityMob -> true)"
                            + " (ENT-S-110)");
            OreSpawnConfig.PLAY_NICELY.set(true);
            helper.assertTrue(!isSuitableTarget(leon, zombie),
                    "with playNicely on the same Leon must refuse the same Zombie on the same spot: orig Leon.java:391-393"
                            + " (PlayNicely != 0 -> false) ahead of the species chain, read live (ENT-S-110)");
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            if (zombie != null) zombie.discard();
            if (leon != null) leon.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // The player branch still holds -- orig Leon.java:415-421 (ENT-S-107)
    // ------------------------------------------------------------------

    /** orig Leon.java:417-419: a creative player is never prey. */
    @GameTest(template = "empty_large", batch = "leonTargeting")
    public void s110_creative_player_still_rejected(GameTestHelper helper) {
        assertPlayerCase(helper, GameType.CREATIVE, false,
                "a creative player must still be rejected (orig Leon.java:417 isCreativeMode -> Abilities.instabuild, ENT-S-107)");
    }

    /** orig Leon.java:420: a survival player is prey of an untamed Leon, decided ahead of the untamed tail. */
    @GameTest(template = "empty_large", batch = "leonTargeting")
    public void s110_survival_player_still_prey(GameTestHelper helper) {
        assertPlayerCase(helper, GameType.SURVIVAL, true,
                "a plain survival player must still be prey of an untamed Leon (orig Leon.java:420 return !isTamed())");
    }

    // ------------------------------------------------------------------
    // Runners
    // ------------------------------------------------------------------

    private static void assertSpeciesCase(GameTestHelper helper, EntityType<? extends Mob> species, String speciesWhy,
                                          boolean tamed, boolean expected, String why) {
        assertNotPeaceful(helper);
        assertPlayNicelyOff(helper);
        Mob leon = null;
        Mob candidate = null;
        try {
            leon = spawnUntamedLeon(helper);
            if (tamed) {
                ((TamableAnimal) leon).setTame(true, false);
                helper.assertTrue(((TamableAnimal) leon).isTame(),
                        "precondition: the Leon must read as tamed after setTame (ENT-S-110 test setup)");
            }
            candidate = spawnFrozen(helper, species, PREY_POS);
            helper.assertTrue(!MyUtils.isIgnoreable(candidate), "precondition: " + speciesWhy
                    + " is not on the shared ignore list, so orig Leon.java:403 is not what decides (ENT-S-110 test setup)");
            helper.assertTrue(!(candidate instanceof Monster), "precondition: " + speciesWhy
                    + " is no Monster, so orig Leon.java:412 is not what decides (ENT-S-110 test setup)");
            assertSees(helper, leon, candidate, speciesWhy);
            boolean actual = isSuitableTarget(leon, candidate);
            helper.assertTrue(actual == expected, "EntityLeon.isSuitableTarget(" + speciesWhy + ", "
                    + (tamed ? "tamed" : "untamed") + " Leon): " + why + " -- expected " + expected + ", got " + actual
                    + " (ENT-S-110)");
        } finally {
            if (candidate != null) candidate.discard();
            if (leon != null) leon.discard();
        }
        helper.succeed();
    }

    private static void assertPlayerCase(GameTestHelper helper, GameType mode, boolean expected, String why) {
        assertNotPeaceful(helper);
        assertPlayNicelyOff(helper);
        Mob leon = null;
        ServerPlayer player = null;
        try {
            leon = spawnUntamedLeon(helper);
            player = playerAt(helper, mode, helper.absoluteVec(PLAYER_POS));
            helper.assertTrue(player.getAbilities().instabuild == (mode == GameType.CREATIVE),
                    "precondition: instabuild must follow the game mode " + mode + " (ENT-S-110 test setup)");
            assertSees(helper, leon, player, "the " + mode + " player");
            boolean actual = isSuitableTarget(leon, player);
            helper.assertTrue(actual == expected, "EntityLeon.isSuitableTarget(" + mode + " player): " + why
                    + " -- expected " + expected + ", got " + actual + " (ENT-S-110)");
        } finally {
            removePlayer(helper, player);
            if (leon != null) leon.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void assertNotPeaceful(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: orig Leon.java:388-390 refuses everything on Peaceful; the game-test level runs at NORMAL"
                        + " (ENT-S-110 test setup)");
    }

    private static void assertPlayNicelyOff(GameTestHelper helper) {
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                "precondition: playNicely must be off (orig Leon.java:391-393 refuses everything while nice); a batch-mate"
                        + " left it raised (ENT-S-110 test setup)");
    }

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity target, String what) {
        helper.assertTrue(hunter.hasLineOfSight(target),
                "precondition: the Leon (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor)"
                        + " must see " + what + " 8 blocks away inside the barrier shell (ENT-S-110 test geometry)");
    }

    /** A fresh Leon, frozen on the floor, untamed as spawned (orig Leon.java:420/:422 read {@code !isTamed()} = true). */
    private static Mob spawnUntamedLeon(GameTestHelper helper) {
        Mob leon = spawnFrozen(helper, ModEntities.ENTITY_LEON.get(), LEON_POS);
        helper.assertTrue(leon instanceof TamableAnimal tame && !tame.isTame(),
                "precondition: a fresh Leon must be untamed (ENT-S-110 test setup)");
        return leon;
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server
     * defaults to CREATIVE, GameTestServer.java:85). Health is raised so nothing incidental
     * can kill it. Deprecated mock-player factory tolerated the way CreativeMappingParityTests
     * and KrakenTargetingParityTests do.
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

    /** The Leon's private {@code isSuitableTarget(LivingEntity)} -- the port's one-arg shape of the orig two-arg method. */
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
}
