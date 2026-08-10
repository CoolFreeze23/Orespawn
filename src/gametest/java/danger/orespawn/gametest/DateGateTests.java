package danger.orespawn.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.util.SeasonalDates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Seasonal date-gate tests (ANIM-016; checklist items i177 Halloween and i179
 * Easter, plus the Girlfriend valentine state the D4 fix documents).
 *
 * <p>The suite NEVER touches the system clock: every date-sensitive check
 * runs through the sanctioned test seam
 * {@link SeasonalDates#setClockForTesting} inside a try/finally that calls
 * {@link SeasonalDates#resetClock} (SeasonalDates.java:27-37) — including the
 * "normal date" halves, which pin an explicit non-holiday date so the suite
 * cannot false-fail when executed ON a holiday.</p>
 *
 * <p>Each test runs in its own GameTest batch because it flips the shared
 * server's day time; the original time is restored before success.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DateGateTests {

    private static final LocalDate HALLOWEEN = LocalDate.of(2026, 10, 31);
    private static final LocalDate VALENTINES = LocalDate.of(2026, 2, 14);
    private static final LocalDate EASTER = LocalDate.of(2026, 4, 20);
    /** An explicitly boring date — none of the three gates. */
    private static final LocalDate NEUTRAL = LocalDate.of(2026, 3, 3);

    private static <T> T withClock(LocalDate date, Supplier<T> body) {
        SeasonalDates.setClockForTesting(() -> date);
        try {
            return body.get();
        } finally {
            SeasonalDates.resetClock();
        }
    }

    private static JsonObject bundledJson(String path) {
        try (InputStream in = DateGateTests.class.getResourceAsStream(path)) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("cannot load bundled resource " + path, e);
        }
    }

    // ---------------------------------------------------------------

    /**
     * i177 (ANIM-016 Halloween, closes ENT-D-039/041) — seasonal-biome half.
     * In a NON-year-round biome (plains), Ghost/GhostSkelly natural spawns
     * pass only on Oct 31 at night: the gate is
     * {@code SeasonalDates.isHalloween() || inYearRoundGhostBiome} then
     * {@code !isDaytime} (port Ghost.java:184-198 / GhostSkelly.java:177-188,
     * restoring orig Ghost.java:145-160 + the Oct-31-only registration
     * OreSpawnMain.java:4518-4565; FIX_LOG "Halloween (ENT-D-039/041
     * closed)"). Data half: halloween_ghosts.json registers ghost+ghost_skelly
     * at w15 count 3-6 across the documented 20 modern biomes (the orig
     * 22-biome block after mapping/dedup — FIX_LOG.md "20 modern biomes after
     * mapping"; D4_items_small_entities.md §13). Drop half (C2): Ghosts drop
     * NOTHING on a player kill (FIX_LOG D4 "Ghost/GhostSkelly (nothing)").
     */
    @GameTest(template = "empty_large", batch = "orespawnDate1", timeoutTicks = 200)
    public void halloween_seasonal_biome_gate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBiome(Biomes.PLAINS);

        // Data assert straight off the shipped biome modifier JSON.
        JsonObject json = bundledJson(
                "/data/orespawn/neoforge/biome_modifier/halloween_ghosts.json");
        JsonArray biomes = json.getAsJsonArray("biomes");
        helper.assertValueEqual(biomes.size(), 20,
                "halloween_ghosts.json: the 22-biome orig block maps to 20 modern biomes");
        int ghostSpawners = 0;
        for (JsonElement e : json.getAsJsonArray("spawners")) {
            JsonObject spawner = e.getAsJsonObject();
            String type = spawner.get("type").getAsString();
            if (type.equals("orespawn:ghost") || type.equals("orespawn:ghost_skelly")) {
                ghostSpawners++;
                helper.assertValueEqual(spawner.get("weight").getAsInt(), 15,
                        "halloween spawn weight 15 (orig w15 block)");
                helper.assertValueEqual(spawner.get("minCount").getAsInt(), 3,
                        "halloween min pack 3");
                helper.assertValueEqual(spawner.get("maxCount").getAsInt(), 6,
                        "halloween max pack 6");
            }
        }
        helper.assertValueEqual(ghostSpawners, 2,
                "halloween_ghosts.json registers both ghost and ghost_skelly");

        long originalDayTime = level.getDayTime();
        Mob ghost = ModEntities.GHOST.get().create(level);
        Mob skelly = ModEntities.GHOST_SKELLY.get().create(level);
        BlockPos center = helper.absolutePos(new BlockPos(24, 3, 24));
        for (Mob mob : new Mob[]{ghost, skelly}) {
            mob.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            mob.setPersistenceRequired();
            level.addFreshEntity(mob);
        }

        level.setDayTime(18000); // midnight; skyDarken settles next tick
        helper.runAfterDelay(5, () -> {
            for (Mob mob : new Mob[]{ghost, skelly}) {
                String name = mob.getType().toShortString();
                helper.assertTrue(withClock(HALLOWEEN,
                                () -> mob.checkSpawnRules(level, MobSpawnType.NATURAL)),
                        name + ": Oct 31 + night + plains → may spawn");
                helper.assertFalse(withClock(NEUTRAL,
                                () -> mob.checkSpawnRules(level, MobSpawnType.NATURAL)),
                        name + ": normal date + plains → blocked (not a year-round biome)");
            }
            level.setDayTime(6000); // noon
        });
        helper.runAfterDelay(10, () -> {
            for (Mob mob : new Mob[]{ghost, skelly}) {
                helper.assertFalse(withClock(HALLOWEEN,
                                () -> mob.checkSpawnRules(level, MobSpawnType.NATURAL)),
                        mob.getType().toShortString() + ": daytime blocks even on Oct 31");
            }
            // C2 drop check: player-attributed kills drop nothing.
            Player killer = helper.makeMockPlayer(GameType.SURVIVAL);
            ghost.hurt(ghost.damageSources().playerAttack(killer), 10_000.0f);
            skelly.hurt(skelly.damageSources().playerAttack(killer), 10_000.0f);
        });
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class,
                            new AABB(center).inflate(10)).isEmpty(),
                    "Ghost/GhostSkelly drop NOTHING (C2)");
            level.getEntitiesOfClass(Entity.class, new AABB(center).inflate(16),
                    e -> e == ghost || e == skelly).forEach(Entity::discard);
            level.setDayTime(originalDayTime);
            helper.succeed();
        });
    }

    /**
     * i177 (ANIM-016 Halloween) — year-round half: in the five ungated biomes
     * (snowy taiga, taiga, frozen river, jungle, dark forest — orig
     * OreSpawnMain.java:4783-4795, port OriginalSpawnGates.inYearRoundGhostBiome)
     * Ghost/GhostSkelly spawn on ANY date at night, and never in daytime.
     */
    @GameTest(template = "empty_large", batch = "orespawnDate2", timeoutTicks = 200)
    public void halloween_year_round_biomes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBiome(Biomes.DARK_FOREST);

        long originalDayTime = level.getDayTime();
        Mob ghost = ModEntities.GHOST.get().create(level);
        Mob skelly = ModEntities.GHOST_SKELLY.get().create(level);
        BlockPos center = helper.absolutePos(new BlockPos(24, 3, 24));
        for (Mob mob : new Mob[]{ghost, skelly}) {
            mob.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            mob.setPersistenceRequired();
            level.addFreshEntity(mob);
        }

        level.setDayTime(18000);
        helper.runAfterDelay(5, () -> {
            for (Mob mob : new Mob[]{ghost, skelly}) {
                helper.assertTrue(withClock(NEUTRAL,
                                () -> mob.checkSpawnRules(level, MobSpawnType.NATURAL)),
                        mob.getType().toShortString()
                                + ": dark forest is year-round — normal date + night passes");
            }
            level.setDayTime(6000);
        });
        helper.runAfterDelay(10, () -> {
            for (Mob mob : new Mob[]{ghost, skelly}) {
                helper.assertFalse(withClock(NEUTRAL,
                                () -> mob.checkSpawnRules(level, MobSpawnType.NATURAL)),
                        mob.getType().toShortString() + ": still night-only in year-round biomes");
                mob.discard();
            }
            level.setDayTime(originalDayTime);
            helper.succeed();
        });
    }

    /**
     * Girlfriend valentine state (ANIM-016 Valentine's; the server-observable
     * half of the D4 fix — FIX_LOG "Valentine's: ... 2.5x8.0 dimensions,
     * 800 HP"; port Girlfriend.java:86-121 applyValentineState /
     * getDefaultDimensions, restoring orig Girlfriend.java:142-144,198-200,
     * 569-574). A Girlfriend created on Feb 14 is the giant angry variant
     * (800 max HP, 2.5x8.0); on any other date she is the normal 80 HP
     * 0.5x1.6 companion. Rendering-side criteria (girlfriendv texture, 5x
     * render scale) stay manual — item i178.
     */
    @GameTest(template = "empty_large", batch = "orespawnDate3")
    public void valentine_girlfriend_state(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(24, 2, 24));

        // The three SeasonalDates gates themselves (SeasonalDates.java:39-55,
        // orig OreSpawnMain.java:4518-4521,4567-4571; PN-014 live evaluation).
        helper.assertTrue(withClock(HALLOWEEN, SeasonalDates::isHalloween), "Oct 31 gate");
        helper.assertTrue(withClock(VALENTINES, SeasonalDates::isValentines), "Feb 14 gate");
        helper.assertTrue(withClock(EASTER, SeasonalDates::isEaster), "Apr 20 gate");
        helper.assertFalse(withClock(NEUTRAL, SeasonalDates::isHalloween), "neutral: no halloween");
        helper.assertFalse(withClock(NEUTRAL, SeasonalDates::isValentines), "neutral: no valentine");
        helper.assertFalse(withClock(NEUTRAL, SeasonalDates::isEaster), "neutral: no easter");

        withClock(VALENTINES, () -> {
            Girlfriend angry = ModEntities.GIRLFRIEND.get().create(level);
            angry.setPos(center.getX(), center.getY(), center.getZ());
            level.addFreshEntity(angry);
            helper.assertTrue(Math.abs(angry.getMaxHealth() - 800.0f) < 0.01,
                    "Feb 14 Girlfriend: 800 max HP (orig Girlfriend.java:569-574), got "
                            + angry.getMaxHealth());
            helper.assertTrue(angry.getHealth() > 799.0f,
                    "Feb 14 Girlfriend spawns at full valentine health");
            helper.assertTrue(Math.abs(angry.getBbWidth() - 2.5f) < 0.01
                            && Math.abs(angry.getBbHeight() - 8.0f) < 0.01,
                    "Feb 14 Girlfriend: 2.5x8.0 giant (orig :142-144), got "
                            + angry.getBbWidth() + "x" + angry.getBbHeight());
            angry.discard();
            return null;
        });

        withClock(NEUTRAL, () -> {
            Girlfriend normal = ModEntities.GIRLFRIEND.get().create(level);
            normal.setPos(center.getX(), center.getY(), center.getZ());
            level.addFreshEntity(normal);
            helper.assertTrue(Math.abs(normal.getMaxHealth() - 80.0f) < 0.01,
                    "normal-date Girlfriend: 80 max HP, got " + normal.getMaxHealth());
            helper.assertTrue(normal.getBbHeight() < 2.0f,
                    "normal-date Girlfriend keeps her small hitbox (0.5x1.6), got "
                            + normal.getBbWidth() + "x" + normal.getBbHeight());
            normal.discard();
            return null;
        });
        helper.succeed();
    }

    /**
     * i179 (ANIM-016 Easter, closes ENT-D-011): EasterBunny NATURAL spawns
     * pass only on Apr 20 — {@code checkSpawnRules} denies unless
     * {@code SeasonalDates.isEaster()}, then requires y ≥ 50, daytime, and no
     * other bunny within 32/8/32 (port EasterBunny.java:246-257, restoring
     * orig EasterBunny.java:67-77 + the Apr-20-only registration
     * OreSpawnMain.java:4570-4571,4681; FIX_LOG "Easter (ENT-D-011 closed)").
     * Biome registration data half: the three shipped
     * companion_easter_bunny__* biome modifiers all spawn
     * {@code orespawn:easter_bunny}. Egg-laying itself is date-independent
     * and covered by A4 — out of scope here.
     */
    @GameTest(template = "empty_large", batch = "orespawnDate4", timeoutTicks = 200)
    public void easter_bunny_gate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // Data assert: the bunny's natural-spawn registration exists.
        String[] modifiers = {
                "/data/orespawn/neoforge/biome_modifier/companion_easter_bunny__plains_forests.json",
                "/data/orespawn/neoforge/biome_modifier/companion_easter_bunny__birch_taigas.json",
                "/data/orespawn/neoforge/biome_modifier/companion_easter_bunny__taiga.json"};
        for (String path : modifiers) {
            JsonObject json = bundledJson(path);
            helper.assertValueEqual(json.get("type").getAsString(), "neoforge:add_spawns",
                    path + " adds spawns");
            JsonElement spawners = json.get("spawners");
            JsonObject first = spawners.isJsonArray()
                    ? spawners.getAsJsonArray().get(0).getAsJsonObject()
                    : spawners.getAsJsonObject();
            helper.assertValueEqual(first.get("type").getAsString(), "orespawn:easter_bunny",
                    path + " spawns the easter bunny");
        }

        long originalDayTime = level.getDayTime();
        Mob bunny = ModEntities.EASTER_BUNNY.get().create(level);
        BlockPos center = helper.absolutePos(new BlockPos(24, 3, 24));
        bunny.setPos(center.getX() + 0.5, 120.0, center.getZ() + 0.5); // above the y>=50 gate
        bunny.setPersistenceRequired();
        level.addFreshEntity(bunny);

        level.setDayTime(1000); // morning
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(withClock(EASTER,
                            () -> bunny.checkSpawnRules(level, MobSpawnType.NATURAL)),
                    "Apr 20 + day + y>=50 → EasterBunny may spawn");
            helper.assertFalse(withClock(NEUTRAL,
                            () -> bunny.checkSpawnRules(level, MobSpawnType.NATURAL)),
                    "any other date → never (ENT-D-011)");
            helper.assertFalse(withClock(LocalDate.of(2026, 4, 19),
                            () -> bunny.checkSpawnRules(level, MobSpawnType.NATURAL)),
                    "Apr 19 is not Easter (fixed Apr 20 gate)");
            // y < 50 fails even on Easter (orig EasterBunny.java:67-77).
            bunny.setPos(center.getX() + 0.5, 30.0, center.getZ() + 0.5);
            helper.assertFalse(withClock(EASTER,
                            () -> bunny.checkSpawnRules(level, MobSpawnType.NATURAL)),
                    "y < 50 blocks the spawn even on Apr 20");
            bunny.setPos(center.getX() + 0.5, 120.0, center.getZ() + 0.5);
            level.setDayTime(18000); // midnight
        });
        helper.runAfterDelay(10, () -> {
            helper.assertFalse(withClock(EASTER,
                            () -> bunny.checkSpawnRules(level, MobSpawnType.NATURAL)),
                    "night blocks the spawn even on Apr 20 (daytime gate)");
            bunny.discard();
            level.setDayTime(originalDayTime);
            helper.succeed();
        });
    }
}
