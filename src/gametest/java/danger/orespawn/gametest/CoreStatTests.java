package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderDriver;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.TheQueen;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Attribute-table tests (batch categories: attributes).
 *
 * <p>Every asserted number comes from {@code phase_b_reports/B2_mobstats.md}
 * (the reconciliation of the original {@code get_mobstats} table, orig
 * OreSpawnMain.java:6466-6525) or from the original entity files cited on the
 * individual methods.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CoreStatTests {

    /** Creates the entity, asserts HP/ATK/ARMOR attribute base values, discards it. */
    private static LivingEntity checkStats(GameTestHelper helper, EntityType<? extends LivingEntity> type,
                                           double hp, double atk, double armor, String cite) {
        LivingEntity e = type.create(helper.getLevel());
        helper.assertTrue(e != null, "EntityType.create returned null for " + type);
        helper.assertValueEqual(e.getAttributeBaseValue(Attributes.MAX_HEALTH), hp, cite + " MAX_HEALTH");
        helper.assertValueEqual(e.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), atk, cite + " ATTACK_DAMAGE");
        helper.assertValueEqual(e.getAttributeBaseValue(Attributes.ARMOR), armor, cite + " ARMOR");
        return e;
    }

    /**
     * Checklist item i094-b2-caps (B2 caps) + the boss half of i095.
     *
     * <p>Boss max-health table 7000/6000/4000/3000/1500/1500/1000:
     * TheKing 7000 (orig OreSpawnMain.java:6521), TheQueen 6000 (:6522),
     * Godzilla/"Mobzilla" 4000 (:6514), ThePrinceAdult 3000 (orig
     * ThePrinceAdult.java:226), SpiderRobot 1500 (:6474), ThePrinceTeen 1500
     * (orig ThePrinceTeen.java:230), Kraken 1000 (:6515) — all per
     * phase_b_reports/B2_mobstats.md §3.</p>
     *
     * <p>The {@code getHealth() == 7000} (etc.) asserts additionally prove the
     * B2 §2 attribute-cap fix: vanilla {@code RangedAttribute} clamps
     * MAX_HEALTH to 1024, and before the fix TheKing silently read 1024. A
     * value above 1024 can only survive if the cap raise in
     * {@code OreSpawnMod} applied (B2_mobstats.md §2).</p>
     */
    @GameTest(template = "empty")
    public void b2_boss_health_table_and_caps(GameTestHelper helper) {
        LivingEntity king = checkStats(helper, ModEntities.THE_KING.get(), 7000.0, 350.0, 21.0,
                "TheKing (orig OreSpawnMain.java:6521)");
        LivingEntity queen = checkStats(helper, ModEntities.THE_QUEEN.get(), 6000.0, 225.0, 21.0,
                "TheQueen (orig OreSpawnMain.java:6522)");
        LivingEntity godzilla = checkStats(helper, ModEntities.GODZILLA.get(), 4000.0, 175.0, 21.0,
                "Godzilla/Mobzilla (orig OreSpawnMain.java:6514)");

        // Above-1024 spot checks — the B2 §2 cap-raise proof ("must read 7000, not 1024").
        helper.assertValueEqual((double) king.getMaxHealth(), 7000.0, "TheKing getMaxHealth (cap raise, B2 §2)");
        helper.assertValueEqual((double) king.getHealth(), 7000.0, "TheKing spawn health (cap raise, B2 §2)");
        helper.assertValueEqual((double) queen.getMaxHealth(), 6000.0, "TheQueen getMaxHealth (cap raise, B2 §2)");
        helper.assertValueEqual((double) godzilla.getMaxHealth(), 4000.0, "Godzilla getMaxHealth (cap raise, B2 §2)");

        LivingEntity adult = ModEntities.THE_PRINCE_ADULT.get().create(helper.getLevel());
        helper.assertTrue(adult != null, "ThePrinceAdult create failed");
        helper.assertValueEqual(adult.getAttributeBaseValue(Attributes.MAX_HEALTH), 3000.0,
                "ThePrinceAdult HP (orig ThePrinceAdult.java:226)");
        LivingEntity teen = ModEntities.THE_PRINCE_TEEN.get().create(helper.getLevel());
        helper.assertTrue(teen != null, "ThePrinceTeen create failed");
        helper.assertValueEqual(teen.getAttributeBaseValue(Attributes.MAX_HEALTH), 1500.0,
                "ThePrinceTeen HP (orig ThePrinceTeen.java:230)");
        LivingEntity spiderRobot = checkStats(helper, ModEntities.SPIDER_ROBOT.get(), 1500.0, 100.0, 16.0,
                "SpiderRobot (orig OreSpawnMain.java:6474)");
        LivingEntity kraken = checkStats(helper, ModEntities.KRAKEN.get(), 1000.0, 40.0, 10.0,
                "Kraken (orig OreSpawnMain.java:6515)");
        helper.assertValueEqual((double) kraken.getMaxHealth(), 1000.0, "Kraken getMaxHealth (B2: 1000, not the old port's 3000)");

        king.discard(); queen.discard(); godzilla.discard();
        adult.discard(); teen.discard(); spiderRobot.discard(); kraken.discard();
        helper.succeed();
    }

    /**
     * Checklist item i095-b2-stats (B2 stats spot-check, generalized to a
     * cross-section of the table instead of the manual 3-4).
     *
     * <p>Each row is the reconciled base value from
     * phase_b_reports/B2_mobstats.md §3, one citation per row (orig
     * OreSpawnMain.java line of the {@code get_mobstats} table, or the orig
     * entity file for entity-side overrides).</p>
     */
    @GameTest(template = "empty")
    public void b2_stats_spot_checks(GameTestHelper helper) {
        checkStats(helper, ModEntities.ENTITY_BEE.get(),       80.0,  12.0,  5.0, "Bee (orig OreSpawnMain.java:6466)").discard();
        checkStats(helper, ModEntities.ENTITY_MANTIS.get(),   120.0,  16.0, 10.0, "Mantis (orig OreSpawnMain.java:6467)").discard();
        checkStats(helper, ModEntities.MOTHRA.get(),          150.0,  12.0,  8.0, "Mothra (orig OreSpawnMain.java:6469)").discard();
        checkStats(helper, ModEntities.GIANT_ROBOT.get(),     550.0,  40.0, 18.0, "GiantRobot/Jeffery (orig OreSpawnMain.java:6476)").discard();
        checkStats(helper, ModEntities.HAMMERHEAD.get(),      240.0,  75.0, 20.0, "Hammerhead (orig OreSpawnMain.java:6477)").discard();
        checkStats(helper, ModEntities.ENTITY_RAT.get(),        5.0,   3.0,  1.0, "Rat (orig OreSpawnMain.java:6483)").discard();
        checkStats(helper, ModEntities.BASILISK.get(),        200.0,  24.0, 15.0, "Basilisk (orig OreSpawnMain.java:6487)").discard();
        checkStats(helper, ModEntities.ALIEN.get(),           100.0,  12.0,  8.0, "Alien (orig OreSpawnMain.java:6491)").discard();
        checkStats(helper, ModEntities.ENTITY_WORM_LARGE.get(), 90.0, 18.0, 14.0, "WormLarge (orig OreSpawnMain.java:6506)").discard();
        // Irukandji: the audit's 200 attack belonged only to the empty-hand
        // retaliation (orig Irukandji.java:96); table value is 1/20/0.
        checkStats(helper, ModEntities.IRUKANDJI.get(),         1.0,  20.0,  0.0, "Irukandji (orig OreSpawnMain.java:6509)").discard();
        // Leonopteryx: entity-side override of the table — orig Leon.java
        // hardcodes HP 250 (:169), ATK 55 (:117), armor 16 (:192).
        checkStats(helper, ModEntities.LEONOPTERYX.get(),     250.0,  55.0, 16.0, "Leonopteryx (orig Leon.java:117,169,192)").discard();

        // Kraken speed 0.37 (orig Kraken.java:90; B2 fixed the old port's 0.5).
        LivingEntity kraken = ModEntities.KRAKEN.get().create(helper.getLevel());
        helper.assertTrue(kraken != null, "Kraken create failed");
        helper.assertValueEqual(kraken.getAttributeBaseValue(Attributes.MOVEMENT_SPEED), 0.37,
                "Kraken MOVEMENT_SPEED (orig Kraken.java:90)");
        kraken.discard();
        helper.succeed();
    }

    /**
     * Checklist item i067-b3-spiderdriver (armor half): 20 armor on foot,
     * 8 while mounted on its SpiderRobot.
     *
     * <p>Documented values: orig SpiderDriver.java:96-101 (getTotalArmorValue
     * override — 8 mounted, 20 on foot), ported as an attribute modifier
     * re-synced each server tick (port SpiderDriver.java:40-42,59-69);
     * phase_b_reports/B2_mobstats.md ENT-S-017/018 disposition.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void b3_spider_driver_armor_riding_state(GameTestHelper helper) {
        SpiderDriver driver = helper.spawn(ModEntities.SPIDER_DRIVER.get(), new BlockPos(24, 2, 24));
        // Triage fix (2026-08-10): spawn the robot only AFTER the on-foot
        // read — a dismounted driver AUTO-MOUNTS a nearby riderless robot
        // (port SpiderDriver.java:106, faithful ENT-S-017), which made the
        // first-run on-foot read see mounted armor 8.
        helper.runAfterDelay(5, () -> {
            // updateArmorModifier runs in customServerAiStep, so read after a few ticks.
            helper.assertValueEqual(driver.getAttributeValue(Attributes.ARMOR), 20.0,
                    "SpiderDriver on-foot armor (orig SpiderDriver.java:100)");
            SpiderRobot robot = helper.spawn(ModEntities.SPIDER_ROBOT.get(), new BlockPos(24, 2, 28));
            robot.setNoAi(true); // keep the 1500-HP robot inert inside the shared test grid
            helper.assertTrue(driver.startRiding(robot, true), "driver failed to mount SpiderRobot");
        });
        helper.runAfterDelay(12, () -> {
            helper.assertValueEqual(driver.getAttributeValue(Attributes.ARMOR), 8.0,
                    "SpiderDriver mounted armor (orig SpiderDriver.java:97-99)");
            if (driver.getVehicle() != null) driver.getVehicle().discard();
            driver.discard();
            helper.succeed();
        });
    }

    /**
     * Checklist item i067-b3-spiderdriver (poison half): while mounted the
     * driver's melee applies Poison.
     *
     * <p>Mechanism (port SpiderDriver.java:113-143, orig SpiderDriver.java:86-94):
     * per tick while mounted and difficulty != PEACEFUL (the gametest server
     * runs NORMAL — GameTestServer.java:85), a 1-in-4 roll picks the nearest
     * suitable target; a target within 2.0 blocks is bitten on a 16-tick
     * cooldown with a 1-in-2 Poison (60 ticks). Statistics: over the 900-tick
     * window the swing cadence is ~1 per 20 ticks (16-tick cooldown + the
     * 1-in-4 roll), giving &ge;40 expected swings; the Poison roll is applied
     * independently of the damage landing, so P(false failure) &le;
     * 0.5^40 &approx; 9.1e-13 &lt; 1e-9.</p>
     *
     * <p>Only PLAYERS can be bitten: the original's target filter rejects
     * non-player targets closer than 6 blocks (port SpiderDriver.java:162,
     * faithful quirk), so the victim is a mock survival player pinned 1 block
     * from the driver each tick.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 900)
    public void b3_spider_driver_mounted_bite_poisons(GameTestHelper helper) {
        SpiderRobot robot = helper.spawn(ModEntities.SPIDER_ROBOT.get(), new BlockPos(24, 2, 24));
        robot.setNoAi(true);
        SpiderDriver driver = helper.spawn(ModEntities.SPIDER_DRIVER.get(), new BlockPos(24, 2, 24));

        Player victim = helper.makeMockPlayer(GameType.SURVIVAL);
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000.0);
        victim.setHealth(10000.0f);
        Vec3 start = helper.absoluteVec(new Vec3(25.5, 2.0, 24.5));
        victim.setPos(start.x, start.y, start.z);
        helper.getLevel().addFreshEntity(victim);

        // The driver AUTO-MOUNTS a nearby riderless robot (SpiderDriver.java:106,
        // faithful ENT-S-017) — within the 2-tick delay it may already be
        // aboard, and startRiding on a mounted entity returns false. Accept
        // either mount path (triage fix, 2026-08-10 — was a timing flake).
        helper.runAfterDelay(2, () -> helper.assertTrue(
                driver.getVehicle() == robot || driver.startRiding(robot, true), "mount failed"));

        boolean[] done = {false};
        helper.onEachTick(() -> {
            if (done[0]) {
                return;
            }
            // Pin the victim inside the 2.0-block bite reach of the (stationary) driver.
            victim.setPos(driver.getX() + 1.0, driver.getY(), driver.getZ());
            if (victim.hasEffect(MobEffects.POISON)) {
                done[0] = true;
                driver.discard();
                robot.discard();
                victim.discard();
                helper.succeed();
            }
        });
    }

    /**
     * Checklist item i100-boss-006 (BOSS-006): TheQueen below 2/3 max HP with
     * fewer than 10 player hits reports armor 23 via the overridden getter and
     * takes measurably reduced damage.
     *
     * <p>Documented values: DEFENSE_VALUE 21 (orig OreSpawnMain.java:6522,
     * B2_mobstats.md BOSS-006 row) + 2 when {@code playerHitCount < 10 &&
     * health < maxHealth*2/3} (orig TheQueen.java:817-828 func_70658_aO;
     * port TheQueen.java:596-605). The +3/+5 branches are the original's
     * unreachable quirk, preserved as-is.</p>
     *
     * <p>Two fresh Queens are used (one per hit) because {@code hurt} arms a
     * 20-tick {@code hurtTimer} that only decrements in her AI step. Damage is
     * player-attributed ({@code playerAttack}) — armor applies, deltas are the
     * deterministic CombatRules armor absorb, so armor 23 strictly reduces the
     * delta vs armor 21. Neither entity is added to the level (no ticking, no
     * multipart side effects).</p>
     */
    @GameTest(template = "empty_large")
    public void boss006_queen_phase_armor_boost(GameTestHelper helper) {
        Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos center = helper.absolutePos(new BlockPos(24, 4, 24));

        TheQueen fullHealthQueen = ModEntities.THE_QUEEN.get().create(helper.getLevel());
        helper.assertTrue(fullHealthQueen != null, "TheQueen create failed");
        fullHealthQueen.moveTo(center.getX(), center.getY(), center.getZ(), 0.0f, 0.0f);
        helper.assertValueEqual(fullHealthQueen.getArmorValue(), 21,
                "Queen base armor at full HP (orig OreSpawnMain.java:6522 / TheQueen.java:827)");
        float before1 = fullHealthQueen.getHealth();
        fullHealthQueen.hurt(helper.getLevel().damageSources().playerAttack(attacker), 100.0f);
        double delta1 = before1 - fullHealthQueen.getHealth();

        TheQueen lowHealthQueen = ModEntities.THE_QUEEN.get().create(helper.getLevel());
        helper.assertTrue(lowHealthQueen != null, "TheQueen create failed");
        lowHealthQueen.moveTo(center.getX(), center.getY(), center.getZ() + 4, 0.0f, 0.0f);
        lowHealthQueen.setHealth(3000.0f); // below 6000 * 2/3 = 4000, zero player hits so far
        helper.assertValueEqual(lowHealthQueen.getArmorValue(), 23,
                "Queen armor below 2/3 HP with <10 player hits (orig TheQueen.java:818-819)");
        float before2 = lowHealthQueen.getHealth();
        lowHealthQueen.hurt(helper.getLevel().damageSources().playerAttack(attacker), 100.0f);
        double delta2 = before2 - lowHealthQueen.getHealth();

        helper.assertTrue(delta1 > 0.0, "full-HP Queen took no damage (delta1=" + delta1 + ")");
        helper.assertTrue(delta2 > 0.0, "low-HP Queen took no damage (delta2=" + delta2 + ")");
        helper.assertTrue(delta2 < delta1,
                "BOSS-006: damage below 2/3 HP (" + delta2 + ") not reduced vs full HP (" + delta1 + ")");

        fullHealthQueen.discard();
        lowHealthQueen.discard();
        helper.succeed();
    }
}
