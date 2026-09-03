package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-101: the shared ignore list. orig MyUtils.java:117-152 names twelve
 * species — RockBase (:118), EntityAnt (:121), EntityButterfly (:124),
 * EntityMosquito (:127), Dragonfly (:130), Firefly (:133), Cricket (:136),
 * Cockateil (:139), Termite (:142), Ghost (:145), GhostSkelly (:148),
 * Elevator (:151) — and every OreSpawn hunter's {@code isSuitableTarget}
 * screens with it ahead of its own species chain. The port's list had
 * dropped six (EntityAnt, Dragonfly, Cricket, Cockateil, Termite, Elevator)
 * and added four (CaveFisher, Fairy, LunaMoth, Coin) without a record; the
 * owner ruled the 1.7.10 membership restored.
 *
 * <p>Two groups. (1) The list itself: one test per 1.7.10 member asserting
 * {@link MyUtils#isIgnoreable} on a spawned instance, and one per port
 * addition asserting the 1.7.10 answer — false for CaveFisher, Fairy and
 * Coin; TRUE for the LunaMoth, which {@code extends EntityButterfly} in both
 * trees (orig EntityLunaMoth.java:16-17), so the port's explicit entry was
 * redundant and its removal changes nothing. (2) One representative hunter
 * per membership change, reached through the hunter's private
 * {@code isSuitableTarget(LivingEntity)} by reflection (the tree's precedent
 * for private entity members — KrakenPlayNicelyGateTests, CrashReproTests):
 * a now-ignored species is rejected while a vanilla pig on the same spot
 * passes, so the ignore step is the only differentiator (each hunter chosen
 * accepts an animal with line of sight once past that step — none of the
 * seven has an on-ground rule, so the frozen spawn's ground flag is moot — and
 * names none of these species in its own chain); a now-hunted species passes
 * the whole filter — which it can only do through the ignore step — while a
 * Ghost (orig :145) on the same spot is still rejected, showing the step
 * live in that hunter.</p>
 *
 * <p>Synchronous, no global state touched. Own batch (TEST-003: a new test
 * class declares its own batch so the default batch's 50-test buckets do
 * not reshuffle). Every spawn sits on the floor of the 48x16x48 empty_large
 * inside the barrier shell, eyes well under the ceiling at rel 17; spawns are
 * frozen (goals stripped, noAi, persistence) and discarded in a finally.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class IgnoreListParityTests {

    /** List tests: mid-floor. */
    private static final BlockPos MEMBER_POS = new BlockPos(24, 1, 24);
    /** Hunter tests: hunter and prey 8 blocks apart on the floor, clear line of sight. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);

    // ------------------------------------------------------------------
    // (1) The shared list — orig MyUtils.java:117-152, one test per member
    // ------------------------------------------------------------------

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_rock_base_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ROCK_BASE.get(), true, "RockBase (orig MyUtils.java:118)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_ant_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_ANT.get(), true, "EntityAnt (orig MyUtils.java:121)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_butterfly_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_BUTTERFLY.get(), true, "EntityButterfly (orig MyUtils.java:124)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_mosquito_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_MOSQUITO.get(), true, "EntityMosquito (orig MyUtils.java:127)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_dragonfly_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_DRAGONFLY.get(), true, "Dragonfly (orig MyUtils.java:130)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_firefly_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.FIREFLY.get(), true, "Firefly (orig MyUtils.java:133)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_cricket_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_CRICKET.get(), true, "Cricket (orig MyUtils.java:136)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_cockateil_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.COCKATEIL.get(), true, "Cockateil (orig MyUtils.java:139)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_termite_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_TERMITE.get(), true, "Termite (orig MyUtils.java:142)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_ghost_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.GHOST.get(), true, "Ghost (orig MyUtils.java:145)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_ghost_skelly_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.GHOST_SKELLY.get(), true, "GhostSkelly (orig MyUtils.java:148)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_member_elevator_is_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ELEVATOR.get(), true, "Elevator (orig MyUtils.java:151)");
    }

    // ---- the four port additions, answered the 1.7.10 way ----

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_removed_cave_fisher_is_not_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.CAVE_FISHER.get(), false,
                "CaveFisher (a port addition; absent from orig MyUtils.java:117-152)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_removed_fairy_is_not_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.FAIRY.get(), false,
                "Fairy (a port addition; absent from orig MyUtils.java:117-152)");
    }

    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_removed_coin_is_not_ignoreable(GameTestHelper helper) {
        assertMembership(helper, ModEntities.COIN.get(), false,
                "Coin (a port addition; absent from orig MyUtils.java:117-152)");
    }

    /**
     * The one removal that flips nothing: EntityLunaMoth extends EntityButterfly in
     * both trees (orig EntityLunaMoth.java:16-17, port EntityLunaMoth.java:24), so the
     * 1.7.10 list caught it through :124 without naming it. The port's explicit entry
     * was redundant; dropping it must leave the moth ignoreable.
     */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_removed_luna_moth_still_ignoreable_through_butterfly(GameTestHelper helper) {
        assertMembership(helper, ModEntities.ENTITY_LUNA_MOTH.get(), true,
                "EntityLunaMoth (extends EntityButterfly, orig EntityLunaMoth.java:16-17 — caught by orig MyUtils.java:124)");
    }

    // ------------------------------------------------------------------
    // (2) One representative hunter per membership change
    // ------------------------------------------------------------------

    /** orig Scorpion.java:213-215 screens with the list; an ant on the ground is otherwise prey (:252 fallthrough). */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_scorpion_now_rejects_ant(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.ENTITY_SCORPION.get(), ModEntities.ENTITY_ANT.get(),
                "EntityAnt (orig MyUtils.java:121) at Scorpion.isSuitableTarget (orig Scorpion.java:213-215)");
    }

    /** orig Basilisk.java:394-396 screens with the list; a dragonfly with line of sight is otherwise prey. */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_basilisk_now_rejects_dragonfly(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.BASILISK.get(), ModEntities.ENTITY_DRAGONFLY.get(),
                "Dragonfly (orig MyUtils.java:130) at Basilisk.isSuitableTarget (orig Basilisk.java:394-396)");
    }

    /** orig Vortex.java:290-339 screens with the list; a cricket is in none of the Vortex's eleven explicit exclusions. */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_vortex_now_rejects_cricket(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.ENTITY_VORTEX.get(), ModEntities.ENTITY_CRICKET.get(),
                "Cricket (orig MyUtils.java:136) at Vortex.isSuitableTarget (orig Vortex.java:290-339)");
    }

    /** orig Mothra.java:437-439 screens with the list; a cockateil is in none of Mothra's explicit exclusions (:443-475). */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_mothra_now_rejects_cockateil(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.MOTHRA.get(), ModEntities.COCKATEIL.get(),
                "Cockateil (orig MyUtils.java:139) at Mothra.isSuitableTarget (orig Mothra.java:437-439)");
    }

    /** orig Alosaurus.java:192 screens with the list; the Alosaurus otherwise hunts everything living (:211). Not the Rotator: it names the Termite itself (orig Rotator.java:316). */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_alosaurus_now_rejects_termite(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.ALOSAURUS.get(), ModEntities.ENTITY_TERMITE.get(),
                "Termite (orig MyUtils.java:142) at Alosaurus.isSuitableTarget (orig Alosaurus.java:192)");
    }

    /** orig Rotator.java:304-306 screens with the list; the Elevator is in none of the Rotator's sixteen explicit exclusions (:316-364). */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_rotator_now_rejects_elevator(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.ENTITY_ROTATOR.get(), ModEntities.ELEVATOR.get(),
                "Elevator (orig MyUtils.java:151) at Rotator.isSuitableTarget (orig Rotator.java:304-306)");
    }

    /**
     * orig Brutalfly.java:427-429 screens with the list, then hunts Monsters and players
     * only: a CaveFisher (EntityMob, orig CaveFisher.java:36-37) passes the whole filter
     * once the ignore step lets it through. No Ghost control here — the Brutalfly
     * rejects an ambient creature either way, so it would show nothing.
     */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_brutalfly_now_hunts_cave_fisher(GameTestHelper helper) {
        assertHuntedByHunter(helper, ModEntities.ENTITY_BRUTALFLY.get(), ModEntities.CAVE_FISHER.get(), false,
                "CaveFisher (a port addition) at Brutalfly.isSuitableTarget (orig Brutalfly.java:427-429)");
    }

    /** orig Rotator.java:304-306: a Fairy is in none of the Rotator's explicit exclusions, so past the ignore step it is prey. */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_rotator_now_hunts_fairy(GameTestHelper helper) {
        assertHuntedByHunter(helper, ModEntities.ENTITY_ROTATOR.get(), ModEntities.FAIRY.get(), true,
                "Fairy (a port addition) at Rotator.isSuitableTarget (orig Rotator.java:304-306)");
    }

    /** orig Vortex.java:290-339: a Coin is in none of the Vortex's explicit exclusions, so past the ignore step it is prey. */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_vortex_now_hunts_coin(GameTestHelper helper) {
        assertHuntedByHunter(helper, ModEntities.ENTITY_VORTEX.get(), ModEntities.COIN.get(), true,
                "Coin (a port addition) at Vortex.isSuitableTarget (orig Vortex.java:290-339)");
    }

    /** The no-op removal seen from a hunter: the LunaMoth stays a butterfly (orig EntityLunaMoth.java:16-17), so orig Basilisk.java:394-396 still spares it. */
    @GameTest(template = "empty_large", batch = "ignoreListParity")
    public void s101_hunter_basilisk_still_rejects_luna_moth(GameTestHelper helper) {
        assertIgnoredByHunter(helper, ModEntities.BASILISK.get(), ModEntities.ENTITY_LUNA_MOTH.get(),
                "EntityLunaMoth (still an EntityButterfly, orig MyUtils.java:124) at Basilisk.isSuitableTarget (orig Basilisk.java:394-396)");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void assertMembership(GameTestHelper helper, EntityType<? extends Mob> type, boolean expected, String who) {
        Mob mob = null;
        try {
            mob = spawnFrozen(helper, type, MEMBER_POS);
            boolean actual = MyUtils.isIgnoreable(mob);
            helper.assertTrue(actual == expected, "MyUtils.isIgnoreable(" + who + ") must be " + expected
                    + " as in orig MyUtils.java:117-152, got " + actual + " (ENT-S-101)");
        } finally {
            if (mob != null) mob.discard();
        }
        helper.succeed();
    }

    /**
     * A now-ignored species: the hunter's filter rejects it, and a vanilla pig on the
     * same spot passes, so line of sight, distance and the rest of the chain are not
     * what rejected it — only the ignore step is.
     */
    private static void assertIgnoredByHunter(GameTestHelper helper, EntityType<? extends Mob> hunterType,
                                              EntityType<? extends Mob> speciesType, String why) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnFrozen(helper, hunterType, HUNTER_POS);
            species = spawnFrozen(helper, speciesType, PREY_POS);
            helper.assertTrue(MyUtils.isIgnoreable(species),
                    "precondition: " + why + " — the species must be on the shared list (ENT-S-101)");
            helper.assertTrue(hunter.hasLineOfSight(species),
                    "precondition: " + why + " — the hunter must see the species 8 blocks away inside the shell (ENT-S-101 test geometry)");
            boolean accepted = isSuitableTarget(hunter, species);
            helper.assertTrue(!accepted, why + ": a species on the shared ignore list must be rejected by the hunter's"
                    + " ignore step, but it was accepted (ENT-S-101)");
            species.discard();
            species = null;
            control = spawnFrozen(helper, EntityType.PIG, PREY_POS);
            helper.assertTrue(hunter.hasLineOfSight(control),
                    "precondition: " + why + " — the hunter must see the control pig (ENT-S-101 test geometry)");
            helper.assertTrue(isSuitableTarget(hunter, control), "control: " + why + " — a vanilla pig on the same"
                    + " spot must pass this hunter's filter, so the species was rejected by the ignore step alone (ENT-S-101)");
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
        helper.succeed();
    }

    /**
     * A now-hunted species: the hunter's filter accepts it, which it can only do by
     * passing the ignore step; with {@code ghostControl} a Ghost (orig MyUtils.java:145)
     * on the same spot is then rejected, showing that step live in this hunter.
     */
    private static void assertHuntedByHunter(GameTestHelper helper, EntityType<? extends Mob> hunterType,
                                             EntityType<? extends Mob> speciesType, boolean ghostControl, String why) {
        Mob hunter = null;
        Mob species = null;
        Mob control = null;
        try {
            hunter = spawnFrozen(helper, hunterType, HUNTER_POS);
            species = spawnFrozen(helper, speciesType, PREY_POS);
            helper.assertTrue(!MyUtils.isIgnoreable(species),
                    "precondition: " + why + " — the species must be off the shared list (ENT-S-101)");
            helper.assertTrue(hunter.hasLineOfSight(species),
                    "precondition: " + why + " — the hunter must see the species 8 blocks away inside the shell (ENT-S-101 test geometry)");
            boolean accepted = isSuitableTarget(hunter, species);
            helper.assertTrue(accepted, why + ": a species off the shared ignore list must pass the hunter's ignore step"
                    + " and, being otherwise fair game for this hunter, the whole filter — but it was rejected (ENT-S-101)");
            if (ghostControl) {
                species.discard();
                species = null;
                control = spawnFrozen(helper, ModEntities.GHOST.get(), PREY_POS);
                helper.assertTrue(hunter.hasLineOfSight(control),
                        "precondition: " + why + " — the hunter must see the control Ghost (ENT-S-101 test geometry)");
                helper.assertTrue(!isSuitableTarget(hunter, control), "control: " + why + " — a Ghost (orig MyUtils.java:145)"
                        + " on the same spot must still be rejected, so the ignore step is live in this hunter (ENT-S-101)");
            }
        } finally {
            if (control != null) control.discard();
            if (species != null) species.discard();
            if (hunter != null) hunter.discard();
        }
        helper.succeed();
    }

    /** Frozen inside the shell: goals stripped, noAi (the hunters fly and hunt from their AI steps), persistence set. */
    private static <E extends Mob> E spawnFrozen(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
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
}
