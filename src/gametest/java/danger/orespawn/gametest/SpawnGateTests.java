package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AttackSquid;
import danger.orespawn.entity.Kraken;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Spawn-flag gate tests (checklist item i121-c8-spawn-flags).
 *
 * <p>Both tests run their config mutations and assertions synchronously on
 * the server thread within a single test tick and restore every mutated
 * value (configs, gamerules) in {@code finally} blocks, so concurrently
 * scheduled tests can never observe the changes.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpawnGateTests {

    /**
     * Drives the real natural-spawn pipeline for one mob: fire the NeoForge
     * FinalizeSpawnEvent with spawn type NATURAL (which
     * {@code ModSpawnControl.onFinalizeSpawn}, ModSpawnControl.java:184-190,
     * uses to tag natural spawns), then attempt the level join
     * ({@code ModSpawnControl.onEntityJoinLevel}, :197-210, cancels it for
     * disabled mobs — a canceled join makes {@code addFreshEntity} return
     * false). Returns whether the mob actually joined; a joined mob is
     * discarded immediately so nothing ever ticks.
     */
    private static boolean attemptSpawn(GameTestHelper helper, EntityType<? extends Mob> type,
                                        BlockPos pos, MobSpawnType spawnType) {
        ServerLevel level = helper.getLevel();
        Mob mob = type.create(level);
        helper.assertTrue(mob != null, "create failed for " + type);
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (spawnType != null) {
            EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), spawnType, null);
        }
        boolean added = level.addFreshEntity(mob);
        if (added) {
            mob.discard();
        }
        return added;
    }

    /**
     * Checklist item i121-c8-spawn-flags (C8 spawn flags): with
     * {@code krakenEnable=false}, {@code godzillaEnable=false},
     * {@code cowEnable=false}, NATURAL spawns of those mobs are discarded
     * while spawn eggs and /summon-style joins still work.
     *
     * <p>Documented mechanism: {@code ModSpawnControl} tags
     * NATURAL/CHUNK_GENERATION finalize events and cancels the level join of
     * disabled mobs; entities loaded from disk, spawn eggs, and commands pass
     * unconditionally (ModSpawnControl.java:178-210; per-mob map rows
     * :127 Kraken / :171 Godzilla / :140-147 CowEnable gating all six custom
     * cow variants, orig OreSpawnMain.java:4609-4624; config keys
     * OreSpawnConfig.java:212,245,223). A positive control with the flag
     * restored proves the discard was config-driven.</p>
     */
    @GameTest(template = "empty_large")
    public void c8_spawn_flags_gate_natural_spawns_not_eggs(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(24, 2, 24));
        boolean priorKraken = OreSpawnConfig.KRAKEN_ENABLE.get();
        boolean priorGodzilla = OreSpawnConfig.GODZILLA_ENABLE.get();
        boolean priorCow = OreSpawnConfig.COW_ENABLE.get();
        try {
            OreSpawnConfig.KRAKEN_ENABLE.set(false);
            OreSpawnConfig.GODZILLA_ENABLE.set(false);
            OreSpawnConfig.COW_ENABLE.set(false);

            helper.assertFalse(attemptSpawn(helper, ModEntities.KRAKEN.get(), center, MobSpawnType.NATURAL),
                    "krakenEnable=false but a NATURAL Kraken joined the level");
            helper.assertFalse(attemptSpawn(helper, ModEntities.GODZILLA.get(), center, MobSpawnType.NATURAL),
                    "godzillaEnable=false but a NATURAL Godzilla joined the level");
            helper.assertFalse(attemptSpawn(helper, ModEntities.RED_COW.get(), center, MobSpawnType.NATURAL),
                    "cowEnable=false but a NATURAL Red Cow joined the level");

            // Spawn eggs and /summon-style joins must pass while disabled.
            helper.assertTrue(attemptSpawn(helper, ModEntities.KRAKEN.get(), center, MobSpawnType.SPAWN_EGG),
                    "krakenEnable=false must not block SPAWN_EGG Krakens");
            helper.assertTrue(attemptSpawn(helper, ModEntities.GODZILLA.get(), center, MobSpawnType.SPAWN_EGG),
                    "godzillaEnable=false must not block SPAWN_EGG Godzillas");
            helper.assertTrue(attemptSpawn(helper, ModEntities.RED_COW.get(), center, MobSpawnType.SPAWN_EGG),
                    "cowEnable=false must not block SPAWN_EGG cows");
            helper.assertTrue(attemptSpawn(helper, ModEntities.KRAKEN.get(), center, null),
                    "krakenEnable=false must not block command/summon joins (no finalize event)");

            // Positive control: with the flag back on, the same NATURAL path joins.
            OreSpawnConfig.KRAKEN_ENABLE.set(true);
            helper.assertTrue(attemptSpawn(helper, ModEntities.KRAKEN.get(), center, MobSpawnType.NATURAL),
                    "positive control failed: NATURAL Kraken blocked with krakenEnable=true");
        } finally {
            OreSpawnConfig.KRAKEN_ENABLE.set(priorKraken);
            OreSpawnConfig.GODZILLA_ENABLE.set(priorGodzilla);
            OreSpawnConfig.COW_ENABLE.set(priorCow);
        }
        helper.succeed();
    }

    /** Spawns an AttackSquid, kills it with player-attributed damage, discards the corpse. */
    private static void killSquidAsPlayer(GameTestHelper helper, Player killer, BlockPos pos) {
        ServerLevel level = helper.getLevel();
        AttackSquid squid = ModEntities.ATTACK_SQUID.get().create(level);
        helper.assertTrue(squid != null, "AttackSquid create failed");
        squid.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(squid);
        squid.hurt(level.damageSources().playerAttack(killer), Float.MAX_VALUE);
        helper.assertTrue(squid.isDeadOrDying() || squid.isRemoved(), "squid survived a max-value player hit");
        squid.discard();
    }

    /**
     * Checklist item i121-c8-spawn-flags, revenge half: no AttackSquid revenge
     * Krakens with {@code krakenEnable=false}.
     *
     * <p>Documented mechanism: a player kill of an AttackSquid rolls
     * {@code nextInt(15)==0} to spawn 1-3 Krakens at y=170 within ±4 XZ of the
     * squid (orig AttackSquid.func_70097_a; port KrakenRevengeHandler.java:38-69),
     * gated on KrakenEnable BEFORE the roll (orig OreSpawnMain.java:6426; port
     * :55). Gate-off half is deterministic across 60 kills. Positive control:
     * with the flag on, P(no revenge spawn in 400 kills) = (14/15)^400 ≈
     * 1.1e-12 &lt; 1e-9 — and any spawned Kraken must sit at y=170. The whole
     * loop is synchronous; spawned Krakens are discarded before they can ever
     * tick or fall, mob loot is suppressed via doMobLoot for the duration, and
     * the XP orbs from the mass kills are swept at the end.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void c8_kraken_revenge_honors_kraken_enable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameRules.BooleanValue lootRule = level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT);
        boolean priorLoot = lootRule.get();
        boolean priorKraken = OreSpawnConfig.KRAKEN_ENABLE.get();
        Player killer = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos center = helper.absolutePos(new BlockPos(24, 2, 24));
        AABB skyBox = new AABB(
                center.getX() - 30.0, 140.0, center.getZ() - 30.0,
                center.getX() + 30.0, 200.0, center.getZ() + 30.0);
        try {
            lootRule.set(false, level.getServer());

            OreSpawnConfig.KRAKEN_ENABLE.set(false);
            for (int i = 0; i < 60; i++) {
                killSquidAsPlayer(helper, killer, center);
                helper.assertTrue(level.getEntitiesOfClass(Kraken.class, skyBox).isEmpty(),
                        "revenge Kraken spawned with krakenEnable=false (KrakenRevengeHandler.java:55)");
            }

            OreSpawnConfig.KRAKEN_ENABLE.set(true);
            boolean spawned = false;
            for (int i = 0; i < 400 && !spawned; i++) {
                killSquidAsPlayer(helper, killer, center);
                List<Kraken> revenge = level.getEntitiesOfClass(Kraken.class, skyBox);
                if (!revenge.isEmpty()) {
                    spawned = true;
                    for (Kraken kraken : revenge) {
                        helper.assertTrue(Math.abs(kraken.getY() - 170.0) < 0.01,
                                "revenge Kraken not at the documented y=170 anchor (KrakenRevengeHandler.java:35,64)");
                        kraken.discard();
                    }
                }
            }
            helper.assertTrue(spawned,
                    "no revenge Kraken across 400 player kills with krakenEnable=true "
                            + "(P(false failure) = (14/15)^400 ~ 1.1e-12)");
        } finally {
            OreSpawnConfig.KRAKEN_ENABLE.set(priorKraken);
            lootRule.set(priorLoot, level.getServer());
            AABB sweep = new AABB(center).inflate(40.0, 40.0, 40.0);
            level.getEntitiesOfClass(ExperienceOrb.class, sweep).forEach(Entity::discard);
        }
        helper.succeed();
    }

    /**
     * BUG-036 regression net: the royals must never sit in any biome's
     * natural CREATURE spawn pool. A pre-audit biome modifier
     * (companion_royalty.json, Phase 4E) had added ThePrince + ThePrincess
     * to {@code #minecraft:is_overworld} at weight 1 — and because vanilla
     * pre-populates newly generated chunks with CREATURE-category mobs
     * during worldgen ({@code NaturalSpawner.spawnMobsForChunkGeneration},
     * {@code MobSpawnType.CHUNK_GENERATION}), a fresh world could roll a
     * persistent-by-category Princess right at world spawn. The original
     * registers NEITHER royal in any spawn list (the complete
     * EntityRegistry.addSpawn roster, orig OreSpawnMain.java:4522+, has no
     * royalty — they come from eggs, the Queen's death, and structures
     * only; their always-true spawn rules, orig ThePrincess.java:369-371 /
     * ThePrince.java:381-383, are moot without a list entry and are kept
     * faithfully). The Girlfriend assertion is the positive control: her
     * plains CREATURE entry IS faithful (orig addSpawn(Girlfriend.class)) —
     * and being in that same chunk-generation pool, girlfriends at world
     * spawn on a new world are original behavior — so it proves this test
     * reads the modifier-baked pools.
     */
    @GameTest(template = "empty")
    public void bug036_no_wild_royalty_in_creature_pools(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        for (var biomeKey : List.of(net.minecraft.world.level.biome.Biomes.PLAINS,
                net.minecraft.world.level.biome.Biomes.DESERT)) {
            var pool = biomes.getHolderOrThrow(biomeKey).value().getMobSettings()
                    .getMobs(net.minecraft.world.entity.MobCategory.CREATURE).unwrap();
            helper.assertTrue(pool.stream().noneMatch(s ->
                            s.type == ModEntities.THE_PRINCESS.get() || s.type == ModEntities.THE_PRINCE.get()),
                    "a royal is in the " + biomeKey.location() + " CREATURE pool (BUG-036 regression)");
        }
        var plainsPool = biomes.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS)
                .value().getMobSettings()
                .getMobs(net.minecraft.world.entity.MobCategory.CREATURE).unwrap();
        helper.assertTrue(plainsPool.stream().anyMatch(s -> s.type == ModEntities.GIRLFRIEND.get()),
                "positive control failed: Girlfriend missing from the plains CREATURE pool");
        helper.succeed();
    }
}
