package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.block.entity.RandomDungeonSpawnerBlockEntity;
import danger.orespawn.world.CrystalStructures;
import danger.orespawn.world.OreSpawnVeinPlacement;
import danger.orespawn.world.feature.SpawnOresPoolFeature;
import danger.orespawn.world.structure.LegacyDungeonStructure;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Config-gated behavior tests (batch categories: config).
 *
 * <p>Far-build offsets (per the suite convention
 * {@code helper.absolutePos(ZERO).offset(K * 512, 0, 50000)}), batch K base
 * 100: K=101 and K=102 (spawn-ore pool ratio volumes), K=103 (urchin-spawner
 * gate terrain), K=104 (DSB haunted-house build site).</p>
 *
 * <p>Config values are mutated with {@code ModConfigSpec.ConfigValue.set} and
 * restored in {@code finally} blocks. All config-holding assertions here run
 * synchronously on the server thread inside a single test tick, so
 * concurrently scheduled tests can never observe a mutated value — except the
 * two boss-spawner fizzle tests, which must hold the flag across the 100-tick
 * fuse; those flags gate nothing that other tests exercise naturally.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ConfigGateTests {

    private static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    private static BlockPos farPos(GameTestHelper helper, int k) {
        return helper.absolutePos(BlockPos.ZERO).offset(k * 512, 0, 50000);
    }

    // =====================================================================
    // ITEM-058 — glide boots (i024-item-058)
    // =====================================================================

    /** Equips {@code boots}, gives the player a -1.0 fall, runs one armor inventory tick. */
    private static void glideTick(GameTestHelper helper, Player player, ItemStack boots) {
        player.setItemSlot(EquipmentSlot.FEET, boots);
        player.setDeltaMovement(0.0, -1.0, 0.0);
        player.fallDistance = 10.0f;
        boots.getItem().inventoryTick(boots, helper.getLevel(), player, 3, false);
    }

    /**
     * Checklist item i024-item-058 (ITEM-058): peacock boots glide regardless
     * of {@code royalGlideEnable}; royal and queen boots only glide with the
     * flag set.
     *
     * <p>Documented values: orig ItemOreSpawnArmor.java:343-358 — royal/peacock
     * glide caps fall speed at -0.1 and zeroes fall distance, with RoyalBoots
     * gated on RoyalGlideEnable and PeacockFeatherBoots NEVER gated (orig
     * :347); queen glide caps at -0.25 and requires the flag (orig :353).
     * Port: ItemOreSpawnArmor.java:166-196 (caps at :119-120,
     * GLIDE_FALL_CAP=-0.1 / QUEEN_FALL_CAP=-0.25; gating at :181-190).</p>
     */
    @GameTest(template = "empty")
    public void item058_glide_boots_config_gate(GameTestHelper helper) {
        boolean prior = OreSpawnConfig.ROYAL_GLIDE_ENABLE.get();
        try {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);

            // Peacock boots: NOT config-gated (orig ItemOreSpawnArmor.java:347).
            OreSpawnConfig.ROYAL_GLIDE_ENABLE.set(false);
            glideTick(helper, player, new ItemStack(ModItems.PEACOCK_BOOTS.get()));
            helper.assertValueEqual(player.getDeltaMovement().y, -0.1,
                    "peacock boots must glide with royalGlideEnable=false (ITEM-058)");
            helper.assertValueEqual((double) player.fallDistance, 0.0,
                    "peacock glide must reset fall distance");

            // Royal boots: gated (orig :347).
            glideTick(helper, player, new ItemStack(ModItems.ROYAL_BOOTS.get()));
            helper.assertValueEqual(player.getDeltaMovement().y, -1.0,
                    "royal boots must NOT glide with royalGlideEnable=false");
            helper.assertValueEqual((double) player.fallDistance, 10.0,
                    "royal boots must not touch fall distance while gated off");

            OreSpawnConfig.ROYAL_GLIDE_ENABLE.set(true);
            glideTick(helper, player, new ItemStack(ModItems.ROYAL_BOOTS.get()));
            helper.assertValueEqual(player.getDeltaMovement().y, -0.1,
                    "royal boots must glide (cap -0.1) with royalGlideEnable=true");

            // Queen boots: gated, cap -0.25 (orig :353).
            glideTick(helper, player, new ItemStack(ModItems.QUEEN_BOOTS.get()));
            helper.assertValueEqual(player.getDeltaMovement().y, -0.25,
                    "queen boots must glide (cap -0.25) with royalGlideEnable=true");
            helper.assertValueEqual((double) player.fallDistance, 0.0,
                    "queen glide must reset fall distance");

            OreSpawnConfig.ROYAL_GLIDE_ENABLE.set(false);
            glideTick(helper, player, new ItemStack(ModItems.QUEEN_BOOTS.get()));
            helper.assertValueEqual(player.getDeltaMovement().y, -1.0,
                    "queen boots must NOT glide with royalGlideEnable=false");
        } finally {
            OreSpawnConfig.ROYAL_GLIDE_ENABLE.set(prior);
        }
        helper.succeed();
    }

    // =====================================================================
    // ITEM-064 — lessOre vein-count division (i107-item-064)
    // =====================================================================

    /**
     * Samples the feature's {@code orespawn:vein_count} modifier with LESS_ORE
     * off then on and asserts the emitted-position ratio sits in [lo, hi].
     * Fixed seeds make the sampling fully deterministic. LESS_ORE is restored to
     * its entry value on every exit of this helper (harness slice, 2026-09-04).
     */
    private static void checkVeinRatio(GameTestHelper helper, Registry<PlacedFeature> reg,
                                       PlacementContext ctx, BlockPos origin,
                                       String name, double lo, double hi) {
        PlacedFeature pf = reg.getOptional(loc(name)).orElse(null);
        helper.assertTrue(pf != null, "missing placed feature " + name);
        PlacementModifier vein = pf.placement().stream()
                .filter(m -> m instanceof OreSpawnVeinPlacement).findFirst().orElse(null);
        helper.assertTrue(vein != null, name + " lacks the orespawn:vein_count modifier (ITEM-064)");

        int rolls = 2000;
        long base = 0;
        long cut = 0;
        // The flag is GLOBAL: this helper used to return — and throw — with it
        // left TRUE, relying on its caller's finally (harness slice, 2026-09-04).
        final boolean lessOrePrior = OreSpawnConfig.LESS_ORE.get();
        try {
            OreSpawnConfig.LESS_ORE.set(false);
            RandomSource random = new XoroshiroRandomSource(0xC0FFEE);
            for (int i = 0; i < rolls; i++) {
                base += vein.getPositions(ctx, random, origin).count();
            }
            OreSpawnConfig.LESS_ORE.set(true);
            random = new XoroshiroRandomSource(0xBEEF00);
            for (int i = 0; i < rolls; i++) {
                cut += vein.getPositions(ctx, random, origin).count();
            }
        } finally {
            OreSpawnConfig.LESS_ORE.set(lessOrePrior);
        }
        helper.assertTrue(base > 0, name + " emitted no positions with lessOre=false");
        double ratio = (double) cut / (double) base;
        helper.assertTrue(ratio > lo && ratio < hi,
                name + " lessOre ratio " + ratio + " outside [" + lo + ", " + hi + "] (ITEM-064)");
    }

    /**
     * Checklist item i107-item-064 (ITEM-064): with {@code lessOre=true},
     * uranium/titanium/amethyst/salt veins drop to ~1/3 and troll blocks to
     * ~1/2.
     *
     * <p>Documented mechanism: the whole lessOre effect is the truncating
     * attempt-count division inside {@code OreSpawnVeinPlacement.getPositions}
     * (port OreSpawnVeinPlacement.java:83-104; orig OreSpawnWorld.java:807-848
     * divide-by-3 for the four ores, :857-870 divide-by-2 for the troll
     * blocks). The per-attempt Y-window rejection is identical for both config
     * states, so it cancels out of the ratio. Sampling: 2000 fixed-seed rolls
     * per state per feature — deterministic, no false-failure probability; the
     * exact expected ratios (E[floor((count+dice)/d)]/E[count+dice]) all sit
     * inside the asserted bands.</p>
     *
     * <p>Data half: every uranium/titanium/amethyst/salt/troll placed feature
     * (overworld and _mining variants) must carry the modifier with the
     * documented divisor wiring (placed_feature/ore_*.json,
     * red_ant_troll*.json, termite_troll*.json).</p>
     */
    @GameTest(template = "empty")
    public void item064_less_ore_vein_ratios(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<PlacedFeature> reg = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        boolean prior = OreSpawnConfig.LESS_ORE.get();
        try {
            // Data assert: modifier present on all 12 vein features.
            String[] all = {
                    "ore_uranium", "ore_titanium", "ore_amethyst", "ore_salt",
                    "red_ant_troll", "termite_troll",
                    "ore_uranium_mining", "ore_titanium_mining", "ore_amethyst_mining", "ore_salt_mining",
                    "red_ant_troll_mining", "termite_troll_mining"};
            for (String name : all) {
                PlacedFeature pf = reg.getOptional(loc(name)).orElse(null);
                helper.assertTrue(pf != null, "missing placed feature " + name);
                helper.assertTrue(pf.placement().stream().anyMatch(m -> m instanceof OreSpawnVeinPlacement),
                        "placed feature " + name + " lacks orespawn:vein_count (ITEM-064)");
            }

            PlacementContext ctx = new PlacementContext(level,
                    level.getChunkSource().getGenerator(), Optional.empty());
            BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0));
            // Divisor 3 (orig OreSpawnWorld.java:807-848): expected ratios 0.28-0.30.
            for (String name : new String[]{"ore_uranium", "ore_titanium", "ore_amethyst", "ore_salt"}) {
                checkVeinRatio(helper, reg, ctx, origin, name, 0.18, 0.42);
            }
            // Divisor 2 (orig OreSpawnWorld.java:857-870): expected ratio ~0.45.
            for (String name : new String[]{"red_ant_troll", "termite_troll"}) {
                checkVeinRatio(helper, reg, ctx, origin, name, 0.35, 0.55);
            }
        } finally {
            OreSpawnConfig.LESS_ORE.set(prior);
        }
        helper.succeed();
    }

    // =====================================================================
    // LessOre=1 spawn-ore pool cut (i134-lessore-1)
    // =====================================================================

    /**
     * Fills a 16x82x16 stone chunk-column at far offset K, runs the
     * registry-bound {@code orespawn:spawn_ores} configured feature
     * {@code rounds} times with the given LESS_ORE state, and returns the
     * total number of stone blocks converted to spawn-ore blocks (each
     * placement is counted then reset to stone, so rounds are independent).
     */
    private static long spawnOrePoolBlocks(GameTestHelper helper, ConfiguredFeature<?, ?> feature,
                                           int k, boolean lessOre, long seed, int rounds) {
        ServerLevel level = helper.getLevel();
        OreSpawnConfig.LESS_ORE.set(lessOre);
        BlockPos far = farPos(helper, k);
        BlockPos chunkOrigin = new BlockPos(far.getX() & ~15, 0, far.getZ() & ~15);
        BlockState stone = Blocks.STONE.defaultBlockState();
        // Vein Y rolls are nextInt(128) accepted in [MIN_DEPTH=50, MAX_DEPTH=128]
        // (SpawnOresPoolFeature.java:63-66,226-234); pad a few layers for clump spread.
        int minY = 46;
        int maxY = 127;
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = minY; y <= maxY; y++) {
                    level.setBlock(new BlockPos(chunkOrigin.getX() + x, y, chunkOrigin.getZ() + z), stone, 2);
                }
            }
        }
        RandomSource random = new XoroshiroRandomSource(seed);
        long total = 0;
        for (int round = 0; round < rounds; round++) {
            feature.place(level, level.getChunkSource().getGenerator(), random, chunkOrigin.atY(0));
            for (int x = 0; x <= 15; x++) {
                for (int z = 0; z <= 15; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        BlockPos p = new BlockPos(chunkOrigin.getX() + x, y, chunkOrigin.getZ() + z);
                        if (!level.getBlockState(p).is(Blocks.STONE)) {
                            total++;
                            level.setBlock(p, stone, 2);
                        }
                    }
                }
            }
        }
        return total;
    }

    /**
     * Checklist item i134-lessore-1 (LessOre=1): new-chunk spawn-ore veins cut
     * to ~1/3.
     *
     * <p>Documented mechanism: {@code SpawnOresPoolFeature.place} rolls
     * {@code RATE + nextInt(count_dice)} veins (+30 on a 1-in-20 burst) and
     * divides by 3 when LESS_ORE is set (port SpawnOresPoolFeature.java:210-225;
     * orig OreSpawnWorld.java:356-362 / ChunkOreGenerator.java:22-28), with the
     * pass count switching from {@code passes} to {@code less_ore_passes}. The
     * Y-window discard (nextInt(128) accepted in 50..128, :226-234) is
     * config-independent and cancels out of the ratio.</p>
     *
     * <p>Empirical half: the real registry-bound configured feature is driven
     * against identical prepared stone volumes at far offsets K=101 (lessOre
     * off) and K=102 (on) with fixed seeds — deterministic; the expected block
     * ratio is ~1/3 and the asserted band [0.18, 0.50] is generous. Data half:
     * configured_feature/spawn_ores.json carries count_dice=20 passes=1
     * less_ore_passes=1; spawn_ores_mining.json carries count_dice=30 passes=3
     * less_ore_passes=1 (the Mining triple pass collapsing to one is orig
     * ChunkProviderOreSpawn2.java:191-195).</p>
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void item064_less_ore_spawn_ores_pool_ratio(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Registry<ConfiguredFeature<?, ?>> reg =
                level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        ConfiguredFeature<?, ?> overworldPool = reg.getOptional(loc("spawn_ores")).orElse(null);
        helper.assertTrue(overworldPool != null, "configured feature orespawn:spawn_ores missing");
        helper.assertTrue(overworldPool.config() instanceof SpawnOresPoolFeature.Configuration,
                "orespawn:spawn_ores is not the SpawnOres pool feature");
        SpawnOresPoolFeature.Configuration cfg = (SpawnOresPoolFeature.Configuration) overworldPool.config();
        helper.assertValueEqual(cfg.countDice(), 20, "spawn_ores count_dice (spawn_ores.json)");
        helper.assertValueEqual(cfg.passes(), 1, "spawn_ores passes");
        helper.assertValueEqual(cfg.lessOrePasses(), 1, "spawn_ores less_ore_passes");

        ConfiguredFeature<?, ?> miningPool = reg.getOptional(loc("spawn_ores_mining")).orElse(null);
        helper.assertTrue(miningPool != null, "configured feature orespawn:spawn_ores_mining missing");
        helper.assertTrue(miningPool.config() instanceof SpawnOresPoolFeature.Configuration,
                "orespawn:spawn_ores_mining is not the SpawnOres pool feature");
        SpawnOresPoolFeature.Configuration miningCfg = (SpawnOresPoolFeature.Configuration) miningPool.config();
        helper.assertValueEqual(miningCfg.countDice(), 30, "spawn_ores_mining count_dice (spawn_ores_mining.json)");
        helper.assertValueEqual(miningCfg.passes(), 3, "spawn_ores_mining passes (orig ChunkProviderOreSpawn2.java:191-195)");
        helper.assertValueEqual(miningCfg.lessOrePasses(), 1, "spawn_ores_mining less_ore_passes");

        boolean prior = OreSpawnConfig.LESS_ORE.get();
        try {
            long base = spawnOrePoolBlocks(helper, overworldPool, 101, false, 0x5EED_0FFL, 30);
            long cut = spawnOrePoolBlocks(helper, overworldPool, 102, true, 0x5EED_0FFL, 30);
            helper.assertTrue(base > 0, "spawn_ores pool placed nothing with lessOre=false");
            double ratio = (double) cut / (double) base;
            helper.assertTrue(ratio > 0.18 && ratio < 0.50,
                    "spawn-ore vein-block ratio " + ratio + " outside [0.18, 0.50] with lessOre=true (i134)");
        } finally {
            OreSpawnConfig.LESS_ORE.set(prior);
        }
        helper.succeed();
    }

    // =====================================================================
    // urchinEnable worldgen gate (i149-urchinspawner-crystal-wgen-042)
    // =====================================================================

    /**
     * Checklist item i149-urchinspawner-crystal-wgen-042 (WGEN-042 urchin
     * spawner): with {@code urchinEnable=false} no urchin spawner structure
     * can ever generate; enabled, the 1/180 roll places one.
     *
     * <p>Documented mechanism: orig OreSpawnWorld.java:1649-1652 — the
     * UrchinEnable kill-switch returns BEFORE the 1/180 roll (port
     * CrystalStructures.java:573-598, gate at :581). The private
     * {@code tryPlaceUrchinSpawner} is driven by reflection over a prepared
     * crystal-grass platform at far offset K=103 (the scan wants air directly
     * above {@code orespawn:crystal_grass} in Y 51..100, :585-595); the built
     * structure's signature is the 3-spawner column at scan-Y+1..+3
     * (CrystalStructures.buildUrchinSpawner, orig GenericDungeon.java:2578-2636).</p>
     *
     * <p>Statistics: gate-off phase is deterministic (the early return never
     * reaches the roll across 4000 invocations). Gate-on phase: P(no success
     * in 5000 rolls) = (179/180)^5000 &approx; 8.5e-13 &lt; 1e-9 — and the
     * fixed seed makes the run deterministic anyway.</p>
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void wgen042_urchin_spawner_enable_gate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean prior = OreSpawnConfig.URCHIN_ENABLE.get();
        try {
            Method tryPlace = CrystalStructures.class.getDeclaredMethod("tryPlaceUrchinSpawner",
                    WorldGenLevel.class, RandomSource.class, int.class, int.class, BlockState.class);
            tryPlace.setAccessible(true);

            BlockPos far = farPos(helper, 103);
            int chunkX = far.getX() & ~15;
            int chunkZ = far.getZ() & ~15;
            BlockState grass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
            for (int x = 0; x <= 15; x++) {
                for (int z = 0; z <= 15; z++) {
                    level.setBlock(new BlockPos(chunkX + x, 60, chunkZ + z), grass, 2);
                }
            }

            OreSpawnConfig.URCHIN_ENABLE.set(false);
            RandomSource random = new XoroshiroRandomSource(0x0BC1EA7L);
            for (int i = 0; i < 4000; i++) {
                boolean placed = (Boolean) tryPlace.invoke(null, level, random, chunkX, chunkZ, grass);
                helper.assertFalse(placed, "urchin spawner placed with urchinEnable=false (WGEN-042)");
            }
            for (int x = -2; x <= 17; x++) {
                for (int z = -2; z <= 17; z++) {
                    for (int y = 61; y <= 66; y++) {
                        helper.assertFalse(level.getBlockState(new BlockPos(chunkX + x, y, chunkZ + z)).is(Blocks.SPAWNER),
                                "spawner block appeared with urchinEnable=false");
                    }
                }
            }

            OreSpawnConfig.URCHIN_ENABLE.set(true);
            random = new XoroshiroRandomSource(0x0BC1EA8L);
            boolean built = false;
            for (int i = 0; i < 5000 && !built; i++) {
                built = (Boolean) tryPlace.invoke(null, level, random, chunkX, chunkZ, grass);
            }
            helper.assertTrue(built, "urchin spawner never placed with urchinEnable=true "
                    + "(P(false failure) = (179/180)^5000 ~ 8.5e-13)");
            int spawners = 0;
            for (int x = 0; x <= 15; x++) {
                for (int z = 0; z <= 15; z++) {
                    for (int y = 61; y <= 66; y++) {
                        if (level.getBlockState(new BlockPos(chunkX + x, y, chunkZ + z)).is(Blocks.SPAWNER)) {
                            spawners++;
                        }
                    }
                }
            }
            helper.assertTrue(spawners >= 1,
                    "urchin spawner column missing after successful placement (orig GenericDungeon.java:2578-2636)");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("reflection into CrystalStructures.tryPlaceUrchinSpawner failed", e);
        } finally {
            OreSpawnConfig.URCHIN_ENABLE.set(prior);
        }
        helper.succeed();
    }

    // =====================================================================
    // disableOverworldDungeons gate (i167-disableoverworlddungeons-gate-wgen-064)
    // =====================================================================

    /** The 11 gated overworld dungeon types (LegacyDungeonStructure.java:58-70; orig OreSpawnWorld.java:284-321). */
    private static final String[] OVERWORLD_DUNGEON_STRUCTURES = {
            "play_pool", "water_dragon_lair", "gold_fish_bowl", "girlfriend_island", "monster_island",
            "frog_pond", "haunted_house", "leaf_monster_dungeon", "spit_bug_lair", "bouncy_castle",
            "rubber_ducky_pond"};

    /**
     * The subset whose anchors (SWAMP_GRASS_SURFACE / SAND_SURFACE_MINUS1,
     * surface window Y 41..100 over a dry column) succeed against the End
     * generator's central-island terrain, giving a deterministic gate-off
     * positive control. The five ocean-anchored types need a water column and
     * can produce no generation point on any generator available to the
     * headless flat gametest server; for them only the gate-on ⇒ empty half
     * (the item's stated expectation) is asserted.
     */
    private static final String[] LAND_ANCHORED_CONTROLS = {
            "frog_pond", "haunted_house", "leaf_monster_dungeon", "spit_bug_lair", "bouncy_castle",
            "rubber_ducky_pond"};

    private static Structure.GenerationContext generationContext(ServerLevel terrainLevel, ChunkPos chunkPos) {
        MinecraftServer server = terrainLevel.getServer();
        return new Structure.GenerationContext(
                server.registryAccess(),
                terrainLevel.getChunkSource().getGenerator(),
                terrainLevel.getChunkSource().getGenerator().getBiomeSource(),
                terrainLevel.getChunkSource().randomState(),
                server.getStructureManager(),
                terrainLevel.getSeed(),
                chunkPos,
                terrainLevel,
                biome -> true);
    }

    /** Ring search (radius 0..maxRadius) for a chunk where the structure yields a generation point. */
    private static ChunkPos findWitnessChunk(Structure structure, ServerLevel terrainLevel, int maxRadius) {
        for (int r = 0; r <= maxRadius; r++) {
            for (int cx = -r; cx <= r; cx++) {
                for (int cz = -r; cz <= r; cz++) {
                    if (Math.max(Math.abs(cx), Math.abs(cz)) != r) {
                        continue;
                    }
                    ChunkPos chunkPos = new ChunkPos(cx, cz);
                    if (structure.findValidGenerationPoint(generationContext(terrainLevel, chunkPos)).isPresent()) {
                        return chunkPos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checklist item i167-disableoverworlddungeons-gate-wgen-064 (WGEN-064):
     * {@code disableOverworldDungeons=true} makes {@code findGenerationPoint}
     * return empty for all 11 gated overworld dungeon types, while the Random
     * Dungeon Spawner / buildNow path still builds.
     *
     * <p>Documented mechanism: orig OreSpawnWorld.java:284 gated the entire
     * overworld dungeon dispatch (:285-321); port LegacyDungeonStructure.java:
     * 97-105 (the D6b close-out fix — the config existed but was read
     * nowhere). The DSB path was never gated (orig DungeonSpawnerBlock.java:
     * 197-199), so with the gate held ON,
     * {@code RandomDungeonSpawnerBlockEntity.buildForType} (the sanctioned
     * public seam) must still build type 5 = HAUNTED_HOUSE
     * (RandomDungeonSpawnerBlockEntity.java:111) at far offset K=104; the
     * haunted house signature is its 3-high center spawner stack plus the
     * furnace/crafting-table furniture row (orig GenericDungeon.java:891-1010;
     * port HauntedHouseGenerator).</p>
     *
     * <p>Positive control: with the gate off, the six land-anchored types must
     * yield a generation point on the End generator's central island (surface
     * inside the anchors' Y 41..100 window; the flat overworld surface is not).
     * Gate-on assertions then reuse the exact witness chunks. Seed is the
     * fixed gametest-server seed 0 (GameTestServer WORLD_OPTIONS) — fully
     * deterministic.</p>
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void wgen064_disable_overworld_dungeons_gate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        ServerLevel end = server.getLevel(Level.END);
        helper.assertTrue(end != null, "End dimension missing on the gametest server");
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        boolean prior = OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS.get();
        try {
            OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS.set(false);
            Map<String, ChunkPos> witnesses = new HashMap<>();
            for (String name : LAND_ANCHORED_CONTROLS) {
                Structure structure = structures.getOptional(loc(name)).orElse(null);
                helper.assertTrue(structure instanceof LegacyDungeonStructure,
                        name + " is missing or not a LegacyDungeonStructure");
                ChunkPos witness = findWitnessChunk(structure, end, 8);
                helper.assertTrue(witness != null,
                        "gate-off positive control: no generation point for " + name + " within 8 chunks of the End origin");
                witnesses.put(name, witness);
            }

            OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS.set(true);
            for (String name : OVERWORLD_DUNGEON_STRUCTURES) {
                Structure structure = structures.getOptional(loc(name)).orElse(null);
                helper.assertTrue(structure != null, "missing structure " + name);
                ChunkPos chunkPos = witnesses.getOrDefault(name, new ChunkPos(0, 0));
                helper.assertTrue(structure.findValidGenerationPoint(generationContext(end, chunkPos)).isEmpty(),
                        "disableOverworldDungeons=true but " + name
                                + " still yields a generation point at " + chunkPos + " (WGEN-064)");
            }

            // DSB / buildNow path is not gated (orig DungeonSpawnerBlock.java:197-199).
            BlockPos dsbSite = farPos(helper, 104).atY(0);
            boolean built = RandomDungeonSpawnerBlockEntity.buildForType(level, dsbSite,
                    5 /* TYPE_HAUNTED_HOUSE — RandomDungeonSpawnerBlockEntity.java:111 */);
            helper.assertTrue(built, "DSB buildForType(HAUNTED_HOUSE) failed with the gate on");
            int spawners = 0;
            boolean craftingTable = false;
            boolean furnace = false;
            for (int dx = -6; dx <= 6; dx++) {
                for (int dy = -1; dy <= 7; dy++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        BlockState state = level.getBlockState(dsbSite.offset(dx, dy, dz));
                        if (state.is(Blocks.SPAWNER)) {
                            spawners++;
                        } else if (state.is(Blocks.CRAFTING_TABLE)) {
                            craftingTable = true;
                        } else if (state.is(Blocks.FURNACE)) {
                            furnace = true;
                        }
                    }
                }
            }
            helper.assertTrue(spawners >= 3,
                    "haunted-house spawner stack missing at the DSB site (orig GenericDungeon.java:996-1006)");
            helper.assertTrue(craftingTable && furnace,
                    "haunted-house furniture row missing at the DSB site (orig GenericDungeon.java:944-946)");
        } finally {
            OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS.set(prior);
        }
        helper.succeed();
    }

    // =====================================================================
    // spiderDriverEnable worldgen gate (parent-directed; automatable slice of
    // i164-spiderhangout — the structure content half belongs to that item)
    // =====================================================================

    /**
     * Spider Hangout worldgen honors {@code spiderDriverEnable}: with the flag
     * off, {@code findGenerationPoint} returns empty; on, it yields a point.
     *
     * <p>Documented mechanism: orig OreSpawnWorld.java:1323-1325
     * ({@code if (SpiderDriverEnable == 0) return false;}); port
     * LegacyDungeonStructure.java:88-96. The buildNow/DSB path is untouched by
     * the gate, which the WGEN-064 test above already exercises structurally.
     * Positive control uses the End generator's central island (the
     * VILLAGE_GRASS_SURFACE anchor wants a dry surface in Y 41..100 plus a
     * clear 20x20 footprint), fixed server seed — deterministic.</p>
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void spider_driver_enable_worldgen_gate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerLevel end = level.getServer().getLevel(Level.END);
        helper.assertTrue(end != null, "End dimension missing on the gametest server");
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure hangout = structures.getOptional(loc("spider_hangout")).orElse(null);
        helper.assertTrue(hangout instanceof LegacyDungeonStructure,
                "spider_hangout is missing or not a LegacyDungeonStructure");

        boolean prior = OreSpawnConfig.SPIDER_DRIVER_ENABLE.get();
        try {
            OreSpawnConfig.SPIDER_DRIVER_ENABLE.set(true);
            ChunkPos witness = findWitnessChunk(hangout, end, 10);
            helper.assertTrue(witness != null,
                    "gate-off positive control: no spider_hangout generation point within 10 chunks of the End origin");

            OreSpawnConfig.SPIDER_DRIVER_ENABLE.set(false);
            helper.assertTrue(hangout.findValidGenerationPoint(generationContext(end, witness)).isEmpty(),
                    "spiderDriverEnable=false but spider_hangout still yields a generation point "
                            + "(orig OreSpawnWorld.java:1323-1325)");
        } finally {
            OreSpawnConfig.SPIDER_DRIVER_ENABLE.set(prior);
        }
        helper.succeed();
    }

    // =====================================================================
    // Boss spawner enable gates (parent-directed; automatable slice of
    // i099-boss-005-012 — the enabled-path spawn/sound half stays manual)
    // =====================================================================

    /**
     * King Spawner with {@code theKingEnable=false}: the block (and the block
     * above) still consume themselves after the 100-tick fuse, but no King
     * spawns.
     *
     * <p>Documented mechanism: orig KingSpawnerBlock.java:51-56 (100-tick
     * fuse on placement), :62-71 (block + block above become air even when the
     * enable gate suppresses the spawn, gate orig :66 = TheKingEnable, orig
     * OreSpawnMain.java:6434); port BossSpawnerBlock.java:69-101 +
     * OreSpawnConfig.THE_KING_ENABLE (OreSpawnConfig.java:284-286). The config
     * flag is held false across the fuse and restored afterwards; it gates
     * only King-spawner detonation and King natural spawns, which no
     * concurrent test exercises.</p>
     */
    // TEST-003: this test holds a GLOBAL config flag flipped across a
    // 140-tick window and then scans a 40-block radius — in the concurrent
    // default batch, both are interference channels (which 50 tests share a
    // bucket is a function of total test count, so ANY suite growth can hand
    // it a King-spawning neighbor; first detonation 2026-08-11). Own batch.
    @GameTest(template = "empty_large", timeoutTicks = 300, batch = "bossGate005")
    public void boss005_king_spawner_fizzles_when_disabled(GameTestHelper helper) {
        boolean prior = OreSpawnConfig.THE_KING_ENABLE.get();
        OreSpawnConfig.THE_KING_ENABLE.set(false);
        BlockPos spawnerPos = new BlockPos(24, 2, 24);
        helper.setBlock(spawnerPos.above(), Blocks.STONE); // orig :69-70 also clears the block above
        helper.setBlock(spawnerPos, ModBlocks.KING_SPAWNER.get());
        helper.runAfterDelay(140, () -> {
            try {
                helper.assertBlockPresent(Blocks.AIR, spawnerPos);
                helper.assertBlockPresent(Blocks.AIR, spawnerPos.above());
                helper.assertTrue(helper.getEntities(ModEntities.THE_KING.get(), spawnerPos, 40.0).isEmpty(),
                        "TheKing spawned despite theKingEnable=false (BOSS-005)");
            } finally {
                OreSpawnConfig.THE_KING_ENABLE.set(prior);
            }
            helper.succeed();
        });
    }

    /**
     * Queen Spawner with {@code theQueenEnable=false}: fizzles exactly like
     * the King's (orig QueenSpawnerBlock, gate orig OreSpawnMain.java:6435;
     * port BossSpawnerBlock.java:93-101 + OreSpawnConfig.java:287-289).
     */
    // TEST-003: same isolation rationale as boss005 above.
    @GameTest(template = "empty_large", timeoutTicks = 300, batch = "bossGate012")
    public void boss012_queen_spawner_fizzles_when_disabled(GameTestHelper helper) {
        boolean prior = OreSpawnConfig.THE_QUEEN_ENABLE.get();
        OreSpawnConfig.THE_QUEEN_ENABLE.set(false);
        BlockPos spawnerPos = new BlockPos(24, 2, 24);
        helper.setBlock(spawnerPos.above(), Blocks.STONE);
        helper.setBlock(spawnerPos, ModBlocks.QUEEN_SPAWNER.get());
        helper.runAfterDelay(140, () -> {
            try {
                helper.assertBlockPresent(Blocks.AIR, spawnerPos);
                helper.assertBlockPresent(Blocks.AIR, spawnerPos.above());
                helper.assertTrue(helper.getEntities(ModEntities.THE_QUEEN.get(), spawnerPos, 40.0).isEmpty(),
                        "TheQueen spawned despite theQueenEnable=false (BOSS-012)");
            } finally {
                OreSpawnConfig.THE_QUEEN_ENABLE.set(prior);
            }
            helper.succeed();
        });
    }

    /**
     * BOSS-017 — PlayNicely across the giant bosses. Phase 1 (deterministic,
     * constructor-time per orig TheKing.java:85-89 / Godzilla.java:71-75):
     * a King constructed while playNicely=true snapshots the 5.5x6 box, is
     * directly pickable, and serves NO part surfaces; constructed with the
     * flag off it is 22x24, unpickable, with 5 parts. Phase 2 (dynamic per
     * orig TheKing.java:985-988): with playNicely=true a full-size King near
     * a survival player acquires no target and never spawns the KingHead
     * sidecar over 80 ticks (the gate fakes headEntityFound=1, orig quirk).
     *
     * <p>Isolated in its own batch: the flag is GLOBAL and held true for ~80
     * ticks here, which pacifies every PlayNicely consumer — concurrently
     * running default-batch tests (e.g. the Vortex drag-pull test, whose
     * gate TF-035 restored) would fail spuriously inside that window.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 300, batch = "playNicelyIsolation")
    public void boss017_play_nicely_gates(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();

        OreSpawnConfig.PLAY_NICELY.set(true);
        danger.orespawn.entity.TheKing niceKing =
                helper.spawnWithNoFreeWill(ModEntities.THE_KING.get(), new BlockPos(8, 8, 8));
        helper.assertTrue(Math.abs(niceKing.getBbWidth() - 5.5f) < 0.01f
                        && Math.abs(niceKing.getBbHeight() - 6.0f) < 0.01f,
                "nice King must snapshot 5.5x6 (orig TheKing.java:85-89), got "
                        + niceKing.getBbWidth() + "x" + niceKing.getBbHeight());
        helper.assertTrue(niceKing.isPickable(), "nice King is directly hittable (single-box orig)");
        helper.assertTrue(niceKing.getParts().length == 0, "nice King serves no part surfaces");
        niceKing.discard();

        OreSpawnConfig.PLAY_NICELY.set(false);
        danger.orespawn.entity.TheKing meanKing =
                helper.spawnWithNoFreeWill(ModEntities.THE_KING.get(), new BlockPos(8, 8, 8));
        helper.assertTrue(Math.abs(meanKing.getBbWidth() - 22.0f) < 0.01f,
                "full King must be 22 wide (orig TheKing.java:86)");
        helper.assertTrue(!meanKing.isPickable() && meanKing.getParts().length == 5,
                "full King routes damage through its 5 parts");
        meanKing.discard();

        // Phase 2: dynamic pacification of a full-size King.
        danger.orespawn.entity.TheKing king =
                helper.spawn(ModEntities.THE_KING.get(), new BlockPos(24, 8, 24));
        Player bystander = helper.makeMockPlayer(GameType.SURVIVAL);
        bystander.setPos(king.getX() + 6.0, king.getY(), king.getZ());
        OreSpawnConfig.PLAY_NICELY.set(true);

        final int[] ticks = {0};
        helper.onEachTick(() -> {
            if (++ticks[0] < 80) return;
            try {
                helper.assertTrue(king.getTarget() == null,
                        "playNicely King must never acquire a target (orig TheKing.java:985-988)");
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(
                                danger.orespawn.entity.KingHead.class,
                                king.getBoundingBox().inflate(96.0, 64.0, 96.0)).isEmpty(),
                        "playNicely King must not spawn the KingHead sidecar (headEntityFound faked)");
            } finally {
                OreSpawnConfig.PLAY_NICELY.set(prior);
                king.discard();
            }
            helper.succeed();
        });
    }
}
