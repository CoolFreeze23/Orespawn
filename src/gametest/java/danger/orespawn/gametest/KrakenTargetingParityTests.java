package danger.orespawn.gametest;

import danger.orespawn.MobStats;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Kraken;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-100: the Kraken's targeting path against orig Kraken.java, the four
 * synchronous items of the owner's ruling ("fix the five; the recorded
 * convention stands" — KT-C, world-rand versus entity-rand on the roll sites,
 * stays as recorded and is what lets {@link VortexParityTests.ForcedRoll}
 * pin the rolls here). KT-D, the hold through death, is tick-driven and lives
 * in {@link KrakenHoldReleaseTests}.
 *
 * <ul>
 *   <li>KT-A — the player search (orig :962-973): {@code findNearestEntityWithinAABB}
 *       takes the NEAREST player of any game mode (:963) and the creative check
 *       nulls it at the call site (:965/:970-972, the WormSmall idiom, orig
 *       WormSmall.java:179-182), so a creative player standing nearer than a
 *       survival one shadows that survival player. The port had skipped
 *       creative players inside the scan.</li>
 *   <li>KT-B1 — {@code isSuitableTarget} (orig :1060-1128): the shared
 *       {@code MyUtils.isIgnoreable} screen (:1070) and the species chain —
 *       EntitySquid (:1086), AttackSquid (:1089), Kraken (:1092), Spyro
 *       (:1095), ridden Dragon / Cephadrome / Leon / ThePrinceTeen /
 *       ThePrinceAdult (:1098-1117, {@code riddenByEntity == null}, port
 *       {@code !isVehicle()}), EntityChicken (:1118), Chipmunk (:1121),
 *       StinkBug (:1124), Mothra (:1127). The port had only Kraken and
 *       Squid.</li>
 *   <li>KT-B2 — the player branch (orig :1076-1082): creative → false
 *       (:1078), then {@code !isFlying} (:1081) — {@code Abilities.flying},
 *       the port's own Dragon / Leon mapping, not {@code invulnerable}.</li>
 *   <li>KT-E — {@code mygetMaxHealth()} (orig :115-117) is
 *       {@code Kraken_stats.health}, i.e. {@code MobStats.KRAKEN.maxHealth()}
 *       (1000, orig OreSpawnMain.java:6515), not a hardcoded 3000: pinned as
 *       the value and at three of its threshold sites — hurt re-target
 *       (:1154, max/4), flee lift (:952, max/4) and far-away despawn (:884,
 *       max/2) — each at a health that separates the 1000 base from 3000.</li>
 * </ul>
 *
 * <p>Synchronous and reflection-driven (the tree's precedent for private
 * entity members: KrakenPlayNicelyGateTests on {@code findSomethingToAttack}
 * and {@code caught}); nothing in Kraken.java was widened. Own batch
 * (TEST-003). Geometry from KrakenPlayNicelyGateTests: every Kraken is frozen
 * with its feet on the floor of the 48x16x48 empty_large (eye at rel 13.75,
 * under the barrier ceiling at rel 17) and every target stands 5 or 10
 * blocks away on the same floor with clear line of sight, asserted as a
 * precondition. Nothing ticks, so mob targets have {@code onGround} set by
 * hand for the ground-or-water rule (orig :1083-1085). Mock players are the
 * ENT-S-097 kind; the game-test server defaults them to CREATIVE, so the
 * mode is always set explicitly.</p>
 *
 * <p>ENT-S-105 rides on the KT-A fixture: the tie rule of the :963 scan (orig
 * {@code World.func_72857_a}, 1.7.10 {@code ahb.class} bytecode {@code dcmpl; ifle}
 * — the candidate is replaced on {@code d1 <= d0}, so the LAST of two equidistant
 * players wins), pinned with two survival players at mirror positions and the
 * scan's own order read back rather than assumed.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KrakenTargetingParityTests {

    /** Feet on the template floor, eye at rel 13.75 — under empty_large's barrier ceiling at rel 17. */
    private static final BlockPos KRAKEN_POS = new BlockPos(24, 1, 24);
    /** Flee test only: mid-air so the ground scan (orig :922-931) finds the floor 8 below; no line of sight involved. */
    private static final BlockPos FLEE_KRAKEN_POS = new BlockPos(24, 8, 24);
    /** Mob targets: 5 blocks away on the floor, inside the 20/40/20 search box (orig :1134) with clear line of sight. */
    private static final BlockPos TARGET_POS = new BlockPos(29, 1, 24);
    /** Players: the nearer one 5 blocks away, the farther one 10, both inside the 25/40/25 box (orig :963). */
    private static final Vec3 NEAR_PLAYER_POS = new Vec3(29.5, 1.0, 24.5);
    private static final Vec3 FAR_PLAYER_POS = new Vec3(34.5, 1.0, 24.5);
    /** ENT-S-105: NEAR_PLAYER_POS mirrored across the Kraken's x (24.5) — 5 blocks west, same y and z, so the two squared distances are bit-identical (25.0). */
    private static final Vec3 MIRROR_PLAYER_POS = new Vec3(19.5, 1.0, 24.5);
    /** A flight target no search would ever produce, to see whether a search wrote one. */
    private static final BlockPos SENTINEL_TARGET = new BlockPos(-100000, -100000, -100000);
    /** orig Kraken.java:952/:1154 divide by 4, :884 by 2, :954 by 8: the healths below straddle the 1000 base's quarter (250) and half (500) but not 3000's (750 / 1500). */
    private static final float BASE = (float) MobStats.KRAKEN.maxHealth();

    // ------------------------------------------------------------------
    // KT-A — player search, orig Kraken.java:962-973
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:963 {@code func_72857_a(EntityPlayer.class, box, this)}: the
     * nearest player, whatever its mode. A creative player 5 blocks away and a
     * survival player 10 blocks away — the search must answer the creative one.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_a_player_search_takes_nearest_of_any_mode(GameTestHelper helper) {
        Kraken kraken = null;
        ServerPlayer creative = null;
        ServerPlayer survival = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            creative = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(NEAR_PLAYER_POS));
            survival = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(FAR_PLAYER_POS));
            helper.assertTrue(creative.getAbilities().instabuild && !survival.getAbilities().instabuild,
                    "precondition: the near player must be creative and the far one survival (ENT-S-100 test setup)");
            helper.assertTrue(kraken.distanceToSqr(creative) < kraken.distanceToSqr(survival),
                    "precondition: the creative player must be the nearer one (ENT-S-100 test geometry)");
            Player found = findNearestPlayer(kraken);
            helper.assertTrue(found == creative, "the player search must take the NEAREST player regardless of game"
                    + " mode (orig Kraken.java:963); the creative check belongs to the call site (:965/:970-972)."
                    + " Expected the creative player, got " + found + " (ENT-S-100 KT-A)");
        } finally {
            removePlayer(helper, creative);
            removePlayer(helper, survival);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * orig Kraken.java:962-973 end to end: with the creative player nearest, the
     * search nulls it (:970-972) and — the :974 fallback roll forced to miss — hunts
     * nobody: no flight target written, nothing caught. Then the very same nearest
     * player switched to survival is hunted (:965-969: flight target = its position
     * +15). A search that skips creative players inside the scan would have hunted
     * the farther survival player in the first phase.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_a_creative_nearest_shadows_farther_survival(GameTestHelper helper) {
        Kraken kraken = null;
        ServerPlayer nearest = null;
        ServerPlayer farther = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            // orig :974 `target == null && nextInt(2) == 0` — the findSomethingToAttack fallback never runs here.
            replaceRandom(kraken, new VortexParityTests.ForcedRoll(RandomSource.create(1234L), 2, 1));
            nearest = playerAt(helper, GameType.CREATIVE, helper.absoluteVec(NEAR_PLAYER_POS));
            farther = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(FAR_PLAYER_POS));
            helper.assertTrue(kraken.hasLineOfSight(nearest) && kraken.hasLineOfSight(farther),
                    "precondition: the Kraken (eye rel 13.75) must see both players inside the barrier shell"
                            + " (ceiling at rel 17 in empty_large) (ENT-S-100 test geometry)");
            helper.assertTrue(kraken.distanceToSqr(nearest) < kraken.distanceToSqr(farther),
                    "precondition: the creative player must be the nearer one (ENT-S-100 test geometry)");
            setFlightTarget(kraken, SENTINEL_TARGET);

            searchForPrey(kraken);
            BlockPos afterCreative = flightTargetOf(kraken);
            helper.assertTrue(SENTINEL_TARGET.equals(afterCreative) && caughtOf(kraken) == null,
                    "a creative player nearer than a survival one must shadow it: the nearest player is taken"
                            + " and nulled for being creative (orig Kraken.java:963-972), so nothing is hunted."
                            + " flightTarget=" + afterCreative + " caught=" + caughtOf(kraken) + " (ENT-S-100 KT-A)");

            nearest.setGameMode(GameType.SURVIVAL);
            helper.assertTrue(!nearest.getAbilities().instabuild,
                    "precondition: the nearest player must now be survival (ENT-S-100 test setup)");
            searchForPrey(kraken);
            BlockPos expected = new BlockPos((int) nearest.getX(), (int) nearest.getY() + 15, (int) nearest.getZ());
            BlockPos afterSurvival = flightTargetOf(kraken);
            helper.assertTrue(expected.equals(afterSurvival),
                    "the same nearest player, now survival, must be hunted (orig Kraken.java:965-969: flight"
                            + " target = player +15): expected " + expected + ", got " + afterSurvival + " (ENT-S-100 KT-A)");
        } finally {
            removePlayer(helper, nearest);
            removePlayer(helper, farther);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // ENT-S-105 — ties in the player search, orig World.func_72857_a (the :963 scan)
    // ------------------------------------------------------------------

    /**
     * ENT-S-105, orig {@code World.func_72857_a} — the scan behind Kraken.java:963
     * (1.7.10 {@code ahb.a(Class, AxisAlignedBB, Entity)}): the candidate is
     * replaced on {@code d1 <= d0} (bytecode {@code dcmpl; ifle}, the update body
     * runs when {@code d1 <= d0}), so of two players at the same distance the LAST
     * one scanned wins; the port's old {@code <} kept the first. Two survival
     * players 5 blocks west and 5 blocks east of the Kraken, same y and z, so the
     * squared distances are bit-identical (asserted). Which of the two the scan
     * meets last is the entity section storage's business, not spawn order's, so
     * the order is read back with the scan's own call ({@link #scanOrder}) and the
     * winner must be the later of the two in that list.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s105_equidistant_players_last_scanned_wins(GameTestHelper helper) {
        Kraken kraken = null;
        ServerPlayer west = null;
        ServerPlayer east = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            west = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(MIRROR_PLAYER_POS));
            east = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(NEAR_PLAYER_POS));
            helper.assertTrue(!west.getAbilities().instabuild && !east.getAbilities().instabuild,
                    "precondition: both players must be survival (ENT-S-105 test setup)");
            double westSq = kraken.distanceToSqr(west);
            double eastSq = kraken.distanceToSqr(east);
            helper.assertTrue(westSq == eastSq,
                    "precondition: mirror positions must give bit-identical squared distances: west " + westSq
                            + ", east " + eastSq + " (ENT-S-105 test geometry)");
            List<Player> order = scanOrder(helper, kraken);
            int westAt = order.indexOf(west);
            int eastAt = order.indexOf(east);
            helper.assertTrue(order.size() == 2 && westAt >= 0 && eastAt >= 0,
                    "precondition: the scan's list must hold exactly the two players, got "
                            + describe(order, kraken) + " (ENT-S-105 test setup)");
            ServerPlayer last = westAt > eastAt ? west : east;
            Player found = findNearestPlayer(kraken);
            helper.assertTrue(found == last,
                    "of two equidistant players the LAST one scanned must win (orig World.func_72857_a replaces its"
                            + " candidate on d1 <= d0, bytecode dcmpl; ifle — the port's old `<` kept the first):"
                            + " scan order " + describe(order, kraken) + ", expected " + describe(last, kraken)
                            + ", got " + describe(found, kraken) + " (ENT-S-105)");
        } finally {
            removePlayer(helper, west);
            removePlayer(helper, east);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * ENT-S-105 control: with the distances unequal the nearer player wins whichever
     * end of the scan it sits at — 5 blocks against 10, then the two swapped in
     * place so the other entity is the nearer one. The scan order is read back the
     * same way and reported, not assumed.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s105_control_unequal_distances_nearest_wins(GameTestHelper helper) {
        Kraken kraken = null;
        ServerPlayer first = null;
        ServerPlayer second = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            first = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(NEAR_PLAYER_POS));
            second = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(FAR_PLAYER_POS));
            helper.assertTrue(!first.getAbilities().instabuild && !second.getAbilities().instabuild,
                    "precondition: both players must be survival (ENT-S-105 test setup)");
            helper.assertTrue(kraken.distanceToSqr(first) < kraken.distanceToSqr(second),
                    "precondition: the first player must be the nearer one (ENT-S-105 test geometry)");
            Player found = findNearestPlayer(kraken);
            helper.assertTrue(found == first,
                    "control: with unequal distances the NEARER player wins (orig World.func_72857_a): scan order "
                            + describe(scanOrder(helper, kraken), kraken) + ", got " + describe(found, kraken)
                            + " (ENT-S-105)");

            Vec3 near = helper.absoluteVec(NEAR_PLAYER_POS);
            Vec3 far = helper.absoluteVec(FAR_PLAYER_POS);
            first.teleportTo(helper.getLevel(), far.x, far.y, far.z, 0.0f, 0.0f);
            second.teleportTo(helper.getLevel(), near.x, near.y, near.z, 0.0f, 0.0f);
            helper.assertTrue(kraken.distanceToSqr(second) < kraken.distanceToSqr(first),
                    "precondition: after the swap the second player must be the nearer one (ENT-S-105 test geometry)");
            found = findNearestPlayer(kraken);
            helper.assertTrue(found == second,
                    "control: after the swap the other, now nearer, player wins (orig World.func_72857_a): scan order "
                            + describe(scanOrder(helper, kraken), kraken) + ", got " + describe(found, kraken)
                            + " (ENT-S-105)");
        } finally {
            removePlayer(helper, first);
            removePlayer(helper, second);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // KT-B1 — isSuitableTarget species chain, orig Kraken.java:1060-1128
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:1070-1072 {@code MyUtils.isIgnoreable}: a Ghost (orig
     * MyUtils.java:145) and an ant (:121, restored by ENT-S-101) are rejected; a pig
     * on the same spot passes (the :1127 fallthrough), so it is the shared list and
     * not geometry that rejected them.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_shared_ignore_list_screens_prey(GameTestHelper helper) {
        Kraken kraken = null;
        Mob ghost = null;
        Mob ant = null;
        Mob control = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            ghost = groundedAt(helper, ModEntities.GHOST.get(), TARGET_POS);
            helper.assertTrue(MyUtils.isIgnoreable(ghost) && kraken.hasLineOfSight(ghost),
                    "precondition: the Ghost must be on the shared list and in sight (ENT-S-100 test setup)");
            helper.assertTrue(!isSuitableTarget(kraken, ghost), "a Ghost is on the shared ignore list (orig"
                    + " MyUtils.java:145) and must be rejected by orig Kraken.java:1070-1072 (ENT-S-100 KT-B1)");
            ghost.discard();
            ghost = null;
            ant = groundedAt(helper, ModEntities.ENTITY_ANT.get(), TARGET_POS);
            helper.assertTrue(MyUtils.isIgnoreable(ant) && kraken.hasLineOfSight(ant),
                    "precondition: the ant must be on the shared list and in sight (ENT-S-100 test setup)");
            helper.assertTrue(!isSuitableTarget(kraken, ant), "an ant is on the shared ignore list (orig"
                    + " MyUtils.java:121, ENT-S-101) and must be rejected by orig Kraken.java:1070-1072 (ENT-S-100 KT-B1)");
            ant.discard();
            ant = null;
            control = groundedAt(helper, EntityType.PIG, TARGET_POS);
            helper.assertTrue(kraken.hasLineOfSight(control) && isSuitableTarget(kraken, control),
                    "control: a grounded pig on the same spot must pass (orig Kraken.java:1083-1085 ground rule,"
                            + " :1127 fallthrough) (ENT-S-100)");
        } finally {
            if (control != null) control.discard();
            if (ant != null) ant.discard();
            if (ghost != null) ghost.discard();
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_squid_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, EntityType.SQUID, "EntitySquid (orig Kraken.java:1086-1088)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_attack_squid_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, ModEntities.ATTACK_SQUID.get(), "AttackSquid (orig Kraken.java:1089-1091)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_other_kraken_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, ModEntities.KRAKEN.get(), "Kraken (orig Kraken.java:1092-1094)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_spyro_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, ModEntities.ENTITY_SPYRO.get(), "Spyro (orig Kraken.java:1095-1097)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_chicken_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, EntityType.CHICKEN, "EntityChicken (orig Kraken.java:1118-1120)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_chipmunk_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, ModEntities.CHIPMUNK.get(), "Chipmunk (orig Kraken.java:1121-1123)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_stink_bug_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, ModEntities.ENTITY_STINK_BUG.get(), "StinkBug (orig Kraken.java:1124-1126)");
    }

    /** Mothra is a butterfly in both trees (orig Mothra.java:50-51), so the shared list (:1070) spares it before :1127 does; rejected either way. */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_mothra_rejected(GameTestHelper helper) {
        assertSpeciesRejected(helper, ModEntities.MOTHRA.get(), "Mothra (orig Kraken.java:1127; also orig MyUtils.java:124 through EntityButterfly)");
    }

    // ---- the five mounts: riddenByEntity == null → prey, ridden → spared ----

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_dragon_unridden_accepted_ridden_rejected(GameTestHelper helper) {
        assertRiddenRule(helper, ModEntities.DRAGON.get(), "Dragon (orig Kraken.java:1098-1101)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_cephadrome_unridden_accepted_ridden_rejected(GameTestHelper helper) {
        assertRiddenRule(helper, ModEntities.CEPHADROME.get(), "Cephadrome (orig Kraken.java:1102-1105)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_leon_unridden_accepted_ridden_rejected(GameTestHelper helper) {
        assertRiddenRule(helper, ModEntities.ENTITY_LEON.get(), "Leon (orig Kraken.java:1106-1109)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_prince_teen_unridden_accepted_ridden_rejected(GameTestHelper helper) {
        assertRiddenRule(helper, ModEntities.THE_PRINCE_TEEN.get(), "ThePrinceTeen (orig Kraken.java:1110-1113)");
    }

    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b1_prince_adult_unridden_accepted_ridden_rejected(GameTestHelper helper) {
        assertRiddenRule(helper, ModEntities.THE_PRINCE_ADULT.get(), "ThePrinceAdult (orig Kraken.java:1114-1117)");
    }

    // ------------------------------------------------------------------
    // KT-B2 — the player branch, orig Kraken.java:1076-1082
    // ------------------------------------------------------------------

    /**
     * orig Kraken.java:1076-1082: a survival player on its feet is prey; the same
     * player flying ({@code capabilities.isFlying}, :1081 — {@code Abilities.flying})
     * is not; back on its feet it is prey again; and creative (:1078-1080) never is.
     * A survival player is not {@code invulnerable}, so the port's old
     * {@code !invulnerable} mapping would have accepted the flying one.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_b2_flying_survival_player_rejected(GameTestHelper helper) {
        Kraken kraken = null;
        ServerPlayer player = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(NEAR_PLAYER_POS));
            helper.assertTrue(kraken.hasLineOfSight(player),
                    "precondition: the Kraken must see the player 5 blocks away inside the shell (ENT-S-100 test geometry)");
            helper.assertTrue(!player.getAbilities().instabuild && !player.getAbilities().flying,
                    "precondition: a survival player on its feet (ENT-S-100 test setup)");
            helper.assertTrue(isSuitableTarget(kraken, player),
                    "a survival player on its feet must be prey (orig Kraken.java:1076-1082) (ENT-S-100 KT-B2)");

            player.getAbilities().flying = true;
            helper.assertTrue(!isSuitableTarget(kraken, player),
                    "a FLYING survival player must be rejected: orig Kraken.java:1081 `return !isFlying`, which maps to"
                            + " Abilities.flying (the port's own Dragon and Leon sites), not to invulnerable (ENT-S-100 KT-B2)");

            player.getAbilities().flying = false;
            helper.assertTrue(isSuitableTarget(kraken, player),
                    "back on its feet the survival player must be prey again (orig Kraken.java:1081) (ENT-S-100 KT-B2)");

            player.setGameMode(GameType.CREATIVE);
            helper.assertTrue(player.getAbilities().instabuild,
                    "precondition: the player must now be creative (ENT-S-100 test setup)");
            helper.assertTrue(!isSuitableTarget(kraken, player),
                    "a creative player must be rejected (orig Kraken.java:1078-1080) (ENT-S-100 KT-B2)");
        } finally {
            removePlayer(helper, player);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // KT-E — mygetMaxHealth and its thresholds, orig Kraken.java:115-117
    // ------------------------------------------------------------------

    /** orig Kraken.java:115-117 {@code Kraken_stats.health}: the table value (orig OreSpawnMain.java:6515, 1000), and the same base the attribute (:89) was built from. */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_e_my_get_max_health_reads_mob_stats(GameTestHelper helper) {
        Kraken kraken = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            int mine = kraken.mygetMaxHealth();
            helper.assertTrue(mine == (int) MobStats.KRAKEN.maxHealth(),
                    "mygetMaxHealth() must be MobStats.KRAKEN.maxHealth() (orig Kraken.java:115-117 Kraken_stats.health,"
                            + " orig OreSpawnMain.java:6515), got " + mine + " (ENT-S-100 KT-E)");
            helper.assertTrue(mine == (int) kraken.getMaxHealth(),
                    "mygetMaxHealth() must agree with the MAX_HEALTH attribute (orig Kraken.java:89 sets the attribute"
                            + " from the same value): " + mine + " vs " + kraken.getMaxHealth() + " (ENT-S-100 KT-E)");
        } finally {
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * orig Kraken.java:1154-1157: a player's hit re-targets the Kraken onto the
     * attacker (+15) and sets hit_by_player only while health &gt; max/4. At 20% of
     * the base nothing happens; at 50% it must — 500 is above 1000/4 = 250 but below
     * 3000/4 = 750, so the old base would have stayed put. The re-target check runs
     * before the 30-tick hurt timer (:1158-1161), so the second hit is seen even
     * though its damage is refused.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_e_hurt_retarget_threshold_is_quarter_of_mob_stats(GameTestHelper helper) {
        Kraken kraken = null;
        ServerPlayer player = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            player = playerAt(helper, GameType.SURVIVAL, helper.absoluteVec(NEAR_PLAYER_POS));
            setFlightTarget(kraken, SENTINEL_TARGET); // orig :1154 needs a flight target to overwrite
            DamageSource byPlayer = helper.getLevel().damageSources().playerAttack(player);

            kraken.setHealth(BASE * 0.2f);
            kraken.hurt(byPlayer, 1.0f);
            helper.assertTrue(!hitByPlayerOf(kraken) && SENTINEL_TARGET.equals(flightTargetOf(kraken)),
                    "at 20% health (below max/4) a player's hit must neither re-target nor set hit_by_player"
                            + " (orig Kraken.java:1154): hitByPlayer=" + hitByPlayerOf(kraken) + " flightTarget="
                            + flightTargetOf(kraken) + " (ENT-S-100 KT-E)");

            kraken.setHealth(BASE * 0.5f);
            kraken.hurt(byPlayer, 1.0f);
            BlockPos expected = new BlockPos((int) player.getX(), (int) player.getY() + 15, (int) player.getZ());
            helper.assertTrue(hitByPlayerOf(kraken) && expected.equals(flightTargetOf(kraken)),
                    "at 50% of MobStats' 1000 (above max/4 = 250; a 3000 base's quarter, 750, would not) a player's hit"
                            + " must re-target onto the attacker +15 and set hit_by_player (orig Kraken.java:1154-1157):"
                            + " hitByPlayer=" + hitByPlayerOf(kraken) + " expected " + expected + ", got "
                            + flightTargetOf(kraken) + " (ENT-S-100 KT-E)");
        } finally {
            removePlayer(helper, player);
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * orig Kraken.java:952-953: below max/4 (or once long_enough runs out) the freshly
     * picked flight target is lifted 30. With the pick's rolls forced (:933-935
     * {@code nextInt(6)}, {@code nextInt(2)}, :945 {@code nextInt(9)} all 0) the two
     * picks differ only by that lift: none at 50% of the base, 30 at 20%. A 3000 base
     * (quarter 750) would lift both, difference 0.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_e_flee_lift_threshold_is_quarter_of_mob_stats(GameTestHelper helper) {
        Kraken kraken = null;
        try {
            kraken = spawnFrozen(helper, FLEE_KRAKEN_POS);
            replaceRandom(kraken, pickRolls());
            helper.assertTrue(kraken.getY() < 200.0,
                    "precondition: orig Kraken.java:952 gates the lift on y < 200 (ENT-S-100 test geometry)");

            kraken.setHealth(BASE * 0.5f);
            pickNewFlightTarget(kraken);
            BlockPos steady = flightTargetOf(kraken);
            kraken.setHealth(BASE * 0.2f);
            pickNewFlightTarget(kraken);
            BlockPos fleeing = flightTargetOf(kraken);
            helper.assertTrue(steady != null && fleeing != null,
                    "precondition: pickNewFlightTarget must write a flight target (ENT-S-100)");
            helper.assertTrue(fleeing.getX() == steady.getX() && fleeing.getZ() == steady.getZ()
                            && fleeing.getY() - steady.getY() == 30,
                    "the flee lift (orig Kraken.java:952-953, +30 below max/4) must apply at 20% of MobStats' 1000"
                            + " and not at 50% (a 3000 base's quarter, 750, would lift both): steady=" + steady
                            + " fleeing=" + fleeing + " (ENT-S-100 KT-E)");
        } finally {
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * orig Kraken.java:877-889 {@code canDespawn}: not persistence-required, long_enough
     * still running, y &gt; 150 and health &lt; max/2 → despawnable (:884). At 70% of the
     * base it is not (700 &gt; 500 — a 3000 base's half, 1500, would despawn it), at
     * 40% it is, and at y 100 the health rule does not apply. The Kraken is added by
     * hand (GameTestHelper.spawn marks every mob persistence-required, the
     * VortexParityTests control pattern) inside the shell, then moved for the y
     * checks; nothing ticks and it is discarded in the finally.
     */
    @GameTest(template = "empty_large", batch = "krakenTargetingParity")
    public void s100_kt_e_far_away_despawn_threshold_is_half_of_mob_stats(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Kraken kraken = null;
        try {
            kraken = ModEntities.KRAKEN.get().create(level);
            helper.assertTrue(kraken != null, "precondition: the Kraken type must construct (ENT-S-100)");
            Vec3 inside = helper.absoluteVec(Vec3.atBottomCenterOf(FLEE_KRAKEN_POS));
            kraken.moveTo(inside.x, inside.y, inside.z, 0.0f, 0.0f);
            kraken.setNoAi(true);
            helper.assertTrue(level.addFreshEntity(kraken), "precondition: the Kraken must be added to the level (ENT-S-100)");
            helper.assertTrue(!kraken.isPersistenceRequired(),
                    "precondition: a hand-added Kraken must not be persistence-required (orig Kraken.java:878) (ENT-S-100)");

            kraken.moveTo(inside.x, 160.0, inside.z, 0.0f, 0.0f); // orig :884 y > 150
            kraken.setHealth(BASE * 0.7f);
            helper.assertTrue(!kraken.removeWhenFarAway(0.0),
                    "at 70% of MobStats' 1000 (above max/2 = 500; a 3000 base's half, 1500, would not) a Kraken above"
                            + " y 150 must NOT be far-away-despawnable (orig Kraken.java:884) (ENT-S-100 KT-E)");
            kraken.setHealth(BASE * 0.4f);
            helper.assertTrue(kraken.removeWhenFarAway(0.0),
                    "at 40% (below max/2) a Kraken above y 150 must be far-away-despawnable (orig Kraken.java:884)"
                            + " (ENT-S-100 KT-E)");
            kraken.moveTo(inside.x, 100.0, inside.z, 0.0f, 0.0f);
            helper.assertTrue(!kraken.removeWhenFarAway(0.0),
                    "below y 150 the health rule does not apply (orig Kraken.java:884-889) (ENT-S-100 KT-E)");
        } finally {
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * A grounded instance of {@code type} 5 blocks from a frozen Kraken with line of
     * sight: the species chain must reject it, and a vanilla pig on the same spot
     * must pass — so geometry, line of sight and the ground rule (orig :1083-1085)
     * are not what rejected it.
     */
    private static void assertSpeciesRejected(GameTestHelper helper, EntityType<? extends Mob> type, String who) {
        Kraken kraken = null;
        Mob target = null;
        Mob control = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            target = groundedAt(helper, type, TARGET_POS);
            helper.assertTrue(kraken.hasLineOfSight(target),
                    "precondition: the Kraken (eye rel 13.75) must see " + who + " 5 blocks away inside the barrier"
                            + " shell (ceiling at rel 17 in empty_large) (ENT-S-100 test geometry)");
            helper.assertTrue(!isSuitableTarget(kraken, target),
                    who + " must be rejected by isSuitableTarget's species chain (orig Kraken.java:1086-1127) (ENT-S-100 KT-B1)");
            target.discard();
            target = null;
            control = groundedAt(helper, EntityType.PIG, TARGET_POS);
            helper.assertTrue(kraken.hasLineOfSight(control) && isSuitableTarget(kraken, control),
                    "control: a grounded pig on the same spot must pass (orig Kraken.java:1083-1085 ground rule,"
                            + " :1127 fallthrough), so " + who + " was rejected by its own species check (ENT-S-100)");
        } finally {
            if (control != null) control.discard();
            if (target != null) target.discard();
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    /**
     * orig Kraken.java:1098-1117 {@code return c.riddenByEntity == null}: the mount is
     * prey while nobody rides it and spared as soon as something does (port
     * {@code !isVehicle()}). A pig is forced aboard with {@code startRiding(mount, true)}.
     */
    private static void assertRiddenRule(GameTestHelper helper, EntityType<? extends Mob> type, String who) {
        Kraken kraken = null;
        Mob mount = null;
        Mob rider = null;
        try {
            kraken = spawnFrozen(helper, KRAKEN_POS);
            mount = groundedAt(helper, type, TARGET_POS);
            helper.assertTrue(kraken.hasLineOfSight(mount),
                    "precondition: the Kraken must see " + who + " 5 blocks away inside the shell (ENT-S-100 test geometry)");
            helper.assertTrue(!mount.isVehicle(), "precondition: " + who + " must start unridden (ENT-S-100 test setup)");
            helper.assertTrue(isSuitableTarget(kraken, mount),
                    who + " unridden must be prey (riddenByEntity == null → true) (ENT-S-100 KT-B1)");
            rider = groundedAt(helper, EntityType.PIG, TARGET_POS);
            helper.assertTrue(rider.startRiding(mount, true) && mount.isVehicle(),
                    "precondition: the pig must mount " + who + " (ENT-S-100 test setup)");
            helper.assertTrue(!isSuitableTarget(kraken, mount),
                    who + " ridden must be spared (riddenByEntity != null → false; port !isVehicle()) (ENT-S-100 KT-B1)");
        } finally {
            if (rider != null) {
                rider.stopRiding();
                rider.discard();
            }
            if (mount != null) mount.discard();
            if (kraken != null) kraken.discard();
        }
        helper.succeed();
    }

    private static Kraken spawnFrozen(GameTestHelper helper, BlockPos pos) {
        Kraken kraken = helper.spawnWithNoFreeWill(ModEntities.KRAKEN.get(), pos);
        kraken.setNoAi(true);
        kraken.setPersistenceRequired();
        return kraken;
    }

    /**
     * A frozen mob target on the floor. Nothing ticks in these tests, so {@code onGround}
     * — normally set by travel — is set by hand for orig Kraken.java:1083-1085
     * ({@code !onGround && !isInWater → false}).
     */
    private static <E extends Mob> E groundedAt(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setOnGround(true);
        return mob;
    }

    /**
     * A mock ServerPlayer in the level with an explicit game mode (the game-test server
     * defaults to CREATIVE, GameTestServer.java:85). Health is raised so nothing
     * incidental can kill it. Deprecated mock-player factory tolerated the way
     * KrakenPlayNicelyGateTests and EntityLogicTestsA do.
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

    /**
     * Entity random for the flight-target pick, orig Kraken.java:933-945: {@code nextInt(6)}
     * (x/z reach) 0, {@code nextInt(2)} (sign flips) 0, {@code nextInt(9)} (y jitter) 0 —
     * every pick lands on the same candidate, so two picks differ only by the :953 lift.
     */
    private static RandomSource pickRolls() {
        RandomSource rolls = RandomSource.create(1234L);
        rolls = new VortexParityTests.ForcedRoll(rolls, 6, 0);
        rolls = new VortexParityTests.ForcedRoll(rolls, 2, 0);
        return new VortexParityTests.ForcedRoll(rolls, 9, 0);
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

    private static Object readField(Kraken kraken, String name) {
        try {
            Field field = Kraken.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(kraken);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read Kraken." + name, exception);
        }
    }

    private static void writeField(Kraken kraken, String name, Object value) {
        try {
            Field field = Kraken.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(kraken, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write Kraken." + name, exception);
        }
    }

    private static Object invoke(Kraken kraken, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = Kraken.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(kraken, args);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Kraken." + name + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke Kraken." + name, exception);
        }
    }

    /** orig Kraken.java:60 {@code caught}. */
    private static LivingEntity caughtOf(Kraken kraken) {
        return (LivingEntity) readField(kraken, "caught");
    }

    /** orig Kraken.java:59 {@code currentFlightTarget} (ChunkCoordinates there, BlockPos here). */
    private static BlockPos flightTargetOf(Kraken kraken) {
        return (BlockPos) readField(kraken, "currentFlightTarget");
    }

    private static void setFlightTarget(Kraken kraken, BlockPos target) {
        writeField(kraken, "currentFlightTarget", target);
    }

    /** orig Kraken.java:66 {@code hit_by_player}. */
    private static boolean hitByPlayerOf(Kraken kraken) {
        return (Boolean) readField(kraken, "hitByPlayer");
    }

    /** orig Kraken.java:1060 {@code isSuitableTarget(EntityLivingBase, boolean)}, the port's one-arg shape. */
    private static boolean isSuitableTarget(Kraken kraken, LivingEntity candidate) {
        return (Boolean) invoke(kraken, "isSuitableTarget", new Class<?>[] {LivingEntity.class}, candidate);
    }

    /** orig Kraken.java:963 {@code func_72857_a(EntityPlayer.class, ...)}: the port's {@code findNearestPlayer()}. */
    private static Player findNearestPlayer(Kraken kraken) {
        return (Player) invoke(kraken, "findNearestPlayer", new Class<?>[0]);
    }

    /**
     * The scan's own list in the scan's own order: the very call
     * {@code findNearestPlayer()} makes — {@code Level.getEntitiesOfClass(Player.class, box)}
     * on the orig :963 box, 25/40/25 around the Kraken — so a tie test reads the
     * order the entity section storage hands out instead of assuming spawn order.
     * Nothing moves between this call and the scan's, so the two lists agree.
     */
    private static List<Player> scanOrder(GameTestHelper helper, Kraken kraken) {
        return helper.getLevel().getEntitiesOfClass(Player.class, kraken.getBoundingBox().inflate(25.0, 40.0, 25.0));
    }

    /** A player by its x offset from the Kraken and squared distance — mock players all share one name. */
    private static String describe(Player player, Kraken kraken) {
        if (player == null) return "null";
        return "player[dx=" + (player.getX() - kraken.getX()) + " distSq=" + kraken.distanceToSqr(player) + "]";
    }

    private static String describe(List<Player> players, Kraken kraken) {
        return players.stream().map(player -> describe(player, kraken)).toList().toString();
    }

    /** orig Kraken.java:962-981, the port's {@code searchForPrey()}. */
    private static void searchForPrey(Kraken kraken) {
        invoke(kraken, "searchForPrey", new Class<?>[0]);
    }

    /** orig Kraken.java:921-959, the port's {@code pickNewFlightTarget()}. */
    private static void pickNewFlightTarget(Kraken kraken) {
        invoke(kraken, "pickNewFlightTarget", new Class<?>[0]);
    }
}
